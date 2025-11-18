# VoxPlanApp Architecture - Quick Reference

## At a Glance

| Aspect | Implementation |
|--------|-----------------|
| **Architecture Pattern** | MVVM with Manual DI |
| **State Management** | StateFlow + Compose State |
| **Database** | Room (SQLite) v13 |
| **Reactive Framework** | Kotlin Flow + Coroutines |
| **UI Framework** | Jetpack Compose |
| **Total Files** | 51 Kotlin files |
| **Screens** | 6 (Main, GoalEdit, Progress, DaySchedule, FocusMode, Daily) |
| **ViewModels** | 6 (Main, Scheduler, Focus, Daily, GoalEdit, Progress) + Navigation + Shared |

---

## Core Components

### State Holders (ViewModels)

1. **SharedViewModel** - Cross-screen breadcrumb navigation
2. **MainViewModel** - Goal hierarchy with breadcrumbs + today's time total
3. **DailyViewModel** - Daily schedule management with quota integration
4. **SchedulerViewModel** - Calendar view with event scheduling
5. **FocusViewModel** - Complex timer with medals & discrete tasks
6. **GoalEditViewModel** - Goal editing with quota settings
7. **ProgressViewModel** - Weekly quota progress tracking
8. **NavigationViewModel** - Bottom nav bar state

### Data Repositories

- **TodoRepository** - Goals/tasks CRUD
- **EventRepository** - Events (dailies + scheduled blocks)
- **TimeBankRepository** - Time accrual tracking
- **QuotaRepository** - Goal quota settings

### Data Entities

```
TodoItem (Goal/Task)
  ├─ id, title, notes
  ├─ parentId (null = root)
  ├─ order (display order)
  ├─ completedDate (completion tracking)
  └─ frequency, estDurationMins, preferredTime

Event (Daily + Scheduled blocks)
  ├─ id, goalId, title
  ├─ startTime, endTime, startDate
  ├─ quotaDuration, scheduledDuration, completedDuration
  └─ parentDailyId (null = daily, set = scheduled block)

TimeBank (Time accrual)
  ├─ id, goalId, date
  └─ duration (minutes)

Quota (Goal quotas)
  ├─ id, goalId, dailyMinutes
  └─ activeDays ("1111100" = Mon-Fri)
```

### Dependency Injection

**Service Locator Pattern:**
```
VoxPlanApplication.container: AppContainer
  └─ AppDataContainer
      ├─ Database (singleton)
      ├─ TodoRepository
      ├─ EventRepository
      ├─ TimeBankRepository
      ├─ QuotaRepository
      └─ SoundPlayer
```

**ViewModel Factory:**
```
AppViewModelProvider.Factory
  ├─ SharedViewModel (singleton)
  └─ Other ViewModels with repository injection
```

---

## State Management Patterns

### Pattern 1: Combined StateFlow (MainViewModel)
```kotlin
val mainUiState: StateFlow<MainUiState> = combine(
    repository.getAllTodos(),      // Observable data
    sharedViewModel.breadcrumbs    // Navigation state
) { todos, breadcrumbs ->
    MainUiState(
        goalList = sharedViewModel.processGoals(todos, currentParentId),
        breadcrumbs = breadcrumbs
    )
}.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), MainUiState())
```

### Pattern 2: Date-Driven Reactivity (SchedulerViewModel)
```kotlin
viewModelScope.launch {
    _currentDate
        .flatMapLatest { date ->
            eventRepository.getEventsForDate(date)
                .map { events -> /* filter */ }
        }
        .collect { events ->
            _eventsForCurrentDate.value = events
        }
}
```

### Pattern 3: Compose State (FocusViewModel)
```kotlin
var focusUiState by mutableStateOf(FocusUiState(isLoading = true))
    private set
```

### Pattern 4: Manual State Updates (DailyViewModel)
```kotlin
fun updateDate(newDate: LocalDate) {
    _uiState.value = _uiState.value.copy(
        date = newDate,
        isLoading = true
    )
}
```

---

## Data Flow Summary

### Goal Navigation Flow
```
User clicks goal
  ↓
MainViewModel.navigateToSubGoals()
  ↓
SharedViewModel.navigateToSubGoal()
  ↓
breadcrumbs StateFlow updated
  ↓
combine() re-evaluates
  ↓
mainUiState updated
  ↓
UI re-renders with new goal context
```

### Time Banking Flow
```
FocusMode timer ticks (1s intervals)
  ↓
User earns medals (30 min rotation)
  ↓
User banks time (clicks button)
  ↓
timeBankRepository.addTimeBankEntry()
  ↓
Event created with scheduled times
  ↓
ProgressViewModel queries TimeBank
  ↓
Weekly progress updated
```

### Reordering Goals Flow
```
UI button click (Up/Down/HierUp/HierDown)
  ↓
ActionModeHandler toggles action mode
  ↓
User clicks goal to reorder
  ↓
MainViewModel.reorderItem()
  ↓
Calculate new order + update transaction
  ↓
repository.updateItemsInTransaction()
  ↓
DB updated
  ↓
getAllTodos() Flow emits new list
  ↓
combine() re-evaluates
  ↓
UI re-renders with new order
```

---

## Key Features & Implementation

### 1. Hierarchical Goals
- **Max depth:** 3 levels
- **Processing:** Recursive processGoals() in SharedViewModel
- **Structure:** GoalWithSubGoals recursive data class
- **Navigation:** Breadcrumb trail stored in SharedViewModel

### 2. Parent-Child Events
- **Dailies:** Event with parentDailyId = null
- **Scheduled blocks:** Event with parentDailyId = parent ID
- **Filtering:** DAO queries filter by parentDailyId IS NULL/IS NOT NULL
- **Cascading deletes:** Foreign key ON DELETE CASCADE

### 3. Time Tracking
- **Accrual:** FocusMode timer awards medals
- **Storage:** TimeBank entries per goal per date
- **Summary:** ProgressViewModel aggregates weekly totals
- **Quotas:** Daily minute targets with active day encoding

### 4. Reordering System
- **ActionMode:** Sealed class with 5 states
- **ActionModeHandler:** Manages state mutations
- **Implementation:** Transaction updates for atomic changes
- **UI Feedback:** Real-time button state visuals

### 5. Quota Management
- **Active Days:** String encoding "1111100" (7 chars)
- **Day Index:** dayOfWeek.value - 1 (0-based)
- **Filtering:** getAllActiveQuotas() returns only active quotas for date
- **Integration:** DailyViewModel auto-adds quota tasks

---

## Navigation Architecture

**Routes (6 screens):**
```
Main (root)
  ├─ GoalEdit/{goalId}
  ├─ FocusMode/{goalId} or ?eventId={eventId}
  ├─ Daily/{date}?newEventId={newEventId}
  ├─ DaySchedule/{date}
  └─ Progress

Bottom Navigation Bar (4 tabs):
  ├─ Goals → Main
  ├─ Daily → Daily/{today}
  ├─ Progress → Progress
  └─ Schedule → DaySchedule/{today}
```

**Type-Safe Navigation:**
```kotlin
sealed class VoxPlanScreen(val route: String) {
    object Main: VoxPlanScreen("main")
    object GoalEdit : VoxPlanScreen("goal_edit") {
        const val goalIdArg = "goalId"
        val routeWithArgs = "$route/{$goalIdArg}"
    }
    // ... more screens
}
```

---

## Error Handling Strategy

**Try-Catch in ViewModel Init:**
```kotlin
viewModelScope.launch {
    try {
        val data = loadData()
        uiState = uiState.copy(data = data, isLoading = false)
    } catch (e: Exception) {
        uiState = uiState.copy(error = e.message, isLoading = false)
    }
}
```

**UI State with Error Field:**
```kotlin
data class UiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val data: T? = null
)
```

---

## Best Practices Implemented

✓ **Single Source of Truth** - Combined StateFlow in MainViewModel
✓ **Unidirectional Data Flow** - Data down, events up
✓ **Reactive Updates** - Flow and StateFlow throughout
✓ **Manual DI** - Simple, understandable service locator
✓ **Type Safety** - Sealed classes for routes & action modes
✓ **Transaction Support** - Atomic multi-item updates
✓ **Coroutine Scopes** - viewModelScope for auto-cleanup
✓ **Resource Management** - soundPlayer.release() on app terminate
✓ **Logging** - Strategic Log.d calls for debugging
✓ **Data Immutability** - All UI states are immutable data classes

---

## File Organization

```
com.voxplanapp/
├── AppViewModelProvider.kt (ViewModel Factory)
├── MainActivity.kt
├── VoxPlanApplication.kt (App class with DI container)
├── data/
│   ├── AppContainer.kt (DI interface)
│   ├── AppDatabase.kt (Room DB)
│   ├── TodoItem.kt (Entity)
│   ├── Event.kt (Entity)
│   ├── TimeBank.kt (Entity)
│   ├── Quota.kt (Entity)
│   ├── GoalWithSubGoals.kt (Composite model)
│   ├── TodoDao.kt (DAO)
│   ├── EventDao.kt (DAO)
│   ├── TimeBankDao.kt (DAO)
│   ├── QuotaDao.kt (DAO)
│   ├── TodoRepository.kt
│   ├── EventRepository.kt
│   ├── TimeBankRepository.kt
│   ├── QuotaRepository.kt
│   ├── Converters.kt (Type converters)
│   └── Constants.kt
├── navigation/
│   ├── VoxPlanApp.kt (Main composable + ActionModeHandler)
│   ├── VoxPlanNavHost.kt (Navigation graph)
│   ├── VoxPlanScreen.kt (Route definitions)
│   └── NavigationViewModel.kt
├── shared/
│   ├── SharedViewModel.kt (Breadcrumb navigation)
│   └── SoundPlayer.kt (ExoPlayer wrapper)
├── model/
│   └── ActionMode.kt (Sealed class)
└── ui/
    ├── main/
    │   ├── MainScreen.kt
    │   ├── MainViewModel.kt
    │   └── Components...
    ├── calendar/
    │   ├── DaySchedule.kt
    │   └── SchedulerViewModel.kt
    ├── daily/
    │   ├── DailyScreen.kt
    │   └── DailyViewModel.kt
    ├── focusmode/
    │   ├── FocusModeScreen.kt
    │   └── FocusViewModel.kt
    ├── goals/
    │   ├── GoalEditScreen.kt
    │   ├── GoalEditViewModel.kt
    │   ├── ProgressScreen.kt
    │   └── ProgressViewModel.kt
    ├── theme/
    └── constants/
```

---

## Common Issues & Solutions

| Issue | Solution |
|-------|----------|
| State not updating | Ensure using `.copy()` for immutable updates, not direct mutation |
| Database deadlock | Use `@Transaction` for multi-item updates |
| Timer not stopping | Store Job in StateFlow, call `job?.cancel()` |
| Navigation not working | Check route format in VoxPlanScreen definitions |
| Quota not filtering correctly | Verify activeDays string encoding (7 chars, index = dayOfWeek - 1) |
| State loss on rotation | Use SavedStateHandle for screen arguments |

---

## Testing Entry Points

- **SharedViewModel:** Test processGoals() recursion with mock TodoItems
- **MainViewModel:** Test combine() with different breadcrumb states
- **RepositoryPatterns:** Mock DAOs to test repository logic
- **ActionMode:** Test ActionModeHandler state transitions
- **DateFlows:** Test flatMapLatest with different date changes

---

## Performance Considerations

- **SharingStarted.WhileSubscribed(5_000L)** - Stops collecting after 5s of no subscribers
- **Lazy repositories** - Created only when first accessed
- **Database indices** - Add indices on frequently queried fields (parentId, startDate, goalId)
- **Flow subscriptions** - Unsubscribe automatically via viewModelScope cleanup
- **Transactions** - Group multiple updates for atomic changes

---

**Full documentation:** See ARCHITECTURE.md (1216 lines)
