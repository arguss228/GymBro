package com.gymbro.app.ui.bodyrank

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gymbro.app.data.local.entity.ExerciseEntity
import com.gymbro.app.domain.model.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BodyAnalysisScreen(
    onBack: () -> Unit,
    viewModel: BodyAnalysisViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    // Диалог добавления/редактирования
    if (state.showAddDialog) {
        AddExerciseRankDialog(
            exercises     = state.allExercises,
            selectedEx    = state.dialogExercise,
            weightInput   = state.dialogWeightInput,
            onSelectEx    = viewModel::setDialogExercise,
            onWeightChange = viewModel::setDialogWeight,
            onSave        = viewModel::saveDialogData,
            onDismiss     = viewModel::closeDialog,
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Анализ тела", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.openAddDialog() }) {
                        Icon(Icons.Default.Add, "Добавить данные", tint = MaterialTheme.colorScheme.primary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
    ) { inner ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize().padding(inner), Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        val bodyRank = state.bodyRank
        if (bodyRank == null) return@Scaffold

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(inner),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Общий ранг
            item {
                OverallRankCard(bodyRank = bodyRank, onAddData = { viewModel.openAddDialog() })
            }

            // Группируем мышечные группы по части тела
            val grouped = bodyRank.muscleGroups.groupBy { it.bodyPartName }
            val bodyPartOrder = listOf("Руки", "Ноги", "Кор", "Плечи", "Грудь", "Спина")

            items(bodyPartOrder.filter { grouped.containsKey(it) }) { bodyPart ->
                val groups = grouped[bodyPart] ?: return@items
                BodyPartSection(
                    bodyPartName = bodyPart,
                    muscleGroups = groups,
                    onExerciseClick = { ex -> viewModel.openAddDialog(ex) },
                )
            }

            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

// ── Карточка общего ранга ─────────────────────────────────────────

@Composable
private fun OverallRankCard(bodyRank: UserBodyRank, onAddData: () -> Unit) {
    val rank = bodyRank.overallRank
    val infiniteTransition = androidx.compose.animation.core.rememberInfiniteTransition(label = "body_glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue  = 0.3f,
        targetValue   = 0.7f,
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
            androidx.compose.animation.core.tween(2000, easing = androidx.compose.animation.core.EaseInOutSine),
            androidx.compose.animation.core.RepeatMode.Reverse,
        ),
        label = "body_glow_alpha",
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.linearGradient(listOf(Color(0xFF0D1B2A), rank.primaryColor.copy(alpha = 0.15f), Color(0xFF070B14)))
            )
            .border(
                1.5.dp,
                Brush.linearGradient(listOf(rank.primaryColor.copy(alpha = 0.6f), rank.secondaryColor.copy(alpha = 0.3f))),
                RoundedCornerShape(24.dp),
            )
            .padding(24.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            // Заголовок
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(rank.symbol, fontSize = 56.sp)
                Column(Modifier.weight(1f)) {
                    Text(
                        "Общий ранг тела",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White.copy(alpha = 0.5f),
                    )
                    Text(
                        rank.name,
                        style      = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black,
                        color      = rank.primaryColor,
                    )
                }
            }

            if (!bodyRank.hasData) {
                Text(
                    "Добавьте данные об упражнениях, чтобы рассчитать ваш ранг",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.55f),
                )
            }

            // Прогресс
            bodyRank.nextRank?.let { next ->
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("До ${next.symbol} ${next.name}", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(0.5f))
                        Text("${(bodyRank.progressToNext * 100).toInt()}%", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = rank.primaryColor)
                    }
                    Box(
                        Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)).background(Color.White.copy(0.08f))
                    ) {
                        Box(
                            Modifier
                                .fillMaxWidth(bodyRank.progressToNext.coerceIn(0f, 1f))
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(4.dp))
                                .background(Brush.horizontalGradient(listOf(rank.primaryColor, rank.secondaryColor)))
                        )
                    }
                }
            }

            // Кнопка добавить данные
            OutlinedButton(
                onClick = onAddData,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = rank.primaryColor),
                border = androidx.compose.foundation.BorderStroke(1.dp, rank.primaryColor.copy(0.5f)),
            ) {
                Icon(Icons.Default.Add, null, Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Добавить / обновить данные", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

// ── Секция части тела ─────────────────────────────────────────────

@Composable
private fun BodyPartSection(
    bodyPartName: String,
    muscleGroups: List<MuscleGroupRank>,
    onExerciseClick: (ExerciseEntity) -> Unit,
) {
    val bodyPartIcon = when (bodyPartName) {
        "Руки"   -> "💪"
        "Ноги"   -> "🦵"
        "Кор"    -> "🎯"
        "Плечи"  -> "🏋️"
        "Грудь"  -> "❤️"
        "Спина"  -> "🔰"
        else     -> "•"
    }

    Card(
        shape  = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Заголовок секции
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(bodyPartIcon, fontSize = 28.sp)
                Text(
                    bodyPartName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(0.4f))

            muscleGroups.forEach { group ->
                MuscleGroupRow(group = group)
            }
        }
    }
}

@Composable
private fun MuscleGroupRow(group: MuscleGroupRank) {
    var expanded by remember { mutableStateOf(false) }
    val rank = group.rank

    Column(
        Modifier
            .fillMaxWidth()
            .animateContentSize()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .clickable { if (group.exerciseRanks.isNotEmpty()) expanded = !expanded }
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Символ ранга
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(
                        if (rank != null) rank.primaryColor.copy(alpha = 0.15f)
                        else MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    rank?.symbol ?: "?",
                    fontSize = if (rank != null) 20.sp else 18.sp,
                    textAlign = TextAlign.Center,
                )
            }

            Column(Modifier.weight(1f)) {
                Text(
                    group.displayName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    if (rank != null) "${rank.name} (${group.exerciseRanks.size} упр.)"
                    else "Нет данных",
                    style = MaterialTheme.typography.bodySmall,
                    color = rank?.primaryColor ?: MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            if (group.exerciseRanks.isNotEmpty()) {
                Icon(
                    if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp),
                )
            }
        }

        // Раскрытые упражнения
        if (expanded) {
            Column(
                modifier = Modifier.padding(start = 56.dp, end = 8.dp, bottom = 4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                group.exerciseRanks.forEach { exRank ->
                    val exRankObj = exRank.rank
                    Row(
                        Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            exRank.exerciseName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f),
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Text(
                                "${exRank.best1Rm.toInt()} кг",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                exRankObj.symbol,
                                fontSize = 14.sp,
                            )
                            Text(
                                exRankObj.name,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = exRankObj.primaryColor,
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── Диалог добавления/редактирования ─────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddExerciseRankDialog(
    exercises: List<ExerciseEntity>,
    selectedEx: ExerciseEntity?,
    weightInput: String,
    onSelectEx: (ExerciseEntity) -> Unit,
    onWeightChange: (String) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit,
) {
    var searchQuery by remember { mutableStateOf("") }
    val filtered = remember(searchQuery, exercises) {
        if (searchQuery.isBlank()) exercises
        else exercises.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    "Добавить / обновить данные",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )

                // Поиск упражнения
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Поиск упражнения") },
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                )

                // Список упражнений
                if (selectedEx == null) {
                    Card(
                        modifier = Modifier.fillMaxWidth().heightIn(max = 250.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    ) {
                        androidx.compose.foundation.lazy.LazyColumn {
                            items(filtered.take(10)) { ex ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onSelectEx(ex) }
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                ) {
                                    Icon(Icons.Default.FitnessCenter, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                    Column(Modifier.weight(1f)) {
                                        Text(ex.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                        Text(ex.primaryMuscle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(0.3f), modifier = Modifier.padding(horizontal = 12.dp))
                            }
                        }
                    }
                } else {
                    // Выбранное упражнение + поле ввода веса
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(0.3f))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Icon(Icons.Default.FitnessCenter, null, tint = MaterialTheme.colorScheme.primary)
                        Text(selectedEx.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        IconButton(onClick = { onSelectEx(exercises.first()) }, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Close, null, modifier = Modifier.size(16.dp))
                        }
                    }

                    OutlinedTextField(
                        value = weightInput,
                        onValueChange = { if (it.all { c -> c.isDigit() || c == '.' }) onWeightChange(it) },
                        label = { Text("Вес на 1 повторение") },
                        suffix = { Text("кг") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                    )

                    // Предварительный ранг
                    val previewRankIdx = weightInput.toDoubleOrNull()?.let {
                        ExerciseRankThresholds.rankIndexFor(selectedEx.name, it)
                    }
                    if (previewRankIdx != null) {
                        val previewRank = StrengthRanks.all[previewRankIdx]
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(previewRank.primaryColor.copy(0.1f))
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text(previewRank.symbol, fontSize = 24.sp)
                            Column {
                                Text("Ранг упражнения", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(previewRank.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = previewRank.primaryColor)
                            }
                        }
                    }
                }

                // Кнопки
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("Отмена") }
                    Button(
                        onClick = onSave,
                        modifier = Modifier.weight(1f),
                        enabled = selectedEx != null && weightInput.toDoubleOrNull() != null,
                        shape = RoundedCornerShape(12.dp),
                    ) { Text("Сохранить") }
                }
            }
        }
    }
}