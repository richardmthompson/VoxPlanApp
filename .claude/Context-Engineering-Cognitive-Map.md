# Context Engineering Cognitive Map
## AR Automation Website Development Framework

**Last Updated:** October 2025
**Version:** 1.0
**Purpose:** Comprehensive guide to the context engineering system used in this repository for AI-assisted development

---

## Table of Contents

1. [Executive Summary](#executive-summary)
2. [System Architecture Overview](#system-architecture-overview)
3. [The Three Pillars](#the-three-pillars)
4. [Product Requirement Prompts (PRPs)](#product-requirement-prompts-prps)
5. [Commands System](#commands-system)
6. [Artifacts System](#artifacts-system)
7. [Integration Patterns](#integration-patterns)
8. [Workflow Examples](#workflow-examples)
9. [Best Practices & Principles](#best-practices--principles)
10. [CLAUDE.md Role](#claudemd-role)
11. [Getting Started Guide](#getting-started-guide)

---

## Executive Summary

This repository uses a **sophisticated context engineering framework** that transforms AI-assisted development from reactive code generation into a **systematic, workflow-driven process** capable of delivering production-ready features on the first pass.

### The Core Problem This System Solves

> "Over-specifying what to build while under-specifying the context, and how to build it, is why so many AI-driven coding attempts stall at 80%. Our context engineering system fixes that by fusing the disciplined scope of a classic PRD with the 'context-is-king' mindset of modern prompt engineering."

### What Makes This System Unique

1. **Context Completeness**: "No Prior Knowledge" test - every specification includes exact file paths, code patterns, library documentation, and known gotchas
2. **Multi-Level Validation**: 4-level validation loop (Syntax → Unit → Integration → Creative) catches defects early
3. **Self-Improving**: Analysis loop learns from every execution, improving templates and patterns over time
4. **Massively Parallel**: Experimental commands can coordinate 25+ AI agents working simultaneously
5. **Strategic Alignment**: Artifacts bridge business strategy with technical implementation

### System Components

```
.claude/
├── PRPs/              # Implementation specifications (7 template types)
├── commands/          # Slash command orchestration (41 commands in 7 categories)
├── artifacts/         # Strategic knowledge & marketing outputs
└── settings.local.json # Claude Code configuration
```

---

## System Architecture Overview

### The Context Engineering Stack

```
┌─────────────────────────────────────────────────────────┐
│  LAYER 1: STRATEGIC CONTEXT (.claude/artifacts/)        │
│  ├─ Business positioning & brand strategy               │
│  ├─ Market research & competitive intelligence          │
│  ├─ Marketing frameworks (StoryBrand brandscripts)      │
│  └─ Strategic planning documents                        │
│                                                          │
│  Purpose: Answers "What should we build and why?"       │
│  Format: Narrative, strategic, persuasive               │
└─────────────┬───────────────────────────────────────────┘
              │ provides strategic context to
              ▼
┌─────────────────────────────────────────────────────────┐
│  LAYER 2: IMPLEMENTATION SPECS (.claude/PRPs/)          │
│  ├─ Product Requirement Prompts (7 template types)      │
│  ├─ Technical specifications with validation gates      │
│  ├─ Exact file paths, code patterns, gotchas            │
│  └─ Knowledge base (failure patterns, success metrics)  │
│                                                          │
│  Purpose: Answers "How should we build it?"             │
│  Format: Structured, technical, prescriptive            │
└─────────────┬───────────────────────────────────────────┘
              │ guides implementation of
              ▼
┌─────────────────────────────────────────────────────────┐
│  LAYER 3: ORCHESTRATION (.claude/commands/)             │
│  ├─ PRP creation/execution commands (15)                │
│  ├─ Development workflows (6)                           │
│  ├─ Code quality & review (3)                           │
│  ├─ Rapid/parallel development (8 experimental)         │
│  ├─ TypeScript specialization (4)                       │
│  ├─ Git operations (3)                                  │
│  └─ Business strategy automation (2)                    │
│                                                          │
│  Purpose: Orchestrates AI agents through workflows      │
│  Format: Executable slash commands                      │
└─────────────┬───────────────────────────────────────────┘
              │ produces
              ▼
┌─────────────────────────────────────────────────────────┐
│  LAYER 4: IMPLEMENTATION (frontend/, backend/)          │
│  ├─ Working code with validation                        │
│  ├─ Tests at all levels                                 │
│  ├─ Documentation and comments                          │
│  └─ Deployment-ready features                           │
│                                                          │
│  Purpose: Delivers production-ready software            │
│  Format: Runnable code, tests, docs                     │
└─────────────────────────────────────────────────────────┘
```

### Information Flow

```
USER INPUT
    │
    ▼
SLASH COMMAND (/prp-story-create "Add user auth")
    │
    ├──→ Spawns Research Agents (parallel)
    │    ├─ Search codebase for patterns
    │    ├─ Research external libraries
    │    ├─ Identify integration points
    │    └─ Design validation strategy
    │
    ├──→ Reads Artifacts for Context
    │    ├─ Brand positioning
    │    ├─ Strategic priorities
    │    └─ Business requirements
    │
    ▼
PRP GENERATION (PRPs/story_user_auth.md)
    ├─ Goal & Success Criteria
    ├─ Context (file paths, patterns, gotchas)
    ├─ Implementation Blueprint (ordered tasks)
    └─ Validation Gates (4 levels)
    │
    ▼
PRP EXECUTION (/prp-story-execute)
    ├─ Task 1 → Validate → Task 2 → Validate → ...
    ├─ Level 1: Syntax & Style checks
    ├─ Level 2: Unit tests per component
    ├─ Level 3: Integration testing
    └─ Level 4: Creative/domain validation
    │
    ▼
PRODUCTION CODE
    ├─ Feature implemented
    ├─ All tests passing
    ├─ Documentation complete
    └─ Ready for deployment
    │
    ▼
ANALYSIS & LEARNING (/prp-analyze-run)
    ├─ Extract success/failure patterns
    ├─ Update knowledge base
    └─ Improve templates for future
```

---

## The Three Pillars

### 1. PRPs (Product Requirement Prompts)
**Location:** `.claude/PRPs/`
**Purpose:** Implementation specifications that provide everything an AI agent needs to build features correctly on the first pass

**Key Characteristics:**
- Extends traditional PRDs with 3 AI-critical layers: Context + Implementation Strategy + Validation Gates
- Passes "No Prior Knowledge" test - complete even without codebase familiarity
- Information-dense keywords (CREATE, UPDATE, PATTERN, GOTCHA, VALIDATE)
- Executable validation commands at every level

### 2. Commands (Slash Commands)
**Location:** `.claude/commands/`
**Purpose:** Meta-prompt library encoding expert workflows as reusable, composable commands

**Key Characteristics:**
- 41 commands across 7 categories
- Create/Execute pairs (planning separated from implementation)
- Parallel agent spawning for research and implementation
- Emphasis on validation gates and quality controls

### 3. Artifacts (Strategic Knowledge)
**Location:** `.claude/artifacts/`
**Purpose:** Strategic knowledge repository bridging business strategy and technical implementation

**Key Characteristics:**
- Marketing frameworks (StoryBrand brandscripts)
- Business strategy documents
- Research and competitive intelligence
- Inputs for development work, not code

---

## Product Requirement Prompts (PRPs)

### What is a PRP?

> "A PRP is a structured prompt that supplies an AI coding agent with everything it needs to deliver a vertical slice of working software—no more, no less."

### PRP vs Traditional PRD

| **Traditional PRD** | **Product Requirement Prompt (PRP)** |
|---------------------|--------------------------------------|
| Describes **what** to build | **What** + **How** + **Context** + **Validation** |
| Avoids implementation details | Explicit implementation strategy with patterns |
| For human developers who can ask questions | For AI agents that need complete context upfront |
| No validation strategy | 4-level deterministic validation gates |
| Generic context references | Exact file paths (with line numbers), URLs (with anchors) |

### The Three AI-Critical Layers

**Layer 1: Context** (What agents need to understand the codebase)
```yaml
Context References:
  - file: src/services/database_service.py
    why: Follow service structure and error handling pattern
    pattern: Async session context manager (lines 45-67)
    gotcha: Always use async with get_session() as session

  - url: https://docs.python.org/3/library/asyncio-task.html#asyncio.create_task
    why: Task creation for background jobs
    critical: Must use create_task(), not run() inside running loop
```

**Layer 2: Implementation Strategy** (Explicit how-to with ordered tasks)
```yaml
Task 2: CREATE services/user_service.py
  - IMPLEMENT: UserService class with async CRUD operations
  - PATTERN: Follow services/product_service.py structure
  - IMPORTS: from models.user import User; from db import get_session
  - GOTCHA: Always use async session context manager
  - VALIDATE: uv run python -c "from services.user_service import UserService"
```

**Layer 3: Validation Gates** (4-level quality control)
```yaml
Level 1: Syntax & Style
  - RUN: ruff check src/ --fix && mypy src/
  - CONTINUE: Only when zero errors

Level 2: Unit Tests (per component)
  - RUN: pytest src/services/tests/test_user_service.py -v
  - REQUIRE: All passing, 80%+ coverage

Level 3: Integration
  - RUN: curl -X POST http://localhost:8000/api/users
  - VERIFY: Service startup, endpoint response, database writes

Level 4: Creative & Domain-Specific
  - USE: MCP servers, performance testing, security scans
  - EXAMPLE: playwright-mcp --test-user-journey
```

### PRP Templates (7 Types)

#### 1. **prp_planning.md** - Strategic Planning
**When to Use:** Start of major features, vague requirements, stakeholder alignment needed

**Process:**
1. Research (market analysis, technical research, codebase context)
2. Diagram generation (user flows, architecture, sequences, data flows)
3. Devil's advocate analysis (risks, edge cases)
4. PRD structure generation

**Output:** Comprehensive PRD with Mermaid diagrams → feeds into implementation PRPs

---

#### 2. **prp_base.md** - General Implementation
**When to Use:** Production features requiring comprehensive validation (language-agnostic structure; examples use Python/FastAPI but adapt to any stack)

**Structure:**
```yaml
- Goal (feature goal, deliverable, success definition)
- User Persona (target user, use case, journey, pain points)
- Why (business value, integration, problems solved)
- What (user-visible behavior, success criteria)
- Context (docs, codebase tree, gotchas)
- Implementation Blueprint (data models, ordered tasks, patterns)
- Validation Loop (4 levels)
- Final Checklist (technical, feature, quality)
```

**Note:** Template structure is language-agnostic. Examples and validation commands use Python/FastAPI because the original author works primarily in Python. Adapt examples, file paths, and validation commands to your stack (Go, Rust, Java, C#, etc.).

---

#### 3. **prp_base_typescript.md** - Frontend Implementation
**When to Use:** React/TypeScript/Next.js features, client-side development

**TypeScript-Specific:**
- React component patterns (Server/Client components)
- Next.js App Router conventions
- TypeScript interfaces, Zod schemas
- Frontend validation (ESLint, tsc, build validation)
- Client/Server gotchas (`'use client'` directive placement)

---

#### 4. **prp_story_task.md** - User Story Implementation
**When to Use:** Implementing Jira/Linear stories, sprint planning, atomic task breakdown

**Features:**
- Original story metadata (type, complexity, affected systems)
- Information-dense keyword format
- Each task atomic and independently testable
- Embedded validation commands

**Keyword System:**
- **IMPLEMENT**: Core functionality to build
- **PATTERN**: Existing pattern to follow
- **IMPORTS**: Required dependencies
- **GOTCHA**: Known pitfalls to avoid
- **VALIDATE**: Executable verification command

---

#### 5. **prp_task.md** - Micro-Task Format
**When to Use:** Quick, concise task descriptions, debugging, small fixes

**Action Keywords:**
- **READ**: Understand patterns
- **CREATE**: New file
- **UPDATE**: Modify existing
- **DELETE**: Remove code
- **FIND**: Search for patterns
- **TEST**: Verify behavior
- **FIX**: Debug and repair

---

#### 6. **prp_spec.md** - Simplified Specification
**When to Use:** Clear objectives, straightforward implementations, well-understood context

**Structure:**
```yaml
- High-Level Objective
- Mid-Level Objective (steps to achieve)
- Implementation Notes (technical details)
- Context (beginning → ending state)
- Low-Level Tasks (ordered start to finish)
```

**Philosophy:** Simple, direct, focused on execution over comprehensive documentation

---

#### 7. **prp_poc_react.md** - Prototype/POC
**When to Use:** Proof-of-concepts, stakeholder validation, multiple parallel POC variations

**POC Approach:**
- "Working over excellent" - speed prioritized
- Mock data only (no backend integration)
- Fidelity levels: Demo (polished UI) vs MVP (near-production)
- 10+ concurrent POCs supported

**POC Anti-Patterns:**
- ❌ Over-engineering (skip complex architecture)
- ❌ Full error handling (happy path only)
- ❌ Comprehensive testing (smoke tests only)
- ❌ Real API integration (mocks exclusively)

---

### PRP Template Comparison Matrix

| Template | Complexity | Validation | Best For | Time to Complete |
|----------|-----------|------------|----------|------------------|
| **prp_planning** | High | Research | Strategic planning, PRDs | 2-4 hours |
| **prp_base** | High | 4-level | Production features (any language) | 4-8 hours |
| **prp_base_typescript** | High | 4-level | Production frontend | 4-8 hours |
| **prp_story_task** | Medium | Task-level | User stories | 2-4 hours |
| **prp_task** | Low | Checkpoint | Bug fixes, micro-tasks | 30min-2hrs |
| **prp_spec** | Medium | Moderate | Straightforward features | 2-4 hours |
| **prp_poc_react** | Medium | 3-level POC | Prototypes, demos | 1-3 hours |

---

### PRP Lifecycle

```
┌─────────────────────────────────────────────────────────┐
│  PHASE 1: RESEARCH & CREATION                           │
│  Command: /prp-story-create "feature description"       │
│                                                          │
│  1. Spawn parallel research agents                      │
│     ├─ Codebase pattern search                          │
│     ├─ External library research                        │
│     ├─ Integration point identification                 │
│     └─ Validation strategy design                       │
│                                                          │
│  2. Read strategic context from artifacts               │
│                                                          │
│  3. Generate PRP from template                          │
│     ├─ Fill context layer (files, patterns, gotchas)    │
│     ├─ Create implementation tasks (ordered, atomic)    │
│     ├─ Define validation gates (executable commands)    │
│     └─ Score confidence (1-10 likelihood of success)    │
│                                                          │
│  4. Apply "No Prior Knowledge" test                     │
│     └─ Validate: Complete without codebase familiarity? │
│                                                          │
│  Output: PRPs/{feature-name}.md                         │
└─────────────────────────────────────────────────────────┘
              │
              ▼
┌─────────────────────────────────────────────────────────┐
│  PHASE 2: EXECUTION                                     │
│  Command: /prp-story-execute "PRPs/{feature-name}.md"  │
│                                                          │
│  Philosophy: Complete → Validate → Next                 │
│  "No task left behind"                                  │
│                                                          │
│  For each task:                                         │
│    1. Implement task                                    │
│    2. Run task validation command                       │
│    3. If pass → next task                               │
│    4. If fail → debug, fix, re-validate                 │
│                                                          │
│  After all tasks:                                       │
│    Run 4-level validation loop                          │
│                                                          │
│  Output: Working code + passing tests                   │
└─────────────────────────────────────────────────────────┘
              │
              ▼
┌─────────────────────────────────────────────────────────┐
│  PHASE 3: ANALYSIS & LEARNING                           │
│  Command: /prp-analyze-run "PRPs/{feature-name}.md"    │
│                                                          │
│  1. Collect metrics                                     │
│     ├─ Implementation time                              │
│     ├─ Commits and files changed                        │
│     ├─ Test results (coverage, passing rate)            │
│     └─ Code quality scores                              │
│                                                          │
│  2. Analyze context effectiveness                       │
│     ├─ Which references were actually used?             │
│     ├─ What context was missing?                        │
│     └─ Which gotchas prevented issues?                  │
│                                                          │
│  3. Extract patterns                                    │
│     ├─ Failure patterns → PRPs/knowledge_base/          │
│     ├─ Success metrics → success_metrics.yaml           │
│     └─ Template improvements identified                 │
│                                                          │
│  4. Update knowledge base                               │
│     └─ Future PRPs benefit from learnings               │
│                                                          │
│  Output: Improved system for next iteration             │
└─────────────────────────────────────────────────────────┘
```

---

## Commands System

### Purpose

The commands system is a **meta-prompt library** that encodes expert development workflows into reusable slash commands. Each command is a carefully engineered prompt guiding Claude through multi-step processes while maintaining best practices.

### Command Categories (41 Total)

#### 1. **PRP Commands** (15 commands) - Core System

**Creation Commands** (Research → Document):
- `prp-base-create.md` - General-purpose PRP
- `prp-story-create.md` - User story → PRP
- `prp-planning-create.md` - Strategic planning PRP
- `prp-spec-create.md` - Simplified spec PRP
- `prp-task-create.md` - Micro-task PRP
- `prp-ts-create.md` - TypeScript PRP
- `prp-poc-create-parallel.md` - POC/prototype PRP

**Execution Commands** (Load → Validate → Implement):
- `prp-base-execute.md` - Execute general PRP
- `prp-story-execute.md` - Execute story PRP (task-by-task)
- `prp-spec-execute.md` - Execute spec PRP
- `prp-task-execute.md` - Execute task PRP
- `prp-ts-execute.md` - Execute TypeScript PRP
- `prp-poc-execute-parallel.md` - Execute POC PRP

**Supporting**:
- `task-list-init.md` - Break PRP into detailed checklist
- `api-contract-define.md` - Define backend/frontend contracts

**Pattern:** All follow **create/execute pairs** - planning separated from implementation

---

#### 2. **Rapid Development / Experimental** (8 commands) - Advanced

**Massive Parallelization:**
- **`hackathon-prp-parallel.md`** - The crown jewel
  - Deploys **25 AI agents in 5 waves**
  - Wave 1: 5 spec generation agents (architecture, UX, business, testing, demo)
  - Wave 2: 5 execution plan agents (backend, frontend, database, DevOps, quality)
  - Wave 3: 10 implementation agents (5 backend + 5 frontend working in parallel)
  - Wave 4: 2 integration agents (full-stack, demo prep)
  - Wave 5: 3 QA agents (backend, frontend, integration)
  - **Time estimate:** Zero → production in ~40 minutes
  - **Success metrics:** 80%+ test coverage, <100ms response times

**Comparative Analysis:**
- **`parallel-prp-creation.md`** - Create 2-5 PRP variations simultaneously
  - Each agent explores different approach (Performance, Security, Maintainability, Rapid, Enterprise)
  - Enables informed selection of optimal strategy
  - Output: Multiple PRPs + comparison analysis

**Self-Improvement:**
- **`prp-analyze-run.md`** - Post-execution analysis
  - Collects metrics (time, commits, tests, quality)
  - Analyzes context effectiveness
  - Extracts failure/success patterns
  - Updates knowledge base → continuous improvement

**Other Experimental:**
- `create-base-prp-parallel.md` - Parallel base PRP creation
- `create-planning-parallel.md` - Parallel planning
- `hackathon-research.md` - Rapid research mode
- `user-story-rapid.md` - Fast user story implementation
- `prp-validate.md` - PRP quality validation

---

#### 3. **Development Workflows** (6 commands)

**Core Workflows:**
- **`smart-commit.md`** - Intelligent git commits
  - Analyzes changes, suggests conventional commit message
  - Offers to modify, stage different files, push/create PR
  - Interactive with user approval gates

- **`onboarding.md`** - Project documentation generator
  - 10-section analysis: Overview, Structure, Setup, Components, Workflow, Architecture, Tasks, Gotchas, Docs, Checklist
  - Outputs: ONBOARDING.md, QUICKSTART.md, README suggestions
  - Goal: New developer productive in <1 hour

- **`prime-core.md`** - Context priming
  - Reads CLAUDE.md, README.md, key source files
  - Explains back project structure for validation
  - Ensures Claude has full context before work

- **`create-pr.md`** - Pull request creation
- **`new-dev-branch.md`** - Branch management
- **`debug-RCA.md`** - Root cause analysis debugging

**Pattern:** These encode **institutional knowledge** - how the team prefers to work

---

#### 4. **Code Quality** (3 commands)

**Review Commands:**
- **`review-staged-unstaged.md`** - Comprehensive code review
  - 7 focus areas: Code quality, Pydantic v2, Security, Structure, Linting, Testing, Performance
  - Output: Critical/Important/Minor issues with file:line references
  - Saves to: `PRPs/code_reviews/review[#].md`
  - Project-specific: Enforces Pydantic v2, vertical slice architecture

- **`refactor-simple.md`** - Quick refactoring scan
  - Identifies: Functions >20 lines, long files, missing types, cross-feature imports, SRP violations
  - Output: `PRPs/ai_docs/refactor_plan.md` with <1hr fixes

- **`review-general.md`** - General code review

**Pattern:** Automated senior engineer reviews - maintain quality and architectural consistency

---

#### 5. **Git Operations** (3 commands)

**Intelligent Conflict Resolution:**
- **`smart-resolver.md`** - Context-aware merge conflict resolution
  - Pre-resolution analysis: Understand what each branch was trying to achieve
  - Different strategies per file type (source, tests, config, docs, lock files)
  - Post-resolution verification: Linters, type checkers, test suite, semantic conflicts
  - Creates detailed resolution summary with TODOs

- **`conflict-resolver-general.md`** - General conflict resolution
- **`conflict-resolver-specific.md`** - File-specific resolution

**Pattern:** Goes beyond mechanical resolution to understand **intent**

---

#### 6. **TypeScript Commands** (4 commands)

**TS-Specific PRPs:**
- **`TS-create-base-prp.md`** - TypeScript PRP creation
  - React patterns, Jest/Vitest/Cypress, TypeScript gotchas
  - Validation: `npm run typecheck`, `npm run lint`, `npm test`, `npm run build`
  - Confidence scoring (1-10)

- **`TS-execute-base-prp.md`** - TS PRP execution
- **`TS-review-general.md`** - TypeScript code review
- **`TS-review-staged-unstaged.md`** - TS staged review

**Pattern:** Language-specific variants adapted to ecosystem (npm, JSX, React)

---

#### 7. **AR Strategy** (2 commands)

**Marketing Automation:**
- **`create_brand_story.md`** - StoryBrand (SB7) BrandScript generation
  - 7-component framework: Hero, Problem, Guide, Plan, CTA, Failure, Success
  - Outputs to: `.claude/artifacts/create_brand_story/`

- **`brand_story_marketing_copywriter.md`** - BrandScript → marketing copy

**Pattern:** Domain adaptability - system isn't just for coding

---

### Command File Structure

All commands follow consistent markdown format:

```markdown
---
description: "Brief command purpose (shown in /help)"
arguments: "What user provides"
---

# Command Name

## Mission
[High-level goal of this command]

## Process
[Step-by-step instructions with explicit guidance for Claude]
- Spawn agents with clear contexts
- Read relevant files
- Generate outputs
- Validate results

## Output
[Expected deliverables and file locations]

## Quality Gates
[Validation criteria and success metrics]
```

---

### Key Command Patterns

#### Pattern 1: Create → Execute Pairs
Almost all commands follow this pattern:
```
Planning Phase          Implementation Phase
     ↓                         ↓
prp-base-create  →  →  →  prp-base-execute
     ↓                         ↓
Research, Context        Sequential Validation
```

**Why:** Separates planning from implementation - prevents "coding while thinking"

---

#### Pattern 2: Parallel Research Agents

From `prp-base-create.md`:
> "During the research process, create clear tasks and spawn as many agents and subagents as needed using the batch tools. The deeper research we do here the better the PRP will be."

Commands explicitly spawn multiple concurrent agents:
- One searches for similar patterns
- Another researches library docs
- Another identifies integration points
- Another designs test strategy

**Result:** Comprehensive context in fraction of sequential time

---

#### Pattern 3: Information-Dense Keywords

Tasks use specific verbs with clear semantics:
- **CREATE** - New files from scratch
- **UPDATE** - Modify existing files
- **ADD** - Insert new functionality
- **REMOVE** - Delete deprecated code
- **REFACTOR** - Restructure without changing behavior
- **MIRROR** - Copy pattern from elsewhere

Example:
```yaml
Task 2: UPDATE api/routes.py
  - FIND: app.include_router(product_router)
  - INSERT: app.include_router(user_router, prefix="/users")
  - PRESERVE: Existing router registrations
  - VALIDATE: grep -q "user_router" api/routes.py && echo "✓"
```

---

#### Pattern 4: Validation Gates at Every Level

All commands emphasize continuous validation:

**Level 1: Syntax & Style** (immediate)
```bash
ruff check src/ --fix
mypy src/
npm run lint
npx tsc --noEmit
```

**Level 2: Unit Tests** (component)
```bash
pytest src/services/tests/ -v
npm test -- component.test.tsx
```

**Level 3: Integration** (system)
```bash
docker-compose up -d
curl http://localhost:8000/health
```

**Level 4: Creative/Domain** (custom)
```bash
playwright-mcp --test-user-journey
lighthouse-mcp --url http://localhost:3000
```

---

#### Pattern 5: Subagent Context Propagation

From `prp-story-create.md`:
> "Remember that subagents will only receive their details from you, the user has no way of interacting with the subagents. so you need to share all the relevant context to the subagent in the subagent prompt."

Shows sophisticated understanding - parent agent must **fully contextualize** child agents since users can't clarify mid-execution.

---

### Command Integration with PRPs

```
┌──────────────────────────────────────────────────────┐
│  Commands orchestrate the PRP lifecycle              │
└──────────────────────────────────────────────────────┘

Step 1: Command triggers PRP creation
  /prp-story-create "Add user authentication"
        ↓
  Spawns agents, researches context
        ↓
  Generates: PRPs/story_user_auth.md

Step 2: User reviews PRP
  Validate context completeness
  Adjust if needed

Step 3: Command executes PRP
  /prp-story-execute "PRPs/story_user_auth.md"
        ↓
  Task-by-task implementation with validation
        ↓
  Produces: Working, tested code

Step 4: Command analyzes results
  /prp-analyze-run "PRPs/story_user_auth.md"
        ↓
  Extracts patterns, updates knowledge base
        ↓
  Improves: Future PRP templates
```

---

## Artifacts System

### Purpose

The artifacts system is a **strategic knowledge repository** that stores outputs from slash commands and research. Artifacts are **finished deliverables** (marketing copy, strategy docs, research) that serve as **context and inputs for development work**.

### What Makes Artifacts Different from PRPs

| Dimension | **Artifacts** | **PRPs** |
|-----------|---------------|----------|
| **Purpose** | Strategic knowledge & marketing | Implementation instructions |
| **Content** | Business strategy, brand messaging, research | Technical specs, architecture, validation |
| **Audience** | Business stakeholders, marketers, sales | AI coding agents, developers |
| **Output** | Finished deliverables (docs, copy) | Working code, features |
| **Timeframe** | Long-lived, evolving | Task-specific, completable |
| **Format** | Narrative, persuasive, strategic | Structured, technical, prescriptive |

**Key Distinction:**
- **Artifacts** = "What should we say to customers?" (Brand, positioning)
- **PRPs** = "What should the code do?" (Features, validation)

---

### Artifact Types

#### Type A: Foundation Documents
**Purpose:** Single source of truth for business positioning

**Example:** `AR-Automation-Overview.md`
- Executive summary of services
- Target market definitions (E-commerce, Accounting, Education)
- Methodology (4-step transformation)
- Success stories and results
- Value propositions

**Usage:** Referenced by all other artifacts and PRPs for consistency

---

#### Type B: Strategic Research
**Purpose:** Market intelligence, competitive analysis, opportunity mapping

**Example:** `EdTech-Conference-Strategy-INTEGRATED.md` (51KB)
- 32 speaker profiles analyzed
- Solution-to-speaker mapping (product-market fit validation)
- Tier 1/2 prospect prioritization ($1.3M-3.3M pipeline)
- Conversation starters and engagement strategies
- ROI projections (15-20x expected return)

**Notable Features:**
- Detailed prospect intelligence (decision-makers, pain points, opportunities)
- Strategic partnerships (VCs, consultants, publishers)
- Geographic expansion strategies

**Usage:** Operational playbook for business development

---

#### Type C: Marketing Frameworks (Brandscripts)
**Purpose:** Structured brand narratives using StoryBrand methodology

**Location:** `.claude/artifacts/create_brand_story/`

**StoryBrand 7-Part Structure:**
1. **Hero/Character** - Target customer persona
2. **Problem** - External, internal, philosophical problems
3. **Guide** - Your company as empathetic expert
4. **Plan** - 3-step transformation process
5. **Call to Action** - Direct and transitional CTAs
6. **Avoiding Failure** - Stakes if they don't act
7. **Success/Transformation** - Future state vision

**Examples:**
- `educational_institutions_brandscript.md` (38KB) - Most detailed
  - Hero: Directors of Operations, Registrars, EdTech CEOs
  - Problem: Administrative burden (24% of budget)
  - Solution: AI-powered multilingual agents
  - Integration: EdTech Asia Summit 2025 prospect intelligence

- `ecommerce-brandscript.md` (30KB)
  - Hero: Operations Manager at €1-10M revenue businesses
  - Problem: Fragmented systems (inventory, payments, logistics)
  - Transformation: "Overwhelmed Firefighter" → "Strategic Leader"

- `accounting-automation-brand-script.md` (21KB)
  - Hero: Practice Partners/Operations Directors (20-200+ employees)
  - Problem: 66% of staff time on manual work
  - Backed by research citations (Grant Thornton, Mengali Accountancy)

**Usage:** Foundation for website copy, sales materials, email campaigns

---

#### Type D: Tactical Playbooks
**Purpose:** Execution guides with specific actions

**Examples:**
- Conference strategy with conversation starters
- Sales call scripts aligned to brandscripts
- Partnership outreach templates

**Usage:** Operational execution (conferences, sales calls, partnerships)

---

### Artifact Naming Conventions

**Top-Level Artifacts:**
```
AR-Automation-Overview.md          # Foundation document
EdTech-Conference-Strategy-INTEGRATED.md  # Strategic synthesis
EdTech-agenda.md                    # Supporting research
EdTech-Speakers.md                  # Supporting research
```

**Subdirectories for Related Artifacts:**
```
create_brand_story/
├── educational_institutions_brandscript.md
├── educational_institutions_landing_page.md
├── ecommerce-brandscript.md
├── ecommerce-marketing-copy.md
├── accounting-automation-brand-script.md
└── accounting-automation-landing-page-copy.md
```

**Pattern:**
- Descriptive names over abbreviations
- Domain prefix for grouped artifacts (EdTech-*, create_brand_story/*)
- Integration suffix for synthesis (-INTEGRATED, -overview)
- Vertical-specific naming ({vertical}_{artifact-type}.md)

---

### How Artifacts Are Created

#### Trigger 1: Slash Command Execution
```bash
/ar_strategy:create_brand_story
    ↓
Generates brandscript using StoryBrand framework
    ↓
Saves to: .claude/artifacts/create_brand_story/{vertical}_brandscript.md

/ar_strategy:brand_story_marketing_copywriter
    ↓
Converts brandscript → landing page copy
    ↓
Saves to: .claude/artifacts/create_brand_story/{vertical}_landing_page.md
```

#### Trigger 2: Strategic Research
- Conference preparation
- Market analysis
- Competitive intelligence
- Prospect research

#### Trigger 3: Business Planning
- Foundational positioning documents
- Vertical-specific strategies
- Partnership opportunity mapping

---

### Artifacts in the Development Workflow

#### Example 1: Homepage Development
```
Artifact Input:
└─> educational_institutions_brandscript.md
    ├─> Hero: "Directors of Operations"
    ├─> Problem: "Administrative burden consuming 24% of budget"
    └─> CTA: "Schedule Your Administrative Burden Assessment"

PRP References Artifact:
└─> phase-1-foundation-homepage.md
    └─> "Use brand messaging from educational_institutions_brandscript.md"

Code Implements PRP:
└─> frontend/src/components/HeroSection.tsx
    ├─> Headline reflects brandscript positioning
    └─> CTA matches exact wording
```

#### Example 2: Solutions Page
```
Artifact Input:
└─> AR-Automation-Overview.md
    └─> Five education segments:
        ├─> Universities & Colleges
        ├─> EdTech Companies
        ├─> International School Networks
        ├─> Education Publishers
        └─> Assessment Organizations

PRP Output:
└─> edtech-expertise-showcase.md
    └─> Solution cards for each segment

Code Output:
└─> frontend/src/pages/EdTechSolutionsPage.tsx
    └─> Renders five solution segments with segment-specific copy
```

---

### Artifact Management Best Practices

**Do's:**
- ✅ Store strategic thinking (don't lose in chat history)
- ✅ Use descriptive naming conventions
- ✅ Organize by domain/vertical when appropriate
- ✅ Include research citations and data sources
- ✅ Reference artifacts in PRPs to maintain context chain
- ✅ Update artifacts as strategy evolves

**Don'ts:**
- ❌ Don't mix artifacts with PRPs (separate concerns)
- ❌ Don't create artifacts for trivial content (reusability threshold)
- ❌ Don't let artifacts become outdated (establish review cycle)
- ❌ Don't store code in artifacts (belongs in codebase)
- ❌ Don't duplicate information across artifacts (DRY principle)

---

## Integration Patterns

### Pattern 1: Strategic Alignment Flow

```
Business Strategy (Artifacts)
    ↓
Feature Planning (PRPs)
    ↓
Implementation (Code)
```

**Example:**
1. **Artifact Created:** `educational_institutions_brandscript.md`
   - Defines target persona: "Director of Operations"
   - Problem: Administrative burden (24% of budget)

2. **PRP References Artifact:** `phase-1-foundation-homepage.md`
   - Specifies: "Hero headline should address administrative burden"
   - Includes: Exact messaging from brandscript

3. **Code Implements:** `frontend/src/components/HeroSection.tsx`
   - Renders on-brand copy
   - Maintains consistency

**Result:** Business strategy flows seamlessly into technical implementation

---

### Pattern 2: Research → Planning → Execution

```
Step 1: Research (Agents + Artifacts)
  /prp-story-create spawns research agents
  Agents read artifacts for business context
  Agents search codebase for technical patterns

Step 2: Planning (PRP Generation)
  Synthesize research into structured PRP
  Include context, implementation strategy, validation

Step 3: Execution (PRP Implementation)
  /prp-story-execute implements tasks sequentially
  Each task validated before proceeding

Step 4: Learning (Analysis)
  /prp-analyze-run extracts patterns
  Updates knowledge base
```

---

### Pattern 3: Parallel Agent Coordination

**Hackathon Mode Example:**

```
Wave 1: Spec Generation (5 agents in parallel)
  Agent 1: Technical Architecture spec
  Agent 2: UX/Design spec
  Agent 3: Business Logic spec
  Agent 4: Testing Strategy spec
  Agent 5: Demo Impact spec
  ↓
  Synchronization Point: Review all specs
  ↓
Wave 2: Execution Plans (5 agents in parallel)
  Agent 1: Backend plan
  Agent 2: Frontend plan
  Agent 3: Database plan
  Agent 4: DevOps plan
  Agent 5: Quality plan
  ↓
  Synchronization Point: Merge plans
  ↓
Wave 3: Implementation (10 agents in parallel)
  Backend Team (5 agents):
    - Entity layer
    - Service layer
    - API layer
    - Security layer
    - Integration layer

  Frontend Team (5 agents):
    - Components
    - State management
    - Forms
    - UI polish
    - Integration
  ↓
  Synchronization Point: Integration testing
  ↓
Wave 4: Integration (2 agents)
  Agent 1: Full-stack integration
  Agent 2: Demo preparation
  ↓
Wave 5: Quality Assurance (3 agents)
  Agent 1: Backend QA
  Agent 2: Frontend QA
  Agent 3: Integration QA
```

**Result:** Production-ready code in ~40 minutes

---

### Pattern 4: Continuous Improvement Loop

```
┌─────────────────────────────────────────┐
│  Execution                              │
│  └─> Implement feature using PRP        │
└─────────────┬───────────────────────────┘
              │
              ▼
┌─────────────────────────────────────────┐
│  Analysis                               │
│  └─> Extract patterns from execution    │
└─────────────┬───────────────────────────┘
              │
              ▼
┌─────────────────────────────────────────┐
│  Learning                               │
│  └─> Update knowledge base              │
│      ├─ failure_patterns.yaml           │
│      └─ success_metrics.yaml            │
└─────────────┬───────────────────────────┘
              │
              ▼
┌─────────────────────────────────────────┐
│  Template Improvement                   │
│  └─> Enhance PRP templates              │
│      ├─ Add new gotchas                 │
│      ├─ Include successful patterns     │
│      └─ Refine validation strategies    │
└─────────────┬───────────────────────────┘
              │
              └──→ Next PRP is better ──→ Loop
```

---

### Pattern 5: Vertical Slice Architecture

Commands and PRPs emphasize **vertical slices** - complete features end-to-end:

```
Feature: User Authentication

Vertical Slice Includes:
├─ Database (User model, migrations)
├─ Service Layer (AuthService with business logic)
├─ API Layer (Auth endpoints)
├─ Frontend Components (Login, Signup forms)
├─ State Management (Auth context/hooks)
├─ Tests (Unit, integration, E2E)
└─ Documentation

Single PRP covers entire slice
Each task builds one layer
Validation ensures all layers integrate
```

**Benefit:** Features are shippable when PRP completes - no partial work

---

## Workflow Examples

### Example 1: Implementing a New Feature

**Scenario:** Add user authentication with JWT

```bash
Step 1: Create PRP with research
$ /prp-story-create "Add user authentication with JWT tokens and refresh logic"

Claude:
- Spawns 4 parallel research agents
  - Agent 1: Search codebase for existing auth patterns
  - Agent 2: Research FastAPI JWT libraries and best practices
  - Agent 3: Identify database integration points
  - Agent 4: Design validation strategy (pytest + integration tests)
- Reads artifacts for business context
- Generates: PRPs/story_user_authentication_jwt.md
  - 12 ordered tasks (models → service → API → tests)
  - Each task with PATTERN, GOTCHA, VALIDATE
  - 4-level validation loop at end

Step 2: Review PRP
- User opens PRPs/story_user_authentication_jwt.md
- Verifies context completeness
- Adjusts if needed (e.g., add specific security requirements)

Step 3: Execute PRP
$ /prp-story-execute "PRPs/story_user_authentication_jwt.md"

Claude:
- Task 1: CREATE models/user.py
  - Implement User model
  - VALIDATE: python -c "from models.user import User"
  - ✅ Pass → Next task

- Task 2: CREATE services/auth_service.py
  - Implement JWT generation/validation
  - VALIDATE: pytest services/tests/test_auth_service.py
  - ✅ Pass → Next task

- [... continues through all 12 tasks ...]

- Final Validation Loop:
  - Level 1: ruff check && mypy . (✅)
  - Level 2: pytest -v --cov (✅ 87% coverage)
  - Level 3: Integration tests (✅ All endpoints working)
  - Level 4: Security scan with safety (✅ No vulnerabilities)

Output: Working authentication system, all tests passing

Step 4: Analyze and learn
$ /prp-analyze-run "PRPs/story_user_authentication_jwt.md"

Claude:
- Collects metrics:
  - Time: 3.5 hours
  - Files: 8 created, 3 modified
  - Tests: 24 tests, 87% coverage
  - Commits: 12 (one per task)

- Analyzes context:
  - ✅ JWT library reference was helpful
  - ✅ FastAPI dependency injection pattern followed correctly
  - ⚠️  Token expiration gotcha prevented production issue

- Updates knowledge base:
  - Adds pattern: "JWT with FastAPI - use oauth2_scheme"
  - Adds success metric: JWT auth avg 3.5hrs, 87% success rate

Output: System improved for next authentication feature
```

---

### Example 2: Quick Code Quality Check

**Scenario:** Review changes before commit

```bash
$ /review-staged-unstaged

Claude:
1. Runs git status and git diff
2. Analyzes changes across 7 dimensions:
   - Code quality (SRP, DRY, naming)
   - Pydantic v2 compliance
   - Security (SQL injection, XSS, secrets)
   - Structure (vertical slice boundaries)
   - Linting (ruff, mypy)
   - Testing (coverage, test quality)
   - Performance (N+1 queries, caching)

Output: PRPs/code_reviews/review_2024-10-12.md

## Critical Issues (2)
- src/services/user_service.py:45
  SQL injection vulnerability: Using f-string for query
  FIX: Use SQLAlchemy parameterized queries

- src/api/routes.py:123
  Missing authentication on DELETE endpoint
  FIX: Add @requires_auth decorator

## Important Issues (3)
[... detailed list ...]

## Minor Issues (5)
[... detailed list ...]

Step 2: Fix issues
$ # Fix critical issues first
$ # Re-run review

Step 3: Commit when clean
$ /smart-commit

Claude:
- Analyzes changes
- Suggests: "feat: add JWT authentication with refresh tokens"
- User approves
- Commits and offers to push/create PR
```

---

### Example 3: Rapid Prototype Development

**Scenario:** Create proof-of-concept for stakeholder demo

```bash
$ /prp-poc-create-parallel "User dashboard with analytics widgets"

Claude:
- Creates POC PRP with:
  - Fidelity: Demo (polished for presentation)
  - Mock data: MSW with faker.js
  - Scope: 5 must-have widgets, 3 nice-to-have
  - Explicit won't-haves: Real APIs, comprehensive tests, error handling

Output: PRPs/poc_user_dashboard.md

$ /prp-poc-execute-parallel "PRPs/poc_user_dashboard.md"

Claude:
- Implements POC structure:
  - /poc-dashboard/components/ (5 widget components)
  - /poc-dashboard/data/mocks/ (faker.js data generators)
  - /poc-dashboard/pages/DashboardDemo.tsx (main demo page)

- Validation:
  - Level 1: TypeScript build succeeds (✅)
  - Level 2: Demo loads, widgets render (✅)
  - Level 3: Screenshots captured for stakeholder (✅)

Output: Working POC in 1.5 hours, ready for demo

Result: Stakeholder approves concept → convert to full PRP for production
```

---

### Example 4: Hackathon-Speed Development

**Scenario:** Build inventory management system in one afternoon

```bash
$ /hackathon-prp-parallel "Build complete inventory management system with products, stock tracking, and order management"

Claude:
[Wave 1: 5 agents generate specs - 8 minutes]
- Technical Architecture spec created
- UX flow spec created
- Business logic spec created
- Testing strategy spec created
- Demo plan spec created

[Wave 2: 5 agents create execution plans - 7 minutes]
- Backend implementation plan
- Frontend implementation plan
- Database schema plan
- DevOps/deployment plan
- Quality assurance plan

[Wave 3: 10 agents implement in parallel - 20 minutes]
Backend Team:
  ✅ Entity models (Product, Stock, Order)
  ✅ Service layer (CRUD + business logic)
  ✅ API endpoints (REST + validation)
  ✅ Authentication & authorization
  ✅ Database migrations

Frontend Team:
  ✅ Product management components
  ✅ Stock tracking UI
  ✅ Order management dashboard
  ✅ Forms with validation
  ✅ Integration with backend APIs

[Wave 4: 2 agents integrate - 3 minutes]
  ✅ Full-stack integration testing
  ✅ Demo scenario preparation

[Wave 5: 3 agents validate quality - 5 minutes]
  ✅ Backend tests: 24 tests, 82% coverage
  ✅ Frontend tests: 18 tests, all passing
  ✅ Integration tests: All user journeys work

Total Time: 43 minutes
Output: Production-ready inventory system

Success Metrics:
- 42 files created
- 2,847 lines of code
- 42 tests (82% coverage)
- Zero critical issues
- <100ms API response times
```

---

### Example 5: Comparative Architecture Analysis

**Scenario:** Uncertain about best approach for caching layer

```bash
$ /parallel-prp-creation "caching-layer" "Add Redis caching layer to improve API performance" 5

Claude spawns 5 agents in parallel:

Agent 1 (Performance Focus):
- Redis with aggressive TTLs
- Cache-aside pattern
- Preloading hot data
Output: PRPs/caching-layer-1-performance.md

Agent 2 (Security Focus):
- Encrypted cache entries
- Short TTLs for sensitive data
- Cache invalidation on auth events
Output: PRPs/caching-layer-2-security.md

Agent 3 (Maintainability Focus):
- Simple decorator-based caching
- Central cache config
- Extensive logging
Output: PRPs/caching-layer-3-maintainability.md

Agent 4 (Rapid Development Focus):
- Flask-Caching library (batteries-included)
- Minimal custom code
- Quick iteration
Output: PRPs/caching-layer-4-rapid.md

Agent 5 (Enterprise Focus):
- Redis cluster with replication
- Monitoring and alerts
- Disaster recovery
Output: PRPs/caching-layer-5-enterprise.md

Synthesis:
Output: PRPs/caching-layer-COMPARISON.md

## Comparison Matrix
| Approach      | Complexity | Performance | Security | Maintainability | Time to Implement |
|---------------|------------|-------------|----------|-----------------|-------------------|
| Performance   | High       | Excellent   | Good     | Moderate        | 6-8 hours         |
| Security      | High       | Good        | Excellent| Good            | 8-10 hours        |
| Maintainability| Low       | Good        | Good     | Excellent       | 4-6 hours         |
| Rapid         | Very Low   | Good        | Good     | Good            | 2-3 hours         |
| Enterprise    | Very High  | Excellent   | Excellent| Good            | 10-15 hours       |

## Recommendation
For current project stage (MVP with <1000 users):
→ Use Approach 4 (Rapid Development)
  - Fastest to implement (2-3 hours)
  - Sufficient performance for current scale
  - Can migrate to Approach 1 or 5 as needed

Decision: User selects Approach 4
$ /prp-story-execute "PRPs/caching-layer-4-rapid.md"
```

---

## Best Practices & Principles

### 1. Context Completeness - "No Prior Knowledge" Test

**Principle:**
> "If someone knew nothing about this codebase, would they have everything needed to implement this successfully?"

**Application:**
- ✅ Provide exact file paths with line numbers
- ✅ Include URLs with section anchors (#specific-section)
- ✅ Reference specific code patterns to follow
- ✅ Document known gotchas with solutions
- ❌ Avoid generic references ("check the docs")
- ❌ Avoid assumptions about codebase knowledge

**Example:**
```yaml
❌ Bad:
  - Use FastAPI for the endpoints
  - Follow our authentication pattern

✅ Good:
  - file: api/routes/product_routes.py
    why: Follow endpoint structure (lines 12-45)
    pattern: @router.post decorator with Pydantic request/response models

  - url: https://fastapi.tiangolo.com/tutorial/security/oauth2-jwt/
    why: JWT authentication implementation
    critical: Must use HTTPBearer, not OAuth2PasswordBearer (incompatible with our frontend)

  - gotcha: FastAPI dependency injection runs per-request
    solution: Use Depends() with callable, not instantiated objects
    bad: @router.get("/", dependencies=[AuthService()])
    good: @router.get("/", dependencies=[Depends(get_auth_service)])
```

---

### 2. Information-Dense Keywords

**Principle:** Use specific verbs that eliminate ambiguity

**Keywords and Semantics:**
- **IMPLEMENT** - What to build (core functionality)
- **PATTERN** - What existing code to follow (with file:lines)
- **FOLLOW** - Which file structure to mirror
- **NAMING** - Conventions to use
- **DEPENDENCIES** - What to import
- **PLACEMENT** - Where files go
- **VALIDATE** - How to verify completion (executable command)
- **GOTCHA** - What to avoid
- **CRITICAL** - Non-negotiable requirements

**Application:**
```yaml
Task 3: CREATE services/notification_service.py
  - IMPLEMENT: NotificationService with email and SMS support
  - PATTERN: Follow services/email_service.py structure (lines 15-89)
  - DEPENDENCIES: from providers.twilio import TwilioClient; from providers.sendgrid import SendGridClient
  - NAMING: Methods use send_{channel} pattern (send_email, send_sms)
  - PLACEMENT: services/ directory (not utils/ or helpers/)
  - GOTCHA: Twilio requires E.164 phone format (+country_code)
  - CRITICAL: Must handle rate limits (429 responses) with exponential backoff
  - VALIDATE: pytest services/tests/test_notification_service.py -v
```

---

### 3. Validation at Every Level

**Principle:** Shift-left quality controls - catch defects immediately

**4-Level Validation Loop:**

**Level 1: Syntax & Style** (Immediate feedback)
```bash
# Python
ruff check src/ --fix
mypy src/

# TypeScript
npm run lint
npx tsc --noEmit

# Requirement: Zero errors before proceeding
```

**Level 2: Unit Tests** (Component validation)
```bash
# Test each component independently
pytest src/services/tests/test_auth_service.py -v
npm test -- components/LoginForm.test.tsx

# Requirement: All tests pass, 80%+ coverage
```

**Level 3: Integration** (System validation)
```bash
# Start services
docker-compose up -d

# Test endpoints
curl -X POST http://localhost:8000/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "test@example.com", "password": "test123"}'

# Verify database writes
psql -d ar_automation -c "SELECT * FROM users WHERE email='test@example.com';"

# Requirement: Services integrate correctly
```

**Level 4: Creative & Domain-Specific** (Custom validation)
```bash
# MCP servers for specialized testing
playwright-mcp --test-user-journey="login-to-dashboard"
lighthouse-mcp --url http://localhost:3000 --min-score=90
security-mcp --scan=api --check=owasp-top-10

# Domain-specific tools
load-test-mcp --endpoint=/api/search --rps=100 --duration=30s

# Requirement: Domain-specific quality gates pass
```

---

### 4. Atomic, Independently Testable Tasks

**Principle:** Each task should stand alone

**Characteristics:**
- ✅ Single responsibility (one task, one change)
- ✅ Validation command included
- ✅ Dependencies explicitly ordered
- ✅ Failure hints provided (IF_FAIL)

**Example:**
```yaml
Task 4: UPDATE api/routes.py to register user router
  - FIND: # Product routes section
  - INSERT_AFTER: app.include_router(product_router)
  - CODE: app.include_router(user_router, prefix="/api/users", tags=["users"])
  - PRESERVE: Existing router registrations (don't modify)
  - VALIDATE: grep -q "user_router" api/routes.py && echo "✓ Router added"
  - IF_FAIL: Check import statement exists: from routes.user_routes import user_router

Task 5: CREATE api/routes/user_routes.py
  [Depends on Task 4 completion]
```

---

### 5. Progressive Elaboration

**Principle:** Start high-level, zoom into specifics

**Levels of Detail:**

**Level 1: Story/Feature**
```yaml
Feature: User Authentication
Goal: Allow users to create accounts and log in securely
Success: Users can register, login, and access protected resources
```

**Level 2: System**
```yaml
Affected Components:
- Database (User model, migrations)
- Backend (AuthService, JWT utilities, API endpoints)
- Frontend (Login/Signup forms, auth context)
- Tests (Unit, integration, E2E)
```

**Level 3: Component**
```yaml
File: services/auth_service.py
Responsibilities:
- Password hashing (bcrypt)
- JWT generation and validation
- User registration logic
- Login verification
Pattern: Follow services/email_service.py structure
```

**Level 4: Code**
```yaml
Method: create_access_token(user_id: str) -> str
- IMPORT: from jose import jwt; from datetime import datetime, timedelta
- LOGIC: Encode user_id + expiration (24hrs) into JWT
- SECRET: Use settings.JWT_SECRET_KEY (from environment)
- ALGORITHM: HS256
- GOTCHA: Must include 'exp' claim for expiration check
- RETURN: Encoded JWT string
```

**Level 5: Validation**
```yaml
VALIDATE: pytest services/tests/test_auth_service.py::test_create_access_token -v
EXPECTED: Test passes, token decodes correctly, expiration set to 24hrs
IF_FAIL: Check JWT_SECRET_KEY in .env, verify jose library installed
```

---

### 6. Parallel Agent Coordination

**Principle:** Leverage concurrency for research and implementation

**When to Use Parallel Agents:**
- Research phase (codebase search + library docs + integration points)
- POC variations (multiple approaches simultaneously)
- Implementation (independent components in parallel)
- Validation (backend + frontend + integration QA)

**Synchronization Pattern:**
```
Phase 1: Spawn N agents with clear, isolated tasks
  ↓
Wait for ALL agents to complete
  ↓
Phase 2: Synthesize results (single coordinator agent)
  ↓
Phase 3: Spawn M agents for next wave
  ↓
Repeat...
```

**Agent Context Requirements:**
> "Subagents will only receive their details from you, the user has no way of interacting with the subagents. You need to share all relevant context in the subagent prompt."

**Example:**
```
/prp-story-create spawns 4 agents:

Agent 1 Task:
"Search the codebase for existing authentication patterns.
Specifically:
- Look in src/services/ for *_service.py files
- Look in api/routes/ for authentication decorators
- Search for 'jwt', 'token', 'auth' in file contents
Return:
- File paths with line numbers
- Pattern descriptions
- Any gotchas or security considerations noted in comments"

[Agent 1 cannot ask user for clarification - context must be complete]
```

---

### 7. Continuous Improvement Loop

**Principle:** System learns from every execution

**Learning Cycle:**
```
Execution → Analysis → Learning → Improvement → Better Execution
```

**What to Capture:**

**Success Patterns:**
```yaml
# success_metrics.yaml
- feature_type: "api_integration"
  implementations: 47
  avg_time: 35 minutes
  avg_coverage: 87%
  success_rate: 92%
  common_patterns:
    - "FastAPI with Pydantic v2 models"
    - "Pytest with fixture-based setup"
    - "Mock external APIs with responses library"
```

**Failure Patterns:**
```yaml
# failure_patterns.yaml
- id: "async_context_mixing"
  frequency: "high"
  description: "Mixing async/sync code causes runtime errors"
  prevention: "Use async/await consistently, never mix with sync DB calls"
  last_seen: "2024-10-11T23:43:00Z"
  examples:
    - file: "services/user_service.py:67"
      issue: "Called sync session.query() inside async function"
      fix: "Use async session.execute(select())"
```

**Template Updates:**
```yaml
# Template improvement log
- date: "2024-10-12"
  template: "prp_base.md"
  change: "Added gotcha about FastAPI dependency injection lifecycle"
  reason: "5 PRPs had this issue in past month"
  impact: "Prevents scoped dependency errors"
```

---

### 8. Specificity Over Generality

**Principle:** Concrete references beat abstract ones

**Examples:**

❌ **Too Generic:**
```yaml
- Check the FastAPI documentation for authentication
- Follow best practices for database queries
- Use the standard React patterns
```

✅ **Appropriately Specific:**
```yaml
- url: https://fastapi.tiangolo.com/tutorial/security/oauth2-jwt/#oauth2-with-password-and-bearer
  why: JWT authentication implementation
  section: Read "OAuth2 with Password (and hashing), Bearer with JWT tokens"
  critical: Use HTTPBearer, not OAuth2PasswordBearer (simpler for frontend)

- file: services/database_service.py:45-67
  why: Async session management pattern
  pattern: |
    async with get_session() as session:
        result = await session.execute(query)
        await session.commit()
  gotcha: Always use 'await session.commit()' - auto-commit doesn't work with async

- file: frontend/src/components/forms/LoginForm.tsx:12-45
  why: Form validation pattern with react-hook-form
  pattern: useForm() hook with zod schema validation
  reuse: Import zodResolver and loginSchema from lib/validations
```

---

### 9. Separation of Planning and Execution

**Principle:** Create/Execute pairs prevent "coding while thinking"

**Why It Matters:**
- Planning phase: Explore options, research patterns, design architecture
- Execution phase: Implement with confidence, knowing path is clear
- Mixing: Leads to refactoring cycles, wasted work, scope creep

**Pattern:**
```
Step 1: Create Phase (/prp-story-create)
  - Deep research (parallel agents)
  - Context gathering
  - Pattern identification
  - Validation strategy design
  - Output: Comprehensive PRP

  [User reviews and approves]

Step 2: Execute Phase (/prp-story-execute)
  - Follow PRP tasks sequentially
  - Validate each task
  - No planning/research during execution
  - Output: Working, tested code
```

**Anti-Pattern:**
```
❌ Don't do this:
  Start coding → Realize context missing → Research mid-execution →
  Refactor already-written code → Realize design issue →
  Rewrite → Finally complete (3x the time)

✅ Do this:
  Research thoroughly → Plan completely → Execute confidently →
  Complete (optimal time)
```

---

### 10. User as Orchestrator, AI as Implementer

**Principle:** Clear role separation

**User Role:**
- Define what to build (high-level requirements)
- Review and approve PRPs (validate context)
- Make architectural decisions (when alternatives exist)
- Provide domain knowledge (business rules, edge cases)

**AI Role:**
- Research patterns and context
- Generate comprehensive specifications
- Implement features following specifications
- Validate work at all levels
- Learn from outcomes

**Example Interaction:**
```
User: "Add user authentication with JWT"

AI: [Researches, creates PRP]
    "I've created a PRP with 12 tasks implementing JWT authentication.
    Please review PRPs/story_user_auth.md and let me know if:
    1. Context is complete
    2. Token expiration (24hrs) is appropriate
    3. Should I include refresh tokens? (Not in current PRP)
    4. Any specific security requirements?"

User: "Add refresh tokens, 7-day expiration. Otherwise looks good."

AI: [Updates PRP]
    "Updated. Ready to execute?"

User: "/prp-story-execute PRPs/story_user_auth.md"

AI: [Implements sequentially with validation]
    [3 hours later]
    "Complete! 12/12 tasks implemented, all tests passing (87% coverage).
    Files created: 8, Files modified: 3.
    Commits: 12 (one per task).
    Ready for code review."
```

---

## CLAUDE.md Role

### What is CLAUDE.md?

**CLAUDE.md** is a **project-specific instruction file** that provides:
- Technical stack and architecture
- Development philosophy and principles
- Coding guidelines and conventions
- Project-specific patterns and gotchas
- Setup and deployment instructions

### Relationship to Context Engineering System

```
┌───────────────────────────────────────────────────────┐
│  CLAUDE.md                                            │
│  Project-Specific Instructions                        │
│  ├─ "This project uses React + Vite (NOT Next.js)"   │
│  ├─ "Keep components under 200 lines"                │
│  ├─ "Use TanStack Query for state (NOT Redux)"       │
│  └─ "Docker Compose for dev environment"             │
└─────────────┬─────────────────────────────────────────┘
              │
              │ provides context to
              ▼
┌───────────────────────────────────────────────────────┐
│  Context Engineering System (.claude/)                │
│  Process & Workflow Instructions                      │
│  ├─ PRPs/        (Implementation specs)               │
│  ├─ commands/    (Workflow orchestration)             │
│  └─ artifacts/   (Strategic knowledge)                │
└───────────────────────────────────────────────────────┘
```

### Key Distinction

| **CLAUDE.md** | **Context Engineering System** |
|---------------|--------------------------------|
| **Project-specific** "how we build THIS project" | **Process-generic** "how to build ANY project" |
| Tech stack, conventions, gotchas | PRPs, commands, workflows |
| Changes per project | Reusable across projects |
| Read by AI at start of session | Orchestrates ongoing work |

### CLAUDE.md Structure (AR Automation Example)

```markdown
# CLAUDE.md - Project Name

## Project Overview
- What this project is
- Key features
- Architecture diagram

## Core Development Philosophy
- KISS, YAGNI, Progressive Enhancement
- Project values and priorities

## Tech Stack
- Frontend: React 18, Vite, TypeScript, Tailwind
- Backend: FastAPI, LangGraph, OpenAI
- Infrastructure: Docker, PostgreSQL

## Getting Started
- Prerequisites
- Setup instructions
- Access URLs

## Development Guidelines
- Component patterns
- TypeScript conventions
- Styling guidelines
- Data fetching patterns

## What to Avoid
- ❌ Don't use Redux (use TanStack Query)
- ❌ Don't create >200 line files
- ❌ Don't use `any` type

## Testing & Quality
- Pre-commit checklist
- Validation commands

## Deployment
- Options and instructions
```

### How to Use CLAUDE.md

**At Session Start:**
```bash
/prime-core
```
This command:
1. Reads CLAUDE.md
2. Reads README.md
3. Scans key source files
4. Explains back understanding for validation

**During Development:**
- PRPs reference CLAUDE.md for project-specific patterns
- Commands use CLAUDE.md to maintain consistency
- AI agents consult CLAUDE.md for conventions

**Example Reference in PRP:**
```yaml
Context:
  - file: /CLAUDE.md
    section: "TypeScript Guidelines"
    why: Project-specific type safety requirements
    critical: No `any` types allowed, components under 200 lines
```

### Process Patterns from CLAUDE.md

While CLAUDE.md is project-specific, it reveals **process patterns** that can be extracted:

**Pattern 1: Simplicity First**
```
Philosophy → KISS, YAGNI, Progressive Enhancement
Process: Start simple, add complexity only when needed
Application: PRPs should prefer simple solutions
```

**Pattern 2: Component Organization**
```
Structure → Marketing (static) vs Interactive (backend-connected)
Process: Organize by function/interaction level
Application: PRPs group related components
```

**Pattern 3: Validation Gates**
```
Quality → Pre-commit checklist, testing checklist
Process: Quality gates at multiple levels
Application: 4-level validation loop in PRPs
```

**Pattern 4: Technology Constraints**
```
Stack → Specific libraries and versions
Process: Document technology decisions and rationale
Application: PRPs include exact library versions and gotchas
```

---

## Getting Started Guide

### For New Users of This System

#### Step 1: Understand the Components

**Read this document** (Context-Engineering-Cognitive-Map.md) to understand:
- The three pillars (PRPs, Commands, Artifacts)
- How they interact
- The philosophy behind the system

**Explore the structure:**
```bash
ls -R .claude/

.claude/PRPs/           # Implementation specifications
.claude/commands/       # Slash command orchestration
.claude/artifacts/      # Strategic knowledge
.claude/settings.local.json  # Configuration
```

---

#### Step 2: Prime Claude with Project Context

**Start every session with:**
```bash
/prime-core
```

This ensures Claude has full context about:
- Project architecture (from CLAUDE.md)
- Codebase structure (from README.md)
- Key patterns (from source files)

---

#### Step 3: Start with Simple Commands

**Try basic workflows first:**

**Review existing code:**
```bash
/review-staged-unstaged
```

**Create smart commits:**
```bash
/smart-commit
```

**Generate onboarding docs:**
```bash
/onboarding
```

---

#### Step 4: Create Your First PRP

**For a small feature:**
```bash
/prp-task-create "Add email validation to signup form"
```

**For a complete user story:**
```bash
/prp-story-create "Implement password reset flow with email verification"
```

**Review the generated PRP:**
- Open `PRPs/story_password_reset.md`
- Check context completeness
- Verify validation commands are executable
- Adjust if needed

---

#### Step 5: Execute the PRP

```bash
/prp-story-execute "PRPs/story_password_reset.md"
```

Watch as Claude:
- Implements each task sequentially
- Validates after each step
- Reports progress
- Completes with all tests passing

---

#### Step 6: Learn from Execution

```bash
/prp-analyze-run "PRPs/story_password_reset.md"
```

Review the analysis:
- What worked well?
- What context was missing?
- What patterns emerged?
- How can templates improve?

---

### For Advanced Users

#### Experiment with Parallel Workflows

**Generate multiple PRP variations:**
```bash
/parallel-prp-creation "caching-layer" "Add Redis caching for API performance" 3
```

Compare approaches and select optimal strategy.

---

#### Try Hackathon Mode

**Build complete features in <1 hour:**
```bash
/hackathon-prp-parallel "Build inventory management system with products, stock, and orders"
```

Watch 25 agents work simultaneously across specs, plans, implementation, integration, and QA.

---

#### Customize the System

**Create your own slash commands:**
1. Add new file to `.claude/commands/{category}/{command-name}.md`
2. Follow existing command structure
3. Test with `/command-name`

**Create custom PRP templates:**
1. Add to `.claude/PRPs/templates/prp_custom.md`
2. Follow existing template patterns
3. Reference in custom commands

---

### Best Practices for Daily Use

**1. Always start with `/prime-core`**
- Ensures Claude has full project context
- Validates understanding

**2. Use create/execute pattern**
- Create PRP first (plan thoroughly)
- Review PRP (validate context)
- Execute PRP (implement confidently)

**3. Validate continuously**
- Don't skip validation steps
- Fix issues immediately
- Don't proceed with failures

**4. Learn from outcomes**
- Run `/prp-analyze-run` after major features
- Update knowledge base with learnings
- Improve templates based on experience

**5. Keep PRPs modular**
- One PRP per feature/story
- Atomic tasks within PRPs
- Clear dependencies

---

### Common Pitfalls to Avoid

**❌ Skipping the research phase**
- Problem: Incomplete context leads to rework
- Solution: Let agents research thoroughly during create phase

**❌ Mixing planning and execution**
- Problem: Coding while thinking leads to refactoring
- Solution: Complete PRP before starting execution

**❌ Vague validation commands**
- Problem: "Make sure it works" isn't executable
- Solution: Provide exact commands (pytest, curl, grep)

**❌ Ignoring validation failures**
- Problem: Issues compound, leading to cascade failures
- Solution: Fix immediately, don't proceed with red tests

**❌ Not learning from failures**
- Problem: Same mistakes repeated
- Solution: Run analyze-run, update knowledge base

---

## Summary

### What This System Provides

**1. Structured Methodology**
- Clear process from idea → PRP → implementation → validation
- Separation of planning and execution
- Continuous improvement loop

**2. Context Completeness**
- "No Prior Knowledge" test ensures specifications are self-contained
- Exact file paths, line numbers, URLs with anchors
- Known gotchas documented

**3. Quality Assurance**
- 4-level validation loop
- Validation at every task
- Shift-left defect detection

**4. Scalability**
- From micro-tasks to multi-week features
- Sequential or massively parallel
- 7 PRP templates for different scenarios

**5. AI-Optimized**
- Information-dense keywords
- Executable validation commands
- Parallel agent coordination
- Self-improving templates

---

### Key Success Factors

**This system works when:**
✅ Context is complete (passes "No Prior Knowledge" test)
✅ Validation is continuous (every task, every level)
✅ Planning is separated from execution
✅ Learning feeds back into templates
✅ Users orchestrate, AI implements

**This system struggles when:**
❌ Context is incomplete or vague
❌ Validation is skipped or aspirational
❌ Planning and execution are mixed
❌ Failures aren't analyzed for learnings
❌ AI is expected to guess requirements

---

### The Big Picture

This context engineering system transforms AI-assisted development from:

**Before:**
- Ad-hoc prompts
- 80% completion rates
- Frequent refactoring
- Inconsistent patterns
- No learning loop

**After:**
- Systematic workflows
- 85-95% completion rates
- First-pass quality
- Consistent patterns
- Continuous improvement

**The key insight:**
> "It's not about making AI smarter. It's about giving AI complete context, clear instructions, and executable validation. The system handles the rest."

---

### Next Steps

**For This Project:**
1. Use this cognitive map as reference
2. Create PRPs using established templates
3. Execute with confidence
4. Analyze outcomes
5. Improve templates

**For Other Projects:**
1. Copy `.claude/` structure
2. Adapt PRP templates to tech stack
3. Create project-specific CLAUDE.md
4. Customize commands as needed
5. Build knowledge base from execution

---

### Feedback & Evolution

This system is **living and evolving**. As you use it:

**Capture learnings:**
- What worked well?
- What context was missing?
- What patterns emerged?
- How can we improve?

**Update documentation:**
- Enhance this cognitive map
- Improve PRP templates
- Refine commands
- Share knowledge

**Contribute back:**
- Document new patterns
- Create better examples
- Simplify complex workflows
- Help others succeed

---

**Remember:** This system is a tool to **amplify human capability**, not replace it. You bring domain knowledge, creative vision, and strategic thinking. The system brings structure, consistency, and scalability. Together, you build great software.

---

*Last Updated: October 2025*
*Version: 1.0*
*Repository: ar3_website*