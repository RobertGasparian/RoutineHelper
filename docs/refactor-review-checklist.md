# Refactor Review Checklist

This checklist tracks the class-by-class standardization pass after modularization. It is the handoff point for future chats and agents. Work through one category at a time and keep each change small enough for manual review.

## Status

- `[x]` reviewed and completed for the current codebase
- `[ ]` not yet reviewed
- The first unchecked category is the default next category.

## Review Order

- [x] **Repositories**
  - Reviewed all domain repository contracts and Room implementations.
  - Standardized atomic field updates, transactional aggregate reads and resets, cadence storage decoding, and cadence-specific contracts.
  - Removed unused or incomplete repository APIs.

- [x] **Use cases**
  - Review every use case for one focused business action, accurate naming, appropriate repository dependencies, duplicated rules, and test coverage.
  - Keep multi-step ordering, branching, and delivery decisions out of use cases.
  - Standardized command/read naming, kept read-only Flow-backed use cases noun-style, and preserved focused command use cases.

- [x] **Orchestrators and workflow coordinators**
  - Review `DailySnapshotOrchestrator`, `WeeklySnapshotOrchestrator`, and `RoutineSnapshotBackfill`.
  - Keep orchestration explicit while delegating atomic business actions to use cases.
  - Kept Daily and Weekly snapshot orchestration explicit, with atomic use cases handling individual business actions.

- [x] **Domain models and domain utilities**
  - Review routine, template, snapshot, cadence, date, and period models/helpers.
  - Check invariants, defaults, naming, cadence symmetry, and whether behavior belongs on a model or in a focused collaborator.
  - Standardized cross-cadence snapshot naming, made snapshot period/cadence contracts explicit, and moved generic calendar helpers to `:core:time`.
  - Deferred broader Daily/Weekly item abstraction until future cadence behavior proves the right shared model shape.

- [x] **Room primitives**
  - Review entities, relation models, DAOs, queries, constraints, defaults, cascade behavior, and database composition.
  - Preserve schema version 1 unless a separately approved behavior change requires a migration.
  - Standardized daily entry Room naming, removed implicit Daily cadence storage defaults, and deleted unused DAO methods.

- [x] **Providers, formatters, and pure mappers**
  - Review time/date providers, presentation providers, share formatting, and domain-to-UI mappers.
  - Keep platform and presentation concerns out of domain code and add focused mapper tests where useful.
  - Extracted History mapper/formatting helpers from ViewModels, added focused mapper tests, and tightened snapshot share text formatter coverage.

- [x] **ViewModels**
  - Review Action Editor, Daily, Weekly, History, and History Detail ViewModels.
  - Standardize state construction, intent handling, coroutine/error behavior, dependency boundaries, and tests.
  - Standardized route argument ownership, public `StateFlow` state exposure, `BaseViewModel` usage, and ViewModel-owned share/form state.

- [ ] **UI state, events, and local state holders** - next
  - Review UI-state models, `XxxIntent` contracts, ViewModel-emitted `XxxUiEvent` outputs, share drafts, reorder state, and Compose-only state holders.
  - Keep Compose-specific interaction details out of ViewModel state.

- [ ] **Screens and components**
  - Review stateful screen boundaries, stateless components, callback/event surfaces, previews, and shared design-system usage.
  - Limit work to architectural cleanup; defer visual redesign and preserve Paparazzi baselines.

- [ ] **Workers and scheduling adapters**
  - Review workers, scheduler setup, work-date calculation, WorkManager input/output, retries, and cancellation handling.
  - Workers stay thin and delegate structured flows to orchestrators.

- [ ] **Dependency-injection modules**
  - Review Hilt bindings and providers for ownership, scope, visibility, and unnecessary app-level wiring.

- [ ] **Navigation and app shell**
  - Review destinations, top-level back stack, root composition, startup, and feature navigation boundaries.
  - Keep `:app` limited to composition and Android application entry points.

- [ ] **Test infrastructure and final enforcement**
  - Review fakes, fixtures, dispatcher rules, test naming, supported verification commands, dependency visibility, and remaining documentation.
  - Add static enforcement only for patterns that have settled during this pass.

## Guardrails For Every Category

- Preserve business behavior and visual output unless a specific behavior change is approved and documented.
- Never record or update Paparazzi baselines without explicit approval.
- Use descriptive backtick test names in `given ... when ... then ...` form; `given` is optional.
- Prefer Kotlin APIs and patterns unless a Java alternative is materially simpler or provides an essential missing capability.
- Use `XxxIntent` for outside-in user/screen actions. Reserve `XxxUiEvent` for one-off outputs emitted by the ViewModel for the screen or outside world to react to.
- Keep upward `XxxIntent` and event payloads narrow: pass only the ids and values the handler consumes instead of full `UiState`/item-state models.
- Keep reusable feature presentation contracts in dedicated files instead of burying them inside `Screen.kt`.
- Keep UI-state files focused; split independent item-state, editor-state, draft-state, or local-state-holder concepts when they gain their own behavior or tests.
- Avoid boolean cadence/variant flags in presentation helpers when an explicit enum, sealed type, or cadence-specific function can describe the branch.
- Keep multi-step flows in explicitly named orchestrators and focused business actions in use cases.
- Apply an accepted pattern to genuinely similar Daily, Weekly, and other flows without hiding cadence-specific behavior.
- After each batch, report whether it is relocation-only, structural, behavior-changing, test-only, or documentation-only.
- Call out meaningful production changes separately from tests and documentation.

## Resume Instruction

At the start of a new chat, read `docs/development-rules.md`, `docs/architecture-refactor-plan.md`, and this checklist. Confirm the working tree and continue with the first unchecked category. Do not redo completed categories unless a later change invalidates their review.
