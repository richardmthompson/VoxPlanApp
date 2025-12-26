# Prompt: Generate Codebase Context Documentation for VoxPlanApp

## Objective
Create a comprehensive `codebase_context.md` file for VoxPlanApp that serves as the technical implementation guide, covering architecture patterns, module responsibilities, data flows, file structures, and development guidelines.

## Purpose of codebase_context.md
This document should provide:
1. **Technical Architecture Map**: Complete directory structure with file locations and purposes
2. **Implementation Patterns**: How the codebase implements MVVM, repository pattern, state management, etc.
3. **Module Responsibilities**: What each major file/module does and how they interact
4. **Data Flow Documentation**: How data moves through the system (UI → ViewModel → Repository → DAO → Database)
5. **Development Guidelines**: Practical instructions for common development tasks
6. **Integration Points**: Where and how to extend or modify functionality

## Target Audience
- Developers actively working on the codebase
- AI coding assistants performing implementation tasks
- Code reviewers evaluating architectural consistency
- New developers learning how to navigate and modify the code

## Required Sections (Based on VoxManifestorApp Structure)

### 1. Directory and File Structure Mapping

**What to include**:
- Complete directory tree with file paths
- File size and line count for major files
- Brief description of each module's contents
- Indicators for critical files (CORE, KEY, COMPLEX)

**For VoxPlanApp, map out**:
```
/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/voxplanapp/
│   │   │   │   ├── ui/
│   │   │   │   │   ├── main/               # Main screen with goal hierarchy
│   │   │   │   │   │   ├── MainScreen.kt (492 lines) - Goal list UI
│   │   │   │   │   │   ├── MainViewModel.kt (362 lines) - Goal state management
│   │   │   │   │   ├── daily/              # Daily tasks screen
│   │   │   │   │   │   ├── DailyScreen.kt (628 lines) - Daily planning UI
│   │   │   │   │   │   ├── DailyViewModel.kt (190 lines) - Daily state
│   │   │   │   │   ├── calendar/           # Scheduling interface
│   │   │   │   │   │   ├── DaySchedule.kt (675 lines) - Day view scheduler
│   │   │   │   │   │   ├── SchedulerViewModel.kt (141 lines) - Scheduler state
│   │   │   │   │   ├── goals/              # Goal editing
│   │   │   │   │   │   ├── GoalEditScreen.kt (292 lines) - Goal editor UI
│   │   │   │   │   │   ├── GoalEditViewModel.kt (191 lines) - Edit state
│   │   │   │   │   ├── focus/              # Focus mode
│   │   │   │   │   │   ├── FocusModeScreen.kt (697 lines) - Timer UI
│   │   │   │   │   │   ├── FocusViewModel.kt (492 lines) - Complex timer state
│   │   │   │   │   ├── progress/           # Progress tracking
│   │   │   │   │   │   ├── ProgressViewModel.kt (174 lines)
│   │   │   │   │   ├── navigation/         # Navigation coordination
│   │   │   │   │   │   ├── VoxPlanNavHost.kt
│   │   │   │   │   │   ├── VoxPlanScreen.kt - Sealed class routes
│   │   │   │   │   │   ├── VoxPlanApp.kt - Scaffold structure
│   │   │   │   │   ├── shared/             # Shared UI components
│   │   │   │   │   │   ├── SharedViewModel.kt (80 lines) - Breadcrumb logic
│   │   │   │   ├── data/                   # Data layer
│   │   │   │   │   ├── AppDatabase.kt (384 lines) - Room database with migrations
│   │   │   │   │   ├── AppContainer.kt - Dependency injection
│   │   │   │   │   ├── TodoRepository.kt (76 lines) - Goal CRUD
│   │   │   │   │   ├── EventRepository.kt (32 lines) - Event CRUD
│   │   │   │   │   ├── TimeBankRepository.kt (56 lines) - Time tracking
│   │   │   │   │   ├── QuotaRepository.kt (47 lines) - Quota management
│   │   │   │   │   ├── TodoItem.kt - Goal entity
│   │   │   │   │   ├── Event.kt - Daily/scheduled event entity
│   │   │   │   │   ├── TimeBank.kt - Time tracking entity
│   │   │   │   │   ├── Quota.kt - Quota entity
│   │   │   │   │   ├── Converters.kt - Room type converters
│   │   │   │   │   ├── Constants.kt - App constants
│   │   │   │   ├── AppViewModelProvider.kt - ViewModel factory
│   │   │   │   ├── MainActivity.kt - App entry point
│   │   │   │   └── VoxPlanApplication.kt - Application class
│   │   │   ├── res/                        # Android resources
│   │   │   └── AndroidManifest.xml
│   │   └── schemas/                        # Room schema exports
│   ├── build.gradle.kts
├── docs/                                   # Documentation
│   ├── ARCHITECTURE.md (1,217 lines)
│   ├── INCOMPLETE_FEATURES.md (572 lines)
│   ├── FEATURES_SUMMARY.md (428 lines)
├── CLAUDE.md                               # Project instructions
└── README.md
```

**Approach**:
1. Use `Glob` tool to discover all .kt files
2. Use `Read` tool to check file sizes and identify major components
3. Group files by feature module (main, daily, calendar, goals, focus, progress, data)
4. Annotate critical files with indicators ([CORE], [KEY], [COMPLEX])

### 2. Key Entry Points and Main Flows

**What to include**:
- Application entry point (MainActivity, VoxPlanApplication)
- Navigation setup (VoxPlanNavHost, VoxPlanApp)
- Primary user flows mapped to code paths
- How different screens interact and transition

**For VoxPlanApp, document**:
1. **App Initialization Flow**:
   - MainActivity → VoxPlanApplication → AppContainer → Database initialization
   - Navigation setup in VoxPlanNavHost
   - ViewModel factory registration

2. **Goal Hierarchy Flow**:
   - MainScreen → MainViewModel → TodoRepository → TodoDao → AppDatabase
   - SharedViewModel breadcrumb management
   - ActionMode reordering system

3. **Daily Planning Flow**:
   - DailyScreen → DailyViewModel → EventRepository + QuotaRepository
   - Parent daily creation and quota integration
   - Vertical reordering via ActionMode

4. **Scheduling Flow**:
   - DaySchedule → SchedulerViewModel → EventRepository
   - Date reactivity with flatMapLatest pattern
   - Parent daily → scheduled child transformation

5. **Focus Mode Flow**:
   - FocusModeScreen → FocusViewModel → TimeBankRepository
   - Timer state management with medals
   - Time banking and event creation

### 3. Core Modules and Their Responsibilities

**What to include**:
- Detailed description of each major module
- File-by-file breakdown with line counts
- Key classes and their purposes
- Dependencies and integration points

**For VoxPlanApp, document modules**:

**UI ViewModels**:
- `MainViewModel.kt` (362 lines):
  - Goal list state management
  - Breadcrumb navigation coordination
  - ActionMode reordering logic
  - SharedViewModel integration
- `FocusViewModel.kt` (492 lines):
  - Complex state: timer, medals, discrete tasks
  - Time banking logic
  - Medal calculation (30→60→90→120+ mins)
- `DailyViewModel.kt` (190 lines):
  - Daily task state with quota integration
  - Parent daily management
  - Vertical reordering
- `SchedulerViewModel.kt` (141 lines):
  - Date-driven event reactivity
  - Day view state management
- `GoalEditViewModel.kt` (191 lines):
  - Goal editing with quota settings
  - Active days encoding/decoding
- `ProgressViewModel.kt` (174 lines):
  - Weekly quota progress tracking
- `SharedViewModel.kt` (80 lines):
  - Breadcrumb navigation state (StateFlow)
  - Recursive processGoals() function (max depth 3)

**UI Screens**:
- `MainScreen.kt` (492 lines): Goal hierarchy UI with ActionMode buttons
- `DailyScreen.kt` (628 lines): Daily tasks with quota integration
- `DaySchedule.kt` (675 lines): Day scheduler with delete dialog bug (lines 110-126)
- `FocusModeScreen.kt` (697 lines): Timer UI with medals and tasks
- `GoalEditScreen.kt` (292 lines): Goal editing form with quota settings

**Data Layer**:
- `AppDatabase.kt` (384 lines):
  - Room database configuration
  - 13 migrations documented
  - Current version: 13
- `TodoRepository.kt` (76 lines): Goal CRUD with Flow-based queries
- `EventRepository.kt` (32 lines): Event CRUD with parent-child filtering
- `TimeBankRepository.kt` (56 lines): Time tracking entries
- `QuotaRepository.kt` (47 lines): Quota management with active days logic

**Navigation**:
- `VoxPlanNavHost.kt`: Route definitions, composable routing
- `VoxPlanScreen.kt`: Sealed class for type-safe navigation
- `VoxPlanApp.kt`: Scaffold with bottom navigation, ActionModeHandler

### 4. Architecture Patterns & Data Flow

**What to include**:
- MVVM pattern implementation details
- Repository pattern usage
- StateFlow reactive patterns
- Navigation architecture
- Dependency injection approach

**For VoxPlanApp, document**:

**MVVM with Manual DI**:
```kotlin
// Pattern: VoxPlanApplication creates AppDataContainer (implements AppContainer)
val application = (context.applicationContext as VoxPlanApplication)
val repository = application.container.todoRepository

// Pattern: AppViewModelProvider.Factory creates ViewModels with injected dependencies
initializer {
    MainViewModel(
        todoRepository = voxPlanApplication().container.todoRepository,
        sharedViewModel = sharedViewModel
    )
}
```

**State Management Architecture**:
- **StateFlow**: Reactive, observable data from repositories
- **Compose State (mutableStateOf)**: UI-local, transient state
- **SharedViewModel**: Cross-screen state (breadcrumb navigation)

**Critical StateFlow Pattern**:
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

**Flow Reactive Patterns**:
1. Simple Flow Subscription: `val goals = todoRepository.getItemsByIds(goalIds).first()`
2. Combining Flows: `combine(flow1, flow2) { data1, data2 → CombinedState(data1, data2) }`
3. Date-Driven State: `_currentDate.flatMapLatest { date → eventRepository.getEventsForDate(date) }`
4. Compose State to Flow: `snapshotFlow { _uiState.value.date }.flatMapLatest { ... }`

**ActionMode Reordering System**:
- Enum states: Normal, VerticalUp, VerticalDown, HierarchyUp, HierarchyDown
- Pattern: Button click → ActionModeHandler toggles state → User clicks goal → ViewModel calculates new order → updateItemsInTransaction() → Flow emits → UI re-renders

### 5. Major Data Models and State Management

**What to include**:
- Database entities with field descriptions
- Room relationships and foreign keys
- StateFlow/Flow usage patterns
- Type converters and serialization

**For VoxPlanApp, document**:

**Entities**:
- `TodoItem`: Hierarchical goals/subgoals (max depth: 3)
  - Fields: id, title, description, parentId, order, createdDate, completed
  - Relationships: Self-referencing for hierarchy

- `Event`: Dual-purpose entity for dailies AND scheduled blocks
  - Fields: id, goalId, title, startDate, startTime, endTime, parentDailyId, order
  - **Critical Pattern**: parentDailyId distinguishes types
    - Parent Daily: `parentDailyId = null`, `startTime/endTime = null`
    - Scheduled Child: `parentDailyId = dailyId`, `startTime/endTime` populated

- `Quota`: Daily time quotas for goals
  - Fields: id, goalId, minutes, activeDays (7-char string)
  - **Active Days Encoding**: "1111100" = Mon-Fri active
  - Index calculation: `dayOfWeek.value - 1` (0-based)
  - Check active: `quota.activeDays[dayIndex] == '1'`

- `TimeBank`: Time tracking entries
  - Fields: id, goalId, date, minutes
  - Used for: Focus mode accrued time

**Type Converters**:
- LocalDate: Stored as Long (epoch day)
- LocalTime: Stored as String (HH:mm format)
- File: `data/Converters.kt` applied to AppDatabase

### 6. External Dependencies and Integrations

**What to include**:
- Android/Jetpack libraries and versions
- Third-party dependencies
- Build configuration highlights

**For VoxPlanApp, document**:

**Key Dependencies**:
- Kotlin Version: 1.9.0
- Compose Compiler: 1.5.1
- Target/Compile SDK: 34
- Min SDK: 27 (Android 8.1)
- Room: 2.6.1 (with KSP for annotation processing)
- Compose BOM: 2023.08.00
- Navigation Compose: 2.7.7
- Media3 ExoPlayer: 1.1.1 (for sound effects)

**Room Configuration**:
- Schema export: `app/schemas/` directory
- Configuration location: `app/build.gradle.kts` line 88-90

### 7. Notable Patterns, Conventions, and Anti-Patterns

**What to include**:
- Architectural patterns observed in the code
- Coding conventions and style
- Known issues and anti-patterns
- Best practices established in the codebase

**For VoxPlanApp, document**:

**Architectural Decisions**:
1. **Single Event Entity**: Flexible design using `parentDailyId` to distinguish dailies from scheduled blocks
2. **Manual DI Over Hilt**: Simpler, more transparent, no reflection overhead
3. **SharedViewModel Pattern**: Single instance shared across ViewModels for breadcrumb state
4. **StateFlow with 5-second timeout**: `SharingStarted.WhileSubscribed(5_000L)` balances reactivity with efficiency
5. **Max Depth 3**: Hierarchical goals limited to prevent infinite recursion
6. **Foreign Key Cascades**: Quota → TodoItem with ON DELETE CASCADE

**Code Style Conventions**:
- ViewModels: `private val _state = MutableStateFlow()` + `val state = _state.asStateFlow()`
- UI state: Data classes with default values
- Repository methods: `Flow<T>` for reads, `suspend fun` for writes
- Composables: Receive ViewModel, not individual state parameters
- Log tags: Use class name: `Log.d("MainViewModel", "...")`
- Package structure: By feature (ui.main, ui.calendar, ui.goals, etc.)

**Known Issues**:
- **Critical Bug**: Delete dialog in DaySchedule.kt (lines 110-126) references undefined `event` variable
- **QuickScheduleScreen.kt**: Entirely commented out (lines 26-86)
- Incomplete features documented in INCOMPLETE_FEATURES.md

### 8. Development Guidelines

**What to include**:
- Common development tasks with step-by-step instructions
- Testing patterns and strategies
- Build and deployment commands
- Database migration procedures

**For VoxPlanApp, document**:

**Build Commands**:
```bash
# Build debug APK
./gradlew assembleDebug

# Run unit tests
./gradlew test

# Run instrumented tests
./gradlew connectedAndroidTest

# Lint checks
./gradlew lint
```

**Adding a New ViewModel**:
1. Create data classes for UI state in ViewModel file
2. Add ViewModel to `AppViewModelProvider.Factory`
3. Use in Composable: `viewModel = viewModel(factory = AppViewModelProvider.Factory)`

**Database Migration Pattern**:
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
**IMPORTANT**: Increment version in `@Database(version = X)` annotation

**Testing Priorities**:
1. SharedViewModel.processGoals() - hierarchical processing
2. TodoRepository transaction operations
3. Event parent-child relationships and DAO queries
4. Quota active days encoding/decoding
5. FocusViewModel timer state management
6. Database migrations (especially 12→13)

### 9. Critical Files & Line Counts

**What to include**:
- Table of most important files sorted by size/complexity
- Line count and brief description
- Indicators for files requiring special attention

**For VoxPlanApp, create table**:
| File | Lines | Category | Description |
|------|-------|----------|-------------|
| FocusModeScreen.kt | 697 | UI | Timer UI with medals and tasks |
| DaySchedule.kt | 675 | UI | Day scheduler **[HAS DELETE BUG]** |
| DailyScreen.kt | 628 | UI | Daily planning with quota integration |
| MainScreen.kt | 492 | UI | Goal hierarchy with ActionMode |
| FocusViewModel.kt | 492 | ViewModel | Complex timer state management |
| AppDatabase.kt | 384 | Data | Room database with 13 migrations |
| MainViewModel.kt | 362 | ViewModel | Goal list state with breadcrumbs |
| GoalEditScreen.kt | 292 | UI | Goal editing with quotas |
| etc... | | | |

### 10. Constants & Magic Numbers

**What to include**:
- Application constants and their locations
- Hardcoded values developers should know about
- Configuration values that might need adjustment

**For VoxPlanApp, document**:
```kotlin
// File: data/Constants.kt
FULLBAR_MINS = 60  // Minutes to fill one power bar
pointsForItemCompletion = 15  // Time bonus for completing task

// Hardcoded values:
- Max goal depth: 3 (in SharedViewModel.processGoals)
- Timer timeout: 5000ms (StateFlow WhileSubscribed)
- Default scroll hour: 6 AM (in DaySchedule)
- Medal thresholds: 30, 60, 90, 120 minutes
```

## Style Guidelines

### Tone & Perspective
- Write for developers actively coding
- Use technical language and code references freely
- Include file paths, line numbers, and code snippets
- Focus on "how it works" and "how to modify it"

### Structure
- Use markdown tables for structured data
- Include code blocks for patterns and examples
- Link between related sections
- Annotate critical files with [CORE], [KEY], [COMPLEX] indicators

### Code Examples
- Show actual code patterns from the codebase
- Include both good and bad examples where relevant
- Reference specific files and line numbers
- Explain rationale behind patterns

## What NOT to Include
- ❌ High-level vision or philosophy (belongs in project_context.md)
- ❌ User-facing workflows without implementation details
- ❌ Domain concepts without technical grounding
- ❌ Product strategy or MVP definition

These belong in `project_context.md` instead.

## Research Strategy

### Sources to Analyze
1. **Codebase Structure**:
   - Use `Glob` to discover all .kt files
   - Organize by feature module
   - Check file sizes and line counts

2. **ViewModels** (state management patterns):
   - MainViewModel, FocusViewModel, DailyViewModel, etc.
   - Identify StateFlow patterns
   - Document state management approaches

3. **Data Layer** (persistence patterns):
   - AppDatabase, entities, DAOs, repositories
   - Migration history
   - Type converters

4. **UI Screens** (component patterns):
   - MainScreen, DailyScreen, DaySchedule, etc.
   - Identify Composable patterns
   - Document UI state handling

5. **Navigation** (routing patterns):
   - VoxPlanNavHost, VoxPlanScreen, VoxPlanApp
   - Navigation state management

6. **Documentation**:
   - ARCHITECTURE.md for technical details
   - CLAUDE.md for build commands and conventions
   - INCOMPLETE_FEATURES.md for known issues

### Analysis Approach
1. Map complete directory structure with Glob
2. Read major files to understand architecture patterns
3. Identify data flow patterns from ViewModels through repositories
4. Document state management approaches (StateFlow, combine, flatMapLatest)
5. Extract code style conventions from existing patterns
6. Identify known issues and anti-patterns
7. Create development guidelines from existing practices

## Deliverable Format
Create a single markdown file: `/Users/richardthompson/StudioProjects/VoxPlanApp/agent/context/codebase_context.md`

The file should be:
- **Comprehensive**: 1000-2000 lines covering all implementation details
- **Technical**: Full of code snippets, file paths, line numbers
- **Practical**: Focused on helping developers navigate and modify code
- **Current**: Reflects actual codebase state including known bugs
- **Organized**: Clear sections with tables of contents

## Success Criteria
After reading this document, a developer should be able to:
1. Navigate to any major file or module quickly
2. Understand the complete data flow for any user action
3. Add a new feature following established patterns
4. Modify database schema with proper migrations
5. Debug issues by knowing where to look in the code
6. Understand architectural decisions and their implementation
7. Follow code conventions consistently

## Example Opening (for inspiration)
```markdown
# VoxPlanApp Codebase Context

## Table of Contents
1. [Directory and File Structure Mapping](#directory-and-file-structure-mapping)
2. [Key Entry Points and Main Flows](#key-entry-points-and-main-flows)
3. [Core Modules and Their Responsibilities](#core-modules-and-their-responsibilities)
...

## Directory and File Structure Mapping

```
/ (project root)
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/voxplanapp/
│   │   │   │   ├── ui/
│   │   │   │   │   ├── main/                     # Goal hierarchy management
│   │   │   │   │   │   ├── MainScreen.kt (492 lines) - [CORE] Goal list UI
│   │   │   │   │   │   └── MainViewModel.kt (362 lines) - [CORE] State management
...
```

---

## Next Steps
1. Read this prompt carefully
2. Use Glob and Read tools to explore the VoxPlanApp codebase systematically
3. Create the `codebase_context.md` file following this structure
4. Include specific file paths, line numbers, and code snippets
5. Cross-reference with project_context.md to ensure proper separation of concerns
6. Focus on technical implementation details that help developers work with the code
