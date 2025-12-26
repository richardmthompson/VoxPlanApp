# VoxPlanApp Architecture Documentation

## Executive Summary

VoxPlanApp is an Android productivity application built with **Jetpack Compose** following **clean architecture principles**. It implements a state-driven, unidirectional data flow pattern using modern Kotlin coroutines and reactive programming with Flow.

**Architecture Pattern:** MVVM with manual Dependency Injection
**State Management:** StateFlow + Compose State
**Database:** Room (SQLite)
**Dependency Injection:** Manual (AppContainer pattern)

---

## 1. STATE MANAGEMENT ARCHITECTURE

### 1.1 Overall State Management Pattern

The app uses a **hybrid state management approach**:
- **StateFlow-based state** for reactive, observable data (data layer)
- **Compose State** (mutableStateOf) for UI-local, transient state
- **SharedViewModel** for cross-screen state management
- **ActionMode state** for hierarchical navigation and reordering

### 1.2 ViewModel Structure

#### Core ViewModels (by feature):

**MainViewModel** (/app/src/main/java/com/voxplanapp/ui/main/MainViewModel.kt)
- Manages goal list with hierarchical breadcrumb navigation
- Combines `getAllTodos()` Flow with `breadcrumbs` StateFlow from SharedViewModel
- **Key StateFlow:**
  - `mainUiState: StateFlow<MainUiState>` - Combined goal list + breadcrumbs
  - `todayTotalTime: StateFlow<Int>` - Time bank total for today
- **State Data Class:**
  ```kotlin
  data class MainUiState(
      val goalList: List<GoalWithSubGoals> = listOf(),
      val breadcrumbs: List<GoalWithSubGoals> = listOf()
  )
  ```

**SharedViewModel** (/app/src/main/java/com/voxplanapp/shared/SharedViewModel.kt)
- Global breadcrumb trail management across all screens
- No dependencies; pure business logic for goal hierarchy processing
- **Key StateFlow:**
  - `breadcrumbs: StateFlow<List<GoalWithSubGoals>>` - Navigation trail
- **Public Methods:**
  - `processGoals(todos, parentId, depth)` - Recursive goal hierarchy builder (max depth: 3)
  - `navigateToSubGoal(goal, parentGoal)` - Breadcrumb navigation logic
  - `navigateUp()` - Pop last breadcrumb
  - `getGoalWithSubGoals(todos, goalId)` - Retrieve single goal with siblings

**DailyViewModel** (/app/src/main/java/com/voxplanapp/ui/daily/DailyViewModel.kt)
- Daily schedule and quota management
- **Key StateFlow:**
  - `uiState: StateFlow<DailyUiState>` - Current date + daily tasks
  - `showDeleteConfirmation: StateFlow<Event?>` - Delete confirmation dialog
- **State Data Class:**
  ```kotlin
  data class DailyUiState(
      val date: LocalDate = LocalDate.now(),
      val dailyTasks: List<Event> = emptyList(),
      val isLoading: Boolean = true,
      val error: String? = null,
      val eventNeedingDuration: Int? = null
  )
  ```
- **Reactive Updates:** Uses `snapshotFlow` to convert date changes to Flow stream

**SchedulerViewModel** (/app/src/main/java/com/voxplanapp/ui/calendar/SchedulerViewModel.kt)
- Day-by-day event scheduler
- **Key StateFlow:**
  - `currentDate: StateFlow<LocalDate>` - Currently viewing date
  - `eventsForCurrentDate: StateFlow<List<Event>>` - Scheduled events (filtered)
  - `showDeleteParentDialog: StateFlow<Event?>` - Parent deletion confirmation
- **Reactive Pattern:** `flatMapLatest` + `collect` to react to date changes

**FocusViewModel** (/app/src/main/java/com/voxplanapp/ui/focusmode/FocusViewModel.kt)
- Most complex state management - focus timer, medals, discrete tasks
- **Key State:**
  - `focusUiState: MutableState<FocusUiState>` - Compose State (not StateFlow)
  - `timerSettingsState: MutableState<TimerSettingsState>` - Pomodoro settings
  - `goalUiState: MutableState<GoalWithSubGoals?>` - Loaded goal
  - `eventUiState: MutableState<Event?>` - Loaded event
  - `_timerJob: StateFlow<Job?>` - Timer coroutine job reference
  - `_discreteTaskJob: StateFlow<Job?>` - Discrete task coroutine job reference
- **State Data Classes:**
  ```kotlin
  data class FocusUiState(
      val isLoading: Boolean = true,
      val error: String? = null,
      val currentTheme: ColorScheme = ColorScheme.REST,
      val eventId: Int? = null,
      val isFromEvent: Boolean = false,
      val startTime: LocalTime? = null,
      val endTime: LocalTime? = null,
      val date: LocalDate? = null,
      val timerStarted: Boolean = false,
      val clockProgress: Float = 0f,
      val medals: List<Medal> = emptyList(),
      val totalAccruedTime: Long = 0L,
      val currentTime: Long = 0L,
      val timerState: TimerState = TimerState.IDLE,
      val clockFaceMins: Float = 30f,
      val isRestPeriod: Boolean = false,
      val isDiscreteMode: Boolean = false,
      val discreteTaskState: DiscreteTaskState = DiscreteTaskState.IDLE,
      val currentTaskLevel: DiscreteTaskLevel = DiscreteTaskLevel.EASY,
  )
  
  data class TimerSettingsState(
      val workDuration: Int = 5,
      val restDuration: Int = 1,
      val usePomodoro: Boolean = false,
  )
  ```

**GoalEditViewModel** (/app/src/main/java/com/voxplanapp/ui/goals/GoalEditViewModel.kt)
- Goal editing with quota settings
- **Key State:**
  - `goalUiState: MutableState<GoalUiState>` - Compose State
- **State Data Class:**
  ```kotlin
  data class GoalUiState(
      val goal: GoalWithSubGoals?,
      val isLoading: Boolean = true,
      val error: String? = null,
      val quotaMinutes: Int = 60,
      val quotaActiveDays: Set<DayOfWeek> = setOf()
  )
  ```

**ProgressViewModel** (/app/src/main/java/com/voxplanapp/ui/goals/ProgressViewModel.kt)
- Weekly quota progress tracking
- **Key StateFlow:**
  - `uiState: StateFlow<ProgressUiState>` - Weekly progress data
- **State Data Classes:**
  ```kotlin
  data class ProgressUiState(
      val currentWeek: LocalDate = LocalDate.now().with(DayOfWeek.MONDAY),
      val dailyProgress: List<DayProgress> = emptyList(),
      val weekTotal: WeekTotal = WeekTotal(emptyList()),
      val completedDays: Int = 0
  )
  
  data class DayProgress(
      val dayOfWeek: DayOfWeek,
      val goalProgress: List<GoalProgress>,
      val isComplete: Boolean,
      val diamonds: Int
  )
  ```

**NavigationViewModel** (/app/src/main/java/com/voxplanapp/navigation/NavigationViewModel.kt)
- Bottom navigation bar state
- **Key StateFlow:**
  - `selectedItemIndex: StateFlow<Int>` - Active bottom nav item

### 1.3 Shared State Pattern

**ActionMode System:**
- Used for reordering goals and events
- **File:** /app/src/main/java/com/voxplanapp/model/ActionMode.kt
- **States:**
  ```kotlin
  sealed class ActionMode {
      object Normal : ActionMode()
      object VerticalUp : ActionMode()
      object VerticalDown : ActionMode()
      object HierarchyUp : ActionMode()
      object HierarchyDown : ActionMode()
  }
  ```
- **Handler:** ActionModeHandler in VoxPlanApp.kt manages state mutations
- **Usage:** MainViewModel and DailyViewModel share action mode state with UI buttons

### 1.4 State Collection & Subscription Patterns

**SharingStarted Strategies:**
```kotlin
// MainViewModel - WhileSubscribed with 5 second timeout
.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5_000L),
    initialValue = MainUiState()
)

// SchedulerViewModel - Uses init block with launch + collect pattern
init {
    viewModelScope.launch {
        _currentDate
            .flatMapLatest { date ->
                eventRepository.getEventsForDate(date)
                    .map { events -> /* filter logic */ }
            }
            .collect { events ->
                _eventsForCurrentDate.value = events
            }
    }
}
```

---

## 2. DATA LAYER ARCHITECTURE

### 2.1 Data Architecture Overview

```
┌─────────────────────────────────────────┐
│         UI Layer (Composables)          │
├─────────────────────────────────────────┤
│         ViewModels (State Holders)      │
├─────────────────────────────────────────┤
│    Repositories (Data Aggregators)      │
├─────────────────────────────────────────┤
│         DAOs (Database Access)          │
├─────────────────────────────────────────┤
│    Room Database (SQLite)               │
└─────────────────────────────────────────┘
```

### 2.2 Repository Pattern Implementation

**Repository Base Design:**
- Single responsibility: abstract data source access
- Wrap DAO operations (suspend functions + Flow)
- No business logic; thin wrapper layer
- Always expose Flow for reactivity

**Repositories (All in /app/src/main/java/com/voxplanapp/data/):**

**TodoRepository.kt**
```kotlin
class TodoRepository(private val todoDao: TodoDao) {
    fun getAllTodos(): Flow<List<TodoItem>>
    fun getItemStream(id: Int): Flow<TodoItem?>
    fun getRootTodos(): List<TodoItem>  // Blocking
    fun getChildrenOf(parentId: Int): List<TodoItem>  // Blocking
    suspend fun insert(todo: TodoItem)
    suspend fun updateItem(todo: TodoItem)
    suspend fun completeItem(todoItem: TodoItem)  // Toggle completion
    suspend fun updateItemsInTransaction(items: List<TodoItem>)
    suspend fun deleteItemAndDescendents(todo: TodoItem)  // Recursive
    suspend fun expandItem(todoId: Int, expand: Boolean)
}
```

**EventRepository.kt**
```kotlin
class EventRepository(private val eventDao: EventDao) {
    fun getDailiesForDate(date: LocalDate): Flow<List<Event>>
    fun getEventsForDate(date: LocalDate): Flow<List<Event>>
    fun getEventsWithParentId(parentId: Int): Flow<List<Event>>
    fun getScheduledBlocksForDate(date: LocalDate): Flow<List<Event>>
    fun getScheduledBlocksForDaily(dailyId: Int): Flow<List<Event>>
    suspend fun getEvent(eventId: Int): Event
    suspend fun insertEvent(event: Event): Int
    suspend fun updateEvent(event: Event)
    suspend fun deleteEvent(eventId: Int)
    suspend fun updateEventOrder(eventId: Int, newOrder: Int)
}
```

**TimeBankRepository.kt**
```kotlin
class TimeBankRepository(private val timeBankDao: TimeBankDao) {
    suspend fun addTimeBankEntry(goalId: Int, duration: Int)
    fun getEntriesForGoal(goal: Int): Flow<List<TimeBank>>
    fun getEntriesForDate(date: LocalDate): Flow<List<TimeBank>>
    fun getEntriesForDateRange(startDate: LocalDate, endDate: LocalDate): Flow<List<TimeBank>>
    fun getTotalTimeForGoal(goalId: Int): Flow<Int?>
    fun getTotalTimeForDate(date: LocalDate): Flow<Int?>
    suspend fun deleteCompletionBonus(goalId: Int, bonusAmount: Int)
}
```

**QuotaRepository.kt**
```kotlin
class QuotaRepository(private val quotaDao: QuotaDao) {
    fun getQuotaForGoal(goalId: Int): Flow<Quota?>
    fun getAllQuotas(): Flow<List<Quota>>
    fun getAllActiveQuotas(date: LocalDate): Flow<List<Quota>>
    suspend fun insertQuota(quota: Quota)
    suspend fun updateQuota(quota: Quota)
    suspend fun deleteQuota(quota: Quota)
    suspend fun deleteQuotaForGoal(goalId: Int)
    fun getQuotasForGoals(goalIds: List<Int>): Flow<List<Quota>>
    fun isQuotaActiveForDate(quota: Quota, date: LocalDate): Boolean
    fun getActiveDays(quota: Quota): List<DayOfWeek>
}
```

### 2.3 Data Entities

**TodoItem** (/app/src/main/java/com/voxplanapp/data/TodoItem.kt)
```kotlin
@Entity
data class TodoItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    var title: String,
    var parentId: Int? = null,  // Hierarchical: null = root goal
    var order: Int = 0,           // Display order within parent
    var notes: String? = null,
    var isDone: Boolean = false,  // Legacy field
    var preferredTime: LocalTime? = null,
    var estDurationMins: Int? = null,
    var frequency: RecurrenceType = RecurrenceType.NONE,
    var expanded: Boolean = true,  // Tree expansion state
    var completedDate: LocalDate? = null  // Completion tracking
)
```

**Event** (/app/src/main/java/com/voxplanapp/data/Event.kt)
```kotlin
@Entity
data class Event(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val goalId: Int,                      // Links to TodoItem
    val title: String,
    val startTime: LocalTime? = null,
    val endTime: LocalTime? = null,
    val startDate: LocalDate,
    val recurrenceType: RecurrenceType = RecurrenceType.NONE,
    val recurrenceInterval: Int? = 0,
    val recurrenceEndDate: LocalDate? = null,
    val color: Int? = null,
    val order: Int = 0,
    val quotaDuration: Int? = null,       // Expected duration from quota
    val scheduledDuration: Int? = null,   // Scheduled time (sum of children)
    val completedDuration: Int? = null,   // Actual time from time bank
    val parentDailyId: Int? = null        // If set, this is a scheduled block under a daily
)

enum class RecurrenceType {
    NONE, DAILY, WEEKLY, MONTHLY, YEARLY
}
```

**TimeBank** (/app/src/main/java/com/voxplanapp/data/TimeBankEntry.kt)
```kotlin
@Entity
data class TimeBank(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "goal_id") val goalId: Int,
    val date: LocalDate,
    val duration: Int  // Minutes
)
```

**Quota** (/app/src/main/java/com/voxplanapp/data/QuotaEntity.kt)
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
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val goalId: Int,
    val dailyMinutes: Int,
    val activeDays: String  // "1111100" for Mon-Fri; index = dayOfWeek - 1
)
```

**Composite Model:**
```kotlin
data class GoalWithSubGoals(
    val goal: TodoItem,
    val subGoals: List<GoalWithSubGoals>  // Recursive structure
)
```

### 2.4 DAO Pattern

**TodoDao** (/app/src/main/java/com/voxplanapp/data/TodoDao.kt)
```kotlin
@Dao
interface TodoDao {
    @Query("SELECT * FROM TodoItem")
    fun getAllTodos(): Flow<List<TodoItem>>
    
    @Query("SELECT * from TodoItem WHERE id = :id")
    fun getItem(id: Int): Flow<TodoItem>
    
    @Query("SELECT * FROM TodoItem WHERE parentID = null")
    fun getRootTodos(): List<TodoItem>
    
    @Query("SELECT * FROM TodoItem WHERE id IN (:ids)")
    fun getItemsByIds(ids: List<Int>): Flow<List<TodoItem>>
    
    @Query("SELECT * FROM TodoItem WHERE parentID = :parentId")
    fun getChildrenOf(parentId: Int): List<TodoItem>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(todo: TodoItem)
    
    @Update
    suspend fun update(todo: TodoItem)
    
    @Query("DELETE FROM TodoItem WHERE id = :id")
    suspend fun deleteById(id: Int)
    
    @Transaction
    suspend fun deleteItemAndDescendants(goalId: Int)  // Recursive helper
    
    @Transaction
    suspend fun updateItemsInTransaction(todos: List<TodoItem>)
}
```

**EventDao** (/app/src/main/java/com/voxplanapp/data/EventDao.kt)
- Key distinction: `getDailiesForDate()` filters `parentDailyId IS NULL`
- `getScheduledBlocksForDate()` filters `parentDailyId IS NOT NULL`
- This design allows parent-child relationships for daily/scheduled event hierarchy

**TimeBankDao & QuotaDao** - Standard CRUD + specialized queries

### 2.5 Database Setup

**AppDatabase** (/app/src/main/java/com/voxplanapp/data/AppDatabase.kt)
- Current version: 13
- Entities: TodoItem, Event, TimeBank, Quota
- Extensive migration strategy (MIGRATION_2_3 through MIGRATION_12_13)
- Type converters for LocalDate, LocalTime
- Database callback logs for debug

**Converters** (/app/src/main/java/com/voxplanapp/data/Converters.kt)
```kotlin
class Converters {
    @TypeConverter
    fun fromTimeString(value: String?): LocalTime?
    
    @TypeConverter
    fun timeToString(time: LocalTime?): String?
    
    @TypeConverter
    fun fromDateLong(value: Long?): LocalDate?  // Stores as epoch day
    
    @TypeConverter
    fun dateToLong(date: LocalDate?): Long?
}
```

### 2.6 Dependency Injection Pattern

**AppContainer Interface** (/app/src/main/java/com/voxplanapp/data/AppContainer.kt)
```kotlin
interface AppContainer {
    val database: AppDatabase
    val todoRepository: TodoRepository
    val eventRepository: EventRepository
    val timeBankRepository: TimeBankRepository
    val quotaRepository: QuotaRepository
    val soundPlayer: SoundPlayer
}

class AppDataContainer(private val context: Context) : AppContainer {
    override val database: AppDatabase by lazy { /* Room.databaseBuilder setup */ }
    override val todoRepository: TodoRepository by lazy { 
        TodoRepository(database.todoDao())
    }
    // ... other repositories with lazy initialization
    override val soundPlayer: SoundPlayer by lazy {
        SoundPlayer(context)
    }
}
```

**Service Locator in Application Class** (/app/src/main/java/com/voxplanapp/VoxPlanApplication.kt)
```kotlin
class VoxPlanApplication: Application() {
    lateinit var container: AppContainer
    
    override fun onCreate() {
        super.onCreate()
        container = AppDataContainer(this)
    }
}
```

**ViewModel Factory** (/app/src/main/java/com/voxplanapp/AppViewModelProvider.kt)
```kotlin
object AppViewModelProvider {
    val Factory = viewModelFactory {
        val sharedViewModel = SharedViewModel()
        
        initializer { sharedViewModel }
        
        initializer {
            MainViewModel(
                voxPlanApplication().container.todoRepository,
                voxPlanApplication().container.eventRepository,
                voxPlanApplication().container.timeBankRepository,
                soundPlayer = voxPlanApplication().container.soundPlayer,
                ioDispatcher = Dispatchers.IO,
                sharedViewModel = sharedViewModel
            )
        }
        // ... other initializers
    }
}
```

**Scopes:**
- **Application scope:** Database, repositories (singleton)
- **Activity scope:** Navigation controller
- **Screen scope:** ViewModel with viewModelScope for coroutines
- **Shared scope:** SharedViewModel injected into multiple ViewModels

---

## 3. REACTIVE PROGRAMMING & FLOW PATTERNS

### 3.1 Flow Usage Patterns

**Pattern 1: Simple Flow Subscription**
```kotlin
// ProgressViewModel - basic flow collection
val quotas = quotaRepository.getAllQuotas().first()  // Suspend
val goals = todoRepository.getItemsByIds(goalIds).first()
```

**Pattern 2: Flow Combination with combine**
```kotlin
// MainViewModel - reactive updates to goal list
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

**Pattern 3: flatMapLatest for Date-Driven State**
```kotlin
// SchedulerViewModel - react to date changes
init {
    viewModelScope.launch {
        _currentDate
            .flatMapLatest { date ->
                eventRepository.getEventsForDate(date)
                    .map { events ->
                        events.filter { event ->
                            event.startTime != null && event.endTime != null &&
                            event.parentDailyId != null
                        }
                    }
            }
            .collect { events ->
                _eventsForCurrentDate.value = events
            }
    }
}
```

**Pattern 4: snapshotFlow for State Changes**
```kotlin
// DailyViewModel - convert Compose State to Flow
init {
    viewModelScope.launch {
        snapshotFlow { _uiState.value.date }
            .flatMapLatest { date ->
                eventRepository.getDailiesForDate(date)
            }
            .collect { events ->
                _uiState.value = _uiState.value.copy(
                    dailyTasks = events,
                    isLoading = false
                )
            }
    }
}
```

**Pattern 5: Flow with map for Transformations**
```kotlin
// MainViewModel - null safety transformation
todayTotalTime: StateFlow<Int> = timeBankRepository
    .getTotalTimeForDate(LocalDate.now())
    .map { value ->
        Log.d("datedebug", "Raw value from repository: $value")
        value ?: 0  // Default to 0 if null
    }
    .onEach { minutes ->
        // Side effect: play sound on milestone
        val hasDiamond = minutes >= FULLBAR_MINS * 4
        if (hasDiamond && !hadDiamond) {
            soundPlayer.playSound(R.raw.power_up)
            hadDiamond = true
        }
    }
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )
```

### 3.2 Error Handling

**Strategy:** Try-catch in ViewModel initialization
```kotlin
// FocusViewModel
private fun loadInitialData() {
    viewModelScope.launch {
        try {
            when {
                eventId != null -> loadEventData(eventId)
                goalId != null -> loadGoalData(goalId)
                else -> throw IllegalArgumentException("Neither eventId or goalId provided")
            }
        } catch (e: Exception) {
            loadErrorScreen(e.message ?: "Unknown error occurred")
            Log.e("FocusViewModel", "Error loading initial data", e)
        }
    }
}

fun loadErrorScreen(error: String) {
    focusUiState = focusUiState.copy(
        isLoading = false,
        error = error
    )
}
```

### 3.3 Loading State Management

**Pattern:** Dedicated boolean in UI state
```kotlin
// Most ViewModels follow this pattern:
data class UiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    // ... data fields
)

// Set during initialization
init {
    viewModelScope.launch {
        goalUiState = goalUiState.copy(isLoading = true)
        try {
            val result = loadData()
            goalUiState = goalUiState.copy(goal = result, isLoading = false)
        } catch (e: Exception) {
            goalUiState = goalUiState.copy(error = e.message, isLoading = false)
        }
    }
}
```

---

## 4. APPLICATION LIFECYCLE

### 4.1 App Initialization Flow

```
System Launch
    ↓
MainActivity.onCreate()
    ├─ setContent { VoxPlanApp() }
    │
VoxPlanApplication.onCreate()
    ├─ Create AppDataContainer
    ├─ Initialize Room Database
    ├─ Lazy-load all repositories
    └─ Ready for ViewModels

VoxPlanApp (Composable)
    ├─ Scaffold with bottom navigation
    ├─ VoxPlanNavHost
    └─ Screen Navigation
```

**Application Class** (/app/src/main/java/com/voxplanapp/VoxPlanApplication.kt)
```kotlin
class VoxPlanApplication: Application() {
    lateinit var container: AppContainer
    
    override fun onCreate() {
        super.onCreate()
        container = AppDataContainer(this)  // Initialize DI container
    }
    
    override fun onTerminate() {
        super.onTerminate()
        (container as? AppDataContainer)?.soundPlayer?.release()
    }
}
```

### 4.2 Database Migrations

The app has 13 database versions with careful migration strategy:
- V2→V3: Add `order` column
- V4→V5: Create Event table
- V6→V7: Create TimeBank table
- V8→V9: Add `completedDate` column to TodoItem
- V9→V10: Create Quota table
- And more...

**Migration Pattern:**
```kotlin
val MIGRATION_12_13 = object : Migration(12, 13) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE ... ADD COLUMN ...")
    }
}
```

### 4.3 Navigation Architecture

**VoxPlanScreen Sealed Class** (/app/src/main/java/com/voxplanapp/navigation/VoxPlanScreen.kt)
- Defines all app routes with type-safe navigation
- Routes:
  - `Main` - Goal hierarchy
  - `GoalEdit/{goalId}` - Edit goal + quota
  - `Progress` - Weekly quota progress
  - `DaySchedule/{date}` - Calendar view
  - `FocusMode/{goalId}` or `FocusMode?eventId={eventId}` - Timer
  - `Daily/{date}?newEventId={newEventId}` - Daily tasks

**Navigation Flow:**
```
Bottom Navigation (4 tabs)
    ├─ Goals (Main)
    ├─ Daily
    ├─ Progress
    └─ Schedule

From Main Screen → Edit Goal → Focus Mode
From Schedule → Focus Mode
From Daily → Add to Schedule → Focus Mode
Focus Mode → Creates Event in Time Bank
```

### 4.4 Background Processing

**Coroutines Usage:**
- **Timer in FocusViewModel:**
  ```kotlin
  _timerJob.value = viewModelScope.launch {
      while (isActive) {
          val currentTime = System.currentTimeMillis()
          val elapsedTime = currentTime - startTime
          updateTimerState(elapsedTime)
          delay(1000)
      }
  }
  ```

- **Discrete Task Timer:**
  ```kotlin
  _discreteTaskJob.value = viewModelScope.launch {
      var elapsedTime = 0L
      val totalTime = 5000L
      while (true) {
          delay(50)
          elapsedTime += 50
          val progress = (elapsedTime.toFloat() / totalTime).coerceIn(0f, 1f)
          updateDiscreteTaskProgress(progress)
          if (elapsedTime >= totalTime) {
              advanceDiscreteTaskLevel()
              elapsedTime = 0L
          }
      }
  }
  ```

**Job Management:**
- Jobs stored in StateFlow
- Cancellation on screen exit via `viewModelScope.launch` auto-cleanup
- Manual cancellation: `_timerJob.value?.cancel()`

### 4.5 Lifecycle-Aware Components

**ViewModel Lifecycle:**
- Created when first requested by Composable
- Scoped to back stack entry (survives config changes)
- Cleared when screen is popped from nav stack
- Auto-cleanup of `viewModelScope` jobs

**Database Lifecycle:**
- Singleton Room instance created at app startup
- Connections managed by Room connection pool
- `soundPlayer.release()` called in `onTerminate()`

---

## 5. DATA FLOW DIAGRAMS

### 5.1 Goal Hierarchy Data Flow

```
Repository.getAllTodos() ──────┐
                               ├──> combine ──> MainUiState
SharedViewModel.breadcrumbs ───┘
                               │
                    processGoals(todos, parentId)
                               │
                        (Recursive traversal)
                               │
                    [GoalWithSubGoals]
                               │
                         UI renders tree
```

### 5.2 Event Scheduling Data Flow

```
DailyViewModel.uiState.date ──────┐
                                  ├──> flatMapLatest
eventRepository.getDailiesForDate()┘
                                  │
                         Filter by parentDailyId
                                  │
                         eventsForCurrentDate
                                  │
                      UI displays daily tasks
                                  │
                        User adds scheduled time
                                  │
                  eventRepository.insertEvent(scheduledBlock)
                     (with parentDailyId reference)
```

### 5.3 Time Bank Accumulation Flow

```
FocusMode: User starts timer
          │
          ├─ Timer ticks (1s intervals)
          │
          ├─ Complete rotation (30 mins)
          │
          ├─ Award medal
          │
          ├─ User banks time
          │
          └─> timeBankRepository.addTimeBankEntry()
              │
              └─> Event created with scheduled times
                  │
                  └─> ProgressViewModel queries TimeBank
                      │
                      └─> Renders weekly progress
```

### 5.4 Action Mode Reordering Flow

```
UI Button Click (Up/Down/HierUp/HierDown)
          │
          └─> ActionModeHandler.toggle*()
              │
              ├─ Set _actionMode state
              │
              ├─ Button visual feedback
              │
              ├─ User clicks goal to reorder
              │
              ├─> MainViewModel.reorderItem()
              │
              ├─ Calculate new order
              │
              ├─ Update multiple TodoItems in transaction
              │
              ├─> DB updated
              │
              ├─> getAllTodos() Flow emits new data
              │
              └─> UI re-renders goal list
```

---

## 6. DEPENDENCY INJECTION GRAPH

```
Application (VoxPlanApplication)
    │
    └─ AppDataContainer (implements AppContainer)
        │
        ├─ Context (from Application)
        │   └─ SoundPlayer
        │
        ├─ Room Database
        │   ├─ TodoDao
        │   │   └─ TodoRepository
        │   │       ├─ MainViewModel
        │   │       ├─ FocusViewModel
        │   │       ├─ DailyViewModel
        │   │       ├─ GoalEditViewModel
        │   │       └─ ProgressViewModel
        │   │
        │   ├─ EventDao
        │   │   └─ EventRepository
        │   │       ├─ MainViewModel
        │   │       ├─ SchedulerViewModel
        │   │       ├─ FocusViewModel
        │   │       ├─ DailyViewModel
        │   │       └─ GoalEditViewModel
        │   │
        │   ├─ TimeBankDao
        │   │   └─ TimeBankRepository
        │   │       ├─ MainViewModel
        │   │       ├─ FocusViewModel
        │   │       ├─ ProgressViewModel
        │   │       └─ Time tracking
        │   │
        │   └─ QuotaDao
        │       └─ QuotaRepository
        │           ├─ DailyViewModel
        │           ├─ GoalEditViewModel
        │           ├─ ProgressViewModel
        │           └─ Quota management
        │
        └─ AppViewModelProvider.Factory
            └─ All ViewModels created via viewModelFactory
                └─ Shared injection of repositories
```

### 6.1 Provided Dependencies

| Dependency | Scope | Created By | Used By |
|---|---|---|---|
| `AppDatabase` | Singleton | Room.databaseBuilder | Repositories |
| `TodoRepository` | Singleton | AppDataContainer | Most ViewModels |
| `EventRepository` | Singleton | AppDataContainer | Event-based ViewModels |
| `TimeBankRepository` | Singleton | AppDataContainer | Time tracking ViewModels |
| `QuotaRepository` | Singleton | AppDataContainer | Quota-related ViewModels |
| `SoundPlayer` | Singleton | AppDataContainer | Main, Focus, Time Bank |
| `SharedViewModel` | Singleton | viewModelFactory | Main, Focus, GoalEdit |
| `NavigationViewModel` | Per-Activity | viewModelFactory | Navigation |
| Other ViewModels | Per-Screen | viewModelFactory | Screens |

---

## 7. COMMON ARCHITECTURAL PATTERNS

### 7.1 Repository Pattern

**Characteristics:**
- Single DAO per repository
- Lazy initialization with `by lazy`
- Expose Flow for reactivity
- Hide implementation details
- No business logic

**Example:**
```kotlin
class TodoRepository(private val todoDao: TodoDao) {
    // No constructor injection of other repos
    // Only one DAO
    
    fun getAllTodos(): Flow<List<TodoItem>> = 
        todoDao.getAllTodos()
    
    suspend fun insert(todo: TodoItem) {
        Log.d("TodoRepository", "Inserting todo: $todo")
        todoDao.insert(todo)
    }
}
```

### 7.2 StateFlow as Single Source of Truth

**MainUiState example:**
```kotlin
val mainUiState: StateFlow<MainUiState> = combine(
    repository.getAllTodos(),       // Data from DB
    sharedViewModel.breadcrumbs     // Navigation state
) { todos, breadcrumbs ->
    // Transform to UI state
    MainUiState(
        goalList = sharedViewModel.processGoals(todos, currentParentId),
        breadcrumbs = breadcrumbs
    )
}.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), MainUiState())
```

**Unidirectional flow:**
Database → Repository → ViewModel → StateFlow → UI → User Interaction → ViewModel Action → Database

### 7.3 Hierarchical Goal Processing

**SharedViewModel.processGoals:**
- Recursive function (max depth 3)
- Filters by parentId at each level
- Builds tree structure
- Used by multiple screens

```kotlin
fun processGoals(todos: List<TodoItem>, parentId: Int?, depth: Int = 1): List<GoalWithSubGoals> {
    return todos.filter { it.parentId == parentId }
        .sortedBy { it.order }
        .map { goal ->
            val subGoals = if (depth < MAX_DEPTH) {
                processGoals(todos, goal.id, depth + 1)
            } else {
                emptyList()
            }
            GoalWithSubGoals(goal = goal, subGoals = subGoals)
        }
}
```

### 7.4 Parent-Child Event Relationships

**Event Design Pattern:**
- Parent Event (Daily): `parentDailyId = null`, no startTime/endTime
- Child Events (Scheduled): `parentDailyId = parentId`, has startTime/endTime
- DAO queries filter by `parentDailyId IS NULL/IS NOT NULL`

**Benefits:**
- Single Event entity for two purposes
- Hierarchical tracking
- Cascading deletes via Foreign Key

### 7.5 Quota Active Days Encoding

**String encoding for active days:**
- String of 7 chars: "1111100" (Mon-Fri active)
- Index = dayOfWeek - 1 (0-based)
- `quota.activeDays[dayIndex] == '1'` checks if active

```kotlin
fun isQuotaActiveForDate(quota: Quota, date: LocalDate): Boolean {
    val dayIndex = date.dayOfWeek.value - 1  // 0-6 for Mon-Sun
    return quota.activeDays[dayIndex] == '1'
}
```

### 7.6 Transaction Management

**updateItemsInTransaction in TodoDao:**
```kotlin
@Transaction
suspend fun updateItemsInTransaction(todos: List<TodoItem>) {
    todos.forEach { update(it) }
}
```

**Use case:** Reordering multiple goals atomically

---

## 8. BEST PRACTICES OBSERVED

### 8.1 State Management

✓ **StateFlow over LiveData** - Better for Compose
✓ **Single Source of Truth** - MainUiState combines all inputs
✓ **Immutable Data Classes** - All UI states are data classes
✓ **Clear State Ownership** - Each ViewModel owns its state
✓ **SharingStarted.WhileSubscribed** - Efficient resource usage

### 8.2 Coroutine Handling

✓ **viewModelScope for auto-cleanup** - Jobs cancelled on ViewModel clear
✓ **Job references stored** - For manual cancellation if needed
✓ **Dispatcher parameter** - MainViewModel accepts ioDispatcher
✓ **Launch vs async** - Uses launch for fire-and-forget operations
✓ **Delay for animations** - Proper use of delay in timer loops

### 8.3 Database Access

✓ **Flow for reactivity** - Queries return Flow for continuous updates
✓ **Suspend functions** - Write operations use suspend
✓ **Type converters** - Proper LocalDate/LocalTime serialization
✓ **Migration strategy** - Careful migration path across 13 versions
✓ **Foreign Keys** - Quota references TodoItem with ON DELETE CASCADE

### 8.4 Architecture

✓ **Separation of Concerns** - Clear layers (UI/VM/Repo/DB)
✓ **No God ViewModels** - Each screen has focused ViewModel
✓ **Manual DI** - Simple, understandable, no reflection
✓ **Lazy Initialization** - Repositories created on first access
✓ **Service Locator** - AppContainer pattern for dependency access

### 8.5 Data Flow

✓ **Unidirectional flow** - Data flows down, events flow up
✓ **Reactive updates** - combine/flatMapLatest for dependent state
✓ **Error handling** - Try-catch in init blocks
✓ **Loading states** - Explicit isLoading boolean
✓ **No circular dependencies** - ViewModels don't reference each other

### 8.6 Code Organization

✓ **Package by feature** - ui.main, ui.calendar, ui.goals, etc.
✓ **Sealed classes for navigation** - Type-safe route definitions
✓ **Constants file** - Centralized magic numbers
✓ **Extension functions** - CreationExtras.voxPlanApplication()
✓ **Logging** - Strategic use of Log.d for debugging

---

## 9. FILE STRUCTURE

### 9.1 Complete File Paths

**Core Application:**
- `/app/src/main/java/com/voxplanapp/MainActivity.kt`
- `/app/src/main/java/com/voxplanapp/VoxPlanApplication.kt`
- `/app/src/main/java/com/voxplanapp/AppViewModelProvider.kt`

**Data Layer:**
- `/app/src/main/java/com/voxplanapp/data/AppContainer.kt`
- `/app/src/main/java/com/voxplanapp/data/AppDatabase.kt`
- `/app/src/main/java/com/voxplanapp/data/TodoItem.kt`
- `/app/src/main/java/com/voxplanapp/data/Event.kt`
- `/app/src/main/java/com/voxplanapp/data/TimeBankEntry.kt`
- `/app/src/main/java/com/voxplanapp/data/QuotaEntity.kt`
- `/app/src/main/java/com/voxplanapp/data/GoalWithSubGoals.kt`
- `/app/src/main/java/com/voxplanapp/data/TodoDao.kt`
- `/app/src/main/java/com/voxplanapp/data/EventDao.kt`
- `/app/src/main/java/com/voxplanapp/data/QuotaDao.kt`
- `/app/src/main/java/com/voxplanapp/data/TodoRepository.kt`
- `/app/src/main/java/com/voxplanapp/data/EventRepository.kt`
- `/app/src/main/java/com/voxplanapp/data/TimeBankRepository.kt`
- `/app/src/main/java/com/voxplanapp/data/QuotaRepository.kt`
- `/app/src/main/java/com/voxplanapp/data/Converters.kt`
- `/app/src/main/java/com/voxplanapp/data/Constants.kt`
- `/app/src/main/java/com/voxplanapp/data/GoalEventMapper.kt`

**Navigation:**
- `/app/src/main/java/com/voxplanapp/navigation/VoxPlanApp.kt`
- `/app/src/main/java/com/voxplanapp/navigation/VoxPlanNavHost.kt`
- `/app/src/main/java/com/voxplanapp/navigation/VoxPlanScreen.kt`
- `/app/src/main/java/com/voxplanapp/navigation/NavigationViewModel.kt`

**ViewModels (UI Layer):**
- `/app/src/main/java/com/voxplanapp/ui/main/MainViewModel.kt`
- `/app/src/main/java/com/voxplanapp/ui/calendar/SchedulerViewModel.kt`
- `/app/src/main/java/com/voxplanapp/ui/focusmode/FocusViewModel.kt`
- `/app/src/main/java/com/voxplanapp/ui/daily/DailyViewModel.kt`
- `/app/src/main/java/com/voxplanapp/ui/goals/GoalEditViewModel.kt`
- `/app/src/main/java/com/voxplanapp/ui/goals/ProgressViewModel.kt`

**Shared Components:**
- `/app/src/main/java/com/voxplanapp/shared/SharedViewModel.kt`
- `/app/src/main/java/com/voxplanapp/shared/SoundPlayer.kt`

**Models:**
- `/app/src/main/java/com/voxplanapp/model/ActionMode.kt`

**UI Screens:**
- `/app/src/main/java/com/voxplanapp/ui/main/MainScreen.kt`
- `/app/src/main/java/com/voxplanapp/ui/main/GoalItem.kt`
- `/app/src/main/java/com/voxplanapp/ui/main/GoalListContainer.kt`
- `/app/src/main/java/com/voxplanapp/ui/main/QuickScheduleScreen.kt`
- `/app/src/main/java/com/voxplanapp/ui/main/BreadCrumbNavigation.kt`
- `/app/src/main/java/com/voxplanapp/ui/main/TodoInputBar.kt`
- `/app/src/main/java/com/voxplanapp/ui/calendar/DaySchedule.kt`
- `/app/src/main/java/com/voxplanapp/ui/daily/DailyScreen.kt`
- `/app/src/main/java/com/voxplanapp/ui/focusmode/FocusModeScreen.kt`
- `/app/src/main/java/com/voxplanapp/ui/goals/GoalEditScreen.kt`
- `/app/src/main/java/com/voxplanapp/ui/goals/ProgressScreen.kt`
- `/app/src/main/java/com/voxplanapp/ui/goals/QuotaSettings.kt`

**Theme & Constants:**
- `/app/src/main/java/com/voxplanapp/ui/theme/Theme.kt`
- `/app/src/main/java/com/voxplanapp/ui/theme/Type.kt`
- `/app/src/main/java/com/voxplanapp/ui/theme/Color.kt`
- `/app/src/main/java/com/voxplanapp/ui/constants/Colors.kt`
- `/app/src/main/java/com/voxplanapp/ui/constants/Dimens.kt`
- `/app/src/main/java/com/voxplanapp/ui/constants/DpValues.kt`
- `/app/src/main/java/com/voxplanapp/ui/constants/TextStyles.kt`

**Total Kotlin files:** 51

---

## 10. KEY TAKEAWAYS

1. **Clean Architecture:** Well-separated layers (UI/VM/Repo/DB)
2. **Reactive First:** StateFlow and Flow throughout for reactive updates
3. **Unidirectional Data Flow:** Data down, events up pattern
4. **Manual DI:** Service locator pattern with AppContainer
5. **Hierarchical Data:** Goal trees with max depth 3
6. **Event-based Time Tracking:** Flexible Event entity for dailies + scheduled blocks
7. **Quota System:** String-encoded active days with daily minute tracking
8. **Timer Architecture:** Coroutine-based timers with medal rewards
9. **Navigation:** Compose Navigation with type-safe sealed class routes
10. **State Persistence:** SavedStateHandle for screen arguments, no process death

---

## Conclusion

VoxPlanApp demonstrates modern Android best practices with Jetpack Compose, emphasizing:
- **Reactive programming** through Flow and StateFlow
- **Clean architecture** with clear separation of concerns
- **Type safety** with Kotlin data classes and sealed classes
- **Scalability** through modular ViewModel design
- **User experience** with loading states and error handling

The architecture is well-suited for a productivity app requiring complex hierarchical data management, time tracking, and weekly progress reporting.

