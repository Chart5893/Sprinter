# Proposed architecture (planning document)

## App layers
- **UI:** Jetpack Compose screens for exercises, routines, active workout and history.
- **Presentation:** ViewModels exposing immutable UI state and handling user actions.
- **Domain/data:** Repository interfaces and use cases where complexity warrants them.
- **Persistence:** Room entities, DAOs and migrations; local database is authoritative during MVP.

## Initial entities
- `Exercise`: stable ID, name, equipment, muscle group, optional notes.
- `Routine`: stable ID, name, creation/update timestamps.
- `RoutineExercise`: routine ID, exercise ID, display order and optional target sets/reps.
- `WorkoutSession`: stable ID, start/end timestamps, status, optional routine ID.
- `WorkoutExercise`: session ID, exercise ID, order.
- `SetEntry`: workout exercise ID, set order, weight, reps, optional set type, completed timestamp.

Use stable UUID identifiers and timestamps from the beginning to support later export and sync. Decide weight units explicitly and store normalized values. Keep routine templates separate from historical completed sessions.

## Reliability rules
- Persist active workout state promptly, not only when the session is completed.
- No account or internet required for normal logging.
- Plan database migration tests before changing the schema.
- Export files should be user-initiated and not expose private records through public URLs.
- Do not commit actual workout data, tokens or credentials.

## AI integration (future)
A separate authenticated service can expose scoped, consent-based read operations (workout history, routines, progression) and later approved write operations. Prefer read-only integration initially. Validate the current custom MCP connector installation and authorization process before committing to a particular hosting approach.
