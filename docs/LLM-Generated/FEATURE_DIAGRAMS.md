# VoxPlanApp - Feature Diagrams & Flow Charts

## 1. EVENT LIFECYCLE FLOW

### 1.1 Event Creation Paths

```
PATH 1: From Goal Scheduling
┌──────────────────────┐
│ Goal Edit Screen     │
│ - Set time/duration  │
│ - Click "Schedule"   │
└──────────────┬───────┘
               │
        ┌──────▼──────┐
        │ Select Date │
        └──────┬──────┘
               │
        ┌──────▼───────────┐
        │ Create Daily     │
        │ parentDailyId=   │
        │ startTime=null   │
        └──────┬───────────┘
               │
        ┌──────▼──────────────┐
        │ Create Scheduled    │
        │ parentDailyId=daily │
        │ startTime/endTime   │
        └──────┬──────────────┘
               │
        ┌──────▼─────────────┐
        │ Day Schedule View  │
        └────────────────────┘


PATH 2: From Quota Activation
┌──────────────────────┐
│ Daily Screen         │
│ Click "Add Quotas"   │
└──────────────┬───────┘
               │
        ┌──────▼────────────────────┐
        │ Query Active Quotas for   │
        │ Today (check activeDays)  │
        └──────┬────────────────────┘
               │
        ┌──────▼──────────────┐
        │ For Each Quota:     │
        │ Create Daily Event  │
        │ quotaDuration=quota │
        └──────┬──────────────┘
               │
        ┌──────▼──────────────┐
        │ Display in Dailies  │
        │ List                │
        └──────┬──────────────┘
               │
        ┌──────▼────────────────┐
        │ User Schedules via    │
        │ TimeSelectionDialog   │
        └──────┬────────────────┘
               │
        ┌──────▼──────────────────────┐
        │ Create Scheduled Child Event │
        │ Set startTime/endTime        │
        └──────┬──────────────────────┘
               │
        ┌──────▼─────────────────┐
        │ Update Parent Daily    │
        │ scheduledDuration += d │
        └────────────────────────┘
```

### 1.2 Event Deletion Flow

```
User clicks delete on Daily Event
│
├─ Get all child events (parentDailyId = this.id)
│
├─ Children exist?
│  ├─ YES: Show confirmation dialog
│  │        "Delete with children?" Y/N
│  │
│  └─ NO: Just delete this event
│
└─ ON CONFIRM:
   ├─ Delete all children first
   └─ Delete parent
```

### 1.3 Event Duration Tracking

```
DAILY EVENT DURATION STATE MACHINE

┌─────────────────────────────────────────────────────┐
│            DAILY EVENT (Parent)                      │
│                                                     │
│  quotaDuration ────────────────────────────────→   │
│      (from Quota.dailyMinutes)                     │
│                                                     │
│  scheduledDuration ────────────────────────────→   │
│      (sum of child event startTime-endTime)        │
│                                                     │
│  completedDuration ────────────────────────────→   │
│      (from TimeBank entries for this goal/date)    │
│                                                     │
│              DAILY: quotaDuration = 240min
│              SCHEDULED: child 1 (60min) + child 2 (180min) = 240min
│              COMPLETED: TimeBank entry = 60min
│
│              VISUAL: ■■□ (2/4 hours green/orange/gray)
└─────────────────────────────────────────────────────┘
```

## 2. QUOTA LIFECYCLE FLOW

### 2.1 Quota Creation & Configuration

```
┌──────────────────────────┐
│ Goal Edit Screen         │
│ User clicks on goal      │
└──────────────┬───────────┘
               │
        ┌──────▼────────────────────┐
        │ Load Quota from Database  │
        │ (if exists)               │
        └──────┬────────────────────┘
               │
        ┌──────▼───────────────────────┐
        │ Display QuotaSettingsSection  │
        │ - Minutes selector (+/- buttons)
        │ - Day selector (M T W T F S S)
        │ - Quick presets              │
        └──────┬───────────────────────┘
               │
        ┌──────▼────────────────────┐
        │ User modifies quota state │
        │ - quotaMinutes            │
        │ - activeDays (Set)        │
        └──────┬────────────────────┘
               │
        ┌──────▼────────────────────┐
        │ Click Save                │
        └──────┬────────────────────┘
               │
        ┌──────▼────────────────────────┐
        │ If activeDays empty:          │
        │   Delete quota from DB        │
        │ Else:                         │
        │   Encode activeDays→String    │
        │   "1111100" = Mon-Fri         │
        │   UPSERT quota to DB          │
        └───────────────────────────────┘
```

### 2.2 Active Days Encoding

```
MONDAY (DayOfWeek.MONDAY = 1)
│
activeDays.mapIndexedNotNull { index, char →
  if (char == '1') DayOfWeek.of(index + 1) else null
}

EXAMPLE STRINGS:
"1111100" → [MON, TUE, WED, THU, FRI]      (Weekdays)
"0000011" → [SAT, SUN]                      (Weekends)
"1111111" → [MON, TUE, WED, THU, FRI, SAT, SUN] (Every day)
```

### 2.3 Quota Activation Check

```
┌─ getAllActiveQuotas(date: LocalDate)
│
├─ dayOfWeek = date.dayOfWeek.value - 1  // Convert to 0-6
│
├─ Query all quotas
│
└─ Filter:
   For each quota:
     quota.activeDays[dayOfWeek] == '1' ?
     ├─ YES: Include in result
     └─ NO:  Skip
```

### 2.4 Quota Progress Calculation

```
DAILY TASK PROGRESS (DailyScreen)

quotaDuration ────┐
                  │
scheduledDuration─┼──→ Display N boxes
                  │    where N = quotaDuration / 60 (if ≥1h)
completedDuration │       or = 1 (if <1h)
                  │
                  └──→ Color each box:
                       GREEN = completed ≥ per-box quota
                       ORANGE = scheduled ≥ per-box quota
                       GRAY = remaining


WEEKLY PROGRESS (ProgressScreen)

For each day in week:
  ├─ achievements = TimeBank entries for that day
  ├─ quota = Quota.dailyMinutes
  ├─ quota_met = achievements ≥ quota
  ├─ diamonds = achievements / 240 minutes
  └─ isComplete = all daily quotas met

For week:
  ├─ diamonds = sum of daily diamonds
  ├─ stars = sum of (remaining hours after diamonds)
  ├─ emeralds = count of days with all quotas met
  └─ completedDays = count(isComplete)
```

## 3. CATEGORY/HIERARCHY FLOW

### 3.1 Goal Hierarchy Structure

```
TREE STRUCTURE (Self-Referential via parentId)

Top-Level (parentId = null)
├── Goal 1
│   ├── Sub 1.1
│   ├── Sub 1.2
│   └── Sub 1.3
├── Goal 2
│   ├── Sub 2.1
│   └── Sub 2.2
└── Goal 3

DATABASE REPRESENTATION:
ID  Title           ParentId  Order
1   Goal 1          null      0
2   Sub 1.1         1         0
3   Sub 1.2         1         1
4   Sub 1.3         1         2
5   Goal 2          null      1
6   Sub 2.1         5         0
7   Sub 2.2         5         1
8   Goal 3          null      2
```

### 3.2 Goal Navigation Breadcrumb

```
Main Screen
  │
  ├─ Display Root Goals
  │  └─ [Goal 1] [Goal 2] [Goal 3]
  │
  ├─ User clicks Goal 1
  │  │
  │  ├─ Load GoalWithSubGoals recursively
  │  │  └─ Goal 1
  │  │     ├─ Sub 1.1
  │  │     ├─ Sub 1.2
  │  │     └─ Sub 1.3
  │  │
  │  └─ Display breadcrumb: [Home] > [Goal 1]
  │     └─ Show sub-goals
  │
  └─ User clicks Sub 1.2
     │
     ├─ Load GoalWithSubGoals(3)
     │  └─ Sub 1.2
     │     └─ (no children)
     │
     └─ Display breadcrumb: [Home] > [Goal 1] > [Sub 1.2]
        └─ Can edit, add children, delete, etc.
```

### 3.3 Recursive Deletion

```
deleteItemAndDescendants(goalId: Int)

1. Get children (parentId = goalId)
2. For each child:
     deleteItemAndDescendants(child.id)  [Recursive]
3. Delete the goal itself

EXAMPLE:
Goal 1 (id=1)
├── Sub 1.1 (id=2)
├── Sub 1.2 (id=3)
└── Sub 1.3 (id=4)

delete(1):
  ├─ delete(2): no children, delete 2
  ├─ delete(3): no children, delete 3
  ├─ delete(4): no children, delete 4
  └─ delete(1)

RESULT: All 4 deleted from database
```

## 4. DATA FLOW DIAGRAMS

### 4.1 Complete Event-Quota-Progress Flow

```
CREATION PHASE
┌──────────────┐
│ Create Goal  │ (TodoItem)
└──────┬───────┘
       │
┌──────▼──────────────┐
│ Set Quota           │ (Quota: dailyMinutes, activeDays)
│ - 4 hours/day       │
│ - Mon-Fri active    │
└──────┬──────────────┘
       │
ACTIVATION PHASE
       │
┌──────▼────────────────────────────┐
│ User views Daily Screen on Monday  │
│ Clicks "Add Quotas"                │
└──────┬────────────────────────────┘
       │
┌──────▼──────────────────────┐
│ Create Daily Event           │
│ - goalId = goal.id          │
│ - quotaDuration = 240 min   │
│ - startDate = Monday        │
│ - parentDailyId = null      │
└──────┬──────────────────────┘
       │
SCHEDULING PHASE
       │
┌──────▼─────────────────────────┐
│ User schedules into time slots  │
│ - 09:00-10:00 (60 min)         │
│ - 14:00-17:00 (180 min)        │
└──────┬─────────────────────────┘
       │
┌──────▼────────────────────────────┐
│ Create 2 Scheduled Events          │
│ - parentDailyId = daily.id        │
│ - startTime/endTime set           │
│ - quotaDuration = actual duration │
└──────┬────────────────────────────┘
       │
┌──────▼──────────────────────┐
│ Update Daily                │
│ scheduledDuration = 240 min │
└──────┬──────────────────────┘
       │
EXECUTION PHASE
       │
┌──────▼──────────────────────┐
│ User focuses on first event  │
│ 09:00-10:00                  │
│ Completes 60 minutes         │
└──────┬──────────────────────┘
       │
┌──────▼──────────────────────┐
│ Record in TimeBank           │
│ goalId = goal.id            │
│ date = Monday               │
│ duration = 60               │
└──────┬──────────────────────┘
       │
┌──────▼──────────────────────┐
│ Update Daily                │
│ completedDuration = 60 min  │
└──────┬──────────────────────┘
       │
REPORTING PHASE
       │
┌──────▼──────────────────────────────┐
│ ProgressScreen queries:             │
│ 1. All active quotas for week      │
│ 2. All goals referenced in quotas  │
│ 3. TimeBank entries for week       │
└──────┬──────────────────────────────┘
       │
┌──────▼─────────────────────────────┐
│ Calculate progress:                │
│ - Monday: 60/240 min = 1/4 hours   │
│ - Diamonds: 60/240 = 0 (need 240)  │
│ - Day complete: NO (need 240/240)  │
└──────┬─────────────────────────────┘
       │
┌──────▼──────────────────────┐
│ Display:                    │
│ Quota: 4h                  │
│ Achieved: ■□□□ (1/4)       │
│ Status: In Progress        │
└──────────────────────────────┘
```

### 4.2 Category-Event Relationship Flow

```
Goal Hierarchy                Event Structure
─────────────────            ────────────────

Learn Guitar (id=1)
├── Chords (id=2)    ─────→  Event goalId=2
│   └── Event 1              Chord Practice
│       Mon 15:00-16:00      [Event 1 details]
│
└── Songs (id=3)     ─────→  Event goalId=3
    └── Event 2              Song Practice
        Thu 14:00-15:30      [Event 2 details]

TimeBank Aggregation:
Chord Practice: 60 mins → TimeBank(goalId=2, 60)
Song Practice: 90 mins → TimeBank(goalId=3, 90)

Progress Aggregation:
  Chords subtotal: 60 mins toward Chords quota
  Songs subtotal: 90 mins toward Songs quota
  Learn Guitar total: 150 mins (aggregate from all sub-goals)
```

## 5. STATE MACHINE DIAGRAMS

### 5.1 Daily Event State Transitions

```
           ┌─────────────────┐
           │   Created       │
           │ (no times set)  │
           └────────┬────────┘
                    │
        ┌───────────▼──────────────┐
        │   With Children Scheduled │
        │ (startTime/endTime set)   │
        └───────────┬──────────────┘
                    │
        ┌───────────▼───────────────┐
        │  Some/All Events Completed │
        │ (completedDuration > 0)    │
        └───────────┬────────────────┘
                    │
        ┌───────────▼──────────────┐
        │  Quota Achieved          │
        │ (completedDuration       │
        │  >= quotaDuration)       │
        └───────────┬──────────────┘
                    │
        ┌───────────▼────────────┐
        │  Diamond/Reward Earned │
        └────────────────────────┘
```

### 5.2 Event Deletion State Flow

```
                 ┌─ DELETE EVENT
                 │
         ┌───────▼──────────┐
         │ Check Parent ID  │
         └───────┬──────────┘
                 │
    ┌────────────┴────────────┐
    │                         │
Has parentDailyId?         No parentDailyId?
    │                         │
    YES                       NO
    │                         │
    ├─ It's a          ├─ It's a Daily
    │  Scheduled       │
    │  Event           │
    │                  │
    ├─ Check           ├─ Check if
    │  siblings        │  has child
    │                  │  events
    │                  │
    ├─ Siblings?       ├─ Children?
    │  ├─ YES: Just   │  │
    │  │  delete this │  ├─ YES:
    │  │             │  │  Show confirm
    │  └─ NO:        │  │  dialog
    │    Ask delete  │  │
    │    parent too  │  └─ NO:
    │               │    Just delete
    └───────────────┴───→ [DELETED]
```

## 6. DATABASE RELATIONSHIP DIAGRAM

```
┌─────────────────────────────────────┐
│          TodoItem (Goal/Category)    │
├─────────────────────────────────────┤
│ PK: id                              │
│ title: String                       │
│ parentId: Int? FK→TodoItem.id       │ [Self-ref]
│ notes: String?                      │
│ preferredTime: LocalTime?           │
│ estDurationMins: Int?               │
│ frequency: RecurrenceType           │
│ completed: LocalDate?               │
│ order: Int                          │
└────┬─────────────────────────────────┘
     │
     │ 1:N
     │ (goalId)
     │
┌────▼─────────────────────────────────┐
│         Event (Daily/Scheduled)      │
├─────────────────────────────────────┤
│ PK: id                              │
│ goalId: Int FK→TodoItem.id          │
│ title: String                       │
│ startDate: LocalDate                │
│ startTime: LocalTime?               │
│ endTime: LocalTime?                 │
│ parentDailyId: Int? FK→Event.id     │ [Self-ref]
│ quotaDuration: Int?                 │
│ scheduledDuration: Int?             │
│ completedDuration: Int?             │
│ recurrenceType: RecurrenceType      │
│ order: Int                          │
└────┬──────────────┬──────────────────┘
     │              │
     │ 1:N          │
     │ (goalId)     │ 1:N (goalId)
     │              │
┌────▼──────────────▼──────────────────┐
│       Quota (Time Target)            │
├─────────────────────────────────────┤
│ PK: id                              │
│ goalId: Int FK→TodoItem.id (CASCADE)│
│ dailyMinutes: Int                   │
│ activeDays: String (7 chars)        │
└────────────────────────────────────┘


┌──────────────────────────────────────┐
│       TimeBank (Completed Time)      │
├──────────────────────────────────────┤
│ PK: id                              │
│ goalId: Int FK→TodoItem.id          │
│ date: LocalDate                     │
│ duration: Int (minutes)             │
└──────────────────────────────────────┘
```

## 7. UI NAVIGATION FLOW

```
Main Screen (Category List)
│
├─ [+] Add Goal
│  └─ Create Top-Level Goal
│
├─ [Goal Item] ─ Click to Enter Sub-Goal View
│  │
│  ├─ SubGoal Screen
│  │  ├─ [+] Add Sub-Goal
│  │  ├─ [Up][Down][Left] Reorder
│  │  └─ [Sub-Item] ─ Click to Edit
│  │
│  └─ Goal Edit Screen
│     ├─ Title/Notes/Duration
│     ├─ QuotaSettingsSection
│     ├─ [Focus NOW!] → FocusMode
│     ├─ [Schedule NOW!] → Date Select → Day Schedule
│     └─ [Save]
│
└─ Bottom Nav Bar
   ├─ [Progress] → ProgressScreen (Weekly Summary)
   ├─ [Daily] → DailyScreen (Today's Tasks)
   ├─ [Schedule] → DaySchedule (Hour View)
   └─ [Focus] → FocusMode
```

## 8. Quota Progress Visual Indicators

### Daily Screen Progress

```
Task: Coding (4h quota)
┌────────────────────────────────────┐
│ Coding                         [🗑] │
│ ■■■○ 3/4h                         │
│ Scheduled: 3:00 (3:00-17:00)       │
│ Completed: 2:00                    │
└────────────────────────────────────┘

Visual Breakdown:
■ = Green box (completed >= 1h each)
□ = Orange box (scheduled >= 1h each)
○ = Gray box (remaining quota)

Sub-hour example (30m quota):
┌────────────────────────────────────┐
│ Meditation                     [🗑] │
│ ■ 30/30m                          │
│ Scheduled: Yes (6:00-6:30)         │
│ Completed: Yes                     │
└────────────────────────────────────┘
```

### Weekly Progress Screen

```
        MON    TUE    WED    THU    FRI    SAT    SUN
Goal1   ◆ ◆ ⭐ ◆ ◆ ⭐  ◆     ◆      -      -      -
        4h     5h     4h     4h     4h     0h     0h

Goal2   ◆      ◆      ◆ ⭐   -      -      -      -
        4h     4h     5h     0h     0h     0h     0h

Legend:
◆ = Diamond (4 hours completed)
⭐ = Star (remaining hour after diamonds)
🔷 = Emerald (all day quotas completed)
- = No quota for this day
```

