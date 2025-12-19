# Jetpack Compose Checkbox Completion Patterns

Research findings for implementing completion checkboxes in task lists with Material3.

## 1. Checkbox Component Usage

### Basic Material3 Checkbox

**Official Documentation:** [Checkbox | Android Developers](https://developer.android.com/develop/ui/compose/components/checkbox)

```kotlin
@Composable
fun CheckboxMinimalExample() {
    var checked by remember { mutableStateOf(true) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("Minimal checkbox")
        Checkbox(
            checked = checked,
            onCheckedChange = { checked = it }
        )
    }

    Text(
        if (checked) "Checkbox is checked" else "Checkbox is unchecked"
    )
}
```

**Key Parameters:**
- `checked: Boolean` - State indicating selection
- `onCheckedChange: ((Boolean) -> Unit)?` - Callback triggered on user interaction
- `enabled: Boolean` - Controls whether interactions are allowed
- `colors: CheckboxColors` - Customize checked/unchecked colors

### Custom Colors

```kotlin
Checkbox(
    checked = isChecked,
    onCheckedChange = { isChecked = it },
    colors = CheckboxDefaults.colors(
        checkedColor = MaterialTheme.colorScheme.primary,
        uncheckedColor = MaterialTheme.colorScheme.outline
    )
)
```

### TriStateCheckbox (Indeterminate)

**Use Case:** Parent checkboxes with child selections

```kotlin
@Composable
fun CheckboxParentExample() {
    val childCheckedStates = remember { mutableStateListOf(false, false, false) }

    val parentState = when {
        childCheckedStates.all { it } -> ToggleableState.On
        childCheckedStates.none { it } -> ToggleableState.Off
        else -> ToggleableState.Indeterminate
    }

    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Select all")
            TriStateCheckbox(
                state = parentState,
                onClick = {
                    val newState = parentState != ToggleableState.On
                    childCheckedStates.forEachIndexed { index, _ ->
                        childCheckedStates[index] = newState
                    }
                }
            )
        }

        childCheckedStates.forEachIndexed { index, checked ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Option ${index + 1}")
                Checkbox(
                    checked = checked,
                    onCheckedChange = { isChecked ->
                        childCheckedStates[index] = isChecked
                    }
                )
            }
        }
    }
}
```

---

## 2. Visual Completion Feedback

### Strikethrough Text Pattern

**Basic Implementation:**

```kotlin
Text(
    text = "Task description",
    style = TextStyle(
        textDecoration = if (isCompleted) TextDecoration.LineThrough else null
    )
)
```

### Color and Alpha Changes

**Official Documentation:** [Style text | Android Developers](https://developer.android.com/develop/ui/compose/text/style-text)

```kotlin
Text(
    text = "Task description",
    style = TextStyle(
        textDecoration = if (isCompleted) TextDecoration.LineThrough else null,
        color = if (isCompleted) Color.Gray else Color.Black,
        alpha = if (isCompleted) 0.6f else 1.0f
    )
)
```

### Advanced Styling with AnnotatedString

```kotlin
Text(
    text = buildAnnotatedString {
        withStyle(
            style = SpanStyle(
                textDecoration = TextDecoration.LineThrough,
                color = Color.Gray,
                alpha = 0.6f
            )
        ) {
            append("Completed task text")
        }
    }
)
```

### Animation Pattern (Optional)

For animated strikethrough effects, use `animateFloatAsState` to gradually reveal the strikethrough:

```kotlin
val strikethroughProgress by animateFloatAsState(
    targetValue = if (isCompleted) 1f else 0f,
    animationSpec = tween(durationMillis = 300)
)
```

---

## 3. List Item Patterns with Checkboxes

### Accessible Pattern (RECOMMENDED)

**Source:** [CVS Health Android Compose Accessibility Techniques](https://github.com/cvs-health/android-compose-accessibility-techniques/blob/main/doc/components/CheckboxControls.md)

**Official Documentation:** [API defaults | Android Developers](https://developer.android.com/develop/ui/compose/accessibility/api-defaults)

```kotlin
@Composable
fun AccessibleCheckboxListItem(
    text: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .toggleable(
                value = isChecked,
                role = Role.Checkbox,
                onValueChange = onCheckedChange
            )
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = isChecked,
            onCheckedChange = null, // null for accessibility
            modifier = Modifier.minimumInteractiveComponentSize()
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(start = 16.dp)
        )
    }
}
```

**Why This Pattern?**

1. **Touch Target:** Entire row (56dp height) is clickable, not just checkbox
2. **Accessibility:** `Modifier.toggleable()` merges semantics - screen readers announce as single element
3. **Null Handler:** `onCheckedChange = null` prevents duplicate touch targets
4. **Role:** `role = Role.Checkbox` explicitly sets semantic role for assistive technologies
5. **Minimum Size:** `minimumInteractiveComponentSize()` ensures 48dp minimum touch target

### Todo Item with Completion Styling

```kotlin
@Composable
fun TodoCheckboxItem(
    text: String,
    isCompleted: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .toggleable(
                value = isCompleted,
                role = Role.Checkbox,
                onValueChange = onCheckedChange
            )
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = isCompleted,
            onCheckedChange = null,
            modifier = Modifier.minimumInteractiveComponentSize()
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge.copy(
                textDecoration = if (isCompleted) TextDecoration.LineThrough else null,
                color = if (isCompleted)
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                else
                    MaterialTheme.colorScheme.onSurface
            ),
            modifier = Modifier.padding(start = 16.dp)
        )
    }
}
```

### Card-Based List Item

```kotlin
@Composable
fun TodoCheckboxCard(
    text: String,
    isCompleted: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 8.dp),
        onClick = { onCheckedChange(!isCompleted) }
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .semantics {
                    role = Role.Checkbox
                    stateDescription = if (isCompleted) "Completed" else "Not completed"
                },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isCompleted,
                onCheckedChange = null
            )
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge.copy(
                    textDecoration = if (isCompleted) TextDecoration.LineThrough else null
                ),
                modifier = Modifier.padding(start = 16.dp)
            )
        }
    }
}
```

---

## 4. State Management Patterns

### Local UI State (Single Checkbox)

```kotlin
@Composable
fun SingleCheckboxExample() {
    var checked by remember { mutableStateOf(false) }

    CheckboxWithLabel(
        checked = checked,
        onCheckedChange = { checked = it },
        label = "Accept terms"
    )
}
```

### ViewModel State (Recommended for VoxPlanApp)

```kotlin
// In ViewModel
data class DailyUiState(
    val dailyTasks: List<DailyTask> = emptyList()
)

data class DailyTask(
    val id: Int,
    val title: String,
    val isCompleted: Boolean
)

class DailyViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(DailyUiState())
    val uiState: StateFlow<DailyUiState> = _uiState.asStateFlow()

    fun toggleTaskCompletion(taskId: Int) {
        viewModelScope.launch {
            val updatedTasks = _uiState.value.dailyTasks.map { task ->
                if (task.id == taskId) {
                    task.copy(isCompleted = !task.isCompleted)
                } else {
                    task
                }
            }
            _uiState.value = _uiState.value.copy(dailyTasks = updatedTasks)

            // Persist to database
            repository.updateTaskCompletion(taskId, !task.isCompleted)
        }
    }
}

// In Composable
@Composable
fun DailyScreen(viewModel: DailyViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    LazyColumn {
        items(uiState.dailyTasks) { task ->
            TodoCheckboxItem(
                text = task.title,
                isCompleted = task.isCompleted,
                onCheckedChange = { viewModel.toggleTaskCompletion(task.id) }
            )
        }
    }
}
```

### Room Database Pattern (for VoxPlanApp Event entity)

```kotlin
// Add isCompleted field to Event entity
@Entity(tableName = "events")
data class Event(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val goalId: Int,
    val title: String,
    val startDate: LocalDate,
    val isCompleted: Boolean = false, // Add this field
    // ... other fields
)

// Repository method
suspend fun updateEventCompletion(eventId: Int, isCompleted: Boolean) {
    eventDao.updateEventCompletion(eventId, isCompleted)
}

// DAO query
@Query("UPDATE events SET isCompleted = :isCompleted WHERE id = :eventId")
suspend fun updateEventCompletion(eventId: Int, isCompleted: Boolean)
```

---

## 5. Accessibility Considerations

### Touch Target Size

**Minimum:** 48dp x 48dp (Material Design guideline)

**Implementation:**

```kotlin
// Material 3 (automatically applies minimum size when onCheckedChange != null)
Checkbox(
    checked = isChecked,
    onCheckedChange = { isChecked = it },
    modifier = Modifier.minimumInteractiveComponentSize() // Material 3
)

// Manual sizing (Material 2 or custom)
Checkbox(
    checked = isChecked,
    onCheckedChange = { isChecked = it },
    modifier = Modifier.defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
)
```

### Semantics for Screen Readers

```kotlin
Row(
    modifier = Modifier
        .toggleable(
            value = isChecked,
            role = Role.Checkbox,
            onValueChange = { isChecked = it }
        )
        .semantics(mergeDescendants = true) {
            stateDescription = if (isChecked) "Completed" else "Not completed"
        }
) {
    Checkbox(checked = isChecked, onCheckedChange = null)
    Text("Task description")
}
```

**Key Semantics Modifiers:**

- `role = Role.Checkbox` - Declares checkbox role
- `mergeDescendants = true` - Combines child elements into single announcement
- `stateDescription` - Custom state description (instead of default "Ticked"/"Not ticked")

### Screen Reader Announcements

With proper semantics, TalkBack announces:
- **Before:** "Task description, checkbox, not completed, double tap to toggle"
- **After tap:** "Task description, checkbox, completed"

---

## 6. Best Practices Summary

### DO:

✅ Use `Modifier.toggleable()` on parent Row with `role = Role.Checkbox`
✅ Set child Checkbox `onCheckedChange = null` for accessibility
✅ Apply `minimumInteractiveComponentSize()` to maintain 48dp touch target
✅ Use Material3 `Checkbox` and `CheckboxDefaults.colors()`
✅ Combine strikethrough + color/alpha for completion feedback
✅ Manage state with StateFlow in ViewModel for persistence
✅ Add custom `stateDescription` semantics for screen readers

### DON'T:

❌ Make only the checkbox clickable (use entire row)
❌ Set both parent `toggleable` and child `onCheckedChange` (duplicate handlers)
❌ Use touch targets smaller than 48dp
❌ Rely solely on color for completion state (add strikethrough)
❌ Forget to persist completion state to database
❌ Ignore accessibility semantics

---

## 7. Common Gotchas

### Issue 1: Checkbox Won't Update

**Problem:** Checkbox appears frozen

```kotlin
// ❌ Wrong - state not hoisted
Checkbox(checked = false, onCheckedChange = { /* ignored */ })
```

**Solution:** Use state hoisting

```kotlin
// ✅ Correct
var checked by remember { mutableStateOf(false) }
Checkbox(checked = checked, onCheckedChange = { checked = it })
```

### Issue 2: Duplicate Touch Targets

**Problem:** TalkBack announces checkbox twice

```kotlin
// ❌ Wrong - both parent and child handle clicks
Row(Modifier.toggleable(value = checked, onValueChange = { checked = it })) {
    Checkbox(checked = checked, onCheckedChange = { checked = it })
}
```

**Solution:** Null the child handler

```kotlin
// ✅ Correct
Row(Modifier.toggleable(value = checked, onValueChange = { checked = it })) {
    Checkbox(checked = checked, onCheckedChange = null)
}
```

### Issue 3: Touch Target Too Small

**Problem:** Hard to tap checkbox on small screens

```kotlin
// ❌ Wrong - checkbox shrinks when onCheckedChange = null
Checkbox(checked = checked, onCheckedChange = null)
```

**Solution:** Apply minimum size

```kotlin
// ✅ Correct
Checkbox(
    checked = checked,
    onCheckedChange = null,
    modifier = Modifier.minimumInteractiveComponentSize()
)
```

### Issue 4: Inconsistent Spacing

**Problem:** Uneven spacing between list items

**Solution:** Use consistent padding values

```kotlin
// Material Design standard spacing
Row(
    modifier = Modifier
        .fillMaxWidth()
        .height(56.dp) // Standard list item height
        .padding(horizontal = 16.dp), // Standard horizontal padding
    verticalAlignment = Alignment.CenterVertically
) {
    Checkbox(...)
    Text(modifier = Modifier.padding(start = 16.dp)) // Space between checkbox and text
}
```

---

## 8. Material Design Guidelines

**Checkbox Spacing:**
- List item height: 56dp
- Horizontal padding: 16dp
- Spacing between checkbox and text: 16dp

**Touch Targets:**
- Minimum: 48dp x 48dp
- Recommended: 56dp height for list items

**Visual States:**
- **Unchecked:** Outline color
- **Checked:** Primary color with checkmark
- **Indeterminate:** Primary color with dash
- **Disabled:** Reduced opacity (38%)

**Completion Styling:**
- Strikethrough text decoration
- Reduced opacity (60% alpha)
- Gray text color
- Optional fade animation (300ms)

---

## References

**Official Documentation:**
- [Checkbox | Android Developers](https://developer.android.com/develop/ui/compose/components/checkbox)
- [Style text | Android Developers](https://developer.android.com/develop/ui/compose/text/style-text)
- [Accessibility in Jetpack Compose | Android Developers](https://developer.android.com/jetpack/compose/accessibility)
- [API defaults | Android Developers](https://developer.android.com/develop/ui/compose/accessibility/api-defaults)

**Accessibility Resources:**
- [CVS Health Android Compose Accessibility Techniques - Checkbox Controls](https://github.com/cvs-health/android-compose-accessibility-techniques/blob/main/doc/components/CheckboxControls.md)
- [Jetpack Compose Accessibility Best Practices - DEV Community](https://dev.to/carlosmonzon/jetpack-compose-accessibility-best-practices-38j0)
- [Semantics and Accessibility in Jetpack Compose: Part 2 | Medium](https://medium.com/@KaushalVasava/semantics-and-accessibility-in-jetpack-compose-part-2-be080f39de81)

**Code Examples:**
- [Checkbox - Jetpack Compose Playground](https://foso.github.io/Jetpack-Compose-Playground/material/checkbox/)
- [Android Jetpack Compose Checkbox Example | GitHub Gist](https://gist.github.com/ruyut/7958022b2237f78440d42044fab42611)
- [Composables.com - Material3 Checkbox](https://composables.com/material3/checkbox)

**Material Design:**
- [Checkbox – Material Design 3](https://m3.material.io/components/checkbox/guidelines)

**Tutorials:**
- [How to Create an Animated Strikethrough Text in Jetpack Compose | Medium](https://medium.com/@kappdev/animated-strikethrough-text-in-jetpack-compose-2350d0f105af)
- [Checkboxes in Jetpack Compose: A Comprehensive Guide | Medium](https://medium.com/@android-world/checkboxes-in-jetpack-compose-a-comprehensive-guide-85df23dbbd68)

---

## Integration with VoxPlanApp

### Recommended Approach for DailyScreen

**Database Migration:**
1. Add `isCompleted: Boolean` field to `Event` entity (version 14)
2. Add DAO method: `updateEventCompletion(eventId: Int, isCompleted: Boolean)`
3. Add Repository method to expose DAO functionality

**ViewModel Update:**
1. Add `toggleDailyCompletion(eventId: Int)` function to `DailyViewModel`
2. Update UI state to reflect completion changes reactively via Flow

**UI Update:**
1. Modify daily item composable to use `TodoCheckboxItem` pattern
2. Apply toggleable modifier to entire row with `role = Role.Checkbox`
3. Style completed items with strikethrough + reduced opacity
4. Set checkbox `onCheckedChange = null` for accessibility

**Visual Design:**
- Strikethrough text for completed tasks
- 60% opacity for completed items
- Maintain existing color scheme (avoid pure gray, use theme colors)
- Optional: fade animation on completion toggle

This implementation will align with Material Design guidelines, maintain accessibility standards, and integrate seamlessly with VoxPlanApp's existing MVVM architecture.
