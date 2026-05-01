package com.gymbro.app.ui.rank

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun Enter1RmScreen(
    onDone: () -> Unit,
    viewModel: RankViewModel = hiltViewModel(),
) {
    var bench    by remember { mutableStateOf("") }
    var squat    by remember { mutableStateOf("") }
    var deadlift by remember { mutableStateOf("") }

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }
    val alpha by animateFloatAsState(
        targetValue   = if (visible) 1f else 0f,
        animationSpec = tween(700),
        label         = "enter1rm_alpha",
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF0A0E1A), Color(0xFF0D1B2A), Color(0xFF070B14))
                )
            ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp)
                .alpha(alpha),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Spacer(Modifier.height(80.dp))

            Text("💪", fontSize = 72.sp, textAlign = TextAlign.Center)
            Spacer(Modifier.height(20.dp))

            Text(
                "Твои максимумы на 1 повторение",
                style      = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                color      = Color.White,
                textAlign  = TextAlign.Center,
            )
            Spacer(Modifier.height(10.dp))
            Text(
                "Введи максимальный вес, который ты поднимал на 1 раз. Мы определим твой ранг и будем отслеживать прогресс.",
                style     = MaterialTheme.typography.bodyMedium,
                color     = Color.White.copy(alpha = 0.55f),
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(44.dp))

            OneRmInputField(
                label    = "🏋️  Жим лёжа",
                value    = bench,
                onChange = { bench = it },
                color    = Color(0xFFFF6B35),
            )
            Spacer(Modifier.height(16.dp))
            OneRmInputField(
                label    = "🦵  Присед со штангой",
                value    = squat,
                onChange = { squat = it },
                color    = Color(0xFF4CAF50),
            )
            Spacer(Modifier.height(16.dp))
            OneRmInputField(
                label    = "⬆️  Становая тяга",
                value    = deadlift,
                onChange = { deadlift = it },
                color    = Color(0xFF2979FF),
            )

            Spacer(Modifier.height(44.dp))

            Button(
                onClick = {
                    val b = bench.toDoubleOrNull()    ?: 0.0
                    val s = squat.toDoubleOrNull()    ?: 0.0
                    val d = deadlift.toDoubleOrNull() ?: 0.0
                    viewModel.save1Rm(b, s, d)
                    onDone()
                },
                modifier = Modifier.fillMaxWidth().height(60.dp),
                shape    = RoundedCornerShape(18.dp),
                colors   = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2979FF),
                    contentColor   = Color.White,
                ),
            ) {
                Text(
                    "Определить мой ранг",
                    style      = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
            Spacer(Modifier.height(8.dp))
            TextButton(
                onClick = { viewModel.save1Rm(0.0, 0.0, 0.0); onDone() },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    "Пропустить — начать с Дерева",
                    color = Color.White.copy(alpha = 0.35f),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            Spacer(Modifier.height(60.dp))
        }
    }
}

@Composable
private fun OneRmInputField(
    label: String,
    value: String,
    onChange: (String) -> Unit,
    color: Color,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            label,
            style      = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color      = color,
        )
        OutlinedTextField(
            value         = value,
            onValueChange = { new ->
                if (new.all { it.isDigit() || it == '.' || it == ',' })
                    onChange(new.replace(',', '.'))
            },
            placeholder     = { Text("кг × 1", color = Color.White.copy(alpha = 0.25f)) },
            suffix          = { Text("кг", color = color.copy(alpha = 0.7f)) },
            modifier        = Modifier.fillMaxWidth(),
            singleLine      = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            shape           = RoundedCornerShape(14.dp),
            colors          = OutlinedTextFieldDefaults.colors(
                focusedBorderColor   = color,
                unfocusedBorderColor = color.copy(alpha = 0.35f),
                focusedTextColor     = Color.White,
                unfocusedTextColor   = Color.White,
                cursorColor          = color,
                focusedContainerColor   = Color.White.copy(alpha = 0.04f),
                unfocusedContainerColor = Color.White.copy(alpha = 0.03f),
            ),
        )
    }
}