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

## Version 2 — Nutrition, education and sustainable progress (future)
**Status: concept/planned; not part of the v1 release gate.** Build on v1's dated measurement and calorie records, without blocking workout logging or requiring a nutrition subscription.

### Nutrition experience
- Optional nutrition onboarding: goals (fat loss, muscle gain, maintenance or general health), food preferences, dietary restrictions, budget, cooking experience, household size and available equipment.
- Practical, affordable recipes and meal plans that teach shopping, basic preparation, substitutions, portioning and cooking skills; include accessible, budget-conscious options rather than assuming premium ingredients.
- Nutrition guidance for calorie and protein targets, with other nutrients where useful. Estimates must be labeled as estimates and adjustable by the user.
- Suggest filling, lower-calorie foods and sensible meal additions when hunger management matters, without treating foods as morally good or bad.
- **One-tap recipe logging:** when a user prepares a recommended recipe, record the estimated calories and nutrients based on the chosen servings and any ingredient substitutions. Let users correct the record.
- Support manual daily intake entry alongside optional recipe-based logging; avoid double counting.

### Progress and coaching principles
- Celebrate consistency and achievable improvements: walking farther, lifting more, preparing a healthier meal or making a better-informed choice, not only weight changes.
- Explain that fat loss and muscle gain can happen together, so scale weight alone may not reflect progress. Connect nutrition trends with workout history and body measurements where the user consents.
- Use supportive, nonjudgmental language; avoid shame, unrealistic deadlines or treating a missed day as failure.
- Make data entry transparent and correctable. Insights depend on user-entered information and should acknowledge missing or uncertain data.
- Preserve offline-first fundamentals, privacy, user control and clear consent for any future AI-powered personalization.

### v2 implementation milestones (not yet scheduled)
1. Define nutrition records, serving sizes, units and recipe data model.
2. Build recipe discovery, budgeting and cooking instruction screens.
3. Add meal plans, grocery lists and substitution support.
4. Implement one-tap recipe-to-food-log flow and corrections.
5. Add optional progress insights linking nutrition, measurements and workouts.
6. Validate nutritional calculations, accessibility, privacy and real-user usability before release.
