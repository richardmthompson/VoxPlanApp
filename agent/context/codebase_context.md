# VoxPlanApp Codebase Context

## Table of Contents
1. [Directory and File Structure Mapping](#directory-and-file-structure-mapping)
2. [Key Entry Points and Main Flows](#key-entry-points-and-main-flows)
3. [Core Modules and Their Responsibilities](#core-modules-and-their-responsibilities)
4. [Architecture Patterns & Data Flow](#architecture-patterns--data-flow)
5. [Major Data Models and State Management](#major-data-models-and-state-management)
6. [External Dependencies and Integrations](#external-dependencies-and-integrations)
7. [Notable Patterns, Conventions, and Anti-Patterns](#notable-patterns-conventions-and-anti-patterns)
8. [Development Guidelines](#development-guidelines)
9. [Critical Files & Line Counts](#critical-files--line-counts)
10. [Constants & Magic Numbers](#constants--magic-numbers)

---

## Directory and File Structure Mapping

```
/Users/richardthompson/StudioProjects/VoxPlanApp/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/voxplanapp/
│   │   │   │   ├── MainActivity.kt                          [CORE] - App entry point
│   │   │   │   ├── VoxPlanApplication.kt                    [CORE] - Application class with DI
│   │   │   │   ├── AppViewModelProvider.kt                  [CORE] - ViewModel factory
│   │   │   │   │
│   │   │   │   ├── data/                                    # Data layer (16 files)
│   │   │   │   │   ├── AppDatabase.kt (384 lines)           [CORE] - Room database v13 with migrations
│   │   │   │   │   ├── AppContainer.kt                      [CORE] - Manual dependency injection
│   │   │   │   │   ├── Constants.kt                         - FULLBAR_MINS, pointsForItemCompletion
│   │   │   │   │   ├── Converters.kt                        - LocalDate/LocalTime type converters
│   │   │   │   │   │
│   │   │   │   │   ├── TodoItem.kt                          # Goal entity
│   │   │   │   │   ├── TodoDao.kt                           # Goal database operations
│   │   │   │   │   ├── TodoRepository.kt (76 lines)         - Goal CRUD with Flow
│   │   │   │   │   │
│   │   │   │   │   ├── Event.kt                             # Dual-purpose event entity
│   │   │   │   │   ├── EventDao.kt                          # Event database operations
│   │   │   │   │   ├── EventRepository.kt (32 lines)        - Event CRUD
│   │   │   │   │   │
│   │   │   │   │   ├── TimeBankEntry.kt                     # Time tracking entity
│   │   │   │   │   ├── TimeBankDao.kt                       # Time bank operations
│   │   │   │   │   ├── TimeBankRepository.kt (56 lines)     - Time tracking
│   │   │   │   │   │
│   │   │   │   │   ├── QuotaEntity.kt                       # Quota entity
│   │   │   │   │   ├── QuotaDao.kt                          # Quota database operations
│   │   │   │   │   ├── QuotaRepository.kt (47 lines)        - Quota management
│   │   │   │   │   │
│   │   │   │   │   ├── GoalWithSubGoals.kt                  # Composite model for hierarchy
│   │   │   │   │   └── GoalEventMapper.kt                   # Mapping utilities
│   │   │   │   │
│   │   │   │   ├── model/                                   # Domain models
│   │   │   │   │   └── ActionMode.kt                        - Reordering state enum
│   │   │   │   │
│   │   │   │   ├── shared/                                  # Shared ViewModels and utilities
│   │   │   │   │   ├── SharedViewModel.kt (80 lines)        [KEY] - Breadcrumb navigation logic
│   │   │   │   │   └── SoundPlayer.kt                       - ExoPlayer wrapper for sound effects
│   │   │   │   │
│   │   │   │   ├── navigation/                              # Navigation infrastructure (4 files)
│   │   │   │   │   ├── VoxPlanApp.kt                        [CORE] - Scaffold + ActionModeHandler
│   │   │   │   │   ├── VoxPlanNavHost.kt                    - Route definitions
│   │   │   │   │   ├── VoxPlanScreen.kt                     - Sealed class for type-safe routes
│   │   │   │   │   └── NavigationViewModel.kt               - Bottom nav state
│   │   │   │   │
│   │   │   │   └── ui/                                      # UI layer
│   │   │   │       │
│   │   │   │       ├── main/                                # Main screen (goal hierarchy)
│   │   │   │       │   ├── MainScreen.kt (492 lines)        [COMPLEX] - Goal list UI
│   │   │   │       │   ├── MainViewModel.kt (362 lines)     [KEY] - Goal state management
│   │   │   │       │   ├── GoalListContainer.kt             - Scrollable goal list
│   │   │   │       │   ├── GoalItem.kt                      - Individual goal card
│   │   │   │       │   ├── TodoInputBar.kt                  - Goal creation input
│   │   │   │       │   ├── BreadCrumbNavigation.kt          - Breadcrumb UI
│   │   │   │       │   └── QuickScheduleScreen.kt           [BUG] - Entirely commented out
│   │   │   │       │
│   │   │   │       ├── daily/                               # Daily tasks screen
│   │   │   │       │   ├── DailyScreen.kt (628 lines)       [COMPLEX] - Daily planning UI
│   │   │   │       │   └── DailyViewModel.kt (190 lines)    - Daily state management
│   │   │   │       │
│   │   │   │       ├── calendar/                            # Scheduling interface
│   │   │   │       │   ├── DaySchedule.kt (675 lines)       [COMPLEX] [BUG at 110-126] - Day scheduler
│   │   │   │       │   └── SchedulerViewModel.kt (141 lines) - Scheduler state
│   │   │   │       │
│   │   │   │       ├── goals/                               # Goal editing and progress
│   │   │   │       │   ├── GoalEditScreen.kt (292 lines)    - Goal editor UI
│   │   │   │       │   ├── GoalEditViewModel.kt (191 lines) - Edit state
│   │   │   │       │   ├── QuotaSettings.kt                 - Quota configuration UI
│   │   │   │       │   ├── ProgressScreen.kt                - Weekly progress view
│   │   │   │       │   └── ProgressViewModel.kt (174 lines) - Progress state
│   │   │   │       │
│   │   │   │       ├── focusmode/                           # Focus mode (timer)
│   │   │   │       │   ├── FocusModeScreen.kt (697 lines)   [COMPLEX] - Timer UI with medals
│   │   │   │       │   └── FocusViewModel.kt (492 lines)    [COMPLEX] - Timer state management
│   │   │   │       │
│   │   │   │       ├── constants/                           # UI constants
│   │   │   │       │   ├── Colors.kt                        - Color definitions
│   │   │   │       │   ├── Dimens.kt                        - Dimension constants
│   │   │   │       │   ├── DpValues.kt                      - Dp value constants
│   │   │   │       │   └── TextStyles.kt                    - Text style definitions
│   │   │   │       │
│   │   │   │       └── theme/                               # Material theme
│   │   │   │           ├── Color.kt                         - Theme colors
│   │   │   │           ├── Theme.kt                         - Material theme setup
│   │   │   │           └── Type.kt                          - Typography
│   │   │   │
│   │   │   ├── res/                                         # Android resources
│   │   │   └── AndroidManifest.xml
│   │   │
│   │   └── schemas/                                         # Room schema exports (v1-v13)
│   │
│   └── build.gradle.kts                                     # Build configuration
│
├── docs/                                                    # Generated documentation
│   └── LLM-Generated/
│       ├── ARCHITECTURE_DOCUMENTATION_SUMMARY.txt (675 lines)
│       ├── INCOMPLETE_FEATURES.md (572 lines)
│       └── [additional documentation files]
│
├── agent/                                                   # AI agent context
│   └── context/
│       ├── project_context.md                               - High-level project overview
│       └── codebase_context.md                              - (This file)
│
├── CLAUDE.md                                                # AI assistant instructions
└── README.md                                                # Project overview

```

### Key Statistics
- **Total Kotlin Files**: 51
- **ViewModels**: 8 (6 feature + 2 shared)
- **Repositories**: 4 (Todo, Event, TimeBank, Quota)
- **Database Entities**: 4 (TodoItem, Event, TimeBank, Quota)
- **DAOs**: 4 (TodoDao, EventDao, TimeBankDao, QuotaDao)
- **Application Screens**: 6 (Main, Daily, Schedule, GoalEdit, Focus, Progress)
- **Bottom Navigation Tabs**: 4 (Goals, Daily, Progress, Schedule)

---

## Key Entry Points and Main Flows

### Application Entry Point

**MainActivity.kt** (`/app/src/main/java/com/voxplanapp/MainActivity.kt`)
```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            VoxPlanTheme {
                VoxPlanApp()  // Main composable
            }
        }
    }
}
```

### Application Initialization Flow

```
MainActivity.onCreate()
    ↓
VoxPlanApplication.onCreate()
    ↓
AppDataContainer initialization (lazy)
    ↓
Room.databaseBuilder() → AppDatabase created
    ↓
Repositories lazy-initialized on first access
    ↓
VoxPlanApp() composable
    ↓
Scaffold + VoxPlanNavHost
    ↓
Navigation routes defined
    ↓
ViewModels created via AppViewModelProvider.Factory
```

### Main Application Flow: Goal Hierarchy Management

**File Path**: `app/src/main/java/com/voxplanapp/ui/main/MainScreen.kt:492`

```
User opens app → MainScreen displayed
    ↓
MainViewModel combines flows:
    repository.getAllTodos() + sharedViewModel.breadcrumbs
    ↓
SharedViewModel.processGoals(todos, currentParentId)
    ↓
Recursive processing (max depth 3):
    - Filter by parentId
    - Sort by order
    - Map to GoalWithSubGoals
    ↓
MainUiState emitted via StateFlow
    ↓
UI recomposes with goal list + breadcrumbs
    ↓
User clicks goal → navigateToSubGoal()
    ↓
Breadcrumbs updated → mainUiState re-evaluates
    ↓
UI shows sub-goals with breadcrumb trail
```

**Code Pattern**:
```kotlin
// MainViewModel.kt:56-70
val mainUiState: StateFlow<MainUiState> = combine(
    repository.getAllTodos(),
    sharedViewModel.breadcrumbs
) { todos, breadcrumbs ->
    val currentParentId = breadcrumbs.lastOrNull()?.goal?.id
    MainUiState(
        goalList = sharedViewModel.processGoals(todos, currentParentId),
        breadcrumbs = breadcrumbs
    )
}.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5_000L),
    initialValue = MainUiState()
)
```

### Daily Planning Flow

**File Path**: `app/src/main/java/com/voxplanapp/ui/daily/DailyScreen.kt:628`

```
User navigates to Dailies → DailyScreen
    ↓
DailyViewModel queries:
    eventRepository.getDailiesForDate(date)
    ↓
Database query: SELECT * FROM Event WHERE parentDailyId IS NULL AND startDate = ?
    ↓
User clicks "Add Quota Tasks"
    ↓
DailyViewModel.addQuotaTasks():
    quotaRepository.getAllActiveQuotas(date)
    ↓
For each active quota:
    - todoRepository.getItem(quota.goalId)
    - Create Event(goalId, title, quotaDuration, parentDailyId=null)
    - eventRepository.insert(event)
    ↓
Flow emits updated daily tasks
    ↓
UI recomposes with quota-based tasks
    ↓
User clicks "Schedule" on task
    ↓
Time selection dialog → scheduleTask(task, startTime, endTime)
    ↓
Create scheduled child event:
    Event(parentDailyId = task.id, startTime, endTime)
    ↓
Insert scheduled event → appears in Schedule screen
```

### Scheduling Flow

**File Path**: `app/src/main/java/com/voxplanapp/ui/calendar/DaySchedule.kt:675`

```
User navigates to Schedule → DaySchedule
    ↓
SchedulerViewModel.currentDate (StateFlow)
    ↓
flatMapLatest pattern:
    _currentDate.flatMapLatest { date →
        eventRepository.getScheduledBlocksForDate(date)
    }
    ↓
Database query: SELECT * FROM Event WHERE parentDailyId IS NOT NULL AND startDate = ?
    ↓
Events positioned by startTime/endTime
    ↓
User drags event:
    detectDragGestures { ... }
    ↓
Calculate new time from offset
    ↓
Round to nearest 15 minutes
    ↓
Update event: viewModel.updateEvent(event.copy(startTime=..., endTime=...))
    ↓
Database update → Flow emits → UI recomposes
```

### Focus Mode Flow

**File Path**: `app/src/main/java/com/voxplanapp/ui/focusmode/FocusModeScreen.kt:697`

```
User clicks "Focus Mode" from Schedule → FocusModeScreen
    ↓
FocusViewModel initialized with goalId/eventId
    ↓
User clicks "Start"
    ↓
Launch coroutine with timer loop:
    while (isActive) {
        elapsedTime++
        progress = (elapsedTime % 1800) / 1800f  // 30-minute cycle
        delay(1000)

        // Medal awards at 30, 60, 90, 120 minutes
        if (elapsedTime == 1800) awardMedal(Bronze)
        if (elapsedTime == 3600) awardMedal(Silver)
        ...
    }
    ↓
User clicks "Bank Time"
    ↓
FocusViewModel.bankTime():
    val totalMinutes = calculateMedalMinutes()
    timeBankRepository.addTimeBankEntry(goalId, date, totalMinutes)
    ↓
Optional: Create scheduled event from banked time
    ↓
Navigate back → Progress screen shows updated time
```

---

## Core Modules and Their Responsibilities

### ViewModels (State Management)

#### 1. MainViewModel.kt (362 lines) - [KEY]

**Location**: `app/src/main/java/com/voxplanapp/ui/main/MainViewModel.kt`

**Responsibilities**:
- Manages goal list state with breadcrumb navigation
- Combines `repository.getAllTodos()` with `sharedViewModel.breadcrumbs`
- Tracks daily power bar (total time from time bank)
- Handles ActionMode reordering (vertical up/down, hierarchy up/down)
- Coordinates sound effects for milestones

**State Management**:
```kotlin
val mainUiState: StateFlow<MainUiState>  // Combined from todos + breadcrumbs
val todayTotalTime: StateFlow<Int>       // Daily time bank total with sound effects
val actionMode: State<ActionMode>        // Compose State for reordering
```

**Key Functions**:
- `navigateToSubGoals(goal)` - Delegates to SharedViewModel
- `reorderItem(item, actionMode)` - Calculates new order, updates via transaction
- `deleteItem(item)` - Recursive deletion with descendants
- `addItem(title, description)` - Creates new goal with auto-generated order

**Dependencies**: TodoRepository, EventRepository, TimeBankRepository, QuotaRepository, SoundPlayer, SharedViewModel

---

#### 2. FocusViewModel.kt (492 lines) - [COMPLEX]

**Location**: `app/src/main/java/com/voxplanapp/ui/focusmode/FocusViewModel.kt`

**Responsibilities**:
- Complex timer state management (work/rest periods)
- Medal award system (Bronze/Silver/Gold/Diamond)
- Discrete task completion tracking
- Time banking logic
- Job lifecycle management for timer coroutines

**State Management**:
```kotlin
val focusUiState: MutableState<FocusUiState>           // Compose State for UI
val timerSettingsState: MutableState<TimerSettingsState>  // Timer config
private val _timerJob: MutableStateFlow<Job?>          // Coroutine job tracking
```

**Key Functions**:
- `startTimer()` - Launches coroutine with 1-second interval loop
- `pauseTimer()` / `resumeTimer()` - Job cancellation and restart
- `awardMedal(type)` - Adds medal to state, plays sound
- `bankTime()` - Creates TimeBank entry and optional Event
- `completeDiscreteTask(task)` - Marks task complete, adds bonus time

**Medal Calculation**:
```kotlin
val elapsedMinutes = elapsedTime / 60
when {
    elapsedMinutes >= 120 -> Diamond
    elapsedMinutes >= 90 -> Gold
    elapsedMinutes >= 60 -> Silver
    elapsedMinutes >= 30 -> Bronze
    else -> null
}
```

---

#### 3. DailyViewModel.kt (190 lines)

**Location**: `app/src/main/java/com/voxplanapp/ui/daily/DailyViewModel.kt`

**Responsibilities**:
- Daily task state with quota integration
- Date-driven reactivity using `snapshotFlow`
- Parent daily management
- Vertical reordering via ActionMode
- Quota-to-daily population

**State Management**:
```kotlin
val uiState: StateFlow<DailyUiState>               // Combined state
val showDeleteConfirmation: StateFlow<Event?>       // Delete dialog
```

**Reactive Pattern (Pattern 3)**:
```kotlin
snapshotFlow { _uiState.value.date }
    .flatMapLatest { date →
        eventRepository.getDailiesForDate(date)
    }
    .collect { events →
        _uiState.update { it.copy(dailyTasks = events) }
    }
```

**Key Functions**:
- `addQuotaTasks()` - Queries active quotas, creates daily events
- `scheduleTask(task, startTime, endTime)` - Creates scheduled child event
- `setTaskDuration(task, duration)` - Sets quotaDuration field
- `reorderTask(task)` - ActionMode-based vertical reordering

---

#### 4. SchedulerViewModel.kt (141 lines)

**Location**: `app/src/main/java/com/voxplanapp/ui/calendar/SchedulerViewModel.kt`

**Responsibilities**:
- Date-driven event reactivity
- Day view state management
- Scheduled event CRUD operations
- Parent-child delete confirmation

**State Management**:
```kotlin
val currentDate: StateFlow<LocalDate>                 // Selected date
val eventsForCurrentDate: StateFlow<List<Event>>      // Filtered events
val showDeleteParentDialog: StateFlow<Event?>         // Delete confirmation
```

**Reactive Pattern (Pattern 2)**:
```kotlin
val eventsForCurrentDate = _currentDate.flatMapLatest { date →
    eventRepository.getScheduledBlocksForDate(date)
}.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5_000L),
    initialValue = emptyList()
)
```

**Key Functions**:
- `updateDate(newDate)` - Changes displayed date, triggers re-query
- `updateEvent(event)` - Updates event (e.g., after drag-to-reschedule)
- `confirmDeleteWithParent(event)` - Deletes both parent daily and child
- `confirmDeleteChildOnly(event)` - Deletes only scheduled event

**Known Bug**: DaySchedule.kt lines 110-126 reference undefined `event` variable in delete dialog

---

#### 5. GoalEditViewModel.kt (191 lines)

**Location**: `app/src/main/java/com/voxplanapp/ui/goals/GoalEditViewModel.kt`

**Responsibilities**:
- Goal editing with quota settings
- Active days encoding/decoding
- SavedStateHandle for goal ID parameter
- Quota creation/update/delete

**State Management**:
```kotlin
val goalUiState: MutableState<GoalUiState>  // Compose State for edit form
```

**Active Days Pattern**:
```kotlin
// Encoding: Boolean array → String
val activeDaysString = activeDays.joinToString("") { if (it) "1" else "0" }
// Result: [true, true, true, true, true, false, false] → "1111100"

// Decoding: String → Boolean array
val activeDays = activeDaysString.map { it == '1' }.toTypedArray()
```

**Key Functions**:
- `loadGoal(goalId)` - Fetches goal + quota from repositories
- `saveGoal()` - Updates goal and quota in transaction
- `deleteGoal()` - Cascading delete with SharedViewModel breadcrumb cleanup

---

#### 6. ProgressViewModel.kt (174 lines)

**Location**: `app/src/main/java/com/voxplanapp/ui/goals/ProgressViewModel.kt`

**Responsibilities**:
- Weekly quota progress tracking
- Time bank aggregation by goal per day
- Diamond/star indicator calculation

**State Management**:
```kotlin
val uiState: StateFlow<ProgressUiState>  // Weekly progress data
```

**Data Aggregation**:
```kotlin
// Query all quotas
val quotas = quotaRepository.getAllQuotas().first()

// For each quota, get time bank entries for week
quotas.forEach { quota →
    val entries = timeBankRepository.getEntriesForDateRange(startDate, endDate).first()
    val dailyTotals = entries.groupBy { it.date }
        .mapValues { (_, entries) → entries.sumOf { it.duration } }
}
```

---

#### 7. SharedViewModel.kt (80 lines) - [KEY]

**Location**: `app/src/main/java/com/voxplanapp/shared/SharedViewModel.kt`

**Responsibilities**:
- Single source of truth for breadcrumb navigation
- Recursive goal processing (max depth 3)
- Shared across multiple ViewModels

**State Management**:
```kotlin
private val _breadcrumbs = MutableStateFlow<List<GoalWithSubGoals>>(emptyList())
val breadcrumbs: StateFlow<List<GoalWithSubGoals>> = _breadcrumbs.asStateFlow()
```

**Core Algorithm**:
```kotlin
fun processGoals(todos: List<TodoItem>, parentId: Int?, depth: Int = 1): List<GoalWithSubGoals> {
    return todos.filter { it.parentId == parentId }
        .sortedBy { it.order }
        .map { goal →
            val subGoals = if (depth < MAX_DEPTH) {
                processGoals(todos, goal.id, depth + 1)  // Recursive call
            } else {
                emptyList()
            }
            GoalWithSubGoals(goal = goal, subGoals = subGoals)
        }
}
```

**Breadcrumb Navigation Logic**:
```kotlin
fun navigateToSubGoal(goal: GoalWithSubGoals, parentGoal: GoalWithSubGoals?) {
    val currentBreadcrumbs = breadcrumbs.value

    if (parentGoal == null) {
        // Top-level goal, start fresh trail
        _breadcrumbs.value = listOf(goal)
        return
    }

    // Find goal in existing breadcrumbs
    val index = breadcrumbs.value.indexOfFirst { it.goal.id == goal.goal.id }
    if (index != -1) {
        // Navigate to existing breadcrumb, trim trail
        _breadcrumbs.value = currentBreadcrumbs.take(index + 1)
        return
    }

    // Add to breadcrumb trail
    val parentIndex = currentBreadcrumbs.indexOfFirst { it.goal.id == parentGoal.goal.id }
    _breadcrumbs.value = if (parentIndex != -1) {
        currentBreadcrumbs.take(parentIndex + 1) + goal
    } else {
        currentBreadcrumbs + parentGoal + goal
    }
}
```

---

#### 8. NavigationViewModel.kt

**Location**: `app/src/main/java/com/voxplanapp/navigation/NavigationViewModel.kt`

**Responsibilities**:
- Bottom navigation bar state
- Selected tab tracking

**State Management**:
```kotlin
val selectedTab: StateFlow<VoxPlanScreen>  // Current bottom nav selection
```

---

### Data Layer

#### Repositories (Repository Pattern)

**Pattern**: Single DAO per repository, wrapping database operations with Flow-based reactivity

**1. TodoRepository.kt (76 lines)**

**Location**: `app/src/main/java/com/voxplanapp/data/TodoRepository.kt`

**Key Methods**:
```kotlin
fun getAllTodos(): Flow<List<TodoItem>>                      // Reactive query
suspend fun getItem(id: Int): TodoItem?                      // Blocking read
suspend fun insert(item: TodoItem)                           // Write operation
suspend fun update(item: TodoItem)                           // Update operation
suspend fun deleteItemAndDescendants(itemId: Int)            // Recursive delete
@Transaction suspend fun updateItemsInTransaction(items: List<TodoItem>)  // Atomic batch
```

**Recursive Deletion Logic**:
```kotlin
suspend fun deleteItemAndDescendants(itemId: Int) {
    val children = todoDao.getItemsByParentId(itemId)
    children.forEach { child →
        deleteItemAndDescendants(child.id)  // Recursive
    }
    todoDao.delete(itemId)
}
```

---

**2. EventRepository.kt (32 lines)**

**Location**: `app/src/main/java/com/voxplanapp/data/EventRepository.kt`

**Key Methods**:
```kotlin
fun getDailiesForDate(date: LocalDate): Flow<List<Event>>           // parentDailyId IS NULL
fun getScheduledBlocksForDate(date: LocalDate): Flow<List<Event>>   // parentDailyId IS NOT NULL
fun getEventsWithParentId(parentId: Int): Flow<List<Event>>         // Children of parent daily
suspend fun insert(event: Event): Long                              // Returns inserted ID
suspend fun update(event: Event)
suspend fun delete(event: Event)
```

**Parent-Child Filtering**:
```kotlin
// DAO queries
@Query("SELECT * FROM Event WHERE parentDailyId IS NULL AND startDate = :date")
fun getDailiesForDate(date: Long): Flow<List<Event>>

@Query("SELECT * FROM Event WHERE parentDailyId IS NOT NULL AND startDate = :date")
fun getScheduledBlocksForDate(date: Long): Flow<List<Event>>
```

---

**3. TimeBankRepository.kt (56 lines)**

**Location**: `app/src/main/java/com/voxplanapp/data/TimeBankRepository.kt`

**Key Methods**:
```kotlin
suspend fun addTimeBankEntry(goalId: Int, date: LocalDate, duration: Int)
fun getEntriesForDate(date: LocalDate): Flow<List<TimeBank>>
fun getEntriesForDateRange(start: LocalDate, end: LocalDate): Flow<List<TimeBank>>
fun getTotalTimeForDate(date: LocalDate): Flow<Int?>                // Aggregation query
```

**Aggregation Query**:
```kotlin
// DAO
@Query("SELECT SUM(duration) FROM TimeBank WHERE date = :date")
fun getTotalTimeForDate(date: Long): Flow<Int?>
```

---

**4. QuotaRepository.kt (47 lines)**

**Location**: `app/src/main/java/com/voxplanapp/data/QuotaRepository.kt`

**Key Methods**:
```kotlin
fun getQuotaForGoal(goalId: Int): Flow<Quota?>
suspend fun getAllQuotas(): Flow<List<Quota>>
suspend fun getAllActiveQuotas(date: LocalDate): Flow<List<Quota>>  // Filters by active days
suspend fun insert(quota: Quota)
suspend fun update(quota: Quota)
suspend fun delete(quota: Quota)
```

**Active Days Filtering**:
```kotlin
fun getAllActiveQuotas(date: LocalDate): Flow<List<Quota>> {
    val dayOfWeekIndex = date.dayOfWeek.value - 1  // 0-based (Mon=0, Sun=6)
    return quotaDao.getAllQuotas().map { quotas →
        quotas.filter { quota →
            quota.activeDays[dayOfWeekIndex] == '1'
        }
    }
}
```

---

#### Database Entities

**1. TodoItem.kt - Hierarchical Goals**

```kotlin
@Entity(tableName = "TodoItem")
data class TodoItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String? = null,
    val parentId: Int? = null,                // Null for top-level goals
    val order: Int = 0,                       // Display order within level
    val createdDate: Long,                    // LocalDate as epoch day
    val completedDate: Long? = null,          // LocalDate or null if incomplete
    val notes: String? = null,
    val preferredTime: String? = null,        // LocalTime as string
    val estDurationMins: Int? = null,
    val frequency: String = "NONE",
    val expanded: Boolean = true              // UI expansion state
)
```

---

**2. Event.kt - Dual-Purpose Entity**

```kotlin
@Entity(
    tableName = "Event",
    foreignKeys = [ForeignKey(
        entity = TodoItem::class,
        parentColumns = ["id"],
        childColumns = ["goalId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class Event(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val goalId: Int,                          // Links to TodoItem
    val title: String,
    val startDate: Long,                      // LocalDate as epoch day
    val startTime: String? = null,            // LocalTime as string (null for dailies)
    val endTime: String? = null,              // LocalTime as string (null for dailies)
    val quotaDuration: Int? = null,           // Target minutes from quota
    val scheduledDuration: Int? = null,       // Total scheduled time
    val completedDuration: Int? = null,       // Actual time from time bank
    val parentDailyId: Int? = null,           // NULL = daily, value = scheduled child
    val order: Int = 0,                       // Display order
    val recurrenceType: String = "NONE",      // Planned feature (not implemented)
    val recurrenceInterval: Int? = null,
    val recurrenceEndDate: Long? = null,
    val color: Int? = null
)
```

**Parent-Child Distinction**:
- **Parent Daily**: `parentDailyId = null`, `startTime = null`, `endTime = null`
- **Scheduled Child**: `parentDailyId = [parent event id]`, `startTime/endTime` populated

---

**3. TimeBank.kt - Time Tracking**

```kotlin
@Entity(
    tableName = "TimeBank",
    foreignKeys = [ForeignKey(
        entity = TodoItem::class,
        parentColumns = ["id"],
        childColumns = ["goal_id"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class TimeBank(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "goal_id") val goalId: Int,
    val date: Long,                           // LocalDate as epoch day
    val duration: Int                         // Minutes spent
)
```

---

**4. Quota.kt - Goal Quotas**

```kotlin
@Entity(
    tableName = "Quota",
    foreignKeys = [ForeignKey(
        entity = TodoItem::class,
        parentColumns = ["id"],
        childColumns = ["goalId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class Quota(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val goalId: Int,                          // Links to TodoItem
    val minutes: Int,                         // Daily target minutes
    val activeDays: String                    // 7-char pattern: "1111100"
)
```

**Active Days Encoding**:
- String of 7 characters representing Mon-Sun
- Example: `"1111100"` = Active Monday-Friday
- Index calculation: `dayOfWeek.value - 1` (Monday = 0, Sunday = 6)
- Check active: `quota.activeDays[dayIndex] == '1'`

---

#### Composite Models

**GoalWithSubGoals.kt - Recursive Structure**

```kotlin
data class GoalWithSubGoals(
    val goal: TodoItem,
    val subGoals: List<GoalWithSubGoals>  // Recursive!
)
```

Used for hierarchical display in UI, maximum depth enforced by `SharedViewModel.processGoals()`.

---

## Architecture Patterns & Data Flow

### MVVM with Manual Dependency Injection

**Pattern**: Service Locator

```kotlin
// VoxPlanApplication.kt
class VoxPlanApplication : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppDataContainer(this)
    }
}

// AppContainer.kt
interface AppContainer {
    val database: AppDatabase
    val todoRepository: TodoRepository
    val eventRepository: EventRepository
    val timeBankRepository: TimeBankRepository
    val quotaRepository: QuotaRepository
    val soundPlayer: SoundPlayer
}

class AppDataContainer(private val context: Context) : AppContainer {
    override val database: AppDatabase by lazy {
        Room.databaseBuilder(context, AppDatabase::class.java, "voxplan_database")
            .addMigrations(MIGRATION_2_3, MIGRATION_3_4, ..., MIGRATION_12_13)
            .build()
    }

    override val todoRepository: TodoRepository by lazy {
        TodoRepository(database.todoDao())
    }
    // ... other repositories
}
```

**Access Pattern**:
```kotlin
val application = (context.applicationContext as VoxPlanApplication)
val repository = application.container.todoRepository
```

---

### ViewModel Factory Pattern

**File**: `app/src/main/java/com/voxplanapp/AppViewModelProvider.kt`

```kotlin
object AppViewModelProvider {
    val Factory = viewModelFactory {
        // Shared ViewModel (singleton across factory)
        val sharedViewModel = initializer {
            SharedViewModel()
        }.get(SharedViewModel::class)

        initializer {
            MainViewModel(
                repository = voxPlanApplication().container.todoRepository,
                eventRepository = voxPlanApplication().container.eventRepository,
                timeBankRepository = voxPlanApplication().container.timeBankRepository,
                quotaRepository = voxPlanApplication().container.quotaRepository,
                soundPlayer = voxPlanApplication().container.soundPlayer,
                ioDispatcher = Dispatchers.IO,
                sharedViewModel = sharedViewModel
            )
        }

        initializer {
            SchedulerViewModel(
                this.createSavedStateHandle(),
                voxPlanApplication().container.eventRepository
            )
        }

        // ... other ViewModels
    }
}

// Extension function
fun CreationExtras.voxPlanApplication(): VoxPlanApplication =
    (this[AndroidViewModelFactory.APPLICATION_KEY] as VoxPlanApplication)
```

**Usage in Composable**:
```kotlin
@Composable
fun MainScreen(
    viewModel: MainViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    // ViewModel automatically provided with dependencies
}
```

---

### State Management Patterns

#### Pattern 1: Combined StateFlow (MainViewModel)

**Problem**: Need to combine two data sources (todos + breadcrumbs) with transformation

**Solution**: `combine()` + `stateIn()` with `WhileSubscribed` strategy

```kotlin
val mainUiState: StateFlow<MainUiState> = combine(
    repository.getAllTodos(),           // Flow<List<TodoItem>>
    sharedViewModel.breadcrumbs         // StateFlow<List<GoalWithSubGoals>>
) { todos, breadcrumbs →
    val currentParentId = breadcrumbs.lastOrNull()?.goal?.id
    MainUiState(
        goalList = sharedViewModel.processGoals(todos, currentParentId),
        breadcrumbs = breadcrumbs
    )
}.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5_000L),  // 5-second timeout
    initialValue = MainUiState()
)
```

---

#### Pattern 2: Date-Driven Reactivity (SchedulerViewModel)

**Problem**: Need to re-query events when date changes

**Solution**: `flatMapLatest()` on date StateFlow

```kotlin
private val _currentDate = MutableStateFlow(LocalDate.now())
val currentDate: StateFlow<LocalDate> = _currentDate.asStateFlow()

val eventsForCurrentDate: StateFlow<List<Event>> = _currentDate
    .flatMapLatest { date →
        eventRepository.getScheduledBlocksForDate(date)  // Returns Flow
    }
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = emptyList()
    )
```

---

#### Pattern 3: Compose State to Flow (DailyViewModel)

**Problem**: Need to convert MutableState changes to Flow for database queries

**Solution**: `snapshotFlow()` to create Flow from Compose State

```kotlin
private val _uiState = mutableStateOf(DailyUiState())

init {
    viewModelScope.launch {
        snapshotFlow { _uiState.value.date }
            .flatMapLatest { date →
                eventRepository.getDailiesForDate(date)
            }
            .collect { events →
                _uiState.value = _uiState.value.copy(dailyTasks = events)
            }
    }
}
```

---

#### Pattern 4: Flow Transformations with Side Effects (MainViewModel)

**Problem**: Need null safety and side effects (sound playback)

**Solution**: `map()` for transformation + `onEach()` for side effects

```kotlin
val todayTotalTime: StateFlow<Int> = timeBankRepository.getTotalTimeForDate(LocalDate.now())
    .map { value →
        value ?: 0  // Null safety
    }
    .onEach { minutes →
        // Side effect: Play sound when milestone reached
        val hasDiamond = minutes >= FULLBAR_MINS * 4
        if (hasDiamond && !hadDiamond) {
            soundPlayer.playSound(R.raw.power_up)
            hadDiamond = true
        }
    }
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = 0
    )
```

---

#### Pattern 5: Simple Blocking Reads (ProgressViewModel)

**Problem**: Need data only once at initialization

**Solution**: `Flow.first()` as suspend function

```kotlin
init {
    viewModelScope.launch {
        try {
            val quotas = quotaRepository.getAllQuotas().first()
            val todos = todoRepository.getAllTodos().first()

            // Process data once
            _uiState.value = ProgressUiState(/* ... */)
        } catch (e: Exception) {
            _uiState.value = ProgressUiState(error = e.message)
        }
    }
}
```

---

### ActionMode Reordering System

**File**: `app/src/main/java/com/voxplanapp/model/ActionMode.kt`

```kotlin
sealed class ActionMode {
    object Normal : ActionMode()
    object VerticalUp : ActionMode()
    object VerticalDown : ActionMode()
    object HierarchyUp : ActionMode()
    object HierarchyDown : ActionMode()
}
```

**ActionModeHandler** (in `VoxPlanApp.kt`):
```kotlin
class ActionModeHandler {
    private val _actionMode = mutableStateOf<ActionMode>(ActionMode.Normal)
    val actionMode: State<ActionMode> = _actionMode

    fun toggleVerticalUp() {
        _actionMode.value = if (_actionMode.value is ActionMode.VerticalUp) {
            ActionMode.Normal
        } else {
            ActionMode.VerticalUp
        }
    }

    fun toggleVerticalDown() { /* similar */ }
    fun toggleHierarchyUp() { /* similar */ }
    fun toggleHierarchyDown() { /* similar */ }
    fun deactivateButtons() { _actionMode.value = ActionMode.Normal }
}
```

**Reordering Logic** (MainViewModel):
```kotlin
fun reorderItem(item: TodoItem, actionMode: ActionMode) {
    viewModelScope.launch {
        when (actionMode) {
            is ActionMode.VerticalUp → {
                // Find item above, swap orders
                val items = repository.getAllTodos().first()
                    .filter { it.parentId == item.parentId }
                    .sortedBy { it.order }
                val currentIndex = items.indexOfFirst { it.id == item.id }
                if (currentIndex > 0) {
                    val above = items[currentIndex - 1]
                    repository.updateItemsInTransaction(
                        listOf(
                            item.copy(order = above.order),
                            above.copy(order = item.order)
                        )
                    )
                }
            }
            // ... other modes
        }
    }
}
```

---

### Database Migration Pattern

**File**: `app/src/main/java/com/voxplanapp/data/AppDatabase.kt:384`

```kotlin
@Database(entities = [TodoItem::class, Event::class, TimeBank::class, Quota::class], version = 13)
abstract class AppDatabase : RoomDatabase() {

    companion object {
        val MIGRATION_12_13 = object : Migration(12, 13) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE Event ADD COLUMN completedDuration INTEGER"
                )
            }
        }

        // Builder usage
        Room.databaseBuilder(context, AppDatabase::class.java, "voxplan_database")
            .addMigrations(
                MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6,
                MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10,
                MIGRATION_10_11, MIGRATION_11_12, MIGRATION_12_13
            )
            .build()
    }
}
```

**CRITICAL**: Increment version in `@Database(version = X)` annotation when adding migration.

---

## Major Data Models and State Management

### Type Converters

**File**: `app/src/main/java/com/voxplanapp/data/Converters.kt`

```kotlin
class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): LocalDate? {
        return value?.let { LocalDate.ofEpochDay(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: LocalDate?): Long? {
        return date?.toEpochDay()
    }

    @TypeConverter
    fun fromTimeString(value: String?): LocalTime? {
        return value?.let { LocalTime.parse(it) }
    }

    @TypeConverter
    fun timeToString(time: LocalTime?): String? {
        return time?.toString()
    }
}
```

Applied to database:
```kotlin
@TypeConverters(Converters::class)
@Database(...)
abstract class AppDatabase : RoomDatabase()
```

---

## External Dependencies and Integrations

### Build Configuration

**File**: `app/build.gradle.kts`

**Key Dependencies**:
```kotlin
// Kotlin
kotlin("android") version "1.9.0"

// Compose
val composeBom = platform("androidx.compose:compose-bom:2023.08.00")
implementation(composeBom)
implementation("androidx.compose.ui:ui")
implementation("androidx.compose.material3:material3")
implementation("androidx.compose.ui:ui-tooling-preview")

// Navigation
implementation("androidx.navigation:navigation-compose:2.7.7")

// Room
val roomVersion = "2.6.1"
implementation("androidx.room:room-runtime:$roomVersion")
implementation("androidx.room:room-ktx:$roomVersion")
ksp("androidx.room:room-compiler:$roomVersion")

// Lifecycle
implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")

// Coroutines
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

// ExoPlayer (for sound effects)
implementation("androidx.media3:media3-exoplayer:1.1.1")
```

**Android Configuration**:
```kotlin
android {
    namespace = "com.voxplanapp"
    compileSdk = 34

    defaultConfig {
        minSdk = 27
        targetSdk = 34
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
}
```

**Room Schema Export**:
```kotlin
ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}
```
Schemas exported to: `app/schemas/` (v1 through v13)

---

## Notable Patterns, Conventions, and Anti-Patterns

### Architectural Decisions

**1. Manual DI Over Hilt**

**Rationale**: Simplicity, transparency, no reflection overhead

**Benefits**:
- Easy to understand (explicit dependencies visible in AppContainer)
- No magic or hidden behavior
- Smaller APK size (no reflection)
- Easier debugging (no generated code)

**Tradeoffs**:
- More boilerplate (manual ViewModel factory)
- No automatic scoping (must manage manually)

---

**2. Hybrid State Management (StateFlow + Compose State)**

**Rationale**: Different screens need different approaches

**When to use StateFlow**:
- Reactive data from repositories
- Cross-screen shared state (SharedViewModel breadcrumbs)
- Long-lived state that outlives recomposition

**When to use Compose State (mutableStateOf)**:
- UI-local transient state (dialog visibility, text input)
- Performance-critical UI updates (FocusViewModel timer)
- Simple state that doesn't need persistence

**Example** (FocusViewModel):
```kotlin
// Compose State for high-frequency timer updates
val focusUiState: MutableState<FocusUiState> = mutableStateOf(FocusUiState())

// StateFlow for job lifecycle management
private val _timerJob = MutableStateFlow<Job?>(null)
val timerJob: StateFlow<Job?> = _timerJob.asStateFlow()
```

---

**3. Shared ViewModel Pattern**

**Problem**: Breadcrumb trail must persist across multiple screens

**Solution**: Singleton SharedViewModel injected into multiple feature ViewModels

**Implementation**:
```kotlin
// AppViewModelProvider.kt
val sharedViewModel = initializer { SharedViewModel() }.get(SharedViewModel::class)

initializer {
    MainViewModel(/* ... */, sharedViewModel = sharedViewModel)
}

initializer {
    GoalEditViewModel(/* ... */, sharedViewModel = sharedViewModel)
}
```

**Benefits**:
- Single source of truth for breadcrumbs
- No prop drilling through UI layers
- Automatic state sharing without extra plumbing

**Tradeoff**: Tight coupling between feature ViewModels and SharedViewModel

---

**4. Parent-Child Events in Single Table**

**Rationale**: Flexible representation, simpler cascading deletes

**Database Design**:
```kotlin
// Single Event entity
data class Event(
    val id: Int,
    val parentDailyId: Int? = null,  // NULL = daily, value = scheduled
    val startTime: String? = null,   // NULL for dailies
    val endTime: String? = null,     // NULL for dailies
    // ...
)
```

**DAO Queries**:
```kotlin
@Query("SELECT * FROM Event WHERE parentDailyId IS NULL")  // Dailies
fun getDailies(): Flow<List<Event>>

@Query("SELECT * FROM Event WHERE parentDailyId IS NOT NULL")  // Scheduled
fun getScheduledBlocks(): Flow<List<Event>>
```

**Benefits**:
- Single entity type (less code)
- Foreign key cascading delete (delete parent → deletes children)
- Flexible schema (easy to add fields for both types)

**Tradeoffs**:
- Nullable fields require careful null handling
- Query complexity (filtering by parentDailyId)
- Less type safety (compiler can't distinguish dailies from scheduled)

---

**5. Quota Active Days String Encoding**

**Rationale**: Compact storage for 7 boolean values

**Pattern**:
```kotlin
// Encoding
val activeDays: BooleanArray = booleanArrayOf(true, true, true, true, true, false, false)
val encoded: String = activeDays.joinToString("") { if (it) "1" else "0" }
// Result: "1111100"

// Decoding
val activeDaysString = "1111100"
val decoded = activeDaysString.map { it == '1' }.toBooleanArray()

// Checking if day is active
val dayOfWeekIndex = LocalDate.now().dayOfWeek.value - 1  // 0-based
val isActive = quota.activeDays[dayOfWeekIndex] == '1'
```

**Benefits**:
- Compact (7 bytes vs 7 booleans)
- Human-readable in database
- Simple indexing

**Tradeoff**: String manipulation instead of boolean operations

---

### Code Style Conventions

**ViewModel State Pattern**:
```kotlin
// Private mutable state
private val _state = MutableStateFlow<UiState>(UiState())

// Public immutable state
val state: StateFlow<UiState> = _state.asStateFlow()
```

**UI State Data Classes**:
```kotlin
data class MainUiState(
    val goalList: List<GoalWithSubGoals> = emptyList(),
    val breadcrumbs: List<GoalWithSubGoals> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
```

**Repository Methods**:
```kotlin
// Reactive queries return Flow
fun getAllTodos(): Flow<List<TodoItem>>

// Write operations are suspend functions
suspend fun insert(item: TodoItem)

// Blocking reads use first() (rare)
val todos = repository.getAllTodos().first()
```

**Composable Patterns**:
```kotlin
// ViewModels injected at screen level
@Composable
fun MainScreen(
    viewModel: MainViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.mainUiState.collectAsState()
    // ...
}

// State passed down, events passed up
@Composable
fun GoalItem(
    goal: TodoItem,
    onGoalClick: (TodoItem) -> Unit,
    onDeleteClick: (TodoItem) -> Unit
)
```

**Logging**:
```kotlin
Log.d("MainViewModel", "Navigating to goal: ${goal.title}")
```

**Package Structure**:
```
com.voxplanapp/
├── data/           # Entities, DAOs, Repositories, Database
├── model/          # Domain models (ActionMode)
├── shared/         # Shared ViewModels and utilities
├── navigation/     # Navigation setup
└── ui/
    ├── main/       # Feature: Goal hierarchy
    ├── daily/      # Feature: Daily planning
    ├── calendar/   # Feature: Scheduling
    ├── goals/      # Feature: Goal editing & progress
    ├── focusmode/  # Feature: Focus timer
    ├── constants/  # UI constants
    └── theme/      # Material theme
```

---

### Known Issues and Anti-Patterns

**1. Critical Bug: Delete Dialog in DaySchedule.kt**

**Location**: `app/src/main/java/com/voxplanapp/ui/calendar/DaySchedule.kt:110-126`

**Problem**: Delete confirmation dialog references undefined `event` variable

```kotlin
// BUGGY CODE
showDeleteParentDialog?.let { parentId →
    AlertDialog(
        onDismissRequest = { viewModel.dismissDeleteParentDialog() },
        confirmButton = {
            TextButton(onClick = {
                viewModel.confirmDeleteChildOnly(event)  // ERROR: 'event' undefined
            }) { Text("Delete This Only") }
        },
        dismissButton = {
            TextButton(onClick = {
                viewModel.confirmDeleteWithParent(event)  // ERROR: 'event' undefined
            }) { Text("Delete Both") }
        }
        // ...
    )
}
```

**Fix Required**: Use `showDeleteParentDialog.value` (the Event) instead of `event`

---

**2. QuickScheduleScreen.kt Entirely Commented Out**

**Location**: `app/src/main/java/com/voxplanapp/ui/main/QuickScheduleScreen.kt:26-86`

**Problem**: Entire composable is commented out, references undefined variables

**Status**: Incomplete feature, should either be implemented or removed from codebase

---

**3. Scroll Position Not Persisted in DaySchedule**

**Problem**: When user changes date, scroll position resets to 6 AM

**Expected**: Remember scroll position per day or at least maintain current position

**Impact**: User experience degradation for users who work late/early hours

---

## Development Guidelines

### Build Commands

```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Clean build
./gradlew clean build

# Run unit tests
./gradlew test

# Run instrumented tests (requires emulator/device)
./gradlew connectedAndroidTest

# Lint checks
./gradlew lint

# Install debug build on connected device
./gradlew installDebug
```

---

### Adding a New ViewModel

**Step 1**: Create data classes for UI state in ViewModel file

```kotlin
data class MyUiState(
    val data: List<MyData> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
```

**Step 2**: Create ViewModel class

```kotlin
class MyViewModel(
    private val repository: MyRepository,
    private val sharedViewModel: SharedViewModel  // if needed
) : ViewModel() {

    private val _uiState = MutableStateFlow(MyUiState())
    val uiState: StateFlow<MyUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            try {
                repository.getData().collect { data →
                    _uiState.update { it.copy(data = data, isLoading = false) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }
}
```

**Step 3**: Add to `AppViewModelProvider.Factory`

```kotlin
// AppViewModelProvider.kt
initializer {
    MyViewModel(
        repository = voxPlanApplication().container.myRepository,
        sharedViewModel = sharedViewModel  // if needed
    )
}
```

**Step 4**: Use in Composable

```kotlin
@Composable
fun MyScreen(
    viewModel: MyViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()

    // Render UI based on uiState
}
```

---

### Database Migration Procedure

**Step 1**: Update entity

```kotlin
@Entity(tableName = "MyEntity")
data class MyEntity(
    @PrimaryKey val id: Int,
    val newField: String  // Added field
)
```

**Step 2**: Create migration

```kotlin
// AppDatabase.kt
companion object {
    val MIGRATION_13_14 = object : Migration(13, 14) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE MyEntity ADD COLUMN newField TEXT NOT NULL DEFAULT ''")
        }
    }
}
```

**Step 3**: Increment database version

```kotlin
@Database(entities = [...], version = 14)  // Changed from 13
abstract class AppDatabase : RoomDatabase()
```

**Step 4**: Add migration to builder

```kotlin
Room.databaseBuilder(...)
    .addMigrations(
        MIGRATION_2_3, MIGRATION_3_4, ..., MIGRATION_12_13, MIGRATION_13_14  // Added
    )
    .build()
```

**Step 5**: Test migration

```bash
# Uninstall app to clear old database
adb uninstall com.voxplanapp

# Install new version
./gradlew installDebug

# Verify schema export
ls app/schemas/com.voxplanapp.data.AppDatabase/14.json
```

---

### Testing Priorities

**High Priority Tests**:

1. **SharedViewModel.processGoals()**
   - Test hierarchical processing up to depth 3
   - Test filtering by parentId
   - Test sorting by order
   - Test depth limit enforcement

2. **TodoRepository.updateItemsInTransaction()**
   - Test atomic batch updates
   - Test transaction rollback on error

3. **Event parent-child relationships**
   - Test DAO queries (dailies vs scheduled filtering)
   - Test cascading deletes

4. **Quota active days encoding**
   - Test encoding: BooleanArray → String
   - Test decoding: String → BooleanArray
   - Test day-of-week indexing

5. **FocusViewModel timer state**
   - Test medal award thresholds
   - Test pause/resume behavior
   - Test job lifecycle management

6. **Database migrations**
   - Test migration 12→13
   - Test migration chain 2→13

---

## Critical Files & Line Counts

| File | Lines | Category | Description | Priority |
|------|-------|----------|-------------|----------|
| **FocusModeScreen.kt** | 697 | UI | Timer UI with medals and discrete tasks | High |
| **DaySchedule.kt** | 675 | UI | Day scheduler **[BUG at lines 110-126]** | Critical |
| **DailyScreen.kt** | 628 | UI | Daily planning with quota integration | High |
| **MainScreen.kt** | 492 | UI | Goal hierarchy with ActionMode | High |
| **FocusViewModel.kt** | 492 | ViewModel | Complex timer state management | High |
| **AppDatabase.kt** | 384 | Data | Room database with 13 migrations | Core |
| **MainViewModel.kt** | 362 | ViewModel | Goal list state with breadcrumbs | Core |
| **GoalEditScreen.kt** | 292 | UI | Goal editing with quotas | Medium |
| **DailyViewModel.kt** | 190 | ViewModel | Daily task state | Medium |
| **GoalEditViewModel.kt** | 191 | ViewModel | Goal edit state | Medium |
| **ProgressViewModel.kt** | 174 | ViewModel | Weekly progress tracking | Medium |
| **SchedulerViewModel.kt** | 141 | ViewModel | Scheduler date reactivity | Medium |
| **SharedViewModel.kt** | 80 | ViewModel | Breadcrumb navigation (recursive) | Core |
| **TodoRepository.kt** | 76 | Data | Goal CRUD with recursive delete | Core |
| **TimeBankRepository.kt** | 56 | Data | Time tracking operations | Medium |
| **QuotaRepository.kt** | 47 | Data | Quota management | Medium |
| **EventRepository.kt** | 32 | Data | Event CRUD with parent/child filtering | Core |

---

## Constants & Magic Numbers

**File**: `app/src/main/java/com/voxplanapp/data/Constants.kt`

```kotlin
const val FULLBAR_MINS = 60              // Minutes to fill one power bar
const val pointsForItemCompletion = 15   // Bonus minutes for completing discrete task
```

**Hardcoded Values**:

```kotlin
// SharedViewModel.kt:15
private val MAX_DEPTH = 3  // Maximum goal hierarchy depth

// MainViewModel.kt:68
SharingStarted.WhileSubscribed(5_000L)  // StateFlow timeout (5 seconds)

// DaySchedule.kt (approx line 150)
val hourHeight = 48.dp                   // Visual height of 1-hour block
val startHour = 1                        // Display starts at 1 AM
val endHour = 24                         // Display ends at midnight

// DaySchedule.kt (scroll position)
initialScrollOffset = hourHeight * 5     // Default scroll to 6 AM (hour 6 = index 5)

// FocusViewModel.kt (medal thresholds)
val BRONZE_MINUTES = 30
val SILVER_MINUTES = 60
val GOLD_MINUTES = 90
val DIAMOND_MINUTES = 120

// DaySchedule.kt (drag snapping)
val SNAP_INTERVAL_MINUTES = 15           // Snap to 15-minute intervals

// FocusViewModel.kt (timer intervals)
val DEFAULT_WORK_MINUTES = 25            // Pomodoro work period
val DEFAULT_REST_MINUTES = 5             // Pomodoro rest period
```

---

## Document Metadata

**Generated**: December 19, 2025
**Project Version**: VoxPlanApp 3.2 (Dailies improved with parent/child Events)
**Database Version**: Room v13
**Purpose**: Technical implementation guide for developers and AI assistants
**Related Documents**:
- `project_context.md` - High-level project vision and workflows
- `docs/LLM-Generated/ARCHITECTURE_DOCUMENTATION_SUMMARY.txt` - Architecture summary
- `docs/LLM-Generated/INCOMPLETE_FEATURES.md` - Feature completion analysis

**For Conceptual Understanding**: Refer to `project_context.md` for user-facing workflows, domain concepts, and product vision.
