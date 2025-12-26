# Fix Focus Mode Dailies Duplication Bug

**Date**: 2025-12-19
**Tier**: 1 (Simple)
**Confidence**: High

---

## Background and Motivation

### VoxPlanApp Architecture Overview

VoxPlanApp is an Android productivity app with hierarchical goal management, time tracking, daily quotas, scheduling, and gamified focus sessions. The app uses:
- **MVVM architecture** with manual dependency injection
- **Room database** for local persistence
- **Jetpack Compose** for UI
- **Kotlin Flow/StateFlow** for reactive data

### The Parent-Child Event System

The app uses a single `Event` entity for two distinct purposes, distinguished by a `parentDailyId` field:

1. **Parent Dailies** (`parentDailyId = null`):
   - Unscheduled daily tasks that appear in the "Dailies" screen
   - Created explicitly by the user (e.g., "Work on Programming" for today)
   - Do NOT have specific start/end times initially
   - Represent tasks the user wants to accomplish today

2. **Scheduled Child Events** (`parentDailyId = <parent_id>`):
   - Time-blocked versions of parent dailies
   - Appear in the day scheduler (calendar view)
   - HAVE specific start/end times (e.g., "9:00 AM - 10:30 AM")
   - Linked to a parent daily via `parentDailyId` foreign key

**Example workflow**:
```
1. User creates parent daily: "Work on Programming" (parentDailyId = null)
   → Shows in Dailies screen

2. User drags task to day scheduler, sets time 9:00-10:30
   → Creates child event: "Work on Programming" (parentDailyId = parent.id, startTime = 9:00, endTime = 10:30)
   → Shows in DaySchedule screen

3. User clicks scheduled event → enters Focus Mode
   → Timer runs, tracks time
   → On exit, UPDATES the existing child event (doesn't create new one)
```

### Focus Mode Entry Points

Users can enter Focus Mode through two different paths:

**Path 1: From MainScreen (ad-hoc focus)**
- User browses goals hierarchy
- Clicks on a goal directly
- Focus Mode starts tracking time
- `focusUiState.isFromEvent = false` (no associated scheduled event)

**Path 2: From DaySchedule (scheduled focus)**
- User views day calendar
- Clicks on a scheduled event (child event with parentDailyId set)
- Focus Mode loads that event's details
- `focusUiState.isFromEvent = true` (associated with scheduled event)

### Time Tracking Architecture

The app has TWO separate time tracking systems:

1. **TimeBank** (`TimeBankRepository`):
   - Tracks accumulated time per goal per date
   - Used for progress tracking and quota fulfillment
   - Entries created when user "banks" earned medals in Focus Mode
   - Source of truth for "how much time did I work on this goal?"

2. **Events** (`EventRepository`):
   - Tracks scheduled time blocks (calendar entries)
   - Used for planning and schedule visualization
   - NOT meant for ad-hoc focus sessions
   - Source of truth for "when am I scheduled to work?"

### The Dailies Screen Query

The Dailies screen shows tasks the user wants to accomplish today. It queries the database like this:

```sql
SELECT * FROM Event
WHERE startDate = :date
AND parentDailyId IS NULL  -- Only parent dailies, not scheduled children
ORDER BY `order`
```

**Critical insight**: Any Event with `parentDailyId = null` will appear as a parent daily in this list.

### The Bug: Unintended Parent Dailies

**Current broken behavior**:

Every time a user enters Focus Mode (Path 1 - ad-hoc), the app creates Event entries with `parentDailyId = null`:

1. **During Focus Mode**: User accumulates "medals" (time increments)
2. **User clicks "Bank Time"**: `bankTime()` function runs
   - ✅ Creates TimeBank entry (correct - tracks time)
   - ❌ **BUG**: Also creates Event with `parentDailyId = null` (wrong - creates unwanted daily)
3. **User exits Focus Mode**: `createOrUpdateEvent()` function runs
   - If >10 minutes spent AND `isFromEvent = false`:
     - ❌ **BUG**: Creates Event with `parentDailyId = null` (wrong - creates unwanted daily)

**Result**: Dailies screen gets polluted with auto-generated entries that were never explicitly created by the user.

**Example scenario**:
```
User's day:
- 9:00 AM: Opens app, enters Focus Mode for "Programming" → Exits after 15 mins
  → BUG: Creates parent daily "Programming" (unintended)

- 11:00 AM: Enters Focus Mode for "Programming" again → Banks 30 mins of medals
  → BUG: Creates another parent daily "Programming" (unintended)

- 2:00 PM: Enters Focus Mode for "Writing" → Exits after 20 mins
  → BUG: Creates parent daily "Writing" (unintended)

Dailies screen now shows 3 duplicate entries the user never explicitly created!
```

### Why This Is Wrong (Current Implementation Issues)

**Likely original intent**: Create Events retroactively to show what the user actually did in the day scheduler - a "post-log" of actual time spent, so users can see their day in retrospect.

**Why current implementation fails**:

1. **Pollutes Dailies UI**: Events appear in Dailies list (where users plan their day), not just in DaySchedule (where they view their calendar)
2. **Creates entries even with no meaningful time**: If user enters Focus Mode and doesn't log time, or logs <10 minutes, an entry may still be created
3. **No distinction between planned vs actual**: There's no way to visually distinguish between:
   - Tasks the user explicitly added to their Dailies (planning)
   - Tasks auto-created from Focus Mode sessions (retrospective logging)
4. **Multiple entries for same goal**: If user does 3 separate focus sessions on "Programming", Dailies shows 3 entries

### Root Cause

Two functions in `FocusViewModel.kt` create Events without setting `parentDailyId`, which defaults to `null`, causing them to appear as "parent daily tasks" in the `getDailiesForDate()` query:

1. **`bankTime()`** (lines 470-483): Creates Event every time user banks medals
2. **`createOrUpdateEvent()`** (lines 524-538): Creates Event when user exits from ad-hoc focus session (>10 mins, `isFromEvent = false`)

**The core issue**: These auto-created Events use `parentDailyId = null`, making them indistinguishable from user-created parent dailies.

### Solution Options

**Option A: Simple Fix (Proposed in this plan)**
- Remove auto-Event creation entirely
- Keep TimeBank for time tracking
- Events only for explicitly scheduled blocks
- **Pros**: Simple, clean separation of concerns, fixes bug immediately
- **Cons**: Loses retrospective "what did I actually do" feature

**Option B: Enhanced Retrospective Logging (Future Feature)**
- Add `visibility` or `isUserCreated` Boolean flag to Event entity
- Modify Dailies query to filter: `WHERE parentDailyId IS NULL AND isUserCreated = true`
- Auto-create Events from Focus Mode with `isUserCreated = false`
- Only create Events if meaningful time logged (e.g., ≥30 mins for calendar visibility)
- **Pros**: Preserves retrospective logging, keeps Dailies clean
- **Cons**: Requires database migration, more complex implementation, needs UI differentiation in DaySchedule

**Option C: Hybrid Approach**
- Implement Option A now (quick fix)
- Plan Option B as separate feature enhancement
- **Pros**: Fixes immediate bug, allows thoughtful design of retrospective feature
- **Cons**: Two-phase implementation

**This plan implements Option A** (simple fix) to immediately resolve the Dailies pollution bug. If retrospective logging is desired, recommend implementing Option B as a separate, well-designed feature with proper schema changes and UI/UX considerations.

### If Implementing Option B (Future Enhancement)

**Database Schema Changes Needed**:
```kotlin
@Entity(tableName = "Event")
data class Event(
    // ... existing fields ...
    val parentDailyId: Int? = null,
    val isUserCreated: Boolean = true,  // NEW FIELD - default true for backward compatibility
    val isVisible: Boolean = true        // NEW FIELD - control Dailies visibility
)
```

**Migration Required**:
```kotlin
val MIGRATION_13_14 = object : Migration(13, 14) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE Event ADD COLUMN isUserCreated INTEGER NOT NULL DEFAULT 1")
        database.execSQL("ALTER TABLE Event ADD COLUMN isVisible INTEGER NOT NULL DEFAULT 1")
    }
}
```

**Updated Dailies Query**:
```kotlin
@Query("""
    SELECT * FROM Event
    WHERE startDate = :date
    AND parentDailyId IS NULL
    AND isVisible = 1  -- Only show visible entries
    ORDER BY `order`
""")
fun getDailiesForDate(date: LocalDate): Flow<List<Event>>
```

**Updated Focus Mode Logic**:
```kotlin
// In createOrUpdateEvent() - only create if ≥30 mins for calendar visibility
private fun createOrUpdateEvent(): Boolean {
    val minutesSpent = ChronoUnit.MINUTES.between(startTime, endTime)
    if (minutesSpent < 30) return false  // Minimum threshold for calendar visibility

    if (!focusUiState.isFromEvent) {
        // Create auto-logged event (hidden from Dailies, visible in DaySchedule)
        val newEvent = Event(
            goalId = goalUiState?.goal?.id ?: return@launch,
            // ... other fields ...
            isUserCreated = false,  // Auto-created
            isVisible = false       // Hidden from Dailies
        )
        eventRepository.insertEvent(newEvent)
    }
}
```

**UI/UX Considerations**:
- DaySchedule should visually distinguish user-created vs auto-logged events (e.g., different opacity, dashed border)
- Consider aggregating multiple short Focus Mode sessions into single Event if on same goal
- Add setting to enable/disable retrospective logging
- Consider showing auto-logged events in a separate "Actuals" section vs "Planned"

---

## Feature Goal

Remove automatic Event creation from Focus Mode that pollutes Dailies list, preserving TimeBank tracking for ad-hoc sessions and Event updates for scheduled sessions.

---

## Context

### Affected Files
- `app/src/main/java/com/voxplanapp/ui/focusmode/FocusViewModel.kt:462-496` (bankTime function)
- `app/src/main/java/com/voxplanapp/ui/focusmode/FocusViewModel.kt:506-541` (createOrUpdateEvent function)

### Pattern to Follow
- **Codebase**: `app/src/main/java/com/voxplanapp/ui/daily/DailyViewModel.kt:147` - Proper `parentDailyId` usage when creating scheduled child events
- **Architecture**: CLAUDE.md:399-404 - Parent-Child Event Pattern specification

### Parent-Child Event Pattern (from CLAUDE.md)
```kotlin
// Parent Daily: parentDailyId = null, startTime/endTime = null
val daily = Event(goalId = 5, title = "Programming", startDate = today, parentDailyId = null)

// Scheduled Child: parentDailyId = dailyId, startTime/endTime populated
val scheduled = Event(goalId = 5, title = "Programming", startDate = today,
    parentDailyId = daily.id, startTime = LocalTime.of(9, 0), endTime = LocalTime.of(10, 0))
```

### Proper Event Creation Pattern (from DailyViewModel.kt:147)
```kotlin
fun scheduleTask(task: Event, startTime: LocalTime, endTime: LocalTime) {
    // create new scheduled event block
    val scheduledTask = task.copy(
        startTime = startTime,
        endTime = endTime,
        parentDailyId = task.id,  // ← Sets parentDailyId correctly
        quotaDuration = duration
    )
    eventRepository.insertEvent(scheduledTask)
}
```

### Query Filter (from EventDao.kt:19-25)
```kotlin
@Query("""
    SELECT * FROM Event
    WHERE startDate = :date
    AND parentDailyId IS NULL  // ← Only shows parent dailies
    ORDER BY `order`
""")
fun getDailiesForDate(date: LocalDate): Flow<List<Event>>
```

### Known Gotchas
1. **TimeBank is source of truth**: Time tracking already handled by `timeBankRepository.addTimeBankEntry()` on line 468 - Event creation in `bankTime()` is redundant
2. **State flag controls behavior**: `focusUiState.isFromEvent` (line 696) determines if user came from scheduled event or ad-hoc Focus Mode entry
3. **No migration needed**: Only changing application logic, not database schema
4. **Existing Events unaffected**: Only preventing future auto-creation, not deleting existing data

### Integration Points
- **TimeBank**: FocusViewModel.kt:468 - Already tracks time for medals
- **Event updates**: FocusViewModel.kt:515-523 - Updates existing scheduled events correctly
- **Dailies query**: EventDao.kt:19-25 - Filters by `parentDailyId IS NULL`
- **Progress tracking**: ProgressViewModel (not modified) - Reads from TimeBank

---

## Implementation Steps

### Step 1: Remove Event Creation from bankTime()
**File**: `app/src/main/java/com/voxplanapp/ui/focusmode/FocusViewModel.kt`
**Lines to DELETE**: 470-483

**Before** (lines 462-496):
```kotlin
fun bankTime() {
    val medalTime = focusUiState.medals.sumOf { it.value }

    if (medalTime > 0) {
        viewModelScope.launch {
            val goalId = goalUiState?.goal?.id ?: return@launch
            timeBankRepository.addTimeBankEntry(goalId, medalTime)  // ← Keep this

            // Create an event for the banked time session  ← DELETE THIS BLOCK START
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
            )
            eventRepository.insertEvent(event)  // ← DELETE THIS BLOCK END
        }

        // clear medals  ← Keep everything below
        focusUiState = focusUiState.copy(medals = emptyList())
        soundPlayer.playSound(R.raw.chaching)
        // ... rest of function
    }
}
```

**After**:
```kotlin
fun bankTime() {
    val medalTime = focusUiState.medals.sumOf { it.value }

    if (medalTime > 0) {
        viewModelScope.launch {
            val goalId = goalUiState?.goal?.id ?: return@launch
            // TimeBank is source of truth for ad-hoc focus sessions
            timeBankRepository.addTimeBankEntry(goalId, medalTime)
        }

        // clear medals
        focusUiState = focusUiState.copy(medals = emptyList())
        soundPlayer.playSound(R.raw.chaching)
        // ... rest of function
    }
}
```

### Step 2: Remove Ad-hoc Event Creation from createOrUpdateEvent()
**File**: `app/src/main/java/com/voxplanapp/ui/focusmode/FocusViewModel.kt`
**Lines to DELETE**: 524-538 (else block)
**Lines to ADD**: Comment explaining TimeBank usage

**Before** (lines 506-541):
```kotlin
private fun createOrUpdateEvent(): Boolean {
    val startTime = focusUiState.startTime ?: return false
    val endTime = focusUiState.endTime ?: return false

    val minutesSpent = ChronoUnit.MINUTES.between(startTime, endTime)
    if (minutesSpent < 10) return false

    viewModelScope.launch {
        if (focusUiState.isFromEvent) {
            // Update existing event  ← Keep this block
            eventUiState?.let { existingEvent ->
                val updatedEvent = existingEvent.copy(
                    startTime = startTime,
                    endTime = endTime
                )
                eventRepository.updateEvent(updatedEvent)
            }
        } else {  // ← DELETE ELSE BLOCK START
            // Create new event
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
            )
            eventRepository.insertEvent(newEvent)  // ← DELETE ELSE BLOCK END
        }
        // Note: Ad-hoc focus sessions tracked via TimeBank only
    }
    return true
}
```

**After**:
```kotlin
private fun createOrUpdateEvent(): Boolean {
    val startTime = focusUiState.startTime ?: return false
    val endTime = focusUiState.endTime ?: return false

    val minutesSpent = ChronoUnit.MINUTES.between(startTime, endTime)
    if (minutesSpent < 10) return false

    viewModelScope.launch {
        if (focusUiState.isFromEvent) {
            // Update existing scheduled event
            eventUiState?.let { existingEvent ->
                val updatedEvent = existingEvent.copy(
                    startTime = startTime,
                    endTime = endTime
                )
                eventRepository.updateEvent(updatedEvent)
            }
        }
        // Note: Ad-hoc focus sessions (not from scheduled event) are tracked
        // via TimeBank only. Events are reserved for explicitly scheduled blocks.
    }
    return true
}
```

### Step 3: Build and Test
**Commands**:
```bash
# Build to verify no compilation errors
./gradlew assembleDebug

# Optional: Run tests (note: currently only example tests exist)
./gradlew test
```

### Step 4: Manual Testing Checklist
1. **Test ad-hoc Focus Mode from MainScreen**:
   - Navigate MainScreen → Select goal → FocusMode
   - Work for 15 minutes
   - Exit Focus Mode
   - Check Dailies screen → Should NOT show auto-created entry
   - Check ProgressViewModel → TimeBank should still update

2. **Test banking medals**:
   - Enter Focus Mode from MainScreen
   - Accumulate 60 minutes of medals
   - Click "Bank Time" button
   - Check Dailies screen → Should NOT show auto-created entry
   - Verify TimeBank entry created

3. **Test scheduled event update**:
   - Navigate DaySchedule → Select scheduled event → FocusMode
   - Work for 20 minutes (different from scheduled time)
   - Exit Focus Mode
   - Check DaySchedule → Event should be updated with new times
   - Event should remain a child (parentDailyId set correctly)

4. **Test boundary conditions**:
   - Focus Mode < 10 minutes → No Event created (expected)
   - Focus Mode exactly 10 minutes → Behavior depends on isFromEvent flag
   - Multiple Focus Mode sessions same day → No Dailies pollution

---

## Success Definition

### Functional Requirements
- [ ] Entering Focus Mode from MainScreen does NOT create Dailies entries
- [ ] Banking medals does NOT create Dailies entries
- [ ] TimeBank entries still created correctly (preserved functionality)
- [ ] Scheduled event updates still work when `isFromEvent == true`
- [ ] Build succeeds without compilation errors

### Validation Commands
```bash
./gradlew assembleDebug  # Must succeed
```

### Expected Outcomes
1. **Dailies list clean**: Only explicitly created parent dailies appear
2. **TimeBank preserved**: Ad-hoc focus time still tracked via TimeBank
3. **Scheduled events work**: Events from DaySchedule → FocusMode still update correctly
4. **No data loss**: Existing Events and TimeBank entries unaffected

---

## Implementation Summary

**Files Modified**: 1
- `app/src/main/java/com/voxplanapp/ui/focusmode/FocusViewModel.kt`

**Lines Changed**:
- Deleted: ~29 lines (Event creation blocks)
- Added: ~6 lines (clarifying comments)
- Net: -23 lines

**Risk Level**: Low
- No database migration
- No data deletion
- Preserves TimeBank tracking
- Only prevents future auto-creation

**Estimated Time**: 10 minutes
