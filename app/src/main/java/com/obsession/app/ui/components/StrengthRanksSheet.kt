package com.obsession.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.obsession.app.domain.rank.Rank

// ════════════════════════════════════════════════════════════════
//  ИСПРАВЛЕНИЕ: актуальная версия экрана рангов
//  — только визуальное разделение, без текстовых надписей
//    «Земная группа» и «Небесная группа»
// ════════════════════════════════════════════════════════════════

@Composable
fun StrengthRanksSheet(
    currentRank: Rank,
    onDismiss: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF070B14)),
        ) {
            // Фоновый градиент
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                currentRank.primaryColor.copy(alpha = 0.22f),
                                Color.Transparent,
                            )
                        )
                    )
            )

            Column(modifier = Modifier.fillMaxSize()) {
                // ── Шапка ─────────────────────────────────────────────
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "Система рангов",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Закрыть",
                            tint = Color.White.copy(alpha = 0.6f),
                        )
                    }
                }

                // ── Текущий ранг (hero) ───────────────────────────────
                CurrentRankHero(rank = currentRank, modifier = Modifier.padding(horizontal = 16.dp))

                Spacer(Modifier.height(24.dp))

                // ── Все ранги — только визуально ─────────────────────
                Text(
                    "Все ранги",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.padding(horizontal = 20.dp),
                )
                Spacer(Modifier.height(12.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    itemsIndexed(Rank.values()) { index, rank ->
                        RankRow(
                            rank = rank,
                            isCurrentRank = rank == currentRank,
                            isUnlocked = rank.ordinal <= currentRank.ordinal,
                        )
                    }
                    item { Spacer(Modifier.navigationBarsPadding()) }
                }
            }
        }
    }
}

// ── Hero блок текущего ранга ─────────────────────────────────────

@Composable
private fun CurrentRankHero(rank: Rank, modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "hero_glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue  = 0.15f,
        targetValue   = 0.40f,
        animationSpec = infiniteRepeatable(tween(2200, easing = EaseInOutSine), RepeatMode.Reverse),
        label         = "glow",
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        rank.primaryColor.copy(alpha = 0.18f),
                        rank.secondaryColor.copy(alpha = 0.10f),
                    )
                )
            )
            .border(
                width = 1.5.dp,
                brush = Brush.linearGradient(listOf(rank.primaryColor.copy(0.6f), rank.secondaryColor.copy(0.3f))),
                shape = RoundedCornerShape(28.dp),
            )
            .drawBehind {
                drawCircle(
                    color  = rank.primaryColor.copy(alpha = glowAlpha),
                    radius = size.minDimension * 0.55f,
                    center = Offset(size.width * 0.85f, size.height * 0.2f),
                    blendMode = BlendMode.Screen,
                )
            }
            .padding(20.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Символ ранга
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(rank.primaryColor.copy(alpha = 0.2f))
                    .border(2.dp, rank.primaryColor.copy(0.5f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(rank.symbol, fontSize = 36.sp)
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    "Ваш текущий ранг",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.45f),
                    letterSpacing = 1.sp,
                )
                Text(
                    rank.name,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black,
                    color = rank.primaryColor,
                )
                Text(
                    rank.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.55f),
                )
            }
        }
    }
}

// ── Строка одного ранга — ТОЛЬКО ВИЗУАЛЬНО, без текстовых групп ──

@Composable
private fun RankRow(
    rank: Rank,
    isCurrentRank: Boolean,
    isUnlocked: Boolean,
) {
    val alpha = if (isUnlocked) 1f else 0.35f

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                if (isCurrentRank)
                    Brush.linearGradient(
                        listOf(
                            rank.primaryColor.copy(alpha = 0.25f),
                            rank.secondaryColor.copy(alpha = 0.12f),
                        )
                    )
                else
                    Brush.linearGradient(
                        listOf(
                            Color.White.copy(alpha = 0.04f * alpha),
                            Color.Transparent,
                        )
                    )
            )
            .border(
                width = if (isCurrentRank) 1.5.dp else 0.5.dp,
                color = if (isCurrentRank) rank.primaryColor.copy(0.7f)
                        else Color.White.copy(alpha = 0.08f),
                shape = RoundedCornerShape(20.dp),
            )
            .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // Символ ранга (цветной кружок + эмодзи)
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                rank.primaryColor.copy(alpha = 0.30f * alpha),
                                rank.secondaryColor.copy(alpha = 0.10f * alpha),
                            )
                        )
                    )
                    .border(
                        width = if (isCurrentRank) 2.dp else 1.dp,
                        color = rank.primaryColor.copy(alpha = 0.6f * alpha),
                        shape = CircleShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    rank.symbol,
                    fontSize = 26.sp,
                    color = Color.White.copy(alpha = alpha),
                )
            }

            // Название + описание
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        rank.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isCurrentRank) rank.primaryColor else Color.White.copy(alpha = alpha),
                    )
                    if (isCurrentRank) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(rank.primaryColor.copy(alpha = 0.22f))
                                .padding(horizontal = 8.dp, vertical = 2.dp),
                        ) {
                            Text(
                                "• СЕЙЧАС",
                                style = MaterialTheme.typography.labelSmall,
                                color = rank.primaryColor,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 9.sp,
                                letterSpacing = 0.5.sp,
                            )
                        }
                    }
                }
                Text(
                    rank.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.45f * alpha),
                )
            }

            // Визуальный индикатор уровня (цветная полоса)
            RankLevelBar(rank = rank, isUnlocked = isUnlocked)
        }
    }
}

/**
 * Вертикальная цветная полоска — визуальный уровень ранга.
 * Заменяет текстовые надписи «Земная группа» / «Небесная группа».
 */
@Composable
private fun RankLevelBar(rank: Rank, isUnlocked: Boolean) {
    val totalRanks = Rank.values().size
    val filled     = (rank.ordinal + 1).toFloat() / totalRanks.toFloat()
    val alpha      = if (isUnlocked) 1f else 0.25f

    Box(
        modifier = Modifier
            .width(6.dp)
            .height(52.dp)
            .clip(RoundedCornerShape(3.dp))
            .background(Color.White.copy(alpha = 0.06f)),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(filled)
                .align(Alignment.BottomCenter)
                .clip(RoundedCornerShape(3.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            rank.primaryColor.copy(alpha = alpha),
                            rank.secondaryColor.copy(alpha = alpha * 0.7f),
                        )
                    )
                ),
        )
    }
}