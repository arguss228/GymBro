package com.gymbro.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gymbro.app.domain.model.BigThreeLift
import com.gymbro.app.domain.model.StrengthLevel

/**
 * Главная «геройская» карточка уровня.
 * Адаптируется к светлой и тёмной теме через MaterialTheme.colorScheme.
 * При нажатии показывает [LevelTableSheet].
 */
@Composable
fun LevelCard(
    level: StrengthLevel,
    modifier: Modifier = Modifier,
) {
    val tier = level.tier
    val accentColor = tier.primary

    // Animate progress bar on first render / value change
    val animatedProgress by animateFloatAsState(
        targetValue = if (level.isMaxLevel) 1f else level.progressToNext,
        animationSpec = tween(durationMillis = 1000),
        label = "levelProgress",
    )

    var showSheet by remember { mutableStateOf(false) }
    if (showSheet) {
        LevelTableSheet(
            currentLevel = level.level,
            onDismiss    = { showSheet = false },
        )
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { showSheet = true },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = accentColor.copy(alpha = 0.10f),
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // ── Top row: tier badge + level number ──────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                // Icon + tier label
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(accentColor.copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                    Column {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(accentColor.copy(alpha = 0.18f))
                                .padding(horizontal = 8.dp, vertical = 2.dp),
                        ) {
                            Text(
                                tier.title.uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                color = accentColor,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.sp,
                            )
                        }
                        Text(
                            "Уровень силы",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                // Level number + arrow hint
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "${level.level}",
                        fontSize = 56.sp,
                        fontWeight = FontWeight.Black,
                        color = accentColor,
                    )
                    Icon(
                        Icons.Default.KeyboardArrowRight,
                        contentDescription = "Таблица уровней",
                        tint = accentColor.copy(alpha = 0.5f),
                        modifier = Modifier.size(22.dp),
                    )
                }
            }

            // ── Progress bar ────────────────────────────────────
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = if (level.isMaxLevel) "Максимальный уровень"
                               else "До уровня ${level.level + 1}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    if (!level.isMaxLevel) {
                        Text(
                            "${(level.progressToNext * 100).toInt()}%",
                            style = MaterialTheme.typography.labelMedium,
                            color = accentColor,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
                LinearProgressIndicator(
                    progress      = { animatedProgress },
                    modifier      = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp)),
                    color         = accentColor,
                    trackColor    = accentColor.copy(alpha = 0.18f),
                    strokeCap     = StrokeCap.Round,
                )
            }

            // ── Lift breakdown ──────────────────────────────────
            if (!level.isMaxLevel && level.kgToNextByLift.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    BigThreeLift.values().forEach { lift ->
                        val kgLeft = level.kgToNextByLift[lift] ?: 0.0
                        val best   = level.best1RM[lift] ?: 0.0
                        LiftChip(
                            liftShort    = lift.shortName,
                            best1Rm      = best,
                            kgRemaining  = kgLeft,
                            accentColor  = accentColor,
                            modifier     = Modifier.weight(1f),
                        )
                    }
                }
            }

            // ── Tap hint ────────────────────────────────────────
            Text(
                "Нажмите, чтобы увидеть таблицу уровней →",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
            )
        }
    }
}

@Composable
private fun LiftChip(
    liftShort: String,
    best1Rm: Double,
    kgRemaining: Double,
    accentColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(accentColor.copy(alpha = 0.10f))
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            liftShort,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            "${best1Rm.toInt()} кг",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Icon(
                Icons.Default.TrendingUp,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(12.dp),
            )
            Text(
                if (kgRemaining > 0.0) "+${kgRemaining.toInt()} кг" else "✓",
                style = MaterialTheme.typography.labelSmall,
                color = accentColor,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}