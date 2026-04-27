package com.gymbro.app.ui.dashboard

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gymbro.app.domain.model.LevelTier
import com.gymbro.app.ui.components.ActionCard
import com.gymbro.app.ui.components.LevelCard

@Composable
fun DashboardScreen(
    onStartWorkout: (sessionId: Long) -> Unit,
    onOpenPlans: () -> Unit,
    onOpenProgress: () -> Unit,
    onOpenExercises: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
    ) { inner ->
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(inner),
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
        )

        // Показываем поздравление при новом уровне.
        state.pendingCelebration?.let { pending ->
            // Анимация: лёгкий scale-in появления — встроен в AlertDialog контент.
            LevelUpDialog(
                newLevel = pending.level,
                onDismiss = { viewModel.onCelebrationDismissed(pending.id) },
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
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(contentPadding)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        // Приветствие
        Column {
            Text(
                text = greeting(state.profile?.name),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = "Поехали качаться 💪",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.ExtraBold,
            )
        }

        // Большая карточка уровня
        LevelCard(level = state.level)

        // Статистика одной строкой
        Text(
            text = "Всего тренировок: ${state.totalSessions}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        // Основная CTA — начать тренировку
        Button(
            onClick = onStartWorkout,
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ),
        ) {
            Icon(Icons.Default.PlayArrow, contentDescription = null)
            Spacer(Modifier.size(8.dp))
            Text(
                text = "Начать тренировку",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
        }

        // Action cards
        ActionCard(
            title = "Мои планы",
            subtitle = "Выбрать готовый или создать свой",
            icon = Icons.Default.List,
            accentColor = MaterialTheme.colorScheme.secondary,
            onClick = onOpenPlans,
        )
        ActionCard(
            title = "Прогресс",
            subtitle = "Графики, PR и история",
            icon = Icons.Default.BarChart,
            accentColor = MaterialTheme.colorScheme.tertiary,
            onClick = onOpenProgress,
        )
        ActionCard(
            title = "Упражнения",
            subtitle = "База упражнений с поиском",
            icon = Icons.Default.FitnessCenter,
            accentColor = MaterialTheme.colorScheme.primary,
            onClick = onOpenExercises,
        )

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun LevelUpDialog(
    newLevel: Int,
    onDismiss: () -> Unit,
) {
    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(600),
        label = "levelUpScale"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Celebration,
                contentDescription = null,
                tint = LevelTier.of(newLevel).primary,
                modifier = Modifier
                    .size(56.dp)
                    .scale(scale),
            )
        },
        title = {
            Text(
                text = "Новый уровень!",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
            )
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Ты достиг $newLevel уровня силы.",
                    style = MaterialTheme.typography.bodyLarge,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = LevelTier.of(newLevel).title,
                    style = MaterialTheme.typography.titleLarge,
                    color = LevelTier.of(newLevel).primary,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Разблокированы новые планы — загляни в раздел «Мои планы».",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Продолжить") }
        },
    )
}

private fun greeting(name: String?): String {
    val base = when (java.time.LocalTime.now().hour) {
        in 5..11 -> "Доброе утро"
        in 12..17 -> "Добрый день"
        in 18..22 -> "Добрый вечер"
        else -> "Привет"
    }
    return if (name.isNullOrBlank()) "$base!" else "$base, $name!"
}
