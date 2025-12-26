# Material 3 Color System - Comprehensive Guide for VoxPlanApp Migration

**Purpose**: Guide migration from hardcoded colors to Material 3 semantic color roles for VoxPlanApp (light mode only initially).

**Last Updated**: 2025-12-26

---

## Table of Contents

1. [Complete Color Role Reference](#complete-color-role-reference)
2. [Decision Tree: Which Color Role to Use](#decision-tree-which-color-role-to-use)
3. [Extended Colors Pattern](#extended-colors-pattern)
4. [Migration Checklist](#migration-checklist)
5. [WCAG Accessibility Requirements](#wcag-accessibility-requirements)
6. [Code Examples](#code-examples)
7. [Key References](#key-references)

---

## Complete Color Role Reference

### All 35+ ColorScheme Properties

Material 3's `ColorScheme` provides 35 semantic color roles organized into categories. **Always use semantic roles (what the color is for), not hex values.**

#### Primary Colors (5 roles)
| Role | Purpose | When to Use |
|------|---------|-------------|
| `primary` | Main brand color, highest emphasis | Filled buttons, FABs, active states, prominent CTAs |
| `onPrimary` | Text/icons on primary backgrounds | Always pair with `primary` background |
| `primaryContainer` | Softer tonal variant of primary | Filled tonal buttons, selected cards, medium-emphasis states |
| `onPrimaryContainer` | Text/icons on primary containers | Always pair with `primaryContainer` background |
| `inversePrimary` | Primary color for inverse surfaces | Use on `inverseSurface` backgrounds |

**Key Difference**: `primary` = high-emphasis (filled button), `primaryContainer` = medium-emphasis (tonal button/selected state)

#### Secondary Colors (4 roles)
| Role | Purpose | When to Use |
|------|---------|-------------|
| `secondary` | Less prominent accent color | Filter chips, secondary buttons, differentiation |
| `onSecondary` | Text/icons on secondary | Pair with `secondary` background |
| `secondaryContainer` | Tonal container for secondary | Medium-emphasis secondary elements |
| `onSecondaryContainer` | Text on secondary containers | Pair with `secondaryContainer` background |

**Use Case**: Secondary colors expand color expression without competing with primary brand color.

#### Tertiary Colors (4 roles)
| Role | Purpose | When to Use |
|------|---------|-------------|
| `tertiary` | Contrasting accent color | Balance primary/secondary, highlight special content |
| `onTertiary` | Text/icons on tertiary | Pair with `tertiary` background |
| `tertiaryContainer` | Tonal container for tertiary | Input fields, goals reached indicators, contrasting emphasis |
| `onTertiaryContainer` | Text on tertiary containers | Pair with `tertiaryContainer` background |

**Use Case**: Tertiary indicates content changes or draws heightened attention (e.g., goal completion).

#### Surface Colors (13 roles)
| Role | Purpose | When to Use |
|------|---------|-------------|
| `surface` | Base surface for components | Cards, sheets, menus, dialogs - majority of UI |
| `onSurface` | Primary text/icons on surface | Default text color for surface backgrounds |
| `surfaceVariant` | Alternative surface option | **Deprecated** - migrate to `surfaceContainerHighest` |
| `onSurfaceVariant` | Text on surface variants | Lower-emphasis text, secondary labels |
| `surfaceTint` | Tonal elevation overlay | Applied automatically by Material3 - rarely set directly |
| `surfaceBright` | Always brighter than `surface` | Light mode highlights |
| `surfaceDim` | Always dimmer than `surface` | Subtle depth differentiation |
| `surfaceContainer` | Default container emphasis | Standard cards, containers |
| `surfaceContainerLow` | Low emphasis containers | Subtle backgrounds |
| `surfaceContainerLowest` | Lowest emphasis | Near-background containers |
| `surfaceContainerHigh` | High emphasis containers | Elevated cards |
| `surfaceContainerHighest` | Highest emphasis | TimePicker, Menu - replaces `surfaceVariant` |
| `inverseSurface` | High-contrast alternative | Snackbars, tooltips |
| `inverseOnSurface` | Text on inverse surface | Pair with `inverseSurface` |

**CRITICAL CHANGE IN M3**: Components formerly using `Surface + TonalElevation` now use `surfaceContainer*` variants by default. **Tonal elevation is no longer tied to elevation.**

**Decision Guide**:
- Page background: `background` (or `surface` in M3)
- Cards/containers: `surfaceContainer` (or High/Low variants for emphasis)
- Layered depth: Use `surfaceContainerLowest` → `Low` → `surfaceContainer` → `High` → `Highest` progression
- Dividers/borders on surface: `outline` or `outlineVariant`

#### Background Colors (2 roles)
| Role | Purpose | When to Use |
|------|---------|-------------|
| `background` | Scrollable content backdrop | Page canvas - **M3 prefers `surface*` roles instead** |
| `onBackground` | Text/icons on background | **M3 prefers `onSurface` instead** |

**Migration**: M3 deprecates `background` in favor of `surface*` roles. Prefer surface roles for containers; use background mainly as page canvas.

#### Error Colors (4 roles)
| Role | Purpose | When to Use |
|------|---------|-------------|
| `error` | Error states and alerts | Error messages, failed validation, destructive actions |
| `onError` | Text/icons on error | Pair with `error` background |
| `errorContainer` | Soft error backgrounds | Error containers with lower emphasis |
| `onErrorContainer` | Text on error containers | Pair with `errorContainer` background |

#### Outline & Utility Colors (3 roles)
| Role | Purpose | When to Use |
|------|---------|-------------|
| `outline` | Boundaries with accessibility contrast | Text field borders, dividers - strong contrast (3:1 minimum) |
| `outlineVariant` | Decorative boundaries | Subtle dividers when strong contrast not required |
| `scrim` | Content obscuring overlay | Modal backdrop, bottom sheet scrim |

---

## Decision Tree: Which Color Role to Use

### For Buttons

```
Is this the primary CTA?
├─ YES → `primary` background + `onPrimary` text (Filled Button)
└─ NO → Is it important but not primary?
    ├─ YES → `primaryContainer` background + `onPrimaryContainer` text (Filled Tonal)
    └─ NO → Is it a secondary action?
        ├─ YES → `secondaryContainer` + `onSecondaryContainer` (or just `onSurface` for text button)
        └─ NO → Tertiary or text-only button
```

### For Containers/Cards

```
What emphasis level?
├─ Highest (TimePicker, prominent dialogs) → `surfaceContainerHighest`
├─ High (elevated cards, important containers) → `surfaceContainerHigh`
├─ Normal (standard cards) → `surfaceContainer`
├─ Low (subtle backgrounds) → `surfaceContainerLow`
└─ Lowest (near-background) → `surfaceContainerLowest`

Alternative: Need layered depth?
└─ Use progression: Lowest → Low → Container → High → Highest
```

### For Text

```
On what background?
├─ `primary` → use `onPrimary`
├─ `primaryContainer` → use `onPrimaryContainer`
├─ `surface` → use `onSurface` (primary text) or `onSurfaceVariant` (secondary text)
├─ `background` → use `onBackground` (or `onSurface` in M3)
├─ `error` → use `onError`
└─ Custom extended color → define corresponding `onCustom` color
```

### For Borders/Dividers

```
Is strong contrast needed?
├─ YES (accessibility requirement) → `outline` (3:1 minimum)
└─ NO (decorative only) → `outlineVariant`
```

### For Special UI (medals, progress bars, etc.)

```
Does it fit a semantic role?
├─ YES → Use closest semantic role (e.g., tertiary for special emphasis)
└─ NO → Use extended colors pattern (see below)
```

---

## Extended Colors Pattern

**When to Use**: VoxPlan has 26 custom colors (medals, power bars, goal colors) that don't fit standard semantic roles. Use extended colors for:
- Medal colors (bronze, silver, gold, diamond)
- Power bar fill colors
- Custom goal/category colors
- Brand-specific UI elements

### Complete Implementation Pattern

#### 1. Define Extended Color Class

```kotlin
// ui/theme/ExtendedColors.kt
@Immutable
data class ExtendedColors(
    val medalBronze: Color = Color.Unspecified,
    val medalSilver: Color = Color.Unspecified,
    val medalGold: Color = Color.Unspecified,
    val medalDiamond: Color = Color.Unspecified,
    val powerBarFill: Color = Color.Unspecified,
    val powerBarBackground: Color = Color.Unspecified,
    val goalColor1: Color = Color.Unspecified,
    val goalColor2: Color = Color.Unspecified,
    // ... other VoxPlan-specific colors
)
```

#### 2. Create Light/Dark Palettes

```kotlin
// ui/theme/Color.kt
private val lightExtendedColors = ExtendedColors(
    medalBronze = Color(0xFFCD7F32),
    medalSilver = Color(0xFFC0C0C0),
    medalGold = Color(0xFFFFD700),
    medalDiamond = Color(0xFFB9F2FF),
    powerBarFill = Color(0xFF4CAF50),
    powerBarBackground = Color(0xFFE0E0E0),
    goalColor1 = Color(0xFF6200EE), // From existing VoxPlan colors
    goalColor2 = Color(0xFF03DAC6),
    // Map all 26 VoxPlan colors here
)

// Future: darkExtendedColors = ExtendedColors(...)
```

#### 3. Set Up CompositionLocal

```kotlin
// ui/theme/Theme.kt
val LocalExtendedColors = staticCompositionLocalOf { ExtendedColors() }
```

#### 4. Provide in MaterialTheme

```kotlin
// ui/theme/Theme.kt
@Composable
fun VoxPlanTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Disable for branding control
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        // Future: dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        //     val context = LocalContext.current
        //     if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        // }
        darkTheme -> DarkColorScheme // Future
        else -> LightColorScheme
    }

    val extendedColors = if (darkTheme) {
        darkExtendedColors // Future
    } else {
        lightExtendedColors
    }

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

#### 5. Create Extension Property for Convenient Access

```kotlin
// ui/theme/Theme.kt
val MaterialTheme.extendedColors: ExtendedColors
    @Composable
    @ReadOnlyComposable
    get() = LocalExtendedColors.current
```

#### 6. Use in Composables

```kotlin
// ui/focusmode/FocusModeScreen.kt
@Composable
fun MedalBadge(medalType: MedalType) {
    val medalColor = when (medalType) {
        MedalType.Bronze -> MaterialTheme.extendedColors.medalBronze
        MedalType.Silver -> MaterialTheme.extendedColors.medalSilver
        MedalType.Gold -> MaterialTheme.extendedColors.medalGold
        MedalType.Diamond -> MaterialTheme.extendedColors.medalDiamond
    }

    Icon(
        imageVector = Icons.Default.Star,
        contentDescription = "Medal",
        tint = medalColor
    )
}

// Power bar with extended colors
@Composable
fun PowerBar(progress: Float) {
    LinearProgressIndicator(
        progress = progress,
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.extendedColors.powerBarFill,
        trackColor = MaterialTheme.extendedColors.powerBarBackground
    )
}
```

---

## Migration Checklist

### Phase 1: Audit Current Colors (VoxPlanApp Specific)

- [ ] **List all 26 VoxPlan colors** from `ui/theme/Color.kt`
- [ ] **Map to semantic roles** where possible:
  - Primary brand color → `primary`
  - Secondary accent → `secondary`
  - Tertiary highlights → `tertiary`
  - Background/surface colors → `surface*` variants
  - Error/warning colors → `error`
- [ ] **Identify custom colors** for extended palette:
  - Medal colors (bronze, silver, gold, diamond)
  - Power bar colors
  - Goal/category colors
  - Any UI-specific colors without semantic meaning

### Phase 2: Create lightColorScheme

- [ ] **Generate base scheme** using [Material Theme Builder](https://material-foundation.github.io/material-theme-builder/)
  - Input VoxPlan primary brand color
  - Export as Compose code (Theme.kt, Color.kt)
  - Download includes all tonal palettes automatically
- [ ] **Define `LightColorScheme`** in `ui/theme/Theme.kt`:
  ```kotlin
  private val LightColorScheme = lightColorScheme(
      primary = md_theme_light_primary,
      onPrimary = md_theme_light_onPrimary,
      primaryContainer = md_theme_light_primaryContainer,
      onPrimaryContainer = md_theme_light_onPrimaryContainer,
      // ... all 35 roles
  )
  ```
- [ ] **Replace hardcoded colors** with semantic roles in VoxPlanTheme

### Phase 3: Implement Extended Colors

- [ ] **Create `ExtendedColors` data class** (see pattern above)
- [ ] **Define `lightExtendedColors`** with VoxPlan-specific colors
- [ ] **Set up `LocalExtendedColors` CompositionLocal**
- [ ] **Provide in `VoxPlanTheme`** composable
- [ ] **Create `MaterialTheme.extendedColors` extension property**

### Phase 4: Migrate Composables

**Strategy**: Migrate screen-by-screen to avoid breaking all UIs at once.

- [ ] **MainScreen.kt** (Goal list)
  - Replace background colors with `surface`/`surfaceContainer`
  - Replace text colors with `onSurface`/`onSurfaceVariant`
  - Replace primary actions with `primary`/`primaryContainer`
- [ ] **FocusModeScreen.kt** (Timer + medals)
  - Migrate medal colors to `extendedColors.medalXXX`
  - Replace power bar colors with `extendedColors.powerBarXXX`
  - Replace timer text with `onSurface`
- [ ] **DailyScreen.kt** (Daily tasks)
  - Replace card backgrounds with `surfaceContainer`
  - Replace dividers with `outline`/`outlineVariant`
- [ ] **SchedulerScreen.kt** / **DaySchedule.kt**
  - Migrate event colors (keep or use extended colors)
  - Replace grid lines with `outline`
- [ ] **GoalEditScreen.kt** / **ProgressScreen.kt**
  - Replace form backgrounds with `surface`
  - Replace buttons with semantic roles
- [ ] **Common components** (buttons, cards, dialogs)
  - Replace all `Color(0xFFXXXXXX)` with semantic roles
  - Add `Modifier.background(MaterialTheme.colorScheme.surface)` where needed

### Phase 5: Test Accessibility

- [ ] **Test contrast ratios** for all text/background pairs:
  - Use [WebAIM Contrast Checker](https://webaim.org/resources/contrastchecker/)
  - Ensure 4.5:1 for normal text (14sp - 18sp)
  - Ensure 3:1 for large text (18sp+ or 14sp+ bold)
  - Ensure 3:1 for UI components (buttons, borders)
- [ ] **Test with Android Accessibility Scanner**:
  - Install on test device
  - Run on each screen
  - Fix any flagged contrast issues
- [ ] **Verify semantic pairings**:
  - `onPrimary` always on `primary` (never mismatched)
  - `onPrimaryContainer` always on `primaryContainer`
  - Same for secondary, tertiary, surface, error
- [ ] **Simulate color blindness** (optional):
  - Use [Color Oracle](https://colororacle.org/) or similar
  - Verify medals/progress bars distinguishable

### Phase 6: Document & Clean Up

- [ ] **Remove old Color.kt** hardcoded color definitions (replace with Material Theme Builder exports)
- [ ] **Update CLAUDE.md** with color usage guidelines
- [ ] **Document extended color usage** in code comments
- [ ] **Create ADR** (Architecture Decision Record) for color system migration

---

## WCAG Accessibility Requirements

### Contrast Ratio Standards

Material 3's tonal palette system is **designed to meet WCAG requirements by default** when semantic roles are paired correctly.

| Element Type | WCAG Level AA | WCAG Level AAA |
|--------------|---------------|----------------|
| Normal text (< 18sp or < 14sp bold) | **4.5:1** | **7:1** |
| Large text (≥ 18sp or ≥ 14sp bold) | **3:1** | **4.5:1** |
| UI components (buttons, borders, icons) | **3:1** | N/A |

**Material 3 Baseline**: The Material color system provides **minimum 3:1** color contrast between paired colors (e.g., `primary`/`onPrimary`). Most pairs exceed **4.5:1** for normal text.

### How Material 3 Achieves Accessibility

**Tonal Palettes**: Material 3 uses **luminance-based** color generation (not hue-based). Each key color generates 13 tones (0-100) based on lightness, ensuring:
- Algorithmic combinations meet accessibility standards
- Dynamic color (Android 12+) is accessible by default
- Removing hue/chroma still shows contrast through shades

**Automatic Pairing**: Material components automatically pair colors:
- `primary` (tone 40 in light mode) → `onPrimary` (tone 100/white)
- `primaryContainer` (tone 90) → `onPrimaryContainer` (tone 10)
- `surface` (tone 98) → `onSurface` (tone 10)

### Testing Workflow for VoxPlanApp

#### 1. Automated Testing (During Development)

```kotlin
// Example instrumented test for contrast
@Test
fun testButtonContrastRatio() {
    val primary = MaterialTheme.colorScheme.primary.toArgb()
    val onPrimary = MaterialTheme.colorScheme.onPrimary.toArgb()
    val ratio = calculateContrastRatio(primary, onPrimary)
    assertTrue("Button contrast ratio $ratio < 4.5:1", ratio >= 4.5)
}
```

#### 2. Manual Testing with Tools

**WebAIM Contrast Checker** (Recommended):
1. Take screenshot of VoxPlan screen
2. Use eyedropper tool to extract foreground/background colors
3. Enter hex values at [WebAIM Contrast Checker](https://webaim.org/resources/contrastchecker/)
4. Verify AA/AAA compliance
5. Use color suggestions if ratio fails

**Android Accessibility Scanner**:
1. Install on test device
2. Navigate to screen in VoxPlan
3. Run scanner
4. Fix flagged issues (scanner shows exact contrast ratios)

**Polypane Color Contrast Checker** (Advanced):
- Handles opacity/transparency automatically (shows real contrast ratio)
- Suggests similar colors with sufficient contrast
- Tests both WCAG 2.0 and APCA standards
- URL: [colorcontrast.app](https://colorcontrast.app/)

#### 3. Extended Color Testing

**CRITICAL**: Extended colors (medals, power bars) must be tested manually:

```
Medal Colors on Dark Background:
- medalBronze (#CD7F32) on surface (#FFFBFE) → Test ratio
- medalSilver (#C0C0C0) on surface → Test ratio
- medalGold (#FFD700) on surface → Test ratio
- medalDiamond (#B9F2FF) on surface → Test ratio

Power Bar:
- powerBarFill (#4CAF50) on powerBarBackground (#E0E0E0) → Test ratio (3:1 min)
```

### Common Pitfalls to Avoid

1. **Mismatched Pairings**:
   - ❌ `onPrimary` text on `primaryContainer` background (wrong pairing)
   - ✅ `onPrimaryContainer` text on `primaryContainer` background

2. **Hardcoded Opacity**:
   - ❌ `Color.Black.copy(alpha = 0.6f)` on `surface` (contrast ratio unknown)
   - ✅ `onSurfaceVariant` on `surface` (guaranteed accessible)

3. **Custom Colors Without Testing**:
   - ❌ Adding `medalBronze` without verifying contrast on all backgrounds
   - ✅ Test each extended color on every background it appears on

4. **Ignoring UI Component Contrast**:
   - ❌ Using `outlineVariant` (#C4C4C4) for critical borders (may not meet 3:1)
   - ✅ Using `outline` for important borders (guaranteed 3:1+)

### Best Practices Summary

- ✅ **Use semantic pairings** (onPrimary on primary, etc.) - guaranteed accessible
- ✅ **Test extended colors** manually with WebAIM or Accessibility Scanner
- ✅ **Prefer outline over outlineVariant** for critical UI elements
- ✅ **Use Material Theme Builder** to generate compliant tonal palettes
- ✅ **Avoid hardcoded opacity** on text colors (use semantic roles instead)
- ✅ **Test with real content** (12sp, 14sp, 16sp, 18sp text sizes)

---

## Code Examples

### Example 1: Migrating a Button

**Before (Hardcoded):**
```kotlin
Button(
    onClick = { /* ... */ },
    colors = ButtonDefaults.buttonColors(
        containerColor = Color(0xFF6200EE), // Hardcoded purple
        contentColor = Color.White
    )
) {
    Text("Start Focus Mode")
}
```

**After (Material 3 Semantic):**
```kotlin
// Filled button (high emphasis) - uses primary by default
Button(
    onClick = { /* ... */ }
    // No colors parameter needed - uses MaterialTheme.colorScheme.primary/onPrimary
) {
    Text("Start Focus Mode")
}

// OR Filled Tonal button (medium emphasis)
FilledTonalButton(
    onClick = { /* ... */ }
    // Uses primaryContainer/onPrimaryContainer by default
) {
    Text("View Daily Tasks")
}
```

### Example 2: Migrating a Card

**Before:**
```kotlin
Card(
    modifier = Modifier.fillMaxWidth(),
    colors = CardDefaults.cardColors(
        containerColor = Color(0xFFF5F5F5) // Hardcoded gray
    )
) {
    Text(
        text = goal.title,
        color = Color(0xFF000000) // Hardcoded black
    )
}
```

**After:**
```kotlin
Card(
    modifier = Modifier.fillMaxWidth()
    // Uses MaterialTheme.colorScheme.surfaceContainer by default
) {
    Text(
        text = goal.title,
        color = MaterialTheme.colorScheme.onSurface // Semantic role
    )
}

// OR for elevated card (high emphasis)
ElevatedCard(
    modifier = Modifier.fillMaxWidth()
    // Uses surfaceContainerHigh with elevation shadow
) {
    Text(
        text = goal.title,
        style = MaterialTheme.typography.titleMedium // Typography handles color
    )
}
```

### Example 3: Using Extended Colors

**Medals in FocusModeScreen:**
```kotlin
@Composable
fun MedalDisplay(medals: List<Medal>) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        medals.forEach { medal ->
            val medalColor = when (medal.type) {
                MedalType.Bronze -> MaterialTheme.extendedColors.medalBronze
                MedalType.Silver -> MaterialTheme.extendedColors.medalSilver
                MedalType.Gold -> MaterialTheme.extendedColors.medalGold
                MedalType.Diamond -> MaterialTheme.extendedColors.medalDiamond
            }

            Icon(
                painter = painterResource(id = R.drawable.ic_medal),
                contentDescription = "${medal.type} medal",
                tint = medalColor,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}
```

**Power Bar with Progress:**
```kotlin
@Composable
fun PowerBar(currentMinutes: Int, fullBarMinutes: Int = 60) {
    val progress = (currentMinutes.toFloat() / fullBarMinutes).coerceIn(0f, 1f)

    LinearProgressIndicator(
        progress = progress,
        modifier = Modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(RoundedCornerShape(4.dp)),
        color = MaterialTheme.extendedColors.powerBarFill,
        trackColor = MaterialTheme.extendedColors.powerBarBackground
    )
}
```

### Example 4: Surface Container Variants for Depth

**Layered containers (dialog with cards):**
```kotlin
@Composable
fun GoalDetailsDialog(goal: TodoItem) {
    AlertDialog(
        onDismissRequest = { /* ... */ },
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh // Dialog layer
    ) {
        Column {
            Text(
                text = goal.title,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Card inside dialog - use higher emphasis
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHighest // Above dialog
                )
            ) {
                Text(
                    text = "Quota: ${goal.quota} mins",
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
```

### Example 5: Complete lightColorScheme Definition

```kotlin
// ui/theme/Color.kt - Generated from Material Theme Builder
val md_theme_light_primary = Color(0xFF6200EE)
val md_theme_light_onPrimary = Color(0xFFFFFFFF)
val md_theme_light_primaryContainer = Color(0xFFBB86FC)
val md_theme_light_onPrimaryContainer = Color(0xFF3700B3)
val md_theme_light_secondary = Color(0xFF03DAC6)
val md_theme_light_onSecondary = Color(0xFF000000)
val md_theme_light_secondaryContainer = Color(0xFF018786)
val md_theme_light_onSecondaryContainer = Color(0xFFFFFFFF)
// ... (Material Theme Builder exports all 35+ color values)

// ui/theme/Theme.kt
private val LightColorScheme = lightColorScheme(
    primary = md_theme_light_primary,
    onPrimary = md_theme_light_onPrimary,
    primaryContainer = md_theme_light_primaryContainer,
    onPrimaryContainer = md_theme_light_onPrimaryContainer,
    inversePrimary = md_theme_light_inversePrimary,
    secondary = md_theme_light_secondary,
    onSecondary = md_theme_light_onSecondary,
    secondaryContainer = md_theme_light_secondaryContainer,
    onSecondaryContainer = md_theme_light_onSecondaryContainer,
    tertiary = md_theme_light_tertiary,
    onTertiary = md_theme_light_onTertiary,
    tertiaryContainer = md_theme_light_tertiaryContainer,
    onTertiaryContainer = md_theme_light_onTertiaryContainer,
    background = md_theme_light_background,
    onBackground = md_theme_light_onBackground,
    surface = md_theme_light_surface,
    onSurface = md_theme_light_onSurface,
    surfaceVariant = md_theme_light_surfaceVariant,
    onSurfaceVariant = md_theme_light_onSurfaceVariant,
    surfaceTint = md_theme_light_surfaceTint,
    inverseSurface = md_theme_light_inverseSurface,
    inverseOnSurface = md_theme_light_inverseOnSurface,
    error = md_theme_light_error,
    onError = md_theme_light_onError,
    errorContainer = md_theme_light_errorContainer,
    onErrorContainer = md_theme_light_onErrorContainer,
    outline = md_theme_light_outline,
    outlineVariant = md_theme_light_outlineVariant,
    scrim = md_theme_light_scrim,
    surfaceBright = md_theme_light_surfaceBright,
    surfaceDim = md_theme_light_surfaceDim,
    surfaceContainer = md_theme_light_surfaceContainer,
    surfaceContainerHigh = md_theme_light_surfaceContainerHigh,
    surfaceContainerHighest = md_theme_light_surfaceContainerHighest,
    surfaceContainerLow = md_theme_light_surfaceContainerLow,
    surfaceContainerLowest = md_theme_light_surfaceContainerLowest,
)
```

---

## Key References

### Official Material Design 3 Documentation

1. **[Color Roles - Material Design 3](https://m3.material.io/styles/color/roles)**
   - Complete reference of all semantic color roles
   - When to use primary vs secondary vs tertiary
   - Color pairing rules (primary/onPrimary, etc.)

2. **[Material Design 3 in Compose | Android Developers](https://developer.android.com/develop/ui/compose/designsystems/material3)**
   - Official Android implementation guide
   - lightColorScheme() and darkColorScheme() setup
   - Dynamic color support (Android 12+)

3. **[Theming in Compose with Material 3 | Google Codelabs](https://developer.android.com/codelabs/jetpack-compose-theming)**
   - Step-by-step theming tutorial
   - Includes Color.kt and Theme.kt examples
   - Covers typography and shapes as well

4. **[ColorScheme API Reference | Kotlin](https://kotlinlang.org/api/compose-multiplatform/material3/androidx.compose.material3/-color-scheme/)**
   - Complete API documentation
   - All 35+ color properties with descriptions
   - Type signatures and nullability

5. **[Accessibility Designing - Color Contrast | Material Design 3](https://m3.material.io/foundations/designing/color-contrast)**
   - WCAG contrast requirements
   - Tonal palette accessibility explanation
   - Testing recommendations

### Extended Colors Implementation

6. **[Custom Colour Scheme with Material3 Compose | Zsolt's Blog](https://herrbert74.github.io/posts/custom-color-scheme-with-m3-compose/)**
   - Complete CompositionLocal pattern
   - Code examples for extended colors
   - Integration with MaterialTheme

7. **[Extending Material 3 with Custom Colors | Medium](https://medium.com/@hidayatasep43/extending-material-3-with-custom-colors-in-jetpack-compose-9393a9db725c)**
   - Step-by-step extended colors guide
   - Best practices for branding colors
   - Multiple color palette support

8. **[Advanced Color Customizations - Material Design 3](https://m3.material.io/styles/color/advanced/define-new-colors)**
   - When to use extended colors
   - Harmonization with dynamic color
   - Material Theme Builder extended colors feature

### Surface Color Variants

9. **[Learn About Tone-based Surfaces in Material 3](https://m3.material.io/blog/tone-based-surface-color-m3)**
   - surfaceContainer vs Surface + TonalElevation
   - Why M3 removed opacity-based elevation
   - When to use surfaceContainerHighest vs surfaceContainerLow

10. **[Jetpack Compose Material 3 Update: SurfaceContainer Variants](https://www.lorenzovainigli.com/blog/jetpack-compose-material3-surfacecontainer-variants/)**
    - Practical guide to surface variants
    - Migration from surfaceVariant
    - Code examples with depth layering

### Tools & Utilities

11. **[Material Theme Builder (Web)](https://material-foundation.github.io/material-theme-builder/)**
    - Generate compliant color schemes from brand colors
    - Export to Compose code (Theme.kt, Color.kt)
    - Extended colors support with harmonization
    - Test light/dark modes interactively

12. **[WebAIM Contrast Checker](https://webaim.org/resources/contrastchecker/)**
    - WCAG 2.0 AA/AAA compliance testing
    - Eyedropper tool for screenshots
    - Color suggestions for failed ratios
    - API access for automated testing

13. **[Polypane Color Contrast Checker](https://colorcontrast.app/)**
    - Handles opacity/transparency automatically
    - WCAG 2.0 and APCA standards
    - Color blindness simulation
    - Similar color suggestions

14. **[Designing with Accessible Colors | Google Codelabs](https://codelabs.developers.google.com/color-contrast-accessibility)**
    - Hands-on accessibility tutorial
    - Testing workflow for Android apps
    - Common pitfalls and fixes

### Migration Best Practices

15. **[Accessible Color Tokens for Enterprise Design Systems](https://www.aufaitux.com/blog/color-tokens-enterprise-design-systems-best-practices/)**
    - Token naming conventions
    - Avoiding hardcoded values
    - WCAG compliance in design systems
    - Real-world enterprise examples (Atlassian)

16. **[Mastering Material3 Colors in Jetpack Compose | ProAndroidDev](https://proandroiddev.com/mastering-material3-colors-in-jetpack-compose-3a6827db97d6)**
    - Common mistakes to avoid
    - Color role decision matrix
    - Testing strategies
    - Performance considerations

---

## VoxPlanApp Mapping Recommendations

### Preliminary Mapping (26 Custom Colors → Material 3)

**High-Priority Mappings (Use Semantic Roles):**

| VoxPlan Color | Suggested Material 3 Role | Rationale |
|---------------|--------------------------|-----------|
| Primary brand color | `primary` | Main CTA buttons, active states |
| Secondary accent | `secondary` | Filter chips, secondary actions |
| Background | `surface` | Card/container backgrounds (M3 prefers surface) |
| Text primary | `onSurface` | Default text color |
| Text secondary | `onSurfaceVariant` | Lower-emphasis labels |
| Error red | `error` | Error states, destructive actions |
| Divider gray | `outline` or `outlineVariant` | Borders (use outline if 3:1 contrast needed) |

**Extended Colors (Custom VoxPlan UI):**

| VoxPlan Color | Extended Color Name | Use Case |
|---------------|---------------------|----------|
| Bronze medal | `medalBronze` | 30-min focus medal |
| Silver medal | `medalSilver` | 60-min focus medal |
| Gold medal | `medalGold` | 90-min focus medal |
| Diamond medal | `medalDiamond` | 120+ min focus medal |
| Power bar fill | `powerBarFill` | Timer progress indicator |
| Power bar background | `powerBarBackground` | Timer progress track |
| Goal color 1 | `goalColor1` | Goal category 1 |
| Goal color 2 | `goalColor2` | Goal category 2 |
| ... | ... | ... (up to 26 total) |

**Next Steps:**
1. Audit all 26 colors in `ui/theme/Color.kt`
2. Use Material Theme Builder to generate base `lightColorScheme`
3. Map remaining custom colors to extended colors
4. Test contrast ratios for all extended colors
5. Migrate screens incrementally

---

## Common Questions & Answers

### Q: What's the difference between primary, primaryContainer, and onPrimary?

**A:**
- **`primary`**: High-emphasis brand color (filled buttons, FABs, active states)
- **`primaryContainer`**: Medium-emphasis tonal variant (filled tonal buttons, selected cards)
- **`onPrimary`**: Text/icon color on `primary` background (always pair with `primary`)

**Use Case Example:**
- "Start Focus Mode" button (primary CTA) → `primary` background + `onPrimary` text
- "View Progress" button (secondary CTA) → `primaryContainer` background + `onPrimaryContainer` text

### Q: When should I use surface vs background vs surfaceVariant?

**A:**
- **`surface`**: Base color for cards, sheets, menus, dialogs (majority of UI)
- **`background`**: Page canvas for scrollable content (M3 prefers `surface` instead)
- **`surfaceVariant`**: **Deprecated in M3** - migrate to `surfaceContainerHighest`

**M3 Recommendation**: Use `surfaceContainer` variants (Lowest → Low → Container → High → Highest) for layered depth. Prefer `surface*` roles over `background`.

### Q: How do I handle colors that don't fit semantic roles (medals, power bars)?

**A:** Use the extended colors pattern:
1. Define `ExtendedColors` data class with custom properties
2. Create `LocalExtendedColors` CompositionLocal
3. Provide in `VoxPlanTheme` composable
4. Access via `MaterialTheme.extendedColors.medalBronze`
5. **CRITICAL**: Test contrast ratios manually for each extended color on every background it appears on

### Q: What's the recommended way to test WCAG contrast compliance?

**A:**
1. **During Development**: Use semantic role pairings (guaranteed accessible by default)
2. **For Extended Colors**:
   - Take screenshot → Eyedropper tool → Extract hex values
   - Enter at [WebAIM Contrast Checker](https://webaim.org/resources/contrastchecker/)
   - Verify 4.5:1 for normal text, 3:1 for large text/UI components
3. **Android Accessibility Scanner**: Install on test device, run on each screen
4. **Automated Tests**: Write instrumented tests using `calculateContrastRatio()`

### Q: Can I use dynamic color (Material You) with custom branding?

**A:** Yes, with caveats:
- Dynamic color (Android 12+) generates palette from user wallpaper
- Overrides your brand colors completely
- For VoxPlan: **Disable dynamic color** (`dynamicColor = false` in VoxPlanTheme) to maintain brand identity
- Alternative: Use extended colors for branding alongside dynamic base colors

### Q: Do I need to define all 35 color roles in lightColorScheme()?

**A:** No - Material Theme Builder generates all 35 values automatically from a few source colors (primary, secondary, tertiary). Just export the code and use as-is. Manual tuning optional but usually unnecessary.

---

**End of Material 3 Color System Guide**

*Last Updated: 2025-12-26*
*For VoxPlanApp Migration - Light Mode Only (Dark Mode Future Enhancement)*
