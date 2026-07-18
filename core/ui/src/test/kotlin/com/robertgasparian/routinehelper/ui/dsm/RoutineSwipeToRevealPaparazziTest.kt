package com.robertgasparian.routinehelper.ui.dsm

import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.robertgasparian.routinehelper.ui.theme.RoutineHelperTheme
import org.junit.Rule
import org.junit.Test

class RoutineSwipeToRevealPaparazziTest {
    @get:Rule
    val paparazzi = Paparazzi(
        deviceConfig = DeviceConfig.PIXEL_5,
    )

    @Test
    fun `given covered state when rendered then action remains behind content`() {
        paparazzi.snapshot {
            SwipeToRevealFixture(isRevealed = false)
        }
    }

    @Test
    fun `given revealed state when rendered then trailing action is visible`() {
        paparazzi.snapshot {
            SwipeToRevealFixture(isRevealed = true)
        }
    }
}

@Composable
private fun SwipeToRevealFixture(isRevealed: Boolean) {
    RoutineHelperTheme(dynamicColor = false) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.TopCenter,
            ) {
                RoutineSwipeToReveal(
                    isRevealed = isRevealed,
                    onRevealedChange = {},
                    onAction = {},
                    backgroundContent = {
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.errorContainer,
                        ) {}
                    },
                    actionContent = { onClick ->
                        Surface(
                            onClick = onClick,
                            modifier = Modifier.fillMaxSize(),
                            color = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer,
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(text = "Delete")
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(88.dp),
                ) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                    ) {
                        Box(
                            modifier = Modifier.padding(16.dp),
                            contentAlignment = Alignment.CenterStart,
                        ) {
                            Text(text = "Pack lunch")
                        }
                    }
                }
            }
        }
    }
}
