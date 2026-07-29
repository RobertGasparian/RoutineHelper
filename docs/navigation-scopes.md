# Navigation scopes

RoutineHelper uses explicit lifetimes for presentation state. Choose the narrowest lifetime that
survives every screen that participates in the workflow.

## Scope hierarchy

1. **Composition scope**
   - Use `remember` for UI mechanics that may be discarded when the composable leaves composition.
   - Use `rememberSaveable` for small UI state that should survive recreation while its destination
     remains saveable.
2. **Navigation-entry scope**
   - The default owner for a screen ViewModel.
   - `NavigationFlowViewModelStoreNavEntryDecorator` creates a separate `ViewModelStore` for every
     entry and clears it when that entry is popped.
3. **Navigation-flow scope**
   - State shared by a parent entry and one or more child entries.
   - A child declares the real parent entry's stable `contentKey` with
     `NavigationFlowScope.parent(...)`.
   - The child keeps its own entry-local owner and receives the parent owner through
     `LocalNavigationFlowViewModelStoreOwner`.
   - The parent entry must remain on the same back stack for the entire flow. Popping the parent
     clears the shared ViewModel store.
4. **App scope**
   - Use a root ViewModel or singleton coordinator only for workflows that intentionally survive
     all navigation changes.
5. **Durable state**
   - Room and DataStore remain the source of truth for business data.
   - A ViewModel survives configuration changes, not process death. Use `SavedStateHandle` only for
     small restorable workflow state and persist durable decisions through business APIs.

## Flow keys

Flow-owner content keys must be stable, unique for concurrently active entries, and saveable by
Android. Derive them from stable identity, not display text or one-time actions.

Current owner keys are:

- `routine:daily`
- `routine:weekly`
- `history:detail:{snapshotId}`

`HistoryDetailInitialAction` is deliberately excluded from the History detail content key. It is a
one-time entry request, not part of the logical destination identity.

A child must never reference a made-up owner key. The corresponding parent entry must exist below
the child in the back stack; otherwise no entry owns cleanup of the shared store.

## Reflection flow

Reflection is the first navigation-flow-scoped feature:

1. Daily, Weekly, or History detail obtains a `ReflectionEditorSessionViewModel` from its local
   entry owner.
2. The client initializes the session with its current summary and pushes
   `ReflectionEditorDestination(parentContentKey)`.
3. The editor is rendered by `BottomSheetSceneStrategy` as an overlay. It obtains the same
   `ReflectionEditorSessionViewModel` from `LocalNavigationFlowViewModelStoreOwner`.
   `BottomSheetSceneStrategy` also provides a sticky `LocalBottomSheetPresentationState`; the
   editor waits for `Presented` before requesting focus so the sheet and IME enter sequentially
   without relying on a fixed delay.
4. Reflection owns the draft and emits an explicit `ReflectionEditorSaveRequest`.
5. The still-alive client screen consumes the request and sends a focused save intent to its own
   ViewModel. Reflection never imports a Daily, Weekly, or History ViewModel or business use case.
6. Dismissing without Save produces no request. Opening a new session always replaces any old
   unsaved draft.

The shared contract lives in `:features:reflection-api`; the ViewModel and editor UI live in
`:features:reflection`. Client features depend only on the API module, while `:app` composes the
implementations and navigation entries.

Bottom-sheet presentation state is composition-scoped UI mechanics, not shared workflow or
business state. It reports only that the scene's initial opening animation has settled and must not
be moved into the flow-scoped ViewModel.

History reminder deep links synthesize:

`HistoryDestination -> HistoryDetailDestination -> ReflectionEditorDestination`

The parent entry loads the persisted summary and initializes the same session used by a manual
open. Link origin does not change the editor or Back behavior.

## Multi-step flows

For onboarding, signup, or another multi-screen setup, keep a stable flow-root entry on the back
stack and point every step at its content key. A flow-specific ViewModel may own custom state
holders or coordinators and release them from `onCleared()`.

Do not add an Activity-scoped ViewModel merely because several screens need the same state. Do not
store callbacks, repositories, or large UI state in destinations or `SavedStateHandle`. Add a
custom non-ViewModel decorator store only after a concrete workflow cannot be modeled cleanly by a
flow-scoped ViewModel.

The implementation follows Navigation 3's
[shared ViewModel recipe](https://developer.android.com/guide/navigation/navigation-3/recipes/sharedviewmodel),
[entry decorator guidance](https://developer.android.com/guide/navigation/navigation-3/naventrydecorators),
and [bottom-sheet recipe](https://developer.android.com/guide/navigation/navigation-3/recipes/bottomsheet).
