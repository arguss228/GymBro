package com.gymbro.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gymbro.app.data.seed.LevelThresholds
import com.gymbro.app.domain.model.LevelTier

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LevelTableSheet(
    currentLevel: Int,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                "Таблица уровней силы",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            Text(
                "Веса указаны для 5 повторений (5RM)",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(4.dp))

            // Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                listOf("Ур.", "Жим", "Присед", "Тяга").forEach { header ->
                    Text(
                        header,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                items(LevelThresholds.table) { targets ->
                    val tier = LevelTier.of(targets.level)
                    val isCurrentLevel = targets.level == currentLevel
                    val bgColor = if (isCurrentLevel)
                        tier.primary.copy(alpha = 0.15f)
                    else
                        MaterialTheme.colorScheme.surfaceVariant

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(bgColor)
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        // Level badge
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(tier.primary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                "${targets.level}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = tier.primary,
                            )
                        }

                        listOf(
                            "${targets.bench.toInt()}",
                            "${targets.squat.toInt()}",
                            "${targets.deadlift.toInt()}",
                        ).forEach { value ->
                            Text(
                                "$value кг",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isCurrentLevel) FontWeight.Bold else FontWeight.Normal,
                                color = if (isCurrentLevel)
                                    tier.primary
                                else
                                    MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }
            }
        }
    }
}