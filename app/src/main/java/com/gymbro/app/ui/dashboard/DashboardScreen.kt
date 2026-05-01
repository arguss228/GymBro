package com.gymbro.app.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gymbro.app.ui.components.ActionCard
import com.gymbro.app.ui.rank.RankDashboardCard
import com.gymbro.app.ui.rank.RankUpDialog

@Composable
fun DashboardScreen(
    onStartWorkout: (Long) -> Unit,
    onOpenPlans: () -> Unit,
    onOpenProgress: () -> Unit,
    onOpenExercises: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenRanks: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    // Анимация повышения ранга
    state.rankUpEvent?.let { newRank ->
        RankUpDialog(
            newRank   = newRank,
            onDismiss = { viewModel.dismissRankUp() },
        )
    }

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { inner ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize().padding(inner), Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(inner)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            // ── Приветствие ───────────────────────────────────────
            Row(
                modifier              = Modifier.fillMaxWidth(),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column {
                    Text(
                        greeting(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        "Поехали качаться 💪",
                        style      = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color      = MaterialTheme.colorScheme.onBackground,
                    )
                }
                IconButton(
                    onClick  = onOpenSettings,
                    modifier = Modifier.size(44.dp).clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                ) {
                    Icon(Icons.Default.Settings, "Настройки", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            // ── Карточка ранга ────────────────────────────────────
            RankDashboardCard(
                state       = state.rankState,
                onOpenRanks = onOpenRanks,
            )

            // ── Статистика ────────────────────────────────────────
            Surface(
                shape  = RoundedCornerShape(16.dp),
                color  = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Box(
                        modifier = Modifier.size(36.dp).clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Default.FitnessCenter, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                    }
                    Column {
                        Text("${state.totalSessions}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, fontSize = 20.sp)
                        Text("тренировок всего", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            // ── CTA ───────────────────────────────────────────────
            Button(
                onClick  = { viewModel.onStartWorkout(onStartWorkout) },
                modifier = Modifier.fillMaxWidth().height(60.dp),
                shape    = RoundedCornerShape(18.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                elevation = ButtonDefaults.buttonElevation(4.dp),
            ) {
                Icon(Icons.Default.PlayArrow, null, Modifier.size(24.dp))
                Spacer(Modifier.size(8.dp))
                Text("Начать тренировку", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }

            // ── Навигационные карточки ────────────────────────────
            ActionCard("Мои планы", "Выбрать готовый или создать свой", Icons.Default.List, MaterialTheme.colorScheme.secondary, onOpenPlans)
            ActionCard("Прогресс", "Графики, PR и история тренировок", Icons.Default.BarChart, MaterialTheme.colorScheme.tertiary, onOpenProgress)
            ActionCard("Упражнения", "База с техникой и рекомендациями", Icons.Default.FitnessCenter, MaterialTheme.colorScheme.primary, onOpenExercises)

            Spacer(Modifier.height(24.dp))
        }
    }
}

private fun greeting(): String = when (java.time.LocalTime.now().hour) {
    in 5..11  -> "Доброе утро!"
    in 12..17 -> "Добрый день!"
    in 18..22 -> "Добрый вечер!"
    else      -> "Привет!"
}