# Sprinter

**Sprint to your goals with AI.**

Sprinter is a personal-first Android strength-training tracker. The initial goal is to replace subscription-limited workout logging with a reliable, offline-first experience. AI-assisted analysis and routine management are planned for later phases, after the core app works independently.

## Product principles
- Offline-first: workouts must be loggable without an account or network.
- No subscription required for core workout tracking.
- Data portability: export and backup personal workout data.
- AI is optional, not required for logging.
- Personal data must never be committed to the repository.

## Planned stack
- Kotlin, Jetpack Compose, Material 3
- Room for local persistence
- ViewModel + repository architecture
- WorkManager where background tasks are appropriate
- Optional future Health Connect integration
- Future authenticated API / MCP connector (feasibility and ChatGPT support to be verified)

## MVP
Exercise library, reusable routines, active workout logging (sets/reps/weights), rest timer, session history, offline persistence, and basic export.

See [ROADMAP.md](ROADMAP.md) for tentative checkpoints and [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) for the proposed design.

## Status
Planning / initial repository setup. No runnable Android application has been committed yet.

## Development
The first development task is to scaffold the Android project in Android Studio, verify the build on a Samsung device, and commit the generated Gradle wrapper and project files.

## License
No license has been selected yet. Do not assume permission to redistribute the source until a license is added.
