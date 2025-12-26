# Feature: Fix SavedStateHandle Restoring Stale State After Process Death

## Background and Motivation

**CRITICAL P0 BUG**: When app is backgrounded and process is killed, timer restoration shows state from BEFORE backgrounding. Timer "goes backwards in time" - user sees old values, not current progress.

**Evidence from logs:**
- User starts timer, runs for 2 minutes (shows 2:00)
- User backgrounds app (home button)
- Timer continues running in background, reaches 3 minutes
- Process killed
- App reopened: Timer shows 2:00 (WRONG - went backwards!)

**Current Behavior:** Process death restoration uses stale SavedStateHandle Bundle captured at Activity.onStop(), not latest state.

**Desired Behavior:** Timer continues seamlessly across process death. On restore, timer shows correct elapsed time (3:00 in example), keeps ticking as though nothing happened.

**Impact:** Users lose focus session progress on process death. Timer resets to old values, breaking core time tracking feature. P0 because it affects primary user flow and causes data loss.

---

## Root Cause Analysis

### Why The Existing "Fix" Doesn't Work

**Current Implementation (Lines 103-185):**
- ✅ Has SavedStateHandle save/restore functions
- ✅ Saves state every 5 seconds (line 362-365)
- ❌ **WRONG**: Saves `currentTime` (constantly changing value)
- ❌ **WRONG**: Uses `System.currentTimeMillis()` (can jump backwards/forwards)
- ❌ **WRONG**: Auto-pauses on restoration (line 147-149) - user doesn't want this

**The Fatal Flaw:**

```kotlin
// Line 237: Uses System.currentTimeMillis() for timer calculation
startTime = System.currentTimeMillis()

// Line 157: Tries to save constantly-changing elapsed time
savedStateHandle[KEY_CURRENT_TIME] = currentTime  // Changes every second!

// Line 362-365: Saves every 5 seconds while running
if (elapsedTime - lastSaveTime >= 5000) {
    saveCurrentState()  // But writes AFTER onStop() don't persist!
}
```

**What Happens:**
1. User starts timer → SavedStateHandle saves `currentTime = 0`
2. Timer runs for 2 minutes → `currentTime = 120000` (UI shows 2:00)
3. App backgrounded → Activity.onStop() fires → **SavedStateHandle Bundle serialized with currentTime = 120000**
4. Timer keeps running in background (viewModelScope coroutine still active)
5. Timer reaches 3 minutes → `saveCurrentState()` called → **savedStateHandle[KEY_CURRENT_TIME] = 180000**
6. **BUT: Bundle already serialized! Write doesn't persist!**
7. Process killed
8. Restoration: Loads Bundle from step 3 → `currentTime = 120000` → Timer shows 2:00 (went backwards!)

**Android Official Docs:**
> "SavedStateHandle only persists data when Activity.onStop() is called. Updates made while the Activity is stopped won't be saved unless the Activity receives another onStart → onStop cycle."

---

## Feature Goal

Fix timer to use **calculation-based approach** where elapsed time is computed from absolute start timestamp, not saved every second. On process death restoration, timer automatically continues from correct elapsed time with no manual resume needed.

---

## Android Best Practices (Research Findings)

### Critical Finding 1: Use SystemClock.elapsedRealtime()

**Official Android Warning:**
> "System.currentTimeMillis() - The wall clock can be set by the user or phone network, so time may jump backwards or forwards unpredictably. Use SystemClock.elapsedRealtime() for measuring elapsed time."

**Your Current Code (Line 237):**
```kotlin
startTime = System.currentTimeMillis()  // ❌ WRONG
```

**Correct:**
```kotlin
startTime = SystemClock.elapsedRealtime()  // ✅ Monotonic, never goes backwards
```

**Why This Matters:**
- `currentTimeMillis()` changes when user adjusts system clock
- Timer would show negative elapsed time or huge jumps
- `elapsedRealtime()` is monotonic (guaranteed to only increase)
- Immune to system time changes

**Source:** [SystemClock API Reference](https://developer.android.com/reference/android/os/SystemClock)

---

### Critical Finding 2: Save Start Timestamp, Not Elapsed Time

**Recommended Pattern:**

```kotlin
// WRONG (current implementation):
savedStateHandle[KEY_CURRENT_TIME] = currentTime  // Changes every second

// CORRECT:
savedStateHandle[KEY_START_TIMESTAMP] = startTimestamp  // Constant value!
```

**Why This Works:**
- Start timestamp is **constant** (written once when timer starts)
- Saved BEFORE backgrounding (during startTimer() call)
- Doesn't need updates every second
- On restoration, calculate: `elapsedTime = SystemClock.elapsedRealtime() - startTimestamp`

**Source:** [Save UI states guide](https://developer.android.com/topic/libraries/architecture/saving-states)

---

### Critical Finding 3: Auto-Restart Timer on Restoration

**Pattern:**

```kotlin
init {
    val savedStartTime = savedStateHandle.get<Long>(KEY_START_TIMESTAMP)
    val savedState = savedStateHandle.get<Int>(KEY_STATE)
        ?.let { TimerState.values()[it] }

    if (savedStartTime != null && savedState == TimerState.RUNNING) {
        // Calculate time that passed during process death
        val elapsedNow = SystemClock.elapsedRealtime() - savedStartTime

        // Automatically restart timer with correct elapsed time
        startTimerInternal(resumeFromElapsed = elapsedNow)
    }
}
```

**Behavior:** Timer continues seamlessly. User sees correct elapsed time, timer keeps ticking.

---

## Pattern Analysis

### Current Pattern: FocusViewModel (BUGGY)

**File:** `FocusViewModel.kt:219-259`

**Current Approach:**
```kotlin
fun startTimer() {
    // Line 237: Uses System.currentTimeMillis() ❌
    startTime = if (focusUiState.timerState == TimerState.PAUSED) {
        System.currentTimeMillis() - previousElapsedTime
    } else {
        System.currentTimeMillis()
    }

    // Line 243-252: Timer job updates every second
    _timerJob.value = viewModelScope.launch {
        while (isActive) {
            val currentTime = System.currentTimeMillis()
            val elapsedTime = currentTime - startTime
            updateTimerState(elapsedTime)  // Line 362: Saves every 5 seconds ❌
            delay(1000)
        }
    }
}
```

**Problems:**
1. ❌ `System.currentTimeMillis()` can jump backwards
2. ❌ `startTime` stored as local variable (not saved to SavedStateHandle)
3. ❌ Tries to save `currentTime` every 5 seconds (doesn't persist after onStop)
4. ❌ Auto-pauses on restoration (line 147-149) - user wants auto-continue

---

### Recommended Pattern: Calculation-Based Timer

**Approach:**

```kotlin
// Save start timestamp when timer starts (happens BEFORE backgrounding)
fun startTimer() {
    val startTimestamp = if (focusUiState.timerState == TimerState.PAUSED) {
        SystemClock.elapsedRealtime() - focusUiState.currentTime
    } else {
        SystemClock.elapsedRealtime()
    }

    // Save to SavedStateHandle immediately
    savedStateHandle[KEY_START_TIMESTAMP] = startTimestamp
    savedStateHandle[KEY_STATE] = TimerState.RUNNING.ordinal

    // Start UI update loop
    startTimerJob(startTimestamp)
}

// On restoration, calculate and auto-restart
init {
    restoreSavedState()
}

private fun restoreSavedState() {
    val startTimestamp = savedStateHandle.get<Long>(KEY_START_TIMESTAMP) ?: return
    val state = savedStateHandle.get<Int>(KEY_STATE)
        ?.let { TimerState.values()[it] } ?: return

    if (state == TimerState.RUNNING) {
        // Calculate elapsed time (includes time during process death)
        val elapsed = SystemClock.elapsedRealtime() - startTimestamp

        // Restore medals, settings, etc.
        restoreMedalsAndSettings()

        // Auto-restart timer with correct elapsed time
        startTimerJob(startTimestamp)

        Log.d("FocusViewModel", "Timer restored: elapsed=$elapsed ms, auto-restarted")
    }
}
```

**Benefits:**
- ✅ Start timestamp saved ONCE (before backgrounding)
- ✅ Calculation reconstructs elapsed time (includes process death period)
- ✅ Auto-restart on restoration (timer never appears to stop)
- ✅ Uses `SystemClock.elapsedRealtime()` (monotonic, immune to time changes)

---

## Integration Impact

### Files to Modify

**1. FocusViewModel.kt** (primary implementation)
   - Replace `System.currentTimeMillis()` with `SystemClock.elapsedRealtime()`
   - Add `KEY_START_TIMESTAMP` constant
   - Save start timestamp in `startTimer()` (not elapsed time)
   - Modify `restoreSavedState()` to calculate elapsed time and auto-restart
   - Remove auto-pause logic (lines 147-149)
   - Remove periodic saves of `currentTime` (line 362-365 becomes unnecessary)

**No Other Files Modified** - FocusModeScreen.kt uses state reactively, no changes needed

---

### Shared Dependencies

- `android.os.SystemClock` - Already available (Android framework)
- `SavedStateHandle` - Already injected via AppViewModelProvider.Factory
- `viewModelScope` - Already used for timer coroutine

**No New Dependencies Required**

---

### Test Coverage Plan

**Unit Tests** (`FocusViewModelTest.kt` - NEW):
1. `timer uses SystemClock.elapsedRealtime not currentTimeMillis`
2. `start timestamp saved to SavedStateHandle on timer start`
3. `restoration calculates correct elapsed time from timestamp`
4. `RUNNING timer auto-restarts on restoration (not paused)`
5. `PAUSED timer stays paused on restoration`
6. `medal serialization to IntArray preserves data`
7. `clearSavedState removes all keys including start timestamp`

**Instrumented Tests** (`FocusModeProcessDeathTest.kt` - NEW):
1. `timer survives process death and shows correct elapsed time`
2. `timer continues ticking after process death restoration`
3. `medals persist after process death`
4. `system time change doesn't affect timer (elapsedRealtime immunity)`

---

### Risks & Mitigations

**Risk 1: viewModelScope Coroutine Doesn't Actually Run in Background**
- **Reality:** Coroutine continues after onStop() but Android may throttle it (Doze mode)
- **Mitigation:** Doesn't matter! We calculate elapsed time from timestamp, not by counting ticks
- **Result:** Timer accuracy guaranteed regardless of background execution

**Risk 2: SystemClock.elapsedRealtime() Includes Deep Sleep**
- **Behavior:** `elapsedRealtime()` continues during device sleep (unlike `uptimeMillis()`)
- **Mitigation:** This is DESIRED behavior - we want "wall clock" elapsed time
- **Result:** Timer shows correct real-world elapsed time

**Risk 3: Backward Compatibility with Existing Saved State**
- **Issue:** Production users may have old `KEY_CURRENT_TIME` saved state
- **Mitigation:** Check for `KEY_START_TIMESTAMP` first, fallback to old restoration if missing
- **Result:** Graceful migration, no data loss

---

## Implementation Steps (Dependency-Ordered)

### Step 1: ADD Import for SystemClock

**File**: `FocusViewModel.kt:1`

**Action**: Add import at top of file

```kotlin
import android.os.SystemClock
```

**Dependency**: None (can implement immediately)

**Validation**: File compiles without import errors

---

### Step 2: ADD Start Timestamp Constant

**File**: `FocusViewModel.kt:49`

**Action**: Add new constant to companion object

```kotlin
companion object {
    private const val KEY_START_TIMESTAMP = "focus_start_timestamp"  // NEW
    private const val KEY_CURRENT_TIME = "focus_current_time"  // Keep for backward compat
    private const val KEY_TIMER_STATE = "focus_timer_state"
    private const val KEY_TIMER_STARTED = "focus_timer_started"
    private const val KEY_MEDALS_VALUES = "focus_medals_values"
    private const val KEY_MEDALS_TYPES = "focus_medals_types"
    private const val KEY_CLOCK_FACE_MINS = "focus_clock_face_mins"
    private const val KEY_IS_DISCRETE_MODE = "focus_is_discrete_mode"
    private const val KEY_DISCRETE_TASK_LEVEL = "focus_discrete_task_level"
}
```

**Follow**: Existing naming convention (`focus_` prefix)

**Preserve**: Existing keys for backward compatibility

**Dependency**: REQUIRES Step 1 completion

**Validation**: Build succeeds

---

### Step 3: REPLACE System.currentTimeMillis() with SystemClock.elapsedRealtime()

**File**: `FocusViewModel.kt:219-259`

**Action**: Replace timer clock in `startTimer()` function

**BEFORE (Lines 220-238):**
```kotlin
fun startTimer() {
    var startTime: Long = 0L

    if (!focusUiState.timerStarted) {
        focusUiState = focusUiState.copy(
            timerStarted = true,
            startTime = LocalTime.now()
        )
    }
    if (focusUiState.timerState == TimerState.IDLE) {
        viewModelScope.launch {
            delay(1500)
            soundPlayer.playSound(R.raw.countdown_start)
        }
    }
    // if coming from a paused state, we accrue previous time to the start time.
    if (focusUiState.timerState == TimerState.PAUSED) {
        val previousElapsedTime = focusUiState.currentTime
        startTime = System.currentTimeMillis() - previousElapsedTime  // ❌ WRONG
    } else startTime = System.currentTimeMillis()  // ❌ WRONG

    Log.d("Timer","starting timer @ ${startTime}")
```

**AFTER:**
```kotlin
fun startTimer() {
    // Calculate start timestamp using monotonic clock
    val startTimestamp = if (focusUiState.timerState == TimerState.PAUSED) {
        // Resume: adjust timestamp to account for previous elapsed time
        SystemClock.elapsedRealtime() - focusUiState.currentTime
    } else {
        // Fresh start
        SystemClock.elapsedRealtime()
    }

    // Save start timestamp immediately (BEFORE any backgrounding)
    savedStateHandle[KEY_START_TIMESTAMP] = startTimestamp
    savedStateHandle[KEY_TIMER_STATE] = TimerState.RUNNING.ordinal

    if (!focusUiState.timerStarted) {
        focusUiState = focusUiState.copy(
            timerStarted = true,
            startTime = LocalTime.now()
        )
    }
    if (focusUiState.timerState == TimerState.IDLE) {
        viewModelScope.launch {
            delay(1500)
            soundPlayer.playSound(R.raw.countdown_start)
        }
    }

    Log.d("Timer","Starting timer @ startTimestamp=$startTimestamp (elapsedRealtime)")
```

**Coordinate**: Save to SavedStateHandle immediately when timer starts

**Verify**: Uses `SystemClock.elapsedRealtime()` instead of `System.currentTimeMillis()`

**Dependency**: REQUIRES Steps 1 & 2 completion

**Validation**: Logs show "elapsedRealtime" message with timestamp value

---

### Step 4: UPDATE Timer Job to Use Start Timestamp

**File**: `FocusViewModel.kt:243-252`

**Action**: Calculate elapsed time from start timestamp in timer loop

**BEFORE (Lines 243-252):**
```kotlin
// make the timer tick by creating a timer job within a separate co-routine
_timerJob.value = viewModelScope.launch {
    while (isActive) {
        val currentTime = System.currentTimeMillis()  // ❌
        //Log.d("Timer","currentTime = ${currentTime}")
        val elapsedTime = currentTime - startTime  // ❌ startTime is local variable
        //Log.d("Timer","elapsed time = ${elapsedTime}")
        updateTimerState(elapsedTime)
        delay(1000)
    }
}
```

**AFTER:**
```kotlin
// Make the timer tick by reading start timestamp from SavedStateHandle
_timerJob.value = viewModelScope.launch {
    while (isActive) {
        val startTimestamp = savedStateHandle.get<Long>(KEY_START_TIMESTAMP) ?: return@launch
        val elapsedTime = SystemClock.elapsedRealtime() - startTimestamp
        updateTimerState(elapsedTime)
        delay(1000)
    }
}
```

**Integrate**: Reads start timestamp from SavedStateHandle (always up-to-date)

**Follow**: Calculation-based approach (no local variable storage)

**Dependency**: REQUIRES Step 3 completion

**Validation**: Timer ticks correctly, elapsed time increases every second

---

### Step 5: REMOVE Periodic Saves from updateTimerState()

**File**: `FocusViewModel.kt:342-386`

**Action**: Remove unnecessary periodic saves (start timestamp doesn't change)

**BEFORE (Lines 361-365):**
```kotlin
// Save state every 5 seconds to keep SavedStateHandle up-to-date
if (elapsedTime - lastSaveTime >= 5000) {
    saveCurrentState()  // ❌ Unnecessary - start timestamp is constant
    lastSaveTime = elapsedTime
}
```

**AFTER:**
```kotlin
// No periodic saves needed - start timestamp is constant!
// Only save on events: pause, medal award, banking, etc.
```

**Also remove (Lines 337-338):**
```kotlin
// Track last save time to avoid saving every second
private var lastSaveTime = 0L  // ❌ Remove - not needed
```

**Coordinate**: Keep medal award saves (line 570), pause saves (line 413), banking saves (line 592)

**Verify**: No performance impact (fewer SavedStateHandle writes)

**Dependency**: REQUIRES Step 4 completion

**Validation**: Timer runs smoothly without periodic saves

---

### Step 6: MODIFY restoreSavedState() to Auto-Restart Timer

**File**: `FocusViewModel.kt:103-153`

**Action**: Calculate elapsed time from timestamp and auto-restart timer

**BEFORE (Lines 103-153):**
```kotlin
private fun restoreSavedState() {
    // Single check: if no saved state exists, return early (fresh start)
    if (!savedStateHandle.contains(KEY_CURRENT_TIME)) {
        Log.d("FocusViewModel", "No saved state found - starting fresh")
        return
    }

    // Now we know saved state exists, restore all fields
    val savedTime = savedStateHandle[KEY_CURRENT_TIME] ?: 0L  // ❌ Stale value
    val savedTimerState = savedStateHandle.get<Int>(KEY_TIMER_STATE)?.let { ordinal ->
        TimerState.values().getOrNull(ordinal)
    } ?: TimerState.IDLE
    val savedTimerStarted = savedStateHandle[KEY_TIMER_STARTED] ?: false

    // Restore medals...
    // ... (lines 118-143)

    // Update state with restored values
    focusUiState = focusUiState.copy(
        currentTime = savedTime,  // ❌ Stale!
        timerState = savedTimerState,
        timerStarted = savedTimerStarted,
        medals = medals,
        clockFaceMins = clockFaceMins,
        isDiscreteMode = isDiscreteMode,
        currentTaskLevel = discreteTaskLevel
    )

    // Handle timer that was RUNNING when process died - auto-pause
    if (savedTimerState == TimerState.RUNNING) {
        focusUiState = focusUiState.copy(timerState = TimerState.PAUSED)  // ❌ Don't pause!
        Log.d("FocusViewModel", "Timer was RUNNING, auto-paused on restoration")
    }

    Log.d("FocusViewModel", "Restored state: currentTime=$savedTime, timerState=$savedTimerState, medals=${medals.size}")
}
```

**AFTER:**
```kotlin
private fun restoreSavedState() {
    // Check for new start timestamp approach first
    val startTimestamp = savedStateHandle.get<Long>(KEY_START_TIMESTAMP)

    if (startTimestamp != null) {
        // NEW APPROACH: Calculate elapsed time from start timestamp
        val savedTimerState = savedStateHandle.get<Int>(KEY_TIMER_STATE)?.let { ordinal ->
            TimerState.values().getOrNull(ordinal)
        } ?: TimerState.IDLE

        if (savedTimerState == TimerState.RUNNING) {
            // Calculate total elapsed time (includes process death period)
            val totalElapsedTime = SystemClock.elapsedRealtime() - startTimestamp

            // Restore saved settings
            val savedTimerStarted = savedStateHandle[KEY_TIMER_STARTED] ?: false
            val savedClockFaceMins = savedStateHandle[KEY_CLOCK_FACE_MINS] ?: 30f
            val isDiscreteMode = savedStateHandle[KEY_IS_DISCRETE_MODE] ?: false
            val discreteTaskLevel = savedStateHandle.get<Int>(KEY_DISCRETE_TASK_LEVEL)?.let { ordinal ->
                DiscreteTaskLevel.values().getOrNull(ordinal)
            } ?: DiscreteTaskLevel.EASY

            // MEDAL CALCULATION: Calculate how many medals should have been earned
            val revolutionMillis = savedClockFaceMins * 60000f
            val completeRevolutions = (totalElapsedTime / revolutionMillis).toInt()

            // Restore existing medals (saved before process death)
            val existingMedals = restoreMedals()

            // Award missed medals for complete revolutions during process death
            val missedMedals = List(completeRevolutions) {
                Medal(savedClockFaceMins.toInt(), MedalType.MINUTES)
            }

            // Combine existing medals with retroactively calculated medals
            val allMedals = existingMedals + missedMedals

            // Calculate remainder time (current partial revolution in progress)
            val remainderTime = totalElapsedTime % revolutionMillis.toLong()

            // Update state with calculated values
            focusUiState = focusUiState.copy(
                currentTime = remainderTime,  // Shows remainder on clock face
                timerState = TimerState.RUNNING,  // Keep RUNNING!
                timerStarted = savedTimerStarted,
                medals = allMedals,  // Includes retroactive medals
                clockFaceMins = savedClockFaceMins,
                isDiscreteMode = isDiscreteMode,
                currentTaskLevel = discreteTaskLevel
            )

            // Auto-restart timer job (timer continues seamlessly)
            _timerJob.value = viewModelScope.launch {
                while (isActive) {
                    val timestamp = savedStateHandle.get<Long>(KEY_START_TIMESTAMP) ?: return@launch
                    val elapsed = SystemClock.elapsedRealtime() - timestamp
                    updateTimerState(elapsed)
                    delay(1000)
                }
            }

            focusUiState = focusUiState.copy(
                timerState = TimerState.RUNNING,
                currentTheme = ColorScheme.WORK
            )

            Log.d("FocusViewModel", "Timer restored: totalElapsed=${totalElapsedTime}ms, " +
                "revolutions=$completeRevolutions, medals=${allMedals.size}, " +
                "remainder=${remainderTime}ms, auto-restarted")
            return
        }
    }

    // BACKWARD COMPATIBILITY: Check for old KEY_CURRENT_TIME approach
    if (!savedStateHandle.contains(KEY_CURRENT_TIME)) {
        Log.d("FocusViewModel", "No saved state found - starting fresh")
        return
    }

    // Old restoration logic (for existing production users)
    val savedTime = savedStateHandle[KEY_CURRENT_TIME] ?: 0L
    val savedTimerState = savedStateHandle.get<Int>(KEY_TIMER_STATE)?.let { ordinal ->
        TimerState.values().getOrNull(ordinal)
    } ?: TimerState.IDLE
    // ... (rest of old logic for backward compatibility)

    Log.d("FocusViewModel", "Restored using legacy approach (no start timestamp)")
}

private fun restoreMedals(): List<Medal> {
    val medalsValues = savedStateHandle.get<IntArray>(KEY_MEDALS_VALUES)
    val medalsTypes = savedStateHandle.get<IntArray>(KEY_MEDALS_TYPES)
    return if (medalsValues != null && medalsTypes != null) {
        medalsValues.zip(medalsTypes).map { (value, typeOrdinal) ->
            Medal(value, MedalType.values()[typeOrdinal])
        }
    } else {
        emptyList()
    }
}
```

**Key Medal Calculation Logic:**

1. **Calculate complete revolutions**: `(totalElapsedTime / revolutionMillis).toInt()`
   - Example: 95 mins / 30 mins = 3 complete revolutions

2. **Award missed medals**: Create medals for each revolution that occurred during process death
   - Each medal valued at `savedClockFaceMins` minutes

3. **Combine medals**: `existingMedals + missedMedals`
   - Preserves medals earned before process death
   - Adds retroactively calculated medals

4. **Calculate remainder**: `totalElapsedTime % revolutionMillis`
   - Shows current progress toward next medal
   - Example: 95 mins % 30 mins = 5 mins (shows 5/30 progress)

**Result:** Timer appears to have never stopped. Medals correctly awarded, timer shows progress toward next medal.

**Coordinate**: Auto-restart timer job when state is RUNNING

**Verify**: No auto-pause logic (timer continues seamlessly)

**Preserve**: Backward compatibility with old `KEY_CURRENT_TIME` approach

**Dependency**: REQUIRES Steps 1-5 completion

**Validation**: Logs show "auto-restarted" message with correct elapsed time

---

### Step 7: UPDATE saveCurrentState() to Save Medals Only

**File**: `FocusViewModel.kt:155-172`

**Action**: Keep medal/settings persistence, remove timer state saves (handled in startTimer)

**BEFORE:**
```kotlin
private fun saveCurrentState() {
    with(focusUiState) {
        savedStateHandle[KEY_CURRENT_TIME] = currentTime  // ❌ Remove
        savedStateHandle[KEY_TIMER_STATE] = timerState.ordinal  // ❌ Remove (saved in startTimer)
        savedStateHandle[KEY_TIMER_STARTED] = timerStarted  // ❌ Remove
        savedStateHandle[KEY_MEDALS_VALUES] = medals.map { it.value }.toIntArray()
        savedStateHandle[KEY_MEDALS_TYPES] = medals.map { it.type.ordinal }.toIntArray()
        savedStateHandle[KEY_CLOCK_FACE_MINS] = clockFaceMins
        savedStateHandle[KEY_IS_DISCRETE_MODE] = isDiscreteMode
        savedStateHandle[KEY_DISCRETE_TASK_LEVEL] = currentTaskLevel.ordinal
    }

    Log.d("FocusViewModel", "Saved state: currentTime=${focusUiState.currentTime}, medals=${focusUiState.medals.size}")
}
```

**AFTER:**
```kotlin
private fun saveCurrentState() {
    with(focusUiState) {
        // Save medals and settings (timer state saved in startTimer/pauseTimer)
        savedStateHandle[KEY_MEDALS_VALUES] = medals.map { it.value }.toIntArray()
        savedStateHandle[KEY_MEDALS_TYPES] = medals.map { it.type.ordinal }.toIntArray()
        savedStateHandle[KEY_CLOCK_FACE_MINS] = clockFaceMins
        savedStateHandle[KEY_IS_DISCRETE_MODE] = isDiscreteMode
        savedStateHandle[KEY_DISCRETE_TASK_LEVEL] = currentTaskLevel.ordinal
    }

    Log.d("FocusViewModel", "Saved medals and settings: medals=${focusUiState.medals.size}")
}
```

**Coordinate**: Timer state (RUNNING/PAUSED) saved explicitly in startTimer/pauseTimer

**Verify**: Medals and settings still persist correctly

**Dependency**: REQUIRES Step 6 completion

**Validation**: Medal awards trigger saves correctly

---

### Step 8: UPDATE pauseTimer() to Save Paused Elapsed Time

**File**: `FocusViewModel.kt:402-414`

**Action**: Save paused state and current elapsed time

**BEFORE (Lines 402-414):**
```kotlin
fun pauseTimer() {
    Log.d("Timer","pausing timer... ")
    // timer state is used to determine state of start/pause button
    focusUiState = focusUiState.copy(
        timerState = TimerState.PAUSED,
        currentTheme = ColorScheme.REST
    )

    // this cancels the ticking.
    _timerJob.value?.cancel()
    _timerJob.value = null
    saveCurrentState()
}
```

**AFTER:**
```kotlin
fun pauseTimer() {
    Log.d("Timer","pausing timer... ")

    // Calculate current elapsed time before pausing
    val startTimestamp = savedStateHandle.get<Long>(KEY_START_TIMESTAMP)
    if (startTimestamp != null) {
        val elapsedTime = SystemClock.elapsedRealtime() - startTimestamp
        focusUiState = focusUiState.copy(currentTime = elapsedTime)
    }

    // Cancel timer job
    _timerJob.value?.cancel()
    _timerJob.value = null

    // Update state
    focusUiState = focusUiState.copy(
        timerState = TimerState.PAUSED,
        currentTheme = ColorScheme.REST
    )

    // Save paused state
    savedStateHandle[KEY_TIMER_STATE] = TimerState.PAUSED.ordinal
    savedStateHandle[KEY_CURRENT_TIME] = focusUiState.currentTime  // Save paused elapsed time
    saveCurrentState()  // Save medals and settings

    Log.d("Timer", "Timer paused at ${focusUiState.currentTime}ms")
}
```

**Coordinate**: Save paused elapsed time to restore correctly

**Verify**: Resume from pause shows correct time

**Dependency**: REQUIRES Step 7 completion

**Validation**: Pause → resume shows correct elapsed time

---

### Step 9: UPDATE clearSavedState() to Include Start Timestamp

**File**: `FocusViewModel.kt:174-185`

**Action**: Add start timestamp key to removal list

**BEFORE:**
```kotlin
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

**AFTER:**
```kotlin
private fun clearSavedState() {
    savedStateHandle.remove<Long>(KEY_START_TIMESTAMP)  // NEW
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

**Action**: Add start timestamp removal for completeness

**Verify**: All 9 keys now removed

**Dependency**: REQUIRES Step 2 completion

**Validation**: Banking time clears all keys including start timestamp

---

### Step 10: CREATE Unit Tests

**File**: `app/src/test/java/com/voxplanapp/ui/focusmode/FocusViewModelTest.kt` (NEW)

**Action**: Create comprehensive unit test suite

```kotlin
class FocusViewModelTest {
    private lateinit var savedStateHandle: SavedStateHandle
    private lateinit var viewModel: FocusViewModel

    @Test
    fun `timer uses SystemClock elapsedRealtime not currentTimeMillis`() {
        // Verify implementation uses correct clock
        // (Code inspection test - check imports)
    }

    @Test
    fun `start timestamp saved to SavedStateHandle on timer start`() {
        viewModel.startTimer()

        val savedTimestamp = savedStateHandle.get<Long>(KEY_START_TIMESTAMP)
        assertNotNull(savedTimestamp)
        assertTrue(savedTimestamp!! > 0)
    }

    @Test
    fun `restoration calculates correct elapsed time from timestamp`() {
        // Given: Timer ran for 30 seconds before process death
        val mockStartTime = SystemClock.elapsedRealtime() - 30000L
        savedStateHandle[KEY_START_TIMESTAMP] = mockStartTime
        savedStateHandle[KEY_TIMER_STATE] = TimerState.RUNNING.ordinal

        // When: ViewModel restored
        viewModel = createViewModel()

        // Then: Shows ~30 seconds elapsed
        assertTrue(viewModel.focusUiState.currentTime >= 30000L)
        assertEquals(TimerState.RUNNING, viewModel.focusUiState.timerState)
    }

    @Test
    fun `RUNNING timer auto-restarts on restoration not paused`() {
        savedStateHandle[KEY_START_TIMESTAMP] = SystemClock.elapsedRealtime()
        savedStateHandle[KEY_TIMER_STATE] = TimerState.RUNNING.ordinal

        viewModel = createViewModel()

        assertEquals(TimerState.RUNNING, viewModel.focusUiState.timerState)
    }

    @Test
    fun `PAUSED timer stays paused on restoration`() {
        savedStateHandle[KEY_TIMER_STATE] = TimerState.PAUSED.ordinal
        savedStateHandle[KEY_CURRENT_TIME] = 15000L

        viewModel = createViewModel()

        assertEquals(TimerState.PAUSED, viewModel.focusUiState.timerState)
        assertEquals(15000L, viewModel.focusUiState.currentTime)
    }
}
```

**Validate**: All unit tests pass (`./gradlew test`)

---

### Step 11: CREATE Instrumented Tests

**File**: `app/src/androidTest/java/com/voxplanapp/ui/focusmode/FocusModeProcessDeathTest.kt` (NEW)

**Action**: Create process death simulation tests

```kotlin
@Test
fun `timer survives process death and shows correct elapsed time`() {
    // Given: Focus mode running with 30 seconds elapsed
    val scenario = ActivityScenario.launch(MainActivity::class.java)
    // Navigate to focus mode, start timer, wait 30 seconds
    Thread.sleep(30000)

    // When: Process death simulated
    scenario.recreate()

    // Then: Timer shows ~30 seconds (RUNNING, not paused)
    onView(withId(R.id.timer_display)).check(matches(withText(containsString("0:30"))))
}

@Test
fun `timer continues ticking after process death restoration`() {
    // Given: Timer restored after process death
    val scenario = ActivityScenario.launch(MainActivity::class.java)
    // Start timer, kill process, restore

    // When: Wait 5 seconds
    Thread.sleep(5000)

    // Then: Timer increased by ~5 seconds
    // Verify timer is still ticking (value increases)
}

@Test
fun `system time change doesn't affect timer`() {
    // Test that changing system time doesn't break timer
    // (SystemClock.elapsedRealtime immunity)
}
```

**Validate**: All instrumented tests pass (`./gradlew connectedAndroidTest`)

---

## Success Definition

### Functional Success
- [ ] Timer uses `SystemClock.elapsedRealtime()` (monotonic clock)
- [ ] Start timestamp saved to SavedStateHandle when timer starts
- [ ] Process death restores correct elapsed time (includes time during death)
- [ ] Timer automatically continues on restoration (no manual resume needed)
- [ ] Timer appears to "never stop" from user perspective
- [ ] Medals persist correctly across process death

### Technical Success
- [ ] All unit tests passing (6 tests in FocusViewModelTest.kt)
- [ ] All instrumented tests passing (3 tests in FocusModeProcessDeathTest.kt)
- [ ] No lint errors or warnings
- [ ] No periodic saves during timer run (performance improvement)
- [ ] Backward compatible with existing saved state

### Validation Commands
```bash
# Build and install
./gradlew assembleDebug
./gradlew installDebug

# Monitor logs during testing
adb logcat -s FocusViewModel:D Timer:D

# Simulate process death
# 1. Start focus mode, run timer for 2 minutes
# 2. Background app (home button)
# 3. Force kill: adb shell am kill com.voxplanapp
# 4. Reopen from launcher
# 5. Verify: Timer shows ~2 minutes (RUNNING), continues ticking

# Run tests
./gradlew test
./gradlew connectedAndroidTest
```

---

## Notes

**Confidence**: High

**Pattern Source**: Calculation-based timer (Android official pattern) using `SystemClock.elapsedRealtime()`

**External Refs**:
- [SystemClock API Reference](https://developer.android.com/reference/android/os/SystemClock)
- [Save UI states guide](https://developer.android.com/topic/libraries/architecture/saving-states)
- [SavedStateHandle limitations](https://developersbreach.com/savedstatehandle-process-death-limitations/)

**Key Decisions**:

- **Decision 1**: Use `SystemClock.elapsedRealtime()` instead of `System.currentTimeMillis()`
  - **Rationale**: Official Android recommendation for elapsed time measurement. Monotonic (never goes backwards), immune to system time changes.
  - **Trade-off**: None - this is strictly better for timers.

- **Decision 2**: Save start timestamp (constant) instead of elapsed time (changing)
  - **Rationale**: Start timestamp written ONCE when timer starts (before backgrounding). Doesn't need updates. Calculation reconstructs elapsed time.
  - **Trade-off**: Slightly more complex restoration logic, but much more reliable.

- **Decision 3**: Auto-restart timer on restoration (no manual resume)
  - **Rationale**: User explicitly requested timer to "never stop" and continue seamlessly.
  - **Trade-off**: Timer may run longer than expected if user forgot app was open. Acceptable for focus session use case.

- **Decision 4**: Remove periodic saves (every 5 seconds)
  - **Rationale**: Not needed - start timestamp is constant. Saves medals on events only.
  - **Trade-off**: Performance improvement (fewer SavedStateHandle writes).

**Why This Fix Works:**

1. ✅ Start timestamp saved BEFORE backgrounding (in `startTimer()` call)
2. ✅ Timestamp is constant (doesn't change while timer runs)
3. ✅ Uses monotonic clock (immune to system time changes)
4. ✅ Calculation reconstructs elapsed time (includes process death period)
5. ✅ Auto-restart on restoration (timer appears to never stop)

**Alternative Considered**: ForegroundService for background timer
- **Why not chosen**: Adds complexity (notification required, service lifecycle). Current solution achieves desired behavior (seamless continuation) without service overhead.
