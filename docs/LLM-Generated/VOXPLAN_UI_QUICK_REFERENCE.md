# VoxPlanApp - UI/UX Quick Reference Guide

## 1. SCREEN MAP

```
┌─────────────────────────────────────────────────────────────┐
│                     VOXPLAN APP STRUCTURE                   │
└─────────────────────────────────────────────────────────────┘

                          MainActivity
                                 ↓
                         VoxPlanApp (Root)
                    ┌────────────────────────┐
                    │   NavHost Container    │
                    │  (Scaffold + NavBar)   │
                    └────────┬───────────────┘
                             ↓
        ┌────────────────────┼────────────────────┐
        ↓                    ↓                    ↓
   [Goals]             [Daily] (Beta)      [Progress]
    Main               DailyScreen          ProgressScreen
    ↓                    ↓                    ↓
  [Edit]             [Schedule]           [Week Stats]
GoalEditScreen      DaySchedule
    ↓                  ↓
 [Focus] ←──────────────┤
FocusModeScreen        ↓
                    [Events]

═══════════════════════════════════════════════════════════════
                    BOTTOM NAVIGATION BAR
═══════════════════════════════════════════════════════════════
│ [Goals]  │  [Daily]  │ [Progress] │ [Schedule]   │
└──────────┴───────────┴────────────┴──────────────┘
```

---

## 2. SCREEN DETAILS

### MAIN SCREEN
**File:** MainScreen.kt
**Route:** "main"
**Features:**
- Hierarchical goal list (parents + sub-goals)
- Expandable/collapsible groups
- Power bar (game-like rewards)
- Input field to add new goals
- Breadcrumb navigation trail
- Reordering mode toggle

**ViewModel:** MainViewModel
**Key State:** mainUiState, todayTotalTime, actionMode

---

### GOAL EDIT SCREEN
**File:** GoalEditScreen.kt
**Route:** "goal_edit/{goalId}"
**Features:**
- Edit goal title/description
- Set quotas (daily minutes, active days)
- View parent goal
- Navigate to scheduler
- Manage sub-goals

**ViewModel:** GoalEditViewModel
**Key State:** goalUiState, quotaMinutes, quotaActiveDays

---

### PROGRESS SCREEN
**File:** ProgressScreen.kt
**Route:** "progress"
**Features:**
- Weekly progress overview
- Daily progress cards (colored by status)
- Goal achievements (stars = hours, diamonds = 4h chunks)
- Week navigation (prev/next)
- Emerald tracker (7-day completion)

**ViewModel:** ProgressViewModel
**Key State:** ProgressUiState (currentWeek, dailyProgress, weekTotal)

---

### DAILY SCREEN (BETA)
**File:** DailyScreen.kt
**Route:** "daily/{date}?newEventId={newEventId}"
**Features:**
- List of daily tasks for selected date
- Date navigation
- Add quota tasks
- Reorder tasks (vert up/down)
- Create scheduled events
- Delete confirmation dialog

**ViewModel:** DailyViewModel
**Key State:** DailyUiState (date, dailyTasks, eventNeedingDuration)

---

### DAY SCHEDULER
**File:** DaySchedule.kt
**Route:** "day_schedule/{date}"
**Features:**
- Hourly grid (1am-midnight)
- Draggable event blocks
- Adjust event duration
- Create events by tapping time slots
- Date navigation
- Delete parent daily confirmation

**ViewModel:** SchedulerViewModel
**Key State:** currentDate, eventsForCurrentDate, showDeleteParentDialog

---

### FOCUS MODE SCREEN
**File:** FocusModeScreen.kt
**Route:** "focus_mode/{goalId}" or "focus_mode?eventId={eventId}"
**Features:**
- Large timer display
- Work/Rest modes (Pomodoro-style)
- Timer adjustment (±1, ±5 min)
- Progress indicator
- Time bank accumulation
- Full-screen (no bottom nav)

**ViewModel:** FocusViewModel
**Key State:** focusUiState, timerSettingsState, goalUiState, eventUiState

---

## 3. REUSABLE COMPONENTS CATALOG

### NAVIGATION COMPONENTS
```
VoxPlanTopAppBar(title, canNavigateBack, actions)
  └─ Centered title + back icon + action buttons

BottomNavigationBar(navController, viewModel)
  └─ 4 items with selected/unselected icons

BreadcrumbNavigation(breadcrumbs, onMainClick, onBreadcrumbClick)
  └─ Horizontal scrolling breadcrumb trail
```

### GOAL/TASK COMPONENTS
```
GoalItem(goal, callbacks)
  ├─ Expandable card with goal title
  ├─ Sub-goal list (when expanded)
  └─ Action icons (edit, complete, delete, focus, add-to-daily)

GoalListContainer(goals, callbacks)
  └─ LazyColumn of GoalItems

IconDropDownMenu(goal, callbacks)
  └─ Edit | Complete | Delete actions menu
```

### INPUT COMPONENTS
```
TodoInputBar(onAddButtonClick)
  ├─ TextField for goal input
  └─ FAB button to add

QuotaSettingsSection(quota, days, callbacks)
  ├─ Minutes selector (±15 min increments)
  └─ Day selector (M-Su toggle)
```

### PROGRESS COMPONENTS
```
PowerBar(totalMinutes)
  ├─ POWER: label + bar/diamond display
  └─ Coin: remaining minutes

DayProgressCard(dayProgress)
  ├─ Emerald/Diamond icons
  ├─ Goal progress rows
  └─ Color-coded (today/past/future)

GoalProgressRow(goalProgress)
  ├─ Goal title
  ├─ Star rating (hours achieved)
  └─ Time display (Xh Ym)

WeeklySummary(weekTotal, completedDays)
  ├─ Emerald tracker (7 gems)
  └─ Goal totals with diamonds
```

### VISUAL COMPONENTS
```
Diamond(size)
  └─ Rotated square (4-hour achievement)

Gem() / EmeraldIcon()
  └─ Multi-sided gem (daily completion)

OneBar(fillAmount)
  └─ Vertical progress bar (60-min increments)
```

---

## 4. NAVIGATION ARGUMENT REFERENCE

| Screen | Arguments | Type | Notes |
|--------|-----------|------|-------|
| GoalEdit | goalId | Int | Required, converted from String |
| Daily | date | LocalDate | Required, parsed from String |
| Daily | newEventId | Int? | Optional, triggers duration dialog |
| DaySchedule | date | LocalDate | Required, parsed from String |
| FocusMode | goalId | Int | Via route path OR |
| FocusMode | eventId | Int | Via query parameter |

**Example Navigation Calls:**
```kotlin
// Go to edit screen
navController.navigate("${VoxPlanScreen.GoalEdit.route}/${goalId}")

// Go to daily with date
navController.navigate(VoxPlanScreen.Daily.createRouteWithDate(date))

// Go to daily with date and event
navController.navigate(VoxPlanScreen.Daily.createRouteWithDate(date, eventId))

// Go to focus from goal
navController.navigate(VoxPlanScreen.FocusMode.createRouteFromGoal(goalId))

// Go to focus from event
navController.navigate(VoxPlanScreen.FocusMode.createRouteFromEvent(eventId))
```

---

## 5. COLOR PALETTE

### Primary Colors
```
PrimaryColor:        #0F0A2C (Dark Purple)
PrimaryDarkColor:    #009688 (Teal)
PrimaryLightColor:   #82BD85 (Sage Green)
SecondaryColor:      #C7E2C9 (Light Green)
TertiaryColor:       #82babd (Muted Teal)
```

### UI Element Colors
```
TodoItemBackground:        #82BD85 (Green)
TodoItemIcon:              #0F0A2C (Purple)
SubGoalBackground:         #C7E2C9 (Light Green)
SubGoalBorder:             #B2DFDB (Lighter Green)

TopAppBarBg:               #82babd (Teal)
ToolbarColor:              #fff2df (Cream)
ToolbarBorder:             #ffddb0 (Tan)
TitlebarColor:             #ffddb0 (Tan)

EventBoxColor:             #41c300 (Bright Green)
ActivatedColor:            #B21720 (Red - for mode toggle)

FocusWork/Rest:            #000000 (Black)
FocusText:                 #FFFFFF (White)
```

---

## 6. DIMENSION CONSTANTS

```
SmallDp:                   4.dp    (minimal spacing)
MediumDp:                  8.dp    (standard spacing)
LargeDp:                   16.dp   (large spacing)

TodoItemHeight:            48.dp
TodoItemIconSize:          24.dp
SubGoalItemHeight:         44.dp
SubGoalItemIconSize:       22.dp

TodoInputBarHeight:        64.dp
TodoInputBarFabSize:       40.dp
OverlappingHeight:         64.dp

EventIconSize:             24.dp
FocusIconSize:             24.dp
```

---

## 7. TEXT STYLES

```
TodoItemTitle:
  fontSize: 17.sp
  fontWeight: Medium
  letterSpacing: 0.5.sp

TodoSubItemTitle:
  fontSize: 15.sp
  fontWeight: Medium
  letterSpacing: 0.5.sp

TodoInputBarText:
  fontSize: 18.sp
  fontWeight: Medium
  letterSpacing: 0.5.sp
  color: White
```

---

## 8. VIEWMODEL QUICK LOOKUP

```
MainViewModel
  ├─ mainUiState: StateFlow<MainUiState>
  ├─ todayTotalTime: StateFlow<Int>
  ├─ actionMode: State<ActionMode>
  └─ actionModeHandler: ActionModeHandler

GoalEditViewModel
  └─ goalUiState: State<GoalUiState>

ProgressViewModel
  └─ uiState: StateFlow<ProgressUiState>

DailyViewModel
  ├─ uiState: StateFlow<DailyUiState>
  ├─ actionMode: State<ActionMode>
  └─ actionModeHandler: ActionModeHandler

SchedulerViewModel
  ├─ currentDate: StateFlow<LocalDate>
  └─ eventsForCurrentDate: StateFlow<List<Event>>

FocusViewModel
  ├─ goalUiState: State<GoalWithSubGoals?>
  ├─ eventUiState: State<Event?>
  ├─ focusUiState: State<FocusUiState>
  └─ timerSettingsState: State<TimerSettingsState>

NavigationViewModel
  └─ selectedItemIndex: StateFlow<Int>

SharedViewModel
  └─ breadcrumbs: StateFlow<List<GoalWithSubGoals>>
```

---

## 9. ACTION MODE SYSTEM

**ActionMode Enum:**
```
Normal           - No reordering active
VerticalUp       - Move goal up in list
VerticalDown     - Move goal down in list
HierarchyUp      - Promote to parent level
HierarchyDown    - Demote to sub-goal level
```

**Visual Feedback:**
- Active mode: Red (ActivatedColor)
- Inactive mode: Dark Purple (PrimaryColor)

**Screens Using:**
- MainScreen (goal reordering)
- DailyScreen (task reordering)

---

## 10. FILE STRUCTURE SUMMARY

```
com.voxplanapp/
├── navigation/
│   ├── VoxPlanApp.kt          (Root + BottomNavBar)
│   ├── VoxPlanNavHost.kt       (NavHost graph)
│   ├── VoxPlanScreen.kt        (Screen definitions)
│   └── NavigationViewModel.kt
├── ui/
│   ├── main/
│   │   ├── MainScreen.kt
│   │   ├── MainViewModel.kt
│   │   ├── GoalItem.kt
│   │   ├── GoalListContainer.kt
│   │   ├── TodoInputBar.kt
│   │   ├── BreadCrumbNavigation.kt
│   │   └── QuickScheduleScreen.kt (stub)
│   ├── goals/
│   │   ├── GoalEditScreen.kt
│   │   ├── GoalEditViewModel.kt
│   │   ├── ProgressScreen.kt
│   │   ├── ProgressViewModel.kt
│   │   └── QuotaSettings.kt
│   ├── daily/
│   │   ├── DailyScreen.kt
│   │   └── DailyViewModel.kt
│   ├── calendar/
│   │   ├── DaySchedule.kt
│   │   └── SchedulerViewModel.kt
│   ├── focusmode/
│   │   ├── FocusModeScreen.kt
│   │   └── FocusViewModel.kt
│   ├── theme/
│   │   ├── Theme.kt
│   │   ├── Color.kt
│   │   └── Type.kt
│   └── constants/
│       ├── Colors.kt
│       ├── DpValues.kt
│       └── TextStyles.kt
└── shared/
    └── SharedViewModel.kt

res/
├── values/
│   ├── strings.xml
│   └── colors.xml
├── drawable/
│   ├── ic_add.xml
│   ├── ic_delete.xml
│   ├── ic_empty_check_box.xml
│   └── ic_selected_check_box.xml
├── raw/
│   ├── power_up.mp3
│   ├── chaching.mp3
│   └── countdown.mp3
└── mipmap-*/
    └── ic_launcher.webp
```

---

## 11. STATE HIERARCHY DIAGRAM

```
SharedViewModel (Breadcrumbs)
    │
    ├─→ MainViewModel
    │   ├─ Goals List + Breadcrumbs
    │   ├─ Daily power total
    │   └─ Action mode (reordering)
    │
    ├─→ GoalEditViewModel
    │   ├─ Goal details
    │   └─ Quota settings
    │
    ├─→ ProgressViewModel
    │   ├─ Week data
    │   └─ Daily progress
    │
    ├─→ DailyViewModel
    │   ├─ Daily tasks
    │   └─ Action mode
    │
    ├─→ SchedulerViewModel
    │   ├─ Current date
    │   └─ Events for date
    │
    ├─→ FocusViewModel
    │   ├─ Goal/Event data
    │   ├─ Focus state
    │   └─ Timer settings
    │
    └─→ NavigationViewModel
        └─ Selected nav tab
```

---

## 12. QUICK FEATURE CHECKLIST

### Fully Implemented
- [x] Goal creation and management
- [x] Hierarchical goals (up to 3 levels)
- [x] Goal expansion/collapse
- [x] Goal completion toggle
- [x] Goal deletion
- [x] Goal reordering (vertical + hierarchical)
- [x] Sub-goal navigation (breadcrumbs)
- [x] Quota settings (minutes + active days)
- [x] Time bank system
- [x] Power bar display
- [x] Weekly progress tracking
- [x] Day scheduler (hourly)
- [x] Scheduled events
- [x] Focus mode timer
- [x] Material 3 theming
- [x] Bottom navigation (4 screens)

### Beta/In Progress
- [ ] Daily screen (functional but may need polish)
- [ ] Parent/child event relationships
- [ ] Event duration on creation

### Not Implemented
- [ ] Settings screen
- [ ] Goal search/filter
- [ ] Goal templates
- [ ] Dark mode manual toggle (auto only)
- [ ] Notifications
- [ ] Goal sharing/collaboration

---

## 13. COMMON NAVIGATION PATTERNS

### Pattern 1: Goal Management Flow
```
MainScreen → Click Goal → GoalEditScreen → Edit Details → Save → Back
```

### Pattern 2: Add to Schedule
```
MainScreen → Click Goal → GoalEditScreen → Click "Schedule" → DaySchedule
```

### Pattern 3: Focus Session
```
MainScreen → Click Focus Icon → FocusModeScreen → Start Timer → navigateUp()
```

### Pattern 4: Progress Check
```
BottomNav "Progress" → ProgressScreen → View Weekly Stats → Navigate Week
```

### Pattern 5: Daily Management
```
BottomNav "Daily" → DailyScreen → Manage Tasks → Schedule to Calendar
```

---

## KEY TAKEAWAYS

1. **Screen Organization:** Feature-based structure (main, goals, daily, calendar, focusmode)
2. **Navigation:** Bottom nav with 4 main screens, navigation via sealed class routes
3. **State:** SharedViewModel for breadcrumbs, individual ViewModels per screen
4. **Composition:** Reusable components (GoalItem, PowerBar, Cards, etc.)
5. **Design:** Custom color palette with Material 3 support, consistent spacing
6. **Patterns:** Hierarchical display, action modes, state flows, SavedStateHandle args
7. **Status:** Production-ready for main features, beta for dailies

