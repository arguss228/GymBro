package com.obsession.app.ui.workout

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.obsession.app.data.local.entity.WorkoutPlanEntity
import com.obsession.app.ui.exercises.ExercisesScreen
import com.obsession.app.ui.workouts.WorkoutsTabUiState
import com.obsession.app.ui.workouts.WorkoutsTabViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutsTabScreen(
    onCreatePlan: () -> Unit,
    onEditPlan: (Long) -> Unit,
    onViewPlan: (Long) -> Unit,
    onExerciseClick: (Long) -> Unit,
    viewModel: WorkoutsTabViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

    Column(modifier = Modifier.fillMaxSize()) {
        WorkoutsHeader(selectedTab = selectedTab, onTabSelect = { selectedTab = it })

        when (selectedTab) {
            0 -> PlansTab(
                state = state,
                onCreatePlan = onCreatePlan,
                onEditPlan = onEditPlan,
                onViewPlan = onViewPlan,
                onDelete = viewModel::deletePlan,
            )
            1 -> ExercisesScreen(
                onBack = { /* embedded */ },
                onExerciseClick = onExerciseClick,
                isEmbedded = true,
            )
        }
    }
}

@Composable
private fun WorkoutsHeader(selectedTab: Int, onTabSelect: (Int) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 0.dp),
    ) {
        Text(
            "Тренировки",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(14.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            listOf("Планы", "Упражнения").forEachIndexed { index, title ->
                val isSelected = selectedTab == index
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                        .clickable { onTabSelect(index) }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        Spacer(Modifier.height(4.dp))
    }
}

@Composable
private fun PlansTab(
    state: WorkoutsTabUiState,
    onCreatePlan: () -> Unit,
    onEditPlan: (Long) -> Unit,
    onViewPlan: (Long) -> Unit,
    onDelete: (Long) -> Unit,
) {
    val beginnerPresets = state.allPlans.filter { it.isPreset && it.minLevel <= 4 }
    val intermediatePresets = state.allPlans.filter { it.isPreset && it.minLevel in 5..9 }
    val advancedPresets = state.allPlans.filter { it.isPreset && it.minLevel >= 10 }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 120.dp, top = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        if (beginnerPresets.isNotEmpty()) {
            item { PlanSectionHeader("Новичок", "Базовые программы · 3–4 дня/нед", "🌱", Color(0xFF4CAF50)) }
            item {
                PresetPlanRow(
                    plans = beginnerPresets,
                    activePlanId = state.activePlanId,
                    onViewPlan = onViewPlan,
                    gradientPool = listOf(
                        listOf(Color(0xFF4CAF50), Color(0xFF66BB6A), Color(0xFF1B5E20)),
                        listOf(Color(0xFF2196F3), Color(0xFF42A5F5), Color(0xFF0D47A1)),
                        listOf(Color(0xFFFF6B35), Color(0xFFFF8C69), Color(0xFFC0392B)),
                        listOf(Color(0xFF9C27B0), Color(0xFFCE93D8), Color(0xFF4A148C)),
                    ),
                    emojiPool = listOf("🌱", "🏋️", "🔥", "⚡"),
                )
            }
            item { Spacer(Modifier.height(8.dp)) }
        }

        if (intermediatePresets.isNotEmpty()) {
            item { PlanSectionHeader("Средний", "Сплит-программы · 4–5 дней/нед", "💎", Color(0xFF2196F3)) }
            item {
                PresetPlanRow(
                    plans = intermediatePresets,
                    activePlanId = state.activePlanId,
                    onViewPlan = onViewPlan,
                    gradientPool = listOf(
                        listOf(Color(0xFF1565C0), Color(0xFF42A5F5), Color(0xFF0A2472)),
                        listOf(Color(0xFF2E7D32), Color(0xFF81C784), Color(0xFF1B5E20)),
                        listOf(Color(0xFF7B1FA2), Color(0xFFBA68C8), Color(0xFF4A148C)),
                    ),
                    emojiPool = listOf("💎", "🎯", "🌊"),
                )
            }
            item { Spacer(Modifier.height(8.dp)) }
        }

        if (advancedPresets.isNotEmpty()) {
            item { PlanSectionHeader("Опытный", "PPL и силовые · 5–6 дней/нед", "⚡", Color(0xFFFF9800)) }
            item {
                PresetPlanRow(
                    plans = advancedPresets,
                    activePlanId = state.activePlanId,
                    onViewPlan = onViewPlan,
                    gradientPool = listOf(
                        listOf(Color(0xFFFF6F00), Color(0xFFFFAB00), Color(0xFFE65100)),
                        listOf(Color(0xFF006064), Color(0xFF00ACC1), Color(0xFF004D40)),
                        listOf(Color(0xFF37474F), Color(0xFF78909C), Color(0xFF263238)),
                        listOf(Color(0xFF880E4F), Color(0xFFEC407A), Color(0xFF4A148C)),
                    ),
                    emojiPool = listOf("🔱", "👑", "⚔️", "🏆"),
                )
            }
            item { Spacer(Modifier.height(8.dp)) }
        }

        item { PlanSectionHeader("Мои планы", "Созданные вами", "✏️", Color(0xFF7C4DFF)) }
        item {
            UserPlansRow(
                plans = state.userPlans,
                activePlanId = state.activePlanId,
                onCreatePlan = onCreatePlan,
                onViewPlan = onViewPlan,
                onEditPlan = onEditPlan,
                onDelete = onDelete,
            )
        }
    }
}

@Composable
private fun PlanSectionHeader(title: String, subtitle: String, emoji: String, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(color.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center,
        ) { Text(emoji, fontSize = 20.sp) }
        Column {
            Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onBackground)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun PresetPlanRow(
    plans: List<WorkoutPlanEntity>,
    activePlanId: Long?,
    onViewPlan: (Long) -> Unit,
    gradientPool: List<List<Color>>,
    emojiPool: List<String>,
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        itemsIndexed(plans, key = { _, p -> p.id }) { index, plan ->
            PresetPlanCard(
                plan = plan,
                isActive = plan.id == activePlanId,
                colors = gradientPool.getOrElse(index) { gradientPool.first() },
                emoji = emojiPool.getOrElse(index) { "🏋️" },
                onClick = { onViewPlan(plan.id) },
            )
        }
    }
}

@Composable
private fun PresetPlanCard(
    plan: WorkoutPlanEntity,
    isActive: Boolean,
    colors: List<Color>,
    emoji: String,
    onClick: () -> Unit,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "glow_${plan.id}")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 0.14f,
        animationSpec = infiniteRepeatable(tween(2400, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "ga_${plan.id}",
    )

    Box(
        modifier = Modifier
            .width(172.dp)
            .height(240.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.linearGradient(
                    colors = colors,
                    start = Offset(0f, 0f),
                    end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
                )
            )
            .drawBehind {
                drawCircle(
                    color = Color.White.copy(alpha = 0.08f + glowAlpha),
                    radius = size.width * 0.55f,
                    center = Offset(size.width * 0.85f, size.height * 0.15f),
                )
            }
            .border(
                width = if (isActive) 2.dp else 1.dp,
                color = if (isActive) Color.White.copy(alpha = 0.9f) else Color.White.copy(alpha = 0.18f),
                shape = RoundedCornerShape(24.dp),
            )
            .clickable { onClick() },
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top,
                ) {
                    Text(emoji, fontSize = 32.sp)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isActive) Color.White.copy(alpha = 0.3f)
                                else Color.Black.copy(alpha = 0.25f)
                            )
                            .padding(horizontal = 7.dp, vertical = 3.dp),
                    ) {
                        Text(
                            text = if (isActive) "✓ АКТИВНЫЙ" else "ГОТОВЫЙ",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            fontSize = 9.sp,
                            letterSpacing = 0.3.sp,
                        )
                    }
                }
                Text(
                    plan.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 20.sp,
                )
                plan.description?.let {
                    Text(
                        it,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.65f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        fontSize = 11.sp,
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    StatPill("📅", "${plan.daysPerWeek}д/нед")
                    StatPill("⭐", "ур.${plan.minLevel}+")
                }
                Text(
                    "Нажмите для просмотра →",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.45f),
                    fontSize = 9.sp,
                )
            }
        }
    }
}

@Composable
private fun StatPill(icon: String, value: String) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color.Black.copy(alpha = 0.2f))
            .padding(horizontal = 7.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        Text(icon, fontSize = 10.sp)
        Text(value, style = MaterialTheme.typography.labelSmall, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 10.sp)
    }
}

@Composable
private fun UserPlansRow(
    plans: List<WorkoutPlanEntity>,
    activePlanId: Long?,
    onCreatePlan: () -> Unit,
    onViewPlan: (Long) -> Unit,
    onEditPlan: (Long) -> Unit,
    onDelete: (Long) -> Unit,
) {
    if (plans.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clickable { onCreatePlan() }
                .padding(24.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Default.Add, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                }
                Text("Создать свой план", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Text(
                    "Настройте упражнения, дни и подходы под себя",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        }
    } else {
        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item { CreatePlanCard(onClick = onCreatePlan) }
            items(plans, key = { it.id }) { plan ->
                UserPlanCard(
                    plan = plan,
                    isActive = plan.id == activePlanId,
                    onClick = { onViewPlan(plan.id) },
                    onEdit = { onEditPlan(plan.id) },
                    onDelete = { onDelete(plan.id) },
                )
            }
        }
    }
}

@Composable
private fun CreatePlanCard(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .width(160.dp)
            .height(220.dp)
            .clip(RoundedCornerShape(24.dp))
            .border(
                2.dp,
                Brush.linearGradient(
                    listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f),
                    )
                ),
                RoundedCornerShape(24.dp),
            )
            .clickable { onClick() }
            .padding(16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Default.Add, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(26.dp))
            }
            Text(
                "Новый план",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
            )
            Text(
                "Создайте свою программу",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                fontSize = 10.sp,
            )
        }
    }
}

@Composable
private fun UserPlanCard(
    plan: WorkoutPlanEntity,
    isActive: Boolean,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    val accentColor = Color(0xFF7C4DFF)

    Box(
        modifier = Modifier
            .width(172.dp)
            .height(220.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(
                if (isActive)
                    Brush.linearGradient(listOf(accentColor.copy(alpha = 0.8f), Color(0xFF4A148C).copy(alpha = 0.9f)))
                else
                    Brush.linearGradient(
                        listOf(
                            MaterialTheme.colorScheme.surfaceVariant,
                            MaterialTheme.colorScheme.surfaceVariant,
                        )
                    )
            )
            .border(
                if (isActive) 2.dp else 1.dp,
                if (isActive) accentColor else MaterialTheme.colorScheme.outlineVariant.copy(0.5f),
                RoundedCornerShape(24.dp),
            )
            .clickable { onClick() },
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("✏️", fontSize = 24.sp)
                    if (isActive) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White.copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 2.dp),
                        ) {
                            Text("АКТИВНЫЙ", fontSize = 8.sp, color = Color.White, fontWeight = FontWeight.ExtraBold, letterSpacing = 0.5.sp)
                        }
                    }
                }
                Text(
                    plan.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isActive) Color.White else MaterialTheme.colorScheme.onSurface,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 18.sp,
                )
                plan.description?.let {
                    Text(
                        it,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isActive) Color.White.copy(0.65f) else MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        fontSize = 10.sp,
                    )
                }
            }

            Text(
                "Нажмите для просмотра →",
                style = MaterialTheme.typography.labelSmall,
                color = if (isActive) Color.White.copy(0.5f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(0.6f),
                fontSize = 9.sp,
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                IconButton(onClick = onEdit, modifier = Modifier.size(34.dp)) {
                    Icon(
                        Icons.Default.Edit,
                        null,
                        tint = if (isActive) Color.White.copy(0.8f) else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp),
                    )
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(34.dp)) {
                    Icon(
                        Icons.Default.Delete,
                        null,
                        tint = if (isActive) Color.White.copy(0.6f) else MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
        }
    }
}