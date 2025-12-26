# BASE PRP: Dailies Screen - Dual-Task System Implementation

## Task Overview

Complete the Dailies feature by implementing a **dual-task system** that distinguishes between:
1. **Quota-based tasks** (ongoing processes) - measured by time spent, no completion checkbox
2. **Singleton tasks** (one-off tasks) - binary completion status with auto-rollover

Transform the Dailies screen from a basic task list (70% complete) into a production-ready daily planning interface with completion tracking, direct Focus Mode access, and task persistence.

**Priority Objectives:**
1. ✅ Data model updates (isFromQuota, isCompleted fields)
2. ✅ Persistence via lazy-initialization
3. ✅ Dual completion tracking (checkbox for singletons, progress for quotas)
4. ✅ Direct Focus Mode access from daily tasks
5. ✅ Singleton task creation (ad-hoc tasks without quotas)
6. ✅ Auto-rollover for incomplete singleton tasks

**Current State:** Dailies Beta (VP 3.2) - Can create daily tasks from quotas and schedule them, but missing completion tracking, direct Focus Mode access, and persistence.

**Target State:** Production-ready daily planning interface with dual-task system that handles both ongoing quota-based work and one-off singleton tasks.

---

## Context

### The Dual-Task System Philosophy

VoxPlanApp recognizes two fundamentally different types of tasks:

**1. Quota-Based Tasks (Ongoing Processes)**
- **Purpose:** Long-term habits and priorities (e.g., "Programming 2h/day Mon-Fri")
- **Measurement:** Time spent toward daily quota
- **Completion:** No binary "done" state - measured by progress toward quota
- **Lifecycle:** Reset each day, tied to specific date's quota
- **UI:** Progress boxes (green = completed time, orange = scheduled, gray = remaining)
- **Example:** "Programming - 2h quota" → user spends 2h in Focus Mode → 🟩🟩 (quota met)

**2. Singleton Tasks (One-Off Tasks)**
- **Purpose:** Discrete tasks with definite endpoints (e.g., "Doctor appointment", "Fix bug #123")
- **Measurement:** Binary completion status (done/not done)
- **Completion:** User checks off when finished (takes as long as it takes)
- **Lifecycle:** Auto-rollover if incomplete at end of day, drop off when completed
- **UI:** Checkbox (☐ → ☑), strikethrough when done
- **Example:** "Dentist appointment" → user attends → checks box → task disappears

### User's Clarification (Key Requirements)

> "Some tasks are ongoing processes that the user will want to come back to day after day, and the accomplishment of that task is measured by whether or not the requisite amount of time has been spent on that category of task. That is one of the main functions that VoxPlan has."

> "Quite separate to that is the idea of single tasks which have a very definite end point. They are less time-based, they take as long as they take, but once they're finished, they're finished and then they can drop off the screen."

> "If something has a quota then we can certainly assume that there's not a definite end point... And then if something doesn't have a quota, we would expect that it is a singleton task which has two potential statuses: either accomplished or not accomplished."

**Rollover Logic:**
> "With tasks that have a definite completion or non-completion status, they should be automatically rolled over to the next day. However, with quota-based tasks, once the day is complete, there's no chance of fulfilling that quota for that day. So there's no rollover possible."

**Creation Method:**
> "Manual creation (keep current 'Add Quota Tasks' button)"

### Architecture Context

**Current Event Entity** (`/Users/richardthompson/StudioProjects/VoxPlanApp/app/src/main/java/com/voxplanapp/data/Event.kt`):
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
    // ❌ MISSING: isFromQuota, isCompleted, completedAt fields
)
```

**Parent-Child Pattern (Already Implemented):**
- **Parent Daily Task:** `parentDailyId = null`, `startTime/endTime = null`
- **Scheduled Child Event:** `parentDailyId = dailyId`, `startTime/endTime` populated

**Database:** Room 2.6.1, current version 13, schema exports in `app/schemas/`

**Existing Patterns to Follow:**

**Completion Pattern (from TodoItem.kt):**
```kotlin
@Entity
data class TodoItem(
    // ...
    var isDone: Boolean = false,
    var completedDate: LocalDate? = null
)
```

**Checkbox UI (from GoalItem.kt lines 286-299):**
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

**Completion Visual Feedback (from GoalItem.kt):**
```kotlin
val textColor = if (goal.goal.completedDate != null)
    TodoItemTextColor.copy(alpha = 0.5f) else TodoItemTextColor
val textDecoration = if (goal.goal.completedDate != null)
    TextDecoration.LineThrough else null
```

### Gotchas

**Issue 1: Boolean Storage in SQLite**
- SQLite has no native boolean type
- Room maps Kotlin `Boolean` to SQLite `INTEGER` (0=false, 1=true)
- Migration SQL: `ALTER TABLE Event ADD COLUMN isCompleted INTEGER NOT NULL DEFAULT 0`
- **Never use "false"/"true" in defaultValue** - must use "0"/"1"

**Issue 2: Nullable Fields in Migration**
- Nullable fields (e.g., `completedAt`) can be added without DEFAULT
- Non-nullable fields MUST have DEFAULT value or migration fails
- Pattern: `ALTER TABLE Event ADD COLUMN completedAt INTEGER` (no DEFAULT needed)

**Issue 3: Quota Duration Null Check**
- Quota-based tasks: `quotaDuration != null` (from quota)
- Singleton tasks: `quotaDuration == null` (user-specified or no duration)
- Can also use `isFromQuota` boolean for explicit check

**Issue 4: Rollover Timing**
- Auto-rollover should happen when viewing "today" if previous day has incomplete singletons
- Don't rollover quota-based tasks (tied to specific date)
- Check on DailyViewModel initialization or first load of "today"

**Issue 5: Focus Mode Navigation**
- Focus Mode expects `goalId` and optional `eventId`
- Pass parent daily's goalId (not child scheduled event)
- Time banking happens at goal level, not event level

---

## Task Breakdown

### PHASE 1: Database Migration & Data Model

**Estimated Time:** 1-2 hours

#### STEP 1.1: Update Event Entity

**ACTION** `app/src/main/java/com/voxplanapp/data/Event.kt`
- **OPERATION:** Add dual-task system fields
- **CHANGE:**
  ```kotlin
  // BEFORE (line 28):
  val parentDailyId: Int? = null
  )

  // AFTER:
  val parentDailyId: Int? = null,
  val isFromQuota: Boolean = true,         // NEW: Distinguish quota vs singleton tasks
  val isCompleted: Boolean = false,        // NEW: Completion status (singleton tasks only)
  val completedAt: Long? = null            // NEW: Completion timestamp (epoch millis)
  )
  ```
- **RATIONALE:**
  - `isFromQuota`: Explicitly flags quota-based vs singleton tasks
  - `isCompleted`: Binary completion for singleton tasks (quota tasks ignore this)
  - `completedAt`: Timestamp using Long (epoch milliseconds) - Room-compatible
- **VALIDATE:** Build project - `./gradlew assembleDebug`
- **EXPECTED:** Compilation error in AppDatabase (expected - migration needed)

#### STEP 1.2: Create Database Migration

**ACTION** `app/src/main/java/com/voxplanapp/data/AppDatabase.kt`
- **OPERATION:** Add MIGRATION_13_14 in companion object
- **LOCATION:** After MIGRATION_12_13 (around line 150)
- **CHANGE:**
  ```kotlin
  val MIGRATION_13_14 = object : Migration(13, 14) {
      override fun migrate(database: SupportSQLiteDatabase) {
          // Add dual-task system fields

          // isFromQuota: Default TRUE (existing events are quota-based)
          database.execSQL(
              "ALTER TABLE Event ADD COLUMN isFromQuota INTEGER NOT NULL DEFAULT 1"
          )

          // isCompleted: Default FALSE (no events completed yet)
          database.execSQL(
              "ALTER TABLE Event ADD COLUMN isCompleted INTEGER NOT NULL DEFAULT 0"
          )

          // completedAt: Nullable timestamp (no default needed)
          database.execSQL(
              "ALTER TABLE Event ADD COLUMN completedAt INTEGER"
          )
      }
  }
  ```
- **VALIDATE:** Check SQL syntax, ensure column names match Event entity exactly
- **EXPECTED:** Migration object compiles but not yet registered

#### STEP 1.3: Register Migration & Update Version

**ACTION** `app/src/main/java/com/voxplanapp/data/AppDatabase.kt`
- **OPERATION:** Increment version number
- **CHANGE:**
  ```kotlin
  // BEFORE (line ~10):
  @Database(entities = [...], version = 13)

  // AFTER:
  @Database(entities = [...], version = 14)
  ```

**ACTION** `app/src/main/java/com/voxplanapp/data/AppContainer.kt`
- **OPERATION:** Register MIGRATION_13_14
- **LOCATION:** Database builder (around line 28-34)
- **CHANGE:**
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
- **VALIDATE:** Build and install on emulator/device
- **TEST:** Open app, verify no crash (migration runs automatically)
- **VERIFY:** Use Database Inspector to check new columns exist

---

### PHASE 2: Persistence via Lazy-Initialization

**Estimated Time:** 2-3 hours

#### STEP 2.1: Update addQuotaTasks() to Set isFromQuota

**ACTION** `app/src/main/java/com/voxplanapp/ui/daily/DailyViewModel.kt`
- **OPERATION:** Mark quota-generated tasks with isFromQuota=true
- **LOCATION:** addQuotaTasks() function (lines 112-137)
- **CHANGE:**
  ```kotlin
  // Inside the quotas.forEach loop, when creating Event:

  // BEFORE:
  val newEvent = Event(
      goalId = goal.id,
      title = goal.title,
      startDate = _uiState.value.date,
      quotaDuration = quota.dailyMinutes,
      order = nextOrder++
  )

  // AFTER:
  val newEvent = Event(
      goalId = goal.id,
      title = goal.title,
      startDate = _uiState.value.date,
      quotaDuration = quota.dailyMinutes,
      order = nextOrder++,
      isFromQuota = true  // NEW: Explicitly mark as quota-based
  )
  ```

#### STEP 2.2: Add Auto-Initialization Check

**ACTION** `app/src/main/java/com/voxplanapp/ui/daily/DailyViewModel.kt`
- **OPERATION:** Auto-populate from quotas if dailies list is empty
- **LOCATION:** Add new function after addQuotaTasks()
- **CHANGE:**
  ```kotlin
  /**
   * Lazy-initialize dailies for the current date if none exist.
   * Called when viewing a date for the first time.
   */
  private suspend fun initializeDailiesIfNeeded() {
      val currentDailies = _uiState.value.dailyTasks

      // Only initialize if list is empty
      if (currentDailies.isEmpty()) {
          // Check if there are any active quotas for this date
          val quotas = quotaRepository.getAllActiveQuotas(_uiState.value.date).first()

          if (quotas.isNotEmpty()) {
              // Auto-populate from quotas (reuse existing logic)
              addQuotaTasks()
              Log.d("DailyViewModel", "Auto-initialized ${quotas.size} quota tasks for ${_uiState.value.date}")
          }
      }
  }
  ```

#### STEP 2.3: Call Auto-Initialization on Date Change

**ACTION** `app/src/main/java/com/voxplanapp/ui/daily/DailyViewModel.kt`
- **OPERATION:** Trigger initialization when user navigates to a date
- **LOCATION:** updateDate() function or StateFlow collection
- **CHANGE:**
  ```kotlin
  // Option A: Add to updateDate() function
  fun updateDate(newDate: LocalDate) {
      _uiState.update { it.copy(date = newDate) }

      viewModelScope.launch {
          initializeDailiesIfNeeded()  // NEW: Auto-initialize if empty
      }
  }

  // Option B: Add to init block with Flow collection
  init {
      viewModelScope.launch {
          snapshotFlow { _uiState.value.date }
              .flatMapLatest { date ->
                  eventRepository.getDailiesForDate(date)
              }
              .collect { events ->
                  _uiState.update { it.copy(dailyTasks = events, isLoading = false) }
                  initializeDailiesIfNeeded()  // NEW: Check after loading
              }
      }
  }
  ```
- **RECOMMENDATION:** Use Option A (simpler, explicit control)

---

### PHASE 3: Dual Completion Tracking

**Estimated Time:** 3-4 hours

#### STEP 3.1: Add Completion Toggle Function to ViewModel

**ACTION** `app/src/main/java/com/voxplanapp/ui/daily/DailyViewModel.kt`
- **OPERATION:** Add function to toggle singleton task completion
- **LOCATION:** After deleteTask() function
- **CHANGE:**
  ```kotlin
  /**
   * Toggle completion status for singleton tasks.
   * Only applicable to tasks with isFromQuota=false.
   * Quota-based tasks are measured by time spent, not binary completion.
   */
  fun toggleCompletion(task: Event) {
      if (task.isFromQuota) {
          Log.w("DailyViewModel", "Cannot toggle completion on quota-based task: ${task.title}")
          return
      }

      viewModelScope.launch {
          val updatedTask = task.copy(
              isCompleted = !task.isCompleted,
              completedAt = if (!task.isCompleted) System.currentTimeMillis() else null
          )
          eventRepository.updateEvent(updatedTask)
          Log.d("DailyViewModel", "Toggled completion for ${task.title}: ${updatedTask.isCompleted}")
      }
  }
  ```

#### STEP 3.2: Update DailyTaskItem UI with Conditional Checkbox

**ACTION** `app/src/main/java/com/voxplanapp/ui/daily/DailyScreen.kt`
- **OPERATION:** Add checkbox for singleton tasks, hide for quota tasks
- **LOCATION:** DailyTaskItem composable (around line 300-400)
- **FIND:** The Row containing task title and action buttons
- **CHANGE:**
  ```kotlin
  // At the start of the Row (before task title):

  @Composable
  fun DailyTaskItem(
      task: Event,
      onScheduleClick: (Event) -> Unit,
      onDeleteClick: (Event) -> Unit,
      onCompleteClick: (Event) -> Unit,  // NEW parameter
      // ... other parameters
  ) {
      Card(/* ... */) {
          Row(
              modifier = Modifier
                  .fillMaxWidth()
                  .padding(12.dp),
              verticalAlignment = Alignment.CenterVertically
          ) {
              // NEW: Conditional checkbox for singleton tasks
              if (!task.isFromQuota) {
                  IconButton(
                      onClick = { onCompleteClick(task) },
                      modifier = Modifier.size(40.dp)
                  ) {
                      Icon(
                          painter = if (task.isCompleted) {
                              painterResource(id = R.drawable.ic_selected_check_box)
                          } else {
                              painterResource(id = R.drawable.ic_empty_check_box)
                          },
                          contentDescription = if (task.isCompleted)
                              "Mark ${task.title} incomplete"
                              else "Mark ${task.title} complete",
                          tint = TodoItemIconColor,
                          modifier = Modifier.size(20.dp)
                      )
                  }
                  Spacer(modifier = Modifier.width(8.dp))
              }

              // Existing task content (title, progress, etc.)
              Column(modifier = Modifier.weight(1f)) {
                  // Task title with conditional strikethrough
                  Text(
                      text = task.title,
                      style = MaterialTheme.typography.bodyLarge,
                      color = if (task.isCompleted)
                          TodoItemTextColor.copy(alpha = 0.5f)
                          else TodoItemTextColor,
                      textDecoration = if (task.isCompleted)
                          TextDecoration.LineThrough
                          else null
                  )

                  // Existing progress indicator code...
              }

              // Existing action buttons...
          }
      }
  }
  ```

#### STEP 3.3: Wire Up Completion Callback

**ACTION** `app/src/main/java/com/voxplanapp/ui/daily/DailyScreen.kt`
- **OPERATION:** Pass viewModel.toggleCompletion to DailyTaskItem
- **LOCATION:** LazyColumn items block (around line 200)
- **CHANGE:**
  ```kotlin
  // BEFORE:
  items(uiState.dailyTasks) { task ->
      DailyTaskItem(
          task = task,
          onScheduleClick = { viewModel.showScheduleDialog(it) },
          onDeleteClick = { viewModel.deleteTask(it) }
      )
  }

  // AFTER:
  items(uiState.dailyTasks) { task ->
      DailyTaskItem(
          task = task,
          onScheduleClick = { viewModel.showScheduleDialog(it) },
          onDeleteClick = { viewModel.deleteTask(it) },
          onCompleteClick = { viewModel.toggleCompletion(it) }  // NEW
      )
  }
  ```

---

### PHASE 4: Direct Focus Mode Access

**Estimated Time:** 2 hours

#### STEP 4.1: Add Start Focus Function to ViewModel

**ACTION** `app/src/main/java/com/voxplanapp/ui/daily/DailyViewModel.kt`
- **OPERATION:** Add navigation callback for Focus Mode
- **LOCATION:** Class properties section
- **CHANGE:**
  ```kotlin
  class DailyViewModel(
      private val eventRepository: EventRepository,
      private val quotaRepository: QuotaRepository,
      private val todoRepository: TodoRepository,
      sharedViewModel: SharedViewModel
  ) : ViewModel() {

      // NEW: Callback for Focus Mode navigation (set by screen)
      var onNavigateToFocus: ((goalId: Int, eventId: Int?) -> Unit)? = null

      // ... existing properties

      /**
       * Start Focus Mode for a daily task.
       * Passes goalId to Focus Mode, which will create time bank entries.
       */
      fun startFocusMode(task: Event) {
          Log.d("DailyViewModel", "Starting Focus Mode for task: ${task.title}, goalId: ${task.goalId}")
          onNavigateToFocus?.invoke(task.goalId, task.id)
      }
  }
  ```

#### STEP 4.2: Add Start Focus Button to UI

**ACTION** `app/src/main/java/com/voxplanapp/ui/daily/DailyScreen.kt`
- **OPERATION:** Add "Start Focus" button to DailyTaskItem
- **LOCATION:** Inside DailyTaskItem, in action buttons row
- **CHANGE:**
  ```kotlin
  // After existing Schedule and Delete buttons:

  Row(
      horizontalArrangement = Arrangement.End,
      verticalAlignment = Alignment.CenterVertically
  ) {
      // NEW: Start Focus button
      IconButton(
          onClick = { onStartFocusClick(task) },
          modifier = Modifier.size(40.dp)
      ) {
          Icon(
              imageVector = Icons.Default.PlayArrow,  // Or custom focus icon
              contentDescription = "Start Focus Mode for ${task.title}",
              tint = PrimaryColor
          )
      }

      // Existing Schedule button
      IconButton(
          onClick = { onScheduleClick(task) },
          modifier = Modifier.size(40.dp)
      ) {
          Icon(
              imageVector = Icons.Default.Schedule,
              contentDescription = "Schedule ${task.title}",
              tint = PrimaryColor
          )
      }

      // Existing Delete button
      // ...
  }
  ```

#### STEP 4.3: Add onStartFocusClick Parameter

**ACTION** `app/src/main/java/com/voxplanapp/ui/daily/DailyScreen.kt`
- **OPERATION:** Add callback parameter to DailyTaskItem
- **CHANGE:**
  ```kotlin
  @Composable
  fun DailyTaskItem(
      task: Event,
      onScheduleClick: (Event) -> Unit,
      onDeleteClick: (Event) -> Unit,
      onCompleteClick: (Event) -> Unit,
      onStartFocusClick: (Event) -> Unit,  // NEW parameter
      // ...
  ) {
      // ... implementation
  }
  ```

#### STEP 4.4: Wire Navigation in DailyScreen

**ACTION** `app/src/main/java/com/voxplanapp/ui/daily/DailyScreen.kt`
- **OPERATION:** Pass onEnterFocusMode callback to ViewModel and items
- **LOCATION:** DailyScreen composable function signature
- **CHANGE:**
  ```kotlin
  @Composable
  fun DailyScreen(
      navController: NavController,
      viewModel: DailyViewModel = viewModel(factory = AppViewModelProvider.Factory),
      onEnterFocusMode: (goalId: Int, eventId: Int?) -> Unit  // NEW parameter
  ) {
      // Set navigation callback on ViewModel
      viewModel.onNavigateToFocus = onEnterFocusMode

      // ... rest of implementation

      // In LazyColumn items:
      items(uiState.dailyTasks) { task ->
          DailyTaskItem(
              task = task,
              onScheduleClick = { viewModel.showScheduleDialog(it) },
              onDeleteClick = { viewModel.deleteTask(it) },
              onCompleteClick = { viewModel.toggleCompletion(it) },
              onStartFocusClick = { viewModel.startFocusMode(it) }  // NEW
          )
      }
  }
  ```

#### STEP 4.5: Update Navigation Route

**ACTION** `app/src/main/java/com/voxplanapp/navigation/VoxPlanNavHost.kt`
- **OPERATION:** Pass onEnterFocusMode callback to DailyScreen
- **LOCATION:** composable route for Daily screen
- **CHANGE:**
  ```kotlin
  // Find Daily screen route (look for VoxPlanScreen.Daily)

  composable(
      route = VoxPlanScreen.Daily.route + "/{date}?newEventId={newEventId}",
      arguments = listOf(/* ... */)
  ) { backStackEntry ->
      DailyScreen(
          navController = navController,
          onEnterFocusMode = { goalId, eventId ->
              navController.navigate(
                  VoxPlanScreen.FocusMode.createRouteFromGoal(goalId)
              )
          }
      )
  }
  ```

---

### PHASE 5: Singleton Task Creation

**Estimated Time:** 2-3 hours

#### STEP 5.1: Add Create Singleton Task Function

**ACTION** `app/src/main/java/com/voxplanapp/ui/daily/DailyViewModel.kt`
- **OPERATION:** Function to create ad-hoc singleton tasks
- **LOCATION:** After addQuotaTasks()
- **CHANGE:**
  ```kotlin
  /**
   * Create a singleton (non-quota) task for the current date.
   * These tasks have binary completion status and auto-rollover if incomplete.
   */
  fun createSingletonTask(title: String, goalId: Int, durationMinutes: Int? = null) {
      viewModelScope.launch {
          val maxOrder = _uiState.value.dailyTasks.maxOfOrNull { it.order } ?: -1

          val newTask = Event(
              goalId = goalId,
              title = title,
              startDate = _uiState.value.date,
              quotaDuration = durationMinutes,  // User-specified or null
              order = maxOrder + 1,
              isFromQuota = false,  // CRITICAL: Mark as singleton
              isCompleted = false
          )

          eventRepository.insertEvent(newTask)
          Log.d("DailyViewModel", "Created singleton task: $title")
      }
  }

  /**
   * Show dialog to create singleton task
   */
  fun showCreateSingletonDialog() {
      _showCreateSingletonDialog.value = true
  }

  fun dismissCreateSingletonDialog() {
      _showCreateSingletonDialog.value = false
  }

  // Add state for dialog
  private val _showCreateSingletonDialog = MutableStateFlow(false)
  val showCreateSingletonDialog: StateFlow<Boolean> = _showCreateSingletonDialog.asStateFlow()
  ```

#### STEP 5.2: Update DailyUiState

**ACTION** `app/src/main/java/com/voxplanapp/ui/daily/DailyViewModel.kt`
- **OPERATION:** Add dialog state to UI state (or use separate StateFlow as above)
- **CHANGE:**
  ```kotlin
  // If using UiState approach:
  data class DailyUiState(
      val date: LocalDate = LocalDate.now(),
      val dailyTasks: List<Event> = emptyList(),
      val isLoading: Boolean = true,
      val error: String? = null,
      val eventNeedingDuration: Int? = null,
      val showCreateSingletonDialog: Boolean = false  // NEW
  )
  ```

#### STEP 5.3: Create Singleton Task Dialog

**ACTION** `app/src/main/java/com/voxplanapp/ui/daily/DailyScreen.kt`
- **OPERATION:** Add composable dialog for creating singleton tasks
- **LOCATION:** Before DailyScreen main composable
- **CHANGE:**
  ```kotlin
  @Composable
  fun CreateSingletonTaskDialog(
      onDismiss: () -> Unit,
      onCreate: (title: String, goalId: Int, duration: Int?) -> Unit,
      availableGoals: List<TodoItem>  // Need to pass from screen
  ) {
      var title by remember { mutableStateOf("") }
      var selectedGoalId by remember { mutableStateOf<Int?>(null) }
      var duration by remember { mutableStateOf<Int?>(null) }
      var showDuration by remember { mutableStateOf(false) }

      AlertDialog(
          onDismissRequest = onDismiss,
          title = { Text("Create Task") },
          text = {
              Column {
                  // Title input
                  OutlinedTextField(
                      value = title,
                      onValueChange = { title = it },
                      label = { Text("Task title") },
                      modifier = Modifier.fillMaxWidth()
                  )

                  Spacer(modifier = Modifier.height(16.dp))

                  // Goal selection (dropdown)
                  Text("Link to goal:", style = MaterialTheme.typography.bodySmall)
                  // TODO: Implement goal picker (can use existing goals from MainScreen pattern)

                  Spacer(modifier = Modifier.height(16.dp))

                  // Optional duration
                  Row(verticalAlignment = Alignment.CenterVertically) {
                      Checkbox(
                          checked = showDuration,
                          onCheckedChange = { showDuration = it }
                      )
                      Text("Set duration")
                  }

                  if (showDuration) {
                      // Duration picker (reuse existing duration dialog logic)
                      OutlinedTextField(
                          value = duration?.toString() ?: "",
                          onValueChange = { duration = it.toIntOrNull() },
                          label = { Text("Minutes") },
                          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                      )
                  }
              }
          },
          confirmButton = {
              TextButton(
                  onClick = {
                      if (title.isNotBlank() && selectedGoalId != null) {
                          onCreate(title, selectedGoalId!!, duration)
                          onDismiss()
                      }
                  },
                  enabled = title.isNotBlank() && selectedGoalId != null
              ) {
                  Text("Create")
              }
          },
          dismissButton = {
              TextButton(onClick = onDismiss) {
                  Text("Cancel")
              }
          }
      )
  }
  ```

#### STEP 5.4: Add FAB to DailyScreen

**ACTION** `app/src/main/java/com/voxplanapp/ui/daily/DailyScreen.kt`
- **OPERATION:** Add FloatingActionButton for creating singleton tasks
- **LOCATION:** Scaffold in DailyScreen (or parent VoxPlanApp if FAB is global)
- **CHANGE:**
  ```kotlin
  Scaffold(
      floatingActionButton = {
          FloatingActionButton(
              onClick = { viewModel.showCreateSingletonDialog() },
              containerColor = PrimaryColor
          ) {
              Icon(
                  imageVector = Icons.Default.Add,
                  contentDescription = "Add task",
                  tint = Color.White
              )
          }
      }
  ) { paddingValues ->
      // Existing content

      // Show dialog if needed
      if (viewModel.showCreateSingletonDialog.collectAsState().value) {
          CreateSingletonTaskDialog(
              onDismiss = { viewModel.dismissCreateSingletonDialog() },
              onCreate = { title, goalId, duration ->
                  viewModel.createSingletonTask(title, goalId, duration)
              },
              availableGoals = /* need to fetch from TodoRepository */
          )
      }
  }
  ```

#### STEP 5.5: Fetch Available Goals for Dialog

**ACTION** `app/src/main/java/com/voxplanapp/ui/daily/DailyViewModel.kt`
- **OPERATION:** Expose available goals for singleton task creation
- **CHANGE:**
  ```kotlin
  // Add StateFlow for available goals
  val availableGoals: StateFlow<List<TodoItem>> = todoRepository.getAllTodos()
      .map { todos ->
          // Filter to only leaf goals (no children) or show all
          todos  // Can add filtering logic if needed
      }
      .stateIn(
          scope = viewModelScope,
          started = SharingStarted.WhileSubscribed(5_000L),
          initialValue = emptyList()
      )
  ```

---

### PHASE 6: Auto-Rollover for Singleton Tasks

**Estimated Time:** 2 hours

#### STEP 6.1: Add Rollover Query to EventDao

**ACTION** `app/src/main/java/com/voxplanapp/data/EventDao.kt`
- **OPERATION:** Query for incomplete singleton tasks from previous day
- **LOCATION:** Add new query function
- **CHANGE:**
  ```kotlin
  /**
   * Get incomplete singleton tasks for a specific date.
   * Used for rollover functionality.
   */
  @Query("""
      SELECT * FROM Event
      WHERE startDate = :date
      AND parentDailyId IS NULL
      AND isFromQuota = 0
      AND isCompleted = 0
      ORDER BY `order`
  """)
  suspend fun getIncompleteSingletonsForDate(date: LocalDate): List<Event>
  ```

#### STEP 6.2: Add Rollover Function to Repository

**ACTION** `app/src/main/java/com/voxplanapp/data/EventRepository.kt`
- **OPERATION:** Add rollover logic
- **LOCATION:** Add new function
- **CHANGE:**
  ```kotlin
  /**
   * Rollover incomplete singleton tasks from sourceDate to targetDate.
   * Creates new Event entities with updated startDate.
   */
  suspend fun rolloverIncompleteSingletons(sourceDate: LocalDate, targetDate: LocalDate) {
      val incompleteTasks = eventDao.getIncompleteSingletonsForDate(sourceDate)

      incompleteTasks.forEach { task ->
          val rolledOverTask = task.copy(
              id = 0,  // Auto-generate new ID
              startDate = targetDate,
              order = 0  // Will be reordered in target date
          )
          eventDao.insertEvent(rolledOverTask)
      }
  }
  ```

#### STEP 6.3: Call Rollover on Initialization

**ACTION** `app/src/main/java/com/voxplanapp/ui/daily/DailyViewModel.kt`
- **OPERATION:** Check for rollover when viewing "today"
- **LOCATION:** Add to initializeDailiesIfNeeded() or separate function
- **CHANGE:**
  ```kotlin
  /**
   * Check if yesterday had incomplete singleton tasks and rollover to today.
   * Only runs when viewing "today".
   */
  private suspend fun checkAndRolloverFromYesterday() {
      val today = LocalDate.now()
      val yesterday = today.minusDays(1)

      // Only rollover if viewing today
      if (_uiState.value.date != today) return

      // Check if rollover already happened (avoid duplicates)
      val todaysSingletons = eventRepository.getDailiesForDate(today)
          .first()
          .filter { !it.isFromQuota }

      // If today already has singleton tasks, assume rollover done
      // (More robust: add "rolledOverFrom" field to track)
      val yesterdaysIncomplete = eventRepository.getIncompleteSingletonsForDate(yesterday)

      if (yesterdaysIncomplete.isNotEmpty() && todaysSingletons.isEmpty()) {
          eventRepository.rolloverIncompleteSingletons(yesterday, today)
          Log.d("DailyViewModel", "Rolled over ${yesterdaysIncomplete.size} tasks from yesterday")
      }
  }
  ```

#### STEP 6.4: Add EventRepository Function

**ACTION** `app/src/main/java/com/voxplanapp/data/EventRepository.kt`
- **OPERATION:** Expose getIncompleteSingletonsForDate
- **CHANGE:**
  ```kotlin
  suspend fun getIncompleteSingletonsForDate(date: LocalDate): List<Event> {
      return eventDao.getIncompleteSingletonsForDate(date)
  }
  ```

#### STEP 6.5: Integrate Rollover Check

**ACTION** `app/src/main/java/com/voxplanapp/ui/daily/DailyViewModel.kt`
- **OPERATION:** Call rollover check in init or updateDate
- **CHANGE:**
  ```kotlin
  init {
      viewModelScope.launch {
          // Check for rollover first
          checkAndRolloverFromYesterday()

          // Then initialize dailies
          snapshotFlow { _uiState.value.date }
              .flatMapLatest { date ->
                  eventRepository.getDailiesForDate(date)
              }
              .collect { events ->
                  _uiState.update { it.copy(dailyTasks = events, isLoading = false) }
                  initializeDailiesIfNeeded()
              }
      }
  }
  ```

---

## Testing Strategy

### Manual Testing Checklist

**Phase 1 - Data Model:**
- [ ] Build succeeds after migration
- [ ] App doesn't crash on first launch after migration
- [ ] Database Inspector shows new columns (isFromQuota, isCompleted, completedAt)
- [ ] Existing events have isFromQuota=1 (true) by default

**Phase 2 - Persistence:**
- [ ] Click "Add Quota Tasks" creates events with isFromQuota=true
- [ ] Close and reopen app → tasks still visible (persistence works)
- [ ] Navigate to future date → auto-populates from quotas (lazy-init works)
- [ ] Navigate to date with no quotas → empty list (no crash)

**Phase 3 - Dual Completion:**
- [ ] Quota-based tasks show NO checkbox
- [ ] Quota-based tasks show progress boxes (green/orange/gray)
- [ ] Singleton tasks show checkbox (unchecked initially)
- [ ] Click checkbox → task gets strikethrough + faded
- [ ] Click again → checkbox unchecks, strikethrough removed
- [ ] Database Inspector: isCompleted toggles, completedAt updates

**Phase 4 - Focus Mode:**
- [ ] Click "Start Focus" on quota task → navigates to Focus Mode
- [ ] Click "Start Focus" on singleton → navigates to Focus Mode
- [ ] Focus Mode shows correct goal name
- [ ] Complete Focus session → time banks to goal
- [ ] Return to Dailies → green progress boxes update

**Phase 5 - Singleton Creation:**
- [ ] Click FAB → dialog opens
- [ ] Enter title + select goal → Create button enabled
- [ ] Click Create → new task appears in list
- [ ] New task has checkbox (isFromQuota=false)
- [ ] Can complete singleton task
- [ ] Can schedule singleton task

**Phase 6 - Rollover:**
- [ ] Create singleton task for "today"
- [ ] Leave uncompleted
- [ ] Fast-forward device time to next day OR manually test with yesterday's date
- [ ] Open Dailies → incomplete task appears in today's list
- [ ] Quota tasks from yesterday do NOT rollover
- [ ] Completed singletons do NOT rollover

### Edge Cases

1. **Empty quota list:** User has no active quotas → Dailies empty → FAB works
2. **Mixed task types:** Quota + singleton tasks render correctly side-by-side
3. **Scheduled singleton:** Singleton with scheduled child events → can still complete
4. **Rollover duplicates:** Rollover runs twice → should not create duplicate tasks
5. **Migration on old database:** Upgrade from v13 → v14 works without data loss

---

## Success Criteria

### Must Have (Phase 1-4)
- ✅ Database migration succeeds (v13 → v14)
- ✅ Quota tasks show progress boxes, NO checkbox
- ✅ Singleton tasks show checkbox, can mark complete
- ✅ Direct "Start Focus" button works from Dailies
- ✅ Tasks persist across app restarts
- ✅ Can create singleton tasks (FAB + dialog)

### Should Have (Phase 5-6)
- ✅ Incomplete singleton tasks auto-rollover to next day
- ✅ Quota tasks never rollover (date-specific)
- ✅ Completed tasks show visual feedback (strikethrough, fade)
- ✅ No crashes or data loss

### Nice to Have (Future)
- 🔮 Bulk operations (multi-select and complete/delete)
- 🔮 Daily summary ("3/5 complete, 4/6 hours")
- 🔮 Visual indicator for scheduled tasks
- 🔮 Quick-reschedule from Dailies screen
- 🔮 Manual rollover (user chooses which tasks to defer)

---

## Files to Modify

### Core Data (Phase 1)
- `app/src/main/java/com/voxplanapp/data/Event.kt` - Add fields
- `app/src/main/java/com/voxplanapp/data/AppDatabase.kt` - Migration
- `app/src/main/java/com/voxplanapp/data/AppContainer.kt` - Register migration

### ViewModel Logic (Phase 2-6)
- `app/src/main/java/com/voxplanapp/ui/daily/DailyViewModel.kt` - All business logic

### UI (Phase 3-5)
- `app/src/main/java/com/voxplanapp/ui/daily/DailyScreen.kt` - UI updates

### Navigation (Phase 4)
- `app/src/main/java/com/voxplanapp/navigation/VoxPlanNavHost.kt` - Focus Mode route

### Repository (Phase 6)
- `app/src/main/java/com/voxplanapp/data/EventRepository.kt` - Rollover functions
- `app/src/main/java/com/voxplanapp/data/EventDao.kt` - Rollover query

---

## Implementation Notes

### Why Dual-Task System?

The dual-task system reflects the reality of how people work:

**Ongoing Processes (Quota-Based):**
- Long-term habits and priorities
- Success measured by consistency and time investment
- Example: "Read 1h/day" - doesn't matter which book, just that you read
- No endpoint - it's about building the habit

**Discrete Tasks (Singleton):**
- Specific deliverables with clear completion criteria
- Success measured by whether it got done
- Example: "Call dentist" - either you did it or you didn't
- Clear endpoint - task can be archived

VoxPlanApp is unique in recognizing this distinction and handling both gracefully.

### Why Manual "Add Quota Tasks"?

User preference for control. Alternatives considered:
- Auto-create every morning (too automatic, might surprise users)
- Create for entire week at once (database bloat)
- Virtual dailies (can't track scheduling/completion per day)

Manual button provides:
- Explicit user action (clear cause → effect)
- Flexibility (user can skip days, adjust quotas first)
- Simplicity (no background jobs, no complex lifecycle)

### Why Auto-Rollover for Singletons Only?

**Singletons:** Have definite endpoints, so rollover makes sense
- "Call dentist" not done today → still need to do it tomorrow
- User intended to complete it, just ran out of time

**Quota-based:** Tied to specific date's quota
- "Programming 2h on Monday" not met → can't retroactively fulfill Monday's quota on Tuesday
- Tuesday has its own quota (also "Programming 2h")
- Rollover would double-count and confuse quota tracking

---

## Risk Assessment

### Low Risk
- Database migration (standard Room pattern, well-tested)
- UI changes (additive, doesn't break existing functionality)
- Focus Mode navigation (reuses existing pattern)

### Medium Risk
- Auto-rollover logic (might create duplicates if not careful)
- Lazy-initialization (could trigger unexpectedly if not gated properly)

### Mitigation
- Rollover: Check for existing singleton tasks before copying
- Lazy-init: Gate with isEmpty() check, only run once per date view
- Testing: Manual testing with device time manipulation

---

## Next Steps After Implementation

1. **User Testing:** Validate dual-task system UX with real users
2. **Polish UI:** Add animations for completion, improve visual hierarchy
3. **Bulk Operations:** Multi-select for power users
4. **Daily Summary:** Aggregated stats at top of screen
5. **Smart Suggestions:** "You have 3 unscheduled tasks, schedule now?"
6. **Week View:** See entire week's dailies at a glance

---

**Document Version:** 1.0
**Created:** 2025-12-19
**Status:** Ready for Implementation
**Estimated Total Time:** 12-16 hours across 6 phases
