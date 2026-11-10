# Sprinter

**Sprint to your goals with AI.**

Sprinter is a personal-first Android strength-training tracker. **The defining product goal is to discuss workouts with ChatGPT using automatically accessible, up-to-date workout data—without manual CSV imports, attachments, or copying logs into chat.** The app must also work reliably offline and without an AI connection.

## Non-negotiable end-to-end experience
1. Log a workout on an Android phone, including when offline.
2. When connected, Sprinter securely syncs records to a private backend automatically.
3. A user-authorized ChatGPT connection retrieves those records via a secure API/MCP server.
4. Ask ChatGPT about progression, exercise history, personal records, and routines without importing files.
5. Later, approve AI-proposed routine changes before they are written back to Sprinter.

**Important dependency:** Validate that a custom authenticated MCP connector can be connected in the user's ChatGPT configuration early. Building an MCP server does not guarantee ChatGPT connector availability.

## Development workflow (hybrid, without Codex/Work)
1. **ChatGPT + GitHub:** Design architecture, implement focused changes, maintain docs and GitHub issues, and commit code.
2. **You + Android Studio:** Pull commits, sync Gradle, compile, run on Samsung, and report build/runtime errors and UX feedback.
3. **Together:** Review decisions and test each checkpoint. A GitHub commit is not proof of a successful build.

## Product principles
- Offline-first workout logging, with **sync-ready data design from day one**.
- Automatic cloud synchronization when online; no manual data import for ChatGPT conversations.
- User-owned, portable data; no subscription required for basic logging.
- Private-by-default access, authenticated connections, least-privilege scopes, revocation, and explicit consent.
- AI connectivity is a core integration requirement but must never block logging workouts.
- Never commit personal workout records or credentials.

## Stack
- Android: Kotlin, Jetpack Compose, Material 3, Room, ViewModel + repository.
- Synchronization: planned outbox/change tracking, WorkManager, retry/backoff, conflict handling.
- Backend: to be selected; private user-scoped storage, authenticated API and HTTPS.
- ChatGPT: compatible authenticated MCP tools for workout read access; approved writes later.
- Optional later: Health Connect.

## MVP checkpoints
See [ROADMAP.md](ROADMAP.md) and [GitHub Issues](https://github.com/Chart5893/Sprinter/issues). The personal tracker review is **November 18, 2026**, extendable to **December 2**. **A usable local tracker is an intermediate milestone, not completion of Sprinter's defining AI-access goal.**

## Future v2: nutrition and practical healthy eating
Sprinter v2 is planned to expand beyond workout logging into **affordable, approachable nutrition education**. The aim is to help people make sustainable improvements whether they want to lose a little weight, lose substantially more, build muscle or simply eat better.

Planned features include budget-aware recipes and meal plans, beginner-friendly cooking instructions, practical grocery guidance, filling meal options, personalized nutrition targets, and **one-tap logging of recommended recipes** with adjustable servings and ingredients. The experience should recognize progress beyond scale weight, including improved strength, endurance and individual food choices, and avoid judgment or guilt. Nutritional values will be estimates that users can review and correct.

This is a **future version 2 plan**, not a promise that these features exist in v1. See [ROADMAP.md](ROADMAP.md) for scope and implementation milestones.

## Current state
Initial Android application scaffold committed: Compose navigation and Room entity/DAO/database skeleton. **Not yet validated by an Android Studio build or device run.** The sync design is documented but not implemented.

## Run locally
1. Clone: `git clone https://github.com/Chart5893/Sprinter.git`
2. Open the repository root in Android Studio.
3. Sync Gradle; install requested SDK components and configure Gradle if necessary (wrapper not yet present).
4. Run the `app` configuration on an emulator or Android phone.
5. Report the first build/sync error. After successful sync, commit the Gradle wrapper.

## Architecture
See [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md).

## License
No license selected yet.
