# VoxPlanApp UI/UX Documentation Index

Complete documentation for all UI/UX patterns, screens, and Compose components in VoxPlanApp.

---

## Documentation Files

### 1. VOXPLAN_UI_ARCHITECTURE.md (1,135 lines)
**The Complete Reference Guide**

Comprehensive documentation covering:
- Screen Architecture & Organization (6 main screens, navigation graph)
- Compose UI Patterns & Components (15+ reusable components)
- ViewModel & State Management (8 ViewModels + SharedViewModel)
- Resource Organization (colors, dimensions, typography, strings)
- Navigation Structure & Arguments
- Complete File Reference
- Implementation Notes & Future Enhancements

**Best for:** Understanding the full system, deep dives into specific features, architectural patterns

**Key Sections:**
- Section 1: Screen Architecture (6 screens with files, routes, features)
- Section 2: UI Patterns & Components (component hierarchy, patterns, theming)
- Section 3: ViewModel & State Management (detailed VM descriptions, state classes)
- Section 4: Resource Organization (colors, typography, strings, drawables)
- Section 5: Navigation Structure (sealed class, bottom nav, flow examples)
- Section 8: Design Patterns Summary (visual hierarchy, color coding, spacing)

---

### 2. VOXPLAN_UI_QUICK_REFERENCE.md (549 lines)
**The Quick Lookup Guide**

Fast reference for common tasks:
- Screen Map & Visual Hierarchy
- Screen Details (6 screens with key features)
- Reusable Components Catalog
- Navigation Arguments Reference Table
- Color Palette (HEX codes)
- Dimension Constants
- Text Styles
- ViewModel Quick Lookup
- Action Mode System
- File Structure Summary
- State Hierarchy Diagram
- Feature Checklist (completed, beta, not implemented)
- Common Navigation Patterns

**Best for:** Quick lookups, daily development, copy-paste color codes, finding components

**Quick Access:**
- Need a color? → Section 5
- Need dimensions? → Section 6
- Need a component? → Section 3
- Need navigation help? → Section 4
- Need file structure? → Section 10

---

### 3. VOXPLAN_UI_FILE_INDEX.md (322 lines)
**The File Finder**

Complete absolute file paths for all code and resource files:
- Screen files (6 main screens)
- ViewModel files (8 ViewModels)
- Navigation files (3 navigation files)
- Reusable components (5 component files)
- Theme files (3 theme files)
- Constants files (4 constants files)
- Resource files (strings, colors, drawables, icons, audio)
- Entry point files
- Shared utilities

**Best for:** Finding specific files, code navigation, IDE search setup

**Quick Features:**
- Directory structure tree
- Usage notes by role (Developers, Designers, Testers)
- Search tips by feature/type/resource
- Quick stats (6 screens, 8 ViewModels, 15+ components)

---

## Documentation Overview

```
Total Documentation: 2,006 lines
├── Architecture Guide: 1,135 lines (56%)
├── Quick Reference: 549 lines (27%)
└── File Index: 322 lines (16%)

Files Documented:
├── Screens: 6
├── ViewModels: 8
├── Reusable Components: 15+
├── Navigation Files: 3
├── Theme Files: 3
├── Constant Files: 4
├── Resource Files: 20+
└── Entry Points: 2
```

---

## How to Use This Documentation

### I Want To...

**...Find a specific file**
→ Read: VOXPLAN_UI_FILE_INDEX.md

**...Understand how screens work together**
→ Read: VOXPLAN_UI_ARCHITECTURE.md Section 1 & 5

**...Learn about a component**
→ Read: VOXPLAN_UI_QUICK_REFERENCE.md Section 3
→ Then: VOXPLAN_UI_ARCHITECTURE.md Section 2

**...Know color values for implementation**
→ Read: VOXPLAN_UI_QUICK_REFERENCE.md Section 5
→ File: `/ui/constants/Colors.kt`

**...Understand state management**
→ Read: VOXPLAN_UI_ARCHITECTURE.md Section 3

**...See navigation flow**
→ Read: VOXPLAN_UI_QUICK_REFERENCE.md Section 1 & 13
→ Then: VOXPLAN_UI_ARCHITECTURE.md Section 5

**...Find all dimension constants**
→ Read: VOXPLAN_UI_QUICK_REFERENCE.md Section 6
→ File: `/ui/constants/DpValues.kt`

**...Learn about a ViewModel**
→ Read: VOXPLAN_UI_QUICK_REFERENCE.md Section 8
→ Then: VOXPLAN_UI_ARCHITECTURE.md Section 3.3

---

## Screen Summary

| Screen | File | ViewModel | Route | Status |
|--------|------|-----------|-------|--------|
| Main (Goals) | MainScreen.kt | MainViewModel | "main" | Production |
| Goal Edit | GoalEditScreen.kt | GoalEditViewModel | "goal_edit/{goalId}" | Production |
| Progress | ProgressScreen.kt | ProgressViewModel | "progress" | Production |
| Daily | DailyScreen.kt | DailyViewModel | "daily/{date}?" | Beta |
| Scheduler | DaySchedule.kt | SchedulerViewModel | "day_schedule/{date}" | Production |
| Focus Mode | FocusModeScreen.kt | FocusViewModel | "focus_mode/{goalId}" | Production |

---

## Component Summary

| Category | Components | Files |
|----------|------------|-------|
| Goal/Task | GoalItem, SubGoalItem, GoalListContainer | GoalItem.kt |
| Input | TodoInputBar, QuotaSettingsSection | TodoInputBar.kt, QuotaSettings.kt |
| Navigation | VoxPlanTopAppBar, BottomNavigationBar, BreadcrumbNavigation | VoxPlanApp.kt, BreadCrumbNavigation.kt |
| Progress | PowerBar, DayProgressCard, EmeraldIcon, Diamond, Gem | MainScreen.kt, ProgressScreen.kt |
| Theme | VoxPlanAppTheme | Theme.kt |

---

## Color Palette Summary

**Primary Colors:**
- Dark Purple: #0F0A2C (text, icons)
- Teal: #009688 (borders, highlights)
- Sage Green: #82BD85 (goals, active content)

**UI Elements:**
- Goal Cards: #82BD85 (green)
- Sub-goals: #C7E2C9 (light green)
- Top App Bar: #82babd (teal)
- Toolbar: #fff2df (cream)
- Events: #41c300 (bright green)
- Active Mode: #B21720 (red)

---

## ViewModel State Flow

```
SharedViewModel (Global Breadcrumbs)
    ├→ MainViewModel (goals + breadcrumbs + power bar)
    ├→ GoalEditViewModel (goal details + quotas)
    ├→ ProgressViewModel (weekly progress data)
    ├→ DailyViewModel (daily tasks + action mode)
    ├→ SchedulerViewModel (hourly events)
    ├→ FocusViewModel (timer + progress)
    └→ NavigationViewModel (bottom nav tab selection)
```

---

## Key Architecture Decisions

1. **Bottom Navigation over Drawer** - 4 main sections with direct access
2. **Breadcrumb Trail** - Hierarchical navigation without back button
3. **Separate Daily & Scheduler** - Complementary views (task-based vs time-based)
4. **Action Mode Toggle** - Prevents accidental reordering
5. **SharedViewModel** - Single source of truth for hierarchy
6. **Feature-Based Organization** - `/ui/main/`, `/ui/goals/`, etc.
7. **Material 3 + Custom Colors** - Modern design with domain-specific palette
8. **StateFlow Patterns** - Reactive UI with proper lifecycle management

---

## Navigation Patterns

### Pattern 1: Browse & Edit Goals
```
MainScreen → Click Goal → GoalEditScreen → Edit → Back to Main
```

### Pattern 2: Set Quotas & Track Progress
```
GoalEditScreen → Set Quota → ProgressScreen → View Week Stats
```

### Pattern 3: Schedule & Focus
```
MainScreen → Add to Daily → DailyScreen → Schedule → DaySchedule → Focus Mode
```

### Pattern 4: Quick Focus Session
```
MainScreen → Click Focus Icon → FocusModeScreen → Timer → Back
```

---

## Current Status

### Fully Implemented & Production Ready
- Goal creation, management, hierarchy
- Quota settings and weekly progress tracking
- Day scheduler with draggable events
- Focus mode timer with work/rest cycles
- Material 3 theming with custom colors
- Bottom navigation with 4 screens
- Breadcrumb hierarchy navigation
- Goal reordering (vertical + hierarchical)

### Beta/In Development
- Daily screen (functional but may need polish)
- Parent/child event relationships
- Event duration setup flow

### Not Yet Implemented
- Settings screen
- Goal search/filter
- Goal templates
- Dark mode manual toggle (auto only)
- Notifications
- Goal sharing

---

## File Statistics

```
Total Code Files:
├── Screens: 6
├── ViewModels: 9 (including Shared & Navigation)
├── Components: 15+
├── Theme: 3
├── Constants: 4
└── Utilities: 2

Resource Files:
├── Strings: 1 (16 strings)
├── Colors: 1 (7 colors)
├── Drawables: 6 (vector icons)
├── Icons: 5 sizes (app launcher)
└── Audio: 6 (sound effects)

Total Lines of Documentation: 2,006
```

---

## Quick Links Within Documentation

### VOXPLAN_UI_ARCHITECTURE.md
- [Executive Summary](https://...)
- [Section 1: Screen Architecture](https://...)
- [Section 2: Compose UI Patterns](https://...)
- [Section 3: ViewModel & State](https://...)
- [Section 5: Navigation Structure](https://...)

### VOXPLAN_UI_QUICK_REFERENCE.md
- [Section 5: Color Palette](https://...)
- [Section 6: Dimensions](https://...)
- [Section 8: ViewModel Lookup](https://...)
- [Section 13: Navigation Patterns](https://...)

### VOXPLAN_UI_FILE_INDEX.md
- [Absolute File Paths](https://...)
- [Directory Structure](https://...)
- [Usage Notes by Role](https://...)

---

## Recommendations for Developers

1. **Start Here:** Read VOXPLAN_UI_QUICK_REFERENCE.md Sections 1-3 for orientation
2. **Deep Dive:** Read VOXPLAN_UI_ARCHITECTURE.md Section 3 for state management
3. **Find Files:** Use VOXPLAN_UI_FILE_INDEX.md for absolute paths
4. **Color Codes:** Copy from VOXPLAN_UI_QUICK_REFERENCE.md Section 5
5. **New Component:** Follow pattern in `/ui/main/GoalItem.kt`
6. **New Screen:** Follow pattern in `/ui/goals/GoalEditScreen.kt` + ViewModel

---

## Last Updated

Generated: November 10, 2025

Documentation created through comprehensive codebase exploration including:
- All 6 main screens and their ViewModels
- 15+ reusable Compose components
- Navigation structure and routing
- Theme and styling system
- State management patterns
- Resource organization

---

For questions about specific sections, refer to the corresponding documentation file listed at the top of this README.

