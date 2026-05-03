package com.gymbro.app.ui.home

import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gymbro.app.domain.model.RankGroup
import com.gymbro.app.domain.model.StrengthRank
import com.gymbro.app.ui.dashboard.DashboardViewModel
import com.gymbro.app.ui.rank.RankUpDialog

private val motivationalQuotes = listOf(
    "Боль временна. Гордость вечна.",
    "Каждый подход — это шаг к лучшей версии себя.",
    "Не жди идеального момента. Начни прямо сейчас.",
    "Железо не лжёт. Только ты знаешь, на что способен.",
    "Дисциплина — это мост между целями и достижениями.",
)

@Composable
fun HomeScreen(
    onStartWorkout: (Long) -> Unit,
    onOpenSettings: () -> Unit,
    onOpenRanks: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    state.rankUpEvent?.let { newRank ->
        RankUpDialog(newRank = newRank, onDismiss = { viewModel.dismissRankUp() })
    }

    if (state.isLoading) {
        Box(Modifier.fillMaxSize(), Alignment.Center) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
        return
    }

    val rank = state.rankState.currentRank
    val quote = remember { motivationalQuotes.random() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState()),
    ) {
        // ── Header: "BURN FOR DISCIPLINE" + Settings ─────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 56.dp, start = 20.dp, end = 20.dp, bottom = 8.dp),
        ) {
            Column(modifier = Modifier.align(Alignment.CenterStart)) {
                Text(
                    "BURN FOR",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontSize = 32.sp, letterSpacing = 4.sp,
                    ),
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.45f),
                )
                Text(
                    "DISCIPLINE",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontSize = 38.sp, letterSpacing = 5.sp,
                    ),
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            IconButton(
                onClick = onOpenSettings,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
            ) {
                Icon(Icons.Default.Settings, "Настройки", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        // ── Rank Hero Card ────────────────────────────────────────
        RankHeroCard(
            rank = rank,
            progress = state.rankState.progress,
            nextRank = state.rankState.nextRank,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
        )

        // ── CTA Button ───────────────────────────────────────────
        Button(
            onClick = { viewModel.onStartWorkout(onStartWorkout) },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).height(68.dp),
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp, pressedElevation = 2.dp),
        ) {
            Icon(Icons.Default.PlayArrow, null, modifier = Modifier.size(30.dp))
            Spacer(Modifier.size(10.dp))
            Text("Начать тренировку", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
        }

        // ── Quick stats ───────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            QuickStatCard("Тренировок", state.totalSessions.toString(), "🏋️", Modifier.weight(1f))
            QuickStatCard("Ранг", rank.name, rank.symbol, Modifier.weight(1f), rank.primaryColor)
        }

        // ── Motivational quote ────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.05f),
                        )
                    )
                )
                .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
                .padding(horizontal = 20.dp, vertical = 16.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.LocalFireDepartment,
                    null,
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                    modifier = Modifier.size(20.dp),
                )
                Text(
                    "\"$quote\"",
                    style = MaterialTheme.typography.bodyMedium,
                    fontStyle = FontStyle.Italic,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                    lineHeight = 20.sp,
                )
            }
        }
    }
}

@Composable
private fun RankHeroCard(
    rank: StrengthRank,
    progress: Float,
    nextRank: StrengthRank?,
    modifier: Modifier = Modifier,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "hero_glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.65f,
        animationSpec = infiniteRepeatable(tween(2200, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "hero_glow_alpha",
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(32.dp))
            .background(
                Brush.linearGradient(listOf(Color(0xFF0D1B2A), rank.primaryColor.copy(alpha = 0.18f), Color(0xFF070B14)))
            )
            .border(
                2.dp,
                Brush.linearGradient(listOf(rank.primaryColor.copy(alpha = 0.7f), rank.secondaryColor.copy(alpha = 0.35f))),
                RoundedCornerShape(32.dp),
            )
            .drawBehind { drawCircle(rank.glowColor.copy(alpha = glowAlpha * 0.15f), radius = size.width * 0.8f) }
            .padding(28.dp),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            // Symbol
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .drawBehind {
                        drawCircle(rank.glowColor.copy(alpha = glowAlpha * 0.5f), radius = size.minDimension / 2f + 18.dp.toPx())
                        drawCircle(rank.primaryColor.copy(alpha = 0.15f), radius = size.minDimension / 2f)
                    },
                contentAlignment = Alignment.Center,
            ) {
                Text(rank.symbol, fontSize = 66.sp, textAlign = TextAlign.Center)
            }

            // Name
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    if (rank.group == RankGroup.EARTH) "ЗЕМНАЯ ГРУППА" else "НЕБЕСНАЯ ГРУППА",
                    style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 3.sp),
                    color = Color.White.copy(alpha = 0.4f),
                )
                Text(
                    rank.name,
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Black,
                    color = rank.primaryColor,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    rank.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.55f),
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp,
                )
            }

            // Progress
            if (nextRank != null) {
                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("До ${nextRank.symbol} ${nextRank.name}", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.5f))
                        Text("${(progress * 100).toInt()}%", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = rank.primaryColor)
                    }
                    Box(
                        modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp)).background(Color.White.copy(alpha = 0.07f)),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(progress.coerceIn(0f, 1f))
                                .height(10.dp)
                                .clip(RoundedCornerShape(5.dp))
                                .background(Brush.horizontalGradient(listOf(rank.primaryColor, rank.secondaryColor))),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickStatCard(
    label: String,
    value: String,
    icon: String,
    modifier: Modifier = Modifier,
    accentColor: Color = MaterialTheme.colorScheme.primary,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(16.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(icon, fontSize = 24.sp)
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = accentColor, maxLines = 1)
            Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}