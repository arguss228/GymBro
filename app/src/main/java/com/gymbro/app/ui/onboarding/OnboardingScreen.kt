package com.gymbro.app.ui.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymbro.app.domain.usecase.OnboardingUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val onboardingUseCase: OnboardingUseCase,
) : ViewModel() {
    fun submit(
        bench: Double?,
        squat: Double?,
        deadlift: Double?,
        onDone: () -> Unit,
    ) {
        viewModelScope.launch {
            onboardingUseCase(OnboardingUseCase.Params(bench, squat, deadlift))
            onDone()
        }
    }
}

@Composable
fun OnboardingScreen(
    onFinished: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    var bench by remember { mutableStateOf("") }
    var squat by remember { mutableStateOf("") }
    var deadlift by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Spacer(Modifier.padding(top = 48.dp))

        Text(
            "Добро пожаловать в GymBro",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            "Введите ваши текущие рабочие веса на 5 повторений. Это поможет определить стартовый уровень силы. Можно пропустить — начнёте с 1 уровня.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        LiftInput("Жим лёжа (кг × 5)", bench) { bench = it }
        LiftInput("Присед со штангой (кг × 5)", squat) { squat = it }
        LiftInput("Становая тяга (кг × 5)", deadlift) { deadlift = it }

        Spacer(Modifier.padding(top = 8.dp))

        Button(
            onClick = {
                viewModel.submit(
                    bench = bench.toDoubleOrNull(),
                    squat = squat.toDoubleOrNull(),
                    deadlift = deadlift.toDoubleOrNull(),
                    onDone = onFinished,
                )
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            ),
        ) {
            Text("Продолжить", modifier = Modifier.padding(vertical = 8.dp))
        }

        TextButton(
            onClick = {
                viewModel.submit(null, null, null, onFinished)
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Пропустить и начать с 1 уровня")
        }
    }
}

@Composable
private fun LiftInput(label: String, value: String, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = { new ->
            if (new.all { it.isDigit() || it == '.' || it == ',' }) {
                onChange(new.replace(',', '.'))
            }
        },
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = Modifier.fillMaxWidth(),
    )
}
