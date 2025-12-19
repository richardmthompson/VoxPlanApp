# BASE PRP: Complete Dailies Feature Implementation

## Task Overview

Complete the Dailies feature by addressing all incomplete features and known issues documented in `INCOMPLETE_FEATURES.md`. Transform the Dailies screen from a basic task list (70% complete) into a production-ready daily planning interface with completion tracking, Focus Mode integration, bulk operations, and smart suggestions.

**Priority Objectives:**
1. ✅ Completion tracking with checkboxes and visual feedback
2. ✅ Direct Focus Mode access from daily tasks
3. ✅ Parent-child completion logic (dailies ↔ scheduled events)
4. ✅ Bulk operations (multi-select, swipe gestures)
5. ✅ Smart task suggestions based on quotas and history
6. ✅ Error handling for quota lookup failures
7. ✅ Remove broken QuickScheduleScreen.kt
8. ✅ Empty states and daily summary celebrations

**Current State:** Dailies Beta (VP 3.2) - Can create daily tasks from quotas and schedule them, but missing completion tracking, bulk operations, and polish.

**Target State:** Production-ready daily planning interface that rivals industry leaders (Todoist, Things 3, TickTick) while leveraging VoxPlanApp's unique quota + time banking system.

---

## Context

### Architecture Context

**Event Entity (Parent-Child Pattern):**
- **Parent Daily**: `parentDailyId = null`, `startTime/endTime = null`, represents unscheduled daily task
- **Scheduled Child**: `parentDailyId = dailyId`, `startTime/endTime` populated, represents time-blocked event

**Current Event Model** (`/Users/richardthompson/StudioProjects/VoxPlanApp/app/src/main/java/com/voxplanapp/data/Event.kt`):
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
    val quotaDuration: Int? = null,     // in minutes, from quota
    val scheduledDuration: Int? = null,  // in minutes, calculated from schedule
    val completedDuration: Int? = null,  // in minutes, from TimeBank entries
    val parentDailyId: Int? = null       // Identifies parent-child relationship
    // ❌ MISSING: isCompleted, completedAt fields
)
```

**Database:** Room 2.6.1, current version 13, schema exports in `app/schemas/`

**Repositories:**
- `EventRepository` - Event CRUD operations
- `QuotaRepository` - Daily quota management with `activeDays` encoding
- `TimeBankRepository` - Actual time tracking from Focus Mode
- `TodoRepository` - Goal hierarchy management

### Existing Patterns to Follow

**From VoxPlanApp Pattern Analysis** (`/Users/richardthompson/StudioProjects/VoxPlanApp/.claude/PRPs/ai_docs/voxplan_design_patterns_analysis.md`):

**1. Checkbox Pattern (from GoalItem.kt lines 286-299):**
```kotlin
IconButton(onClick = { onCompleteClick(task) }) {
    Icon(
        painter = if (task.completedDate != null)
            painterResource(id = R.drawable.ic_selected_check_box)
            else painterResource(R.drawable.ic_empty_check_box),
        contentDescription = if (task.completedDate != null) "Mark Incomplete" else "Mark Complete",
        tint = TodoItemIconColor,
        modifier = Modifier.size(20.dp)
    )
}
```

**2. Completion Visual Feedback (from GoalItem.kt lines 83-84):**
```kotlin
val textColor = if (goal.goal.completedDate != null)
    TodoItemTextColor.copy(alpha = 0.5f) else TodoItemTextColor
val textDecoration = if (goal.goal.completedDate != null)
    TextDecoration.LineThrough else null
```

**3. StateFlow Pattern (from DailyViewModel.kt lines 49-55):**
```kotlin
private val _uiState = MutableStateFlow(DailyUiState(...))
val uiState: StateFlow<DailyUiState> = _uiState.asStateFlow()
```

**4. AlertDialog Pattern (from DailyScreen.kt lines 55-71):**
```kotlin
AlertDialog(
    onDismissRequest = { viewModel.cancelAction() },
    title = { Text("Dialog Title") },
    text = { Text("Dialog message") },
    confirmButton = { Button(onClick = { ... }) { Text("Confirm") } },
    dismissButton = { Button(onClick = { ... }) { Text("Cancel") } }
)
```

**5. ActionMode Integration (from DailyViewModel.kt lines 43-45):**
```kotlin
private val _actionMode = mutableStateOf<ActionMode>(ActionMode.Normal)
val actionMode: State<ActionMode> = _actionMode
val actionModeHandler = ActionModeHandler(_actionMode)
```

**6. Focus Mode Navigation (from VoxPlanNavHost.kt line 45-47):**
```kotlin
onEnterFocusMode = { goalId ->
    navController.navigate(VoxPlanScreen.FocusMode.createRouteFromGoal(goalId))
}
```

### Color Constants (from Colors.kt)
- **TodoItemIconColor** = Color(0xFF0F0A2C) - Dark blue for icons
- **ActivatedColor** = Color(0xFFB21720) - Red for active ActionMode
- **QuotaCompleteBackgroundColor** = Color(0xFFC107) - Gold for completed quotas
- **TodoItemTextColor** = Color(0xFF0F0A2C) - Dark blue for text

### Research-Backed UI/UX Decisions

**From Dailies Completion UX Research** (`.claude/PRPs/ai_docs/dailies_completion_ux_research.md`):

**Decision 1: Checkbox > Tap-to-Complete**
- All major apps (Todoist, Things, TickTick) use left-side checkbox
- Prevents accidental completion when tapping for details/scheduling
- Clear separation between "mark done" vs "interact with task"

**Decision 2: Fade-and-Stay Pattern (Things 3)**
- Completed tasks fade to 50% opacity + strikethrough
- Stay visible until manually archived
- Benefits: immediate satisfaction, undo safety, progress visibility

**Decision 3: Parent-Child Completion Logic**
- Completing parent daily → Auto-completes all scheduled children (with confirmation)
- All children complete → Suggest completing parent (via snackbar)
- Maintains data consistency with time banking

**Decision 4: One-Tap Focus Access**
- Industry trend: unified workflows without app switching
- Two paths: "Quick Focus" (unscheduled) vs "Start Focus" (scheduled)
- Active indicator shows "🎯 IN FOCUS" when task is being worked on

**From Material Design 3 Research** (`.claude/PRPs/ai_docs/material_design_3_action_heavy_lists.md`):

**Action Limit:** Max 2 visible actions per list item (checkbox + schedule icon)
- Additional actions in overflow menu (⋮): Focus, Edit, Delete
- Reduces cognitive load, meets Material Design guideline

**Touch Targets:** 48dp minimum (already met by existing icon sizes)

**Selection Mode:** Long-press activation with contextual action bar

**Swipe Actions:**
- Right swipe: Complete (green background)
- Left swipe: Delete (red background)

### External Research URLs

**Checkbox Patterns:**
- https://developer.android.com/develop/ui/compose/components/checkbox
- https://m3.material.io/components/checkbox/guidelines
- https://github.com/cvs-health/android-compose-accessibility-techniques/blob/main/doc/components/CheckboxControls.md

**Multi-Select:**
- https://developer.android.com/develop/ui/compose/lists
- https://developer.android.com/develop/ui/compose/touch-input/pointer-input/tap-and-press
- https://medium.com/make-apps-simple/multi-list-item-selection-in-jetpack-compose-301fcf375a6c

**Room Migration:**
- https://developer.android.com/training/data-storage/room/migrating-db-versions
- https://medium.com/androiddevelopers/testing-room-migrations-be93cdb0d975

**Productivity Apps:**
- https://www.todoist.com/help/articles/plan-your-day-with-the-today-view-UVUXaiSs
- https://culturedcode.com/things/support/articles/4001304/
- https://help.ticktick.com/articles/7055792921664028672

### Gotchas

**Issue 1: Boolean Storage in SQLite**
- SQLite has no native boolean type
- Room maps Kotlin `Boolean` to SQLite `INTEGER` (0=false, 1=true)
- Migration SQL: `ALTER TABLE Event ADD COLUMN isCompleted INTEGER NOT NULL DEFAULT 0`
- **Never use "false"/"true" in defaultValue** - must use "0"/"1"

**Issue 2: Parent-Child Orphaning**
- Deleting parent daily without handling children creates orphaned scheduled events
- Solution: DailyViewModel already has confirmDelete logic (lines 175-184) that deletes children first
- Must extend this pattern for completion

**Issue 3: Multi-Select State Management**
- Selection state must survive recomposition
- Use StateFlow in ViewModel, not mutableStateOf in Composable
- Provide stable keys in LazyColumn: `items(key = { event -> event.id })`

**Issue 4: Quota Lookup Can Fail**
- Current addQuotaTasks() (lines 112-137) has no error handling
- If goal lookup fails, silently continues
- Solution: Wrap in try-catch, show user-friendly snackbar

**Issue 5: Time Unit Inconsistencies**
- Timer: seconds (Long)
- Medals: minutes (Int)
- TimeBank: minutes (Int)
- Quota: minutes (Int)
- Always convert timer to minutes: `currentTime / 60`

**Issue 6: QuickScheduleScreen Broken**
- Lines 26-86 entirely commented out
- References undefined `goal` variable (parameter is `goalId: Int`)
- **Solution: Remove file entirely** - functionality replaced by improved DailyScreen

---

## Task Breakdown

### PHASE 1: Database Migration & Core Completion

**Estimated Time:** 2-3 days

#### STEP 1.1: Update Event Entity

**ACTION** `app/src/main/java/com/voxplanapp/data/Event.kt`
- **OPERATION:** Add completion tracking fields
- **CHANGE:**
  ```kotlin
  // BEFORE (line 28):
  val parentDailyId: Int? = null
  )

  // AFTER:
  val parentDailyId: Int? = null,
  val isCompleted: Boolean = false,        // NEW: Completion status
  val completedAt: Instant? = null         // NEW: Completion timestamp
  )
  ```
- **IMPORTS TO ADD:**
  ```kotlin
  import kotlinx.datetime.Instant
  ```
- **VALIDATE:** Build project - `./gradlew assembleDebug`
- **IF_FAIL:** Check import statement, verify Instant is from `kotlinx.datetime`
- **EXPECTED:** Compilation error in AppDatabase (expected - we'll fix next)

#### STEP 1.2: Create Database Migration

**ACTION** `app/src/main/java/com/voxplanapp/data/AppDatabase.kt`
- **OPERATION:** Add MIGRATION_13_14 object in companion object (after MIGRATION_12_13)
- **CHANGE:**
  ```kotlin
  // Add after MIGRATION_12_13 (around line 150):

  val MIGRATION_13_14 = object : Migration(13, 14) {
      override fun migrate(database: SupportSQLiteDatabase) {
          // Add completion tracking columns
          // SQLite stores boolean as INTEGER: 0=false, 1=true
          database.execSQL(
              "ALTER TABLE Event ADD COLUMN isCompleted INTEGER NOT NULL DEFAULT 0"
          )

          // Nullable timestamp for completion time (Long milliseconds since epoch)
          database.execSQL(
              "ALTER TABLE Event ADD COLUMN completedAt INTEGER"
          )
      }
  }
  ```
- **VALIDATE:** Build project - `./gradlew assembleDebug`
- **IF_FAIL:** Check SQL syntax, verify column names match Event entity exactly
- **EXPECTED:** Compilation succeeds, but runtime error (migration not registered yet)

#### STEP 1.3: Update Database Version & Register Migration

**ACTION** `app/src/main/java/com/voxplanapp/data/AppDatabase.kt`
- **OPERATION:** Increment version number and register new migration
- **CHANGE:**
  ```kotlin
  // BEFORE (line 10):
  @Database(entities = [TodoItem::class, Event::class, TimeBank::class, Quota::class], version = 13)

  // AFTER:
  @Database(entities = [TodoItem::class, Event::class, TimeBank::class, Quota::class], version = 14)
  ```
- **AND** in `AppContainer.kt` (line 28-34):
  ```kotlin
  // BEFORE:
  .addMigrations(
      AppDatabase.MIGRATION_2_3, AppDatabase.MIGRATION_3_4,
      AppDatabase.MIGRATION_4_5, AppDatabase.MIGRATION_5_6,
      AppDatabase.MIGRATION_6_7, AppDatabase.MIGRATION_7_8,
      AppDatabase.MIGRATION_8_9, AppDatabase.MIGRATION_9_10,
      AppDatabase.MIGRATION_10_11, AppDatabase.MIGRATION_11_12,
      AppDatabase.MIGRATION_12_13
  )

  // AFTER:
  .addMigrations(
      AppDatabase.MIGRATION_2_3, AppDatabase.MIGRATION_3_4,
      AppDatabase.MIGRATION_4_5, AppDatabase.MIGRATION_5_6,
      AppDatabase.MIGRATION_6_7, AppDatabase.MIGRATION_7_8,
      AppDatabase.MIGRATION_8_9, AppDatabase.MIGRATION_9_10,
      AppDatabase.MIGRATION_10_11, AppDatabase.MIGRATION_11_12,
      AppDatabase.MIGRATION_12_13, AppDatabase.MIGRATION_13_14
  )
  ```
- **VALIDATE:** `./gradlew clean assembleDebug` then install on device with existing data
- **IF_FAIL:** Check migration is imported, verify version numbers match
- **DEBUG STRATEGY:**
  ```bash
  # Check database after migration
  adb shell
  run-as com.voxplanapp
  sqlite3 databases/todo-db
  PRAGMA table_info(Event);
  # Should show isCompleted and completedAt columns
  ```
- **EXPECTED:** App launches successfully, existing events have isCompleted=0, completedAt=null

#### STEP 1.4: Add EventDao Queries for Completion

**ACTION** `app/src/main/java/com/voxplanapp/data/EventDao.kt`
- **OPERATION:** Add queries for filtering by completion status
- **CHANGE:**
  ```kotlin
  // Add after existing queries (around line 52):

  @Query("""
      SELECT * FROM Event
      WHERE startDate = :date
      AND parentDailyId IS NULL
      AND isCompleted = 1
      ORDER BY completedAt DESC
  """)
  fun getCompletedDailiesForDate(date: LocalDate): Flow<List<Event>>

  @Query("""
      SELECT * FROM Event
      WHERE startDate = :date
      AND parentDailyId IS NULL
      AND isCompleted = 0
      ORDER BY `order`
  """)
  fun getIncompleteDailiesForDate(date: LocalDate): Flow<List<Event>>

  @Query("UPDATE Event SET isCompleted = :completed, completedAt = :timestamp WHERE id = :eventId")
  suspend fun updateCompletion(eventId: Int, completed: Boolean, timestamp: Long?)

  // Bulk completion update
  @Query("UPDATE Event SET isCompleted = :completed, completedAt = :timestamp WHERE id IN (:eventIds)")
  suspend fun bulkUpdateCompletion(eventIds: List<Int>, completed: Boolean, timestamp: Long?)
  ```
- **VALIDATE:** Build project - `./gradlew assembleDebug`
- **IF_FAIL:** Check query syntax, verify column names match schema
- **EXPECTED:** Successful compilation

#### STEP 1.5: Update EventRepository

**ACTION** `app/src/main/java/com/voxplanapp/data/EventRepository.kt`
- **OPERATION:** Add repository methods for completion operations
- **CHANGE:**
  ```kotlin
  // Add after existing methods (around line 28):

  fun getCompletedDailiesForDate(date: LocalDate): Flow<List<Event>> =
      eventDao.getCompletedDailiesForDate(date)

  fun getIncompleteDailiesForDate(date: LocalDate): Flow<List<Event>> =
      eventDao.getIncompleteDailiesForDate(date)

  suspend fun updateCompletion(eventId: Int, completed: Boolean) {
      val timestamp = if (completed) Clock.System.now().toEpochMilliseconds() else null
      eventDao.updateCompletion(eventId, completed, timestamp)
  }

  suspend fun bulkUpdateCompletion(eventIds: List<Int>, completed: Boolean) {
      val timestamp = if (completed) Clock.System.now().toEpochMilliseconds() else null
      eventDao.bulkUpdateCompletion(eventIds, completed, timestamp)
  }
  ```
- **IMPORTS TO ADD:**
  ```kotlin
  import kotlinx.datetime.Clock
  ```
- **VALIDATE:** Build project - `./gradlew assembleDebug`
- **IF_FAIL:** Check Clock import from kotlinx.datetime
- **EXPECTED:** Successful compilation

#### STEP 1.6: Update DailyViewModel with Completion Logic

**ACTION** `app/src/main/java/com/voxplanapp/ui/daily/DailyViewModel.kt`
- **OPERATION:** Add completion toggle function and dialog state
- **CHANGE:**
  ```kotlin
  // Add after _showDeleteConfirmation (around line 60):

  // State for parent-child completion dialog
  private val _showCompletionDialog = MutableStateFlow<Event?>(null)
  val showCompletionDialog: StateFlow<Event?> = _showCompletionDialog.asStateFlow()

  // Add after cancelDelete() (around line 190):

  fun toggleTaskCompletion(task: Event) {
      viewModelScope.launch {
          // Check if this is a parent daily with scheduled children
          if (task.parentDailyId == null) {
              val children = eventRepository.getEventsWithParentId(task.id).first()

              if (children.isNotEmpty() && !task.isCompleted) {
                  // Completing parent with children - show dialog
                  _showCompletionDialog.value = task
              } else {
                  // No children or uncompleting - toggle directly
                  eventRepository.updateCompletion(task.id, !task.isCompleted)
              }
          } else {
              // Child event - toggle directly
              eventRepository.updateCompletion(task.id, !task.isCompleted)

              // Check if all siblings are now complete
              checkParentCompletionSuggestion(task)
          }
      }
  }

  fun confirmCompleteWithChildren(task: Event) {
      viewModelScope.launch {
          // Mark parent complete
          eventRepository.updateCompletion(task.id, true)

          // Mark all children complete
          val children = eventRepository.getEventsWithParentId(task.id).first()
          children.forEach { child ->
              eventRepository.updateCompletion(child.id, true)
          }

          _showCompletionDialog.value = null
      }
  }

  fun confirmCompleteDailyOnly(task: Event) {
      viewModelScope.launch {
          // Mark only parent complete, leave children independent
          eventRepository.updateCompletion(task.id, true)
          _showCompletionDialog.value = null
      }
  }

  fun cancelCompletionDialog() {
      _showCompletionDialog.value = null
  }

  private suspend fun checkParentCompletionSuggestion(childEvent: Event) {
      childEvent.parentDailyId?.let { parentId ->
          val siblings = eventRepository.getEventsWithParentId(parentId).first()
          val allComplete = siblings.all { it.isCompleted }

          if (allComplete) {
              // TODO: Show snackbar suggestion to complete parent
              // Will implement in UI phase
          }
      }
  }
  ```
- **VALIDATE:** Build project - `./gradlew assembleDebug`
- **IF_FAIL:** Check imports, verify EventRepository methods exist
- **EXPECTED:** Successful compilation

#### STEP 1.7: Add Checkbox UI to DailyTaskItem

**ACTION** `app/src/main/java/com/voxplanapp/ui/daily/DailyScreen.kt`
- **OPERATION:** Add checkbox IconButton and completion visual feedback
- **LOCATION:** DailyTaskItem composable (around line 348)
- **CHANGE:**
  ```kotlin
  // Find the Row with action icons (around line 375):
  Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
  ) {
      // ADD THIS - Checkbox button at start of row:
      IconButton(
          onClick = { onTaskComplete(task) },
          modifier = Modifier.size(32.dp)
      ) {
          Icon(
              painter = if (task.isCompleted)
                  painterResource(id = R.drawable.ic_selected_check_box)
              else
                  painterResource(id = R.drawable.ic_empty_check_box),
              contentDescription = if (task.isCompleted) "Mark Incomplete" else "Mark Complete",
              tint = TodoItemIconColor,
              modifier = Modifier.size(20.dp)
          )
      }

      // UPDATE existing Text - Add visual feedback for completed state:
      Text(
          text = task.title,
          style = MaterialTheme.typography.bodyLarge.copy(
              color = if (task.isCompleted)
                  TodoItemTextColor.copy(alpha = 0.5f)
              else
                  TodoItemTextColor,
              textDecoration = if (task.isCompleted)
                  TextDecoration.LineThrough
              else
                  null
          ),
          modifier = Modifier
              .weight(1f)
              .padding(start = 8.dp)
      )

      // Existing schedule and delete buttons remain...
  }
  ```
- **IMPORTS TO ADD:**
  ```kotlin
  import androidx.compose.ui.res.painterResource
  import androidx.compose.ui.text.style.TextDecoration
  import com.voxplanapp.ui.constants.TodoItemIconColor
  ```
- **VALIDATE:** Build and run on device - `./gradlew installDebug`
- **IF_FAIL:**
  - Check drawable resources exist: `R.drawable.ic_selected_check_box`, `R.drawable.ic_empty_check_box`
  - Verify TodoItemIconColor import from constants
- **EXPECTED:** Checkboxes appear on left side of daily tasks

#### STEP 1.8: Wire Checkbox to ViewModel

**ACTION** `app/src/main/java/com/voxplanapp/ui/daily/DailyScreen.kt`
- **OPERATION:** Add callback parameter and connect to ViewModel
- **CHANGE:**
  ```kotlin
  // Update DailyTaskItem signature (around line 348):
  @Composable
  fun DailyTaskItem(
      task: Event,
      quotaProgress: QuotaProgress?,
      actionMode: ActionMode,
      onSchedule: (Event) -> Unit,
      onDelete: (Event) -> Unit,
      onReorder: () -> Unit,
      onTaskComplete: (Event) -> Unit,  // ADD THIS
      modifier: Modifier = Modifier
  ) {
      // ... existing code
  }

  // In DailyScreen composable, update LazyColumn items call (around line 236):
  items(
      items = uiState.dailyTasks,
      key = { task -> task.id }  // IMPORTANT: Stable keys for animations
  ) { task ->
      val quotaProgress = quotaProgresses[task.goalId]

      DailyTaskItem(
          task = task,
          quotaProgress = quotaProgress,
          actionMode = actionMode,
          onSchedule = {
              selectedTaskForScheduling = it
          },
          onDelete = { viewModel.deleteTask(it) },
          onReorder = {
              if (actionMode != ActionMode.Normal) {
                  viewModel.reorderTask(task)
              }
          },
          onTaskComplete = { viewModel.toggleTaskCompletion(it) },  // ADD THIS
          modifier = Modifier.fillMaxWidth()
      )
  }
  ```
- **VALIDATE:** Build and run - `./gradlew installDebug`
- **IF_FAIL:** Check parameter names match function signature
- **EXPECTED:** Tapping checkbox toggles completion state

#### STEP 1.9: Add Completion Dialog

**ACTION** `app/src/main/java/com/voxplanapp/ui/daily/DailyScreen.kt`
- **OPERATION:** Add parent-child completion confirmation dialog
- **LOCATION:** After delete confirmation dialog (around line 72)
- **CHANGE:**
  ```kotlin
  // Collect completion dialog state
  val showCompletionDialog by viewModel.showCompletionDialog.collectAsState()

  // Add dialog after showDeleteConfirmation dialog:
  showCompletionDialog?.let { task ->
      AlertDialog(
          onDismissRequest = { viewModel.cancelCompletionDialog() },
          title = { Text("Complete Daily Task") },
          text = {
              Text(
                  "This daily task has scheduled time blocks. Would you like to mark them as complete too?"
              )
          },
          confirmButton = {
              Button(onClick = {
                  viewModel.confirmCompleteWithChildren(task)
              }) {
                  Text("Complete All")
              }
          },
          dismissButton = {
              Button(onClick = {
                  viewModel.confirmCompleteDailyOnly(task)
              }) {
                  Text("Daily Only")
              }
          }
      )
  }
  ```
- **VALIDATE:** Build and run - `./gradlew installDebug`
- **TEST STEPS:**
  1. Create daily task from quota
  2. Schedule it (creates child event)
  3. Mark daily complete (checkbox)
  4. **VERIFY:** Dialog appears with "Complete All" and "Daily Only" options
  5. Test both options
- **IF_FAIL:** Check StateFlow collection, verify dialog appears
- **EXPECTED:** Dialog shows when completing parent with children

#### STEP 1.10: Add Completion Animation

**ACTION** `app/src/main/java/com/voxplanapp/ui/daily/DailyScreen.kt`
- **OPERATION:** Add subtle animation on completion toggle
- **CHANGE:**
  ```kotlin
  // In DailyTaskItem, wrap Card with AnimatedVisibility or add animation modifier:

  @Composable
  fun DailyTaskItem(
      // ... parameters
  ) {
      // Add animation state
      val alpha by animateFloatAsState(
          targetValue = if (task.isCompleted) 0.5f else 1f,
          animationSpec = tween(durationMillis = 200),
          label = "completion_alpha"
      )

      Card(
          modifier = modifier
              .fillMaxWidth()
              .alpha(alpha)  // ADD THIS - Smooth fade animation
              .clickable(
                  enabled = actionMode != ActionMode.Normal,
                  onClick = onReorder
              ),
          elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
      ) {
          // ... existing content
      }
  }
  ```
- **IMPORTS TO ADD:**
  ```kotlin
  import androidx.compose.animation.core.animateFloatAsState
  import androidx.compose.animation.core.tween
  import androidx.compose.ui.draw.alpha
  ```
- **VALIDATE:** Build and run - `./gradlew installDebug`
- **TEST:** Toggle checkbox, verify smooth 200ms fade animation
- **IF_FAIL:** Check animation imports
- **EXPECTED:** Smooth visual feedback on completion toggle

---

### PHASE 2: Direct Focus Mode Access

**Estimated Time:** 1-2 days

#### STEP 2.1: Add Focus Button to DailyTaskItem

**ACTION** `app/src/main/java/com/voxplanapp/ui/daily/DailyScreen.kt`
- **OPERATION:** Add "Start Focus" IconButton to action row
- **LOCATION:** DailyTaskItem Row with icons (around line 390)
- **CHANGE:**
  ```kotlin
  // In the Row with schedule and delete icons, ADD:

  // Focus Mode button (between title and schedule button)
  IconButton(
      onClick = { onEnterFocusMode(task) },
      modifier = Modifier.size(32.dp)
  ) {
      Icon(
          imageVector = Icons.Default.Stream,
          contentDescription = "Start Focus Mode",
          modifier = Modifier.size(20.dp)
      )
  }

  // Existing schedule button...
  // Existing delete button...
  ```
- **UPDATE DailyTaskItem signature:**
  ```kotlin
  @Composable
  fun DailyTaskItem(
      task: Event,
      quotaProgress: QuotaProgress?,
      actionMode: ActionMode,
      onSchedule: (Event) -> Unit,
      onDelete: (Event) -> Unit,
      onReorder: () -> Unit,
      onTaskComplete: (Event) -> Unit,
      onEnterFocusMode: (Event) -> Unit,  // ADD THIS
      modifier: Modifier = Modifier
  ) {
      // ... existing code
  }
  ```
- **IMPORTS TO ADD:**
  ```kotlin
  import androidx.compose.material.icons.Icons
  import androidx.compose.material.icons.filled.Stream
  ```
- **VALIDATE:** Build - `./gradlew assembleDebug`
- **IF_FAIL:** Check Icons.Default.Stream import
- **EXPECTED:** Focus icon appears in daily task items

#### STEP 2.2: Add Navigation Callback to DailyScreen

**ACTION** `app/src/main/java/com/voxplanapp/ui/daily/DailyScreen.kt`
- **OPERATION:** Add onEnterFocusMode parameter to DailyScreen composable
- **LOCATION:** DailyScreen signature (around line 40)
- **CHANGE:**
  ```kotlin
  // BEFORE:
  @Composable
  fun DailyScreen(
      onNavigateToScheduler: (LocalDate) -> Unit,
      modifier: Modifier = Modifier,
      viewModel: DailyViewModel = viewModel(factory = AppViewModelProvider.Factory)
  ) {

  // AFTER:
  @Composable
  fun DailyScreen(
      onNavigateToScheduler: (LocalDate) -> Unit,
      onEnterFocusMode: (Int, Int) -> Unit,  // ADD THIS - goalId, eventId
      modifier: Modifier = Modifier,
      viewModel: DailyViewModel = viewModel(factory = AppViewModelProvider.Factory)
  ) {
  ```
- **AND** pass to DailyTaskItem in LazyColumn (around line 246):
  ```kotlin
  DailyTaskItem(
      task = task,
      quotaProgress = quotaProgress,
      actionMode = actionMode,
      onSchedule = { selectedTaskForScheduling = it },
      onDelete = { viewModel.deleteTask(it) },
      onReorder = {
          if (actionMode != ActionMode.Normal) {
              viewModel.reorderTask(task)
          }
      },
      onTaskComplete = { viewModel.toggleTaskCompletion(it) },
      onEnterFocusMode = { event ->  // ADD THIS
          onEnterFocusMode(event.goalId, event.id)
      },
      modifier = Modifier.fillMaxWidth()
  )
  ```
- **VALIDATE:** Build - `./gradlew assembleDebug`
- **IF_FAIL:** Check parameter names match
- **EXPECTED:** Compilation succeeds

#### STEP 2.3: Update VoxPlanNavHost Navigation

**ACTION** `app/src/main/java/com/voxplanapp/navigation/VoxPlanNavHost.kt`
- **OPERATION:** Add onEnterFocusMode callback to DailyScreen route
- **LOCATION:** Daily composable definition (around line 120)
- **CHANGE:**
  ```kotlin
  // Find the Daily screen composable (around line 115-135):
  composable(
      route = VoxPlanScreen.Daily.routeWithArgs,
      arguments = listOf(
          navArgument(VoxPlanScreen.Daily.dateArg) {
              type = NavType.StringType
          },
          navArgument(VoxPlanScreen.Daily.newEventIdArg) {
              type = NavType.StringType
              nullable = true
          }
      )
  ) {
      DailyScreen(
          onNavigateToScheduler = { date ->
              navController.navigate(VoxPlanScreen.DaySchedule.createRouteWithDate(date))
          },
          onEnterFocusMode = { goalId, eventId ->  // ADD THIS
              navController.navigate(
                  VoxPlanScreen.FocusMode.createRouteFromGoalAndEvent(goalId, eventId)
              )
          },
          modifier = modifier.padding(innerPadding)
      )
  }
  ```
- **VALIDATE:** Build and run - `./gradlew installDebug`
- **TEST STEPS:**
  1. Navigate to Daily screen
  2. Create or view daily task
  3. Tap Focus Mode icon (Stream icon)
  4. **VERIFY:** Navigate to FocusMode screen with correct goalId and eventId
  5. Complete focus session
  6. **VERIFY:** Navigate back to Daily screen
- **IF_FAIL:**
  - Check VoxPlanScreen.FocusMode.createRouteFromGoalAndEvent exists
  - Verify navigation parameters passed correctly
- **EXPECTED:** Seamless navigation Dailies ↔ FocusMode

#### STEP 2.4: Add "Currently Focusing" Indicator

**ACTION** `app/src/main/java/com/voxplanapp/ui/daily/DailyScreen.kt`
- **OPERATION:** Show indicator when task is actively being worked on in Focus Mode
- **CHANGE:**
  ```kotlin
  // In DailyViewModel, add current focus tracking:

  // Add after existing state (around line 55):
  private val _currentlyFocusingEventId = MutableStateFlow<Int?>(null)
  val currentlyFocusingEventId: StateFlow<Int?> = _currentlyFocusingEventId.asStateFlow()

  // Method to set/clear active focus (called from FocusViewModel integration)
  fun setCurrentlyFocusing(eventId: Int?) {
      _currentlyFocusingEventId.value = eventId
  }

  // In DailyScreen, collect state:
  val currentlyFocusingEventId by viewModel.currentlyFocusingEventId.collectAsState()

  // In DailyTaskItem, add indicator when focusing:
  Row(
      // ... existing row content
  ) {
      // After checkbox, before title:
      if (task.id == currentlyFocusingEventId) {
          Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.padding(start = 4.dp, end = 8.dp)
          ) {
              // Pulsing indicator
              Icon(
                  imageVector = Icons.Default.FiberManualRecord,
                  contentDescription = "Currently Focusing",
                  tint = Color.Red,
                  modifier = Modifier.size(12.dp)
              )
              Text(
                  text = "IN FOCUS",
                  style = MaterialTheme.typography.labelSmall,
                  color = Color.Red,
                  modifier = Modifier.padding(start = 4.dp)
              )
          }
      }

      // Existing title Text...
  }
  ```
- **IMPORTS TO ADD:**
  ```kotlin
  import androidx.compose.material.icons.filled.FiberManualRecord
  import androidx.compose.ui.graphics.Color
  ```
- **VALIDATE:** Build and run
- **TEST:** Start focus session from daily task, verify "IN FOCUS" indicator appears
- **IF_FAIL:** Check StateFlow collection and conditionals
- **EXPECTED:** Red pulsing dot + "IN FOCUS" text when task is active

#### STEP 2.5: Quick Focus Feature (Optional Enhancement)

**ACTION** `app/src/main/java/com/voxplanapp/ui/daily/DailyScreen.kt`
- **OPERATION:** Add "Quick Focus" button for unscheduled dailies
- **CHANGE:**
  ```kotlin
  // In DailyTaskItem, check if task has scheduled children:

  // Pass additional parameter:
  @Composable
  fun DailyTaskItem(
      task: Event,
      quotaProgress: QuotaProgress?,
      hasScheduledBlocks: Boolean,  // ADD THIS
      // ... other params
  ) {
      // Conditionally show Quick Focus vs Start Focus:
      IconButton(
          onClick = { onEnterFocusMode(task) },
          modifier = Modifier.size(32.dp)
      ) {
          Icon(
              imageVector = if (hasScheduledBlocks)
                  Icons.Default.Stream  // "Start Focus" for scheduled
              else
                  Icons.Default.PlayArrow,  // "Quick Focus" for unscheduled
              contentDescription = if (hasScheduledBlocks)
                  "Start Focus Mode"
              else
                  "Quick Focus (30 min)",
              modifier = Modifier.size(20.dp)
          )
      }
  }

  // In DailyScreen, calculate hasScheduledBlocks:
  items(
      items = uiState.dailyTasks,
      key = { task -> task.id }
  ) { task ->
      val quotaProgress = quotaProgresses[task.goalId]
      val hasScheduledBlocks = (task.scheduledDuration ?: 0) > 0  // Or query children

      DailyTaskItem(
          task = task,
          quotaProgress = quotaProgress,
          hasScheduledBlocks = hasScheduledBlocks,  // ADD THIS
          // ... other params
      )
  }
  ```
- **IMPORTS TO ADD:**
  ```kotlin
  import androidx.compose.material.icons.filled.PlayArrow
  ```
- **VALIDATE:** Build and run
- **TEST:** View unscheduled daily task, verify PlayArrow icon instead of Stream
- **IF_FAIL:** Check conditional logic and icon imports
- **EXPECTED:** Different icons for scheduled vs unscheduled tasks

---

### PHASE 3: Bulk Operations & Multi-Select

**Estimated Time:** 2-3 days

#### STEP 3.1: Add Selection State to DailyViewModel

**ACTION** `app/src/main/java/com/voxplanapp/ui/daily/DailyViewModel.kt`
- **OPERATION:** Add multi-select state management
- **CHANGE:**
  ```kotlin
  // Add after existing state (around line 59):

  // Multi-select state
  private val _selectedEventIds = MutableStateFlow<Set<Int>>(emptySet())
  val selectedEventIds: StateFlow<Set<Int>> = _selectedEventIds.asStateFlow()

  val isSelectionMode: StateFlow<Boolean> = _selectedEventIds
      .map { it.isNotEmpty() }
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), false)

  // Selection functions
  fun toggleSelection(eventId: Int) {
      _selectedEventIds.update { currentSet ->
          if (eventId in currentSet) {
              currentSet - eventId
          } else {
              currentSet + eventId
          }
      }
  }

  fun selectAll(allEventIds: List<Int>) {
      _selectedEventIds.value = allEventIds.toSet()
  }

  fun clearSelection() {
      _selectedEventIds.value = emptySet()
  }

  // Bulk operations
  suspend fun completeSelectedTasks() {
      val idsToComplete = _selectedEventIds.value.toList()
      eventRepository.bulkUpdateCompletion(idsToComplete, true)
      clearSelection()
  }

  suspend fun deleteSelectedTasks() {
      val idsToDelete = _selectedEventIds.value.toList()

      // Get events to check for children
      val events = idsToDelete.map { id ->
          eventRepository.getEvent(id)
      }

      // Delete children first, then parents
      events.forEach { event ->
          if (event.parentDailyId == null) {
              // Parent - delete children first
              val children = eventRepository.getEventsWithParentId(event.id).first()
              children.forEach { child -> eventRepository.deleteEvent(child.id) }
          }
          eventRepository.deleteEvent(event.id)
      }

      clearSelection()
  }
  ```
- **IMPORTS TO ADD:**
  ```kotlin
  import kotlinx.coroutines.flow.map
  import kotlinx.coroutines.flow.SharingStarted
  import kotlinx.coroutines.flow.stateIn
  import kotlinx.coroutines.flow.update
  ```
- **VALIDATE:** Build - `./gradlew assembleDebug`
- **IF_FAIL:** Check Flow imports and StateFlow operations
- **EXPECTED:** Successful compilation

#### STEP 3.2: Add Long-Press Detection to DailyTaskItem

**ACTION** `app/src/main/java/com/voxplanapp/ui/daily/DailyScreen.kt`
- **OPERATION:** Implement long-press to enter selection mode
- **CHANGE:**
  ```kotlin
  // Update DailyTaskItem with combinedClickable:

  @Composable
  fun DailyTaskItem(
      task: Event,
      // ... existing params
      isSelected: Boolean,  // ADD THIS
      isSelectionMode: Boolean,  // ADD THIS
      onLongPress: (Event) -> Unit,  // ADD THIS
      // ... other params
  ) {
      val haptics = LocalHapticFeedback.current

      Card(
          modifier = modifier
              .fillMaxWidth()
              .alpha(alpha)
              .combinedClickable(  // REPLACE .clickable with this
                  interactionSource = remember { MutableInteractionSource() },
                  indication = LocalIndication.current,
                  enabled = true,
                  onClick = {
                      if (isSelectionMode) {
                          onLongPress(task)  // In selection mode, click toggles selection
                      } else if (actionMode != ActionMode.Normal) {
                          onReorder()
                      }
                      // Otherwise, no-op (let icon buttons handle actions)
                  },
                  onLongClick = {
                      haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                      onLongPress(task)
                  },
                  onLongClickLabel = "Select task"
              ),
          elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
          colors = CardDefaults.cardColors(
              containerColor = if (isSelected) {
                  MaterialTheme.colorScheme.primaryContainer
              } else {
                  MaterialTheme.colorScheme.surface
              }
          )
      ) {
          Column(
              modifier = Modifier
                  .padding(horizontal = 16.dp, vertical = 8.dp)
                  .fillMaxWidth()
          ) {
              Row(
                  modifier = Modifier.fillMaxWidth(),
                  verticalAlignment = Alignment.CenterVertically
              ) {
                  // Show selection checkbox in selection mode
                  AnimatedVisibility(visible = isSelectionMode) {
                      Checkbox(
                          checked = isSelected,
                          onCheckedChange = null,  // Handled by card click
                          modifier = Modifier.padding(end = 8.dp)
                      )
                  }

                  // Existing content...
              }

              // Existing quota progress, etc...
          }
      }
  }
  ```
- **IMPORTS TO ADD:**
  ```kotlin
  import androidx.compose.foundation.combinedClickable
  import androidx.compose.foundation.interaction.MutableInteractionSource
  import androidx.compose.ui.hapticfeedback.HapticFeedbackType
  import androidx.compose.ui.platform.LocalHapticFeedback
  import androidx.compose.foundation.LocalIndication
  import androidx.compose.animation.AnimatedVisibility
  import androidx.compose.material3.Checkbox
  ```
- **VALIDATE:** Build - `./gradlew assembleDebug`
- **IF_FAIL:** Check imports, verify combinedClickable syntax
- **EXPECTED:** Long-press triggers haptic feedback

#### STEP 3.3: Update DailyScreen to Handle Selection Mode

**ACTION** `app/src/main/java/com/voxplanapp/ui/daily/DailyScreen.kt`
- **OPERATION:** Collect selection state and pass to items
- **CHANGE:**
  ```kotlin
  // In DailyScreen, collect selection state:
  val selectedEventIds by viewModel.selectedEventIds.collectAsState()
  val isSelectionMode by viewModel.isSelectionMode.collectAsState()

  // Add BackHandler for selection mode:
  BackHandler(enabled = isSelectionMode) {
      viewModel.clearSelection()
  }

  // Update items call in LazyColumn:
  items(
      items = uiState.dailyTasks,
      key = { task -> task.id }
  ) { task ->
      val quotaProgress = quotaProgresses[task.goalId]
      val hasScheduledBlocks = (task.scheduledDuration ?: 0) > 0
      val isSelected = task.id in selectedEventIds

      DailyTaskItem(
          task = task,
          quotaProgress = quotaProgress,
          hasScheduledBlocks = hasScheduledBlocks,
          actionMode = actionMode,
          isSelected = isSelected,  // ADD THIS
          isSelectionMode = isSelectionMode,  // ADD THIS
          onSchedule = { selectedTaskForScheduling = it },
          onDelete = { viewModel.deleteTask(it) },
          onReorder = {
              if (actionMode != ActionMode.Normal) {
                  viewModel.reorderTask(task)
              }
          },
          onTaskComplete = { viewModel.toggleTaskCompletion(it) },
          onEnterFocusMode = { event ->
              onEnterFocusMode(event.goalId, event.id)
          },
          onLongPress = { event ->  // ADD THIS
              viewModel.toggleSelection(event.id)
          },
          modifier = Modifier
              .fillMaxWidth()
              .animateItem()  // Smooth animations for reorder/remove
      )
  }
  ```
- **IMPORTS TO ADD:**
  ```kotlin
  import androidx.activity.compose.BackHandler
  import androidx.compose.foundation.lazy.LazyItemScope
  ```
- **VALIDATE:** Build and run
- **TEST:** Long-press on daily task, verify selection mode activates
- **IF_FAIL:** Check StateFlow collection and conditionals
- **EXPECTED:** Long-press enters selection mode, checkboxes appear

#### STEP 3.4: Add Contextual Action Bar for Selection Mode

**ACTION** `app/src/main/java/com/voxplanapp/ui/daily/DailyScreen.kt`
- **OPERATION:** Show action buttons during selection mode
- **LOCATION:** Top app bar area (around line 140)
- **CHANGE:**
  ```kotlin
  // Add after date navigation row, before daily tasks column:

  // Selection mode action bar
  AnimatedVisibility(visible = isSelectionMode) {
      Row(
          modifier = Modifier
              .fillMaxWidth()
              .background(MaterialTheme.colorScheme.primaryContainer)
              .padding(horizontal = 16.dp, vertical = 8.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
      ) {
          // Selection count
          Text(
              text = "${selectedEventIds.size} selected",
              style = MaterialTheme.typography.titleMedium,
              color = MaterialTheme.colorScheme.onPrimaryContainer
          )

          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              // Select All button
              TextButton(onClick = {
                  viewModel.selectAll(uiState.dailyTasks.map { it.id })
              }) {
                  Icon(Icons.Default.SelectAll, contentDescription = null)
                  Spacer(Modifier.width(4.dp))
                  Text("All")
              }

              // Complete All button
              IconButton(onClick = {
                  scope.launch {
                      viewModel.completeSelectedTasks()
                  }
              }) {
                  Icon(Icons.Default.CheckCircle, contentDescription = "Complete Selected")
              }

              // Delete button
              IconButton(onClick = {
                  showBulkDeleteDialog = true
              }) {
                  Icon(
                      Icons.Default.Delete,
                      contentDescription = "Delete Selected",
                      tint = MaterialTheme.colorScheme.error
                  )
              }

              // Clear selection button
              IconButton(onClick = { viewModel.clearSelection() }) {
                  Icon(Icons.Default.Close, contentDescription = "Clear Selection")
              }
          }
      }
  }
  ```
- **ADD STATE:**
  ```kotlin
  var showBulkDeleteDialog by remember { mutableStateOf(false) }
  val scope = rememberCoroutineScope()
  ```
- **IMPORTS TO ADD:**
  ```kotlin
  import androidx.compose.material.icons.filled.SelectAll
  import androidx.compose.material.icons.filled.CheckCircle
  import androidx.compose.material.icons.filled.Close
  import androidx.compose.material3.TextButton
  import androidx.compose.runtime.rememberCoroutineScope
  import kotlinx.coroutines.launch
  ```
- **VALIDATE:** Build and run
- **TEST:** Enter selection mode, verify action bar appears with buttons
- **IF_FAIL:** Check AnimatedVisibility and icon imports
- **EXPECTED:** Action bar slides in when selection mode active

#### STEP 3.5: Add Bulk Delete Confirmation Dialog

**ACTION** `app/src/main/java/com/voxplanapp/ui/daily/DailyScreen.kt`
- **OPERATION:** Show confirmation before bulk delete
- **LOCATION:** After other dialogs (around line 90)
- **CHANGE:**
  ```kotlin
  // Add bulk delete dialog:
  if (showBulkDeleteDialog) {
      AlertDialog(
          onDismissRequest = { showBulkDeleteDialog = false },
          title = { Text("Delete Selected Tasks") },
          text = {
              Text(
                  "Are you sure you want to delete ${selectedEventIds.size} tasks? " +
                  "This will also delete any scheduled time blocks."
              )
          },
          confirmButton = {
              TextButton(
                  onClick = {
                      scope.launch {
                          viewModel.deleteSelectedTasks()
                          showBulkDeleteDialog = false
                      }
                  },
                  colors = ButtonDefaults.textButtonColors(
                      contentColor = MaterialTheme.colorScheme.error
                  )
              ) {
                  Text("Delete")
              }
          },
          dismissButton = {
              TextButton(onClick = { showBulkDeleteDialog = false }) {
                  Text("Cancel")
              }
          }
      )
  }
  ```
- **VALIDATE:** Build and run
- **TEST:** Select multiple tasks, click delete, verify confirmation dialog
- **IF_FAIL:** Check dialog state and button callbacks
- **EXPECTED:** Confirmation dialog prevents accidental bulk deletes

#### STEP 3.6: Add Undo Snackbar for Bulk Delete (Optional)

**ACTION** `app/src/main/java/com/voxplanapp/ui/daily/DailyScreen.kt`
- **OPERATION:** Show undo option after bulk delete
- **CHANGE:**
  ```kotlin
  // Add to ViewModel:
  suspend fun deleteSelectedTasksWithUndo(): List<Event> {
      val idsToDelete = _selectedEventIds.value.toList()
      val deletedEvents = idsToDelete.map { id ->
          eventRepository.getEvent(id)
      }

      // Perform deletion
      deleteSelectedTasks()

      // Return deleted events for undo
      return deletedEvents
  }

  suspend fun restoreEvents(events: List<Event>) {
      events.forEach { event ->
          eventRepository.insertEvent(event)
      }
  }

  // In DailyScreen, add Snackbar support:
  val snackbarHostState = remember { SnackbarHostState() }

  Scaffold(
      snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
  ) { paddingValues ->
      // ... existing content
  }

  // In bulk delete confirmation onClick:
  scope.launch {
      val deletedEvents = viewModel.deleteSelectedTasksWithUndo()
      showBulkDeleteDialog = false

      val result = snackbarHostState.showSnackbar(
          message = "${deletedEvents.size} tasks deleted",
          actionLabel = "Undo",
          duration = SnackbarDuration.Long
      )

      when (result) {
          SnackbarResult.ActionPerformed -> {
              viewModel.restoreEvents(deletedEvents)
          }
          SnackbarResult.Dismissed -> {
              // Deletion confirmed
          }
      }
  }
  ```
- **IMPORTS TO ADD:**
  ```kotlin
  import androidx.compose.material3.SnackbarHost
  import androidx.compose.material3.SnackbarHostState
  import androidx.compose.material3.SnackbarResult
  import androidx.compose.material3.SnackbarDuration
  import androidx.compose.material3.Scaffold
  ```
- **VALIDATE:** Build and run
- **TEST:** Bulk delete tasks, tap "Undo" in snackbar, verify restoration
- **IF_FAIL:** Check SnackbarHostState and result handling
- **EXPECTED:** Undo successfully restores deleted tasks

---

### PHASE 4: Smart Suggestions & Empty States

**Estimated Time:** 2-3 days

#### STEP 4.1: Add Smart Suggestions Algorithm to DailyViewModel

**ACTION** `app/src/main/java/com/voxplanapp/ui/daily/DailyViewModel.kt`
- **OPERATION:** Implement suggestion generation logic
- **CHANGE:**
  ```kotlin
  // Add data class for suggestions:
  data class SuggestedDaily(
      val goalId: Int,
      val goalTitle: String,
      val reason: String,
      val priority: Int,  // Lower = higher priority
      val estimatedMinutes: Int?
  )

  // Add to DailyUiState:
  data class DailyUiState(
      val date: LocalDate = LocalDate.now(),
      val dailyTasks: List<Event> = emptyList(),
      val suggestedTasks: List<SuggestedDaily> = emptyList(),  // ADD THIS
      val isLoading: Boolean = true,
      val error: String? = null,
      val eventNeedingDuration: Int? = null
  )

  // Add after init block (around line 90):
  init {
      // ... existing init code

      // Load suggestions
      viewModelScope.launch {
          snapshotFlow { _uiState.value.date }
              .flatMapLatest { date ->
                  generateSuggestions(date)
              }
              .collect { suggestions ->
                  _uiState.value = _uiState.value.copy(suggestedTasks = suggestions)
              }
      }
  }

  private fun generateSuggestions(date: LocalDate): Flow<List<SuggestedDaily>> {
      return combine(
          quotaRepository.getAllActiveQuotas(date),
          timeBankRepository.getRecentGoals(date.minusDays(7), limit = 5),
          eventRepository.getDailiesForDate(date)
      ) { activeQuotas, recentGoalIds, existingDailies ->

          val suggestions = mutableListOf<SuggestedDaily>()
          val existingGoalIds = existingDailies.map { it.goalId }.toSet()

          // Priority 1: Active quotas for today
          activeQuotas.forEach { quota ->
              if (quota.goalId !in existingGoalIds) {
                  val goal = todoRepository.getItemStream(quota.goalId).first()
                  if (goal != null) {
                      suggestions.add(
                          SuggestedDaily(
                              goalId = quota.goalId,
                              goalTitle = goal.title,
                              reason = "Active quota: ${quota.dailyMinutes} mins",
                              priority = 1,
                              estimatedMinutes = quota.dailyMinutes
                          )
                      )
                  }
              }
          }

          // Priority 2: Incomplete quotas from yesterday
          val yesterday = date.minusDays(1)
          val yesterdayQuotas = quotaRepository.getAllActiveQuotas(yesterday).first()
          yesterdayQuotas.forEach { quota ->
              val timeBankEntries = timeBankRepository.getEntriesForDate(yesterday).first()
              val actualMinutes = timeBankEntries
                  .filter { it.goalId == quota.goalId }
                  .sumOf { it.duration }

              if (actualMinutes < quota.dailyMinutes && quota.goalId !in existingGoalIds) {
                  val goal = todoRepository.getItemStream(quota.goalId).first()
                  if (goal != null) {
                      val remaining = quota.dailyMinutes - actualMinutes
                      suggestions.add(
                          SuggestedDaily(
                              goalId = quota.goalId,
                              goalTitle = goal.title,
                              reason = "Yesterday: $remaining mins short",
                              priority = 2,
                              estimatedMinutes = remaining
                          )
                      )
                  }
              }
          }

          // Priority 3: Recently worked goals (past 7 days)
          recentGoalIds.forEach { goalId ->
              if (goalId !in existingGoalIds &&
                  suggestions.none { it.goalId == goalId }) {
                  val goal = todoRepository.getItemStream(goalId).first()
                  if (goal != null) {
                      suggestions.add(
                          SuggestedDaily(
                              goalId = goalId,
                              goalTitle = goal.title,
                              reason = "Recently active",
                              priority = 3,
                              estimatedMinutes = null
                          )
                      )
                  }
              }
          }

          suggestions.sortedBy { it.priority }.take(5)
      }
  }

  fun addSuggestedTask(suggested: SuggestedDaily) {
      viewModelScope.launch {
          val event = Event(
              goalId = suggested.goalId,
              title = suggested.goalTitle,
              startDate = _uiState.value.date,
              quotaDuration = suggested.estimatedMinutes,
              scheduledDuration = 0,
              completedDuration = 0
          )
          eventRepository.insertEvent(event)
      }
  }

  fun dismissSuggestion(suggested: SuggestedDaily) {
      _uiState.value = _uiState.value.copy(
          suggestedTasks = _uiState.value.suggestedTasks.filterNot { it.goalId == suggested.goalId }
      )
  }
  ```
- **VALIDATE:** Build - `./gradlew assembleDebug`
- **IF_FAIL:** Check Flow operations and repository methods
- **EXPECTED:** Suggestions generate based on quotas and history

#### STEP 4.2: Add Suggestions UI Section

**ACTION** `app/src/main/java/com/voxplanapp/ui/daily/DailyScreen.kt`
- **OPERATION:** Display suggested tasks above daily list
- **LOCATION:** Before LazyColumn with daily tasks (around line 220)
- **CHANGE:**
  ```kotlin
  // Add after date navigation, before daily tasks:

  // Suggested Tasks Section
  AnimatedVisibility(visible = uiState.suggestedTasks.isNotEmpty()) {
      Column(
          modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp, vertical = 8.dp)
      ) {
          Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
          ) {
              Text(
                  text = "Suggested Tasks",
                  style = MaterialTheme.typography.titleMedium,
                  color = MaterialTheme.colorScheme.primary
              )

              TextButton(onClick = {
                  uiState.suggestedTasks.forEach { suggestion ->
                      viewModel.addSuggestedTask(suggestion)
                  }
              }) {
                  Text("Add All")
              }
          }

          Spacer(Modifier.height(8.dp))

          // Suggested task cards
          uiState.suggestedTasks.forEach { suggestion ->
              SuggestedTaskCard(
                  suggestion = suggestion,
                  onAdd = { viewModel.addSuggestedTask(it) },
                  onDismiss = { viewModel.dismissSuggestion(it) }
              )
              Spacer(Modifier.height(8.dp))
          }
      }
  }

  // Add composable for suggested task card:
  @Composable
  fun SuggestedTaskCard(
      suggestion: SuggestedDaily,
      onAdd: (SuggestedDaily) -> Unit,
      onDismiss: (SuggestedDaily) -> Unit
  ) {
      Card(
          modifier = Modifier.fillMaxWidth(),
          elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
          colors = CardDefaults.cardColors(
              containerColor = MaterialTheme.colorScheme.surfaceVariant
          )
      ) {
          Row(
              modifier = Modifier
                  .fillMaxWidth()
                  .padding(12.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
          ) {
              Column(modifier = Modifier.weight(1f)) {
                  Text(
                      text = suggestion.goalTitle,
                      style = MaterialTheme.typography.bodyMedium,
                      fontWeight = FontWeight.Medium
                  )
                  Text(
                      text = suggestion.reason,
                      style = MaterialTheme.typography.bodySmall,
                      color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
              }

              Row {
                  IconButton(onClick = { onAdd(suggestion) }) {
                      Icon(
                          Icons.Default.Add,
                          contentDescription = "Add to dailies",
                          tint = MaterialTheme.colorScheme.primary
                      )
                  }
                  IconButton(onClick = { onDismiss(suggestion) }) {
                      Icon(
                          Icons.Default.Close,
                          contentDescription = "Dismiss suggestion"
                      )
                  }
              }
          }
      }
  }
  ```
- **IMPORTS TO ADD:**
  ```kotlin
  import androidx.compose.material.icons.filled.Add
  import androidx.compose.ui.text.font.FontWeight
  ```
- **VALIDATE:** Build and run
- **TEST:** View daily screen with active quotas, verify suggestions appear
- **IF_FAIL:** Check suggestion generation logic and UI rendering
- **EXPECTED:** Suggested tasks appear above daily list with Add/Dismiss buttons

#### STEP 4.3: Add Empty State Variants

**ACTION** `app/src/main/java/com/voxplanapp/ui/daily/DailyScreen.kt`
- **OPERATION:** Show contextual empty states
- **LOCATION:** Replace existing loading/empty state (around line 225)
- **CHANGE:**
  ```kotlin
  // Replace current empty state with:

  if (uiState.isLoading) {
      // Loading state
      Box(
          modifier = Modifier.fillMaxSize(),
          contentAlignment = Alignment.Center
      ) {
          CircularProgressIndicator()
      }
  } else if (uiState.error != null) {
      // Error state
      Box(
          modifier = Modifier
              .fillMaxSize()
              .padding(16.dp),
          contentAlignment = Alignment.Center
      ) {
          Text(
              text = uiState.error ?: "An error occurred",
              color = MaterialTheme.colorScheme.error,
              style = MaterialTheme.typography.bodyLarge,
              textAlign = TextAlign.Center
          )
      }
  } else if (uiState.dailyTasks.isEmpty()) {
      // Empty state - determine which variant
      val hasActiveQuotas = uiState.suggestedTasks.isNotEmpty()

      if (hasActiveQuotas) {
          // Variant 1: Has suggestions (first use or just cleared)
          EmptyStateWithSuggestions()
      } else {
          // Variant 2: No active quotas for today
          EmptyStateNoQuotas(
              onEditQuotas = { /* Navigate to goal edit */ },
              onAddManualTask = { /* Show add task dialog */ }
          )
      }
  } else {
      // Check if all tasks complete (success state)
      val allComplete = uiState.dailyTasks.all { it.isCompleted }

      if (allComplete && uiState.dailyTasks.isNotEmpty()) {
          // Variant 3: All done for today
          EmptyStateAllComplete(
              completedCount = uiState.dailyTasks.size,
              totalMinutes = uiState.dailyTasks.sumOf { it.completedDuration ?: 0 },
              onPlanTomorrow = { viewModel.updateDate(_uiState.value.date.plusDays(1)) }
          )
      } else {
          // Normal state - show task list
          LazyColumn(...) { /* existing content */ }
      }
  }

  // Add empty state composables:

  @Composable
  fun EmptyStateWithSuggestions() {
      Column(
          modifier = Modifier
              .fillMaxSize()
              .padding(32.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.Center
      ) {
          Icon(
              painter = painterResource(R.drawable.ic_clipboard),  // Or use Icons.Default.Assignment
              contentDescription = null,
              modifier = Modifier.size(80.dp),
              tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
          )

          Spacer(Modifier.height(24.dp))

          Text(
              text = "No daily tasks yet",
              style = MaterialTheme.typography.headlineSmall,
              textAlign = TextAlign.Center
          )

          Spacer(Modifier.height(8.dp))

          Text(
              text = "Add suggested tasks from your active quotas above, or create new ones.",
              style = MaterialTheme.typography.bodyMedium,
              textAlign = TextAlign.Center,
              color = MaterialTheme.colorScheme.onSurfaceVariant
          )
      }
  }

  @Composable
  fun EmptyStateNoQuotas(
      onEditQuotas: () -> Unit,
      onAddManualTask: () -> Unit
  ) {
      Column(
          modifier = Modifier
              .fillMaxSize()
              .padding(32.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.Center
      ) {
          Icon(
              imageVector = Icons.Default.CalendarToday,
              contentDescription = null,
              modifier = Modifier.size(80.dp),
              tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
          )

          Spacer(Modifier.height(24.dp))

          Text(
              text = "No active quotas for today",
              style = MaterialTheme.typography.headlineSmall,
              textAlign = TextAlign.Center
          )

          Spacer(Modifier.height(8.dp))

          Text(
              text = "Your quotas are scheduled for different days of the week.",
              style = MaterialTheme.typography.bodyMedium,
              textAlign = TextAlign.Center,
              color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          Spacer(Modifier.height(24.dp))

          Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
              OutlinedButton(onClick = onEditQuotas) {
                  Icon(Icons.Default.Edit, contentDescription = null)
                  Spacer(Modifier.width(8.dp))
                  Text("Edit Quotas")
              }

              Button(onClick = onAddManualTask) {
                  Icon(Icons.Default.Add, contentDescription = null)
                  Spacer(Modifier.width(8.dp))
                  Text("Add Task")
              }
          }
      }
  }

  @Composable
  fun EmptyStateAllComplete(
      completedCount: Int,
      totalMinutes: Int,
      onPlanTomorrow: () -> Unit
  ) {
      Column(
          modifier = Modifier
              .fillMaxSize()
              .padding(32.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.Center
      ) {
          Icon(
              imageVector = Icons.Default.EmojiEvents,  // Trophy icon
              contentDescription = null,
              modifier = Modifier.size(100.dp),
              tint = Color(0xFFFFC107)  // Gold color
          )

          Spacer(Modifier.height(24.dp))

          Text(
              text = "All done for today!",
              style = MaterialTheme.typography.headlineMedium,
              textAlign = TextAlign.Center
          )

          Spacer(Modifier.height(16.dp))

          Card(
              colors = CardDefaults.cardColors(
                  containerColor = MaterialTheme.colorScheme.primaryContainer
              )
          ) {
              Column(
                  modifier = Modifier.padding(20.dp),
                  horizontalAlignment = Alignment.CenterHorizontally
              ) {
                  Text(
                      text = "You completed $completedCount tasks",
                      style = MaterialTheme.typography.bodyLarge
                  )
                  Text(
                      text = "Total: $totalMinutes minutes focused",
                      style = MaterialTheme.typography.bodyMedium,
                      color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                  )
              }
          }

          Spacer(Modifier.height(24.dp))

          Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
              OutlinedButton(onClick = { /* Navigate to progress */ }) {
                  Icon(Icons.Default.Analytics, contentDescription = null)
                  Spacer(Modifier.width(8.dp))
                  Text("View Progress")
              }

              Button(onClick = onPlanTomorrow) {
                  Icon(Icons.Default.ArrowForward, contentDescription = null)
                  Spacer(Modifier.width(8.dp))
                  Text("Tomorrow")
              }
          }
      }
  }
  ```
- **IMPORTS TO ADD:**
  ```kotlin
  import androidx.compose.material.icons.filled.CalendarToday
  import androidx.compose.material.icons.filled.EmojiEvents
  import androidx.compose.material.icons.filled.Analytics
  import androidx.compose.material.icons.filled.ArrowForward
  import androidx.compose.material3.OutlinedButton
  import androidx.compose.ui.text.style.TextAlign
  ```
- **VALIDATE:** Build and run
- **TEST:**
  1. Empty daily list with suggestions → Variant 1
  2. Empty daily list, no quotas → Variant 2
  3. All tasks complete → Variant 3
- **IF_FAIL:** Check conditional logic for each variant
- **EXPECTED:** Appropriate empty state shows based on context

#### STEP 4.4: Add Daily Summary Screen (Optional)

**ACTION** Create new file `app/src/main/java/com/voxplanapp/ui/daily/DailySummaryDialog.kt`
- **OPERATION:** End-of-day summary with gamification
- **CHANGE:**
  ```kotlin
  package com.voxplanapp.ui.daily

  import androidx.compose.foundation.layout.*
  import androidx.compose.material.icons.Icons
  import androidx.compose.material.icons.filled.*
  import androidx.compose.material3.*
  import androidx.compose.runtime.Composable
  import androidx.compose.ui.Alignment
  import androidx.compose.ui.Modifier
  import androidx.compose.ui.graphics.Color
  import androidx.compose.ui.text.font.FontWeight
  import androidx.compose.ui.unit.dp
  import java.time.LocalDate
  import java.time.format.DateTimeFormatter

  data class DailySummaryData(
      val date: LocalDate,
      val tasksCompleted: Int,
      val totalTasks: Int,
      val minutesFocused: Int,
      val quotasMet: Int,
      val totalQuotas: Int,
      val medalsEarned: Int,
      val goalStreaks: List<GoalStreak>
  )

  data class GoalStreak(
      val goalTitle: String,
      val currentStreak: Int,
      val quotaMet: Boolean
  )

  @Composable
  fun DailySummaryDialog(
      summary: DailySummaryData,
      onDismiss: () -> Unit,
      onPlanTomorrow: () -> Unit
  ) {
      AlertDialog(
          onDismissRequest = onDismiss,
          title = {
              Column(horizontalAlignment = Alignment.CenterHorizontally) {
                  Text(
                      text = "Daily Summary",
                      style = MaterialTheme.typography.headlineMedium
                  )
                  Text(
                      text = summary.date.format(DateTimeFormatter.ofPattern("EEEE, MMM d")),
                      style = MaterialTheme.typography.bodyMedium,
                      color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
              }
          },
          text = {
              Column(
                  modifier = Modifier.fillMaxWidth(),
                  verticalArrangement = Arrangement.spacedBy(16.dp)
              ) {
                  // Overall stats
                  SummaryStatCard(
                      icon = Icons.Default.CheckCircle,
                      label = "Tasks Completed",
                      value = "${summary.tasksCompleted}/${summary.totalTasks}",
                      color = if (summary.tasksCompleted == summary.totalTasks)
                          Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary
                  )

                  SummaryStatCard(
                      icon = Icons.Default.Timer,
                      label = "Minutes Focused",
                      value = "${summary.minutesFocused}",
                      color = MaterialTheme.colorScheme.primary
                  )

                  SummaryStatCard(
                      icon = Icons.Default.Flag,
                      label = "Quotas Met",
                      value = "${summary.quotasMet}/${summary.totalQuotas}",
                      color = if (summary.quotasMet == summary.totalQuotas)
                          Color(0xFFFFC107) else MaterialTheme.colorScheme.primary
                  )

                  // Goal streaks
                  if (summary.goalStreaks.isNotEmpty()) {
                      Divider()

                      Text(
                          text = "Streaks",
                          style = MaterialTheme.typography.titleSmall,
                          fontWeight = FontWeight.Bold
                      )

                      summary.goalStreaks.forEach { streak ->
                          GoalStreakRow(streak)
                      }
                  }
              }
          },
          confirmButton = {
              Button(onClick = onPlanTomorrow) {
                  Icon(Icons.Default.ArrowForward, contentDescription = null)
                  Spacer(Modifier.width(8.dp))
                  Text("Plan Tomorrow")
              }
          },
          dismissButton = {
              TextButton(onClick = onDismiss) {
                  Text("Close")
              }
          }
      )
  }

  @Composable
  fun SummaryStatCard(
      icon: androidx.compose.ui.graphics.vector.ImageVector,
      label: String,
      value: String,
      color: Color
  ) {
      Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
      ) {
          Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(12.dp)
          ) {
              Icon(
                  imageVector = icon,
                  contentDescription = null,
                  tint = color,
                  modifier = Modifier.size(24.dp)
              )
              Text(
                  text = label,
                  style = MaterialTheme.typography.bodyMedium
              )
          }

          Text(
              text = value,
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = color
          )
      }
  }

  @Composable
  fun GoalStreakRow(streak: GoalStreak) {
      Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
      ) {
          Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
              Icon(
                  imageVector = Icons.Default.LocalFireDepartment,
                  contentDescription = null,
                  tint = Color(0xFFFF5722),
                  modifier = Modifier.size(20.dp)
              )
              Text(
                  text = streak.goalTitle,
                  style = MaterialTheme.typography.bodyMedium
              )
          }

          Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(4.dp)
          ) {
              Text(
                  text = "${streak.currentStreak}",
                  style = MaterialTheme.typography.bodyMedium,
                  fontWeight = FontWeight.Bold,
                  color = Color(0xFFFF5722)
              )
              if (streak.quotaMet) {
                  Icon(
                      imageVector = Icons.Default.Check,
                      contentDescription = "Quota met",
                      tint = Color(0xFF4CAF50),
                      modifier = Modifier.size(16.dp)
                  )
              }
          }
      }
  }
  ```
- **VALIDATE:** Build - `./gradlew assembleDebug`
- **USAGE:** Call from DailyScreen when user taps summary button or auto-show at 9 PM
- **EXPECTED:** Comprehensive daily summary with streaks and stats

---

### PHASE 5: Error Handling & Polish

**Estimated Time:** 1-2 days

#### STEP 5.1: Add Error Handling to addQuotaTasks

**ACTION** `app/src/main/java/com/voxplanapp/ui/daily/DailyViewModel.kt`
- **OPERATION:** Wrap quota operations in try-catch with user feedback
- **LOCATION:** addQuotaTasks function (lines 112-137)
- **CHANGE:**
  ```kotlin
  // BEFORE:
  fun addQuotaTasks() {
      viewModelScope.launch {
          val date = uiState.value.date
          val quotas = quotaRepository.getAllActiveQuotas(date).first()

          quotas.forEach { quota ->
              val goal = todoRepository.getItemStream(quota.goalId).first()
              if (goal != null) {
                  val event = Event(...)
                  eventRepository.insertEvent(event)
              }
          }
      }
  }

  // AFTER:
  fun addQuotaTasks() {
      viewModelScope.launch {
          try {
              val date = uiState.value.date
              val quotas = quotaRepository.getAllActiveQuotas(date).first()

              if (quotas.isEmpty()) {
                  _uiState.value = _uiState.value.copy(
                      error = "No active quotas found for ${date.dayOfWeek}. Check your quota settings."
                  )
                  return@launch
              }

              var successCount = 0
              var failCount = 0

              quotas.forEach { quota ->
                  try {
                      val goal = todoRepository.getItemStream(quota.goalId).first()
                      if (goal != null) {
                          val event = Event(
                              goalId = quota.goalId,
                              title = goal.title,
                              startDate = date,
                              quotaDuration = quota.dailyMinutes,
                              scheduledDuration = 0,
                              completedDuration = 0
                          )
                          eventRepository.insertEvent(event)
                          successCount++
                      } else {
                          Log.w("DailyViewModel", "Goal not found for quota: ${quota.goalId}")
                          failCount++
                      }
                  } catch (e: Exception) {
                      Log.e("DailyViewModel", "Error adding quota task: ${quota.goalId}", e)
                      failCount++
                  }
              }

              // Show result to user
              if (failCount > 0) {
                  _uiState.value = _uiState.value.copy(
                      error = "Added $successCount tasks. Failed to add $failCount tasks."
                  )
              } else {
                  // Success message shown via snackbar in UI
              }

          } catch (e: Exception) {
              Log.e("DailyViewModel", "Error in addQuotaTasks", e)
              _uiState.value = _uiState.value.copy(
                  error = "Failed to load quotas: ${e.message}"
              )
          }
      }
  }
  ```
- **IMPORTS TO ADD:**
  ```kotlin
  import android.util.Log
  ```
- **VALIDATE:** Build - `./gradlew assembleDebug`
- **TEST:** Try adding quotas with missing goals, verify error message
- **IF_FAIL:** Check try-catch blocks and error state updates
- **EXPECTED:** User-friendly error messages for failures

#### STEP 5.2: Add Snackbar for Success/Error Messages

**ACTION** `app/src/main/java/com/voxplanapp/ui/daily/DailyScreen.kt`
- **OPERATION:** Show snackbar for add quota tasks result
- **CHANGE:**
  ```kotlin
  // Add to DailyScreen state:
  val snackbarHostState = remember { SnackbarHostState() }
  val scope = rememberCoroutineScope()

  // Wrap content in Scaffold with Snackbar:
  Scaffold(
      snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
  ) { paddingValues ->
      Column(modifier = Modifier.padding(paddingValues)) {
          // ... existing content
      }
  }

  // In "Add Quota Tasks" button onClick:
  Button(onClick = {
      scope.launch {
          viewModel.addQuotaTasks()

          // Wait for completion and show result
          delay(500)  // Give time for operation to complete
          val error = viewModel.uiState.value.error

          if (error != null) {
              snackbarHostState.showSnackbar(
                  message = error,
                  duration = SnackbarDuration.Long
              )
              viewModel.clearError()
          } else {
              snackbarHostState.showSnackbar(
                  message = "Quota tasks added successfully",
                  duration = SnackbarDuration.Short
              )
          }
      }
  }) {
      Icon(Icons.Default.AddTask, contentDescription = null)
      Spacer(Modifier.width(8.dp))
      Text("Add Quota Tasks")
  }

  // Add clearError function to ViewModel:
  fun clearError() {
      _uiState.value = _uiState.value.copy(error = null)
  }
  ```
- **IMPORTS TO ADD:**
  ```kotlin
  import kotlinx.coroutines.delay
  ```
- **VALIDATE:** Build and run
- **TEST:** Add quota tasks, verify snackbar shows success or error
- **IF_FAIL:** Check snackbar state and message timing
- **EXPECTED:** Immediate feedback for user actions

#### STEP 5.3: Remove QuickScheduleScreen.kt

**ACTION** Delete file
- **OPERATION:** Remove broken commented-out code
- **FILE TO DELETE:** `app/src/main/java/com/voxplanapp/ui/main/QuickScheduleScreen.kt`
- **RATIONALE:** Entire file is commented out (lines 26-86), references undefined variables, functionality replaced by improved DailyScreen scheduling
- **VALIDATE:**
  1. Search codebase for imports of QuickScheduleScreen
  2. Remove any references
  3. Build - `./gradlew clean assembleDebug`
- **IF_FAIL:** Check for remaining references to QuickScheduleScreen
- **EXPECTED:** Clean build with no references to removed file

#### STEP 5.4: Add Performance Optimizations

**ACTION** `app/src/main/java/com/voxplanapp/ui/daily/DailyScreen.kt`
- **OPERATION:** Optimize LazyColumn rendering
- **CHANGE:**
  ```kotlin
  // Ensure stable keys (already added in Phase 1):
  items(
      items = uiState.dailyTasks,
      key = { task -> task.id }  // CRITICAL for performance
  ) { task ->
      // ... item content with animateItem() modifier
  }

  // Add derivedStateOf for expensive computations:
  val quotaProgresses by remember {
      derivedStateOf {
          // Cache quota progress calculations
          uiState.dailyTasks.associate { task ->
              task.goalId to calculateQuotaProgress(task)
          }
      }
  }

  // Debounce quota progress updates in ViewModel:
  private val quotaProgressFlow = snapshotFlow { _uiState.value.dailyTasks }
      .debounce(300)  // 300ms debounce
      .flatMapLatest { tasks ->
          combine(tasks.map { task ->
              quotaRepository.getQuotaForGoal(task.goalId)
          }) { quotas -> quotas.toList() }
      }
  ```
- **IMPORTS TO ADD:**
  ```kotlin
  import androidx.compose.runtime.derivedStateOf
  import kotlinx.coroutines.flow.debounce
  ```
- **VALIDATE:** Profile with Android Studio Profiler
- **TEST:** Scroll through 50+ daily tasks, verify 60fps
- **IF_FAIL:** Add more aggressive debouncing or caching
- **EXPECTED:** Smooth performance even with large lists

#### STEP 5.5: Add Accessibility Improvements

**ACTION** Throughout DailyScreen.kt and DailyTaskItem
- **OPERATION:** Enhance screen reader support
- **CHANGE:**
  ```kotlin
  // Add semantic modifiers to key interactive elements:

  // Checkbox:
  IconButton(
      onClick = { onTaskComplete(task) },
      modifier = Modifier
          .size(32.dp)
          .semantics {
              contentDescription = if (task.isCompleted)
                  "Mark ${task.title} as incomplete"
              else
                  "Mark ${task.title} as complete"
              role = Role.Checkbox
          }
  ) {
      // ... icon content
  }

  // Focus button:
  IconButton(
      onClick = { onEnterFocusMode(task) },
      modifier = Modifier
          .size(32.dp)
          .semantics {
              contentDescription = "Start focus session for ${task.title}"
          }
  ) {
      // ... icon content
  }

  // Schedule button:
  IconButton(
      onClick = { onSchedule(task) },
      modifier = Modifier
          .size(32.dp)
          .semantics {
              contentDescription = "Schedule ${task.title} to time block"
          }
  ) {
      // ... icon content
  }

  // Card in selection mode:
  Card(
      modifier = modifier
          .fillMaxWidth()
          .semantics {
              if (isSelectionMode) {
                  stateDescription = if (isSelected) "Selected" else "Not selected"
                  role = Role.Checkbox
              }
          }
  ) {
      // ... card content
  }
  ```
- **IMPORTS TO ADD:**
  ```kotlin
  import androidx.compose.ui.semantics.semantics
  import androidx.compose.ui.semantics.contentDescription
  import androidx.compose.ui.semantics.stateDescription
  import androidx.compose.ui.semantics.Role
  ```
- **VALIDATE:** Test with TalkBack enabled on Android device
- **TEST:** Navigate daily screen with screen reader, verify all actions announced
- **IF_FAIL:** Add more semantic information to complex UI elements
- **EXPECTED:** Full screen reader support with clear announcements

---

### PHASE 6: Testing & Documentation

**Estimated Time:** 1-2 days

#### STEP 6.1: Write Migration Test

**ACTION** Create `app/src/androidTest/java/com/voxplanapp/data/MigrationTest.kt`
- **OPERATION:** Automated test for database migration 13→14
- **CHANGE:**
  ```kotlin
  package com.voxplanapp.data

  import androidx.room.Room
  import androidx.room.testing.MigrationTestHelper
  import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
  import androidx.test.ext.junit.runners.AndroidJUnit4
  import androidx.test.platform.app.InstrumentationRegistry
  import android.content.ContentValues
  import android.database.sqlite.SQLiteDatabase
  import org.junit.Rule
  import org.junit.Test
  import org.junit.runner.RunWith
  import org.hamcrest.MatcherAssert.assertThat
  import org.hamcrest.CoreMatchers.`is`
  import java.io.IOException

  @RunWith(AndroidJUnit4::class)
  class MigrationTest {
      companion object {
          private const val TEST_DB = "migration-test"
      }

      @get:Rule
      val helper: MigrationTestHelper = MigrationTestHelper(
          InstrumentationRegistry.getInstrumentation(),
          AppDatabase::class.java.canonicalName,
          FrameworkSQLiteOpenHelperFactory()
      )

      @Test
      @Throws(IOException::class)
      fun migrate13To14_addsCompletionFields() {
          // Create database at version 13
          var db = helper.createDatabase(TEST_DB, 13)

          // Insert test Event record (without completion fields)
          val values = ContentValues().apply {
              put("id", 1)
              put("goalId", 5)
              put("title", "Test Daily Task")
              put("startTime", null)
              put("endTime", null)
              put("startDate", 19000) // Some epoch day value
              put("recurrenceType", "NONE")
              put("recurrenceInterval", 0)
              put("recurrenceEndDate", null)
              put("color", null)
              put("order", 0)
              put("quotaDuration", 60)
              put("scheduledDuration", 0)
              put("completedDuration", 0)
              put("parentDailyId", null)
          }
          db.insert("Event", SQLiteDatabase.CONFLICT_REPLACE, values)
          db.close()

          // Run migration to version 14
          db = helper.runMigrationsAndValidate(TEST_DB, 14, true, AppDatabase.MIGRATION_13_14)

          // Verify the migration
          val cursor = db.query("SELECT * FROM Event WHERE id = ?", arrayOf("1"))
          assertThat(cursor.moveToFirst(), `is`(true))

          // Verify isCompleted column exists and has default value 0
          val isCompletedIndex = cursor.getColumnIndex("isCompleted")
          assertThat(isCompletedIndex >= 0, `is`(true))
          val isCompleted = cursor.getInt(isCompletedIndex)
          assertThat(isCompleted, `is`(0)) // Default false

          // Verify completedAt column exists and is null
          val completedAtIndex = cursor.getColumnIndex("completedAt")
          assertThat(completedAtIndex >= 0, `is`(true))
          val completedAt = cursor.getLong(completedAtIndex)
          assertThat(completedAt, `is`(0L)) // NULL stored as 0

          cursor.close()
          db.close()
      }
  }
  ```
- **ADD TEST DEPENDENCY:** In `app/build.gradle.kts`:
  ```kotlin
  androidTestImplementation("androidx.room:room-testing:2.6.1")
  ```
- **VALIDATE:** Run test - `./gradlew connectedAndroidTest`
- **IF_FAIL:** Check schema export in `app/schemas/14.json`
- **EXPECTED:** Test passes, migration verified

#### STEP 6.2: Manual Testing Checklist

**ACTION** Test complete feature on real device/emulator
- **OPERATION:** Systematic testing of all phases
- **TEST SCENARIOS:**

**Scenario 1: Basic Completion Flow**
1. Create daily task from quota
2. Mark complete with checkbox
3. Verify: fade + strikethrough animation
4. Mark incomplete
5. Verify: restore original appearance

**Scenario 2: Parent-Child Completion**
1. Create daily task
2. Schedule it (creates child event)
3. Mark daily complete
4. Verify: Dialog appears
5. Choose "Complete All"
6. Verify: Both parent and child marked complete
7. Repeat steps 1-3, choose "Daily Only"
8. Verify: Only parent marked complete

**Scenario 3: Focus Mode Integration**
1. Create daily task
2. Tap Focus Mode icon (Stream)
3. Verify: Navigate to FocusMode
4. Complete short focus session
5. Return to Dailies
6. Verify: "IN FOCUS" indicator appears during session

**Scenario 4: Multi-Select & Bulk Operations**
1. Long-press daily task
2. Verify: Selection mode activates, checkboxes appear
3. Select 3 tasks
4. Verify: Action bar shows "3 selected"
5. Tap "Complete All"
6. Verify: All 3 marked complete
7. Select 2 tasks, tap Delete
8. Verify: Confirmation dialog, then deletion

**Scenario 5: Smart Suggestions**
1. View Dailies for today
2. Verify: Suggestions appear based on active quotas
3. Tap "+ icon on suggestion
4. Verify: Task added to dailies
5. Tap "Add All"
6. Verify: All suggestions added

**Scenario 6: Empty States**
1. Clear all daily tasks
2. Verify: Empty state with instructions
3. Complete all tasks
4. Verify: "All done" celebration screen
5. Change to day with no active quotas
6. Verify: "No quotas" empty state

**Scenario 7: Error Handling**
1. Delete all goals with quotas
2. Try "Add Quota Tasks"
3. Verify: Snackbar shows error message

- **DOCUMENT RESULTS:** Create test report with screenshots for each scenario
- **IF_FAIL:** Fix bugs and re-test
- **EXPECTED:** All scenarios pass with no crashes

#### STEP 6.3: Update Documentation

**ACTION** Update relevant documentation files
- **OPERATION:** Reflect completed features
- **FILES TO UPDATE:**

**1. INCOMPLETE_FEATURES.md:**
- Remove completed items from "What's Missing" sections
- Update completeness percentages (70% → 95%+)
- Mark Dailies as "Production Ready"

**2. CLAUDE.md:**
- Update "Current Version" section
- Add notes about completion tracking
- Document new navigation flows

**3. Create CHANGELOG entry:**
```markdown
## Version 3.3 - Dailies Completion Feature

### Added
- ✅ Completion tracking with checkboxes for daily tasks
- ✅ Visual feedback (fade + strikethrough) for completed tasks
- ✅ Parent-child completion logic with confirmation dialogs
- ✅ Direct Focus Mode access from daily tasks
- ✅ Bulk operations (multi-select with long-press)
- ✅ Smart task suggestions based on quotas and history
- ✅ Empty states (first use, all complete, no quotas)
- ✅ Swipe gestures for quick actions (optional)
- ✅ Daily summary screen with streaks (optional)

### Fixed
- ✅ Error handling for quota lookup failures
- ✅ Removed broken QuickScheduleScreen.kt

### Changed
- Database schema updated (v13 → v14) with completion fields
- Daily screen UI redesigned for better action access
- Improved performance with stable keys and debouncing

### Migration Notes
- Existing events will have isCompleted=false by default
- No data loss during upgrade
```

**4. Add to PRPs folder:**
- Copy this PRP document to `.claude/PRPs/dailies-completion-feature.md` for future reference

- **VALIDATE:** Review documentation for accuracy
- **EXPECTED:** Complete documentation of new features

---

## Validation Strategy

### Build Validation (After Each Phase)
```bash
# Clean build
./gradlew clean assembleDebug

# Run lint
./gradlew lint

# Check for warnings
./gradlew build --warning-mode all
```

**Success Criteria:**
- No compilation errors
- No new lint warnings
- No deprecated API usage

### Migration Testing (Phase 1)
```bash
# Run migration tests
./gradlew connectedAndroidTest

# Manual device testing:
adb install app/build/outputs/apk/debug/app-debug.apk
adb shell
run-as com.voxplanapp
sqlite3 databases/todo-db
PRAGMA table_info(Event);
# Should show isCompleted INTEGER and completedAt INTEGER columns
```

**Success Criteria:**
- Automated test passes
- Existing events have isCompleted=0
- No crashes on app upgrade

### Integration Testing (After Phase 3)
```bash
# Install on device with existing data
./gradlew installDebug

# Monitor logs
adb logcat -s DailyViewModel EventRepository FocusViewModel
```

**Test Flows:**
1. Dailies → FocusMode → back to Dailies
2. Complete parent → confirm complete children
3. Multi-select → bulk delete → undo
4. Add suggested tasks → schedule → focus

**Success Criteria:**
- No navigation errors
- State preserved across screens
- Undo functionality works correctly

### Performance Testing (Phase 5)
```bash
# Use Android Studio Profiler
# Create 50+ daily tasks
# Profile:
# - Frame rate (should maintain 60fps)
# - Memory usage (no leaks)
# - Database query count (optimized)
```

**Success Criteria:**
- 60fps maintained during scrolling
- No memory leaks from Flow collection
- Database queries debounced appropriately

### Accessibility Testing (Phase 5)
- Enable TalkBack
- Navigate Dailies screen with gestures
- Verify all actions announced
- Check color contrast ratios

**Success Criteria:**
- All interactive elements have contentDescription
- Selection state announced correctly
- Navigation logical with screen reader

---

## Rollback Strategy

### Phase-by-Phase Rollback

**If Phase 1 Fails (Database Migration):**
```bash
# Uninstall app (wipes database)
adb uninstall com.voxplanapp

# Or manually rollback migration:
# 1. Remove MIGRATION_13_14 from AppDatabase.kt
# 2. Revert version back to 13
# 3. Rebuild and reinstall
```

**If Phase 2 Fails (Focus Mode Integration):**
```bash
# Remove onEnterFocusMode callback from:
# - DailyScreen.kt
# - DailyTaskItem.kt
# - VoxPlanNavHost.kt

# Keep Phase 1 completion tracking (independent feature)
```

**If Phase 3 Fails (Bulk Operations):**
```bash
# Comment out:
# - Selection state in DailyViewModel
# - Multi-select UI in DailyScreen
# - Contextual action bar

# Keep Phase 1-2 (completion + focus access)
```

**If Phase 4 Fails (Smart Suggestions):**
```bash
# Remove:
# - Suggestion generation in DailyViewModel
# - Suggested tasks UI section

# Keep all previous phases (core functionality intact)
```

**Complete Rollback:**
```bash
# Revert all changes
git diff HEAD --name-only | xargs git checkout

# Clean rebuild
./gradlew clean build

# Note: Users who upgraded to v14 database will need app reinstall
```

**Database Downgrade (Emergency):**
```sql
-- If users report issues after migration:
-- Create MIGRATION_14_13 to remove columns:
val MIGRATION_14_13 = object : Migration(14, 13) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // SQLite doesn't support DROP COLUMN directly
        // Must recreate table:
        database.execSQL("""
            CREATE TABLE Event_backup AS
            SELECT id, goalId, title, startTime, endTime, startDate,
                   recurrenceType, recurrenceInterval, recurrenceEndDate,
                   color, `order`, quotaDuration, scheduledDuration,
                   completedDuration, parentDailyId
            FROM Event
        """)
        database.execSQL("DROP TABLE Event")
        database.execSQL("ALTER TABLE Event_backup RENAME TO Event")
    }
}
```

---

## Success Criteria

### Functional Requirements
- [ ] Users can mark daily tasks complete/incomplete with checkbox
- [ ] Completed tasks show visual feedback (fade + strikethrough)
- [ ] Completing parent daily with children shows confirmation dialog
- [ ] All children complete triggers parent completion suggestion
- [ ] Direct Focus Mode access from daily tasks via icon button
- [ ] "IN FOCUS" indicator shows when task is active
- [ ] Long-press enters multi-select mode with checkboxes
- [ ] Bulk operations (Complete All, Delete All) work correctly
- [ ] Swipe right marks complete, swipe left deletes
- [ ] Smart suggestions appear based on quotas and history
- [ ] "Add All" button adds all suggested tasks
- [ ] Empty states show appropriate variant (first use, all done, no quotas)
- [ ] Error messages show for quota lookup failures
- [ ] QuickScheduleScreen.kt removed from codebase

### Non-Functional Requirements
- [ ] Database migration 13→14 succeeds without data loss
- [ ] App maintains 60fps scrolling with 50+ daily tasks
- [ ] No memory leaks from Flow collection
- [ ] All interactive elements have 48dp touch targets
- [ ] Screen reader announces all actions correctly
- [ ] Build produces no new lint warnings
- [ ] Existing tests continue to pass
- [ ] New migration test passes

### User Experience
- [ ] Checkbox toggle feels responsive (<100ms)
- [ ] Completion animation smooth and satisfying
- [ ] Selection mode entry clear (haptic + visual feedback)
- [ ] Focus Mode navigation seamless (no lag)
- [ ] Suggestions relevant and accurate
- [ ] Empty states helpful and delightful
- [ ] Error messages clear and actionable

### Code Quality
- [ ] Follows existing VoxPlanApp patterns (Colors, TextStyles, etc.)
- [ ] Uses Material Design 3 components correctly
- [ ] StateFlow management consistent with other ViewModels
- [ ] No code duplication between Daily/Main/Goal screens
- [ ] Proper error handling with try-catch blocks
- [ ] Logging added for debugging (Log.d, Log.e)
- [ ] Comments explain complex logic (parent-child relationships)

---

## Files Modified

### Data Layer (5 files)
1. `app/src/main/java/com/voxplanapp/data/Event.kt` - Add isCompleted, completedAt fields
2. `app/src/main/java/com/voxplanapp/data/AppDatabase.kt` - MIGRATION_13_14, version 14
3. `app/src/main/java/com/voxplanapp/data/EventDao.kt` - Completion queries
4. `app/src/main/java/com/voxplanapp/data/EventRepository.kt` - Completion methods
5. `app/src/main/java/com/voxplanapp/data/AppContainer.kt` - Register migration

### UI Layer (2 files)
6. `app/src/main/java/com/voxplanapp/ui/daily/DailyViewModel.kt` - Completion, selection, suggestions logic
7. `app/src/main/java/com/voxplanapp/ui/daily/DailyScreen.kt` - UI for all features

### Navigation (1 file)
8. `app/src/main/java/com/voxplanapp/navigation/VoxPlanNavHost.kt` - Focus Mode routing

### New Files (2 optional)
9. `app/src/main/java/com/voxplanapp/ui/daily/DailySummaryDialog.kt` (optional)
10. `app/src/androidTest/java/com/voxplanapp/data/MigrationTest.kt` (recommended)

### Deleted Files (1 file)
11. `app/src/main/java/com/voxplanapp/ui/main/QuickScheduleScreen.kt` ❌ REMOVE

### Documentation (3 files)
12. `docs/INCOMPLETE_FEATURES.md` - Update completion status
13. `CLAUDE.md` - Add feature documentation
14. `CHANGELOG.md` (create if doesn't exist)

---

## Dependencies

### No New External Dependencies Required
All features use existing libraries:
- ✅ Room 2.6.1 (already present)
- ✅ Compose BOM 2023.08.00 (already present)
- ✅ Material3 (already present)
- ✅ kotlinx.datetime (already present)
- ✅ Kotlin Coroutines (already present)

### New Test Dependencies (Optional)
```kotlin
// In app/build.gradle.kts:
dependencies {
    // ... existing dependencies

    // For migration testing:
    androidTestImplementation("androidx.room:room-testing:2.6.1")
}
```

---

## Notes

### Design Decisions Rationale

**1. Why Checkbox Pattern?**
- Industry standard (all major apps use it)
- Prevents accidental completion vs tap-for-details
- Accessible (screen reader friendly)
- Familiar to users

**2. Why Parent-Child Confirmation Dialog?**
- Prevents accidental cascading completion
- Gives user control over scheduled blocks
- Maintains data integrity with time banking
- Follows existing delete confirmation pattern

**3. Why Multi-Select with Long-Press?**
- Material Design standard activation method
- Doesn't interfere with normal tap interactions
- Haptic feedback provides clear entry signal
- Matches user expectations from other Android apps

**4. Why Smart Suggestions vs Auto-Add?**
- Respects user agency (no surprise tasks)
- Allows daily curation and focus
- Prevents overwhelm on busy days
- Aligns with Microsoft To Do "My Day" pattern

**5. Why Fade-and-Stay vs Immediate Hide?**
- Provides undo safety window
- Shows progress/satisfaction during work session
- Prevents disorientation from disappearing items
- Aligns with Things 3 best practice

### Implementation Notes

**Database Migration:**
- Version 13→14 is safe for existing users
- Default isCompleted=0 ensures all existing events appear incomplete
- No data loss or transformation required

**Performance:**
- Stable keys in LazyColumn critical for animations
- Debouncing quota calculations prevents excessive recomposition
- Flow combination with WhileSubscribed(5000L) balances reactivity and efficiency

**Accessibility:**
- All icon buttons have contentDescription
- Selection mode uses semantic Role.Checkbox
- Screen readers announce completion state changes
- Touch targets exceed 48dp minimum

**Testing:**
- Migration test prevents regression on database changes
- Manual test checklist covers all user flows
- Performance profiling ensures smooth experience
- Accessibility testing with TalkBack validates usability

### Future Enhancements (Post-Implementation)

**Notifications:**
- Scheduled daily reminders (requires Android notification setup)
- Daily planning ritual trigger at configurable time
- Streak maintenance alerts

**Recurring Tasks:**
- Leverage existing recurrenceType field in Event entity
- Daily, weekly, monthly patterns
- Quota-based recurring task templates

**Advanced Suggestions:**
- Machine learning for optimal task timing
- Deadline-aware prioritization
- Workload balancing across days

**Export/Import:**
- JSON export of daily tasks and quotas
- Calendar format export (ICS)
- Backup/restore functionality

**Analytics:**
- Completion rate trends
- Optimal productivity hours (from TimeBank data)
- Quota achievement patterns

### Known Limitations

**Current Implementation:**
- No notification system (requires additional Android permissions)
- No recurrence support (Event model supports it, but no UI)
- Suggestions don't consider calendar conflicts
- No undo for individual task completion (only bulk operations)
- QuickScheduleScreen removed (not fixed) - functionality in DailyScreen

**Acceptable Trade-offs:**
- Basic suggestion algorithm (vs ML-based predictions)
- Manual daily curation (vs fully automated)
- Single-day view (no week/month view)
- Limited to 5 suggestions (prevents UI overwhelm)

---

## Confidence Level: 8.5/10

**High Confidence Because:**
- ✅ All patterns researched and validated against industry standards
- ✅ Existing codebase conventions thoroughly documented
- ✅ Database migration pattern proven in MIGRATION_12_13
- ✅ UI patterns consistent with GoalItem, MainScreen implementations
- ✅ External research from 4 specialized subagents
- ✅ Material Design 3 compliance verified
- ✅ Room migration best practices followed

**Slight Uncertainty Because:**
- ⚠️ Smart suggestions algorithm needs tuning based on real usage data
- ⚠️ Parent-child completion logic complex (requires thorough testing)
- ⚠️ Performance with 100+ daily tasks untested (assumed based on patterns)
- ⚠️ Swipe gestures may conflict with horizontal scrolling in some layouts

**Risk Mitigation:**
- Phased implementation allows early detection of issues
- Rollback strategy defined for each phase
- Comprehensive testing before each phase completion
- Manual DI pattern makes debugging easier than Hilt

---

**Implementation Timeline:** 4 weeks (6 phases)

**Next Steps:**
1. Create beads issues for each phase
2. Begin Phase 1 (Database Migration)
3. Test migration on development device
4. Proceed to Phase 2 after Phase 1 validation

**End of PRP**
