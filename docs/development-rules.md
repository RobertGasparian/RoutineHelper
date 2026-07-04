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
- `:features:*` modules are presentation entry points. They own screens, components, ViewModels, UI state, intents, UI events, previews, and Paparazzi tests.
- `:libs:*` modules are business capability modules. They own app data, business rules, repositories, use cases, and implementation details for a specific capability.
- When a capability earns separate submodules, use `:domain` for platform-independent models, repository contracts, and use cases, and `:data` for data sources and repository implementations.
- A capability's `:data` submodule may depend on its `:domain` submodule; `:domain` must not depend on `:data`.
- A capability's `:data` submodule owns the dependency-injection binding from its repository implementation to its domain repository contract.
- `:core:*` modules are cross-cutting building blocks that are not specific to RoutineHelper business capabilities.
- `:core:presentation` owns shared ViewModel infrastructure. Keep it generic: no feature, routine, Room, WorkManager, Compose, or business-language dependencies.
- Do not create a broad `routine`, `domain`, or `data` module whose job is "shared app logic." Prefer capability boundaries such as template, tracking, snapshot, reminders, reflection, and background work.
- `:features:*` may depend on `:libs:*` APIs and `:core:*`.
- `:features:*` must not depend on Room DAOs, Room entities, Room database classes, WorkManager workers, or repository implementations.
- `:features:*` must not depend on `:app`.
- Avoid feature-to-feature implementation dependencies. Route cross-feature navigation through `:app` or a small feature API/navigation contract only when needed.
- Root navigation should use typed destination contracts instead of `Any` route values. Keep top-level destinations as a narrower subtype when top-level navigation needs special behavior.
- Root navigation back-stack state should be saveable with an explicit app-owned saver. Encode destinations into primitive saveable fields instead of relying on broad object serialization.
- Keep root navigation files focused as the app shell grows: destination contracts, back-stack persistence, graph entries, transitions, and bottom navigation metadata may live in separate app-owned files.
- Cadence variants that share one presentation workflow may live as packages inside one cohesive feature module; cadence packages must share neutral presentation contracts rather than depend on one another.
- `:libs:*` may depend on `:core:*` and other `:libs:*` APIs when there is a clear one-way capability relationship.
- `:libs:*` should avoid depending on other `:libs:*` implementations unless the module is explicit infrastructure wiring.
- `:libs:routine:database` is infrastructure wiring only: it may aggregate capability-owned Room entities and DAOs and provide the shared database/DAO bindings, but must not contain repositories or business rules.
- `:app` aggregates the data and database modules but must not construct capability repository implementations itself.
- Default cross-cutting bindings live with their `:core:*` implementation, and feature-specific presentation collaborator bindings live with their feature; `:app` aggregates these modules instead of constructing their implementations.
- Modules declare direct dependencies for APIs their source imports. Dependencies used only to aggregate DI/runtime implementations are allowed in `:app`, but should be identifiable as composition dependencies rather than accidental transitive access.
- Prefer Hilt `@Binds` for interface-to-implementation bindings when the implementation has an injectable constructor. Use `@Provides` for framework factories, builder APIs, external objects, and values that require custom construction.
- Scope bindings installed in `SingletonComponent` with `@Singleton` when the implementation is stateless or intended to share app lifetime. Leave bindings unscoped only when each injection should create fresh state.
- `:libs:*` must not depend on `:features:*`.
- `:core:*` must not depend on features or business libs.
- The module graph must stay acyclic. If two libs need each other both ways, split out a smaller shared API/model or merge them into one cohesive capability.
- Cadence is a horizontal dimension inside routine libs. Start daily, weekly, and future monthly support as packages/classes inside template/tracking/snapshot libs; promote cadence to modules only if the boundary becomes independently valuable.
- Cadence-specific contracts encode their cadence in the API and must not accept a redundant cadence argument that permits invalid combinations.
- Keep shared Compose components, theme, and design-system primitives in a shared UI boundary once modules are introduced.
- Move WorkManager code out of `:app` before adding new unrelated background jobs.
- Keep `CoroutineWorker` subclasses thin: delegate multi-step operations to injectable, unit-tested orchestrators while the worker retains WorkManager result, retry, and cancellation handling.
- Keep repeated WorkManager result policy behind a small tested collaborator so workers only name the background action they run.
- Keep recurring WorkManager schedule specs testable without requiring Android WorkManager runtime setup; isolate pure schedule policy such as unique names, tags, intervals, policies, and initial delays from enqueueing.
- Current app-start snapshot backfill is best-effort for the latest missed daily and weekly boundary only. Do not claim multi-period recovery until the app stores enough per-period state to recover multiple missed days or weeks.
- Use cases model one focused business action. Multi-step flows, ordering, branching, and delivery-specific decisions belong to explicitly named orchestrators that compose those use cases.
- Prefer focused add/update/delete use cases over a broad `Save...UseCase` when create and update paths call different repository operations or require different context.
- Read-only use cases that expose ongoing repository `Flow` data may use noun-style names; reserve verb-style names for commands and state-changing operations.
- Do not rename an orchestrator to `UseCase` when it still owns multiple commands or a decision tree.
- Pure display/export string builders should be named as formatters, not use cases, and should receive time-sensitive context through providers instead of reading system time or timezone directly.

## UI

- Screen-level composables use two layers:
  - `XxxScreen`: stateful boundary. Owns the `ViewModel`, collects state, handles launched effects, and maps UI events to ViewModel calls.
  - `XxxComponent`: stateless renderer. Receives a `UiState`, renders it, and propagates events upward.
- If a screen component (`XxxComponent`) or reusable composable needs more than three callbacks, replace the callback list with a corresponding `XxxIntent` sealed interface and a single `onIntent: (XxxIntent) -> Unit` callback. The `XxxScreen` handles those intents by calling ViewModel functions, navigation callbacks, Android intents, or other side-effect boundaries.
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
- Feature ViewModels should extend `BaseViewModel<XxxUiState, XxxIntent, XxxUiEvent>` from `:core:presentation`; use `Nothing` for `XxxUiEvent` until the ViewModel emits one-off outputs.
- ViewModels expose a single stable `override val uiState: StateFlow<XxxUiState>` for screen state. Build it from private `MutableStateFlow`s and read-only use-case flows with `stateInViewModel(initialState)`.
- ViewModels handle outside-in actions by overriding `handleIntent(intent: XxxIntent)`. Screens call the inherited final `onIntent(...)`.
- Use the inherited `launch { ... }` helper for ViewModel coroutines unless a call needs custom coroutine behavior.
- Use Hilt assisted ViewModels for stable route/screen identity arguments such as IDs, cadence, or other navigation parameters. Once the ViewModel is created, public ViewModel methods should read those owned arguments instead of requiring the screen to pass them back on every call.
- Keep mutable presentation state private inside the ViewModel. Screens collect `uiState` and send outside-in actions through `onIntent(...)`; they should not assemble ViewModel state flows themselves.
- Name outside-in user or screen actions as `XxxIntent`, even when the screen handles some of them directly for navigation or platform side effects.
- Keep navigation and platform callbacks at the stateful `XxxScreen` boundary; stateless components should report those user actions through `XxxIntent` instead of receiving separate navigation callbacks.
- Keep `XxxIntent` payloads minimal and action-specific. Pass stable ids, primitive values, or focused value objects; do not pass an entire `UiState`/item state when the handler only needs a few fields. Full `UiState` objects are appropriate as renderer inputs, not as upward event or ViewModel action payloads.
- Name ViewModel-to-screen one-off outputs as `XxxUiEvent`. Use this only for effects initiated by the ViewModel after state changes or handled intents, such as navigation requests, share requests, or snackbars.
- Keep public feature presentation contracts such as `XxxUiState`, `XxxIntent`, and `XxxUiEvent` in dedicated files once they are used by both a screen/component and a ViewModel, test, or preview.
- Keep small contract-owned mapping helpers next to the contract when multiple call sites need them; use a dedicated feature-owned presentation mapper when the mapping grows beyond that contract.
- Keep derived presentation properties on the UI-state model when multiple components need the same derived value, instead of repeating private extension properties in each component.
- Do not drive UI styling by comparing display strings. Expose explicit presentation state, such as a boolean or sealed summary, and keep labels as labels.
- Keep UI-state files focused. Split item-state models, local state holders, and UI-state models with behavior into dedicated files when one `XxxUiState.kt` file starts to own multiple independent concepts.
- Avoid boolean flags for cadence or variant selection in UI-state factories and presentation helpers. Pass an explicit enum/sealed type such as `RoutineCadence`, or split into cadence-specific functions when that reads better.
- Do not pass screen callbacks such as `onSaved` or `onDeleted` into ViewModel operations. The screen should send an intent, the ViewModel should finish the operation, then emit a `XxxUiEvent` for navigation, share requests, snackbars, or other outside-world reactions.
- UI state should be named for the feature or shared concept it represents. Avoid reusing a feature-specific name for another feature unless that is the intentional shared model.
- Direct time reads should be isolated behind a small provider/collaborator when the code path is business logic, scheduling, snapshotting, or test-sensitive presentation state.

## Data And Domain

- Public lib APIs describe app behavior, not Room operations.
- Keep public repository contracts limited to demonstrated consumers; remove unused speculative queries instead of preserving incomplete behavior for possible future use.
- Repository implementations map Room entities and relation models into business models before returning them.
- Business logic needed by multiple features, workers, app startup, or future surfaces belongs in a lib capability, not in a feature module.
- Features render and coordinate UI-specific state. They do not own data/business rules that background work or other features must also respect.
- Keep storage string conversions, such as cadence storage values, behind data-layer boundaries unless a business type explicitly owns the conversion.
- Define persisted enum strings as named constants owned by their data schema. Decode every supported value explicitly and fail on unknown values; never silently reinterpret malformed or newer data as an existing domain value.
- Cadence-specific operations must carry cadence through repository/data boundaries when raw IDs alone cannot prove the cadence, and implementations must not update records from a mismatched cadence.
- Normalize user-entered text at the boundary that persists it, and keep that rule consistent across similar flows.
- Prefer field-specific, single-statement DAO updates/upserts when independent fields of one row can change concurrently; avoid read-copy-replace writes that can overwrite unrelated state.
- Wrap repository operations that mutate multiple rows or DAOs as one logical action in a Room transaction.
- Read a Room-backed domain aggregate through one transactional relation query; do not combine independently observed parent and child queries in a repository.

## Testing

- New code should include unit tests when the behavior is feasible to test without excessive framework setup.
- Feasible unit-test targets include domain use cases, pure Kotlin models/mappers/formatters, repository logic that can run against fakes or in-memory stores, and ViewModel state/event behavior with fake use cases.
- Prefer Paparazzi for component rendering states and regular unit tests for logic/state transformations.
- Shared test doubles that are reused across modules should live in the owning module's `testFixtures` source set. Keep one-off DAO or repository fakes local to the test file when only one test class needs them.
- ViewModel tests and other tests that depend on `Dispatchers.Main` should use `MainDispatcherRule` from `:core:testing` instead of setting or resetting the main dispatcher inline.
- Do not force unit tests around thin DI modules, generated framework glue, simple data classes with no behavior, or code that is better covered by a UI/snapshot/integration test.
- Test function names should be descriptive Kotlin backtick names using `given ... when ... then ...`; omit `given ...` when there is no meaningful setup condition.
- Existing Paparazzi test method names are part of their baseline image identity. Do not rename them solely for style compliance without explicit approval to update the corresponding baselines; new Paparazzi tests should follow the descriptive naming rule from the start.
- Paparazzi snapshots are regression checks during refactors.
- Do not run Paparazzi record/update tasks unless explicitly approved.
- Do not update existing snapshot images unless explicitly approved.
- If UI-rendered code changes, run Paparazzi verification when practical and report any visual diffs without recording new baselines.
- Default refactor verification is `.\gradlew.bat :app:testDebugUnitTest :app:assembleDebug --no-daemon`, plus focused module tests for any changed module whose tests are not covered by the app task.
- Run `git diff --check` before handoff. Line-ending warnings are acceptable when no whitespace errors are reported.
