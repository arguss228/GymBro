package com.obsession.app.ui.workout

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.obsession.app.data.local.entity.SetLogEntity
import com.obsession.app.ui.rank.RankUpDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutSessionScreen(
    onBack: () -> Unit,
    viewModel: WorkoutSessionViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    state.rankUpEvent?.let { newRank ->
        RankUpDialog(newRank = newRank, onDismiss = { viewModel.dismissRankUp() })
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(state.activePlan?.name ?: "Тренировка", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        // ИСПРАВЛЕНИЕ: показываем таймер тренировки
                        Text(
                            formatElapsedTime(state.workoutElapsedSeconds),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    // ИСПРАВЛЕНИЕ: «Завершить» вызывает finishWorkout() перед закрытием
                    TextButton(onClick = {
                        viewModel.finishWorkout()
                        onBack()
                    }) {
                        Icon(Icons.Default.Check, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text("Завершить")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
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
            Box(Modifier.fillMaxSize().padding(inner).padding(24.dp), contentAlignment = Alignment.Center) {
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
                LazyRow(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(state.days) { day ->
                        FilterChip(
                            selected = state.selectedDayId == day.id,
                            onClick  = { viewModel.selectDay(day.id) },
                            label    = { Text(day.name) },
                        )
                    }
                }
            }

            // Rest timer banner
            AnimatedVisibility(visible = state.restSecondsRemaining > 0, enter = slideInVertically { -it }, exit = slideOutVertically { -it }) {
                RestBanner(seconds = state.restSecondsRemaining, onSkip = viewModel::skipRest)
            }

            // PR celebration
            AnimatedVisibility(visible = state.recentPrMessage != null, enter = slideInVertically { -it }, exit = slideOutVertically { -it }) {
                Box(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.tertiaryContainer).padding(12.dp), contentAlignment = Alignment.Center) {
                    Text(state.recentPrMessage ?: "", color = MaterialTheme.colorScheme.onTertiaryContainer, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(state.exercises, key = { it.planEntry.id }) { item ->
                    ExerciseCard(
                        ui    = item,
                        onLog = { w, r -> viewModel.logSet(exerciseId = item.exercise.id, weightKg = w, reps = r, restSeconds = item.planEntry.restSeconds) },
                    )
                }
            }
        }
    }
}

// ── Форматирование времени ────────────────────────────────────────

private fun formatElapsedTime(seconds: Long): String {
    val h = seconds / 3600
    val m = (seconds % 3600) / 60
    val s = seconds % 60
    return if (h > 0) "%d:%02d:%02d".format(h, m, s)
           else "%d:%02d".format(m, s)
}

// ── Rest banner ───────────────────────────────────────────────────

@Composable
private fun RestBanner(seconds: Int, onSkip: () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.primary).padding(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Timer, null, tint = MaterialTheme.colorScheme.onPrimary)
            Spacer(Modifier.width(8.dp))
            Text("Отдых: ${seconds}с", color = MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            TextButton(onClick = onSkip) {
                Icon(Icons.Default.SkipNext, null, tint = MaterialTheme.colorScheme.onPrimary)
                Spacer(Modifier.width(4.dp))
                Text("Пропустить", color = MaterialTheme.colorScheme.onPrimary)
            }
        }
    }
}

// ── Карточка упражнения ───────────────────────────────────────────

@Composable
private fun ExerciseCard(ui: WorkoutExerciseUi, onLog: (weightKg: Double, reps: Int) -> Unit) {
    val initialWeight = remember(ui.exercise.id) {
        mutableStateMapOf<Long, String>().also { map ->
            val fromPlan   = ui.planEntry.targetWeightKg?.toString() ?: ""
            val lastWeight = ui.loggedSets.lastOrNull()?.weightKg?.toString() ?: fromPlan
            map[ui.exercise.id] = lastWeight
        }
    }
    val weightText = initialWeight[ui.exercise.id] ?: ""
    val repsTextInit = remember(ui.exercise.id) {
        mutableStateMapOf<Long, String>().also { it[ui.exercise.id] = ui.planEntry.targetReps.toString() }
    }
    val repsText = repsTextInit[ui.exercise.id] ?: ""

    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(ui.exercise.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Text("${ui.loggedSets.size} / ${ui.planEntry.targetSets}", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
            Text("План: ${ui.planEntry.targetSets}×${ui.planEntry.targetReps}, отдых ${ui.planEntry.restSeconds}с", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

            if (ui.loggedSets.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    ui.loggedSets.forEach { set -> SetLogRow(set) }
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = weightText,
                    onValueChange = { s -> if (s.all { it.isDigit() || it == '.' || it == ',' }) initialWeight[ui.exercise.id] = s.replace(',', '.') },
                    label = { Text("Вес") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f), singleLine = true,
                )
                OutlinedTextField(
                    value = repsText,
                    onValueChange = { s -> if (s.all { it.isDigit() }) repsTextInit[ui.exercise.id] = s },
                    label = { Text("Повторов") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f), singleLine = true,
                )
                Button(
                    onClick = {
                        val w = weightText.toDoubleOrNull() ?: return@Button
                        val r = repsText.toIntOrNull()      ?: return@Button
                        onLog(w, r)
                    },
                    colors   = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
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
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text("Подход ${set.setNumber}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text("${set.weightKg.formatKg()} × ${set.reps}  (1RM ≈ ${set.estimated1Rm.formatKg()})", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
    }
}

private fun Double.formatKg(): String =
    if (this % 1.0 == 0.0) "${this.toInt()} кг" else "%.1f кг".format(this)