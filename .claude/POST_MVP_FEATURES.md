# VoxPlan Post-MVP Features

This document tracks feature ideas and enhancements that are intentionally deferred until after the MVP release. These features have been considered but are not critical for the initial launch.

---

## Retrospective Time-Logging Enhancement

**Status**: Design Complete
**Priority**: Medium (post-MVP)
**Related Fix**: Focus Mode dailies duplication bug (MVP: simple fix applied)

**Design Location**: `.claude/scratchpad/focus-mode-dailies-bug/option-b-retrospective-logging-design.md`

**Summary**: Add `isUserCreated` and `isVisible` Boolean flags to Event entity to enable automatic logging of actual time spent in Focus Mode while keeping the Dailies UI clean for planning purposes. This would allow users to see "what they actually did" in the day scheduler without polluting their daily task planning list.

**Why Post-MVP**:
- Requires database migration (v13 → v14)
- Additional UI/UX work needed for visual distinction between planned vs actual events
- Settings integration for enabling/disabling auto-logging
- Need to establish minimum time thresholds for calendar visibility
- MVP needs clean Dailies list first (simple fix solves immediate bug)

**Technical Requirements**:
- Database schema changes: Add `isUserCreated` and `isVisible` fields to Event entity
- Migration MIGRATION_13_14
- Update EventDao queries to filter by `isVisible`
- Modify FocusViewModel to create auto-logged events with flags set appropriately
- DaySchedule UI changes to visually differentiate event types (opacity, border styles)
- Settings screen integration

**Dependencies**:
- MVP release
- User feedback on scheduling/dailies workflow
- Understanding of actual usage patterns

**Estimated Complexity**: Medium (database migration + UI changes)

---

## [Future Features - Add Below]

