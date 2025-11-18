# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

VoxPlanApp is an Android productivity app built with Jetpack Compose that implements hierarchical goal management with time tracking, daily quotas, scheduling, and gamified focus sessions. The app uses MVVM architecture with manual dependency injection, Room database, and reactive programming with Kotlin Flow/StateFlow.

**Current Version:** 3.2 (Dailies improved with parent/child Events)

## Build & Development Commands

### Building the Project
```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Clean build
./gradlew clean build
```

### Running Tests
```bash
# Run unit tests
./gradlew test

# Run instrumented tests (requires emulator or device)
./gradlew connectedAndroidTest

# Run specific test
./gradlew test --tests com.voxplanapp.ExampleUnitTest
```

### Database Management
```bash
# Room schema exports are located in app/schemas/
# Schema location is configured via KSP: app/build.gradle.kts line 88-90
# Current database version: 13
```

### Gradle Tasks
```bash
# List all available tasks
./gradlew tasks

# Lint checks
./gradlew lint

# Install debug build on connected device
./gradlew installDebug
```

## Architecture Overview

### MVVM with Manual Dependency Injection

**Dependency Container Pattern:**
- `VoxPlanApplication` creates `AppDataContainer` (implements `AppContainer`)
- All repositories and database are lazy-initialized singletons
- `AppViewModelProvider.Factory` creates ViewModels with injected dependencies
- `SharedViewModel` is shared across multiple screens for breadcrumb navigation

**Key Pattern:**
```kotlin
// Access application container
val application = (context.applicationContext as VoxPlanApplication)
val repository = application.container.todoRepository
```

### State Management Architecture

**Hybrid Approach:**
1. **StateFlow** - For reactive, observable data from repositories
2. **Compose State (mutableStateOf)** - For UI-local, transient state
3. **SharedViewModel** - For cross-screen state (breadcrumb navigation)

**Critical Pattern:**
```kotlin
// Combining multiple flows into single UI state
val mainUiState: StateFlow<MainUiState> = combine(
    repository.getAllTodos(),
    sharedViewModel.breadcrumbs
) { todos, breadcrumbs ->
    MainUiState(
        goalList = sharedViewModel.processGoals(todos, currentParentId),
        breadcrumbs = breadcrumbs
    )
}.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), MainUiState())
```

### Data Layer Architecture

**Entities:**
- `TodoItem` - Hierarchical goals/subgoals (max depth: 3)
- `Event` - Used for BOTH dailies AND scheduled blocks (distinguished by `parentDailyId`)
- `TimeBank` - Time tracking entries (minutes per goal per date)
- `Quota` - Daily time quotas for goals (with active days encoding)

**Parent-Child Event Pattern (CRITICAL):**
```kotlin
// Parent Daily: parentDailyId = null, startTime/endTime = null
val daily = Event(goalId = 5, title = "Programming", startDate = today, parentDailyId = null)

// Scheduled Child: parentDailyId = dailyId, startTime/endTime populated
val scheduled = Event(goalId = 5, title = "Programming", startDate = today,
    parentDailyId = daily.id, startTime = LocalTime.of(9, 0), endTime = LocalTime.of(10, 0))
```

**Quota Active Days Encoding:**
- String of 7 chars representing Mon-Sun: `"1111100"` = Mon-Fri active
- Index = `dayOfWeek.value - 1` (0-based)
- Check if active: `quota.activeDays[dayIndex] == '1'`

### Hierarchical Goal Processing

**SharedViewModel.processGoals()** - Recursive function (max depth 3) that builds goal tree:
- Filters by `parentId` at each level
- Sorts by `order` field
- Returns `List<GoalWithSubGoals>` with nested structure
- Used by MainViewModel, GoalEditViewModel, and FocusViewModel

**Breadcrumb Navigation:**
- SharedViewModel maintains `breadcrumbs: StateFlow<List<GoalWithSubGoals>>`
- Navigate down: `navigateToSubGoal(goal, parentGoal)`
- Navigate up: `navigateUp()` - pops last breadcrumb
- Current parent ID: `breadcrumbs.lastOrNull()?.goal?.id`

## Critical Files & Line Counts

### ViewModels (State Management)
- `MainViewModel.kt` (362 lines) - Goal list with breadcrumb navigation, ActionMode reordering
- `FocusViewModel.kt` (492 lines) - Complex state: timer, medals, discrete tasks, time banking
- `DailyViewModel.kt` (190 lines) - Daily tasks with quota integration
- `SchedulerViewModel.kt` (141 lines) - Day-by-day event scheduler with date reactivity
- `GoalEditViewModel.kt` (191 lines) - Goal editing with quota settings
- `ProgressViewModel.kt` (174 lines) - Weekly quota progress tracking
- `SharedViewModel.kt` (80 lines) - Breadcrumb navigation logic

### UI Screens
- `DailyScreen.kt` (628 lines) - Daily tasks screen with quota integration
- `DaySchedule.kt` (675 lines) - **Contains critical delete dialog bug (lines 110-126)**
- `FocusModeScreen.kt` (697 lines) - Timer UI with medals and discrete tasks
- `MainScreen.kt` (492 lines) - Goal hierarchy with ActionMode buttons
- `GoalEditScreen.kt` (292 lines) - Goal editing with quota settings

### Data Layer
- `AppDatabase.kt` (384 lines) - Room database with 13 migrations
- `TodoRepository.kt` (76 lines) - Goal CRUD operations
- `EventRepository.kt` (32 lines) - Event CRUD with parent/child filtering
- `TimeBankRepository.kt` (56 lines) - Time tracking entries
- `QuotaRepository.kt` (47 lines) - Quota management with active days logic

### Navigation
- `VoxPlanNavHost.kt` - Route definitions and composable routing
- `VoxPlanScreen.kt` - Sealed class for type-safe navigation
- `VoxPlanApp.kt` - Scaffold with bottom navigation, ActionModeHandler

## Known Issues & Incomplete Features

### Critical Bugs (Fix First)

**1. Delete Dialog Bug in DaySchedule.kt (lines 110-126)**
```kotlin
// ERROR: 'event' variable is undefined in this scope
AlertDialog(
    onDismissRequest = { viewModel.dismissDeleteParentDialog() },
    confirmButton = {
        TextButton(onClick = { viewModel.confirmDeleteChildOnly(event) }) { /* ... */ }
    },
    dismissButton = {
        TextButton(onClick = { viewModel.confirmDeleteWithParent(event) }) { /* ... */ }
    }
)
```
**Fix:** Refactor to use `showDeleteParentDialog.value` (the Event) instead of undefined `event` variable.

**2. QuickScheduleScreen.kt - Entirely Commented Out**
- Lines 26-86 are all commented out
- Contains undefined variables
- Either implement properly or remove file

### Incomplete Features (Dailies - ~70% Complete)
- ❌ Completion tracking (no checkboxes for daily tasks)
- ❌ Direct Focus Mode access from dailies
- ❌ Bulk operations (multi-select, batch actions)
- ❌ Quick-reschedule feature
- ✅ Quota integration ("Add Quota Tasks" button)
- ✅ Parent-child relationship with scheduled events
- ✅ Vertical reordering with ActionMode

### Incomplete Features (Scheduling - ~65% Complete)
- ❌ Event creation from schedule (tap empty space)
- ❌ Week/month calendar views
- ❌ Recurrence support (RecurrenceType enum exists but not used)
- ❌ Color-coding by goal
- ❌ Smart scheduling suggestions
- ❌ Scroll position persistence on date change
- ✅ Day view with hourly grid
- ✅ Drag-to-schedule from dailies
- ✅ Focus Mode access from scheduled events

## Development Workflow Patterns

### Adding a New ViewModel

1. Create data classes for UI state in ViewModel file
2. Add ViewModel to `AppViewModelProvider.Factory`:
```kotlin
initializer {
    MyViewModel(
        repository = voxPlanApplication().container.myRepository,
        sharedViewModel = sharedViewModel  // if needed
    )
}
```
3. Use in Composable: `viewModel = viewModel(factory = AppViewModelProvider.Factory)`

### Database Migration Pattern

```kotlin
// In AppDatabase.kt
val MIGRATION_X_Y = object : Migration(X, Y) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE ... ADD COLUMN ...")
    }
}

// Add to database builder
.addMigrations(MIGRATION_2_3, ..., MIGRATION_X_Y)
```
**IMPORTANT:** Increment version number in `@Database(version = X)` annotation.

### Flow Reactive Patterns

**Pattern 1: Simple Flow Subscription**
```kotlin
val goals = todoRepository.getItemsByIds(goalIds).first()  // Suspend function
```

**Pattern 2: Combining Flows**
```kotlin
combine(flow1, flow2) { data1, data2 ->
    CombinedState(data1, data2)
}.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), InitialState())
```

**Pattern 3: Date-Driven State with flatMapLatest**
```kotlin
_currentDate.flatMapLatest { date ->
    eventRepository.getEventsForDate(date)
}.collect { events ->
    _eventsForCurrentDate.value = events
}
```

**Pattern 4: Compose State to Flow with snapshotFlow**
```kotlin
snapshotFlow { _uiState.value.date }
    .flatMapLatest { date ->
        eventRepository.getDailiesForDate(date)
    }
    .collect { events -> /* update state */ }
```

### ActionMode Reordering System

**Enum States:** Normal, VerticalUp, VerticalDown, HierarchyUp, HierarchyDown

**Usage Pattern:**
1. User clicks ActionMode button (e.g., VerticalUp)
2. ActionModeHandler toggles state in VoxPlanApp
3. UI highlights active button
4. User clicks goal to reorder
5. ViewModel calculates new order based on mode
6. `updateItemsInTransaction()` updates multiple items atomically
7. Flow emits new data, UI re-renders

## Constants & Magic Numbers

**File:** `data/Constants.kt`
- `FULLBAR_MINS = 60` - Minutes to fill one power bar
- `pointsForItemCompletion = 15` - Time bonus for completing a task

**Hardcoded Values to Know:**
- Max goal depth: 3 (in SharedViewModel.processGoals)
- Timer WhileSubscribed timeout: 5000ms
- Default scroll hour in DaySchedule: 6 AM
- Pomodoro defaults: 5 min work, 1 min rest

## Type Converters & Serialization

**LocalDate:** Stored as Long (epoch day)
**LocalTime:** Stored as String (HH:mm format)

**File:** `data/Converters.kt` - Applied to AppDatabase

## Focus Mode Medal System

**Awards medals for 30-minute blocks:**
- Bronze: 30 mins
- Silver: 60 mins
- Gold: 90 mins
- Diamond: 120+ mins

**Time Banking:** User can bank accrued time to create Event with scheduled duration.

## Navigation Routes

```kotlin
sealed class VoxPlanScreen {
    object Main
    data class GoalEdit(val goalId: Int)
    object Progress
    data class DaySchedule(val date: String)
    data class FocusMode(val goalId: Int?, val eventId: Int?)
    data class Daily(val date: String, val newEventId: Int?)
}
```

**Bottom Navigation:** Goals (Main), Daily, Progress, Schedule

## Testing Strategy

**Current State:** Only example tests exist

**Priority Tests to Write:**
1. SharedViewModel.processGoals() - hierarchical processing logic
2. TodoRepository transaction operations
3. Event parent-child relationships and DAO queries
4. Quota active days encoding/decoding
5. FocusViewModel timer state management
6. Database migrations (especially 12→13)

## Documentation

**Comprehensive docs in /docs directory:**
- `ARCHITECTURE.md` - Complete architecture documentation (1,217 lines)
- `INCOMPLETE_FEATURES.md` - Detailed analysis of dailies/scheduling (572 lines)
- `FEATURES_SUMMARY.md` - Quick reference (428 lines)
- `DOCUMENTATION_INDEX.md` - Navigation guide for docs
- `DATA_MODELS_DOCUMENTATION.md` - Database schema details
- `VOXPLAN_UI_ARCHITECTURE.md` - UI component structure

**Start with ARCHITECTURE.md for deep understanding of the codebase.**

## Important Architectural Decisions

1. **Single Event Entity for Dailies + Scheduled Blocks** - Flexible design using `parentDailyId` to distinguish types. Simplifies data model but requires careful DAO queries.

2. **Manual DI Over Hilt** - Simpler, more transparent, no reflection overhead. All dependencies visible in AppContainer.

3. **SharedViewModel Pattern** - Single instance shared across multiple ViewModels for breadcrumb state. Avoids prop drilling and StateFlow duplication.

4. **StateFlow with 5-second timeout** - `SharingStarted.WhileSubscribed(5_000L)` balances reactivity with resource efficiency.

5. **Room TypeConverters for java.time** - LocalDate/LocalTime require custom converters. Always use these types, never String dates in code.

6. **Hierarchical Goals Max Depth 3** - Prevents infinite recursion and maintains UI simplicity. Enforced in SharedViewModel.processGoals().

7. **Foreign Key Cascades** - Quota → TodoItem with ON DELETE CASCADE. Deleting a goal auto-deletes its quota.

8. **Transaction-Based Reordering** - `updateItemsInTransaction()` ensures atomic updates when reordering multiple goals.

## Code Style Conventions

- ViewModels use `private val _state = MutableStateFlow()` + `val state = _state.asStateFlow()` pattern
- UI state classes are data classes with default values
- Repository methods that return data use `Flow<T>`, write operations use `suspend fun`
- Composables receive ViewModel, not individual state parameters
- Log tags use class name: `Log.d("MainViewModel", "...")`
- Package structure: by feature (ui.main, ui.calendar, ui.goals, etc.)

## Gradle Configuration

**Kotlin Version:** 1.9.0
**Compose Compiler:** 1.5.1
**Target/Compile SDK:** 34
**Min SDK:** 27 (Android 8.1)

**Key Dependencies:**
- Room: 2.6.1 (with KSP for annotation processing)
- Compose BOM: 2023.08.00
- Navigation Compose: 2.7.7
- Media3 ExoPlayer: 1.1.1 (for sound effects)

**Room Schema Export:** `app/schemas/` directory (configured in app/build.gradle.kts line 88-90)
