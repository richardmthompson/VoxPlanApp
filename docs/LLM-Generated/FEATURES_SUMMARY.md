# VoxPlanApp Core Features Summary

## Document Overview

This documentation package contains three comprehensive documents:

1. **FEATURE_DOCUMENTATION.md** - Detailed technical reference
2. **FEATURE_DIAGRAMS.md** - Visual flows and relationships
3. **FEATURES_SUMMARY.md** - This overview document

---

## Quick Reference: File Locations

### Data Layer (Models & DAOs)

| Component | File |
|-----------|------|
| Event Entity | `/data/Event.kt` |
| Event DAO & Repository | `/data/EventDao.kt`, `/data/EventRepository.kt` |
| Quota Entity | `/data/QuotaEntity.kt` |
| Quota DAO & Repository | `/data/QuotaDao.kt`, `/data/QuotaRepository.kt` |
| TodoItem (Goal/Category) | `/data/TodoItem.kt` |
| TodoItem DAO & Repository | `/data/TodoDao.kt`, `/data/TodoRepository.kt` |
| TimeBank Entry | `/data/TimeBankEntry.kt` |
| Composite Goal Structure | `/data/GoalWithSubGoals.kt` |
| Database Configuration | `/data/AppDatabase.kt` |

### UI Layer - Daily Tasks

| Screen/Component | File |
|------------------|------|
| Daily Tasks Screen | `/ui/daily/DailyScreen.kt` |
| Daily ViewModel | `/ui/daily/DailyViewModel.kt` |

### UI Layer - Calendar/Scheduler

| Screen/Component | File |
|------------------|------|
| Day Schedule Screen | `/ui/calendar/DaySchedule.kt` |
| Scheduler ViewModel | `/ui/calendar/SchedulerViewModel.kt` |

### UI Layer - Goals & Progress

| Screen/Component | File |
|------------------|------|
| Goal Edit Screen | `/ui/goals/GoalEditScreen.kt` |
| Goal Edit ViewModel | `/ui/goals/GoalEditViewModel.kt` |
| Quota Settings Component | `/ui/goals/QuotaSettings.kt` |
| Progress Screen | `/ui/goals/ProgressScreen.kt` |
| Progress ViewModel | `/ui/goals/ProgressViewModel.kt` |

### UI Layer - Main Navigation

| Screen/Component | File |
|------------------|------|
| Main Screen | `/ui/main/MainScreen.kt` |
| Goal List Container | `/ui/main/GoalListContainer.kt` |
| Main ViewModel | `/ui/main/MainViewModel.kt` |

---

## Feature 1: Events

### What are Events?

Events are scheduled time blocks for activities. They come in two types:
- **Daily Events** (parents): Represent a goal/quota for a day without specific times
- **Scheduled Events** (children): Specific time slots (09:00-10:00) within a daily

### Key Properties

```kotlin
Event {
  id: Int (auto)
  goalId: Int → TodoItem
  title: String
  startDate: LocalDate
  startTime: LocalTime? (null for dailies)
  endTime: LocalTime? (null for dailies)
  parentDailyId: Int? (null for parents, has value for children)
  quotaDuration: Int? (minutes, from quota)
  scheduledDuration: Int? (sum of child event times)
  completedDuration: Int? (from TimeBank entries)
}
```

### Event Workflows

**Creating from Quota:**
1. User goes to Daily Screen
2. Clicks "Add Quotas" button
3. System queries active quotas for the day
4. Creates one Daily Event per active quota
5. User schedules daily into time slots via dialog
6. System creates Scheduled child events

**Creating from Goal:**
1. User edits goal
2. Sets preferred time (e.g., 09:00) and duration (e.g., 60 min)
3. Clicks "Schedule NOW!"
4. Selects a date
5. System creates parent Daily + child Scheduled Event
6. Opens Day Schedule view

**Deleting:**
- Deleting a Daily asks to delete all child events too (cascade)
- Deleting a Scheduled Event checks if parent has other children
- If no siblings, offers to delete parent as well

### Events in UI

- **Daily Screen**: Shows dailies (parent events) as task cards with progress boxes
- **Day Schedule**: Shows scheduled events (child events) as blocks on 24-hour timeline
- **Reordering**: Drag/drop in scheduler or up/down in daily list

---

## Feature 2: Quotas

### What are Quotas?

Quotas are time targets for goals. Each goal can have one quota specifying:
- How many minutes per day the goal should be worked on
- Which days of the week the quota is active

### Key Properties

```kotlin
Quota {
  id: Int (auto)
  goalId: Int → TodoItem (CASCADE delete)
  dailyMinutes: Int (e.g., 240 for 4 hours)
  activeDays: String (e.g., "1111100" for Mon-Fri)
}
```

### Active Days Encoding

7-character string, one per day Monday-Sunday:
- "1111100" = Monday through Friday (weekdays)
- "0000011" = Saturday and Sunday (weekends)
- "1111111" = Every day
- "0000000" = No days (empty quota, will be deleted on save)

### Quota Workflows

**Setting a Quota:**
1. User edits a goal in Goal Edit Screen
2. Sees QuotaSettingsSection
3. Sets minutes (15-min increments via +/- buttons)
4. Selects active days (toggle buttons M-S)
5. Can use quick presets: Weekdays, Weekends, Every Day
6. Clicks Save
7. System encodes activeDays and saves/updates Quota

**Activating Quotas:**
1. Daily Screen loads
2. User clicks "Add Quotas" button
3. System calls getAllActiveQuotas(today)
4. Checks activeDays string against today's day of week
5. Creates Daily Event for each matching quota
6. User can then schedule these into time slots

**Tracking Progress:**
- Daily boxes: Green (completed), Orange (scheduled), Gray (remaining)
- "2/4h" display shows completed vs quota
- Weekly view shows diamonds (1 per 4 hours) and emeralds (full day)

### Quotas in UI

- **Goal Edit Screen**: QuotaSettingsSection for configuration
- **Daily Screen**: Progress boxes under each task
- **Progress Screen**: Weekly diamonds/stars/emeralds

---

## Feature 3: Categories (Goal Hierarchy)

### What are Categories?

Categories are implemented as hierarchical goal structures using parent-child relationships in TodoItem. Each goal can:
- Be a top-level category (parentId = null)
- Have sub-goals (parentId = parent's id)
- Have sub-sub-goals, etc. (unlimited nesting)

### Key Properties

```kotlin
TodoItem {
  id: Int (auto)
  title: String
  parentId: Int? (null = top-level, has value = sub-goal)
  notes: String?
  preferredTime: LocalTime? (default start time for events)
  estDurationMins: Int? (default duration for events)
  frequency: RecurrenceType
  completedDate: LocalDate? (null = not complete)
  expanded: Boolean (expand/collapse state)
  order: Int (sequence within same level)
}

GoalWithSubGoals {
  goal: TodoItem
  subGoals: List<GoalWithSubGoals> // recursive
}
```

### Category Workflows

**Creating Categories:**
1. Main Screen shows top-level goals
2. Click "+" to add new goal
3. Enter title
4. Saved as top-level (parentId = null)

**Creating Sub-Categories:**
1. Click a goal to enter its sub-goal view
2. Click "+" to add sub-goal
3. New goal has parentId = current goal.id

**Navigation:**
- Main Screen shows all top-level goals
- Click a goal to view its children
- Breadcrumb shows path: [Home] > [Goal 1] > [Sub 1.1]
- Click breadcrumb to navigate back
- Expand/collapse sub-goals without entering view

**Reordering:**
1. Click goal to enter sub-goal view
2. Activate "Up" or "Down" mode
3. Click items to move up/down
4. Order field updated in database

**Moving Between Levels:**
- Move Left: Goal becomes child of current parent's parent
- Move Right: Goal becomes child of selected sibling

**Deleting:**
- Delete goal: Recursively deletes all descendants
- Shows confirmation dialog
- Orphaned quotas cleaned up automatically

### Categories in UI

- **Main Screen**: Expandable/collapsible hierarchy
- **Goal Edit Screen**: Shows parent, allows adding sub-goals
- **Sub-Goal Screen**: Full management of children
- **Breadcrumb Navigation**: Visual hierarchy path

---

## Feature Interactions

### Event → Quota → Category

```
Category/Goal
    ↓
Set Quota (4h daily, Mon-Fri)
    ↓
Daily Screen "Add Quotas"
    ↓
Creates Daily Event (quotaDuration = 240)
    ↓
User schedules into time slots
    ↓
Creates Scheduled child events (startTime/endTime)
    ↓
Updates parent Daily (scheduledDuration += duration)
    ↓
User completes event
    ↓
Records in TimeBank (goalId, date, duration)
    ↓
Daily shows completedDuration updated
    ↓
Progress Screen aggregates and displays achievement
```

### Cross-Feature Dependencies

1. **Events depend on Categories**: Each event has goalId pointing to a TodoItem
2. **Quotas depend on Categories**: Each quota has goalId pointing to a TodoItem
3. **Progress depends on all three**: 
   - Gets list of categories with quotas
   - Queries TimeBank for achievements
   - Calculates progress vs quotas
   - Displays visual rewards

---

## Key Business Rules

### Event Rules
- Dailies can have multiple scheduled child events
- Cascade deletion: deleting daily deletes all children
- duration = endTime - startTime (calculated in minutes)
- Events can overlap in time (no conflict checking)

### Quota Rules
- One quota per goal (UPSERT on save)
- Quota applies to all events for that goal
- Active days determined by checking string at day index
- Orphaned quotas (goal deleted) cleaned up on ProgressScreen load
- Minimum 15 minutes, increments of 15 min

### Category Rules
- Unlimited nesting depth
- Recursive deletion removes all descendants
- Parent-child relationship only via parentId
- Order field determines display sequence within level
- Top-level goals have no parent (parentId = null)

---

## Database Schema Overview

```
TodoItem ─────────────────┐
  ├─ id (PK)             │
  ├─ parentId (self-ref) │
  └─ [other fields]      │
                         │
         ┌───────────────┤
         │         (goalId FK)
         │               │
Event ◄──┤          Quota ◄──────┐
  ├─ id (PK)         ├─ id (PK) │
  ├─ goalId (FK)     ├─ goalId  │ (CASCADE)
  ├─ parentDailyId   └─ [fields]│
  │   (self-ref)                │
  └─ [other fields]             │
                          ┌─────┘
         ┌────────────────┘
         │ (goalId FK)
         │
    TimeBank
      ├─ id (PK)
      ├─ goalId (FK)
      └─ [other fields]
```

---

## Development Tips

### Adding a New Event Type
1. Extend Event entity with new fields
2. Update EventDao queries if needed
3. Update EventRepository
4. Modify DailyViewModel or SchedulerViewModel
5. Update UI screens to display new fields

### Adding a New Quota Frequency
Currently supports daily quotas. To add weekly/monthly:
1. Add frequency field to Quota
2. Update activation logic in QuotaRepository
3. Modify UI to select frequency in QuotaSettingsSection
4. Update ProgressViewModel aggregation logic

### Adding Category Features
1. Modify TodoItem with new fields
2. Update TodoDao queries if needed
3. Modify GoalWithSubGoals if structure changes
4. Update UI screens showing categories
5. Test recursive deletion

---

## Testing Checklist

- [ ] Create goal with quota
- [ ] Add quota tasks to daily
- [ ] Schedule daily into time slots
- [ ] Verify scheduledDuration updated
- [ ] Record completion in TimeBank
- [ ] Verify completedDuration updated
- [ ] Check progress screen calculation
- [ ] Delete daily (test cascade)
- [ ] Delete goal (test recursive)
- [ ] Verify orphaned quota cleanup
- [ ] Navigate category hierarchy
- [ ] Test all reorder operations
- [ ] Test quota activation for different days
- [ ] Verify active days encoding

---

## Version History

**v3.2** (Latest)
- Events with parent/child relationships implemented
- Quotas fully functional with daily tracking
- Categories as hierarchical goals
- Progress screen with weekly summaries
- Time banking system
- Dailies screen with scheduled boxes

**v3.1**
- Dailies screen introduced (beta)
- Release/debug bifurcation

**v3.0**
- Quotas and quota screen added

**v2.7**
- Power bar and time banking

**v2.5**
- Focus mode and focus timer

**Earlier**
- Basic goal hierarchy
- Event scheduling
- Calendar view

---

## Contact & Notes

- Database version: 13
- Min/Max time increments: 15 minutes
- Power bar fills: Every 60 minutes
- Diamonds awarded: Every 240 minutes (4 hours)
- Default start time for events: 9:00 AM
- Default event duration: 60 minutes

