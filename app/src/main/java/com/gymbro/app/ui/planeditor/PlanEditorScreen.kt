package com.gymbro.app.ui.planeditor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gymbro.app.ui.exercises.ExercisesScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanEditorScreen(
    onBack: () -> Unit,
    viewModel: PlanEditorViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var pickingForDayId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(state.saved) {
        if (state.saved) onBack()
    }

    if (pickingForDayId != null) {
        ExercisesScreen(
            onBack = { pickingForDayId = null },
            onPickExercise = { exId ->
                viewModel.addExerciseToDay(pickingForDayId!!, exId)
                pickingForDayId = null
            },
        )
        return
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Новый план") },
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
                        Text(if (state.isSaving) "..." else "Сохранить")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
    ) { inner ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedTextField(
                value = state.name,
                onValueChange = viewModel::setName,
                label = { Text("Название плана *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            OutlinedTextField(
                value = state.description,
                onValueChange = viewModel::setDescription,
                label = { Text("Описание") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
            )

            Text(
                "Дней в неделю: ${state.daysPerWeek}",
                style = MaterialTheme.typography.titleMedium,
            )
            Slider(
                value = state.daysPerWeek.toFloat(),
                onValueChange = { viewModel.setDaysPerWeek(it.toInt()) },
                valueRange = 3f..6f,
                steps = 2,
            )

            Text(
                "Мин. уровень для фильтра: ${state.minLevel}",
                style = MaterialTheme.typography.titleMedium,
            )
            Slider(
                value = state.minLevel.toFloat(),
                onValueChange = { viewModel.setMinLevel(it.toInt()) },
                valueRange = 1f..15f,
                steps = 13,
            )

            Spacer(Modifier.height(8.dp))

            state.days.forEach { day ->
                DayEditor(
                    day = day,
                    onRename = { viewModel.renameDay(day.uiId, it) },
                    onAddExercise = { pickingForDayId = day.uiId },
                    onUpdateExercise = { exId, f -> viewModel.updateExercise(day.uiId, exId, f) },
                    onRemoveExercise = { exId -> viewModel.removeExercise(day.uiId, exId) },
                )
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun DayEditor(
    day: DayDraft,
    onRename: (String) -> Unit,
    onAddExercise: () -> Unit,
    onUpdateExercise: (String, (ExerciseDraft) -> ExerciseDraft) -> Unit,
    onRemoveExercise: (String) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            OutlinedTextField(
                value = day.name,
                onValueChange = onRename,
                label = { Text("Название дня") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            day.exercises.forEach { ex ->
                ExerciseDraftRow(
                    ex = ex,
                    onUpdate = { f -> onUpdateExercise(ex.uiId, f) },
                    onRemove = { onRemoveExercise(ex.uiId) },
                )
            }
            Button(
                onClick = onAddExercise,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                    contentColor = MaterialTheme.colorScheme.primary,
                ),
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text("Добавить упражнение")
            }
        }
    }
}

@Composable
private fun ExerciseDraftRow(
    ex: ExerciseDraft,
    onUpdate: ((ExerciseDraft) -> ExerciseDraft) -> Unit,
    onRemove: () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    ex.exerciseName,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                IconButton(onClick = onRemove) {
                    Icon(Icons.Default.Close, contentDescription = "Удалить")
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IntField(
                    label = "Подходы",
                    value = ex.sets,
                    onValueChange = { v -> onUpdate { it.copy(sets = v.coerceIn(1, 20)) } },
                    modifier = Modifier.weight(1f),
                )
                IntField(
                    label = "Повторы",
                    value = ex.reps,
                    onValueChange = { v -> onUpdate { it.copy(reps = v.coerceIn(1, 50)) } },
                    modifier = Modifier.weight(1f),
                )
                IntField(
                    label = "Отдых, сек",
                    value = ex.restSeconds,
                    onValueChange = { v -> onUpdate { it.copy(restSeconds = v.coerceIn(15, 600)) } },
                    modifier = Modifier.weight(1.2f),
                )
            }
        }
    }
}

@Composable
private fun IntField(
    label: String,
    value: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = value.toString(),
        onValueChange = { it.toIntOrNull()?.let(onValueChange) },
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = modifier,
        singleLine = true,
    )
}
