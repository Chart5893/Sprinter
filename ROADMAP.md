# Sprinter roadmap

## Product completion criterion
**ChatGPT can securely retrieve newly logged workout data without the user manually importing anything into a chat.** The Android tracker is an intermediate milestone, not the final deliverable.

## Two-week personal tracker checkpoints
Tentative personal tracker review: **November 18, 2026**, extendable to **December 2, 2026**.

| Date | Checkpoint | Acceptance criteria |
| --- | --- | --- |
| Nov 5 | Project setup and sync-ready architecture | Android project builds; stable IDs, timestamps and synchronization contract defined |
| Nov 7 | Exercise library and routines | Create and reopen ordered routines offline |
| Nov 9 | Active workout logging | Record sets/reps/weight offline and persist a completed session |
| Nov 11 | History and reliability | Review/edit history; recover interrupted sessions |
| Nov 13 | Strength insights and export | Display records and export data as a fallback, **not** as the ChatGPT integration |
| Nov 16 | Real-world gym test | Complete a workout without Strong |
| Nov 18 | Personal tracker review | Decide readiness or extend to Dec 2 |

## Early integration feasibility gate (do not postpone)
- Verify whether this ChatGPT account can connect to a user-built authenticated remote MCP service.
- Define minimum read tools: `get_workout_history`, `get_exercise_progress`, `get_routines`.
- Select an authentication and backend hosting approach only after verifying connection requirements.
- If connection support is unavailable, identify the limitation explicitly and revise the integration plan; do not present manual file uploads as meeting the goal.

## Post-local-MVP work: essential for product vision
1. Implement user-scoped backend, authentication and automatic background sync.
2. Test offline edits, retry, duplicates, conflicts, data isolation and deletion.
3. Implement read-only MCP tools; connect them to ChatGPT and verify real workout retrieval.
4. Add user-confirmed routine updates through the integration.
5. Optional Health Connect, backup/restore and historical Strong import.
6. Prepare multi-user security, privacy disclosures, Play Store testing and release.

## Scope control
Keep the personal tracker simple, but do not build a local-only data model that would require a redesign for automatic synchronization. No subscription or social features during the MVP.
