package com.gymbro.app.ui.rank

import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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

    val infiniteTransition = rememberInfiniteTransition(label = "rank_glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue  = 0.25f,
        targetValue   = 0.65f,
        animationSpec = infiniteRepeatable(tween(2200, easing = EaseInOutSine), RepeatMode.Reverse),
        label         = "glowAlpha",
    )
    val animatedProgress by animateFloatAsState(
        targetValue   = state.progress,
        animationSpec = tween(1400, easing = FastOutSlowInEasing),
        label         = "rankProgress",
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFF0D1B2A), rank.primaryColor.copy(alpha = 0.13f), Color(0xFF070B14))
                )
            )
            .border(
                1.5.dp,
                Brush.linearGradient(listOf(rank.primaryColor.copy(alpha = 0.55f), rank.secondaryColor.copy(alpha = 0.25f))),
                RoundedCornerShape(28.dp),
            )
            .drawBehind {
                drawCircle(rank.glowColor.copy(alpha = glowAlpha * 0.12f), radius = size.width * 0.75f)
            }
            .clickable { onOpenRanks() }
            .padding(24.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {

            // ── Ранг ─────────────────────────────────────────────
            Row(
                modifier              = Modifier.fillMaxWidth(),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .drawBehind {
                                drawCircle(rank.glowColor.copy(alpha = glowAlpha * 0.45f), radius = size.minDimension / 2f + 10.dp.toPx())
                                drawCircle(rank.primaryColor.copy(alpha = 0.1f), radius = size.minDimension / 2f, style = Stroke(2.dp.toPx()))
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(rank.symbol, fontSize = 38.sp)
                    }

                    Column {
                        Text(
                            rank.name,
                            style      = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Black,
                            color      = rank.primaryColor,
                        )
                        Text(
                            if (rank.group == RankGroup.EARTH) "Земная группа"
                            else "Небесная группа",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White.copy(alpha = 0.4f),
                        )
                    }
                }
                Icon(
                    Icons.Default.KeyboardArrowRight,
                    contentDescription = "Все ранги",
                    tint     = rank.primaryColor.copy(alpha = 0.5f),
                    modifier = Modifier.size(26.dp),
                )
            }

            // ── 1RM текущие ───────────────────────────────────────
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                listOf(
                    Triple("🏋️ Жим",   state.bench,    state.kgToBench),
                    Triple("🦵 Присед", state.squat,    state.kgToSquat),
                    Triple("⬆️ Тяга",  state.deadlift, state.kgToDeadlift),
                ).forEach { (label, current, kgLeft) ->
                    LiftChip(
                        label    = label,
                        current  = current,
                        kgLeft   = kgLeft,
                        color    = rank.primaryColor,
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            // ── Прогресс до следующего ────────────────────────────
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically,
                ) {
                    Text(
                        if (state.nextRank != null) "До ${state.nextRank.symbol} ${state.nextRank.name}"
                        else "Максимальный ранг достигнут",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White.copy(alpha = 0.55f),
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
                // Progress bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(9.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(Color.White.copy(alpha = 0.07f)),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(animatedProgress)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(5.dp))
                            .background(Brush.horizontalGradient(listOf(rank.primaryColor, rank.secondaryColor))),
                    )
                }
            }

            Text(
                "Нажмите, чтобы увидеть все ранги →",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.25f),
            )
        }
    }
}

@Composable
private fun LiftChip(
    label: String,
    current: Double,
    kgLeft: Double,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(color.copy(alpha = 0.07f))
            .border(1.dp, color.copy(alpha = 0.18f), RoundedCornerShape(14.dp))
            .padding(horizontal = 8.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.45f), maxLines = 1)
        Text("${current.toInt()} кг", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = Color.White)
        if (kgLeft > 0.0) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                Icon(Icons.Default.TrendingUp, null, tint = color, modifier = Modifier.size(10.dp))
                Text("+${kgLeft.toInt()} кг", style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.Bold)
            }
        } else {
            Text("✓", style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.Bold)
        }
    }
}