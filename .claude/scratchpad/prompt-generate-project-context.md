# Prompt: Generate Project Context Documentation for VoxPlanApp

## Objective
Create a comprehensive `project_context.md` file for VoxPlanApp that serves as the high-level, non-technical overview of the project's vision, philosophy, user workflows, and domain concepts.

## Purpose of project_context.md
This document should provide:
1. **Strategic Vision**: The "why" behind the project - its philosophy, goals, and unique value proposition
2. **User-Facing Perspective**: Focus on what users experience and accomplish, not how it's implemented
3. **Domain Knowledge**: Core concepts, terminology, and mental models that define the problem space
4. **Workflow Documentation**: Key user journeys and interaction patterns from a functional perspective
5. **Product Definition**: Clear articulation of MVP/MLP scope and feature priorities
6. **Context for AI Agents**: Enable AI assistants to understand the project's purpose without diving into code

## Target Audience
- New developers joining the project who need to understand "what we're building and why"
- AI coding assistants that need conceptual grounding before making technical changes
- Stakeholders and designers who need to understand the product vision
- Future team members who need to grasp the domain model quickly

## Required Sections (Based on VoxManifestorApp Structure)

### 1. High-Level Project Overview
- **What to include**:
  - 2-3 paragraph description of VoxPlanApp's core purpose
  - What problem it solves for users
  - Key differentiators from other productivity apps
  - Current version and major milestones achieved

- **For VoxPlanApp, consider**:
  - Hierarchical goal management system
  - Time tracking and quota-based planning
  - Gamified focus sessions with medals
  - Relationship between goals, dailies, scheduling, and focus mode

### 2. Vision & Philosophy
- **What to include**:
  - Long-term vision for the app's evolution
  - Design philosophy and principles guiding development
  - Core beliefs about productivity, goal management, or time tracking
  - How the app aims to change user behavior or thinking

- **For VoxPlanApp, explore**:
  - Philosophy behind hierarchical goal decomposition (max depth 3)
  - Why quotas and time banking matter
  - The role of gamification (medals) in sustained focus
  - Balance between structure (scheduling) and flexibility (dailies)

### 3. Core Concepts (Domain Model - Non-Technical)
- **What to include**:
  - Key domain entities explained in user-facing language
  - Relationships between concepts (conceptual, not technical)
  - Terminology glossary
  - Mental models users should understand

- **For VoxPlanApp, document**:
  - **Goals/Subgoals**: Hierarchical structure (parent → child → grandchild), why max depth 3
  - **Dailies**: Parent daily tasks vs scheduled child events, relationship to goals
  - **Quotas**: Daily time allocations, active days encoding concept (Mon-Sun pattern)
  - **Time Banking**: Accrued time from focus sessions, how it converts to scheduled blocks
  - **Focus Mode**: Pomodoro-style sessions, medal system (Bronze→Silver→Gold→Diamond)
  - **Events**: Dual purpose (dailies vs scheduled blocks), parent-child relationship
  - **ActionMode**: Vertical and hierarchical reordering concepts

### 4. Key User Workflows
- **What to include**:
  - Primary user journeys from start to finish
  - Decision points and branching paths
  - Expected user mental model at each step
  - How workflows connect to achieve larger goals

- **For VoxPlanApp, map out**:
  1. **Goal Management Workflow**:
     - Creating hierarchical goals (breadcrumb navigation)
     - Vertical and hierarchical reordering via ActionMode
     - Setting quotas and active days for goals

  2. **Daily Planning Workflow**:
     - Reviewing daily tasks (parent dailies without time slots)
     - Adding quota-based tasks automatically
     - Vertical reordering of dailies
     - Transitioning to scheduling or focus mode

  3. **Scheduling Workflow**:
     - Day-by-day event viewing and management
     - Creating scheduled child events from parent dailies
     - Drag-to-schedule interactions (if applicable)
     - Relationship between dailies and scheduled blocks

  4. **Focus Mode Workflow**:
     - Starting focus session from goal or event
     - Timer management and discrete task completion
     - Medal earning progression (30min → 60min → 90min → 120min+)
     - Time banking and converting to scheduled events

  5. **Progress Tracking Workflow**:
     - Weekly quota progress visualization
     - Time bank review and allocation
     - Goal completion tracking

### 5. Challenges & Design Decisions
- **What to include**:
  - Key challenges faced in the problem domain
  - Major design decisions and their rationale
  - Tradeoffs made and why
  - User experience complexities addressed

- **For VoxPlanApp, explore**:
  - Why single Event entity for both dailies and scheduled blocks (vs separate tables)
  - Challenges of hierarchical goal management (navigation, reordering)
  - Balancing quota rigidity with daily flexibility
  - Focus mode gamification without over-optimization
  - Parent-child event relationship complexity

### 6. MVP/MLP Definition
- **What to include**:
  - Clear definition of Minimal Viable/Lovable Product scope
  - Core features required for initial value delivery
  - Features explicitly deferred for later iterations
  - Success criteria for MVP validation

- **For VoxPlanApp, define**:
  - Current state: v3.2 with dailies parent-child events
  - Incomplete features analysis (from INCOMPLETE_FEATURES.md):
    - Dailies: ~70% complete (missing completion tracking, bulk operations)
    - Scheduling: ~65% complete (missing event creation, recurrence, color-coding)
  - Critical bugs to address (delete dialog in DaySchedule.kt)
  - Next iteration priorities

### 7. Glossary
- **What to include**:
  - Alphabetical list of domain-specific terms
  - Clear, concise definitions (1-2 sentences each)
  - Cross-references where concepts relate

- **For VoxPlanApp, define terms like**:
  - ActionMode, Active Days, Breadcrumb Navigation, Daily, Event, Focus Mode, Goal, Medal, Parent Daily, Quota, Scheduled Block, Subgoal, Time Bank, etc.

## Style Guidelines

### Tone & Perspective
- Write from user perspective, not developer perspective
- Use "users can..." not "the system supports..."
- Avoid implementation details (Room, StateFlow, ViewModels, etc.)
- Focus on "what" and "why", not "how"

### Language
- Clear, accessible language (avoid jargon unless defined in glossary)
- Active voice preferred
- Present tense for current state, future tense for vision
- Concrete examples over abstract descriptions

### Structure
- Use markdown headers for clear hierarchy
- Include inline links between related sections
- Use bullet points for lists, numbered lists for sequential workflows
- Include visual descriptions where diagrams would help (even if not rendering diagrams)

## What NOT to Include
- ❌ Code snippets or file paths
- ❌ Technical architecture details (MVVM, repositories, DAOs)
- ❌ Database schema or migration information
- ❌ Implementation patterns (StateFlow, Room, Compose specifics)
- ❌ Build commands or development setup
- ❌ Specific line numbers or code references

These belong in `codebase_context.md` instead.

## Research Strategy

### Sources to Analyze
1. **Existing Documentation** (docs/ directory):
   - ARCHITECTURE.md - Extract user-facing workflow descriptions
   - INCOMPLETE_FEATURES.md - Understand feature state and priorities
   - FEATURES_SUMMARY.md - Quick feature overview
   - CLAUDE.md - Project overview and domain concepts

2. **Data Models** (conceptual understanding):
   - TodoItem entity → Goal concept
   - Event entity → Daily vs Scheduled Block distinction
   - Quota entity → Time allocation concept
   - TimeBank entity → Accrued time concept

3. **Key UI Screens** (workflow inference):
   - MainScreen.kt → Goal hierarchy navigation workflow
   - DailyScreen.kt → Daily planning workflow
   - DaySchedule.kt → Scheduling workflow
   - FocusModeScreen.kt → Focus session workflow
   - ProgressViewModel.kt → Progress tracking workflow

4. **Critical Business Logic** (domain rules):
   - SharedViewModel.processGoals() → Hierarchical goal processing concept
   - Quota active days encoding → Weekly pattern concept
   - Event parent-child relationship → Daily→Scheduled transformation

### Analysis Approach
1. Read existing documentation to understand current project state
2. Infer user workflows from UI screen structure and ViewModel logic
3. Extract domain concepts from data model relationships
4. Identify design decisions from code patterns and architecture choices
5. Map incomplete features to MVP/MLP definition

## Deliverable Format
Create a single markdown file: `/Users/richardthompson/StudioProjects/VoxPlanApp/agent/context/project_context.md`

The file should be:
- **Comprehensive**: 800-1500 lines covering all required sections
- **Accessible**: Readable by non-technical stakeholders
- **Cohesive**: Sections flow logically and reference each other
- **Current**: Reflects v3.2 state and known issues
- **Vision-Forward**: Balances current state with future direction

## Success Criteria
After reading this document, a new team member or AI assistant should be able to:
1. Explain VoxPlanApp's core value proposition in 2-3 sentences
2. Describe the key user workflows without seeing the code
3. Understand domain terminology and use it correctly
4. Identify what's in scope for MVP vs future iterations
5. Grasp the design philosophy behind major product decisions
6. Navigate to the appropriate section to answer conceptual questions

## Example Opening (for inspiration)
```markdown
# Project Context: VoxPlanApp

## High-Level Project Overview
VoxPlanApp is an Android productivity application that helps users achieve their goals through hierarchical planning, time-based quotas, and gamified focus sessions. Unlike traditional task managers that treat all tasks equally, VoxPlanApp recognizes that goals exist in nested hierarchies—big ambitions decompose into smaller subgoals, which further break down into actionable tasks. The app provides four integrated modes of interaction: goal hierarchy management, daily task planning, time-block scheduling, and focused work sessions with real-time tracking.

**Current Version:** 3.2 (Dailies improved with parent/child Events)
...
```

---

## Next Steps
1. Read this prompt carefully
2. Gather information from the sources listed above
3. Create the `project_context.md` file following this structure
4. Ensure every section is complete and coherent
5. Cross-reference with codebase_context.md to ensure no overlap in technical details
