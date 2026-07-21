package com.robertgasparian.routinehelper.ui.history

import android.content.Context
import com.robertgasparian.routinehelper.core.time.TimeProvider
import com.robertgasparian.routinehelper.domain.model.RoutineCadence
import com.robertgasparian.routinehelper.domain.model.RoutineSnapshot
import com.robertgasparian.routinehelper.domain.model.RoutineSnapshotItem
import com.robertgasparian.routinehelper.features.history.R
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import javax.inject.Inject
import javax.inject.Singleton

interface HistoryTextProvider {
    fun finalizedTime(finalizedAtMillis: Long): String

    fun snapshotShareText(snapshot: RoutineSnapshot): String

    fun snapshotsShareText(snapshots: List<RoutineSnapshot>): String

    fun snapshotsFileMessage(snapshots: List<RoutineSnapshot>): String

    fun snapshotFileMessage(snapshot: RoutineSnapshot): String

    fun snapshotsFileName(): String

    fun snapshotFileName(snapshot: RoutineSnapshot): String
}

@Singleton
class AndroidHistoryTextProvider @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val timeProvider: TimeProvider,
) : HistoryTextProvider {
    override fun finalizedTime(finalizedAtMillis: Long): String =
        DateTimeFormatter
            .ofLocalizedTime(FormatStyle.SHORT)
            .withLocale(context.resources.configuration.locales[0])
            .withZone(timeProvider.now().zone)
            .format(Instant.ofEpochMilli(finalizedAtMillis))

    override fun snapshotShareText(snapshot: RoutineSnapshot): String {
        return buildString {
            appendLine(
                context.getString(
                    if (snapshot.cadence == RoutineCadence.Weekly) {
                        R.string.history_export_weekly_snapshot_title
                    } else {
                        R.string.history_export_daily_snapshot_title
                    },
                ),
            )
            appendLine(
                context.getString(
                    if (snapshot.cadence == RoutineCadence.Weekly) {
                        R.string.history_export_weekly_date
                    } else {
                        R.string.history_export_daily_date
                    },
                    snapshot.periodStartDate,
                ),
            )
            appendLine(
                context.getString(
                    R.string.history_export_finalized,
                    finalizedTime(snapshot.finalizedAtMillis),
                ),
            )
            appendLine()

            snapshot.summaryNote
                ?.takeIf(String::isNotBlank)
                ?.let { summaryNote ->
                    appendLine(context.getString(R.string.history_export_summary_note))
                    appendLine(summaryNote)
                    appendLine()
                }

            if (snapshot.items.isEmpty()) {
                appendLine(
                    context.getString(
                        if (snapshot.cadence == RoutineCadence.Weekly) {
                            R.string.history_export_weekly_empty
                        } else {
                            R.string.history_export_daily_empty
                        },
                    ),
                )
                return@buildString
            }

            snapshot.items
                .sortedBy(RoutineSnapshotItem::position)
                .forEachIndexed { index, item ->
                    val statusMarker = context.getString(
                        when {
                            item.isHidden -> R.string.history_export_skipped_marker
                            item.isChecked -> R.string.history_export_checked_marker
                            else -> R.string.history_export_unchecked_marker
                        },
                    )
                    appendLine(
                        context.getString(
                            R.string.history_export_item,
                            index + 1,
                            statusMarker,
                            item.title,
                        ),
                    )
                    item.repeatTargetCount?.let { repeatTargetCount ->
                        append(ShareIndent)
                        appendLine(
                            context.getString(
                                R.string.history_export_count,
                                item.completedCount,
                                repeatTargetCount,
                            ),
                        )
                    }
                    item.description?.takeIf(String::isNotBlank)?.let { description ->
                        append(ShareIndent)
                        appendLine(context.getString(R.string.history_export_description, description))
                    }
                    item.note?.takeIf(String::isNotBlank)?.let { note ->
                        append(ShareIndent)
                        appendLine(context.getString(R.string.history_export_note, note))
                    }
                    if (index < snapshot.items.lastIndex) appendLine()
                }
        }.trimEnd()
    }

    override fun snapshotsShareText(snapshots: List<RoutineSnapshot>): String =
        snapshots
            .sortedWith(
                compareByDescending<RoutineSnapshot> { snapshot -> snapshot.periodStartDate }
                    .thenByDescending { snapshot -> snapshot.finalizedAtMillis },
            )
            .joinToString(separator = SnapshotSeparator, transform = ::snapshotShareText)

    override fun snapshotsFileMessage(snapshots: List<RoutineSnapshot>): String {
        val dates = snapshots.map(RoutineSnapshot::periodStartDate).distinct().sorted()
        return when (dates.size) {
            0 -> context.getString(R.string.history_share_snapshots_message_empty)
            1 -> context.getString(R.string.history_share_snapshots_message_single, dates.first())
            else -> context.getString(R.string.history_share_snapshots_message_range, dates.first(), dates.last())
        }
    }

    override fun snapshotFileMessage(snapshot: RoutineSnapshot): String =
        context.getString(
            if (snapshot.cadence == RoutineCadence.Weekly) {
                R.string.history_share_weekly_snapshot_message
            } else {
                R.string.history_share_daily_snapshot_message
            },
            snapshot.periodStartDate,
        )

    override fun snapshotsFileName(): String =
        context.getString(R.string.history_snapshots_file_name)

    override fun snapshotFileName(snapshot: RoutineSnapshot): String =
        context.getString(R.string.history_snapshot_file_name, snapshot.periodStartDate)
}

@Module
@InstallIn(SingletonComponent::class)
abstract class HistoryTextProviderModule {
    @Binds
    abstract fun bindHistoryTextProvider(
        provider: AndroidHistoryTextProvider,
    ): HistoryTextProvider
}

private const val ShareIndent = "   "
private const val SnapshotSeparator = "\n\n---\n\n"
