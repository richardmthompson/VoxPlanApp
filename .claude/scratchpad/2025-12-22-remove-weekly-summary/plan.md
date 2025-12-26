# Remove Weekly Summary Card from Progress Screen

## Background and Motivation

The Progress screen currently displays a weekly summary card showing cumulative progress for the selected week with emerald gem icons for daily completion status. This card needs to be removed per user request.

## Feature Goal

Remove the weekly summary card from the Progress screen while maintaining all other progress tracking functionality.

## Context

**Affected Files:**
- `app/src/main/java/com/voxplanapp/ui/goals/ProgressScreen.kt:87-100` (Card wrapper)
- `app/src/main/java/com/voxplanapp/ui/goals/ProgressScreen.kt:117-152` (WeeklySummary composable)
- `app/src/main/java/com/voxplanapp/ui/goals/ProgressViewModel.kt:160-165` (ProgressUiState.weekTotal property)
- `app/src/main/java/com/voxplanapp/ui/goals/ProgressViewModel.kt:181-183` (WeekTotal data class)

**Pattern to Follow:**
- Standard component removal - delete composable and clean up unused ViewModel state
- No external dependencies or references found

**Known Gotchas:**
- None - component is fully self-contained
- No tests exist for this functionality
- No navigation or deep links to weekly summary

**Integration Points:**
- WeeklySummary is private to ProgressScreen.kt (line 118)
- WeekTotal used only within ProgressViewModel.kt and ProgressScreen.kt
- Zero external coupling

**Validation:**
- Build succeeds with `./gradlew assembleDebug`
- Manual test: Navigate to Progress screen, verify weekly summary card is gone
- Manual test: Verify remaining progress tracking still works

## Implementation Steps

1. **REMOVE** ProgressScreen.kt:87-100 - Delete the Card wrapper that contains WeeklySummary
2. **REMOVE** ProgressScreen.kt:117-152 - Delete the entire WeeklySummary composable function
3. **REMOVE** ProgressViewModel.kt:160-165 - Remove `weekTotal: WeekTotal?` property from ProgressUiState data class
4. **REMOVE** ProgressViewModel.kt:181-183 - Delete the WeekTotal data class (no longer needed)
5. **CLEAN UP** ProgressViewModel.kt - Remove any code that calculates/populates weekTotal in the ViewModel
6. **BUILD** Run `./gradlew assembleDebug` to verify compilation
7. **TEST** Manual verification on emulator/device

## Success Definition

- Build completes successfully without errors
- Progress screen displays without weekly summary card
- Individual goal progress tracking still works correctly
- No crashes or visual regressions in Progress screen
- Clean code with no unused imports or dead code
