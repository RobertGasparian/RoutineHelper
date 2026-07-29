package com.robertgasparian.routinehelper.ui.app

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.robertgasparian.routinehelper.R
import com.robertgasparian.routinehelper.core.navigation.BottomSheetSceneStrategy
import com.robertgasparian.routinehelper.core.navigation.NavigationFlowScope
import com.robertgasparian.routinehelper.core.navigation.rememberNavigationFlowViewModelStoreNavEntryDecorator
import com.robertgasparian.routinehelper.domain.model.RoutineCadence
import com.robertgasparian.routinehelper.ui.actioneditor.ActionEditorScreen
import com.robertgasparian.routinehelper.ui.currentlist.CurrentListScreen
import com.robertgasparian.routinehelper.ui.daily.DailyScreen
import com.robertgasparian.routinehelper.ui.history.HistoryScreen
import com.robertgasparian.routinehelper.ui.history.detail.HistoryDetailScreen
import com.robertgasparian.routinehelper.ui.reflection.ReflectionEditorScreen
import com.robertgasparian.routinehelper.ui.reflection.ReflectionEditorSessionViewModel
import com.robertgasparian.routinehelper.ui.settings.SettingsScreen
import com.robertgasparian.routinehelper.ui.share.ShareTextPreviewScreen
import com.robertgasparian.routinehelper.ui.weekly.WeeklyScreen

@Composable
internal fun RoutineNavGraph(
    backStack: List<RoutineDestination>,
    transitionDirection: HorizontalDirection,
    onNavigateToDetail: (RoutineDestination) -> Unit,
    onBack: () -> Boolean,
    onDebugSummaryNotificationClick: (snapshotId: Long) -> Unit,
    onShareText: (text: String, title: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val shareCurrentListTitle = stringResource(R.string.app_share_current_list)
    val shareRoutineSnapshotsTitle = stringResource(R.string.app_share_routine_snapshots)
    val shareRoutineSnapshotTitle = stringResource(R.string.app_share_routine_snapshot)
    val bottomSheetSceneStrategy = remember {
        BottomSheetSceneStrategy<RoutineDestination>()
    }

    NavDisplay(
        backStack = backStack,
        modifier = modifier.fillMaxSize(),
        onBack = { onBack() },
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberNavigationFlowViewModelStoreNavEntryDecorator(),
        ),
        sceneStrategies = listOf(bottomSheetSceneStrategy),
        transitionSpec = {
            horizontalSlideContentTransform(direction = transitionDirection)
        },
        popTransitionSpec = {
            horizontalSlideContentTransform(direction = transitionDirection)
        },
        entryProvider = entryProvider {
            entry<DailyDestination>(
                clazzContentKey = { DailyNavigationContentKey },
            ) {
                val reflectionEditorSession = hiltViewModel<ReflectionEditorSessionViewModel>()
                DailyScreen(
                    onCreateActionClick = {
                        onNavigateToDetail(ActionEditorDestination())
                    },
                    onEditActionClick = { actionId ->
                        onNavigateToDetail(ActionEditorDestination(actionId))
                    },
                    onSummaryEditorClick = {
                        onNavigateToDetail(
                            ReflectionEditorDestination(DailyNavigationContentKey),
                        )
                    },
                    onSettingsClick = {
                        onNavigateToDetail(SettingsDestination)
                    },
                    reflectionEditorSession = reflectionEditorSession,
                )
            }

            entry<WeeklyDestination>(
                clazzContentKey = { WeeklyNavigationContentKey },
            ) {
                val reflectionEditorSession = hiltViewModel<ReflectionEditorSessionViewModel>()
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
                    onSummaryEditorClick = {
                        onNavigateToDetail(
                            ReflectionEditorDestination(WeeklyNavigationContentKey),
                        )
                    },
                    onSettingsClick = {
                        onNavigateToDetail(SettingsDestination)
                    },
                    reflectionEditorSession = reflectionEditorSession,
                )
            }

            entry<CurrentListDestination> {
                CurrentListScreen(
                    onShareTextPreviewClick = { text ->
                        onNavigateToDetail(
                            ShareTextPreviewDestination(
                                initialText = text,
                                shareTitle = shareCurrentListTitle,
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
                                shareTitle = shareRoutineSnapshotsTitle,
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

            entry<HistoryDetailDestination>(
                clazzContentKey = { destination ->
                    historyDetailNavigationContentKey(destination.snapshotId)
                },
            ) { destination ->
                val parentContentKey = historyDetailNavigationContentKey(destination.snapshotId)
                val reflectionEditorSession = hiltViewModel<ReflectionEditorSessionViewModel>()
                HistoryDetailScreen(
                    snapshotId = destination.snapshotId,
                    initialAction = destination.initialAction,
                    onBackClick = { onBack() },
                    onSummaryEditorClick = {
                        onNavigateToDetail(
                            ReflectionEditorDestination(parentContentKey),
                        )
                    },
                    onInitialSummaryEditorUnavailable = {
                        onBack()
                    },
                    reflectionEditorSession = reflectionEditorSession,
                    onDebugSummaryNotificationClick = {
                        onDebugSummaryNotificationClick(destination.snapshotId)
                    },
                    onShareTextPreviewClick = { text ->
                        onNavigateToDetail(
                            ShareTextPreviewDestination(
                                initialText = text,
                                shareTitle = shareRoutineSnapshotTitle,
                            ),
                        )
                    },
                )
            }

            entry<ReflectionEditorDestination>(
                metadata = { destination ->
                    BottomSheetSceneStrategy.bottomSheet(
                        skipPartiallyExpanded = true,
                    ) + NavigationFlowScope.parent(destination.parentContentKey)
                },
            ) {
                ReflectionEditorScreen(
                    onDismiss = { onBack() },
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
