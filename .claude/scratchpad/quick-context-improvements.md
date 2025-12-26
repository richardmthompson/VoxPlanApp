# Improvements for /quick-context Command

**Date**: 2025-12-19
**Based on**: Focus Mode Dailies Bug Analysis
**Purpose**: Document enhancements needed for quick-context.md to produce better scratchpads

---

## Key Gaps Identified

During the Focus Mode dailies duplication bug analysis, we discovered that the current `/quick-context` command template lacks several critical elements that would help produce comprehensive, understandable scratchpads.

---

## 1. Missing: "Background and Motivation" Depth Requirement

**Current**: Just says "Background and Motivation (why this work)"

**Problem**: Doesn't provide enough architectural context for someone unfamiliar with the codebase

**Needed**: Based on user feedback, we need much more context about:
- Architecture overview (how does this system work?)
- Workflow explanations (how do users interact with it?)
- System design patterns (parent-child relationships, dual-purpose entities)
- Data flow (where does data come from/go to?)

**Suggested Addition to quick-context.md**:

```markdown
### Background and Motivation - Expanded Requirements

**Purpose**: Provide enough architectural context that someone unfamiliar with the codebase can understand the bug/feature without reading code.

**Required subsections**:

1. **System Architecture Overview** (2-3 paragraphs):
   - What is this component/feature?
   - How does it fit into the larger application?
   - What architectural patterns does it use?
   - Example: "VoxPlanApp uses MVVM with Room database. The Event entity serves dual purposes..."

2. **User Workflows** (bullet list or narrative):
   - How do users interact with this feature?
   - What are the entry points?
   - What are the common paths through the system?
   - Example: "Users can enter Focus Mode through two paths: (1) From MainScreen (ad-hoc)..."

3. **Data Model Context** (if applicable):
   - What entities/tables are involved?
   - What relationships exist between them?
   - What are the key fields and their meanings?
   - Example: "The app uses a single Event entity for two purposes, distinguished by parentDailyId field..."

4. **The Problem/Opportunity**:
   - What is broken or missing?
   - Why does it matter?
   - What is the user impact?
   - Include concrete example scenario showing the bug in action

5. **Why This Happens** (for bugs):
   - What was the original intent of the code?
   - What assumption or requirement changed?
   - Why does current implementation fail?

6. **Root Cause** (for bugs):
   - What specific code/logic causes the issue?
   - File paths and line numbers
   - Why was it implemented this way originally (if discoverable)?

**Quality Test**:
- Can a developer unfamiliar with this codebase understand the problem after reading Background section alone?
- If NO → Add more architectural context

**Examples**:

✅ **GOOD Background Section**:
```
### Background and Motivation

#### VoxPlanApp Architecture Overview

VoxPlanApp is an Android productivity app with hierarchical goal management, time tracking,
daily quotas, scheduling, and gamified focus sessions. The app uses:
- MVVM architecture with manual dependency injection
- Room database for local persistence
- Jetpack Compose for UI
- Kotlin Flow/StateFlow for reactive data

#### The Parent-Child Event System

The app uses a single Event entity for two distinct purposes, distinguished by a parentDailyId field:

1. Parent Dailies (parentDailyId = null):
   - Unscheduled daily tasks that appear in "Dailies" screen
   - Created explicitly by user
   - Do NOT have specific start/end times initially

2. Scheduled Child Events (parentDailyId = <parent_id>):
   - Time-blocked versions of parent dailies
   - Appear in day scheduler (calendar view)
   - HAVE specific start/end times

[Continue with workflows, problem description, etc.]
```

❌ **BAD Background Section**:
```
### Background and Motivation

Focus Mode creates duplicate entries in Dailies. This is caused by bankTime() function
in FocusViewModel.kt creating Events without parentDailyId set.
```

**Why bad**: Jumps straight to technical details without explaining:
- What is Focus Mode?
- What is Dailies?
- What is the Event/parentDailyId system?
- Why does this matter to users?
```

---

## 2. Missing: Multi-Solution Option Analysis

**Current**: Assumes single solution path

**Problem**: For bugs and features, there are often multiple valid approaches with different tradeoffs. Current template doesn't encourage exploring options.

**Needed**: Document multiple solution paths, analyze tradeoffs, recommend one approach

**Suggested Addition to quick-context.md**:

```markdown
### Phase 3.5: Solution Options Analysis (REQUIRED for bugs and features)

**When**: After understanding the problem but BEFORE writing Implementation Steps

**Purpose**: Explore multiple solution approaches, document tradeoffs, choose best path

**For Bugs**, consider at least:
1. **Quick fix**: Minimal change, addresses symptom (may not fix root cause)
2. **Proper fix**: Addresses root cause (may be more complex)
3. **Future-proof fix**: Prevents similar issues (may require architecture changes)

**For Features**, consider at least:
1. **Minimal viable implementation**: Simplest version that works
2. **Full-featured implementation**: Complete solution with all bells and whistles
3. **Phased approach**: Deliver in stages (MVP now, enhancements later)

**Documentation Format in Scratchpad**:

```
### Solution Options

**Option A: [Descriptive Name]**
- **Description**: [What changes, 1-2 sentences]
- **Pros**: [Benefits - bullet list]
- **Cons**: [Drawbacks - bullet list]
- **Complexity**: [Tier 1/2/3]
- **Estimated Time**: [duration]

**Option B: [Descriptive Name]**
- **Description**: [What changes]
- **Pros**: [Benefits]
- **Cons**: [Drawbacks]
- **Complexity**: [Tier 1/2/3]
- **Estimated Time**: [duration]

**Option C: [Descriptive Name]** (if applicable)
- **Description**: [What changes]
- **Pros**: [Benefits]
- **Cons**: [Drawbacks]
- **Complexity**: [Tier 1/2/3]
- **Estimated Time**: [duration]

**Recommendation**: Option [X] because [rationale - 2-3 sentences]
**This plan implements**: Option [X]
**Future consideration**: Option [Y] for [reason] (if applicable)
```

**Example** (from Focus Mode bug):

```
### Solution Options

**Option A: Simple Fix (Remove Auto-Creation)**
- **Description**: Remove Event creation from bankTime() and createOrUpdateEvent() entirely
- **Pros**:
  - Simple, clean separation of concerns
  - Fixes bug immediately
  - No database migration required
  - Low risk
- **Cons**:
  - Loses retrospective "what did I actually do" feature
  - No timeline view of actual work
- **Complexity**: Tier 1 (Simple)
- **Estimated Time**: 30 minutes

**Option B: Enhanced Retrospective Logging**
- **Description**: Add isVisible/isUserCreated flags to Event entity, filter Dailies query
- **Pros**:
  - Preserves retrospective logging in calendar
  - Keeps Dailies clean for planning
  - Foundation for productivity analytics
  - Scheduled vs actual time tracking
- **Cons**:
  - Requires database migration (v13→v14)
  - More complex event lifecycle logic
  - UI work for visual differentiation
  - Settings management needed
- **Complexity**: Tier 3 (Complex)
- **Estimated Time**: 15-20 hours

**Option C: Hybrid Approach**
- **Description**: Implement Option A now, plan Option B as separate feature
- **Pros**:
  - Fixes immediate bug quickly
  - Allows thoughtful design of retrospective feature
  - De-risks migration
- **Cons**:
  - Two-phase implementation
  - May create temporary user confusion if they liked auto-logging
- **Complexity**: Tier 1 now, Tier 3 later
- **Estimated Time**: 30 mins + future work

**Recommendation**: Option A (Simple Fix) because it immediately resolves the Dailies
pollution bug with minimal risk and no migration. If retrospective logging proves
valuable to users, Option B can be implemented as a well-designed feature in v3.4.

**This plan implements**: Option A
**Future consideration**: Option B if users request "show me what I actually worked on" feature
```
```

---

## 3. Missing: "Why This Happens" Analysis

**Current**: Focuses on "what" and "how to fix"

**Problem**: Without understanding original intent, might break something or miss better solution

**Needed**: Understanding WHY the buggy code exists helps:
- Avoid breaking intended functionality
- Identify if it's a bug or feature request misunderstanding
- Find better solutions that preserve original intent

**Suggested Addition to quick-context.md**:

```markdown
### Root Cause Analysis - "Why This Happens" (REQUIRED for bugs)

**When**: During Phase 1 research, after identifying buggy code

**Purpose**: Understand original developer intent to avoid unintended consequences

**Questions to answer**:
1. **Original intent**: What was this code trying to accomplish?
2. **Why it made sense**: What requirement/use case justified this approach?
3. **What changed**: Why is it now a bug? (requirements changed, edge case discovered, user expectation shifted?)
4. **Unintended consequences**: What assumptions were violated?
5. **Key insight**: What is the fundamental issue? (often: missing abstraction, wrong layer, conflated concerns)

**How to research**:
- Check git blame for original commit message
- Look for related comments in code
- Search for related issues/PRs
- Read surrounding code context
- Check documentation for feature history

**Documentation in Scratchpad**:

```
### Why This Happens (Current Implementation Issues)

**Likely original intent**: [What the developer was trying to achieve]

**Why current implementation fails**:
1. [Specific failure mode 1]
2. [Specific failure mode 2]
3. [Specific failure mode 3]

**The core issue**: [Root cause explanation - why the implementation doesn't match intent]
```

**Example** (from Focus Mode bug):

```
### Why This Happens (Current Implementation Issues)

**Likely original intent**: Create Events retroactively to show what the user actually
did in the day scheduler - a "post-log" of actual time spent, so users can see their
day in retrospect.

**Why current implementation fails**:

1. **Pollutes Dailies UI**: Events appear in Dailies list (where users plan their day),
   not just in DaySchedule (where they view their calendar)
2. **Creates entries even with no meaningful time**: If user enters Focus Mode and
   doesn't log time, or logs <10 minutes, an entry may still be created
3. **No distinction between planned vs actual**: There's no way to visually distinguish between:
   - Tasks the user explicitly added to their Dailies (planning)
   - Tasks auto-created from Focus Mode sessions (retrospective logging)
4. **Multiple entries for same goal**: If user does 3 separate focus sessions on
   "Programming", Dailies shows 3 entries

**The core issue**: These auto-created Events use `parentDailyId = null`, making them
indistinguishable from user-created parent dailies. The single Event entity serves
dual purposes (planning vs actuals) but lacks a visibility control mechanism.
```
```

---

## 4. Missing: Edge Cases and Failure Modes

**Current**: Not explicitly required

**Problem**: Production code needs to handle edge cases. Without explicit prompt, developers might skip this analysis.

**Needed**: Systematic edge case identification before implementation

**Suggested Addition to quick-context.md**:

```markdown
### Phase 2.5: Edge Cases & Failure Modes (REQUIRED)

**When**: After understanding solution but BEFORE writing Implementation Steps

**Purpose**: Identify potential failures, boundary conditions, race conditions

**Minimum requirement**: Document 3-5 edge cases for any production code change

**Brainstorming Template**:

1. **Boundary conditions**:
   - What happens at limits? (0, null, max, negative values)
   - Empty collections? Single item? Maximum items?
   - Start/end of time periods?

2. **Concurrent operations**:
   - What if user does X while Y is happening?
   - What if same operation called twice simultaneously?
   - What if data changes during operation?

3. **State transitions**:
   - What if system is in unexpected state when this runs?
   - What if prerequisite data is missing?
   - What if operation partially completes?

4. **External failures**:
   - What if network/disk/permission fails?
   - What if database write fails?
   - What if migration fails mid-process?

5. **Race conditions**:
   - What if timing changes?
   - What if operations happen in different order?
   - What if user acts faster than expected?

6. **User errors**:
   - What if user provides invalid input?
   - What if user cancels mid-operation?
   - What if user does something unexpected but not prevented by UI?

**Documentation Format in Scratchpad**:

```
## Edge Cases & Failure Modes

| Edge Case | Expected Behavior | Implementation Notes |
|-----------|-------------------|---------------------|
| [Specific scenario] | System should [expected outcome] | [How to handle: code approach, line reference] |
| [Specific scenario] | System should [expected outcome] | [How to handle] |
```

**Example** (from Focus Mode bug):

```
## Edge Cases & Failure Modes

| Edge Case | Expected Behavior | Implementation Notes |
|-----------|-------------------|---------------------|
| Manual daily exists, then ad-hoc focus session | Two separate entries: (1) Daily visible in Dailies, (2) Auto-event hidden from Dailies but visible in DaySchedule. Link via parentDailyId. | findRelatedDaily() sets parentDailyId to link them |
| Scheduled 9-10am, actual work 9:15-10:45 | Update existing event: startTime=9:00 (scheduled), actualStartTime=9:15, actualEndTime=10:45. Show both times in UI. | createOrUpdateEvent() preserves scheduled times, sets actual times |
| App crash during focus session | No event created (requires explicit exit trigger). TimeBank entries already saved. | Acceptable: time tracked in TimeBank, no orphaned events |
| Device time/timezone change mid-session | Use system time at entry/exit. Accept as source of truth. | Document limitation: rare edge case, low impact |
| User re-enters Focus Mode same goal same day | Create new event for each session. No aggregation. | Each entry/exit pair = separate session, preserves timeline integrity |
| Focus session <10 mins | Skip event creation. TimeBank still records if banked. | Intentional: short sessions don't clutter calendar |
| bankTime() AND onExit() both trigger | hasCreatedEventThisSession flag prevents duplicate. | Critical fix: set flag in bankTime(), check in onExit() |
```
```

---

## 5. Missing: Migration/Backward Compatibility Check

**Current**: Not mentioned

**Problem**: Database/API changes can break production. Need explicit compatibility analysis.

**Needed**: Checklist for migration and breaking change impact

**Suggested Addition to quick-context.md**:

```markdown
### Phase 2: Compatibility Check (REQUIRED before Implementation Steps)

**Purpose**: Identify migration needs, breaking changes, rollback requirements

**Database Changes Checklist**:
- [ ] Schema changes? → Requires migration script
- [ ] New columns with default values? → Backward compatible
- [ ] New columns without defaults? → NOT backward compatible
- [ ] Null constraints added? → Check existing data impact
- [ ] Foreign keys added/modified? → Verify cascade behavior
- [ ] Indices added? → Migration performance impact
- [ ] Column renamed/removed? → Breaking change, requires table recreation

**API Changes Checklist**:
- [ ] Function signature changes? → Breaking change for callers
- [ ] New required parameters? → Provide defaults or version API
- [ ] Removed functionality? → Deprecation path needed
- [ ] Return type changes? → Breaking change
- [ ] Exception types changed? → Breaking change

**Data Migration Checklist**:
- [ ] Existing data affected? → Migration script to transform data
- [ ] Data loss possible? → Backup strategy required
- [ ] Rollback plan exists? → How to undo changes
- [ ] Migration tested? → Test on copy of production data

**Document in Context section**:

```
### Compatibility

- **Migration required**: Yes/No
  - If yes: Migration version X→Y
  - Migration script: [description or file reference]
- **Backward compatible**: Yes/No/Partial
  - If no: [what breaks]
- **Rollback strategy**: [how to undo if production issues]
  - Code rollback: [steps]
  - Data rollback: [steps if applicable]
- **Testing requirements**:
  - [ ] Test migration on v[X] database
  - [ ] Verify existing data preserved
  - [ ] Test rollback procedure
```

**Example** (from Option B design):

```
### Compatibility

- **Migration required**: Yes
  - Migration v13→v14
  - Adds 4 columns to Event table: eventSource (TEXT), isVisibleInDailies (INTEGER),
    actualStartTime (TEXT), actualEndTime (TEXT)
  - All columns have DEFAULT values → backward compatible at schema level

- **Backward compatible**: Yes (with defaults)
  - Existing Events get eventSource='USER_CREATED', isVisibleInDailies=1
  - No data loss
  - Existing queries unchanged (Dailies filter automatically excludes isVisibleInDailies=false)

- **Rollback strategy**:
  - **Code rollback**: Revert commits, set isVisibleInDailies=true for new events
  - **Data rollback** (nuclear option): MIGRATION_14_13_DOWNGRADE recreates table
    without new columns (loses auto-logged events and actual time data)
  - Recommend: Code rollback only unless critical data corruption

- **Testing requirements**:
  - [ ] Test migration on v13 database with 100+ existing events
  - [ ] Verify all existing events marked USER_CREATED and visible
  - [ ] Test app functions normally after migration
  - [ ] Test rollback procedure on non-production database
```
```

---

## 6. Enhanced Scratchpad Template

**Complete updated template** incorporating all improvements:

```markdown
# [Feature/Bug Name]

**Date**: YYYY-MM-DD
**Tier**: [1 (Simple) / 2 (Medium) / 3 (Complex)]
**Confidence**: [High / Medium / Low]

---

## Background and Motivation

### [Project Name] Architecture Overview

[2-3 paragraphs explaining:
- What is this application/system?
- What tech stack/architecture patterns?
- How does this component fit into the larger system?]

### [Relevant Subsystem] Design Pattern

[If applicable, explain key design patterns:
- Data models and relationships
- Workflow patterns
- State management approaches
- Example: "The app uses a single Event entity for two purposes..."]

### User Workflows

[Explain how users interact with this feature:
- Entry points
- Common paths
- User expectations
- Example scenarios]

### The Problem/Opportunity

[What's wrong or missing:
- User impact
- Concrete example scenario showing the issue
- Why it matters]

### Why This Happens (for bugs)

**Likely original intent**: [What was the code trying to achieve]

**Why current implementation fails**:
1. [Specific failure mode 1]
2. [Specific failure mode 2]
3. [Specific failure mode 3]

**The core issue**: [Root cause - fundamental problem]

### Root Cause

[Specific code/logic causing issue:
- File paths with line numbers
- What the code does
- Why it causes the problem]

### Solution Options

**Option A: [Descriptive Name]**
- **Description**: [What changes, 1-2 sentences]
- **Pros**:
  - [Benefit 1]
  - [Benefit 2]
- **Cons**:
  - [Drawback 1]
  - [Drawback 2]
- **Complexity**: Tier [1/2/3]
- **Estimated Time**: [duration]

**Option B: [Descriptive Name]**
- **Description**: [What changes]
- **Pros**:
  - [Benefit 1]
  - [Benefit 2]
- **Cons**:
  - [Drawback 1]
  - [Drawback 2]
- **Complexity**: Tier [1/2/3]
- **Estimated Time**: [duration]

**Option C: [Descriptive Name]** (if applicable)
- **Description**: [What changes]
- **Pros**: [Benefits]
- **Cons**: [Drawbacks]
- **Complexity**: Tier [1/2/3]
- **Estimated Time**: [duration]

**Recommendation**: Option [X] because [rationale - 2-3 sentences explaining why this is best choice]

**This plan implements**: Option [X]

**Future consideration**: Option [Y] for [reason] (if applicable - when might we revisit other options)

---

## Feature Goal

[Single sentence: what this accomplishes after implementation]

---

## Context

### Affected Files
- `path/to/file.ext:line-range` (description of what changes)
- `path/to/file2.ext:line-range` (description of what changes)

### Pattern to Follow
- **Codebase**: `file.ext:line` - [description of pattern]
- **Docs** (if applicable): url#section - [what we're following from official docs]

### Known Gotchas
1. [Codebase-specific gotcha from context files or code comments]
2. [External gotcha from official docs - deprecations, anti-patterns]
3. [Edge case to watch for based on research]

### Integration Points
- [What code depends on this change]
- [What this change depends on]
- [Data flow implications]

### Compatibility

- **Migration required**: Yes/No
  - [If yes: version, script description]
- **Backward compatible**: Yes/No/Partial
  - [If no: what breaks]
- **Rollback strategy**: [how to undo]
  - Code rollback: [steps]
  - Data rollback: [steps if applicable]
- **Testing requirements**:
  - [ ] [Specific test requirement 1]
  - [ ] [Specific test requirement 2]

---

## Implementation Steps

### Step 1: [Action Description]
**File**: `path/to/file.ext`
**Lines to [MODIFY/DELETE/ADD]**: [line numbers]

**Before** (if modifying):
```[language]
[existing code snippet]
```

**After**:
```[language]
[new code snippet]
```

**Explanation**: [Why this change, what it accomplishes]

### Step 2: [Action Description]
[Repeat pattern]

### Step 3: Build and Test
**Commands**:
```bash
[build command]
[test command]
```

### Step 4: Manual Testing Checklist
- [ ] [Specific test scenario 1]
- [ ] [Specific test scenario 2]
- [ ] [Specific test scenario 3]

---

## Edge Cases & Failure Modes

| Edge Case | Expected Behavior | Implementation Notes |
|-----------|-------------------|---------------------|
| [Specific scenario] | System should [expected outcome] | [How handled: approach, line ref] |
| [Specific scenario] | System should [expected outcome] | [How handled] |
| [Specific scenario] | System should [expected outcome] | [How handled] |

---

## Success Definition

### Functional Requirements
- [ ] [Specific deliverable 1]
- [ ] [Specific deliverable 2]
- [ ] [Specific deliverable 3]

### Validation Commands
```bash
[Commands to run to verify success]
```

### Expected Outcomes
1. [Specific observable result 1]
2. [Specific observable result 2]
3. [Specific observable result 3]

---

## Implementation Summary

**Files Modified**: [number]
- [List of files]

**Lines Changed**:
- Added: ~[number] lines
- Deleted: ~[number] lines
- Modified: ~[number] lines
- Net: [+/- number] lines

**Risk Level**: [Low / Medium / High]
- [Justification for risk level]

**Estimated Time**: [duration]
```

---

## Summary: What to Add to /quick-context.md

### Critical Additions:

1. **Expand Phase 3 "Background and Motivation"** with mandatory subsections:
   - System Architecture Overview (2-3 paragraphs)
   - User Workflows (bullet list or narrative)
   - Data Model Context (if applicable)
   - The Problem/Opportunity (with concrete examples)
   - Why This Happens (for bugs - original intent analysis)
   - Root Cause (specific code/logic)

2. **Add Phase 3.5 "Solution Options"** (REQUIRED):
   - Identify 2-3 solution approaches
   - Document pros/cons/complexity for each
   - Recommend one with rationale
   - Note future considerations

3. **Add Phase 2.5 "Edge Cases & Failure Modes"** (REQUIRED):
   - Brainstorm 6 categories of edge cases
   - Document minimum 3-5 edge cases in table format
   - Include expected behavior and implementation notes

4. **Add Phase 2 "Compatibility Check"** (REQUIRED):
   - Database changes checklist
   - API changes checklist
   - Data migration checklist
   - Document migration needs and rollback strategy

5. **Update Scratchpad Template** with all new sections

6. **Add "Root Cause Analysis" guidance** for bugs:
   - Questions to answer about original intent
   - How to research (git blame, comments, docs)
   - Documentation format

### Quality Gates to Add:

**Before proceeding from Phase 1 to Phase 2**:
- [ ] Can someone unfamiliar with codebase understand the problem from Background section?
- [ ] Have we identified 2-3 solution options with tradeoffs?
- [ ] Have we documented at least 3-5 edge cases?

**Before proceeding from Phase 2 to Phase 3**:
- [ ] Is migration/compatibility impact documented?
- [ ] Is rollback strategy defined?
- [ ] Are all references specific (file:line, url#section)?

**Before completing Phase 3**:
- [ ] Does scratchpad include all required sections?
- [ ] Can an EXECUTOR implement from this plan without asking questions?
- [ ] Is confidence level justified by research depth?

---

## Implementation Notes

These improvements should be added to:
- `.claude/commands/coding-planner/quick-context.md`

The key insight from the Focus Mode bug analysis: **Context is king**. A scratchpad should tell the complete story from architecture to implementation, not just jump to technical fixes. Someone reading the scratchpad should understand:
1. **What** the system does (architecture)
2. **Why** it exists (user workflows)
3. **How** it's broken (problem + root cause + why it happens)
4. **What** could fix it (solution options)
5. **How** to fix it safely (implementation + edge cases + compatibility)

This approach transforms scratchpads from "todo lists" into "comprehensive implementation guides."
