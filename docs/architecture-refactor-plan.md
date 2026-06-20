# Architecture Refactor Plan

This plan describes the staged refactor from the current single-module Android app to a cleaner, multimodule architecture. The refactor must preserve business behavior and visual UI output unless a specific change is explicitly approved.

## Current Shape

- `:app` is the only Gradle module.
- `MainActivity` and `RoutineHelperApplication` are Android app shell entry points.
- `RoutineHelperScreen` owns app navigation and imports every feature screen directly.
- `di` wires Room-backed repositories to domain repository interfaces.
- `data.local` owns the Room database, DAOs, entities, and Room relation models.
- `data.repository` owns Room repository implementations.
- `domain.model`, `domain.repository`, and `domain.usecase` already form a mostly clean domain layer.
- `ui.daily`, `ui.weekly`, `ui.history`, `ui.history.detail`, `ui.actioneditor`, `ui.share`, and `ui.settings` are feature-like packages inside `:app`.
- `ui.dsm`, `ui.reorder`, and `ui.theme` are shared UI packages.
- `work` owns WorkManager scheduling, workers, backfill, and snapshot date calculations.
- Unit tests cover domain use cases, and Paparazzi tests cover several stateless components.

## Existing Strengths

- ViewModels mostly depend on use cases rather than repositories.
- Repository interfaces are already separated from Room implementations.
- Most screen packages follow the `XxxScreen` stateful boundary and `XxxComponent` stateless renderer pattern.
- Component previews and Paparazzi snapshots are already present.
- Daily and Weekly are intentionally similar, which gives us a useful pattern-migration path.

## Known Refactor Targets

- The single `:app` module hides dependency direction and lets unrelated packages reference each other too easily.
- Daily and Weekly duplicate ViewModel, repository, mapping, finalize, reset, and note-editor patterns.
- `RoutineHelperScreen` is a central navigation shell that imports all feature screens.
- Time is read directly from `LocalDate.now()`, `ZonedDateTime.now()`, `System.currentTimeMillis()`, `SimpleDateFormat`, and `Date`.
- Debug/test snapshot affordances exist in production source and should be gated when touched.
- Some shared UI state lives in Daily-named types even when Weekly reuses it.
- Large UI component files, especially shared cards and feature screens, are harder to review and test in small pieces.

## Target Module Direction

Do not create every module upfront. Add modules only when a boundary is stable enough that the move makes the code easier to understand.

The destination should use three large groups plus the app shell:

- `:app`
  - Android application shell.
  - Owns `MainActivity`, `RoutineHelperApplication`, app-level navigation composition, app startup, and dependency aggregation.
  - Wires feature implementations and library implementations together.
- `:features:*`
  - Presentation entry points: screens, components, ViewModels, UI state, UI events, previews, and Paparazzi tests.
  - Feature modules may contain presentation logic and feature-local UI state.
  - Feature modules should not own data or business rules needed by other features, workers, app startup, or future surfaces.
- `:libs:*`
  - Business capability modules. They own application data, business rules, repositories, use cases, and implementation details for a specific capability.
  - These modules are not a dumping ground for "anything used in two places"; each lib must have a clear domain boundary.
  - A lib may contain domain and data code in one module at first. Split into `:domain` and `:data` only when that separation makes review, dependency direction, testing, or build performance meaningfully better.
- `:core:*`
  - Cross-cutting building blocks that are not specific to RoutineHelper business capabilities.
  - Examples include time abstractions, shared UI primitives/theme, small common utilities, test helpers, and platform wrappers.
  - Core modules must stay small and boring. If a module contains routine, reflection, reminder, snapshot, or action-item business language, it is probably a lib, not core.

Proposed module families:

- `:features:daily`
  - Daily tracking UI, ViewModel, UI state/events, previews, and Paparazzi tests.
- `:features:weekly`
  - Weekly tracking UI, ViewModel, UI state/events, previews, and Paparazzi tests.
- `:features:history`
  - Snapshot history list and snapshot detail UI flows.
- `:features:action-editor`
  - Create/edit action UI flow.
- `:features:share`
  - Share preview UI, share dialogs, and share draft presentation state.
- `:features:settings`
  - Settings UI and future settings state management.
- `:features:reflection`
  - Future reflection UI flows.
- `:libs:routine:template:domain`
  - Platform-independent template models, repository contracts, and use cases for reusable action definitions such as title, description, cadence, repeat target count, and future deadline configuration if deadlines are configured on actions.
- `:libs:routine:template:data`
  - Room-backed template entities, DAOs, relation models, and repository implementations.
  - The shared `RoutineDatabase` remains temporarily in `:app` as schema composition while capability-owned Room types move into their data modules.
- `:libs:routine:tracking:domain`
  - Platform-independent per-period models, repository contracts, and use cases for today/weekly entries, checked state, hidden state, notes, completed count, and future check availability or lock state.
- `:libs:routine:tracking:data`
  - Room-backed per-period entities, DAOs, and repository implementations.
  - It depends on template data for the routine definitions that tracked entries reference.
- `:libs:routine:snapshot`
  - Snapshot finalization, snapshot summaries, snapshot detail data, snapshot deletion, and share-text source data.
- `:libs:reflection`
  - Future reflection business rules, data, and use cases.
- `:libs:reminder`
  - Future reminder scheduling intent, reminder settings, and reminder business rules.
- `:libs:background:work`
  - WorkManager workers, scheduling, constraints, and app-start/background orchestration.
  - Workers should orchestrate use cases from other libs; workers should not own routine/reflection/reminder business rules.
- `:core:time`
  - Date, clock, and timezone abstractions.
- `:core:ui`
  - Theme, design-system components from `ui.dsm`, reusable UI helpers, and shared component test helpers where appropriate.
- `:core:testing`
  - Shared test fakes, coroutine test setup, and test helpers when duplication across modules justifies it.

Cadence is a horizontal dimension inside routine libs. Daily, weekly, and future monthly support should start as packages/classes inside capability module families such as `:libs:routine:template:*`, `:libs:routine:tracking:*`, and `:libs:routine:snapshot:*`, not as separate modules by default. Promote cadence to module level only if it earns an independent boundary through separate dependencies, complexity, ownership, or review lifecycle.

## Dependency Rules

- `:app` may depend on feature implementations, lib implementations, lib APIs, and core modules.
- `:features:*` may depend on core modules and lib APIs.
- `:features:*` must not depend on lib implementation internals such as Room DAOs, Room entities, Room database classes, WorkManager workers, or repository implementations.
- `:features:*` must not depend on `:app`.
- Feature-to-feature implementation dependencies are avoided. Cross-feature navigation is coordinated by `:app` or, when necessary, by small feature API/navigation contracts.
- `:libs:*` may depend on core modules and other lib APIs when there is a clear one-way capability relationship.
- `:libs:*` should avoid depending on other lib implementations unless the module is explicit infrastructure wiring.
- `:libs:*` must not depend on features.
- `:core:*` must not depend on app-specific features or business libs.
- The module graph must stay acyclic. If two libs need to know too much about each other, split out a smaller shared API/model or merge the code into one cohesive capability.
- ViewModels depend on use cases or small presentation-specific collaborators, not repositories.
- Repository implementations depend on data sources and map to business models before crossing into public APIs.
- Shared code is extracted after the same pattern appears in at least two refactored flows, unless it is already clearly shared infrastructure.

Allowed examples:

- `:features:daily` depends on `:libs:routine:tracking:domain` APIs and `:core:ui`.
- `:features:weekly` depends on `:libs:routine:tracking:domain` APIs and `:core:ui`.
- `:libs:routine:tracking:domain` depends on `:libs:routine:template:domain` APIs.
- `:libs:routine:snapshot` depends on `:libs:routine:tracking:domain` APIs.
- `:libs:background:work` depends on routine, reflection, or reminder use-case APIs.

Disallowed examples:

- `:libs:routine:template:domain` depending on `:libs:routine:tracking:*`.
- `:libs:routine:tracking:domain` depending on `:libs:routine:snapshot:*`.
- `:libs:*` depending on `:features:daily` or any other feature implementation.
- `:features:daily` depending on `:features:weekly`.

## Staged Work Plan

### Stage 1: Architecture Plan And Rules

- Update architecture and development rules.
- Define guardrails for Paparazzi, review chunks, debug code, and module boundaries.
- No production code movement.

### Stage 2: Safety Baseline

- Run unit tests.
- Run Paparazzi verification, not recording.
- Record any existing failures before refactoring.
- Do not update screenshots unless explicitly approved.

### Stage 3: Daily Reference Slice

- Refactor Daily as the first vertical slice.
- Keep business behavior and visual output stable.
- Prefer state/event/ViewModel/use-case cleanup over UI polish.
- Add or adjust tests only for changed behavior or moved pure logic.
- If debug snapshot controls are touched, guard them behind debug build behavior.

### Stage 4: Weekly Mirror Slice

- Apply the proven Daily pattern to Weekly.
- Compare Daily and Weekly after both are refactored.
- Extract shared code only where it removes real duplication without hiding cadence-specific behavior.

### Stage 5: Shared Domain/Data Cleanup

- Consolidate repeated finalize/reset/snapshot/item-mapping patterns when the Daily and Weekly shape is stable.
- Introduce common time boundaries so date and clock behavior is testable.
- Keep repository interfaces focused on domain behavior, not Room convenience.
- Replace broad "shared routine" thinking with capability boundaries such as template, tracking, snapshot, reminders, reflection, and background work.

### Stage 6: Incremental Module Split

- Start with a low-risk module such as `:core:time`, `:core:ui`, or the smallest stable `:libs:*` capability API.
- Move one stable boundary per review chunk.
- Keep tests passing after each module move.
- Move UI feature modules after their required lib APIs, shared UI dependencies, and navigation contracts are clean enough.
- Move WorkManager code out of `:app` before introducing new background jobs.

### Stage 7: Remaining Feature Passes

- Apply the established patterns to History, History Detail, Action Editor, Share, Settings, and WorkManager snapshot flows.
- Keep each flow reviewable on its own.

### Stage 8: Final Rules And Enforcement

- Update rules with examples from the refactored code.
- Add build/test guidance for future AI-agent work.
- Consider adding static checks only after the desired patterns have settled.

## Review Chunk Rules

- Each chunk should have one primary purpose.
- Each chunk should identify the affected flow or boundary.
- Avoid mixing module moves, behavior changes, UI changes, and cleanup in the same chunk.
- Prefer small diffs that can be read in one sitting.
- Use examples from this repository when explaining changes.
- Mark general examples clearly when they are not taken from this codebase.

## Snapshot Policy

- Paparazzi snapshots are regression guardrails.
- Run verification when UI-rendered code changes.
- Do not run snapshot recording commands unless explicitly approved.
- Do not update existing snapshot images unless explicitly approved.
- If a visual change is intentionally approved later, record only the approved snapshot set.

## Reference Guidance

- Android modularization guidance: modules should be loosely coupled, self-contained, and responsible for a clear purpose.
- Android common modularization patterns: feature modules usually represent screens or flows and depend on data/business modules; data modules encapsulate data and business logic for a domain and hide data sources.
- Android dependency inversion guidance: consumers should depend on APIs/abstractions, while implementations are supplied externally, usually through app-level dependency injection.
- Now in Android sample: app, feature, and core/library modules form an acyclic dependency graph, and library modules may depend on other library modules when the dependency direction is clear.
- Kotlin-first guidance: prefer Kotlin-based APIs and patterns when they meet the need; use Java-based APIs only when they are simpler, required for Android interop, or provide missing capabilities.
