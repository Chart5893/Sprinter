# Sprinter

**Sprint to your goals with AI.**

Sprinter is a personal-first Android strength-training tracker. The goal is to replace subscription-limited logging with a reliable, offline-first app; AI-assisted analysis and routine management come later.

## Our development workflow (hybrid, without Codex/Work)

1. **ChatGPT + GitHub:** Design architecture, implement focused changes, maintain docs and GitHub issues, and commit code directly to `main` after reviewable changes.
2. **You + Android Studio:** Pull commits, sync Gradle, compile, run on your Samsung phone, exercise the UI, and report build/runtime errors or UX feedback.
3. **Together:** Review trade-offs, diagnose errors, prioritize fixes, and decide whether a checkpoint is complete. A GitHub commit is **not** proof the app builds or runs on your device.
4. **Issues:** Track work at [GitHub Issues](https://github.com/Chart5893/Sprinter/issues). Make small, traceable commits rather than large unexplained changes.

## Product principles
- Offline-first: workouts must be loggable without an account or network.
- No subscription required for core workout tracking.
- Data portability: export and back up personal workout data.
- AI is optional, not required for logging.
- Personal data and credentials must never be committed to the repository.

## Stack
- Kotlin, Jetpack Compose, Material 3
- Room for local persistence
- ViewModel + repository architecture
- Future: optional Health Connect and authenticated API / MCP connector

## MVP checkpoints
See [ROADMAP.md](ROADMAP.md) and [GitHub Issues](https://github.com/Chart5893/Sprinter/issues). The tentative personal MVP review is **November 18, 2026**, extendable to **December 2**.

## Current state
Initial Android application scaffold committed: Compose navigation and Room entity/DAO/database skeleton. **Not yet validated with an Android Studio build or a device run.** Exercise editing, routines, and workout logging are upcoming issues.

## Run locally
1. Clone: `git clone https://github.com/Chart5893/Sprinter.git`
2. Open the repository root in Android Studio.
3. Allow Gradle sync, install the Android SDK requested by the project, and use the IDE's configured Gradle distribution if a wrapper is not yet present.
4. Run the `app` configuration on an emulator or Android device.
5. Report the first Gradle sync/build error with its full message. After a successful sync, generate and commit the Gradle wrapper for reproducible command-line builds.

## Architecture
See [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md).

## License
No license has been selected yet. Do not assume permission to redistribute the source until one is added.
