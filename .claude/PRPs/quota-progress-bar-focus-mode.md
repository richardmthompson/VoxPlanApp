# Task PRP: Add Quota Progress Bar to Focus Mode Task Card

## Task Overview

Add a visual quota progress bar to the green task card at the top of Focus Mode screen. The card will show progress toward the daily quota by combining:
- Time already banked for today
- Time accrued in the Time Vault (medals)
- Time on the current timer

The bar fills left-to-right with bright green, turns yellow when quota is complete, and plays the medal completion sound.

before the time is actually banked, we get the filling bright green effect of the so-called power bar. And let's make it so that it's only when the time has actually been banked for that day, for that task or goal, that then we have the sort of turning gold effect. And then if we do it that way when the medal is banked, we are already making that nice sound, so we don't have to do an additional sound making event because it's only once the time is banked that the green bar will turn to yellow.

## Context

### Current State

**EventBox Component** (`FocusModeScreen.kt:439-476`)
- Green card displaying goal title
- Uses solid `EventBoxColor` (0xFF41c300) background
- No quota visualization
- Located at lines 153-158 in main FocusModeScreen composable

**Current Implementation:**
```kotlin
Box(
    modifier = Modifier
        .fillMaxWidth(0.8f)
        .height(48.dp)
        .background(EventBoxColor, shape = RoundedCornerShape(MediumDp))
        .padding(MediumDp)
) {
    Text(
        text = goalUiState?.goal?.title ?: "",
        style = MaterialTheme.typography.bodyMedium,
        color = Color.Black,
        modifier = Modifier.align(Alignment.CenterStart)
    )
}
```

### Architecture Context

**FocusViewModel State** (`FocusViewModel.kt:611-640`)
```kotlin
data class FocusUiState (
    val totalAccruedTime: Long = 0L,  // Total time on timer (seconds)
    val currentTime: Long = 0L,       // Current timer time (seconds)
    val medals: List<Medal> = emptyList(),  // Time Vault medals
    val clockFaceMins: Float = 30f,
    val date: LocalDate? = null,
    // ... other fields
)

data class Medal(val value: Int, val type: MedalType)  // value = minutes
```

**Time Tracking Sources:**
1. **Banked Time** - `TimeBankRepository.getEntriesForDate(date)` returns `Flow<List<TimeBank>>`
   - Each `TimeBank` entry has `duration: Int` field (minutes)
   - Filter by `goalId` and `date`

2. **Time Vault (Medals)** - `focusUiState.medals`
   - Each `Medal.value` = minutes
   - Sum all medal values for total vault time

3. **Current Timer** - `focusUiState.currentTime`
   - In seconds, convert to minutes

**Quota System:**
- `QuotaRepository.getQuotaForGoal(goalId)` returns `Flow<Quota?>`
- `Quota.dailyMinutes` = target minutes
- `Quota.activeDays` = "1111100" string (Mon-Sun active days)
- Check if today is active: `quotaRepository.isQuotaActiveForDate(quota, date)`

### Color Specifications

**Current Colors** (`ui/constants/Colors.kt:15`)
```kotlin
val EventBoxColor = Color(0xFF41c300)  // Bright green (current card background)
```

**Target Colors:**
- **Unfilled background**: `Color(0xFF002702)` - darker green (same as clock face background)
- **Filling progress**: `Color(0xFF41c300)` - bright green (current EventBoxColor)
- **Completion state**: `Color(0xFFFFC107)` - yellow (rim color from clock)

### Sound Effect

**Medal Completion Sound** (`FocusViewModel.kt:377`)
- File: `R.raw.mario_coin`
- Played via: `soundPlayer.playSound(R.raw.mario_coin)`

### Dependencies

**Current FocusViewModel Dependencies** (`AppViewModelProvider.kt:64-71`)
```kotlin
FocusViewModel(
    savedStateHandle,
    todoRepository = container.todoRepository,
    eventRepository = container.eventRepository,
    timeBankRepository = container.timeBankRepository,
    soundPlayer = container.soundPlayer,
    sharedViewModel = sharedViewModel
)
// ❌ Missing: quotaRepository
```

**QuotaRepository is available** in `AppContainer.kt:18` but not injected into FocusViewModel.

### Existing Patterns

**Progress Bar Pattern** (from codebase):
- Use `Box` with layered backgrounds
- Bottom layer = unfilled background
- Top layer with `fillMaxWidth(progress)` = filled portion

**Flow Collection in ViewModel:**
```kotlin
// Pattern used in SchedulerViewModel and DailyViewModel
viewModelScope.launch {
    combine(
        flow1,
        flow2,
        flow3
    ) { data1, data2, data3 ->
        // Calculate combined state
    }.collect { result ->
        _uiState.value = result
    }
}
```

### Gotchas

**Issue 1:** Time unit inconsistencies
- **Timer**: seconds (Long)
- **Medals**: minutes (Int)
- **TimeBank**: minutes (Int)
- **Quota**: minutes (Int)
- **Fix:** Convert timer to minutes: `currentTime / 60`

**Issue 2:** Null quota handling
- **Context:** Not all goals have quotas
- **Fix:** Only show progress bar if quota exists AND is active for today

**Issue 3:** State management complexity
- **Context:** Need to combine multiple flows (quota, time bank, timer state)
- **Fix:** Use `combine` operator in ViewModel to create derived state

**Issue 4:** Sound should play only once when crossing threshold
- **Context:** State updates frequently, could trigger sound multiple times
- **Fix:** Track previous completion state, only play sound when transitioning from incomplete → complete

**Issue 5:** Progress can exceed 100%
- **Context:** User might accrue more time than quota
- **Fix:** Cap visual progress at 100% (1.0f), but still show actual numbers

## Task Breakdown

### PHASE 1: Add QuotaRepository to FocusViewModel

**ACTION** `app/src/main/java/com/voxplanapp/ui/focusmode/FocusViewModel.kt:35-41`
- **OPERATION:** Add quotaRepository parameter to constructor
- **CHANGE:**
  ```kotlin
  // BEFORE (lines 35-42):
  class FocusViewModel(
      savedStateHandle: SavedStateHandle,
      private val todoRepository: TodoRepository,
      private val eventRepository: EventRepository,
      private val timeBankRepository: TimeBankRepository,
      private val sharedViewModel: SharedViewModel,
      private val soundPlayer: SoundPlayer
  ): ViewModel() {

  // AFTER:
  class FocusViewModel(
      savedStateHandle: SavedStateHandle,
      private val todoRepository: TodoRepository,
      private val eventRepository: EventRepository,
      private val timeBankRepository: TimeBankRepository,
      private val quotaRepository: QuotaRepository,
      private val sharedViewModel: SharedViewModel,
      private val soundPlayer: SoundPlayer
  ): ViewModel() {
  ```
- **VALIDATE:** Build project - `./gradlew assembleDebug`
- **IF_FAIL:** Check imports - add `import com.voxplanapp.data.QuotaRepository`
- **EXPECTED:** Compilation error in AppViewModelProvider (expected - we'll fix next)

**ACTION** `app/src/main/java/com/voxplanapp/AppViewModelProvider.kt:64-71`
- **OPERATION:** Inject quotaRepository into FocusViewModel
- **CHANGE:**
  ```kotlin
  // BEFORE (lines 64-71):
  initializer {
      FocusViewModel(
          this.createSavedStateHandle(),
          todoRepository = voxPlanApplication().container.todoRepository,
          eventRepository = voxPlanApplication().container.eventRepository,
          timeBankRepository = voxPlanApplication().container.timeBankRepository,
          soundPlayer = voxPlanApplication().container.soundPlayer,
          sharedViewModel = sharedViewModel
      )
  }

  // AFTER:
  initializer {
      FocusViewModel(
          this.createSavedStateHandle(),
          todoRepository = voxPlanApplication().container.todoRepository,
          eventRepository = voxPlanApplication().container.eventRepository,
          timeBankRepository = voxPlanApplication().container.timeBankRepository,
          quotaRepository = voxPlanApplication().container.quotaRepository,
          soundPlayer = voxPlanApplication().container.soundPlayer,
          sharedViewModel = sharedViewModel
      )
  }
  ```
- **VALIDATE:** Build project - `./gradlew assembleDebug`
- **IF_FAIL:** Check that quotaRepository exists in AppContainer.kt
- **EXPECTED:** Successful compilation

### PHASE 2: Add Quota Progress State to FocusUiState

**ACTION** `app/src/main/java/com/voxplanapp/ui/focusmode/FocusViewModel.kt:611-640`
- **OPERATION:** Add quota progress fields to FocusUiState
- **CHANGE:**
  ```kotlin
  // Add these fields to FocusUiState data class (around line 640, before closing paren)

      // quota progress fields
      val quota: Quota? = null,
      val bankedMinutesToday: Int = 0,
      val totalProgressMinutes: Int = 0,
      val quotaProgress: Float = 0f,  // 0.0 to 1.0+ (can exceed 100%)
      val isQuotaComplete: Boolean = false,
  )
  ```
- **VALIDATE:** Build project - `./gradlew assembleDebug`
- **IF_FAIL:** Check import: `import com.voxplanapp.data.Quota`
- **EXPECTED:** Successful compilation

### PHASE 3: Create Quota Progress Calculation Logic in ViewModel

**ACTION** `app/src/main/java/com/voxplanapp/ui/focusmode/FocusViewModel.kt` (after line 81, in init block)
- **OPERATION:** Set up quota progress tracking
- **CHANGE:**
  ```kotlin
  // Add after setupTimeBank() call in init block (line 81):

  init {
      loadInitialData()
      checkStartTime()
      setupTimeBank()
      setupQuotaTracking()  // ADD THIS LINE
  }

  // Then add this new function after setupTimeBank() (around line 160):

  private fun setupQuotaTracking() {
      viewModelScope.launch {
          // Get the goal ID
          val currentGoalId = goalId ?: return@launch

          combine(
              quotaRepository.getQuotaForGoal(currentGoalId),
              timeBankRepository.getEntriesForDate(LocalDate.now()),
              snapshotFlow { focusUiState }
          ) { quota, timeBankEntries, currentFocusState ->

              // Only process if quota exists and is active for today
              if (quota == null || !quotaRepository.isQuotaActiveForDate(quota, LocalDate.now())) {
                  return@combine Triple(null, 0, false)
              }

              // Calculate banked minutes for this goal today
              val bankedMinutes = timeBankEntries
                  .filter { it.goalId == currentGoalId }
                  .sumOf { it.duration }

              // Calculate Time Vault (medals) minutes
              val vaultMinutes = currentFocusState.medals.sumOf { it.value }

              // Calculate current timer minutes
              val timerMinutes = (currentFocusState.currentTime / 60).toInt()

              // Total progress
              val totalMinutes = bankedMinutes + vaultMinutes + timerMinutes

              // Progress ratio
              val progress = if (quota.dailyMinutes > 0) {
                  totalMinutes.toFloat() / quota.dailyMinutes.toFloat()
              } else 0f

              // Check if complete
              val isComplete = totalMinutes >= quota.dailyMinutes

              Triple(
                  QuotaProgressData(
                      quota = quota,
                      bankedMinutes = bankedMinutes,
                      totalMinutes = totalMinutes,
                      progress = progress,
                      isComplete = isComplete
                  ),
                  totalMinutes,
                  isComplete
              )

          }.collect { (progressData, totalMinutes, isComplete) ->

              // Check if we just completed the quota (play sound)
              val wasComplete = focusUiState.isQuotaComplete
              if (!wasComplete && isComplete && progressData != null) {
                  soundPlayer.playSound(R.raw.mario_coin)
              }

              // Update state
              focusUiState = focusUiState.copy(
                  quota = progressData?.quota,
                  bankedMinutesToday = progressData?.bankedMinutes ?: 0,
                  totalProgressMinutes = totalMinutes,
                  quotaProgress = progressData?.progress ?: 0f,
                  isQuotaComplete = isComplete
              )
          }
      }
  }

  // Add helper data class at bottom of file (after Medal and FocusUiState, around line 645):

  private data class QuotaProgressData(
      val quota: Quota,
      val bankedMinutes: Int,
      val totalMinutes: Int,
      val progress: Float,
      val isComplete: Boolean
  )
  ```
- **VALIDATE:** Build project - `./gradlew assembleDebug`
- **IF_FAIL:**
  - Check imports: `import androidx.compose.runtime.snapshotFlow`, `import kotlinx.coroutines.flow.combine`
  - Verify quotaRepository is available
  - Check that goalId is accessible in this scope
- **DEBUG STRATEGY:**
  - Add logging: `Log.d("QuotaProgress", "Total: $totalMinutes / ${quota.dailyMinutes} = $progress")`
  - Verify quota is retrieved: `Log.d("QuotaProgress", "Quota: ${quota?.dailyMinutes}")`
- **EXPECTED:** Successful compilation, quota progress state updates during focus sessions

### PHASE 4: Update EventBox UI to Show Progress Bar

**ACTION** `app/src/main/java/com/voxplanapp/ui/focusmode/FocusModeScreen.kt:439-476`
- **OPERATION:** Replace EventBox composable with quota-aware progress bar version
- **CHANGE:**
  ```kotlin
  // REPLACE entire EventBox function (lines 439-476):

  @Composable
  fun EventBox(
      focusUiState: FocusUiState,
      goalUiState: GoalWithSubGoals?,
      eventUiState: Event?,
      modifier: Modifier = Modifier
  ) {
      Column (modifier = modifier.fillMaxWidth()) {
          Text(
              text = focusUiState.startTime?.format(DateTimeFormatter.ofPattern("HH:mm")) ?: "",
              style = MaterialTheme.typography.bodyMedium,
              modifier = modifier.align(Alignment.Start)
          )

          // Determine colors based on quota state
          val (backgroundColor, progressColor) = when {
              focusUiState.quota == null -> {
                  // No quota: use original solid green
                  Pair(EventBoxColor, EventBoxColor)
              }
              focusUiState.isQuotaComplete -> {
                  // Quota complete: yellow
                  Pair(Color(0xFF002702), Color(0xFFFFC107))
              }
              else -> {
                  // Quota in progress: dark green background, bright green progress
                  Pair(Color(0xFF002702), EventBoxColor)
              }
          }

          // Progress value capped at 1.0 for visual display
          val displayProgress = focusUiState.quotaProgress.coerceIn(0f, 1.0f)

          Box(
              modifier = Modifier
                  .fillMaxWidth(0.8f)
                  .height(48.dp)
                  .background(backgroundColor, shape = RoundedCornerShape(MediumDp))
          ) {
              // Progress bar (fills left to right)
              if (focusUiState.quota != null) {
                  Box(
                      modifier = Modifier
                          .fillMaxWidth(displayProgress)
                          .height(48.dp)
                          .background(progressColor, shape = RoundedCornerShape(MediumDp))
                  )
              }

              // Goal title text on top
              Text(
                  text = goalUiState?.goal?.title ?: "",
                  style = MaterialTheme.typography.bodyMedium,
                  color = Color.Black,
                  modifier = Modifier
                      .align(Alignment.CenterStart)
                      .padding(MediumDp)
              )

              // Optional: Show progress numbers on right side
              if (focusUiState.quota != null) {
                  Text(
                      text = "${focusUiState.totalProgressMinutes}/${focusUiState.quota!!.dailyMinutes}m",
                      style = MaterialTheme.typography.bodySmall,
                      color = Color.Black,
                      fontWeight = FontWeight.Bold,
                      modifier = Modifier
                          .align(Alignment.CenterEnd)
                          .padding(MediumDp)
                  )
              }
          }

          Text(
              text = eventUiState?.endTime?.format(DateTimeFormatter.ofPattern("HH:mm")) ?: "",
              style = MaterialTheme.typography.bodyMedium,
              modifier = modifier
                  .fillMaxWidth()
                  .align(Alignment.Start)
          )
      }
  }
  ```
- **VALIDATE:** Build and run on emulator - `./gradlew installDebug`
- **IF_FAIL:**
  - Check imports for FontWeight if showing progress numbers
  - Verify Color values match constants
  - Check RoundedCornerShape doesn't cause visual glitches
- **DEBUG STRATEGY:**
  - Test with goal without quota → should show solid green
  - Test with goal with quota → should show dark green with filling bright green
  - Log progress values: `Log.d("EventBox", "Progress: $displayProgress, Complete: ${focusUiState.isQuotaComplete}")`
- **EXPECTED:** Visual progress bar that fills left to right

### PHASE 5: Integration Testing

**ACTION** Manual testing on device/emulator
- **OPERATION:** Test complete quota progress flow
- **TEST STEPS:**

  **Setup:**
  1. Create a test goal: "Test Quota Goal"
  2. Set daily quota: 60 minutes, active today
  3. Bank 20 minutes for today using time banking feature

  **Test 1: Initial State with Banked Time**
  4. Navigate to Focus Mode for "Test Quota Goal"
  5. **VERIFY:**
     - Card shows dark green background
     - Left portion (1/3) filled with bright green
     - Shows "20/60m" on right side

  **Test 2: Timer Progress**
  6. Start timer, let run for 15 minutes
  7. **VERIFY:**
     - Progress bar grows to ~58% filled (35/60 minutes)
     - Still bright green
     - Numbers update to "35/60m"

  **Test 3: Earn Medal (Time Vault)**
  8. Complete a 30-minute timer cycle to earn medal
  9. **VERIFY:**
     - Medal appears in Time Vault
     - Progress bar includes medal time
     - Shows "65/60m" (exceeds quota)
     - Bar is 100% filled (capped visually)

  **Test 4: Quota Completion**
  10. Clear medals, restart
  11. Accrue exactly 40 more minutes (timer + vault) to reach 60 total
  12. **VERIFY:**
     - At 60 minutes, bar turns yellow
     - Mario coin sound plays ONCE
     - Shows "60/60m"

  **Test 5: No Quota Goal**
  13. Navigate to Focus Mode for goal without quota
  14. **VERIFY:**
     - Card shows solid bright green (original behavior)
     - No progress numbers displayed

  **Test 6: Inactive Quota Day**
  15. Create goal with quota active Mon-Fri
  16. Test on Saturday
  17. **VERIFY:**
     - Card shows solid green (no progress bar)
     - Quota ignored for inactive day

- **VALIDATE:** All test cases pass
- **IF_FAIL:**
  - Check quota active days calculation
  - Verify time calculations (seconds → minutes conversion)
  - Check combine() flow emits on all state changes
  - Verify TimeBank filtering by goalId
- **DEBUG STRATEGY:**
  ```bash
  # Monitor logs
  adb logcat -s FocusViewModel QuotaProgress EventBox

  # Check database
  adb shell
  run-as com.voxplanapp
  sqlite3 databases/todo-db
  SELECT * FROM TimeBank WHERE date = (SELECT date('now'));
  SELECT * FROM Quota;
  ```
- **ROLLBACK:** If critical issues, revert EventBox to original implementation

### PHASE 6: Edge Case Testing

**ACTION** Test edge cases
- **TEST CASES:**

  **Case 1: Quota exactly met**
  - Set quota 30 mins, accrue exactly 30
  - **VERIFY:** Turns yellow at 30, not 31

  **Case 2: Large overrun**
  - Set quota 30 mins, accrue 120 mins
  - **VERIFY:** Bar fills 100% (not 400%), shows "120/30m"

  **Case 3: Rapid state changes**
  - Timer running, bank time simultaneously from another session
  - **VERIFY:** Progress updates smoothly, no sound spam

  **Case 4: Multiple medals**
  - Earn 3 bronze medals (30m each) in Time Vault
  - **VERIFY:** All medals counted: 90 minutes in vault

  **Case 5: Date boundary**
  - Bank time at 11:59 PM, test at 12:01 AM next day
  - **VERIFY:** Previous day's time not counted

  **Case 6: Quota change mid-session**
  - Start with 60m quota, edit goal to change to 90m
  - **VERIFY:** Progress bar adjusts to new quota

- **VALIDATE:** All edge cases handled gracefully
- **IF_FAIL:**
  - Add debouncing to sound trigger
  - Ensure LocalDate.now() is consistent throughout calculations
  - Check Flow collection handles rapid emissions
- **ROLLBACK:** Document any unhandled edge cases for future work

### PHASE 7: Performance Validation

**ACTION** Check performance impact
- **OPERATION:** Profile rendering and database queries
- **CHECKS:**
  1. **Frame Rate:** Monitor UI thread with Android Profiler
     - **VERIFY:** 60 FPS maintained during timer running
     - **VERIFY:** No jank when progress bar updates

  2. **Database Queries:**
     - **VERIFY:** TimeBank query runs once per minute max (not every second)
     - **VERIFY:** Quota query cached (not repeated)

  3. **Memory:**
     - **VERIFY:** No memory leaks from Flow collection
     - **VERIFY:** ViewModelScope properly cancels on destroy

- **VALIDATE:** No performance degradation
- **IF_FAIL:**
  - Add `.distinctUntilChanged()` to reduce emissions
  - Use `SharingStarted.WhileSubscribed(5000L)` for flows
  - Sample timer updates to minutes instead of seconds
- **DEBUG STRATEGY:**
  ```bash
  # Profile with Android Studio Profiler
  # Check database queries:
  adb shell setprop log.tag.SQLiteStatements VERBOSE
  adb logcat -s SQLiteStatements
  ```

## Validation Strategy

### Build Validation
```bash
# Clean build
./gradlew clean assembleDebug

# Run lint
./gradlew lint
```
**Success Criteria:**
- No compilation errors
- No new lint warnings
- No deprecated API usage

### Unit Test Recommendations
```kotlin
// Suggested test cases (not required for initial implementation)
@Test
fun `calculateQuotaProgress with banked time`() {
    val banked = 20
    val vault = 10
    val timer = 30L // seconds
    val quota = 60

    val progress = (banked + vault + (timer / 60)) / quota.toFloat()
    assertEquals(0.5f, progress, 0.01f)
}

@Test
fun `quota complete detection`() {
    val totalMinutes = 60
    val quotaMinutes = 60
    assertTrue(totalMinutes >= quotaMinutes)
}
```

### Runtime Validation
```bash
# Install and monitor
./gradlew installDebug
adb logcat -s FocusViewModel QuotaProgress EventBox TimeBank
```
**Success Criteria:**
- Progress bar renders correctly
- Colors transition properly at completion
- Sound plays exactly once
- No crashes or null pointer exceptions

## Rollback Strategy

### Phase-by-Phase Rollback

**If Phase 1 fails (dependency injection):**
```bash
git checkout app/src/main/java/com/voxplanapp/ui/focusmode/FocusViewModel.kt
git checkout app/src/main/java/com/voxplanapp/AppViewModelProvider.kt
```

**If Phase 3 fails (quota calculation logic):**
- Comment out `setupQuotaTracking()` call in init block
- Remove quota fields from FocusUiState (keep defaults)
- Revert to Phase 2 state

**If Phase 4 fails (UI rendering):**
- Revert EventBox to original implementation (lines 439-476)
- Keep backend quota tracking for future retry

**Complete rollback:**
```bash
# Revert all changes
git diff HEAD --name-only | xargs git checkout
./gradlew clean build
```

## Success Criteria

- [ ] QuotaRepository injected into FocusViewModel
- [ ] Quota progress state calculated correctly combining 3 time sources
- [ ] Progress bar renders with correct colors
  - [ ] Dark green background when quota active
  - [ ] Bright green fill for progress
  - [ ] Yellow fill when complete
  - [ ] Solid green when no quota
- [ ] Progress fills left to right based on quota percentage
- [ ] Progress capped visually at 100% even when exceeded
- [ ] Mario coin sound plays when quota reached
- [ ] Sound plays only once per completion
- [ ] Progress numbers displayed (e.g., "45/60m")
- [ ] Works correctly for goals without quotas (original behavior)
- [ ] Works correctly when quota inactive for current day
- [ ] No performance degradation (60 FPS maintained)
- [ ] No memory leaks from Flow collection
- [ ] Build succeeds with no new warnings

## Files Modified

1. `app/src/main/java/com/voxplanapp/ui/focusmode/FocusViewModel.kt`
   - Add quotaRepository parameter
   - Add quota progress fields to FocusUiState
   - Add setupQuotaTracking() function
   - Add QuotaProgressData helper class

2. `app/src/main/java/com/voxplanapp/AppViewModelProvider.kt`
   - Inject quotaRepository into FocusViewModel

3. `app/src/main/java/com/voxplanapp/ui/focusmode/FocusModeScreen.kt`
   - Replace EventBox composable with quota-aware progress bar version

## Dependencies

**No new dependencies required** - All components exist:
- QuotaRepository (already in AppContainer)
- TimeBankRepository (already injected in FocusViewModel)
- StateFlow/Flow (already used)
- SoundPlayer (already injected)

**Imports to add:**
```kotlin
// FocusViewModel.kt
import com.voxplanapp.data.QuotaRepository
import com.voxplanapp.data.Quota
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.flow.combine

// FocusModeScreen.kt (if not already present)
import androidx.compose.ui.text.font.FontWeight
```

## Security Considerations

**Data Integrity:**
- Quota queries use indexed goalId (Room DAO)
- TimeBank filtering prevents cross-goal data leakage
- No user input validation needed (read-only display)

**State Management:**
- Flow collection in viewModelScope ensures proper cleanup
- No shared mutable state across ViewModels
- Quota data cached in StateFlow (single source of truth)

## Performance Impact

**Expected:**
- **Minimal** - Progress bar is simple Box layering
- Database queries: 1 quota query (cached), 1 TimeBank query per minute max
- Flow combines efficiently with `distinctUntilChanged()`

**Optimizations:**
- Consider sampling timer updates to minute granularity instead of seconds
- Use `SharingStarted.WhileSubscribed(5_000L)` if memory issues arise
- Progress bar recomposition scoped to EventBox only

## Future Enhancements

1. **Animate progress bar fill** - Use `animateFloatAsState` for smooth transitions
2. **Color customization** - Per-goal quota colors
3. **Daily streak indicator** - Show consecutive days quota met
4. **Quota breakdown tooltip** - Tap to see banked/vault/timer breakdown
5. **Overflow visualization** - Different color/pattern when exceeding quota
6. **Weekly quota view** - Aggregate weekly progress on card

## Notes

- This implementation follows the existing pattern of Flow-based state management used in SchedulerViewModel and DailyViewModel
- The progress bar design aligns with the "power bar" metaphor already present in the timer system (FULLBAR_MINS = 60)
- Sound effect reuses existing medal completion sound for consistency
- The visual design uses colors already present in the Focus Mode UI for cohesion
- No database migrations needed (quota system already exists)
- Compatible with existing time banking and medal systems
