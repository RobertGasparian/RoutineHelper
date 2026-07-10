package com.robertgasparian.routinehelper.ui.app

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.robertgasparian.routinehelper.domain.model.RoutineCadence
import com.robertgasparian.routinehelper.ui.actioneditor.ActionEditorScreen
import com.robertgasparian.routinehelper.ui.currentlist.CurrentListScreen
import com.robertgasparian.routinehelper.ui.daily.DailyScreen
import com.robertgasparian.routinehelper.ui.history.HistoryScreen
import com.robertgasparian.routinehelper.ui.history.detail.HistoryDetailScreen
import com.robertgasparian.routinehelper.ui.settings.SettingsScreen
import com.robertgasparian.routinehelper.ui.share.ShareTextPreviewScreen
import com.robertgasparian.routinehelper.ui.weekly.WeeklyScreen

@Composable
internal fun RoutineNavGraph(
    backStack: List<RoutineDestination>,
    transitionDirection: HorizontalDirection,
    onNavigateToDetail: (RoutineDestination) -> Unit,
    onBack: () -> Boolean,
    onShareText: (text: String, title: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    NavDisplay(
        backStack = backStack,
        modifier = modifier.fillMaxSize(),
        onBack = { onBack() },
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        ),
        transitionSpec = {
            horizontalSlideContentTransform(direction = transitionDirection)
        },
        popTransitionSpec = {
            horizontalSlideContentTransform(direction = transitionDirection)
        },
        entryProvider = entryProvider {
            entry<DailyDestination> {
                DailyScreen(
                    onCreateActionClick = {
                        onNavigateToDetail(ActionEditorDestination())
                    },
                    onEditActionClick = { actionId ->
                        onNavigateToDetail(ActionEditorDestination(actionId))
                    },
                    onSettingsClick = {
                        onNavigateToDetail(SettingsDestination)
                    },
                )
            }

            entry<WeeklyDestination> {
                WeeklyScreen(
                    onCreateActionClick = {
                        onNavigateToDetail(
                            ActionEditorDestination(cadence = RoutineCadence.Weekly),
                        )
                    },
                    onEditActionClick = { actionId ->
                        onNavigateToDetail(
                            ActionEditorDestination(
                                actionId = actionId,
                                cadence = RoutineCadence.Weekly,
                            ),
                        )
                    },
                    onSettingsClick = {
                        onNavigateToDetail(SettingsDestination)
                    },
                )
            }

            entry<CurrentListDestination> {
                CurrentListScreen(
                    onShareTextPreviewClick = { text ->
                        onNavigateToDetail(
                            ShareTextPreviewDestination(
                                initialText = text,
                                shareTitle = "Share current list",
                            ),
                        )
                    },
                    onSettingsClick = {
                        onNavigateToDetail(SettingsDestination)
                    },
                )
            }

            entry<HistoryDestination> {
                HistoryScreen(
                    onSnapshotClick = { snapshotId ->
                        onNavigateToDetail(HistoryDetailDestination(snapshotId))
                    },
                    onShareTextPreviewClick = { text ->
                        onNavigateToDetail(
                            ShareTextPreviewDestination(
                                initialText = text,
                                shareTitle = "Share routine snapshots",
                            ),
                        )
                    },
                    onSettingsClick = {
                        onNavigateToDetail(SettingsDestination)
                    },
                )
            }

            entry<SettingsDestination> {
                SettingsScreen(
                    onBackClick = { onBack() },
                )
            }

            entry<HistoryDetailDestination> { destination ->
                HistoryDetailScreen(
                    snapshotId = destination.snapshotId,
                    onBackClick = { onBack() },
                    onShareTextPreviewClick = { text ->
                        onNavigateToDetail(
                            ShareTextPreviewDestination(
                                initialText = text,
                                shareTitle = "Share routine snapshot",
                            ),
                        )
                    },
                )
            }

            entry<ActionEditorDestination> { destination ->
                ActionEditorScreen(
                    actionId = destination.actionId,
                    cadence = destination.cadence,
                    onBackClick = { onBack() },
                )
            }

            entry<ShareTextPreviewDestination> { destination ->
                ShareTextPreviewScreen(
                    initialText = destination.initialText,
                    onBackClick = { onBack() },
                    onShareClick = { text ->
                        onShareText(text, destination.shareTitle)
                        onBack()
                    },
                )
            }
        },
    )
}
