package com.gymbro.app.ui.dashboard

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

import com.gymbro.app.domain.model.LevelTier
import com.gymbro.app.domain.model.StrengthRank          // ← новый импорт
import com.gymbro.app.ui.components.ActionCard
import com.gymbro.app.ui.components.LevelCard
import com.gymbro.app.ui.components.RankDashboardCard     // ← новый импорт (если лежит в components)

// Если RankUpDialog лежит в другом месте, добавь его импорт тоже
// import com.gymbro.app.ui.components.RankUpDialog

@Composable
fun DashboardScreen(
    onStartWorkout: (sessionId: Long) -> Unit,
    onOpenPlans: () -> Unit,
    onOpenProgress: () -> Unit,
    onOpenExercises: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenRanks: () -> Unit,                    // ← добавь этот параметр
    viewModel: DashboardViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
    ) { inner ->
        if (state.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(inner),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
            return@Scaffold
        }

        DashboardContent(
            state = state,
            contentPadding = inner,
            onStartWorkout = { viewModel.onStartWorkout(onStartWorkout) },
            onOpenPlans = onOpenPlans,
            onOpenProgress = onOpenProgress,
            onOpenExercises = onOpenExercises,
            onOpenSettings = onOpenSettings,
            onOpenRanks = onOpenRanks,               // ← передаём дальше
        )

        // Level up диалог (старый)
        state.pendingCelebration?.let { pending ->
            LevelUpDialog(
                newLevel = pending.level,
                onDismiss = { viewModel.onCelebrationDismissed(pending.id) },
            )
        }

        // Новый Rank Up диалог
        state.showRankUp?.let { newRank ->
            RankUpDialog(
                newRank = newRank,
                onDismiss = { viewModel.dismissRankUp() },
            )
        }
    }
}

@Composable
private fun DashboardContent(
    state: DashboardUiState,
    contentPadding: PaddingValues,
    onStartWorkout: () -> Unit,
    onOpenPlans: () -> Unit,
    onOpenProgress: () -> Unit,
    onOpenExercises: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenRanks: () -> Unit,                     // ← новый параметр
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(contentPadding)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        // ── Top bar: greeting + settings ──────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Text(
                    text = greeting(state.profile?.name),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = "Поехали качаться 💪",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.ExtraBold,
                )
            }
            IconButton(
                onClick = onOpenSettings,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
            ) {
                Icon(
                    Icons.Default.Settings,
                    contentDescription = "Настройки",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        // ← Здесь была LevelCard, теперь заменяем на RankDashboardCard
        RankDashboardCard(
            state = state.rankState,
            onOpenRanks = onOpenRanks,
        )

        // ── Stats strip ───────────────────────────────────────────
        StatsStrip(totalSessions = state.totalSessions)

        // ── Primary CTA ───────────────────────────────────────────
        Button(
            onClick = onStartWorkout,
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
        ) {
            Icon(
                Icons.Default.PlayArrow,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
            )
            Spacer(Modifier.size(8.dp))
            Text(
                "Начать тренировку",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
        }

        // Остальные карточки без изменений...
        ActionCard(
            title = "Мои планы",
            subtitle = "Выбрать готовый или создать свой",
            icon = Icons.Default.List,
            accentColor = MaterialTheme.colorScheme.secondary,
            onClick = onOpenPlans,
        )

        ActionCard(
            title = "Прогресс",
            subtitle = "Графики, PR и история тренировок",
            icon = Icons.Default.BarChart,
            accentColor = MaterialTheme.colorScheme.tertiary,
            onClick = onOpenProgress,
        )

        ActionCard(
            title = "Упражнения",
            subtitle = "База с техникой и рекомендациями",
            icon = Icons.Default.FitnessCenter,
            accentColor = MaterialTheme.colorScheme.primary,
            onClick = onOpenExercises,
        )

        Spacer(Modifier.height(24.dp))
    }
}

// ── Stats strip ───────────────────────────────────────────────────

@Composable
private fun StatsStrip(totalSessions: Int) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            // Sessions icon
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Default.FitnessCenter,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp),
                )
            }
            Column {
                Text(
                    "$totalSessions",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 20.sp,
                )
                Text(
                    "тренировок всего",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

// ── Level up dialog ───────────────────────────────────────────────

@Composable
private fun LevelUpDialog(newLevel: Int, onDismiss: () -> Unit) {
    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(600),
        label = "levelUpScale",
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        icon = {
            Icon(
                imageVector = Icons.Default.Celebration,
                contentDescription = null,
                tint = LevelTier.of(newLevel).primary,
                modifier = Modifier.size(56.dp).scale(scale),
            )
        },
        title = {
            Text(
                "Новый уровень!",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
            )
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Ты достиг $newLevel уровня силы.", style = MaterialTheme.typography.bodyLarge)
                Spacer(Modifier.height(8.dp))
                Text(
                    LevelTier.of(newLevel).title,
                    style = MaterialTheme.typography.titleLarge,
                    color = LevelTier.of(newLevel).primary,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Разблокированы новые планы — загляни в «Мои планы».",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Продолжать!") }
        },
    )
}

// ── Helpers ───────────────────────────────────────────────────────

private fun greeting(name: String?): String {
    val base = when (java.time.LocalTime.now().hour) {
        in 5..11  -> "Доброе утро"
        in 12..17 -> "Добрый день"
        in 18..22 -> "Добрый вечер"
        else      -> "Привет"
    }
    return if (name.isNullOrBlank()) "$base!" else "$base, $name!"
}