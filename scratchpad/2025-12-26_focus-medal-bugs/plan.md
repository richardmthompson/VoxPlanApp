# Feature: Fix FocusMode Medal Bugs

## Background and Motivation

Three related bugs in the FocusMode medal system affect user experience when:
1. App is backgrounded/killed and restored (process death recovery)
2. User changes the revolution time setting during a session

These bugs cause incorrect medal counts and timer state, frustrating users who expect accurate time tracking.

## Feature Goal

Fix all three medal-related bugs to ensure accurate medal counting and timer state after process death recovery and revolution time changes.

---

## Pattern Analysis

### Patterns Found

**Pattern A**: `FocusViewModel.kt:247-256` - Timer from timestamp
- **Approach**: Uses `SystemClock.elapsedRealtime()` minus saved `startTimestamp` to calculate elapsed time
- **Pros**: Survives process death (monotonic clock), accurate timing
- **Cons**: `startTimestamp` is never updated after medal awards - tracks total session time, not time since last medal

**Pattern B**: `FocusViewModel.kt:418-424` - Medal award on revolution completion
- **Approach**: When `progress == 1f`, award medal, reset timer, restart
- **Pros**: Clean state machine, plays sound, auto-restarts
- **Cons**: Only works during active timer loop - doesn't handle medals earned during process death

**Pattern C**: `FocusViewModel.kt:109-121` - Save state
- **Approach**: Saves medals, settings, and state to `SavedStateHandle`
- **Pros**: Persists across process death
- **Cons**: Doesn't save "medals already earned count" separately from total elapsed time

### Recommended Pattern

**FOLLOW**: Combination of all three patterns with corrections

**RATIONALE**: The existing patterns are sound but have a fundamental flaw - they conflate "total elapsed time" with "time since last medal award". Need to track medals earned BEFORE process death separately from medals to award AFTER recovery.

---

## Root Cause Analysis

### Bug 1: Triple Medal Count (VoxPlanApp-kgk)

**Location**: `FocusViewModel.kt:161-176`

**Root Cause**: Double-counting medals. The calculation:
```kotlin
val completeRevolutions = (totalElapsedTime / revolutionMillis).toInt()
val existingMedals = restoreMedals()  // Medals already awarded
val newMedals = List(completeRevolutions) { ... }  // ALL revolutions, not just new ones!
val allMedals = existingMedals + newMedals  // Double counts!
```

`completeRevolutions` counts ALL revolutions since timer start, but `existingMedals` already contains medals for SOME of those revolutions. Result: medals counted twice.

**Example**:
- Timer running for 25 minutes with 10-minute revolution
- 2 medals already awarded (for 0-10 and 10-20 minutes)
- Process death occurs at 25 minutes
- On restore:
  - `existingMedals` = 2 medals (saved in state)
  - `totalElapsedTime` = 25 minutes
  - `completeRevolutions` = 25/10 = 2 (should be 0 new, since no medal threshold crossed during death)
  - `newMedals` = 2 medals (WRONG - these were already counted!)
  - Result: 4 medals instead of 2

**Fix**: Calculate `newMedalsEarned` as `completeRevolutions - existingMedals.size`

### Bug 2: Timer Resets to 0 (VoxPlanApp-eey)

**Location**: `FocusViewModel.kt:179` and `FocusViewModel.kt:418-424`

**Root Cause**: Related to Bug 1. When triple medals are incorrectly awarded, the `remainderTime` calculation becomes incorrect:

```kotlin
val remainderTime = totalElapsedTime % revolutionMillis.toLong()
```

If `totalElapsedTime` is 25 minutes and revolution is 10 minutes:
- `remainderTime` = 25 % 10 = 5 minutes (correct)

But if the timer state was at 7 minutes before process death (awaiting 3rd medal at 30 min):
- User expects to see ~7 minutes on timer after restore
- Instead sees 5 minutes (the mathematical remainder)

Actually, looking more carefully, the timer reset issue may also be caused by:
- Lines 418-424: When `progress == 1f`, it calls `resetTimer()` which sets `currentTime = 0L`
- If multiple medals are awarded during recovery, each medal might trigger a reset

**Fix**: After process death recovery, set timer to correct remainder based on actual medals earned, not total revolutions.

### Bug 3: Revolution Change Awards Only 1 Medal (VoxPlanApp-ccs)

**Location**: `FocusViewModel.kt:491-494`

**Root Cause**: `updateClockFaceMinutes()` doesn't calculate medals owed at new setting:

```kotlin
fun updateClockFaceMinutes(minutes: Float) {
    focusUiState = focusUiState.copy(clockFaceMins = minutes)
    saveCurrentState()
}
```

**Example**:
- At 2 minutes elapsed with 3-minute revolution (0 medals earned yet)
- User changes to 1-minute revolution
- At new setting: 2 minutes = 2 complete revolutions = 2 medals owed
- But function doesn't award any medals

**Fix**: Add medal calculation logic when changing revolution time:
1. Get current elapsed time
2. Calculate medals at NEW revolution setting
3. Award those medals
4. Reset timer with remainder

---

## Integration Impact

### Files to Modify (Dependency Order)

1. **FocusViewModel.kt** (primary - all fixes here)

### Shared Dependencies

- `SavedStateHandle` - Need to save "medals count at last save" or equivalent
- `focusUiState.medals` - Read and update
- `savedStateHandle[KEY_MEDALS_VALUES]` / `[KEY_MEDALS_TYPES]` - Already exists

### Test Coverage Plan

- **Manual Tests**:
  1. Start timer, wait for 1+ medals, background, kill, restore - verify correct medal count
  2. Start timer at 2 min elapsed with 3-min revolution, change to 1-min - verify 2 medals awarded
  3. Start timer, let it run past 1 revolution, background/kill/restore - verify timer shows correct remainder

- **Unit Tests** (future):
  - `restoreSavedState()` with various elapsed times and saved medal counts
  - `updateClockFaceMinutes()` medal calculation
  - Edge cases: 0 elapsed time, exact revolution boundary, multiple revolutions during death

### Risks

- Changing medal calculation logic could affect normal (non-process-death) medal awards
- Need to ensure backward compatibility with existing saved states

---

## Implementation Steps (Dependency-Ordered)

**Step 1: FIX Bug 1 - Triple Medal Count**

**File**: `FocusViewModel.kt:161-176`

**Action**: Change medal calculation to only award NEW medals

```kotlin
// BEFORE (buggy):
val completeRevolutions = (totalElapsedTime / revolutionMillis).toInt()
val existingMedals = restoreMedals()
val newMedalsEarned = completeRevolutions  // WRONG
val newMedals = List(newMedalsEarned) { Medal(...) }
val allMedals = existingMedals + newMedals

// AFTER (fixed):
val completeRevolutions = (totalElapsedTime / revolutionMillis).toInt()
val existingMedals = restoreMedals()
val newMedalsEarned = (completeRevolutions - existingMedals.size).coerceAtLeast(0)
val newMedals = List(newMedalsEarned) { Medal(...) }
val allMedals = existingMedals + newMedals
```

**Validation**: Background app after earning 1 medal, kill, restore after another revolution - should show exactly 2 medals

---

**Step 2: FIX Bug 2 - Timer Reset**

**File**: `FocusViewModel.kt:179`

**Action**: The `remainderTime` calculation is actually correct mathematically. The issue is that `totalElapsedTime` includes time already accounted for by saved medals.

Need to adjust by subtracting time already accounted for:

```kotlin
// Calculate remainder based on time AFTER last medal
val timeAccountedForByMedals = existingMedals.size * revolutionMillis.toLong()
val timeSinceLastMedal = totalElapsedTime - timeAccountedForByMedals
val additionalCompleteRevolutions = (timeSinceLastMedal / revolutionMillis).toInt()
val remainderTime = timeSinceLastMedal % revolutionMillis.toLong()
```

But actually, simplest fix: use `(totalElapsedTime % revolutionMillis)` which gives correct remainder regardless of medal count.

The issue may be in how `startTimestamp` is being managed. Let me trace:
- `startTimestamp` is set in `startTimer()` (line 315)
- It's read in `startTimerJobFromTimestamp()` (line 250)
- After medal award in `updateTimerState()` (lines 418-424), `resetTimer()` is called, then `startTimer()`
- `startTimer()` sets NEW `startTimestamp` (line 310-311)

So the issue is: after process death recovery, if we calculate medals and then call `startTimerJobFromTimestamp(startTimestamp)`, we're using the ORIGINAL timestamp, not accounting for the medals just awarded.

**Fix**: After awarding process-death medals, update `startTimestamp` to account for medal time:

```kotlin
// After awarding new medals during restore:
val newStartTimestamp = startTimestamp + (allMedals.size * revolutionMillis.toLong())
savedStateHandle[KEY_START_TIMESTAMP] = newStartTimestamp
startTimerJobFromTimestamp(newStartTimestamp)
```

**Validation**: After restore, timer should show correct partial progress, not 0

---

**Step 3: FIX Bug 3 - Revolution Change Medal Award**

**File**: `FocusViewModel.kt:491-494`

**Action**: Add medal calculation when changing revolution time

```kotlin
fun updateClockFaceMinutes(newMinutes: Float) {
    val oldMinutes = focusUiState.clockFaceMins

    // Only process if timer is running and minutes changed
    if (focusUiState.timerState == TimerState.RUNNING && newMinutes != oldMinutes) {
        // Calculate elapsed time
        val startTimestamp = savedStateHandle.get<Long>(KEY_START_TIMESTAMP) ?: return
        val elapsedMillis = SystemClock.elapsedRealtime() - startTimestamp

        // Calculate medals at NEW revolution setting
        val newRevolutionMillis = newMinutes * 60000f
        val medalsEarned = (elapsedMillis / newRevolutionMillis).toInt()

        // Award medals
        repeat(medalsEarned) {
            awardMedal(Medal(newMinutes.toInt(), MedalType.MINUTES))
        }

        // Calculate remainder and reset timer
        val remainderMillis = elapsedMillis % newRevolutionMillis.toLong()

        // Update start timestamp to reflect medals awarded
        val newStartTimestamp = SystemClock.elapsedRealtime() - remainderMillis
        savedStateHandle[KEY_START_TIMESTAMP] = newStartTimestamp

        // Update current time to show remainder
        focusUiState = focusUiState.copy(
            currentTime = remainderMillis,
            clockFaceMins = newMinutes
        )
    } else {
        focusUiState = focusUiState.copy(clockFaceMins = newMinutes)
    }

    saveCurrentState()
}
```

**Validation**: At 2 min elapsed with 3-min setting, change to 1-min → should award 2 medals, timer shows 0

---

## Success Definition

### Functional Success
- [ ] Process death recovery awards correct medal count (not 3x)
- [ ] Timer shows correct remainder after process death recovery
- [ ] Changing revolution time awards correct number of medals for elapsed time
- [ ] Normal operation (no process death) continues to work correctly

### Technical Success
- [ ] Build passes (`./gradlew assembleDebug`)
- [ ] No regression in timer accuracy during normal use
- [ ] SavedStateHandle properly persists all required state

### Validation Commands
```bash
./gradlew assembleDebug
./gradlew installDebug
# Manual testing on device/emulator
```

---

## Notes

**Confidence**: High

**Pattern Source**: Existing FocusViewModel patterns for timer management

**Key Insight**: The fundamental issue is conflating "total elapsed time since timer start" with "time since last medal award". The fix requires tracking these separately or calculating the difference correctly.

**Beads Issues**:
- VoxPlanApp-kgk: Triple medal bug
- VoxPlanApp-eey: Timer reset bug
- VoxPlanApp-ccs: Revolution change bug
