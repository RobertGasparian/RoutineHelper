# Development Rules

## Refactor Workflow

- Work in small, reviewable chunks with one primary purpose per chunk.
- Preserve business logic and visual UI output unless the change explicitly calls for different behavior or visuals.
- Prefer refactoring one vertical slice first, then applying the proven pattern to similar flows.
- Do not create abstractions before they are earned by at least two concrete flows, unless the code is clearly shared infrastructure.
- Keep explanations and examples grounded in this codebase. Mark general examples clearly when they are not taken from this project.
- Do not mix module moves, behavior changes, UI design changes, and opportunistic cleanup in the same review chunk.
- Every refactor handoff must classify the chunk as relocation-only, structural wiring, or behavior-changing, and explicitly list meaningful production, test, and documentation changes so reviewers know whether to inspect file contents or only ownership/location changes.
- When touching debug/test-only production-source affordances, keep the affordance only when it is gated by debug build behavior.

## Kotlin First

- Prefer Kotlin-based frameworks, APIs, and patterns over Java-based alternatives when both satisfy the requirement well.
- Use a Java-based solution when it is materially simpler in this codebase, required for Android/framework interop, or provides essential capabilities that the Kotlin alternative does not provide.
- When using a Java-based solution by exception, keep it behind the smallest reasonable boundary so feature and business code can stay Kotlin-oriented.
- Prefer Kotlin idioms such as function types, immutable data, extension functions, and sealed types over Java-style callback interfaces, mutable holders, or framework abstractions when the Kotlin version is clear and testable.

## Modules

- The target architecture is multimodule. The current `:app` module should be split incrementally as boundaries stabilize.
- Add modules only when the boundary is useful and reviewable; do not create empty future-facing modules.
- Use four top-level groups: `:app`, `:features:*`, `:libs:*`, and `:core:*`.
- `:app` is the Android shell. It owns app entry points, root navigation, app startup, and dependency aggregation.
- `:features:*` modules are presentation entry points. They own screens, components, ViewModels, UI state, UI events, previews, and Paparazzi tests.
- `:libs:*` modules are business capability modules. They own app data, business rules, repositories, use cases, and implementation details for a specific capability.
- When a capability earns separate submodules, use `:domain` for platform-independent models, repository contracts, and use cases, and `:data` for data sources and repository implementations.
- A capability's `:data` submodule may depend on its `:domain` submodule; `:domain` must not depend on `:data`.
- A capability's `:data` submodule owns the dependency-injection binding from its repository implementation to its domain repository contract.
- `:core:*` modules are cross-cutting building blocks that are not specific to RoutineHelper business capabilities.
- Do not create a broad `routine`, `domain`, or `data` module whose job is "shared app logic." Prefer capability boundaries such as template, tracking, snapshot, reminders, reflection, and background work.
- `:features:*` may depend on `:libs:*` APIs and `:core:*`.
- `:features:*` must not depend on Room DAOs, Room entities, Room database classes, WorkManager workers, or repository implementations.
- `:features:*` must not depend on `:app`.
- Avoid feature-to-feature implementation dependencies. Route cross-feature navigation through `:app` or a small feature API/navigation contract only when needed.
- Cadence variants that share one presentation workflow may live as packages inside one cohesive feature module; cadence packages must share neutral presentation contracts rather than depend on one another.
- `:libs:*` may depend on `:core:*` and other `:libs:*` APIs when there is a clear one-way capability relationship.
- `:libs:*` should avoid depending on other `:libs:*` implementations unless the module is explicit infrastructure wiring.
- `:libs:routine:database` is infrastructure wiring only: it may aggregate capability-owned Room entities and DAOs and provide the shared database/DAO bindings, but must not contain repositories or business rules.
- `:app` aggregates the data and database modules but must not construct capability repository implementations itself.
- Default cross-cutting bindings live with their `:core:*` implementation, and feature-specific presentation collaborator bindings live with their feature; `:app` aggregates these modules instead of constructing their implementations.
- Modules declare direct dependencies for APIs their source imports. Dependencies used only to aggregate DI/runtime implementations are allowed in `:app`, but should be identifiable as composition dependencies rather than accidental transitive access.
- `:libs:*` must not depend on `:features:*`.
- `:core:*` must not depend on features or business libs.
- The module graph must stay acyclic. If two libs need each other both ways, split out a smaller shared API/model or merge them into one cohesive capability.
- Cadence is a horizontal dimension inside routine libs. Start daily, weekly, and future monthly support as packages/classes inside template/tracking/snapshot libs; promote cadence to modules only if the boundary becomes independently valuable.
- Keep shared Compose components, theme, and design-system primitives in a shared UI boundary once modules are introduced.
- Move WorkManager code out of `:app` before adding new unrelated background jobs.
- Keep `CoroutineWorker` subclasses thin: delegate multi-step operations to injectable, unit-tested orchestrators while the worker retains WorkManager result, retry, and cancellation handling.
- Use cases model one focused business action. Multi-step flows, ordering, branching, and delivery-specific decisions belong to explicitly named orchestrators that compose those use cases.
- Do not rename an orchestrator to `UseCase` when it still owns multiple commands or a decision tree.

## UI

- Screen-level composables use two layers:
  - `XxxScreen`: stateful boundary. Owns the `ViewModel`, collects state, handles launched effects, and maps UI events to ViewModel calls.
  - `XxxComponent`: stateless renderer. Receives a `UiState`, renders it, and propagates events upward.
- If a screen component (`XxxComponent`) or reusable composable needs more than three callbacks, replace the callback list with a corresponding `XxxUiEvent` sealed interface and a single `onEvent: (XxxUiEvent) -> Unit` callback. The `XxxScreen` handles those events by calling ViewModel functions, navigation callbacks, Android intents, or other side-effect boundaries.
- Components must be covered with Paparazzi snapshot tests for relevant states.
- Every `UiState` data class must include a `companion object` with at least `preview()`. Add state-specific helpers such as `previewEmpty()` or `previewError()` when applicable.
- Each component should have:
  - a simple working `@Preview` near the component for fast local tweaking.
  - a separate `XxxPreviews.kt` file next to the component with normal mobile light/dark, landscape light/dark, tablet, and foldable previews.
- Avoid visual redesign during architecture refactors. Touch UI code when it is needed for state, event, ViewModel, preview, snapshot, or module-boundary cleanup.
- Keep feature-specific display mappings such as labels and icons in one feature-owned presentation mapping. Do not move business-specific mappings into `:core:ui` merely to reuse a visual component.
- Debug-only UI actions in production source must be gated by debug build behavior when touched.

## Presentation

- ViewModels must depend on use cases or presentation-specific collaborators, not repositories or data sources.
- Repositories stay behind use cases or lib APIs so presentation code does not know data-source or repository implementation boundaries.
- ViewModels should expose stable UI state and receive state-changing UI intents through a feature event handler.
- When a feature has navigation events and ViewModel-handled events in the same UI event stream, model the ViewModel-handled subset as `XxxUiEvent.Intent`.
- UI state should be named for the feature or shared concept it represents. Avoid reusing a feature-specific name for another feature unless that is the intentional shared model.
- Direct time reads should be isolated behind a small provider/collaborator when the code path is business logic, scheduling, snapshotting, or test-sensitive presentation state.

## Data And Domain

- Public lib APIs describe app behavior, not Room operations.
- Keep public repository contracts limited to demonstrated consumers; remove unused speculative queries instead of preserving incomplete behavior for possible future use.
- Repository implementations map Room entities and relation models into business models before returning them.
- Business logic needed by multiple features, workers, app startup, or future surfaces belongs in a lib capability, not in a feature module.
- Features render and coordinate UI-specific state. They do not own data/business rules that background work or other features must also respect.
- Keep storage string conversions, such as cadence storage values, behind data-layer boundaries unless a business type explicitly owns the conversion.
- Decode every supported stored enum value explicitly and fail on unknown values; never silently reinterpret malformed or newer data as an existing domain value.
- Normalize user-entered text at the boundary that persists it, and keep that rule consistent across similar flows.
- Prefer field-specific, single-statement DAO updates/upserts when independent fields of one row can change concurrently; avoid read-copy-replace writes that can overwrite unrelated state.
- Wrap repository operations that mutate multiple rows or DAOs as one logical action in a Room transaction.
- Read a Room-backed domain aggregate through one transactional relation query; do not combine independently observed parent and child queries in a repository.

## Testing

- New code should include unit tests when the behavior is feasible to test without excessive framework setup.
- Feasible unit-test targets include domain use cases, pure Kotlin models/mappers/formatters, repository logic that can run against fakes or in-memory stores, and ViewModel state/event behavior with fake use cases.
- Prefer Paparazzi for component rendering states and regular unit tests for logic/state transformations.
- Do not force unit tests around thin DI modules, generated framework glue, simple data classes with no behavior, or code that is better covered by a UI/snapshot/integration test.
- Test function names should be descriptive Kotlin backtick names using `given ... when ... then ...`; omit `given ...` when there is no meaningful setup condition.
- Existing Paparazzi test method names are part of their baseline image identity. Do not rename them solely for style compliance without explicit approval to update the corresponding baselines; new Paparazzi tests should follow the descriptive naming rule from the start.
- Paparazzi snapshots are regression checks during refactors.
- Do not run Paparazzi record/update tasks unless explicitly approved.
- Do not update existing snapshot images unless explicitly approved.
- If UI-rendered code changes, run Paparazzi verification when practical and report any visual diffs without recording new baselines.
