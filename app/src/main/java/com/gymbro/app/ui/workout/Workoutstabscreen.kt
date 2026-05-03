package com.gymbro.app.ui.workouts

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gymbro.app.ui.exercises.ExercisesScreen
import com.gymbro.app.ui.plans.PlansScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutsTabScreen(
    onCreatePlan: () -> Unit,
    onEditPlan: (Long) -> Unit,
    onExerciseClick: (Long) -> Unit,
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    val tabs = listOf("Мои планы", "Упражнения")

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                Text(
                    "Тренировки",
                    fontWeight = FontWeight.ExtraBold,
                    style = MaterialTheme.typography.headlineSmall,
                )
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.background,
            ),
        )

        PrimaryTabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.primary,
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            title,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                            style = MaterialTheme.typography.titleSmall,
                        )
                    },
                )
            }
        }

        when (selectedTab) {
            0 -> PlansScreen(
                onBack = { /* no back in tab mode */ },
                onCreatePlan = onCreatePlan,
                onEditPlan = onEditPlan,
                isEmbedded = true,
            )
            1 -> ExercisesScreen(
                onBack = { /* no back in tab mode */ },
                onExerciseClick = onExerciseClick,
                isEmbedded = true,
            )
        }
    }
}