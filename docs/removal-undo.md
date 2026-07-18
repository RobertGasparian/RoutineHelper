# Removal Undo Workflow

This document is the source of truth for recoverable removal from Current List, Daily, and Weekly.
Read it before changing delete gestures, snackbar behavior, pending-removal storage, template
ordering, or app-start cleanup.

The central rule is:

> A removed item stays in its original source of truth until the undo window expires. The UI hides
> it; undo clears the pending marker; timeout performs the real delete.

## User-visible policy

Only one removal source can own the four-second undo window at a time. The three sources are:

- Current List
- Daily
- Weekly

Items from the active source can be removed into the same batch. Each same-source removal resets
the four-second timer. Delete gestures from the other two sources are disabled until the active
batch is undone or finalized. Tab navigation and non-delete interactions remain available.

| Current state | User action | Result |
| --- | --- | --- |
| Idle | Remove a Daily action | Daily owns the window; snackbar appears. |
| Daily owns window | Remove another Daily action | Item joins the batch; timer resets. |
| Daily owns window | Try to remove Weekly or Current List | Delete interaction is disabled; coordinator also rejects the request. |
| Any active batch | Undo latest | Most recently removed item is restored; timer resets if items remain. |
| Any active batch | Undo all | Entire batch is restored; all sources unlock. |
| Any active batch | Timer expires | Batch is permanently deleted; all sources unlock. |

The root snackbar identifies the source and count, for example `2 Daily actions removed`. It is
hosted by the app shell, so switching tabs does not hide or replace it. `Undo latest` and `Undo all`
remain explicit separate actions because a standard Material snackbar supports only one action.

## Ownership and file map

| Boundary | Responsibility |
| --- | --- |
| `:libs:routine:removal:domain` | Source lock, pending ID batch, timer, undo/finalize ordering, and public coordinator state. |
| `:features:removal-undo` | Activity/root-scoped ViewModel, source-aware UI state, custom snackbar renderer, and coordinator DI scope/binding. |
| `:libs:routine:current-list:*` | Current List pending marker, visible query, restore/delete commands, and hidden-slot reorder planning. |
| `:libs:routine:template:*` | Daily/Weekly pending marker, cadence-guarded restore/delete commands, action cleanup, and hidden-slot reorder planning. |
| `:features:current-list` | Sends Current List removal requests and disables destructive UI while another source owns the window. |
| `:features:routine-tracking` | Sends explicit Daily or Weekly requests and disables the corresponding delete gesture while another source owns the window. |
| `:app` | Hosts the snackbar and invokes dangling-pending cleanup at process start. |

Feature ViewModels depend on `RoutineRemovalUndoCoordinator`, not on one another and not on Room.
The coordinator is a business orchestrator rather than shared UI, so it does not belong in
`:core:ui` or `:app`.

## Durable storage

Current List stores `pendingRemovalAtMillis` on `CurrentListItemEntity`. Daily and Weekly store the
same marker on `RoutineItemEntity`.

Visible list queries filter out pending rows. Marking a template item pending does **not** delete:

- its `ActionEntity`;
- its `RoutineItemEntity` identity, cadence, or position;
- its Daily/Weekly tracking entries, including check, count, hidden, and note state.

This is why undo can restore the original item exactly. Deleting and reinserting would create a new
routine item ID and lose or disconnect tracking state through foreign-key cascades.

On timeout, template deletion runs in one Room transaction: it snapshots the pending routine rows,
deletes those rows, and then deletes their owning actions. Tracking entries are removed by their
existing cascade only at this final step.

All cadence-specific template updates carry Daily or Weekly through the repository and DAO query.
An ID from the wrong cadence must not be marked, restored, reordered, or finalized by that command.

## Ordering while an item is hidden

A pending row continues reserving its persisted position. Reordering operates only on visible IDs
and fills the visible slots around every pending row. The pending row itself does not move.

Example:

```text
stored:        A, [pending B], C, D
visible order: D, A, C
persisted:     D, [pending B], A, C
undo B:        D, B, A, C
```

Without this planner, reordering visible rows to consecutive positions would collide with or move
the hidden row, and undo would restore it into an unexpected location. Keep this rule in pure domain
planners with tests; do not rebuild it ad hoc in a repository loop or UI state.

Prepending shifts every stored row in that source or cadence, including pending rows, one position
before inserting the new item at position zero. This keeps the new item first without compacting or
moving a pending row relative to the items around it; undo restores the hidden item after the new
item in the expected durable order.

## Serialization and timer rules

`DefaultRoutineRemovalUndoCoordinator` owns one mutex around mark-pending, restore, timeout
finalization, Current List clear, and startup cleanup. The mutex is not optional: a timeout that
runs while undo is restoring could otherwise permanently delete the same row.

The active source is claimed inside that serialized request before the public state is published.
The UI lock is the user-facing guard; the coordinator check is the authoritative race-safe guard.

The batch stores IDs in removal order. Re-removing an existing ID moves it to the end, so
`Undo latest` always refers to the latest request. Every accepted same-source request and every
partial undo restarts the full four-second window.

Do not create one timer per item. Do not let feature ViewModels own separate timers. Either change
would allow overlapping sources and make timeout/undo ordering ambiguous.

## Swipe-to-reveal interaction

`RoutineSwipeToReveal` dispatches the removal intent immediately when the exposed action is tapped.
It does not add an off-screen foreground exit; the containing Lazy item's disappearance and
neighbor movement are the only delete animation. A row is enabled only when coordinator state
allows its source. When another source takes the lock, any revealed row is closed and horizontal
reveal is disabled.

Keep both layers of enforcement:

- UI gating prevents the delete action from starting for a request that another source already owns.
- Coordinator gating protects against stale presentation state or concurrent callers.

Do not infer the source from a screen title or another display string. Daily and Weekly ViewModels
pass explicit `RoutineRemovalSource` values and expose a derived boolean to their shared renderer.

## Process death and startup

The timer and in-memory batch do not survive process death. On the next application start,
`finalizeDanglingPendingRemovalsOnLaunch` permanently deletes all pending Current List and template
rows before a new undo workflow begins. This matches the existing Current List policy and prevents
invisible pending rows from remaining indefinitely.

If product behavior later requires undo across process death, persist the batch source, ID order,
and deadline as a real workflow. Do not simply restore every dangling row on startup; that changes
the meaning of a removal the user already confirmed by leaving it past an unknown deadline.

## Pitfalls not to reintroduce

- Do not delete Daily/Weekly routine rows before the undo deadline; their tracking entries cascade.
- Do not reinsert an item on undo; identity, history, and exact position must be preserved.
- Do not let Current List, Daily, and Weekly run independent snackbar timers.
- Do not allow a cross-source request to replace the active snackbar or silently finalize its batch.
- Do not compact visible positions across a pending row.
- Do not keep the source lock only in Compose state; the coordinator must reject cross-source races.
- Do not hide or block tab navigation while a batch is pending.
- Do not update Paparazzi baselines for the source-aware snackbar without explicit approval.

## Regression checklist

- Remove one item from each source and verify the correct source-specific snackbar copy.
- Remove two items from the same source and verify one batch, count two, and a reset timer.
- Switch tabs during a pending batch; verify snackbar persistence and disabled delete gestures on
  the other sources.
- Undo latest, then undo all; verify item identity, tracking state, and positions.
- Let timeout expire; verify the routine item, owning action, and tracking entry are deleted.
- Reorder around a hidden pending item, then undo; verify the pending slot was preserved.
- Kill the process with a pending item, relaunch, and verify startup finalizes the dangling row.
- Race undo against the timeout in unit tests; verify a restored item is never finalized.
