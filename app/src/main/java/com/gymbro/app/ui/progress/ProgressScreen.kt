package com.gymbro.app.ui.progress

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gymbro.app.data.local.entity.ExerciseEntity
import com.gymbro.app.data.local.entity.PrType
import com.gymbro.app.ui.components.StrengthRanksSheet
import com.gymbro.app.ui.rank.RankDashboardCard
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressScreen(
    onBack: () -> Unit,
    isEmbedded: Boolean = false,
    viewModel: ProgressViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var searchFocused by remember { mutableStateOf(false) }
    // Показывать ли шит со всеми рангами
    var showRanksSheet by remember { mutableStateOf(false) }

    if (showRanksSheet) {
        StrengthRanksSheet(
            currentRank = state.rankState.currentRank,
            onDismiss   = { showRanksSheet = false },
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            if (!isEmbedded) {
                TopAppBar(
                    title = { Text("Прогресс", fontWeight = FontWeight.ExtraBold) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                    ),
                )
            }
        },
    ) { inner ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                start = 16.dp, end = 16.dp, top = 12.dp, bottom = 32.dp
            ),
        ) {

            // ── 1. Карточка ранга (кликабельная → список всех рангов) ────
            item {
                PowerliftingRankCard(
                    state   = state,
                    onClick = { showRanksSheet = true },
                )
            }

            // ── 2. Статистика: Тренировки + Серия ───────────────────────
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    StatCard(
                        title  = "Тренировок",
                        value  = state.totalSessions.toString(),
                        icon   = "🏋️",
                        modifier = Modifier.weight(1f),
                        accentColor = MaterialTheme.colorScheme.primary,
                    )
                    WinStreakCard(
                        streak   = state.winStreak,
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            // ── 3. Заголовок раздела графика ─────────────────────────────
            item {
                Text(
                    "График максимальных весов",
                    style      = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color      = MaterialTheme.colorScheme.onBackground,
                )
            }

            // ── 4. Поиск упражнения + автодополнение ─────────────────────
            item {
                Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                    OutlinedTextField(
                        value = state.searchQuery,
                        onValueChange = viewModel::setSearchQuery,
                        placeholder = {
                            Text(
                                if (state.searchQuery.isEmpty()) state.selectedExerciseName
                                else "",
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                maxLines = 1,
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        },
                        trailingIcon = {
                            if (state.searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Очистить",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { searchFocused = it.isFocused },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor    = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor  = MaterialTheme.colorScheme.outlineVariant,
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        ),
                    )

                    AnimatedVisibility(
                        visible = state.searchQuery.isNotEmpty(),
                        enter = expandVertically() + fadeIn(),
                        exit  = shrinkVertically() + fadeOut(),
                    ) {
                        Card(
                            modifier  = Modifier.fillMaxWidth().padding(top = 4.dp),
                            shape     = RoundedCornerShape(14.dp),
                            colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        ) {
                            Column {
                                val visible = state.filteredExercises.take(6)
                                visible.forEachIndexed { index, exercise ->
                                    ExerciseSearchItem(
                                        exercise   = exercise,
                                        isSelected = exercise.id == state.selectedExerciseId,
                                        onClick    = {
                                            viewModel.selectExercise(exercise.id)
                                            searchFocused = false
                                        },
                                    )
                                    if (index < visible.lastIndex) {
                                        HorizontalDivider(
                                            color    = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                                            modifier = Modifier.padding(horizontal = 14.dp),
                                        )
                                    }
                                }
                                if (state.filteredExercises.isEmpty()) {
                                    Text(
                                        "Упражнения не найдены",
                                        style    = MaterialTheme.typography.bodyMedium,
                                        color    = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(14.dp),
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ── 5. Фильтр периода ─────────────────────────────────────────
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(ProgressPeriod.values()) { period ->
                        FilterChip(
                            selected = state.selectedPeriod == period,
                            onClick  = { viewModel.setPeriod(period) },
                            label    = { Text(period.label) },
                            colors   = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor     = MaterialTheme.colorScheme.onPrimary,
                            ),
                        )
                    }
                }
            }

            // ── 6. График ─────────────────────────────────────────────────
            item {
                if (state.chartPoints.isEmpty()) {
                    EmptyChartPlaceholder()
                } else {
                    PrProgressChart(
                        points    = state.chartPoints,
                        yAxisMin  = state.yAxisMin,
                        yAxisMax  = state.yAxisMax,
                        yLabels   = state.yAxisLabels,
                        accentColor = MaterialTheme.colorScheme.primary,
                    )
                }
            }

            // ── 7. Личные рекорды выбранного упражнения ──────────────────
            if (state.personalRecords.isNotEmpty()) {
                item {
                    Text(
                        "Личные рекорды — ${state.selectedExerciseName}",
                        style      = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color      = MaterialTheme.colorScheme.onBackground,
                    )
                }
                items(state.personalRecords, key = { it.pr.id }) { item ->
                    PrRow(item)
                }
            } else if (!state.searchQuery.isEmpty() || state.selectedExerciseId != 0L) {
                item {
                    Text(
                        "Для «${state.selectedExerciseName}» рекордов пока нет.\nНачните тренироваться!",
                        style     = MaterialTheme.typography.bodyMedium,
                        color     = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier  = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    )
                }
            }
        }
    }
}

// ════════════════════════════════════════════════════════════════
//  Карточка ранга — «Ранг в Пауэрлифтинге»
// ════════════════════════════════════════════════════════════════

@Composable
private fun PowerliftingRankCard(
    state: ProgressUiState,
    onClick: () -> Unit,
) {
    val rank = state.rankState.currentRank

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        Color(0xFF0D1B2A),
                        rank.primaryColor.copy(alpha = 0.18f),
                        Color(0xFF070B14),
                    )
                )
            )
            .clickable { onClick() }
            .padding(20.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {

            // Заголовок раздела
            Text(
                "Ранг в Пауэрлифтинге",
                style      = MaterialTheme.typography.labelMedium,
                color      = Color.White.copy(alpha = 0.5f),
                letterSpacing = 1.sp,
            )

            // Символ + название
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(rank.symbol, fontSize = 52.sp)
                Column {
                    Text(
                        rank.name,
                        style      = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black,
                        color      = rank.primaryColor,
                    )
                    Text(
                        rank.description,
                        style  = MaterialTheme.typography.bodySmall,
                        color  = Color.White.copy(alpha = 0.5f),
                        maxLines = 2,
                    )
                }
            }

            // 1RM чипы
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                listOf(
                    "🏋️ Жим"   to state.rankState.bench,
                    "🦵 Присед" to state.rankState.squat,
                    "⬆️ Тяга"  to state.rankState.deadlift,
                ).forEach { (label, kg) ->
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(rank.primaryColor.copy(alpha = 0.1f))
                            .padding(horizontal = 6.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(label, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.45f), maxLines = 1)
                        Text(
                            "${kg.toInt()} кг",
                            style      = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Black,
                            color      = Color.White,
                        )
                    }
                }
            }

            // Прогресс бар
            state.rankState.nextRank?.let { next ->
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            "До ${next.symbol} ${next.name}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.45f),
                        )
                        Text(
                            "${(state.rankState.progress * 100).toInt()}%",
                            style      = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color      = rank.primaryColor,
                        )
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color.White.copy(alpha = 0.07f)),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(state.rankState.progress.coerceIn(0f, 1f))
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    Brush.horizontalGradient(listOf(rank.primaryColor, rank.secondaryColor))
                                ),
                        )
                    }
                }
            }

            // Подсказка
            Text(
                "Нажмите, чтобы увидеть все ранги →",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.22f),
            )
        }
    }
}

// ════════════════════════════════════════════════════════════════
//  Статистика — базовая карточка
// ════════════════════════════════════════════════════════════════

@Composable
private fun StatCard(
    title: String,
    value: String,
    icon: String,
    modifier: Modifier = Modifier,
    accentColor: Color = MaterialTheme.colorScheme.primary,
) {
    Card(
        modifier  = modifier,
        shape     = RoundedCornerShape(20.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(icon, fontSize = 26.sp)
            Text(
                value,
                style      = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Black,
                color      = accentColor,
                maxLines   = 1,
            )
            Text(
                title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

// ════════════════════════════════════════════════════════════════
//  Win Streak карточка
// ════════════════════════════════════════════════════════════════

@Composable
private fun WinStreakCard(streak: Int, modifier: Modifier = Modifier) {
    val streakColor = Color(0xFFFF6D00) // оранжевый — огонь

    Card(
        modifier  = modifier,
        shape     = RoundedCornerShape(20.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(streakColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Default.LocalFireDepartment,
                    contentDescription = null,
                    tint     = streakColor,
                    modifier = Modifier.size(20.dp),
                )
            }
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    "$streak",
                    style      = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Black,
                    color      = streakColor,
                )
                Text(
                    "дней",
                    style    = MaterialTheme.typography.bodyMedium,
                    color    = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 4.dp),
                )
            }
            Text(
                "Серия",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

// ════════════════════════════════════════════════════════════════
//  График PR — нарастающий максимум, Y-ось с шагом 5 кг
// ════════════════════════════════════════════════════════════════

@Composable
private fun PrProgressChart(
    points: List<ChartPoint>,
    yAxisMin: Float,
    yAxisMax: Float,
    yLabels: List<Float>,
    accentColor: Color,
) {
    val onSurface       = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val gridColor       = MaterialTheme.colorScheme.outlineVariant
    val dateFormat      = SimpleDateFormat("d MMM", Locale("ru"))

    val yRange = (yAxisMax - yAxisMin).coerceAtLeast(1f)

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(20.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {

            // Шапка: макс + кол-во точек
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        "Максимум: ${"%.1f".format(points.last().weightKg)} кг",
                        style      = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color      = accentColor,
                    )
                    Text(
                        "Личных рекордов: ${points.size}",
                        style = MaterialTheme.typography.bodySmall,
                        color = onSurfaceVariant,
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(accentColor.copy(alpha = 0.12f))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                ) {
                    Text(
                        "📈 PR",
                        style      = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color      = accentColor,
                    )
                }
            }

            // Область графика
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
            ) {
                // ── Y-ось: подписи ─────────────────────────────────
                // Показываем каждую 2-ю метку чтобы не было тесно
                val displayedLabels = yLabels.filterIndexed { i, _ -> i % 2 == 0 }
                Column(
                    modifier = Modifier
                        .width(44.dp)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.SpaceBetween,
                ) {
                    displayedLabels.reversed().forEach { kg ->
                        Text(
                            "${kg.toInt()}",
                            style     = MaterialTheme.typography.labelSmall,
                            color     = onSurfaceVariant,
                            textAlign = TextAlign.End,
                            modifier  = Modifier.fillMaxWidth(),
                            fontSize  = 10.sp,
                        )
                    }
                }

                Spacer(Modifier.width(4.dp))

                // ── Canvas: сетка + линия ──────────────────────────
                Canvas(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(10.dp)),
                ) {
                    val w   = size.width
                    val h   = size.height
                    val padH = 6.dp.toPx() // вертикальный отступ

                    // Горизонтальные линии сетки (по меткам Y)
                    displayedLabels.forEach { kg ->
                        val yFrac = 1f - (kg - yAxisMin) / yRange
                        val y     = padH + yFrac * (h - 2 * padH)
                        drawLine(
                            color       = gridColor.copy(alpha = 0.4f),
                            start       = Offset(0f, y),
                            end         = Offset(w, y),
                            strokeWidth = 1.dp.toPx(),
                        )
                    }

                    if (points.size < 2) {
                        // Одна точка — просто кружок
                        val pt  = points.first()
                        val yFrac = 1f - (pt.weightKg - yAxisMin) / yRange
                        val cx  = w / 2f
                        val cy  = padH + yFrac * (h - 2 * padH)
                        drawCircle(accentColor, 6.dp.toPx(), Offset(cx, cy))
                        return@Canvas
                    }

                    val stepX = (w) / (points.size - 1).toFloat()

                    // Позиции точек
                    val coords = points.mapIndexed { i, pt ->
                        val yFrac = 1f - (pt.weightKg - yAxisMin) / yRange
                        Offset(
                            x = i * stepX,
                            y = (padH + yFrac * (h - 2 * padH)).coerceIn(padH, h - padH),
                        )
                    }

                    // Заливка под линией
                    val fillPath = Path().apply {
                        moveTo(coords.first().x, h)
                        coords.forEach { lineTo(it.x, it.y) }
                        lineTo(coords.last().x, h)
                        close()
                    }
                    drawPath(
                        path  = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(accentColor.copy(alpha = 0.35f), Color.Transparent),
                        ),
                    )

                    // Линия
                    val linePath = Path().apply {
                        moveTo(coords.first().x, coords.first().y)
                        coords.drop(1).forEach { lineTo(it.x, it.y) }
                    }
                    drawPath(
                        path  = linePath,
                        color = accentColor,
                        style = Stroke(
                            width = 2.5.dp.toPx(),
                            cap   = StrokeCap.Round,
                            join  = StrokeJoin.Round,
                        ),
                    )

                    // Точки
                    coords.forEach { c ->
                        drawCircle(accentColor, 5.dp.toPx(), c)
                        drawCircle(Color.White, 2.5.dp.toPx(), c)
                    }
                }
            }

            // X-ось: даты
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 48.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    dateFormat.format(Date(points.first().timestampMs)),
                    style = MaterialTheme.typography.labelSmall,
                    color = onSurfaceVariant,
                    fontSize = 10.sp,
                )
                if (points.size > 2) {
                    Text(
                        dateFormat.format(Date(points[points.size / 2].timestampMs)),
                        style = MaterialTheme.typography.labelSmall,
                        color = onSurfaceVariant,
                        fontSize = 10.sp,
                    )
                }
                Text(
                    dateFormat.format(Date(points.last().timestampMs)),
                    style = MaterialTheme.typography.labelSmall,
                    color = onSurfaceVariant,
                    fontSize = 10.sp,
                )
            }
        }
    }
}

@Composable
private fun EmptyChartPlaceholder() {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(20.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 48.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text("📊", fontSize = 36.sp)
                Text(
                    "Нет данных за выбранный период.\nНачните тренироваться!",
                    style     = MaterialTheme.typography.bodyMedium,
                    color     = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

// ════════════════════════════════════════════════════════════════
//  Личный рекорд — расширенная карточка
// ════════════════════════════════════════════════════════════════

private val dateFormatFull = SimpleDateFormat("d MMMM yyyy", Locale("ru"))

@Composable
private fun PrRow(item: PrWithExercise) {
    val pr         = item.pr
    val dateString = dateFormatFull.format(Date(pr.achievedAt))

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier  = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // Иконка
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint     = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(24.dp),
                )
            }

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    labelFor(pr.type),
                    style      = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color      = MaterialTheme.colorScheme.onSurface,
                )
                // Вес × повторения
                val weightStr = if (pr.weightKg % 1.0 == 0.0)
                    "${pr.weightKg.toInt()} кг"
                else
                    "${"%.1f".format(pr.weightKg)} кг"

                Text(
                    buildWeightRepsLabel(pr.type, weightStr, pr.reps, pr.estimated1Rm),
                    style      = MaterialTheme.typography.bodyMedium,
                    color      = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                )
                // Дата
                Text(
                    dateString,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

private fun labelFor(type: PrType): String = when (type) {
    PrType.ONE_RM         -> "Оценка 1ПМ"
    PrType.FIVE_RM        -> "Лучший вес × 5"
    PrType.SESSION_VOLUME -> "Макс. объём за тренировку"
}

private fun buildWeightRepsLabel(
    type: PrType,
    weightStr: String,
    reps: Int,
    estimated1Rm: Double,
): String = when (type) {
    PrType.ONE_RM -> {
        val e = if (estimated1Rm % 1.0 == 0.0) "${estimated1Rm.toInt()} кг"
                else "${"%.1f".format(estimated1Rm)} кг"
        "$weightStr × $reps  →  ≈ $e 1ПМ"
    }
    PrType.FIVE_RM -> "$weightStr × $reps"
    PrType.SESSION_VOLUME -> weightStr
}

// ════════════════════════════════════════════════════════════════
//  Строка поиска упражнения — item дропдауна
// ════════════════════════════════════════════════════════════════

@Composable
private fun ExerciseSearchItem(
    exercise: ExerciseEntity,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                exercise.name,
                style      = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color      = if (isSelected) MaterialTheme.colorScheme.primary
                             else MaterialTheme.colorScheme.onSurface,
            )
            Text(
                "${exercise.category.name.lowercase().replaceFirstChar { it.uppercase() }} · ${exercise.primaryMuscle}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (isSelected) {
            Icon(
                Icons.Default.EmojiEvents,
                contentDescription = null,
                tint     = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}