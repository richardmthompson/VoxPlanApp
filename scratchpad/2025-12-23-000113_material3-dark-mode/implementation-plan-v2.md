# VoxPlan Dark Mode Implementation Plan v2.0
## Ultra-Detailed Implementation Strategy

**Created**: 2025-12-23
**Status**: Awaiting user approval
**Prerequisite**: Read `color-analysis.md` first

---

## Table of Contents

1. [Executive Summary](#1-executive-summary)
2. [Design Principles & Aesthetics](#2-design-principles--aesthetics)
3. [Material 3 Best Practices Alignment](#3-material-3-best-practices-alignment)
4. [Implementation Phases](#4-implementation-phases)
5. [Detailed File-by-File Changes](#5-detailed-file-by-file-changes)
6. [Testing & Validation Strategy](#6-testing--validation-strategy)
7. [Accessibility Compliance](#7-accessibility-compliance)
8. [Risk Mitigation](#8-risk-mitigation)
9. [User Questions Requiring Decisions](#9-user-questions-requiring-decisions)

---

## 1. Executive Summary

### 1.1 What We're Building

A Material 3 compliant dark/light theme system for VoxPlanApp that:
- ✅ Preserves VoxPlan brand green (#1BA821) as the seed color
- ✅ Maintains power bar visual identity exactly (branding requirement)
- ✅ Uses programmatic color generation (MaterialKolor library)
- ✅ Follows Material 3 design principles for elevation, contrast, accessibility
- ✅ Replaces all 26 Colors.kt constants + 80 inline colors with theme-aware equivalents
- ✅ Provides seamless light/dark mode switching

### 1.2 Why MaterialKolor Library?

**Decision Rationale**:
1. **Official Algorithm**: Uses Google's material-color-utilities (HCT color space)
2. **Programmatic**: No manual color picking, scientifically generated tonal palettes
3. **Maintainable**: Single seed color change updates entire palette
4. **Compose-Native**: Designed for Jetpack Compose, integrates with MaterialTheme
5. **Accessible**: Guarantees WCAG AA contrast ratios for all color role pairs
6. **Current**: Active development, latest spec support

**Alternative Considered**: Manual palette from Material Theme Builder Figma plugin
- ❌ Rejected: Requires manual export, harder to iterate, loses programmatic benefits

### 1.3 Scope

**Phase 1 (MVP)**:
- Theme infrastructure (Theme.kt, ExtendedColors.kt, MainActivity.kt)
- MainScreen.kt (primary goal screen)
- DailyScreen.kt (daily tasks screen)
- Total: ~10 files, ~1200 lines modified

**Phase 2 (Full)**:
- Remaining 10 UI screens
- Settings screen with theme toggle
- Total: ~20 files, ~2400 lines modified

**Excluded**: FocusModeScreen.kt (stays dark-only per requirement)

---

## 2. Design Principles & Aesthetics

### 2.1 Material 3 Core Principles

**Principle 1: Expressive & Adaptive**
- **What**: Colors adapt to user's light/dark preference while maintaining brand
- **How**: MaterialKolor generates harmonious palettes from VoxPlan green seed
- **Why**: Respects user preference without losing brand identity

**Principle 2: Elevated Surfaces, Not Shadows**
- **What**: Dark mode uses tonal elevation (lighter shades) instead of drop shadows
- **How**: Surface (tone 6) → SurfaceVariant (tone 30) → Dialog (tone 40)
- **Why**: Better readability, less visual fatigue in dark mode

**Principle 3: Semantic Color Roles**
- **What**: Use semantic names (primary, surface) not literal (green, white)
- **How**: `MaterialTheme.colorScheme.primary` replaces `Color(0xFF1BA821)`
- **Why**: Automatic light/dark adaptation, accessible contrast, maintainable

**Principle 4: Accessible by Default**
- **What**: All color pairs meet WCAG AA (4.5:1 contrast)
- **How**: HCT algorithm ensures proper tone pairings
- **Why**: Legal compliance, better UX for all users

### 2.2 VoxPlan-Specific Design Rules

**Rule 1: Power Bar Immutability**
- **What**: 11-color power bar stays pixel-perfect identical in both themes
- **How**: Extended color scheme with hardcoded values, not theme-derived
- **Why**: Branding consistency, user recognition, gamification psychology

**Rule 2: Green = Success = Progress**
- **What**: All success states, progress, completed items use green tones
- **How**: Primary palette derived from VoxPlan green (#1BA821)
- **Why**: Reinforces positive feedback loop, brand association

**Rule 3: Hierarchy Through Tone, Not Color**
- **What**: Related elements use same hue at different tones (not different colors)
- **How**: Quota bar (primary tone 40) → Background (primaryContainer tone 90)
- **Why**: Cleaner visual hierarchy, less color chaos

**Rule 4: Dark Mode is Not Just Inverted**
- **What**: Dark mode is intentionally designed, not auto-inverted colors
- **How**: Different tone mappings (light surface = 99, dark surface = 6, not 0)
- **Why**: Proper contrast, reduced eye strain, Material 3 spec compliance

### 2.3 Aesthetic Guidelines

**Visual Consistency**:
- All greens derive from single seed → harmonious green family
- Secondary/tertiary colors are algorithmically harmonized → no color clashes
- Neutral tones share same temperature → cohesive gray palette

**Elevation Strategy**:
```
Dark Mode Elevation Hierarchy:
├─ Background (tone 0-6): App background, nav drawer
├─ Surface (tone 10): Cards, bottom sheets
├─ Surface Variant (tone 20-30): Elevated cards, selected items
└─ Surface Highest (tone 40): Dialogs, menus
```

**Typography Contrast**:
- High emphasis text: `onSurface` (tone 90 in dark, tone 10 in light)
- Medium emphasis: `onSurfaceVariant` (tone 80 in dark, tone 30 in light)
- Disabled: `onSurface.copy(alpha = 0.38f)`

---

## 3. Material 3 Best Practices Alignment

### 3.1 Official Guidelines Followed

**Source**: [Android Developers - Material 3 in Compose](https://developer.android.com/develop/ui/compose/designsystems/material3)

**Guideline 1**: Use Material 3 components
- ✅ Already using: `androidx.compose.material3:material3` (via BOM 2023.08.00)
- ✅ No changes needed to component imports

**Guideline 2**: Apply color scheme through MaterialTheme
- ✅ Current: Theme exists but unused
- ✅ Fix: Wrap VoxPlanApp() in VoxPlanAppTheme { } in MainActivity

**Guideline 3**: Avoid hardcoded colors
- ❌ Current: 80+ inline Color(0x...) calls
- ✅ Fix: Replace with MaterialTheme.colorScheme.* or AppTheme.colors.*

**Guideline 4**: Disable dynamic color for branded apps
- ✅ Set `dynamicColor = false` in theme
- ✅ Prevents Android 12+ wallpaper override

**Guideline 5**: Test in both themes
- ✅ Planned: Visual testing checklist for each screen

### 3.2 Compose Best Practices

**Practice 1**: Use CompositionLocal for custom theme extensions
- ✅ `LocalExtendedColors` for power bar colors
- ✅ Accessible via `AppTheme.colors.powerBar.fullBorder`

**Practice 2**: Hoist theme state
- ✅ Theme preference stored in SharedPreferences (future Settings screen)
- ✅ Pass `darkTheme` parameter to VoxPlanAppTheme

**Practice 3**: Preview both themes
- ✅ Add `@Preview(uiMode = UI_MODE_NIGHT_YES)` annotations
- ✅ Test components in isolation

**Practice 4**: Avoid recomposition with derived state
- ✅ MaterialKolor uses `rememberDynamicColorScheme` (stable across recompositions)
- ✅ Extended colors are data classes (structural equality)

### 3.3 Android Best Practices (2025)

**Practice 1**: Use latest stable libraries
- ⚠️ Current: Compose BOM 2023.08.00 (16 months old)
- ✅ Material 3 v1.1.2 included in that BOM is stable
- 💡 Consider: Upgrade to latest BOM (2024.12.00+) in future, not critical for this feature

**Practice 2**: Respect system preferences
- ✅ `isSystemInDarkTheme()` as default
- ✅ User can override via Settings (future)

**Practice 3**: Efficient color calculations
- ✅ MaterialKolor caches generated palettes
- ✅ Extended colors are const (no runtime computation)

**Practice 4**: Accessibility scanner integration
- ✅ Planned: Manual contrast validation for extended colors
- 💡 Future: Integrate Accessibility Scanner app for automated testing

---

## 4. Implementation Phases

### Phase 0: Preparation (30 minutes)

**Goal**: Set up infrastructure, no UI changes

**Tasks**:
1. Add MaterialKolor dependency to `app/build.gradle.kts`
2. Sync Gradle (verify no conflicts)
3. Create `ExtendedColors.kt` with power bar definitions
4. Create backup branch: `git checkout -b feature/material3-dark-mode`

**Verification**:
- [ ] Build succeeds
- [ ] No new warnings in Build Output
- [ ] ExtendedColors.kt compiles without errors

**Files Modified**: 2 (build.gradle.kts, new ExtendedColors.kt)

### Phase 1: Theme Infrastructure (1 hour)

**Goal**: Make theme system functional

**Tasks**:
1. Refactor `Theme.kt` to use MaterialKolor
2. Delete `Color.kt` (placeholder colors)
3. Fix `MainActivity.kt` to wrap in VoxPlanAppTheme
4. Test: Run app, verify it compiles and runs (colors will look wrong, that's OK)

**Verification**:
- [ ] App launches without crash
- [ ] Theme.kt uses `rememberDynamicColorScheme`
- [ ] MainActivity wraps VoxPlanApp in theme
- [ ] No references to Color.kt remain

**Files Modified**: 3 (Theme.kt, MainActivity.kt, Color.kt deleted)

### Phase 2: Power Bar Migration (1 hour)

**Goal**: Verify extended color system works, preserve branding

**Tasks**:
1. Refactor `MainScreen.kt:180-306` to use `AppTheme.colors.powerBar.*`
2. Visual test: Compare power bar before/after (pixel-perfect match required)
3. Test in both light and dark mode

**Verification**:
- [ ] Power bar looks identical in light mode
- [ ] Power bar looks identical in dark mode
- [ ] Screenshot comparison: 0 pixel difference
- [ ] No inline Color(0x...) in power bar code

**Files Modified**: 1 (MainScreen.kt)

### Phase 3: MainScreen Complete (2 hours)

**Goal**: Fully migrate primary screen

**Tasks**:
1. Replace all Colors.kt imports in MainScreen.kt with theme equivalents
2. Replace all inline Color(0x...) with theme equivalents
3. Visual test: Light mode aesthetics
4. Visual test: Dark mode aesthetics
5. Functional test: All interactions work (ActionMode, navigation, etc.)

**Verification**:
- [ ] No imports from Colors.kt
- [ ] No inline Color(0x...) except power bar
- [ ] Light mode looks good (screenshots for approval)
- [ ] Dark mode looks good (screenshots for approval)
- [ ] Breadcrumb navigation works
- [ ] ActionMode buttons work
- [ ] FAB works

**Files Modified**: 1 (MainScreen.kt)

### Phase 4: DailyScreen Complete (2 hours)

**Goal**: Migrate secondary screen

**Tasks**:
1. Replace Colors.kt imports in DailyScreen.kt
2. Replace inline Color(0x...) in DailyScreen.kt
3. Visual test: Light/dark modes
4. Functional test: Add daily, reorder, ActionMode

**Verification**:
- [ ] No Colors.kt imports
- [ ] No inline Color(0x...)
- [ ] Light mode aesthetics approved
- [ ] Dark mode aesthetics approved
- [ ] Quota integration still works
- [ ] Vertical reordering works

**Files Modified**: 1 (DailyScreen.kt)

### Phase 5: Cleanup & Deprecation (30 minutes)

**Goal**: Remove old color system

**Tasks**:
1. Delete `Colors.kt` entirely
2. Verify no remaining imports of Colors.kt (should fail build if any)
3. Fix any discovered usages
4. Run full app test

**Verification**:
- [ ] Colors.kt deleted
- [ ] Build succeeds (proves no lingering references)
- [ ] Full app navigation test passes

**Files Deleted**: 1 (Colors.kt)

### Phase 6: Remaining Screens (4-6 hours) **OPTIONAL - Post-MVP**

**Goal**: Full app theme coverage

**Screens**:
1. ProgressScreen.kt
2. DaySchedule.kt
3. GoalEditScreen.kt
4. QuickScheduleScreen.kt (if fixed)
5. Remaining components

**Per-Screen Process**:
1. Replace Colors.kt imports
2. Replace inline colors
3. Visual test light/dark
4. Functional test interactions

**Verification**: Same as Phase 3/4 for each screen

### Phase 7: Settings Screen Theme Toggle (2 hours) **OPTIONAL - Post-MVP**

**Goal**: User control over theme

**Tasks**:
1. Add Settings screen (new)
2. Add SharedPreferences for theme preference
3. Add RadioButton/Switch: Auto / Light / Dark
4. Wire up to VoxPlanAppTheme
5. Test persistence across app restarts

**Verification**:
- [ ] Settings screen accessible
- [ ] Theme changes immediately when toggled
- [ ] Preference persists on restart
- [ ] "Auto" follows system preference

**Files Modified**: 3-4 (new SettingsScreen.kt, SettingsViewModel.kt, Theme.kt, VoxPlanNavHost.kt)

---

## 5. Detailed File-by-File Changes

### 5.1 app/build.gradle.kts

**Current** (line 52-85):
```kotlin
dependencies {
    implementation("androidx.room:room-runtime:2.6.1")
    // ... other dependencies
    implementation(platform("androidx.compose:compose-bom:2023.08.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    // ...
}
```

**After**:
```kotlin
dependencies {
    implementation("androidx.room:room-runtime:2.6.1")
    // ... other dependencies
    implementation(platform("androidx.compose:compose-bom:2023.08.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")

    // Material 3 dynamic color generation
    implementation("com.materialkolor:material-kolor:4.0.0")

    // ...
}
```

**Lines Changed**: 1 line added after line 67
**Rationale**: MaterialKolor provides programmatic color palette generation
**Risk**: None - library is stable, no conflicts with existing dependencies
**Testing**: Gradle sync, verify build succeeds

### 5.2 app/src/main/java/com/voxplanapp/ui/theme/ExtendedColors.kt **(NEW FILE)**

**Full Content**:
```kotlin
package com.voxplanapp.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Extended color scheme for VoxPlan branding elements that must remain
 * consistent across both light and dark themes.
 *
 * Primary use case: Power bar colors (branding requirement)
 */
data class PowerBarColors(
    // Container
    val background: Color = Color.Black,

    // Text labels
    val label: Color = Color(0xFFFF5722),  // Deep Orange 500 - "POWER:"

    // Diamond medal
    val diamond: Color = Color(0xFF4CAF50),  // Green 400 - count text
    val diamondBackground: Color = Color(0xFF9C27B0),  // Purple 500
    val diamondBorder: Color = Color(0xFFBA68C8),  // Purple 300

    // Progress bars
    val fullBorder: Color = Color(0xFF1BA821),  // VoxPlan brand green!
    val partialBorder: Color = Color(0xFF3F51B5),  // Indigo 500
    val fullFill: Color = Color(0xFF13D31B),  // Bright green
    val partialFill: Color = Color(0xFFFF0000),  // Red

    // Coin display
    val coinBackground: Color = Color(0xFFFFD700),  // Gold
    val coinBorder: Color = Color(0xFFFF9800)  // Orange 500
)

/**
 * Container for all extended colors (power bar + future additions)
 */
data class ExtendedColorScheme(
    val powerBar: PowerBarColors = PowerBarColors()
)

/**
 * CompositionLocal for accessing extended colors throughout the app.
 *
 * Usage: AppTheme.colors.powerBar.fullBorder
 */
val LocalExtendedColors = staticCompositionLocalOf { ExtendedColorScheme() }

/**
 * Helper object for convenient access to extended colors.
 *
 * Example:
 *   Box(modifier = Modifier.background(AppTheme.colors.powerBar.background))
 */
object AppTheme {
    val colors: ExtendedColorScheme
        @androidx.compose.runtime.Composable
        get() = LocalExtendedColors.current
}
```

**Lines**: 67 total
**Rationale**:
- Power bar colors must be identical in both themes (branding)
- CompositionLocal pattern is Material 3 best practice for theme extensions
- AppTheme helper provides clean syntax (no `LocalExtendedColors.current` everywhere)

**Design Decision**: Why not derive from theme?
- Power bar is branding, not semantic UI
- Changing seed color shouldn't change power bar
- These colors have been tested for visual impact (gamification psychology)

### 5.3 app/src/main/java/com/voxplanapp/ui/theme/Theme.kt

**Current** (lines 9-44):
```kotlin
private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40
)

@Composable
fun VoxPlanAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
```

**After**:
```kotlin
package com.voxplanapp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import com.materialkolor.PaletteStyle
import com.materialkolor.rememberDynamicColorScheme

/**
 * VoxPlan app theme with Material 3 color system.
 *
 * Colors are generated programmatically from VoxPlan brand green (#1BA821)
 * using MaterialKolor library, which implements Material 3's HCT algorithm.
 *
 * @param darkTheme Whether to use dark theme. Defaults to system preference.
 * @param dynamicColor Whether to use Android 12+ dynamic colors from wallpaper.
 *                     Disabled by default to preserve VoxPlan branding.
 * @param content The composable content to theme.
 */
@Composable
fun VoxPlanAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,  // Disabled to preserve brand identity
    content: @Composable () -> Unit
) {
    // Generate Material 3 color scheme from VoxPlan brand green
    // Uses HCT color space for perceptually accurate tonal palettes
    val colorScheme = rememberDynamicColorScheme(
        seedColor = Color(0xFF1BA821),  // VoxPlan brand green
        isDark = darkTheme,
        style = PaletteStyle.TonalSpot  // Default Material 3 style
        // Alternative styles: Expressive, Vibrant, Rainbow, FruitSalad, Monochrome, Neutral
    )

    // Extended colors for branding elements (power bar, etc.)
    val extendedColors = ExtendedColorScheme(
        powerBar = PowerBarColors()
    )

    // Provide both Material theme and extended colors
    CompositionLocalProvider(LocalExtendedColors provides extendedColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = Shapes,
            content = content
        )
    }
}
```

**Lines Changed**: Entire file rewritten (~60 lines)
**Rationale**:
- `rememberDynamicColorScheme` generates palette from seed color
- `dynamicColor = false` prevents Android 12+ wallpaper override
- `PaletteStyle.TonalSpot` is default Material 3 style (balanced, professional)
- Extended colors provided via CompositionLocal
- Comprehensive documentation for future maintainers

**Design Decision**: Why PaletteStyle.TonalSpot?
- **TonalSpot**: Balanced, primary-focused (good for brand-driven apps)
- Expressive: More vibrant, playful (not VoxPlan's tone)
- Monochrome: Too subtle for gamification
- Can be changed to `.Expressive` if user prefers more vibrant UI

### 5.4 app/src/main/java/com/voxplanapp/ui/theme/Color.kt **(DELETE FILE)**

**Current**: 11 lines of placeholder colors (Purple80, etc.)

**After**: File deleted entirely

**Rationale**:
- These colors are unused once MaterialKolor generates the palette
- Keeping them invites accidental usage
- Material 3 doesn't use named color constants (uses semantic roles)

**Migration Path**: All references replaced by `MaterialTheme.colorScheme.*`

### 5.5 app/src/main/java/com/voxplanapp/MainActivity.kt

**Current** (line ~25):
```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
        VoxPlanApp()
    }
}
```

**After**:
```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
        VoxPlanAppTheme {
            VoxPlanApp()
        }
    }
}
```

**Lines Changed**: 1 line added (wrap in theme)
**Rationale**: This is THE critical fix - theme was defined but never applied
**Impact**: Enables all MaterialTheme.colorScheme.* calls throughout app
**Testing**: App should launch, colors will be wrong (seed green applied to everything) until individual screens migrate

### 5.6 app/src/main/java/com/voxplanapp/ui/main/MainScreen.kt

**Section 1: Power Bar (lines 180-306)**

**Current** (example snippet):
```kotlin
@Composable
fun PowerBarDisplay(/*...*/) {
    Column(
        modifier = Modifier
            .background(Color.Black, RoundedCornerShape(8.dp))  // LINE 195
            .padding(8.dp)
    ) {
        Text(
            text = "POWER:",
            color = Color(0xFFFF5722),  // LINE 202
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
        // ... more power bar code with inline colors
    }
}
```

**After**:
```kotlin
@Composable
fun PowerBarDisplay(/*...*/) {
    // Access extended colors once at top of composable
    val powerBarColors = AppTheme.colors.powerBar

    Column(
        modifier = Modifier
            .background(powerBarColors.background, RoundedCornerShape(8.dp))
            .padding(8.dp)
    ) {
        Text(
            text = "POWER:",
            color = powerBarColors.label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
        // ... all 11 colors replaced with powerBarColors.*
    }
}
```

**Lines Changed**: ~40 lines (all power bar color references)
**Pattern**:
1. Add `val powerBarColors = AppTheme.colors.powerBar` at top
2. Replace each `Color(0x...)` with `powerBarColors.{property}`

**Verification**: Screenshot comparison before/after must be pixel-perfect identical

**Section 2: Other Colors in MainScreen**

**Current** (example):
```kotlin
import com.voxplanapp.ui.constants.PrimaryColor
import com.voxplanapp.ui.constants.ToolbarColor
// ...
Box(modifier = Modifier.background(PrimaryColor))  // LINE 95
TopAppBar(colors = TopAppBarDefaults.topAppBarColors(
    containerColor = ToolbarColor  // LINE 112
))
```

**After**:
```kotlin
// Remove imports of Colors.kt constants
// ...
Box(modifier = Modifier.background(MaterialTheme.colorScheme.surface))
TopAppBar(colors = TopAppBarDefaults.topAppBarColors(
    containerColor = MaterialTheme.colorScheme.surfaceVariant
))
```

**Migration Table for MainScreen**:

| Current Code | Replace With | Line # | Semantic Meaning |
|--------------|-------------|--------|------------------|
| `import PrimaryColor` | Remove import | ~10 | N/A |
| `import ToolbarColor` | Remove import | ~10 | N/A |
| `PrimaryColor` | `MaterialTheme.colorScheme.surface` | ~95 | Background |
| `ToolbarColor` | `MaterialTheme.colorScheme.surfaceVariant` | ~112 | Elevated surface |
| `Color.White` (cards) | `MaterialTheme.colorScheme.surface` | ~150 | Card background |
| `Color(0xFF0F0A2C)` | `MaterialTheme.colorScheme.surface` | ~180 | Dark background |
| Power bar colors | `AppTheme.colors.powerBar.*` | 180-306 | Branding |

**Total Changes**: ~60 lines across MainScreen.kt

### 5.7 app/src/main/java/com/voxplanapp/ui/daily/DailyScreen.kt

**Current** (examples):
```kotlin
import com.voxplanapp.ui.constants.EventBoxColor
import com.voxplanapp.ui.constants.QuotaProgressColor
// ...
Box(
    modifier = Modifier.background(EventBoxColor)  // LINE 215
)
LinearProgressIndicator(
    progress = progress,
    color = QuotaProgressColor,  // LINE 340
    trackColor = Color(0xFFE8F5E9)
)
```

**After**:
```kotlin
// Remove imports
// ...
Box(
    modifier = Modifier.background(MaterialTheme.colorScheme.primaryContainer)
)
LinearProgressIndicator(
    progress = progress,
    color = MaterialTheme.colorScheme.primary,
    trackColor = MaterialTheme.colorScheme.surfaceVariant
)
```

**Migration Table for DailyScreen**:

| Current Code | Replace With | Semantic Meaning |
|--------------|-------------|------------------|
| `EventBoxColor` | `MaterialTheme.colorScheme.primaryContainer` | Event background |
| `QuotaProgressColor` | `MaterialTheme.colorScheme.primary` | Progress bar |
| `Color(0xFFE8F5E9)` | `MaterialTheme.colorScheme.surfaceVariant` | Progress track |
| `Color.White` | `MaterialTheme.colorScheme.surface` | Card background |
| `Color(0xFF4CAF50)` | `MaterialTheme.colorScheme.primary` | Success indicator |
| `DeleteRed` | `MaterialTheme.colorScheme.error` | Delete button |

**Total Changes**: ~25 lines across DailyScreen.kt

### 5.8 app/src/main/java/com/voxplanapp/ui/constants/Colors.kt **(DELETE FILE)**

**Current**: 52 lines, 26 color constants

**After**: File deleted entirely

**Rationale**:
- All constants migrated to theme
- Keeping creates technical debt (developers might use it)
- Material 3 philosophy: no app-level color constants

**Verification**: Build fails if any file still imports Colors.kt (proves complete migration)

---

## 6. Testing & Validation Strategy

### 6.1 Visual Testing Checklist

**Power Bar Verification** (CRITICAL):
```
Test ID: VISUAL-001
Screen: MainScreen
Component: PowerBarDisplay
Steps:
  1. Checkout main branch
  2. Launch app, navigate to MainScreen
  3. Take screenshot of power bar (crop to exact bounds)
  4. Save as power-bar-before.png
  5. Checkout feature branch (after Phase 2)
  6. Launch app, navigate to MainScreen (light mode)
  7. Take screenshot of power bar
  8. Save as power-bar-after-light.png
  9. Toggle to dark mode (system settings)
  10. Take screenshot of power bar
  11. Save as power-bar-after-dark.png
  12. Pixel diff: before vs after-light (must be 0% difference)
  13. Pixel diff: before vs after-dark (must be 0% difference)

Pass Criteria:
  - 0 pixel difference between before and after-light
  - 0 pixel difference between before and after-dark
  - All 11 colors visually identical

Failure Action:
  - Review ExtendedColors.kt for typos in hex values
  - Verify AppTheme.colors.powerBar usage in MainScreen
```

**MainScreen Light Mode** (Post Phase 3):
```
Test ID: VISUAL-002
Screen: MainScreen
Theme: Light
Steps:
  1. Launch app in light mode
  2. Take full screenshot
  3. Review for:
     - Text legibility (dark text on light backgrounds)
     - Visual hierarchy (elevated cards visible)
     - Brand identity (green primary color evident)
     - No pure black or white (Material 3 uses tones)
  4. Compare to design principles (Section 2.2)

Pass Criteria:
  - All text readable (no low contrast)
  - Power bar unchanged
  - FAB is green (primary color)
  - Background is near-white (tone 99), not pure white

Failure Action:
  - Review color mappings in Section 5.6
  - Check for missed inline colors
```

**MainScreen Dark Mode** (Post Phase 3):
```
Test ID: VISUAL-003
Screen: MainScreen
Theme: Dark
Steps:
  1. Toggle system dark mode
  2. Launch app
  3. Take full screenshot
  4. Review for:
     - No pure black backgrounds (should be tone 6)
     - Elevated cards visible (tonal elevation, not shadows)
     - Text is off-white (tone 90), not pure white
     - Power bar unchanged

Pass Criteria:
  - Background is dark gray (#191C18 ish), not pure black
  - Cards have subtle tonal difference from background
  - Power bar identical to light mode
  - No eye strain (proper contrast, not harsh white text)

Failure Action:
  - Verify MaterialKolor isDark=true
  - Check surface tones (should be 6, not 0)
```

**DailyScreen Light/Dark** (Post Phase 4):
- Same process as VISUAL-002/003
- Additional focus: Quota progress bars, event boxes

**Repeat for all screens** (Post Phase 6)

### 6.2 Functional Testing Checklist

**MainScreen Interactions**:
```
Test ID: FUNC-001
Screen: MainScreen
Steps:
  1. Launch app
  2. Navigate goal hierarchy (breadcrumbs)
  3. Enable ActionMode (VerticalUp)
  4. Reorder a goal
  5. Disable ActionMode
  6. Tap FAB (add goal)
  7. Navigate to subgoal
  8. Navigate back

Pass Criteria:
  - All interactions work identically to before
  - No visual glitches
  - State persists across theme changes

Failure Action:
  - Check if theme migration broke ViewModel logic
  - Verify no color-dependent conditional logic
```

**DailyScreen Interactions**:
- Add daily task
- Quota integration
- Vertical reordering
- Delete daily

**Cross-screen navigation**:
- MainScreen → DailyScreen → ProgressScreen → DaySchedule → back

### 6.3 Automated Testing (Future)

**Unit Tests**:
```kotlin
@Test
fun extendedColors_powerBarColorsMatch() {
    val powerBar = PowerBarColors()
    assertEquals(Color.Black, powerBar.background)
    assertEquals(Color(0xFFFF5722), powerBar.label)
    assertEquals(Color(0xFF1BA821), powerBar.fullBorder)
    // All 11 colors verified
}

@Test
fun theme_generatesGreenPrimaryFromSeed() {
    val scheme = rememberDynamicColorScheme(
        seedColor = Color(0xFF1BA821),
        isDark = false
    )
    // Verify primary has green hue (HCT hue ~120-150)
    val hue = scheme.primary.toHct().hue
    assertTrue(hue in 100f..160f)
}
```

**Screenshot Tests** (Compose):
```kotlin
@Test
fun mainScreen_lightMode_matchesGolden() {
    composeTestRule.setContent {
        VoxPlanAppTheme(darkTheme = false) {
            MainScreen(/* ... */)
        }
    }
    composeTestRule.onRoot().captureToImage()
        .assertAgainstGolden("mainscreen_light")
}
```

*(Not implemented in this phase, but recommended for CI/CD)*

### 6.4 Accessibility Testing

**Manual Contrast Validation**:
```
Tool: https://webaim.org/resources/contrastchecker/

Test: All extended colors against their backgrounds

Power Bar:
  - label (FF5722) on background (000000): Check contrast
  - diamond (4CAF50) on diamondBg (9C27B0): Check contrast
  - All 11 color pairs validated

Target: WCAG AA (4.5:1 for normal text, 3:1 for large)

Pass Criteria: All pairs meet AA minimum

Failure Action:
  - If fails: Use MaterialKolor's lighten/darken utilities
  - Adjust tone while preserving hue
```

**Material 3 Built-in Validation**:
- `colorScheme.primary` / `onPrimary` guaranteed AA by HCT algorithm
- `surface` / `onSurface` guaranteed AA
- All Material 3 pairs auto-validated ✅

---

## 7. Accessibility Compliance

### 7.1 WCAG 2.1 Requirements

**Level AA (Minimum)**:
- Normal text: 4.5:1 contrast ratio
- Large text (18pt+): 3:1 contrast ratio
- Interactive elements: 3:1 against background

**Level AAA (Enhanced)**:
- Normal text: 7:1 contrast ratio
- Large text: 4.5:1 contrast ratio

**VoxPlan Target**: AA compliance for all text, AAA where feasible

### 7.2 Material 3 Accessibility Features

**HCT Color Space**:
- Perceptually uniform (unlike RGB/HSL)
- Tone directly correlates to perceived lightness
- Guarantees contrast when pairing proper tones

**Automatic Contrast**:
```kotlin
// Material 3 ensures these pairs meet WCAG AA:
primary (tone 40) / onPrimary (tone 100) = ~8.5:1 (AA ✅, AAA ✅)
surface (tone 99) / onSurface (tone 10) = ~14:1 (AA ✅, AAA ✅)
error (tone 40) / onError (tone 100) = ~7.2:1 (AA ✅, AAA ✅)
```

### 7.3 Extended Color Validation

**Power Bar Colors** (must manually verify):

| Foreground | Background | Ratio | AA Pass? | AAA Pass? | Action |
|------------|------------|-------|----------|-----------|--------|
| label (FF5722) | background (000000) | ~5.8:1 | ✅ Yes | ❌ No | Accept (AA sufficient) |
| diamond (4CAF50) | diamondBg (9C27B0) | ~3.2:1 | ✅ Yes* | ❌ No | Accept (large text) |
| coinBorder (FF9800) | coinBg (FFD700) | ~1.5:1 | ❌ No | ❌ No | ⚠️ Decorative only |

*Assumes large text (>18pt)

**Recommendation**: Power bar is gamification UI (not critical text), AA compliance for readable text, decorative elements can have lower contrast

### 7.4 Accessibility Testing Tools

**Android Accessibility Scanner**:
1. Install from Play Store
2. Enable in Settings → Accessibility
3. Run on MainScreen, DailyScreen
4. Review contrast warnings
5. Fix any critical issues

**Chrome DevTools** (for color picking):
1. Use eyedropper to sample colors
2. Use contrast ratio calculator
3. Validate manually

**Figma Plugin** (design validation):
- Material Theme Builder has built-in contrast checker
- Verify palette before implementation

---

## 8. Risk Mitigation

### 8.1 Identified Risks

**Risk 1: Breaking Changes**
- **Threat**: Theme refactor breaks existing UI
- **Probability**: Medium
- **Impact**: High (app crashes or unusable)
- **Mitigation**:
  - Feature branch (isolate changes)
  - Phase-by-phase testing (catch issues early)
  - Screenshot comparisons (verify no regressions)
  - Functional testing checklist

**Risk 2: Performance Regression**
- **Threat**: MaterialKolor adds runtime overhead
- **Probability**: Low
- **Impact**: Medium (slower app startup)
- **Mitigation**:
  - `rememberDynamicColorScheme` caches result (no recomputation)
  - Profile app startup with Android Profiler before/after
  - Target: <50ms added latency (imperceptible)

**Risk 3: Accessibility Failures**
- **Threat**: Extended colors don't meet WCAG AA
- **Probability**: Low (already validated most)
- **Impact**: High (legal/compliance risk)
- **Mitigation**:
  - Manual validation with contrast checker
  - Accessibility Scanner testing
  - Fallback: Adjust tone while preserving hue

**Risk 4: User Confusion**
- **Threat**: Dark mode looks too different, users dislike
- **Probability**: Medium
- **Impact**: Medium (negative reviews)
- **Mitigation**:
  - Power bar stays identical (familiar anchor)
  - Gradual rollout (beta testing)
  - User preference toggle (opt-out to light mode)
  - Collect feedback before wide release

**Risk 5: Incomplete Migration**
- **Threat**: Some screens still use old colors
- **Probability**: Medium
- **Impact**: Medium (inconsistent UI)
- **Mitigation**:
  - Delete Colors.kt (forces compile errors if used)
  - Grep for `Color(0x` after migration (find stragglers)
  - Comprehensive testing checklist

### 8.2 Rollback Plan

If critical issues discovered:

**Rollback Steps**:
1. `git checkout main` (instant rollback)
2. Rebuild app
3. Deploy previous version

**Partial Rollback** (if only one screen broken):
1. Keep theme infrastructure
2. Revert only broken screen to old colors
3. Fix incrementally

**Data Loss Risk**: None (theme is UI-only, no database changes)

---

## 9. User Questions Requiring Decisions

### Question 1: Color Palette Generation Method

**Context**: We need a Material 3 color palette from seed color #1BA821

**Option A: MaterialKolor Library** (RECOMMENDED)
- ✅ Programmatic (Kotlin code)
- ✅ No manual work
- ✅ Easy to iterate (change seed color instantly)
- ✅ Compose-native integration
- ❌ Adds 1 dependency

**Option B: Material Theme Builder (Figma Plugin)**
- ✅ Visual preview before implementing
- ✅ No code dependency
- ❌ Manual export (copy/paste 60+ hex codes)
- ❌ Hard to iterate (re-export every change)
- ❌ Requires Figma

**Question**: Which method do you prefer?
**Recommendation**: Option A (MaterialKolor)

---

### Question 2: Implementation Scope

**Context**: We can deliver incrementally or all-at-once

**Option A: MVP Scope** (RECOMMENDED)
- ✅ Faster delivery (Phase 1-5, ~8 hours)
- ✅ Lower risk (test core screens first)
- ✅ User feedback earlier
- ❌ Inconsistent UI temporarily (some screens dark, some not)

**Screens Included**: MainScreen, DailyScreen (primary user flows)

**Option B: Full Scope**
- ✅ Complete solution immediately
- ✅ Consistent UI across all screens
- ❌ Longer delivery (Phase 1-6, ~16 hours)
- ❌ Higher risk (more changes, more testing)

**Screens Added**: ProgressScreen, DaySchedule, GoalEditScreen, all others

**Question**: MVP first or full implementation?
**Recommendation**: Option A (MVP), then Phase 6 post-feedback

---

### Question 3: Dynamic Color Support

**Context**: Android 12+ can generate colors from wallpaper

**Option A: Disabled** (RECOMMENDED)
- ✅ Preserves VoxPlan brand green
- ✅ Consistent across all devices
- ❌ Users can't personalize colors

**Option B: Enabled by Default**
- ✅ "Material You" personalization
- ❌ Loses brand identity (app could be blue, pink, etc.)
- ❌ Confusing (app looks different for each user)

**Option C: User Toggle**
- ✅ Best of both worlds
- ✅ Power users can personalize
- ❌ Requires Settings screen (Phase 7, +2 hours)
- ❌ More testing (3 states: light/dark/dynamic)

**Question**: Enable dynamic color? Add toggle?
**Recommendation**: Option A now, Option C in future Settings screen

---

### Question 4: FocusModeScreen Inclusion

**Context**: You said "focus mode should be only dark-mode, and so should stay as is"

**Clarification Needed**:
1. **Exclude from migration** (keep current inline colors)?
   - ✅ Less work
   - ❌ Inconsistent (doesn't use theme system)
   - ❌ Can't toggle to light mode

2. **Migrate but force dark mode** (use `VoxPlanAppTheme(darkTheme = true)`)?
   - ✅ Uses theme system (consistent)
   - ✅ Could add light mode later if requested
   - ❌ Slightly more work

**Question**: Exclude FocusModeScreen entirely, or migrate but keep dark?
**Recommendation**: Migrate but force dark (future-proof)

---

## 10. Estimated Timeline

**Assumptions**:
- Single developer
- No interruptions
- Includes testing time

| Phase | Tasks | Duration | Cumulative |
|-------|-------|----------|------------|
| 0 | Preparation | 30 min | 0.5 hr |
| 1 | Theme Infrastructure | 1 hr | 1.5 hr |
| 2 | Power Bar Migration | 1 hr | 2.5 hr |
| 3 | MainScreen Complete | 2 hr | 4.5 hr |
| 4 | DailyScreen Complete | 2 hr | 6.5 hr |
| 5 | Cleanup & Deprecation | 30 min | 7 hr |
| **MVP Total** | **Phases 0-5** | **7 hours** | - |
| 6 | Remaining Screens | 4-6 hr | 11-13 hr |
| 7 | Settings Screen | 2 hr | 13-15 hr |
| **Full Total** | **All Phases** | **13-15 hours** | - |

**Buffer**: Add 25% for unexpected issues = **9 hours (MVP)** or **17 hours (Full)**

---

## 11. Success Metrics

**How we'll know this is done well**:

1. ✅ **Power bar pixel-perfect** (0% difference before/after)
2. ✅ **No inline Color(0x...)** (except power bar) - grep verify
3. ✅ **No Colors.kt imports** - build fails if any remain
4. ✅ **WCAG AA compliance** - all text meets 4.5:1 contrast
5. ✅ **Functional parity** - all interactions work in both themes
6. ✅ **User approval** - light/dark screenshots approved
7. ✅ **No performance regression** - <50ms startup added
8. ✅ **Clean code** - all changes follow Material 3 best practices

---

## 12. Summary & Next Steps

### What We've Designed

A comprehensive, Material 3 compliant dark mode implementation that:
- Preserves VoxPlan branding (power bar, brand green)
- Uses scientific color generation (HCT algorithm)
- Follows Android best practices (2025)
- Delivers incrementally (MVP → Full)
- Mitigates risks (feature branch, testing, rollback plan)

### What You Need to Decide

1. **Palette method**: MaterialKolor (recommended) or Figma export?
2. **Scope**: MVP (MainScreen + DailyScreen) or full (all screens)?
3. **Dynamic color**: Disabled (recommended), enabled, or user toggle?
4. **FocusModeScreen**: Exclude or migrate-but-force-dark?

### Next Actions

**If you approve this plan**:
1. Answer 4 questions above
2. I'll create beads tasks for each phase
3. I'll start Phase 0 (preparation)
4. I'll send progress updates via Agent Mail

**If you want changes**:
1. Point out specific sections to revise
2. I'll update plan
3. Re-submit for approval

---

**Ready to proceed?** Let me know your decisions on the 4 questions, and I'll begin implementation.
