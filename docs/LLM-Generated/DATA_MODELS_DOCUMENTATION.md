# VoxPlanApp - Complete Data Models & Entity Documentation

## Project Overview
VoxPlanApp is an Android task and time management application built with Kotlin, using Android Room database for persistence. The application supports hierarchical goals/tasks, time-based scheduling, quotas, and a focus mode with time banking.

**Database Version:** 13
**Database Name:** todo-db
**Framework:** Android Room ORM
**Type System:** Kotlin with LocalDate/LocalTime support

---

## TABLE OF CONTENTS
1. [Database Entities](#database-entities)
2. [Database Schema](#database-schema)
3. [Data Access Objects (DAOs)](#data-access-objects)
4. [Repositories](#repositories)
5. [Domain Models & Value Objects](#domain-models--value-objects)
6. [Enums & Constants](#enums--constants)
7. [Type Converters](#type-converters)
8. [Database Relationships](#database-relationships)
9. [Migration History](#migration-history)
10. [Data Access Patterns](#data-access-patterns)

---

## DATABASE ENTITIES

### 1. TodoItem Entity
**File:** `/Users/richardthompson/StudioProjects/VoxPlanApp/app/src/main/java/com/voxplanapp/data/TodoItem.kt`

**Purpose:** Represents goals/tasks in the application. Supports hierarchical structure with parent-child relationships.

**Entity Class Definition:**
```kotlin
@Entity
data class TodoItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    var title: String,
    var parentId: Int? = null,
    var order: Int = 0,
    var notes: String? = null,
    var isDone: Boolean = false,
    var preferredTime: LocalTime? = null,
    var estDurationMins: Int? = null,
    var frequency: RecurrenceType = RecurrenceType.NONE,
    var expanded: Boolean = true,
    var completedDate: LocalDate? = null
)
```

**Field Documentation:**

| Field | Type | Nullable | Default | Description |
|-------|------|----------|---------|-------------|
| id | Int | No | AutoIncrement | Primary key, auto-generated |
| title | String | No | N/A | Name/title of the goal/task |
| parentId | Int? | Yes | null | ID of parent task (for hierarchical structure) |
| order | Int | No | 0 | Display order within parent |
| notes | String? | Yes | null | Additional notes about the task |
| isDone | Boolean | No | false | **DEPRECATED** - Use completedDate instead |
| preferredTime | LocalTime? | Yes | null | Preferred time to work on this task |
| estDurationMins | Int? | Yes | null | Estimated duration in minutes |
| frequency | RecurrenceType | No | NONE | Recurrence pattern (NONE, DAILY, WEEKLY, MONTHLY, YEARLY) |
| expanded | Boolean | No | true | UI state: whether task details are expanded |
| completedDate | LocalDate? | Yes | null | Date when task was completed (null = incomplete) |

**Key Characteristics:**
- Supports hierarchical parent-child relationships
- Maintains ordering within hierarchical levels
- Uses `completedDate` to track completion status
- Supports frequency/recurrence patterns
- Can be expanded/collapsed in UI

---

### 2. Event Entity
**File:** `/Users/richardthompson/StudioProjects/VoxPlanApp/app/src/main/java/com/voxplanapp/data/Event.kt`

**Purpose:** Represents both Daily events and Scheduled time blocks. This dual-purpose entity distinguishes between them using `parentDailyId`.

**Entity Class Definition:**
```kotlin
@Entity
data class Event(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val goalId: Int,
    val title: String,
    val startTime: LocalTime? = null,
    val endTime: LocalTime? = null,
    val startDate: LocalDate,
    val recurrenceType: RecurrenceType = RecurrenceType.NONE,
    val recurrenceInterval: Int? = 0,
    val recurrenceEndDate: LocalDate? = null,
    val color: Int? = null,
    val order: Int = 0,
    val quotaDuration: Int? = null,
    val scheduledDuration: Int? = null,
    val completedDuration: Int? = null,
    val parentDailyId: Int? = null
)
```

**Field Documentation:**

| Field | Type | Nullable | Default | Description |
|-------|------|----------|---------|-------------|
| id | Int | No | AutoIncrement | Primary key, auto-generated |
| goalId | Int | No | N/A | Reference to TodoItem (goal) |
| title | String | No | N/A | Event title |
| startTime | LocalTime? | Yes | null | Start time of event (null for all-day events) |
| endTime | LocalTime? | Yes | null | End time of event (null for all-day events) |
| startDate | LocalDate | No | N/A | Date of the event |
| recurrenceType | RecurrenceType | No | NONE | Recurrence pattern |
| recurrenceInterval | Int? | Yes | 0 | Interval for recurrence (e.g., every 2 weeks) |
| recurrenceEndDate | LocalDate? | Yes | null | End date for recurrence |
| color | Int? | Yes | null | Display color for the event |
| order | Int | No | 0 | Display order for the day |
| quotaDuration | Int? | Yes | null | Duration from quota (minutes) |
| scheduledDuration | Int? | Yes | null | Duration calculated from scheduled times (minutes) |
| completedDuration | Int? | Yes | null | Actual duration completed from time bank (minutes) |
| parentDailyId | Int? | Yes | null | **Dual Purpose:** null = Daily, not null = Scheduled block under a Daily |

**Type Determination Logic:**
- `parentDailyId IS NULL` → **Daily Event** (parent-level recurring goal)
- `parentDailyId IS NOT NULL` → **Scheduled Block** (time-slot under a Daily)

**Key Characteristics:**
- Dual-purpose entity for both recurring dailies and scheduled time blocks
- Supports flexible time specification (all-day or timed)
- Tracks multiple duration metrics (quota, scheduled, completed)
- Parent-child relationship via `parentDailyId`

---

### 3. Quota Entity
**File:** `/Users/richardthompson/StudioProjects/VoxPlanApp/app/src/main/java/com/voxplanapp/data/QuotaEntity.kt`

**Purpose:** Represents daily time quotas for goals with active day tracking.

**Entity Class Definition:**
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
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val goalId: Int,
    val dailyMinutes: Int,
    val activeDays: String // "1111100" for Mon-Fri active days
)
```

**Field Documentation:**

| Field | Type | Nullable | Default | Description |
|-------|------|----------|---------|-------------|
| id | Int | No | AutoIncrement | Primary key, auto-generated |
| goalId | Int | No | N/A | Foreign key to TodoItem (CASCADE delete) |
| dailyMinutes | Int | No | N/A | Daily time quota in minutes |
| activeDays | String | No | N/A | 7-char string (0/1) for Mon-Sun activity |

**activeDays Format:**
```
Position: 0 1 2 3 4 5 6
Day:      M T W T F S S
Example:  1 1 1 1 1 0 0  (Mon-Fri active)
```

**Constraints:**
- Foreign key to TodoItem with CASCADE delete (deleting a TodoItem deletes its Quota)
- One quota per goal (unique constraint via primary key relationship)

**Key Characteristics:**
- Time-based quota system for goals
- Day-of-week activity pattern
- Cascading deletion ensures referential integrity

---

### 4. TimeBank Entity
**File:** `/Users/richardthompson/StudioProjects/VoxPlanApp/app/src/main/java/com/voxplanapp/data/TimeBankEntry.kt`

**Purpose:** Tracks time entries (completions) for goals, supporting time banking mechanics.

**Entity Class Definition:**
```kotlin
@Entity
data class TimeBank(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "goal_id") val goalId: Int,
    val date: LocalDate,
    val duration: Int
)
```

**Field Documentation:**

| Field | Type | Nullable | Default | Description |
|-------|------|----------|---------|-------------|
| id | Int | No | AutoIncrement | Primary key, auto-generated |
| goal_id | Int | No | N/A | Reference to goal/TodoItem |
| date | LocalDate | No | N/A | Date of time entry |
| duration | Int | No | N/A | Duration in minutes |

**Key Characteristics:**
- Records actual time spent on goals
- Supports aggregation by goal or by date
- Used for time banking and progress tracking
- No formal foreign key constraint (but references TodoItem IDs)

---

## DATABASE SCHEMA

### Complete Schema Definition (Version 13)

```sql
-- TodoItem Table
CREATE TABLE TodoItem (
    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    title TEXT NOT NULL,
    parentID INTEGER,
    order INTEGER NOT NULL DEFAULT 0,
    notes TEXT,
    isDone INTEGER NOT NULL DEFAULT 0,  -- BOOLEAN stored as INT
    preferredTime TEXT,                  -- LocalTime as String
    estDurationMins INTEGER,
    frequency TEXT NOT NULL DEFAULT 'NONE',
    expanded INTEGER NOT NULL DEFAULT 1,
    completedDate INTEGER               -- LocalDate as epoch days (Long)
);

-- Event Table (Version 13 - Latest)
CREATE TABLE Event (
    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    goalId INTEGER NOT NULL,
    title TEXT NOT NULL,
    startTime TEXT,                     -- LocalTime as String (nullable)
    endTime TEXT,                       -- LocalTime as String (nullable)
    startDate INTEGER NOT NULL,         -- LocalDate as epoch days
    recurrenceType TEXT NOT NULL,       -- Enum stored as String
    recurrenceInterval INTEGER,
    recurrenceEndDate INTEGER,          -- LocalDate as epoch days
    color INTEGER,
    `order` INTEGER NOT NULL DEFAULT 0,
    quotaDuration INTEGER,              -- minutes
    scheduledDuration INTEGER,          -- minutes
    completedDuration INTEGER,          -- minutes
    parentDailyId INTEGER               -- NULL for Dailies, set for Scheduled blocks
);

-- Quota Table
CREATE TABLE Quota (
    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    goalId INTEGER NOT NULL,
    dailyMinutes INTEGER NOT NULL,
    activeDays TEXT NOT NULL,
    FOREIGN KEY (goalId) REFERENCES TodoItem(id) ON DELETE CASCADE
);

-- TimeBank Table
CREATE TABLE TimeBank (
    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    goal_id INTEGER NOT NULL,
    date INTEGER NOT NULL,              -- LocalDate as epoch days
    duration INTEGER NOT NULL           -- minutes
);
```

### Type Conversions (Room TypeConverters)
**File:** `/Users/richardthompson/StudioProjects/VoxPlanApp/app/src/main/java/com/voxplanapp/data/Converters.kt`

```kotlin
class Converters {
    // LocalTime: String format (HH:mm:ss)
    @TypeConverter
    fun fromTimeString(value: String?): LocalTime? = value?.let { LocalTime.parse(it) }
    
    @TypeConverter
    fun timeToString(time: LocalTime?): String? = time?.toString()

    // LocalDate: Epoch days (Long)
    @TypeConverter
    fun fromDateLong(value: Long?): LocalDate? = value?.let { LocalDate.ofEpochDay(it) }
    
    @TypeConverter
    fun dateToLong(date: LocalDate?): Long? = date?.toEpochDay()
}
```

**Type Storage Mapping:**
| Kotlin Type | Database Type | Storage Format |
|------------|---------------|----------------|
| LocalTime | TEXT | HH:mm:ss (ISO 8601) |
| LocalDate | INTEGER | Epoch days (days since 1970-01-01) |
| Boolean (isActive) | INTEGER | 0 or 1 |
| RecurrenceType | TEXT | Enum name (NONE, DAILY, etc.) |
| Color | INTEGER | ARGB integer value |

---

## DATA ACCESS OBJECTS (DAOs)

### 1. TodoDao
**File:** `/Users/richardthompson/StudioProjects/VoxPlanApp/app/src/main/java/com/voxplanapp/data/TodoDao.kt`

**Purpose:** Data access interface for TodoItem entities.

**Primary Operations:**
```kotlin
@Dao
interface TodoDao {
    // Read Operations
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

    // Write Operations
    @Update
    suspend fun update(todo: TodoItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(todo: TodoItem)

    // Update Operations
    @Query("UPDATE TodoItem SET 'order' = :newOrder WHERE id = :id")
    suspend fun updateItemOrder(id: Int, newOrder: Int)

    @Query("UPDATE TodoItem SET 'expanded' = :expand WHERE id = :id")
    suspend fun expandItem(id: Int, expand: Boolean)

    // Delete Operations
    @Query("DELETE FROM TodoItem WHERE id = :id")
    suspend fun deleteById(id: Int)

    // Transaction Operations
    @Transaction
    suspend fun deleteItemAndDescendants(goalId: Int)

    @Transaction
    suspend fun updateItemsInTransaction(todos: List<TodoItem>)
}
```

**Key Features:**
- Supports hierarchical query operations
- Transaction support for cascading deletes
- Reactive streaming with Flow (non-blocking)
- Optimistic upsert (REPLACE conflict strategy)

---

### 2. EventDao
**File:** `/Users/richardthompson/StudioProjects/VoxPlanApp/app/src/main/java/com/voxplanapp/data/EventDao.kt`

**Purpose:** Data access interface for Event entities (Dailies and Scheduled blocks).

**Primary Operations:**
```kotlin
@Dao
interface EventDao {
    // Daily Queries
    @Query("""
        SELECT * FROM Event
        WHERE startDate = :date
        AND parentDailyId IS NULL
        ORDER BY `order`
    """)
    fun getDailiesForDate(date: LocalDate): Flow<List<Event>>

    // Scheduled Block Queries
    @Query("""
        SELECT * FROM Event
        WHERE startDate = :date
        AND parentDailyId IS NOT NULL
        ORDER BY startTime
    """)
    fun getScheduledBlocksForDate(date: LocalDate): Flow<List<Event>>

    @Query("SELECT * FROM Event WHERE parentDailyId = :dailyId")
    fun getScheduledBlocksForDaily(dailyId: Int): Flow<List<Event>>

    // General Queries
    @Query("SELECT * FROM Event")
    fun getAllEvents(): Flow<List<Event>>

    @Query("SELECT * FROM Event WHERE startDate = :date")
    fun getEventsForDate(date: LocalDate): Flow<List<Event>>

    @Query("SELECT * FROM Event WHERE id = :eventId")
    suspend fun getEvent(eventId: Int): Event

    @Query("SELECT * FROM Event WHERE parentDailyId = :parentId")
    fun getEventsWithParentId(parentId: Int): Flow<List<Event>>

    // Write Operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: Event): Long

    @Update
    suspend fun updateEvent(event: Event)

    // Delete Operations
    @Query("DELETE FROM Event WHERE id = :eventId")
    suspend fun deleteEvent(eventId: Int)

    // Order Management
    @Query("UPDATE Event SET `order` = :newOrder WHERE id = :id")
    suspend fun updateEventOrder(id: Int, newOrder: Int)
}
```

**Key Features:**
- Dual-query pattern for Dailies vs Scheduled blocks
- Date-based filtering
- Order preservation for UI display
- Parent-child relationship queries

---

### 3. QuotaDao
**File:** `/Users/richardthompson/StudioProjects/VoxPlanApp/app/src/main/java/com/voxplanapp/data/QuotaDao.kt`

**Purpose:** Data access interface for Quota entities.

**Primary Operations:**
```kotlin
@Dao
interface QuotaDao {
    @Query("SELECT * FROM Quota WHERE goalId = :goalId")
    fun getQuotaForGoal(goalId: Int): Flow<Quota?>

    @Query("SELECT * FROM Quota")
    fun getAllQuotas(): Flow<List<Quota>>

    @Query("SELECT * FROM Quota WHERE goalId IN (:goalIds)")
    fun getQuotasForGoals(goalIds: List<Int>): Flow<List<Quota>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuota(quota: Quota)

    @Update
    suspend fun updateQuota(quota: Quota)

    @Delete
    suspend fun deleteQuota(quota: Quota)

    @Query("DELETE FROM Quota WHERE goalId = :goalId")
    suspend fun deleteQuotaForGoal(goalId: Int)
}
```

**Key Features:**
- Goal-centric queries
- Bulk quota retrieval
- Cascade-compatible deletion

---

### 4. TimeBankDao
**File:** `/Users/richardthompson/StudioProjects/VoxPlanApp/app/src/main/java/com/voxplanapp/data/TimeBankEntry.kt`

**Purpose:** Data access interface for TimeBank entries.

**Primary Operations:**
```kotlin
@Dao
interface TimeBankDao {
    @Insert
    suspend fun insert(entry: TimeBank): Long

    @Query("SELECT * FROM TimeBank WHERE goal_id = :goalId")
    fun getEntriesForGoal(goalId: Int): Flow<List<TimeBank>>

    @Query("SELECT * FROM TimeBank WHERE date = :date")
    fun getEntriesForDate(date: LocalDate): Flow<List<TimeBank>>

    @Query("SELECT * FROM TimeBank WHERE date BETWEEN :startDate AND :endDate")
    fun getEntriesForDateRange(startDate: LocalDate, endDate: LocalDate): Flow<List<TimeBank>>

    @Query("SELECT SUM(duration) FROM TimeBank WHERE goal_id = :goalId")
    fun getTotalTimeForGoal(goalId: Int): Flow<Int?>

    @Query("SELECT SUM(duration) FROM TimeBank WHERE date = :date")
    fun getTotalTimeForDate(date: LocalDate): Flow<Int?>

    @Query("DELETE FROM TimeBank WHERE goal_id = :goalId AND duration = :bonusAmount")
    suspend fun deleteCompletionBonus(goalId: Int, bonusAmount: Int)
}
```

**Key Features:**
- Time aggregation queries
- Date range filtering
- Goal-based time tracking
- Bonus deletion capability

---

## REPOSITORIES

### 1. TodoRepository
**File:** `/Users/richardthompson/StudioProjects/VoxPlanApp/app/src/main/java/com/voxplanapp/data/TodoRepository.kt`

**Purpose:** Business logic layer for TodoItem operations.

**Key Methods:**
```kotlin
class TodoRepository(private val todoDao: TodoDao) {
    fun getItemStream(id: Int): Flow<TodoItem?>
    fun getAllTodos(): Flow<List<TodoItem>>
    fun getRootTodos(): List<TodoItem>
    fun getItemsByIds(ids: List<Int>): Flow<List<TodoItem>>
    fun getChildrenOf(parentId: Int): List<TodoItem>
    suspend fun expandItem(todoId: Int, expand: Boolean)
    suspend fun insert(todo: TodoItem)
    suspend fun completeItem(todoItem: TodoItem)  // Toggles completedDate
    suspend fun updateItemsInTransaction(items: List<TodoItem>)
    suspend fun deleteItemAndDescendents(todo: TodoItem)
    suspend fun updateItem(todo: TodoItem)
}
```

**Special Logic:**
- `completeItem()`: Toggles completion by setting/clearing `completedDate` to current date

---

### 2. EventRepository
**File:** `/Users/richardthompson/StudioProjects/VoxPlanApp/app/src/main/java/com/voxplanapp/data/EventRepository.kt`

**Purpose:** Business logic layer for Event operations.

**Key Methods:**
```kotlin
class EventRepository(private val eventDao: EventDao) {
    fun getDailiesForDate(date: LocalDate): Flow<List<Event>>
    fun getEventsForDate(date: LocalDate): Flow<List<Event>>
    fun getEventsWithParentId(parentId: Int): Flow<List<Event>>
    fun getScheduledBlocksForDate(date: LocalDate): Flow<List<Event>>
    fun getScheduledBlocksForDaily(dailyId: Int): Flow<List<Event>>
    suspend fun updateEventOrder(eventId: Int, newOrder: Int)
    suspend fun getEvent(eventId: Int): Event
    suspend fun insertEvent(event: Event): Int
    suspend fun updateEvent(event: Event)
    suspend fun deleteEvent(eventId: Int)
}
```

---

### 3. QuotaRepository
**File:** `/Users/richardthompson/StudioProjects/VoxPlanApp/app/src/main/java/com/voxplanapp/data/QuotaRepository.kt`

**Purpose:** Business logic layer for Quota operations with helper methods.

**Key Methods:**
```kotlin
class QuotaRepository(private val quotaDao: QuotaDao) {
    fun getQuotaForGoal(goalId: Int): Flow<Quota?>
    fun getAllQuotas(): Flow<List<Quota>>
    suspend fun insertQuota(quota: Quota)
    suspend fun updateQuota(quota: Quota)
    suspend fun deleteQuota(quota: Quota)
    suspend fun deleteQuotaForGoal(goalId: Int)
    fun getQuotasForGoals(goalIds: List<Int>): Flow<List<Quota>>
    
    // Helper methods
    fun isQuotaActiveForDate(quota: Quota, date: LocalDate): Boolean
    fun getActiveDays(quota: Quota): List<DayOfWeek>
    fun getAllActiveQuotas(date: LocalDate): Flow<List<Quota>>
}
```

**Helper Logic:**
- `isQuotaActiveForDate()`: Checks if quota's `activeDays` bit is set for given date
- `getActiveDays()`: Returns list of DayOfWeek objects from `activeDays` string

---

### 4. TimeBankRepository
**File:** `/Users/richardthompson/StudioProjects/VoxPlanApp/app/src/main/java/com/voxplanapp/data/TimeBankEntry.kt`

**Purpose:** Business logic layer for TimeBank operations.

**Key Methods:**
```kotlin
class TimeBankRepository(private val timeBankDao: TimeBankDao) {
    suspend fun addTimeBankEntry(goalId: Int, duration: Int)
    suspend fun deleteCompletionBonus(goalId: Int, bonusAmount: Int)
    fun getEntriesForGoal(goal: Int): Flow<List<TimeBank>>
    fun getEntriesForDate(date: LocalDate): Flow<List<TimeBank>>
    fun getEntriesForDateRange(startDate: LocalDate, endDate: LocalDate): Flow<List<TimeBank>>
    fun getTotalTimeForGoal(goalId: Int): Flow<Int?>
    fun getTotalTimeForDate(date: LocalDate): Flow<Int?>
}
```

**Special Logic:**
- `addTimeBankEntry()`: Automatically uses current date for new entries

---

## DOMAIN MODELS & VALUE OBJECTS

### 1. GoalWithSubGoals
**File:** `/Users/richardthompson/StudioProjects/VoxPlanApp/app/src/main/java/com/voxplanapp/data/GoalWithSubGoals.kt`

**Purpose:** Recursive data structure representing hierarchical goal/task structure.

```kotlin
data class GoalWithSubGoals(
    val goal: TodoItem,
    val subGoals: List<GoalWithSubGoals>
)
```

**Use Cases:**
- Tree representation of hierarchical tasks
- Recursive traversal of goal hierarchies
- UI rendering of nested task structures

---

### 2. ActionMode
**File:** `/Users/richardthompson/StudioProjects/VoxPlanApp/app/src/main/java/com/voxplanapp/model/ActionMode.kt`

**Purpose:** Enumeration of navigation/action states within the task editor.

```kotlin
sealed class ActionMode {
    object Normal : ActionMode()
    object VerticalUp : ActionMode()
    object VerticalDown : ActionMode()
    object HierarchyUp : ActionMode()
    object HierarchyDown : ActionMode()
}
```

**Mode Descriptions:**
- **Normal:** Standard editing mode
- **VerticalUp/Down:** Reordering within same hierarchy level
- **HierarchyUp/Down:** Moving between hierarchy levels

---

## ENUMS & CONSTANTS

### 1. RecurrenceType Enum
**Location:** `/Users/richardthompson/StudioProjects/VoxPlanApp/app/src/main/java/com/voxplanapp/data/Event.kt`

```kotlin
enum class RecurrenceType {
    NONE,       // No recurrence
    DAILY,      // Every day
    WEEKLY,     // Every week
    MONTHLY,    // Every month
    YEARLY      // Every year
}
```

**Used In:**
- `TodoItem.frequency` - Task recurrence pattern
- `Event.recurrenceType` - Event recurrence pattern

---

### 2. Focus Mode Enums
**Location:** `/Users/richardthompson/StudioProjects/VoxPlanApp/app/src/main/java/com/voxplanapp/ui/focusmode/FocusViewModel.kt`

**TimerState Enum:**
```kotlin
enum class TimerState {
    IDLE,       // Timer not running
    RUNNING,    // Timer actively counting
    PAUSED      // Timer paused
}
```

**DiscreteTaskState Enum:**
```kotlin
enum class DiscreteTaskState {
    IDLE,       // Waiting to start task
    COMPLETING, // Currently completing task
    COMPLETED   // Task completed
}
```

**DiscreteTaskLevel Enum:**
```kotlin
enum class DiscreteTaskLevel(
    val text: String,
    val description: String,
    val color: Color
) {
    EASY("EASY", "SMALL TASK", Color(0xFFFFEB3B)),           // Yellow
    CHALLENGE("CHALLENGE", "LARGE TASK", Color(0xFFFF9800)), // Orange
    DISCIPLINE("DISCIPLINE", "WORTHY GOAL", Color(0xFFF57C00)), // Dark Orange
    EPIC_WIN("EPIC WIN", "MAJOR ACCOMPLISHMENT", Color(0xFFE65100)) // Red Orange
}
```

**MedalType Enum:**
```kotlin
enum class MedalType {
    MINUTES,    // Achievement in minutes
    HOURS       // Achievement in hours
}
```

**ColorScheme Enum:**
```kotlin
enum class ColorScheme {
    WORK,       // Work/focus theme
    REST        // Rest/break theme
}
```

---

### 3. Data Layer Constants
**File:** `/Users/richardthompson/StudioProjects/VoxPlanApp/app/src/main/java/com/voxplanapp/data/Constants.kt`

```kotlin
// how many minutes to fill up a power bar on main screen?
const val FULLBAR_MINS = 60

// how many time-points do we accrue to complete an item?
const val pointsForItemCompletion = 15
```

---

## TYPE CONVERTERS

**File:** `/Users/richardthompson/StudioProjects/VoxPlanApp/app/src/main/java/com/voxplanapp/data/Converters.kt`

The application uses Room's TypeConverter system to bridge Kotlin's type-rich date/time classes with SQLite's limited type system:

```kotlin
@TypeConverters(Converters::class)
@Database(entities = [TodoItem::class, Event::class, TimeBank::class, Quota::class], version = 13)
abstract class AppDatabase : RoomDatabase() {
    // Database declarations...
}

class Converters {
    // LocalTime <-> String conversion
    @TypeConverter fun fromTimeString(value: String?): LocalTime?
    @TypeConverter fun timeToString(time: LocalTime?): String?
    
    // LocalDate <-> Long conversion (epoch days)
    @TypeConverter fun fromDateLong(value: Long?): LocalDate?
    @TypeConverter fun dateToLong(date: LocalDate?): Long?
}
```

---

## DATABASE RELATIONSHIPS

### Entity Relationship Diagram

```
TodoItem (root entity)
    ├── Quota (1:1 relationship)
    │   └── Foreign Key: goalId -> TodoItem.id
    │   └── Cascade Delete: ON DELETE CASCADE
    │
    ├── Event (1:many relationship)
    │   ├── goalId -> TodoItem.id (implicit, no FK constraint)
    │   └── DUAL PURPOSE based on parentDailyId:
    │       ├── parentDailyId IS NULL: Daily event
    │       └── parentDailyId IS NOT NULL: Scheduled block under Daily
    │
    ├── TimeBank (1:many relationship)
    │   └── goal_id -> TodoItem.id (implicit)
    │
    └── TodoItem (self-referential, parent-child)
        └── parentId -> TodoItem.id (implicit, no FK constraint)

Quota
    ├── Foreign Key: goalId -> TodoItem.id
    └── DELETE: Cascading

Event
    ├── Parent Relationship: goalId -> TodoItem.id
    └── Child Relationship: parentDailyId -> Event.id (self-reference)

TimeBank
    └── Reference: goal_id -> TodoItem.id
```

### Relationship Details

#### 1. TodoItem Parent-Child (Hierarchical)
- **Type:** Self-referential
- **Constraint:** None (implicit reference)
- **Cardinality:** 1:Many
- **Property:** `TodoItem.parentId` -> `TodoItem.id`
- **Example:** Task > SubTask > SubSubTask

#### 2. TodoItem -> Quota
- **Type:** One-to-One
- **Constraint:** Foreign Key with CASCADE delete
- **Cardinality:** 1:1
- **Property:** `Quota.goalId` -> `TodoItem.id`
- **Deletion:** Deleting TodoItem cascades to delete Quota

#### 3. TodoItem -> Event
- **Type:** One-to-Many
- **Constraint:** None (implicit reference)
- **Cardinality:** 1:Many
- **Property:** `Event.goalId` -> `TodoItem.id`
- **Usage:** Multiple events/dailies per goal

#### 4. Event -> Event (Daily -> Scheduled blocks)
- **Type:** Parent-Child relationship
- **Constraint:** None (implicit reference)
- **Cardinality:** 1:Many
- **Property:** `Event.parentDailyId` -> `Event.id`
- **Description:** 
  - Parent Daily: `parentDailyId = NULL`
  - Child Scheduled: `parentDailyId = parent_daily_id`

#### 5. TodoItem -> TimeBank
- **Type:** One-to-Many
- **Constraint:** None (implicit reference)
- **Cardinality:** 1:Many
- **Property:** `TimeBank.goal_id` -> `TodoItem.id`
- **Usage:** Multiple time entries per goal

---

## MIGRATION HISTORY

### Database Version Evolution

**Current Version:** 13

**Complete Migration Sequence:**

| Version | From | To | Migration Type | Changes |
|---------|------|----|----|---------|
| 1 → 2 | Initial | v2 | SCHEMA | [Initial schema created] |
| 2 → 3 | v2 | v3 | ADD COLUMN | TodoItem: Added `order` column |
| 3 → 4 | v3 | v4 | ADD COLUMN | TodoItem: Added `notes` column |
| 4 → 5 | v4 | v5 | NEW TABLE | Created Event table; TodoItem: Added preferredTime, estDurationMins, frequency |
| 5 → 6 | v5 | v6 | ADD COLUMN | TodoItem: Added `expanded` column |
| 6 → 7 | v6 | v7 | NEW TABLE | Created TimeBank table |
| 7 → 8 | v7 | v8 | RECREATE TABLE | TimeBank table structure |
| 8 → 9 | v8 | v9 | ADD COLUMN | TodoItem: Added `completedDate` column |
| 9 → 10 | v9 | v10 | NEW TABLE | Created Quota table with foreign key to TodoItem |
| 10 → 11 | v10 | v11 | SCHEMA CHANGE | Event: Made startTime and endTime nullable; Added scheduled and order columns; Removed other constraints |
| 11 → 12 | v11 | v12 | ADD COLUMNS | Event: Added quotaDuration, scheduledDuration, completedDuration |
| 12 → 13 | v12 | v13 | SCHEMA CHANGE | Event: Added parentDailyId (for Daily/Scheduled distinction); Removed scheduled column |

### Key Migration Details

#### Migration 5 → 6 (Event Introduction)
- Creates Event table for goal scheduling
- Adds time-preference properties to TodoItem

#### Migration 9 → 10 (Quota System)
- Introduces Quota entity with daily time limits
- Establishes foreign key relationship with TodoItem

#### Migration 10 → 11 (Event Schema Refinement)
- Makes Event start/end times optional (all-day events)
- Adds scheduled and order fields
- Complex table recreation with data migration

#### Migration 12 → 13 (Daily/Scheduled Distinction)
- Adds `parentDailyId` field to distinguish Daily events from Scheduled blocks
- Removes `scheduled` boolean column in favor of NULL check
- Enables parent-child relationship for daily breakdown into time slots

---

## DATABASE INITIALIZATION & CONFIGURATION

### Database Setup
**File:** `/Users/richardthompson/StudioProjects/VoxPlanApp/app/src/main/java/com/voxplanapp/data/AppContainer.kt`

```kotlin
@Database(
    entities = [TodoItem::class, Event::class, TimeBank::class, Quota::class],
    version = 13
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun todoDao(): TodoDao
    abstract fun eventDao(): EventDao
    abstract fun timeBankDao(): TimeBankDao
    abstract fun quotaDao(): QuotaDao
}
```

### Database Initialization
```kotlin
class AppDataContainer(private val context: Context) : AppContainer {
    override val database: AppDatabase by lazy {
        Room.databaseBuilder(context, AppDatabase::class.java, "todo-db")
            .addMigrations(
                AppDatabase.MIGRATION_2_3,
                AppDatabase.MIGRATION_3_4,
                AppDatabase.MIGRATION_4_5,
                AppDatabase.MIGRATION_5_6,
                AppDatabase.MIGRATION_6_7,
                AppDatabase.MIGRATION_7_8,
                AppDatabase.MIGRATION_8_9,
                AppDatabase.MIGRATION_9_10,
                AppDatabase.MIGRATION_10_11,
                AppDatabase.MIGRATION_11_12,
                AppDatabase.MIGRATION_12_13
            )
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    Log.d("Appdatabase", "database created")
                }

                override fun onOpen(db: SupportSQLiteDatabase) {
                    Log.d("Appdatabase", "database opened")
                }
            })
            .build()
    }

    // Repository initialization
    override val todoRepository: TodoRepository by lazy {
        TodoRepository(database.todoDao())
    }
    override val eventRepository: EventRepository by lazy {
        EventRepository(database.eventDao())
    }
    override val timeBankRepository: TimeBankRepository by lazy {
        TimeBankRepository(database.timeBankDao())
    }
    override val quotaRepository: QuotaRepository by lazy {
        QuotaRepository(database.quotaDao())
    }
}
```

---

## DATA ACCESS PATTERNS

### Pattern 1: Reactive Flow-Based Queries
Most queries return `Flow<T>` for reactive updates:

```kotlin
// From ViewModel
eventRepository.getDailiesForDate(selectedDate)
    .collectAsState(initial = emptyList())
    .value
    .forEach { daily -> /* render daily */ }
```

**Benefits:**
- Non-blocking UI updates
- Automatic recomposition on data changes
- Coroutine-based backpressure handling

### Pattern 2: Suspend Functions for One-Time Reads
Single-value queries use suspend functions:

```kotlin
// From ViewModel scope
val event = eventRepository.getEvent(eventId) // suspend function
updateUiState(event)
```

### Pattern 3: Transaction Support
Complex multi-step operations use @Transaction:

```kotlin
@Transaction
suspend fun deleteItemAndDescendants(goalId: Int) {
    val children = getChildrenOf(goalId)
    for (child in children) {
        deleteItemAndDescendants(child.id)  // Recursive
    }
    deleteById(goalId)
}
```

### Pattern 4: Cascading Deletes
Foreign key constraints enforce referential integrity:

```kotlin
// Deleting TodoItem automatically deletes associated Quota
// Due to: ForeignKey.CASCADE on Quota.goalId
quotaDao.deleteQuota(quota)  // Not needed; FK handles it
```

### Pattern 5: Aggregation Queries
TimeBank queries support aggregation:

```kotlin
// Get total time spent on a goal
val totalMinutes = timeBankRepository.getTotalTimeForGoal(goalId)
    .collectAsState(initial = 0).value
```

### Pattern 6: Date-Based Filtering
Events filtered by date for calendar views:

```kotlin
// Get all events for a specific date
eventRepository.getEventsForDate(selectedDate)
    .collectAsState(initial = emptyList()).value

// Get dailies vs scheduled blocks
eventRepository.getDailiesForDate(date)
eventRepository.getScheduledBlocksForDate(date)
```

---

## SUMMARY

### Entity Count: 4 Primary Entities
1. **TodoItem** - Goals/tasks with hierarchy
2. **Event** - Daily events and scheduled time blocks
3. **Quota** - Daily time quotas
4. **TimeBank** - Time entry tracking

### DAO Count: 4 Data Access Objects
1. TodoDao
2. EventDao
3. QuotaDao
4. TimeBankDao

### Repository Count: 4 Repository Classes
1. TodoRepository
2. EventRepository
3. QuotaRepository
4. TimeBankRepository

### Enum Types: 6
1. RecurrenceType
2. TimerState
3. DiscreteTaskState
4. DiscreteTaskLevel
5. MedalType
6. ColorScheme

### Total Tables: 4
1. TodoItem
2. Event
3. Quota
4. TimeBank

### Relationships: 5
1. TodoItem self-referential (parent-child)
2. TodoItem ↔ Quota (1:1, cascading)
3. TodoItem ↔ Event (1:many)
4. Event self-referential (Daily → Scheduled)
5. TodoItem ↔ TimeBank (1:many)

### Database Version: 13
### Migration Count: 11 (versions 2→13)
### Type Converters: 2 (LocalTime, LocalDate)

---

## FILE REFERENCE GUIDE

| Entity/Component | File Path |
|-----------------|-----------|
| TodoItem | `/Users/richardthompson/StudioProjects/VoxPlanApp/app/src/main/java/com/voxplanapp/data/TodoItem.kt` |
| Event | `/Users/richardthompson/StudioProjects/VoxPlanApp/app/src/main/java/com/voxplanapp/data/Event.kt` |
| Quota | `/Users/richardthompson/StudioProjects/VoxPlanApp/app/src/main/java/com/voxplanapp/data/QuotaEntity.kt` |
| TimeBank | `/Users/richardthompson/StudioProjects/VoxPlanApp/app/src/main/java/com/voxplanapp/data/TimeBankEntry.kt` |
| TodoDao | `/Users/richardthompson/StudioProjects/VoxPlanApp/app/src/main/java/com/voxplanapp/data/TodoDao.kt` |
| EventDao | `/Users/richardthompson/StudioProjects/VoxPlanApp/app/src/main/java/com/voxplanapp/data/EventDao.kt` |
| QuotaDao | `/Users/richardthompson/StudioProjects/VoxPlanApp/app/src/main/java/com/voxplanapp/data/QuotaDao.kt` |
| TimeBankDao | `/Users/richardthompson/StudioProjects/VoxPlanApp/app/src/main/java/com/voxplanapp/data/TimeBankEntry.kt` |
| TodoRepository | `/Users/richardthompson/StudioProjects/VoxPlanApp/app/src/main/java/com/voxplanapp/data/TodoRepository.kt` |
| EventRepository | `/Users/richardthompson/StudioProjects/VoxPlanApp/app/src/main/java/com/voxplanapp/data/EventRepository.kt` |
| QuotaRepository | `/Users/richardthompson/StudioProjects/VoxPlanApp/app/src/main/java/com/voxplanapp/data/QuotaRepository.kt` |
| TimeBankRepository | `/Users/richardthompson/StudioProjects/VoxPlanApp/app/src/main/java/com/voxplanapp/data/TimeBankEntry.kt` |
| Converters | `/Users/richardthompson/StudioProjects/VoxPlanApp/app/src/main/java/com/voxplanapp/data/Converters.kt` |
| AppDatabase | `/Users/richardthompson/StudioProjects/VoxPlanApp/app/src/main/java/com/voxplanapp/data/AppDatabase.kt` |
| AppContainer | `/Users/richardthompson/StudioProjects/VoxPlanApp/app/src/main/java/com/voxplanapp/data/AppContainer.kt` |
| Constants | `/Users/richardthompson/StudioProjects/VoxPlanApp/app/src/main/java/com/voxplanapp/data/Constants.kt` |
| ActionMode | `/Users/richardthompson/StudioProjects/VoxPlanApp/app/src/main/java/com/voxplanapp/model/ActionMode.kt` |
| GoalWithSubGoals | `/Users/richardthompson/StudioProjects/VoxPlanApp/app/src/main/java/com/voxplanapp/data/GoalWithSubGoals.kt` |
| Focus Enums | `/Users/richardthompson/StudioProjects/VoxPlanApp/app/src/main/java/com/voxplanapp/ui/focusmode/FocusViewModel.kt` |

