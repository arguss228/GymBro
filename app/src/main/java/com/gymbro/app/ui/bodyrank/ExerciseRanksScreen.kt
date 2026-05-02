package com.gymbro.app.ui.bodyrank

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gymbro.app.domain.model.ExerciseRank

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseRanksScreen(
    onBack: () -> Unit,
    viewModel: BodyAnalysisViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Ранги упражнений", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
    ) { inner ->
        val allExerciseRanks = state.bodyRank?.muscleGroups
            ?.flatMap { it.exerciseRanks }
            ?.sortedByDescending { it.rankIndex }
            ?: emptyList()

        if (allExerciseRanks.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(inner), Alignment.Center) {
                Text(
                    "Нет данных.\nДобавьте максимумы в разделе «Анализ тела».",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(inner),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(allExerciseRanks, key = { it.exerciseId }) { exRank ->
                ExerciseRankCard(exRank)
            }
        }
    }
}

@Composable
private fun ExerciseRankCard(exRank: ExerciseRank) {
    val rank = exRank.rank
    Card(
        shape  = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(rank.primaryColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(rank.symbol, fontSize = 26.sp)
            }
            Column(Modifier.weight(1f)) {
                Text(exRank.exerciseName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text(
                    "${exRank.best1Rm.toInt()} кг × 1",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                rank.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.ExtraBold,
                color = rank.primaryColor,
            )
        }
    }
}