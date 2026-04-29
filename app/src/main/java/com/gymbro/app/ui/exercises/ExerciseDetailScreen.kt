package com.gymbro.app.ui.exercises

import androidx.compose.animation.animateContentSize
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gymbro.app.data.local.entity.ExerciseCategory
import com.gymbro.app.data.local.entity.ExerciseEquipment
import com.gymbro.app.domain.model.ExerciseDetail
import com.gymbro.app.domain.model.Mistake
import com.gymbro.app.domain.model.RepRecommendation
import com.gymbro.app.domain.model.TechniqueStep

// ════════════════════════════════════════════════════════════════
//  Root screen
// ════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseDetailScreen(
    onBack: () -> Unit,
    viewModel: ExerciseDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Snackbar при нажатии «Добавить в тренировку»
    LaunchedEffect(state.addedToWorkout) {
        if (state.addedToWorkout) {
            snackbarHostState.showSnackbar("Упражнение добавлено в тренировку ✓")
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        state.detail?.name ?: "",
                        maxLines = 1,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
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
                Box(
                    modifier = Modifier.fillMaxSize().padding(inner),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }

            state.detail == null -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(inner),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        "Упражнение не найдено",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            else -> {
                ExerciseDetailContent(
                    detail = state.detail!!,
                    contentPadding = inner,
                    onAddToWorkout = viewModel::onAddToWorkout,
                )
            }
        }
    }
}

// ════════════════════════════════════════════════════════════════
//  Main content
// ════════════════════════════════════════════════════════════════

@Composable
private fun ExerciseDetailContent(
    detail: ExerciseDetail,
    contentPadding: PaddingValues,
    onAddToWorkout: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding),
        contentPadding = PaddingValues(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        // ── Hero banner ──
        item { HeroBanner(detail = detail, onAddToWorkout = onAddToWorkout) }

        // ── Overview ──
        if (detail.overview.isNotBlank()) {
            item {
                SectionCard(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Text(
                        detail.overview,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 24.sp,
                    )
                }
            }
        }

        // ── Video placeholder ──
        item {
            VideoPlaceholder(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
        }

        // ── Technique steps ──
        if (detail.techniqueSteps.isNotEmpty()) {
            item {
                SectionHeader(
                    title = "Техника выполнения",
                    icon = Icons.Default.FitnessCenter,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }
            itemsIndexed(detail.techniqueSteps) { index, step ->
                TechniqueStepCard(
                    step = step,
                    stepNumber = index + 1,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                )
            }
        }

        // ── Rep recommendations ──
        if (detail.repRecommendations.isNotEmpty()) {
            item {
                SectionHeader(
                    title = "Рекомендации по повторениям",
                    icon = Icons.Default.Repeat,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }
            itemsIndexed(detail.repRecommendations) { index, rec ->
                RepRecommendationCard(
                    rec = rec,
                    index = index,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                )
            }
        }

        // ── Common mistakes ──
        if (detail.commonMistakes.isNotEmpty()) {
            item {
                SectionHeader(
                    title = "Распространённые ошибки",
                    icon = Icons.Default.ErrorOutline,
                    accentColor = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }
            itemsIndexed(detail.commonMistakes) { _, mistake ->
                MistakeCard(
                    mistake = mistake,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                )
            }
        }

        // ── Tips ──
        if (detail.tips.isNotEmpty()) {
            item {
                SectionHeader(
                    title = "Советы и дыхание",
                    icon = Icons.Default.Lightbulb,
                    accentColor = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }
            itemsIndexed(detail.tips) { _, tip ->
                TipCard(
                    tip = tip,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                )
            }
        }

        // ── Variations ──
        if (detail.variations.isNotEmpty()) {
            item {
                SectionHeader(
                    title = "Варианты и прогрессии",
                    icon = Icons.Default.Shuffle,
                    accentColor = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
                VariationsCard(
                    variations = detail.variations,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                )
            }
        }

        // ── Bottom CTA ──
        item {
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = onAddToWorkout,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
                shape = RoundedCornerShape(16.dp),
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    "Добавить в тренировку",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

// ════════════════════════════════════════════════════════════════
//  Hero banner
// ════════════════════════════════════════════════════════════════

@Composable
private fun HeroBanner(
    detail: ExerciseDetail,
    onAddToWorkout: () -> Unit,
) {
    val gradient = Brush.verticalGradient(
        colors = listOf(
            categoryColor(detail.category).copy(alpha = 0.85f),
            categoryColor(detail.category).copy(alpha = 0.45f),
            MaterialTheme.colorScheme.background,
        )
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp),
    ) {
        // Background gradient
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .background(gradient),
        )

        // Large exercise icon
        Box(
            modifier = Modifier
                .size(120.dp)
                .align(Alignment.TopCenter)
                .padding(top = 24.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.15f))
                .border(2.dp, Color.White.copy(alpha = 0.3f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = categoryIcon(detail.category),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(56.dp),
            )
        }

        // Info chips — bottom of banner
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                InfoChip(
                    label = equipmentLabel(detail.equipment),
                    color = MaterialTheme.colorScheme.primary,
                )
                InfoChip(
                    label = categoryLabel(detail.category),
                    color = categoryColor(detail.category),
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                InfoChip(
                    label = "▶ ${detail.primaryMuscle}",
                    color = MaterialTheme.colorScheme.secondary,
                )
                if (detail.secondaryMuscles.isNotEmpty()) {
                    InfoChip(
                        label = "+ ${detail.secondaryMuscles.take(2).joinToString(", ")}",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoChip(label: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
            .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = color,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

// ════════════════════════════════════════════════════════════════
//  Video placeholder
// ════════════════════════════════════════════════════════════════

@Composable
private fun VideoPlaceholder(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Default.PlayCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp),
                )
            }
            Column {
                Text(
                    "Видео техники",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    "Видео будет добавлено в следующем обновлении",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

// ════════════════════════════════════════════════════════════════
//  Section header
// ════════════════════════════════════════════════════════════════

@Composable
private fun SectionHeader(
    title: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    accentColor: Color = MaterialTheme.colorScheme.primary,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(accentColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(20.dp))
        }
        Text(
            title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
}

// ════════════════════════════════════════════════════════════════
//  Technique step card
// ════════════════════════════════════════════════════════════════

@Composable
private fun TechniqueStepCard(
    step: TechniqueStep,
    stepNumber: Int,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(true) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize()
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                // Step number badge
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        "$stepNumber",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                }
                Text(
                    step.phase,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                )
                Icon(
                    if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp),
                )
            }
            if (expanded) {
                Spacer(Modifier.height(10.dp))
                Text(
                    step.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 22.sp,
                )
            }
        }
    }
}

// ════════════════════════════════════════════════════════════════
//  Rep recommendation card
// ════════════════════════════════════════════════════════════════

private val goalColors = listOf(
    Color(0xFF7C4DFF), // Сила — фиолетовый
    Color(0xFFFF6B35), // Гипертрофия — оранжевый
    Color(0xFF4CAF50), // Выносливость — зелёный
)

@Composable
private fun RepRecommendationCard(
    rec: RepRecommendation,
    index: Int,
    modifier: Modifier = Modifier,
) {
    val accentColor = goalColors.getOrElse(index) { Color(0xFF9E9E9E) }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Goal label
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(accentColor),
                )
                Text(
                    rec.goal,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = accentColor,
                )
            }

            // Metrics grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                MetricBox(
                    label = "Повторения",
                    value = rec.repsRange,
                    icon = Icons.Default.Repeat,
                    color = accentColor,
                    modifier = Modifier.weight(1f),
                )
                MetricBox(
                    label = "Подходы",
                    value = rec.setsRange,
                    icon = Icons.Default.EmojiEvents,
                    color = accentColor,
                    modifier = Modifier.weight(1f),
                )
                MetricBox(
                    label = "Отдых",
                    value = rec.restSeconds,
                    icon = Icons.Default.Timer,
                    color = accentColor,
                    modifier = Modifier.weight(1f),
                )
            }

            // Intensity
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "Интенсивность:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(accentColor.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 2.dp),
                ) {
                    Text(
                        rec.intensity,
                        style = MaterialTheme.typography.labelMedium,
                        color = accentColor,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }

            // Notes
            if (rec.notes.isNotBlank()) {
                Text(
                    rec.notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp,
                )
            }
        }
    }
}

@Composable
private fun MetricBox(
    label: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.10f))
            .padding(horizontal = 8.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
        Text(
            value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            fontSize = 10.sp,
        )
    }
}

// ════════════════════════════════════════════════════════════════
//  Mistake card
// ════════════════════════════════════════════════════════════════

@Composable
private fun MistakeCard(mistake: Mistake, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f),
        ),
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.error.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Default.ErrorOutline,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(20.dp),
                )
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    mistake.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error,
                )
                Text(
                    mistake.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 18.sp,
                )
            }
        }
    }
}

// ════════════════════════════════════════════════════════════════
//  Tip card
// ════════════════════════════════════════════════════════════════

@Composable
private fun TipCard(tip: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f),
        ),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Icon(
                Icons.Default.Lightbulb,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.size(18.dp).padding(top = 2.dp),
            )
            Text(
                tip,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 18.sp,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

// ════════════════════════════════════════════════════════════════
//  Variations card
// ════════════════════════════════════════════════════════════════

@Composable
private fun VariationsCard(variations: List<String>, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f),
        ),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            variations.forEach { variation ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(16.dp),
                    )
                    Text(
                        variation,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
    }
}

// ════════════════════════════════════════════════════════════════
//  Section card wrapper
// ════════════════════════════════════════════════════════════════

@Composable
private fun SectionCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Box(modifier = Modifier.padding(16.dp)) { content() }
    }
}

// ════════════════════════════════════════════════════════════════
//  Helpers: category colors, icons, labels
// ════════════════════════════════════════════════════════════════

@Composable
private fun categoryColor(category: ExerciseCategory): Color = when (category) {
    ExerciseCategory.CHEST      -> Color(0xFFFF6B35)
    ExerciseCategory.BACK       -> Color(0xFF2196F3)
    ExerciseCategory.LEGS       -> Color(0xFF4CAF50)
    ExerciseCategory.SHOULDERS  -> Color(0xFFFFC107)
    ExerciseCategory.ARMS       -> Color(0xFF7C4DFF)
    ExerciseCategory.CORE       -> Color(0xFF00BCD4)
    ExerciseCategory.FULL_BODY  -> Color(0xFFE91E63)
    ExerciseCategory.OTHER      -> MaterialTheme.colorScheme.primary
}

private fun categoryIcon(category: ExerciseCategory): ImageVector = when (category) {
    ExerciseCategory.CHEST,
    ExerciseCategory.BACK,
    ExerciseCategory.SHOULDERS,
    ExerciseCategory.ARMS,
    ExerciseCategory.LEGS,
    ExerciseCategory.CORE,
    ExerciseCategory.FULL_BODY,
    ExerciseCategory.OTHER     -> Icons.Default.FitnessCenter
}

private fun categoryLabel(category: ExerciseCategory): String = when (category) {
    ExerciseCategory.CHEST     -> "Грудь"
    ExerciseCategory.BACK      -> "Спина"
    ExerciseCategory.LEGS      -> "Ноги"
    ExerciseCategory.SHOULDERS -> "Плечи"
    ExerciseCategory.ARMS      -> "Руки"
    ExerciseCategory.CORE      -> "Кор"
    ExerciseCategory.FULL_BODY -> "Всё тело"
    ExerciseCategory.OTHER     -> "Другое"
}

private fun equipmentLabel(equipment: ExerciseEquipment): String = when (equipment) {
    ExerciseEquipment.BARBELL    -> "Штанга"
    ExerciseEquipment.DUMBBELL   -> "Гантели"
    ExerciseEquipment.MACHINE    -> "Тренажёр"
    ExerciseEquipment.CABLE      -> "Блок"
    ExerciseEquipment.BODYWEIGHT -> "Без инвентаря"
    ExerciseEquipment.KETTLEBELL -> "Гиря"
    ExerciseEquipment.OTHER      -> "Другое"
}