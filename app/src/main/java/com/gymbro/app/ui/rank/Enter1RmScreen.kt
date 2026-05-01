package com.gymbro.app.ui.rank

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
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

    // Entrance animation
    val alpha by produceState(0f) {
        animate(0f, 1f, animationSpec = tween(800)) { v, _ -> value = v }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF0A0E1A), Color(0xFF1A1F35), Color(0xFF0D1B2A))
                )
            )
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
            Spacer(Modifier.height(60.dp))

            Text("💪", fontSize = 72.sp, textAlign = TextAlign.Center)
            Spacer(Modifier.height(16.dp))

            Text(
                "Введи свои максимумы",
                style      = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                color      = Color.White,
                textAlign  = TextAlign.Center,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Укажи максимальный вес на 1 повторение в каждом упражнении. Мы определим твой начальный ранг.",
                style     = MaterialTheme.typography.bodyMedium,
                color     = Color.White.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(40.dp))

            OneRmField(
                label     = "🏋️  Жим лёжа",
                hint      = "кг × 1",
                value     = bench,
                onChange  = { bench = it },
                color     = Color(0xFFFF6B35),
            )
            Spacer(Modifier.height(16.dp))
            OneRmField(
                label     = "🦵  Присед со штангой",
                hint      = "кг × 1",
                value     = squat,
                onChange  = { squat = it },
                color     = Color(0xFF4CAF50),
            )
            Spacer(Modifier.height(16.dp))
            OneRmField(
                label     = "⬆️  Становая тяга",
                hint      = "кг × 1",
                value     = deadlift,
                onChange  = { deadlift = it },
                color     = Color(0xFF2979FF),
            )

            Spacer(Modifier.height(40.dp))

            Button(
                onClick = {
                    val b = bench.toDoubleOrNull() ?: 0.0
                    val s = squat.toDoubleOrNull() ?: 0.0
                    val d = deadlift.toDoubleOrNull() ?: 0.0
                    viewModel.save1Rm(b, s, d)
                    onDone()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                shape  = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
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

            TextButton(
                onClick = { viewModel.save1Rm(0.0, 0.0, 0.0); onDone() },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    "Пропустить — начать с Дерева",
                    color = Color.White.copy(alpha = 0.4f),
                )
            }

            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
private fun OneRmField(
    label: String,
    hint: String,
    value: String,
    onChange: (String) -> Unit,
    color: Color,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, style = MaterialTheme.typography.labelLarge, color = color, fontWeight = FontWeight.Bold)
        OutlinedTextField(
            value         = value,
            onValueChange = { new ->
                if (new.all { it.isDigit() || it == '.' || it == ',' })
                    onChange(new.replace(',', '.'))
            },
            placeholder   = { Text(hint, color = Color.White.copy(alpha = 0.3f)) },
            modifier      = Modifier.fillMaxWidth(),
            singleLine    = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            colors        = OutlinedTextFieldDefaults.colors(
                focusedBorderColor   = color,
                unfocusedBorderColor = color.copy(alpha = 0.4f),
                focusedTextColor     = Color.White,
                unfocusedTextColor   = Color.White,
                cursorColor          = color,
            ),
            shape = RoundedCornerShape(14.dp),
        )
    }
}