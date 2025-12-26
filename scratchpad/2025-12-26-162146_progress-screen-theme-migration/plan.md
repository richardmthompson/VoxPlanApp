# Progress Screen Material 3 Theme Migration

**Created:** 2025-12-26
**Task Type:** Simple (Tier 1) - Color migration
**Estimated Time:** 1 hour

---

## Background and Motivation

ProgressScreen.kt uses 13 hardcoded Color() values for card backgrounds, borders, and text, preventing dark mode support and breaking Material 3 theming consistency. The screen already uses MaterialTheme.colorScheme for some elements (emerald icons, stars, time text), demonstrating the target pattern.

**Dependencies:** Requires Task 1 (Enable Material 3 Theme Infrastructure) to be completed first.

---

## Feature Goal

Replace all hardcoded colors in ProgressScreen.kt with Material 3 semantic color roles from MaterialTheme.colorScheme, maintaining visual design while enabling theme support.

---

## Context

### Affected File
`app/src/main/java/com/voxplanapp/ui/goals/ProgressScreen.kt` (442 lines)

### Pattern to Follow

**Same file** already demonstrates correct usage:
- **Line 247**: `MaterialTheme.colorScheme.tertiary` for filled emerald gem
- **Line 249**: `MaterialTheme.colorScheme.outline` for unfilled gem
- **Line 339**: `MaterialTheme.colorScheme.outline` for unachieved stars
- **Line 360, 403**: `MaterialTheme.colorScheme.onSurfaceVariant` for secondary text
- **Line 394**: `MaterialTheme.colorScheme.primary` for achievement stars

**External Reference**: DaySchedule.kt:211-217 shows clean state-based color pattern using `secondary` + `onSecondary` for "TODAY" button.

### Hardcoded Colors to Replace

| Line | Current Color | Hex Value | Used For | Material 3 Role |
|------|---------------|-----------|----------|-----------------|
| 172 | `Color(0xFFBAF7FF)` | Cyan | Today card background | `tertiaryContainer` |
| 173 | `Color(0xFFFFF8DC)` | Cream | Past card background | `secondaryContainer` |
| 174 | `Color(0xFFEEEEEE)` | Light gray | Future card background | `surfaceContainerHigh` |
| 177 | `Color(0xFF3F51B5)` | Indigo | Today card border | `primary` |
| 178 | `Color(0xC1FF9800)` | Orange+alpha | Past card border | `secondary` |
| 179 | `Color.LightGray` | - | Future card border | `outline` |
| 183 | `Color.Gray` | - | Future day text (grayed) | `onSurfaceVariant` |
| 184 | `Color(0xFF3F51B5)` | Indigo | Today day text | `primary` |
| 186 | `Color.Black` | - | Past day text | `onSurface` |
| 337 | `Color(0xFFFFC107)` | Gold | Filled achievement star | Keep OR `tertiary` |
| 350 | `Color(0xFFDA70D6)` | Orchid | Overachievement star | **Keep** (special indicator) |

### Color Already in Theme (Commented Out)
- **Line 94**: `Color(0xFF002702)` - Dark green card (entire WeeklySummary is commented out, skip for now)
- **Line 135**: `Color(0xFF3F51B5)` - "WEEK TOTAL" text (in commented code)

### Known Gotcha

**Alpha Channel**: Line 178 uses `Color(0xC1FF9800)` with alpha channel (0xC1 = 75% opacity). Material 3 semantic colors don't have alpha variants. **Solution**: Use `.copy(alpha = 0.75f)` to preserve translucency.

```kotlin
// BEFORE:
borderColor = Color(0xC1FF9800)

// AFTER:
borderColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.75f)
```

**Star Colors**: Gold star (line 337) can either:
1. **Option A**: Use `MaterialTheme.colorScheme.tertiary` (semantic role for accents)
2. **Option B**: Keep `Color(0xFFFFC107)` temporarily until extended colors defined

Recommend **Option B** for this migration (minimize risk), convert to extended color in separate task.

### Integration Points

**None** - ProgressScreen is self-contained. Changes are local to this file only.

**Testing Impact**:
- No existing tests for ProgressScreen (manual visual testing required)
- ProgressViewModel.kt unchanged (no color logic)

---

## Implementation Steps

### STEP 1: Replace Card Background Colors (Lines 171-175)

**LOCATION**: `DayProgressCard` composable, lines 171-175

**CHANGE**:
```kotlin
// FROM:
val cardColor = when {
    isToday -> Color(0xFFBAF7FF)        // Cyan
    isBeforeToday -> Color(0xFFFFF8DC)  // Cream
    else -> Color(0xFFEEEEEE)           // Light gray
}

// TO:
val cardColor = when {
    isToday -> MaterialTheme.colorScheme.tertiaryContainer
    isBeforeToday -> MaterialTheme.colorScheme.secondaryContainer
    else -> MaterialTheme.colorScheme.surfaceContainerHigh
}
```

### STEP 2: Replace Border Colors (Lines 176-180)

**LOCATION**: `DayProgressCard` composable, lines 176-180

**CHANGE**:
```kotlin
// FROM:
val borderColor = when {
    isToday -> Color(0xFF3F51B5)        // Indigo
    isBeforeToday -> Color(0xC1FF9800)  // Orange with alpha
    else -> Color.LightGray
}

// TO:
val borderColor = when {
    isToday -> MaterialTheme.colorScheme.primary
    isBeforeToday -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.75f)
    else -> MaterialTheme.colorScheme.outline
}
```

**NOTE**: `.copy(alpha = 0.75f)` preserves original 75% opacity (0xC1 = 193/255 = 0.757).

### STEP 3: Replace Text Colors (Lines 182-186)

**LOCATION**: `DayProgressCard` composable, lines 182-186

**CHANGE**:
```kotlin
// FROM:
val textColor = when {
    isFuture -> Color.Gray
    isToday -> Color(0xFF3F51B5)  // Indigo
    else -> Color.Black
}

// TO:
val textColor = when {
    isFuture -> MaterialTheme.colorScheme.onSurfaceVariant
    isToday -> MaterialTheme.colorScheme.primary
    else -> MaterialTheme.colorScheme.onSurface
}
```

### STEP 4: Verify Build

**VALIDATE**:
```bash
./gradlew assembleDebug
```

**Expected**: Compilation succeeds, no errors.

**IF_FAIL**:
- Check MaterialTheme import: `import androidx.compose.material3.MaterialTheme`
- Verify VoxPlanTheme is used in MainActivity.setContent { }
- Check for typos in colorScheme property names

### STEP 5: Manual Visual Testing

**TEST PLAN**:
1. Build and install: `./gradlew installDebug`
2. Navigate to Progress screen
3. **Verify Today card**:
   - Background: Tertiary container color (cyan-ish tint from theme)
   - Border: Primary color (VoxPlan blue)
   - Day text: Primary color
4. **Verify Past days**:
   - Background: Secondary container (pale green-ish from theme)
   - Border: Secondary with 75% opacity
   - Day text: On surface (standard text color)
5. **Verify Future days**:
   - Background: Surface container high (elevated gray)
   - Border: Outline (subtle gray)
   - Day text: On surface variant (de-emphasized gray)
6. **Verify Stars/Emeralds**:
   - Filled emerald: Tertiary (unchanged)
   - Unfilled: Outline (unchanged)
   - Filled star: Gold (unchanged for now)
   - Overachievement star: Orchid (unchanged)

**Success Criteria**:
- All day cards render with correct colors
- Today/past/future states visually distinct
- No "unspecified color" errors
- No visual regressions from original design

---

## Success Definition

**Build Validation**:
- ✅ `./gradlew assembleDebug` succeeds
- ✅ No compilation errors or warnings

**Visual Validation**:
- ✅ Today card uses tertiary container background + primary border/text
- ✅ Past days use secondary container background + secondary border (75% alpha) + on surface text
- ✅ Future days use surface container high + outline border + on surface variant text
- ✅ State differentiation remains clear (today/past/future visually distinguishable)
- ✅ No color regressions (emeralds/stars unchanged)

**Code Quality**:
- ✅ No hardcoded Color() values remain (except gold/orchid stars - deferred)
- ✅ Follows existing pattern from lines 247-403 (MaterialTheme.colorScheme usage)

**Testing**:
- ✅ Manual test on emulator/device shows correct rendering
- ✅ Week navigation works (previous/next week)
- ✅ Day cards display progress correctly

---

## Rollback Strategy

### If Build Fails (Step 4):
```bash
git checkout app/src/main/java/com/voxplanapp/ui/goals/ProgressScreen.kt
./gradlew clean assembleDebug
```

### If Visual Issues (Step 5):
1. **Document the issue** (screenshot, describe problem)
2. **Revert changes**:
   ```bash
   git checkout app/src/main/java/com/voxplanapp/ui/goals/ProgressScreen.kt
   ```
3. **Report to user** with screenshot showing issue

---

## Files Modified

1. **`app/src/main/java/com/voxplanapp/ui/goals/ProgressScreen.kt`** - Replace hardcoded colors with MaterialTheme.colorScheme (9 replacements)

---

## Dependencies

**Prerequisites**:
- Task 1: "Enable Material 3 Theme Infrastructure" **MUST** be completed first
- VoxPlanTheme must be functional with VoxPlan color scheme
- MaterialTheme.colorScheme.* must be accessible

**No new dependencies required** - Material 3 already in project.

---

## Notes

- **Star colors deferred**: Gold (0xFFFFC107) and orchid (0xFFDA70D6) stars kept as hardcoded for now. Should be migrated to extended colors in separate task when medal/achievement color system is defined.
- **Commented code skipped**: WeeklySummary component (lines 87-103, 121-155) is entirely commented out. Skip migration for now; address when/if uncommented.
- **Alpha transparency preserved**: Line 178's 75% opacity maintained using `.copy(alpha = 0.75f)`.
- **Self-contained change**: No impact on other screens or ViewModels.
- **No tests**: ProgressScreen has no existing tests. Manual visual validation is primary verification method.
- **Incremental migration**: This is pilot migration for VoxPlan. If successful, pattern can be replicated for other screens (MainScreen, DailyScreen, etc.).

---

## AI Docs References

- **Material 3 Color System Guide**: `.claude/PRPs/ai_docs/material3_color_system.md` - Complete semantic role reference
- **VoxPlan Color Mapping**: `.claude/PRPs/ai_docs/voxplan_color_mapping_template.md` - Color decision matrix
- **Material 3 Context**: `agent/context/material3_context.md` - VoxPlan-specific Material 3 integration notes

---

**Task Complexity**: Simple (Tier 1)
**Confidence**: High (existing pattern in same file, clear color mappings, no external dependencies)
**Estimated Effort**: 30 minutes coding + 30 minutes testing = 1 hour total
