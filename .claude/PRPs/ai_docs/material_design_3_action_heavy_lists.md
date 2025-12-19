# Material Design 3: Action-Heavy Lists Research

**Research Date:** 2025-12-18
**Context:** VoxPlanApp Dailies screen design optimization
**Target:** Daily task list with multiple actions per item (complete, schedule, delete, reorder, focus)

---

## Table of Contents

1. [Material Design 3 List Item Anatomy](#list-item-anatomy)
2. [Multi-Action List Items](#multi-action-list-items)
3. [Selection Modes & Batch Operations](#selection-modes)
4. [Progress Indicators in Lists](#progress-indicators)
5. [Empty States](#empty-states)
6. [Accessibility Requirements](#accessibility)
7. [Animations & Feedback](#animations)
8. [Implementation Patterns for VoxPlanApp](#implementation-recommendations)

---

## List Item Anatomy

### Material 3 ListItem API

The `ListItem` composable provides a structured way to build list items with multiple content slots:

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

### Content Slots

| Slot | Purpose | Common Uses |
|------|---------|-------------|
| **leadingContent** | Start-aligned icon or media | Checkbox, goal icon, avatar |
| **headlineContent** | Primary text (required) | Task title |
| **overlineContent** | Text above headline | Category, date label |
| **supportingContent** | Secondary text below headline | Task details, time estimate |
| **trailingContent** | End-aligned metadata/actions | Time, icons, menu button |

### Three-Line List Item Structure

For complex list items with multiple text elements:
- **Overline** (optional): Category or context
- **Headline** (required): Primary task name
- **Supporting text** (optional): Additional details or description

**Key Principle:** Leading icons represent primary action/information; trailing icons indicate secondary actions or metadata.

**Source:** [ListItem – Material 3 Compose Documentation](https://composables.com/material3/listitem)

---

## Multi-Action List Items

### Action Placement Guidelines

Material Design places:
- **Left side:** States and primary actions (checkbox for completion)
- **Right side:** Secondary actions and metadata

**Important:** Secondary actions are always a separate target from the primary action. Users expect every icon to trigger an action.

**Source:** [Lists: Controls - Material Design](https://m1.material.io/components/lists-controls.html)

### How Many Actions Per Item?

#### Material Design Guidelines

- **Avoid more than 3 actions** fully displayed within a toolbar/list item
- **Do not use overflow menu** when there are 2 or fewer actions
- **Overflow menu appropriate** when 3+ actions available

**Source:** [PatternFly Overflow Menu](https://www.patternfly.org/components/overflow-menu/design-guidelines/)

#### Cognitive Load Research

**Miller's Law:** Average person can keep 7±2 items in working memory.

**Mobile UX Considerations:**
- Mobile users face higher cognitive load (smaller screens, distractions)
- Every task adds to cognitive burden
- Minimize number of actions user must perform
- Use progressive disclosure (show only necessary actions)

**Key Principle:** Streamline processes by removing irrelevant tasks and breaking down complex actions into smaller steps.

**Sources:**
- [Cognitive Load | Laws of UX](https://lawsofux.com/cognitive-load/)
- [Minimize Cognitive Load to Maximize Usability - NN/G](https://www.nngroup.com/articles/minimize-cognitive-load/)
- [Cognitive Load and Mobile UX Design | CustomerThink](https://customerthink.com/cognitive-load-and-mobile-ux-design-how-to-make-a-user-less-overwhelmed/)

### Action Organization Patterns

#### Pattern 1: Checkbox + Overflow Menu
```
[✓] Task name                    [⋮]
    Supporting text
```
- Best for: 3+ actions
- Primary: Checkbox (complete/uncomplete)
- Secondary: All other actions in menu

#### Pattern 2: Checkbox + 1-2 Inline Icons
```
[✓] Task name              [⏰] [⋮]
    Supporting text
```
- Best for: Most important secondary action + overflow
- Example: Complete + Schedule + More actions

#### Pattern 3: Swipe Actions
```
← Swipe reveals background actions →
```
- Best for: 1-2 most common actions
- Typically: Complete (right swipe) + Delete (left swipe)

#### Pattern 4: Selection Mode
```
Long-press activates batch selection
[✓] [✓] [ ] Items selected
Contextual action bar appears
```
- Best for: Batch operations on multiple items

---

## Selection Modes

### Material Design Selection Guidelines

**Activation:**
- Use **long press gesture** exclusively for selection activation
- Do not use long press for traditional contextual menus

**Contextual Action Bar (CAB):**
- Temporary action bar overlays app's current action bar
- Remains active until action taken or dismissed
- Appears after long-press on selectable item

**Visual Indicators:**
- Checkboxes appear when selection mode active
- Background color change for selected items
- Border or elevation changes

**Sources:**
- [Selection – Material Design 3](https://m3.material.io/foundations/interaction/selection)
- [Selection | Android Developers](http://www.androiddocs.com/design/patterns/selection.html)
- [Material Components Android - Top App Bar](https://github.com/material-components/material-components-android/blob/master/docs/components/TopAppBar.md)

### Implementation Pattern

```kotlin
// Selection mode state
var selectionMode by remember { mutableStateOf(false) }
var selectedItems by remember { mutableStateOf(setOf<Int>()) }

// List item with selection support
ListItem(
    headlineContent = { Text(task.title) },
    leadingContent = {
        if (selectionMode) {
            Checkbox(
                checked = task.id in selectedItems,
                onCheckedChange = { /* toggle selection */ }
            )
        } else {
            Icon(Icons.Default.CheckCircle, null)
        }
    },
    modifier = Modifier
        .combinedClickable(
            onClick = {
                if (selectionMode) {
                    // Toggle selection
                } else {
                    // Navigate to detail
                }
            },
            onLongClick = {
                // Activate selection mode
                selectionMode = true
                selectedItems = setOf(task.id)
            }
        )
)

// Contextual action bar when selection active
if (selectionMode) {
    TopAppBar(
        title = { Text("${selectedItems.size} selected") },
        actions = {
            IconButton(onClick = { /* Delete selected */ }) {
                Icon(Icons.Default.Delete, "Delete")
            }
            IconButton(onClick = { /* More actions */ }) {
                Icon(Icons.Default.MoreVert, "More")
            }
        }
    )
}
```

---

## Swipe Actions

### SwipeToDismissBox Implementation

Material 3 provides `SwipeToDismissBox` for swipe-to-reveal actions:

```kotlin
@Composable
fun SwipeableTaskItem(
    task: Task,
    onComplete: () -> Unit,
    onDelete: () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { direction ->
            when (direction) {
                SwipeToDismissBoxValue.StartToEnd -> {
                    onComplete()
                    false // Don't dismiss, just toggle
                }
                SwipeToDismissBoxValue.EndToStart -> {
                    onDelete()
                    true // Dismiss item
                }
                else -> false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val color = when (dismissState.dismissDirection) {
                SwipeToDismissBoxValue.StartToEnd -> Color.Green
                SwipeToDismissBoxValue.EndToStart -> Color.Red
                else -> Color.Transparent
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color),
                contentAlignment = if (dismissState.dismissDirection ==
                    SwipeToDismissBoxValue.StartToEnd) {
                    Alignment.CenterStart
                } else {
                    Alignment.CenterEnd
                }
            ) {
                Icon(
                    imageVector = if (dismissState.dismissDirection ==
                        SwipeToDismissBoxValue.StartToEnd) {
                        Icons.Default.Check
                    } else {
                        Icons.Default.Delete
                    },
                    contentDescription = null,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    ) {
        ListItem(
            headlineContent = { Text(task.title) },
            // ... other content
        )
    }
}
```

### Swipe Direction Conventions

- **StartToEnd (Right swipe):** Positive actions (complete, mark done, archive)
- **EndToStart (Left swipe):** Destructive actions (delete, remove)

**Advanced:** Use `lerp()` with `dismissState.progress` for smooth color transitions based on swipe distance.

**Sources:**
- [Swipe to dismiss or update | Android Developers](https://developer.android.com/develop/ui/compose/touch-input/user-interactions/swipe-to-dismiss)
- [Android Jetpack Compose - Swipe-to-Dismiss with Material 3](https://www.geeksforgeeks.org/android/android-jetpack-compose-swipe-to-dismiss-with-material-3/)
- [Swipe To Dismiss - Jetpack Compose Material3 | Medium](https://medium.com/@daniel.atitienei/swipe-to-dismiss-jetpack-compose-material3-4f9720333fad)

---

## Progress Indicators

### Types and Placement

Material Design 3 provides two types:
- **Linear:** Horizontal bar showing progress
- **Circular:** Ring showing progress or indeterminate loading

**Consistency Rule:** Use only one type per process throughout the app. If refresh uses circular, don't use linear elsewhere for refresh.

### Placement Guidelines

**Linear Progress Indicators:**
- **Centered on screen:** Indicates loading all screen content
- **Attached to container (card/list item):** Indicates process for that specific item
- Scope of process indicated by placement

**Circular Progress Indicators:**
- **Centered on screen:** Initial loading of screen content
- **Above/below content:** Draws attention to where new content will appear
- **Integrated into button/card:** Shows connection between interaction and item
- Can be applied directly to surfaces (buttons, cards)

**Recent Material 3 Updates:**
- Gap between active and inactive tracks
- Stop indicator
- Rounded corners (linear)
- Rounded stroke cap (circular)

**Source:** [Progress indicators – Material Design 3](https://m3.material.io/components/progress-indicators/guidelines)

### Progress in VoxPlanApp Context

**Quota Progress Display Options:**

#### Option 1: Inline Linear Progress
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

#### Option 2: Trailing Progress Indicator
```kotlin
ListItem(
    headlineContent = { Text(task.title) },
    trailingContent = {
        Column(horizontalAlignment = Alignment.End) {
            Text("${task.minutesCompleted}/${task.quotaMinutes}")
            LinearProgressIndicator(
                progress = task.quotaProgress,
                modifier = Modifier.width(60.dp)
            )
        }
    }
)
```

#### Option 3: Custom Quota Boxes (Current VoxPlanApp Pattern)
Keep existing visual design if it provides clear value, but ensure:
- Minimum touch target: 48dp
- Clear visual hierarchy
- Not overwhelming with other actions

---

## Empty States

### Material Design Empty State Guidelines

**Best Practice:** Combine illustration, headline, body text, and call to action.

### Components

**Illustration (Highly Preferred):**
- Makes page more attractive
- Adds personality/humor
- Helps user understand situation
- Should complement text and CTA, not dominate

**Headline:**
- Clear, concise explanation of empty state
- Example: "No tasks for today"

**Body Text:**
- Brief explanation or guidance
- Example: "Add your first task to get started"

**Call to Action (CTA):**
- Primary button for most logical action
- Gets user from empty state to content
- **One CTA is almost always enough**
- Maximum two CTAs if multiple clear paths

**Sources:**
- [Empty states - Material Design](https://m1.material.io/patterns/empty-states.html)
- [Material Design - Empty States](https://m2.material.io/design/communication/empty-states.html)
- [Empty States Design Best Practices | Medium](https://cadabrastudio.medium.com/empty-states-design-best-practices-4ae6f72b654b)

### Empty State Example

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

---

## Accessibility

### Touch Target Sizes

**Material Design 3 Requirement:**
- Minimum: **48dp × 48dp** for all interactive elements
- Touch target extends beyond visual bounds
- Example: 24dp icon with 12dp padding = 48dp touch target

**WCAG Standards:**

| Level | Size Requirement | Notes |
|-------|------------------|-------|
| WCAG 2.1 AAA (2.5.5) | 44px × 44px | Highest standard |
| WCAG 2.2 AA (2.5.8) | 24px × 24px | Minimum, no overlap |
| Material Design 3 | 48dp × 48dp | Exceeds WCAG requirements |

Material Design's 48dp recommendation "accommodates a larger spectrum of users" beyond minimum WCAG requirements.

**Sources:**
- [Understanding Success Criterion 2.5.5: Target Size | W3C](https://www.w3.org/WAI/WCAG21/Understanding/target-size.html)
- [Understanding Success Criterion 2.5.8: Target Size (Minimum) | W3C](https://www.w3.org/WAI/WCAG22/Understanding/target-size-minimum)
- [Touch target size - Android Accessibility](https://support.google.com/accessibility/android/answer/7101858?hl=en)

### Additional Accessibility Requirements

**Screen Readers:**
- Provide contentDescription for all icon-only buttons
- Use semantics modifiers for complex interactions
- Announce state changes (selected, completed, etc.)

**Color Contrast:**
- WCAG AA: 4.5:1 for normal text, 3:1 for large text
- WCAG AAA: 7:1 for normal text, 4.5:1 for large text
- Don't rely on color alone to convey information

**Focus Indicators:**
- Visible focus state for keyboard navigation
- Clear indication of currently focused element

**Implementation Example:**

```kotlin
IconButton(
    onClick = { /* schedule task */ },
    modifier = Modifier
        .size(48.dp) // Meets touch target requirement
        .semantics {
            contentDescription = "Schedule task for ${task.title}"
            role = Role.Button
        }
) {
    Icon(
        imageVector = Icons.Default.Schedule,
        contentDescription = null, // Handled by button semantics
        modifier = Modifier.size(24.dp) // Visual size
    )
}
```

---

## Animations

### List Item Animations

Material Design 3 provides smooth animations for:
- Item addition
- Item removal
- Reordering
- State transitions

### The `animateItem()` Modifier

**Official Android Pattern:**

```kotlin
@Composable
fun AnimatedTaskList(
    tasks: List<Task>,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier) {
        items(
            items = tasks,
            key = { it.id } // CRITICAL: Unique keys required
        ) { task ->
            ListItem(
                headlineContent = { Text(task.title) },
                modifier = Modifier
                    .animateItem( // Automatic fade + slide animations
                        fadeInSpec = tween(durationMillis = 300),
                        fadeOutSpec = tween(durationMillis = 300)
                    )
                    .fillParentMaxWidth()
            )
        }
    }
}
```

**Key Requirements:**
- Specify **unique keys** for items (or animations won't work)
- Default uses indices as keys (breaks on reorder)

**Automatic Behavior:**
- Fade in/out effects
- Smooth position transitions
- Handles insert, remove, reorder

**Customization:**
- `fadeInSpec` / `fadeOutSpec` for timing
- Can disable fades entirely if needed

**Migration Note:** Replace deprecated `animateItemPlacement()` with `animateItem()`.

**Sources:**
- [Lists and grids | Jetpack Compose | Android Developers](https://developer.android.com/develop/ui/compose/lists)
- [Animating LazyList items in Jetpack Compose | Medium](https://medium.com/@gregkorossy/animating-lazylist-items-in-jetpack-compose-6b40f94aaa1a)

### Completion Animation Pattern

```kotlin
var completed by remember { mutableStateOf(task.completed) }
val scale by animateFloatAsState(
    targetValue = if (completed) 1.1f else 1f,
    animationSpec = spring(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessLow
    )
)

Checkbox(
    checked = completed,
    onCheckedChange = {
        completed = it
        // Trigger haptic feedback
        // Update repository
    },
    modifier = Modifier.scale(scale)
)
```

### Haptic Feedback

Provide tactile feedback for important actions:
```kotlin
val haptic = LocalHapticFeedback.current

IconButton(onClick = {
    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    onDelete()
}) {
    Icon(Icons.Default.Delete, "Delete")
}
```

---

## FAB vs Inline Add Button

### Material Design FAB Guidelines

**Floating Action Button (FAB):**
- Represents the **most important action** on a screen
- Prominently positioned above all content
- Quick access to primary action (Create, Reply, etc.)

**Types:**
- **Standard FAB:** Icon only
- **Extended FAB:** Icon + text label (wider, more detail)
- **FAB Menu:** Opens panel with related actions

**Real-World Examples:**
- Google Keep: FAB in bottom nav for "Add note"
- Gmail: Extended FAB for "Compose email"

**When to Use:**
- Primary action applicable throughout scrolling
- Action independent of list item context
- Creates new content (not modifying existing)

**Sources:**
- [FAB – Material Design 3](https://m3.material.io/components/floating-action-button/guidelines)
- [How I used a FAB - Material Design 3](https://mstflotfy.com/theindiedev/fab-material-design-3)

### Inline Add Button

**When to Use:**
- Adding related items to specific context
- Multiple add locations in hierarchy
- Section-specific additions

**Example:** "Add subtask" button within a parent task.

### Recommendation for VoxPlanApp Dailies

**Use FAB for:**
- "Create new task" (universal action)
- Available during scrolling

**Use Inline Button for:**
- "Add quota tasks" (specific to current day's quotas)
- Contextual to daily view

**Compromise:**
- Extended FAB: "Add Task" (primary)
- Secondary button at top: "Import from Quota" (contextual)

---

## Implementation Recommendations for VoxPlanApp

### Current VoxPlanApp Dailies Actions

Per item:
1. Complete/uncomplete (checkbox)
2. Schedule (opens time picker)
3. Delete
4. Reorder (ActionMode: VerticalUp/Down)
5. Focus Mode access (needs to be added)

### Recommended Action Pattern

#### Primary Pattern: Hybrid Approach

**Visible Actions (2-3):**
```
[✓] Task name                    [⏰] [⋮]
    Supporting text
    [Quota progress visualization]
```

Components:
- **Leading:** Checkbox (complete/uncomplete)
- **Trailing:** Schedule icon + Overflow menu
- **Supporting:** Description + quota progress

**Overflow Menu (⋮) Actions:**
- Focus Mode
- Delete
- Edit
- More...

**Selection Mode (Long Press):**
- Activate for bulk operations
- Delete multiple
- Reschedule multiple
- Move to different date

**Swipe Actions (Optional Enhancement):**
- Right swipe: Mark complete
- Left swipe: Delete (with confirmation)

### Why This Pattern?

**Cognitive Load:**
- Reduces visible actions from 5 to 2 (checkbox + schedule)
- Meets guideline of "no more than 3 actions fully displayed"
- Overflow menu provides discoverability without clutter

**Accessibility:**
- All buttons meet 48dp touch target
- Clear visual hierarchy
- Screen reader support

**Common Actions Prioritized:**
- Complete: Most frequent (always visible)
- Schedule: High frequency (always visible)
- Delete, Focus: Less frequent (in menu)

**Reordering:**
- Keep existing ActionMode system
- Separate from item actions (reduces clutter)
- Already implemented and familiar

### Quota Progress Display

**Option A: Subtle Integration (Recommended)**
```kotlin
ListItem(
    headlineContent = { Text(task.title) },
    supportingContent = {
        Column {
            Text(task.description)
            // Small progress bar or text indicator
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Icon(
                    Icons.Default.Timer,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    "${task.minutesCompleted}/${task.quotaMinutes} min",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
)
```

**Option B: Visual Emphasis**
- Keep existing quota box design if user feedback is positive
- Ensure it doesn't dominate list item
- Consider moving to supporting text area

**Recommendation:** User test both approaches. Material Design favors simplicity, but custom visualizations can add value if they aid comprehension.

### Action Priority Matrix

| Action | Frequency | Visibility | Implementation |
|--------|-----------|------------|----------------|
| Complete | Very High | Always visible | Leading checkbox |
| Schedule | High | Always visible | Trailing icon |
| Focus Mode | Medium | Overflow menu | Menu item + FAB option |
| Delete | Low-Medium | Overflow menu | Menu item + swipe option |
| Edit | Low | Overflow menu | Menu item |
| Bulk operations | Low | Selection mode | Long press activation |

### Information Density Balance

**Small Screens (< 600dp width):**
- Minimal quota progress (text only)
- Collapse supporting text for shorter items
- Focus on critical actions

**Large Screens (≥ 600dp width):**
- Full quota progress visualization
- Expanded supporting text
- Additional inline actions possible

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

### Code Example: Complete Daily Task List Item

```kotlin
@Composable
fun DailyTaskListItem(
    task: DailyTask,
    onCompleteToggle: (Boolean) -> Unit,
    onSchedule: () -> Unit,
    onDelete: () -> Unit,
    onFocusMode: () -> Unit,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }

    ListItem(
        headlineContent = { Text(task.title) },
        supportingContent = {
            Column {
                Text(
                    text = task.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                // Quota progress indicator
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Icon(
                        Icons.Default.Timer,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "${task.minutesCompleted}/${task.quotaMinutes} min",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        },
        leadingContent = {
            Checkbox(
                checked = task.completed,
                onCheckedChange = onCompleteToggle,
                modifier = Modifier.semantics {
                    contentDescription = if (task.completed) {
                        "Mark ${task.title} as incomplete"
                    } else {
                        "Mark ${task.title} as complete"
                    }
                }
            )
        },
        trailingContent = {
            Row {
                // Schedule button
                IconButton(
                    onClick = onSchedule,
                    modifier = Modifier
                        .size(48.dp)
                        .semantics {
                            contentDescription = "Schedule ${task.title}"
                        }
                ) {
                    Icon(
                        Icons.Default.Schedule,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Overflow menu
                Box {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier
                            .size(48.dp)
                            .semantics {
                                contentDescription = "More actions for ${task.title}"
                            }
                    ) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Focus Mode") },
                            onClick = {
                                showMenu = false
                                onFocusMode()
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Timer, "Focus Mode")
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Edit") },
                            onClick = {
                                showMenu = false
                                onEdit()
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Edit, "Edit")
                            }
                        )
                        Divider()
                        DropdownMenuItem(
                            text = { Text("Delete") },
                            onClick = {
                                showMenu = false
                                onDelete()
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Delete,
                                    "Delete",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            },
                            colors = MenuDefaults.itemColors(
                                textColor = MaterialTheme.colorScheme.error
                            )
                        )
                    }
                }
            }
        },
        modifier = modifier.animateItem()
    )
}
```

### Implementation Checklist

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

## Key Takeaways

### DO:

- Limit visible actions to 2-3 per list item
- Use overflow menu when 3+ actions available
- Place primary actions on left, secondary on right
- Meet 48dp minimum touch target size
- Use long-press for selection mode activation
- Provide unique keys for animated list items
- Add haptic feedback for important actions
- Test with screen readers and ensure proper semantics
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

### Material Design Philosophy

"Material Design helps teams build high-quality digital experiences by providing a system that adapts to any product and any platform while maintaining the essential guidance needed to create consistency and usability."

For action-heavy lists, this means:
- **Clarity:** Make it obvious what actions are available
- **Efficiency:** Prioritize frequent actions, hide rare ones
- **Consistency:** Use established patterns (long-press for selection, overflow menus, etc.)
- **Accessibility:** Ensure all users can interact regardless of ability

---

## Additional Resources

### Official Documentation

- [Lists – Material Design 3](https://m3.material.io/components/lists/guidelines)
- [Selection – Material Design 3](https://m3.material.io/foundations/interaction/selection)
- [Icon buttons – Material Design 3](https://m3.material.io/components/icon-buttons/guidelines)
- [Progress indicators – Material Design 3](https://m3.material.io/components/progress-indicators/guidelines)
- [FAB – Material Design 3](https://m3.material.io/components/floating-action-button/guidelines)

### Android Developer Guides

- [Lists and grids | Jetpack Compose](https://developer.android.com/develop/ui/compose/lists)
- [Swipe to dismiss | Jetpack Compose](https://developer.android.com/develop/ui/compose/touch-input/user-interactions/swipe-to-dismiss)
- [Material Design 3 in Compose](https://developer.android.com/develop/ui/compose/designsystems/material3)
- [Touch target size - Android Accessibility](https://support.google.com/accessibility/android/answer/7101858)

### Community Resources

- [ListItem – Material 3 Compose Documentation](https://composables.com/material3/listitem)
- [Discovering Material 3 for Android — ListItem | Medium](https://medium.com/@renaud.mathieu/discovering-material-3-for-android-listitem-62f30b3cad68)
- [Animating LazyList items in Jetpack Compose | Medium](https://medium.com/@gregkorossy/animating-lazylist-items-in-jetpack-compose-6b40f94aaa1a)

### UX Research

- [Cognitive Load | Laws of UX](https://lawsofux.com/cognitive-load/)
- [Minimize Cognitive Load to Maximize Usability - NN/G](https://www.nngroup.com/articles/minimize-cognitive-load/)
- [WCAG 2.5.8: Target Size (Minimum) | W3C](https://www.w3.org/WAI/WCAG22/Understanding/target-size-minimum)

---

**Document Version:** 1.0
**Last Updated:** 2025-12-18
**Maintained By:** VoxPlanApp Development Team
