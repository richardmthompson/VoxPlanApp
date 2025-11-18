# Task PRP: Fix Delete Dialog Bug in DaySchedule.kt

## Task Overview

Fix the critical bug in `DaySchedule.kt` (lines 110-126) where the delete parent confirmation dialog references an undefined `event` variable. The dialog is currently commented out and non-functional.

## Context

### Problem Statement

The delete dialog code uses an undefined `event` variable in the AlertDialog callbacks:

```kotlin
// Lines 110-126 in DaySchedule.kt (COMMENTED OUT)
showDeleteParentDialog?.let { parentId ->
    AlertDialog(
        onDismissRequest = { viewModel.confirmDeleteChildOnly(event) },  // ❌ 'event' undefined
        confirmButton = {
            TextButton(onClick = { viewModel.confirmDeleteWithParent(event) }) { /* ... */ }  // ❌ 'event' undefined
        },
        dismissButton = {
            TextButton(onClick = { viewModel.confirmDeleteChildOnly(event) }) { /* ... */ }  // ❌ 'event' undefined
        }
    )
}
```

**Root Cause:** The `showDeleteParentDialog` StateFlow was originally designed to hold `Event?` but the dialog implementation attempts to use a non-existent `event` variable instead of extracting it from `showDeleteParentDialog.value`.

### Architecture Context

**ViewModel State:**
- `showDeleteParentDialog: StateFlow<Event?>` - Holds the event to potentially delete
- Currently line 114 in `SchedulerViewModel.kt` is commented out: `// _showDeleteParentDialog.value = event.parentDailyId`
- The flow should hold the **Event object**, not just the parent ID

**Deletion Flow:**
1. User taps delete icon in `EventActions` → calls `onEventDeleted(event)`
2. `SchedulerViewModel.deleteEvent(event)` checks if event has siblings
3. If no siblings exist → should show dialog asking about parent deletion
4. User chooses:
   - **Delete Both** → `confirmDeleteWithParent(event)`
   - **Delete Only This Event** → `confirmDeleteChildOnly(event)`

**Key Files:**
- `app/src/main/java/com/voxplanapp/ui/calendar/DaySchedule.kt` (675 lines) - UI layer
- `app/src/main/java/com/voxplanapp/ui/calendar/SchedulerViewModel.kt` (141 lines) - State management

### Data Model

**Event Entity (Parent-Child Pattern):**
```kotlin
// Parent Daily: parentDailyId = null, startTime/endTime = null
// Scheduled Child: parentDailyId = dailyId, startTime/endTime populated
```

### Existing Patterns

**Similar Dialog Implementation (from codebase search):**
Other screens use AlertDialog with similar patterns - they properly access state values.

**ViewModel Functions (Already Implemented):**
```kotlin
// SchedulerViewModel.kt lines 122-141
fun confirmDeleteWithParent(event: Event) {
    viewModelScope.launch {
        eventRepository.deleteEvent(event.id)
        event.parentDailyId?.let { parentId ->
            eventRepository.deleteEvent(parentId)
        }
        _showDeleteParentDialog.value = null
    }
}

fun confirmDeleteChildOnly(event: Event) {
    viewModelScope.launch {
        eventRepository.deleteEvent(event.id)
        _showDeleteParentDialog.value = null
    }
}

fun cancelDelete() {
    _showDeleteParentDialog.value = null
}
```

### Gotchas

**Issue 1:** StateFlow type mismatch
- **Current:** `showDeleteParentDialog: StateFlow<Event?>`
- **Bug:** Line 114 was trying to set `event.parentDailyId` (Int) instead of `event` (Event)
- **Fix:** Set the entire event object: `_showDeleteParentDialog.value = event`

**Issue 2:** Dialog dismissal behavior
- **Context:** `onDismissRequest` should just dismiss, not delete
- **Fix:** Use `viewModel.cancelDelete()` instead of `confirmDeleteChildOnly(event)`

**Issue 3:** Uncommented code placement
- **Current:** Dialog code is commented at lines 110-126, inside `DaySchedule` composable
- **Fix:** Uncomment and place before the main Column (similar to other dialog patterns)

## Task Breakdown

### PHASE 1: Fix ViewModel Logic

**ACTION** `app/src/main/java/com/voxplanapp/ui/calendar/SchedulerViewModel.kt:114`
- **OPERATION:** Uncomment line 114 and fix assignment
- **CHANGE:**
  ```kotlin
  // FROM:
  //                _showDeleteParentDialog.value = event.parentDailyId

  // TO:
                  _showDeleteParentDialog.value = event
  ```
- **VALIDATE:** Build project - `./gradlew assembleDebug`
- **IF_FAIL:** Check that Event type matches StateFlow<Event?>
- **EXPECTED:** Compilation succeeds, no type errors

### PHASE 2: Fix UI Dialog Code

**ACTION** `app/src/main/java/com/voxplanapp/ui/calendar/DaySchedule.kt:109-129`
- **OPERATION:** Uncomment and refactor AlertDialog to use proper event reference
- **CHANGE:**
  ```kotlin
  // FROM (lines 109-129):
  /*
  // In the screen content...
      showDeleteParentDialog?.let { parentId ->
          AlertDialog(
              onDismissRequest = { viewModel.confirmDeleteChildOnly(event) },
              title = { Text("Delete Event") },
              text = { Text("Would you also like to delete the parent daily task?") },
              confirmButton = {
                  Button(onClick = { viewModel.confirmDeleteWithParent(event) }) {
                      Text("Delete Both")
                  }
              },
              dismissButton = {
                  Button(onClick = { viewModel.confirmDeleteChildOnly(event) }) {
                      Text("Delete Only This Event")
                  }
              }
          )
      }

       */

  // TO:
  showDeleteParentDialog?.let { eventToDelete ->
      AlertDialog(
          onDismissRequest = { viewModel.cancelDelete() },
          title = { Text("Delete Event") },
          text = { Text("This is the only scheduled block for this daily task. Delete the parent daily task too?") },
          confirmButton = {
              Button(onClick = {
                  viewModel.confirmDeleteWithParent(eventToDelete)
              }) {
                  Text("Delete Both")
              }
          },
          dismissButton = {
              Button(onClick = {
                  viewModel.confirmDeleteChildOnly(eventToDelete)
              }) {
                  Text("Keep Daily, Delete Event")
              }
          }
      )
  }
  ```
- **VALIDATE:** Build project - `./gradlew assembleDebug`
- **IF_FAIL:** Check for missing imports (AlertDialog, Button, Text should already be imported)
- **EXPECTED:** Compilation succeeds

### PHASE 3: Integration Testing

**ACTION** Manual testing on device/emulator
- **OPERATION:** Test the complete deletion flow
- **TEST STEPS:**
  1. Navigate to Schedule screen
  2. Create a daily task with quota from Daily screen
  3. Drag-to-schedule a single event from that daily
  4. Select the scheduled event
  5. Tap delete (recycle) icon
  6. **VERIFY:** Dialog appears asking about parent deletion
  7. Test "Delete Both" button → both event and parent daily deleted
  8. Repeat steps 1-5
  9. Test "Keep Daily, Delete Event" button → only scheduled event deleted, daily remains
  10. Test dismiss (tap outside) → no deletion occurs
- **VALIDATE:** All scenarios work as expected
- **IF_FAIL:**
  - Check logcat for errors: `adb logcat -s SchedulerViewModel BasicSchedule`
  - Verify parent-child relationship in database
- **DEBUG STRATEGY:**
  - Add logging in `deleteEvent()` to trace sibling count
  - Log when `_showDeleteParentDialog` is set
  - Verify dialog state flow emission

### PHASE 4: Edge Case Testing

**ACTION** Test edge cases
- **TEST CASES:**
  1. **Multiple Scheduled Events:** Event with siblings should delete immediately without dialog
  2. **Orphan Event:** Event with null parentDailyId should delete without dialog
  3. **Rapid Deletion:** Delete multiple events quickly - ensure dialog doesn't show multiple times
- **VALIDATE:**
  - Only shows dialog when deleting last scheduled child
  - Dialog state resets properly after each interaction
- **IF_FAIL:**
  - Review `getEventsWithParentId()` query in EventRepository
  - Check StateFlow collection in DaySchedule composable
- **ROLLBACK:** If issues found, comment out line 114 in ViewModel and dialog in DaySchedule

## Validation Strategy

### Build Validation
```bash
./gradlew clean assembleDebug
```
**Success Criteria:** No compilation errors

### Runtime Validation
```bash
./gradlew installDebug
adb logcat -s SchedulerViewModel BasicSchedule DaySchedule
```
**Success Criteria:**
- Dialog appears when expected
- No crashes or null pointer exceptions
- Proper state cleanup after dialog dismissal

### Code Quality Checks
```bash
./gradlew lint
```
**Success Criteria:** No new warnings introduced

## Rollback Strategy

### If Phase 1 Fails:
```kotlin
// Re-comment line 114 in SchedulerViewModel.kt
//                _showDeleteParentDialog.value = event
```

### If Phase 2 Fails:
```kotlin
// Re-comment lines 109-129 in DaySchedule.kt
/*
showDeleteParentDialog?.let { eventToDelete ->
    ...
}
*/
```

### If Integration Issues:
- Revert both files using git:
```bash
git checkout app/src/main/java/com/voxplanapp/ui/calendar/SchedulerViewModel.kt
git checkout app/src/main/java/com/voxplanapp/ui/calendar/DaySchedule.kt
```

## Success Criteria

- [ ] Code compiles without errors
- [ ] Dialog appears when deleting the last scheduled event of a daily
- [ ] "Delete Both" removes both event and parent daily
- [ ] "Keep Daily, Delete Event" removes only the scheduled event
- [ ] Dismissing dialog cancels deletion
- [ ] Events with siblings delete immediately without showing dialog
- [ ] No memory leaks or state pollution
- [ ] Lint passes with no new warnings

## Files Modified

1. `app/src/main/java/com/voxplanapp/ui/calendar/SchedulerViewModel.kt` - Uncomment line 114, fix assignment
2. `app/src/main/java/com/voxplanapp/ui/calendar/DaySchedule.kt` - Uncomment and refactor dialog (lines 109-129)

## Dependencies

**No new dependencies required** - All necessary components already exist:
- AlertDialog, Button, Text (Material3) - already imported
- StateFlow collection - already in use
- ViewModel functions - already implemented

## Security Considerations

**Data Integrity:**
- Deletion operations use Room transactions (implicit in suspend functions)
- Parent-child relationships maintained via foreign keys
- No SQL injection risk (using Room DAOs)

**State Management:**
- StateFlow ensures thread-safe state updates
- ViewModelScope ensures proper lifecycle management
- No shared mutable state across composables

## Performance Impact

**Negligible:**
- Dialog is conditionally rendered (only when state is non-null)
- StateFlow collection uses WhileSubscribed strategy (already in use)
- Database queries are indexed (parentDailyId column)

## Notes

- This fix unblocks the dailies feature completion (currently 70% complete)
- After this fix, consider implementing similar parent deletion logic in DailyScreen.kt if needed
- The dialog text emphasizes the parent-child relationship to help users understand the choice
- Future enhancement: Add animation to dialog appearance/dismissal
