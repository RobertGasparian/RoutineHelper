package com.robertgasparian.routinehelper.domain.formatter

import com.robertgasparian.routinehelper.domain.model.CurrentListItem
import javax.inject.Inject

class CurrentListShareTextFormatter @Inject constructor() {
    operator fun invoke(items: List<CurrentListItem>): String =
        buildString {
            appendLine("Current list")
            appendLine()

            if (items.isEmpty()) {
                appendLine("No items.")
                return@buildString
            }

            items
                .sortedBy(CurrentListItem::position)
                .forEachIndexed { index, item ->
                    appendLine("${index + 1}. ${item.statusLabel} ${item.title}")
                    item.description
                        ?.takeIf(String::isNotBlank)
                        ?.let { description -> appendLine("   Description: $description") }
                    if (index < items.lastIndex) appendLine()
                }
        }.trimEnd()

    private val CurrentListItem.statusLabel: String
        get() = if (isChecked) "[x]" else "[ ]"
}
