# Architecture Refactor Plan

This plan records the staged refactor from the original single-module Android app to the current multimodule architecture. The refactor must preserve business behavior and visual UI output unless a specific change is explicitly approved.

The post-modularization class-by-class review is tracked in [`refactor-review-checklist.md`](refactor-review-checklist.md).

## Current Shape

- The project has 16 Gradle modules across `:app`, `:features:*`, `:libs:*`, and `:core:*`.
- `:app` is a thin Android shell. It owns `MainActivity`, `RoutineHelperApplication`, root Navigation 3 composition, app startup, and dependency aggregation.
- Daily and Weekly presentation live together in `:features:routine-tracking`, with cadence-specific ViewModels/mappers and neutral shared tracking state/components.
- History/detail/sharing, Action Editor, and Settings live in their own feature modules.
- Template, tracking, and snapshot capabilities each have separate `:domain` and `:data` modules.
- `:libs:routine:database` owns only Room database/schema/DAO composition. Capability data modules own repository implementations and their Hilt bindings.
- `:libs:background:work` owns WorkManager integration. Thin workers delegate structured flows to Daily and Weekly snapshot orchestrators, which compose focused use cases.
- `:core:time`, `:core:ui`, and `:core:testing` own cross-cutting time, design-system, and shared test infrastructure.
- ViewModels depend on use cases or presentation collaborators rather than repositories. Feature modules do not import Room, repository implementations, or workers.
- Repository, use-case, orchestrator, ViewModel, mapper, state-holder, and component snapshot coverage is distributed with the modules that own those behaviors.

## Completed Outcomes

- Module boundaries now enforce the intended feature/lib/core dependency direction.
- Repository and cross-cutting DI bindings live with their implementations; `:app` no longer constructs repositories or providers.
- Time-sensitive business and presentation code uses `TimeProvider`; direct system-clock access is isolated to the provider or debug-only UI tooling.
- Daily and Weekly follow the same MVI naming and screen/component/state/event structure while retaining cadence-specific names and behavior.
- Snapshot finalization uses explicit Daily and Weekly orchestrators rather than one broad use case.
- WorkManager code is outside `:app` and ready to coexist with future unrelated workers.
- Existing Paparazzi baselines remain unchanged.

## Remaining Targets

- Finish final dependency-visibility and build/test-command audits, then document the supported verification commands.
- Add Room migration tests when the schema first moves beyond version 1; there is no migration path to test yet.
- Keep existing debug-only snapshot/delete affordances gated and remove them when their replacement tooling or UX is ready.
- Defer visual cleanup and component redesign to the separately planned UI/UX overhaul.
- Create reflection, reminder, deadline, or monthly modules only when those capabilities are implemented and their boundaries are concrete.

## Target Module Direction

The target direction is implemented for current capabilities. Continue to add future modules only when a boundary is stable enough that the move makes the code easier to understand.

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

Current and planned module families:

- `:features:routine-tracking`
  - Daily and Weekly tracking screens, shared tracking components/state/events, cadence-specific ViewModels/mappers, previews, and Paparazzi tests.
  - Daily and Weekly remain packages inside one feature because they are cadence variants of the same presentation workflow and share substantial UI behavior.
- `:features:history`
  - Snapshot history list, snapshot detail, and the snapshot-sharing presentation flow they own today.
- `:features:action-editor`
  - Create/edit action UI flow.
- A separate share feature is not introduced while sharing is only part of History. If another feature later needs the same complete presentation flow, reevaluate the boundary without adding a feature-to-feature dependency.
- `:features:settings`
  - Settings UI and future settings state management.
- `:features:reflection`
  - Future reflection UI flows.
- `:libs:routine:template:domain`
  - Platform-independent template models, repository contracts, and use cases for reusable action definitions such as title, description, cadence, repeat target count, and future deadline configuration if deadlines are configured on actions.
- `:libs:routine:template:data`
  - Room-backed template entities, DAOs, relation models, and repository implementations.
- `:libs:routine:tracking:domain`
  - Platform-independent per-period models, repository contracts, and use cases for today/weekly entries, checked state, hidden state, notes, completed count, and future check availability or lock state.
- `:libs:routine:tracking:data`
  - Room-backed per-period entities, DAOs, and repository implementations.
  - It depends on template data for the routine definitions that tracked entries reference.
- `:libs:routine:snapshot:domain`
  - Platform-independent snapshot models, snapshot-period calculations, history repository contracts, finalization, queries, deletion, and share-text use cases.
- `:libs:routine:snapshot:data`
  - Room-backed snapshot entities, DAOs, relation models, and repository implementations.
- `:libs:routine:database`
  - Shared Room database composition, schema export, and future migrations.
  - It may depend on capability data modules to aggregate their entities and DAOs and provide the shared database/DAO bindings, but it must not own repositories or business rules.
  - Each capability data module owns the DI binding between its repository implementation and domain repository contract; `:app` only aggregates those modules.
- `:libs:reflection`
  - Future reflection business rules, data, and use cases.
- `:libs:reminder`
  - Future reminder scheduling intent, reminder settings, and reminder business rules.
- `:libs:background:work`
  - WorkManager workers, scheduling, constraints, and app-start/background orchestration.
  - Workers should orchestrate use cases from other libs; workers should not own routine/reflection/reminder business rules.
- `:core:time`
  - Date, clock, timezone abstractions, reusable calendar calculations, and the default application `TimeProvider` binding.
  - Generic calendar helpers such as calendar-week start belong here; capability modules own only business-specific period rules.
- `:core:ui`
  - Theme, design-system components from `ui.dsm`, reusable UI helpers, and shared component test helpers where appropriate.
- `:core:testing`
  - Shared coroutine test setup and test helpers used across feature modules; capability-specific fakes remain with their owning domain test fixtures.

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
- Repository implementation bindings live with their owning capability data module. Shared Room and DAO bindings live in `:libs:routine:database`; `:app` does not construct capability repositories.
- Cross-cutting implementation bindings live with their owning core module, while feature-specific presentation collaborator bindings live with their feature module.
- Reusable date/time helpers live in `:core:time`; feature or lib modules should not own generic calendar extensions just because they are the first caller.
- Shared code is extracted after the same pattern appears in at least two refactored flows, unless it is already clearly shared infrastructure.

Allowed examples:

- `:features:routine-tracking` depends on tracking, template, and snapshot domain APIs plus `:core:ui`.
- `:libs:routine:tracking:domain` depends on `:libs:routine:template:domain` APIs.
- `:libs:routine:snapshot:domain` depends on `:libs:routine:tracking:domain` APIs.
- `:libs:routine:database` depends on routine capability data modules for Room schema composition.
- `:libs:background:work` depends on routine, reflection, or reminder use-case APIs.

Disallowed examples:

- `:libs:routine:template:domain` depending on `:libs:routine:tracking:*`.
- `:libs:routine:tracking:domain` depending on `:libs:routine:snapshot:*`.
- `:libs:*` depending on `:features:daily` or any other feature implementation.
- One cadence package inside `:features:routine-tracking` depending on another cadence package's implementation.

## Staged Work Plan

### Stage 1: Architecture Plan And Rules (Complete)

- Update architecture and development rules.
- Define guardrails for Paparazzi, review chunks, debug code, and module boundaries.
- No production code movement.

### Stage 2: Safety Baseline (Ongoing Guardrail)

- Run unit tests.
- Run Paparazzi verification, not recording.
- Record any existing failures before refactoring.
- Do not update screenshots unless explicitly approved.

### Stage 3: Daily Reference Slice (Complete)

- Refactor Daily as the first vertical slice.
- Keep business behavior and visual output stable.
- Prefer state/event/ViewModel/use-case cleanup over UI polish.
- Add or adjust tests only for changed behavior or moved pure logic.
- If debug snapshot controls are touched, guard them behind debug build behavior.

### Stage 4: Weekly Mirror Slice (Complete)

- Apply the proven Daily pattern to Weekly.
- Compare Daily and Weekly after both are refactored.
- Extract shared code only where it removes real duplication without hiding cadence-specific behavior.

### Stage 5: Shared Domain/Data Cleanup (Complete For Current Capabilities)

- Consolidate repeated finalize/reset/snapshot/item-mapping patterns when the Daily and Weekly shape is stable.
- Introduce common time boundaries so date and clock behavior is testable.
- Keep repository interfaces focused on domain behavior, not Room convenience.
- Replace broad "shared routine" thinking with capability boundaries such as template, tracking, snapshot, reminders, reflection, and background work.

### Stage 6: Incremental Module Split (Complete For Current Capabilities)

- Start with a low-risk module such as `:core:time`, `:core:ui`, or the smallest stable `:libs:*` capability API.
- Move one stable boundary per review chunk.
- Keep tests passing after each module move.
- Move UI feature modules after their required lib APIs, shared UI dependencies, and navigation contracts are clean enough.
- Move WorkManager code out of `:app` before introducing new background jobs.

### Stage 7: Remaining Feature Passes (Complete For Current Features)

- Apply the established patterns to History, History Detail, Action Editor, Share, Settings, and WorkManager snapshot flows.
- Keep each flow reviewable on its own.

### Stage 8: Final Rules And Enforcement (In Progress)

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
