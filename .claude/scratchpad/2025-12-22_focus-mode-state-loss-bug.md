# Feature: Fix Focus Mode State Loss Bug

## Background and Motivation

Users report that when the VoxPlanApp is backgrounded for an extended period, the focus mode loses all its state including accrued time that resulted from timer completion. The timer resets entirely, causing users to lose their progress and earned medals. This happens when Android's system kills the app process due to low memory or extended backgrounding, which is normal Android behavior.

**User Impact**: Loss of work and frustration when users return to the app expecting to see their timer progress or earned medals, only to find everything reset to zero.

**Technical Root Cause**: FocusViewModel uses `mutableStateOf` for timer state (line 62: `var focusUiState by mutableStateOf(FocusUiState(isLoading = true))`), which survives configuration changes (rotation) but NOT process death. When Android kills the process, all Compose State values are lost.

---

## Feature Goal

Fix the state loss bug so that users can return to Focus Mode after extended backgrounding and see their timer state restored (elapsed time, timer state, earned medals).

---

## Pattern Analysis

### Current Implementation Problem

**File**: `app/src/main/java/com/voxplanapp/ui/focusmode/FocusViewModel.kt:62-65`

The app currently uses `mutableStateOf` for timer state, which survives configuration changes (rotation) but is completely lost on process death. This is the root cause of the bug - when Android kills the app process due to low memory or extended backgrounding, all timer state resets.

### Solution: SavedStateHandle for State Persistence

**File**: No examples in codebase (currently only used read-only for nav args)
**External Docs**: [Android Save UI States](https://developer.android.com/topic/libraries/architecture/saving-states)

**Approach**: SavedStateHandle is a key-value map in ViewModel that automatically persists to Bundle during Activity onStop. Store essential primitives (currentTime: Long, timerState enum ordinal, timerStarted: Boolean, medals as primitive array) in SavedStateHandle. On ViewModel init, restore state from SavedStateHandle if present. SavedStateHandle survives both configuration changes AND process death. Write to SavedStateHandle whenever critical state changes (timer starts, pauses, medals awarded). Data serializes automatically to Bundle and restores on process recreation.

**Why This Approach**:
- Survives process death (solves the bug)
- Already injected into FocusViewModel constructor (line 40) - zero new dependencies
- Idiomatic Android ViewModel state saving pattern
- Lightweight - critical state fits in ~100 bytes
- Automatic serialization/deserialization

**Tradeoffs**:
- Bundle size limited (1 MB shared across all components) - not an issue for timer state
- Serialization on main thread - negligible for small primitives
- Medal list requires conversion to primitive array (simple IntArray mapping)

### External Documentation

- **SavedStateHandle Official Docs**: [Save UI states - Android Developers](https://developer.android.com/topic/libraries/architecture/saving-states)
- **SavedStateHandle with ViewModel**: [Saved State module for ViewModel](https://developer.android.com/topic/libraries/architecture/viewmodel/viewmodel-savedstate)
- **Compose State Saving**: [Save UI state in Compose](https://developer.android.com/develop/ui/compose/state-saving)

**Gotchas**:
- Bundle size limited to 1 MB shared across all saved state - avoid storing large objects
- Serialization happens on main thread during configuration changes - keep data small
- SavedStateHandle only persists on Activity onStop - rapid updates while running may be lost if killed before stop
- Medal list must be converted to IntArray for Bundle serialization (Medal contains Int value + enum type)

---

## Integration Impact

### Files to Modify (Dependency Order)

1. **FocusViewModel.kt** (foundation - main implementation)
2. **No other files** (self-contained fix in ViewModel)

### Shared Dependencies

- `SavedStateHandle` - Already injected at line 40, currently only reads nav args (goalId, eventId)
- No changes needed to repositories, UI, or navigation
- Medal data class may need Parcelable implementation or convert to primitive array

### Test Coverage Plan

**Currently**: No tests exist for FocusViewModel (only example tests in project)

**After Implementation**:

**Unit Tests** (create new file: `FocusViewModelTest.kt`):
1. Test SavedStateHandle writes when timer starts
2. Test state restoration from SavedStateHandle in ViewModel init
3. Test medal list serialization to IntArray and deserialization
4. Test timer resume calculation with various timerState values (IDLE, RUNNING, PAUSED)
5. Test no restoration when SavedStateHandle is empty (first launch)

**Instrumented Tests** (create: `FocusModeScreenTest.kt`):
1. Simulate process death with ActivityScenario.recreate()
2. Verify timer state restored after Activity recreation
3. Test user sees same elapsed time after backgrounding
4. Test medals persist and display after process death

### Risks

1. **Medal Serialization Complexity**: List<Medal> cannot be directly saved to Bundle. Must convert to IntArray (medals.map { it.value }.toIntArray()) and enum ordinal array. Low risk - straightforward conversion.

2. **Timer Job Cannot Be Saved**: Coroutine Job is not serializable. If timer was RUNNING at process death, must restart job with recalculated elapsed time. Medium risk - requires careful timestamp math.

3. **Timestamp Drift**: If timer was RUNNING during process death, elapsed time must be recalculated from saved startTimeMillis + System.currentTimeMillis(). Risk of drift if system clock changes. Low risk - use SystemClock.elapsedRealtime() instead of currentTimeMillis.

4. **Bundle Size Overflow**: Large medal lists (unlikely) or excessive state could exceed Bundle limits. Low risk - typical focus session has max 4 medals (30, 60, 90, 120 min thresholds).

---

## Implementation Steps (Dependency-Ordered)

### Step 1: ADD SavedStateHandle Constants (FocusViewModel.kt:40-50)

- **Action**: Define companion object with SavedStateHandle key constants after class declaration
- **Code**:
```kotlin
class FocusViewModel(...) : ViewModel() {

    companion object {
        private const val KEY_CURRENT_TIME = "focus_current_time"
        private const val KEY_TIMER_STATE = "focus_timer_state"
        private const val KEY_TIMER_STARTED = "focus_timer_started"
        private const val KEY_MEDALS_VALUES = "focus_medals_values"
        private const val KEY_MEDALS_TYPES = "focus_medals_types"
        private const val KEY_CLOCK_FACE_MINS = "focus_clock_face_mins"
        private const val KEY_IS_DISCRETE_MODE = "focus_is_discrete_mode"
        private const val KEY_DISCRETE_TASK_LEVEL = "focus_discrete_task_level"
    }

    // ... existing code
}
```
- **Dependency**: None (can start immediately)
- **Validation**: Code compiles, constants accessible within ViewModel

---

### Step 2: RESTORE State in init Block (FocusViewModel.kt:75-88)

- **Action**: Add state restoration logic at end of init block, after setupQuotaTracking()
- **Code**:
```kotlin
init {
    loadInitialData()
    checkStartTime()
    setupTimeBank()
    setupQuotaTracking()

    // NEW: Restore saved state if present
    restoreSavedState()
}

private fun restoreSavedState() {
    // Single check: if no saved state exists, return early (fresh start)
    if (!savedStateHandle.contains(KEY_CURRENT_TIME)) {
        Log.d("FocusViewModel", "No saved state found - starting fresh")
        return
    }

    // Now we know saved state exists, restore all fields
    val savedTime = savedStateHandle[KEY_CURRENT_TIME] ?: 0L
    val savedTimerState = savedStateHandle.get<Int>(KEY_TIMER_STATE)?.let { ordinal ->
        TimerState.values().getOrNull(ordinal)
    } ?: TimerState.IDLE
    val savedTimerStarted = savedStateHandle[KEY_TIMER_STARTED] ?: false

    // Restore medals
    val medalsValues = savedStateHandle.get<IntArray>(KEY_MEDALS_VALUES)
    val medalsTypes = savedStateHandle.get<IntArray>(KEY_MEDALS_TYPES)
    val medals = if (medalsValues != null && medalsTypes != null) {
        medalsValues.zip(medalsTypes).map { (value, typeOrdinal) ->
            Medal(value, MedalType.values()[typeOrdinal])
        }
    } else {
        emptyList()
    }

    // Restore other settings
    val clockFaceMins = savedStateHandle[KEY_CLOCK_FACE_MINS] ?: 30f
    val isDiscreteMode = savedStateHandle[KEY_IS_DISCRETE_MODE] ?: false
    val discreteTaskLevel = savedStateHandle.get<Int>(KEY_DISCRETE_TASK_LEVEL)?.let { ordinal ->
        DiscreteTaskLevel.values().getOrNull(ordinal)
    } ?: DiscreteTaskLevel.EASY

    // Update state with restored values
    focusUiState = focusUiState.copy(
        currentTime = savedTime,
        timerState = savedTimerState,
        timerStarted = savedTimerStarted,
        medals = medals,
        clockFaceMins = clockFaceMins,
        isDiscreteMode = isDiscreteMode,
        currentTaskLevel = discreteTaskLevel
    )

    Log.d("FocusViewModel", "Restored state: currentTime=$savedTime, timerState=$savedTimerState, medals=${medals.size}")
}
```
- **Dependency**: REQUIRES Step 1 (constants defined)
- **Validation**: Run app, background for 1+ minute, return and verify state restored

---

### Step 3: SAVE State When Timer State Changes (FocusViewModel.kt:121-160)

- **Action**: Add saveState() calls in startTimer(), pauseTimer(), bankTimer(), awardMedal()
- **Rationale**: Android best practice is to keep SavedStateHandle updated continuously. Writing to SavedStateHandle is cheap (in-memory HashMap update), and Android automatically serializes it during onSaveInstanceState() before backgrounding. This ensures the latest state is always ready to be persisted without needing lifecycle observers
- **Code**:
```kotlin
fun startTimer() {
    // ... existing startTimer logic ...

    // NEW: Save state when timer starts
    saveCurrentState()
}

fun pauseTimer() {
    // ... existing pauseTimer logic ...

    // NEW: Save state when timer pauses
    saveCurrentState()
}

fun bankTimer() {
    // ... existing bankTimer logic ...

    // NEW: Save state after banking (medals cleared)
    saveCurrentState()
}

fun awardMedal(medal: Medal) {
    // ... existing awardMedal logic ...

    // NEW: Save state after medal awarded
    saveCurrentState()
}

fun updateClockFaceMinutes(minutes: Float) {
    focusUiState = focusUiState.copy(clockFaceMins = minutes)

    // NEW: Save clock face setting
    saveCurrentState()
}

fun toggleFocusMode() {
    // ... existing toggle logic ...

    // NEW: Save discrete mode toggle
    saveCurrentState()
}

private fun saveCurrentState() {
    with(focusUiState) {
        savedStateHandle[KEY_CURRENT_TIME] = currentTime
        savedStateHandle[KEY_TIMER_STATE] = timerState.ordinal
        savedStateHandle[KEY_TIMER_STARTED] = timerStarted

        // Save medals as parallel arrays
        savedStateHandle[KEY_MEDALS_VALUES] = medals.map { it.value }.toIntArray()
        savedStateHandle[KEY_MEDALS_TYPES] = medals.map { it.type.ordinal }.toIntArray()

        // Save timer settings
        savedStateHandle[KEY_CLOCK_FACE_MINS] = clockFaceMins
        savedStateHandle[KEY_IS_DISCRETE_MODE] = isDiscreteMode
        savedStateHandle[KEY_DISCRETE_TASK_LEVEL] = currentTaskLevel.ordinal
    }

    Log.d("FocusViewModel", "Saved state: currentTime=${focusUiState.currentTime}, medals=${focusUiState.medals.size}")
}
```
- **Dependency**: REQUIRES Steps 1 & 2 (constants and restore logic exist)
- **Validation**: Check logcat for "Saved state" logs when timer actions occur

---

### Step 4: CLEAR Saved State on Exit (FocusViewModel.kt:488-490)

- **Action**: Clear SavedStateHandle when user explicitly exits focus mode or banks time
- **Code**:
```kotlin
fun onExit() {
    createOrUpdateEvent()

    // NEW: Clear saved state when session ends normally
    clearSavedState()
}

fun bankTime() {
    // ... existing bankTime logic ...

    focusUiState = focusUiState.copy(medals = emptyList())
    // ... existing code ...

    // NEW: If medals are banked, clear saved state (session complete)
    if (medalTime > 0) {
        clearSavedState()
    }
}

private fun clearSavedState() {
    savedStateHandle.remove<Long>(KEY_CURRENT_TIME)
    savedStateHandle.remove<Int>(KEY_TIMER_STATE)
    savedStateHandle.remove<Boolean>(KEY_TIMER_STARTED)
    savedStateHandle.remove<IntArray>(KEY_MEDALS_VALUES)
    savedStateHandle.remove<IntArray>(KEY_MEDALS_TYPES)
    savedStateHandle.remove<Float>(KEY_CLOCK_FACE_MINS)
    savedStateHandle.remove<Boolean>(KEY_IS_DISCRETE_MODE)
    savedStateHandle.remove<Int>(KEY_DISCRETE_TASK_LEVEL)

    Log.d("FocusViewModel", "Cleared saved state")
}
```
- **Dependency**: REQUIRES Steps 1-3 (all state saving infrastructure exists)
- **Validation**: Exit focus mode, check logcat for "Cleared saved state", verify no restoration on next entry

---

### Step 5: HANDLE Running Timer Edge Case (FocusViewModel.kt:restoreSavedState)

- **Action**: If timer was RUNNING at process death, optionally auto-resume or prompt user
- **Code** (add to restoreSavedState() function):
```kotlin
private fun restoreSavedState() {
    // ... existing restoration code from Step 2 ...

    // NEW: Handle timer that was RUNNING when process died
    if (savedTimerState == TimerState.RUNNING) {
        // Option A: Auto-pause and let user manually resume
        focusUiState = focusUiState.copy(
            timerState = TimerState.PAUSED,
            // currentTime already restored above
        )
        Log.d("FocusViewModel", "Timer was RUNNING, auto-paused on restoration")

        // Option B: Auto-resume (uncomment if preferred)
        // startTimer()
        // Log.d("FocusViewModel", "Timer was RUNNING, auto-resumed on restoration")
    }
}
```
- **Dependency**: REQUIRES Step 2 (called within restoreSavedState)
- **Validation**: Start timer, force-kill app via Android Studio, reopen and verify timer is paused with correct elapsed time
- **Note**: Discuss with user which option (A or B) is preferred behavior

---

## Success Definition

### Functional Success
- [ ] User starts focus mode timer, backgrounds app for 5+ minutes, returns to see timer state restored with correct elapsed time
- [ ] User earns medals (30, 60, 90 min), backgrounds app, returns to see medals still present in vault
- [ ] User pauses timer, backgrounds app, returns to see timer still paused at correct time
- [ ] User completes focus session and banks time, exits, re-enters to see fresh state (no restoration)
- [ ] Timer that was RUNNING when process died shows paused state with correct elapsed time (auto-pauses)

### Technical Success
- [ ] SavedStateHandle writes occur on timer start, pause, medal award, banking
- [ ] SavedStateHandle reads occur in ViewModel init when present
- [ ] SavedStateHandle cleared on normal exit or banking completion
- [ ] No process death simulation failures (Android Studio → terminate process)
- [ ] No Bundle size exceptions (TransactionTooLargeException)
- [ ] Logcat shows "Saved state" and "Restored state" logs with correct values

### Validation Commands
```bash
# Run app on emulator or device
./gradlew installDebug

# Monitor logcat for state saving/restoration
adb logcat -s FocusViewModel

# Simulate process death via Android Studio:
# 1. Start focus mode timer
# 2. Background app (Home button)
# 3. Android Studio → Terminal → adb shell am kill com.voxplanapp
# 4. Reopen app from launcher
# 5. Verify timer state restored
```

---

## Notes

**Confidence**: High

**Pattern Source**: SavedStateHandle pattern from Android official documentation (Pattern B), replacing current mutableStateOf pattern (Pattern A)

**External Refs**:
- [Save UI states - Android Developers](https://developer.android.com/topic/libraries/architecture/saving-states)
- [Saved State module for ViewModel](https://developer.android.com/topic/libraries/architecture/viewmodel/viewmodel-savedstate)

**User Clarifications**:
1. **Running Timer Behavior**: When timer was RUNNING at process death, should it auto-resume or auto-pause on restoration? Current implementation: auto-pause (Option A in Step 5). This is safer as it prevents unexpected background time accrual.
2. **Discrete Task State**: Should discrete task progress (challenge level) be restored, or should discrete tasks always start fresh? Current implementation: restores discrete task level for continuity.

**Implementation Notes**:
- Medal serialization uses parallel IntArray approach (values + type ordinals) to avoid Parcelable complexity
- SavedStateHandle updates are lightweight (primitives only, ~100 bytes total)
- No UI changes needed - restoration is transparent to user
- Backward compatible - if SavedStateHandle is empty (first launch or post-banking), normal initialization occurs
- SystemClock.elapsedRealtime() could be used instead of System.currentTimeMillis() for timestamp drift protection, but currentTime field is already milliseconds, so delta calculation works fine

---

**Total Lines**: ~150 lines of code across 5 steps
**Estimated Implementation Time**: 2-3 hours (including testing)
**Risk Level**: Low (SavedStateHandle is well-supported, changes isolated to FocusViewModel)
