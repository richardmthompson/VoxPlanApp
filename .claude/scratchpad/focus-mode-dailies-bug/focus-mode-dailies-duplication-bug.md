# Focus Mode Dailies Duplication Bug - Verification Report

**Task Tier**: Tier 1 (Simple)
**Confidence**: High
**Date**: 2025-12-19

---

## Background

User reported that entering Focus Mode creates duplicate Event entries in the Dailies list. If a user enters Focus Mode 10 times in a day, the Dailies screen shows 10 irrelevant entries.

**Hypothesis**: Two functions in `FocusViewModel.kt` create Events with `parentDailyId = null`, causing them to appear as "parent daily tasks" in the Dailies query.

---

## Verification Results

### ✅ Bug Confirmed - Root Cause Verified

**File**: `app/src/main/java/com/voxplanapp/ui/focusmode/FocusViewModel.kt`

#### Evidence 1: EventDao Query (Lines 19-25)

```kotlin
@Query("""
    SELECT * FROM Event
    WHERE startDate = :date
    AND parentDailyId IS NULL
    ORDER BY `order`
""")
fun getDailiesForDate(date: LocalDate): Flow<List<Event>>
```

**Analysis**: The query correctly filters for Events where `parentDailyId IS NULL` to show parent dailies. Any Event created without a `parentDailyId` will appear in this list.

#### Evidence 2: bankTime() Function (Lines 462-496)

```kotlin
fun bankTime() {
    val medalTime = focusUiState.medals.sumOf { it.value }

    if (medalTime > 0) {
        viewModelScope.launch {
            val goalId = goalUiState?.goal?.id ?: return@launch
            timeBankRepository.addTimeBankEntry(goalId, medalTime)

            // Create an event for the banked time session
            val startTime = focusUiState.startTime ?: LocalTime.now()
            val event = Event(
                goalId = goalId,
                title = goalUiState?.goal?.title ?: "Focused Work",
                startTime = startTime,
                endTime = startTime.plusMinutes(medalTime.toLong()),
                startDate = focusUiState.date ?: LocalDate.now(),
                recurrenceType = RecurrenceType.NONE,
                recurrenceInterval = null,
                recurrenceEndDate = null,
                color = 0
                // NOTE: parentDailyId is NOT set, defaults to null
            )
            eventRepository.insertEvent(event)  // ← BUG: Creates parent daily
        }
        // ... rest of function
    }
}
```

**Problem**: Every time user clicks "Bank Time" button, this creates an Event with `parentDailyId = null`, causing it to appear in Dailies list.

#### Evidence 3: createOrUpdateEvent() Function (Lines 506-541)

```kotlin
private fun createOrUpdateEvent(): Boolean {
    val startTime = focusUiState.startTime ?: return false
    val endTime = focusUiState.endTime ?: return false

    val minutesSpent = ChronoUnit.MINUTES.between(startTime, endTime)
    if (minutesSpent < 10) return false

    viewModelScope.launch {
        if (focusUiState.isFromEvent) {
            // Update existing event (GOOD - no new Event created)
            eventUiState?.let { existingEvent ->
                val updatedEvent = existingEvent.copy(
                    startTime = startTime,
                    endTime = endTime
                )
                eventRepository.updateEvent(updatedEvent)
            }
        } else {
            // Create new event (BUG - no parentDailyId set)
            val newEvent = Event(
                goalId = goalUiState?.goal?.id ?: return@launch,
                title = goalUiState?.goal?.title ?: return@launch,
                startTime = startTime,
                endTime = endTime,
                startDate = LocalDate.now(),
                recurrenceType = RecurrenceType.NONE,
                recurrenceInterval = null,
                recurrenceEndDate = null,
                color = 0
                // NOTE: parentDailyId is NOT set, defaults to null
            )
            eventRepository.insertEvent(newEvent)  // ← BUG: Creates parent daily
        }
    }
    return true
}
```

**Problem**: When user enters Focus Mode from MainScreen (not from an existing scheduled event), and spends >10 minutes, this creates an Event with `parentDailyId = null` on exit, causing it to appear in Dailies list.

**Key State Variable**: `focusUiState.isFromEvent: Boolean` (line 696)
- `true`: User came from existing scheduled event (DaySchedule → FocusMode)
- `false`: User came from goal directly (MainScreen → FocusMode)

---

## Proposed Solution Assessment

### Solution Part 1: Remove Event Creation from bankTime()

**Proposed**: Delete lines 470-483 entirely (Event creation block in bankTime())

**Assessment**: ✅ **CORRECT**

**Rationale**:
1. TimeBank entry is already created on line 468: `timeBankRepository.addTimeBankEntry(goalId, medalTime)`
2. Event creation is **redundant** - TimeBank is the source of truth for banked time
3. The Event created here serves no purpose and only pollutes the Dailies list
4. No side effects - TimeBank tracking is preserved

**Impact**:
- Lines to delete: 470-483 (14 lines)
- No migration needed
- No data loss (TimeBank entries preserved)

### Solution Part 2: Conditional Event Creation in createOrUpdateEvent()

**Proposed**: Only create Events if `focusUiState.isFromEvent == true`

**Assessment**: ⚠️ **PARTIALLY CORRECT - Needs Clarification**

**Analysis**:

Current logic (line 515):
```kotlin
if (focusUiState.isFromEvent) {
    // Update existing event
} else {
    // Create new event ← BUG HERE
}
```

**Option A (Strict)**: Remove `else` block entirely
- Only update existing events, never create new ones
- Assumes Focus Mode should ONLY track time via TimeBank for ad-hoc sessions
- **Trade-off**: Users lose automatic event creation for unplanned focus sessions

**Option B (Permissive)**: Keep `else` block but set `parentDailyId`
- Create Events linked to a parent daily (requires identifying parent)
- Complex: Need to find/create parent daily for today
- **Trade-off**: More complex, but preserves event tracking

**Option C (Hybrid - RECOMMENDED)**: Remove `else` block + document behavior
- Ad-hoc focus sessions → TimeBank only (no Event)
- Scheduled focus sessions → Event update only
- **Rationale**: TimeBank already tracks time; Events are for scheduling only

**Recommendation**: Option C (Hybrid)
- Delete lines 524-538 (else block)
- Add comment explaining TimeBank is source of truth for unplanned sessions
- Preserves scheduled event updates
- Eliminates Dailies pollution

---

## Files Affected

### Modified
- `app/src/main/java/com/voxplanapp/ui/focusmode/FocusViewModel.kt`
  - `bankTime()`: Delete lines 470-483 (~14 lines)
  - `createOrUpdateEvent()`: Delete lines 524-538 (~15 lines)
  - Add clarifying comments (~3 lines)

### Total Changes
- 1 file modified
- ~29 lines deleted
- ~3 lines added (comments)
- Net: -26 lines

---

## Success Criteria

### Functional Requirements
- [ ] Entering Focus Mode from MainScreen does NOT create Dailies entries
- [ ] Banking medals does NOT create Dailies entries
- [ ] TimeBank entries still created correctly (preserved)
- [ ] Scheduled event updates still work (when `isFromEvent == true`)

### Testing Checklist
- [ ] Enter Focus Mode from MainScreen, spend 15 mins, exit → No Dailies entry
- [ ] Enter Focus Mode from MainScreen, bank 60 mins → No Dailies entry, TimeBank updated
- [ ] Enter Focus Mode from DaySchedule scheduled event → Event updated correctly
- [ ] Check Dailies list shows only explicitly created parent dailies

---

## Implementation Steps

1. **Delete bankTime() event creation block**
   - Remove lines 470-483
   - Keep TimeBank entry creation (line 468)
   - Keep medal clearing and sound (lines 486-488)

2. **Delete createOrUpdateEvent() else block**
   - Remove lines 524-538 (event creation for non-scheduled sessions)
   - Keep if-branch (lines 515-523) for scheduled event updates
   - Simplify function logic

3. **Add clarifying comments**
   - Document that TimeBank is source of truth for ad-hoc focus sessions
   - Explain that Events are only for scheduled blocks

4. **Test all scenarios** (see Success Criteria)

---

## Side Effects & Risks

### Low Risk
- **No database migration needed**: Only changing application logic, not schema
- **No data loss**: TimeBank entries preserved, Event creation removed (not deletion)
- **Backwards compatible**: Existing Events unaffected

### Behavioral Changes
- **Before**: Ad-hoc focus sessions created Events appearing in Dailies
- **After**: Ad-hoc focus sessions tracked via TimeBank only, no Dailies pollution

### User Experience Impact
- **Positive**: Dailies list no longer cluttered with irrelevant entries
- **Neutral**: Time still tracked via TimeBank, accessible via ProgressViewModel

---

## Confidence Assessment

**Confidence Level**: **High (9/10)**

**Reasoning**:
- Root cause clearly identified and verified in code
- Solution aligns with existing architecture (TimeBank for time tracking, Events for scheduling)
- Minimal code changes with clear scope
- No architectural impact
- Low risk of regression

**Remaining Uncertainty**:
- Need to verify ProgressViewModel reads TimeBank correctly (likely already does)
- Need to confirm no other code paths depend on these auto-created Events
