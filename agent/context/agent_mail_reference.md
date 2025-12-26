# Agent Mail Quick Reference

## Web UI Access

**URL:** http://127.0.0.1:8765/mail

**Server Status Check:**
```bash
ps aux | grep "mcp_agent_mail" | grep -v grep
```

**Port Check:**
```bash
lsof -i :8765
```

## Web UI Features

- **Unified inbox** - All messages across projects in reverse chronological order
- **Full-text search** - FTS5 search with subject/body filtering
- **Project overview** - View agents, file reservations, and attachments
- **Agent inboxes** - Individual agent message views with pagination
- **Message details** - Markdown rendering and attachment viewing
- **File reservations** - Track active and historical file leases

## VoxPlan Project Configuration

**Project Key (MANDATORY):** `/Users/richardthompson/StudioProjects/VoxPlanApp`

Always use this exact absolute path for all Agent Mail operations in VoxPlan.

## Common MCP Tool Operations

### Check Agent Status
```
mcp__mcp-agent-mail__whois(
  project_key="/Users/richardthompson/StudioProjects/VoxPlanApp",
  agent_name="<agent_name>"
)
```

### Fetch Inbox
```
mcp__mcp-agent-mail__fetch_inbox(
  project_key="/Users/richardthompson/StudioProjects/VoxPlanApp",
  agent_name="<your_agent_name>",
  limit=20,
  include_bodies=true
)
```

### Send Message
```
mcp__mcp-agent-mail__send_message(
  project_key="/Users/richardthompson/StudioProjects/VoxPlanApp",
  sender_name="<your_agent_name>",
  to=["<recipient_agent_name>"],
  subject="Brief subject",
  body_md="Message content in markdown",
  thread_id="bd-<issue-id>"  # Optional: link to beads issue
)
```

### Reserve Files
```
mcp__mcp-agent-mail__file_reservation_paths(
  project_key="/Users/richardthompson/StudioProjects/VoxPlanApp",
  agent_name="<your_agent_name>",
  paths=["app/src/main/java/com/voxplanapp/..."],
  ttl_seconds=3600,
  exclusive=true,
  reason="Working on <issue-id>"
)
```

### Release Files
```
mcp__mcp-agent-mail__release_file_reservations(
  project_key="/Users/richardthompson/StudioProjects/VoxPlanApp",
  agent_name="<your_agent_name>"
)
```

### List Available Agents
Use resource to discover agent names:
```
resource://agents//Users/richardthompson/StudioProjects/VoxPlanApp
```

## Server Management

**Start Server:**
```bash
uv run python -m mcp_agent_mail.cli serve-http
# or
scripts/run_server_with_token.sh
```

**Default Port:** 8765

**Health Check:** http://127.0.0.1:8765/health (public, no auth)

## Authentication

- **Localhost:** No authentication required by default
- **Remote/Token:** Set `HTTP_BEARER_TOKEN` environment variable
- **Health endpoints:** Always public (`/health/*`)

## Documentation Links

- **GitHub Repo:** https://github.com/Dicklesworthstone/mcp_agent_mail
- **MCP Tools:** All tools prefixed with `mcp__mcp-agent-mail__`
- **CLAUDE.md:** See "Agent Mail Coordination (MANDATORY)" section for workflow

## Quick Troubleshooting

**Can't access UI:**
1. Check server is running: `ps aux | grep mcp_agent_mail`
2. Check port is listening: `lsof -i :8765`
3. Try accessing: http://127.0.0.1:8765/mail

**No agents showing:**
1. Register agent first using `create_agent_identity` or `register_agent`
2. Set contact policy to "open" for collaboration
3. Check project key matches exactly

**Messages not appearing:**
1. Verify sender/recipient are registered
2. Check project key is correct
3. Use web UI search to find messages
4. Check thread_id linkage if using beads integration
