package com.gymbro.app.ui.onboarding

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel

// Встроенные аватары (emoji)
private val builtInAvatars = listOf(
    "💪", "🏋️", "⚡", "🔥", "🦁", "🐺", "🦅", "🐉",
    "👊", "🌟", "💎", "🏆", "⚔️", "🛡️", "🎯", "🚀",
)

@Composable
fun OnboardingProfileScreen(
    onDone: () -> Unit,
    viewModel: OnboardingProfileViewModel = hiltViewModel(),
) {
    var name by remember { mutableStateOf("") }
    var selectedAvatar by remember { mutableStateOf(0) }
    var weightInput by remember { mutableStateOf("") }
    var heightInput by remember { mutableStateOf("") }
    var ageInput by remember { mutableStateOf("") }
    var showAvatarPicker by remember { mutableStateOf(false) }

    val canContinue = name.isNotBlank()

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
                .padding(top = 60.dp, bottom = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            // Title
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("👤", fontSize = 56.sp)
                Text(
                    "Расскажи о себе",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                )
                Text(
                    "Заполни профиль, чтобы получить персонализированный опыт",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.55f),
                    textAlign = TextAlign.Center,
                )
            }

            // Avatar picker
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "Выбери аватар",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White.copy(alpha = 0.6f),
                    fontWeight = FontWeight.Bold,
                )

                // Current avatar
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(Color(0xFF2979FF).copy(alpha = 0.3f), Color(0xFF0A0E1A))
                            )
                        )
                        .border(2.dp, Color(0xFF2979FF).copy(alpha = 0.6f), CircleShape)
                        .clickable { showAvatarPicker = !showAvatarPicker },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(builtInAvatars[selectedAvatar], fontSize = 44.sp)
                }

                TextButton(onClick = { showAvatarPicker = !showAvatarPicker }) {
                    Text(
                        if (showAvatarPicker) "Скрыть" else "Сменить аватар",
                        color = Color(0xFF2979FF),
                    )
                }

                if (showAvatarPicker) {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF131929)),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        val rows = builtInAvatars.chunked(4)
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            rows.forEachIndexed { rowIdx, rowAvatars ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly,
                                ) {
                                    rowAvatars.forEachIndexed { colIdx, avatar ->
                                        val idx = rowIdx * 4 + colIdx
                                        Box(
                                            modifier = Modifier
                                                .size(56.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    if (selectedAvatar == idx)
                                                        Color(0xFF2979FF).copy(alpha = 0.3f)
                                                    else Color.White.copy(alpha = 0.04f)
                                                )
                                                .border(
                                                    if (selectedAvatar == idx) 2.dp else 0.dp,
                                                    Color(0xFF2979FF),
                                                    CircleShape,
                                                )
                                                .clickable {
                                                    selectedAvatar = idx
                                                    showAvatarPicker = false
                                                },
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            Text(avatar, fontSize = 26.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Name field
            ProfileField(
                label = "Имя *",
                value = name,
                onChange = { name = it },
                placeholder = "Как тебя зовут?",
                accentColor = Color(0xFF2979FF),
            )

            // Physical data
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                ProfileField(
                    label = "Вес (кг)",
                    value = weightInput,
                    onChange = { weightInput = it },
                    placeholder = "75",
                    accentColor = Color(0xFF4CAF50),
                    keyboardType = KeyboardType.Decimal,
                    modifier = Modifier.weight(1f),
                )
                ProfileField(
                    label = "Рост (см)",
                    value = heightInput,
                    onChange = { heightInput = it },
                    placeholder = "180",
                    accentColor = Color(0xFF4CAF50),
                    keyboardType = KeyboardType.Number,
                    modifier = Modifier.weight(1f),
                )
            }

            ProfileField(
                label = "Возраст",
                value = ageInput,
                onChange = { ageInput = it },
                placeholder = "25",
                accentColor = Color(0xFFFF6D00),
                keyboardType = KeyboardType.Number,
            )

            Spacer(Modifier.height(8.dp))

            // Continue button
            Button(
                onClick = {
                    viewModel.saveProfile(
                        name = name,
                        avatarIndex = selectedAvatar,
                        weightKg = weightInput.toDoubleOrNull(),
                        heightCm = heightInput.toIntOrNull(),
                        age = ageInput.toIntOrNull(),
                    )
                    onDone()
                },
                enabled = canContinue,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2979FF),
                    contentColor = Color.White,
                    disabledContainerColor = Color(0xFF2979FF).copy(alpha = 0.4f),
                ),
            ) {
                Icon(Icons.Default.Check, null, modifier = Modifier.size(22.dp))
                Spacer(Modifier.width(10.dp))
                Text(
                    "Продолжить",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
private fun ProfileField(
    label: String,
    value: String,
    onChange: (String) -> Unit,
    placeholder: String,
    accentColor: Color,
    keyboardType: KeyboardType = KeyboardType.Text,
    modifier: Modifier = Modifier.fillMaxWidth(),
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = accentColor,
        )
        OutlinedTextField(
            value = value,
            onValueChange = onChange,
            placeholder = { Text(placeholder, color = Color.White.copy(alpha = 0.25f)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = accentColor,
                unfocusedBorderColor = accentColor.copy(alpha = 0.35f),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                cursorColor = accentColor,
                focusedContainerColor = Color.White.copy(alpha = 0.04f),
                unfocusedContainerColor = Color.White.copy(alpha = 0.03f),
            ),
        )
    }
}