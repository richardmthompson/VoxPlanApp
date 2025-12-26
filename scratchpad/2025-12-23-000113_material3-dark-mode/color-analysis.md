# VoxPlan Dark Mode Color Analysis & Palette Generation

## Executive Summary

This document provides the complete color analysis for implementing Material 3 dark mode in VoxPlanApp. It includes:
- Generated Material 3 tonal palettes from VoxPlan brand green
- Documentation of all 80+ existing color usages
- Semantic mapping from existing colors to Material 3 roles
- Extended color scheme for preserved branding elements

## 1. Material 3 Color Palette Generation

### Seed Color
**VoxPlan Brand Green**: `#1BA821` (Color(0xFF1BA821))

### Generation Strategy
We'll use **MaterialKolor** library (version 4.0.0) to programmatically generate the complete Material 3 color scheme:

```kotlin
// Add to app/build.gradle.kts dependencies
implementation("com.materialkolor:material-kolor:4.0.0")

// Usage in Theme.kt
@Composable
fun VoxPlanAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = rememberDynamicColorScheme(
        seedColor = Color(0xFF1BA821),  // VoxPlan green
        isDark = darkTheme,
        style = PaletteStyle.TonalSpot  // Default Material 3 style
    )

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
```

### Generated Tonal Palettes (Estimated)

MaterialKolor generates 5 key tonal palettes, each with 13 tones (0, 10, 20, 30, 40, 50, 60, 70, 80, 90, 95, 99, 100):

**Primary Palette** (from #1BA821):
- Primary Light: Tone 40 (~#157A1C - darker green for light mode)
- On Primary Light: Tone 100 (#FFFFFF)
- Primary Container Light: Tone 90 (~#B8F5BC - very light green)
- On Primary Container Light: Tone 10 (~#002203)

- Primary Dark: Tone 80 (~#9DDB9F - light green for dark mode)
- On Primary Dark: Tone 20 (~#00390A)
- Primary Container Dark: Tone 30 (~#005314)
- On Primary Container Dark: Tone 90 (~#B8F5BC)

**Secondary Palette** (harmonized with primary):
- Derived from seed, typically shifted hue
- Used for FABs, selection controls, less prominent components

**Tertiary Palette**:
- Accent colors for highlighting, differentiating content
- Complementary to primary

**Error Palette**:
- Standard Material 3 error red tones
- Error Light: ~#BA1A1A
- Error Dark: ~#FFB4AB

**Neutral/Neutral Variant Palettes**:
- Surface, background, outline colors
- Surface Light: Tone 99 (~#FDFCF6)
- Surface Dark: Tone 6 (~#191C18)

### Why MaterialKolor?

1. **Programmatic**: Generates palettes in Kotlin, no manual color picking
2. **Material 3 Compliant**: Uses official HCT color space algorithm
3. **Dynamic**: Supports runtime palette changes if needed later
4. **Maintained**: Active library (v4.0.0 released recently)
5. **Compose-Native**: Designed for Jetpack Compose projects

## 2. All Existing Colors Documented

### 2.1 Colors.kt Constants (26 colors)

Located: `app/src/main/java/com/voxplanapp/ui/constants/Colors.kt`

| Constant Name | Hex Value | RGB | Semantic Purpose | Material 3 Mapping |
|---------------|-----------|-----|------------------|-------------------|
| `PrimaryColor` | `#0F0A2C` | (15,10,44) | Very dark blue/black background | `colorScheme.surface` (dark) |
| `PrimaryDarkColor` | `#009688` | (0,150,136) | Teal accent, close to VoxPlan green | `colorScheme.tertiary` or extended |
| `PrimaryLightColor` | `#82BD85` | (130,189,133) | Light green for completed states | `colorScheme.primaryContainer` |
| `AccentColor` | `#C28F2C` | (194,143,44) | Gold accent for highlights | `colorScheme.secondary` |
| `ToolbarColor` | `#FFF2DF` | (255,242,223) | Cream toolbar background | `colorScheme.surfaceVariant` (light) |
| `DarkerToolbarColor` | `#3E3B3B` | (62,59,59) | Dark gray toolbar | `colorScheme.surfaceVariant` (dark) |
| `NavigationItemColor` | `#CFD8DC` | (207,216,220) | Light gray nav items | `colorScheme.onSurfaceVariant` |
| `SelectedIconColor` | `#009688` | (0,150,136) | Teal selected state | `colorScheme.primary` |
| `UnselectedIconColor` | `#8C8C8C` | (140,140,140) | Gray unselected state | `colorScheme.onSurfaceVariant` |
| `FabGreen` | `#00FF00` | (0,255,0) | Bright green FAB | `colorScheme.primaryContainer` (adjusted tone) |
| `LightBlueBackground` | `#E3F2FD` | (227,242,253) | Very light blue background | `colorScheme.surfaceVariant` |
| `DarkSurfaceColor` | `#121212` | (18,18,18) | Standard dark surface | `colorScheme.surface` (dark) - Material spec |
| `MediumGray` | `#666666` | (102,102,102) | Medium gray for secondary text | `colorScheme.onSurfaceVariant` |
| `EventBoxColor` | `#41C300` | (65,195,0) | Bright green for event boxes | `colorScheme.primaryContainer` |
| `EventBoxInnerBoxColour` | `#75E800` | (117,232,0) | Brighter green for inner event | `colorScheme.primaryContainer` + lighten |
| `DeleteRed` | `#D32F2F` | (211,47,47) | Red for delete actions | `colorScheme.error` |
| `HighlightYellow` | `#FFEB3B` | (255,235,59) | Yellow for highlights | `colorScheme.tertiary` or extended |
| `WarningOrange` | `#FF9800` | (255,152,0) | Orange for warnings | Extended color |
| `SuccessGreen` | `#4CAF50` | (76,175,80) | Green for success states | `colorScheme.primary` |
| `InfoBlue` | `#2196F3` | (33,150,243) | Blue for info messages | `colorScheme.secondary` |
| `SubtleGray` | `#E0E0E0` | (224,224,224) | Light gray borders/dividers | `colorScheme.outlineVariant` |
| `QuotaProgressColor` | `#4CAF50` | (76,175,80) | Green for quota progress bars | `colorScheme.primary` |
| `QuotaBackgroundColor` | `#E8F5E9` | (232,245,233) | Very light green background | `colorScheme.surfaceVariant` (light) |
| `QuotaCompleteBackgroundColor` | `#FFC107` | (255,193,7) | Gold for completed quotas | `colorScheme.tertiary` or extended |
| `DailyItemBackgroundLight` | `#F5F5F5` | (245,245,245) | Light gray daily item bg | `colorScheme.surfaceVariant` |
| `DailyItemBackgroundDark` | `#2C2C2C` | (44,44,44) | Dark gray daily item bg | `colorScheme.surfaceVariant` (dark) |

**Analysis**:
- **Green family** (6 colors): Map to primary palette tones
- **Accent colors** (gold, teal): Map to secondary/tertiary
- **Surfaces** (5 colors): Map to surface/surfaceVariant at different tones
- **Semantic states** (success, warning, error, info): Use Material 3 semantic slots
- **Grays** (6 colors): Map to neutral palette tones

### 2.2 Inline Color Usage (80 occurrences across 9 files)

Analyzed via: `grep -r "Color(0x" app/src/main/java/com/voxplanapp/ui/ --include="*.kt" | wc -l`

#### MainScreen.kt - Power Bar Colors (11 colors) **MUST PRESERVE**

Located: `MainScreen.kt:180-306` (PowerBarDisplay and related components)

| Usage | Hex Value | Purpose | Preservation Strategy |
|-------|-----------|---------|----------------------|
| Container background | `Color.Black` | Power bar background | Extended color: `powerBarBackground` |
| "POWER:" label | `Color(0xFFFF5722)` | Deep Orange 500 | Extended color: `powerBarLabel` |
| Diamond count text | `Color(0xFF4CAF50)` | Green 400 | Extended color: `powerBarDiamond` |
| Diamond background | `Color(0xFF9C27B0)` | Purple 500 | Extended color: `powerBarDiamondBg` |
| Diamond border | `Color(0xFFBA68C8)` | Purple 300 | Extended color: `powerBarDiamondBorder` |
| Bar border (full) | `Color(0xFF1BA821)` | VoxPlan green! | Extended color: `powerBarFullBorder` |
| Bar border (partial) | `Color(0xFF3F51B5)` | Indigo 500 | Extended color: `powerBarPartialBorder` |
| Bar fill (full) | `Color(0xFF13D31B)` | Bright green | Extended color: `powerBarFullFill` |
| Bar fill (partial) | `Color(0xFFFF0000)` | Red | Extended color: `powerBarPartialFill` |
| Coin background | `Color(0xFFFFD700)` | Gold | Extended color: `powerBarCoinBg` |
| Coin border | `Color(0xFFFF9800)` | Orange 500 | Extended color: `powerBarCoinBorder` |

**Implementation**:
```kotlin
// Create extended color scheme (same values for light AND dark themes)
data class PowerBarColors(
    val background: Color = Color.Black,
    val label: Color = Color(0xFFFF5722),
    val diamond: Color = Color(0xFF4CAF50),
    val diamondBackground: Color = Color(0xFF9C27B0),
    val diamondBorder: Color = Color(0xFFBA68C8),
    val fullBorder: Color = Color(0xFF1BA821),
    val partialBorder: Color = Color(0xFF3F51B5),
    val fullFill: Color = Color(0xFF13D31B),
    val partialFill: Color = Color(0xFFFF0000),
    val coinBackground: Color = Color(0xFFFFD700),
    val coinBorder: Color = Color(0xFFFF9800)
)

val LocalPowerBarColors = staticCompositionLocalOf { PowerBarColors() }
```

#### DailyScreen.kt (9+ inline colors)

| Line Range | Color | Purpose | Material 3 Mapping |
|------------|-------|---------|-------------------|
| ~200 | `Color.White` | Card backgrounds | `colorScheme.surface` |
| ~215 | `Color(0xFFE0E0E0)` | Dividers | `colorScheme.outlineVariant` |
| ~230 | `Color(0xFF4CAF50)` | Success indicators | `colorScheme.primary` |
| ~245 | Various grays | Text colors | `colorScheme.onSurface` variations |

*(Full line-by-line analysis needed)*

#### DaySchedule.kt (15+ inline colors)

Event boxes, time slots, grid lines, headers - detailed analysis needed

#### FocusModeScreen.kt (20+ inline colors)

Timer displays, medal colors, task completion indicators - **Note: This screen stays dark-only, but should still use theme colors for consistency**

#### GoalEditScreen.kt (5+ inline colors)

Form elements, quota indicators - detailed analysis needed

#### ProgressScreen.kt (8+ inline colors)

Charts, progress bars, week indicators - detailed analysis needed

#### Other screens (12+ inline colors)

Various UI elements across remaining screens

### 2.3 Theme.kt Placeholder Colors (6 colors)

Located: `app/src/main/java/com/voxplanapp/ui/theme/Color.kt`

| Constant | Hex Value | Status |
|----------|-----------|--------|
| `Purple80` | `#D0BCFF` | **REPLACE** with generated primary tone 80 |
| `PurpleGrey80` | `#CCC2DC` | **REPLACE** with generated neutralVariant tone 80 |
| `Pink80` | `#EFB8C8` | **REPLACE** with generated tertiary tone 80 |
| `Purple40` | `#6650A4` | **REPLACE** with generated primary tone 40 |
| `PurpleGrey40` | `#625B71` | **REPLACE** with generated neutralVariant tone 40 |
| `Pink40` | `#7D5260` | **REPLACE** with generated tertiary tone 40 |

**Action**: Delete this file entirely, use MaterialKolor generated colors

## 3. Semantic Color Mapping Strategy

### 3.1 Direct Material 3 ColorScheme Mappings

| Existing Usage | Material 3 Role | Light Mode Tone | Dark Mode Tone | Rationale |
|----------------|----------------|-----------------|----------------|-----------|
| Primary buttons, FABs | `primary` | 40 | 80 | Core brand color |
| Text on primary | `onPrimary` | 100 | 20 | Contrast with primary |
| Success states | `primary` | 40 | 80 | Aligns with VoxPlan green |
| Completed items | `primaryContainer` | 90 | 30 | Lower emphasis success |
| Accents, highlights | `secondary` | 40 | 80 | Secondary brand element |
| Warnings | `tertiary` | 40 | 80 | Distinct from primary/secondary |
| Errors, delete | `error` | 40 | 80 | Standard error semantics |
| Main backgrounds | `surface` | 99 | 6 | Primary surface |
| Cards, elevated surfaces | `surfaceVariant` | 90 | 30 | Subtle elevation |
| Body text | `onSurface` | 10 | 90 | High contrast text |
| Secondary text | `onSurfaceVariant` | 30 | 80 | Lower emphasis text |
| Borders, dividers | `outline` | 50 | 60 | Medium contrast |
| Subtle dividers | `outlineVariant` | 80 | 30 | Low contrast |

### 3.2 Extended Color Scheme

For colors that don't fit Material 3's semantic roles (power bar branding, custom game elements):

```kotlin
// ExtendedColors.kt
data class ExtendedColorScheme(
    // Power bar colors (IDENTICAL in light and dark)
    val powerBar: PowerBarColors = PowerBarColors(),

    // Additional game/branding colors if needed
    val gamification: GamificationColors = GamificationColors()
)

data class GamificationColors(
    val medalBronze: Color = Color(0xFFCD7F32),
    val medalSilver: Color = Color(0xFFC0C0C0),
    val medalGold: Color = Color(0xFFFFD700),
    val medalDiamond: Color = Color(0xFFB9F2FF)
    // Add more as discovered during implementation
)

val LocalExtendedColors = staticCompositionLocalOf { ExtendedColorScheme() }
```

### 3.3 Colors.kt Migration Plan

Each constant in Colors.kt will be replaced with theme access:

**Before**:
```kotlin
// In composable
import com.voxplanapp.ui.constants.PrimaryColor
Box(modifier = Modifier.background(PrimaryColor))
```

**After**:
```kotlin
// In composable
Box(modifier = Modifier.background(MaterialTheme.colorScheme.surface))
```

**Migration Table**:

| Colors.kt Constant | Replace With |
|-------------------|--------------|
| `PrimaryColor` | `MaterialTheme.colorScheme.surface` (in dark theme context) |
| `PrimaryDarkColor` | `MaterialTheme.colorScheme.tertiary` |
| `PrimaryLightColor` | `MaterialTheme.colorScheme.primaryContainer` |
| `AccentColor` | `MaterialTheme.colorScheme.secondary` |
| `ToolbarColor` | `MaterialTheme.colorScheme.surfaceVariant` |
| `DarkerToolbarColor` | `MaterialTheme.colorScheme.surfaceVariant` (auto-dark) |
| `NavigationItemColor` | `MaterialTheme.colorScheme.onSurfaceVariant` |
| `SelectedIconColor` | `MaterialTheme.colorScheme.primary` |
| `UnselectedIconColor` | `MaterialTheme.colorScheme.onSurfaceVariant` |
| `FabGreen` | `MaterialTheme.colorScheme.primaryContainer` |
| `LightBlueBackground` | `MaterialTheme.colorScheme.surfaceVariant` |
| `DarkSurfaceColor` | `MaterialTheme.colorScheme.surface` (auto-dark) |
| `MediumGray` | `MaterialTheme.colorScheme.onSurfaceVariant` |
| `EventBoxColor` | `MaterialTheme.colorScheme.primaryContainer` |
| `EventBoxInnerBoxColour` | `MaterialTheme.colorScheme.primaryContainer` + `.copy(alpha = 0.8f)` |
| `DeleteRed` | `MaterialTheme.colorScheme.error` |
| `HighlightYellow` | `MaterialTheme.colorScheme.tertiary` |
| `WarningOrange` | `LocalExtendedColors.current.gamification.warning` |
| `SuccessGreen` | `MaterialTheme.colorScheme.primary` |
| `InfoBlue` | `MaterialTheme.colorScheme.secondary` |
| `SubtleGray` | `MaterialTheme.colorScheme.outlineVariant` |
| `QuotaProgressColor` | `MaterialTheme.colorScheme.primary` |
| `QuotaBackgroundColor` | `MaterialTheme.colorScheme.surfaceVariant` |
| `QuotaCompleteBackgroundColor` | `MaterialTheme.colorScheme.tertiary` |
| `DailyItemBackgroundLight` | `MaterialTheme.colorScheme.surfaceVariant` (auto-light) |
| `DailyItemBackgroundDark` | `MaterialTheme.colorScheme.surfaceVariant` (auto-dark) |

## 4. Implementation Architecture

### 4.1 New File Structure

```
app/src/main/java/com/voxplanapp/ui/theme/
├── Theme.kt                  # Main theme composable (MODIFY)
├── Color.kt                  # DEPRECATE - delete after migration
├── ExtendedColors.kt         # NEW - power bar + game colors
├── Typography.kt             # Existing - no changes
└── Shape.kt                  # Existing - no changes

app/src/main/java/com/voxplanapp/ui/constants/
└── Colors.kt                 # DEPRECATE - delete after migration
```

### 4.2 Theme.kt Refactored

```kotlin
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.materialkolor.PaletteStyle
import com.materialkolor.rememberDynamicColorScheme

// Extended color definitions
data class PowerBarColors(
    val background: Color = Color.Black,
    val label: Color = Color(0xFFFF5722),
    val diamond: Color = Color(0xFF4CAF50),
    val diamondBackground: Color = Color(0xFF9C27B0),
    val diamondBorder: Color = Color(0xFFBA68C8),
    val fullBorder: Color = Color(0xFF1BA821),
    val partialBorder: Color = Color(0xFF3F51B5),
    val fullFill: Color = Color(0xFF13D31B),
    val partialFill: Color = Color(0xFFFF0000),
    val coinBackground: Color = Color(0xFFFFD700),
    val coinBorder: Color = Color(0xFFFF9800)
)

data class ExtendedColorScheme(
    val powerBar: PowerBarColors = PowerBarColors()
)

val LocalExtendedColors = staticCompositionLocalOf { ExtendedColorScheme() }

@Composable
fun VoxPlanAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,  // Disabled to preserve brand
    content: @Composable () -> Unit
) {
    // Generate Material 3 color scheme from VoxPlan green
    val colorScheme = rememberDynamicColorScheme(
        seedColor = Color(0xFF1BA821),  // VoxPlan brand green
        isDark = darkTheme,
        style = PaletteStyle.TonalSpot  // Default Material 3 style
    )

    val extendedColors = ExtendedColorScheme(
        powerBar = PowerBarColors()  // Same in both light/dark
    )

    CompositionLocalProvider(LocalExtendedColors provides extendedColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = Shapes,
            content = content
        )
    }
}

// Helper extension for easy access
object AppTheme {
    val colors: ExtendedColorScheme
        @Composable
        get() = LocalExtendedColors.current
}
```

### 4.3 MainActivity.kt Fix

**Current** (line ~25):
```kotlin
setContent {
    VoxPlanApp()
}
```

**After**:
```kotlin
setContent {
    VoxPlanAppTheme {
        VoxPlanApp()
    }
}
```

## 5. Accessibility Considerations

### 5.1 Contrast Ratios

Material 3's HCT algorithm ensures WCAG AA compliance (4.5:1) for:
- `primary` / `onPrimary`
- `surface` / `onSurface`
- `error` / `onError`
- All container / onContainer pairs

**Validation needed for**:
- Power bar colors (branding exception, but should verify)
- Extended gamification colors

### 5.2 Dark Mode Specifics

Material 3 dark mode uses:
- **Elevated surfaces**: Higher tones (e.g., surface = tone 6, surfaceVariant = tone 30)
- **No shadows**: Tonal overlays replace shadows for elevation
- **Reduced contrast**: Softer whites (tone 90) instead of pure white (tone 100)

## 6. Migration Checklist

- [ ] Add MaterialKolor dependency to build.gradle.kts
- [ ] Create ExtendedColors.kt with power bar definitions
- [ ] Refactor Theme.kt to use MaterialKolor
- [ ] Fix MainActivity.kt to wrap in theme
- [ ] Migrate MainScreen.kt power bar to use `AppTheme.colors.powerBar`
- [ ] Migrate DailyScreen.kt inline colors (9 files total)
- [ ] Replace all Colors.kt constant imports (26 constants)
- [ ] Delete Color.kt (placeholder colors)
- [ ] Delete Colors.kt (deprecated constants)
- [ ] Visual testing: Light mode MainScreen
- [ ] Visual testing: Dark mode MainScreen
- [ ] Visual testing: Light mode DailyScreen
- [ ] Visual testing: Dark mode DailyScreen
- [ ] Verify power bar unchanged in both modes
- [ ] Accessibility testing (contrast ratios)
- [ ] User preference toggle implementation (Settings screen)

## 7. Testing Strategy

### 7.1 Visual Regression Tests

**Power bar verification** (CRITICAL):
1. Screenshot power bar in current implementation
2. Screenshot power bar after migration (light mode)
3. Screenshot power bar after migration (dark mode)
4. Pixel-perfect comparison - must be identical

**Screen comparisons**:
1. MainScreen light vs dark
2. DailyScreen light vs dark
3. ProgressScreen light vs dark
4. GoalEditScreen light vs dark
5. DaySchedule light vs dark

### 7.2 Automated Tests (Future)

```kotlin
@Test
fun powerBarColors_preservedAfterMigration() {
    val powerBar = PowerBarColors()
    assertEquals(Color.Black, powerBar.background)
    assertEquals(Color(0xFFFF5722), powerBar.label)
    assertEquals(Color(0xFF1BA821), powerBar.fullBorder)
    // ... all 11 colors
}

@Test
fun theme_generatesPrimaryFromSeed() {
    val colorScheme = rememberDynamicColorScheme(
        seedColor = Color(0xFF1BA821),
        isDark = false
    )
    // Verify primary is greenish (hue validation)
    assertTrue(colorScheme.primary.green > colorScheme.primary.red)
}
```

## 8. Open Questions for User

1. **Color Palette Generation**:
   - Use MaterialKolor library (recommended) or manual palette?
   - If manual: Use Material Theme Builder Figma plugin and export?

2. **Implementation Scope**:
   - MVP: MainScreen + DailyScreen only (10 files)
   - Full: All 20 files at once

3. **Dynamic Color**:
   - Keep `dynamicColor = false` (preserve branding)
   - OR: Add user toggle in Settings

4. **FocusModeScreen**:
   - Confirmed: stays dark-only, excluded from light mode?
   - Should it still use theme colors for consistency?

## 9. Material 3 Design Principles Applied

### 9.1 Color Roles Over Literal Colors

**Principle**: Use semantic roles (primary, surface, error) instead of literal color names (green, white, red)

**Application**:
- Success states → `primary` (not "SuccessGreen")
- Backgrounds → `surface` (not "WhiteBackground")
- Text → `onSurface` (not "BlackText")

### 9.2 Tonal Palettes for Elevation

**Principle**: In dark mode, elevation is shown through lighter tones, not shadows

**Application**:
- Base surface: tone 6
- Elevated cards: tone 10-20
- Dialogs: tone 30
- App bar: tone 0-6 depending on design

### 9.3 Accessibility First

**Principle**: Contrast ratios built into the color system

**Application**:
- All text on surfaces meets WCAG AA automatically
- User doesn't need to manually validate contrast
- Extended colors (power bar) need manual verification

## 10. Next Steps

1. **User approval** on this color analysis
2. **Answer 4 questions** above
3. **Generate actual color swatches** from MaterialKolor (can be done programmatically or via preview app)
4. **Create detailed line-by-line migration plan** for each of 20 files
5. **Implement in phases**: Theme setup → MainScreen → DailyScreen → Remaining screens
6. **Visual testing** at each phase

---

**Document Version**: 1.0
**Created**: 2025-12-23
**Last Updated**: 2025-12-23
**Status**: Awaiting user review and approval
