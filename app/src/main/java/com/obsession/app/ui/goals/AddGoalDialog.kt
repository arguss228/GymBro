package com.obsession.app.ui.goals

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.obsession.app.data.local.entity.ExerciseEntity
import com.obsession.app.domain.goals.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AddGoalDialog(
    exercises: List<ExerciseEntity>,
    currentUserWeight: Double,
    onConfirm: (GoalParams) -> Unit,
    onDismiss: () -> Unit,
) {
    var selectedType by remember { mutableStateOf<GoalType?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .clip(RoundedCornerShape(28.dp))
                .background(MaterialTheme.colorScheme.surface),
        ) {
            when (selectedType) {
                null -> GoalTypePicker(
                    onSelect = { selectedType = it },
                    onDismiss = onDismiss,
                )
                GoalType.STRENGTH -> StrengthGoalForm(
                    exercises = exercises,
                    onConfirm = { onConfirm(it); onDismiss() },
                    onBack = { selectedType = null },
                )
                GoalType.BODY_WEIGHT -> BodyWeightGoalForm(
                    currentWeight = currentUserWeight,
                    onConfirm = { onConfirm(it); onDismiss() },
                    onBack = { selectedType = null },
                )
                GoalType.WIN_STREAK -> WinStreakGoalForm(
                    onConfirm = { onConfirm(it); onDismiss() },
                    onBack = { selectedType = null },
                )
            }
        }
    }
}

@Composable
private fun GoalTypePicker(onSelect: (GoalType) -> Unit, onDismiss: () -> Unit) {
    Column(
        modifier = Modifier.padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            "Выберите тип цели",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
        )

        val types = listOf(
            Triple(GoalType.STRENGTH, "🏋️", "Цель по силе" to "Новый максимум в упражнении"),
            Triple(GoalType.BODY_WEIGHT, "⚖️", "Цель по весу тела" to "Достичь желаемого веса"),
            Triple(GoalType.WIN_STREAK, "🔥", "Win Streak" to "Серия последовательных тренировок"),
        )

        types.forEach { (type, icon, titleDesc) ->
            val (title, desc) = titleDesc
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { onSelect(type) }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Text(icon, fontSize = 30.sp)
                Column(Modifier.weight(1f)) {
                    Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text(desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) {
            Text("Отмена")
        }
    }
}

@Composable
private fun StrengthGoalForm(
    exercises: List<ExerciseEntity>,
    onConfirm: (GoalParams.Strength) -> Unit,
    onBack: () -> Unit,
) {
    var selectedExercise by remember { mutableStateOf<ExerciseEntity?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var targetWeight by remember { mutableStateOf("") }
    var targetReps by remember { mutableStateOf("5") }
    var showDatePicker by remember { mutableStateOf(false) }
    var selectedDateMillis by remember { mutableStateOf<Long?>(null) }

    val filtered = remember(searchQuery, exercises) {
        if (searchQuery.isBlank()) exercises
        else exercises.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }

    val canConfirm = selectedExercise != null &&
            targetWeight.toDoubleOrNull() != null &&
            targetReps.toIntOrNull() != null

    Column(
        modifier = Modifier
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            IconButton(onClick = onBack, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.ArrowBack, null, modifier = Modifier.size(20.dp))
            }
            Text("Цель по силе 🏋️", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
        }

        if (selectedExercise == null) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Поиск упражнения") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
            )
            Card(
                modifier = Modifier.fillMaxWidth().heightIn(max = 240.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            ) {
                LazyColumn {
                    items(filtered.take(8)) { ex ->
                        Row(
                            modifier = Modifier.fillMaxWidth().clickable { selectedExercise = ex }.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Icon(Icons.Default.FitnessCenter, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            Column(Modifier.weight(1f)) {
                                Text(ex.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                Text(ex.primaryMuscle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(0.3f))
                    }
                }
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(0.3f))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Icon(Icons.Default.FitnessCenter, null, tint = MaterialTheme.colorScheme.primary)
                Text(selectedExercise!!.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                IconButton(onClick = { selectedExercise = null; searchQuery = "" }, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Close, null, modifier = Modifier.size(16.dp))
                }
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(
                value = targetWeight,
                onValueChange = { targetWeight = it },
                label = { Text("Вес, кг") },
                suffix = { Text("кг") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f),
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
            )
            OutlinedTextField(
                value = targetReps,
                onValueChange = { targetReps = it },
                label = { Text("Повторения") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
            )
        }

        DatePickerRow(
            selectedDateMillis = selectedDateMillis,
            onPickDate = { showDatePicker = true },
            onClearDate = { selectedDateMillis = null },
        )

        Button(
            onClick = {
                onConfirm(
                    GoalParams.Strength(
                        exerciseId = selectedExercise!!.id,
                        exerciseName = selectedExercise!!.name,
                        targetWeightKg = targetWeight.toDouble(),
                        targetReps = targetReps.toIntOrNull() ?: 1,
                        deadlineMillis = selectedDateMillis,
                    )
                )
            },
            enabled = canConfirm,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(14.dp),
        ) {
            Icon(Icons.Default.Flag, null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Установить цель", fontWeight = FontWeight.Bold)
        }
    }

    if (showDatePicker) {
        SimpleDatePickerDialog(
            onConfirm = { selectedDateMillis = it; showDatePicker = false },
            onDismiss = { showDatePicker = false },
        )
    }
}

@Composable
private fun BodyWeightGoalForm(
    currentWeight: Double,
    onConfirm: (GoalParams.BodyWeight) -> Unit,
    onBack: () -> Unit,
) {
    var targetWeight by remember { mutableStateOf(currentWeight.toInt().toString()) }
    var selectedDateMillis by remember { mutableStateOf<Long?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }

    val targetKg = targetWeight.toDoubleOrNull() ?: currentWeight
    val difficulty = weightDifficulty(currentWeight, targetKg)
    val canConfirm = targetWeight.toDoubleOrNull() != null

    Column(
        modifier = Modifier.padding(24.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            IconButton(onClick = onBack, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.ArrowBack, null, modifier = Modifier.size(20.dp))
            }
            Text("Цель по весу ⚖️", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Текущий вес", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("${currentWeight.toInt()} кг", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }

        Text("Целевой вес", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

        OutlinedTextField(
            value = targetWeight,
            onValueChange = { targetWeight = it },
            label = { Text("Целевой вес, кг") },
            suffix = { Text("кг") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
        )

        val sliderRange = 40f..200f
        var sliderPos by remember { mutableStateOf(targetKg.toFloat().coerceIn(sliderRange)) }
        LaunchedEffect(sliderPos) { targetWeight = sliderPos.toInt().toString() }

        Slider(
            value = sliderPos,
            onValueChange = { sliderPos = it },
            valueRange = sliderRange,
            modifier = Modifier.fillMaxWidth(),
        )

        DifficultyBadge(difficulty = difficulty, diff = Math.abs(targetKg - currentWeight))

        DatePickerRow(
            selectedDateMillis = selectedDateMillis,
            onPickDate = { showDatePicker = true },
            onClearDate = { selectedDateMillis = null },
        )

        Button(
            onClick = {
                onConfirm(
                    GoalParams.BodyWeight(
                        currentWeightKg = currentWeight,
                        targetWeightKg = targetKg,
                        deadlineMillis = selectedDateMillis,
                    )
                )
            },
            enabled = canConfirm,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(14.dp),
        ) {
            Icon(Icons.Default.Flag, null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Установить цель", fontWeight = FontWeight.Bold)
        }
    }

    if (showDatePicker) {
        SimpleDatePickerDialog(
            onConfirm = { selectedDateMillis = it; showDatePicker = false },
            onDismiss = { showDatePicker = false },
        )
    }
}

@Composable
private fun DifficultyBadge(difficulty: WeightGoalDifficulty, diff: Double) {
    val (bg, fg) = when (difficulty) {
        WeightGoalDifficulty.EASY -> Color(0xFF4CAF50).copy(0.15f) to Color(0xFF4CAF50)
        WeightGoalDifficulty.REASONABLE -> Color(0xFF2196F3).copy(0.15f) to Color(0xFF2196F3)
        WeightGoalDifficulty.HARD -> Color(0xFFFF6D00).copy(0.15f) to Color(0xFFFF6D00)
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(bg)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(difficulty.emoji, fontSize = 22.sp)
        Column {
            Text(difficulty.label, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = fg)
            Text("Изменение: ${"%.1f".format(diff)} кг", style = MaterialTheme.typography.bodySmall, color = fg.copy(0.75f))
        }
    }
}

@Composable
private fun WinStreakGoalForm(
    onConfirm: (GoalParams.WinStreak) -> Unit,
    onBack: () -> Unit,
) {
    var selected by remember { mutableStateOf<StreakGoalDuration?>(null) }

    Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            IconButton(onClick = onBack, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.ArrowBack, null, modifier = Modifier.size(20.dp))
            }
            Text("Win Streak 🔥", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
        }

        Text("Выберите продолжительность серии:", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

        StreakGoalDuration.values().forEach { duration ->
            val isSelected = selected == duration
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(0.4f)
                        else MaterialTheme.colorScheme.surfaceVariant
                    )
                    .border(
                        if (isSelected) 2.dp else 0.dp,
                        MaterialTheme.colorScheme.primary,
                        RoundedCornerShape(16.dp),
                    )
                    .clickable { selected = duration }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Text("🔥", fontSize = 24.sp)
                Column(Modifier.weight(1f)) {
                    Text(duration.label, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text("${duration.days} дней подряд", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Default.Check, null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(14.dp))
                    }
                }
            }
        }

        AnimatedVisibility(visible = selected != null, enter = fadeIn(), exit = fadeOut()) {
            Button(
                onClick = { selected?.let { onConfirm(GoalParams.WinStreak(it)) } },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp),
            ) {
                Icon(Icons.Default.Flag, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Установить цель", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun DatePickerRow(
    selectedDateMillis: Long?,
    onPickDate: () -> Unit,
    onClearDate: () -> Unit,
) {
    val dateFormat = remember { SimpleDateFormat("d MMMM yyyy", Locale("ru")) }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OutlinedButton(
            onClick = onPickDate,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp),
        ) {
            Icon(Icons.Default.CalendarToday, null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text(
                selectedDateMillis?.let { dateFormat.format(Date(it)) } ?: "Дата достижения",
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        if (selectedDateMillis != null) {
            IconButton(onClick = onClearDate, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Default.Close, null, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SimpleDatePickerDialog(
    onConfirm: (Long) -> Unit,
    onDismiss: () -> Unit,
) {
    val state = rememberDatePickerState(initialSelectedDateMillis = System.currentTimeMillis())
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { state.selectedDateMillis?.let { onConfirm(it) } ?: onDismiss() }) {
                Text("Выбрать")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } },
    ) {
        DatePicker(state = state)
    }
}