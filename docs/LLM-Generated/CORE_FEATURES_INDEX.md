# VoxPlanApp Core Features Documentation Index

## Documentation Package Contents

This package contains comprehensive documentation of VoxPlanApp's three core features: **Events**, **Quotas**, and **Categories**. All documentation is located in the project root directory.

---

## Core Feature Documentation Files

### 1. FEATURE_DOCUMENTATION.md (PRIMARY REFERENCE)
**Size:** ~1,075 lines | **Scope:** Very Thorough

Complete technical documentation of all three core features with:
- Data models with all properties explained
- Database access patterns (DAOs, Repositories)
- UI/Screen implementations
- Feature workflows and creation flows
- Business logic algorithms
- Database schema and relationships
- Complete file references with absolute paths

**Key Sections:**
- Section 1: Events (parent/child structure, dailies, scheduled events)
- Section 2: Quotas (time targets, active days encoding, progress tracking)
- Section 3: Categories (goal hierarchies, recursive deletion, navigation)
- Section 4: Feature Interactions (cross-feature dependencies)
- Section 5: Business Logic & Algorithms
- Section 6: Database Schema
- Section 7: Key Files Reference
- Section 8: Development Roadmap
- Section 9: Constants & Configuration
- Section 10: Error Handling & Edge Cases
- Section 11: Testing Considerations

---

### 2. FEATURE_DIAGRAMS.md (VISUAL REFERENCE)
**Size:** ~623 lines | **Scope:** Flow diagrams and state machines

Visual representation of all major features with:
- Event lifecycle flows (creation, deletion, duration tracking)
- Quota lifecycle flows (configuration, activation, progress)
- Category/hierarchy flows (structure, navigation, deletion)
- Complete data flow diagrams
- Database relationship diagrams
- UI navigation flow
- State machine diagrams
- Visual progress indicators

**Key Diagrams:**
1. Event Creation Paths (2 paths shown)
2. Event Deletion Flow
3. Event Duration Tracking State Machine
4. Quota Creation & Configuration
5. Active Days Encoding
6. Quota Activation Check Algorithm
7. Quota Progress Calculation
8. Category Hierarchy Structure
9. Goal Navigation Breadcrumb
10. Recursive Deletion Algorithm
11. Complete Event-Quota-Progress Data Flow
12. Category-Event Relationship Flow
13. Daily Event State Transitions
14. Event Deletion State Flow
15. Database Relationship Diagram
16. UI Navigation Flow
17. Quota Progress Visual Indicators

---

### 3. FEATURES_SUMMARY.md (QUICK REFERENCE)
**Size:** ~428 lines | **Scope:** Overview and quick lookup

High-level summary perfect for getting oriented quickly:
- Quick reference file location table
- What each feature is (brief description)
- Key properties overview
- Essential workflows (create, activate, delete)
- Feature interactions overview
- Key business rules checklist
- Database schema overview
- Development tips
- Testing checklist
- Version history

**Best For:** Developers new to the codebase, quick lookups, planning meetings

---

## File Location Quick Reference

### Core Feature Files (By Feature)

#### EVENTS FILES
```
Data Layer:
- /data/Event.kt                    Entity definition
- /data/EventDao.kt                 Database access
- /data/EventRepository.kt          Data layer abstraction

UI Layer:
- /ui/daily/DailyScreen.kt         Daily tasks view
- /ui/daily/DailyViewModel.kt      Daily logic
- /ui/calendar/DaySchedule.kt      Schedule view  
- /ui/calendar/SchedulerViewModel.kt Schedule logic
```

#### QUOTA FILES
```
Data Layer:
- /data/QuotaEntity.kt             Entity definition
- /data/QuotaDao.kt                Database access
- /data/QuotaRepository.kt         Data layer abstraction

UI Layer:
- /ui/goals/QuotaSettings.kt       Configuration UI
- /ui/goals/ProgressScreen.kt      Progress tracking
- /ui/goals/ProgressViewModel.kt   Progress logic
```

#### CATEGORY FILES
```
Data Layer:
- /data/TodoItem.kt                Goal/Category entity
- /data/TodoDao.kt                 Database access
- /data/TodoRepository.kt          Data layer abstraction
- /data/GoalWithSubGoals.kt        Composite structure

UI Layer:
- /ui/main/MainScreen.kt           Category listing
- /ui/main/GoalListContainer.kt    Hierarchy display
- /ui/goals/GoalEditScreen.kt      Category editing
- /ui/goals/GoalEditViewModel.kt   Edit logic
```

#### SUPPORTING FILES
```
- /data/AppDatabase.kt             Room database config (v13)
- /data/TimeBankEntry.kt           Completion tracking
- /data/Constants.kt               Configuration constants
- /data/Converters.kt              Type converters
- /data/GoalWithSubGoals.kt        Recursive goal structure
```

---

## How to Use This Documentation

### For Understanding the System
1. Start with **FEATURES_SUMMARY.md** for overview
2. Read **FEATURE_DOCUMENTATION.md** Section 1-4 for detailed understanding
3. Reference **FEATURE_DIAGRAMS.md** for visual confirmation

### For Implementation
1. Check **FEATURES_SUMMARY.md** "Quick Reference: File Locations"
2. Open relevant files in FEATURE_DOCUMENTATION.md Section 7
3. Review **FEATURE_DIAGRAMS.md** for workflow verification
4. Use database schema from FEATURE_DOCUMENTATION.md Section 6

### For Debugging
1. Trace issue using **FEATURE_DIAGRAMS.md** data flows
2. Check business rules in **FEATURES_SUMMARY.md**
3. Review error handling in FEATURE_DOCUMENTATION.md Section 10
4. Check affected files using location reference

### For Testing
1. Use testing checklist in **FEATURES_SUMMARY.md**
2. Verify flows against **FEATURE_DIAGRAMS.md**
3. Check validation rules in FEATURE_DOCUMENTATION.md Section 5

### For Documentation Updates
When modifying code:
1. Update relevant data model in FEATURE_DOCUMENTATION.md Section 1-3
2. Update file reference in FEATURE_DOCUMENTATION.md Section 7
3. Update workflow/flow in FEATURE_DIAGRAMS.md
4. Update business rules in FEATURES_SUMMARY.md

---

## Key Documentation Sections by Topic

### Understanding Data Models
- FEATURE_DOCUMENTATION.md Sections 1.2, 2.2, 3.2
- FEATURE_DIAGRAMS.md Section 6 (Database Diagram)

### Understanding Workflows
- FEATURE_DOCUMENTATION.md Sections 1.7, 2.8, 3.8
- FEATURE_DIAGRAMS.md Sections 1-3 (Lifecycle flows)

### Understanding Interactions
- FEATURE_DOCUMENTATION.md Section 4
- FEATURE_DIAGRAMS.md Section 4 (Complete data flow)

### Understanding UI Implementation
- FEATURE_DOCUMENTATION.md Sections 1.6, 2.6, 3.7
- FEATURE_DIAGRAMS.md Section 7 (UI Navigation)

### Understanding Business Logic
- FEATURE_DOCUMENTATION.md Section 5
- FEATURES_SUMMARY.md "Key Business Rules"

### Understanding Database
- FEATURE_DOCUMENTATION.md Section 6
- FEATURE_DIAGRAMS.md Section 6

---

## Feature Dependency Map

```
TodoItem (Categories/Goals)
  ├─ Can have Quota
  ├─ Can have Event (goalId reference)
  └─ Can have sub-TodoItem (parentId reference)

Quota
  └─ Belongs to TodoItem
  └─ Used to create Daily Events

Event
  ├─ Belongs to TodoItem (goalId reference)
  ├─ Can have child Events (parentDailyId reference)
  └─ Generates TimeBank entries

TimeBank
  ├─ Records completion for TodoItem
  └─ Feeds into Progress calculations

Progress (ProgressScreen/ViewModel)
  ├─ Queries Quotas
  ├─ Queries TimeBank
  ├─ Aggregates per TodoItem
  └─ Displays rewards (diamonds, emeralds, stars)
```

---

## Code Examples by Feature

### Creating a Quota-Driven Daily Event
See: FEATURE_DOCUMENTATION.md Section 1.7 Path 2 + Section 2.8

### Scheduling an Event into Time Slots
See: FEATURE_DOCUMENTATION.md Section 1.7 Path 1 + Section 1.8

### Tracking Event Completion
See: FEATURE_DOCUMENTATION.md Section 1.8 + Section 5.2

### Setting Up Goal Hierarchy
See: FEATURE_DOCUMENTATION.md Section 3.8 + FEATURE_DIAGRAMS.md Section 3

### Calculating Progress
See: FEATURE_DOCUMENTATION.md Section 2.7 + Section 5.2

---

## Database Schema Quick Reference

```
TodoItem ─────────┬─────── Quota
  self-ref        └─ goalId FK (CASCADE)
  (parentId)
     │
     ├─ Event ─────────────┐
     │  └─ self-ref        │
     │     (parentDailyId)  │
     │                      │
     └─ TimeBank ──────────┘
        (goalId FK)
```

**Current Schema Version:** 13
**Last Migration:** v12→v13 (Added parentDailyId to Event)

---

## Constants & Configuration

Located in: `/data/Constants.kt`

```kotlin
FULLBAR_MINS = 60              // Power bar fills every 60 minutes
pointsForItemCompletion = 15   // Points per completion
```

Visual Constants (in UI files):
- Hour height in scheduler: 48.dp
- Time increments: 15 minutes
- Diamonds awarded: Every 240 minutes (4 hours)
- Scheduler hours: 1 AM - 11:59 PM
- Default start time: 9:00 AM

---

## Testing Reference

### Unit Test Topics
- Duration calculations (FEATURE_DOCUMENTATION.md Section 5.2)
- Quota activation (FEATURE_DOCUMENTATION.md Section 5.1)
- Hierarchy traversal (FEATURE_DOCUMENTATION.md Section 5.3)
- Delete cascade (FEATURE_DOCUMENTATION.md Section 5.3)

### Integration Test Topics
- Event creation flow (FEATURE_DIAGRAMS.md Section 4.1)
- Quota tracking (FEATURE_DOCUMENTATION.md Section 2.8)
- Category operations (FEATURE_DOCUMENTATION.md Section 3.8)
- Progress aggregation (FEATURE_DOCUMENTATION.md Section 2.7)

### Testing Checklist
See: FEATURES_SUMMARY.md "Testing Checklist"

---

## Development Workflow

### Adding a New Feature to Events
1. Extend Event.kt with new properties
2. Update EventDao queries if needed
3. Update EventRepository methods
4. Modify DailyViewModel/SchedulerViewModel
5. Update UI screens
6. Update FEATURE_DOCUMENTATION.md
7. Update FEATURE_DIAGRAMS.md if workflow changed

### Adding a New Quota Type
1. Add frequency field to Quota entity
2. Update QuotaRepository activation logic
3. Update QuotaSettingsSection UI
4. Update ProgressViewModel aggregation
5. Update FEATURE_DOCUMENTATION.md
6. Update FEATURE_DIAGRAMS.md

### Adding Category Feature
1. Extend TodoItem with new fields
2. Update TodoDao if needed
3. Update GoalWithSubGoals if structure changes
4. Update UI screens
5. Test recursive operations
6. Update FEATURE_DOCUMENTATION.md
7. Update FEATURE_DIAGRAMS.md

---

## Quick Navigation

**Need to understand Events?**
→ Read FEATURES_SUMMARY.md "Feature 1: Events"
→ Then FEATURE_DOCUMENTATION.md Section 1
→ Reference FEATURE_DIAGRAMS.md Sections 1-2

**Need to understand Quotas?**
→ Read FEATURES_SUMMARY.md "Feature 2: Quotas"
→ Then FEATURE_DOCUMENTATION.md Section 2
→ Reference FEATURE_DIAGRAMS.md Sections 2-4

**Need to understand Categories?**
→ Read FEATURES_SUMMARY.md "Feature 3: Categories"
→ Then FEATURE_DOCUMENTATION.md Section 3
→ Reference FEATURE_DIAGRAMS.md Section 3

**Need to find a specific file?**
→ Use FEATURE_DOCUMENTATION.md Section 7
→ Or FEATURES_SUMMARY.md "Quick Reference: File Locations"

**Need to understand how features interact?**
→ Read FEATURES_SUMMARY.md "Feature Interactions"
→ Then FEATURE_DOCUMENTATION.md Section 4
→ Reference FEATURE_DIAGRAMS.md Section 4.1

**Need database details?**
→ Read FEATURE_DOCUMENTATION.md Section 6
→ Reference FEATURE_DIAGRAMS.md Section 6

**Need to test something?**
→ Check FEATURES_SUMMARY.md "Testing Checklist"
→ Reference FEATURE_DOCUMENTATION.md Section 11

---

## Related Documentation

Other documentation in the project:
- README.md - Project overview
- description_voxplan.md - Product vision
- voxplan_process.md - Development notes and TODOs
- ARCHITECTURE.md - Overall app architecture
- DATA_MODELS_DOCUMENTATION.md - Detailed data model reference

---

## Document Maintenance Notes

- Last Updated: 2025-11-10
- Database Version: 13
- Features Documented: Events, Quotas, Categories
- Completeness: Very Thorough (all code paths covered)
- Coverage: All data models, DAOs, repositories, UI screens, business logic

---

## Contact & Support

For questions about the documentation:
1. Check if answer exists in one of the three main docs
2. Search FEATURE_DOCUMENTATION.md Section 10 (Error Handling)
3. Check FEATURES_SUMMARY.md "Development Tips"
4. Review FEATURE_DIAGRAMS.md for visual confirmation

For code questions:
1. Find file location in FEATURE_DOCUMENTATION.md Section 7
2. Read relevant section in FEATURE_DOCUMENTATION.md
3. Check FEATURE_DIAGRAMS.md for workflow confirmation

