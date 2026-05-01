// ui/components/StrengthRanksSheet.kt
package com.gymbro.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gymbro.app.domain.model.BigThreeLift
import com.gymbro.app.domain.model.RankGroup
import com.gymbro.app.domain.model.StrengthRank
import com.gymbro.app.domain.model.StrengthRanks

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StrengthRanksSheet(
    currentRank: StrengthRank,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Показываем ранги снизу вверх — reverseLayout
    val ranksBottomToTop = remember { StrengthRanks.all.reversed() }

    var selectedRank by remember { mutableStateOf<StrengthRank?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.Transparent,
        dragHandle = null,
        windowInsets = WindowInsets(0),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.93f)
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF0A0E1A),
                            Color(0xFF131929),
                        )
                    )
                )
        ) {
            Column(modifier = Modifier.fillMaxSize()) {

                // ── Drag handle ──────────────────────────────────────
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp, bottom = 8.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .width(40.dp)
                            .height(4.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.3f))
                    )
                }

                // ── Header ───────────────────────────────────────────
                Column(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        "Система рангов силы",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                    )
                    Text(
                        "Прогрессия по 1RM · коснитесь ранга для деталей",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.55f),
                    )
                }

                Spacer(Modifier.height(8.dp))

                // ── Ranks list (bottom → top) ────────────────────────
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        horizontal = 16.dp,
                        bottom = 48.dp,
                        top = 4.dp,
                    ),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    // Небесная группа — сверху в списке (т.к. reversed),
                    // но вверху экрана — значит первой
                    val heavenRanks = ranksBottomToTop.filter { it.group == RankGroup.HEAVEN }
                    val earthRanks  = ranksBottomToTop.filter { it.group == RankGroup.EARTH }

                    // Разделитель небесной группы
                    item {
                        GroupDivider(
                            label = "☁️  Небесная группа",
                            gradient = Brush.horizontalGradient(
                                listOf(Color(0xFF651FFF), Color(0xFF2979FF), Color(0xFF00E5FF))
                            )
                        )
                    }

                    items(heavenRanks, key = { it.name }) { rank ->
                        RankCard(
                            rank = rank,
                            isCurrentRank = rank.name == currentRank.name,
                            isExpanded = selectedRank?.name == rank.name,
                            onClick = {
                                selectedRank = if (selectedRank?.name == rank.name) null else rank
                            },
                        )
                    }

                    item { Spacer(Modifier.height(8.dp)) }

                    // Разделитель земной группы
                    item {
                        GroupDivider(
                            label = "🌍  Земная группа",
                            gradient = Brush.horizontalGradient(
                                listOf(Color(0xFF4CAF50), Color(0xFFCD7F32), Color(0xFFFFD600))
                            )
                        )
                    }

                    items(earthRanks, key = { it.name }) { rank ->
                        RankCard(
                            rank = rank,
                            isCurrentRank = rank.name == currentRank.name,
                            isExpanded = selectedRank?.name == rank.name,
                            onClick = {
                                selectedRank = if (selectedRank?.name == rank.name) null else rank
                            },
                        )
                    }
                }
            }
        }
    }
}

// ── Group divider ─────────────────────────────────────────────────

@Composable
private fun GroupDivider(label: String, gradient: Brush) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(2.dp)
                .background(gradient)
        )
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White.copy(alpha = 0.85f),
            letterSpacing = 0.8.sp,
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(2.dp)
                .background(gradient)
        )
    }
}

// ── Individual rank card ──────────────────────────────────────────

@Composable
private fun RankCard(
    rank: StrengthRank,
    isCurrentRank: Boolean,
    isExpanded: Boolean,
    onClick: () -> Unit,
) {
    val borderAlpha = if (isCurrentRank) 1f else 0f
    val bgAlpha = if (isCurrentRank) 0.18f else 0.09f

    val isHeaven = rank.group == RankGroup.HEAVEN
    val cardBg = if (isHeaven)
        Brush.radialGradient(
            colors = listOf(
                rank.primaryColor.copy(alpha = bgAlpha + 0.04f),
                Color.Transparent,
            ),
            radius = 600f,
        )
    else
        Brush.linearGradient(
            colors = listOf(
                rank.primaryColor.copy(alpha = bgAlpha),
                rank.secondaryColor.copy(alpha = bgAlpha * 0.5f),
            )
        )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(cardBg)
            .then(
                if (isCurrentRank) Modifier.border(
                    width = 2.dp,
                    brush = Brush.horizontalGradient(
                        listOf(rank.primaryColor, rank.secondaryColor)
                    ),
                    shape = RoundedCornerShape(20.dp),
                ) else Modifier
            )
            .shadow(
                elevation = if (isCurrentRank) 12.dp else 0.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = rank.primaryColor,
                spotColor = rank.primaryColor,
            )
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

            // ── Main row: symbol + name + current badge ──────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Large symbol
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(
                                    rank.primaryColor.copy(alpha = 0.25f),
                                    rank.secondaryColor.copy(alpha = 0.10f),
                                )
                            )
                        )
                        .border(1.dp, rank.primaryColor.copy(alpha = 0.4f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(rank.symbol, fontSize = 32.sp, textAlign = TextAlign.Center)
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        rank.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = rank.primaryColor,
                    )
                    Text(
                        if (rank.group == RankGroup.EARTH) "Земная группа" else "Небесная группа",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.45f),
                        letterSpacing = 0.5.sp,
                    )
                }

                if (isCurrentRank) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(rank.primaryColor, rank.secondaryColor)
                                )
                            )
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                    ) {
                        Text(
                            "ВАШ РАНГ",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.Black,
                            letterSpacing = 1.sp,
                        )
                    }
                }
            }

            // ── Expanded: requirements + description ─────────────
            if (isExpanded) {
                HorizontalDivider(color = rank.primaryColor.copy(alpha = 0.2f))

                // Description
                Text(
                    rank.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.75f),
                    lineHeight = 20.sp,
                )

                // Requirements table
                Text(
                    "Требуется 1RM:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = rank.primaryColor,
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    listOf(
                        Triple("🏋️ Жим", BigThreeLift.BENCH_PRESS, rank.bench1RmKg),
                        Triple("🦵 Присед", BigThreeLift.BACK_SQUAT, rank.squat1RmKg),
                        Triple("⬆️ Тяга", BigThreeLift.DEADLIFT, rank.deadlift1RmKg),
                    ).forEach { (label, _, weight) ->
                        RequirementChip(
                            label = label,
                            weightKg = weight,
                            accentColor = rank.primaryColor,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }
    }
}

// ── Requirement chip ──────────────────────────────────────────────

@Composable
private fun RequirementChip(
    label: String,
    weightKg: Double,
    accentColor: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(accentColor.copy(alpha = 0.12f))
            .border(1.dp, accentColor.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
            .padding(horizontal = 8.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.6f), textAlign = TextAlign.Center)
        Text(
            "${weightKg.toInt()} кг",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Black,
            color = accentColor,
        )
    }
}