# Material 3 Design System Context

**Last Updated:** 2025-12-26
**Related Beads:** VoxPlanApp-yuo (Material 3 Dark Mode implementation)
**Related PRPs:**
- `.claude/PRPs/ai_docs/material_design_3_action_heavy_lists.md` (Action-heavy list patterns)
- `scratchpad/2025-12-23-000113_material3-dark-mode/` (Dark mode implementation plan)

---

## Overview

VoxPlanApp has Material 3 theme infrastructure but it's **currently unused**. All screens use hardcoded colors, resulting in:
- No dark mode support despite user demand
- 26 color constants + 42+ inline hex values scattered across codebase
- Accessibility issues (no WCAG contrast guarantees)
- Inconsistent theming across screens

**Material 3 Implementation Status:**
- ✅ Theme infrastructure exists (`Theme.kt`)
- ❌ Not actually used by any screens
- ❌ No dark mode support
- ❌ Colors hardcoded throughout codebase

---

## Material 3 Dark Mode Implementation Plan

### Current State Analysis

**Files Affected (11+ screens):**
- DailyScreen.kt
- DaySchedule.kt
- FocusModeScreen.kt
- MainScreen.kt
- GoalEditScreen.kt
- ProgressScreen.kt
- Theme.kt
- Colors.kt
- Plus 3+ more screens

**Anti-Patterns Found:**
1. **Hardcoded colors everywhere** - `Color(0xFF...)`
2. **No semantic color roles** - No relationship to Material 3 system
3. **No dark mode awareness** - Would require duplicating all color logic

### Migration Strategy

**Detailed Plan Location:** `scratchpad/2025-12-23-000113_material3-dark-mode/`

**Key Documents:**
- `plan.md` - Strategic overview, pattern analysis (100 lines)
- `implementation-plan-v2.md` - Step-by-step migration guide (39KB)
- `color-analysis.md` - Material 3 color role mapping (21KB)

**Proposed Approach:**
1. Migrate from hardcoded colors to Material 3 semantic color roles
2. Implement light and dark color schemes
3. Support system dark mode detection (`isSystemInDarkTheme()`)
4. Preserve VoxPlan branding with extended colors (power bars, medals)
5. Ensure WCAG AA compliance for accessibility

**Extended Colors (Branding):**
- Power bar colors (bronze, silver, gold, diamond medals)
- Goal hierarchy indicators
- Quota progress visualizations
- Custom accent colors

**Decision Points:**
- [ ] Full migration vs incremental (screen by screen)?
- [ ] Dynamic color support (Android 12+ wallpaper-based)?
- [ ] Extended color approach for branding?
- [ ] Timeline and resource allocation?

**Status:** ⚠️ Plan complete, awaiting technical review and sanity-check before task breakdown

---

## Material 3 List Design Patterns

### Research Documentation

**Complete Guide:** `.claude/PRPs/ai_docs/material_design_3_action_heavy_lists.md`

This research document covers Material 3 best practices for action-heavy lists like VoxPlanApp's daily tasks screen.

### Key Principles

#### Action Density Guidelines

**Material Design Rule:** Avoid more than **2-3 actions** fully displayed per list item

**Cognitive Load Research:**
- Miller's Law: 7±2 items in working memory
- Mobile users face higher cognitive load (small screens, distractions)
- Every visible action adds cognitive burden
- Use progressive disclosure (overflow menus)

#### Recommended Pattern for VoxPlan Daily Tasks

**Current Actions Per Item:**
1. Complete/uncomplete (checkbox)
2. Schedule (time picker)
3. Delete
4. Reorder (ActionMode buttons)
5. Focus Mode access (planned)

**Recommended Visible Actions (2):**
```
[✓] Task name                    [⏰] [⋮]
    Supporting text
    [Quota progress]
```

**Components:**
- **Leading:** Checkbox (complete/uncomplete) - primary action
- **Trailing:** Schedule icon + Overflow menu (⋮)
- **Supporting:** Description + quota progress indicator

**Overflow Menu Contents:**
- Focus Mode
- Delete
- Edit
- More actions...

**Selection Mode (Long Press):**
- Bulk delete
- Bulk reschedule
- Move to different date

**Optional Swipe Actions:**
- Right swipe: Mark complete
- Left swipe: Delete (with confirmation)

### Material 3 ListItem API

```kotlin
@Composable
fun ListItem(
    headlineContent: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    overlineContent: @Composable (() -> Unit)? = null,
    supportingContent: @Composable (() -> Unit)? = null,
    leadingContent: @Composable (() -> Unit)? = null,
    trailingContent: @Composable (() -> Unit)? = null,
    colors: ListItemColors = ListItemDefaults.colors(),
    tonalElevation: Dp = ListItemDefaults.Elevation,
    shadowElevation: Dp = ListItemDefaults.Elevation,
)
```

**Content Slot Guidelines:**
- **leadingContent:** Checkbox, goal icon, avatar
- **headlineContent:** Task title (required)
- **overlineContent:** Category, date label
- **supportingContent:** Task details, time estimate, progress
- **trailingContent:** Time, icons, menu button

### Accessibility Requirements

#### Touch Target Sizes

**Material Design 3 Standard:** **48dp × 48dp** minimum

**WCAG Compliance:**
- WCAG 2.1 AAA: 44px × 44px
- WCAG 2.2 AA: 24px × 24px (minimum, no overlap)
- Material Design's 48dp exceeds WCAG requirements

**Implementation:**
```kotlin
IconButton(
    onClick = { /* action */ },
    modifier = Modifier
        .size(48.dp) // Touch target
        .semantics {
            contentDescription = "Action description"
            role = Role.Button
        }
) {
    Icon(
        imageVector = Icons.Default.Action,
        contentDescription = null, // Handled by button
        modifier = Modifier.size(24.dp) // Visual size
    )
}
```

#### Screen Reader Support

- Provide `contentDescription` for all icon-only buttons
- Use semantics modifiers for complex interactions
- Announce state changes (selected, completed, etc.)

#### Color Contrast

- WCAG AA: 4.5:1 for normal text, 3:1 for large text
- WCAG AAA: 7:1 for normal text, 4.5:1 for large text
- Don't rely on color alone to convey information

### Animations

#### The `animateItem()` Modifier

**Official Android Pattern for LazyColumn:**

```kotlin
LazyColumn(modifier) {
    items(
        items = tasks,
        key = { it.id } // CRITICAL: Unique keys required!
    ) { task ->
        ListItem(
            headlineContent = { Text(task.title) },
            modifier = Modifier
                .animateItem( // Automatic fade + slide
                    fadeInSpec = tween(durationMillis = 300),
                    fadeOutSpec = tween(durationMillis = 300)
                )
                .fillParentMaxWidth()
        )
    }
}
```

**Automatic Behavior:**
- Fade in/out effects
- Smooth position transitions
- Handles insert, remove, reorder

**Requirements:**
- **Must specify unique keys** for items (or animations won't work)
- Default uses indices (breaks on reorder)

**Note:** Replace deprecated `animateItemPlacement()` with `animateItem()`

#### Haptic Feedback

```kotlin
val haptic = LocalHapticFeedback.current

IconButton(onClick = {
    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    onDelete()
}) {
    Icon(Icons.Default.Delete, "Delete")
}
```

### Selection Mode Pattern

**Material Design Guidelines:**
- Use **long press** exclusively for selection activation
- Don't use long press for traditional contextual menus
- Show **Contextual Action Bar (CAB)** when selection active
- Replace leading icons with checkboxes in selection mode

```kotlin
var selectionMode by remember { mutableStateOf(false) }
var selectedItems by remember { mutableStateOf(setOf<Int>()) }

ListItem(
    headlineContent = { Text(task.title) },
    leadingContent = {
        if (selectionMode) {
            Checkbox(
                checked = task.id in selectedItems,
                onCheckedChange = { /* toggle */ }
            )
        } else {
            Icon(Icons.Default.CheckCircle, null)
        }
    },
    modifier = Modifier.combinedClickable(
        onClick = {
            if (selectionMode) {
                // Toggle selection
            } else {
                // Navigate to detail
            }
        },
        onLongClick = {
            selectionMode = true
            selectedItems = setOf(task.id)
        }
    )
)
```

### Progress Indicators

**Material Design 3 Guidelines:**
- Use **only one type** per process (linear OR circular, not both)
- Placement indicates scope:
  - **Centered on screen:** Loading all content
  - **Attached to item:** Process for that specific item

**Linear Progress in List Items:**

```kotlin
ListItem(
    headlineContent = { Text(task.title) },
    supportingContent = {
        Column {
            Text(task.description)
            LinearProgressIndicator(
                progress = task.quotaProgress,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
            )
        }
    }
)
```

**VoxPlan Quota Progress Options:**
1. **Inline linear progress** (Material 3 standard)
2. **Trailing progress indicator** (compact)
3. **Custom quota boxes** (current pattern - keep if user feedback positive)

### Empty States

**Material Design Best Practice:** Combine all four elements:

1. **Illustration** (highly preferred)
2. **Headline** ("No tasks for today")
3. **Body Text** ("Add your first task to get started")
4. **Call to Action** (Primary button - ONE is almost always enough)

```kotlin
@Composable
fun EmptyDailiesState(onAddTask: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            modifier = Modifier.size(120.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "No tasks for today",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Add quota tasks or create a new task to get started",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = onAddTask) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Add Task")
        }
    }
}
```

### FAB vs Inline Add Button

**Floating Action Button (FAB):**
- Represents **most important action** on screen
- Prominently positioned above all content
- Quick access during scrolling
- Use for universal actions (create new content)

**Inline Add Button:**
- Adding related items to specific context
- Multiple add locations in hierarchy
- Section-specific additions

**VoxPlan Dailies Recommendation:**
- **FAB:** "Create new task" (universal, always available)
- **Inline Button:** "Add quota tasks" (specific to current day's quotas)

---

## Responsive Design

### Screen Size Adaptations

```kotlin
val isCompactScreen = LocalConfiguration.current.screenWidthDp < 600

ListItem(
    supportingContent = if (isCompactScreen) {
        { Text("${task.minutesCompleted}/${task.quotaMinutes} min") }
    } else {
        {
            Column {
                Text(task.description)
                LinearProgressIndicator(
                    progress = task.quotaProgress,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
)
```

**Small Screens (< 600dp):**
- Minimal quota progress (text only)
- Collapse supporting text for shorter items
- Focus on critical actions

**Large Screens (≥ 600dp):**
- Full quota progress visualization
- Expanded supporting text
- Additional inline actions possible

---

## Implementation Checklist

### Material 3 Dark Mode Migration

- [ ] Technical review of implementation plan
- [ ] Sanity-check proposed migration strategy
- [ ] Validate color scheme accessibility (WCAG compliance)
- [ ] Decide: Full migration vs incremental?
- [ ] Decide: Dynamic color support (true/false)?
- [ ] Approve extended color approach for branding
- [ ] Create detailed beads tasks for implementation
- [ ] Migrate Theme.kt and Colors.kt
- [ ] Migrate 11+ screens to use Material 3 colors
- [ ] Test light and dark modes
- [ ] Verify WCAG AA contrast ratios

### Action-Heavy Lists Implementation

- [ ] Reduce visible actions to 2-3 per item
- [ ] Implement overflow menu for less frequent actions
- [ ] Add long-press selection mode for bulk operations
- [ ] Consider swipe-to-complete and swipe-to-delete
- [ ] Ensure all touch targets are 48dp minimum
- [ ] Add proper contentDescription for accessibility
- [ ] Implement `animateItem()` modifier for smooth list updates
- [ ] Add haptic feedback for key actions
- [ ] Test with screen readers (TalkBack)
- [ ] User test information density on small screens
- [ ] Verify color contrast meets WCAG AA
- [ ] Add focus indicators for keyboard navigation

---

## Code Examples

### Complete Daily Task List Item

**Location:** `.claude/PRPs/ai_docs/material_design_3_action_heavy_lists.md` (lines 820-959)

Full example showing:
- Material 3 ListItem with all content slots
- Checkbox leading content
- Quota progress in supporting content
- Schedule button + overflow menu in trailing content
- Proper accessibility semantics
- 48dp touch targets
- Dropdown menu with actions

---

## Key Design Principles

### DO:
- Limit visible actions to 2-3 per list item
- Use overflow menu when 3+ actions available
- Place primary actions on left, secondary on right
- Meet 48dp minimum touch target size
- Use long-press for selection mode activation
- Provide unique keys for animated list items
- Add haptic feedback for important actions
- Test with screen readers
- Use progressive disclosure to reduce cognitive load

### DON'T:
- Don't show more than 3 actions inline
- Don't use overflow menu with 2 or fewer actions
- Don't make secondary actions part of primary action target
- Don't rely on color alone to convey information
- Don't forget contentDescription for icon-only buttons
- Don't use different progress indicator types for same process
- Don't skip animation keys (breaks transitions)
- Don't overwhelm users with too much information density

---

## External References

### Official Documentation
- [Lists – Material Design 3](https://m3.material.io/components/lists/guidelines)
- [Selection – Material Design 3](https://m3.material.io/foundations/interaction/selection)
- [Progress indicators – Material Design 3](https://m3.material.io/components/progress-indicators/guidelines)
- [FAB – Material Design 3](https://m3.material.io/components/floating-action-button/guidelines)

### Android Developer Guides
- [Lists and grids | Jetpack Compose](https://developer.android.com/develop/ui/compose/lists)
- [Swipe to dismiss | Jetpack Compose](https://developer.android.com/develop/ui/compose/touch-input/user-interactions/swipe-to-dismiss)
- [Material Design 3 in Compose](https://developer.android.com/develop/ui/compose/designsystems/material3)
- [Touch target size - Android Accessibility](https://support.google.com/accessibility/android/answer/7101858)

### UX Research
- [Cognitive Load | Laws of UX](https://lawsofux.com/cognitive-load/)
- [Minimize Cognitive Load - Nielsen Norman Group](https://www.nngroup.com/articles/minimize-cognitive-load/)
- [WCAG 2.5.8: Target Size (Minimum)](https://www.w3.org/WAI/WCAG22/Understanding/target-size-minimum)

---

**Next Steps:** See beads issue **VoxPlanApp-yuo** for Material 3 Dark Mode implementation plan awaiting review.
