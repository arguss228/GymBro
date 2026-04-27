package com.gymbro.app.ui.workout

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gymbro.app.data.local.entity.SetLogEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutSessionScreen(
    onBack: () -> Unit,
    viewModel: WorkoutSessionViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(state.activePlan?.name ?: "Тренировка")
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    TextButton(onClick = onBack) {
                        Icon(Icons.Default.Check, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text("Завершить")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
    ) { inner ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize().padding(inner), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
            return@Scaffold
        }

        if (state.activePlan == null) {
            Box(
                modifier = Modifier.fillMaxSize().padding(inner).padding(24.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    "У вас нет активного плана. Выберите план в разделе «Мои планы», чтобы начать тренировку.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            return@Scaffold
        }

        Column(Modifier.fillMaxSize().padding(inner)) {
            // Day picker
            if (state.days.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    state.days.forEach { day ->
                        FilterChip(
                            selected = state.selectedDayId == day.id,
                            onClick = { viewModel.selectDay(day.id) },
                            label = { Text(day.name) },
                        )
                    }
                }
            }

            // Rest timer banner
            AnimatedVisibility(
                visible = state.restSecondsRemaining > 0,
                enter = slideInVertically { -it },
                exit = slideOutVertically { -it },
            ) {
                RestBanner(
                    seconds = state.restSecondsRemaining,
                    onSkip = viewModel::skipRest,
                )
            }

            // PR celebration
            AnimatedVisibility(
                visible = state.recentPrMessage != null,
                enter = slideInVertically { -it },
                exit = slideOutVertically { -it },
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.tertiaryContainer)
                        .padding(12.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        state.recentPrMessage ?: "",
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    horizontal = 16.dp, vertical = 8.dp
                ),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(state.exercises, key = { it.planEntry.id }) { item ->
                    ExerciseCard(
                        ui = item,
                        onLog = { w, r ->
                            viewModel.logSet(
                                exerciseId = item.exercise.id,
                                weightKg = w,
                                reps = r,
                                restSeconds = item.planEntry.restSeconds,
                            )
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun RestBanner(seconds: Int, onSkip: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primary)
            .padding(12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Default.Timer,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
            )
            Spacer(Modifier.width(8.dp))
            Text(
                "Отдых: ${seconds}с",
                color = MaterialTheme.colorScheme.onPrimary,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
            )
            TextButton(onClick = onSkip) {
                Icon(
                    Icons.Default.SkipNext,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                )
                Spacer(Modifier.width(4.dp))
                Text("Пропустить", color = MaterialTheme.colorScheme.onPrimary)
            }
        }
    }
}

@Composable
private fun ExerciseCard(
    ui: WorkoutExerciseUi,
    onLog: (weightKg: Double, reps: Int) -> Unit,
) {
    // Inputs, pre-filled with target weight from plan or last-session weight.
    val initialWeight = remember(ui.exercise.id) {
        mutableStateMapOf<Long, String>().also { map ->
            val fromPlan = ui.planEntry.targetWeightKg?.toString() ?: ""
            val lastWeight = ui.loggedSets.lastOrNull()?.weightKg?.toString() ?: fromPlan
            map[ui.exercise.id] = lastWeight
        }
    }
    val weightText = initialWeight[ui.exercise.id] ?: ""
    val repsTextInit = remember(ui.exercise.id) {
        mutableStateMapOf<Long, String>().also {
            it[ui.exercise.id] = ui.planEntry.targetReps.toString()
        }
    }
    val repsText = repsTextInit[ui.exercise.id] ?: ""

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    ui.exercise.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    "${ui.loggedSets.size} / ${ui.planEntry.targetSets}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                )
            }
            Text(
                "План: ${ui.planEntry.targetSets}×${ui.planEntry.targetReps}, отдых ${ui.planEntry.restSeconds}с",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            // Previously logged sets
            if (ui.loggedSets.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    ui.loggedSets.forEach { set ->
                        SetLogRow(set)
                    }
                }
            }

            // Input row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedTextField(
                    value = weightText,
                    onValueChange = { s ->
                        if (s.all { it.isDigit() || it == '.' || it == ',' }) {
                            initialWeight[ui.exercise.id] = s.replace(',', '.')
                        }
                    },
                    label = { Text("Вес") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                )
                OutlinedTextField(
                    value = repsText,
                    onValueChange = { s ->
                        if (s.all { it.isDigit() }) repsTextInit[ui.exercise.id] = s
                    },
                    label = { Text("Повторов") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                )
                Button(
                    onClick = {
                        val w = weightText.toDoubleOrNull() ?: return@Button
                        val r = repsText.toIntOrNull() ?: return@Button
                        onLog(w, r)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.height(56.dp),
                ) {
                    Icon(Icons.Default.Check, contentDescription = "Залогировать")
                }
            }
        }
    }
}

@Composable
private fun SetLogRow(set: SetLogEntity) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            "Подход ${set.setNumber}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            "${set.weightKg.formatKg()} × ${set.reps}  (1RM ≈ ${set.estimated1Rm.formatKg()})",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

private fun Double.formatKg(): String =
    if (this % 1.0 == 0.0) "${this.toInt()} кг" else "%.1f кг".format(this)
