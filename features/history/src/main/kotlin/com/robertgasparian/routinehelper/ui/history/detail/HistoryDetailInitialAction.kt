package com.robertgasparian.routinehelper.ui.history.detail

/**
 * A one-time presentation action requested while opening a history detail destination.
 *
 * Keep this typed instead of adding route-specific booleans so future entry actions remain
 * explicit and invalid combinations cannot accumulate on the destination.
 */
enum class HistoryDetailInitialAction {
    OpenSummaryEditor,
}
