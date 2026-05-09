package com.gymbro.app.ui.profile

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle

private val builtInAvatars = listOf(
    "💪", "🏋️", "⚡", "🔥", "🦁", "🐺", "🦅", "🐉",
    "👊", "🌟", "💎", "🏆", "⚔️", "🛡️", "🎯", "🚀",
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onOpenSettings: () -> Unit = {},
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var editMode by remember { mutableStateOf(false) }
    var showAvatarPicker by remember { mutableStateOf(false) }

    // Edit state
    var nameEdit by remember(state.name) { mutableStateOf(state.name ?: "") }
    var weightEdit by remember(state.weightKg) { mutableStateOf(state.weightKg?.toString() ?: "") }
    var heightEdit by remember(state.heightCm) { mutableStateOf(state.heightCm?.toString() ?: "") }
    var ageEdit by remember(state.age) { mutableStateOf(state.age?.toString() ?: "") }
    var avatarEdit by remember(state.avatarIndex) { mutableStateOf(state.avatarIndex) }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                Text(
                    "Профиль",
                    fontWeight = FontWeight.ExtraBold,
                    style = MaterialTheme.typography.headlineSmall,
                )
            },
            actions = {
                // Settings icon inside Profile tab
                IconButton(onClick = onOpenSettings) {
                    Icon(Icons.Default.Settings, "Настройки", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (editMode) {
                    IconButton(onClick = {
                        viewModel.saveProfile(
                            name = nameEdit,
                            avatarIndex = avatarEdit,
                            weightKg = weightEdit.toDoubleOrNull(),
                            heightCm = heightEdit.toIntOrNull(),
                            age = ageEdit.toIntOrNull(),
                        )
                        editMode = false
                    }) {
                        Icon(Icons.Default.Check, "Сохранить", tint = MaterialTheme.colorScheme.primary)
                    }
                } else {
                    IconButton(onClick = { editMode = true }) {
                        Icon(Icons.Default.Edit, "Редактировать", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            // Avatar + Name Hero
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                MaterialTheme.colorScheme.surfaceVariant,
                            )
                        )
                    )
                    .padding(24.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    // Avatar circle
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                            .border(3.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), CircleShape)
                            .then(if (editMode) Modifier.clickable { showAvatarPicker = !showAvatarPicker } else Modifier),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(builtInAvatars.getOrElse(avatarEdit) { "💪" }, fontSize = 50.sp)
                    }

                    if (editMode) {
                        TextButton(onClick = { showAvatarPicker = !showAvatarPicker }) {
                            Text(if (showAvatarPicker) "Скрыть" else "Сменить аватар")
                        }
                    }

                    if (editMode) {
                        OutlinedTextField(
                            value = nameEdit,
                            onValueChange = { nameEdit = it },
                            label = { Text("Имя") },
                            singleLine = true,
                            textStyle = LocalTextStyle.current.copy(
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                        )
                    } else {
                        Text(
                            state.name ?: "Пользователь",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            }

            // Avatar picker
            if (showAvatarPicker && editMode) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                ) {
                    val rows = builtInAvatars.chunked(4)
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        rows.forEachIndexed { rowIdx, rowAvatars ->
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                rowAvatars.forEachIndexed { colIdx, avatar ->
                                    val idx = rowIdx * 4 + colIdx
                                    Box(
                                        modifier = Modifier
                                            .size(56.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (avatarEdit == idx) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                                else Color.Transparent
                                            )
                                            .border(if (avatarEdit == idx) 2.dp else 0.dp, MaterialTheme.colorScheme.primary, CircleShape)
                                            .clickable { avatarEdit = idx; showAvatarPicker = false },
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

            // Body stats card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Физические данные", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                    if (editMode) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedTextField(
                                value = weightEdit,
                                onValueChange = { weightEdit = it },
                                label = { Text("Вес (кг)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                            )
                            OutlinedTextField(
                                value = heightEdit,
                                onValueChange = { heightEdit = it },
                                label = { Text("Рост (см)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                            )
                        }
                        OutlinedTextField(
                            value = ageEdit,
                            onValueChange = { ageEdit = it },
                            label = { Text("Возраст") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                        )
                    } else {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            PhysStatChip("⚖️", "Вес", state.weightKg?.let { "${it.toInt()} кг" } ?: "—", modifier = Modifier.weight(1f))
                            PhysStatChip("📏", "Рост", state.heightCm?.let { "$it см" } ?: "—", modifier = Modifier.weight(1f))
                            PhysStatChip("🎂", "Возраст", state.age?.let { "$it лет" } ?: "—", modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            // Weight progress chart placeholder
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("График изменения веса", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("📊", fontSize = 28.sp)
                            Text(
                                "График будет доступен после\nначала тренировок",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun PhysStatChip(emoji: String, label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.background.copy(alpha = 0.5f))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(emoji, fontSize = 20.sp)
        Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}