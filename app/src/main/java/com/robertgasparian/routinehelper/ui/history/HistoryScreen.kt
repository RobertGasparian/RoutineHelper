package com.robertgasparian.routinehelper.ui.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.robertgasparian.routinehelper.ui.theme.RoutineHelperTheme

@Composable
fun HistoryScreen(
    onSnapshotClick: (snapshotId: Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HistoryViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    HistoryComponent(
        uiState = uiState,
        onSnapshotClick = onSnapshotClick,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryComponent(
    uiState: HistoryUiState,
    onSnapshotClick: (snapshotId: Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(text = "History") },
            )
        },
    ) { innerPadding ->
        if (uiState.snapshots.isEmpty()) {
            EmptyHistoryContent(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    top = 16.dp,
                    end = 16.dp,
                    bottom = 112.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(
                    items = uiState.snapshots,
                    key = { snapshot -> snapshot.snapshotId },
                ) { snapshot ->
                    HistorySnapshotCard(
                        snapshot = snapshot,
                        onClick = { onSnapshotClick(snapshot.snapshotId) },
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyHistoryContent(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = Icons.Default.DateRange,
            contentDescription = null,
        )
        Text(
            text = "No snapshots yet",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(top = 16.dp),
        )
        Text(
            text = "Use the Snapshot action on Today while we build the nightly reset.",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}

@Composable
private fun HistorySnapshotCard(
    snapshot: HistorySnapshotUiState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick,
    ) {
        ListItem(
            headlineContent = {
                Text(text = snapshot.date)
            },
            supportingContent = {
                Text(text = snapshot.finalizedLabel)
            },
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HistoryComponentPreview() {
    RoutineHelperTheme {
        HistoryComponent(
            uiState = HistoryUiState.preview(),
            onSnapshotClick = {},
        )
    }
}
