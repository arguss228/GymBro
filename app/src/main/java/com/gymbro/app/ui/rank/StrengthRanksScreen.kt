package com.gymbro.app.ui.rank

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gymbro.app.data.repository.RankState
import com.gymbro.app.domain.model.RankGroup
import com.gymbro.app.domain.model.StrengthRank
import com.gymbro.app.domain.model.StrengthRanks
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StrengthRanksScreen(
    onBack: () -> Unit,
    viewModel: RankViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    // Ранги снизу вверх: Дерево внизу, Божество вверху при скролле вверх
    val ranksBottomToTop = remember { StrengthRanks.all.reversed() }
    var expandedRankName by remember { mutableStateOf<String?>(null) }

    // Infinite animation for clouds divider
    val infiniteTransition = rememberInfiniteTransition(label = "clouds")
    val cloudOffset by infiniteTransition.animateFloat(
        initialValue  = 0f,
        targetValue   = 1f,
        animationSpec = infiniteRepeatable(tween(8000, easing = LinearEasing)),
        label         = "cloudOffset",
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF070B14))
    ) {
        LazyColumn(
            modifier       = Modifier.fillMaxSize(),
            reverseLayout  = false, // Список идёт сверху вниз, но наполнен снизу-вверх
            contentPadding = PaddingValues(bottom = 32.dp),
        ) {
            // TopBar
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFF070B14), Color.Transparent)
                            )
                        )
                        .padding(top = 56.dp, start = 16.dp, end = 16.dp, bottom = 8.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                        }
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text(
                                "Система рангов",
                                style      = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Black,
                                color      = Color.White,
                            )
                            Text(
                                "Прогрессия по 1RM · текущий: ${state.currentRank.symbol} ${state.currentRank.name}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.5f),
                            )
                        }
                    }
                }
            }

            // Небесные ранги (вверху экрана — при reverseLayout=false небо "выше")
            val heavenRanks = ranksBottomToTop.filter { it.group == RankGroup.HEAVEN }
            val earthRanks  = ranksBottomToTop.filter { it.group == RankGroup.EARTH }

            // Небесный фон-заголовок
            item {
                HeavenHeaderSection()
            }

            items(heavenRanks, key = { it.name }) { rank ->
                RankCardItem(
                    rank          = rank,
                    isCurrentRank = rank.name == state.currentRank.name,
                    isExpanded    = expandedRankName == rank.name,
                    onClick       = {
                        expandedRankName = if (expandedRankName == rank.name) null else rank.name
                    },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                )
            }

            // Разделитель Земля → Небо (облака)
            item {
                CloudsDivider(cloudOffset = cloudOffset)
            }

            // Земные ранги
            items(earthRanks, key = { it.name }) { rank ->
                RankCardItem(
                    rank          = rank,
                    isCurrentRank = rank.name == state.currentRank.name,
                    isExpanded    = expandedRankName == rank.name,
                    onClick       = {
                        expandedRankName = if (expandedRankName == rank.name) null else rank.name
                    },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                )
            }

            // Земной фон-подвал
            item {
                EarthFooterSection()
            }
        }
    }
}

// ── Heaven header ─────────────────────────────────────────────────

@Composable
private fun HeavenHeaderSection() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF0D1B4A),
                        Color(0xFF1A1040),
                        Color(0xFF070B14),
                    )
                )
            )
            .drawBehind {
                // Звёзды
                val stars = listOf(
                    Offset(size.width * 0.1f, size.height * 0.2f),
                    Offset(size.width * 0.3f, size.height * 0.5f),
                    Offset(size.width * 0.55f, size.height * 0.15f),
                    Offset(size.width * 0.7f, size.height * 0.6f),
                    Offset(size.width * 0.85f, size.height * 0.3f),
                    Offset(size.width * 0.45f, size.height * 0.75f),
                    Offset(size.width * 0.92f, size.height * 0.7f),
                )
                stars.forEach { pos ->
                    drawCircle(Color.White.copy(alpha = 0.6f), radius = 2.dp.toPx(), center = pos)
                    drawCircle(Color.White.copy(alpha = 0.15f), radius = 6.dp.toPx(), center = pos)
                }
                // Золотое сияние сверху
                drawRect(
                    brush = Brush.verticalGradient(
                        listOf(Color(0xFFFFD700).copy(alpha = 0.08f), Color.Transparent),
                        startY = 0f, endY = size.height,
                    )
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("✨", fontSize = 28.sp)
            Spacer(Modifier.height(4.dp))
            Text(
                "НЕБЕСНЫЕ РАНГИ",
                style         = MaterialTheme.typography.labelLarge,
                fontWeight    = FontWeight.Black,
                color         = Color(0xFFFFD700).copy(alpha = 0.7f),
                letterSpacing = 3.sp,
            )
        }
    }
}

// ── Clouds divider ────────────────────────────────────────────────

@Composable
private fun CloudsDivider(cloudOffset: Float) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .drawWithContent {
                // Sky → Earth gradient
                drawRect(
                    brush = Brush.verticalGradient(
                        listOf(
                            Color(0xFF0D1B4A),
                            Color(0xFF1A3A5C),
                            Color(0xFF2D4A3E),
                            Color(0xFF1A2E1A),
                        )
                    )
                )
                // Animated cloud shapes
                drawClouds(cloudOffset)
                // Glowing horizon line
                drawRect(
                    color  = Color(0xFF00E5FF).copy(alpha = 0.3f),
                    topLeft = Offset(0f, size.height * 0.5f),
                    size   = androidx.compose.ui.geometry.Size(size.width, 2.dp.toPx()),
                )
                // Reflection below
                drawRect(
                    brush = Brush.verticalGradient(
                        listOf(Color(0xFF00E5FF).copy(alpha = 0.05f), Color.Transparent),
                        startY = size.height * 0.5f, endY = size.height,
                    )
                )
                drawContent()
            }
    ) {
        Box(
            modifier          = Modifier.fillMaxSize(),
            contentAlignment  = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(Modifier.height(20.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF00E5FF).copy(alpha = 0.1f))
                        .border(1.dp, Color(0xFF00E5FF).copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                ) {
                    Text(
                        "⬆  Небесные ранги          Земные ранги  ⬇",
                        style         = MaterialTheme.typography.labelSmall,
                        color         = Color(0xFF00E5FF).copy(alpha = 0.8f),
                        letterSpacing = 0.5.sp,
                    )
                }
            }
        }
    }
}

private fun DrawScope.drawClouds(offset: Float) {
    val w = size.width; val h = size.height * 0.45f
    val cloudColor = Color.White.copy(alpha = 0.08f)
    val shift = (offset * w * 0.3f) % (w * 0.5f)

    // Несколько эллипсов-облаков
    listOf(
        listOf(Offset(-shift + w * 0.1f, h), 80f, 30f),
        listOf(Offset(-shift + w * 0.35f, h * 0.8f), 120f, 40f),
        listOf(Offset(-shift + w * 0.6f, h), 90f, 35f),
        listOf(Offset(-shift + w * 0.85f, h * 0.85f), 100f, 38f),
        listOf(Offset(-shift + w * 1.1f, h), 80f, 30f),
    ).forEach { params ->
        @Suppress("UNCHECKED_CAST")
        val (center, rx, ry) = params as List<Any>
        drawOval(
            color     = cloudColor,
            topLeft   = Offset((center as Offset).x - rx as Float, center.y - ry as Float),
            size      = androidx.compose.ui.geometry.Size(rx * 2f, ry * 2f),
        )
    }
}

// ── Earth footer ─────────────────────────────────────────────────

@Composable
private fun EarthFooterSection() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF1A2E1A),
                        Color(0xFF0D1F0D),
                        Color(0xFF070B14),
                    )
                )
            )
            .drawBehind {
                // Текстура земли — горизонтальные штрихи
                val lineColor = Color(0xFF2E4A2E).copy(alpha = 0.4f)
                for (i in 0..5) {
                    val y = size.height * 0.3f + i * 8.dp.toPx()
                    drawLine(lineColor, Offset(0f, y), Offset(size.width, y), strokeWidth = 1.dp.toPx())
                }
            }
    )
}

// ── Rank card ─────────────────────────────────────────────────────

@Composable
private fun RankCardItem(
    rank: StrengthRank,
    isCurrentRank: Boolean,
    isExpanded: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isHeaven = rank.group == RankGroup.HEAVEN
    val bgColors = if (isHeaven) listOf(
        Color(0xFF0D1B4A).copy(alpha = 0.9f),
        rank.primaryColor.copy(alpha = 0.12f),
    ) else listOf(
        Color(0xFF1A2E1A).copy(alpha = 0.9f),
        rank.primaryColor.copy(alpha = 0.10f),
    )

    val infiniteTransition = rememberInfiniteTransition(label = "card_${rank.name}")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue  = if (isCurrentRank) 0.4f else 0f,
        targetValue   = if (isCurrentRank) 0.8f else 0f,
        animationSpec = infiniteRepeatable(tween(1800, easing = EaseInOutSine), RepeatMode.Reverse),
        label         = "glow_${rank.name}",
    )

    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Brush.linearGradient(bgColors))
                .then(
                    if (isCurrentRank) Modifier
                        .border(
                            2.dp,
                            Brush.linearGradient(listOf(rank.primaryColor, rank.secondaryColor)),
                            RoundedCornerShape(24.dp),
                        )
                        .drawBehind {
                            drawRoundRect(
                                color        = rank.glowColor.copy(alpha = glowAlpha * 0.3f),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(24.dp.toPx()),
                                style        = Stroke(width = 8.dp.toPx()),
                            )
                        }
                    else Modifier.border(
                        1.dp,
                        rank.primaryColor.copy(alpha = 0.2f),
                        RoundedCornerShape(24.dp),
                    )
                )
                .clickable { onClick() }
                .padding(20.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                // Main row
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                ) {
                    // Big symbol
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .drawBehind {
                                if (isCurrentRank) {
                                    drawCircle(
                                        color  = rank.glowColor.copy(alpha = glowAlpha * 0.5f),
                                        radius = size.minDimension / 2f + 12.dp.toPx(),
                                    )
                                }
                                drawCircle(
                                    brush  = Brush.radialGradient(
                                        listOf(
                                            rank.primaryColor.copy(alpha = 0.2f),
                                            Color.Transparent,
                                        )
                                    ),
                                    radius = size.minDimension / 2f,
                                )
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(rank.symbol, fontSize = 44.sp, textAlign = TextAlign.Center)
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        if (isCurrentRank) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(rank.primaryColor.copy(alpha = 0.2f))
                                    .padding(horizontal = 8.dp, vertical = 2.dp),
                            ) {
                                Text(
                                    "ВАШ РАНГ",
                                    style         = MaterialTheme.typography.labelSmall,
                                    fontWeight    = FontWeight.ExtraBold,
                                    color         = rank.primaryColor,
                                    letterSpacing = 1.sp,
                                )
                            }
                            Spacer(Modifier.height(4.dp))
                        }
                        Text(
                            rank.name,
                            style      = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Black,
                            color      = rank.primaryColor,
                        )
                    }

                    Icon(
                        if (isExpanded) Icons.Default.KeyboardArrowUp
                        else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint     = rank.primaryColor.copy(alpha = 0.6f),
                        modifier = Modifier.size(24.dp),
                    )
                }

                // Expanded details
                AnimatedVisibility(
                    visible = isExpanded,
                    enter   = expandVertically() + fadeIn(),
                    exit    = shrinkVertically() + fadeOut(),
                ) {
                    Column(
                        modifier = Modifier.padding(top = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        HorizontalDivider(color = rank.primaryColor.copy(alpha = 0.2f))

                        Text(
                            rank.description,
                            style      = MaterialTheme.typography.bodyMedium,
                            color      = Color.White.copy(alpha = 0.7f),
                            lineHeight = 22.sp,
                        )

                        Text(
                            "Требуется 1RM:",
                            style      = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color      = rank.primaryColor,
                        )

                        Row(
                            modifier              = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            listOf(
                                "🏋️\nЖим"    to rank.bench1RmKg,
                                "🦵\nПрисед" to rank.squat1RmKg,
                                "⬆️\nТяга"   to rank.deadlift1RmKg,
                            ).forEach { (label, weight) ->
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(rank.primaryColor.copy(alpha = 0.1f))
                                        .border(1.dp, rank.primaryColor.copy(alpha = 0.25f), RoundedCornerShape(14.dp))
                                        .padding(vertical = 12.dp, horizontal = 8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(6.dp),
                                ) {
                                    Text(label, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.5f), textAlign = TextAlign.Center)
                                    Text(
                                        "${weight.toInt()} кг",
                                        style      = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Black,
                                        color      = rank.primaryColor,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}