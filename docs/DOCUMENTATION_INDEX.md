# VoxPlanApp Documentation Index

This index guides you through the documentation of VoxPlanApp's incomplete features: Dailies and Scheduling.

## Quick Start

If you're new to understanding these features, read in this order:
1. **FEATURES_SUMMARY.md** - Quick reference (5 min read)
2. **INCOMPLETE_FEATURES.md** - Comprehensive analysis (20 min read)

## Document Overview

### FEATURES_SUMMARY.md
**Quick reference guide** (428 lines, ~12 KB)
- Feature status overview (% completeness)
- What works vs. what's missing
- Critical files and line counts
- Data model architecture
- **3 Critical Issues** with code examples
- Testing recommendations
- Next steps/roadmap

**Best for:** Quick lookup, getting up to speed, identifying priorities

### INCOMPLETE_FEATURES.md  
**Comprehensive documentation** (572 lines, ~22 KB)
- Detailed implementation status
- Architectural decisions and patterns
- Component hierarchies
- Integration points with other features
- Complete file listings
- Workflow descriptions
- Detailed issue analysis
- Recommendations for completion

**Best for:** Deep understanding, development planning, code review

## Feature Quick Reference

### Dailies (BETA - VP 3.1-3.2)
Status: Partially implemented (~70% complete)
- Primary files:
  - DailyScreen.kt (628 lines)
  - DailyViewModel.kt (190 lines)
- Critical missing: Completion tracking, direct Focus Mode access
- Integration: Quotas → Dailies → Schedule → Focus Mode

### Scheduling (PARTIAL - VP 2.1-2.5)
Status: Functional but incomplete (~65% complete)
- Primary files:
  - DaySchedule.kt (675 lines) **Contains critical bug**
  - SchedulerViewModel.kt (141 lines)
- Critical bug: Delete dialog references undefined variable (line 110-126)
- Integration: Daily tasks → Schedule → Focus Mode → Time Bank

## Navigation Guide

### By Topic

**Data Model**
- Event entity (used for both dailies and scheduled events)
- Parent-child relationship pattern
- Database schema in Event.kt, EventDao.kt

**Architecture**
- ActionMode enum (reordering)
- Quota integration system
- Parent-child deletion cascade

**UI Components**
- DailyScreen composables and dialogs
- DaySchedule grid rendering and drag handling
- EventBox and EventActions

**State Management**
- DailyUiState and DailyViewModel
- SchedulerViewModel
- Flow-based reactive updates

### By Problem Type

**Bugs to Fix**
1. DaySchedule.kt lines 110-126 - Delete dialog
2. QuickScheduleScreen.kt - Entirely commented out
3. Scroll position loss on date change

**Features to Implement**
1. Completion tracking (dailies)
2. Direct Focus Mode access (dailies)
3. Event creation from schedule
4. Week/month calendar views
5. Recurrence support
6. Color-coding by goal
7. Smart scheduling suggestions

**Improvements Needed**
1. Error handling and user feedback
2. Performance optimization
3. User preferences system
4. Notifications/reminders

## File Locations

All paths are absolute paths from macOS system.

### Dailies
```
/Users/richardthompson/StudioProjects/VoxPlanApp/app/src/main/java/com/voxplanapp/ui/daily/
├── DailyScreen.kt (628 lines)
└── DailyViewModel.kt (190 lines)
```

### Scheduling
```
/Users/richardthompson/StudioProjects/VoxPlanApp/app/src/main/java/com/voxplanapp/ui/calendar/
├── DaySchedule.kt (675 lines) **Contains bug**
└── SchedulerViewModel.kt (141 lines)
```

### Data Models
```
/Users/richardthompson/StudioProjects/VoxPlanApp/app/src/main/java/com/voxplanapp/data/
├── Event.kt (33 lines)
├── EventDao.kt (64 lines)
├── EventRepository.kt (32 lines)
├── QuotaEntity.kt (26 lines)
└── QuotaRepository.kt (47 lines)
```

### Navigation
```
/Users/richardthompson/StudioProjects/VoxPlanApp/app/src/main/java/com/voxplanapp/navigation/
├── VoxPlanScreen.kt
├── VoxPlanNavHost.kt
└── VoxPlanApp.kt
```

### Incomplete Code
```
/Users/richardthompson/StudioProjects/VoxPlanApp/app/src/main/java/com/voxplanapp/ui/main/
└── QuickScheduleScreen.kt (86 lines, all commented out)
```

## Version History

| Version | Release | Major Feature |
|---------|---------|---------------|
| 3.2 | Jan 29 | Dailies improved with parent/child events |
| 3.1 | Jan 20 | Dailies screen (introduced in beta) |
| 3.0 | Jan 10 | Quotas and quota screen |
| 2.7 | Sep 18 | Power bar and time banking |
| 2.5 | Aug 23 | Focus mode accessible from schedule |
| 2.3 | Aug 13 | Event drag/select and action icons |
| 2.2 | Aug 7  | Add goals to schedule |
| 2.1 | Jul 28 | Schedule screen first introduced |

## Key Architectural Patterns

### Parent-Child Events
- **Parent Daily:** parentDailyId = null, startTime/endTime = null
- **Scheduled Child:** parentDailyId = dailyId, startTime/endTime = populated
- Database filtering uses these relationships

### ActionMode Enumeration
Used for task reordering in dailies:
- Normal (default)
- VerticalUp (reorder up)
- VerticalDown (reorder down)

### Quota-Driven Dailies
1. Quotas define daily minutes requirement
2. "Add Quota Tasks" button populates dailies
3. Visual progress boxes show: completed / scheduled / quota

## Critical Issues at a Glance

### Issue 1: Delete Dialog Bug (DaySchedule.kt:110-126)
```kotlin
// ERROR: 'event' is undefined in this scope
viewModel.confirmDeleteChildOnly(event)
viewModel.confirmDeleteWithParent(event)
```
**Severity:** CRITICAL
**Impact:** Delete operations from schedule don't work
**Fix Required:** Refactor dialog to use available parentId variable

### Issue 2: QuickScheduleScreen Completely Commented
```kotlin
// Entire file (lines 26-86) is commented out
// Contains undefined variables
```
**Severity:** MEDIUM
**Impact:** Fast scheduling from main screen unavailable
**Fix Required:** Implement properly or remove

### Issue 3: Scroll Position Lost
```kotlin
val initialScrollPosition = ((scrollToHour - startHour) * hourHeight.value).toInt() * 2
// Always resets to 6 AM on date change
```
**Severity:** LOW
**Impact:** User loses scroll context when navigating dates
**Fix Required:** Add scroll position persistence to ViewModel

## Testing Priorities

### High Priority (Critical Bugs)
- [ ] Delete dialog in DaySchedule
- [ ] Parent-child cascade deletion
- [ ] Quota integration and generation

### Medium Priority (Feature Completion)
- [ ] Date navigation edge cases
- [ ] Overlapping event rendering
- [ ] Focus mode entry/exit flows

### Low Priority (Polish)
- [ ] UI/UX improvements
- [ ] Performance testing (50+ events)
- [ ] Notification system

## Development Roadmap

### Phase 1: Stabilization (Weeks 1-2)
1. Fix delete dialog bug in DaySchedule.kt
2. Complete or remove QuickScheduleScreen
3. Implement scroll position persistence
4. Comprehensive testing of core flows

### Phase 2: Dailies Completion (Weeks 3-4)
1. Add completion checkboxes to daily tasks
2. Implement direct Focus Mode access
3. Add bulk operations (multi-select, batch actions)
4. Quick-reschedule feature from dailies screen

### Phase 3: Scheduling Enhancements (Weeks 5-6)
1. Event creation from schedule (tap empty space)
2. Week and month calendar views
3. Recurrence support implementation
4. Color-coding by goal

### Phase 4: Advanced Features (Weeks 7+)
1. Smart scheduling suggestions
2. Conflict detection and warnings
3. Event template system
4. Comprehensive notifications/reminders

## Related Documentation

- **voxplan_process.md** - Development process and feature ideas
- **description_voxplan.md** - Product vision and pitch
- **README.md** - Project overview

## Questions?

For specific questions about implementation details:
1. Check FEATURES_SUMMARY.md for quick answers
2. Search INCOMPLETE_FEATURES.md for comprehensive details
3. Examine source files directly - they're well-commented

## Summary Statistics

**Total Code Analyzed:**
- Dailies: 818 lines (UI + ViewModel)
- Scheduling: 816 lines (UI + ViewModel)
- Total: 1,634 lines of feature code

**Documentation Created:**
- INCOMPLETE_FEATURES.md: 572 lines (22 KB)
- FEATURES_SUMMARY.md: 428 lines (12 KB)
- DOCUMENTATION_INDEX.md: This file

**Coverage:**
- 4 major files analyzed
- 5 data model files reviewed
- 3 navigation files examined
- 1 incomplete file found
- 3 critical issues identified
- 15+ missing features documented
