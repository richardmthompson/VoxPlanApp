# Dailies Feature System

## System Context & Architecture Overview

### VoxPlanApp's Time Management Philosophy

The Dailies feature operates within VoxPlanApp's hierarchical goal-to-execution workflow. Understanding this broader context is essential for working on the Dailies functionality.

**Core Philosophy:** VoxPlanApp recognizes that goals without scheduled time rarely get accomplished. The Dailies feature serves as the critical bridge between aspirational goals and actual calendar commitments, transforming quota-based intentions into actionable daily tasks.

### The Four-Mode Workflow

The complete flow from goal creation to time tracking:

```
Goals Screen (Main)
    ↓
User creates hierarchical goals
User sets quotas (daily minutes + active days)
    ↓
DAILIES SCREEN ← ← ← YOU ARE HERE
    ↓
Automatic quota population
Manual daily task creation
Duration setting
Vertical reordering
    ↓
Schedule Screen
    ↓
Time-block scheduling
Drag-to-reschedule
    ↓
Focus Mode
    ↓
Timer with medals
Time banking
    ↓
Progress Screen
    ↓
Weekly progress tracking
Quota achievement visualization
```

### Integration with VoxPlanApp's Core Systems

```
┌─────────────────────────────────────────────────────────────┐
│                      DAILIES SYSTEM                         │
│  ┌──────────────────────────────────────────────────────┐   │
│  │                  DailyScreen.kt (628 lines)          │   │
│  │  ┌────────────┐  ┌────────────┐  ┌────────────┐     │   │
│  │  │ Date Nav   │  │ Quota      │  │ ActionMode │     │   │
│  │  │ Previous/  │  │ Progress   │  │ Reordering │     │   │
│  │  │ Next/Today │  │ Indicators │  │ Up/Down    │     │   │
│  │  └────────────┘  └────────────┘  └────────────┘     │   │
│  │                                                      │   │
│  │  ┌──────────────────────────────────────────────┐   │   │
│  │  │          Task List (LazyColumn)              │   │   │
│  │  │  - DailyTaskItem (per task)                  │   │   │
│  │  │    * Schedule button → TimeSelectionDialog   │   │   │
│  │  │    * Delete button → Delete confirmation     │   │   │
│  │  │    * Duration setting dialog                 │   │   │
│  │  └──────────────────────────────────────────────┘   │   │
│  └──────────────────────────────────────────────────────┘   │
│                                                             │
│  ┌──────────────────────────────────────────────────────┐   │
│  │               DailyViewModel.kt (190 lines)          │   │
│  │  ┌────────────────────────────────────────────────┐  │   │
│  │  │  State: DailyUiState (date, tasks, loading)   │  │   │
│  │  │  ActionMode: VerticalUp/VerticalDown          │  │   │
│  │  │  Delete Confirmation: StateFlow<Event?>       │  │   │
│  │  └────────────────────────────────────────────────┘  │   │
│  │  ┌────────────────────────────────────────────────┐  │   │
│  │  │  Reactive Pattern: snapshotFlow + flatMapLatest│  │   │
│  │  │  date → getDailiesForDate() → update UI       │  │   │
│  │  └────────────────────────────────────────────────┘  │   │
│  └──────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
         ↓                    ↓                    ↓
┌────────────────┐   ┌────────────────┐   ┌────────────────┐
│ EventRepository│   │ TodoRepository │   │QuotaRepository │
│ (Daily CRUD)   │   │ (Goal lookup)  │   │(Quota queries) │
└────────────────┘   └────────────────┘   └────────────────┘
         ↓                    ↓                    ↓
    ┌───────────────────────────────────────────────┐
    │           AppDatabase (Room v13)              │
    │  Event table (parentDailyId IS NULL = daily)  │
    └───────────────────────────────────────────────┘
```

### State Management Architecture

- **DailyViewModel**: Feature ViewModel managing daily-specific state
  - `uiState: StateFlow<DailyUiState>` - Date, tasks, loading state, error
  - `actionMode: State<ActionMode>` - Compose State for reordering (VerticalUp/VerticalDown)
  - `showDeleteConfirmation: StateFlow<Event?>` - Delete dialog state
  - Uses `ActionModeHandler` for reorder button management

- **EventRepository**: Data access for parent dailies and scheduled children
  - `getDailiesForDate(date): Flow<List<Event>>` - Query parent dailies (parentDailyId IS NULL)
  - `insertEvent(event)` - Create new daily or scheduled child
  - `updateEvent(event)` - Update duration, order, etc.
  - `deleteEvent(id)` - Delete with manual cascade to children

- **Reactive Pattern**: snapshotFlow() + flatMapLatest()
  ```kotlin
  snapshotFlow { _uiState.value.date }
      .flatMapLatest { date →
          eventRepository.getDailiesForDate(date)
      }
      .collect { events →
          _uiState.value = _uiState.value.copy(dailyTasks = events)
      }
  ```

---

## Current State: Beta / 70% Complete

**Version History:**
- **VP 3.1** (Jan 20): Dailies screen introduced (beta status)
- **VP 3.2** (Jan 29): Improved with parent/child Event relationships
- **Current**: Functional but incomplete, missing key UX features

**What Works Well:**
✅ Date navigation (previous/next/today)
✅ Quota-driven task population ("Add Quota Tasks")
✅ Visual quota progress indicators (scheduled/completed/remaining boxes)
✅ Vertical reordering via ActionMode
✅ Scheduling tasks into time blocks (creates scheduled children)
✅ Delete confirmation to prevent orphaned events
✅ Duration setting dialogs

**What's Missing:**
❌ Completion tracking (no checkboxes or "mark done")
❌ Direct focus mode access (must navigate to Schedule first)
❌ Bulk operations (multi-select, batch schedule/delete)
❌ Quick-reschedule (can't modify scheduled blocks from dailies)
❌ Smart scheduling suggestions
❌ Notifications for scheduled tasks
❌ Task persistence issues across app restarts

**Known Bugs:**
🐛 **QuickScheduleScreen.kt** (lines 26-86) - Entirely commented out, references undefined variables
🐛 **eventNeedingDuration** - Parameter handling may be fragile

---

## Dailies Feature Theory of Operation

### Purpose and Design Intent

The Dailies screen answers the question: **"What should I work on today?"**

**Design Principles:**
1. **Flexible Planning**: Tasks exist without time slots, allowing for just-in-time scheduling
2. **Quota-Driven**: Automatically populates from active goal quotas (e.g., "Programming: 90 mins Mon-Fri")
3. **Visual Progress**: Shows quota vs. scheduled vs. completed time at a glance
4. **Easy Scheduling**: One-click scheduling into time blocks
5. **Reordering**: Prioritize tasks by moving up/down

### The Parent-Child Event Pattern (CRITICAL)

The Dailies feature relies on VoxPlanApp's dual-purpose Event entity design:

**Parent Daily Event** (lives on Dailies screen):
```kotlin
Event(
    id = 123,
    goalId = 5,
    title = "Programming",
    startDate = LocalDate.of(2025, 12, 19),
    startTime = null,                    // No time slot yet
    endTime = null,                      // No time slot yet
    quotaDuration = 90,                  // From quota: 90 minutes target
    scheduledDuration = 60,              // Total scheduled so far
    completedDuration = 30,              // From time bank
    parentDailyId = null,                // NULL = this is a parent daily
    order = 0
)
```

**Scheduled Child Events** (appear on Schedule screen):
```kotlin
// Child 1
Event(
    id = 456,
    goalId = 5,
    title = "Programming",
    startDate = LocalDate.of(2025, 12, 19),
    startTime = LocalTime.of(9, 0),      // 9:00 AM
    endTime = LocalTime.of(10, 0),       // 10:00 AM
    quotaDuration = 60,                  // This block's duration
    parentDailyId = 123,                 // Points to parent daily
    order = 0
)

// Child 2
Event(
    id = 789,
    goalId = 5,
    title = "Programming",
    startDate = LocalDate.of(2025, 12, 19),
    startTime = LocalTime.of(14, 0),     // 2:00 PM
    endTime = LocalTime.of(14, 30),      // 2:30 PM
    quotaDuration = 30,
    parentDailyId = 123,                 // Same parent
    order = 1
)
```

**Database Filtering:**
```sql
-- Get parent dailies (Dailies screen)
SELECT * FROM Event
WHERE parentDailyId IS NULL
  AND startDate = ?

-- Get scheduled children (Schedule screen)
SELECT * FROM Event
WHERE parentDailyId IS NOT NULL
  AND startDate = ?
```

**Relationship Rules:**
1. **Parent Creation**: Created via "Add Quota Tasks" or manual entry
2. **Child Creation**: Created when user clicks "Schedule" button on parent
3. **Duration Tracking**: Parent's `scheduledDuration` = sum of all children's durations
4. **Deletion**: Deleting parent must delete all children (orphan prevention)
5. **Completion**: Parent's `completedDuration` comes from time bank entries for that goal/date

---

## Data Flow & Key Operations

### Operation 1: Add Quota Tasks

**User Action**: Clicks "Add Quota Tasks" button

**Flow:**
```
DailyScreen → viewModel.addQuotaTasks()
    ↓
DailyViewModel.addQuotaTasks():
    1. Get current date from uiState
    2. quotaRepository.getAllActiveQuotas(date).first()
       → Queries quotas where activeDays[dayOfWeek] == '1'
    3. For each quota:
       a. todoRepository.getItemStream(quota.goalId).first()
       b. Create Event:
          - goalId, title from goal
          - startDate = current date
          - quotaDuration = quota.dailyMinutes
          - scheduledDuration = 0
          - completedDuration = 0
          - parentDailyId = null
       c. eventRepository.insertEvent(event)
    ↓
Database: INSERT INTO Event (...)
    ↓
Flow emission: getDailiesForDate() emits new list
    ↓
collect() in ViewModel init block
    ↓
_uiState.value = copy(dailyTasks = newEvents)
    ↓
UI recomposes with new tasks
```

**Code Reference**: `DailyViewModel.kt:112-137`

---

### Operation 2: Schedule Task into Time Block

**User Action**: Clicks "Schedule" button → Time selection dialog → Confirms

**Flow:**
```
DailyScreen → TimeSelectionDialog → onConfirm
    ↓
viewModel.scheduleTask(task, startTime, endTime)
    ↓
DailyViewModel.scheduleTask():
    1. Calculate duration = minutes between start and end
    2. Create scheduled child event:
       - Copy parent task
       - Set startTime and endTime
       - Set parentDailyId = task.id
       - Set quotaDuration = duration
    3. Update parent task:
       - scheduledDuration += duration
    4. eventRepository.insertEvent(scheduledTask)
    5. eventRepository.updateEvent(updatedParent)
    ↓
Database:
    - INSERT scheduled child
    - UPDATE parent with new scheduledDuration
    ↓
Two flow emissions:
    - getDailiesForDate() → parent updated
    - getScheduledBlocksForDate() → child appears on Schedule
    ↓
Dailies UI shows updated progress indicators
Schedule UI shows new time block
```

**Code Reference**: `DailyViewModel.kt:139-159`

**Visual Effect**: Progress indicator boxes update:
```
Before: [⬜⬜⬜] (0/90 mins scheduled)
After:  [🟦🟦⬜] (60/90 mins scheduled)
```

---

### Operation 3: Delete Task with Cascade

**User Action**: Clicks delete button → Confirmation dialog → "Delete All"

**Flow:**
```
DailyScreen → viewModel.deleteTask(task)
    ↓
DailyViewModel.deleteTask():
    _showDeleteConfirmation.value = task
    ↓
UI: AlertDialog appears with warning
    ↓
User confirms → viewModel.confirmDelete(task)
    ↓
DailyViewModel.confirmDelete():
    1. Get child events:
       eventRepository.getEventsWithParentId(task.id).first()
    2. Delete all children:
       childEvents.forEach { eventRepository.deleteEvent(it.id) }
    3. Delete parent:
       eventRepository.deleteEvent(task.id)
    4. Clear dialog:
       _showDeleteConfirmation.value = null
    ↓
Database:
    - DELETE FROM Event WHERE id IN (child1, child2, ...)
    - DELETE FROM Event WHERE id = parentId
    ↓
Flow emissions:
    - getDailiesForDate() → parent removed
    - getScheduledBlocksForDate() → children removed
    ↓
Dailies UI removes task
Schedule UI removes all associated time blocks
```

**Code Reference**: `DailyViewModel.kt:171-189`

**Why Manual Cascade?**: Event table has Foreign Key with CASCADE, but ViewModel explicitly deletes children for:
1. Logging/tracking purposes
2. Potential UI feedback
3. Explicit control over deletion order

---

### Operation 4: Vertical Reordering

**User Action**: Clicks "Vertical Up/Down" button → Clicks task to reorder

**Flow:**
```
DailyScreen → ActionButton clicked
    ↓
viewModel.actionModeHandler.toggleUpActive() or toggleDownActive()
    ↓
ActionModeHandler: _actionMode.value = ActionMode.VerticalUp
    ↓
UI: Button highlights, actionMode observable changes
    ↓
User clicks task → viewModel.reorderTask(task)
    ↓
DailyViewModel.reorderTask():
    1. Get current task list and find task index
    2. Calculate new index based on actionMode:
       - VerticalUp: index - 1 (coerced to 0)
       - VerticalDown: index + 1 (coerced to lastIndex)
    3. Reorder list in memory:
       - Remove task from old position
       - Insert task at new position
    4. Update all affected tasks in database:
       forEachIndexed { index, event →
           eventRepository.updateEvent(event.copy(order = index))
       }
    5. Update UI state immediately:
       _uiState.value = copy(dailyTasks = reorderedList)
    ↓
Database: UPDATE Event SET order = ? WHERE id = ? (for each task)
    ↓
UI recomposes with new task order
```

**Code Reference**: `DailyViewModel.kt:85-110`

**Note**: Optimistic UI update (immediate) with database write (async)

---

## Quota Progress Indicators

### Visual Design

Each daily task displays quota progress as colored boxes:

```
[🟩🟩🟦⬜⬜] Programming (60/90 mins)
 │  │  │  │
 │  │  │  └─ Remaining (white/empty)
 │  │  └──── Scheduled (blue)
 │  └─────── Completed (green)
 └────────── Completed (green)
```

**Box Calculation:**
```kotlin
// Assuming 5 boxes represent quota duration
val totalBoxes = 5
val minutesPerBox = quotaDuration / totalBoxes

val completedBoxes = (completedDuration / minutesPerBox).toInt()
val scheduledBoxes = (scheduledDuration / minutesPerBox).toInt()
val remainingBoxes = totalBoxes - completedBoxes - scheduledBoxes
```

### Data Sources

**completedDuration**: From TimeBank table
```sql
SELECT SUM(duration) FROM TimeBank
WHERE goalId = ? AND date = ?
```

**scheduledDuration**: Tracked in parent Event
- Updated when child scheduled events are created
- Represents total minutes allocated in time blocks

**quotaDuration**: From Quota table via goal
- User-defined daily target
- Used as denominator for progress calculation

### Integration with Other Features

**Time Banking (Focus Mode)**:
1. User completes focus session
2. FocusViewModel.bankTime() creates TimeBank entry
3. Entry: `TimeBank(goalId, date, duration)`
4. Next time Dailies loads: completedDuration reflects banked time
5. Green boxes appear in progress indicator

**Scheduling (Schedule Screen)**:
1. User drags event or adjusts duration in Schedule
2. SchedulerViewModel updates Event
3. Parent Event's scheduledDuration recalculated
4. Dailies screen shows updated blue boxes

---

## Integration Points with Other Features

### 1. Goals Screen (Main) Integration

**Data Flow: Goals → Dailies**

```
MainScreen (Goal hierarchy)
    ↓
User sets quota on goal:
    - GoalEditScreen
    - QuotaSettings component
    - quotaRepository.insert(quota)
    ↓
Quota stored: Quota(goalId, dailyMinutes, activeDays)
    ↓
User navigates to Dailies
    ↓
DailyScreen: "Add Quota Tasks"
    ↓
quotaRepository.getAllActiveQuotas(date)
    ↓
Creates parent daily Event for each active quota
```

**Navigation Path**:
```kotlin
// From MainScreen to Dailies
navController.navigate(VoxPlanScreen.Daily.route(date))

// With new event parameter (to pop duration dialog)
navController.navigate(VoxPlanScreen.Daily.route(date, eventId))
```

**Shared State**: None (Dailies reads from repositories, doesn't use SharedViewModel breadcrumbs)

---

### 2. Schedule Screen Integration

**Data Flow: Dailies → Schedule**

```
DailyScreen: User schedules task
    ↓
scheduleTask() creates child Event with:
    - parentDailyId = parent.id
    - startTime, endTime populated
    ↓
EventRepository.insertEvent()
    ↓
Schedule Screen queries:
    getScheduledBlocksForDate()
    WHERE parentDailyId IS NOT NULL
    ↓
Child event appears on Schedule as time block
```

**Bidirectional Relationship**:

From Schedule → Affects Dailies:
1. User adjusts event duration in Schedule
2. SchedulerViewModel updates child Event
3. Parent Event's scheduledDuration needs recalculation
4. **CURRENT GAP**: Parent's scheduledDuration NOT automatically updated
5. Dailies progress indicator may be stale until manual refresh

**Missing Sync Logic**:
```kotlin
// When child event updated in Schedule:
// 1. Get parent event
// 2. Recalculate scheduledDuration = sum of all children
// 3. Update parent event
// Currently NOT implemented
```

---

### 3. Focus Mode Integration

**Current State: INDIRECT (via Schedule)**

```
DailyScreen (parent daily)
    ↓
User schedules → child event created
    ↓
Schedule Screen shows time block
    ↓
User clicks time block → "Focus Mode" button
    ↓
FocusModeScreen(goalId, eventId)
    ↓
Timer runs → medals earned
    ↓
User banks time → TimeBank entry created
    ↓
Next time Dailies loads:
    completedDuration = sum of TimeBank entries
    ↓
Green progress boxes appear
```

**Missing: Direct Focus Access**

Desired flow (NOT implemented):
```
DailyScreen (parent daily)
    ↓
"Start Focus" button → FocusModeScreen(goalId)
    ↓
Timer runs → time banked
    ↓
Immediate feedback on Dailies screen
```

**Implementation Gap**:
- No "Focus Mode" button on DailyTaskItem
- No direct navigation from Dailies to Focus
- Must go through Schedule intermediary

---

### 4. Progress Screen Integration

**Data Flow: Dailies → Progress**

```
DailyScreen operations:
    - Add quota tasks
    - Schedule tasks
    - Complete tasks via Focus Mode
    ↓
Data written to:
    - Event table (parent dailies)
    - TimeBank table (completed time)
    ↓
Progress Screen queries:
    - quotaRepository.getAllQuotas()
    - timeBankRepository.getEntriesForDateRange(weekStart, weekEnd)
    ↓
Aggregates time by goal by day
    ↓
Shows weekly progress grid with diamonds for quota achievement
```

**Visual Feedback Loop**:
1. User sees quota target on Dailies (e.g., "90 mins")
2. User schedules and completes work
3. Progress screen confirms achievement with diamond icon
4. User gains confidence in quota-based planning

---

## UI Components Breakdown

### DailyScreen.kt (628 lines)

**Primary Composable**:
```kotlin
@Composable
fun DailyScreen(
    modifier: Modifier = Modifier,
    viewModel: DailyViewModel = viewModel(factory = AppViewModelProvider.Factory)
)
```

**Component Hierarchy**:
```
Scaffold
├─ topBar:
│  ├─ DailyHeader (date navigation)
│  └─ VoxPlanTopAppBar
│     └─ DailyActionButtons (Today, Add Quotas, Reorder)
└─ content:
   ├─ Loading indicator (if isLoading)
   ├─ Error message (if error != null)
   └─ DailyTaskList
      └─ LazyColumn
         └─ items(tasks) → DailyTaskItem

AlertDialog (showDeleteConfirmation)
```

### DailyHeader Composable

**Purpose**: Date navigation and relative date display

**Features**:
- Previous day button (left arrow)
- Relative date text ("Today", "Tomorrow", "Monday Dec 23")
- Next day button (right arrow)

**Code Reference**: `DailyScreen.kt:137-180` (approx)

**Relative Date Logic**:
```kotlin
fun getRelativeDayText(date: LocalDate): String {
    val today = LocalDate.now()
    val tomorrow = today.plusDays(1)
    val yesterday = today.minusDays(1)

    return when {
        date == today -> "Today"
        date == tomorrow -> "Tomorrow"
        date == yesterday -> "Yesterday"
        // Within this week
        date.isAfter(today) && date.isBefore(today.plusDays(7)) ->
            date.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault())
        // Absolute date
        else -> date.format(DateTimeFormatter.ofPattern("MMM d, yyyy"))
    }
}
```

### DailyActionButtons Composable

**Purpose**: Action buttons for bulk operations and reordering

**Buttons**:
1. **Today**: Jump to current date
2. **Add Quotas**: Populate tasks from active quotas
3. **Vertical Up**: Reorder mode (move task up)
4. **Vertical Down**: Reorder mode (move task down)

**Visual State**: Activated buttons show highlighted border/background

**Code Reference**: `DailyScreen.kt:182-250` (approx)

### DailyTaskList Composable

**Purpose**: Scrollable list of daily tasks

```kotlin
@Composable
fun DailyTaskList(
    tasks: List<Event>,
    eventNeedingDuration: Int?,
    onTaskReorder: (Event) -> Unit,
    onTaskSchedule: (Event, LocalTime, LocalTime) -> Unit,
    onTaskDuration: (Event, Int) -> Unit,
    onTaskDelete: (Event) -> Unit,
    actionMode: ActionMode
)
```

**Implementation**:
```kotlin
LazyColumn {
    items(tasks, key = { it.id }) { task →
        DailyTaskItem(
            task = task,
            showDurationDialog = (task.id == eventNeedingDuration),
            onReorder = { onTaskReorder(task) },
            onSchedule = { start, end → onTaskSchedule(task, start, end) },
            onDuration = { duration → onTaskDuration(task, duration) },
            onDelete = { onTaskDelete(task) },
            actionMode = actionMode
        )
    }
}
```

### DailyTaskItem Composable

**Purpose**: Individual task card with actions

**Layout**:
```
┌─────────────────────────────────────────┐
│ [Reorder if ActionMode] Task Title      │ ← Clickable for reorder
│                                         │
│ Quota Progress: [🟩🟩🟦⬜⬜]            │
│ 60/90 minutes                           │
│                                         │
│ [Schedule Button] [Delete Button]      │
└─────────────────────────────────────────┘
```

**Conditional Rendering**:
- **ActionMode Active**: Entire card clickable for reordering
- **ActionMode Normal**: Buttons at bottom for schedule/delete

**Dialogs**:
- **TimeSelectionDialog**: Hour/minute pickers for start and end time
- **DurationSelectionDialog**: If `showDurationDialog == true`, pops automatically

**Code Reference**: `DailyScreen.kt:300-450` (approx)

### Quota Progress Indicator Composable

**Purpose**: Visual representation of quota completion

**Implementation**:
```kotlin
@Composable
fun QuotaProgressIndicator(
    quotaDuration: Int?,
    scheduledDuration: Int?,
    completedDuration: Int?
) {
    val totalBoxes = 5
    val minutesPerBox = (quotaDuration ?: 0) / totalBoxes.toFloat()

    val completed = ((completedDuration ?: 0) / minutesPerBox).toInt()
    val scheduled = ((scheduledDuration ?: 0) / minutesPerBox).toInt()
    val remaining = totalBoxes - completed - scheduled

    Row {
        repeat(completed) { Box(color = Green) }      // 🟩
        repeat(scheduled) { Box(color = Blue) }       // 🟦
        repeat(remaining) { Box(color = Gray) }       // ⬜
    }
}
```

**Visual Design**: 5 boxes, each representing 1/5 of quota duration

---

## Known Issues and Limitations

### Critical Issues

**1. No Completion Tracking**

**Problem**: Daily tasks cannot be marked as "done"

**Impact**:
- Users can't check off completed tasks
- No visual indication of task completion
- Completed tasks remain in list indefinitely

**Desired Behavior**:
- Checkbox to mark task complete
- Completed tasks show strikethrough or gray out
- Optional: Hide completed tasks after date changes

**Implementation Requirements**:
```kotlin
// Add to Event entity
val completed: Boolean = false

// Update DailyTaskItem
Checkbox(
    checked = task.completed,
    onCheckedChange = { isChecked →
        onTaskComplete(task, isChecked)
    }
)

// Add to DailyViewModel
fun completeTask(task: Event, completed: Boolean) {
    viewModelScope.launch {
        eventRepository.updateEvent(task.copy(completed = completed))
    }
}
```

---

**2. No Direct Focus Mode Access**

**Problem**: Must navigate to Schedule screen first to enter Focus Mode

**Impact**:
- Extra navigation steps (Dailies → Schedule → Focus)
- User friction in starting focused work
- Breaks mental flow

**Desired Behavior**:
- "Start Focus" button on each daily task
- Direct navigation: Dailies → Focus Mode
- Option: Auto-create scheduled block when focus completes

**Implementation Requirements**:
```kotlin
// Add to DailyTaskItem
Button(onClick = { onStartFocus(task) }) {
    Icon(Icons.Default.Timer)
    Text("Focus")
}

// Add to DailyScreen
fun onStartFocus(task: Event) {
    navController.navigate(
        VoxPlanScreen.FocusMode.route(goalId = task.goalId)
    )
}
```

---

**3. QuickScheduleScreen Entirely Commented Out**

**Location**: `app/src/main/java/com/voxplanapp/ui/main/QuickScheduleScreen.kt:26-86`

**Problem**: Intended feature for fast scheduling from Main screen, but code is broken

**Status**: All code commented out, references undefined variables

**Options**:
1. **Fix and implement**: Uncomment, fix variable references, integrate properly
2. **Remove entirely**: Delete file, remove from navigation

**Decision Required**: Determine if quick-scheduling from Main screen is still desired

---

**4. Scheduled Duration Sync Issue**

**Problem**: When child event updated in Schedule, parent's `scheduledDuration` not recalculated

**Impact**:
- Progress indicators may show stale data
- User adjusts event duration in Schedule, but Dailies doesn't reflect change

**Example**:
1. Daily task: "Programming" quota = 90 mins
2. User schedules 9:00-10:00 (60 mins)
3. Dailies shows: [🟦🟦⬜] (60/90)
4. User adjusts in Schedule: 9:00-10:30 (90 mins)
5. Dailies still shows: [🟦🟦⬜] (60/90) ← STALE

**Implementation Required**:
```kotlin
// In SchedulerViewModel when updating child event
fun updateEvent(event: Event) {
    viewModelScope.launch {
        // Update child
        eventRepository.updateEvent(event)

        // Recalculate parent's scheduledDuration
        if (event.parentDailyId != null) {
            val parent = eventRepository.getEvent(event.parentDailyId).first()
            val allChildren = eventRepository.getEventsWithParentId(event.parentDailyId).first()
            val totalScheduled = allChildren.sumOf {
                ChronoUnit.MINUTES.between(it.startTime, it.endTime).toInt()
            }
            eventRepository.updateEvent(
                parent.copy(scheduledDuration = totalScheduled)
            )
        }
    }
}
```

---

### Missing Features

**5. Bulk Operations**

**Problem**: Cannot select multiple tasks for batch operations

**Desired Features**:
- Multi-select mode (checkboxes)
- Batch delete selected tasks
- Batch schedule selected tasks (e.g., "Schedule all between 9-5")
- Batch reorder (move selected to top/bottom)

**UX Pattern**:
```
[Select Mode Button]
    ↓
Checkboxes appear on each task
    ↓
User selects multiple tasks
    ↓
Action bar appears: [Delete All] [Schedule All] [Cancel]
```

---

**6. Smart Scheduling Suggestions**

**Problem**: No AI or algorithmic suggestions for optimal scheduling

**Desired Features**:
- Suggest time blocks based on:
  - Available gaps in schedule
  - Quota requirements
  - Historical patterns (when user typically works on this goal)
  - Energy levels (morning vs. afternoon tasks)

**Example**:
```
"Programming" - 90 mins needed
Suggestions:
  ✓ 9:00-10:30 AM (90 mins available)
  ✓ 2:00-3:30 PM (90 mins available)
  ✗ 11:00-12:00 (only 60 mins, conflict at noon)
```

---

**7. Notifications and Reminders**

**Problem**: No notifications when scheduled dailies are about to start

**Desired Features**:
- Notification 5 mins before scheduled block
- Reminder if daily task unscheduled by end of day
- Weekly summary: "You completed 4/5 daily quotas this week"

**Android Integration Required**:
- WorkManager for scheduled notifications
- NotificationChannel setup
- User permission handling (Android 13+)

---

## Testing Considerations

### Unit Tests (ViewModel Logic)

**DailyViewModel Tests**:

```kotlin
class DailyViewModelTest {
    @Test
    fun `addQuotaTasks creates daily events for active quotas`() {
        // Setup: Mock repositories with test quotas
        // Action: Call addQuotaTasks()
        // Assert: eventRepository.insertEvent called for each active quota
    }

    @Test
    fun `scheduleTask creates child event and updates parent`() {
        // Setup: Mock parent daily task
        // Action: scheduleTask(task, 9AM, 10AM)
        // Assert:
        //   - Child event inserted with parentDailyId = task.id
        //   - Parent event updated with scheduledDuration += 60
    }

    @Test
    fun `deleteTask cascades to all children`() {
        // Setup: Mock parent with 3 children
        // Action: confirmDelete(parent)
        // Assert: All 3 children deleted before parent deleted
    }

    @Test
    fun `reorderTask moves task up correctly`() {
        // Setup: List of 5 tasks
        // Action: actionMode = VerticalUp, reorderTask(task at index 2)
        // Assert: Task moved to index 1, all order fields updated
    }
}
```

### Integration Tests (Database + Repository)

```kotlin
class DailiesIntegrationTest {
    @Test
    fun `quota-to-daily flow end-to-end`() {
        // 1. Insert goal
        // 2. Insert quota for goal
        // 3. Call addQuotaTasks for today
        // 4. Query getDailiesForDate
        // Assert: Daily event exists with quotaDuration from quota
    }

    @Test
    fun `schedule-then-delete orphan prevention`() {
        // 1. Create parent daily
        // 2. Create 2 scheduled children
        // 3. Delete parent (with cascade)
        // 4. Query children
        // Assert: No orphaned children exist
    }
}
```

### UI Tests (Compose)

```kotlin
class DailyScreenTest {
    @Test
    fun `add quota tasks button creates daily tasks`() {
        // Setup: ComposeTestRule with DailyScreen
        // Action: Click "Add Quota Tasks" button
        // Assert: Task items appear in list
    }

    @Test
    fun `schedule dialog sets time correctly`() {
        // Setup: DailyScreen with task
        // Action:
        //   1. Click "Schedule" button
        //   2. Set start time to 9:00
        //   3. Set end time to 10:00
        //   4. Confirm
        // Assert: ViewModel.scheduleTask called with correct times
    }
}
```

---

## Future Enhancements Roadmap

### Phase 1: Complete Core Functionality (MVP)

**Priority: HIGH**

1. **Add Completion Tracking**
   - Checkbox UI component
   - `completed` field in Event entity
   - Database migration
   - Visual feedback (strikethrough)
   - Estimated effort: 2-3 hours

2. **Fix Scheduled Duration Sync**
   - Add recalculation logic to SchedulerViewModel
   - Test parent-child duration consistency
   - Estimated effort: 3-4 hours

3. **Add Direct Focus Mode Button**
   - "Start Focus" button on DailyTaskItem
   - Navigation to FocusMode with goalId
   - Estimated effort: 1-2 hours

### Phase 2: User Experience Polish

**Priority: MEDIUM**

4. **Bulk Operations**
   - Multi-select mode
   - Batch delete
   - Batch schedule
   - Estimated effort: 8-10 hours

5. **Quick Reschedule**
   - Show scheduled blocks on daily task
   - Edit/delete scheduled blocks from Dailies
   - Estimated effort: 6-8 hours

6. **Better Error Handling**
   - Network error retry logic
   - Database error recovery
   - User-friendly error messages
   - Estimated effort: 4-5 hours

### Phase 3: Advanced Features

**Priority: LOW**

7. **Smart Scheduling**
   - Algorithm for optimal time slot suggestions
   - Machine learning based on historical patterns
   - Estimated effort: 20-30 hours

8. **Notifications**
   - WorkManager integration
   - Reminder system
   - Weekly summary notifications
   - Estimated effort: 12-15 hours

9. **Task Templates**
   - Save recurring daily patterns
   - One-click daily setup
   - Estimated effort: 8-10 hours

---

## Development Guidelines for Dailies Feature

### Adding a New Field to Daily Tasks

**Example: Adding `priority` field**

**Step 1**: Update Event entity
```kotlin
// data/Event.kt
data class Event(
    // ...
    val priority: Int = 0,  // 0=normal, 1=high, 2=urgent
)
```

**Step 2**: Create database migration
```kotlin
// data/AppDatabase.kt
val MIGRATION_13_14 = object : Migration(13, 14) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE Event ADD COLUMN priority INTEGER NOT NULL DEFAULT 0")
    }
}
```

**Step 3**: Update ViewModel
```kotlin
// ui/daily/DailyViewModel.kt
fun setPriority(task: Event, priority: Int) {
    viewModelScope.launch {
        eventRepository.updateEvent(task.copy(priority = priority))
    }
}
```

**Step 4**: Update UI
```kotlin
// ui/daily/DailyScreen.kt - in DailyTaskItem
PrioritySelector(
    priority = task.priority,
    onPriorityChange = { newPriority →
        onSetPriority(task, newPriority)
    }
)
```

### Best Practices

**DO**:
- ✅ Use `snapshotFlow` + `flatMapLatest` for date-driven queries
- ✅ Handle parent-child relationships explicitly
- ✅ Provide delete confirmation for destructive actions
- ✅ Update parent's `scheduledDuration` when children change
- ✅ Use `ActionModeHandler` for consistent reordering UX

**DON'T**:
- ❌ Query database on main thread (use `viewModelScope.launch`)
- ❌ Delete parent without checking for orphaned children
- ❌ Update UI state without Flow observation
- ❌ Hardcode colors (use theme constants from `ui/constants/`)
- ❌ Forget to increment database version after migration

---

## References and Related Documents

**Project Context**:
- `agent/context/project_context.md` - High-level project vision and workflows
- `agent/context/codebase_context.md` - Technical implementation guide

**Generated Documentation**:
- `docs/LLM-Generated/INCOMPLETE_FEATURES.md` - Detailed feature analysis
- `docs/LLM-Generated/ARCHITECTURE_DOCUMENTATION_SUMMARY.txt` - Architecture overview

**Code Files**:
- `app/src/main/java/com/voxplanapp/ui/daily/DailyScreen.kt` (628 lines)
- `app/src/main/java/com/voxplanapp/ui/daily/DailyViewModel.kt` (190 lines)
- `app/src/main/java/com/voxplanapp/data/Event.kt` - Event entity
- `app/src/main/java/com/voxplanapp/data/EventRepository.kt` - Event data access

**Related Features**:
- Goals Screen (Main): Quota setup
- Schedule Screen: Time block management
- Focus Mode: Time tracking
- Progress Screen: Weekly progress visualization

---

**Document Version**: 1.0
**Last Updated**: December 19, 2025
**Status**: Beta (70% complete)
**Next Review**: After Phase 1 enhancements
