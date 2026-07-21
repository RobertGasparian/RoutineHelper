package com.robertgasparian.routinehelper.ui.currentlist

import android.content.Context
import com.robertgasparian.routinehelper.domain.model.CurrentListItem
import com.robertgasparian.routinehelper.features.currentlist.R
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject
import javax.inject.Singleton

interface CurrentListTextProvider {
    fun shareText(items: List<CurrentListItem>): String

    fun debugItemTitle(itemNumber: Int): String

    fun debugItemDescription(itemNumber: Int): String
}

@Singleton
class AndroidCurrentListTextProvider @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : CurrentListTextProvider {
    override fun debugItemTitle(itemNumber: Int): String =
        context.getString(R.string.current_list_debug_item_title, itemNumber)

    override fun debugItemDescription(itemNumber: Int): String =
        context.getString(R.string.current_list_debug_item_description, itemNumber)

    override fun shareText(items: List<CurrentListItem>): String =
        buildString {
            appendLine(context.getString(R.string.current_list_share_heading))
            appendLine()

            if (items.isEmpty()) {
                appendLine(context.getString(R.string.current_list_share_no_items))
                return@buildString
            }

            items
                .sortedBy(CurrentListItem::position)
                .forEachIndexed { index, item ->
                    val statusMarker = context.getString(
                        if (item.isChecked) {
                            R.string.current_list_share_checked_marker
                        } else {
                            R.string.current_list_share_unchecked_marker
                        },
                    )
                    appendLine(
                        context.getString(
                            R.string.current_list_share_item,
                            index + 1,
                            statusMarker,
                            item.title,
                        ),
                    )
                    item.description
                        ?.takeIf(String::isNotBlank)
                        ?.let { description ->
                            append(ShareIndent)
                            appendLine(context.getString(R.string.current_list_share_description, description))
                        }
                    if (index < items.lastIndex) appendLine()
                }
        }.trimEnd()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class CurrentListTextProviderModule {
    @Binds
    abstract fun bindCurrentListTextProvider(
        provider: AndroidCurrentListTextProvider,
    ): CurrentListTextProvider
}

private const val ShareIndent = "   "
