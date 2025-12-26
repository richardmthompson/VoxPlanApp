# VoxPlanApp Data Models - Quick Reference

## Entity Summary Table

| Entity | Purpose | Key Fields | Relationships |
|--------|---------|-----------|-----------------|
| **TodoItem** | Goals/Tasks | id, title, parentId, completedDate, frequency | Parent: Quota, Event, TimeBank; Self: parentId |
| **Event** | Dailies & Scheduled Blocks | id, goalId, title, startDate, parentDailyId | Parent: TodoItem; Self: parentDailyId |
| **Quota** | Daily Time Quotas | id, goalId, dailyMinutes, activeDays | Parent: TodoItem (CASCADE) |
| **TimeBank** | Time Entries | id, goal_id, date, duration | Parent: TodoItem |

---

## Entity Schemas at a Glance

### TodoItem (Goals/Tasks)
```
PK: id (auto-increment)
- title: String (required)
- parentId: Int? (hierarchy)
- order: Int (display order)
- notes: String?
- frequency: RecurrenceType (NONE|DAILY|WEEKLY|MONTHLY|YEARLY)
- preferredTime: LocalTime?
- estDurationMins: Int?
- completedDate: LocalDate? (null = incomplete)
- isDone: Boolean (DEPRECATED)
- expanded: Boolean (UI state)
```

### Event (Dailies & Scheduled Time Blocks)
```
PK: id (auto-increment)
FK: goalId -> TodoItem.id
- title: String
- startDate: LocalDate
- startTime: LocalTime? (null = all-day)
- endTime: LocalTime? (null = all-day)
- recurrenceType: RecurrenceType
- recurrenceInterval: Int?
- recurrenceEndDate: LocalDate?
- color: Int? (display color)
- order: Int (display order)
- quotaDuration: Int? (minutes)
- scheduledDuration: Int? (minutes)
- completedDuration: Int? (minutes)
- parentDailyId: Int? (NULL=Daily, NOT_NULL=Scheduled)
```

### Quota (Daily Time Quotas)
```
PK: id (auto-increment)
FK: goalId -> TodoItem.id (CASCADE DELETE)
- dailyMinutes: Int (quota in minutes)
- activeDays: String (7 chars: MTWTFSS, e.g., "1111100")
```

### TimeBank (Time Entry Tracking)
```
PK: id (auto-increment)
- goal_id: Int (reference to TodoItem)
- date: LocalDate
- duration: Int (minutes)
```

---

## Dual-Purpose Event Design

The Event entity serves two distinct purposes distinguished by the `parentDailyId` field:

### Daily Events (parentDailyId = NULL)
- Top-level recurring events linked to a TodoItem goal
- Represent recurring daily/weekly/monthly/yearly goals
- Have recurrence pattern information
- Examples: "Exercise", "Write", "Read"

### Scheduled Time Blocks (parentDailyId ≠ NULL)
- Child events scheduled under a Daily event
- Represent specific time slots allocated to complete the Daily
- Have specific start/end times
- Example: "Exercise" (Daily) → ["6:00-6:30 Run", "6:30-7:00 Stretch"] (Scheduled)

---

## Type Storage Conversions

| Kotlin Type | SQL Type | Storage Format |
|------------|----------|----------------|
| LocalTime | TEXT | HH:mm:ss (ISO 8601) |
| LocalDate | INTEGER | Epoch days (since 1970-01-01) |
| Boolean | INTEGER | 0 or 1 |
| Enum | TEXT | Enum name (e.g., "DAILY") |
| Color | INTEGER | ARGB int (0xAARRGGBB) |

**TypeConverter Location:** `data/Converters.kt`

---

## Enums

### RecurrenceType
```
NONE, DAILY, WEEKLY, MONTHLY, YEARLY
```
Used in: TodoItem.frequency, Event.recurrenceType

### Focus Mode Enums (in FocusViewModel)
```
TimerState: IDLE, RUNNING, PAUSED
DiscreteTaskState: IDLE, COMPLETING, COMPLETED
DiscreteTaskLevel: EASY, CHALLENGE, DISCIPLINE, EPIC_WIN
MedalType: MINUTES, HOURS
ColorScheme: WORK, REST
```

---

## Data Access Pattern Summary

| Layer | Class | Pattern |
|-------|-------|---------|
| **Entity** | TodoItem, Event, Quota, TimeBank | @Entity, @PrimaryKey |
| **DAO** | TodoDao, EventDao, QuotaDao, TimeBankDao | @Dao, @Query, @Insert, @Update, @Delete |
| **Repository** | TodoRepository, EventRepository, QuotaRepository, TimeBankRepository | Business logic, Flow wrappers |
| **ViewModel** | Uses repositories | Collects Flow as State |

---

## Common Queries

### Get Daily Events for Date
```kotlin
eventRepository.getDailiesForDate(date)
  // Returns: Flow<List<Event>> where parentDailyId IS NULL
```

### Get Scheduled Blocks for a Daily
```kotlin
eventRepository.getScheduledBlocksForDaily(dailyId)
  // Returns: Flow<List<Event>> where parentDailyId = dailyId
```

### Get Task Hierarchy
```kotlin
todoRepository.getChildrenOf(parentId)
  // Returns: List<TodoItem> with matching parentId
```

### Check Quota Active for Today
```kotlin
quotaRepository.isQuotaActiveForDate(quota, LocalDate.now())
  // Returns: Boolean based on activeDays bit
```

### Get Total Time Spent on Goal
```kotlin
timeBankRepository.getTotalTimeForGoal(goalId)
  // Returns: Flow<Int?> sum of all durations for goal
```

### Complete a Task (Toggle)
```kotlin
todoRepository.completeItem(todoItem)
  // Sets completedDate to today if null, or clears it if set
```

---

## Database Version History

Current: **v13**

| From | To | Type | Description |
|------|----|----|---|
| 2→3 | ADD | TodoItem.order |
| 3→4 | ADD | TodoItem.notes |
| 4→5 | NEW | Event table |
| 5→6 | ADD | TodoItem.expanded |
| 6→7 | NEW | TimeBank table |
| 7→8 | RECREATE | TimeBank |
| 8→9 | ADD | TodoItem.completedDate |
| 9→10 | NEW | Quota table |
| 10→11 | SCHEMA | Event times nullable |
| 11→12 | ADD | Event duration fields |
| 12→13 | SCHEMA | Event.parentDailyId (Daily/Scheduled) |

---

## File Locations

### Entities (4 files)
- `data/TodoItem.kt`
- `data/Event.kt`
- `data/QuotaEntity.kt`
- `data/TimeBankEntry.kt`

### DAOs (4 files)
- `data/TodoDao.kt`
- `data/EventDao.kt`
- `data/QuotaDao.kt`
- `data/TimeBankEntry.kt` (contains TimeBankDao)

### Repositories (4 files)
- `data/TodoRepository.kt`
- `data/EventRepository.kt`
- `data/QuotaRepository.kt`
- `data/TimeBankEntry.kt` (contains TimeBankRepository)

### Database & Config (3 files)
- `data/AppDatabase.kt` (database definition + migrations)
- `data/AppContainer.kt` (dependency injection)
- `data/Converters.kt` (type converters)

### Enums & Models (3 files)
- `data/Event.kt` (RecurrenceType enum)
- `ui/focusmode/FocusViewModel.kt` (TimerState, DiscreteTaskState, etc.)
- `model/ActionMode.kt` (ActionMode sealed class)

### Constants
- `data/Constants.kt`

---

## Key Architectural Patterns

### 1. Reactive Architecture
- All queries return Flow<T> for reactive UI updates
- Repository wraps DAO with Flow
- ViewModel collects Flow as Compose State

### 2. Dependency Injection
- AppDataContainer creates database and repositories
- Passed through UI layer to ViewModels
- Lazy initialization with `by lazy` blocks

### 3. Type Converters
- LocalDate/LocalTime to SQLite compatible types
- Automatic conversion on read/write
- Registered with @TypeConverters annotation

### 4. Cascading Deletes
- Quota uses foreign key with CASCADE
- Deleting TodoItem automatically deletes Quota
- Other references rely on DAO logic

### 5. Self-Referential Hierarchy
- TodoItem.parentId → TodoItem.id
- Recursive queries via getChildrenOf()
- Transaction support for cascading deletes

### 6. Dual-Purpose Entity
- Event serves as both Daily and Scheduled
- Discriminated by parentDailyId (NULL vs NOT NULL)
- Optimizes schema vs. having separate tables

---

## Constants

```kotlin
const val FULLBAR_MINS = 60              // Power bar fill amount
const val pointsForItemCompletion = 15   // Points for completing task
```

---

## Relationship Summary

```
TodoItem (root)
├─ Quota (1:1, CASCADE)
├─ Event (1:many, goalId reference)
├─ TimeBank (1:many, goal_id reference)
└─ TodoItem (self, parentId reference - hierarchy)

Event (dual-purpose)
├─ TodoItem (goalId reference)
└─ Event (self, parentDailyId reference - Daily→Scheduled)
```

---

End of Quick Reference
