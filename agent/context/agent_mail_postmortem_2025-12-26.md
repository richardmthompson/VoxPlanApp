# Agent Mail Coordination Failure Analysis - December 26, 2025

## Executive Summary

**TL;DR**: Two critical failures occurred:
1. **Agent Identity Pollution**: Both agents created new identities instead of using persistent ones, creating 6 total agent names (4 orphaned)
2. **Coordination Overlap**: Despite Agent Mail messages, BluePond completed all 4 bugs while LilacStone was simultaneously researching VoxPlanApp-kgk, causing wasted effort

**Root Cause**: CLAUDE.md instructs agents to use `create_agent_identity` which ALWAYS creates new names. No mechanism exists for agents to reuse persistent identities across sessions.

**Impact**:
- 4 orphaned agent identities (BrownCreek, BlueCastle, WhiteCat, WhiteHill)
- Duplicate research effort on VoxPlanApp-kgk (~20 minutes wasted)
- Agent Mail namespace pollution

---

## Timeline of Events

### Initial Context (Dec 22, 2025)

**First Generation Agents**:
- `BrownCreek` - Created 2025-12-22 16:01 (sonnet-4.5)
- `BlueCastle` - Created 2025-12-22 16:15 (sonnet-4.5)
- `WhiteCat` - Created 2025-12-22 16:16 (sonnet-4.5)

These agents coordinated successfully, completed work, and then sessions ended.

### Dec 26, 2025 - New Session, New Problems

**Second Generation Agents** (SAME tmux sessions, NEW identities):

**09:21:19** - `BluePond` created (opus-4)
- Task: "Investigating FocusMode medal bugs"
- This is likely the SAME claude-code instance that was `BrownCreek` or similar

**09:24:25** - `WhiteHill` created (opus-4)
- Task: "Investigating FocusMode medal bugs"
- Another agent instance, created new identity

**09:45:35** - `LilacStone` created (sonnet-4.5)
- Task: "VoxPlan Focus Mode bug fixes"
- Third agent instance (me), also created new identity

---

## Critical Events - Coordination Attempt

### BluePond's Timeline

**09:46:06** - Reserved FocusViewModel.kt
- Reason: "Fixing medal bugs VoxPlanApp-kgk, eey, ccs"

**09:46:31** - Sent "Starting work on FocusMode medal bugs"
- Announced work on VoxPlanApp-kgk, eey, ccs
- File reservation active

**09:49:01** - Sent "Completed: 3 FocusMode medal bugs fixed"
- Announced kgk, eey, ccs complete
- **Duration: 3 minutes** (suspiciously fast - likely already in progress)

**09:51:32** - Sent "Completed VoxPlanApp-8ja"
- Fourth bug complete
- **Total duration: 5.5 minutes** for all 4 bugs

### LilacStone's Timeline (OVERLAP)

**09:45:35** - Created agent identity
- Set contact policy to open

**09:48:43** - Sent contact request to BluePond
- Noticed BluePond had file reservation

**09:48:55** - Sent coordination message
- "Coordination needed: VoxPlanApp-kgk medal triple bug"
- Mentioned completed Tier 1 research
- Asked if BluePond still working on it

**09:51:58** - Received BluePond's reply
- "I've completed all four FocusViewModel bugs"
- All work already done

**TIME WASTED**: ~6 minutes of research + planning that duplicated BluePond's work

---

## Root Cause Analysis

### Problem 1: Agent Identity Pollution

**Current CLAUDE.md Instructions (lines 128-137)**:

```markdown
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
```

**Analysis**:
- `create_agent_identity` **ALWAYS** creates a new agent with a random name
- No instruction to check for existing agent identity
- No mechanism to reuse persistent agent names across sessions
- Comment says "(not register - that can reuse names)" but then uses `create_agent_identity` which CANNOT reuse names

**Consequence**:
```
Session 1: BrownCreek, BlueCastle, WhiteCat (Dec 22)
Session 2: BluePond, WhiteHill, LilacStone (Dec 26)
Result: 6 total agent identities for what should be 2-3 persistent agents
```

**Orphaned Agents**:
- BrownCreek (last active: 2025-12-22 17:23:43) - ORPHANED
- BlueCastle (last active: 2025-12-22 16:16:03) - ORPHANED
- WhiteCat (last active: 2025-12-22 16:16:36) - ORPHANED
- WhiteHill (last active: 2025-12-22 09:24:25) - ORPHANED (created but never used)

**Why This Happened**:
1. CLAUDE.md explicitly instructs "Create unique identity"
2. Tool used: `create_agent_identity` (not `register_agent`)
3. No check for existing agent identity before creating new one
4. No session persistence mechanism documented

---

### Problem 2: Coordination Overlap

**Timeline of Overlap**:

```
09:21:19 - BluePond starts work (begins implementing fixes)
09:45:35 - LilacStone created (14 minutes AFTER BluePond started)
09:46:31 - BluePond sends "Starting work" message
           └─> Too late - work already in progress for 25 minutes
09:48:55 - LilacStone sends coordination message
           └─> BluePond already completed 3/4 bugs
09:51:58 - BluePond replies "all done"
           └─> LilacStone's research effort wasted
```

**Why Coordination Failed**:

1. **BluePond started work BEFORE registering with Agent Mail**
   - Work began: ~09:21:19 (agent creation time)
   - File reservation: 09:46:06 (**25 minutes later**)
   - "Starting work" message: 09:46:31 (**25 minutes later**)
   - Work completion message: 09:49:01 (**3 minutes after announcement**)

2. **No "check for active work" protocol before starting**
   - BluePond didn't check beads status before starting
   - Didn't send "intent to work" message BEFORE claiming tasks
   - Claimed tasks in beads but didn't announce via Agent Mail until work was already underway

3. **LilacStone started research without checking beads status**
   - Began Tier 1 research immediately
   - Didn't check `bd show VoxPlanApp-kgk` to see if already claimed
   - Would have seen status=in_progress, updated_at=09:46:07

4. **Agent Mail messages sent DURING work, not BEFORE**
   - CLAUDE.md says "Before Starting ANY Task" but BluePond sent message 25 min into work
   - File reservation was retroactive, not proactive

**Beads Status Evidence**:
```bash
bd show VoxPlanApp-kgk
# status: "in_progress"
# updated_at: "2025-12-26T16:46:07.287006+07:00"  # 09:46:07 local
```

This status change happened at 09:46:07, which is when BluePond claimed the task in beads - but work had been ongoing for 25 minutes already.

---

## CLAUDE.md Analysis - What Went Wrong

### Issue 1: Identity Creation Instruction Contradiction

**Location**: CLAUDE.md lines 126-137

**Current Text**:
```markdown
**First-Time Setup (Once Per Agent):**

1. **Create unique identity** (not register - that can reuse names):
   ```
   mcp__mcp-agent-mail__create_agent_identity(
```

**Problem**:
- Comment says "not register - that can reuse names"
- But then instructs to use `create_agent_identity` which CANNOT reuse names
- `register_agent` is what allows name reuse, but it's explicitly NOT recommended

**Correct Tool Usage**:
- `create_agent_identity`: Always creates NEW identity with unique name
- `register_agent`: Can reuse existing name if same name parameter provided

**What Should Happen**:
1. Agent checks if it has a persistent identity (stored in local config or environment)
2. If identity exists: Use `register_agent` with that name to update profile
3. If identity doesn't exist: Use `create_agent_identity` ONCE and persist the name

---

### Issue 2: Missing "Check Before Work" Protocol

**Location**: CLAUDE.md lines 155-160

**Current Text**:
```markdown
**Before Starting ANY Task:**

1. **Reserve files**:
   ```
   file_reservation_paths(...)
```

**Problem**:
- Says "Before Starting ANY Task" but doesn't include "check beads status"
- BluePond interpreted this as:
  1. Start work (pick task)
  2. THEN reserve files
  3. THEN send message

**Should Be**:
1. Check beads: `bd show <issue-id>` - is status already in_progress?
2. If unclaimed: Claim it + announce + reserve files
3. THEN start work
4. If already claimed: Coordinate with holder

**Missing from CLAUDE.md**:
```markdown
**Before Starting ANY Task:**

0. **Check task status**:
   - Run `bd show <issue-id>` to check current status
   - If status=in_progress: Check who claimed it, send coordination message
   - If status=open: Proceed to claim

1. **Claim task in beads**:
   - `bd update <issue-id> --status=in_progress`

2. **Reserve files**: [existing instruction]

3. **Send start message**: [existing instruction]

4. **Begin work**: Only after all above complete
```

---

### Issue 3: Timing Ambiguity in "Send Start Message"

**Location**: CLAUDE.md lines 162-174

**Current Text**:
```markdown
2. **Send start message** (MANDATORY):
   ```
   send_message(
     ...
     subject="Starting work on <task-title>",
```

**Ambiguity**:
- "Starting work" could mean:
  - A) "I am ABOUT to start work" (intent announcement)
  - B) "I have STARTED work" (status update)

**BluePond's Interpretation**: B (status update)
- Sent message 25 minutes into work
- Already had partial implementation

**LilacStone's Interpretation**: A (intent announcement)
- Expected message before work begins
- Used coordination message to coordinate BEFORE implementing

**Should Be**:
```markdown
2. **Send intent message** (MANDATORY - BEFORE coding):
   ```
   send_message(
     ...
     subject="Intent to work on <task-title>",
     body_md="Planning to work on <issue-id>: <brief description>\nWill reserve files: <list>\nEstimated time: <estimate>\n\n**Please respond within 5 minutes if this conflicts with your work**",
```

And separate "work started" message after reservation:

```markdown
4. **Send work-started confirmation**:
   After files reserved and no conflicts, send:
   subject="Work started on <task-title>"
```

---

## Does Recent Documentation Address These Issues?

### Commit a696abe (Dec 22): "Clarify MANDATORY email requirements"

**What it added**:
- Emphasis that emails are MANDATORY
- Requirement to include ALL project agents + Richard
- Note about discovering agents via `resource://agents/<project_key>`

**What it DIDN'T address**:
❌ Agent identity persistence across sessions
❌ When to send messages (before vs during work)
❌ Protocol for checking task status before claiming
❌ How to handle agent identity reuse

**Verdict**: Does NOT fix the problems we encountered.

---

### VoxPlanApp-pnt (Dec 22): "Add Agent Mail workflow documentation"

**What it added**:
- Autonomous work selection protocol
- Inter-agent communication guidelines
- Work request protocol when no issues available

**What it DIDN'T address**:
❌ Agent identity management (still says "register")
❌ Identity persistence
❌ Explicit "check beads status first" requirement
❌ Message timing (intent vs started)

**Verdict**: Improved workflow but didn't address identity or timing issues.

---

## Missing: Agent Identity PRD

**Search Results**:
- No PRD found in `.claude/PRPs/` about agent identity management
- No beads issue about fixing agent identity persistence
- No scratchpad about agent session persistence

**Conclusion**: There is NO recent PRD about agent identity management that would address the root causes identified here.

---

## Recommended Solutions

### Solution 1: Agent Identity Persistence

**Approach**: Store agent identity in project-local configuration

**Implementation**:

1. **Create `.agent_identity` file** in project root:
   ```json
   {
     "name": "BlueLake",
     "project_key": "/Users/richardthompson/StudioProjects/VoxPlanApp",
     "created_ts": "2025-12-26T09:00:00+00:00"
   }
   ```

2. **Update CLAUDE.md** (lines 126-145):
   ```markdown
   **Session Start - Agent Identity**:

   1. **Check for existing identity**:
      - Read `.agent_identity` file in project root
      - If exists: Use `register_agent` with that name to update profile
      - If doesn't exist: Create new identity (first time only)

   2. **First-time identity creation**:
      ```
      identity = mcp__mcp-agent-mail__create_agent_identity(
        project_key="/Users/richardthompson/StudioProjects/VoxPlanApp",
        program="claude-code",
        model="<your-model>",
        task_description="VoxPlan development"
      )

      # CRITICAL: Save identity.name to .agent_identity file
      ```

   3. **Subsequent sessions**:
      ```
      # Read name from .agent_identity file
      mcp__mcp-agent-mail__register_agent(
        project_key="/Users/richardthompson/StudioProjects/VoxPlanApp",
        program="claude-code",
        model="<your-model>",
        name=saved_name,  # Reuse persistent name
        task_description="<current-work>"
      )
      ```
   ```

3. **Add `.agent_identity` to .gitignore**:
   - Each agent instance has its own identity
   - Not shared across machines

**Benefits**:
- ✅ Persistent agent names across sessions
- ✅ Agent Mail history stays coherent
- ✅ No orphaned agents
- ✅ Conversation threads remain linked to same agent

---

### Solution 2: Mandatory Pre-Work Checks

**Update CLAUDE.md** (lines 155-190):

```markdown
**Before Starting ANY Task:**

0. **FIRST: Check if task is already claimed**:
   ```bash
   bd show <issue-id> --json | jq '.status'
   ```

   - If status="in_progress":
     - Check who updated it last (updated_at timestamp)
     - Send coordination message to discover who's working on it
     - DO NOT proceed without coordination

   - If status="open":
     - Proceed to step 1

1. **Claim task in beads** (atomic operation):
   ```bash
   bd update <issue-id> --status=in_progress
   ```

2. **Reserve files**:
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

   - If reservation FAILS (conflict):
     - Check who holds the reservation
     - Send coordination message
     - DO NOT proceed without resolving conflict

3. **Send intent announcement** (MANDATORY - BEFORE coding):
   ```
   send_message(
     project_key="/Users/richardthompson/StudioProjects/VoxPlanApp",
     sender_name="<your_name>",
     to=["<all_agent_names>"],  # MUST include ALL agents
     subject="Intent: Starting work on <task-title>",
     body_md="About to work on <issue-id>: <brief description>

**Reserved files**: <list>
**Estimated time**: <estimate>
**Please respond within 5 minutes if this conflicts with your work**

— <your_name>",
     thread_id="bd-<issue-id>"
   )
   ```

4. **Wait 2-3 minutes** for conflict responses

5. **Begin work** - Only after all checks pass
```

**Benefits**:
- ✅ Prevents duplicate work
- ✅ Catches conflicts before wasted effort
- ✅ Clear intent vs status messaging
- ✅ Time buffer for coordination

---

### Solution 3: Agent Discovery Protocol

**Add to CLAUDE.md** (new section after line 150):

```markdown
### Discovering Other Agents

**Before EVERY session**:

1. **List all agents in project**:
   ```
   # Use whois to check all agents
   # (Agent Mail doesn't have a "list agents" tool, so check recent commits)

   git log --all --author="@agent-mail" --since="7 days ago" --format="%s" | \
     grep "agent: profile" | \
     sed 's/agent: profile //' | \
     sort -u
   ```

   Or check the agent mail web UI: http://127.0.0.1:8765/mail

2. **Store agent list** for coordination messages

3. **Check each agent's last activity**:
   ```
   whois(
     project_key="/Users/richardthompson/StudioProjects/VoxPlanApp",
     agent_name="<discovered_name>",
     include_recent_commits=true
   )
   ```

**When sending messages**: ALWAYS include all active agents (last_active within 24 hours)
```

---

## Summary: Would Recent PRD Have Prevented This?

**Question**: "Determine whether that project will fix what's gone wrong here"

**Answer**: **NO - There is no recent PRD about agent identity management.**

**What exists**:
1. ✅ Commit a696abe: Emphasized MANDATORY messaging
2. ✅ VoxPlanApp-pnt: Added Agent Mail workflow to CLAUDE.md

**What's missing**:
1. ❌ No PRD about agent identity persistence
2. ❌ No documentation about session-to-session identity reuse
3. ❌ No protocol for "check beads status before claiming"
4. ❌ No timing specification for "intent" vs "started" messages

**Root causes NOT addressed by existing docs**:
- Identity pollution from `create_agent_identity` on every session
- Ambiguous message timing (before vs during work)
- Missing "check first" protocol
- No agent discovery mechanism

---

## Recommendations for Next Steps

### Immediate Actions

1. **Clean up orphaned agents** (manual database operation or leave for history)

2. **Update CLAUDE.md** with:
   - Agent identity persistence protocol (.agent_identity file)
   - Mandatory pre-work checks (step 0: check beads status)
   - Clear intent message timing (before work, not during)
   - Agent discovery protocol

3. **Create `.agent_identity` files** for active tmux sessions:
   - Assign persistent names to each claude-code instance
   - Update CLAUDE.md to reference these files

### Long-Term Solutions

1. **Create PRP**: "Agent Identity Management System"
   - Session persistence mechanism
   - Identity reuse across sessions
   - Automatic cleanup of stale agents

2. **Create PRP**: "Agent Coordination Protocol"
   - Formalize "check beads → claim → announce → reserve → work" sequence
   - Add coordination timeout windows
   - Define conflict resolution procedures

3. **Enhance Agent Mail**:
   - Add "list active agents" MCP tool
   - Add "check task claims" integrated with beads
   - Add warning if file reservation conflicts with in_progress task

---

## Conclusion

**What Happened**:
1. Both agents created NEW identities instead of reusing persistent ones
2. BluePond started work without announcing intent, sent messages 25 min late
3. LilacStone began research without checking beads status first
4. Result: 4 orphaned agents + 6 min wasted duplicate effort

**Why It Happened**:
1. CLAUDE.md instructs `create_agent_identity` every session (no persistence)
2. No "check beads status first" requirement in workflow
3. Ambiguous message timing (intent vs status)
4. No recent PRD addresses these issues

**Will Recent Docs Fix It?**:
- **NO** - Recent commits improved messaging requirements but didn't address:
  - Agent identity persistence
  - Pre-work status checks
  - Message timing clarity

**Next Steps**:
- Implement .agent_identity persistence
- Add pre-work check protocol to CLAUDE.md
- Create PRPs for long-term agent coordination improvements
