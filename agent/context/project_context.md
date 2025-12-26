# Project Context: VoxPlanApp

## High-Level Project Overview

VoxPlanApp is an Android productivity application that transforms how users manage time and achieve goals through hierarchical planning, quota-based time allocation, and gamified focus sessions. Unlike traditional task managers that treat all tasks as flat lists, VoxPlanApp recognizes that meaningful goals exist in nested hierarchies—ambitious life goals decompose into manageable projects, which break down further into daily actionable tasks.

The app provides four integrated modes of interaction that work together to support the complete lifecycle of goal achievement: **goal hierarchy management** for organizing objectives, **daily task planning** for translating goals into daily work, **time-block scheduling** for allocating specific time slots, and **focused work sessions** with real-time tracking and gamification to sustain concentration and measure progress.

**Current Version:** 3.2 (Dailies improved with parent/child Events)

**The Core Problem:** Time is the one truly limited resource. We all have the same 24 hours, but some people achieve far more than others. The difference lies in how time is organized, prioritized, and actually spent. VoxPlanApp addresses the fundamental challenge that setting goals alone isn't enough—those goals must be broken down, scheduled into your actual available time, and then consistently executed with focus.

**Key Differentiators:**
- **Hierarchical Goal Management**: Three levels deep (Goal → Subgoal → Task) preventing overwhelming complexity while enabling sophisticated planning
- **Quota-Based Planning**: Daily time allocations with active day patterns (e.g., "30 minutes Mon-Fri") that automatically populate your daily task list
- **Time Banking**: Accrued focus time that can be scheduled into your calendar, making productivity tangible and rewarding
- **Gamified Focus Sessions**: Medal system (Bronze → Silver → Gold → Diamond) that rewards sustained concentration in 30-minute increments
- **Integrated Workflow**: Seamless transitions from goals → daily tasks → scheduled blocks → focused execution → progress tracking

---

## Vision & Philosophy

### Long-Term Vision

VoxPlanApp aspires to become a conversational personal assistant—not just a passive tool waiting for input, but a proactive coach that guides users through planning strategies, helps prioritize activities, and actively supports execution like an always-available personal secretary. The future envisions **voice-driven intelligent interfaces** that extend our cognition, planning, reasoning, and ability to execute effective, goal-driven behaviors.

**Future Iterations (Planned Versions):**
- **Version 4**: Voice-controlled assistant for goal entry and scheduling
- **Version 5**: Cloud synchronization and desktop connectivity
- **Version 6**: Intelligent AI-driven process conducting "secretary conversations" to update app functioning based on user needs

### Design Philosophy

**1. Hierarchical Decomposition (Max Depth 3)**

The human mind struggles with both extremes: massive goals feel overwhelming, while endless flat task lists create decision paralysis. VoxPlanApp strikes a balance with three-level hierarchy:
- **Level 1 (Goals)**: Large ambitions (e.g., "Learn Guitar")
- **Level 2 (Subgoals)**: Concrete milestones (e.g., "Master Chord Progressions")
- **Level 3 (Tasks)**: Specific actions (e.g., "Practice G-C-D progression")

Why three levels? Deep enough for meaningful organization, shallow enough to avoid infinite tree traversal and cognitive overhead.

**2. Quota-Driven Time Allocation**

Goals without scheduled time rarely get done. VoxPlanApp's quota system answers: "How much time should I spend on this goal each day?" Quotas define:
- **Daily minute targets** (e.g., 60 minutes for "Programming")
- **Active days patterns** (e.g., Monday-Friday only)
- **Automatic daily population** (quota tasks appear in your daily list without manual entry)

This bridges the gap between aspirational goals and actual calendar commitment.

**3. The Power of Time Banking**

Every focused minute you spend becomes tangible through **time banking**—accrued time that can be visualized, reviewed, and scheduled. This creates:
- **Visible Progress**: See exactly how much effort you've invested in each goal
- **Motivation**: Medal awards (30min, 60min, 90min, 120min+) provide immediate feedback
- **Flexible Scheduling**: Banked time can be converted into scheduled calendar blocks

**4. Gamification Without Over-Optimization**

Focus mode uses medals to reward sustained concentration, but deliberately avoids excessive gamification that might lead to unhealthy behaviors:
- **Realistic Increments**: 30-minute minimum prevents constant context switching
- **Visual Feedback**: Simple medal icons (Bronze/Silver/Gold/Diamond) rather than complex point systems
- **Time-Based**: Rewards time spent, not arbitrary metrics

**5. Balance Between Structure and Flexibility**

VoxPlanApp provides structure (quotas, schedules) without rigidity:
- **Dailies**: Tasks you intend to work on today, without time constraints
- **Scheduled Blocks**: Specific time slots for focused work
- **Flexible Transition**: Convert daily tasks into scheduled blocks when ready

This accommodates different planning styles—some users schedule everything upfront, others prefer just-in-time scheduling throughout the day.

---

## Core Concepts (Domain Model)

### Goals and Subgoals

**Goals** represent the fundamental organizing unit of VoxPlanApp. Every goal can contain **subgoals**, which can themselves contain further subgoals, creating a hierarchical tree structure (maximum depth of 3).

**Conceptual Structure:**
```
🎯 Learn Guitar (Level 1 Goal)
├── 🎵 Master Chord Progressions (Level 2 Subgoal)
│   ├── ✓ Practice G-C-D progression (Level 3 Task)
│   └── ✓ Learn F chord fingering (Level 3 Task)
└── 🎵 Learn to Read Tablature (Level 2 Subgoal)
    ├── ⬜ Understand tab notation (Level 3 Task)
    └── ⬜ Play simple tab song (Level 3 Task)
```

**Key Properties:**
- **Completion Tracking**: Goals can be marked as completed
- **Order Management**: Goals have a defined sequence within their level
- **Breadcrumb Navigation**: When you navigate into subgoals, the app maintains a trail back to parents

**Why Three Levels Maximum?**
- Prevents infinite complexity and cognitive overload
- Maintains UI simplicity (nested views are manageable)
- Encourages meaningful decomposition (if you need more depth, your subgoal might actually be a separate goal)

### Quotas and Active Days

**Quotas** define how much time you commit to spending on a goal on a daily basis. They answer the question: "How many minutes should I work on this goal each day?"

**Key Quota Concepts:**
- **Daily Minute Target**: How long you intend to spend (e.g., 60 minutes)
- **Active Days Pattern**: Which days of the week the quota applies

**Active Days Encoding:**
A seven-character pattern representing Monday through Sunday:
- `"1111100"` = Active Monday-Friday (weekdays)
- `"0000011"` = Active Saturday-Sunday (weekends)
- `"1010101"` = Active Mon/Wed/Fri/Sun (custom pattern)

**Example:**
> Goal: "Programming"
> Quota: 90 minutes daily
> Active Days: `"1111100"` (Mon-Fri)
> **Result**: Every weekday, "Programming" appears in your daily task list with a 90-minute target

### Dailies (Daily Tasks)

**Dailies** are tasks you intend to work on today. They exist as **parent daily events** without specific time slots—just the intention to complete them on a given date.

**Conceptual Understanding:**
- **Parent Daily**: A task scheduled for today but not yet time-blocked
  - Example: "Programming - 90 minutes" on Monday, Feb 3rd
  - No start time, no end time—just a daily commitment
- **Scheduled Child**: Once you decide when to work on it, you schedule it into a specific time block (becomes a "scheduled event")

**Parent-Child Relationship:**
```
Parent Daily: "Programming" (Monday, no time)
├── Scheduled Child: "Programming" (Monday 9:00-10:00 AM)
└── Scheduled Child: "Programming" (Monday 2:00-3:00 PM)
```

Deleting the parent daily removes all scheduled children. Deleting a scheduled child preserves the parent daily.

**Daily Task Creation:**
1. **Manual**: Add a task directly to today's list
2. **From Quotas**: Click "Add Quota Tasks" to automatically populate from active quotas
3. **From Goals**: Create a goal and add it to today

### Events (Scheduled Blocks)

**Events** serve a dual purpose in VoxPlanApp: they represent both **dailies** (parent tasks) and **scheduled time blocks** (child tasks). This flexible design uses a single data structure for both concepts.

**Two Event Types:**
1. **Parent Daily Event**:
   - Has a date, no start/end times
   - Example: "Programming" scheduled for "today"

2. **Scheduled Event**:
   - Has a date, start time, and end time
   - Links back to parent daily
   - Example: "Programming" scheduled for "today 9:00 AM - 10:00 AM"

**Duration Tracking:**
- **Quota Duration**: Target time from the goal's quota (e.g., 90 minutes)
- **Scheduled Duration**: Total time allocated in scheduled blocks (e.g., 60 minutes scheduled so far)
- **Completed Duration**: Actual time spent (tracked through time banking)

**Recurrence (Planned, Not Yet Implemented):**
Events have support for recurring patterns (daily, weekly, monthly) but this feature is not yet active in the UI.

### Time Banking

**Time Banking** is the system that tracks actual time spent working on goals. When you complete a focus session, the minutes you worked are "banked" against that goal for that day.

**How It Works:**
1. Enter Focus Mode for a goal
2. Work for 30+ minutes (earning medals)
3. Complete the session
4. Click "Bank Time" to record your work
5. Time bank entry created: [Goal: Programming] [Date: Feb 3] [Duration: 90 minutes]

**Time Bank Benefits:**
- **Progress Visualization**: See how much time you've invested in each goal over the week
- **Diamond Awards**: Progress screen shows diamonds/stars when you hit quota targets
- **Scheduled Conversion**: Banked time can create scheduled events in your calendar

**Weekly Progress Tracking:**
The Progress screen aggregates time bank entries across the week, showing:
- Time spent per goal per day
- Weekly totals
- Quota completion status
- Visual indicators (diamonds for quota achieved)

### Focus Mode

**Focus Mode** transforms your device into a dedicated work timer, minimizing distractions and gamifying concentration through a medal reward system.

**Key Features:**
1. **Pomodoro-Style Timer**: Configurable work/rest periods (default: 25 minutes work, 5 minutes rest)
2. **Medal Awards**:
   - **Bronze**: 30 minutes of focused work
   - **Silver**: 60 minutes of focused work
   - **Gold**: 90 minutes of focused work
   - **Diamond**: 120+ minutes of focused work
3. **Discrete Tasks**: Check off specific tasks during focus session
4. **Time Banking**: Convert earned medals into time bank entries

**Focus Mode Workflow:**
```
User selects goal or event → Enter Focus Mode
     ↓
Configure timer settings (work/rest periods)
     ↓
Start timer → Work period begins
     ↓
Complete 30-minute block → Earn Bronze medal
     ↓
Continue working → Earn Silver (60min), Gold (90min), Diamond (120min+)
     ↓
Finish session → Bank time against goal
     ↓
Time bank entry created → Progress updated
```

**Why Medals?**
- **Immediate Feedback**: Visual reward for sustained focus
- **Progressive Achievement**: Each medal tier feels meaningful
- **Realistic Targets**: 30-minute minimum prevents unhealthy micro-optimization

### ActionMode (Reordering)

**ActionMode** is a user interface pattern for reordering goals or daily tasks without drag-and-drop gestures. It enables precise control over item positioning.

**Five ActionMode States:**
1. **Normal**: Default mode, no reordering active
2. **Vertical Up**: Move selected item up within current list
3. **Vertical Down**: Move selected item down within current list
4. **Hierarchy Up**: Promote selected item up one level in hierarchy
5. **Hierarchy Down**: Demote selected item down one level in hierarchy

**User Interaction Pattern:**
1. Click ActionMode button (e.g., "Move Up")
2. Button highlights, indicating mode is active
3. Click the goal/task you want to move
4. Item repositions according to active mode
5. ActionMode deactivates (or stays active for multiple operations)

**Use Cases:**
- **Vertical Reordering**: Prioritize goals in a list (move "Exercise" above "Reading")
- **Hierarchical Reordering**: Convert a task into a subgoal (or promote a subgoal to top-level goal)

### Breadcrumb Navigation

**Breadcrumb Navigation** maintains context as users traverse the goal hierarchy. As you navigate into subgoals, the app remembers your path and provides a visual trail back.

**Example Navigation:**
```
🏠 Goals                    (Root level)
   ↓ (User clicks "Learn Guitar")
🎯 Learn Guitar             (Level 1, breadcrumb: 🏠)
   ↓ (User clicks "Master Chord Progressions")
🎵 Master Chord Progressions (Level 2, breadcrumb: 🏠 → 🎯 Learn Guitar)
```

**Navigation Actions:**
- **Navigate Down**: Click a goal with subgoals to drill into it
- **Navigate Up**: Click breadcrumb trail to return to parent level
- **Jump to Root**: Click home breadcrumb to return to top-level goals

---

## Key User Workflows

### 1. Goal Management Workflow

**Objective**: Create, organize, and maintain hierarchical goals

**Step-by-Step Journey:**

1. **Creating Goals**:
   - User opens Goals screen (Main Screen)
   - Clicks "Add Goal" button
   - Enters goal title and description
   - (Optional) Sets quota with daily minutes and active days
   - Goal appears in list

2. **Organizing Hierarchy**:
   - User clicks a goal to navigate into its subgoals
   - Breadcrumb trail updates (e.g., 🏠 → Learn Guitar)
   - User adds subgoals within that context
   - Subgoals inherit relationship to parent automatically

3. **Reordering Goals**:
   - User clicks ActionMode button (Vertical Up/Down)
   - Button highlights to indicate active mode
   - User clicks goal to reorder
   - Goal moves up or down in list
   - Repeat for multiple items or deactivate mode

4. **Setting Quotas**:
   - User clicks "Edit" on a goal
   - Enters daily minute target (e.g., 60 minutes)
   - Selects active days pattern (e.g., Monday-Friday)
   - Saves changes
   - Goal now appears automatically in daily lists on active days

**Decision Points**:
- Should this be a top-level goal or a subgoal?
- How much time should I commit daily?
- Which days should I work on this?

### 2. Daily Planning Workflow

**Objective**: Review and prepare the day's work based on quotas and priorities

**Step-by-Step Journey:**

1. **Opening Daily View**:
   - User navigates to Dailies screen
   - Sees current date (e.g., "Today: Monday, Feb 3")
   - Reviews existing daily tasks (if any)

2. **Populating from Quotas**:
   - User clicks "Add Quota Tasks" button
   - App queries all active quotas for current day
   - Daily tasks automatically populate:
     - "Programming - 90 minutes" (from quota)
     - "Exercise - 30 minutes" (from quota)
     - "Reading - 45 minutes" (from quota)

3. **Reviewing Daily List**:
   - User sees visual quota progress indicators:
     - 🟦 = Scheduled time
     - 🟩 = Completed time
     - ⬜ = Remaining time
   - Example: "Programming [🟦🟦⬜] (60/90 mins scheduled)"

4. **Scheduling Tasks**:
   - User clicks "Schedule" button on a daily task
   - Time selection dialog appears
   - User selects start and end times (e.g., 9:00 AM - 10:00 AM)
   - Scheduled event created (daily task becomes parent, scheduled block is child)
   - Visual indicator updates to show scheduled time

5. **Vertical Reordering**:
   - User activates ActionMode (Vertical Up/Down)
   - Clicks tasks to reorder by priority
   - High-priority tasks move to top of list

**Decision Points**:
- Which tasks should I schedule vs. leave flexible?
- When during the day can I allocate time?
- Should I reorder by priority or leave as-is?

**Connection to Other Workflows**:
- Scheduled tasks appear in **Scheduling Workflow** (time-blocked calendar)
- Daily tasks can transition to **Focus Mode Workflow** (after scheduling)

### 3. Scheduling Workflow

**Objective**: Allocate specific time blocks throughout the day and adjust as needed

**Step-by-Step Journey:**

1. **Opening Schedule View**:
   - User navigates to Schedule screen
   - Sees time-based grid (1 AM - Midnight)
   - Hourly and half-hourly grid lines visible
   - Scheduled events appear as blocks at their designated times

2. **Viewing Scheduled Events**:
   - Events display as colored boxes with titles
   - Position indicates start time (vertical offset)
   - Height indicates duration (taller = longer)
   - Overlapping events appear side-by-side in columns

3. **Rescheduling Events**:
   - User clicks and drags an event box
   - Event moves vertically along timeline
   - Dragging snaps to 15-minute intervals
   - Release to confirm new time
   - Database updates with new start/end times

4. **Adjusting Duration**:
   - User clicks event to select it
   - Action toolbar appears above event
   - User clicks "+" or "-" buttons
   - Duration adjusts by 15-minute increments
   - Visual block height updates

5. **Entering Focus Mode**:
   - User clicks selected event
   - Action toolbar shows "Focus Mode" button
   - User clicks button
   - Navigates to Focus Mode screen with timer ready

6. **Deleting Events**:
   - User clicks delete button on event
   - Confirmation dialog appears
   - Options presented:
     - "Delete only this scheduled block"
     - "Delete this and parent daily task"
   - User confirms choice
   - Event removed from schedule

**Decision Points**:
- Should I reschedule this event to a different time?
- Is this block too long/short (adjust duration)?
- Should I start focus mode now or later?

**Connection to Other Workflows**:
- Events come from **Daily Planning Workflow** (scheduled tasks become events here)
- Focus Mode button connects to **Focus Mode Workflow**
- Completed time feeds into **Progress Tracking Workflow**

### 4. Focus Mode Workflow

**Objective**: Execute focused work sessions with time tracking and gamified rewards

**Step-by-Step Journey:**

1. **Entering Focus Mode**:
   - User arrives from Schedule screen (selected event) or Main screen (selected goal)
   - Focus Mode screen displays with:
     - Goal title at top
     - Timer in center (showing 0:00:00)
     - Medal slots (empty, ready to fill)
     - Discrete task checklist (optional)

2. **Configuring Timer** (Optional):
   - User adjusts work/rest periods
   - Default: 25 minutes work, 5 minutes rest
   - Custom: Any duration (e.g., 50 minutes work, 10 minutes rest)

3. **Starting Session**:
   - User clicks "Start" button
   - Timer begins counting up
   - Visual clock animation rotates (progress = time/30 minutes)
   - User works on goal, minimizing distractions

4. **Earning Medals**:
   - **30 minutes**: Bronze medal awarded, visual feedback
   - **60 minutes**: Silver medal awarded
   - **90 minutes**: Gold medal awarded
   - **120+ minutes**: Diamond medal awarded
   - Power bar fills incrementally (1 bar = 60 minutes)

5. **Discrete Task Completion** (Optional):
   - During session, user checks off specific tasks
   - Example: "Write introduction paragraph" ✓
   - Task completion adds bonus time (e.g., 15 minutes)

6. **Pausing/Resuming**:
   - User clicks "Pause" if interrupted
   - Timer stops, progress preserved
   - Click "Resume" to continue
   - Medals remain intact

7. **Ending Session**:
   - User clicks "Stop" when done
   - Summary appears:
     - Total time worked
     - Medals earned
     - Discrete tasks completed
   - User clicks "Bank Time"
   - Time bank entry created for this goal/date

**Decision Points**:
- Should I pause or push through this distraction?
- Have I worked long enough to bank time?
- Should I continue to the next medal tier?

**Connection to Other Workflows**:
- Banked time appears in **Progress Tracking Workflow**
- Time bank can create scheduled events in **Scheduling Workflow**

### 5. Progress Tracking Workflow

**Objective**: Review time spent across the week and assess quota achievement

**Step-by-Step Journey:**

1. **Opening Progress View**:
   - User navigates to Progress screen
   - Sees weekly calendar grid:
     - Rows = Goals with quotas
     - Columns = Days of the week (Mon-Sun)

2. **Reviewing Weekly Progress**:
   - Each cell shows:
     - Time spent on that goal on that day
     - Visual indicator (diamond/star if quota achieved)
   - Example:
     ```
     Programming:  Mon [90★] Tue [60] Wed [90★] Thu [45] Fri [90★]
     Exercise:     Mon [30★] Tue [30★] Wed [0] Thu [30★] Fri [30★]
     ```

3. **Identifying Gaps**:
   - User sees days with zero time (missed commitments)
   - User sees days below quota (partial completion)
   - User sees days with diamonds (quota achieved)

4. **Weekly Totals**:
   - Bottom row shows total time per goal for the week
   - Example: "Programming: 375 minutes this week (target: 450)"

5. **Adjusting Strategy**:
   - User decides to adjust quotas if consistently missing targets
   - Or recommits to scheduling more consistently
   - Or celebrates consistent achievement

**Connection to Other Workflows**:
- Time data comes from **Focus Mode Workflow** (time banking)
- Insights inform next week's **Daily Planning Workflow**

---

## Challenges & Design Decisions

### 1. Single Event Entity for Dailies and Scheduled Blocks

**Challenge**: Should dailies and scheduled events be separate database tables?

**Decision**: Use a single Event entity with a `parentDailyId` field to distinguish types.

**Rationale**:
- **Flexibility**: Same data structure handles both concepts
- **Simplicity**: One entity, one set of queries
- **Relationship Management**: Parent-child relationship is explicit (parentDailyId = NULL → daily, parentDailyId = X → scheduled)
- **Cascading Deletes**: Foreign key relationship makes deletion logic straightforward

**Tradeoffs**:
- **Query Complexity**: Must filter by `parentDailyId IS NULL` vs `IS NOT NULL`
- **Nullable Fields**: Daily events have null startTime/endTime, which requires careful handling

**User Experience Impact**: Users don't see this complexity—they just experience seamless transitions from daily tasks to scheduled blocks.

### 2. Hierarchical Goal Management Complexity

**Challenge**: How deep should goal hierarchies go? How do users navigate complex trees?

**Decision**: Maximum depth of 3 levels with breadcrumb navigation.

**Rationale**:
- **Cognitive Load**: Three levels balances sophistication and simplicity
- **UI Simplicity**: Nested views are manageable with breadcrumbs
- **Performance**: Recursive processing up to depth 3 is efficient
- **User Clarity**: Deep hierarchies become confusing (if you need more, create separate goals)

**Tradeoffs**:
- **Limit on Complexity**: Users with extremely detailed planning may feel constrained
- **Enforcement Logic**: Code must prevent adding subgoals beyond level 3

**User Experience Impact**: Most users find three levels sufficient. Power users may initially desire more but adapt once they experience the clarity.

### 3. Balancing Quota Rigidity with Daily Flexibility

**Challenge**: How do we provide structure (quotas) without making users feel imprisoned by rigid schedules?

**Decision**: Two-tier system—daily tasks (flexible) and scheduled blocks (time-bound).

**Rationale**:
- **Quota as Guidance**: Quotas populate daily lists but don't force scheduling
- **User Autonomy**: Users decide when (or if) to schedule daily tasks
- **Accommodates Styles**: Structured planners schedule everything; flexible planners leave tasks unscheduled until ready

**Tradeoffs**:
- **Potential Procrastination**: Users can ignore daily tasks without scheduling
- **Complexity**: Two layers (dailies + schedule) adds conceptual overhead

**User Experience Impact**: Most users appreciate the flexibility—quotas remind them what's important without dictating when to do it.

### 4. Focus Mode Gamification Balance

**Challenge**: How do we motivate sustained focus without encouraging unhealthy optimization (e.g., working excessively to earn medals)?

**Decision**: Time-based medals with realistic thresholds and no excessive rewards.

**Rationale**:
- **Realistic Increments**: 30-minute minimum reflects actual focus blocks
- **Diminishing Returns**: Diamond (120+ mins) is the cap—no benefit to extreme marathon sessions
- **Visual Only**: Medals are visual feedback, not currency or points to accumulate
- **Completion Bonus**: Discrete task completion adds small bonus (15 mins) to encourage finishing tasks

**Tradeoffs**:
- **Limited Dopamine**: Some users may want more elaborate gamification
- **No Leaderboards**: No social competition features

**User Experience Impact**: Gamification feels motivating without becoming addictive or unhealthy.

### 5. Parent-Child Event Relationship Complexity

**Challenge**: When deleting a scheduled event, should the parent daily also be deleted? What about other scheduled children?

**Decision**: Prompt user with choice—delete only child or delete parent (and all children).

**Rationale**:
- **User Control**: User decides whether daily task is still relevant
- **Prevent Orphans**: If all scheduled children are deleted, daily task may become meaningless
- **Clear Intent**: Dialog explains consequences before deletion

**Tradeoffs**:
- **Extra Clicks**: Users must answer dialog each time
- **Potential Confusion**: Some users may not understand parent-child relationship

**User Experience Impact**: Advanced users appreciate the control; beginners may need education about the relationship.

---

## MVP/MLP Definition

### Current State: Version 3.2

**Major Milestones Achieved:**
- **v1.0-1.6** (Jun-Jul): Core goal hierarchy, breadcrumb navigation, multi-layer goals
- **v2.1-2.7** (Jul-Sep): Schedule screen, focus mode, power bar, time banking, medal awards
- **v3.0-3.2** (Dec-Jan): Daily quotas, dailies screen, parent-child events

**What's Working Well:**
- ✅ Goal hierarchy management with breadcrumb navigation
- ✅ Quota system with active days pattern
- ✅ Focus mode with medal awards and time banking
- ✅ Daily task population from quotas
- ✅ Scheduling tasks into time blocks
- ✅ Drag-to-reschedule in schedule view
- ✅ Progress tracking with weekly summaries
- ✅ Parent-child event relationship

### Incomplete Features Analysis

#### 1. Dailies Feature (~70% Complete)

**What's Implemented:**
- Daily task list display with date navigation
- "Add Quota Tasks" automatic population
- Quota progress indicators (scheduled/completed/remaining)
- Vertical reordering with ActionMode
- Scheduling dailies into time blocks
- Delete confirmation with parent-child awareness

**What's Missing:**
- ❌ **Completion Tracking**: No checkboxes to mark daily tasks as "done"
- ❌ **Direct Focus Mode Access**: Must go to Schedule first, can't enter focus from dailies
- ❌ **Bulk Operations**: Can't select multiple tasks and schedule/delete together
- ❌ **Quick-Reschedule**: Can't modify existing scheduled blocks from daily screen
- ❌ **Task Persistence**: Daily tasks may not maintain state properly across app restarts
- ❌ **Smart Scheduling**: No AI suggestions for optimal scheduling times
- ❌ **Notifications**: No reminders when scheduled dailies are about to start

**Critical Bugs:**
- ⚠️ QuickScheduleScreen completely commented out (intended for fast scheduling from main screen, references undefined variables)

#### 2. Scheduling Feature (~65% Complete)

**What's Implemented:**
- Time-based grid display (1 AM - Midnight)
- Event boxes positioned by start/end times
- Drag-to-reschedule with 15-minute snapping
- Overlapping event detection and column layout
- Action toolbar (focus mode, duration adjustment, delete)
- Date navigation with scroll to 6 AM default

**What's Missing:**
- ❌ **Event Creation from Schedule**: Can't tap empty space to create new event
- ❌ **Recurrence Support**: Event model has recurrence fields but UI doesn't use them
- ❌ **Week/Month View**: Only day view implemented
- ❌ **Multi-Day Events**: Events can't span across days
- ❌ **Color-Coding by Goal**: All events same color, no visual distinction
- ❌ **Conflict Detection**: Can schedule overlapping events without warning
- ❌ **Event Templates**: Can't save recurring patterns or repeat schedules
- ❌ **In-Place Editing**: Can only adjust duration from toolbar, can't edit title/goal
- ❌ **Completion Status**: Can't mark events as completed in schedule view
- ❌ **Scroll Position Persistence**: Returns to 6 AM each day, doesn't remember position

**Critical Bugs:**
- 🚨 **Delete Dialog Bug**: DaySchedule.kt lines 110-126 reference undefined `event` variable in confirmation dialog

### MVP Success Criteria

**Core Features Required for Initial Value Delivery:**

1. **Goal Management**: ✅ Complete
   - Create/edit/delete hierarchical goals
   - Breadcrumb navigation
   - Quota settings with active days

2. **Daily Planning**: ⚠️ Mostly Complete (Missing completion tracking)
   - Populate dailies from quotas
   - Schedule tasks into time blocks
   - Visual quota progress

3. **Time-Block Scheduling**: ⚠️ Mostly Complete (Critical delete bug)
   - Day view with hourly grid
   - Drag-to-reschedule
   - Duration adjustment
   - Focus mode access

4. **Focus Mode**: ✅ Complete
   - Timer with configurable work/rest periods
   - Medal awards (Bronze/Silver/Gold/Diamond)
   - Time banking
   - Discrete task completion

5. **Progress Tracking**: ✅ Complete
   - Weekly time bank summaries
   - Quota achievement indicators
   - Per-goal time totals

### Features Explicitly Deferred for Future Iterations

**Version 4 (Voice Control):**
- Voice recognition for goal entry
- Voice-driven scheduling
- Conversational AI assistant

**Version 5 (Cloud Sync):**
- Cloud storage of goals/quotas/time bank
- Desktop version connectivity
- Multi-device synchronization

**Version 6 (Intelligent AI):**
- AI-driven scheduling suggestions
- "Secretary conversation" feature
- Adaptive learning from user behavior

**Lower Priority (No Version Assigned):**
- Event recurrence (weekly/monthly patterns)
- Week/month calendar views
- Event color-coding by goal
- Smart scheduling recommendations
- Break time visualization
- Notification/reminder system
- Export to calendar formats

### Next Iteration Priorities

**Immediate (Pre-v4.0):**
1. **Fix Critical Bugs**:
   - Delete dialog bug in DaySchedule.kt (lines 110-126)
   - QuickScheduleScreen undefined variables

2. **Complete Dailies Feature**:
   - Add completion checkboxes for daily tasks
   - Enable direct focus mode access from dailies
   - Implement basic bulk operations (multi-select delete)

3. **Polish Scheduling**:
   - Add event creation by tapping empty schedule space
   - Implement basic color-coding by goal
   - Add scroll position persistence

4. **User Experience Enhancements**:
   - Error handling and user feedback improvements
   - Tutorial/onboarding for new users
   - Performance optimization for large event lists

---

## Glossary

**Action Mode**: A user interface state that enables reordering goals or daily tasks. Five states: Normal, Vertical Up, Vertical Down, Hierarchy Up, Hierarchy Down.

**Active Days**: A pattern defining which days of the week a quota applies, encoded as a seven-character string (e.g., "1111100" = Mon-Fri).

**Breadcrumb Navigation**: A visual trail showing the user's path through the goal hierarchy, allowing navigation back to parent levels.

**Completion Duration**: The actual time spent on a goal or event, tracked through time banking (in minutes).

**Daily (Daily Task)**: A task scheduled for a specific day without a designated time slot (parent event with no start/end time).

**Diamond Award**: The highest focus mode medal, earned for 120+ minutes of sustained work.

**Discrete Task**: A specific checkable item within a focus session (e.g., "Write introduction paragraph").

**Event**: A data entity representing either a daily task (parent event) or a scheduled time block (child event).

**Focus Mode**: A timer-based work session with gamified rewards (medals) and time tracking.

**Goal**: A hierarchical organizing unit representing an objective, project, or task. Can contain subgoals up to depth 3.

**Hierarchy Level**: The depth of a goal in the tree structure (Level 1 = top-level goal, Level 2 = subgoal, Level 3 = task).

**Medal**: A visual reward earned during focus sessions based on time spent (Bronze = 30min, Silver = 60min, Gold = 90min, Diamond = 120min+).

**Parent Daily**: An event without start/end times, representing a daily task. Has `parentDailyId = null`.

**Parent-Child Relationship**: The link between a daily task (parent) and its scheduled time blocks (children). Children have `parentDailyId` pointing to parent.

**Power Bar**: A visual indicator showing focus time progress, where one full bar equals 60 minutes.

**Progress Tracking**: A weekly summary view showing time spent per goal per day with quota achievement indicators.

**Quota**: A daily time commitment for a goal, defining how many minutes should be spent and on which days of the week.

**Quota Duration**: The target time allocation from a goal's quota setting (in minutes).

**Scheduled Block (Scheduled Event)**: An event with specific start and end times, representing a time-blocked calendar entry. Has `parentDailyId` set.

**Scheduled Duration**: The total time allocated in scheduled blocks for a daily task (in minutes).

**Subgoal**: A goal nested under a parent goal, representing a milestone or component of the larger objective.

**Time Bank**: A record of actual time spent on goals, created when focus sessions are completed. Enables progress tracking and quota verification.

**Time Banking**: The act of recording completed focus time into the time bank for a specific goal and date.

---

## Document Metadata

**Generated**: December 19, 2025
**Project Version**: VoxPlanApp 3.2 (Dailies improved with parent/child Events)
**Purpose**: High-level project context for developers, AI assistants, and stakeholders
**Related Documents**:
- `codebase_context.md` - Technical implementation details (to be generated)
- `docs/LLM-Generated/ARCHITECTURE_DOCUMENTATION_SUMMARY.txt` - Architecture overview
- `docs/LLM-Generated/INCOMPLETE_FEATURES.md` - Detailed incomplete feature analysis

**For Technical Implementation Details**: Refer to `codebase_context.md` for ViewModels, data flows, architecture patterns, and code-level guidance.
