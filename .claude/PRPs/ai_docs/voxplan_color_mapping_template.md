# VoxPlanApp Color Mapping Template

**Purpose**: Quick reference for mapping VoxPlan's 26 custom colors to Material 3 semantic roles and extended colors.

**Status**: Template - Requires audit of actual colors from `ui/theme/Color.kt`

---

## Step 1: Audit Current Colors

**Action**: List all colors currently defined in `app/src/main/java/com/voxplanapp/ui/theme/Color.kt`

| Current Color Name | Hex Value | Current Usage | Occurrences |
|-------------------|-----------|---------------|-------------|
| ??? | #?????? | ??? | ??? |
| ??? | #?????? | ??? | ??? |
| ... | ... | ... | ... |

**Grep Command to Find Usage**:
```bash
# Find all Color references in Composables
grep -r "Color(0x" app/src/main/java/com/voxplanapp/ui/
```

---

## Step 2: Map to Material 3 Roles

### Semantic Roles (Use These When Possible)

| VoxPlan Color | Material 3 Role | Rationale | Migration Priority |
|---------------|-----------------|-----------|-------------------|
| ??? | `primary` | Main brand color for CTAs | **HIGH** |
| ??? | `primaryContainer` | Selected states, tonal buttons | **HIGH** |
| ??? | `secondary` | Secondary actions, filters | **MEDIUM** |
| ??? | `tertiary` | Accents, highlights | **MEDIUM** |
| ??? | `surface` | Card/container backgrounds | **HIGH** |
| ??? | `surfaceContainer` | Standard containers | **HIGH** |
| ??? | `surfaceContainerHigh` | Elevated containers | **MEDIUM** |
| ??? | `onSurface` | Primary text color | **HIGH** |
| ??? | `onSurfaceVariant` | Secondary text, labels | **HIGH** |
| ??? | `error` | Error states, destructive actions | **MEDIUM** |
| ??? | `outline` | Borders, dividers (3:1 contrast) | **LOW** |
| ??? | `outlineVariant` | Subtle dividers | **LOW** |

### Extended Colors (Custom VoxPlan UI)

**Define in `ExtendedColors` data class:**

| VoxPlan Color | Extended Property Name | Use Case | Needs Contrast Test? |
|---------------|------------------------|----------|---------------------|
| Bronze medal color | `medalBronze` | 30-min focus medal | ✅ YES |
| Silver medal color | `medalSilver` | 60-min focus medal | ✅ YES |
| Gold medal color | `medalGold` | 90-min focus medal | ✅ YES |
| Diamond medal color | `medalDiamond` | 120+ min focus medal | ✅ YES |
| Power bar fill | `powerBarFill` | Timer progress indicator | ✅ YES |
| Power bar background | `powerBarBackground` | Timer progress track | ✅ YES |
| ??? | `goalColor1` | Goal category 1 | ✅ YES |
| ??? | `goalColor2` | Goal category 2 | ✅ YES |
| ??? | `eventScheduledColor` | Scheduled event background | ✅ YES |
| ??? | `dailyTaskColor` | Daily task highlight | ✅ YES |
| ... | ... | ... | ... |

---

## Step 3: Generate Material 3 Theme

### Using Material Theme Builder

1. **Open Tool**: [Material Theme Builder](https://material-foundation.github.io/material-theme-builder/)
2. **Input Primary Color**: Enter VoxPlan's main brand color (from audit above)
3. **Customize** (optional):
   - Adjust secondary/tertiary colors
   - Add extended colors via "Add a color" button
4. **Export**:
   - Click "Export" → Select "Android Compose (Jetpack)"
   - Download `Theme.kt` and `Color.kt` files
5. **Replace** existing `ui/theme/Theme.kt` and `ui/theme/Color.kt`

### Exported Files Structure

```
app/src/main/java/com/voxplanapp/ui/theme/
├── Color.kt        # All 35+ color values (md_theme_light_primary, etc.)
├── Theme.kt        # lightColorScheme() definition + VoxPlanTheme composable
└── ExtendedColors.kt  # NEW - Custom VoxPlan colors (medals, power bars, etc.)
```

---

## Step 4: Test Contrast Ratios

### Required Tests (Extended Colors Only)

**Note**: Semantic role pairings (primary/onPrimary, etc.) are guaranteed accessible by Material 3.

| Extended Color | Background | Ratio | Pass AA? | Pass AAA? | Action |
|----------------|------------|-------|----------|-----------|--------|
| `medalBronze` | `surface` | ??? | ??? | ??? | Test with [WebAIM](https://webaim.org/resources/contrastchecker/) |
| `medalSilver` | `surface` | ??? | ??? | ??? | ??? |
| `medalGold` | `surface` | ??? | ??? | ??? | ??? |
| `medalDiamond` | `surface` | ??? | ??? | ??? | ??? |
| `powerBarFill` | `powerBarBackground` | ??? | ??? (3:1 min) | ??? | ??? |
| `goalColor1` | `surface` | ??? | ??? | ??? | ??? |
| ... | ... | ... | ... | ... | ... |

**Testing Workflow**:
1. Take screenshot of UI with extended color
2. Use eyedropper tool to extract foreground/background hex values
3. Enter at [WebAIM Contrast Checker](https://webaim.org/resources/contrastchecker/)
4. Record ratio and pass/fail status
5. If fails, use suggested colors or adjust manually

---

## Step 5: Migration Checklist

### Phase 1: Replace Theme Files
- [ ] Generate theme with Material Theme Builder (using VoxPlan primary color)
- [ ] Replace `ui/theme/Theme.kt` with exported version
- [ ] Replace `ui/theme/Color.kt` with exported version
- [ ] Create `ui/theme/ExtendedColors.kt` (see template in ai_docs/material3_color_system.md)
- [ ] Build project - verify no compilation errors

### Phase 2: Migrate Screens (Incremental)
- [ ] **MainScreen.kt** - Goal list backgrounds, text colors
- [ ] **FocusModeScreen.kt** - Medal colors (extended), power bar (extended), timer UI
- [ ] **DailyScreen.kt** - Card backgrounds, dividers, text colors
- [ ] **DaySchedule.kt** - Event colors (extended?), grid lines, backgrounds
- [ ] **ProgressScreen.kt** - Chart colors (extended?), progress bars, text
- [ ] **GoalEditScreen.kt** - Form backgrounds, buttons, text fields
- [ ] **Common components** - Buttons, cards, dialogs, bottom sheets

### Phase 3: Verify Accessibility
- [ ] Run Android Accessibility Scanner on each screen
- [ ] Fix flagged contrast issues
- [ ] Test all extended colors with WebAIM Contrast Checker
- [ ] Verify semantic pairings (onPrimary on primary, etc.)

### Phase 4: Clean Up
- [ ] Remove old hardcoded color values from `Color.kt`
- [ ] Update code comments with semantic role rationale
- [ ] Document extended color usage in `ExtendedColors.kt`
- [ ] Run full UI test suite
- [ ] Manual smoke test on device

---

## Decision Matrix: Semantic vs Extended

**Use this decision tree for each color:**

```
Does this color fit a semantic meaning?
├─ YES → Use Material 3 role
│   ├─ Primary brand color? → primary
│   ├─ Secondary action? → secondary
│   ├─ Accent/highlight? → tertiary
│   ├─ Container background? → surfaceContainer (or variants)
│   ├─ Text on container? → onSurface / onSurfaceVariant
│   ├─ Error state? → error
│   └─ Border/divider? → outline / outlineVariant
│
└─ NO → Use Extended Color
    ├─ Medal colors → ExtendedColors.medalBronze/Silver/Gold/Diamond
    ├─ Power bar → ExtendedColors.powerBarFill/Background
    ├─ Goal categories → ExtendedColors.goalColor1/2/3...
    └─ Custom UI → Define new extended color property
```

---

## Code Templates

### ExtendedColors Definition

```kotlin
// ui/theme/ExtendedColors.kt
package com.voxplanapp.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

@Immutable
data class ExtendedColors(
    // Medal colors (Focus Mode)
    val medalBronze: Color = Color.Unspecified,
    val medalSilver: Color = Color.Unspecified,
    val medalGold: Color = Color.Unspecified,
    val medalDiamond: Color = Color.Unspecified,

    // Power bar (Focus Mode)
    val powerBarFill: Color = Color.Unspecified,
    val powerBarBackground: Color = Color.Unspecified,

    // Goal categories (customize count as needed)
    val goalColor1: Color = Color.Unspecified,
    val goalColor2: Color = Color.Unspecified,
    val goalColor3: Color = Color.Unspecified,

    // Event/schedule colors (if needed beyond semantic roles)
    val eventScheduled: Color = Color.Unspecified,
    val dailyTaskHighlight: Color = Color.Unspecified,

    // Add other VoxPlan-specific colors here...
)

// Light mode palette (populate after audit)
val lightExtendedColors = ExtendedColors(
    medalBronze = Color(0xFFCD7F32),      // Example - replace with actual
    medalSilver = Color(0xFFC0C0C0),      // Example
    medalGold = Color(0xFFFFD700),        // Example
    medalDiamond = Color(0xFFB9F2FF),     // Example
    powerBarFill = Color(0xFF4CAF50),     // Example
    powerBarBackground = Color(0xFFE0E0E0), // Example
    goalColor1 = Color(0xFF6200EE),       // Example
    goalColor2 = Color(0xFF03DAC6),       // Example
    // ... fill in rest after audit
)

// Dark mode palette (future enhancement)
val darkExtendedColors = ExtendedColors(
    // TBD - implement when adding dark mode support
)
```

### Theme.kt Integration

```kotlin
// ui/theme/Theme.kt
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf

// CompositionLocal for extended colors
val LocalExtendedColors = staticCompositionLocalOf { ExtendedColors() }

// Extension property for easy access
val MaterialTheme.extendedColors: ExtendedColors
    @Composable
    @ReadOnlyComposable
    get() = LocalExtendedColors.current

@Composable
fun VoxPlanTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Disable for VoxPlan branding
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        // Dynamic color disabled for brand consistency
        // Future: Support with fallback to extended colors
        darkTheme -> DarkColorScheme // Future implementation
        else -> LightColorScheme // From Material Theme Builder export
    }

    val extendedColors = if (darkTheme) {
        darkExtendedColors // Future implementation
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

### Usage in Composables

```kotlin
// Example: FocusModeScreen.kt
@Composable
fun MedalBadge(medalType: MedalType) {
    val medalColor = when (medalType) {
        MedalType.Bronze -> MaterialTheme.extendedColors.medalBronze
        MedalType.Silver -> MaterialTheme.extendedColors.medalSilver
        MedalType.Gold -> MaterialTheme.extendedColors.medalGold
        MedalType.Diamond -> MaterialTheme.extendedColors.medalDiamond
    }

    Icon(
        painter = painterResource(R.drawable.ic_medal),
        contentDescription = "${medalType.name} medal",
        tint = medalColor,
        modifier = Modifier.size(32.dp)
    )
}

// Example: Button migration
@Composable
fun StartFocusButton(onClick: () -> Unit) {
    // Before: Button(colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6200EE)))
    // After: Just use default - Material3 applies primary/onPrimary automatically
    Button(onClick = onClick) {
        Text("Start Focus Mode")
    }
}

// Example: Card with semantic colors
@Composable
fun GoalCard(goal: TodoItem) {
    Card(
        // Uses surfaceContainer by default
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Text(
            text = goal.title,
            style = MaterialTheme.typography.titleMedium // Typography handles color
        )
    }
}
```

---

## Next Steps

1. **Audit Phase** (30 mins):
   - List all colors in `ui/theme/Color.kt`
   - Grep for `Color(0x` in UI files
   - Categorize: semantic vs extended

2. **Generate Phase** (15 mins):
   - Use Material Theme Builder with VoxPlan primary color
   - Download and replace Theme.kt / Color.kt
   - Create ExtendedColors.kt

3. **Test Phase** (45 mins):
   - Test all extended colors with WebAIM
   - Record contrast ratios
   - Adjust colors if needed

4. **Migrate Phase** (2-4 hours):
   - Migrate one screen at a time
   - Test each screen after migration
   - Run Accessibility Scanner

5. **Verify Phase** (30 mins):
   - Full app smoke test
   - Check all screens for visual regressions
   - Verify accessibility compliance

**Total Estimated Time**: 4-6 hours

---

**References**:
- Comprehensive guide: `.claude/PRPs/ai_docs/material3_color_system.md`
- Material Theme Builder: https://material-foundation.github.io/material-theme-builder/
- WebAIM Contrast Checker: https://webaim.org/resources/contrastchecker/
