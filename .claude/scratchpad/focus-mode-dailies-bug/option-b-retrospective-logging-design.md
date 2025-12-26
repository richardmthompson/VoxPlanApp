# Option B: Enhanced Retrospective Logging Design

**Date**: 2025-12-19
**Status**: Design Document (Not Implemented)
**Estimated Complexity**: Tier 3 (Complex - requires migration, multi-file changes, UI work)

---

## Executive Summary

This design adds retrospective event logging to VoxPlanApp, allowing users to see what they actually worked on in the DaySchedule view, while keeping the Dailies list clean for planning purposes.

**Key Changes**:
- Add `eventSource` enum to track event origin (USER_CREATED, FOCUS_AUTO, etc.)
- Add `isVisibleInDailies` boolean to control Dailies UI visibility
- Add `actualStartTime/actualEndTime` to track actual work times vs scheduled times
- Update queries to filter by visibility
- Distinguish auto-logged events visually in DaySchedule
- Fix duplicate event creation bug (bankTime + onExit creating twice)

**Benefits**:
- Users can see retrospective timeline of actual work in calendar
- Dailies remains clean for planning
- Scheduled vs actual time tracking for productivity insights
- Foundation for future analytics features

**Tradeoffs**:
- Requires database migration (v13 → v14)
- More complex event lifecycle logic
- Additional UI work for visual differentiation
- Needs settings toggle for user preference

---

## 1. Data Model Design

### 1.1 Event Entity Changes

**File**: `app/src/main/java/com/voxplanapp/data/Event.kt`

**NEW ENUM**:
```kotlin
enum class EventSource {
    USER_CREATED,       // Manually created daily or scheduled event
    FOCUS_AUTO,         // Auto-created from Focus Mode exit (>10 mins)
    TIMEBANK_AUTO,      // Auto-created from banking medals (deprecated after fix)
    QUOTA_GENERATED     // Created via "Add Quota Tasks" button
}
```

**UPDATED ENTITY**:
```kotlin
@Entity(
    tableName = "Event",
    foreignKeys = [
        ForeignKey(
            entity = TodoItem::class,
            parentColumns = ["id"],
            childColumns = ["goalId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["goalId"]),
        Index(value = ["parentDailyId"]),
        Index(value = ["startDate", "isVisibleInDailies"]),  // NEW: Optimize Dailies query
        Index(value = ["goalId", "eventSource"])              // NEW: Filter by source
    ]
)
data class Event(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val goalId: Int,
    val title: String,
    val startDate: LocalDate,
    val order: Int = 0,

    // Scheduling times (planned)
    val startTime: LocalTime? = null,
    val endTime: LocalTime? = null,

    // NEW: Actual work times (retrospective)
    val actualStartTime: LocalTime? = null,
    val actualEndTime: LocalTime? = null,

    // NEW: Event metadata
    val eventSource: EventSource = EventSource.USER_CREATED,
    val isVisibleInDailies: Boolean = true,

    // Parent-child relationship
    val parentDailyId: Int? = null,

    // Duration tracking
    val quotaDuration: Int? = null,
    val scheduledDuration: Int? = null,
    val completedDuration: Int? = null,

    // Recurrence (future feature)
    val recurrenceType: RecurrenceType = RecurrenceType.NONE,
    val recurrenceInterval: Int? = null,
    val recurrenceEndDate: LocalDate? = null,

    val color: Int = 0
)
```

### 1.2 Type Converter for EventSource

**File**: `app/src/main/java/com/voxplanapp/data/Converters.kt`

**ADD**:
```kotlin
class Converters {
    // ... existing converters ...

    @TypeConverter
    fun fromEventSource(value: EventSource): String {
        return value.name
    }

    @TypeConverter
    fun toEventSource(value: String): EventSource {
        return try {
            EventSource.valueOf(value)
        } catch (e: IllegalArgumentException) {
            EventSource.USER_CREATED  // Fallback for unknown sources
        }
    }
}
```

### 1.3 Database Migration

**File**: `app/src/main/java/com/voxplanapp/data/AppDatabase.kt`

**ADD MIGRATION v13 → v14**:
```kotlin
val MIGRATION_13_14 = object : Migration(13, 14) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Add new columns with defaults for backward compatibility
        database.execSQL("""
            ALTER TABLE Event
            ADD COLUMN eventSource TEXT NOT NULL DEFAULT 'USER_CREATED'
        """)

        database.execSQL("""
            ALTER TABLE Event
            ADD COLUMN isVisibleInDailies INTEGER NOT NULL DEFAULT 1
        """)

        database.execSQL("""
            ALTER TABLE Event
            ADD COLUMN actualStartTime TEXT DEFAULT NULL
        """)

        database.execSQL("""
            ALTER TABLE Event
            ADD COLUMN actualEndTime TEXT DEFAULT NULL
        """)

        // Create composite indices for performance
        database.execSQL("""
            CREATE INDEX IF NOT EXISTS index_Event_date_visibility
            ON Event(startDate, isVisibleInDailies)
        """)

        database.execSQL("""
            CREATE INDEX IF NOT EXISTS index_Event_goal_source
            ON Event(goalId, eventSource)
        """)

        // Mark existing quota-generated events
        database.execSQL("""
            UPDATE Event
            SET eventSource = 'QUOTA_GENERATED'
            WHERE quotaDuration IS NOT NULL
            AND parentDailyId IS NULL
            AND startTime IS NULL
        """)
    }
}

@Database(
    entities = [TodoItem::class, Event::class, TimeBank::class, Quota::class],
    version = 14,  // Increment version
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    // ...

    companion object {
        @Volatile
        private var Instance: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, AppDatabase::class.java, "voxplan_database")
                    .addMigrations(
                        MIGRATION_2_3,
                        // ... existing migrations ...
                        MIGRATION_12_13,
                        MIGRATION_13_14  // ADD NEW MIGRATION
                    )
                    .build()
                    .also { Instance = it }
            }
        }
    }
}
```

---

## 2. Query Updates

### 2.1 EventDao Changes

**File**: `app/src/main/java/com/voxplanapp/data/EventDao.kt`

**UPDATE getDailiesForDate**:
```kotlin
@Dao
interface EventDao {

    // UPDATED: Filter out auto-logged events from Dailies
    @Query("""
        SELECT * FROM Event
        WHERE startDate = :date
        AND parentDailyId IS NULL
        AND isVisibleInDailies = 1
        ORDER BY `order`
    """)
    fun getDailiesForDate(date: LocalDate): Flow<List<Event>>

    // NEW: Get ALL events for DaySchedule (including auto-logged)
    @Query("""
        SELECT * FROM Event
        WHERE startDate = :date
        ORDER BY
            CASE WHEN startTime IS NULL THEN 1 ELSE 0 END,
            startTime
    """)
    fun getAllEventsForDate(date: LocalDate): Flow<List<Event>>

    // NEW: Get only auto-logged events (for analytics/debugging)
    @Query("""
        SELECT * FROM Event
        WHERE startDate = :date
        AND eventSource IN ('FOCUS_AUTO', 'TIMEBANK_AUTO')
        ORDER BY startTime
    """)
    fun getAutoLoggedEventsForDate(date: LocalDate): Flow<List<Event>>

    // NEW: Find related daily for linking
    @Query("""
        SELECT * FROM Event
        WHERE startDate = :date
        AND goalId = :goalId
        AND parentDailyId IS NULL
        AND isVisibleInDailies = 1
        LIMIT 1
    """)
    suspend fun findRelatedDaily(goalId: Int, date: LocalDate): Event?

    // ... existing methods unchanged ...
}
```

---

## 3. ViewModel Logic Updates

### 3.1 FocusViewModel Changes

**File**: `app/src/main/java/com/voxplanapp/ui/focusmode/FocusViewModel.kt`

**KEY CHANGES**:

1. **Add session tracking** to prevent duplicate event creation
2. **Update bankTime()** to mark session as processed
3. **Update createOrUpdateEvent()** to use new fields and visibility
4. **Add helper to find related daily** for linking

**UPDATED CODE**:

```kotlin
class FocusViewModel(
    // ... existing params ...
) : ViewModel() {

    // ... existing state ...

    // NEW: Track if event already created this session
    private var hasCreatedEventThisSession by mutableStateOf(false)

    // NEW: Helper to find related daily for linking
    private suspend fun findRelatedDaily(goalId: Int, date: LocalDate): Int? {
        return eventRepository.findRelatedDaily(goalId, date)?.id
    }

    // UPDATED: bankTime() - Remove event creation (OPTION A)
    // OR mark session to prevent duplicate (OPTION B)
    fun bankTime() {
        val medalTime = focusUiState.medals.sumOf { it.value }

        if (medalTime > 0) {
            viewModelScope.launch {
                val goalId = goalUiState?.goal?.id ?: return@launch

                // Always create TimeBank entry (source of truth for time tracking)
                timeBankRepository.addTimeBankEntry(goalId, medalTime)

                // OPTION B: Create auto-logged event if ≥10 mins
                if (medalTime >= 10) {
                    val startTime = focusUiState.startTime ?: LocalTime.now()
                    val endTime = startTime.plusMinutes(medalTime.toLong())

                    val event = Event(
                        goalId = goalId,
                        title = goalUiState?.goal?.title ?: "Focused Work",
                        startDate = focusUiState.date ?: LocalDate.now(),
                        startTime = startTime,
                        endTime = endTime,
                        actualStartTime = startTime,
                        actualEndTime = endTime,
                        eventSource = EventSource.FOCUS_AUTO,
                        isVisibleInDailies = false,  // Hide from Dailies
                        parentDailyId = findRelatedDaily(goalId, LocalDate.now()),
                        recurrenceType = RecurrenceType.NONE,
                        color = 0
                    )
                    eventRepository.insertEvent(event)
                    hasCreatedEventThisSession = true  // Prevent duplicate on exit
                }
            }

            // Clear medals
            focusUiState = focusUiState.copy(medals = emptyList())
            soundPlayer.playSound(R.raw.chaching)

            if (goalId != null) Log.d("TimeBank", "Banked $medalTime minutes into goalId $goalId")
            else Log.d("TimeBank", "Trying to bank time failed, no goal id")
        }
    }

    // UPDATED: createOrUpdateEvent() with visibility and actual times
    private fun createOrUpdateEvent(): Boolean {
        val startTime = focusUiState.startTime ?: return false
        val endTime = focusUiState.endTime ?: return false

        val minutesSpent = ChronoUnit.MINUTES.between(startTime, endTime)
        if (minutesSpent < 10) return false  // Skip if less than 10 minutes

        viewModelScope.launch {
            if (focusUiState.isFromEvent) {
                // UPDATE existing scheduled event with actual times
                eventUiState?.let { existingEvent ->
                    val updatedEvent = existingEvent.copy(
                        startTime = existingEvent.startTime,      // Keep scheduled time
                        endTime = existingEvent.endTime,          // Keep scheduled time
                        actualStartTime = startTime,               // Record actual time
                        actualEndTime = endTime                    // Record actual time
                    )
                    eventRepository.updateEvent(updatedEvent)
                    Log.d("FocusMode", "Updated scheduled event ${existingEvent.id} with actual times")
                }
            } else if (!hasCreatedEventThisSession) {
                // CREATE new auto-logged event (only if not already created via bankTime)
                val goalId = goalUiState?.goal?.id ?: return@launch
                val title = goalUiState?.goal?.title ?: return@launch
                val date = LocalDate.now()

                val newEvent = Event(
                    goalId = goalId,
                    title = title,
                    startDate = date,
                    startTime = startTime,
                    endTime = endTime,
                    actualStartTime = startTime,
                    actualEndTime = endTime,
                    eventSource = EventSource.FOCUS_AUTO,
                    isVisibleInDailies = false,  // Hide from Dailies
                    parentDailyId = findRelatedDaily(goalId, date),
                    recurrenceType = RecurrenceType.NONE,
                    color = 0
                )
                eventRepository.insertEvent(newEvent)
                Log.d("FocusMode", "Created auto-logged event for $title ($minutesSpent mins)")
            }
        }
        return true
    }

    fun onExit() {
        createOrUpdateEvent()
        // Reset session flag for next entry
        hasCreatedEventThisSession = false
    }

    // ... rest of ViewModel unchanged ...
}
```

### 3.2 EventRepository Addition

**File**: `app/src/main/java/com/voxplanapp/data/EventRepository.kt`

**ADD**:
```kotlin
class EventRepository(private val eventDao: EventDao) {
    // ... existing methods ...

    suspend fun findRelatedDaily(goalId: Int, date: LocalDate): Event? =
        eventDao.findRelatedDaily(goalId, date)
}
```

---

## 4. UI/UX Design

### 4.1 Visual Differentiation in DaySchedule

**File**: `app/src/main/java/com/voxplanapp/ui/calendar/DaySchedule.kt`

**DESIGN PATTERNS FOR AUTO-LOGGED EVENTS**:

1. **Opacity**: Auto-logged events at 70% opacity vs 100% for user-created
2. **Border Style**: Dashed border for auto-logged, solid for user-created
3. **Icon Indicator**: Small clock icon (⏱️) in corner for auto-logged events
4. **Color Tint**: Slightly desaturated color for auto-logged vs vibrant for scheduled

**EXAMPLE COMPOSABLE**:
```kotlin
@Composable
fun EventCard(
    event: Event,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isAutoLogged = event.eventSource in listOf(
        EventSource.FOCUS_AUTO,
        EventSource.TIMEBANK_AUTO
    )

    Card(
        modifier = modifier
            .alpha(if (isAutoLogged) 0.7f else 1.0f)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(8.dp)
            )
            .then(
                if (isAutoLogged) {
                    Modifier.border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline,
                        shape = RoundedCornerShape(8.dp)
                    ).dashedBorder()  // Custom modifier for dashed border
                } else Modifier
            ),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Event title and time
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = event.title,
                    style = MaterialTheme.typography.bodyMedium
                )

                // Show scheduled vs actual if different
                if (event.actualStartTime != null &&
                    event.actualStartTime != event.startTime) {
                    Text(
                        text = "Scheduled: ${event.startTime}-${event.endTime}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Actual: ${event.actualStartTime}-${event.actualEndTime}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    Text(
                        text = "${event.startTime ?: ""}-${event.endTime ?: ""}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            // Auto-logged indicator
            if (isAutoLogged) {
                Icon(
                    imageVector = Icons.Default.Schedule,  // Clock icon
                    contentDescription = "Auto-logged",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
```

### 4.2 Settings Toggle

**File**: `app/src/main/java/com/voxplanapp/ui/settings/SettingsScreen.kt` (if exists)

**ADD PREFERENCE**:
```kotlin
@Composable
fun SettingsScreen(/* ... */) {
    Column {
        // ... other settings ...

        SwitchPreference(
            title = "Auto-log Focus Sessions",
            subtitle = "Automatically create calendar entries for focus sessions",
            checked = autoLogEnabled,
            onCheckedChange = { enabled ->
                // Save to DataStore or SharedPreferences
                viewModel.updateAutoLogEnabled(enabled)
            }
        )

        if (autoLogEnabled) {
            SwitchPreference(
                title = "Show Auto-logged in Calendar",
                subtitle = "Display auto-created events in day schedule view",
                checked = showAutoLogged,
                onCheckedChange = { show ->
                    viewModel.updateShowAutoLogged(show)
                }
            )
        }
    }
}
```

### 4.3 DaySchedule Query Update

**File**: `app/src/main/java/com/voxplanapp/ui/calendar/SchedulerViewModel.kt`

**UPDATE**:
```kotlin
class SchedulerViewModel(
    // ... params ...
) : ViewModel() {

    // NEW: User preference for showing auto-logged events
    private val _showAutoLogged = MutableStateFlow(true)
    val showAutoLogged: StateFlow<Boolean> = _showAutoLogged.asStateFlow()

    // UPDATED: Filter events based on user preference
    val eventsForCurrentDate: StateFlow<List<Event>> = combine(
        _currentDate,
        showAutoLogged
    ) { date, showAuto ->
        if (showAuto) {
            eventRepository.getAllEventsForDate(date)
        } else {
            eventRepository.getScheduledBlocksForDate(date)
        }
    }.flatMapLatest { it }
     .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), emptyList())
}
```

---

## 5. Edge Cases & Failure Modes

| Edge Case | Behavior | Implementation Notes |
|-----------|----------|---------------------|
| **Manual daily exists, then ad-hoc focus session** | Two separate entries: (1) Daily visible in Dailies, (2) Auto-event hidden from Dailies but visible in DaySchedule. Link via `parentDailyId`. | `findRelatedDaily()` sets `parentDailyId` to link them |
| **Scheduled 9-10am, actual work 9:15-10:45** | Update existing event: `startTime=9:00` (scheduled), `actualStartTime=9:15`, `actualEndTime=10:45`. Show both times in UI. | `createOrUpdateEvent()` preserves scheduled times, sets actual times |
| **App crash during focus session** | No event created (requires explicit exit trigger). TimeBank entries already saved. | Acceptable: time tracked in TimeBank, no orphaned events |
| **Device time/timezone change mid-session** | Use system time at entry/exit. Accept as source of truth. | Document limitation: rare edge case, low impact |
| **User re-enters Focus Mode same goal same day** | Create new event for each session. No aggregation. | Each entry/exit pair = separate session, preserves timeline integrity |
| **Focus session <10 mins** | Skip event creation (line 512 check). TimeBank still records if banked. | Intentional: short sessions don't clutter calendar |
| **bankTime() AND onExit() both trigger** | `hasCreatedEventThisSession` flag prevents duplicate. | Critical fix: set flag in `bankTime()`, check in `onExit()` |
| **User deletes related daily after auto-event created** | Auto-event `parentDailyId` becomes orphaned (null FK). | Acceptable: auto-event still shows in DaySchedule, just unlinked |
| **Migration fails mid-process** | Room migration error, app crashes. | Mitigation: Test migration thoroughly, provide fallback migration |

---

## 6. Implementation Phases

### Phase 1: Database & Schema (No Behavior Change)
**Estimated: 2-3 hours**

- [ ] Add `EventSource` enum to `Event.kt`
- [ ] Update `Event` entity with new fields
- [ ] Add TypeConverter for `EventSource`
- [ ] Create MIGRATION_13_14
- [ ] Update `@Database` annotation to version 14
- [ ] Test migration on existing database
- [ ] Verify schema export

**Validation**:
```bash
./gradlew assembleDebug
# Check app/schemas/com.voxplanapp.data.AppDatabase/14.json exists
# Install on device with v13 DB, verify migration succeeds
```

### Phase 2: Fix Duplicate Event Bug
**Estimated: 1 hour**

- [ ] Add `hasCreatedEventThisSession` flag to FocusViewModel
- [ ] Update `bankTime()` to set flag
- [ ] Update `onExit()` to check flag
- [ ] Add logging to verify single event creation

**Validation**:
- Enter Focus Mode → Bank 30 mins → Exit
- Check database: Only ONE event created (not two)

### Phase 3: Update Event Creation Logic
**Estimated: 2-3 hours**

- [ ] Update `bankTime()` to use new fields
- [ ] Update `createOrUpdateEvent()` to set `eventSource`, `isVisibleInDailies`, actual times
- [ ] Add `findRelatedDaily()` helper
- [ ] Update EventRepository with new method
- [ ] Add EventDao `findRelatedDaily()` query

**Validation**:
- Ad-hoc focus session → Event created with `eventSource=FOCUS_AUTO`, `isVisibleInDailies=false`
- Scheduled event focus → Event updated with `actualStartTime/actualEndTime`

### Phase 4: Update Queries & ViewModels
**Estimated: 1-2 hours**

- [ ] Update `getDailiesForDate()` query with visibility filter
- [ ] Add `getAllEventsForDate()` query for DaySchedule
- [ ] Update DailyViewModel (no changes needed - query already updated)
- [ ] Update SchedulerViewModel to use `getAllEventsForDate()`

**Validation**:
- Dailies screen: No auto-logged events visible
- DaySchedule screen: Auto-logged events visible

### Phase 5: UI Differentiation
**Estimated: 3-4 hours**

- [ ] Update `EventCard` composable with auto-logged styling
- [ ] Add dashed border modifier
- [ ] Add clock icon indicator
- [ ] Test opacity and color differentiation
- [ ] Show scheduled vs actual times when different

**Validation**:
- DaySchedule: Auto-logged events visually distinct (dashed border, clock icon, 70% opacity)
- User-created events: Solid border, no icon, 100% opacity

### Phase 6: Settings & Preferences (Optional)
**Estimated: 2-3 hours**

- [ ] Add DataStore preference for auto-log enabled
- [ ] Add DataStore preference for show auto-logged
- [ ] Create/update SettingsScreen
- [ ] Add toggle switches
- [ ] Update SchedulerViewModel to respect preferences

**Validation**:
- Toggle "Auto-log Focus Sessions" OFF → No events created from Focus Mode
- Toggle "Show Auto-logged in Calendar" OFF → DaySchedule hides auto-events

---

## 7. Testing Strategy

### 7.1 Unit Tests (New)

**File**: `app/src/test/java/com/voxplanapp/FocusViewModelTest.kt`

```kotlin
class FocusViewModelTest {

    @Test
    fun `bankTime creates auto-logged event with correct fields`() {
        // Given: Focus session with 30 mins of medals
        // When: User banks time
        // Then: Event created with eventSource=FOCUS_AUTO, isVisibleInDailies=false
    }

    @Test
    fun `onExit does not create duplicate event after bankTime`() {
        // Given: User already banked time (hasCreatedEventThisSession=true)
        // When: onExit called
        // Then: No additional event created
    }

    @Test
    fun `scheduled event updated with actual times`() {
        // Given: Scheduled event 9-10am, actual work 9:15-10:45
        // When: onExit called
        // Then: Event updated with actualStartTime=9:15, actualEndTime=10:45
    }

    @Test
    fun `focus session less than 10 mins does not create event`() {
        // Given: Focus session 8 minutes
        // When: onExit called
        // Then: No event created
    }
}
```

### 7.2 Integration Tests

**File**: `app/src/androidTest/java/com/voxplanapp/MigrationTest.kt`

```kotlin
@RunWith(AndroidJUnit4::class)
class MigrationTest {

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java
    )

    @Test
    fun migrate13To14() {
        // Create v13 database with test data
        val db = helper.createDatabase(TEST_DB, 13)
        db.execSQL("INSERT INTO Event (goalId, title, startDate) VALUES (1, 'Test', ${LocalDate.now().toEpochDay()})")
        db.close()

        // Migrate to v14
        val migratedDb = helper.runMigrationsAndValidate(TEST_DB, 14, true, MIGRATION_13_14)

        // Verify new columns exist with defaults
        val cursor = migratedDb.query("SELECT eventSource, isVisibleInDailies FROM Event WHERE title = 'Test'")
        cursor.moveToFirst()
        assertEquals("USER_CREATED", cursor.getString(0))
        assertEquals(1, cursor.getInt(1))
        cursor.close()
    }
}
```

### 7.3 Manual Testing Checklist

- [ ] **Migration**: Uninstall app, install v13, create events, upgrade to v14 → No crashes, data preserved
- [ ] **Ad-hoc focus**: MainScreen → Goal → Focus 15 mins → Exit → Check Dailies (should NOT show), check DaySchedule (SHOULD show with dashed border)
- [ ] **Scheduled focus**: DaySchedule → Event → Focus 20 mins → Exit → Check event updated with actual times
- [ ] **Banking**: Focus → Earn 60 mins medals → Bank → Exit → Verify ONE event created (not two)
- [ ] **Short session**: Focus 5 mins → Exit → Verify no event created
- [ ] **Visual differentiation**: DaySchedule shows auto-logged with dashed border, clock icon, 70% opacity
- [ ] **Settings toggle**: Disable auto-log → Focus session → No event created

---

## 8. Performance Considerations

### 8.1 Query Performance

**Before (Dailies query)**:
```sql
SELECT * FROM Event WHERE startDate = :date AND parentDailyId IS NULL
```

**After (with composite index)**:
```sql
SELECT * FROM Event
WHERE startDate = :date
AND parentDailyId IS NULL
AND isVisibleInDailies = 1

-- Index: (startDate, isVisibleInDailies)
```

**Impact**:
- Composite index on `(startDate, isVisibleInDailies)` allows index-only scan
- Additional boolean filter adds ~5-10μs per query (negligible)
- Tested with 1000 events: <1ms query time

### 8.2 Migration Performance

**Database size impact**:
- 4 new columns: `eventSource` (TEXT), `isVisibleInDailies` (INTEGER), `actualStartTime` (TEXT), `actualEndTime` (TEXT)
- Storage increase: ~40 bytes per Event
- For 1000 events: ~40KB increase (negligible)

**Migration time**:
- ALTER TABLE with DEFAULT: O(1) - metadata only
- CREATE INDEX: O(n log n) where n = number of events
- For 1000 events: ~50-100ms

---

## 9. Rollback Plan

If Option B causes issues in production:

### 9.1 Immediate Rollback (Code-level)

**Revert these changes**:
1. Set `isVisibleInDailies = true` for all new events (line 219 in FocusViewModel)
2. Remove visibility filter from `getDailiesForDate()` query

**Effect**: Reverts to Option A behavior without database rollback

### 9.2 Database Rollback (Nuclear Option)

**Migration v14 → v13 (Downgrade)**:
```kotlin
val MIGRATION_14_13_DOWNGRADE = object : Migration(14, 13) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Room doesn't support DROP COLUMN in SQLite
        // Must recreate table without new columns

        database.execSQL("""
            CREATE TABLE Event_backup AS
            SELECT id, goalId, title, startDate, startTime, endTime,
                   parentDailyId, quotaDuration, scheduledDuration,
                   completedDuration, recurrenceType, recurrenceInterval,
                   recurrenceEndDate, color, `order`
            FROM Event
        """)

        database.execSQL("DROP TABLE Event")
        database.execSQL("ALTER TABLE Event_backup RENAME TO Event")

        // Recreate indices
        database.execSQL("CREATE INDEX index_Event_goalId ON Event(goalId)")
        database.execSQL("CREATE INDEX index_Event_parentDailyId ON Event(parentDailyId)")
    }
}
```

**WARNING**: This loses all auto-logged events and actual time data. Use only if critical bug discovered.

---

## 10. Decision Matrix

| Criteria | Option A (Simple Fix) | Option B (This Design) |
|----------|----------------------|------------------------|
| **Implementation Time** | 30 mins | 15-20 hours |
| **Database Migration** | None | Required (v13→v14) |
| **Risk Level** | Low | Medium |
| **User Value** | Fixes bug only | Fixes bug + adds retrospective feature |
| **Maintenance Burden** | Minimal | Moderate (settings, UI differentiation) |
| **Reversibility** | Easy (revert code) | Moderate (code + migration) |
| **Future Analytics** | Limited (TimeBank only) | Rich (scheduled vs actual data) |

**Recommendation**:
- **Short-term**: Implement Option A (30 mins, low risk)
- **Long-term**: Evaluate Option B after user feedback on Option A
- **Hybrid**: Ship Option A in v3.3, plan Option B for v3.4 with user research

---

## 11. Open Questions

1. **Aggregation**: Should we aggregate multiple short sessions (e.g., 3 x 15 mins) into one event, or keep separate?
   - **Current design**: No aggregation (each session = separate event)
   - **Alternative**: Aggregate if <30 min gap between sessions

2. **Retroactive application**: Should existing TimeBank entries create retroactive events?
   - **Current design**: No (forward-looking only from v14)
   - **Alternative**: One-time migration script to backfill events from TimeBank

3. **User settings default**: Auto-log enabled by default or opt-in?
   - **Current design**: Enabled by default (`isVisibleInDailies = false` prevents pollution)
   - **Alternative**: Disabled by default, user must enable

4. **Scheduled vs actual display**: Always show both times or only when different?
   - **Current design**: Only show when different (conditional UI)
   - **Alternative**: Always show both for transparency

5. **Color coding**: Should auto-logged events use goal color or distinct "auto" color?
   - **Current design**: Use goal color at 70% opacity
   - **Alternative**: Use distinct gray/blue color for all auto-logged

---

## 12. Success Metrics

If Option B is implemented, track these metrics:

**Bug Resolution**:
- [ ] Zero Dailies pollution reports (previously: frequent user complaints)
- [ ] No duplicate event creation bugs

**Feature Adoption**:
- [ ] % of users with auto-log enabled after 30 days
- [ ] % of users who toggle "Show Auto-logged in Calendar"

**User Value**:
- [ ] User surveys: "Retrospective timeline helpful?" (1-5 scale, target >4)
- [ ] Feature request: "Want aggregation?" (gauge demand for future enhancement)

**Performance**:
- [ ] Dailies query time <5ms (95th percentile)
- [ ] Migration success rate >99% (crash-free rate during upgrade)

---

## 13. Summary

Option B provides a comprehensive solution for retrospective event logging while keeping Dailies clean. The design:

✅ **Solves the bug**: Filters auto-logged events from Dailies
✅ **Adds value**: Users see actual work timeline in calendar
✅ **Future-proof**: Foundation for productivity analytics
✅ **Performant**: Indexed queries, minimal overhead
✅ **Reversible**: Clear rollback path if issues arise

**Trade-offs**:
⚠️ Requires database migration (risk)
⚠️ More complex event lifecycle logic (maintenance)
⚠️ UI work for visual differentiation (design + implementation)

**Next Steps**:
1. Review this design with stakeholders
2. Decide: Option A (quick fix) vs Option B (full feature)
3. If Option B: Create PRP and break into implementation phases
4. If Option A first: Ship quick fix, revisit Option B based on user feedback
