package com.obsession.app.ui.home

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.obsession.app.data.repository.RankState
import com.obsession.app.domain.goals.AddGoalResult
import com.obsession.app.domain.goals.GoalType
import com.obsession.app.domain.goals.UserGoal
import com.obsession.app.domain.model.StrengthRank
import com.obsession.app.ui.goals.AddGoalDialog

@Composable
fun HomeScreen(
    onStartWorkout: (Long) -> Unit,
    onOpenSettings: () -> Unit,
    onOpenRanks: () -> Unit,
    onOpenProfile: () -> Unit,
    onAddGoal: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showAddGoalDialog by remember { mutableStateOf(false) }
    var goalDialogError by remember { mutableStateOf<String?>(null) }

    if (state.achievedGoal != null) {
        GoalAchievedDialog(goal = state.achievedGoal!!, onDismiss = viewModel::dismissAchievedGoal)
    }

    // БАГФИКС: раньше кнопка "Добавить цель" никак не была подключена.
    // Теперь по клику открывается модальное окно выбора типа цели.
    // Если по категории "Вес тела" / "Win Streak" уже есть активная цель —
    // диалог не закрывается, а показывает сообщение об этом.
    if (showAddGoalDialog) {
        AddGoalDialog(
            exercises = state.exercises,
            currentUserWeight = state.userWeightKg,
            errorMessage = goalDialogError,
            onConfirm = { params ->
                viewModel.addGoal(params) { result ->
                    when (result) {
                        AddGoalResult.Success -> {
                            goalDialogError = null
                            showAddGoalDialog = false
                        }
                        AddGoalResult.DuplicateCategory -> {
                            goalDialogError = "Цель по этой категории уже установлена"
                        }
                    }
                }
            },
            onDismiss = {
                showAddGoalDialog = false
                goalDialogError = null
            },
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        item { HomeHeader(userName = state.userName, onOpenProfile = onOpenProfile) }
        item { RankFlipCard(state = state, onOpenRanks = onOpenRanks) }
        item { Spacer(Modifier.height(16.dp)) }
        item {
            // БАГФИКС: кнопка "Начать тренировку" перенесена сразу под карточку
            // ранга (раньше была в самом низу экрана).
            StartWorkoutButton(
                hasActivePlan = state.hasActivePlan,
                onClick = { viewModel.onStartWorkout(onStartWorkout) },
                modifier = Modifier.padding(horizontal = 20.dp),
            )
        }
        item { Spacer(Modifier.height(24.dp)) }
        item { StatsSection(state = state) }
        item { Spacer(Modifier.height(20.dp)) }
        item {
            GoalsSection(
                state = state,
                onAddGoal = { showAddGoalDialog = true },
            )
        }
    }
}

@Composable
private fun HomeHeader(userName: String, onOpenProfile: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(start = 20.dp, end = 12.dp, top = 8.dp, bottom = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text(
                text = "Obsession",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = (-0.5).sp,
            )
            if (userName.isNotBlank()) {
                Text(
                    "Привет, $userName 👋",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        // БАГФИКС: иконка профиля/настроек была утеряна с Главной вкладки.
        IconButton(
            onClick = onOpenProfile,
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Профиль",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/**
 * Единая большая карточка ранга.
 * По умолчанию показывает Общий ранг тела (совпадает со вкладкой "Анализ тела").
 * Отдельная кнопка-иконка в углу карточки -> плавный 3D-переворот на сторону "Ранг в пауэрлифтинге" и обратно.
 * Клик по самой карточке (не по кнопке) -> открывает экран со всеми рангами.
 */
@Composable
private fun RankFlipCard(state: HomeUiState, onOpenRanks: () -> Unit) {
    var flipped by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(
        targetValue = if (flipped) 180f else 0f,
        animationSpec = tween(600, easing = FastOutSlowInEasing),
        label = "rank_card_flip",
    )

    val bodyRank = state.bodyRankState
    val plRank = state.plRankState
    val activeRank = if (rotation <= 90f) bodyRank.currentRank else plRank.currentRank

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(240.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    rotationY = rotation
                    cameraDistance = 14f * density
                }
                .clip(RoundedCornerShape(28.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF0D1B2A),
                            activeRank.primaryColor.copy(alpha = 0.22f),
                            Color(0xFF0A1018),
                        )
                    )
                )
                .border(1.dp, activeRank.primaryColor.copy(0.3f), RoundedCornerShape(28.dp))
                .clickable { onOpenRanks() }
                .padding(20.dp),
        ) {
            if (rotation <= 90f) {
                RankCardFace(
                    rank = bodyRank.currentRank,
                    title = "Общий ранг",
                    subtitle = "тела",
                    stats = null,
                    progress = bodyRank.progress,
                    hint = "Нажмите, чтобы открыть все ранги",
                )
            } else {
                Box(modifier = Modifier.fillMaxSize().graphicsLayer { rotationY = 180f }) {
                    RankCardFace(
                        rank = plRank.currentRank,
                        title = "Ранг в",
                        subtitle = "пауэрлифтинге",
                        stats = Triple(plRank.bench, plRank.squat, plRank.deadlift),
                        progress = plRank.progress,
                        hint = "Нажмите, чтобы открыть все ранги",
                    )
                }
            }
        }

        // Кнопка переворота карточки — не вращается вместе с карточкой,
        // поэтому остаётся в одном и том же месте и не зависит от текущей стороны.
        IconButton(
            onClick = { flipped = !flipped },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(12.dp)
                .size(38.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.12f)),
        ) {
            Icon(
                imageVector = Icons.Default.Autorenew,
                contentDescription = "Перевернуть карточку",
                tint = Color.White.copy(alpha = 0.85f),
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
private fun RankCardFace(
    rank: StrengthRank,
    title: String,
    subtitle: String,
    stats: Triple<Double, Double, Double>?,
    progress: Float,
    hint: String,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, style = MaterialTheme.typography.labelMedium, color = Color.White.copy(0.5f), letterSpacing = 0.5.sp)
            Text(subtitle, style = MaterialTheme.typography.labelMedium, color = Color.White.copy(0.5f), letterSpacing = 0.5.sp)
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(rank.symbol, fontSize = 48.sp)
                Text(
                    rank.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = rank.primaryColor,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                )
            }
            if (stats != null) {
                val (bench, squat, dead) = stats
                Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.weight(1f)) {
                    listOf("Жим" to bench, "Присед" to squat, "Тяга" to dead).forEach { (label, kg) ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(label, style = MaterialTheme.typography.labelMedium, color = Color.White.copy(0.5f))
                            Text("${kg.toInt()} кг", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = rank.primaryColor)
                        }
                    }
                }
            } else {
                Text(
                    rank.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(0.55f),
                    lineHeight = 16.sp,
                    modifier = Modifier.weight(1f),
                )
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Box(modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)).background(Color.White.copy(0.08f))) {
                Box(modifier = Modifier.fillMaxWidth(progress.coerceIn(0f, 1f)).fillMaxHeight().clip(RoundedCornerShape(3.dp)).background(Brush.horizontalGradient(listOf(rank.primaryColor, rank.secondaryColor))))
            }
            Text(hint, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(0.25f), fontSize = 10.sp)
        }
    }
}

@Composable
private fun StatsSection(state: HomeUiState) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            "Статистика тренировок",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatTile(
                icon = "🏋️",
                value = formatTonnage(state.totalTonnageKg),
                label = "Тоннаж",
                color = Color(0xFF7C4DFF),
                modifier = Modifier.weight(1f),
            )
            StatTile(
                icon = "⏱️",
                value = formatDuration(state.totalWorkoutMinutes),
                label = "Общее время",
                color = Color(0xFF2196F3),
                modifier = Modifier.weight(1f),
            )
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatTile(
                icon = "🏆",
                value = state.totalRecords.toString(),
                label = "Рекорды",
                color = Color(0xFFFF9800),
                modifier = Modifier.weight(1f),
            )
            StatTile(
                icon = "⚖️",
                value = if (state.userWeightKg > 0) "${"%.1f".format(state.userWeightKg)} кг" else "—",
                label = "Мой вес",
                color = Color(0xFF4CAF50),
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun StatTile(icon: String, value: String, label: String, color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(Brush.linearGradient(listOf(color.copy(alpha = 0.12f), color.copy(alpha = 0.05f))))
            .border(1.dp, color.copy(alpha = 0.18f), RoundedCornerShape(18.dp))
            .padding(horizontal = 14.dp, vertical = 14.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(icon, fontSize = 22.sp)
            Text(
                value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
                color = color,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

private fun formatTonnage(kg: Double): String {
    return when {
        kg >= 1000.0 -> "${"%.1f".format(kg / 1000.0)} т"
        else -> "${kg.toInt()} кг"
    }
}

private fun formatDuration(minutes: Long): String {
    return when {
        minutes >= 60 -> "${minutes / 60}ч ${minutes % 60}м"
        else -> "${minutes}м"
    }
}

private enum class GoalsFilter(val label: String) {
    IN_PROGRESS("Не выполнены"),
    COMPLETED("Выполнены"),
}

@Composable
private fun GoalsSection(
    state: HomeUiState,
    onAddGoal: () -> Unit,
) {
    // БАГФИКС: кнопка "Все" рядом с "Добавить цель" раньше ничего не делала.
    // Теперь это переключатель между невыполненными и выполненными целями.
    var filter by remember { mutableStateOf(GoalsFilter.IN_PROGRESS) }
    val filteredGoals = remember(state.goals, filter) {
        when (filter) {
            GoalsFilter.IN_PROGRESS -> state.goals.filter { !it.isCompleted }
            GoalsFilter.COMPLETED -> state.goals.filter { it.isCompleted }
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "Ваши цели",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
            )
            SmallAddGoalButton(onClick = onAddGoal)
        }

        if (state.goals.isNotEmpty()) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                GoalsFilter.entries.forEach { f ->
                    GoalsFilterChip(
                        label = f.label,
                        selected = filter == f,
                        onClick = { filter = f },
                    )
                }
            }
        }

        when {
            state.goals.isEmpty() -> EmptyGoalsPlaceholder(onAddGoal = onAddGoal)
            filteredGoals.isEmpty() -> EmptyFilteredGoalsPlaceholder(filter = filter)
            // БАГФИКС: раньше показывались только первые 3 цели (.take(3)),
            // остальные были не видны и никак не доступны. Теперь все цели
            // помещаются в панель фиксированной высоты (видно ~4 карточки),
            // а если их больше — панель скроллится по вертикали, не растягивая
            // главный экран.
            else -> GoalsPanel(goals = filteredGoals)
        }
    }
}

/** Высота одной карточки цели + отступ между карточками. */
private val GoalCardHeight = 108.dp
private val GoalCardSpacing = 10.dp
private const val VisibleGoalCards = 4

@Composable
private fun GoalsPanel(goals: List<UserGoal>) {
    val maxPanelHeight = GoalCardHeight * VisibleGoalCards + GoalCardSpacing * (VisibleGoalCards - 1)

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = maxPanelHeight),
        verticalArrangement = Arrangement.spacedBy(GoalCardSpacing),
    ) {
        items(goals, key = { it.id }) { goal ->
            GoalCard(goal = goal)
        }
    }
}

@Composable
private fun GoalsFilterChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (selected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.surfaceVariant
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = if (selected) MaterialTheme.colorScheme.onPrimary
            else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun EmptyFilteredGoalsPlaceholder(filter: GoalsFilter) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            if (filter == GoalsFilter.COMPLETED) "Пока нет выполненных целей"
            else "Нет целей в процессе",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun SmallAddGoalButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.primary)
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Icon(Icons.Default.Add, null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(16.dp))
            Text("Цель", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
        }
    }
}

@Composable
private fun EmptyGoalsPlaceholder(onAddGoal: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable { onAddGoal() }
            .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("🎯", fontSize = 32.sp)
            Text("Добавьте первую цель", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Text("Сила, вес тела или серия тренировок", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun GoalCard(goal: UserGoal) {
    val accentColor = goalColor(goal)
    val progress = goal.progress.coerceIn(0f, 1f)
    val isDone = goal.isCompleted

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(
                if (isDone)
                    Brush.linearGradient(listOf(Color(0xFF1B5E20).copy(0.4f), Color(0xFF2E7D32).copy(0.2f)))
                else
                    Brush.linearGradient(listOf(accentColor.copy(0.10f), accentColor.copy(0.04f)))
            )
            .border(1.dp, if (isDone) Color(0xFF4CAF50).copy(0.4f) else accentColor.copy(0.18f), RoundedCornerShape(18.dp))
            .padding(14.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(goalIcon(goal), fontSize = 20.sp)
                Column(modifier = Modifier.weight(1f)) {
                    Text(goal.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Text(goal.subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (isDone) {
                    Box(modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(Color(0xFF4CAF50).copy(0.2f)).padding(horizontal = 8.dp, vertical = 3.dp)) {
                        Text("✓ Выполнено", style = MaterialTheme.typography.labelSmall, color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold, fontSize = 10.sp)
                    }
                } else {
                    Text("${(progress * 100).toInt()}%", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = accentColor)
                }
            }

            if (!isDone) {
                Box(modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)).background(MaterialTheme.colorScheme.outlineVariant.copy(0.3f))) {
                    Box(modifier = Modifier.fillMaxWidth(progress).fillMaxHeight().clip(RoundedCornerShape(3.dp)).background(Brush.horizontalGradient(listOf(accentColor, accentColor.copy(0.6f)))))
                }
            }

            goal.deadline?.let { deadline ->
                Text(
                    "Цель к: $deadline",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.6f),
                )
            }
        }
    }
}

private fun goalColor(goal: UserGoal): Color = when (goal.type) {
    GoalType.STRENGTH -> Color(0xFF7C4DFF)
    GoalType.BODY_WEIGHT -> Color(0xFF4CAF50)
    GoalType.WIN_STREAK -> Color(0xFFFF6D00)
}

private fun goalIcon(goal: UserGoal): String = when (goal.type) {
    GoalType.STRENGTH -> "💪"
    GoalType.BODY_WEIGHT -> "⚖️"
    GoalType.WIN_STREAK -> "🔥"
}

@Composable
private fun StartWorkoutButton(hasActivePlan: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().height(60.dp),
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
        ),
    ) {
        Icon(Icons.Default.PlayArrow, null, modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(10.dp))
        Text(
            if (hasActivePlan) "Начать тренировку" else "Выбрать план",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
        )
    }
}

@Composable
private fun GoalAchievedDialog(goal: UserGoal, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text("🎉", fontSize = 48.sp)
                Spacer(Modifier.height(8.dp))
                Text("Цель достигнута!", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary, textAlign = TextAlign.Center)
            }
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(goal.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                Spacer(Modifier.height(4.dp))
                Text(goal.subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                Text("Отлично! 💪", fontWeight = FontWeight.Bold)
            }
        },
        shape = RoundedCornerShape(24.dp),
    )
}