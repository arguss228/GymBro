package com.gymbro.app.ui.planeditor

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gymbro.app.data.local.entity.ExerciseEntity

// ════════════════════════════════════════════════════════════════
//  Root composable
// ════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanEditorScreen(
    onBack: () -> Unit,
    viewModel: PlanEditorViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    // Navigate back after successful save
    LaunchedEffect(state.saved) {
        if (state.saved) onBack()
    }

    // Exercise picker sheet
    if (state.exercisePickerDayUiId != null) {
        ExercisePickerSheet(
            onDismiss = viewModel::closeExercisePicker,
            onPick = { exId ->
                viewModel.addExerciseToDay(state.exercisePickerDayUiId!!, exId)
            },
        )
    }

    // Delete day dialog
    state.pendingDeleteDayUiId?.let { uid ->
        val dayName = state.days.find { it.uiId == uid }?.name ?: "день"
        ConfirmDeleteDialog(
            title = "Удалить день?",
            message = "«$dayName» и все его упражнения будут безвозвратно удалены.",
            onConfirm = viewModel::confirmDeleteDay,
            onDismiss = viewModel::cancelDeleteDay,
        )
    }

    // Delete exercise dialog
    state.pendingDeleteExercise?.let { (dayUiId, exUiId) ->
        val exName = state.days.find { it.uiId == dayUiId }
            ?.exercises?.find { it.uiId == exUiId }?.exerciseName ?: "упражнение"
        ConfirmDeleteDialog(
            title = "Удалить упражнение?",
            message = "«$exName» будет удалено из тренировочного дня.",
            onConfirm = viewModel::confirmDeleteExercise,
            onDismiss = viewModel::cancelDeleteExercise,
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (state.isEditMode) "Редактировать план" else "Новый план",
                        fontWeight = FontWeight.Bold,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    TextButton(
                        onClick = viewModel::save,
                        enabled = state.name.isNotBlank() && !state.isSaving,
                    ) {
                        if (state.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        } else {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                "Сохранить",
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
    ) { inner ->
        if (state.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(inner),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
            return@Scaffold
        }

        EditorContent(
            state = state,
            contentPadding = inner,
            onSetName = viewModel::setName,
            onSetDescription = viewModel::setDescription,
            onSetDaysPerWeek = viewModel::setDaysPerWeek,
            onSetMinLevel = viewModel::setMinLevel,
            onRenameDay = viewModel::renameDay,
            onAddDay = viewModel::addDay,
            onRequestDeleteDay = viewModel::requestDeleteDay,
            onReorderDays = viewModel::reorderDays,
            onOpenExercisePicker = viewModel::openExercisePicker,
            onUpdateExercise = viewModel::updateExercise,
            onRequestDeleteExercise = viewModel::requestDeleteExercise,
            onReorderExercises = viewModel::reorderExercises,
        )
    }
}

// ════════════════════════════════════════════════════════════════
//  Scrollable editor body
// ════════════════════════════════════════════════════════════════

@Composable
private fun EditorContent(
    state: PlanEditorUiState,
    contentPadding: androidx.compose.foundation.layout.PaddingValues,
    onSetName: (String) -> Unit,
    onSetDescription: (String) -> Unit,
    onSetDaysPerWeek: (Int) -> Unit,
    onSetMinLevel: (Int) -> Unit,
    onRenameDay: (String, String) -> Unit,
    onAddDay: () -> Unit,
    onRequestDeleteDay: (String) -> Unit,
    onReorderDays: (List<DayDraft>) -> Unit,
    onOpenExercisePicker: (String) -> Unit,
    onUpdateExercise: (String, String, (ExerciseDraft) -> ExerciseDraft) -> Unit,
    onRequestDeleteExercise: (String, String) -> Unit,
    onReorderExercises: (String, List<ExerciseDraft>) -> Unit,
) {
    val days = state.days

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .imePadding(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            horizontal = 16.dp,
            vertical = 12.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Plan metadata
        item { MetadataCard(state, onSetName, onSetDescription, onSetDaysPerWeek, onSetMinLevel) }

        // Section header + Add day button
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    "Тренировочные дни",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                FilledTonalButton(
                    onClick = onAddDay,
                    enabled = days.size < 7,
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Добавить день")
                }
            }
        }

        // Hint
        if (days.isNotEmpty()) {
            item {
                Text(
                    "▲ ▼ — изменить порядок.  ✎ — переименовать день или отредактировать упражнение.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                )
            }
        }

        // Day cards
        itemsIndexed(days, key = { _, day -> day.uiId }) { index, day ->
            DayCard(
                day = day,
                index = index,
                totalDays = days.size,
                onRename = { onRenameDay(day.uiId, it) },
                onRequestDelete = { onRequestDeleteDay(day.uiId) },
                onMoveUp = {
                    if (index > 0) onReorderDays(days.toMutableList().apply {
                        val tmp = this[index]; this[index] = this[index - 1]; this[index - 1] = tmp
                    })
                },
                onMoveDown = {
                    if (index < days.lastIndex) onReorderDays(days.toMutableList().apply {
                        val tmp = this[index]; this[index] = this[index + 1]; this[index + 1] = tmp
                    })
                },
                onOpenExercisePicker = { onOpenExercisePicker(day.uiId) },
                onUpdateExercise = { exUiId, f -> onUpdateExercise(day.uiId, exUiId, f) },
                onRequestDeleteExercise = { exUiId -> onRequestDeleteExercise(day.uiId, exUiId) },
                onReorderExercises = { newList -> onReorderExercises(day.uiId, newList) },
            )
        }

        item { Spacer(Modifier.height(32.dp)) }
    }
}

// ════════════════════════════════════════════════════════════════
//  Metadata card
// ════════════════════════════════════════════════════════════════

@Composable
private fun MetadataCard(
    state: PlanEditorUiState,
    onSetName: (String) -> Unit,
    onSetDescription: (String) -> Unit,
    onSetDaysPerWeek: (Int) -> Unit,
    onSetMinLevel: (Int) -> Unit,
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedTextField(
                value = state.name,
                onValueChange = onSetName,
                label = { Text("Название плана *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                ),
            )
            OutlinedTextField(
                value = state.description,
                onValueChange = onSetDescription,
                label = { Text("Описание (необязательно)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 4,
            )
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    "Дней в неделю: ${state.daysPerWeek}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Slider(
                    value = state.daysPerWeek.toFloat(),
                    onValueChange = { onSetDaysPerWeek(it.toInt()) },
                    valueRange = 1f..6f,
                    steps = 4,
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    "Мин. уровень для доступа: ${state.minLevel}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Slider(
                    value = state.minLevel.toFloat(),
                    onValueChange = { onSetMinLevel(it.toInt()) },
                    valueRange = 1f..15f,
                    steps = 13,
                )
            }
        }
    }
}

// ════════════════════════════════════════════════════════════════
//  Day card
// ════════════════════════════════════════════════════════════════

@Composable
private fun DayCard(
    day: DayDraft,
    index: Int,
    totalDays: Int,
    onRename: (String) -> Unit,
    onRequestDelete: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onOpenExercisePicker: () -> Unit,
    onUpdateExercise: (String, (ExerciseDraft) -> ExerciseDraft) -> Unit,
    onRequestDeleteExercise: (String) -> Unit,
    onReorderExercises: (List<ExerciseDraft>) -> Unit,
) {
    var editingName by remember(day.uiId) { mutableStateOf(false) }
    var nameInput by remember(day.uiId) { mutableStateOf(day.name) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                // Index badge
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        "${index + 1}",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }

                // Name or editable field
                if (editingName) {
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        trailingIcon = {
                            IconButton(onClick = {
                                onRename(nameInput.ifBlank { day.name })
                                editingName = false
                            }) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = "Сохранить",
                                    tint = MaterialTheme.colorScheme.primary,
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                        ),
                    )
                } else {
                    Text(
                        text = day.name.ifBlank { "День ${index + 1}" },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    // Rename button
                    FilledTonalIconButton(
                        onClick = { nameInput = day.name; editingName = true },
                        modifier = Modifier.size(32.dp),
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Переименовать", modifier = Modifier.size(16.dp))
                    }
                }

                // Move up / down
                Column {
                    IconButton(onClick = onMoveUp, enabled = index > 0, modifier = Modifier.size(28.dp)) {
                        Text(
                            "▲",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (index > 0) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f),
                        )
                    }
                    IconButton(onClick = onMoveDown, enabled = index < totalDays - 1, modifier = Modifier.size(28.dp)) {
                        Text(
                            "▼",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (index < totalDays - 1) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f),
                        )
                    }
                }

                // Delete day
                IconButton(onClick = onRequestDelete, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Удалить день", tint = MaterialTheme.colorScheme.error)
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

            // Exercises
            if (day.exercises.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        "Нет упражнений. Нажмите «+» чтобы добавить.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                day.exercises.forEachIndexed { exIdx, ex ->
                    ExerciseRow(
                        ex = ex,
                        index = exIdx,
                        totalCount = day.exercises.size,
                        onUpdate = { f -> onUpdateExercise(ex.uiId, f) },
                        onRequestDelete = { onRequestDeleteExercise(ex.uiId) },
                        onMoveUp = {
                            if (exIdx > 0) onReorderExercises(day.exercises.toMutableList().apply {
                                val tmp = this[exIdx]; this[exIdx] = this[exIdx - 1]; this[exIdx - 1] = tmp
                            })
                        },
                        onMoveDown = {
                            if (exIdx < day.exercises.lastIndex) onReorderExercises(day.exercises.toMutableList().apply {
                                val tmp = this[exIdx]; this[exIdx] = this[exIdx + 1]; this[exIdx + 1] = tmp
                            })
                        },
                    )
                }
            }

            // Add exercise button
            Button(
                onClick = onOpenExercisePicker,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
                    contentColor = MaterialTheme.colorScheme.primary,
                ),
                shape = RoundedCornerShape(12.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Добавить упражнение", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

// ════════════════════════════════════════════════════════════════
//  Exercise row inside day
// ════════════════════════════════════════════════════════════════

@Composable
private fun ExerciseRow(
    ex: ExerciseDraft,
    index: Int,
    totalCount: Int,
    onUpdate: ((ExerciseDraft) -> ExerciseDraft) -> Unit,
    onRequestDelete: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
) {
    var expanded by remember(ex.uiId) { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // Header row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Default.FitnessCenter,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(17.dp),
                    )
                }
                Column(Modifier.weight(1f)) {
                    Text(
                        ex.exerciseName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        "${ex.sets} подх. × ${ex.reps} повт.  ·  ${ex.restSeconds}с отдых",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                // Move up/down
                Column {
                    IconButton(onClick = onMoveUp, enabled = index > 0, modifier = Modifier.size(22.dp)) {
                        Text("▲", style = MaterialTheme.typography.labelSmall,
                            color = if (index > 0) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f))
                    }
                    IconButton(onClick = onMoveDown, enabled = index < totalCount - 1, modifier = Modifier.size(22.dp)) {
                        Text("▼", style = MaterialTheme.typography.labelSmall,
                            color = if (index < totalCount - 1) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f))
                    }
                }
                // Edit toggle
                FilledTonalIconButton(onClick = { expanded = !expanded }, modifier = Modifier.size(32.dp)) {
                    Icon(
                        if (expanded) Icons.Default.Close else Icons.Default.Edit,
                        contentDescription = if (expanded) "Свернуть" else "Редактировать",
                        modifier = Modifier.size(15.dp),
                    )
                }
                // Delete
                IconButton(onClick = onRequestDelete, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Удалить",
                        tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(17.dp))
                }
            }

            // Expanded numeric fields
            if (expanded) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    CompactIntField(
                        label = "Подходы",
                        value = ex.sets,
                        onValueChange = { v -> onUpdate { it.copy(sets = v.coerceIn(1, 20)) } },
                        modifier = Modifier.weight(1f),
                    )
                    CompactIntField(
                        label = "Повторы",
                        value = ex.reps,
                        onValueChange = { v -> onUpdate { it.copy(reps = v.coerceIn(1, 50)) } },
                        modifier = Modifier.weight(1f),
                    )
                    CompactIntField(
                        label = "Отдых, с",
                        value = ex.restSeconds,
                        onValueChange = { v -> onUpdate { it.copy(restSeconds = v.coerceIn(15, 600)) } },
                        modifier = Modifier.weight(1.1f),
                    )
                }
            }
        }
    }
}

// ════════════════════════════════════════════════════════════════
//  Exercise picker bottom sheet
// ════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExercisePickerSheet(
    onDismiss: () -> Unit,
    onPick: (Long) -> Unit,
    pickerViewModel: ExercisePickerViewModel = hiltViewModel(),
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val exercises by pickerViewModel.filteredExercises.collectAsStateWithLifecycle()
    val query by pickerViewModel.query.collectAsStateWithLifecycle()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                "Выберите упражнение",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )

            OutlinedTextField(
                value = query,
                onValueChange = pickerViewModel::setQuery,
                placeholder = { Text("Поиск по названию") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { pickerViewModel.setQuery("") }) {
                            Icon(Icons.Default.Close, contentDescription = "Очистить")
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                ),
            )

            LazyColumn(
                modifier = Modifier.fillMaxWidth().height(430.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp),
            ) {
                itemsIndexed(exercises, key = { _, e -> e.id }) { idx, exercise ->
                    PickerExerciseItem(exercise = exercise, onClick = { onPick(exercise.id) })
                    if (idx < exercises.lastIndex) {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                            modifier = Modifier.padding(horizontal = 8.dp),
                        )
                    }
                }
                if (exercises.isEmpty()) {
                    item {
                        Box(
                            Modifier.fillMaxWidth().padding(vertical = 48.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text("Упражнения не найдены", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PickerExerciseItem(exercise: ExerciseEntity, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Default.FitnessCenter, contentDescription = null,
                tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
        }
        Column(Modifier.weight(1f)) {
            Text(exercise.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            Text(
                "${exercise.category.name.lowercase().replaceFirstChar { it.uppercase() }} · ${exercise.primaryMuscle}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Icon(Icons.Default.Add, contentDescription = "Выбрать", tint = MaterialTheme.colorScheme.primary)
    }
}

// ════════════════════════════════════════════════════════════════
//  Confirmation dialog
// ════════════════════════════════════════════════════════════════

@Composable
private fun ConfirmDeleteDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(Icons.Default.Warning, contentDescription = null,
                tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(28.dp))
        },
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = {
            Text(message, style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError,
                ),
            ) { Text("Удалить") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } },
    )
}

// ════════════════════════════════════════════════════════════════
//  Compact int field
// ════════════════════════════════════════════════════════════════

@Composable
private fun CompactIntField(
    label: String,
    value: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = value.toString(),
        onValueChange = { it.toIntOrNull()?.let(onValueChange) },
        label = { Text(label, style = MaterialTheme.typography.bodySmall) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = modifier,
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary),
    )
}