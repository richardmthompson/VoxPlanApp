# VoxPlanApp - Incomplete Features Documentation

## Overview
This document provides a comprehensive analysis of two major incomplete features in VoxPlanApp: **Dailies** and **Scheduling**. Both features have partial implementations (introduced in versions 3.1-3.2) but are marked as incomplete/beta and require further development.

---

## 1. DAILIES FEATURE

### Current State: BETA / PARTIALLY IMPLEMENTED
**Version History:**
- VP 3.1 (Jan 20): Dailies screen introduced (in beta)
- VP 3.2 (Jan 29): Dailies improved with parent/child Events

### Intended Purpose
The Dailies feature is designed to:
- Create a daily task list for the current day
- Automatically populate tasks from quota-based goals
- Allow scheduling of daily tasks into specific time blocks
- Track progress toward daily quotas (duration completed vs quota)
- Manage parent-child relationships between daily tasks and scheduled events

### Implemented Components

#### 1. **Dailies Screen UI** (`DailyScreen.kt`)
**Path:** `/Users/richardthompson/StudioProjects/VoxPlanApp/app/src/main/java/com/voxplanapp/ui/daily/DailyScreen.kt`

**Implemented Features:**
- ✓ Daily header with date navigation (previous/next day buttons)
- ✓ "Go to Today" button
- ✓ "Add Quota Tasks" button to populate dailies from active quotas
- ✓ Vertical reorder buttons (up/down arrows for ActionMode)
- ✓ Task list display with cards
- ✓ Schedule button for each task (opens TimeSelectionDialog)
- ✓ Delete button for each task
- ✓ Duration setting dialog for new tasks
- ✓ Time selection dialog for scheduling tasks
- ✓ Quota progress indicators (colored boxes showing: completed/scheduled/pending)
- ✓ Delete confirmation dialog to prevent orphaned events
- ✓ Relative date display ("today", "tomorrow", "this Monday", etc.)

**UI Components:**
```
DailyScreen
├─ DailyHeader (date navigation)
├─ DailyActionButtons (Today, AddQuotas, Reorder Up/Down)
├─ DailyTaskList (scrollable list of tasks)
│  └─ DailyTaskItem (individual task card)
│     ├─ Task title
│     ├─ QuotaProgressIndicator (visual boxes)
│     ├─ Schedule button
│     ├─ Delete button
│     └─ Dialogs:
│        ├─ TimeSelectionDialog
│        └─ DurationSelectionDialog
└─ Delete confirmation dialog
```

#### 2. **Dailies ViewModel** (`DailyViewModel.kt`)
**Path:** `/Users/richardthompson/StudioProjects/VoxPlanApp/app/src/main/java/com/voxplanapp/ui/daily/DailyViewModel.kt`

**State Management:**
```kotlin
data class DailyUiState(
    val date: LocalDate = LocalDate.now(),
    val dailyTasks: List<Event> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val eventNeedingDuration: Int? = null  // Triggers duration dialog on entry
)
```

**Implemented Functions:**
1. **updateDate(newDate: LocalDate)** - Changes current date
2. **reorderTask(task: Event)** - Moves tasks up/down in list
3. **addQuotaTasks()** - Creates daily events from active quotas for a date
4. **scheduleTask(task: Event, startTime: LocalTime, endTime: LocalTime)** - Creates scheduled event blocks
5. **setTaskDuration(task: Event, duration: Int)** - Sets estimated duration
6. **deleteTask(task: Event)** - Marks task for deletion (shows confirmation)
7. **confirmDelete(task: Event)** - Deletes task and all child scheduled events
8. **cancelDelete()** - Cancels deletion

**Data Flow:**
- Uses EventRepository to query dailies for a specific date
- Uses QuotaRepository to get active quotas
- Uses TodoRepository to map goals to quotas
- Maintains ActionMode for reordering operations

#### 3. **Data Model Integration**
**Event Entity Fields Used:**
```kotlin
val id: Int              // Event ID
val goalId: Int          // Associated goal
val title: String        // Task title
val startTime: LocalTime? = null     // Scheduled start (null for daily)
val endTime: LocalTime? = null       // Scheduled end (null for daily)
val startDate: LocalDate // Daily date
val quotaDuration: Int?  // Duration from quota (minutes)
val scheduledDuration: Int? = null   // Total scheduled time (minutes)
val completedDuration: Int? = null   // Time bank entries (minutes)
val parentDailyId: Int? = null       // Non-null = scheduled event, null = daily task
val order: Int = 0                   // Ordering within the day
```

**Database Queries:**
- `getDailiesForDate(date)` - Gets all dailies (parentDailyId IS NULL) for a date
- `getEventsWithParentId(parentId)` - Gets scheduled blocks for a daily

### What's Missing / Incomplete

#### 1. **Incomplete Features:**
- ❌ Persistence of daily tasks across app restarts
  - Tasks are created but may not maintain state between sessions
- ❌ Completion tracking for individual daily tasks
  - No UI to mark a daily as "done" or check off tasks
- ❌ Visual feedback when task completes
  - No checkmark, strikethrough, or visual completion indicator
- ❌ Re-scheduling already scheduled tasks
  - Can't modify scheduled time blocks from daily screen
- ❌ Bulk operations on daily tasks
  - Can't select multiple and delete/schedule together
- ❌ Integration with Focus Mode from Dailies screen
  - Must go to Schedule screen first to enter focus mode
- ❌ Notifications/reminders for scheduled dailies
  - No alerts when daily tasks are scheduled to start

#### 2. **Known Issues:**
- ⚠️ QuickScheduleScreen completely commented out (line 26-86)
  - Was intended as fast scheduling from main screen
  - References undefined variables (e.g., `goal` not passed as parameter)
- ⚠️ eventNeedingDuration parameter handling
  - Attempts to pop duration dialog on entry, but logic may be fragile
- ⚠️ ActionMode reordering doesn't auto-deselect
  - Developer note in process doc: "do not auto de-select vertical up and down movers"
- ⚠️ No error handling for quota lookup failures
  - If goal/quota lookup fails, silently continues

#### 3. **Design Gaps:**
- The daily screen expects manual scheduling of quotas
- No smart scheduling algorithm to suggest optimal times
- No conflict detection when scheduling overlapping times
- Parent/child relationship management is basic (delete parent = delete all children)

### Integration Points with Other Features

1. **Quotas Integration:**
   - `addQuotaTasks()` uses QuotaRepository to get active quotas
   - Populates daily list from Quota.dailyMinutes
   - Display boxes show quota vs. scheduled vs. completed time

2. **Schedule Integration:**
   - Scheduled events created in Daily Screen have `parentDailyId` pointing to daily task
   - Schedule Screen filters to show only child events (parentDailyId != NULL)
   - Deleting scheduled event prompts to delete parent daily if last scheduled block

3. **Focus Mode Integration:**
   - Must navigate to Schedule Screen first
   - From Schedule, can enter Focus Mode via event action icons
   - No direct Focus Mode entry from Daily Screen

4. **Main Screen:**
   - MainScreen can navigate to Daily Screen with newEventId parameter
   - This triggers duration dialog to pop automatically
   - Used when user creates goal and wants to add to today's dailies

---

## 2. SCHEDULING FEATURE

### Current State: PARTIALLY IMPLEMENTED
**Version History:**
- VP 2.1 (Jul 28): Schedule screen introduced with sample event
- VP 2.2 (Aug 7): Can add goals to schedule for today
- VP 2.3 (Aug 13): Scheduled events can select, drag, and action icons display on select
- VP 2.5 (Aug 23): Focus Mode accessible from Schedule
- Current: Functional but missing several intended features

### Intended Purpose
The Scheduling feature is designed to:
- Display time-based calendar view of the day (1-24 hours)
- Allow dragging events to reschedule them
- Show overlapping events intelligently
- Provide quick access to Focus Mode from schedule
- Manage event duration adjustments in-place
- Support creating scheduled events from daily tasks

### Implemented Components

#### 1. **Day Schedule Screen** (`DaySchedule.kt`)
**Path:** `/Users/richardthompson/StudioProjects/VoxPlanApp/app/src/main/java/com/voxplanapp/ui/calendar/DaySchedule.kt`

**Architecture:**
```
DaySchedule (Main container)
├─ DayHeader (date nav + Today button)
├─ ScheduleSideBar (hourly labels)
└─ BasicSchedule (event canvas)
   ├─ Grid lines (hourly + half-hourly)
   ├─ EventBox components (draggable)
   │  └─ ScheduleParams (CompositionLocal)
   └─ EventActions (toolbar on select)
      ├─ Focus Mode button
      ├─ Duration decrease (-15 min)
      ├─ Duration increase (+15 min)
      └─ Delete button
```

**Layout Parameters:**
```kotlin
data class ScheduleParams(
    val hourHeight: Dp,      // 48.dp - visual size of 1-hour block
    val startHour: Int,      // 1 - display starts at 1 AM
    val endHour: Int         // 24 - display ends at midnight
)
```

**Implemented Features:**
- ✓ Time-based visual grid (1-24 hour display)
- ✓ Hourly and half-hourly grid lines
- ✓ Event boxes positioned by start/end times
- ✓ Event selection/deselection
- ✓ Drag to reschedule (snaps to 15-minute intervals)
- ✓ Action toolbar (Focus, +/- duration, delete)
- ✓ Overlapping event detection and column layout
- ✓ Scrollable view (defaults to 6 AM position)
- ✓ Date navigation (previous/next/today buttons)
- ✓ Delete with parent daily confirmation dialog
- ✓ Event text display with basic styling

**Key Functions:**
1. **findOverlappingEvents()** - Calculates layout info for overlapping events
2. **calculateEventPosition()** - Converts time to screen Y position
3. **EventBox.detectDragGestures()** - Handles dragging and time snapping
4. **roundToNearest15Minutes()** - Snaps dragged offset to 15-min intervals
5. **EventActions()** - Displays toolbar with duration/focus controls

#### 2. **Scheduler ViewModel** (`SchedulerViewModel.kt`)
**Path:** `/Users/richardthompson/StudioProjects/VoxPlanApp/app/src/main/java/com/voxplanapp/ui/calendar/SchedulerViewModel.kt`

**State Management:**
```kotlin
val currentDate: StateFlow<LocalDate>           // Currently displayed date
val eventsForCurrentDate: StateFlow<List<Event>> // Filtered scheduled events
val showDeleteParentDialog: StateFlow<Event?>   // Delete confirmation
```

**Implemented Functions:**
1. **updateDate(newDate: LocalDate)** - Changes displayed date
2. **getEventsForDate(date: LocalDate)** - Query flow for events
3. **addEvent(event: Event)** - Insert new event
4. **updateEvent(event: Event)** - Update event (both DB and UI state)
5. **deleteEvent(event: Event)** - Delete with parent daily check
6. **confirmDeleteWithParent()** - Delete both event and parent daily
7. **confirmDeleteChildOnly()** - Delete only the scheduled event
8. **cancelDelete()** - Dismiss delete dialog

**Event Filtering:**
- Only shows events with `parentDailyId != NULL` (scheduled events)
- Filters events with valid `startTime` and `endTime`
- Excludes parent daily tasks

#### 3. **UI Components Hierarchy**

**DaySchedule (Main):**
- Passes `onEnterFocusMode` callback to EventActions
- Manages date state and event list
- Handles delete parent dialog

**ScheduleSideBar:**
- Displays hourly time labels (1 AM - midnight)
- Uses CompositionLocal for consistent positioning

**BasicSchedule:**
- Canvas for event rendering
- Draws grid lines
- Handles event selection/deselection
- Detects overlapping events and adjusts layout
- Shows EventActions toolbar for selected events

**EventBox:**
- Individual event display
- Draggable to reschedule
- Shows selection border when selected
- Accessible text label

**EventActions:**
- Toolbar appearing above selected event
- Focus Mode button (enters focus timer)
- Duration adjustment (+/- 15 minutes)
- Delete button with confirmation

### What's Missing / Incomplete

#### 1. **Incomplete Features:**
- ❌ Event creation directly from schedule
  - Can only create events from Daily or Goal Edit screens
  - Can't tap empty space to create event
- ❌ Event recurrence support
  - Event model has `recurrenceType` and `recurrenceEndDate` fields but not used
  - Schedule doesn't show recurring events
- ❌ Week/Month view
  - Only day view implemented
  - No calendar view
- ❌ Multi-day event support
  - Events assumed to fit within single day
  - No spanning across days
- ❌ Event color coding by goal
  - All events display same color
  - No visual distinction by goal
- ❌ Conflict detection and warnings
  - Can schedule overlapping events
  - No visual warning for conflicts
- ❌ Event templates or quick-add patterns
  - Can't save recurring schedule patterns
  - No "repeat last week's schedule" function
- ❌ Break time visualization
  - No distinction between scheduled work/breaks
  - No lunch break indicators
- ❌ Event editing dialog
  - Can only adjust duration from action toolbar
  - Can't edit title, goal, or other fields in-place
- ❌ Completion status tracking
  - Can't mark events as completed in schedule view
  - No visual feedback when time expires
- ❌ Time bank integration from schedule
  - No direct way to log actual time spent
  - Must enter Focus Mode, then time is recorded

#### 2. **Bug/Issue Areas:**
- ⚠️ Delete parent dialog references undefined variable
  ```kotlin
  // Line 110-126: Dialog shows but uses 'event' which is not in scope
  showDeleteParentDialog?.let { parentId ->
      AlertDialog(
          onDismissRequest = { viewModel.confirmDeleteChildOnly(event) }, // ERROR: 'event' undefined
          ...
          confirmButton = {
              Button(onClick = { viewModel.confirmDeleteWithParent(event) }) { ... } // ERROR
  ```
  This is a critical bug - the dialog won't work properly
  
- ⚠️ Overlapping event layout calculation is basic
  - Assumes events are sortable by start time
  - May break with complex overlaps
  - Uses simple column width division (0.85f / groupSize)

- ⚠️ Drag rounding implementation quirky
  ```kotlin
  val roundedMins = ((mod60 +7)/15) * 15  // Rounds minutes to nearest 15
  ```
  Adds 7 to ensure proper rounding, but unclear if handles negative offsets correctly

- ⚠️ Scroll position not maintained on date change
  - Returns to initial scroll position (6 AM) each day
  - Doesn't remember where user was scrolled

#### 3. **Known Limitations:**
- Display range hardcoded to 1 AM - 24 (midnight)
  - No customization for shift workers or different time zones
- Event height calculation doesn't account for very short events
  - Sub-15 minute events may be hard to see/interact with
- No zoom functionality
  - Can't focus on specific time ranges
- Background click to deselect events works but no visual feedback
- Composition recomputation happens on every render
  - May cause performance issues with many events

#### 4. **Navigation/Integration Issues:**
- No direct link from MainScreen to Scheduler
  - Must go through GoalEdit or Daily screen
- FocusMode exit doesn't return to schedule
  - Returns to previous screen in back stack
- No notification/reminder when event time arrives
- QuickScheduleScreen completely commented out
  - Was intended to provide faster scheduling from main screen

### Integration Points with Other Features

1. **Dailies Integration:**
   - Scheduled events must have `parentDailyId` set to appear in schedule
   - Parent daily tasks (parentDailyId = NULL) are filtered out
   - Deleting scheduled event prompts to delete parent daily

2. **Focus Mode Integration:**
   - Action toolbar provides "Enter Focus Mode" button
   - Passes event.id to FocusMode screen
   - FocusMode creates time bank entries for the event's goal

3. **Time Banking Integration:**
   - Actual time spent recorded via Focus Mode
   - Schedule shows `scheduledDuration` (intended time)
   - Time bank shows `completedDuration` (actual time)

4. **Goal Integration:**
   - Events have `goalId` linking to TodoItem goals
   - Event title pulled from goal title
   - Focus Mode records time against goal

---

## 3. ARCHITECTURAL DECISIONS & PATTERNS

### Parent-Child Event Relationship
**Pattern:** Events are used for both "dailies" and "scheduled events"
- **Parent Daily Event:** `startDate` set, `startTime/endTime` null, `parentDailyId` null
- **Scheduled Event:** `startDate` set, `startTime/endTime` set, `parentDailyId` points to parent
- **Database Filtering:**
  - Dailies: `parentDailyId IS NULL`
  - Scheduled: `parentDailyId IS NOT NULL`

### ActionMode Enumeration
```kotlin
// Used to activate reorder up/down mode
enum class ActionMode {
    Normal,        // Default state
    VerticalUp,    // Reorder up active
    VerticalDown   // Reorder down active
}
```

### Quota-Driven Dailies
- Quotas define daily time requirements
- Button "Add Quota Tasks" automatically creates dailies for active quotas
- QuotaRepository filters by day-of-week
- Dailies show progress: scheduled / quota visual indicators

---

## 4. CURRENT WORKFLOW

### How Dailies Work Today:
1. User navigates to Dailies screen
2. Selects "Add Quota Tasks" button
3. App queries active quotas for current date
4. For each active quota:
   - Fetches associated goal from TodoRepository
   - Creates Event with quotaDuration but no startTime/endTime
5. User can:
   - Reorder tasks up/down using action buttons
   - Click "Schedule" button to open time selector
   - Confirm to create scheduled event block
   - Click "Delete" to remove task (with confirmation)
   - Sets duration for tasks without quota

### How Scheduling Works Today:
1. User navigates to DaySchedule
2. App loads all scheduled events (parentDailyId != NULL) for date
3. Events positioned by start/end times
4. User can:
   - Click event to select it
   - Drag event to reschedule (snaps to 15-min intervals)
   - Use action toolbar to adjust duration or enter Focus Mode
   - Click delete to remove event
   - Navigate to different dates

### Flow from Dailies to Schedule to Focus:
```
Daily Screen (select task + schedule)
    ↓
Creates scheduled event with startTime/endTime
    ↓
Schedule Screen (shows event in grid)
    ↓
Select event + click Focus Mode button
    ↓
FocusMode Screen (timer)
    ↓
Creates time bank entry
    ↓
Return to Schedule (or previous screen)
```

---

## 5. FILES INVOLVED

### Dailies Feature Files:
- `/Users/richardthompson/StudioProjects/VoxPlanApp/app/src/main/java/com/voxplanapp/ui/daily/DailyScreen.kt`
- `/Users/richardthompson/StudioProjects/VoxPlanApp/app/src/main/java/com/voxplanapp/ui/daily/DailyViewModel.kt`

### Scheduling Feature Files:
- `/Users/richardthompson/StudioProjects/VoxPlanApp/app/src/main/java/com/voxplanapp/ui/calendar/DaySchedule.kt`
- `/Users/richardthompson/StudioProjects/VoxPlanApp/app/src/main/java/com/voxplanapp/ui/calendar/SchedulerViewModel.kt`

### Data/Model Files:
- `/Users/richardthompson/StudioProjects/VoxPlanApp/app/src/main/java/com/voxplanapp/data/Event.kt` (Event entity)
- `/Users/richardthompson/StudioProjects/VoxPlanApp/app/src/main/java/com/voxplanapp/data/EventDao.kt` (Database queries)
- `/Users/richardthompson/StudioProjects/VoxPlanApp/app/src/main/java/com/voxplanapp/data/EventRepository.kt` (Repository)
- `/Users/richardthompson/StudioProjects/VoxPlanApp/app/src/main/java/com/voxplanapp/data/QuotaEntity.kt` (Quota model)
- `/Users/richardthompson/StudioProjects/VoxPlanApp/app/src/main/java/com/voxplanapp/data/QuotaRepository.kt` (Quota repository)

### Navigation Files:
- `/Users/richardthompson/StudioProjects/VoxPlanApp/app/src/main/java/com/voxplanapp/navigation/VoxPlanScreen.kt`
- `/Users/richardthompson/StudioProjects/VoxPlanApp/app/src/main/java/com/voxplanapp/navigation/VoxPlanNavHost.kt`
- `/Users/richardthompson/StudioProjects/VoxPlanApp/app/src/main/java/com/voxplanapp/navigation/VoxPlanApp.kt`

### Incomplete/Commented Code:
- `/Users/richardthompson/StudioProjects/VoxPlanApp/app/src/main/java/com/voxplanapp/ui/main/QuickScheduleScreen.kt` (Entirely commented out, lines 26-86)

---

## 6. RECOMMENDATIONS FOR COMPLETION

### For Dailies Feature:
1. Implement completion checkbox/toggle for daily tasks
2. Add quick-reschedule option from daily screen
3. Implement completion callbacks to time bank
4. Add notification system for scheduled dailies
5. Fix QuickScheduleScreen to work from main screen
6. Add bulk operations (select multiple, schedule/delete together)
7. Implement smart scheduling suggestions
8. Add visual feedback for completed tasks

### For Scheduling Feature:
1. Fix the delete parent dialog bug (line 110-126)
2. Add event creation by tapping empty time slots
3. Implement week/month view navigation
4. Add recurrence support
5. Implement color-coding by goal
6. Add conflict detection warnings
7. Implement event editing dialog
8. Add completion status tracking in schedule
9. Improve scroll position persistence
10. Add zoom functionality for time ranges

### General Improvements:
1. Comprehensive error handling and user feedback
2. Performance optimization for large event lists
3. User preferences for display settings
4. Backup/restore functionality
5. Export schedule/dailies to calendar formats
6. Mobile responsiveness improvements

---

## 7. TESTING CONSIDERATIONS

### Dailies Testing:
- Test with no active quotas (empty daily list)
- Test with many quotas (scroll, performance)
- Test date navigation edge cases
- Test scheduling with overlapping times
- Test deletion cascading to children
- Test duration dialog input validation

### Scheduling Testing:
- Test drag operations with various event durations
- Test overlapping event layout (3+, 5+, 10+ overlaps)
- Test very short events (< 15 minutes)
- Test very long events (4+ hours)
- Test delete operations
- Test navigation edge cases (leap years, month boundaries)
- Test Focus Mode entry/exit
- Performance test with 50+ events

---

## SUMMARY TABLE

| Feature | Status | Completeness | Key Issue |
|---------|--------|--------------|-----------|
| **Dailies Screen** | Beta | 70% | No completion tracking, no direct focus mode |
| **Daily Task Management** | Partial | 60% | Limited editing, no bulk operations |
| **Daily-to-Schedule Flow** | Working | 85% | Good integration, minor UI polish needed |
| **Schedule Grid Display** | Working | 80% | Display works well, missing creation |
| **Event Dragging** | Working | 90% | Responsive, snapping works well |
| **Overlapping Events** | Working | 70% | Layout algorithm basic but functional |
| **Event Editing** | Partial | 40% | Only duration adjustable from schedule |
| **Recurrence** | Not Started | 0% | Model supports it, no UI implementation |
| **Week/Month Views** | Not Started | 0% | No calendar view at all |
| **Delete Dialog Bug** | Bug | 0% | Critical issue in DaySchedule.kt line 110-126 |

