package com.robertgasparian.routinehelper.ui.app

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.robertgasparian.routinehelper.ui.currentlist.undo.CurrentListUndoIntent
import com.robertgasparian.routinehelper.ui.currentlist.undo.CurrentListUndoSnackbarHost
import com.robertgasparian.routinehelper.ui.currentlist.undo.CurrentListUndoUiState
import com.robertgasparian.routinehelper.ui.currentlist.undo.CurrentListUndoViewModel
import com.robertgasparian.routinehelper.ui.share.shareText
import com.robertgasparian.routinehelper.ui.theme.RoutineHelperTheme

@Composable
fun RoutineHelperScreen(
    currentListUndoViewModel: CurrentListUndoViewModel = hiltViewModel<CurrentListUndoViewModel>(),
) {
    val topLevelBackStack = rememberSaveable(saver = RoutineDestinationBackStackSaver) {
        TopLevelBackStack<RoutineDestination>(DailyDestination)
    }
    val currentListUndoUiState by currentListUndoViewModel.uiState.collectAsStateWithLifecycle()

    RoutineHelperComponent(
        topLevelBackStack = topLevelBackStack,
        currentListUndoUiState = currentListUndoUiState,
        onCurrentListUndoIntent = currentListUndoViewModel::onIntent,
    )
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun RoutineHelperComponent(
    topLevelBackStack: TopLevelBackStack<RoutineDestination>,
    modifier: Modifier = Modifier,
    currentListUndoUiState: CurrentListUndoUiState = CurrentListUndoUiState(),
    onCurrentListUndoIntent: (CurrentListUndoIntent) -> Unit = {},
) {
    val currentDestination = topLevelBackStack.backStack.lastOrNull()
    val context = LocalContext.current
    val showBottomNavigation = currentDestination is TopLevelDestination
    var navigationTransitionDirection by remember { mutableStateOf(HorizontalDirection.Right) }

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
            CurrentListUndoSnackbarHost(
                uiState = currentListUndoUiState,
                onIntent = onCurrentListUndoIntent,
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
