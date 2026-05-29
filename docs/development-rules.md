# Development Rules

## UI

- Screen-level composables use two layers:
  - `XxxScreen`: stateful boundary. Owns the `ViewModel`, collects state, handles launched effects, and maps UI events to ViewModel calls.
  - `XxxComponent`: stateless renderer. Receives a `UiState`, renders it, and propagates events upward.
- Components must be covered with Paparazzi snapshot tests for relevant states.
- Every `UiState` data class must include a `companion object` with at least `preview()`. Add state-specific helpers such as `previewEmpty()` or `previewError()` when applicable.
- Each component should have:
  - a simple working `@Preview` near the component for fast local tweaking.
  - a separate `XxxPreviews.kt` file next to the component with normal mobile light/dark, landscape light/dark, tablet, and foldable previews.

## Presentation

- ViewModels must depend on domain use cases, not repositories.
- Repositories stay behind use cases so presentation code does not know data-source or repository boundaries.

## Testing

- New code should include unit tests when the behavior is feasible to test without excessive framework setup.
- Feasible unit-test targets include domain use cases, pure Kotlin models/mappers/formatters, repository logic that can run against fakes or in-memory stores, and ViewModel state/event behavior with fake use cases.
- Prefer Paparazzi for component rendering states and regular unit tests for logic/state transformations.
- Do not force unit tests around thin DI modules, generated framework glue, simple data classes with no behavior, or code that is better covered by a UI/snapshot/integration test.
