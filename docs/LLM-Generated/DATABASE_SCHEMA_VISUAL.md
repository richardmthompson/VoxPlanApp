# VoxPlanApp Database Schema - Visual Reference

## ER Diagram (Text Format)

```
┌─────────────────────────────────────────────────────────────┐
│                      DATABASE SCHEMA v13                     │
└─────────────────────────────────────────────────────────────┘

┌──────────────────────────┐
│       TodoItem           │  Root Entity: Goals/Tasks
├──────────────────────────┤
│ PK  id: Int              │
│     title: String        │
│ FK  parentId: Int?       │ ◄──┐ (self-ref for hierarchy)
│     order: Int           │    │
│     notes: String?       │    └────┬─────────────────┐
│     isDone: Boolean      │         │ (hierarchy)     │
│     preferredTime: Time? │         │                 │
│     estDurationMins: Int?│         │                 │
│     frequency: Enum      │         │                 │
│     completedDate: Date? │         │                 │
│     expanded: Boolean    │         │                 │
└──────────────────────────┘         │                 │
    │                                 │                 │
    │ 1:1 (CASCADE)                   │                 │
    ├────────────┬───────────────────┼─────────────────┤
    │            │                   │
    │            │                   │ 1:Many
    │            │                   │
    │            │        ┌──────────────────────────┐
    │            │        │    TodoItem (parent)     │
    │            │        │    with parentId = null  │
    │            │        └──────────────────────────┘
    │            │
    │            │
    │     ┌──────▼──────────┐
    │     │     Quota       │  Quota System
    │     ├─────────────────┤
    │     │ PK id: Int      │
    │     │ FK goalId: Int  │ ──► TodoItem.id (CASCADE)
    │     │    dailyMinutes │
    │     │    activeDays   │ (String "1111100")
    │     └─────────────────┘
    │
    ├─────────────────┐
    │ 1:Many          │ 1:Many
    │                 │
    │         ┌───────┴──────────────┐
    │         │                      │
    │  ┌──────▼──────────────┐  ┌────▼─────────────┐
    │  │      Event         │  │   TimeBank       │
    │  ├────────────────────┤  ├──────────────────┤
    │  │ PK id: Int         │  │ PK id: Int       │
    │  │ FK goalId: Int     │  │    goal_id: Int  │ ──► TodoItem.id
    │  │    title: String   │  │    date: Date    │
    │  │    startDate: Date │  │    duration: Int │
    │  │    startTime: Time?│  └──────────────────┘
    │  │    endTime: Time?  │     (time entries)
    │  │    recurrenceType  │
    │  │    recurrenceInt   │
    │  │    recurrenceEnd   │
    │  │    color: Int?     │
    │  │    order: Int      │
    │  │    quotaDuration   │
    │  │ scheduledDuration  │
    │  │ completedDuration  │
    │  │ FK parentDailyId? ◄├─┐ (self-ref)
    │  └────────────────────┘  │
    │      │                    │
    │      └────────────────────┘
    │    (Daily ─► Scheduled)
    │
    └────────────────────► Event.goalId

Legend:
  PK   = Primary Key
  FK   = Foreign Key
  ──►  = Foreign Key Reference
  ◄──  = Self Reference
  1:1  = One-to-One Relationship
  1:Many = One-to-Many Relationship
```

---

## Table Structure Details

### TodoItem Table
```
Column Name         │ Type      │ Nullable │ Default    │ Notes
────────────────────┼───────────┼──────────┼────────────┼─────────────────
id                  │ INTEGER   │ NO       │ AUTOINCR   │ Primary Key
title               │ TEXT      │ NO       │            │ Task name
parentID            │ INTEGER   │ YES      │ null       │ Self-reference
order               │ INTEGER   │ NO       │ 0          │ Display order
notes               │ TEXT      │ YES      │ null       │ Description
isDone              │ INTEGER   │ NO       │ 0          │ DEPRECATED
preferredTime       │ TEXT      │ YES      │ null       │ HH:mm:ss
estDurationMins     │ INTEGER   │ YES      │ null       │ Minutes
frequency           │ TEXT      │ NO       │ 'NONE'     │ RecurrenceType
expanded            │ INTEGER   │ NO       │ 1          │ Boolean UI state
completedDate       │ INTEGER   │ YES      │ null       │ Epoch days
```

### Event Table
```
Column Name         │ Type      │ Nullable │ Default    │ Notes
────────────────────┼───────────┼──────────┼────────────┼─────────────────
id                  │ INTEGER   │ NO       │ AUTOINCR   │ Primary Key
goalId              │ INTEGER   │ NO       │            │ TodoItem reference
title               │ TEXT      │ NO       │            │ Event name
startTime           │ TEXT      │ YES      │ null       │ HH:mm:ss or null
endTime             │ TEXT      │ YES      │ null       │ HH:mm:ss or null
startDate           │ INTEGER   │ NO       │            │ Epoch days
recurrenceType      │ TEXT      │ NO       │            │ RecurrenceType
recurrenceInterval  │ INTEGER   │ YES      │            │ n-day/week/etc
recurrenceEndDate   │ INTEGER   │ YES      │ null       │ Epoch days
color               │ INTEGER   │ YES      │ null       │ ARGB color
order               │ INTEGER   │ NO       │ 0          │ Display order
quotaDuration       │ INTEGER   │ YES      │ null       │ Minutes
scheduledDuration   │ INTEGER   │ YES      │ null       │ Minutes
completedDuration   │ INTEGER   │ YES      │ null       │ Minutes
parentDailyId       │ INTEGER   │ YES      │ null       │ NULL=Daily, >0=Scheduled
```

### Quota Table
```
Column Name         │ Type      │ Nullable │ Default    │ Notes
────────────────────┼───────────┼──────────┼────────────┼─────────────────
id                  │ INTEGER   │ NO       │ AUTOINCR   │ Primary Key
goalId              │ INTEGER   │ NO       │            │ FK→TodoItem(id)
dailyMinutes        │ INTEGER   │ NO       │            │ Time quota
activeDays          │ TEXT      │ NO       │            │ "1111100" pattern
                    │           │          │            │ FK: CASCADE
```

### TimeBank Table
```
Column Name         │ Type      │ Nullable │ Default    │ Notes
────────────────────┼───────────┼──────────┼────────────┼─────────────────
id                  │ INTEGER   │ NO       │ AUTOINCR   │ Primary Key
goal_id             │ INTEGER   │ NO       │            │ TodoItem reference
date                │ INTEGER   │ NO       │            │ Epoch days
duration            │ INTEGER   │ NO       │            │ Minutes
```

---

## Relationship Cardinality Matrix

```
                TodoItem    Event       Quota       TimeBank
                ────────    ─────       ─────       ────────
TodoItem        1:Many*     1:Many      1:1         1:Many
                (parent)    (goalId)    (FK)        (goal_id)

Event           Many:1      1:Many**    —           —
                (goalId)    (parent)

Quota           1:1         —           —           —
                (FK)

TimeBank        Many:1      —           —           —
                (goal_id)

Legend:
  * Self-referential: parentId → id
  ** Self-referential: parentDailyId → id
  FK = Cascading Foreign Key
```

---

## Data Type Conversions

```
Kotlin Type     ──TypeConverter──►  SQLite Type  ──Storage──►  Format
─────────────────────────────────────────────────────────────────────
LocalTime       ──fromTimeString     TEXT                        HH:mm:ss
                  timeToString──►

LocalDate       ──fromDateLong       INTEGER      ──Epoch Days─► Long
                  dateToLong──►       (days)

Boolean         ─────────────►       INTEGER                     0 or 1

Enum            ──toString()─►       TEXT                        Name
                  valueOf()──►

Int (Color)     ─────────────►       INTEGER                     0xAARRGGBB
```

**Converter File:** `data/Converters.kt`

---

## Inheritance Hierarchy

```
Parent-Child Relationships in VoxPlanApp:

TodoItem (Goal)
├── Quota (1:1 cascade)
│   └── constraints: goalId FOREIGN KEY → TodoItem.id ON DELETE CASCADE
│
├── Event (1:many)
│   └── reference: goalId points to TodoItem.id
│
├── TimeBank (1:many)
│   └── reference: goal_id points to TodoItem.id
│
└── TodoItem (self-referential)
    └── hierarchy: parentId points to TodoItem.id


Event (Dual Purpose)
├── Daily (when parentDailyId IS NULL)
│   └── represents top-level recurring goal
│
└── Scheduled Block (when parentDailyId IS NOT NULL)
    └── child time slot under a Daily
    └── reference: parentDailyId points to Event.id
```

---

## Migration Path v12 → v13

```
MIGRATION 12 → 13: Dual-Purpose Event

Before (v12):                          After (v13):
─────────────                          ───────────
Event table                            Event table
├─ scheduled: BOOLEAN                  ├─ parentDailyId: INTEGER?
└─ (other fields)                      └─ (scheduled removed)

Query Logic:                           Query Logic:
WHERE scheduled = 0 → Daily            WHERE parentDailyId IS NULL → Daily
WHERE scheduled = 1 → Scheduled        WHERE parentDailyId IS NOT NULL → Scheduled

SQL Migration:
1. Add parentDailyId column to Event
2. Create new Event_new table (without scheduled)
3. Copy data: INSERT INTO Event_new SELECT ... (exclude scheduled)
4. Drop old Event table
5. Rename Event_new to Event
```

---

## Query Patterns

### Hierarchical Queries (TodoItem)

```sql
-- Get root tasks only
SELECT * FROM TodoItem WHERE parentID IS NULL

-- Get all children of a task
SELECT * FROM TodoItem WHERE parentID = :parentId

-- Get all descendants (requires recursive CTE or app logic)
-- VoxPlanApp uses recursive app-level calls
```

### Temporal Queries (Event)

```sql
-- Get all dailies for a date
SELECT * FROM Event
WHERE startDate = :date AND parentDailyId IS NULL
ORDER BY order

-- Get scheduled blocks for a date
SELECT * FROM Event
WHERE startDate = :date AND parentDailyId IS NOT NULL
ORDER BY startTime

-- Get scheduled blocks under a daily
SELECT * FROM Event
WHERE parentDailyId = :dailyId
```

### Aggregation Queries (TimeBank)

```sql
-- Total time on a goal
SELECT SUM(duration) FROM TimeBank WHERE goal_id = :goalId

-- Total time for a date
SELECT SUM(duration) FROM TimeBank WHERE date = :date

-- Time range
SELECT * FROM TimeBank
WHERE date BETWEEN :startDate AND :endDate
```

### Activity Queries (Quota)

```sql
-- Get quota for goal
SELECT * FROM Quota WHERE goalId = :goalId

-- Get all active quotas for a date
SELECT * FROM Quota
-- App logic: filter by activeDays bit for day of week
```

---

## Indexes & Performance

**Current Indexes:**
- Primary Keys on all tables (auto-indexed)
- Foreign Key on Quota.goalId (auto-indexed)
- No explicit secondary indexes defined

**Recommended Indexes (for optimization):**
```sql
-- For temporal queries
CREATE INDEX idx_event_startDate ON Event(startDate);
CREATE INDEX idx_event_parentDailyId ON Event(parentDailyId);
CREATE INDEX idx_timebank_date ON TimeBank(date);
CREATE INDEX idx_timebank_goal_id ON TimeBank(goal_id);

-- For hierarchical queries
CREATE INDEX idx_todoitem_parentID ON TodoItem(parentID);
```

---

## Constraints Summary

```
Entity      │ Constraint Type         │ Details
────────────┼─────────────────────────┼────────────────────────────
TodoItem    │ PRIMARY KEY             │ id (auto-increment)
            │ NONE                    │ (parentId self-ref not enforced)

Event       │ PRIMARY KEY             │ id (auto-increment)
            │ NONE                    │ (goalId, parentDailyId not enforced)

Quota       │ PRIMARY KEY             │ id (auto-increment)
            │ FOREIGN KEY (CASCADE)   │ goalId → TodoItem.id

TimeBank    │ PRIMARY KEY             │ id (auto-increment)
            │ NONE                    │ (goal_id not enforced)
```

---

## View Definition (Conceptual)

```sql
-- Views could be created for common queries:

-- Active Goals with Quotas
CREATE VIEW active_goals AS
SELECT t.id, t.title, q.dailyMinutes, q.activeDays
FROM TodoItem t
LEFT JOIN Quota q ON t.id = q.goalId
WHERE t.completedDate IS NULL;

-- Dailies for Today
CREATE VIEW dailies_today AS
SELECT * FROM Event
WHERE startDate = DATE('now')
AND parentDailyId IS NULL
ORDER BY order;

-- Time Bank Summary by Goal
CREATE VIEW timebank_summary AS
SELECT goal_id, COUNT(*) as entries, SUM(duration) as total_minutes
FROM TimeBank
GROUP BY goal_id;
```

---

## Data Relationships Flowchart

```
User creates TodoItem
    ↓
TodoItem can have Quota
    ↓
TodoItem can have Event (Daily)
    ├→ Event (Daily) can have Event (Scheduled)
    │
    └→ Quota defines dailyMinutes & activeDays
        │
        └→ Used to generate Dailies schedule

When user completes time:
    ↓
TimeBank entry is created
    ├→ Linked to goal_id (TodoItem)
    ├→ Records duration spent
    └→ References date of activity

Event tracks:
    ├→ quotaDuration (from Quota)
    ├→ scheduledDuration (from start/end times)
    └→ completedDuration (from TimeBank)
```

---

End of Database Schema Visual Reference
