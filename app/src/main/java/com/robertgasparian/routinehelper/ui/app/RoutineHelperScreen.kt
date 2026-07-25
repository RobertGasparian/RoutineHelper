package com.robertgasparian.routinehelper.ui.app

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.robertgasparian.routinehelper.notification.RoutineSummaryReminderNotificationPublisher
import com.robertgasparian.routinehelper.ui.app.deeplink.RoutineNavigationCommand
import com.robertgasparian.routinehelper.ui.removalundo.RoutineRemovalUndoIntent
import com.robertgasparian.routinehelper.ui.removalundo.RoutineRemovalUndoSnackbarHost
import com.robertgasparian.routinehelper.ui.removalundo.RoutineRemovalUndoUiState
import com.robertgasparian.routinehelper.ui.removalundo.RoutineRemovalUndoViewModel
import com.robertgasparian.routinehelper.ui.share.shareText
import com.robertgasparian.routinehelper.ui.theme.RoutineHelperTheme

@Composable
internal fun RoutineHelperScreen(
    navigationRequestId: Long? = null,
    navigationCommand: RoutineNavigationCommand? = null,
    onNavigationCommandConsumed: () -> Unit = {},
    routineRemovalUndoViewModel: RoutineRemovalUndoViewModel = hiltViewModel<RoutineRemovalUndoViewModel>(),
) {
    val topLevelBackStack = rememberSaveable(saver = RoutineDestinationBackStackSaver) {
        TopLevelBackStack<RoutineDestination>(DailyDestination)
    }
    val routineRemovalUndoUiState by routineRemovalUndoViewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(navigationRequestId) {
        val command = navigationCommand ?: return@LaunchedEffect
        topLevelBackStack.replaceWithTopLevelPath(
            topLevelKey = command.topLevelDestination,
            nestedKeys = command.nestedDestinations,
        )
        onNavigationCommandConsumed()
    }

    RoutineHelperComponent(
        topLevelBackStack = topLevelBackStack,
        routineRemovalUndoUiState = routineRemovalUndoUiState,
        onRoutineRemovalUndoIntent = routineRemovalUndoViewModel::onIntent,
    )
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun RoutineHelperComponent(
    topLevelBackStack: TopLevelBackStack<RoutineDestination>,
    modifier: Modifier = Modifier,
    routineRemovalUndoUiState: RoutineRemovalUndoUiState = RoutineRemovalUndoUiState(),
    onRoutineRemovalUndoIntent: (RoutineRemovalUndoIntent) -> Unit = {},
) {
    val currentDestination = topLevelBackStack.backStack.lastOrNull()
    val context = LocalContext.current
    val summaryReminderNotificationPublisher = remember(context) {
        RoutineSummaryReminderNotificationPublisher(context)
    }
    var pendingDebugNotificationSnapshotId by rememberSaveable {
        mutableStateOf<Long?>(null)
    }
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { isGranted ->
        val snapshotId = pendingDebugNotificationSnapshotId
        pendingDebugNotificationSnapshotId = null
        if (isGranted && snapshotId != null) {
            summaryReminderNotificationPublisher.showSummaryEditorReminder(snapshotId)
        }
    }
    val showBottomNavigation = currentDestination is TopLevelDestination
    var navigationTransitionDirection by remember { mutableStateOf(HorizontalDirection.Right) }

    fun publishDebugSummaryNotification(snapshotId: Long) {
        if (
            Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            summaryReminderNotificationPublisher.showSummaryEditorReminder(snapshotId)
        } else {
            pendingDebugNotificationSnapshotId = snapshotId
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    fun navigateToTopLevel(destination: TopLevelDestination) {
        if (destination == topLevelBackStack.topLevelKey) return

        val fromIndex = topLevelBackStack.topLevelKey.topLevelTabIndex ?: 0
        val toIndex = destination.topLevelTabIndex ?: fromIndex
        navigationTransitionDirection = if (toIndex > fromIndex) {
            HorizontalDirection.Left
        } else {
            HorizontalDirection.Right
        }
        topLevelBackStack.addTopLevel(destination)
    }

    fun navigateToDetail(destination: RoutineDestination) {
        navigationTransitionDirection = HorizontalDirection.Right
        topLevelBackStack.add(destination)
    }

    fun navigateBack(): Boolean {
        navigationTransitionDirection = HorizontalDirection.Left
        return topLevelBackStack.removeLast()
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = {
            RoutineRemovalUndoSnackbarHost(
                uiState = routineRemovalUndoUiState,
                onIntent = onRoutineRemovalUndoIntent,
            )
        },
        bottomBar = {
            AnimatedVisibility(
                visible = showBottomNavigation,
                enter = slideInVertically(initialOffsetY = { height -> height }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { height -> height }) + fadeOut(),
            ) {
                FloatingBottomNavigationBar(
                    selectedDestination = topLevelBackStack.topLevelKey,
                    onDestinationSelected = ::navigateToTopLevel,
                )
            }
        },
    ) { _ ->
        RoutineNavGraph(
            backStack = topLevelBackStack.backStack,
            transitionDirection = navigationTransitionDirection,
            onNavigateToDetail = ::navigateToDetail,
            onBack = { navigateBack() },
            onDebugSummaryNotificationClick = ::publishDebugSummaryNotification,
            onShareText = { text, title ->
                context.shareText(text = text, title = title)
            },
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun RoutineHelperComponentPreview() {
    RoutineHelperTheme {
        RoutineHelperComponent(
            topLevelBackStack = TopLevelBackStack<RoutineDestination>(DailyDestination),
        )
    }
}
