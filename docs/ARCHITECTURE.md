# Sprinter architecture — offline-first, sync-ready, AI-accessible

## Core acceptance test
A workout logged on Android is automatically synchronized when online. During a normal ChatGPT conversation, an authorized MCP tool can fetch that workout without manual import, copying, or attachment. This is the defining product requirement, not a stretch feature.

## System boundaries
1. **Android UI:** Jetpack Compose exercise library, routines, active workout, history.
2. **Local storage:** Room database; writes work without a network.
3. **Sync engine:** durable change/outbox tracking, WorkManager scheduling, retry/backoff and idempotent requests.
4. **Private backend:** authenticated HTTPS API, user-scoped records, server-side authorization, sync cursors and change versions.
5. **ChatGPT integration:** authenticated remote MCP service exposes consented read tools and later approved write tools.

The backend and MCP connection do not yet exist. Confirm actual ChatGPT custom connector support and authentication requirements before committing to hosting technology.

## Local data model
- `Exercise`: stable UUID, name, equipment, muscle group, notes.
- `Routine`: UUID, name, creation/update timestamps.
- `RoutineExercise`: routine ID, exercise ID, order, optional targets.
- `WorkoutSession`: UUID, start/end timestamps, status, optional routine ID.
- `WorkoutExercise`: UUID, session ID, exercise ID, order.
- `SetEntry`: UUID, workout exercise ID, set order, normalized weight, reps, type, completion timestamp.

**Sync-ready additions to design before schema stabilization:**
- Stable client-generated UUIDs and UTC timestamps.
- Local `SyncOutbox` (operation ID, entity type/ID, operation, payload/version, creation timestamp, retry status).
- Per-record version or revision and deletion tombstones; do not rely on last-write-wins for simultaneous edits without reviewing consequences.
- Account ownership stored and enforced on the backend, not trusted solely from client-provided IDs.
- Sync cursor/checkpoint, idempotent upserts and deterministic conflict rules.
- Explicit handling of initial anonymous/offline records when linking an account.
- Database migrations and tests for edit/delete/offline replay.

The initial Room entities in the scaffold are **not yet fully sync-ready**. Update them before adding production data.

## Workout integrity
- Persist active session changes promptly; never depend on network success to log a set.
- Routine templates and historical sessions remain separate.
- Store weight in a normalized unit and convert for display.
- Do not silently overwrite historical records when routines change.
- Export/import is a portability and backup feature, not the intended ChatGPT workflow.

## Security and privacy
- HTTPS, authentication, server-enforced per-user authorization and minimal access scopes.
- Keep API tokens in appropriate secure storage; never in the repository.
- Revoke access, delete account data, and audit authorized writes.
- No public workout endpoints; no secret tokens in client code.
- AI read access first. Require explicit confirmation for routine modifications and log edits.
- Clearly indicate last successful sync so ChatGPT can disclose when data may be stale.

## MCP tool contract (proposed)
- `get_workout_history(start_date, end_date)`: sessions and completed sets.
- `get_exercise_progress(exercise_id, start_date, end_date)`: progression, volume and records.
- `get_routines()`: current routine definitions.
- Future: `propose_routine_update` / confirmed mutation tools.

## Verification milestones
1. Android project builds and stores a workout offline.
2. App automatically syncs a new workout to an authenticated backend.
3. Remote MCP connection is available and authorized in ChatGPT.
4. ChatGPT retrieves the workout with no manual file transfer.
5. Offline/reconnect and token revocation tests pass.

A local-only MVP completes step 1, **not** the full Sprinter product.
