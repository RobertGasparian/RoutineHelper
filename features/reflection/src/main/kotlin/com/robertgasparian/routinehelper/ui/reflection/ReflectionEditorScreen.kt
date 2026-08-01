package com.robertgasparian.routinehelper.ui.reflection

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.robertgasparian.routinehelper.core.navigation.BottomSheetPresentationState
import com.robertgasparian.routinehelper.core.navigation.LocalBottomSheetPresentationState
import com.robertgasparian.routinehelper.core.navigation.LocalNavigationFlowViewModelStoreOwner
import com.robertgasparian.routinehelper.features.reflection.R
import com.robertgasparian.routinehelper.ui.dsm.RoutineOutlinedTextField
import com.robertgasparian.routinehelper.ui.reflection.api.ReflectionEditorState
import com.robertgasparian.routinehelper.ui.theme.RoutineHelperTheme

@Composable
fun ReflectionEditorScreen(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ReflectionEditorSessionViewModel = hiltViewModel(
        viewModelStoreOwner = LocalNavigationFlowViewModelStoreOwner.current,
    ),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val bottomSheetPresentationState = LocalBottomSheetPresentationState.current

    ReflectionEditorComponent(
        state = state,
        onIntent = { intent ->
            when (intent) {
                is ReflectionEditorIntent.DraftChange -> viewModel.updateDraft(
                    text = intent.text,
                    selectionStart = intent.selectionStart,
                    selectionEnd = intent.selectionEnd,
                )
                is ReflectionEditorIntent.RatingChange -> viewModel.updateRating(intent.rating)
                ReflectionEditorIntent.ClearClick -> viewModel.clearDraft()
                ReflectionEditorIntent.CancelClick -> {
                    viewModel.cancel()
                    onDismiss()
                }
                ReflectionEditorIntent.SaveClick -> {
                    viewModel.requestSave()
                    onDismiss()
                }
            }
        },
        modifier = modifier,
        autoFocus = bottomSheetPresentationState == BottomSheetPresentationState.Presented,
    )
}

@Composable
fun ReflectionEditorComponent(
    state: ReflectionEditorState,
    onIntent: (ReflectionEditorIntent) -> Unit,
    modifier: Modifier = Modifier,
    autoFocus: Boolean = true,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(start = 24.dp, end = 24.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = stringResource(R.string.reflection_editor_title),
            style = MaterialTheme.typography.headlineSmall,
        )
        Text(
            text = stringResource(R.string.reflection_editor_supporting_text),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        if (state.isInitialized) {
            CompactPillRatingSelector(
                rating = state.draftRating,
                onRatingChange = { rating ->
                    onIntent(ReflectionEditorIntent.RatingChange(rating))
                },
            )
            ReflectionTextField(
                state = state,
                onValueChange = { value ->
                    onIntent(
                        ReflectionEditorIntent.DraftChange(
                            text = value.text,
                            selectionStart = value.selection.start,
                            selectionEnd = value.selection.end,
                        ),
                    )
                },
                autoFocus = autoFocus,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (state.canClear) {
                    TextButton(onClick = { onIntent(ReflectionEditorIntent.ClearClick) }) {
                        Text(text = stringResource(R.string.reflection_editor_clear))
                    }
                }
                Spacer(modifier = Modifier.weight(1f))
                TextButton(onClick = { onIntent(ReflectionEditorIntent.CancelClick) }) {
                    Text(text = stringResource(R.string.reflection_editor_cancel))
                }
                Button(
                    enabled = state.saveRequest == null,
                    onClick = { onIntent(ReflectionEditorIntent.SaveClick) },
                ) {
                    Text(text = stringResource(R.string.reflection_editor_save))
                }
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 40.dp),
                horizontalArrangement = Arrangement.Center,
            ) {
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
private fun ReflectionTextField(
    state: ReflectionEditorState,
    onValueChange: (TextFieldValue) -> Unit,
    autoFocus: Boolean,
    modifier: Modifier = Modifier,
) {
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(state.isInitialized, autoFocus) {
        if (state.isInitialized && autoFocus) {
            focusRequester.requestFocus()
        }
    }

    RoutineOutlinedTextField(
        value = state.toTextFieldValue(),
        onValueChange = onValueChange,
        label = stringResource(R.string.reflection_editor_note_label),
        modifier = modifier
            .fillMaxWidth()
            .focusRequester(focusRequester),
        minLines = 7,
        maxLines = 12,
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Sentences,
        ),
    )
}

private fun ReflectionEditorState.toTextFieldValue(): TextFieldValue =
    TextFieldValue(
        text = draftText,
        selection = TextRange(
            start = selectionStart.coerceIn(0, draftText.length),
            end = selectionEnd.coerceIn(0, draftText.length),
        ),
    )

@Preview(showBackground = true, widthDp = 390)
@Composable
private fun ReflectionEditorComponentPreview() {
    RoutineHelperTheme(dynamicColor = false) {
        ReflectionEditorComponent(
            state = ReflectionEditorState.preview(),
            onIntent = {},
            autoFocus = false,
        )
    }
}
