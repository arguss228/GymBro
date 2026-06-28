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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.obsession.app.data.repository.RankState
import com.obsession.app.domain.goals.GoalType
import com.obsession.app.domain.goals.UserGoal
import com.obsession.app.domain.model.StrengthRank

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

    if (state.achievedGoal != null) {
        GoalAchievedDialog(goal = state.achievedGoal!!, onDismiss = viewModel::dismissAchievedGoal)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        item { HomeHeader(userName = state.userName) }
        item { RankCardsSection(state = state, onViewRank = onOpenRanks) }
        item { Spacer(Modifier.height(20.dp)) }
        item { StatsSection(state = state) }
        item { Spacer(Modifier.height(20.dp)) }
        item {
            GoalsSection(
                state = state,
                onAddGoal = onAddGoal,
                onViewGoals = {},
            )
        }
        item { Spacer(Modifier.height(20.dp)) }
        item {
            StartWorkoutButton(
                hasActivePlan = state.hasActivePlan,
                onClick = { viewModel.onStartWorkout(onStartWorkout) },
                modifier = Modifier.padding(horizontal = 20.dp),
            )
        }
    }
}

@Composable
private fun HomeHeader(userName: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 16.dp),
    ) {
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
}

@Composable
private fun RankCardsSection(state: HomeUiState, onViewRank: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        BodyRankCard(
            rankState = state.bodyRankState,
            modifier = Modifier.weight(1f),
            onClick = onViewRank,
        )
        PowerliftingRankCard(
            rankState = state.plRankState,
            modifier = Modifier.weight(1f),
            onClick = onViewRank,
        )
    }
}

@Composable
private fun BodyRankCard(rankState: RankState, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val rank = rankState.currentRank
    FlippableRankCard(
        modifier = modifier,
        rank = rank,
        title = "Общий ранг",
        subtitle = "тела",
        stats = null,
        progress = rankState.progress,
        onClick = onClick,
    )
}

@Composable
private fun PowerliftingRankCard(rankState: RankState, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val rank = rankState.currentRank
    FlippableRankCard(
        modifier = modifier,
        rank = rank,
        title = "Ранг в",
        subtitle = "Пауэрлифтинге",
        stats = Triple(rankState.bench, rankState.squat, rankState.deadlift),
        progress = rankState.progress,
        onClick = onClick,
    )
}

@Composable
private fun FlippableRankCard(
    rank: StrengthRank,
    title: String,
    subtitle: String,
    stats: Triple<Double, Double, Double>?,
    progress: Float,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    var flipped by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(
        targetValue = if (flipped) 180f else 0f,
        animationSpec = tween(500, easing = FastOutSlowInEasing),
        label = "card_flip",
    )

    Box(
        modifier = modifier
            .height(220.dp)
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 12f * density
            }
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(0xFF0D1B2A), rank.primaryColor.copy(alpha = 0.22f), Color(0xFF0A1018))
                )
            )
            .border(1.dp, rank.primaryColor.copy(0.3f), RoundedCornerShape(24.dp))
            .clickable { flipped = !flipped; onClick() }
            .padding(14.dp),
    ) {
        if (rotation <= 90f) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(title, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(0.45f), letterSpacing = 0.5.sp)
                    Text(subtitle, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(0.45f), letterSpacing = 0.5.sp)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text(rank.symbol, fontSize = 38.sp)
                    Text(rank.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = rank.primaryColor, textAlign = TextAlign.Center, maxLines = 1)
                }
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Box(modifier = Modifier.fillMaxWidth().height(5.dp).clip(RoundedCornerShape(2.5.dp)).background(Color.White.copy(0.08f))) {
                        Box(modifier = Modifier.fillMaxWidth(progress.coerceIn(0f, 1f)).fillMaxHeight().clip(RoundedCornerShape(2.5.dp)).background(Brush.horizontalGradient(listOf(rank.primaryColor, rank.secondaryColor))))
                    }
                    Text("Нажмите для переворота", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(0.2f), fontSize = 9.sp)
                }
            }
        } else {
            Box(modifier = Modifier.fillMaxSize().graphicsLayer { rotationY = 180f }) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(rank.description, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(0.55f), lineHeight = 16.sp)
                    if (stats != null) {
                        val (bench, squat, dead) = stats
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf("Жим" to bench, "Присед" to squat, "Тяга" to dead).forEach { (label, kg) ->
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(label, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(0.45f))
                                    Text("${kg.toInt()} кг", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = rank.primaryColor)
                                }
                            }
                        }
                    }
                    Text("Нажмите снова", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(0.2f), fontSize = 9.sp)
                }
            }
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
            "Последние 14 тренировок",
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
                label = "Время",
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

@Composable
private fun GoalsSection(
    state: HomeUiState,
    onAddGoal: () -> Unit,
    onViewGoals: () -> Unit,
) {
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
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (state.goals.isNotEmpty()) {
                    TextButton(onClick = onViewGoals) {
                        Text("Все", style = MaterialTheme.typography.labelLarge)
                    }
                }
                SmallAddGoalButton(onClick = onAddGoal)
            }
        }

        if (state.goals.isEmpty()) {
            EmptyGoalsPlaceholder(onAddGoal = onAddGoal)
        } else {
            state.goals.take(3).forEach { goal ->
                GoalCard(goal = goal)
            }
        }
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