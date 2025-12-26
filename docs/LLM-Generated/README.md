# VoxPlanApp Documentation Directory

**Last Updated:** January 2025
**Current Version:** 3.2 (Dailies improved with parent/child Events)

This directory contains comprehensive documentation for VoxPlanApp's architecture, data models, UI components, and features. Use this guide to quickly find the information you need.

---

## Quick Navigation

### Need to understand the overall architecture?
→ Start with **ARCHITECTURE.md** (1,217 lines - comprehensive)
→ Or **ARCHITECTURE_QUICK_REFERENCE.md** for faster overview

### Working with data models or database?
→ **DATA_MODELS_DOCUMENTATION.md** (complete entity documentation)
→ **DATA_MODELS_QUICK_REFERENCE.md** (quick lookup)
→ **DATABASE_SCHEMA_VISUAL.md** (visual schema diagrams)

### Building or modifying UI?
→ **VOXPLAN_UI_ARCHITECTURE.md** (complete UI documentation)
→ **VOXPLAN_UI_QUICK_REFERENCE.md** (quick lookup)
→ **VOXPLAN_UI_FILE_INDEX.md** (file locations)

### Implementing or fixing features?
→ **FEATURE_DOCUMENTATION.md** (detailed technical reference)
→ **FEATURES_SUMMARY.md** (overview with file locations)
→ **FEATURE_DIAGRAMS.md** (visual flows)
→ **INCOMPLETE_FEATURES.md** (known issues and missing features)

### Understanding the product vision?
→ **description_voxplan.md** (product vision and pitch)
→ **voxplan_process.md** (development process and feature ideas)

---

## Document Catalog

### Architecture Documentation

#### ARCHITECTURE.md (1,217 lines)
**Most comprehensive architecture reference**

**Contents:**
- Complete state management architecture (StateFlow, Compose State, SharedViewModel)
- All ViewModels with state data classes and patterns
- Data layer architecture (repositories, DAOs, entities)
- Reactive programming patterns (Flow, combine, flatMapLatest, snapshotFlow)
- Application lifecycle and initialization flow
- Database migrations (versions 2-13)
- Navigation architecture and routes
- Dependency injection graph
- Common architectural patterns
- Best practices observed
- Complete file structure with line counts
- Data flow diagrams

**When to use:**
- Deep dive into any architectural component
- Understanding state management patterns
- Learning reactive programming patterns in the codebase
- Reviewing database design and migrations
- Understanding dependency injection setup

**Read this if:** You're new to the codebase or need comprehensive understanding

---

#### ARCHITECTURE_QUICK_REFERENCE.md
**Condensed architecture overview**

**Contents:**
- High-level architecture pattern summary
- Key ViewModels and their responsibilities
- State management at a glance
- Critical data entities
- Navigation overview
- Common patterns quick reference

**When to use:**
- Quick refresher on architecture
- Finding which ViewModel handles what
- Identifying which repository to use
- Quick pattern lookup

**Read this if:** You already understand the codebase and need quick reference

---

#### ARCHITECTURE_DOCUMENTATION_SUMMARY.txt
**Plain text summary of architecture highlights**

**When to use:**
- Quick text-based overview
- Copy-paste reference for external documentation

---

### Data Model Documentation

#### DATA_MODELS_DOCUMENTATION.md (Comprehensive)
**Complete database and entity documentation**

**Contents:**
- All database entities (TodoItem, Event, TimeBank, Quota)
- Complete field descriptions and constraints
- Data Access Objects (DAOs) with query documentation
- Repository patterns and methods
- Domain models (GoalWithSubGoals)
- Enums (RecurrenceType, ActionMode)
- Type converters (LocalDate, LocalTime)
- Database relationships and foreign keys
- Migration history (versions 1-13)
- Data access patterns

**When to use:**
- Creating or modifying database entities
- Writing new DAO queries
- Understanding parent-child relationships
- Database migrations
- Type conversion issues

**Read this if:** Working with data layer, repositories, or database

---

#### DATA_MODELS_QUICK_REFERENCE.md
**Quick lookup for data models**

**Contents:**
- Entity quick reference with field summaries
- DAO method signatures
- Repository interface overview
- Key relationships diagram

**When to use:**
- Quick field lookup
- Finding DAO method signatures
- Checking relationship patterns

**Read this if:** You need quick data model information

---

#### DATABASE_SCHEMA_VISUAL.md
**Visual database schema and relationships**

**Contents:**
- Entity relationship diagrams
- Table structures with field types
- Foreign key relationships
- Visual data flow diagrams

**When to use:**
- Understanding table relationships visually
- Designing new features that interact with database
- Explaining database structure to others

**Read this if:** You're a visual learner or need to present database structure

---

### UI/UX Documentation

#### VOXPLAN_UI_ARCHITECTURE.md (Comprehensive)
**Complete UI component and screen documentation**

**Contents:**
- All screen architectures (Main, Daily, Schedule, Focus, Progress, GoalEdit)
- Component hierarchies for each screen
- State management per screen
- UI composables and their responsibilities
- Theme system (Material 3)
- Constants (Colors, Dimens, TextStyles)
- Navigation flows
- Action modes and UI states
- User interaction patterns

**When to use:**
- Building new UI components
- Modifying existing screens
- Understanding composable hierarchies
- Implementing new navigation flows
- Working with theme/styling

**Read this if:** Working with UI, Compose, or user interactions

---

#### VOXPLAN_UI_QUICK_REFERENCE.md
**Quick UI component lookup**

**Contents:**
- Screen-by-screen component lists
- Key composables quick reference
- State management summary per screen
- Common UI patterns

**When to use:**
- Finding which composable does what
- Quick screen capability lookup
- Identifying state management approach for a screen

**Read this if:** You need fast UI component information

---

#### VOXPLAN_UI_FILE_INDEX.md
**File location index for UI components**

**Contents:**
- Complete file paths for all UI components
- Organized by feature/screen
- Line counts per file

**When to use:**
- Finding the file path for a specific component
- Understanding project structure
- Navigating large UI codebase

**Read this if:** You need to locate specific UI files quickly

---

#### README_UI_DOCUMENTATION.md
**Meta-documentation about UI docs**

**When to use:**
- Understanding how UI documentation is organized
- Finding which UI doc to read

---

### Feature Documentation

#### FEATURE_DOCUMENTATION.md (Detailed Technical Reference)
**Complete feature implementation details**

**Contents:**
- Dailies feature (implementation status, data flow, components)
- Scheduling feature (day scheduler, event management)
- Quota system (daily quotas, active days, progress tracking)
- Focus mode integration
- Time banking system
- Parent-child event relationships
- Technical implementation details

**When to use:**
- Implementing new features
- Understanding how existing features work
- Debugging feature-specific issues
- Integration points between features

**Read this if:** Working on feature implementation or enhancement

---

#### FEATURES_SUMMARY.md (Overview)
**Quick feature status and file locations**

**Contents:**
- Feature completion status (% complete)
- What works vs. what's missing
- Critical files with line counts
- Data model architecture per feature
- Critical issues with code examples
- Testing recommendations
- Next steps/roadmap

**When to use:**
- Quick feature status check
- Finding feature-related files
- Identifying known issues
- Planning feature work

**Read this if:** You need feature overview or status

---

#### FEATURE_DIAGRAMS.md (Visual Flows)
**Visual representation of feature workflows**

**Contents:**
- Data flow diagrams
- User interaction flows
- Feature integration diagrams
- Sequence diagrams

**When to use:**
- Understanding feature workflows visually
- Planning feature integrations
- Documenting new features

**Read this if:** You're a visual learner or documenting features

---

#### INCOMPLETE_FEATURES.md (Known Issues)
**Comprehensive analysis of incomplete features**

**Contents:**
- Dailies feature gaps (~70% complete)
- Scheduling feature gaps (~65% complete)
- Critical bugs with line numbers
- Missing functionality
- Recommendations for completion
- Implementation priorities

**When to use:**
- Before working on Dailies or Scheduling features
- Bug fixing
- Feature completion planning
- Understanding technical debt

**Read this if:** Working on Dailies, Scheduling, or fixing known bugs

---

#### CORE_FEATURES_INDEX.md
**Index of core app features**

**When to use:**
- Finding documentation for specific core features
- Understanding feature organization

---

### Navigation Documentation

#### DOCUMENTATION_INDEX.md
**Meta-index focused on incomplete features**

**Contents:**
- Navigation guide for Dailies and Scheduling documentation
- Problem-type navigation (bugs, features, improvements)
- Version history
- Testing priorities
- Development roadmap

**When to use:**
- Working specifically on Dailies or Scheduling
- Understanding documentation structure for incomplete features
- Finding specific bug documentation

**Read this if:** Working on incomplete features (Dailies/Scheduling)

---

### Product & Process Documentation

#### description_voxplan.md
**Product vision and pitch**

**Contents:**
- Product description
- Target audience
- Value proposition
- Feature highlights
- Vision for future development

**When to use:**
- Understanding product goals
- Making design decisions aligned with vision
- Explaining the app to others

**Read this if:** You need product context for development decisions

---

#### voxplan_process.md
**Development process and feature ideas**

**Contents:**
- Version history (1.0 → 3.2)
- Feature breakdown by version
- Application components
- Detailed feature explanations
- Future version plans (v4-v6)
- Developer overview
- Workflow descriptions

**When to use:**
- Understanding development history
- Planning future features
- Learning original design intentions
- Understanding feature evolution

**Read this if:** You want historical context or future planning ideas

---

#### vox_original_readme.md
**Original README before reorganization**

**When to use:**
- Historical reference
- Comparing with current state

---

## Recommended Reading Order

### For New Developers

**Day 1: Get Oriented (2-3 hours)**
1. description_voxplan.md (15 min) - Understand the product
2. ARCHITECTURE_QUICK_REFERENCE.md (30 min) - High-level architecture
3. FEATURES_SUMMARY.md (30 min) - What exists and what's missing
4. VOXPLAN_UI_QUICK_REFERENCE.md (30 min) - UI component overview
5. DATA_MODELS_QUICK_REFERENCE.md (30 min) - Data structures

**Week 1: Deep Dive (8-10 hours)**
1. ARCHITECTURE.md (3-4 hours) - Complete architecture understanding
2. DATA_MODELS_DOCUMENTATION.md (2 hours) - Database mastery
3. VOXPLAN_UI_ARCHITECTURE.md (2 hours) - UI component mastery
4. FEATURE_DOCUMENTATION.md (2 hours) - Feature implementation details

**Ongoing Reference**
- Keep quick reference docs handy
- Consult INCOMPLETE_FEATURES.md before working on Dailies/Scheduling
- Review FEATURE_DIAGRAMS.md for visual workflows

---

### For Specific Tasks

**Adding a New Feature**
1. ARCHITECTURE.md (state management, ViewModel patterns)
2. FEATURE_DOCUMENTATION.md (integration points)
3. DATA_MODELS_DOCUMENTATION.md (database changes)
4. VOXPLAN_UI_ARCHITECTURE.md (UI patterns)

**Fixing a Bug**
1. INCOMPLETE_FEATURES.md (known issues)
2. ARCHITECTURE.md (relevant component section)
3. Specific feature docs

**Database Migration**
1. DATA_MODELS_DOCUMENTATION.md (migration history)
2. ARCHITECTURE.md (database architecture section)
3. DATABASE_SCHEMA_VISUAL.md (visual relationships)

**UI Enhancement**
1. VOXPLAN_UI_ARCHITECTURE.md (screen architecture)
2. VOXPLAN_UI_FILE_INDEX.md (file locations)
3. ARCHITECTURE.md (state management patterns)

**Code Review**
1. ARCHITECTURE.md (architectural patterns)
2. FEATURES_SUMMARY.md (known issues)
3. Specific component documentation

---

## Documentation Maintenance

### When to Update Documentation

**Always update when:**
- Adding new screens or major UI components → Update VOXPLAN_UI_ARCHITECTURE.md
- Adding/modifying database entities → Update DATA_MODELS_DOCUMENTATION.md
- Creating new ViewModels or state patterns → Update ARCHITECTURE.md
- Implementing incomplete features → Update INCOMPLETE_FEATURES.md, FEATURES_SUMMARY.md
- Database migrations → Update DATA_MODELS_DOCUMENTATION.md (migration history)
- Fixing critical bugs → Update INCOMPLETE_FEATURES.md

**Consider updating when:**
- Significant architectural changes → All architecture docs
- New navigation flows → ARCHITECTURE.md, VOXPLAN_UI_ARCHITECTURE.md
- Repository/DAO changes → DATA_MODELS_DOCUMENTATION.md
- Feature completion → FEATURES_SUMMARY.md status updates

---

## Finding Information Fast

### By Topic

**State Management** → ARCHITECTURE.md (Section 1)
**ViewModels** → ARCHITECTURE.md (Section 1.2), ARCHITECTURE_QUICK_REFERENCE.md
**Database Queries** → DATA_MODELS_DOCUMENTATION.md (DAOs section)
**UI Composables** → VOXPLAN_UI_ARCHITECTURE.md, VOXPLAN_UI_FILE_INDEX.md
**Data Entities** → DATA_MODELS_DOCUMENTATION.md (Entities section)
**Navigation Routes** → ARCHITECTURE.md (Section 4.3), VOXPLAN_UI_ARCHITECTURE.md
**Repository Methods** → DATA_MODELS_DOCUMENTATION.md (Repositories section)
**Flow Patterns** → ARCHITECTURE.md (Section 3)
**Feature Integration** → FEATURE_DOCUMENTATION.md
**Known Bugs** → INCOMPLETE_FEATURES.md
**Visual Diagrams** → FEATURE_DIAGRAMS.md, DATABASE_SCHEMA_VISUAL.md

### By File Type

**Kotlin File Locations** → VOXPLAN_UI_FILE_INDEX.md, ARCHITECTURE.md (Section 9)
**Database Files** → DATA_MODELS_DOCUMENTATION.md
**UI Files** → VOXPLAN_UI_FILE_INDEX.md
**ViewModel Files** → ARCHITECTURE.md (Section 1.2)

### By Problem Type

**Bug Fixing** → INCOMPLETE_FEATURES.md → Relevant architecture docs
**Performance Issues** → ARCHITECTURE.md (reactive patterns, state management)
**Database Issues** → DATA_MODELS_DOCUMENTATION.md
**UI Issues** → VOXPLAN_UI_ARCHITECTURE.md
**Navigation Issues** → ARCHITECTURE.md (Section 4.3)

---

## Document Statistics

**Total Documentation:** ~19 files
**Total Estimated Lines:** ~5,000+ lines
**Most Comprehensive:** ARCHITECTURE.md (1,217 lines)
**Quick Reference Docs:** 5 files
**Visual/Diagram Docs:** 2 files
**Feature-Specific:** 5 files

**Coverage:**
- ✅ Complete architecture documentation
- ✅ Complete data model documentation
- ✅ Complete UI/UX documentation
- ✅ Feature documentation (with known gaps)
- ✅ Product vision and process
- ⚠️ Testing documentation (minimal)
- ⚠️ API documentation (none - no external APIs)

---

## Getting Help

**Can't find what you're looking for?**
1. Check this README's "Finding Information Fast" section
2. Use your IDE's file search for specific terms
3. Consult DOCUMENTATION_INDEX.md for incomplete features
4. Search through ARCHITECTURE.md - it's the most comprehensive

**Documentation appears outdated?**
- Check file timestamps
- Cross-reference with actual code
- Update documentation when you find discrepancies

**Need to understand a specific component?**
- Start with quick reference docs
- Dive into comprehensive docs for details
- Check visual diagrams for workflows

---

## Contributing to Documentation

When adding or updating documentation:
1. Update this README if adding new doc files
2. Maintain consistent formatting and structure
3. Include code examples where helpful
4. Add line counts for major sections
5. Cross-reference related documentation
6. Update "Last Updated" dates

---

**Happy Coding! 🚀**

For project build instructions and development setup, see `/CLAUDE.md` in the root directory.
