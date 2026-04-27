package com.gymbro.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gymbro.app.domain.model.BigThreeLift
import com.gymbro.app.domain.model.StrengthLevel

/**
 * Большая «геройская» карточка уровня на Dashboard.
 * Цветовая гамма зависит от tier: серый → синий → золотой → фиолетовый.
 */
@Composable
fun LevelCard(
    level: StrengthLevel,
    modifier: Modifier = Modifier,
) {
    val tier = level.tier
    val gradient = Brush.linearGradient(
        colors = listOf(tier.primary, tier.secondary)
    )

    Surface(
        modifier = modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = Color.Transparent,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(gradient)
                .padding(24.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Header row: icon + tier label + "Level X"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f))
                                .border(2.dp, Color.White.copy(alpha = 0.4f), CircleShape),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(32.dp),
                            )
                        }
                        Column {
                            Text(
                                text = tier.title.uppercase(),
                                style = MaterialTheme.typography.labelLarge,
                                color = Color.White.copy(alpha = 0.85f),
                            )
                            Text(
                                text = "Уровень силы",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }

                    // Level number — очень крупный
                    Text(
                        text = "${level.level}",
                        fontSize = 72.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                    )
                }

                // Progress bar
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = if (level.isMaxLevel) "Максимальный уровень" else "До уровня ${level.level + 1}",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White.copy(alpha = 0.9f),
                        )
                        if (!level.isMaxLevel) {
                            Text(
                                text = "${(level.progressToNext * 100).toInt()}%",
                                style = MaterialTheme.typography.labelLarge,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                    LevelProgressBar(
                        progress = if (level.isMaxLevel) 1f else level.progressToNext,
                        gradientStart = Color.White,
                        gradientEnd = Color.White.copy(alpha = 0.8f),
                        trackColor = Color.Black.copy(alpha = 0.2f),
                    )
                }

                // Breakdown: сколько кг осталось по каждому лифту
                if (!level.isMaxLevel) {
                    Spacer(Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        BigThreeLift.values().forEach { lift ->
                            val kg = level.kgToNextByLift[lift] ?: 0.0
                            val best = level.best5RM[lift] ?: 0.0
                            LiftDelta(
                                liftShort = lift.shortName,
                                best5Rm = best,
                                kgRemaining = kg,
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LiftDelta(
    liftShort: String,
    best5Rm: Double,
    kgRemaining: Double,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.15f))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = liftShort,
            style = MaterialTheme.typography.labelLarge,
            color = Color.White.copy(alpha = 0.85f),
        )
        Text(
            text = "${best5Rm.toInt()} кг",
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
            fontWeight = FontWeight.Bold,
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Icon(
                imageVector = Icons.Default.TrendingUp,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.9f),
                modifier = Modifier.size(14.dp),
            )
            Text(
                text = if (kgRemaining > 0.0) "+${kgRemaining.toInt()} кг" else "✓",
                style = MaterialTheme.typography.labelLarge,
                color = Color.White,
            )
        }
    }
}
