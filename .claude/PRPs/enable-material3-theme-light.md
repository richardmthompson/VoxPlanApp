# PRP: Enable Material 3 Theme Infrastructure (Light Mode)

## Task Overview

Enable VoxPlanApp's existing Material 3 theme infrastructure by replacing default Android Studio colors with VoxPlan's actual color palette. This creates the foundation for the Progress screen migration and future theming work.

**Scope**: Light mode only (dark mode is future enhancement)
**Goal**: Make `MaterialTheme.colorScheme.*` functional and accessible app-wide
**Non-Goals**: Migrating any screens to use the theme (separate task), dark mode support, dynamic colors

---

## Context

### Problem Statement

VoxPlanApp has Material 3 theme infrastructure (`Theme.kt`, `Color.kt`) that is **completely unused**:

```kotlin
// ui/theme/Color.kt - DEFAULT ANDROID STUDIO COLORS (UNUSED)
val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

// ui/theme/Theme.kt - DEFINES BUT NEVER USED
private val LightColorScheme = lightColorScheme(
    primary = Purple40,      // ❌ Wrong colors
    secondary = PurpleGrey40, // ❌ Not VoxPlan palette
    tertiary = Pink40        // ❌ Never referenced in UI
)
```

Meanwhile, **all screens use hardcoded colors**:

```kotlin
// ui/constants/Colors.kt - ACTUAL VOXPLAN COLORS (26 CONSTANTS)
val PrimaryColor = Color(0xFF0F0A2C)          // Dark blue-purple
val PrimaryLightColor = Color(0xFF82BD85)     // Light green
val SecondaryColor = Color(0xFFC7E2C9)        // Pale green
val TertiaryColor = Color(0xFF82babd)         // Teal
// + 22 more colors

// Screens reference these directly:
// MainScreen.kt: 10 hardcoded colors
// FocusModeScreen.kt: 15 hardcoded colors
// ProgressScreen.kt: 8 hardcoded colors
// Total: 54 Color(0xFF...) instances across codebase
```

**The Gap**: Theme infrastructure exists but doesn't contain VoxPlan colors, so it's unused.

---

### Architecture Context

**Current Theme Structure**:
```
app/src/main/java/com/voxplanapp/
├── ui/
│   ├── theme/
│   │   ├── Color.kt          # Purple/pink colors (unused)
│   │   └── Theme.kt          # Material 3 setup (unused)
│   └── constants/
│       └── Colors.kt         # VoxPlan colors (26 constants, ACTUALLY USED)
```

**Material 3 Theme Components**:
1. **ColorScheme** (35 semantic roles):
   - Primary colors: `primary`, `onPrimary`, `primaryContainer`, `onPrimaryContainer`, `inversePrimary`
   - Secondary colors: `secondary`, `onSecondary`, `secondaryContainer`, `onSecondaryContainer`
   - Tertiary colors: `tertiary`, `onTertiary`, `tertiaryContainer`, `onTertiaryContainer`
   - Error colors: `error`, `onError`, `errorContainer`, `onErrorContainer`
   - Surface/Background: `surface`, `surfaceContainer*` (5 variants), `background`, etc.
   - Utility: `outline`, `outlineVariant`, `scrim`

2. **Extended Colors** (custom VoxPlan UI):
   - Medal colors (bronze, silver, gold, diamond)
   - Power bar colors (fill, background)
   - Goal category colors
   - Event/schedule colors

**Key Architecture Decision**: Use CompositionLocal pattern for extended colors (doesn't fit Material 3 semantic roles).

---

### Data Model

**VoxPlan Color Categories** (from color inventory research):

| Category | Colors | Usage | Map To |
|----------|--------|-------|--------|
| **Text Colors** | PrimaryColor (0xFF0F0A2C) | Goal names, headings | `onSurface` |
| **Background Colors** | TodoItemBackgroundColor (0xFF82BD85), SubGoalItemBackgroundColor (0xFFC7E2C9) | Card backgrounds | `surfaceContainer`, `surfaceContainerHigh` |
| **Border Colors** | TopLevelGoalBorderColor (0xFF009688), SubGoalItemBorderColor (0xFFB2DFDB) | Goal borders | `outline` |
| **Brand Colors** | PrimaryDarkColor (0xFF009688), TertiaryColor (0xFF82babd) | CTAs, accents | `primary`, `tertiary` |
| **Toolbar Colors** | ToolbarColor (0xFFfff2df), TitlebarColor (0xFFffddb0) | App bar backgrounds | `surfaceContainer` |
| **Special UI** | QuotaCompleteBackgroundColor, FocusColorWork, EventBoxColor | Quota, focus, events | **Extended colors** |

**Medal Colors** (Focus Mode - NOT in constants, hardcoded):
- Bronze: `Color(0xFFCD7F32)` → `ExtendedColors.medalBronze`
- Silver: `Color(0xFFC0C0C0)` → `ExtendedColors.medalSilver`
- Gold: `Color(0xFFFFD700)` → `ExtendedColors.medalGold`
- Diamond: `Color(0xFFB9F2FF)` → `ExtendedColors.medalDiamond`

**Power Bar Colors** (Focus Mode - hardcoded):
- Fill: `Color(0xFF4CAF50)` → `ExtendedColors.powerBarFill`
- Background: `Color(0xFFE0E0E0)` → `ExtendedColors.powerBarBackground`

---

### Existing Patterns

**Theme Composable Pattern** (from Google Reply sample):
```kotlin
@Composable
fun VoxPlanTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // VoxPlan needs brand consistency
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context)
            else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme // Future
        else -> LightColorScheme      // THIS TASK
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
```

**Extended Colors Pattern** (from research - herrbert74.github.io):
```kotlin
// 1. Define data class
@Immutable
data class ExtendedColors(
    val medalBronze: Color = Color.Unspecified,
    // ...
)

// 2. Create CompositionLocal
val LocalExtendedColors = staticCompositionLocalOf { ExtendedColors() }

// 3. Provide in theme
@Composable
fun VoxPlanTheme(content: @Composable () -> Unit) {
    CompositionLocalProvider(
        LocalExtendedColors provides lightExtendedColors
    ) {
        MaterialTheme(/* ... */) { content() }
    }
}

// 4. Access via extension property
val MaterialTheme.extendedColors: ExtendedColors
    @Composable
    @ReadOnlyComposable
    get() = LocalExtendedColors.current
```

---

### Gotchas

#### Issue 1: Material Theme Builder Limitations

**Problem**: Material Theme Builder generates colors from a single primary color using tonal palettes (luminance-based). VoxPlan has 26 specific colors that may not match generated palette.

**Solution**: Use Material Theme Builder as starting point, then manually adjust color values to match VoxPlan's exact hex codes. Priority: `primary`, `onSurface`, `surfaceContainer` (most used).

**Tool**: [Material Theme Builder](https://material-foundation.github.io/material-theme-builder/)

#### Issue 2: Extended Colors Require Manual Contrast Testing

**Problem**: Material 3's semantic color pairings (`primary`/`onPrimary`) are guaranteed WCAG compliant, but extended colors are NOT. Must test every extended color on every background it appears on.

**Solution**:
1. Use [WebAIM Contrast Checker](https://webaim.org/resources/contrastchecker/)
2. Test each extended color against `surface`, `surfaceContainer`, etc.
3. Minimum ratios: 4.5:1 for text, 3:1 for UI components
4. Document results in `ExtendedColors.kt` comments

**Example**:
```kotlin
// ui/theme/ExtendedColors.kt
val lightExtendedColors = ExtendedColors(
    // Tested on surface (0xFFFFFBFE): Ratio 7.2:1 ✅ AA + AAA
    medalBronze = Color(0xFFCD7F32),

    // Tested on surface: Ratio 3.8:1 ⚠️ AA only (not AAA)
    medalSilver = Color(0xFFC0C0C0),
)
```

#### Issue 3: Dynamic Colors Conflict with VoxPlan Branding

**Problem**: Android 12+ dynamic colors extract palette from wallpaper, overriding app branding.

**Solution**: Set `dynamicColor = false` in `VoxPlanTheme` composable (at least for MVP). Future enhancement: Support dynamic colors but preserve extended colors for medals/power bars.

#### Issue 4: Purple Colors Still Exported

**Problem**: Material Theme Builder exports some purple accent colors even when using non-purple primary.

**Solution**: Verify all generated color values. Replace any remaining purples with VoxPlan teal/green palette. Check `tertiary*` colors especially.

#### Issue 5: 16 Unused Color Constants

**Problem**: Current `Colors.kt` has 26 constants but only 10 are actually used (from color inventory).

**Solution**: **DO NOT DELETE** unused constants yet. Wait until screens are migrated to confirm they're truly unused. Document in `Colors.kt` which are deprecated vs active.

---

## Task Breakdown

### PHASE 1: Generate Material 3 Theme

**ACTION** Generate color scheme using Material Theme Builder
**OPERATION**: Create VoxPlan color palette from brand colors

**STEPS**:
1. Open [Material Theme Builder](https://material-foundation.github.io/material-theme-builder/)
2. Input VoxPlan primary brand color: `#0F0A2C` (PrimaryColor from Colors.kt)
3. Observe generated palette:
   - Primary: Should match or complement `#0F0A2C`
   - Secondary: Adjust to match `#82BD85` (PrimaryLightColor - light green)
   - Tertiary: Adjust to match `#82babd` (TertiaryColor - teal)
4. Export → Select "Android Compose (Jetpack)"
5. Download `Theme.kt` and `Color.kt` files

**VALIDATE**:
```bash
# Verify files downloaded
ls ~/Downloads/Theme.kt ~/Downloads/Color.kt
```
**IF_FAIL**: Use manual export button, check browser downloads folder
**EXPECTED**: Two files in downloads directory

---

### PHASE 2: Replace Theme Files

**ACTION** Replace `ui/theme/Theme.kt` and `ui/theme/Color.kt`
**OPERATION**: Backup old files, install generated files

**CHANGE**:
```bash
# Backup existing files
mv app/src/main/java/com/voxplanapp/ui/theme/Theme.kt \
   app/src/main/java/com/voxplanapp/ui/theme/Theme.kt.bak

mv app/src/main/java/com/voxplanapp/ui/theme/Color.kt \
   app/src/main/java/com/voxplanapp/ui/theme/Color.kt.bak

# Copy generated files
cp ~/Downloads/Theme.kt app/src/main/java/com/voxplanapp/ui/theme/
cp ~/Downloads/Color.kt app/src/main/java/com/voxplanapp/ui/theme/
```

**POST-PROCESSING** (critical):
1. Open `Theme.kt`
2. Find `@Composable fun YourAppTheme(...)` function name
3. Rename to `@Composable fun VoxPlanTheme(...)`
4. Verify package: `package com.voxplanapp.ui.theme`
5. Set `dynamicColor` parameter default to `false`:
   ```kotlin
   fun VoxPlanTheme(
       darkTheme: Boolean = isSystemInDarkTheme(),
       dynamicColor: Boolean = false, // Changed from true
       content: @Composable () -> Unit
   ) { /* ... */ }
   ```

**VALIDATE**:
```bash
./gradlew assembleDebug
```
**IF_FAIL**:
- Check imports (Material 3, not Material 2)
- Verify `lightColorScheme()` and `darkColorScheme()` functions exist
- Check for any `@Preview` annotations that might reference old colors

**EXPECTED**: Compilation succeeds (warnings OK, no errors)

---

### PHASE 3: Manual Color Adjustments

**ACTION** Fine-tune generated colors to match VoxPlan exact values
**OPERATION**: Edit `Color.kt` to use VoxPlan's actual hex codes

**FILE**: `app/src/main/java/com/voxplanapp/ui/theme/Color.kt`

**CHANGES** (priority colors to match VoxPlan branding):

```kotlin
// FROM (Material Theme Builder generated):
val md_theme_light_primary = Color(0xFF123456)           // Some generated color
val md_theme_light_onSurface = Color(0xFF1A1C1E)         // Generic dark gray
val md_theme_light_surfaceContainer = Color(0xFFECEFF1)  // Generic light gray

// TO (VoxPlan actual colors from Colors.kt):
val md_theme_light_primary = Color(0xFF0F0A2C)           // PrimaryColor
val md_theme_light_onSurface = Color(0xFF0F0A2C)         // PrimaryColor (text)
val md_theme_light_surfaceContainer = Color(0xFFC7E2C9)  // SecondaryColor (pale green)
```

**Priority Adjustments**:
1. `md_theme_light_primary` → `0xFF0F0A2C` (VoxPlan dark blue)
2. `md_theme_light_primaryContainer` → `0xFF82BD85` (VoxPlan light green)
3. `md_theme_light_secondary` → `0xFF009688` (VoxPlan teal)
4. `md_theme_light_tertiary` → `0xFF82babd` (VoxPlan accent teal)
5. `md_theme_light_onSurface` → `0xFF0F0A2C` (text color)
6. `md_theme_light_surfaceContainer` → `0xFFC7E2C9` (card background)
7. `md_theme_light_outline` → `0xFFB2DFDB` (borders)

**Reference**: See `ui/constants/Colors.kt` for VoxPlan's color constants and hex values.

**VALIDATE**:
```bash
./gradlew assembleDebug
```
**IF_FAIL**: Check Color syntax (all 0xFFXXXXXX, six hex digits)
**EXPECTED**: Compilation succeeds

---

### PHASE 4: Create Extended Colors

**ACTION** Create `ExtendedColors.kt` for VoxPlan-specific UI
**OPERATION**: Define new file with medal, power bar, and goal colors

**FILE**: `app/src/main/java/com/voxplanapp/ui/theme/ExtendedColors.kt` (NEW FILE)

**CONTENT**:
```kotlin
package com.voxplanapp.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.MaterialTheme

/**
 * Extended colors for VoxPlanApp UI elements that don't fit Material 3 semantic roles.
 *
 * Contrast testing required: All extended colors must be tested on every background
 * they appear on using WebAIM Contrast Checker (https://webaim.org/resources/contrastchecker/).
 *
 * Minimum ratios: 4.5:1 for text (AA), 3:1 for UI components/large text.
 */
@Immutable
data class ExtendedColors(
    // Focus Mode: Medal colors (30, 60, 90, 120+ minute achievements)
    val medalBronze: Color = Color.Unspecified,
    val medalSilver: Color = Color.Unspecified,
    val medalGold: Color = Color.Unspecified,
    val medalDiamond: Color = Color.Unspecified,

    // Focus Mode: Power bar (timer progress indicator)
    val powerBarFill: Color = Color.Unspecified,
    val powerBarBackground: Color = Color.Unspecified,

    // Goals: Category colors (future use - currently not implemented)
    val goalCategory1: Color = Color.Unspecified,
    val goalCategory2: Color = Color.Unspecified,
    val goalCategory3: Color = Color.Unspecified,

    // Schedule: Event type colors
    val eventScheduled: Color = Color.Unspecified,
    val dailyTaskHighlight: Color = Color.Unspecified,

    // Quota: Progress indication
    val quotaComplete: Color = Color.Unspecified,
    val quotaProgress: Color = Color.Unspecified,
)

// Light mode palette (from color inventory + manual hardcoded colors analysis)
val lightExtendedColors = ExtendedColors(
    // Medal colors (from FocusModeScreen.kt analysis)
    medalBronze = Color(0xFFCD7F32),    // Bronze medal (30 min)
    medalSilver = Color(0xFFC0C0C0),    // Silver medal (60 min)
    medalGold = Color(0xFFFFD700),      // Gold medal (90 min)
    medalDiamond = Color(0xFFB9F2FF),   // Diamond medal (120+ min)

    // Power bar colors (from MainScreen.kt and FocusModeScreen.kt)
    powerBarFill = Color(0xFF4CAF50),     // Green fill
    powerBarBackground = Color(0xFFE0E0E0), // Light gray background

    // Goal colors (placeholder - not currently used in codebase)
    goalCategory1 = Color(0xFF82BD85),  // Light green (PrimaryLightColor)
    goalCategory2 = Color(0xFFC7E2C9),  // Pale green (SecondaryColor)
    goalCategory3 = Color(0xFF82babd),  // Teal (TertiaryColor)

    // Event colors (from EventRepository and DaySchedule.kt)
    eventScheduled = Color(0xFF41c300),      // EventBoxColor from Colors.kt
    dailyTaskHighlight = Color(0xFFFFA500),  // Orange (from hardcoded analysis)

    // Quota colors (from Colors.kt)
    quotaComplete = Color(0xFFFFC107),    // QuotaCompleteBackgroundColor
    quotaProgress = Color(0xFFFF5722),    // QuotaProgressFillColor (orange)
)

// Dark mode palette (future enhancement - not implemented in this task)
val darkExtendedColors = ExtendedColors(
    // TODO: Implement dark mode extended colors
)

// CompositionLocal for accessing extended colors
val LocalExtendedColors = staticCompositionLocalOf { ExtendedColors() }

// Extension property for MaterialTheme to access extended colors
val MaterialTheme.extendedColors: ExtendedColors
    @androidx.compose.runtime.Composable
    @ReadOnlyComposable
    get() = LocalExtendedColors.current
```

**VALIDATE**:
```bash
./gradlew assembleDebug
```
**IF_FAIL**:
- Check import statements (Compose runtime, Material 3)
- Verify `@Immutable` annotation available
- Check all Color values are valid hex (0xFFXXXXXX)

**EXPECTED**: Compilation succeeds

---

### PHASE 5: Integrate Extended Colors into Theme

**ACTION** Update `Theme.kt` to provide extended colors via CompositionLocal
**OPERATION**: Wire extended colors into VoxPlanTheme composable

**FILE**: `app/src/main/java/com/voxplanapp/ui/theme/Theme.kt`

**CHANGE**:
```kotlin
// ADD THIS IMPORT (at top of file):
import androidx.compose.runtime.CompositionLocalProvider

// MODIFY VoxPlanTheme function:
@Composable
fun VoxPlanTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
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

    // NEW: Select extended colors based on theme
    val extendedColors = if (darkTheme) {
        darkExtendedColors // Not implemented yet
    } else {
        lightExtendedColors
    }

    // NEW: Provide extended colors via CompositionLocal
    CompositionLocalProvider(
        LocalExtendedColors provides extendedColors
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
```

**VALIDATE**:
```bash
./gradlew assembleDebug
```
**IF_FAIL**:
- Verify `ExtendedColors.kt` is in same package (`com.voxplanapp.ui.theme`)
- Check imports for `CompositionLocalProvider`
- Ensure `lightExtendedColors` and `LocalExtendedColors` are accessible

**EXPECTED**: Compilation succeeds

---

### PHASE 6: Test Theme Access

**ACTION** Create test composable to verify theme works
**OPERATION**: Temporarily add test composable to verify colors accessible

**FILE**: `app/src/main/java/com/voxplanapp/ui/theme/ThemeTest.kt` (TEMPORARY FILE)

**CONTENT**:
```kotlin
package com.voxplanapp.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * TEMPORARY TEST COMPOSABLE
 *
 * Purpose: Verify Material 3 theme and extended colors are accessible.
 * Delete this file after testing completes.
 */
@Composable
fun ThemeTestScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        // Test semantic colors
        Text(
            text = "Primary Color",
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text = "On Surface (text color)",
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.bodyLarge
        )

        // Test extended colors
        Text(
            text = "Medal Bronze",
            color = MaterialTheme.extendedColors.medalBronze,
            style = MaterialTheme.typography.bodyLarge
        )

        Text(
            text = "Medal Gold",
            color = MaterialTheme.extendedColors.medalGold,
            style = MaterialTheme.typography.bodyLarge
        )

        Text(
            text = "Power Bar Fill",
            color = MaterialTheme.extendedColors.powerBarFill,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
```

**TEST STEPS**:
1. Temporarily add `ThemeTestScreen()` to `VoxPlanApp.kt` navigation
2. Run app: `./gradlew installDebug && adb shell am start -n com.voxplanapp/.MainActivity`
3. Navigate to test screen
4. **VERIFY**:
   - "Primary Color" text is VoxPlan dark blue (0xFF0F0A2C)
   - "Medal Bronze" text is bronze color (0xFFCD7F32)
   - "Medal Gold" text is gold color (0xFFFFD700)
   - No crashes or "Unspecified color" errors
5. Remove `ThemeTestScreen` from navigation
6. Delete `ThemeTest.kt` file

**VALIDATE**:
- Visual inspection of test screen
- No runtime crashes
- Colors match expected hex values

**IF_FAIL**:
- Check `VoxPlanTheme` is used in `MainActivity.setContent { }`
- Verify `CompositionLocalProvider` is providing `LocalExtendedColors`
- Use `adb logcat` to check for Compose errors

**EXPECTED**: All colors display correctly, no crashes

---

### PHASE 7: Contrast Testing (Extended Colors Only)

**ACTION** Test WCAG compliance for all extended colors
**OPERATION**: Use WebAIM Contrast Checker for manual testing

**TOOL**: [WebAIM Contrast Checker](https://webaim.org/resources/contrastchecker/)

**TEST MATRIX**:

| Extended Color | Hex | Background | Hex | Minimum Ratio | Test Result | Pass? |
|----------------|-----|------------|-----|---------------|-------------|-------|
| `medalBronze` | `#CD7F32` | `surface` | `#FFFBFE` | 4.5:1 | ??? | ??? |
| `medalSilver` | `#C0C0C0` | `surface` | `#FFFBFE` | 4.5:1 | ??? | ??? |
| `medalGold` | `#FFD700` | `surface` | `#FFFBFE` | 4.5:1 | ??? | ??? |
| `medalDiamond` | `#B9F2FF` | `surface` | `#FFFBFE` | 4.5:1 | ??? | ??? |
| `powerBarFill` | `#4CAF50` | `powerBarBackground` | `#E0E0E0` | 3:1 | ??? | ??? |
| `eventScheduled` | `#41c300` | `surface` | `#FFFBFE` | 3:1 (UI) | ??? | ??? |
| `quotaComplete` | `#FFC107` | `surface` | `#FFFBFE` | 3:1 (UI) | ??? | ??? |

**TESTING STEPS** (for each extended color):
1. Go to [WebAIM Contrast Checker](https://webaim.org/resources/contrastchecker/)
2. Enter foreground color (extended color hex)
3. Enter background color (surface or specific background hex)
4. Record ratio shown
5. Check if passes AA/AAA standards:
   - **Text (< 18sp)**: 4.5:1 (AA), 7:1 (AAA)
   - **Large text / UI components**: 3:1 (AA), 4.5:1 (AAA)
6. If fails, click "Lighten" or "Darken" to get suggestions
7. Update color in `ExtendedColors.kt` if needed
8. Document results in comments

**DOCUMENT RESULTS** in `ExtendedColors.kt`:
```kotlin
val lightExtendedColors = ExtendedColors(
    // TESTED: Bronze on surface = 5.2:1 ✅ AA + AAA
    medalBronze = Color(0xFFCD7F32),

    // TESTED: Silver on surface = 3.1:1 ⚠️ AA for large text only (not normal text)
    medalSilver = Color(0xFFC0C0C0), // Consider darkening for normal text use

    // ... etc.
)
```

**IF_FAIL** (any color fails 4.5:1 for text):
- Use WebAIM suggestions (lighten/darken buttons)
- Adjust hex value in `ExtendedColors.kt`
- Re-test until passes
- **Prioritize accessibility over exact color match**

**VALIDATE**: All extended colors meet minimum contrast ratios for their use cases

**EXPECTED**: Document shows test results, any failing colors adjusted

---

### PHASE 8: Documentation

**ACTION** Document color mapping decisions
**OPERATION**: Add comments to Color.kt and ExtendedColors.kt explaining choices

**FILE**: `app/src/main/java/com/voxplanapp/ui/theme/Color.kt`

**ADD AT TOP**:
```kotlin
/**
 * VoxPlanApp Material 3 Color Palette (Light Mode)
 *
 * Generated with Material Theme Builder and manually adjusted to match VoxPlan branding.
 * Base primary color: #0F0A2C (dark blue-purple)
 *
 * Semantic Role Mapping:
 * - primary: VoxPlan PrimaryColor (0xFF0F0A2C) - CTAs, active states
 * - primaryContainer: PrimaryLightColor (0xFF82BD85) - selected cards, tonal buttons
 * - secondary: PrimaryDarkColor (0xFF009688) - secondary actions, filters
 * - tertiary: TertiaryColor (0xFF82babd) - accents, highlights
 * - onSurface: PrimaryColor (0xFF0F0A2C) - primary text color
 * - surfaceContainer: SecondaryColor (0xFFC7E2C9) - card backgrounds
 * - outline: SubGoalItemBorderColor (0xFFB2DFDB) - borders, dividers
 *
 * Extended colors (medals, power bars, goals) are in ExtendedColors.kt.
 *
 * WCAG Compliance: All semantic role pairings (primary/onPrimary, surface/onSurface, etc.)
 * are guaranteed to meet WCAG AA standards by Material 3 tonal palette generation.
 *
 * Dynamic Colors: Disabled for VoxPlan branding consistency (see Theme.kt).
 */
```

**FILE**: `app/src/main/java/com/voxplanapp/ui/theme/ExtendedColors.kt`

**UPDATE HEADER COMMENT** with test results:
```kotlin
/**
 * Extended colors for VoxPlanApp UI elements that don't fit Material 3 semantic roles.
 *
 * ## Contrast Testing Results
 *
 * All extended colors tested with WebAIM Contrast Checker:
 * - medalBronze: 5.2:1 ✅ AA + AAA (text)
 * - medalSilver: 3.1:1 ⚠️ AA large text only
 * - medalGold: 6.8:1 ✅ AA + AAA (text)
 * - medalDiamond: 4.9:1 ✅ AA (text)
 * - powerBarFill: 4.1:1 ✅ AA (UI component)
 * - quotaComplete: 5.5:1 ✅ AA + AAA (UI)
 *
 * Test date: [YYYY-MM-DD]
 * Tool: https://webaim.org/resources/contrastchecker/
 *
 * ## Usage Guidelines
 *
 * Access via MaterialTheme.extendedColors:
 * ```
 * val medalColor = MaterialTheme.extendedColors.medalBronze
 * ```
 *
 * ## Future Enhancements
 * - Dark mode extended colors (not implemented)
 * - Additional goal category colors (as needed)
 * - Dynamic color harmonization (Android 12+)
 */
```

---

## Validation Strategy

### Build Validation
```bash
./gradlew clean assembleDebug
```
**Success Criteria**:
- No compilation errors
- Warnings about unused colors OK (will be cleaned up after screen migration)

### Theme Access Verification
**Manual test using ThemeTestScreen (Phase 6)**:
- [ ] `MaterialTheme.colorScheme.primary` returns VoxPlan blue (0xFF0F0A2C)
- [ ] `MaterialTheme.colorScheme.onSurface` returns VoxPlan text color
- [ ] `MaterialTheme.extendedColors.medalBronze` returns bronze color
- [ ] `MaterialTheme.extendedColors.medalGold` returns gold color
- [ ] No "Unspecified color" crashes

### Accessibility Validation
**All extended colors tested with WebAIM** (Phase 7):
- [ ] Medal colors meet 4.5:1 ratio on surface background
- [ ] Power bar fill meets 3:1 ratio on power bar background
- [ ] Quota colors meet 3:1 ratio on relevant backgrounds
- [ ] Results documented in `ExtendedColors.kt` comments

### Code Quality Checks
```bash
./gradlew lint
```
**Success Criteria**: No new warnings introduced (existing warnings OK)

---

## Rollback Strategy

### If Phase 2 Fails (Theme File Replacement):
```bash
# Restore backup files
mv app/src/main/java/com/voxplanapp/ui/theme/Theme.kt.bak \
   app/src/main/java/com/voxplanapp/ui/theme/Theme.kt

mv app/src/main/java/com/voxplanapp/ui/theme/Color.kt.bak \
   app/src/main/java/com/voxplanapp/ui/theme/Color.kt

# Rebuild
./gradlew clean assembleDebug
```

### If Phase 4 or 5 Fails (Extended Colors):
```bash
# Delete new file
rm app/src/main/java/com/voxplanapp/ui/theme/ExtendedColors.kt

# Revert Theme.kt changes (remove CompositionLocalProvider)
git checkout app/src/main/java/com/voxplanapp/ui/theme/Theme.kt

# Rebuild
./gradlew clean assembleDebug
```

### If Integration Issues:
```bash
# Full revert to start of task
git checkout app/src/main/java/com/voxplanapp/ui/theme/Theme.kt
git checkout app/src/main/java/com/voxplanapp/ui/theme/Color.kt
rm app/src/main/java/com/voxplanapp/ui/theme/ExtendedColors.kt
./gradlew clean assembleDebug
```

---

## Success Criteria

- [ ] `Theme.kt` contains VoxPlanTheme composable (replaces default Android Studio setup)
- [ ] `Color.kt` contains VoxPlan color values (not purple/pink defaults)
- [ ] `ExtendedColors.kt` created with medal, power bar, and quota colors
- [ ] Extended colors integrated via CompositionLocal pattern
- [ ] `dynamicColor` parameter defaults to `false` (brand consistency)
- [ ] All extended colors tested for WCAG compliance (results documented)
- [ ] Code compiles without errors
- [ ] `MaterialTheme.colorScheme.*` accessible in composables
- [ ] `MaterialTheme.extendedColors.*` accessible in composables
- [ ] ThemeTestScreen displays all colors correctly (then removed)
- [ ] Documentation added to Color.kt and ExtendedColors.kt
- [ ] No regressions in existing screens (theme not yet applied, so no changes expected)

---

## Files Modified

1. **`app/src/main/java/com/voxplanapp/ui/theme/Color.kt`** - Replace with Material Theme Builder export, adjust to VoxPlan hex values
2. **`app/src/main/java/com/voxplanapp/ui/theme/Theme.kt`** - Replace with Material Theme Builder export, integrate extended colors
3. **`app/src/main/java/com/voxplanapp/ui/theme/ExtendedColors.kt`** - NEW FILE - Define VoxPlan-specific extended colors

**TEMPORARY**:
4. **`app/src/main/java/com/voxplanapp/ui/theme/ThemeTest.kt`** - Test composable (DELETE after Phase 6)

---

## Dependencies

**No new dependencies required** - Material 3 already in project:
```kotlin
// build.gradle.kts (already exists)
implementation "androidx.compose.material3:material3:1.0.0"
```

**External Tools Used**:
- [Material Theme Builder](https://material-foundation.github.io/material-theme-builder/) - Generate color schemes
- [WebAIM Contrast Checker](https://webaim.org/resources/contrastchecker/) - Accessibility testing

---

## Security Considerations

**No security impact** - This task only modifies color definitions, no data handling or permissions.

**Static Analysis**: Colors are compile-time constants, no runtime vulnerabilities.

---

## Performance Impact

**Negligible**:
- Color definitions are static constants (no runtime computation)
- `CompositionLocal` for extended colors has minimal overhead (same pattern as Material 3)
- No impact on app startup or memory usage
- Theme switching (future dark mode) would use same mechanism as Material 3 (already optimized)

---

## Notes

- **This task does NOT migrate any screens** - screens will continue using hardcoded colors from `Colors.kt` until separate migration task
- **Light mode only** - dark mode is future enhancement (requires separate task)
- **Dynamic colors disabled** - VoxPlan needs brand consistency; can be enabled later with extended color preservation
- **16 unused color constants** in `Colors.kt` - keep for now, will be deprecated after screen migration confirms they're truly unused
- **Material Theme Builder** generates all 35+ color values automatically - only need to adjust 6-8 priority colors to match VoxPlan exactly
- **Extended colors pattern** is standard Android approach (same as Google Reply sample app uses for custom colors)

---

## AI Docs References

**Comprehensive guides created during research**:
1. `.claude/PRPs/ai_docs/material3_color_system.md` (914 lines) - Complete Material 3 color system guide
2. `.claude/PRPs/ai_docs/voxplan_color_mapping_template.md` (363 lines) - VoxPlan-specific migration template

**Key External References**:
- [Material Design 3 in Compose | Android Developers](https://developer.android.com/develop/ui/compose/designsystems/material3)
- [Color Roles - Material Design 3](https://m3.material.io/styles/color/roles)
- [Material Theme Builder Tool](https://material-foundation.github.io/material-theme-builder/)
- [Custom Colour Scheme with Material3](https://herrbert74.github.io/posts/custom-color-scheme-with-m3-compose/)
- [WebAIM Contrast Checker](https://webaim.org/resources/contrastchecker/)
- [Google Reply Sample App](https://github.com/android/compose-samples/tree/main/Reply) - Extended colors implementation reference

---

## Estimated Effort

- **Phase 1 (Generate)**: 15 minutes
- **Phase 2 (Replace)**: 10 minutes
- **Phase 3 (Adjust)**: 20 minutes
- **Phase 4 (Extended Colors)**: 20 minutes
- **Phase 5 (Integration)**: 15 minutes
- **Phase 6 (Test)**: 20 minutes
- **Phase 7 (Contrast)**: 45 minutes
- **Phase 8 (Documentation)**: 15 minutes

**Total**: ~2.5 hours

**Confidence Score**: **8/10** - High confidence for one-pass implementation success

**Risk Areas**:
- Manual color adjustments (Phase 3) - may need iteration to match VoxPlan branding exactly
- Contrast testing (Phase 7) - some extended colors may need adjustment
- Material Theme Builder generated palette may need more tweaking than expected

**Mitigation**: Comprehensive research completed, real-world examples reviewed, clear rollback strategy defined.
