# Fix Medal Tripling Bug in Process Death Recovery

**Beads Issue**: VoxPlanApp-kgk
**Created**: 2025-12-26
**Tier**: 1 (Simple)
**Confidence**: High

---

## Background and Motivation

When backgrounding the app, killing the process, and returning after the timer would have completed a revolution, **3x the expected medals are awarded**. For example, user should receive 1x 10-minute medal but receives 3x 10-minute medals instead.

**Root Cause**: Process death recovery code at `FocusViewModel.kt:161-176` calculates `completeRevolutions` from TOTAL elapsed time since `startTimestamp`, but then ADDS these to `existingMedals` (which already contains medals for some of those revolutions), causing double-counting.

---

## Feature Goal

Fix medal calculation during process death recovery to award only NEW medals (not already counted).

---

## Context

**Affected file**: `app/src/main/java/com/voxplanapp/ui/focusmode/FocusViewModel.kt:161-176`

**Current buggy logic**:
```kotlin
// Line 163: Calculate TOTAL revolutions from entire elapsed time
val completeRevolutions = (totalElapsedTime / revolutionMillis).toInt()

// Line 166: Restore medals already awarded
val existingMedals = restoreMedals()

// Line 171: BUG - treats total as new
val newMedalsEarned = completeRevolutions  // Should subtract existing!

// Line 176: Adds "new" to existing → double-counting
val allMedals = existingMedals + newMedals
```

**Pattern to follow**:
- Codebase uses `coerceAtLeast()` for safe bounds checking
- Examples: FocusViewModel.kt:414, DailyViewModel.kt:92-93
- Kotlin standard library: `Int.coerceAtLeast(minimumValue)` prevents negative values

**Known gotcha**:
- Comment at lines 169-170 is INCORRECT:
  ```kotlin
  // Note: completeRevolutions represents NEW revolutions since last medal
  // (startTimestamp resets after each medal, so this is additional medals)
  ```
  This comment is misleading - `startTimestamp` does NOT reset after each medal in process death scenario. The comment needs updating.

**Integration points**:
- Process death recovery is only triggered during `init` block
- Normal runtime medal awarding uses different code path (`awardMedal()` at line 640)
- This fix only affects app recovery after process death

**Validation**:
- Manual testing required (no unit tests exist for FocusViewModel)
- Test scenario: Background app → Kill process → Wait for revolution time → Return to app

---

## Implementation Steps

1. **MODIFY** `FocusViewModel.kt:171`
   - **From**: `val newMedalsEarned = completeRevolutions`
   - **To**: `val newMedalsEarned = (completeRevolutions - existingMedals.size).coerceAtLeast(0)`
   - **Rationale**: Subtract already-awarded medals from total count, ensure non-negative

2. **UPDATE** misleading comment at lines 169-170
   - **From**:
     ```kotlin
     // Note: completeRevolutions represents NEW revolutions since last medal
     // (startTimestamp resets after each medal, so this is additional medals)
     ```
   - **To**:
     ```kotlin
     // Note: completeRevolutions represents TOTAL revolutions from startTimestamp
     // Subtract existingMedals.size to get only NEW medals not yet awarded
     ```

3. **VERIFY** the fix logic:
   - If user had 0 medals and 3 revolutions elapsed: `(3 - 0).coerceAtLeast(0) = 3` ✓
   - If user had 2 medals and 3 revolutions elapsed: `(3 - 2).coerceAtLeast(0) = 1` ✓
   - If user had 3 medals and 3 revolutions elapsed: `(3 - 3).coerceAtLeast(0) = 0` ✓
   - Edge case: `(2 - 5).coerceAtLeast(0) = 0` (no negative medals) ✓

4. **BUILD** the project
   ```bash
   ./gradlew assembleDebug
   ```

5. **MANUAL TEST** (process death scenario):
   - Start Focus Mode with 5-minute revolution
   - Wait 2 minutes (no medal yet)
   - Background app
   - Kill app process: `adb shell am kill com.voxplanapp`
   - Wait 5 more minutes (total 7 mins = 1 revolution + 2 mins remainder)
   - Return to app
   - **Expected**: 1 medal awarded, timer shows 2:00 remaining
   - **Previously**: 3 medals awarded (bug)

6. **TEST** edge cases:
   - Process death at exactly revolution boundary (e.g., 5:00)
   - Process death with multiple existing medals
   - Process death with no elapsed time

---

## Success Definition

**Build succeeds**:
- ✅ `./gradlew assembleDebug` completes without errors

**Correct medal count**:
- ✅ After 1 revolution + process death: Award exactly 1 medal (not 3)
- ✅ After 2 revolutions + process death: Award exactly 2 NEW medals (not 6)
- ✅ Edge case: If medals already awarded = total revolutions → 0 new medals

**Comment accuracy**:
- ✅ Updated comment reflects actual behavior (total revolutions, not new)

**No regressions**:
- ✅ Normal runtime medal awarding still works (not affected by this change)
- ✅ Timer remainder calculation correct (shows correct time after medals)

---

## Related Issues

- **VoxPlanApp-eey**: Timer resets to 0 after process death medal award (related, same code path)
- **VoxPlanApp-ccs**: Changing revolution setting only awards 1 medal (similar medal calculation logic)

These bugs may share root causes in medal calculation and should be reviewed together.
