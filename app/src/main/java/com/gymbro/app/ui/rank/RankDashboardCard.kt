package com.gymbro.app.ui.rank

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gymbro.app.data.repository.RankState
import com.gymbro.app.domain.model.RankGroup

@Composable
fun RankDashboardCard(
    state: RankState,
    onOpenRanks: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val rank = state.currentRank
    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue   = 0.3f,
        targetValue    = 0.7f,
        animationSpec  = infiniteRepeatable(tween(2000, easing = EaseInOutSine), RepeatMode.Reverse),
        label          = "glowAlpha",
    )

    val animatedProgress by animateFloatAsState(
        targetValue   = state.progress,
        animationSpec = tween(1200, easing = FastOutSlowInEasing),
        label         = "progress",
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF0D1B2A),
                        rank.primaryColor.copy(alpha = 0.15f),
                        Color(0xFF0A0E1A),
                    )
                )
            )
            .border(
                width = 1.5.dp,
                brush = Brush.linearGradient(
                    listOf(rank.primaryColor.copy(alpha = 0.6f), rank.secondaryColor.copy(alpha = 0.3f))
                ),
                shape = RoundedCornerShape(28.dp),
            )
            .drawBehind {
                drawCircle(
                    color  = rank.glowColor.copy(alpha = glowAlpha * 0.15f),
                    radius = size.width * 0.8f,
                    center = center,
                )
            }
            .clickable { onOpenRanks() }
            .padding(24.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {

            // ── Ранг ──────────────────────────────────────────────
            Row(
                modifier              = Modifier.fillMaxWidth(),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    // Symbol with glow ring
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .drawBehind {
                                drawCircle(
                                    color  = rank.glowColor.copy(alpha = glowAlpha * 0.4f),
                                    radius = size.minDimension / 2f + 8.dp.toPx(),
                                )
                                drawCircle(
                                    brush  = Brush.radialGradient(
                                        listOf(rank.primaryColor.copy(alpha = 0.3f), Color.Transparent)
                                    ),
                                    radius = size.minDimension / 2f,
                                    style  = Stroke(width = 2.dp.toPx()),
                                )
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(rank.symbol, fontSize = 40.sp)
                    }

                    Column {
                        Text(
                            rank.name,
                            style      = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Black,
                            color      = rank.primaryColor,
                        )
                        Text(
                            if (rank.group == RankGroup.EARTH) "Земная группа" else "Небесная группа",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White.copy(alpha = 0.45f),
                        )
                    }
                }

                Icon(
                    Icons.Default.KeyboardArrowRight,
                    contentDescription = "Все ранги",
                    tint     = rank.primaryColor.copy(alpha = 0.6f),
                    modifier = Modifier.size(28.dp),
                )
            }

            // ── 1RM текущие ───────────────────────────────────────
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                listOf(
                    Triple("🏋️ Жим",     state.bench,    state.kgToBench),
                    Triple("🦵 Присед",   state.squat,    state.kgToSquat),
                    Triple("⬆️ Тяга",     state.deadlift, state.kgToDeadlift),
                ).forEach { (label, current, kgLeft) ->
                    LiftStatChip(
                        label   = label,
                        current = current,
                        kgLeft  = kgLeft,
                        color   = rank.primaryColor,
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            // ── Прогресс-бар ──────────────────────────────────────
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically,
                ) {
                    Text(
                        if (state.nextRank != null) "До ${state.nextRank.name}"
                        else "Максимальный ранг",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White.copy(alpha = 0.6f),
                    )
                    if (state.nextRank != null) {
                        Text(
                            "${(state.progress * 100).toInt()}%",
                            style      = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color      = rank.primaryColor,
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.White.copy(alpha = 0.08f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(animatedProgress)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(rank.primaryColor, rank.secondaryColor)
                                )
                            )
                    )
                }
                if (state.nextRank != null) {
                    Text(
                        "${state.nextRank.symbol} ${state.nextRank.name}",
                        style = MaterialTheme.typography.labelSmall,
                        color = state.nextRank.primaryColor.copy(alpha = 0.7f),
                    )
                }
            }

            Text(
                "Нажмите, чтобы увидеть все ранги →",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.3f),
            )
        }
    }
}

@Composable
private fun LiftStatChip(
    label: String,
    current: Double,
    kgLeft: Double,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(color.copy(alpha = 0.08f))
            .border(1.dp, color.copy(alpha = 0.2f), RoundedCornerShape(14.dp))
            .padding(horizontal = 8.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.5f), maxLines = 1)
        Text(
            "${current.toInt()} кг",
            style      = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Black,
            color      = Color.White,
        )
        if (kgLeft > 0.0) {
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Icon(Icons.Default.TrendingUp, null, tint = color, modifier = Modifier.size(10.dp))
                Text("+${kgLeft.toInt()} кг", style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.Bold)
            }
        } else {
            Text("✓", style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.Bold)
        }
    }
}