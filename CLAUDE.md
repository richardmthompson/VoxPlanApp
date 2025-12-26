---
file: CLAUDE.md
function: This file provides guidance to Claude Code when working with code in this repository.
variables: $CURRENT_PROJECT_ROOT = root directory of current project (should contain .claude/)
---

# **IMPORTANT**

- Always check - is this a coding task?
- If so, am I in one of the correct modes (PLANNER / EXECUTOR)?
- Have I invoked the coding-planner?

# Dual Roles for Problem Solving

You operate in two modes: **PLANNER** and **EXECUTOR** for coding tasks.

**Default**: Start in PLANNER mode unless explicitly instructed otherwise.

## PLANNER Mode

### Coding Task Workflow (MANDATORY)

**CRITICAL**: When PLANNER mode receives a new coding task (rather than continuing work on an existing one), you MUST invoke the coding-planner skill. When the user says 'let's plan <this coding task>', this is the skill to use.

**Workflow Sequence**:

```
1. PLANNER receives coding task from user
2. PLANNER invokes coding-planner skill
3. Skill categorizes, plans, and returns output
4. PLANNER presents to user
5. User approves
6. PLANNER creates beads tasks
7. PLANNER → EXECUTOR handoff
```

**Never skip step 2** - Even simple tasks require complexity assessment.

---

### After Skill Completes

The skill returns plan location and confidence. Your job:

1. **Present to user** - Show plan summary and ask for approval
2. **Wait for approval** - ⛔ STOP - Do not proceed without explicit approval
3. **Create beads tasks** - Only after approval, reference plan path
4. **Handoff to EXECUTOR** - Signal mode switch with plan reference

### Approval Gate Rules

**NEVER**:

- ❌ Auto-create beads tasks before user approval
- ❌ Proceed to implementation without approval
- ❌ Skip approval gate for any task

**ALWAYS**:

- ✅ Invoke coding-planner skill for new coding tasks
- ✅ Present plan to user
- ✅ Wait for explicit approval
- ✅ Reference plan path in beads tasks

---

# START HERE WHEN PLANNING: Gather Context First

- `MVP_RELEASE_PLAN.md`: Overview of all current goals (MVP Release)

#### ALWAYS LOOK AT THESE FIRST...

`agent/context/project_context.md`: High level project context info
`agent/context/codebase_context.md`: Codebase specific for dev-level architectural understanding.

- `agent/context/`: ALSO, make sure you SEARCH this directory for more specific CODE-context. This will save you time.

---

## EXECUTOR Mode

**Responsibilities**:

- Read scratchpad/PRP from path provided by PLANNER
- Convert proposed milestones & tasks into beads tasks, complete with dependencies as instructed below.
- Execute according to plan
- Update beads notes with progress
- Mark tasks completed

**Never**:

- Skip reading the plan
- Modify plan structures (PLANNER only)

## Scratchpad Location (Standardized)

**All scratchpads MUST be created in**: `$CURRENT_PROJECT_ROOT/scratchpad/`

**Format**: `scratchpad/<task-description>_<DD-MMM-YY>/plan.md`

**Example**: `scratchpad/focus-medal-bugs_26-Dec-25/plan.md`

**NOT in**: `.claude/scratchpad/` (reserved for Claude Code configuration only)

This ensures all coding agents use the same location for planning artifacts.

---

## Task Tracking with Beads

**Primary Task System**: Use the `bd-issue-tracking` skill for all task management work.

**Session Start Protocol (Mandatory)**:

1. `bd ready --json` - Load available work
2. `bd list --status=in_progress --json` - Check active tasks
3. `bv --robot-plan` - Understand strategic priorities and impact
4. Report to user: "X items ready, current priorities: [summary from bv]"

## Agent Mail Coordination (MANDATORY)

### Agent Identity & Registration

**CRITICAL**: VoxPlan project key is `/Users/richardthompson/StudioProjects/VoxPlanApp` - use this EXACT path for all Agent Mail operations.

**First-Time Setup (Once Per Agent):**

1. **Create unique identity** (not register - that can reuse names):
   ```
   mcp__mcp-agent-mail__create_agent_identity(
     project_key="/Users/richardthompson/StudioProjects/VoxPlanApp",
     program="claude-code",
     model="sonnet-4.5",
     task_description="VoxPlan development"
   )
   ```
   System assigns unique name (e.g., "BlueLake", "GreenCastle")

2. **Set contact policy to "open"**:
   ```
   mcp__mcp-agent-mail__set_contact_policy(
     project_key="/Users/richardthompson/StudioProjects/VoxPlanApp",
     agent_name="<your_assigned_name>",
     policy="open"
   )
   ```

3. **Send introduction**: Announce you're online to other VoxPlan agents

### Session Start Protocol (Every Session)

1. Check inbox: `fetch_inbox(project_key=..., agent_name=...)`
2. Review available work: `bd ready --json`
3. Check for messages from other agents
4. Report status to user

### Task Workflow - MANDATORY Communication

**Before Starting ANY Task:**

1. **Reserve files**:
   ```
   file_reservation_paths(
     project_key="/Users/richardthompson/StudioProjects/VoxPlanApp",
     agent_name="<your_name>",
     paths=["app/src/main/java/com/voxplanapp/..."],
     ttl_seconds=3600,
     exclusive=True,
     reason="Working on <issue-id>"
   )
   ```

2. **Send start message** (MANDATORY):
   ```
   send_message(
     project_key="/Users/richardthompson/StudioProjects/VoxPlanApp",
     sender_name="<your_name>",
     to=["<all_agent_names>"],  # MUST include ALL project agents + Richard
     subject="Starting work on <task-title>",
     body_md="Working on <issue-id>: <brief description>\nReserved files: <list>\nEstimated time: <estimate>",
     thread_id="bd-<issue-id>"
   )
   ```

   **Critical**: List all agents in project. Use `resource://agents/<project_key>` to discover names.

3. Claim task: `bd update <issue-id> --status=in_progress`

**After Completing ANY Task:**

1. **Send completion message** (MANDATORY):
   ```
   send_message(
     project_key="/Users/richardthompson/StudioProjects/VoxPlanApp",
     sender_name="<your_name>",
     to=["<all_agent_names>"],  # MUST include ALL project agents + Richard
     subject="Completed <task-title>",
     body_md="Completed <issue-id>\n\nChanges:\n- <summary>\n\nFiles modified:\n- <list>\n\nTests: <status>",
     thread_id="bd-<issue-id>"
   )
   ```

   **Critical**: Notify everyone of completion for coordination.

2. **Release file reservations**:
   ```
   release_file_reservations(
     project_key="/Users/richardthompson/StudioProjects/VoxPlanApp",
     agent_name="<your_name>"
   )
   ```

3. Update beads notes
4. Close task: `bd close <issue-id>`

### Inter-Agent Communication

- **Check inbox periodically** during work (every 15-20 minutes)
- **Acknowledge important messages**: Use `acknowledge_message`
- **Respond to coordination requests** promptly
- **Use thread_id="bd-<issue-id>"** to link messages to beads tasks

### File Coordination Rules

- **ALWAYS reserve before editing** - prevents conflicts
- **Use specific globs** - not `**/*` (too broad)
- **Release when done** - don't hold reservations
- **Respect others' locks** - if files are reserved, message to coordinate

### Project Key Reference

**ALWAYS use:** `/Users/richardthompson/StudioProjects/VoxPlanApp`
**NEVER use:** Any other path (like CODE/PAI/projects/voxplan)

**Planning Hierarchy**:

- **Scratchpad/PRP**: High-level project context, strategic planning, solution architecture - human-readable
- **Beads**: Lower-level agentic task breakdown - once strategy is clear, create beads issues for each phase/stage/milestone

**Task Extraction from Planning Docs**:

- Use `/taskinit` command for comprehensive task extraction workflow
- Batching strategy defined in /taskinit to avoid overwhelming user
- Planning docs updated to reference beads issues, removing duplication

---

### TodoWrite vs Beads: When to Use Which

| Tool | Timescale | Persistence | Use For |
|------|-----------|-------------|---------|
| **TodoWrite** | Minutes-hours (single session) | Cleared at session end | Multi-step execution tracking (2+ steps), implementation, analysis, debugging |
| **Beads** | Days-weeks (multi-session) | Survives compaction | Strategic objectives, long-term tracking, agent coordination |

**Key Decision**: Use TodoWrite for transparency during execution, beads for persistence across sessions.

**Handoff Pattern:**
```bash
bd show PAI-123              # Read strategic objective
# Create TodoWrite for execution → Work → Mark complete
bd update PAI-123 --notes "..." # Checkpoint progress
# Session end: TodoWrite cleared, bead persists
```

---

### Beads Issue Fields Overview

| Field | Purpose | Immutable? | When to Set/Update |
|-------|---------|------------|-------------------|
| `description` | Problem statement (what & why) | ✅ Yes | At creation only - never modify |
| `design` | Technical approach | ❌ No | During planning - update only for major pivots |
| `acceptance_criteria` | Completion checklist | ⚠️ Semi | When design clear - mark items done as you work |
| `notes` | Session handoff document | ❌ No | **Most frequent** - update at milestones, blockers, session end |

**Field Usage:**
- **`description`**: Set once. Problem, not solution. Example: "Email service lacks retry logic for password resets"
- **`design`**: Architecture decisions during planning. Rarely updated after set.
- **`acceptance_criteria`**: Concrete deliverables checklist. Mark complete as you work.
- **`notes`**: Progress tracking (see "Beads Notes Structure" below). Update frequently during active work.

---

### Beads Notes Structure (Mandatory)

**Structured Format:**
```
COMPLETED: [Specific deliverables with technical details]
IN PROGRESS: [Current state + immediate next step]
BLOCKERS: [What prevents progress, or "None"]
KEY DECISIONS: [Important decisions with rationale]
```

**Quality Test**: Could you or another agent resume this work without conversation history? If no, improve notes.

**Guidelines:**
- **COMPLETED**: Past tense, file names, verification (e.g., "Implemented JWT auth in AuthService.kt, tests passing")
- **IN PROGRESS**: Current state with file:line, concrete next step (e.g., "Adding error handling in RefreshTokenHandler.kt:67. Next: Implement token rotation")
- **BLOCKERS**: Specific blockers or "None"
- **KEY DECISIONS**: Why, not just what - include rationale

## Project Overview

VoxPlanApp is an Android productivity app built with Jetpack Compose that implements hierarchical goal management with time tracking, daily quotas, scheduling, and gamified focus sessions. The app uses MVVM architecture with manual dependency injection, Room database, and reactive programming with Kotlin Flow/StateFlow.

**Current Version:** 3.2 (Dailies improved with parent/child Events)

## Build & Development Commands

### Building the Project

```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Clean build
./gradlew clean build
```

### Running Tests

```bash
# Run unit tests
./gradlew test

# Run instrumented tests (requires emulator or device)
./gradlew connectedAndroidTest

# Run specific test
./gradlew test --tests com.voxplanapp.ExampleUnitTest
```

### Database Management

```bash
# Room schema exports are located in app/schemas/
# Schema location is configured via KSP: app/build.gradle.kts line 88-90
# Current database version: 13
```

### Gradle Tasks

```bash
# List all available tasks
./gradlew tasks

# Lint checks
./gradlew lint

# Install debug build on connected device
./gradlew installDebug
```

## Architecture & Codebase Reference

**Detailed architecture documentation is in context files** - refer to these instead of duplicating here:

- **`agent/context/project_context.md`** - High-level project architecture, features, and design decisions
- **`agent/context/codebase_context.md`** - Development-level architectural details, patterns, and code structure
- **`agent/context/dailies_context.md`** - Specific context for dailies feature implementation

**When planning/coding**: Always check agent/context/ directory first for existing architectural context.

## Critical Files & Line Counts

### ViewModels (State Management)

- `MainViewModel.kt` (362 lines) - Goal list with breadcrumb navigation, ActionMode reordering
- `FocusViewModel.kt` (492 lines) - Complex state: timer, medals, discrete tasks, time banking
- `DailyViewModel.kt` (190 lines) - Daily tasks with quota integration
- `SchedulerViewModel.kt` (141 lines) - Day-by-day event scheduler with date reactivity
- `GoalEditViewModel.kt` (191 lines) - Goal editing with quota settings
- `ProgressViewModel.kt` (174 lines) - Weekly quota progress tracking
- `SharedViewModel.kt` (80 lines) - Breadcrumb navigation logic

### UI Screens

- `DailyScreen.kt` (628 lines) - Daily tasks screen with quota integration
- `DaySchedule.kt` (675 lines) - **Contains critical delete dialog bug (lines 110-126)**
- `FocusModeScreen.kt` (697 lines) - Timer UI with medals and discrete tasks
- `MainScreen.kt` (492 lines) - Goal hierarchy with ActionMode buttons
- `GoalEditScreen.kt` (292 lines) - Goal editing with quota settings

### Data Layer

- `AppDatabase.kt` (384 lines) - Room database with 13 migrations
- `TodoRepository.kt` (76 lines) - Goal CRUD operations
- `EventRepository.kt` (32 lines) - Event CRUD with parent/child filtering
- `TimeBankRepository.kt` (56 lines) - Time tracking entries
- `QuotaRepository.kt` (47 lines) - Quota management with active days logic

### Navigation

- `VoxPlanNavHost.kt` - Route definitions and composable routing
- `VoxPlanScreen.kt` - Sealed class for type-safe navigation
- `VoxPlanApp.kt` - Scaffold with bottom navigation, ActionModeHandler

## Known Issues & Incomplete Features

### Critical Bugs (Fix First)

**1. Delete Dialog Bug in DaySchedule.kt (lines 110-126)**

```kotlin
// ERROR: 'event' variable is undefined in this scope
AlertDialog(
    onDismissRequest = { viewModel.dismissDeleteParentDialog() },
    confirmButton = {
        TextButton(onClick = { viewModel.confirmDeleteChildOnly(event) }) { /* ... */ }
    },
    dismissButton = {
        TextButton(onClick = { viewModel.confirmDeleteWithParent(event) }) { /* ... */ }
    }
)
```

**Fix:** Refactor to use `showDeleteParentDialog.value` (the Event) instead of undefined `event` variable.

**2. QuickScheduleScreen.kt - Entirely Commented Out**

- Lines 26-86 are all commented out
- Contains undefined variables
- Either implement properly or remove file

### Incomplete Features (Dailies - ~70% Complete)

- ❌ Completion tracking (no checkboxes for daily tasks)
- ❌ Direct Focus Mode access from dailies
- ❌ Bulk operations (multi-select, batch actions)
- ❌ Quick-reschedule feature
- ✅ Quota integration ("Add Quota Tasks" button)
- ✅ Parent-child relationship with scheduled events
- ✅ Vertical reordering with ActionMode

### Incomplete Features (Scheduling - ~65% Complete)

- ❌ Event creation from schedule (tap empty space)
- ❌ Week/month calendar views
- ❌ Recurrence support (RecurrenceType enum exists but not used)
- ❌ Color-coding by goal
- ❌ Smart scheduling suggestions
- ❌ Scroll position persistence on date change
- ✅ Day view with hourly grid
- ✅ Drag-to-schedule from dailies
- ✅ Focus Mode access from scheduled events

## Development Workflow Patterns

### Adding a New ViewModel

1. Create data classes for UI state in ViewModel file
2. Add ViewModel to `AppViewModelProvider.Factory`:

```kotlin
initializer {
    MyViewModel(
        repository = voxPlanApplication().container.myRepository,
        sharedViewModel = sharedViewModel  // if needed
    )
}
```

3. Use in Composable: `viewModel = viewModel(factory = AppViewModelProvider.Factory)`

### Database Migration Pattern

```kotlin
// In AppDatabase.kt
val MIGRATION_X_Y = object : Migration(X, Y) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE ... ADD COLUMN ...")
    }
}

// Add to database builder
.addMigrations(MIGRATION_2_3, ..., MIGRATION_X_Y)
```

**IMPORTANT:** Increment version number in `@Database(version = X)` annotation.

### Flow Reactive Patterns

**Pattern 1: Simple Flow Subscription**

```kotlin
val goals = todoRepository.getItemsByIds(goalIds).first()  // Suspend function
```

**Pattern 2: Combining Flows**

```kotlin
combine(flow1, flow2) { data1, data2 ->
    CombinedState(data1, data2)
}.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), InitialState())
```

**Pattern 3: Date-Driven State with flatMapLatest**

```kotlin
_currentDate.flatMapLatest { date ->
    eventRepository.getEventsForDate(date)
}.collect { events ->
    _eventsForCurrentDate.value = events
}
```

**Pattern 4: Compose State to Flow with snapshotFlow**

```kotlin
snapshotFlow { _uiState.value.date }
    .flatMapLatest { date ->
        eventRepository.getDailiesForDate(date)
    }
    .collect { events -> /* update state */ }
```

### ActionMode Reordering System

**Enum States:** Normal, VerticalUp, VerticalDown, HierarchyUp, HierarchyDown

**Usage Pattern:**

1. User clicks ActionMode button (e.g., VerticalUp)
2. ActionModeHandler toggles state in VoxPlanApp
3. UI highlights active button
4. User clicks goal to reorder
5. ViewModel calculates new order based on mode
6. `updateItemsInTransaction()` updates multiple items atomically
7. Flow emits new data, UI re-renders

## Constants & Magic Numbers

**File:** `data/Constants.kt`

- `FULLBAR_MINS = 60` - Minutes to fill one power bar
- `pointsForItemCompletion = 15` - Time bonus for completing a task

**Hardcoded Values to Know:**

- Max goal depth: 3 (in SharedViewModel.processGoals)
- Timer WhileSubscribed timeout: 5000ms
- Default scroll hour in DaySchedule: 6 AM
- Pomodoro defaults: 5 min work, 1 min rest

## Type Converters & Serialization

**LocalDate:** Stored as Long (epoch day)
**LocalTime:** Stored as String (HH:mm format)

**File:** `data/Converters.kt` - Applied to AppDatabase

## Focus Mode Medal System

**Awards medals for 30-minute blocks:**

- Bronze: 30 mins
- Silver: 60 mins
- Gold: 90 mins
- Diamond: 120+ mins

**Time Banking:** User can bank accrued time to create Event with scheduled duration.

## Navigation Routes

```kotlin
sealed class VoxPlanScreen {
    object Main
    data class GoalEdit(val goalId: Int)
    object Progress
    data class DaySchedule(val date: String)
    data class FocusMode(val goalId: Int?, val eventId: Int?)
    data class Daily(val date: String, val newEventId: Int?)
}
```

**Bottom Navigation:** Goals (Main), Daily, Progress, Schedule

## Testing Strategy

**Current State:** Only example tests exist

**Priority Tests to Write:**

1. SharedViewModel.processGoals() - hierarchical processing logic
2. TodoRepository transaction operations
3. Event parent-child relationships and DAO queries
4. Quota active days encoding/decoding
5. FocusViewModel timer state management
6. Database migrations (especially 12→13)

## Documentation

**Comprehensive docs in /docs directory:**

- `ARCHITECTURE.md` - Complete architecture documentation (1,217 lines)
- `INCOMPLETE_FEATURES.md` - Detailed analysis of dailies/scheduling (572 lines)
- `FEATURES_SUMMARY.md` - Quick reference (428 lines)
- `DOCUMENTATION_INDEX.md` - Navigation guide for docs
- `DATA_MODELS_DOCUMENTATION.md` - Database schema details
- `VOXPLAN_UI_ARCHITECTURE.md` - UI component structure

**Start with ARCHITECTURE.md for deep understanding of the codebase.**

## Important Architectural Decisions

1. **Single Event Entity for Dailies + Scheduled Blocks** - Flexible design using `parentDailyId` to distinguish types. Simplifies data model but requires careful DAO queries.

2. **Manual DI Over Hilt** - Simpler, more transparent, no reflection overhead. All dependencies visible in AppContainer.

3. **SharedViewModel Pattern** - Single instance shared across multiple ViewModels for breadcrumb state. Avoids prop drilling and StateFlow duplication.

4. **StateFlow with 5-second timeout** - `SharingStarted.WhileSubscribed(5_000L)` balances reactivity with resource efficiency.

5. **Room TypeConverters for java.time** - LocalDate/LocalTime require custom converters. Always use these types, never String dates in code.

6. **Hierarchical Goals Max Depth 3** - Prevents infinite recursion and maintains UI simplicity. Enforced in SharedViewModel.processGoals().

7. **Foreign Key Cascades** - Quota → TodoItem with ON DELETE CASCADE. Deleting a goal auto-deletes its quota.

8. **Transaction-Based Reordering** - `updateItemsInTransaction()` ensures atomic updates when reordering multiple goals.

## Code Style Conventions

- ViewModels use `private val _state = MutableStateFlow()` + `val state = _state.asStateFlow()` pattern
- UI state classes are data classes with default values
- Repository methods that return data use `Flow<T>`, write operations use `suspend fun`
- Composables receive ViewModel, not individual state parameters
- Log tags use class name: `Log.d("MainViewModel", "...")`
- Package structure: by feature (ui.main, ui.calendar, ui.goals, etc.)

## Gradle Configuration

**Kotlin Version:** 1.9.0
**Compose Compiler:** 1.5.1
**Target/Compile SDK:** 34
**Min SDK:** 27 (Android 8.1)

**Key Dependencies:**

- Room: 2.6.1 (with KSP for annotation processing)
- Compose BOM: 2023.08.00
- Navigation Compose: 2.7.7
- Media3 ExoPlayer: 1.1.1 (for sound effects)

**Room Schema Export:** `app/schemas/` directory (configured in app/build.gradle.kts line 88-90)
