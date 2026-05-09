package com.gymbro.app.ui.home

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gymbro.app.domain.model.RankGroup
import com.gymbro.app.domain.model.StrengthRank
import com.gymbro.app.ui.dashboard.DashboardViewModel
import com.gymbro.app.ui.rank.RankUpDialog

// Типы целей
private enum class GoalType(val label: String, val icon: String, val description: String) {
    STRENGTH("Цель по силе", "🏋️", "Новый максимум в упражнении"),
    WEIGHT("Цель по весу тела", "⚖️", "Достичь желаемого веса"),
    WIN_STREAK("Win Streak", "🔥", "Серия последовательных тренировок"),
}

@Composable
fun HomeScreen(
    onStartWorkout: (Long) -> Unit,
    onOpenSettings: () -> Unit,
    onOpenRanks: () -> Unit,
    onOpenProfile: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showGoalDialog by remember { mutableStateOf(false) }
    var goals by remember { mutableStateOf<List<String>>(emptyList()) }

    state.rankUpEvent?.let { newRank ->
        RankUpDialog(newRank = newRank, onDismiss = { viewModel.dismissRankUp() })
    }

    if (showGoalDialog) {
        GoalTypeDialog(
            onDismiss = { showGoalDialog = false },
            onSelectGoalType = { type ->
                goals = goals + type.label
                showGoalDialog = false
            },
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState()),
    ) {
        // ── Header: "OBSESSION" + Profile ────────────────────────
        ObsessionHeader(onOpenProfile = onOpenProfile)

        // ── Flip Rank Card ────────────────────────────────────────
        FlipRankCard(
            overallRank = state.rankState.currentRank,
            overallProgress = state.rankState.progress,
            overallNextRank = state.rankState.nextRank,
            plRank = state.rankState.currentRank,
            plProgress = state.rankState.progress,
            plNextRank = state.rankState.nextRank,
            bench = state.rankState.bench,
            squat = state.rankState.squat,
            deadlift = state.rankState.deadlift,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
        )

        // ── CTA ───────────────────────────────────────────────────
        Button(
            onClick = { viewModel.onStartWorkout(onStartWorkout) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .height(68.dp),
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp),
        ) {
            Icon(Icons.Default.PlayArrow, null, modifier = Modifier.size(30.dp))
            Spacer(Modifier.size(10.dp))
            Text("Начать тренировку", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
        }

        // ── Ваши цели ─────────────────────────────────────────────
        GoalsSection(
            goals = goals,
            onAddGoal = { showGoalDialog = true },
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 20.dp),
        )

        // ── Последние 14 тренировок (статистика) ──────────────────
        Last14WorkoutsSection(
            totalSessions = state.totalSessions,
            currentWeight = state.rankState.bench, // заглушка — реальный вес придёт из профиля
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
        )

        Spacer(Modifier.height(32.dp))
    }
}

// ════════════════════════════════════════════════════════════════
//  OBSESSION HEADER
// ════════════════════════════════════════════════════════════════

@Composable
private fun ObsessionHeader(onOpenProfile: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 56.dp, start = 20.dp, end = 20.dp, bottom = 8.dp),
    ) {
        Column(modifier = Modifier.align(Alignment.CenterStart)) {
            Text(
                "OBSESSION",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontSize = 42.sp,
                    letterSpacing = 6.sp,
                ),
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                "Добейся своего",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.45f),
                letterSpacing = 1.sp,
            )
        }

        // Profile icon (заменяет Settings)
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(46.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clickable { onOpenProfile() },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Default.Person,
                "Профиль",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp),
            )
        }
    }
}

// ════════════════════════════════════════════════════════════════
//  FLIP RANK CARD
// ════════════════════════════════════════════════════════════════

@Composable
private fun FlipRankCard(
    overallRank: StrengthRank,
    overallProgress: Float,
    overallNextRank: StrengthRank?,
    plRank: StrengthRank,
    plProgress: Float,
    plNextRank: StrengthRank?,
    bench: Double,
    squat: Double,
    deadlift: Double,
    modifier: Modifier = Modifier,
) {
    var isFlipped by remember { mutableStateOf(false) }

    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "card_flip",
    )

    val isFrontVisible = rotation < 90f

    Box(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 12f * density
            },
    ) {
        if (isFrontVisible) {
            // Передняя сторона — Общий ранг тела
            OverallRankFace(
                rank = overallRank,
                progress = overallProgress,
                nextRank = overallNextRank,
                onFlip = { isFlipped = true },
            )
        } else {
            // Задняя сторона — Ранг в Пауэрлифтинге
            Box(
                modifier = Modifier.graphicsLayer { rotationY = 180f },
            ) {
                PowerliftingRankFace(
                    rank = plRank,
                    progress = plProgress,
                    nextRank = plNextRank,
                    bench = bench,
                    squat = squat,
                    deadlift = deadlift,
                    onFlip = { isFlipped = false },
                )
            }
        }
    }
}

@Composable
private fun OverallRankFace(
    rank: StrengthRank,
    progress: Float,
    nextRank: StrengthRank?,
    onFlip: () -> Unit,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "overall_glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.65f,
        animationSpec = infiniteRepeatable(tween(2200, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "glow",
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(32.dp))
            .background(
                Brush.linearGradient(listOf(Color(0xFF0D1B2A), rank.primaryColor.copy(alpha = 0.18f), Color(0xFF070B14)))
            )
            .border(
                2.dp,
                Brush.linearGradient(listOf(rank.primaryColor.copy(alpha = 0.7f), rank.secondaryColor.copy(alpha = 0.35f))),
                RoundedCornerShape(32.dp),
            )
            .drawBehind { drawCircle(rank.glowColor.copy(alpha = glowAlpha * 0.15f), radius = size.width * 0.8f) }
            .padding(28.dp),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            // Label + flip button
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "ОБЩИЙ РАНГ ТЕЛА",
                    style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 2.sp),
                    color = Color.White.copy(alpha = 0.45f),
                )
                IconButton(
                    onClick = onFlip,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(rank.primaryColor.copy(alpha = 0.15f)),
                ) {
                    Icon(Icons.Default.Refresh, "Перевернуть", tint = rank.primaryColor, modifier = Modifier.size(18.dp))
                }
            }

            // Symbol
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .drawBehind {
                        drawCircle(rank.glowColor.copy(alpha = glowAlpha * 0.5f), radius = size.minDimension / 2f + 18.dp.toPx())
                        drawCircle(rank.primaryColor.copy(alpha = 0.15f), radius = size.minDimension / 2f)
                    },
                contentAlignment = Alignment.Center,
            ) {
                Text(rank.symbol, fontSize = 60.sp, textAlign = TextAlign.Center)
            }

            // Name + description
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    if (rank.group == RankGroup.EARTH) "ЗЕМНАЯ ГРУППА" else "НЕБЕСНАЯ ГРУППА",
                    style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 3.sp),
                    color = Color.White.copy(alpha = 0.4f),
                )
                Text(
                    rank.name,
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Black,
                    color = rank.primaryColor,
                    textAlign = TextAlign.Center,
                )
                Text(
                    rank.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.55f),
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp,
                )
            }

            // Progress
            if (nextRank != null) {
                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("До ${nextRank.symbol} ${nextRank.name}", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.5f))
                        Text("${(progress * 100).toInt()}%", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = rank.primaryColor)
                    }
                    Box(
                        modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp)).background(Color.White.copy(alpha = 0.07f)),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(progress.coerceIn(0f, 1f))
                                .height(10.dp)
                                .clip(RoundedCornerShape(5.dp))
                                .background(Brush.horizontalGradient(listOf(rank.primaryColor, rank.secondaryColor))),
                        )
                    }
                }
            }

            Text(
                "Нажмите 🔄 для ранга в пауэрлифтинге",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.22f),
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun PowerliftingRankFace(
    rank: StrengthRank,
    progress: Float,
    nextRank: StrengthRank?,
    bench: Double,
    squat: Double,
    deadlift: Double,
    onFlip: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(32.dp))
            .background(
                Brush.linearGradient(listOf(Color(0xFF070B14), rank.primaryColor.copy(alpha = 0.2f), Color(0xFF0D1B2A)))
            )
            .border(
                2.dp,
                Brush.linearGradient(listOf(rank.secondaryColor.copy(alpha = 0.7f), rank.primaryColor.copy(alpha = 0.35f))),
                RoundedCornerShape(32.dp),
            )
            .padding(28.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "РАНГ В ПАУЭРЛИФТИНГЕ",
                    style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 2.sp),
                    color = Color.White.copy(alpha = 0.45f),
                )
                IconButton(
                    onClick = onFlip,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(rank.primaryColor.copy(alpha = 0.15f)),
                ) {
                    Icon(Icons.Default.Refresh, "Перевернуть", tint = rank.primaryColor, modifier = Modifier.size(18.dp))
                }
            }

            // Symbol + name
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(rank.symbol, fontSize = 52.sp)
                Column {
                    Text(rank.name, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black, color = rank.primaryColor)
                    Text(rank.description, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.5f), maxLines = 2)
                }
            }

            // 1RM chips
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("🏋️ Жим" to bench, "🦵 Присед" to squat, "⬆️ Тяга" to deadlift).forEach { (label, kg) ->
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(rank.primaryColor.copy(alpha = 0.1f))
                            .padding(horizontal = 6.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(label, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.45f), maxLines = 1)
                        Text("${kg.toInt()} кг", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Black, color = Color.White)
                    }
                }
            }

            // Progress bar
            if (nextRank != null) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("До ${nextRank.symbol} ${nextRank.name}", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.45f))
                        Text("${(progress * 100).toInt()}%", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = rank.primaryColor)
                    }
                    Box(
                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)).background(Color.White.copy(alpha = 0.07f)),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(progress.coerceIn(0f, 1f))
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(4.dp))
                                .background(Brush.horizontalGradient(listOf(rank.primaryColor, rank.secondaryColor))),
                        )
                    }
                }
            }
        }
    }
}

// ════════════════════════════════════════════════════════════════
//  GOALS SECTION
// ════════════════════════════════════════════════════════════════

@Composable
private fun GoalsSection(
    goals: List<String>,
    onAddGoal: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(
                "Ваши цели",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Button(
                onClick = onAddGoal,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), contentColor = MaterialTheme.colorScheme.primary),
                elevation = ButtonDefaults.buttonElevation(0.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
            ) {
                Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Добавить цель", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            }
        }

        if (goals.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
                    .padding(24.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("🎯", fontSize = 32.sp)
                    Text(
                        "Добавьте первую цель",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        } else {
            goals.forEach { goal ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Icon(Icons.Default.Flag, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    Text(goal, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

// ════════════════════════════════════════════════════════════════
//  LAST 14 WORKOUTS STATS
// ════════════════════════════════════════════════════════════════

@Composable
private fun Last14WorkoutsSection(
    totalSessions: Int,
    currentWeight: Double,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            "Последние 14 тренировок",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onBackground,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            StatCard14(
                title = "Тоннаж",
                value = "0 кг",
                subtitle = "суммарный вес",
                emoji = "🏋️",
                accentColor = Color(0xFF2979FF),
                modifier = Modifier.weight(1f),
            )
            StatCard14(
                title = "Время",
                value = "0 мин",
                subtitle = "в зале",
                emoji = "⏱️",
                accentColor = Color(0xFF4CAF50),
                modifier = Modifier.weight(1f),
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            StatCard14(
                title = "Рекорды",
                value = "0",
                subtitle = "PR установлено",
                emoji = "🏆",
                accentColor = Color(0xFFF9A825),
                modifier = Modifier.weight(1f),
            )
            StatCard14(
                title = "Мой вес",
                value = if (currentWeight > 0) "${currentWeight.toInt()} кг" else "—",
                subtitle = "текущий",
                emoji = "⚖️",
                accentColor = Color(0xFFFF6D00),
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun StatCard14(
    title: String,
    value: String,
    subtitle: String,
    emoji: String,
    accentColor: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(1.dp, accentColor.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
            .padding(16.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(emoji, fontSize = 22.sp)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(accentColor.copy(alpha = 0.12f))
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                ) {
                    Text(title, style = MaterialTheme.typography.labelSmall, color = accentColor, fontWeight = FontWeight.Bold)
                }
            }
            Text(
                value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

// ════════════════════════════════════════════════════════════════
//  GOAL TYPE DIALOG
// ════════════════════════════════════════════════════════════════

@Composable
private fun GoalTypeDialog(
    onDismiss: () -> Unit,
    onSelectGoalType: (GoalType) -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .clip(RoundedCornerShape(28.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(24.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    "Выберите тип цели",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                GoalType.values().forEach { goalType ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { onSelectGoalType(goalType) }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        Text(goalType.icon, fontSize = 32.sp)
                        Column(Modifier.weight(1f)) {
                            Text(goalType.label, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            Text(goalType.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) {
                    Text("Отмена")
                }
            }
        }
    }
}