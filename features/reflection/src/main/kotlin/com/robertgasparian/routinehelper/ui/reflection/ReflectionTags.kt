package com.robertgasparian.routinehelper.ui.reflection

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.robertgasparian.routinehelper.domain.model.ReflectionTagInputNormalizer
import com.robertgasparian.routinehelper.features.reflection.R
import com.robertgasparian.routinehelper.ui.dsm.RoutineOutlinedTextField
import com.robertgasparian.routinehelper.ui.reflection.api.ReflectionEditorDraftTag
import com.robertgasparian.routinehelper.ui.theme.RoutineHelperTheme

@Composable
internal fun ReflectionTagsEditor(
    tags: List<ReflectionEditorDraftTag>,
    onTagSelectionChange: (Long) -> Unit,
    onAddTag: (String) -> Unit,
    onDeleteTag: (Long) -> Unit,
    modifier: Modifier = Modifier,
    showAddTestTags: Boolean = false,
) {
    var mode by remember { mutableStateOf<ReflectionTagsMode>(ReflectionTagsMode.Selection) }
    var newTagName by remember { mutableStateOf("") }
    ReflectionTagsComponent(
        uiState = ReflectionTagsUiState(
            tags = tags.map { tag ->
                ReflectionTagUi(id = tag.draftId, label = tag.label)
            },
            selectedTagIds = tags.filter(ReflectionEditorDraftTag::isSelected)
                .mapTo(mutableSetOf(), ReflectionEditorDraftTag::draftId),
            mode = mode,
            newTagName = newTagName,
        ),
        onAction = { action ->
            when (action) {
                ReflectionTagsAction.AddClick -> {
                    mode = ReflectionTagsMode.Adding
                    newTagName = ""
                }
                ReflectionTagsAction.AddCancelClick -> {
                    mode = ReflectionTagsMode.Selection
                    newTagName = ""
                }
                ReflectionTagsAction.AddConfirmClick -> {
                    onAddTag(newTagName)
                    mode = ReflectionTagsMode.Selection
                    newTagName = ""
                }
                ReflectionTagsAction.ManageClick -> mode = ReflectionTagsMode.Managing
                ReflectionTagsAction.ManageDoneClick -> mode = ReflectionTagsMode.Selection
                is ReflectionTagsAction.NewTagNameChange -> {
                    newTagName = action.value.take(ReflectionTagInputNormalizer.MAX_LABEL_LENGTH)
                }
                is ReflectionTagsAction.TagToggle -> onTagSelectionChange(action.tagId)
                is ReflectionTagsAction.DeleteTagClick -> {
                    onDeleteTag(action.tagId)
                    if (tags.size == 1) mode = ReflectionTagsMode.Selection
                }
                ReflectionTagsAction.AddTestTagsClick -> {
                    (1..TestTagCount).forEach { number -> onAddTag("Tag $number") }
                }
            }
        },
        modifier = modifier,
        showAddTestTags = showAddTestTags,
    )
}

@Composable
internal fun ReflectionTagsComponent(
    uiState: ReflectionTagsUiState,
    onAction: (ReflectionTagsAction) -> Unit,
    modifier: Modifier = Modifier,
    isEditable: Boolean = true,
    showAddTestTags: Boolean = false,
) {
    val visibleTags = if (isEditable) {
        uiState.tags
    } else {
        uiState.tags.filter { tag -> tag.id in uiState.selectedTagIds }
    }
    if (!isEditable && visibleTags.isEmpty()) return

    val isAdding = isEditable && uiState.mode == ReflectionTagsMode.Adding
    val isManaging = isEditable && uiState.mode == ReflectionTagsMode.Managing

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.reflection_editor_tags_label),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleMedium,
            )
            if (isEditable && visibleTags.isNotEmpty() && !isAdding) {
                if (isManaging) {
                    Button(onClick = { onAction(ReflectionTagsAction.ManageDoneClick) }) {
                        Text(text = stringResource(R.string.reflection_editor_tags_done))
                    }
                } else {
                    TextButton(onClick = { onAction(ReflectionTagsAction.ManageClick) }) {
                        Text(text = stringResource(R.string.reflection_editor_tags_manage))
                    }
                }
            }
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
        ) {
            Column(
                modifier = Modifier.padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (visibleTags.isNotEmpty()) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        visibleTags.forEach { tag ->
                            when {
                                !isEditable -> ReflectionTagIndicator(tag = tag)
                                isManaging -> ManageReflectionTagButton(
                                    tag = tag,
                                    onDeleteClick = {
                                        onAction(ReflectionTagsAction.DeleteTagClick(tag.id))
                                    },
                                )
                                else -> ReflectionTagToggleButton(
                                    tag = tag,
                                    isSelected = tag.id in uiState.selectedTagIds,
                                    enabled = !isAdding,
                                    onToggle = {
                                        onAction(ReflectionTagsAction.TagToggle(tag.id))
                                    },
                                )
                            }
                        }

                        if (isEditable && uiState.mode == ReflectionTagsMode.Selection) {
                            OutlinedButton(
                                onClick = { onAction(ReflectionTagsAction.AddClick) },
                                modifier = Modifier.heightIn(min = 48.dp),
                                shape = CircleShape,
                                border = BorderStroke(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.outlineVariant,
                                ),
                                contentPadding = ButtonDefaults.ContentPadding,
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = stringResource(R.string.reflection_editor_tags_add))
                            }
                        }
                    }
                } else if (!isAdding) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.reflection_editor_tags_empty),
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Button(onClick = { onAction(ReflectionTagsAction.AddClick) }) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = stringResource(R.string.reflection_editor_tags_create_first))
                        }
                    }
                }

                if (isAdding) {
                    ReflectionTagInput(
                        value = uiState.newTagName,
                        onValueChange = { value ->
                            onAction(ReflectionTagsAction.NewTagNameChange(value))
                        },
                        onCancelClick = { onAction(ReflectionTagsAction.AddCancelClick) },
                        onConfirmClick = { onAction(ReflectionTagsAction.AddConfirmClick) },
                    )
                }
            }
        }
        if (showAddTestTags && isEditable && !isAdding && !isManaging) {
            TextButton(
                onClick = { onAction(ReflectionTagsAction.AddTestTagsClick) },
                modifier = Modifier.align(Alignment.End),
            ) {
                Text(
                    text = "Add 5 test tags",
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

@Composable
private fun ReflectionTagIndicator(
    tag: ReflectionTagUi,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.widthIn(max = 220.dp),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
    ) {
        Text(
            text = tag.label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelLarge,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ReflectionTagToggleButton(
    tag: ReflectionTagUi,
    isSelected: Boolean,
    enabled: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ToggleButton(
        checked = isSelected,
        onCheckedChange = { onToggle() },
        modifier = modifier
            .heightIn(min = 48.dp)
            .widthIn(max = 220.dp)
            .semantics { role = Role.Checkbox },
        enabled = enabled,
        shapes = ToggleButtonDefaults.shapes(
            shape = CircleShape,
            pressedShape = CircleShape,
            checkedShape = CircleShape,
        ),
        colors = ToggleButtonDefaults.toggleButtonColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            checkedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            checkedContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ),
        contentPadding = ButtonDefaults.ContentPadding,
    ) {
        Text(
            text = tag.label,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun ManageReflectionTagButton(
    tag: ReflectionTagUi,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val description = stringResource(R.string.reflection_editor_tags_delete_description, tag.label)
    Surface(
        modifier = modifier
            .heightIn(min = 48.dp)
            .widthIn(max = 220.dp),
        shape = CircleShape,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = tag.label,
                modifier = Modifier
                    .weight(1f, fill = false)
                    .padding(start = 16.dp, top = 8.dp, bottom = 8.dp),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            IconButton(onClick = onDeleteClick) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = description,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

@Composable
private fun ReflectionTagInput(
    value: String,
    onValueChange: (String) -> Unit,
    onCancelClick: () -> Unit,
    onConfirmClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        RoutineOutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = stringResource(R.string.reflection_editor_tags_name_label),
            modifier = Modifier
                .weight(1f)
                .focusRequester(focusRequester),
            singleLine = true,
        )
        IconButton(onClick = onCancelClick) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = stringResource(R.string.reflection_editor_tags_add_cancel_description),
            )
        }
        FilledIconButton(
            enabled = value.trim().isNotEmpty(),
            onClick = onConfirmClick,
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = stringResource(R.string.reflection_editor_tags_add_confirm_description),
            )
        }
    }
}

@Immutable
internal data class ReflectionTagsUiState(
    val tags: List<ReflectionTagUi> = emptyList(),
    val selectedTagIds: Set<Long> = emptySet(),
    val mode: ReflectionTagsMode = ReflectionTagsMode.Selection,
    val newTagName: String = "",
) {
    companion object {
        fun preview(
            selectedTagIds: Set<Long> = setOf(1L, 2L, 6L),
            mode: ReflectionTagsMode = ReflectionTagsMode.Selection,
        ): ReflectionTagsUiState =
            ReflectionTagsUiState(
                tags = listOf(
                    ReflectionTagUi(id = 1L, label = "Productive"),
                    ReflectionTagUi(id = 2L, label = "Calm"),
                    ReflectionTagUi(id = 3L, label = "Low energy"),
                    ReflectionTagUi(id = 4L, label = "Focused"),
                    ReflectionTagUi(id = 5L, label = "Overwhelmed"),
                    ReflectionTagUi(id = 6L, label = "Needs adjustment and more sleep"),
                ),
                selectedTagIds = selectedTagIds,
                mode = mode,
            )
    }
}

@Immutable
internal data class ReflectionTagUi(
    val id: Long,
    val label: String,
)

internal enum class ReflectionTagsMode {
    Selection,
    Adding,
    Managing,
}

internal sealed interface ReflectionTagsAction {
    data class TagToggle(val tagId: Long) : ReflectionTagsAction

    data object AddClick : ReflectionTagsAction

    data class NewTagNameChange(val value: String) : ReflectionTagsAction

    data object AddConfirmClick : ReflectionTagsAction

    data object AddCancelClick : ReflectionTagsAction

    data object ManageClick : ReflectionTagsAction

    data object ManageDoneClick : ReflectionTagsAction

    data class DeleteTagClick(val tagId: Long) : ReflectionTagsAction

    data object AddTestTagsClick : ReflectionTagsAction
}

private const val TestTagCount = 5

@Preview(showBackground = true, widthDp = 390)
@Composable
private fun ReflectionTagsComponentPreview() {
    RoutineHelperTheme(dynamicColor = false) {
        ReflectionTagsComponent(
            uiState = ReflectionTagsUiState.preview(),
            onAction = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(showBackground = true, widthDp = 390)
@Composable
private fun ReflectionTagsManagePreview() {
    RoutineHelperTheme(dynamicColor = false) {
        ReflectionTagsComponent(
            uiState = ReflectionTagsUiState.preview(
                selectedTagIds = setOf(1L, 2L),
                mode = ReflectionTagsMode.Managing,
            ),
            onAction = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(showBackground = true, widthDp = 390)
@Composable
private fun ReflectionTagsReadOnlyPreview() {
    RoutineHelperTheme(dynamicColor = false) {
        ReflectionTagsComponent(
            uiState = ReflectionTagsUiState.preview(),
            onAction = {},
            modifier = Modifier.padding(16.dp),
            isEditable = false,
        )
    }
}
