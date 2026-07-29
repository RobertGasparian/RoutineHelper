# Deep links

Deep-link delivery and in-app navigation are intentionally separate:

1. An entry point, such as an implicit Android link or an explicit notification intent, provides
   only its URI string to `RoutineDeepLinkRegistry`.
2. Exactly one multibound `RoutineDeepLinkHandler` must match the request. Malformed, unsupported,
   or ambiguous links are ignored.
3. The handler decodes an external, serializable link key and maps it to a
   `RoutineNavigationCommand`.
4. The command replaces the root back stack with the same top-level and nested path that manual
   navigation would produce.

`MainActivity` inspects every incoming intent, including intents received while Android restores an
existing task. Once a supported URI is resolved, `RoutineDeepLinkIntentConsumer` clears that URI
from the activity intent. This one-shot consumption prevents ordinary activity recreation from
replaying old navigation while still allowing a later intent, even one with the same URI, to be
handled normally.

This means a link's source never changes its back behavior. For example, the History summary link
creates `HistoryDestination -> HistoryDetailDestination -> ReflectionEditorDestination`. The first
Back dismisses the same Reflection sheet used by a manual edit; the next Back returns to the normal
History root.

External parsing keys must remain separate from `RoutineDestination` types. A URI is a public input
contract, while a destination is internal presentation state; coupling them would make URI changes
force unrelated destinations to become serializable and could expose presentation-only fields as
link parameters.

## Current links

`routinehelper://history/snapshots/{snapshotId}/summary/edit`

The snapshot ID must be positive. Opening the link selects History, opens that snapshot, and pushes
the Reflection editor as a child of the History detail flow scope. History loads and supplies the
initial summary through the shared session; Reflection does not load History data itself.

`routinehelper://routines/{cadence}`

Supported cadence values are `daily` and `weekly`. Opening the link replaces the root path with the
corresponding top-level destination, exactly like selecting that bottom-navigation tab. Summary
reminders use these links when the completed period had no actions and therefore has no History
snapshot.

The routine-tab links currently arrive through explicit notification intents, so they do not need
an Android manifest filter. Once their URI reaches `MainActivity`, they use the same registry and
back-stack mechanism as every other deep link.

## Adding a link

1. Add the canonical URI builder and pattern to `RoutineDeepLinks`.
2. Add a focused serializable parsing key and `RoutineDeepLinkHandler`.
3. Bind the handler into the Hilt set with `@IntoSet`.
4. Map it to the top-level and nested path that manual navigation would create.
5. Add a narrow manifest intent filter only when external implicit delivery is required.
6. Test valid, malformed, and unsupported input plus the expected Back path.

Do not branch on `Intent.action`, notification extras, or an explicit-versus-implicit flag after the
URI string reaches the registry.
