package com.gymbro.app.ui.rank

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gymbro.app.domain.model.RankGroup
import com.gymbro.app.domain.model.StrengthRank
import com.gymbro.app.domain.model.StrengthRanks

@Composable
fun StrengthRanksScreen(
    onBack: () -> Unit,
    viewModel: RankViewModel = hiltViewModel(),
) {
    val state by viewModel.rankState.collectAsStateWithLifecycle()

    // Список снизу вверх: Дерево отображается последним в LazyColumn (внизу при скролле)
    // reverseLayout = true: первый элемент списка — внизу экрана
    val allRanks = remember { StrengthRanks.all } // Дерево[0] → Божество[11]
    var expandedName by remember { mutableStateOf<String?>(null) }

    val infiniteTransition = rememberInfiniteTransition(label = "clouds_anim")
    val cloudShift by infiniteTransition.animateFloat(
        initialValue  = 0f,
        targetValue   = 1f,
        animationSpec = infiniteRepeatable(tween(10000, easing = LinearEasing)),
        label         = "cloud_shift",
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF050810)),
    ) {
        LazyColumn(
            modifier       = Modifier.fillMaxSize(),
            reverseLayout  = true,          // Дерево — внизу, Божество — вверху
            contentPadding = PaddingValues(bottom = 80.dp, top = 0.dp),
        ) {

            // ── ЗЕМНАЯ ГРУППА (рисуется снизу) ───────────────────
            items(
                allRanks.filter { it.group == RankGroup.EARTH },
                key = { it.name },
            ) { rank ->
                EarthBackground {
                    RankCard(
                        rank          = rank,
                        isCurrentRank = rank.name == state.currentRank.name,
                        isExpanded    = expandedName == rank.name,
                        onClick       = {
                            expandedName = if (expandedName == rank.name) null else rank.name
                        },
                        modifier      = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                    )
                }
            }

            // ── РАЗДЕЛИТЕЛЬ: Земля → Небо (облака) ───────────────
            item(key = "divider") {
                CloudsToSkyDivider(shift = cloudShift)
            }

            // ── НЕБЕСНАЯ ГРУППА (рисуется выше) ──────────────────
            items(
                allRanks.filter { it.group == RankGroup.HEAVEN },
                key = { it.name },
            ) { rank ->
                HeavenBackground {
                    RankCard(
                        rank          = rank,
                        isCurrentRank = rank.name == state.currentRank.name,
                        isExpanded    = expandedName == rank.name,
                        onClick       = {
                            expandedName = if (expandedName == rank.name) null else rank.name
                        },
                        modifier      = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                    )
                }
            }

            // ── TopBar (при reverseLayout — рисуется последним = вверху) ──
            item(key = "topbar") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(listOf(Color(0xFF050810), Color(0xFF0A1428), Color.Transparent))
                        )
                        .padding(top = 52.dp, start = 8.dp, end = 16.dp, bottom = 16.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                        }
                        Spacer(Modifier.width(4.dp))
                        Column {
                            Text(
                                "Система рангов",
                                style      = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Black,
                                color      = Color.White,
                            )
                            Text(
                                "${state.currentRank.symbol} ${state.currentRank.name}  ·  прогрессия по 1RM",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.45f),
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── Обёртки фона ──────────────────────────────────────────────────

@Composable
private fun EarthBackground(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF0E1A0E), Color(0xFF0A130A))
                )
            )
            .drawBehind {
                // Каменная текстура — горизонтальные линии
                val lineColor = Color(0xFF1A3A1A).copy(alpha = 0.3f)
                for (i in 0..8) {
                    val y = i * 12.dp.toPx()
                    drawLine(lineColor, Offset(0f, y), Offset(size.width, y), 0.5.dp.toPx())
                }
            },
    ) { content() }
}

@Composable
private fun HeavenBackground(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF050820), Color(0xFF0A1035))
                )
            )
            .drawBehind {
                // Звёзды
                val stars = listOf(
                    0.05f to 0.2f, 0.18f to 0.7f, 0.32f to 0.15f,
                    0.47f to 0.55f, 0.61f to 0.3f, 0.75f to 0.8f,
                    0.88f to 0.1f, 0.93f to 0.6f,
                )
                stars.forEach { (xRatio, yRatio) ->
                    val pos = Offset(size.width * xRatio, size.height * yRatio)
                    drawCircle(Color.White.copy(alpha = 0.5f), 1.5.dp.toPx(), pos)
                    drawCircle(Color.White.copy(alpha = 0.1f), 4.dp.toPx(), pos)
                }
            },
    ) { content() }
}

// ── Разделитель облака ────────────────────────────────────────────

@Composable
private fun CloudsToSkyDivider(shift: Float) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .drawWithContent {
                // Фон — переход земля → небо
                drawRect(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFF050820), // небо (вверху при reverseLayout)
                            Color(0xFF0A2040),
                            Color(0xFF0D3020),
                            Color(0xFF0A1A0A), // земля (внизу)
                        )
                    )
                )
                // Горизонт — светящаяся линия
                val horizY = size.height * 0.5f
                drawLine(
                    brush       = Brush.horizontalGradient(
                        listOf(Color.Transparent, Color(0xFF00E5FF).copy(alpha = 0.5f), Color.Transparent)
                    ),
                    start       = Offset(0f, horizY),
                    end         = Offset(size.width, horizY),
                    strokeWidth = 1.5.dp.toPx(),
                )
                // Облака анимированные
                drawAnimatedClouds(shift)
                drawContent()
            },
        contentAlignment = Alignment.Center,
    ) {
        // Центральный бейдж без текста групп
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(Color(0xFF00E5FF).copy(alpha = 0.08f))
                .border(1.dp, Color(0xFF00E5FF).copy(alpha = 0.25f), RoundedCornerShape(50))
                .padding(horizontal = 24.dp, vertical = 10.dp),
        ) {
            Text("☁️  ✨  ☁️", fontSize = 20.sp)
        }
    }
}

private fun DrawScope.drawAnimatedClouds(shift: Float) {
    val w = size.width
    val cloudY = size.height * 0.42f
    val cloudColor = Color.White.copy(alpha = 0.06f)
    val dx = (shift * w * 0.25f) % (w * 0.4f)

    // Несколько облаков-эллипсов
    listOf(
        Triple(-dx + w * 0.05f, cloudY,         60f to 22f),
        Triple(-dx + w * 0.28f, cloudY * 0.85f, 90f to 32f),
        Triple(-dx + w * 0.55f, cloudY,         75f to 26f),
        Triple(-dx + w * 0.78f, cloudY * 0.9f,  85f to 30f),
        Triple(-dx + w * 1.05f, cloudY,         60f to 22f),
        // Второй слой (медленнее)
        Triple(dx * 0.5f + w * 0.15f, cloudY * 1.15f, 50f to 18f),
        Triple(dx * 0.5f + w * 0.45f, cloudY * 1.2f,  70f to 25f),
        Triple(dx * 0.5f + w * 0.72f, cloudY * 1.1f,  55f to 20f),
    ).forEach { (cx, cy, size_) ->
        val (rx, ry) = size_
        drawOval(
            color   = cloudColor,
            topLeft = Offset(cx - rx, cy - ry),
            size    = Size(rx * 2f, ry * 2f),
        )
    }
}

// ── Карточка ранга ────────────────────────────────────────────────

@Composable
private fun RankCard(
    rank: StrengthRank,
    isCurrentRank: Boolean,
    isExpanded: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "card_${rank.name}")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue  = if (isCurrentRank) 0.35f else 0f,
        targetValue   = if (isCurrentRank) 0.75f else 0f,
        animationSpec = infiniteRepeatable(tween(1800, easing = EaseInOutSine), RepeatMode.Reverse),
        label         = "card_glow_${rank.name}",
    )

    val isHeaven = rank.group == RankGroup.HEAVEN
    val cardBg   = if (isHeaven)
        Brush.linearGradient(listOf(Color(0xFF0D1B4A).copy(alpha = 0.95f), rank.primaryColor.copy(alpha = 0.1f)))
    else
        Brush.linearGradient(listOf(Color(0xFF1A2A1A).copy(alpha = 0.95f), rank.primaryColor.copy(alpha = 0.08f)))

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(cardBg)
            .then(
                if (isCurrentRank) Modifier
                    .border(2.dp, Brush.horizontalGradient(listOf(rank.primaryColor, rank.secondaryColor)), RoundedCornerShape(22.dp))
                    .drawBehind {
                        drawRoundRect(
                            color        = rank.glowColor.copy(alpha = glowAlpha * 0.35f),
                            cornerRadius = CornerRadius(22.dp.toPx()),
                            style        = Stroke(width = 8.dp.toPx()),
                        )
                    }
                else Modifier.border(1.dp, rank.primaryColor.copy(alpha = 0.15f), RoundedCornerShape(22.dp))
            )
            .clickable { onClick() }
            .padding(18.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {

            // Главная строка: символ + название
            Row(
                modifier              = Modifier.fillMaxWidth(),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                // Большой символ
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .drawBehind {
                            if (isCurrentRank) {
                                drawCircle(
                                    color  = rank.glowColor.copy(alpha = glowAlpha * 0.5f),
                                    radius = size.minDimension / 2f + 14.dp.toPx(),
                                )
                            }
                            drawCircle(
                                brush  = Brush.radialGradient(
                                    listOf(rank.primaryColor.copy(alpha = 0.18f), Color.Transparent)
                                ),
                                radius = size.minDimension / 2f,
                            )
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(rank.symbol, fontSize = 42.sp, textAlign = TextAlign.Center)
                }

                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (isCurrentRank) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(rank.primaryColor.copy(alpha = 0.18f))
                                .padding(horizontal = 8.dp, vertical = 2.dp),
                        ) {
                            Text("ВАШ РАНГ", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.ExtraBold, color = rank.primaryColor, letterSpacing = 1.sp)
                        }
                    }
                    Text(
                        rank.name,
                        style      = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black,
                        color      = rank.primaryColor,
                    )
                }

                Icon(
                    if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    null,
                    tint     = rank.primaryColor.copy(alpha = 0.55f),
                    modifier = Modifier.size(22.dp),
                )
            }

            // Раскрываемая часть — детали
            AnimatedVisibility(
                visible = isExpanded,
                enter   = expandVertically(tween(300)) + fadeIn(tween(300)),
                exit    = shrinkVertically(tween(250)) + fadeOut(tween(250)),
            ) {
                Column(
                    modifier = Modifier.padding(top = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    HorizontalDivider(color = rank.primaryColor.copy(alpha = 0.18f))

                    Text(rank.description, style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.65f), lineHeight = 22.sp)

                    Text("Требуется 1RM:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = rank.primaryColor)

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(
                            "🏋️\nЖим"    to rank.bench1RmKg,
                            "🦵\nПрисед" to rank.squat1RmKg,
                            "⬆️\nТяга"   to rank.deadlift1RmKg,
                        ).forEach { (label, kg) ->
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(rank.primaryColor.copy(alpha = 0.09f))
                                    .border(1.dp, rank.primaryColor.copy(alpha = 0.22f), RoundedCornerShape(14.dp))
                                    .padding(vertical = 14.dp, horizontal = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                Text(label, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.45f), textAlign = TextAlign.Center)
                                Text("${kg.toInt()} кг", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = rank.primaryColor)
                            }
                        }
                    }
                }
            }
        }
    }
}