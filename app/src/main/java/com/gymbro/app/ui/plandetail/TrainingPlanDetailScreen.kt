package com.obsession.app.ui.plandetail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gymbro.app.domain.model.LevelTier

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrainingPlanDetailScreen(
    onBack: () -> Unit,
    onEdit: (Long) -> Unit,
    viewModel: TrainingPlanDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.activatedEvent) {
        if (state.activatedEvent) {
            snackbarHostState.showSnackbar("✅ План успешно выбран как активный")
            viewModel.consumeActivatedEvent()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        state.plan?.name ?: "",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    state.plan?.let { plan ->
                        if (!plan.isPreset) {
                            IconButton(onClick = { onEdit(plan.id) }) {
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = "Редактировать",
                                    tint = MaterialTheme.colorScheme.primary,
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
    ) { inner ->
        when {
            state.isLoading -> {
                Box(Modifier.fillMaxSize().padding(inner), Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }

            state.plan == null -> {
                Box(Modifier.fillMaxSize().padding(inner), Alignment.Center) {
                    Text(
                        "План не найден",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            else -> {
                val plan = state.plan!!
                val tier = LevelTier.of(plan.minLevel)
                val isActive = state.isActive

                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(inner),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    // ── Hero card ─────────────────────────────────
                    item {
                        PlanHeroCard(
                            name = plan.name,
                            description = plan.description,
                            daysPerWeek = plan.daysPerWeek,
                            minLevel = plan.minLevel,
                            tierColor = tier.primary,
                            tierTitle = tier.title,
                            isActive = isActive,
                            isPreset = plan.isPreset,
                        )
                    }

                    // ── Select button ──────────────────────────────
                    item {
                        if (isActive) {
                            // Already active badge
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(
                                                MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.10f),
                                            )
                                        )
                                    )
                                    .border(
                                        1.dp,
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                                        RoundedCornerShape(16.dp),
                                    )
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                            ) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp),
                                )
                                Spacer(Modifier.width(10.dp))
                                Text(
                                    "Это ваш активный план",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                            }
                        } else {
                            Button(
                                onClick = { viewModel.setAsActive(); onBack() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                ),
                            ) {
                                Icon(
                                    Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    modifier = Modifier.size(22.dp),
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "Выбрать этот план",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                    }

                    // ── Days header ────────────────────────────────
                    item {
                        Text(
                            "Программа тренировок",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }

                    // ── Day cards ──────────────────────────────────
                    if (state.days.isEmpty()) {
                        item {
                            Box(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                Alignment.Center,
                            ) {
                                Text(
                                    "Дни тренировок не добавлены",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    } else {
                        itemsIndexed(state.days, key = { _, d -> d.day.id }) { idx, dayData ->
                            DayDetailCard(
                                dayName = dayData.day.name,
                                dayIndex = idx,
                                exercises = dayData.exercises,
                                exerciseNames = state.exerciseNames,
                            )
                        }
                    }

                    item { Spacer(Modifier.height(24.dp)) }
                }
            }
        }
    }
}

// ── Hero card ─────────────────────────────────────────────────────

@Composable
private fun PlanHeroCard(
    name: String,
    description: String?,
    daysPerWeek: Int,
    minLevel: Int,
    tierColor: Color,
    tierTitle: String,
    isActive: Boolean,
    isPreset: Boolean,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        tierColor.copy(alpha = 0.18f),
                        MaterialTheme.colorScheme.surfaceVariant,
                    )
                )
            )
            .then(
                if (isActive) Modifier.border(
                    2.dp,
                    Brush.horizontalGradient(
                        listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.secondary,
                        )
                    ),
                    RoundedCornerShape(24.dp),
                ) else Modifier.border(
                    1.dp,
                    tierColor.copy(alpha = 0.3f),
                    RoundedCornerShape(24.dp),
                )
            )
            .padding(20.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Active badge
            if (isActive) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primary)
                            .padding(horizontal = 10.dp, vertical = 3.dp),
                    ) {
                        Text(
                            "АКТИВНЫЙ",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onPrimary,
                            letterSpacing = 1.sp,
                        )
                    }
                }
            }

            // Icon + meta
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(tierColor.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Default.FitnessCenter,
                        contentDescription = null,
                        tint = tierColor,
                        modifier = Modifier.size(28.dp),
                    )
                }
                Column(Modifier.weight(1f)) {
                    Text(
                        name,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        "$tierTitle · ${daysPerWeek} дн/нед · от $minLevel уровня",
                        style = MaterialTheme.typography.bodyMedium,
                        color = tierColor,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }

            // Description
            description?.let {
                Text(
                    it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 20.sp,
                )
            }
        }
    }
}

// ── Day card ──────────────────────────────────────────────────────

@Composable
private fun DayDetailCard(
    dayName: String,
    dayIndex: Int,
    exercises: List<com.gymbro.app.data.local.entity.TrainingDayExerciseEntity>,
    exerciseNames: Map<Long, String>,
) {
    var expanded by rememberSaveable(dayIndex) { mutableStateOf(true) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(animationSpec = tween(250)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Day header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { expanded = !expanded }
                    .padding(4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // Day number badge
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        "${dayIndex + 1}",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }

                Column(Modifier.weight(1f)) {
                    Text(
                        dayName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        "${exercises.size} упражнений",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                Icon(
                    if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(22.dp),
                )
            }

            // Exercise list
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(tween(250)) + fadeIn(tween(200)),
                exit = shrinkVertically(tween(200)) + fadeOut(tween(150)),
            ) {
                Column(
                    modifier = Modifier.padding(top = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(0.dp),
                ) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                        modifier = Modifier.padding(bottom = 8.dp),
                    )
                    exercises.forEachIndexed { idx, ex ->
                        ExerciseDetailRow(
                            index = idx,
                            name = exerciseNames[ex.exerciseId] ?: "Упражнение",
                            sets = ex.targetSets,
                            reps = ex.targetReps,
                            restSeconds = ex.restSeconds,
                            notes = ex.notes,
                        )
                        if (idx < exercises.lastIndex) {
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f),
                                modifier = Modifier.padding(horizontal = 4.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── Exercise row inside day ────────────────────────────────────────

@Composable
private fun ExerciseDetailRow(
    index: Int,
    name: String,
    sets: Int,
    reps: Int,
    restSeconds: Int,
    notes: String?,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        // Index
        Text(
            "${index + 1}",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(18.dp),
            fontWeight = FontWeight.Bold,
        )

        // Icon
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Default.FitnessCenter,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp),
            )
        }

        Column(Modifier.weight(1f)) {
            Text(
                name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "$sets × $reps",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    "отдых ${restSeconds}с",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            notes?.let {
                Text(
                    it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Normal,
                )
            }
        }
    }
}