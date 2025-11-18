# VoxPlanApp - Complete UI/UX Architecture Documentation

## Executive Summary
VoxPlanApp is a goal planning and time management application built with Jetpack Compose. It features a hierarchical goal structure, daily task management, scheduling capabilities, and focus mode for concentrated work sessions. The app uses a bottom navigation bar to switch between main sections and implements a sophisticated state management system using StateFlow and SharedViewModel.

---

## 1. SCREEN ARCHITECTURE & ORGANIZATION

### 1.1 Screen Structure Overview
The app is organized around a feature-based structure with dedicated packages for each major feature:

```
com.voxplanapp.ui/
├── main/              # Main goals/todos screen
├── goals/             # Goal editing and progress tracking
├── daily/             # Daily task management (Beta)
├── calendar/          # Day scheduler/planning
├── focusmode/         # Focus timer screen
├── theme/             # Material 3 theming
└── constants/         # Colors, dimensions, text styles
```

### 1.2 Main Screens

#### Screen 1: Main Screen (Goals/Todos)
**File Path:** `/Users/richardthompson/StudioProjects/VoxPlanApp/app/src/main/java/com/voxplanapp/ui/main/MainScreen.kt`
**ViewModel:** MainViewModel.kt
**Route:** `VoxPlanScreen.Main.route = "main"`
**Purpose:** Display hierarchical goals and todos with expansion capabilities

**Key Features:**
- Hierarchical goal display (parents and sub-goals)
- Expandable/collapsible goal groups
- Breadcrumb navigation for sub-goal tracking
- Power bar showing daily time accumulation (diamonds, bars, coins)
- Todo input bar at bottom
- Reordering controls (vertical up/down, hierarchy up/down)
- Goal completion checkboxes
- Quick add to daily tasks

**UI Components:**
- `PowerBar()` - Displays accumulated time with game-like rewards
- `Diamond()` - Visual element for 4-hour chunks
- `OneBar()` - Progress bar element
- `GoalListContainer()` - LazyColumn of goal items
- `GoalItem()` - Individual goal card with actions
- `TodoInputBar()` - Input field with FAB for adding new goals
- `BreadcrumbNavigation()` - Shows navigation path
- `ReorderButtons()` - Mode toggle buttons

**ViewModel State:**
```kotlin
data class MainUiState(
    val goalList: List<GoalWithSubGoals> = listOf(),
    val breadcrumbs: List<GoalWithSubGoals> = listOf()
)
```

---

#### Screen 2: Goal Edit Screen
**File Path:** `/Users/richardthompson/StudioProjects/VoxPlanApp/app/src/main/java/com/voxplanapp/ui/goals/GoalEditScreen.kt`
**ViewModel:** GoalEditViewModel.kt
**Route:** `VoxPlanScreen.GoalEdit.routeWithArgs = "goal_edit/{goalId}"`
**Purpose:** Edit goal details, set quotas, and manage sub-goals

**Key Features:**
- Goal title and description editing
- Goal completion status
- Quota management (daily minutes target, active days)
- Sub-goal display and navigation
- Navigation to day scheduler
- Parent goal display

**ViewModel State:**
```kotlin
data class GoalUiState(
    val goal: GoalWithSubGoals?,
    val isLoading: Boolean = true,
    val error: String? = null,
    val quotaMinutes: Int = 60,
    val quotaActiveDays: Set<DayOfWeek> = setOf()
)
```

---

#### Screen 3: Progress Screen (Quotas)
**File Path:** `/Users/richardthompson/StudioProjects/VoxPlanApp/app/src/main/java/com/voxplanapp/ui/goals/ProgressScreen.kt`
**ViewModel:** ProgressViewModel.kt
**Route:** `VoxPlanScreen.Progress.route = "progress"`
**Purpose:** Display weekly progress on quota-tracked goals

**Key Features:**
- Week navigation (previous/next week)
- Weekly summary with emerald tracker (7-day completion)
- Daily progress cards for each day of week
- Goal progress per day with star ratings
- Diamond icons for 4-hour achievements
- Color-coded cards (today, past, future)

**UI Components:**
- `WeeklySummary()` - Overall week stats
- `DayProgressCard()` - Per-day progress display
- `GoalProgressRow()` - Goal-specific stats with stars
- `GoalSummaryRow()` - Weekly totals per goal
- `WeekNavigator()` - Week selection buttons
- `EmeraldIcon()` / `Gem()` - Completion tracker
- `DiamondIcon()` - Achievement display

**ViewModel State:**
```kotlin
data class ProgressUiState(
    val currentWeek: LocalDate = LocalDate.now().with(DayOfWeek.MONDAY),
    val dailyProgress: List<DayProgress> = emptyList(),
    val weekTotal: WeekTotal = WeekTotal(emptyList()),
    val completedDays: Int = 0
)
```

---

#### Screen 4: Daily Screen (Dailies - Beta)
**File Path:** `/Users/richardthompson/StudioProjects/VoxPlanApp/app/src/main/java/com/voxplanapp/ui/daily/DailyScreen.kt`
**ViewModel:** DailyViewModel.kt
**Route:** `VoxPlanScreen.Daily.routeWithArgs = "daily/{date}?newEventId={newEventId}"`
**Purpose:** Manage daily tasks and create scheduled events

**Key Features:**
- Date navigation (previous/next day, jump to today)
- Daily task list with parent/child event relationships
- Add quota tasks to daily
- Reorder tasks (vertical up/down)
- Delete confirmation dialog
- Direct scheduling from daily to calendar
- Event duration dialogs

**Navigation Arguments:**
- `date`: LocalDate (required) - Current daily date
- `newEventId`: Int? (optional) - Event needing duration setup

**ViewModel State:**
```kotlin
data class DailyUiState(
    val date: LocalDate = LocalDate.now(),
    val dailyTasks: List<Event> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val eventNeedingDuration: Int? = null
)
```

---

#### Screen 5: Day Scheduler/Calendar
**File Path:** `/Users/richardthompson/StudioProjects/VoxPlanApp/app/src/main/java/com/voxplanapp/ui/calendar/DaySchedule.kt`
**ViewModel:** SchedulerViewModel.kt
**Route:** `VoxPlanScreen.DaySchedule.routeWithArgs = "day_schedule/{date}"`
**Purpose:** Visual hourly schedule for planning and time blocking

**Key Features:**
- Hourly grid display (1-24 hours)
- Draggable event blocks
- Event duration adjustment via drag
- Quick event creation by tapping on time slots
- Date navigation
- Focus mode entry from scheduled events
- Delete with parent daily cleanup confirmation

**ViewModel State:**
```kotlin
val eventsForCurrentDate: StateFlow<List<Event>>
val showDeleteParentDialog: StateFlow<Event?>
```

**Layout Constants:**
```kotlin
data class ScheduleParams(
    val hourHeight: Dp,      // 48.dp
    val startHour: Int,      // 1
    val endHour: Int         // 24
)
```

---

#### Screen 6: Focus Mode Screen
**File Path:** `/Users/richardthompson/StudioProjects/VoxPlanApp/app/src/main/java/com/voxplanapp/ui/focusmode/FocusModeScreen.kt`
**ViewModel:** FocusViewModel.kt
**Route:** `VoxPlanScreen.FocusMode.routeWithArgs = "focus_mode/{goalId}"` OR `"focus_mode?eventId={eventId}"`
**Purpose:** Immersive focus timer for concentrated work

**Key Features:**
- Large timer display (work/rest modes)
- Pomodoro-style work/rest cycles
- Timer adjustment controls (±1, ±5 minutes)
- Progress indicator
- Task-based or time-based discrete work tracking
- Time bank accumulation
- No bottom navigation bar (full-screen focus)

**Navigation Arguments:**
- `goalId`: Int (alternative) - For goal-based focus
- `eventId`: Int (alternative) - For event-based focus

---

### 1.3 Navigation Graph

```
VoxPlanApp (Root)
├── VoxPlanNavHost
│   ├── Main (route: "main")
│   │   ├── → GoalEdit (goalId argument)
│   │   ├── → FocusMode (goalId)
│   │   └── → Daily (date, optional newEventId)
│   │
│   ├── GoalEdit (route: "goal_edit/{goalId}")
│   │   ├── → DaySchedule (date)
│   │   └── → FocusMode (goalId)
│   │
│   ├── Progress (route: "progress")
│   │
│   ├── DaySchedule (route: "day_schedule/{date}")
│   │   └── → FocusMode (eventId)
│   │
│   ├── FocusMode (route: "focus_mode/{goalId}" | "focus_mode?eventId={eventId}")
│   │   └── ← All routes (navigateUp)
│   │
│   └── Daily (route: "daily/{date}?newEventId={newEventId}")
│       └── → DaySchedule (date)
│
└── BottomNavigationBar (4 items)
    ├── Goals → Main
    ├── Daily → Daily (today)
    ├── Progress → Progress
    └── Schedule → DaySchedule (today)
```

---

### 1.4 Navigation File Reference

**File Path:** `/Users/richardthompson/StudioProjects/VoxPlanApp/app/src/main/java/com/voxplanapp/navigation/VoxPlanNavHost.kt`

The NavHost composable manages all route transitions and argument passing. It uses Android Navigation's `navArgument` system with type conversion:

```kotlin
// Example: Goal Edit route with Int argument
composable(
    route = VoxPlanScreen.GoalEdit.routeWithArgs,
    arguments = listOf(
        navArgument(VoxPlanScreen.GoalEdit.goalIdArg) {
            type = NavType.IntType
        })
)

// Example: Daily route with LocalDate and optional Int
composable(
    route = VoxPlanScreen.Daily.routeWithArgs,
    arguments = listOf(
        navArgument(VoxPlanScreen.Daily.dateArg) {
            type = NavType.StringType
        },
        navArgument(VoxPlanScreen.Daily.newEventIdArg) {
            type = NavType.StringType
            nullable = true
            defaultValue = null
        }
    )
)
```

---

## 2. COMPOSE UI PATTERNS & COMPONENTS

### 2.1 Reusable Component Hierarchy

#### High-Level Components
1. **Screen Containers**
   - Scaffold + TopAppBar + Content Area + BottomBar
   - Used in: MainScreen, DailyScreen, GoalEditScreen, ProgressScreen

2. **Navigation Components**
   - `VoxPlanTopAppBar()` - Centered title with back button and actions
   - `VoxPlanApp()` - Root with bottom navigation
   - `BottomNavigationBar()` - 4-item navigation
   - `BreadcrumbNavigation()` - Hierarchical breadcrumbs

3. **Goal/Task Components**
   - `GoalItem()` - Expandable goal card with actions
   - `SubGoalItem()` - Sub-goal display with icons
   - `GoalListContainer()` - LazyColumn of goals
   - `IconDropDownMenu()` - Actions menu (edit, complete, delete)

4. **Input Components**
   - `TodoInputBar()` - Text field with FAB for adding todos
   - `QuotaSettingsSection()` - Quota configuration UI

5. **Progress/Status Components**
   - `PowerBar()` - Game-like time accumulation display
   - `DayProgressCard()` - Daily progress summary
   - `GoalProgressRow()` - Goal achievement stats
   - `WeeklySummary()` - Week overview

6. **Icon Components**
   - `FocusModeIcon()` - Stream icon for focus entry
   - `HasSubGoalsIcon()` - List icon for sub-goals
   - `EmeraldIcon()` / `Gem()` - Completion tracker
   - `DiamondIcon()` / `Diamond()` - Achievement display

#### Component File Locations

| Component | File Path |
|-----------|-----------|
| GoalItem, SubGoalItem, IconDropDownMenu | `/ui/main/GoalItem.kt` |
| GoalListContainer | `/ui/main/GoalListContainer.kt` |
| TodoInputBar | `/ui/main/TodoInputBar.kt` |
| BreadcrumbNavigation | `/ui/main/BreadCrumbNavigation.kt` |
| PowerBar, Diamond, OneBar | `/ui/main/MainScreen.kt` |
| VoxPlanTopAppBar, VoxPlanApp, BottomNavigationBar | `/navigation/VoxPlanApp.kt` |
| ProgressScreen components | `/ui/goals/ProgressScreen.kt` |
| QuotaSettingsSection | `/ui/goals/QuotaSettings.kt` |

---

### 2.2 UI Pattern: Goal Card with Nested Sub-Goals

```
┌─────────────────────────────────────────┐
│ [▼] Goal Title                [+] [⧉] │  ← GoalItem (Card)
└─────────────────────────────────────────┘
  ┌───────────────────────────────────────┐
  │   SubGoal 1            [⧉]            │  ← SubGoalItem
  ├───────────────────────────────────────┤
  │   SubGoal 2            [⧉]            │  ← SubGoalItem
  └───────────────────────────────────────┘
```

**Expand Logic:**
- Clicking expand/collapse icon toggles `goal.expanded` state
- Expanded sub-goals rendered in background color below parent
- Dynamic dividers between sub-goals

---

### 2.3 UI Pattern: Power Bar (Time Accumulation)

```
┌──────────────────────────────────────────────────────────┐
│ POWER:        [Bar] [Bar] [Bar] [Bar]      [60]          │
│ ◆ x 2                                                     │
└──────────────────────────────────────────────────────────┘

Diamond = 4 hours (240 minutes)
Bars = 0-60 minutes each, color-coded:
  - Green = Full (60 mins)
  - Red = Partial
Coin = Remaining minutes (0-59)
```

**Logic:**
```kotlin
val diamonds = totalMinutes / 240
val bars = (0..3).map { minOf(60, maxOf(0, remainingTime - it * 60)) }
```

---

### 2.4 Common UI Patterns

#### Pattern 1: Action Mode Buttons (Reordering)
```kotlin
ReorderButtons(
    onVUpClick = { actionModeHandler.toggleUpActive() },
    onVDownClick = { actionModeHandler.toggleDownActive() },
    onHUpClick = { actionModeHandler.toggleHierarchyUp() },
    onHDownClick = { actionModeHandler.toggleHierarchyDown() },
    currentMode = actionMode
)

// Buttons color based on current mode:
// - ActivatedColor when in that mode
// - PrimaryColor when inactive
```

#### Pattern 2: Hierarchical Navigation with Breadcrumbs
```
MAIN > Goal1 > SubGoal1 > DeepGoal
  ▼     ▼         ▼          ▼
 Click  Click    Click      Current
```

Each breadcrumb is clickable to jump back to that level.

#### Pattern 3: Modal Dialogs for Confirmations
```kotlin
AlertDialog(
    onDismissRequest = { cancelAction() },
    title = { Text("Confirm Action") },
    text = { Text("Details...") },
    confirmButton = { Button(onClick = { confirmAction() }) },
    dismissButton = { Button(onClick = { cancelAction() }) }
)
```

Used for:
- Delete confirmations (daily tasks)
- Schedule event creation
- Goal completion

---

### 2.5 Material 3 & Custom Theming

**Theme Application:**
- `VoxPlanAppTheme()` composable wraps entire app
- Supports light/dark mode with dynamic colors (Android 12+)
- Custom color palette for business domain

**Key Theme Colors:**
```kotlin
// Material 3 color scheme
val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40
)

// Custom colors (in constants/Colors.kt)
val TodoItemBackgroundColor = PrimaryLightColor      // #82BD85 (Green)
val TodoItemIconColor = PrimaryColor                  // #0F0A2C (Dark Purple)
val SubGoalItemBackGroundColor = SecondaryColor      // #C7E2C9 (Light Green)
val EventBoxColor = Color(0xFF41c300)                // Bright Green
val ActivatedColor = Color(0xFFB21720)               // Red (for active mode)
val TopAppBarBgColor = TertiaryColor                 // #82babd (Teal)
```

---

## 3. VIEWMODEL & STATE MANAGEMENT

### 3.1 ViewModel Architecture

All ViewModels inherit from `androidx.lifecycle.ViewModel` and use:
- `StateFlow` for reactive UI state
- `viewModelScope` for coroutine management
- `MutableStateFlow` for internal state
- Repositories for data access

### 3.2 ViewModel Overview Table

| ViewModel | Location | Key State | Purpose |
|-----------|----------|-----------|---------|
| **MainViewModel** | `/ui/main/MainViewModel.kt` | `mainUiState: StateFlow<MainUiState>`, `todayTotalTime: StateFlow<Int>`, `actionMode: State<ActionMode>` | Main screen: goal list, breadcrumbs, power bar |
| **GoalEditViewModel** | `/ui/goals/GoalEditViewModel.kt` | `goalUiState: State<GoalUiState>` | Edit screen: goal details, quotas |
| **ProgressViewModel** | `/ui/goals/ProgressViewModel.kt` | `uiState: StateFlow<ProgressUiState>` | Progress: weekly tracking, daily breakdowns |
| **DailyViewModel** | `/ui/daily/DailyViewModel.kt` | `uiState: StateFlow<DailyUiState>`, `actionMode: State<ActionMode>` | Daily: task list for date, reordering |
| **SchedulerViewModel** | `/ui/calendar/SchedulerViewModel.kt` | `currentDate: StateFlow<LocalDate>`, `eventsForCurrentDate: StateFlow<List<Event>>` | Calendar: events for date |
| **FocusViewModel** | `/ui/focusmode/FocusViewModel.kt` | `goalUiState: State<GoalWithSubGoals?>`, `focusUiState: State<FocusUiState>`, `timerSettingsState: State<TimerSettingsState>` | Focus mode: timer, progress |
| **NavigationViewModel** | `/navigation/NavigationViewModel.kt` | `selectedItemIndex: StateFlow<Int>` | Bottom nav: track selected tab |
| **SharedViewModel** | `/shared/SharedViewModel.kt` | `breadcrumbs: StateFlow<List<GoalWithSubGoals>>` | Shared: breadcrumb state across screens |

---

### 3.3 Detailed ViewModel Deep Dives

#### MainViewModel
**File:** `/ui/main/MainViewModel.kt`
**Dependencies:**
- TodoRepository
- EventRepository
- TimeBankRepository
- SoundPlayer
- SharedViewModel

**Key Methods:**
```kotlin
// State management
fun clearBreadcrumbs()
fun navigateToSubGoals(goal: GoalWithSubGoals)
fun navigateUp()

// Goal operations
fun reorderItem(goal: GoalWithSubGoals)
fun addTodo(todo: String)
fun completeItem(goal: TodoItem)
fun deleteItem(goal: GoalWithSubGoals)
fun saveExpandedSetting(todoId: Int, expanded: Boolean)

// Navigation
fun addToDaily(goal: TodoItem, onEventCreated: (Int) -> Unit)

// Action mode handling
val actionModeHandler: ActionModeHandler
```

**State Transformation:**
Combines TodoRepository stream with SharedViewModel breadcrumbs to create hierarchical goal view.

---

#### SharedViewModel
**File:** `/shared/SharedViewModel.kt`
**Purpose:** Central breadcrumb state management shared across all screens

**Key Methods:**
```kotlin
// Process todos into hierarchical structure
fun processGoals(todos: List<TodoItem>, parentId: Int?, depth: Int = 1): List<GoalWithSubGoals>

// Find specific goal in hierarchy
fun getGoalWithSubGoals(todos: List<TodoItem>, goalId: Int, depth: Int = 1): GoalWithSubGoals?

// Breadcrumb navigation
fun navigateToSubGoal(goal: GoalWithSubGoals, parentGoal: GoalWithSubGoals?)
fun navigateUp()
fun clearBreadcrumbs()
fun getTopBreadCrumb(): GoalWithSubGoals?
```

**State:**
```kotlin
private val _breadcrumbs = MutableStateFlow<List<GoalWithSubGoals>>(emptyList())
val breadcrumbs: StateFlow<List<GoalWithSubGoals>>
```

---

#### ProgressViewModel
**File:** `/ui/goals/ProgressViewModel.kt`

**State:**
```kotlin
data class ProgressUiState(
    val currentWeek: LocalDate,
    val dailyProgress: List<DayProgress>,
    val weekTotal: WeekTotal,
    val completedDays: Int
)

data class DayProgress(
    val dayOfWeek: DayOfWeek,
    val goalProgress: List<GoalProgress>,
    val isComplete: Boolean,
    val diamonds: Int
)

data class GoalProgress(
    val goalId: Int,
    val title: String,
    val minutesAchieved: Int,
    val quotaMinutes: Int
)
```

**Key Methods:**
```kotlin
fun previousWeek()      // Load previous week's data
fun nextWeek()         // Load next week's data
```

---

#### FocusViewModel
**File:** `/ui/focusmode/FocusViewModel.kt`

**Key State:**
```kotlin
var goalUiState: State<GoalWithSubGoals?>
var eventUiState: State<Event?>
var focusUiState: State<FocusUiState>
var timerSettingsState: State<TimerSettingsState>
```

**State Classes:**
```kotlin
data class FocusUiState(
    val isLoading: Boolean = true,
    val timeRemaining: Long = 0L,
    val isRunning: Boolean = false,
    val mode: String = "work"  // "work" or "rest"
)

data class TimerSettingsState(
    val workMinutes: Int = 25,
    val restMinutes: Int = 5
)
```

---

### 3.4 Action Mode Handler

**File:** `/navigation/VoxPlanApp.kt`

```kotlin
class ActionModeHandler(
    private val actionModeState: MutableState<ActionMode>
) {
    fun toggleUpActive()
    fun toggleDownActive()
    fun toggleHierarchyUp()
    fun toggleHierarchyDown()
    fun deactivateButtons()
}

enum class ActionMode {
    Normal,
    VerticalUp,
    VerticalDown,
    HierarchyUp,
    HierarchyDown
}
```

Used in: MainScreen, DailyScreen for controlling reordering operations.

---

### 3.5 Event-Driven State Updates

**Pattern: ViewModel Launch & Collect**
```kotlin
val state by viewModel.uiState.collectAsState()

// Trigger actions
Button(onClick = { viewModel.deleteItem(item) })
```

**Pattern: SavedStateHandle for Navigation Args**
```kotlin
class GoalEditViewModel(
    savedStateHandle: SavedStateHandle
) {
    private val goalId: Int = checkNotNull(
        savedStateHandle[VoxPlanScreen.GoalEdit.goalIdArg]
    )
}
```

---

## 4. RESOURCE ORGANIZATION

### 4.1 UI Constants Structure

**Location:** `/ui/constants/`

#### DpValues.kt
```kotlin
val SmallDp: Dp = 4.dp       // Minimal spacing
val MediumDp: Dp = 8.dp      // Standard spacing
val LargeDp: Dp = 16.dp      // Large spacing

val TodoItemHeight: Dp = 48.dp
val TodoItemIconSize: Dp = 24.dp
val SubGoalItemHeight: Dp = 44.dp
val SubGoalItemIconSize: Dp = 22.dp
val TodoInputBarHeight: Dp = 64.dp
val TodoInputBarFabSize: Dp = 40.dp
val OverlappingHeight = TodoInputBarHeight
```

#### Colors.kt
```kotlin
// Primary color palette
val PrimaryColor = Color(0xFF0F0A2C)          // Dark Purple
val PrimaryDarkColor = Color(0xFF009688)      // Teal
val PrimaryLightColor = Color(0xFF82BD85)     // Sage Green

// UI element colors
val TodoItemBackgroundColor = PrimaryLightColor
val TodoItemIconColor = PrimaryColor
val TodoItemTextColor = PrimaryColor
val TopLevelGoalBorderColor = PrimaryDarkColor

// Focus mode colors
val FocusColorWork = Color(0xFF000000)
val FocusColorWorkText = Color(0xFFFFFFFF)
val FocusColorRest = Color(0xFF000000)
val FocusColorRestText = Color(0xFFFFFFFF)

// Toolbar/App bar
val ToolbarColor = Color(0xFFfff2df)
val ToolbarBorderColor = Color(0xFFffddb0)
val TopAppBarBgColor = TertiaryColor

// Event colors
val EventBoxColor = Color(0xFF41c300)

// State colors
val ActivatedColor = Color(0xFFB21720)  // Red for active mode
```

#### TextStyles.kt
```kotlin
val TodoItemTitleTextStyle = TextStyle(
    fontWeight = FontWeight.Medium,
    fontSize = 17.sp,
    letterSpacing = 0.5.sp,
    color = TodoItemTextColor
)

val TodoSubItemTitleTextStyle = TextStyle(
    fontWeight = FontWeight.Medium,
    fontSize = 15.sp,
    letterSpacing = 0.5.sp,
    color = TodoItemTextColor
)

val TodoInputBarTextStyle = TextStyle(
    fontWeight = FontWeight.Medium,
    fontSize = 18.sp,
    letterSpacing = 0.5.sp,
    color = Color.White
)
```

#### Theme Files (Material 3)

**Color.kt:**
```kotlin
val Purple80 = Color(0xFFD0BCFF)      // Light purple
val PurpleGrey80 = Color(0xFFCCC2DC)  // Light purple-grey
val Pink80 = Color(0xFFEFB8C8)        // Light pink

val Purple40 = Color(0xFF6650a4)      // Medium purple
val PurpleGrey40 = Color(0xFF625b71)  // Medium purple-grey
val Pink40 = Color(0xFF7D5260)        // Medium pink
```

**Theme.kt:**
Implements Material 3 theming with dynamic color support for Android 12+.

**Type.kt:**
```kotlin
val Typography = Typography(
    bodyLarge = TextStyle(...),
    headlineMedium = TextStyle(...)
)
```

---

### 4.2 String Resources

**File:** `/res/values/strings.xml`

```xml
<string name="app_name">Vox Plan App</string>
<string name="todo_input_bar_hint">Write your goal</string>
<string name="vox_plan_title">Vox Plan</string>
<string name="goal_edit_screen">Edit Goal</string>
<string name="save_button">Save</string>
<string name="goal_title">Goal description</string>
<string name="goal_details">Goal Details</string>
<string name="goal_notes">Notes</string>
<string name="is_goal_done">Completed ?</string>
<string name="goal_id_label">Goal id#</string>
<string name="reorder_upward">Move Up</string>
<string name="reorder_downward">Move Down</string>
<string name="reorder_right">Make Subgoal</string>
<string name="reorder_left">Make Goal</string>
```

---

### 4.3 Drawable Resources

**Location:** `/res/drawable/`

#### Vector Drawables (XML):
- `ic_add.xml` - Plus icon for adding
- `ic_delete.xml` - Trash icon for deletion
- `ic_empty_check_box.xml` - Unchecked checkbox
- `ic_selected_check_box.xml` - Checked checkbox
- `ic_launcher_foreground.xml` - App icon foreground
- `ic_launcher_background.xml` - App icon background

#### Mipmap Resources:
- `ic_launcher.webp` - App icon (various DPIs)
- `ic_launcher_round.webp` - Rounded app icon

---

### 4.4 Raw Audio Resources

**Location:** `/res/raw/`

- `power_up.mp3` - Sound when reaching 4-hour milestone
- `chaching.mp3` - Sound when completing a goal
- `countdown.mp3` - Countdown timer audio
- `countdown_start.mp3` - Focus session start
- `mario_start.mp3` - Mario theme start
- `mario_coin.mp3` - Mario coin collection sound

---

## 5. NAVIGATION STRUCTURE IN DETAIL

### 5.1 VoxPlanScreen Sealed Class

**File:** `/navigation/VoxPlanScreen.kt`

```kotlin
sealed class VoxPlanScreen(val route: String) {
    object Main: VoxPlanScreen("main")
    
    object GoalEdit : VoxPlanScreen("goal_edit") {
        const val goalIdArg = "goalId"
        val routeWithArgs = "$route/{$goalIdArg}"
    }
    
    object Progress: VoxPlanScreen("progress")
    
    object DaySchedule : VoxPlanScreen("day_schedule") {
        const val dateArg = "date"
        val routeWithArgs = "$route/{$dateArg}"
        fun createRouteWithDate(date: LocalDate = LocalDate.now()): String
    }
    
    object FocusMode : VoxPlanScreen("focus_mode") {
        const val goalIdArg = "goalId"
        const val eventIdArg = "eventId"
        val routeWithArgs = "$route/{$goalIdArg}"
        val routeWithEventArg = "$route?$eventIdArg={$eventIdArg}"
        fun createRouteFromGoal(goalId: Int): String
        fun createRouteFromEvent(eventId: Int): String
    }
    
    object Daily : VoxPlanScreen("daily") {
        const val dateArg = "date"
        const val newEventIdArg = "newEventId"
        val routeWithArgs = "$route/{$dateArg}?$newEventIdArg={$newEventIdArg}"
        fun createRouteWithDate(
            date: LocalDate = LocalDate.now(),
            newEventId: Int? = null
        ): String
    }
}
```

### 5.2 Bottom Navigation Implementation

**File:** `/navigation/VoxPlanApp.kt`

```kotlin
private val items = listOf(
    BottomNavigationItem(
        title = "Goals",
        selectedIcon = Icons.Filled.List,
        unselectedIcon = Icons.Outlined.List,
        route = VoxPlanScreen.Main.route
    ),
    BottomNavigationItem(
        title = "Daily",
        selectedIcon = Icons.Filled.Today,
        unselectedIcon = Icons.Outlined.Today,
        route = VoxPlanScreen.Daily.createRouteWithDate()
    ),
    BottomNavigationItem(
        title = "Progress",
        selectedIcon = Icons.Filled.Timeline,
        unselectedIcon = Icons.Outlined.Timeline,
        route = VoxPlanScreen.Progress.route
    ),
    BottomNavigationItem(
        title = "Schedule",
        selectedIcon = Icons.Filled.DateRange,
        unselectedIcon = Icons.Outlined.DateRange,
        route = VoxPlanScreen.DaySchedule.createRouteWithDate()
    )
)
```

Each item:
- Shows filled icon when selected
- Shows outlined icon when unselected
- Clears back stack on navigation (`popUpTo` + `launchSingleTop`)
- Preserves state on re-selection

---

### 5.3 Navigation Flow Examples

#### Flow 1: Main → Edit → Focus
```
MainScreen (list of goals)
    ↓ Click goal
GoalEditScreen (edit goal details)
    ↓ Click Focus icon
FocusModeScreen (timer)
    ↓ navigateUp()
→ Back to GoalEditScreen
```

#### Flow 2: Main → Daily
```
MainScreen
    ↓ Click "Add to Daily"
→ Creates Event
    ↓ Navigate to Daily with newEventId
DailyScreen (with duration dialog popup)
```

#### Flow 3: Schedule → Focus
```
DayScheduleScreen (hourly grid)
    ↓ Click scheduled event
FocusModeScreen (with event data)
    ↓ navigateUp()
→ Back to DayScheduleScreen
```

---

## 6. LIFECYCLE & STATE PERSISTENCE

### 6.1 ViewModel Scope & Lifecycle

- **MainViewModel**: Lives as long as MainActivity
- **GoalEditViewModel**: Created when navigating to edit, destroyed on back
- **FocusViewModel**: Created when entering focus mode, cleaned up on exit
- **NavigationViewModel**: Shared across app for bottom nav state

### 6.2 SavedStateHandle for Arguments

Used to pass navigation arguments to ViewModels:

```kotlin
// In ViewModel constructor:
val goalId: Int = checkNotNull(savedStateHandle[VoxPlanScreen.GoalEdit.goalIdArg])
val date: LocalDate = savedStateHandle.get<String>(VoxPlanScreen.Daily.dateArg)?.let {
    LocalDate.parse(it)
} ?: LocalDate.now()
```

### 6.3 State Flow Subscriptions

Screens collect StateFlow values:

```kotlin
val uiState by viewModel.uiState.collectAsState()
val actionMode by viewModel.actionMode  // Regular State

// Trigger recomposition on state change
Button(onClick = { uiState.dailyTasks.forEach { ... } })
```

---

## 7. INCOMPLETE / BETA FEATURES

### 7.1 Incomplete Screens

1. **QuickScheduleScreen** (`/ui/main/QuickScheduleScreen.kt`)
   - Status: Commented out/stub implementation
   - Purpose: Would allow quick scheduling from Main screen
   - Missing: Full implementation, UI polish

2. **DailyScreen** (`/ui/daily/DailyScreen.kt`)
   - Status: Beta (marked in documentation)
   - Current: Functional but may have limitations
   - Features: Date navigation, task list, event management

### 7.2 Partially Implemented Features

1. **Quotas & Progress Tracking**
   - Quota settings UI functional
   - Progress screen works with time bank data
   - Need more integration testing

2. **Time Bank System**
   - Backend: Functional
   - UI: Displayed in power bar and progress screen

---

## 8. UI/UX DESIGN PATTERNS SUMMARY

### 8.1 Visual Hierarchy
```
Main Content Area (LazyColumn with goals)
    ↓
Goal Cards (expandable sections)
    ↓
Nested Sub-Goal Items (indented, colored differently)
    ↓
Action Icons (edit, complete, delete, focus)
```

### 8.2 Color Coding Strategy

| Color | Meaning |
|-------|---------|
| Green (#82BD85) | Primary goals/items, Active content |
| Dark Purple (#0F0A2C) | Text, icons on light backgrounds |
| Teal (#82babd) | Top app bar, important sections |
| Light Green (#C7E2C9) | Sub-goals background |
| Cream/Beige (#fff2df) | Toolbar background |
| Red (#B21720) | Active mode indicator |
| Bright Green (#41c300) | Event boxes in scheduler |

### 8.3 Spacing Strategy

| Size | Use Cases |
|------|-----------|
| SmallDp (4dp) | Minimal gaps, internal padding |
| MediumDp (8dp) | Standard spacing between elements |
| LargeDp (16dp) | Major gaps, card padding |

### 8.4 Typography

- **Titles**: 18sp, Medium weight, 0.5sp letter spacing
- **Subtitles**: 15sp, Medium weight, 0.5sp letter spacing
- **Input Text**: 18sp, Medium weight, white text
- **Body**: 16sp, Normal weight (Material 3 default)

---

## 9. COMPLETE FILE REFERENCE

### Screens
- `/ui/main/MainScreen.kt` - Goals/todos list with power bar
- `/ui/goals/GoalEditScreen.kt` - Edit goal and quotas
- `/ui/goals/ProgressScreen.kt` - Weekly progress tracking
- `/ui/daily/DailyScreen.kt` - Daily task management
- `/ui/calendar/DaySchedule.kt` - Hourly scheduler
- `/ui/focusmode/FocusModeScreen.kt` - Focus timer

### ViewModels
- `/ui/main/MainViewModel.kt`
- `/ui/goals/GoalEditViewModel.kt`
- `/ui/goals/ProgressViewModel.kt`
- `/ui/daily/DailyViewModel.kt`
- `/ui/calendar/SchedulerViewModel.kt`
- `/ui/focusmode/FocusViewModel.kt`
- `/navigation/NavigationViewModel.kt`
- `/shared/SharedViewModel.kt`

### Reusable Components
- `/ui/main/GoalItem.kt` - Goal card component
- `/ui/main/GoalListContainer.kt` - Goal list container
- `/ui/main/TodoInputBar.kt` - Input field with FAB
- `/ui/main/BreadCrumbNavigation.kt` - Breadcrumb navigation
- `/ui/goals/QuotaSettings.kt` - Quota configuration
- `/ui/goals/ProgressScreen.kt` - Progress components (EmeraldIcon, Gem, etc.)

### Navigation
- `/navigation/VoxPlanApp.kt` - Root composable with bottom nav
- `/navigation/VoxPlanNavHost.kt` - Navigation graph
- `/navigation/VoxPlanScreen.kt` - Screen definitions

### Theme & Constants
- `/ui/theme/Theme.kt` - Material 3 theme setup
- `/ui/theme/Color.kt` - Material color scheme
- `/ui/theme/Type.kt` - Typography definitions
- `/ui/constants/Colors.kt` - Custom colors
- `/ui/constants/DpValues.kt` - Dimension constants
- `/ui/constants/TextStyles.kt` - Text styles
- `/ui/constants/Dimens.kt` - Additional dimensions

### Resources
- `/res/values/strings.xml` - String resources
- `/res/drawable/` - Vector drawables and icons
- `/res/raw/` - Audio files (sounds)
- `/res/mipmap-*/` - App icons

---

## 10. IMPLEMENTATION NOTES

### 10.1 Important Design Decisions

1. **Bottom Navigation over Drawer**
   - Easier navigation with 4 main sections
   - Standard Android pattern for tab-based apps

2. **Breadcrumb Trail for Hierarchy**
   - Allows deep nesting (up to 3 levels)
   - Shows path without traditional back button

3. **Separate Daily vs Calendar**
   - Daily: Task-based management
   - Calendar: Time-based scheduling
   - Complementary, not redundant

4. **Reordering Mode Toggle**
   - ActionMode system prevents accidental reordering
   - Explicit button click required to enable
   - Visual feedback with color change

5. **SharedViewModel for Breadcrumbs**
   - Single source of truth for hierarchical state
   - Available to all screens without prop drilling

### 10.2 Future Enhancement Opportunities

1. **Settings Screen** - Not yet implemented
   - Theme preferences
   - Notification settings
   - Focus mode duration presets

2. **Search/Filter** - Not implemented
   - Filter goals by status
   - Search goals by title

3. **Recurring Goals** - Data model exists but UI not complete
   - RecurrenceType enum in data layer
   - No UI for setting recurrence

4. **Goal Templates** - Not implemented
   - Create goal from templates
   - Quick setup for common goals

5. **Dark Mode Toggle** - Auto based on system settings
   - Could add manual override

---

## CONCLUSION

VoxPlanApp demonstrates a well-structured Jetpack Compose architecture with:
- Clear separation of concerns (screens, components, ViewModels)
- Consistent design system with custom colors and typography
- Flexible navigation with deep linking support
- State management using StateFlow and SharedViewModel
- Hierarchical UI patterns for complex goal structures

The app is production-ready for core features (goals, quotas, progress) with beta features (dailies) needing final polish.

---
