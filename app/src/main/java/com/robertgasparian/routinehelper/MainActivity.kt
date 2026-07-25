package com.robertgasparian.routinehelper

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import com.robertgasparian.routinehelper.ui.app.DailyDestination
import com.robertgasparian.routinehelper.ui.app.RoutineDestination
import com.robertgasparian.routinehelper.ui.app.RoutineHelperComponent
import com.robertgasparian.routinehelper.ui.app.RoutineHelperScreen
import com.robertgasparian.routinehelper.ui.app.TopLevelBackStack
import com.robertgasparian.routinehelper.ui.app.deeplink.RoutineDeepLinkRegistry
import com.robertgasparian.routinehelper.ui.app.deeplink.RoutineNavigationCommand
import com.robertgasparian.routinehelper.ui.theme.RoutineHelperTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    @Inject
    internal lateinit var deepLinkRegistry: RoutineDeepLinkRegistry

    private var pendingNavigationRequest by mutableStateOf<PendingNavigationRequest?>(null)
    private var nextNavigationRequestId = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            acceptDeepLink(intent)
        }

        setContent {
            RoutineHelperTheme {
                RoutineHelperApp(
                    navigationRequest = pendingNavigationRequest,
                    onNavigationRequestConsumed = {
                        pendingNavigationRequest = null
                    },
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        acceptDeepLink(intent)
    }

    private fun acceptDeepLink(intent: Intent) {
        val command = deepLinkRegistry.resolve(intent.dataString) ?: return
        pendingNavigationRequest = PendingNavigationRequest(
            id = ++nextNavigationRequestId,
            command = command,
        )
    }
}

@Composable
private fun RoutineHelperApp(
    navigationRequest: PendingNavigationRequest?,
    onNavigationRequestConsumed: () -> Unit,
) {
    RoutineHelperScreen(
        navigationRequestId = navigationRequest?.id,
        navigationCommand = navigationRequest?.command,
        onNavigationCommandConsumed = onNavigationRequestConsumed,
    )
}

private data class PendingNavigationRequest(
    val id: Long,
    val command: RoutineNavigationCommand,
)

@Preview(showBackground = true)
@Composable
private fun RoutineHelperAppPreview() {
    RoutineHelperTheme {
        RoutineHelperComponent(
            topLevelBackStack = TopLevelBackStack<RoutineDestination>(DailyDestination),
        )
    }
}
