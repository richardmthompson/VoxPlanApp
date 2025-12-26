# Progress Screen Color Improvements

## Background and Motivation

The ProgressScreen currently uses light cyan for the current day, no background for the title, and white/default for the general screen background. User wants to improve the color scheme to match existing palette colors used elsewhere in the app for better visual consistency and reduced eye strain.

## Feature Goal

Update ProgressScreen to use: (1) pale green background for current day card (matching subgoal lists), (2) turquoise background for screen title header (matching app theme), and (3) a complementary background color for the entire screen (not white, not yellow).

## Context

**Target File:** `/Users/richardthompson/StudioProjects/VoxPlanApp/app/src/main/java/com/voxplanapp/ui/goals/ProgressScreen.kt`

**Color Constants** (from Colors.kt):
- `SecondaryColor = Color(0xFFC7E2C9)` - pale green (Colors.kt:9) - used for sub-goal backgrounds
- `TertiaryColor = Color(0xFF82babd)` - turquoise (Colors.kt:11) - only turquoise in palette
- `ToolbarColor = Color(0xFFfff2df)` - pale yellow (Colors.kt:30) - avoid (already used for cards)
- `SecondaryDarkColor = Color(0xFFB2DFDB)` - pale cyan/mint (Colors.kt:10)

**Current Implementation:**
- ProgressScreen.kt:172 - Current day uses `Color(0xFFBAF7FF)` (light cyan)
- ProgressScreen.kt:75-79 - Title "Progress" has no background
- ProgressScreen.kt:62-66 - Column has no background (default white/surface)

**Pattern to Follow:**
- Use existing Colors.kt constants instead of hardcoded Color() values
- Follow pattern from MainScreen.kt and other screens using named color constants
- Add `.background()` modifier with padding for visual hierarchy

**Known Gotchas:**
- **Clarification needed**: User mentioned "turquoise from arrow buttons" but arrow buttons (VoxPlanApp.kt:152-183) only use icon tint colors (PrimaryColor/ActivatedColor), no background. `TertiaryColor` is the only turquoise color in the app's palette - assuming this is what user wants.
- Must ensure text contrast when adding backgrounds (verify readability)
- App uses hardcoded colors throughout (not Material3 colorScheme) - staying consistent with current approach
- No tests exist for ProgressScreen - visual validation required

**Integration Points:**
- ProgressScreen.kt imports from `com.voxplanapp.ui.constants.*`
- Already imports Colors.kt constants (TopAppBarBgColor at line 129)
- No other screens depend on ProgressScreen styling

**Background Color Recommendation:**
Suggest `Color(0xFFF0F8F8)` - very light cyan/mint tint that complements the palette without being too bold. Alternative: use `SecondaryDarkColor` for stronger mint theme.

## Implementation Steps

### 1. Add Color Import (if needed)
**File:** ProgressScreen.kt
**Action:** Verify imports include `SecondaryColor`, `TertiaryColor` from `com.voxplanapp.ui.constants`
**Lines:** Top of file (imports section around line 1-40)

### 2. Update Current Day Card Color
**File:** ProgressScreen.kt:171-175
**Action:** Replace `Color(0xFFBAF7FF)` with `SecondaryColor` (pale green)
**Current:**
```kotlin
val cardColor = when {
    isToday -> Color(0xFFBAF7FF)  // <- CHANGE THIS
    isBeforeToday -> Color(0xFFFFF8DC)
    else -> Color(0xFFEEEEEE)
}
```
**Change to:**
```kotlin
val cardColor = when {
    isToday -> SecondaryColor  // <- Pale green from Colors.kt
    isBeforeToday -> Color(0xFFFFF8DC)
    else -> Color(0xFFEEEEEE)
}
```

### 3. Add Title Header Background
**File:** ProgressScreen.kt:68-85
**Action:** Add `.background(TertiaryColor)` and padding to the Row containing the title
**Current:**
```kotlin
Row(
    modifier = Modifier
        .fillMaxWidth()
        .padding(bottom = 16.dp),
    ...
)
```
**Change to:**
```kotlin
Row(
    modifier = Modifier
        .fillMaxWidth()
        .background(TertiaryColor, RoundedCornerShape(8.dp))  // <- ADD THIS
        .padding(16.dp)  // <- CHANGE: add all-sides padding for visual spacing
        .padding(bottom = 8.dp),  // <- KEEP: extra bottom padding
    ...
)
```

### 4. Add General Screen Background
**File:** ProgressScreen.kt:62-66
**Action:** Add `.background()` to Column modifier
**Current:**
```kotlin
Column(
    modifier = modifier
        .fillMaxSize()
        .padding(16.dp)
)
```
**Change to:**
```kotlin
Column(
    modifier = modifier
        .fillMaxSize()
        .background(Color(0xFFF0F8F8))  // <- ADD: Very light cyan/mint tint
        .padding(16.dp)
)
```

**Note:** If user wants stronger color, can use `SecondaryDarkColor` instead.

### 5. Verify Imports
**File:** ProgressScreen.kt (top)
**Action:** Ensure these imports exist:
```kotlin
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import com.voxplanapp.ui.constants.SecondaryColor
import com.voxplanapp.ui.constants.TertiaryColor
```

### 6. Build and Test
**Commands:**
```bash
./gradlew assembleDebug
```

**Manual Testing:**
1. Launch app on emulator/device
2. Navigate to Progress screen (bottom nav, 3rd icon)
3. Verify visual changes:
   - Current day card has pale green background (not cyan)
   - "Progress" title header has turquoise background with padding
   - Overall screen has light mint/cyan tint (not white)
   - Text is readable with good contrast
4. Test both current week and other weeks (navigate with arrow buttons in header)

## Success Definition

- ✅ Build completes without errors
- ✅ Current day card uses pale green background (matches subgoal lists color)
- ✅ Title header has turquoise background with proper padding
- ✅ Screen background is light mint/cyan (not white, easier on eyes)
- ✅ All text remains readable with sufficient contrast
- ✅ No visual regressions on other days (past/future cards still styled correctly)
- ✅ User confirms turquoise color is correct (pending clarification on arrow button reference)

## Open Questions

**For User Approval:**
1. **Turquoise color**: Arrow buttons don't have a background color (only icon tint). The only turquoise in the app's color palette is `TertiaryColor (0xFF82babd)`. Is this the color you want for the title? Or did you mean a different turquoise?

2. **General background**: Recommending `Color(0xFFF0F8F8)` - very light cyan/mint. Would you prefer:
   - This light tint (subtle, easy on eyes)
   - `SecondaryDarkColor (0xFFB2DFDB)` (stronger mint/cyan)
   - A different complementary color?

## Files Modified

- `/Users/richardthompson/StudioProjects/VoxPlanApp/app/src/main/java/com/voxplanapp/ui/goals/ProgressScreen.kt` (~10 lines changed)

## Estimated Scope

- **Complexity:** Simple (single file, color updates only)
- **Lines changed:** ~10-15 lines
- **Risk:** Low (cosmetic changes, no logic modifications)
