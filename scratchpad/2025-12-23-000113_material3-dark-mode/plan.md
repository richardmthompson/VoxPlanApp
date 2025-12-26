# Feature: Material 3 Dark Mode Implementation

## Background and Motivation

VoxPlanApp currently has a Material 3 theme infrastructure (Theme.kt with lightColorScheme and darkColorScheme) but **it is completely unused**. All screens bypass the Material theming system by:
1. Importing hardcoded colors from `ui.constants.Colors.kt` (26 color constants)
2. Using inline `Color(0xFFxxxxxx)` values directly in composables (42+ occurrences)
3. MainActivity does NOT wrap VoxPlanApp() in the theme

This creates multiple problems:
- **No dark mode** support despite user demand
- **Accessibility issues** - cannot guarantee WCAG contrast ratios
- **Maintenance burden** - same colors redefined multiple times across files
- **Violations of Material Design principles** - ignores semantic color roles

**Business Value**: Dark mode is expected in modern Android apps. Material 3 adoption future-proofs the app and enables dynamic theming (wallpaper-based colors on Android 12+).

---

## Feature Goal

Implement Material 3 compliant light and dark themes across MainScreen and DailyScreen while preserving the power bar branding component, enabling users to seamlessly switch between themes with guaranteed accessibility compliance.

---

## Pattern Analysis

### Patterns Found

**Pattern A**: MainScreen.kt, GoalItem.kt, DailyScreen.kt - **Direct color imports**
- **File**: `/Users/richardthompson/StudioProjects/VoxPlanApp/app/src/main/java/com/voxplanapp/ui/main/MainScreen.kt:1-10` (imports), used throughout
- **Approach**: Import color constants from `ui.constants.Colors.kt` and use directly (`PrimaryColor`, `TopLevelGoalBorderColor`, etc.). Also heavy use of inline `Color(0xFFxxxxxx)` for power bars, medals, and UI highlights.
- **Pros**:
  - Complete control over exact colors
  - No ambiguity - clear which color is used where
  - Easy to find all colors in one central file
- **Cons**:
  - **Completely ignores Material 3 theming** - impossible to support dark mode
  - Violates Material Design accessibility and semantic color role principles
  - Hardcoded colors scattered across 11+ files
  - No way to dynamically theme based on system preferences

**Pattern B**: ProgressScreen.kt:94-95, 118, 247-249, 339, 360, 394, 403 - **Hybrid MaterialTheme + inline**
- **File**: `/Users/richardthompson/StudioProjects/VoxPlanApp/app/src/main/java/com/voxplanapp/ui/goals/ProgressScreen.kt:94-403`
- **Approach**: Mix MaterialTheme.colorScheme (error, tertiary, outline, primary, onSurfaceVariant) with inline hex colors for specific UI elements like progress indicators and cards.
- **Pros**:
  - Shows awareness of Material theming system
  - Uses semantic color roles for some elements (error states, borders)
  - Partially responsive to theme changes
- **Cons**:
  - **Inconsistent** - some elements themed, others hardcoded
  - Mixed approach creates maintenance complexity
  - Still no dark mode for custom inline colors
  - Incomplete Material 3 adoption

**Pattern C**: FocusModeScreen.kt:163-788 - **Pure inline hex colors**
- **File**: `/Users/richardthompson/StudioProjects/VoxPlanApp/app/src/main/java/com/voxplanapp/ui/focusmode/FocusModeScreen.kt:163-788`
- **Approach**: Extensive use of `Color(0xFFxxxxxx)` directly in composables for dark backgrounds, medal colors (gold, green), timer arcs, and control buttons. Zero MaterialTheme.colorScheme usage.
- **Pros**:
  - Maximum flexibility for complex visual elements
  - Precise color control needed for gamification (medals, progress arcs)
  - Already dark-themed (green/black aesthetic)
- **Cons**:
  - **Impossible to theme** without rewriting hundreds of inline color references
  - No reusability - same colors redefined multiple times
  - Would require massive refactor for dark mode support

### Recommended Pattern

**FOLLOW**: Material 3 Official Pattern (from Phase 2 research)

**RATIONALE**:
The codebase needs to migrate from Pattern A/C to proper Material 3 theming. None of the existing patterns are suitable for dark mode. The official Material 3 approach from Android documentation provides:
1. **Semantic color roles** - primary, onPrimary, surface, etc. with guaranteed accessibility
2. **Built-in dark mode** - lightColorScheme and darkColorScheme with proper contrast
3. **System integration** - `isSystemInDarkTheme()` automatic detection
4. **Extended colors** - CompositionLocal pattern for app-specific colors (power bar, medals)
5. **Dynamic theming** - Optional Android 12+ wallpaper-based colors

This is not Pattern B (hybrid) but a **complete migration** to Material 3 with extended colors for branding.

### External Documentation

**Official Material 3 Resources:**
- **Color system**: [Material Design 3 in Compose](https://developer.android.com/develop/ui/compose/designsystems/material3)
- **Dark theme**: [Theming in Compose with Material 3 Codelab](https://codelabs.developers.google.com/jetpack-compose-theming)
- **Custom colors**: [Extending Material 3 with Custom Colors](https://medium.com/@hidayatasep43/extending-material-3-with-custom-colors-in-jetpack-compose-9393a9db725c)
- **BOM compatibility**: [BOM to Library Version Mapping](https://developer.android.com/develop/ui/compose/bom/bom-mapping)

**Gotchas** (from Phase 2 research):
- **Contrast pairing violations** - Always use `onPrimary` on `primary`, `onPrimaryContainer` on `primaryContainer`. Mixing color roles breaks WCAG accessibility.
- **Hardcoded opacity with dynamic colors** - Semi-transparent overlays designed for static themes may disappear with certain dynamic color schemes.
- **Background deprecation** - `MaterialTheme.colorScheme.background` is deprecated in Material 3; use surface roles instead.
- **Dynamic color override** - Current Theme.kt has `dynamicColor = true` which uses wallpaper colors on Android 12+, potentially overriding VoxPlan branding. Set to `false` or apply selectively.

---

## Integration Impact

### Files to Modify (Dependency Order)

**20 files total**

#### **Phase 1: Theme Foundation** (3 files - no dependencies)
1. `/app/src/main/java/com/voxplanapp/ui/theme/Color.kt` (11 lines → ~50 lines)
2. `/app/src/main/java/com/voxplanapp/ui/theme/Theme.kt` (70 lines → ~150 lines)
3. `/app/src/main/java/com/voxplanapp/MainActivity.kt` (17 lines → 20 lines)

#### **Phase 2: Constants Migration** (2 files - depends on Phase 1)
4. Create extended color scheme in Theme.kt for non-Material colors
5. `/app/src/main/java/com/voxplanapp/ui/constants/Colors.kt` - Deprecate (52 lines)
6. `/app/src/main/java/com/voxplanapp/ui/constants/TextStyles.kt` - Update to use MaterialTheme (29 lines)

#### **Phase 3: Navigation Layer** (1 file - depends on Phases 1-2)
7. `/app/src/main/java/com/voxplanapp/navigation/VoxPlanApp.kt` (310 lines)

#### **Phase 4: Main Screens** (9 files - depends on Phases 1-3, ordered by complexity)
8. `/app/src/main/java/com/voxplanapp/ui/main/TodoInputBar.kt` (98 lines)
9. `/app/src/main/java/com/voxplanapp/ui/main/BreadCrumbNavigation.kt` (192 lines)
10. `/app/src/main/java/com/voxplanapp/ui/main/GoalListContainer.kt` (84 lines)
11. `/app/src/main/java/com/voxplanapp/ui/main/GoalItem.kt` (441 lines)
12. `/app/src/main/java/com/voxplanapp/ui/main/MainScreen.kt` (306 lines) - **POWER BAR CRITICAL**
13. `/app/src/main/java/com/voxplanapp/ui/daily/DailyScreen.kt` (627 lines)
14. `/app/src/main/java/com/voxplanapp/ui/goals/QuotaSettings.kt` (207 lines)
15. `/app/src/main/java/com/voxplanapp/ui/goals/GoalEditScreen.kt` (539 lines)
16. `/app/src/main/java/com/voxplanapp/ui/calendar/DaySchedule.kt` (677 lines)
17. `/app/src/main/java/com/voxplanapp/ui/goals/ProgressScreen.kt` (441 lines)

#### **Phase 5: Complex Components** (2 files - depends on Phase 4, OPTIONAL for MVP)
18. `/app/src/main/java/com/voxplanapp/ui/focusmode/FocusViewModel.kt` (492 lines) - Medal colors
19. `/app/src/main/java/com/voxplanapp/ui/focusmode/FocusModeScreen.kt` (1005 lines) - **SKIP** (already dark, user requirement)

### Shared Dependencies

**Safe (no changes needed):**
- `/app/src/main/java/com/voxplanapp/ui/constants/DpValues.kt` - Dimensions only
- `/app/src/main/java/com/voxplanapp/ui/constants/Dimens.kt` - Dimensions only
- `/app/src/main/java/com/voxplanapp/ui/theme/Type.kt` - Typography (verify no color refs)

**Must update:**
- `/app/src/main/java/com/voxplanapp/ui/constants/Colors.kt` - Map to theme OR deprecate
- `/app/src/main/java/com/voxplanapp/ui/constants/TextStyles.kt` - Convert to MaterialTheme.typography

### Test Coverage Plan

**Current State**: Only example tests exist - NO UI tests

**New Tests Required:**

1. **ThemeTest.kt** (Priority: HIGH)
   ```kotlin
   - Verify light color scheme has correct Material 3 values
   - Verify dark color scheme has correct Material 3 values
   - Verify extended color scheme exists for power bar/medals
   - Test theme switches properly based on isSystemInDarkTheme()
   - Test dynamicColor disabled (branding preservation)
   ```

2. **PowerBarPreservationTest.kt** (Priority: CRITICAL)
   ```kotlin
   - PowerBarDisplay colors match brand spec in light mode
   - PowerBarDisplay colors match brand spec in dark mode
   - OneBar fill/border colors unchanged
   - Diamond component colors preserved
   - Coin display colors preserved
   ```

3. **ColorAccessibilityTest.kt** (Priority: MEDIUM)
   ```kotlin
   - All text meets WCAG AA contrast (4.5:1)
   - Important UI elements meet AAA (7:1)
   - Test both light and dark schemes
   - Power bar black background with colored elements passes
   ```

4. **VisualRegressionTest.kt** (Priority: MEDIUM)
   ```kotlin
   - Screenshot tests for MainScreen (light + dark)
   - Screenshot tests for DailyScreen (light + dark)
   - Compare against baseline images
   ```

### Risks

**HIGH RISK - Breaking Changes:**
- 11 files import from `ui.constants.Colors`
- 42+ inline color references across screens
- Current theme completely unused (MainActivity doesn't wrap)
- **Mitigation**: Migrate one screen at a time, keep Colors.kt temporarily with @Deprecated

**MEDIUM RISK - Power Bar Preservation:**
- 10 hardcoded colors in power bar (must NOT change)
- Risk of accidentally modifying during migration
- **Mitigation**: Create `PowerBarColors` sealed class with fixed values, explicit tests

**MEDIUM RISK - Dynamic Color Override:**
- Theme.kt has `dynamicColor = true` - uses wallpaper on Android 12+
- Could override VoxPlan branding colors
- **Mitigation**: Set `dynamicColor = false` or apply only to non-branded elements

**LOW RISK - Circular Dependencies:**
- Architecture is hierarchical (theme → constants → screens)
- No circular imports detected
- **Mitigation**: None needed

**MEDIUM RISK - Accessibility:**
- Current inline colors may not meet WCAG contrast
- Dark mode could introduce new violations
- **Mitigation**: Use Material 3's accessible color roles, run contrast checker

---

## Implementation Steps (Dependency-Ordered)

### **PHASE 1: Theme Foundation**

#### **Step 1.1: MODIFY `/app/src/main/java/com/voxplanapp/ui/theme/Color.kt`**
- **Action**: Replace Material 3 purple placeholder colors with VoxPlan brand palette
- **Current**: Purple80, Purple40, Pink80, etc.
- **New**: Create seed color from main branding (e.g., `Color(0xFF1BA821)` green) and generate palette using Material Theme Builder
- **Follow**: Material 3 color generation guidelines - 5 key colors (primary, secondary, tertiary, error, neutral) with tonal variations
- **Preserve**: N/A (this file is just color definitions)
- **Dependency**: None
- **Validation**: Color values compile successfully

**Example structure:**
```kotlin
// Light theme colors
val md_theme_light_primary = Color(0xFF1BA821) // VoxPlan green
val md_theme_light_onPrimary = Color(0xFFFFFFFF)
val md_theme_light_primaryContainer = Color(0xFFA8F5A8)
// ... continue for all Material 3 roles

// Dark theme colors
val md_theme_dark_primary = Color(0xFF8CDB8C)
val md_theme_dark_onPrimary = Color(0xFF003A05)
// ... continue for all Material 3 roles
```

#### **Step 1.2: MODIFY `/app/src/main/java/com/voxplanapp/ui/theme/Theme.kt:18-70`**
- **Action**:
  1. Update lightColorScheme to use colors from Step 1.1
  2. Update darkColorScheme to use dark colors from Step 1.1
  3. Create extended color scheme using CompositionLocal for power bar colors
  4. Set `dynamicColor = false` to preserve branding
- **Follow**: Material 3 extended colors pattern - `staticCompositionLocalOf` for custom colors
- **Preserve**: `isSystemInDarkTheme()` logic, existing structure
- **Dependency**: REQUIRES Step 1.1 completion
- **Validation**: Theme compiles, extended colors accessible via `LocalExtendedColors.current`

**Extended colors structure:**
```kotlin
@Immutable
data class ExtendedColors(
    val powerBarFillFull: Color,
    val powerBarFillPartial: Color,
    val powerBarBorderFull: Color,
    val powerBarBorderPartial: Color,
    val diamondBackground: Color,
    val diamondBorder: Color,
    val coinBackground: Color,
    val coinBorder: Color,
    val powerLabel: Color,
    // Medal colors
    val medalEasy: Color,
    val medalChallenge: Color,
    val medalDiscipline: Color,
    val medalEpicWin: Color
)

// Brand colors - NEVER change for dark mode
val lightExtendedColors = ExtendedColors(
    powerBarFillFull = Color(0xFF13D31B),
    powerBarFillPartial = Color(0xFFFF0000),
    powerBarBorderFull = Color(0xFF1BA821),
    powerBarBorderPartial = Color(0xFF3F51B5),
    diamondBackground = Color(0xFF9C27B0),
    diamondBorder = Color(0xFFBA68C8),
    coinBackground = Color(0xFFFFD700),
    coinBorder = Color(0xFFFF9800),
    powerLabel = Color(0xFFFF5722),
    // Medals
    medalEasy = Color(0xFFFFEB3B),
    medalChallenge = Color(0xFFFF9800),
    medalDiscipline = Color(0xFFF57C00),
    medalEpicWin = Color(0xFFE65100)
)

val darkExtendedColors = lightExtendedColors // SAME - no dark mode for branding

val LocalExtendedColors = staticCompositionLocalOf { lightExtendedColors }

@Composable
fun VoxPlanAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Changed from true
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val extendedColors = if (darkTheme) darkExtendedColors else lightExtendedColors

    CompositionLocalProvider(LocalExtendedColors provides extendedColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
```

#### **Step 1.3: MODIFY `/app/src/main/java/com/voxplanapp/MainActivity.kt:15-17`**
- **Action**: Wrap `VoxPlanApp()` call with `VoxPlanAppTheme { }`
- **Current**: `VoxPlanApp()` called directly (line 16)
- **New**: `VoxPlanAppTheme { VoxPlanApp() }`
- **Follow**: Standard Compose theming pattern
- **Preserve**: All other MainActivity code unchanged
- **Dependency**: REQUIRES Steps 1.1 and 1.2 completion
- **Validation**: App runs without crashes, theme is active (verify with Layout Inspector)

---

### **PHASE 2: Constants Migration**

#### **Step 2.1: CREATE Color mapping in Theme.kt**
- **Action**: Document mapping from Colors.kt constants to MaterialTheme.colorScheme roles
- **Create**: Comment block in Theme.kt explaining:
  - `PrimaryColor` → `MaterialTheme.colorScheme.primary`
  - `ToolbarColor` → `MaterialTheme.colorScheme.surface`
  - `TodoItemTextColor` → `MaterialTheme.colorScheme.onSurface`
  - Power bar colors → `LocalExtendedColors.current.powerBarXxx`
  - Etc.
- **Dependency**: REQUIRES Phase 1 completion
- **Validation**: Mapping document is complete and accurate

#### **Step 2.2: DEPRECATE `/app/src/main/java/com/voxplanapp/ui/constants/Colors.kt`**
- **Action**: Add `@Deprecated` annotations to all color constants with migration message
- **Preserve**: File remains in codebase temporarily (don't delete)
- **Dependency**: REQUIRES Step 2.1
- **Validation**: IDE shows deprecation warnings

**Example:**
```kotlin
@Deprecated(
    "Use MaterialTheme.colorScheme.primary instead",
    ReplaceWith("MaterialTheme.colorScheme.primary", "androidx.compose.material3.MaterialTheme")
)
val PrimaryColor = Color(0xFF1BA821)
```

#### **Step 2.3: UPDATE `/app/src/main/java/com/voxplanapp/ui/constants/TextStyles.kt`**
- **Action**: Replace `TodoItemTextColor` import with `MaterialTheme.colorScheme.onSurface`
- **Current**: Imports color from Colors.kt
- **New**: Use theme directly in text style definition
- **Dependency**: REQUIRES Steps 2.1-2.2
- **Validation**: File compiles, text styles use theme colors

---

### **PHASE 3: Navigation Layer**

#### **Step 3.1: MODIFY `/app/src/main/java/com/voxplanapp/navigation/VoxPlanApp.kt`**
- **Action**: Replace color constant imports with MaterialTheme.colorScheme
- **Current**: Uses `ActivatedColor`, `PrimaryColor`, `TertiaryBorderColor`, `TopAppBarBgColor`
- **New**:
  - `ActivatedColor` → `MaterialTheme.colorScheme.primary`
  - `TopAppBarBgColor` → `MaterialTheme.colorScheme.surface`
  - `TertiaryBorderColor` → `MaterialTheme.colorScheme.outline`
- **Follow**: Material 3 semantic color roles
- **Preserve**: All navigation logic unchanged
- **Dependency**: REQUIRES Phase 2 completion
- **Validation**: Navigation works identically, colors come from theme

---

### **PHASE 4: Main Screens (Priority for MVP: MainScreen + DailyScreen only)**

#### **Step 4.1: MODIFY `/app/src/main/java/com/voxplanapp/ui/main/TodoInputBar.kt`**
- **Action**: Replace `TodoInputBarBackgroundColor` and `TodoInputBarFabColor` with theme
- **Mapping**:
  - `TodoInputBarBackgroundColor` → `MaterialTheme.colorScheme.surfaceVariant`
  - `TodoInputBarFabColor` → `MaterialTheme.colorScheme.primaryContainer`
- **Dependency**: REQUIRES Phase 3
- **Validation**: Input bar visible and functional in both themes

#### **Step 4.2: MODIFY `/app/src/main/java/com/voxplanapp/ui/main/BreadCrumbNavigation.kt`**
- **Action**: Replace `ToolbarColor` and `ToolbarBorderColor`
- **Mapping**:
  - `ToolbarColor` → `MaterialTheme.colorScheme.surface`
  - `ToolbarBorderColor` → `MaterialTheme.colorScheme.outline`
- **Dependency**: REQUIRES Step 4.1
- **Validation**: Breadcrumbs display correctly

#### **Step 4.3: MODIFY `/app/src/main/java/com/voxplanapp/ui/main/GoalItem.kt:265`**
- **Action**: Replace inline `Color(0xFFFFF8DC)` cornsilk background with theme
- **Mapping**: `Color(0xFFFFF8DC)` → `MaterialTheme.colorScheme.surfaceVariant` or `tertiaryContainer`
- **Replace all color constant imports** with theme equivalents
- **Dependency**: REQUIRES Step 4.2
- **Validation**: Goal items render correctly, text readable on both themes

#### **Step 4.4: MODIFY `/app/src/main/java/com/voxplanapp/ui/main/MainScreen.kt` - POWER BAR CRITICAL**
- **Action**:
  1. Replace general UI colors with MaterialTheme.colorScheme
  2. **Power bar components (lines 180-306)**: Use `LocalExtendedColors.current`
  3. Keep power bar colors EXACTLY as defined in extended color scheme
- **Mapping**:
  - Background colors → `MaterialTheme.colorScheme.background` / `surface`
  - Text colors → `MaterialTheme.colorScheme.onBackground` / `onSurface`
  - **Power bar**: Use `LocalExtendedColors.current.powerBarXxx` (NO CHANGES to values)
- **Critical**: Add test before and after to verify power bar colors unchanged
- **Dependency**: REQUIRES Step 4.3
- **Validation**:
  - Main screen functional in both themes
  - Power bar IDENTICAL to before (screenshot comparison)
  - All tests pass

**Power bar modification example:**
```kotlin
@Composable
fun OneBar(isFull: Boolean) {
    val extendedColors = LocalExtendedColors.current
    Canvas(modifier = Modifier.size(8.dp, 32.dp)) {
        // Use extended colors instead of hardcoded values
        val borderColor = if (isFull) {
            extendedColors.powerBarBorderFull  // Was: Color(0xFF1BA821)
        } else {
            extendedColors.powerBarBorderPartial  // Was: Color(0xFF3F51B5)
        }
        val fillColor = if (isFull) {
            extendedColors.powerBarFillFull  // Was: Color(0xFF13D31B)
        } else {
            extendedColors.powerBarFillPartial  // Was: Color(0xFFFF0000)
        }
        // ... rest of drawing logic
    }
}
```

#### **Step 4.5: MODIFY `/app/src/main/java/com/voxplanapp/ui/daily/DailyScreen.kt`**
- **Action**: Replace color constants and 2 inline orange quota colors with theme
- **Mapping**:
  - Inline orange colors → `MaterialTheme.colorScheme.tertiary` or extended colors
  - Background/surface colors → Material 3 surface roles
  - Text colors → `onSurface` / `onBackground`
- **Dependency**: REQUIRES Step 4.4
- **Validation**: Daily screen functional in both themes, quota indicators clear

---

### **PHASE 5: Testing & Validation**

#### **Step 5.1: CREATE ThemeTest.kt**
- **Action**: Write Compose UI tests for theme switching
- **Tests**:
  - Light theme colors match expected values
  - Dark theme colors match expected values
  - Extended colors accessible via LocalExtendedColors
- **Dependency**: REQUIRES Phase 4 completion
- **Validation**: All theme tests pass

#### **Step 5.2: CREATE PowerBarPreservationTest.kt**
- **Action**: Write tests verifying power bar colors unchanged
- **Method**: Screenshot comparison or color sampling
- **Tests**:
  - PowerBarDisplay in light mode matches baseline
  - PowerBarDisplay in dark mode matches baseline (same as light)
  - All 10 power bar colors preserved
- **Dependency**: REQUIRES Step 4.4 completion
- **Validation**: Power bar preservation tests pass

#### **Step 5.3: CREATE ColorAccessibilityTest.kt**
- **Action**: Automated WCAG contrast ratio tests
- **Tests**:
  - All text/background combinations meet WCAG AA (4.5:1)
  - Important UI meets WCAG AAA (7:1)
  - Both light and dark themes pass
- **Dependency**: REQUIRES Phase 4 completion
- **Validation**: Accessibility tests pass, no violations

#### **Step 5.4: MANUAL Testing**
- **Action**: Test on physical devices with both themes
- **Scenarios**:
  - Switch between light/dark with system settings
  - Navigate all migrated screens
  - Verify power bar unchanged
  - Test with TalkBack enabled
- **Dependency**: REQUIRES all previous steps
- **Validation**: No visual regressions, themes switch smoothly

---

## Success Definition

### Functional Success
- [ ] MainScreen displays correctly in both light and dark themes
- [ ] DailyScreen displays correctly in both light and dark themes
- [ ] Theme switches smoothly when system theme changes
- [ ] Power bar component looks IDENTICAL in both themes (branding preserved)
- [ ] All text is readable with sufficient contrast in both themes
- [ ] Navigation bar and UI elements themed consistently

### Technical Success
- [ ] All tests passing (theme tests, power bar tests, accessibility tests)
- [ ] No deprecation warnings (Colors.kt constants replaced in migrated screens)
- [ ] No inline `Color(0xFF...)` in MainScreen/DailyScreen (except power bar via extended colors)
- [ ] Material 3 colorScheme used throughout migrated screens
- [ ] Extended color scheme properly provides power bar colors
- [ ] Dynamic color disabled (`dynamicColor = false`)

### Validation Commands
```bash
# Build and run
./gradlew assembleDebug
./gradlew installDebug

# Run tests (once created)
./gradlew test
./gradlew connectedAndroidTest

# Lint check
./gradlew lint
```

---

## Notes

**Confidence**: **Medium-High**

**Rationale**:
- Pattern is clear (Material 3 official approach)
- Integration points well-mapped (20 files identified)
- External docs thorough (Material 3 codelab + articles)
- Risks identified with mitigations
- Main uncertainty: Power bar preservation during migration (needs careful testing)
- Complexity: Medium (Tier 2 appropriate - 2-3 main files for MVP, existing pattern to follow)

**Pattern Source**: Material 3 Official Documentation + Android Developer Guides

**External Refs**:
- https://developer.android.com/develop/ui/compose/designsystems/material3
- https://codelabs.developers.google.com/jetpack-compose-theming
- https://medium.com/@hidayatasep43/extending-material-3-with-custom-colors-in-jetpack-compose-9393a9db725c

**User Clarifications Needed**:
1. **Scope confirmation**: MVP focuses on MainScreen + DailyScreen only, or include all screens?
2. **FocusModeScreen**: User said it should "stay as-is" (already dark) - confirm it's excluded from theming?
3. **Dynamic color preference**: Disable completely or allow user toggle in settings?
4. **Color palette**: Use Material Theme Builder with VoxPlan green `#1BA821` as seed, or provide custom palette?

**Estimated Effort**:
- **Phase 1-2 (Foundation)**: 2-3 hours
- **Phase 3 (Navigation)**: 1 hour
- **Phase 4 (MainScreen + DailyScreen MVP)**: 4-6 hours
- **Phase 5 (Testing)**: 2-3 hours
- **Total**: 9-13 hours for MVP (MainScreen + DailyScreen)
- **Full implementation (all screens)**: 20-30 hours

**Files Affected (MVP)**:
- **Theme infrastructure**: 3 files
- **Navigation**: 1 file
- **Main screens**: 6 files (TodoInputBar, BreadcrumbNav, GoalListContainer, GoalItem, MainScreen, DailyScreen)
- **Total**: 10 files for MVP

**Next Step**: Present plan to user for approval, clarify scope questions, then proceed to EXECUTOR mode for implementation.
