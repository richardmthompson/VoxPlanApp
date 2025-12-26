# VoxPlanApp - Core Features Documentation

## Executive Summary

VoxPlanApp is a time management and goal-tracking application built with Jetpack Compose and Android Room Database. The three core features are:

1. **Events** - Scheduled activities with time blocks, parent-child relationships, and duration tracking
2. **Quotas** - Daily/weekly time targets for goals with achievement tracking
3. **Categories** - Goal hierarchies and organizational structures (Note: Implemented as hierarchical TodoItems with parent-child relationships)

---

## 1. EVENTS FEATURE

### 1.1 Overview

Events represent scheduled time blocks for activities. They support:
- Parent-child relationships (Dailies as parents, Scheduled events as children)
- Duration tracking (quota, scheduled, and completed time)
- Recurrence patterns
- Time bank integration for completed time recording

### 1.2 Data Model

**File:** `/Users/richardthompson/StudioProjects/VoxPlanApp/app/src/main/java/com/voxplanapp/data/Event.kt`

```kotlin
@Entity
data class Event(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val goalId: Int,                           // Links to TodoItem (Goal)
    val title: String,
    val startTime: LocalTime? = null,          // Time event starts (nullable for dailies)
    val endTime: LocalTime? = null,            // Time event ends (nullable for dailies)
    val startDate: LocalDate,
    val recurrenceType: RecurrenceType = RecurrenceType.NONE,
    val recurrenceInterval: Int? = 0,
    val recurrenceEndDate: LocalDate? = null,
    val color: Int? = null,
    val order: Int = 0,
    val quotaDuration: Int? = null,            // Target duration in minutes from quota
    val scheduledDuration: Int? = null,        // Calculated scheduled time in minutes
    val completedDuration: Int? = null,        // Time spent from time bank in minutes
    val parentDailyId: Int? = null             // Parent daily ID (null = parent, !null = child)
)

enum class RecurrenceType {
    NONE, DAILY, WEEKLY, MONTHLY, YEARLY
}
```

### 1.3 Event Types

#### Dailies (Parent Events)
- `parentDailyId = null`
- No specific start/end times initially
- Can have multiple scheduled child events
- Track aggregate scheduled and completed time
- Created when quotas are added via "Add Quotas" button

#### Scheduled Events (Child Events)
- `parentDailyId != null`
- Have specific `startTime` and `endTime`
- Appear in Day Schedule view
- Created manually or from dailies

### 1.4 Database Access

**File:** `/Users/richardthompson/StudioProjects/VoxPlanApp/app/src/main/java/com/voxplanapp/data/EventDao.kt`

Key queries:
```kotlin
// Get all dailies for a specific date (parent events)
fun getDailiesForDate(date: LocalDate): Flow<List<Event>>

// Get all scheduled blocks for a date (child events with times)
fun getScheduledBlocksForDate(date: LocalDate): Flow<List<Event>>

// Get scheduled blocks for a specific daily
fun getScheduledBlocksForDaily(dailyId: Int): Flow<List<Event>>

// Get all events with parent ID (children of a daily)
fun getEventsWithParentId(parentId: Int): Flow<List<Event>>

// CRUD operations
suspend fun insertEvent(event: Event): Long
suspend fun updateEvent(event: Event)
suspend fun deleteEvent(eventId: Int)
```

### 1.5 Repository Layer

**File:** `/Users/richardthompson/StudioProjects/VoxPlanApp/app/src/main/java/com/voxplanapp/data/EventRepository.kt`

Wraps EventDao with additional business logic. Main operations:
- Insert, update, delete events
- Fetch dailies and scheduled blocks
- Update event ordering

### 1.6 UI/Events Flow

#### Daily Tasks Screen
**File:** `/Users/richardthompson/StudioProjects/VoxPlanApp/app/src/main/java/com/voxplanapp/ui/daily/DailyScreen.kt`

Features:
- Date navigation (previous/next day, jump to today)
- View dailies for a specific date
- Schedule dailies into time slots
- Track quota progress with visual boxes (green=completed, orange=scheduled, gray=remaining)
- Delete dailies (with cascade to child events)
- Reorder dailies (up/down)

**ViewModel:** `/Users/richardthompson/StudioProjects/VoxPlanApp/app/src/main/java/com/voxplanapp/ui/daily/DailyViewModel.kt`

Key functions:
```kotlin
fun addQuotaTasks()                                    // Create dailies from active quotas
fun scheduleTask(task: Event, startTime, endTime)    // Create scheduled child event
fun setTaskDuration(task: Event, duration: Int)      // Set quota duration
fun deleteTask(task: Event)                           // Delete daily with confirmation
fun reorderTask(task: Event)                          // Reorder dailies
```

#### Day Schedule Screen
**File:** `/Users/richardthompson/StudioProjects/VoxPlanApp/app/src/main/java/com/voxplanapp/ui/calendar/DaySchedule.kt`

Features:
- Hour-by-hour visual schedule (1:00 AM to 11:59 PM)
- Drag/drop events to change time
- Event duration adjustment with +/- buttons
- Visual event blocks with colors
- Delete events with parent handling

**ViewModel:** `/Users/richardthompson/StudioProjects/VoxPlanApp/app/src/main/java/com/voxplanapp/ui/calendar/SchedulerViewModel.kt`

Key functions:
```kotlin
fun updateEvent(event: Event)              // Update event time/duration
fun deleteEvent(event: Event)              // Delete with parent check
fun confirmDeleteWithParent(event: Event)  // Delete event and parent daily
fun updateDate(newDate: LocalDate)         // Change viewing date
```

### 1.7 Event Creation Flows

**Flow 1: From Goal to Scheduled Event**
```
Goal Edit Screen 
  → Set preferred time & duration 
  → Click "Schedule NOW!" 
  → Select date 
  → Creates parent Daily + child Scheduled Event
  → Opens Day Schedule view
```

**Flow 2: Add Quota Tasks to Dailies**
```
Daily Screen (click "Add Quotas") 
  → Queries all active quotas for date 
  → Creates Daily event for each quota'd goal 
  → Displays in Dailies list
  → User can schedule each daily into time slots
```

**Flow 3: Manual Event Creation in Scheduler**
```
Day Schedule 
  → Drag event area at desired time 
  → System creates parent/child structure
  → Can edit time/duration with UI controls
```

### 1.8 Event Business Logic

**Cascade Deletion:**
- When a Daily is deleted, all child Scheduled Events are deleted
- Confirmation dialog shown to user before deletion

**Duration Tracking:**
- `quotaDuration`: Set from Quota or manually
- `scheduledDuration`: Sum of child event durations
- `completedDuration`: Read from TimeBank entries for goal on date

**Parent-Child Relationships:**
```
Daily Event (parent)
├── Scheduled Event 1 (child, parentDailyId = Daily.id)
├── Scheduled Event 2 (child, parentDailyId = Daily.id)
└── Scheduled Event 3 (child, parentDailyId = Daily.id)
```

---

## 2. QUOTAS FEATURE

### 2.1 Overview

Quotas define daily/weekly time targets for goals. They enable:
- Setting desired time allocation per goal
- Tracking achievement across days and weeks
- Visual progress indicators
- Gamification through diamonds/emeralds on achievement

### 2.2 Data Model

**File:** `/Users/richardthompson/StudioProjects/VoxPlanApp/app/src/main/java/com/voxplanapp/data/QuotaEntity.kt`

```kotlin
@Entity(
    foreignKeys = [
        ForeignKey(
            entity = TodoItem::class,
            parentColumns = ["id"],
            childColumns = ["goalId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Quota(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val goalId: Int,                    // Links to TodoItem goal
    val dailyMinutes: Int,              // Target minutes per active day
    val activeDays: String              // "1111100" = Mon-Fri (7 chars, one per day)
)
```

### 2.3 Active Days Encoding

Days are encoded as a 7-character string where each position represents Mon-Sun:
```
Index:  0 1 2 3 4 5 6
Day:    M T W T F S S
Value:  1 1 1 1 1 0 0  = Weekdays only
```

Helper methods in QuotaRepository:
```kotlin
fun isQuotaActiveForDate(quota: Quota, date: LocalDate): Boolean
fun getActiveDays(quota: Quota): List<DayOfWeek>
fun getAllActiveQuotas(date: LocalDate): Flow<List<Quota>>
```

### 2.4 Database Access

**File:** `/Users/richardthompson/StudioProjects/VoxPlanApp/app/src/main/java/com/voxplanapp/data/QuotaDao.kt`

```kotlin
@Dao
interface QuotaDao {
    @Query("SELECT * FROM Quota WHERE goalId = :goalId")
    fun getQuotaForGoal(goalId: Int): Flow<Quota?>
    
    @Query("SELECT * FROM Quota")
    fun getAllQuotas(): Flow<List<Quota>>
    
    @Query("SELECT * FROM Quota WHERE goalId IN (:goalIds)")
    fun getQuotasForGoals(goalIds: List<Int>): Flow<List<Quota>>
    
    // CRUD operations
    suspend fun insertQuota(quota: Quota)
    suspend fun updateQuota(quota: Quota)
    suspend fun deleteQuota(quota: Quota)
    suspend fun deleteQuotaForGoal(goalId: Int)
}
```

### 2.5 Repository Layer

**File:** `/Users/richardthompson/StudioProjects/VoxPlanApp/app/src/main/java/com/voxplanapp/data/QuotaRepository.kt`

Provides filtering and calculation methods:
```kotlin
fun getQuotaForGoal(goalId: Int): Flow<Quota?>
fun getAllQuotas(): Flow<List<Quota>>
fun getQuotasForGoals(goalIds: List<Int>): Flow<List<Quota>>
fun getAllActiveQuotas(date: LocalDate): Flow<List<Quota>>
fun isQuotaActiveForDate(quota: Quota, date: LocalDate): Boolean
fun getActiveDays(quota: Quota): List<DayOfWeek>
```

### 2.6 UI/Quotas Configuration

#### Goal Edit Screen - Quota Settings
**File:** `/Users/richardthompson/StudioProjects/VoxPlanApp/app/src/main/java/com/voxplanapp/ui/goals/QuotaSettings.kt`

Features:
- Set daily minutes target (15-minute increments via +/- buttons)
- Select active days (Mon-Sun toggle buttons)
- Quick presets: Weekdays, Weekends, Every Day, Clear
- Add/remove quota

**Component:** `QuotaSettingsSection`
```kotlin
@Composable
fun QuotaSettingsSection(
    quotaMinutes: Int,
    activeDays: Set<DayOfWeek>,
    onQuotaMinutesChanged: (Int) -> Unit,
    onActiveDaysChanged: (Set<DayOfWeek>) -> Unit,
    onRemoveQuota: () -> Unit
)
```

**ViewModel Support:** `/Users/richardthompson/StudioProjects/VoxPlanApp/app/src/main/java/com/voxplanapp/ui/goals/GoalEditViewModel.kt`

```kotlin
fun updateQuotaMinutes(minutes: Int)
fun updateQuotaActiveDays(days: Set<DayOfWeek>)
fun removeQuota()
fun saveGoal()  // Encodes activeDays string when saving
```

### 2.7 Quota Progress Tracking

#### Daily Task Progress
In DailyScreen, each task shows progress with colored boxes:
```kotlin
@Composable
fun QuotaProgressIndicator(
    quotaDuration: Int?,
    scheduledDuration: Int?,
    completedDuration: Int?
)
```

Visual representation:
- **Green box**: Completed time (from TimeBank)
- **Orange box**: Scheduled time
- **Gray box**: Remaining quota

Example display: "2/4h" = 2 hours completed of 4-hour daily quota

#### Weekly Progress View
**File:** `/Users/richardthompson/StudioProjects/VoxPlanApp/app/src/main/java/com/voxplanapp/ui/goals/ProgressScreen.kt`

**ViewModel:** `/Users/richardthompson/StudioProjects/VoxPlanApp/app/src/main/java/com/voxplanapp/ui/goals/ProgressViewModel.kt`

Features:
- Week-by-week progress navigation
- Daily progress cards showing:
  - Quota achievement per goal
  - Diamonds (1 per 4 hours completed)
  - Emeralds (full day achievement)
- Weekly totals with goal summaries

Key logic:
```kotlin
private fun processDailyProgress(
    goals: List<TodoItem>,
    quotas: List<Quota>,
    entries: List<TimeBank>
): List<DayProgress>

private fun calculateWeekTotal(
    entries: List<TimeBank>,
    goals: List<TodoItem>,
    quotas: List<Quota>
): WeekTotal
```

### 2.8 Quota Lifecycle

**Creation:**
1. User edits goal in GoalEditScreen
2. Sets daily minutes and active days
3. Clicks Save
4. Quota saved to database with encoded activeDays string

**Activation:**
1. Daily Screen checks date
2. Queries getAllActiveQuotas(date)
3. User clicks "Add Quotas" button
4. Creates Daily Event for each active quota'd goal

**Tracking:**
1. Daily Tasks track:
   - `quotaDuration`: From Quota.dailyMinutes
   - `scheduledDuration`: Sum of child events
   - `completedDuration`: From TimeBank entries
2. Visual progress boxes updated in real-time

**Deletion:**
1. User removes quota in Goal Edit Screen
2. Clear activeDays = empty set
3. On save, quota deleted from database
4. Orphaned quota check in ProgressViewModel cleans up

### 2.9 Quota Business Rules

**Validation Rules:**
- quotaMinutes > 0 and divisible by 15
- activeDays must have at least one day selected (or none to delete)
- Cannot have multiple quotas per goal (UPSERT on save)

**Calculation Rules:**
- Daily achievement = sum of TimeBank entries for goal on date
- All quotas for day achieved = Emerald reward
- 4 hours completed = 1 Diamond
- Remaining hours after diamonds = Stars

---

## 3. CATEGORIES FEATURE

### 3.1 Overview

"Categories" in VoxPlanApp are implemented as **hierarchical goal structures** using parent-child TodoItem relationships. While not explicitly called "Categories," this provides organizational structure similar to project categories or goal domains.

### 3.2 Data Model

**File:** `/Users/richardthompson/StudioProjects/VoxPlanApp/app/src/main/java/com/voxplanapp/data/TodoItem.kt`

```kotlin
@Entity
data class TodoItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    var title: String,
    var parentId: Int? = null,                  // null = top-level/category, !null = subcategory
    var order: Int = 0,                         // Ordering within same level
    var notes: String? = null,
    var isDone: Boolean = false,
    var preferredTime: LocalTime? = null,       // Default start time for events
    var estDurationMins: Int? = null,           // Default duration for events
    var frequency: RecurrenceType = RecurrenceType.NONE,
    var expanded: Boolean = true,               // Expand/collapse state
    var completedDate: LocalDate? = null        // When completed (null = not complete)
)
```

### 3.3 Hierarchy Structure

The TodoItem structure supports unlimited nesting:
```
Top-Level Goal/Category (parentId = null)
├── Sub-Goal (parentId = top-level.id)
│   ├── Sub-Sub-Goal (parentId = sub-goal.id)
│   └── Sub-Sub-Goal
├── Sub-Goal
└── Sub-Goal
```

### 3.4 Composite Data Structure

**File:** `/Users/richardthompson/StudioProjects/VoxPlanApp/app/src/main/java/com/voxplanapp/data/GoalWithSubGoals.kt`

```kotlin
data class GoalWithSubGoals(
    val goal: TodoItem,
    val subGoals: List<GoalWithSubGoals>  // Recursive structure
)
```

### 3.5 Database Access

**File:** `/Users/richardthompson/StudioProjects/VoxPlanApp/app/src/main/java/com/voxplanapp/data/TodoDao.kt`

Key queries:
```kotlin
@Query("SELECT * FROM TodoItem WHERE parentID = null")
fun getRootTodos(): List<TodoItem>

@Query("SELECT * FROM TodoItem WHERE parentID = :parentId")
fun getChildrenOf(parentId: Int): List<TodoItem>

@Query("SELECT * FROM TodoItem WHERE id IN (:ids)")
fun getItemsByIds(ids: List<Int>): Flow<List<TodoItem>>

// Recursive deletion
@Transaction
suspend fun deleteItemAndDescendants(goalId: Int)

// Batch operations
@Transaction
suspend fun updateItemsInTransaction(todos: List<TodoItem>)
```

### 3.6 Repository Layer

**File:** `/Users/richardthompson/StudioProjects/VoxPlanApp/app/src/main/java/com/voxplanapp/data/TodoRepository.kt`

Provides high-level category/goal operations:
```kotlin
fun getItemStream(id: Int): Flow<TodoItem?>
fun getAllTodos(): Flow<List<TodoItem>>
fun getRootTodos(): List<TodoItem>
fun getItemsByIds(ids: List<Int>): Flow<List<TodoItem>>
fun getChildrenOf(parentId: Int): List<TodoItem>
suspend fun insert(todo: TodoItem)
suspend fun updateItem(todo: TodoItem)
suspend fun updateItemsInTransaction(items: List<TodoItem>)
suspend fun deleteItemAndDescendents(todo: TodoItem)
suspend fun completeItem(todoItem: TodoItem)
```

### 3.7 UI/Category Management

#### Main Screen - Category Listing
**File:** `/Users/richardthompson/StudioProjects/VoxPlanApp/app/src/main/java/com/voxplanapp/ui/main/MainScreen.kt`

Features:
- Expandable/collapsible goal hierarchy
- Top-level goals displayed as categories
- Click to expand and view sub-goals
- Click to enter sub-goal view (breadcrumb navigation)
- Time banking progress display

**Component:** `GoalListContainer`
```kotlin
@Composable
fun GoalListContainer(
    goals: List<GoalWithSubGoals>,
    selectedGoal: GoalWithSubGoals?,
    onGoalSelected: (GoalWithSubGoals) -> Unit,
    onAddGoal: () -> Unit,
    onDeleteGoal: (TodoItem) -> Unit
)
```

#### Goal Edit Screen - Category Hierarchy
**File:** `/Users/richardthompson/StudioProjects/VoxPlanApp/app/src/main/java/com/voxplanapp/ui/goals/GoalEditScreen.kt`

Features:
- Shows parent goal in hierarchy
- Edit goal title, notes, preferred time, duration
- Add/manage sub-goals
- Set quota for category
- Move items up/down in order

**Component:** `GoalHierarchyDisplay`
```kotlin
@Composable
fun GoalHierarchyDisplay(
    goal: TodoItem,
    parentGoalTitle: String?,
    modifier: Modifier = Modifier
)
```

#### SubGoal Screen - Category Details
Shows all sub-goals of a category with:
- Move up/down/left/right operations
- Expand/collapse
- Delete with cascade
- Create new sub-goal
- Breadcrumb navigation back to parent

### 3.8 Category Operations

**Create Category:**
```
Main Screen 
  → Click "+" to add new goal 
  → Enter title 
  → Becomes top-level category (parentId = null)
```

**Create Sub-Category:**
```
Goal Edit Screen (with category selected)
  → Click to enter sub-goal view
  → Click "+" to add new sub-goal
  → Sub-goal created with parentId = current goal.id
```

**Navigate Hierarchy:**
```
Main Screen 
  → Click on goal with sub-goals 
  → "Enter" icon shows sub-goal count
  → Click to view sub-goals
  → Breadcrumb at top shows hierarchy path
  → Click breadcrumb to navigate back
```

**Reorder Within Category:**
```
Sub-Goal Screen
  → Select "Up" or "Down" action mode
  → Click item to move
  → Order field updated in database
```

**Move Between Categories:**
```
Sub-Goal Screen (move left)
  → Item moves to parent of current category
Goal Edit Screen (move right)
  → Item moves to become sub-goal of selected category
```

### 3.9 Category-Event-Quota Integration

**Relationship:**
```
TodoItem (Goal/Category)
├── Quota (target time for goal)
├── Event (scheduled time blocks)
│   ├── Daily Event (parent, goalId = TodoItem.id)
│   └── Scheduled Event (child, goalId = TodoItem.id, parentDailyId = Daily.id)
└── TimeBank Entry (completed time, goalId = TodoItem.id)
```

**Data Flow:**
1. Create goal/category (TodoItem)
2. Set quota (Quota) specifying daily/weekly time target
3. Schedule events (Event) to meet quota
4. Complete events → record in TimeBank
5. Track progress in ProgressScreen
6. View quota achievement with visual indicators

### 3.10 Category Business Rules

**Hierarchy Rules:**
- Top-level goals have parentId = null
- Sub-goals cannot be more than 1 level deep in current implementation
- Recursive deletion removes all descendants
- Order field manages display sequence within level

**Completion Rules:**
- completedDate = LocalDate.now() when marked done
- completedDate = null when marked incomplete
- Completed goals still visible but marked as done
- Orphaned quotas cleaned up when goal is deleted

**Quota Rules:**
- Each category can have one quota
- Quota applies to goal and all activities toward it
- Time from child events/dailies aggregates to parent quota

---

## 4. FEATURE INTERACTIONS

### 4.1 Event-Quota Integration

**Data Flow:**
```
Quota (target time)
  ↓
Daily Event created with quotaDuration
  ↓
User schedules into time slots
  ↓
Scheduled Events created with startTime/endTime
  ↓
scheduledDuration updated on Daily
  ↓
On completion, TimeBank entry recorded
  ↓
completedDuration updated on Daily
  ↓
ProgressScreen queries TimeBank to show achievement
```

**Example:**
```
Quota: 4 hours coding per day, Mon-Fri
  → Coding Daily created on Monday
  → User schedules:
     - 09:00-10:00 (1h scheduled)
     - 14:00-17:00 (3h scheduled)
  → Daily shows scheduledDuration = 240 minutes
  → User completes 09:00-10:00 event
  → TimeBank entry: coding goal, 60 mins, Monday date
  → Daily shows completedDuration = 60 minutes
  → Progress card shows 1/4 hours filled
```

### 4.2 Category-Event Integration

**Hierarchy Preserved:**
- Event.goalId points to TodoItem
- TodoItem.parentId creates hierarchy
- Events inherit parent-child relationships from goals
- Progress calculated per goal and aggregated to parent

**Example:**
```
Goal: Learn Guitar (top-level)
  ├── Sub-Goal: Learn Chords
  │   └── Event: Chord Practice (Tue 15:00-16:00)
  │       └── TimeBank: 60 mins on chord practice
  └── Sub-Goal: Learn Songs
      └── Event: Song Practice (Thu 14:00-15:30)
          └── TimeBank: 90 mins on song practice

Progress aggregates:
  - Chords: 60 mins toward chord quota
  - Songs: 90 mins toward song quota
  - Learn Guitar: 150 mins total toward overall quota
```

### 4.3 Category-Quota Integration

**Quota Inheritance:**
- Quota set at goal level (TodoItem)
- Applies to all events for that goal
- Progress tracked by goal
- Completion achievement tied to daily quota for that goal

**Example:**
```
Meditation Goal
  Quota: 30 mins daily, every day
  Monday:
    - Daily created with quotaDuration = 30
    - Event scheduled 06:00-06:30
    - User completes 30 mins
    - Diamond earned
  Tuesday:
    - Daily created with quotaDuration = 30
    - Event scheduled 06:00-06:20 only
    - User completes 20 mins
    - Quota not met for day (no diamond)
```

### 4.4 Data Persistence Flow

**Create to Display:**
```
1. Goal created (TodoItem inserted)
2. Quota set (Quota inserted with goalId FK)
3. Event scheduled (Event inserted with goalId)
4. Time recorded (TimeBank inserted with goalId, date)
5. Progress screen queries:
   - All Quotas
   - All Goals by IDs from Quotas
   - TimeBank entries for date range
   - Aggregates and displays
```

**Delete Cascade:**
```
Delete Goal (TodoItem)
  → Foreign Key CASCADE removes Quota
  → Events with goalId still exist (orphaned)
  → TimeBank entries still exist (orphaned)
  → ProgressViewModel cleanup detects orphaned quotas
  → Cleans up orphaned quota entries
```

### 4.5 Shared Components

**ActionModeHandler:**
- Used by DailyScreen and subgoal screens
- Manages Up/Down/Left/Right reordering modes
- Applies to both events and goals

**Time Formatting:**
- Consistent TimeUnit selector in Goal Edit, Daily, Calendar
- 15-minute increments
- "Xh Ym" display format

**Progress Indicators:**
- DailyScreen: Colored boxes for quota progress
- ProgressScreen: Diamonds/Emeralds/Stars for weekly
- Visual consistency across screens

---

## 5. BUSINESS LOGIC & ALGORITHMS

### 5.1 Quota Activation Logic

**Algorithm (QuotaRepository.getAllActiveQuotas):**
```
Input: LocalDate
Process:
  1. Get day of week (0-6, Mon-Sun)
  2. Query all quotas
  3. For each quota:
     - Check activeDays[dayOfWeek]
     - If '1', include in result
Output: Flow<List<Quota>> (only today's active quotas)
```

**DailyViewModel.addQuotaTasks:**
```
Input: Current date
Process:
  1. Query getAllActiveQuotas(date)
  2. For each quota:
     - Get source goal from TodoRepository
     - Create Daily Event with:
       * goalId = quota.goalId
       * quotaDuration = quota.dailyMinutes
       * startDate = date
       * scheduledDuration = 0
       * completedDuration = 0
  3. Insert all Daily Events
Output: Dailies list populated for user
```

### 5.2 Duration Tracking Algorithm

**Parent Daily Duration Update (DailyViewModel.scheduleTask):**
```
Input: Task (Daily Event), startTime, endTime
Process:
  1. Calculate duration = minutes between startTime and endTime
  2. Create child Scheduled Event:
     * parentDailyId = task.id
     * startTime = startTime
     * endTime = endTime
     * quotaDuration = duration
  3. Update parent Daily:
     * scheduledDuration += duration
  4. Insert child, update parent
Output: Daily and child event persisted
```

**Progress Calculation (ProgressViewModel.processDailyProgress):**
```
Input: Goals, Quotas, TimeBank entries (weekly)
Process:
  For each day (0-6):
    1. Get entries for day
    2. For each goal:
       - achieved = sum(entries where goalId == goal.id).duration
       - quota = quota.dailyMinutes or 0
       - isQuotaMet = achieved >= quota
    3. diamonds = totalMinutes / 240
    4. isComplete = all goals met quota
Output: DayProgress list with achievements and rewards
```

### 5.3 Hierarchy Navigation

**Get Goal With SubGoals (SharedViewModel.getGoalWithSubGoals):**
```
Recursive algorithm:
  Input: goalId
  Process:
    1. Fetch TodoItem with goalId
    2. Fetch all children (parentId = goalId)
    3. For each child:
       - Recursively call getGoalWithSubGoals(child.id)
    4. Return GoalWithSubGoals(goal, subGoals[])
Output: Nested structure with all descendants
```

**Hierarchical Deletion (TodoDao.deleteItemAndDescendants):**
```
Recursive algorithm:
  Input: goalId
  Process:
    1. Fetch all children (parentId = goalId)
    2. For each child:
       - Recursively call deleteItemAndDescendants(child.id)
    3. Delete the goal itself (goalId)
Output: All descendants and goal removed from database
```

### 5.4 Event Time Rendering

**DaySchedule Hour-to-Pixel Calculation:**
```
Constants:
  - hourHeight = 48.dp (height per hour)
  - startHour = 1 (display starts at 1 AM)
  - endHour = 24 (display ends at midnight)

For each event:
  - Calculate topOffset = (startTime.hour - startHour) * hourHeight
  - Calculate height = (endTime.hour - startTime.hour) * hourHeight
  - position event at offset with calculated height
```

### 5.5 Validation Rules

**Duration Constraints:**
- Minimum: 15 minutes
- Maximum: 24 hours
- Increments: 15 minutes

**Quota Constraints:**
- Minimum: 15 minutes
- Maximum: theoretical, no hard limit
- Increments: 15 minutes

**Schedule Constraints:**
- No time range validation (events can overlap)
- startTime < endTime enforced by UI
- Events can span midnight (LocalTime support)

---

## 6. DATABASE SCHEMA

### 6.1 Entities and Relationships

```
TodoItem (Goals/Categories)
  ├─ PrimaryKey: id
  ├─ ForeignKey: parentId → TodoItem.id (self-referential)
  └─ Fields: title, notes, isDone, preferredTime, estDurationMins, 
             frequency, expanded, completedDate, order

Event (Scheduled/Daily Events)
  ├─ PrimaryKey: id
  ├─ ForeignKey: goalId → TodoItem.id
  ├─ Self-reference: parentDailyId → Event.id
  └─ Fields: title, startTime, endTime, startDate, recurrenceType,
             recurrenceInterval, recurrenceEndDate, color, order,
             quotaDuration, scheduledDuration, completedDuration

Quota (Time Targets)
  ├─ PrimaryKey: id
  ├─ ForeignKey: goalId → TodoItem.id (CASCADE)
  └─ Fields: dailyMinutes, activeDays (encoded string)

TimeBank (Completed Time Records)
  ├─ PrimaryKey: id
  ├─ ForeignKey: goalId → TodoItem.id
  └─ Fields: date, duration
```

### 6.2 Database Versions

Current version: 13

Recent migrations:
- v12→v13: Added parentDailyId to Event (supports daily-scheduled relationship)
- v11→v12: Added quotaDuration, scheduledDuration, completedDuration to Event
- v10→v11: Made startTime/endTime nullable for daily events
- v9→v10: Created Quota table with foreign key

### 6.3 Type Converters

**File:** `/Users/richardthompson/StudioProjects/VoxPlanApp/app/src/main/java/com/voxplanapp/data/Converters.kt`

Handles conversion between:
- LocalDate ↔ Long (epoch day)
- LocalTime ↔ String (ISO format)
- RecurrenceType ↔ String

---

## 7. KEY FILES REFERENCE

### Data Layer
- `/app/src/main/java/com/voxplanapp/data/Event.kt` - Event entity
- `/app/src/main/java/com/voxplanapp/data/EventDao.kt` - Event DAO
- `/app/src/main/java/com/voxplanapp/data/EventRepository.kt` - Event repository
- `/app/src/main/java/com/voxplanapp/data/QuotaEntity.kt` - Quota entity
- `/app/src/main/java/com/voxplanapp/data/QuotaDao.kt` - Quota DAO
- `/app/src/main/java/com/voxplanapp/data/QuotaRepository.kt` - Quota repository
- `/app/src/main/java/com/voxplanapp/data/TodoItem.kt` - Goal/Category entity
- `/app/src/main/java/com/voxplanapp/data/TodoDao.kt` - Goal/Category DAO
- `/app/src/main/java/com/voxplanapp/data/TodoRepository.kt` - Goal/Category repository
- `/app/src/main/java/com/voxplanapp/data/TimeBankEntry.kt` - TimeBank entity & DAO
- `/app/src/main/java/com/voxplanapp/data/GoalWithSubGoals.kt` - Composite goal structure
- `/app/src/main/java/com/voxplanapp/data/AppDatabase.kt` - Room database config
- `/app/src/main/java/com/voxplanapp/data/Constants.kt` - Constants (FULLBAR_MINS, etc.)

### UI Layer - Daily Tasks
- `/app/src/main/java/com/voxplanapp/ui/daily/DailyScreen.kt` - Daily tasks UI
- `/app/src/main/java/com/voxplanapp/ui/daily/DailyViewModel.kt` - Daily tasks ViewModel

### UI Layer - Scheduler
- `/app/src/main/java/com/voxplanapp/ui/calendar/DaySchedule.kt` - Day schedule UI
- `/app/src/main/java/com/voxplanapp/ui/calendar/SchedulerViewModel.kt` - Scheduler ViewModel

### UI Layer - Goals & Categories
- `/app/src/main/java/com/voxplanapp/ui/goals/GoalEditScreen.kt` - Goal edit UI
- `/app/src/main/java/com/voxplanapp/ui/goals/GoalEditViewModel.kt` - Goal edit ViewModel
- `/app/src/main/java/com/voxplanapp/ui/goals/QuotaSettings.kt` - Quota UI component
- `/app/src/main/java/com/voxplanapp/ui/goals/ProgressScreen.kt` - Progress tracking UI
- `/app/src/main/java/com/voxplanapp/ui/goals/ProgressViewModel.kt` - Progress ViewModel

### UI Layer - Main
- `/app/src/main/java/com/voxplanapp/ui/main/MainScreen.kt` - Category listing
- `/app/src/main/java/com/voxplanapp/ui/main/MainViewModel.kt` - Main ViewModel
- `/app/src/main/java/com/voxplanapp/ui/main/GoalListContainer.kt` - Goal hierarchy display

---

## 8. DEVELOPMENT ROADMAP (From voxplan_process.md)

### Completed (v3.2)
- Events with parent/child relationships
- Quotas with daily tracking
- Daily event/scheduled event separation
- Progress screen with weekly summaries
- Time banking system
- Dailies screen with scheduled boxes

### In Progress
- Focus mode for time tracking
- Additional quota analytics

### Planned
- Multi-event focus mode sequences
- Categories as 5 life areas (vision screen)
- Voice integration for planning
- Calendar view enhancements
- Weekly summary reports

---

## 9. CONSTANTS & CONFIGURATION

**File:** `/Users/richardthompson/StudioProjects/VoxPlanApp/app/src/main/java/com/voxplanapp/data/Constants.kt`

```kotlin
const val FULLBAR_MINS = 60              // Power bar fills every 60 minutes
const val pointsForItemCompletion = 15   // Points awarded per discrete completion
```

**Visual Constants:**
- Hour height in scheduler: 48.dp
- Schedule view hours: 1 AM - 11:59 PM (24 hours)
- Initial scroll position: 6 AM
- Quota progress box size: 16.dp

---

## 10. ERROR HANDLING & EDGE CASES

### Event Deletion Edge Cases
1. **Delete only child event:**
   - Check if siblings exist
   - If yes, just delete child
   - If no, prompt user about parent

2. **Delete parent daily:**
   - Cascade delete all children
   - Show confirmation to user
   - User can cancel operation

### Quota Edge Cases
1. **Orphaned quotas:**
   - When goal deleted, quota cascade deleted by FK
   - ProgressViewModel runs cleanup on load
   - Checks if quotas exist for non-existent goals

2. **No active quotas:**
   - "Add Quotas" button does nothing if none active
   - User sees empty daily list until manual addition

### Duration Overflow
1. **Schedule across midnight:**
   - LocalTime supports 24-hour format
   - Events can start at 23:00, end at 00:30 (theoretical)

2. **Zero-duration events:**
   - UI enforces 15-minute minimum
   - Database allows 0 (not recommended)

---

## 11. TESTING CONSIDERATIONS

### Unit Test Areas
- Duration calculations (parent daily aggregation)
- Quota activation logic (day of week matching)
- Hierarchy traversal (recursive goal fetching)
- Delete cascade (descendant removal)

### Integration Test Areas
- Event creation flow (daily → scheduled → timebank)
- Quota progress tracking (multiple goals, multiple days)
- Category operations (create, move, delete with descendants)
- Progress screen data aggregation

### UI Test Areas
- Daily task list rendering
- Event drag/drop in scheduler
- Quota day selector
- Breadcrumb navigation
- Delete confirmation dialogs

---

