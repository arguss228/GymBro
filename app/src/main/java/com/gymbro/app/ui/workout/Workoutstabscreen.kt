package com.gymbro.app.ui.workouts

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
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gymbro.app.data.local.entity.WorkoutPlanEntity
import com.gymbro.app.ui.exercises.ExercisesScreen
import com.gymbro.app.ui.plans.PlansScreen

// ════════════════════════════════════════════════════════════════
//  Entry point
// ════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutsTabScreen(
    onCreatePlan: () -> Unit,
    onEditPlan: (Long) -> Unit,
    onExerciseClick: (Long) -> Unit,
    viewModel: WorkoutsTabViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

    Column(modifier = Modifier.fillMaxSize()) {

        // ── Top header ─────────────────────────────────────────────
        WorkoutsHeader(selectedTab = selectedTab, onTabSelect = { selectedTab = it })

        // ── Content ────────────────────────────────────────────────
        when (selectedTab) {
            0 -> PlansTab(
                state        = state,
                onCreatePlan = onCreatePlan,
                onEditPlan   = onEditPlan,
                onSetActive  = viewModel::setActive,
                onDelete     = viewModel::deletePlan,
            )
            1 -> ExercisesScreen(
                onBack          = { /* embedded */ },
                onExerciseClick = onExerciseClick,
                isEmbedded      = true,
            )
        }
    }
}

// ════════════════════════════════════════════════════════════════
//  Header with segmented tabs
// ════════════════════════════════════════════════════════════════

@Composable
private fun WorkoutsHeader(
    selectedTab: Int,
    onTabSelect: (Int) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(top = 16.dp, start = 20.dp, end = 20.dp, bottom = 0.dp),
    ) {
        Text(
            "Тренировки",
            style      = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            color      = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(14.dp))

        // Segmented control
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
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primary
                            else Color.Transparent
                        )
                        .clickable { onTabSelect(index) }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        title,
                        style      = MaterialTheme.typography.titleSmall,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color      = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                     else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        Spacer(Modifier.height(4.dp))
    }
}

// ════════════════════════════════════════════════════════════════
//  Plans tab — vertical scroll, horizontal rows per level
// ════════════════════════════════════════════════════════════════

@Composable
private fun PlansTab(
    state: WorkoutsTabUiState,
    onCreatePlan: () -> Unit,
    onEditPlan: (Long) -> Unit,
    onSetActive: (Long) -> Unit,
    onDelete: (Long) -> Unit,
) {
    LazyColumn(
        modifier        = Modifier.fillMaxSize(),
        contentPadding  = PaddingValues(bottom = 120.dp, top = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {

        // ── Новичок ──────────────────────────────────────────────
        item {
            PlanSectionHeader(
                title    = "Новичок",
                subtitle = "Базовые программы · 3–4 дня/нед",
                emoji    = "🌱",
                color    = Color(0xFF4CAF50),
            )
        }
        item {
            PresetPlanRow(plans = WorkoutPresets.beginnerPlans)
        }

        item { Spacer(Modifier.height(8.dp)) }

        // ── Продвинутый ──────────────────────────────────────────
        item {
            PlanSectionHeader(
                title    = "Продвинутый",
                subtitle = "Сплит-программы · 4–5 дней/нед",
                emoji    = "💎",
                color    = Color(0xFF2196F3),
            )
        }
        item {
            PresetPlanRow(plans = WorkoutPresets.intermediatePlans)
        }

        item { Spacer(Modifier.height(8.dp)) }

        // ── Опытный ──────────────────────────────────────────────
        item {
            PlanSectionHeader(
                title    = "Опытный",
                subtitle = "PPL и силовые · 5–6 дней/нед",
                emoji    = "⚡",
                color    = Color(0xFFFF9800),
            )
        }
        item {
            PresetPlanRow(plans = WorkoutPresets.advancedPlans)
        }

        item { Spacer(Modifier.height(8.dp)) }

        // ── Мои планы ─────────────────────────────────────────────
        item {
            PlanSectionHeader(
                title    = "Мои планы",
                subtitle = "Созданные вами",
                emoji    = "✏️",
                color    = Color(0xFF7C4DFF),
            )
        }
        item {
            UserPlansRow(
                plans        = state.userPlans,
                activePlanId = state.activePlanId,
                onCreatePlan = onCreatePlan,
                onEditPlan   = onEditPlan,
                onSetActive  = onSetActive,
                onDelete     = onDelete,
            )
        }
    }
}

// ════════════════════════════════════════════════════════════════
//  Section header
// ════════════════════════════════════════════════════════════════

@Composable
private fun PlanSectionHeader(
    title: String,
    subtitle: String,
    emoji: String,
    color: Color,
) {
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
        ) {
            Text(emoji, fontSize = 20.sp)
        }
        Column {
            Text(
                title,
                style      = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color      = MaterialTheme.colorScheme.onBackground,
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
//  Horizontal preset plan row
// ════════════════════════════════════════════════════════════════

@Composable
private fun PresetPlanRow(plans: List<PresetPlan>) {
    LazyRow(
        contentPadding       = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(plans, key = { it.id }) { plan ->
            PresetPlanCard(plan = plan)
        }
    }
}

// ════════════════════════════════════════════════════════════════
//  Preset plan card — tall, gradient, visually rich
// ════════════════════════════════════════════════════════════════

@Composable
private fun PresetPlanCard(plan: PresetPlan) {
    // Screen width ÷ 2 minus padding → 2 cards per row
    val cardWidth  = 172.dp
    val cardHeight = 240.dp

    // Pulsing glow animation for accent
    val infiniteTransition = rememberInfiniteTransition(label = "card_glow_${plan.id}")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue  = 0.0f,
        targetValue   = 0.12f,
        animationSpec = infiniteRepeatable(tween(2400, easing = EaseInOutSine), RepeatMode.Reverse),
        label         = "glow_${plan.id}",
    )

    Box(
        modifier = Modifier
            .width(cardWidth)
            .height(cardHeight)
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.linearGradient(
                    colors = plan.gradientColors,
                    start  = Offset(0f, 0f),
                    end    = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
                )
            )
            .drawBehind {
                // Decorative circle glow top-right
                drawCircle(
                    color  = Color.White.copy(alpha = 0.08f + glowAlpha),
                    radius = size.width * 0.55f,
                    center = Offset(size.width * 0.85f, size.height * 0.15f),
                )
                // Bottom accent bar
                drawRoundRect(
                    color        = Color.White.copy(alpha = 0.07f),
                    topLeft      = Offset(16.dp.toPx(), size.height - 60.dp.toPx()),
                    size         = Size(size.width - 32.dp.toPx(), 44.dp.toPx()),
                    cornerRadius = CornerRadius(12.dp.toPx()),
                )
            }
            .border(
                1.dp,
                Color.White.copy(alpha = 0.18f),
                RoundedCornerShape(24.dp),
            ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            // Top section: emoji + difficulty badge
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.Top,
                ) {
                    Text(plan.emoji, fontSize = 32.sp)
                    // Difficulty chip
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Black.copy(alpha = 0.25f))
                            .padding(horizontal = 7.dp, vertical = 3.dp),
                    ) {
                        Text(
                            plan.difficulty.label,
                            style         = MaterialTheme.typography.labelSmall,
                            fontWeight    = FontWeight.Bold,
                            color         = Color.White,
                            fontSize      = 9.sp,
                            letterSpacing = 0.3.sp,
                        )
                    }
                }

                // Plan name
                Text(
                    plan.name,
                    style      = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color      = Color.White,
                    maxLines   = 2,
                    overflow   = TextOverflow.Ellipsis,
                    lineHeight = 20.sp,
                )
                Text(
                    plan.subtitle,
                    style  = MaterialTheme.typography.bodySmall,
                    color  = Color.White.copy(alpha = 0.65f),
                    fontSize = 11.sp,
                )
            }

            // Muscle tags
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                MuscleTagsRow(muscles = plan.muscles.take(3))
            }

            // Bottom stats
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically,
            ) {
                StatPill(icon = "📅", value = "${plan.daysPerWeek}д/нед")
                StatPill(icon = "⏱", value = plan.duration.substringBefore("–") + "+")
            }
        }
    }
}

@Composable
private fun MuscleTagsRow(muscles: List<MuscleTarget>) {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        muscles.forEach { muscle ->
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.White.copy(alpha = 0.18f))
                    .padding(horizontal = 6.dp, vertical = 2.dp),
            ) {
                Text(
                    muscle.displayName,
                    style    = MaterialTheme.typography.labelSmall,
                    color    = Color.White,
                    fontSize = 9.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
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
        Text(
            value,
            style      = MaterialTheme.typography.labelSmall,
            color      = Color.White,
            fontWeight = FontWeight.SemiBold,
            fontSize   = 10.sp,
        )
    }
}

// ════════════════════════════════════════════════════════════════
//  User plans horizontal row + create button
// ════════════════════════════════════════════════════════════════

@Composable
private fun UserPlansRow(
    plans: List<WorkoutPlanEntity>,
    activePlanId: Long?,
    onCreatePlan: () -> Unit,
    onEditPlan: (Long) -> Unit,
    onSetActive: (Long) -> Unit,
    onDelete: (Long) -> Unit,
) {
    if (plans.isEmpty()) {
        // Empty state with create button
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
                    Icon(
                        Icons.Default.Add,
                        contentDescription = null,
                        tint     = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp),
                    )
                }
                Text(
                    "Создать свой план",
                    style      = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color      = MaterialTheme.colorScheme.primary,
                )
                Text(
                    "Настройте упражнения, дни и подходы под себя",
                    style    = MaterialTheme.typography.bodySmall,
                    color    = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        }
    } else {
        LazyRow(
            contentPadding       = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Create new button as first card
            item {
                CreatePlanCard(onClick = onCreatePlan)
            }
            items(plans, key = { it.id }) { plan ->
                UserPlanCard(
                    plan         = plan,
                    isActive     = plan.id == activePlanId,
                    onSetActive  = { onSetActive(plan.id) },
                    onEdit       = { onEditPlan(plan.id) },
                    onDelete     = { onDelete(plan.id) },
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
                Icon(
                    Icons.Default.Add,
                    contentDescription = null,
                    tint     = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(26.dp),
                )
            }
            Text(
                "Новый план",
                style      = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color      = MaterialTheme.colorScheme.primary,
                textAlign  = TextAlign.Center,
            )
            Text(
                "Создайте свою программу",
                style    = MaterialTheme.typography.bodySmall,
                color    = MaterialTheme.colorScheme.onSurfaceVariant,
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
    onSetActive: () -> Unit,
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
                    Brush.linearGradient(
                        listOf(
                            accentColor.copy(alpha = 0.8f),
                            Color(0xFF4A148C).copy(alpha = 0.9f),
                        )
                    )
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
            ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            // Top
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
                    style      = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color      = if (isActive) Color.White else MaterialTheme.colorScheme.onSurface,
                    maxLines   = 3,
                    overflow   = TextOverflow.Ellipsis,
                    lineHeight = 18.sp,
                )
                plan.description?.let {
                    Text(
                        it,
                        style    = MaterialTheme.typography.bodySmall,
                        color    = if (isActive) Color.White.copy(0.65f) else MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        fontSize = 10.sp,
                    )
                }
            }

            // Actions
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                if (!isActive) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(accentColor.copy(alpha = 0.15f))
                            .clickable { onSetActive() }
                            .padding(vertical = 7.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            "Выбрать",
                            style      = MaterialTheme.typography.labelSmall,
                            color      = accentColor,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
                IconButton(
                    onClick  = onEdit,
                    modifier = Modifier.size(34.dp),
                ) {
                    Icon(
                        Icons.Default.Edit,
                        null,
                        tint     = if (isActive) Color.White.copy(0.8f) else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp),
                    )
                }
                IconButton(
                    onClick  = onDelete,
                    modifier = Modifier.size(34.dp),
                ) {
                    Icon(
                        Icons.Default.Delete,
                        null,
                        tint     = if (isActive) Color.White.copy(0.6f) else MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
        }
    }
}