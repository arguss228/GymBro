package com.obsession.app.ui.bodytab

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.obsession.app.data.local.entity.ExerciseEntity
import com.obsession.app.domain.model.ExerciseRank
import com.obsession.app.domain.model.ExerciseRankThresholds
import com.obsession.app.domain.model.MuscleGroupId
import com.obsession.app.domain.model.MuscleGroupRank
import com.obsession.app.domain.model.StrengthRank
import com.obsession.app.domain.model.StrengthRanks
import com.obsession.app.domain.model.UserBodyRank
import com.obsession.app.ui.bodyrank.BodyAnalysisViewModel
import com.obsession.app.ui.bodyrank.BodyAnalysisUiState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.text.input.KeyboardType

// ─── Tab entry point ─────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BodyAnalysisTabScreen(
    viewModel: BodyAnalysisViewModel = hiltViewModel(),
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    val tabs = listOf("Анализ тела", "Ранги упражнений")
    val state by viewModel.state.collectAsStateWithLifecycle()

    // Add dialog
    if (state.showAddDialog) {
        AddExerciseRankDialog(
            exercises      = state.allExercises,
            selectedEx     = state.dialogExercise,
            weightInput    = state.dialogWeightInput,
            onSelectEx     = viewModel::setDialogExercise,
            onWeightChange = viewModel::setDialogWeight,
            onSave         = viewModel::saveDialogData,
            onDismiss      = viewModel::closeDialog,
        )
    }

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { inner ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner),
        ) {
            TopAppBar(
                title = {
                    Text(
                        "Анализ тела",
                        fontWeight = FontWeight.ExtraBold,
                        style      = MaterialTheme.typography.headlineSmall,
                    )
                },
                actions = {
                    androidx.compose.material3.IconButton(onClick = viewModel::openAddDialog) {
                        Icon(
                            Icons.Default.Add,
                            "Добавить данные",
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )

            PrimaryTabRow(
                selectedTabIndex = selectedTab,
                containerColor   = MaterialTheme.colorScheme.background,
                contentColor     = MaterialTheme.colorScheme.primary,
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick  = { selectedTab = index },
                        text = {
                            Text(
                                title,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                style      = MaterialTheme.typography.titleSmall,
                            )
                        },
                    )
                }
            }

            when (selectedTab) {
                0 -> BodyAnalysisTab(state = state, onAddData = viewModel::openAddDialog)
                1 -> ExerciseRanksMasonryTab(state = state)
            }
        }
    }
}

// ─── Tab 1: Body Analysis ─────────────────────────────────────────

@Composable
private fun BodyAnalysisTab(
    state: BodyAnalysisUiState,
    onAddData: (ExerciseEntity?) -> Unit,
) {
    if (state.isLoading) {
        Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
        return
    }

    val bodyRank = state.bodyRank ?: return

    LazyColumn(
        modifier        = Modifier.fillMaxSize(),
        contentPadding  = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // 1. Overall rank card
        item { OverallRankCard(bodyRank = bodyRank, onAddData = { onAddData(null) }) }

        // 2. BodyGraf
        item { BodyGrafSection(bodyRank = bodyRank) }

        // 3. Section label
        item {
            Text(
                "Мышечные группы",
                style      = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color      = MaterialTheme.colorScheme.onBackground,
            )
        }

        // 4. Expandable muscle groups
        val bodyPartOrder = listOf("Руки", "Ноги", "Кор", "Плечи", "Грудь", "Спина")
        val grouped = bodyRank.muscleGroups.groupBy { it.bodyPartName }
        items(bodyPartOrder.filter { grouped.containsKey(it) }) { bodyPart ->
            ExpandableBodyPartCard(
                bodyPartName = bodyPart,
                muscleGroups = grouped[bodyPart] ?: emptyList(),
            )
        }

        item { Spacer(Modifier.height(24.dp)) }
    }
}

// ─── Overall rank card ────────────────────────────────────────────

@Composable
private fun OverallRankCard(bodyRank: UserBodyRank, onAddData: () -> Unit) {
    val rank = bodyRank.overallRank

    val infiniteTransition = rememberInfiniteTransition(label = "body_glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue  = 0.25f,
        targetValue   = 0.65f,
        animationSpec = infiniteRepeatable(tween(2000, easing = EaseInOutSine), RepeatMode.Reverse),
        label         = "body_glow_alpha",
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.linearGradient(listOf(Color(0xFF0D1B2A), rank.primaryColor.copy(alpha = 0.15f), Color(0xFF070B14)))
            )
            .border(
                1.5.dp,
                Brush.linearGradient(listOf(rank.primaryColor.copy(alpha = 0.6f), rank.secondaryColor.copy(alpha = 0.3f))),
                RoundedCornerShape(24.dp),
            )
            .padding(20.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Text(rank.symbol, fontSize = 52.sp)
                Column(Modifier.weight(1f)) {
                    Text(
                        "Общий ранг тела",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White.copy(alpha = 0.5f),
                    )
                    Text(
                        rank.name,
                        style      = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black,
                        color      = rank.primaryColor,
                    )
                }
            }

            bodyRank.nextRank?.let { next ->
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("До ${next.symbol} ${next.name}", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.5f))
                        Text("${(bodyRank.progressToNext * 100).toInt()}%", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = rank.primaryColor)
                    }
                    Box(
                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)).background(Color.White.copy(alpha = 0.08f)),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(bodyRank.progressToNext.coerceIn(0f, 1f))
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Brush.horizontalGradient(listOf(rank.primaryColor, rank.secondaryColor))),
                        )
                    }
                }
            }

            Button(
                onClick  = onAddData,
                modifier = Modifier.fillMaxWidth(),
                shape    = RoundedCornerShape(12.dp),
                colors   = ButtonDefaults.buttonColors(
                    containerColor = rank.primaryColor.copy(alpha = 0.18f),
                    contentColor   = rank.primaryColor,
                ),
                elevation = ButtonDefaults.buttonElevation(0.dp),
            ) {
                Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Добавить / обновить данные", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

// ─── BodyGraf ─────────────────────────────────────────────────────

@Composable
private fun BodyGrafSection(bodyRank: UserBodyRank) {
    var showFront by remember { mutableStateOf(true) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(24.dp),
        colors   = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(
            modifier            = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                "BodyGraf",
                style      = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color      = MaterialTheme.colorScheme.onSurface,
            )

            // Front / Back toggle
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.background.copy(alpha = 0.5f))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                listOf("Спереди" to true, "Сзади" to false).forEach { (label, isFront) ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (showFront == isFront) MaterialTheme.colorScheme.primary
                                else Color.Transparent
                            )
                            .clickable { showFront = isFront }
                            .padding(horizontal = 20.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            label,
                            style      = MaterialTheme.typography.labelLarge,
                            fontWeight = if (showFront == isFront) FontWeight.Bold else FontWeight.Normal,
                            color      = if (showFront == isFront) MaterialTheme.colorScheme.onPrimary
                                         else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            // Mannequin
            Box(
                modifier            = Modifier.fillMaxWidth().height(320.dp),
                contentAlignment    = Alignment.Center,
            ) {
                if (showFront) {
                    FrontMannequin(bodyRank = bodyRank)
                } else {
                    BackMannequin(bodyRank = bodyRank)
                }
            }

            // Legend
            MuscleRankLegend()
        }
    }
}

// ─── Colour helpers ───────────────────────────────────────────────

private fun rankColor(rankIndex: Int): Color = when {
    rankIndex < 0  -> Color(0xFF2A2A2A)          // no data – dark grey
    rankIndex == 0 -> Color(0xFF2E7D32)           // Дерево – green
    rankIndex == 1 -> Color(0xFF8D6E63)           // Бронза
    rankIndex == 2 -> Color(0xFF78909C)           // Серебро
    rankIndex == 3 -> Color(0xFFF9A825)           // Золото
    rankIndex == 4 -> Color(0xFF00ACC1)           // Платина
    rankIndex == 5 -> Color(0xFF00E5FF)           // Алмаз
    rankIndex == 6 -> Color(0xFF1565C0)           // Чемпион
    rankIndex == 7 -> Color(0xFF6A1B9A)           // Герой
    rankIndex == 8 -> Color(0xFFC62828)           // Спартанец
    rankIndex == 9 -> Color(0xFFE65100)           // Титан
    rankIndex == 10-> Color(0xFFFFAB00)           // Олимпиец
    else           -> Color(0xFFFFD700)           // Божество
}

// Average rank index for a set of muscle group IDs
private fun avgRankFor(bodyRank: UserBodyRank, vararg groupIds: MuscleGroupId): Int {
    val ranks = bodyRank.muscleGroups
        .filter { it.groupId in groupIds }
        .map { it.averageRankIndex.toInt() }
        .filter { it >= 0 }
    return if (ranks.isEmpty()) -1 else ranks.average().toInt()
}

// ─── Front Mannequin ─────────────────────────────────────────────

@Composable
private fun FrontMannequin(bodyRank: UserBodyRank) {
    val chestColor    = rankColor(avgRankFor(bodyRank, MuscleGroupId.UPPER_CHEST, MuscleGroupId.LOWER_CHEST))
    val shoulderColor = rankColor(avgRankFor(bodyRank, MuscleGroupId.FRONT_DELT, MuscleGroupId.MID_DELT))
    val armColor      = rankColor(avgRankFor(bodyRank, MuscleGroupId.BICEPS, MuscleGroupId.TRICEPS, MuscleGroupId.FOREARMS))
    val absColor      = rankColor(avgRankFor(bodyRank, MuscleGroupId.ABS, MuscleGroupId.OBLIQUES))
    val legColor      = rankColor(avgRankFor(bodyRank, MuscleGroupId.QUADS, MuscleGroupId.CALVES))
    val glueColor     = rankColor(avgRankFor(bodyRank, MuscleGroupId.GLUTES))

    Box(
        modifier = Modifier
            .size(160.dp, 310.dp)
            .drawBehind {
                drawFrontBody(
                    chestColor    = chestColor,
                    shoulderColor = shoulderColor,
                    armColor      = armColor,
                    absColor      = absColor,
                    legColor      = legColor,
                    glueColor     = glueColor,
                )
            },
    )
}

private fun DrawScope.drawFrontBody(
    chestColor: Color,
    shoulderColor: Color,
    armColor: Color,
    absColor: Color,
    legColor: Color,
    glueColor: Color,
) {
    val w = size.width
    val h = size.height
    val cx = w / 2f

    val bodyStroke = Color(0xFF444444)
    val strokeW    = 1.2.dp.toPx()

    // ── Head ──
    drawOval(
        color   = Color(0xFF9E9E9E),
        topLeft = Offset(cx - 20.dp.toPx(), 0f),
        size    = Size(40.dp.toPx(), 44.dp.toPx()),
    )
    drawOval(
        color     = bodyStroke,
        topLeft   = Offset(cx - 20.dp.toPx(), 0f),
        size      = Size(40.dp.toPx(), 44.dp.toPx()),
        style     = Stroke(strokeW),
    )

    // ── Neck ──
    drawRect(
        color   = Color(0xFF9E9E9E),
        topLeft = Offset(cx - 8.dp.toPx(), 44.dp.toPx()),
        size    = Size(16.dp.toPx(), 12.dp.toPx()),
    )

    // ── Left shoulder ──
    drawOval(
        color   = shoulderColor.copy(alpha = 0.85f),
        topLeft = Offset(cx - 52.dp.toPx(), 52.dp.toPx()),
        size    = Size(26.dp.toPx(), 22.dp.toPx()),
    )
    // ── Right shoulder ──
    drawOval(
        color   = shoulderColor.copy(alpha = 0.85f),
        topLeft = Offset(cx + 26.dp.toPx(), 52.dp.toPx()),
        size    = Size(26.dp.toPx(), 22.dp.toPx()),
    )

    // ── Chest / torso ──
    val torsoPath = Path().apply {
        moveTo(cx - 36.dp.toPx(), 55.dp.toPx())
        lineTo(cx + 36.dp.toPx(), 55.dp.toPx())
        lineTo(cx + 28.dp.toPx(), 140.dp.toPx())
        lineTo(cx - 28.dp.toPx(), 140.dp.toPx())
        close()
    }
    drawPath(torsoPath, chestColor.copy(alpha = 0.85f))
    drawPath(torsoPath, bodyStroke, style = Stroke(strokeW))

    // Pec divider line
    drawLine(
        color       = bodyStroke.copy(alpha = 0.5f),
        start       = Offset(cx, 55.dp.toPx()),
        end         = Offset(cx, 100.dp.toPx()),
        strokeWidth = strokeW * 0.6f,
    )

    // ── Abs ──
    val absPath = Path().apply {
        moveTo(cx - 28.dp.toPx(), 100.dp.toPx())
        lineTo(cx + 28.dp.toPx(), 100.dp.toPx())
        lineTo(cx + 20.dp.toPx(), 155.dp.toPx())
        lineTo(cx - 20.dp.toPx(), 155.dp.toPx())
        close()
    }
    drawPath(absPath, absColor.copy(alpha = 0.85f))
    drawPath(absPath, bodyStroke, style = Stroke(strokeW))
    // Abs grid lines
    for (i in 1..3) {
        val y = 100.dp.toPx() + i * 14.dp.toPx()
        drawLine(bodyStroke.copy(alpha = 0.3f), Offset(cx - 22.dp.toPx(), y), Offset(cx + 22.dp.toPx(), y), strokeW * 0.5f)
    }
    drawLine(bodyStroke.copy(alpha = 0.3f), Offset(cx, 100.dp.toPx()), Offset(cx, 155.dp.toPx()), strokeW * 0.5f)

    // ── Left arm ──
    drawRoundRect(
        color        = armColor.copy(alpha = 0.85f),
        topLeft      = Offset(cx - 60.dp.toPx(), 74.dp.toPx()),
        size         = Size(22.dp.toPx(), 80.dp.toPx()),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx()),
    )
    // forearm left
    drawRoundRect(
        color        = armColor.copy(alpha = 0.7f),
        topLeft      = Offset(cx - 58.dp.toPx(), 158.dp.toPx()),
        size         = Size(18.dp.toPx(), 60.dp.toPx()),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx()),
    )

    // ── Right arm ──
    drawRoundRect(
        color        = armColor.copy(alpha = 0.85f),
        topLeft      = Offset(cx + 38.dp.toPx(), 74.dp.toPx()),
        size         = Size(22.dp.toPx(), 80.dp.toPx()),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx()),
    )
    drawRoundRect(
        color        = armColor.copy(alpha = 0.7f),
        topLeft      = Offset(cx + 40.dp.toPx(), 158.dp.toPx()),
        size         = Size(18.dp.toPx(), 60.dp.toPx()),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx()),
    )

    // ── Hips ──
    drawRoundRect(
        color        = glueColor.copy(alpha = 0.85f),
        topLeft      = Offset(cx - 24.dp.toPx(), 155.dp.toPx()),
        size         = Size(48.dp.toPx(), 24.dp.toPx()),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx()),
    )

    // ── Left leg ──
    drawRoundRect(
        color        = legColor.copy(alpha = 0.85f),
        topLeft      = Offset(cx - 24.dp.toPx(), 180.dp.toPx()),
        size         = Size(20.dp.toPx(), 70.dp.toPx()),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx()),
    )
    // shin left
    drawRoundRect(
        color        = legColor.copy(alpha = 0.7f),
        topLeft      = Offset(cx - 22.dp.toPx(), 254.dp.toPx()),
        size         = Size(16.dp.toPx(), 50.dp.toPx()),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(5.dp.toPx()),
    )

    // ── Right leg ──
    drawRoundRect(
        color        = legColor.copy(alpha = 0.85f),
        topLeft      = Offset(cx + 4.dp.toPx(), 180.dp.toPx()),
        size         = Size(20.dp.toPx(), 70.dp.toPx()),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx()),
    )
    drawRoundRect(
        color        = legColor.copy(alpha = 0.7f),
        topLeft      = Offset(cx + 6.dp.toPx(), 254.dp.toPx()),
        size         = Size(16.dp.toPx(), 50.dp.toPx()),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(5.dp.toPx()),
    )
}

// ─── Back Mannequin ───────────────────────────────────────────────

@Composable
private fun BackMannequin(bodyRank: UserBodyRank) {
    val latsColor  = rankColor(avgRankFor(bodyRank, MuscleGroupId.LATS, MuscleGroupId.UPPER_BACK, MuscleGroupId.TRAPS))
    val lowerBackC = rankColor(avgRankFor(bodyRank, MuscleGroupId.LOWER_BACK))
    val shoulderC  = rankColor(avgRankFor(bodyRank, MuscleGroupId.REAR_DELT, MuscleGroupId.MID_DELT))
    val armColor   = rankColor(avgRankFor(bodyRank, MuscleGroupId.BICEPS, MuscleGroupId.TRICEPS))
    val hamColor   = rankColor(avgRankFor(bodyRank, MuscleGroupId.HAMSTRINGS, MuscleGroupId.GLUTES))
    val calfColor  = rankColor(avgRankFor(bodyRank, MuscleGroupId.CALVES))

    Box(
        modifier = Modifier
            .size(160.dp, 310.dp)
            .drawBehind {
                drawBackBody(
                    latsColor    = latsColor,
                    lowerBackC   = lowerBackC,
                    shoulderC    = shoulderC,
                    armColor     = armColor,
                    hamColor     = hamColor,
                    calfColor    = calfColor,
                )
            },
    )
}

private fun DrawScope.drawBackBody(
    latsColor: Color,
    lowerBackC: Color,
    shoulderC: Color,
    armColor: Color,
    hamColor: Color,
    calfColor: Color,
) {
    val w = size.width
    val h = size.height
    val cx = w / 2f
    val bodyStroke = Color(0xFF444444)
    val strokeW    = 1.2.dp.toPx()

    // Head (back)
    drawOval(color = Color(0xFF9E9E9E), topLeft = Offset(cx - 20.dp.toPx(), 0f), size = Size(40.dp.toPx(), 44.dp.toPx()))
    drawOval(color = bodyStroke, topLeft = Offset(cx - 20.dp.toPx(), 0f), size = Size(40.dp.toPx(), 44.dp.toPx()), style = Stroke(strokeW))
    // Neck
    drawRect(color = Color(0xFF9E9E9E), topLeft = Offset(cx - 8.dp.toPx(), 44.dp.toPx()), size = Size(16.dp.toPx(), 12.dp.toPx()))

    // Traps / upper back
    val trapPath = Path().apply {
        moveTo(cx - 14.dp.toPx(), 44.dp.toPx())
        lineTo(cx - 38.dp.toPx(), 60.dp.toPx())
        lineTo(cx - 32.dp.toPx(), 90.dp.toPx())
        lineTo(cx, 80.dp.toPx())
        lineTo(cx + 32.dp.toPx(), 90.dp.toPx())
        lineTo(cx + 38.dp.toPx(), 60.dp.toPx())
        lineTo(cx + 14.dp.toPx(), 44.dp.toPx())
        close()
    }
    drawPath(trapPath, latsColor.copy(alpha = 0.9f))
    drawPath(trapPath, bodyStroke, style = Stroke(strokeW))

    // Rear delts
    drawOval(color = shoulderC.copy(alpha = 0.85f), topLeft = Offset(cx - 52.dp.toPx(), 52.dp.toPx()), size = Size(24.dp.toPx(), 20.dp.toPx()))
    drawOval(color = shoulderC.copy(alpha = 0.85f), topLeft = Offset(cx + 28.dp.toPx(), 52.dp.toPx()), size = Size(24.dp.toPx(), 20.dp.toPx()))

    // Lats / mid back
    val latPath = Path().apply {
        moveTo(cx - 36.dp.toPx(), 72.dp.toPx())
        lineTo(cx + 36.dp.toPx(), 72.dp.toPx())
        lineTo(cx + 26.dp.toPx(), 145.dp.toPx())
        lineTo(cx - 26.dp.toPx(), 145.dp.toPx())
        close()
    }
    drawPath(latPath, latsColor.copy(alpha = 0.85f))
    drawPath(latPath, bodyStroke, style = Stroke(strokeW))
    // spine line
    drawLine(bodyStroke.copy(alpha = 0.4f), Offset(cx, 80.dp.toPx()), Offset(cx, 145.dp.toPx()), strokeW * 0.5f)

    // Lower back
    drawRoundRect(
        color        = lowerBackC.copy(alpha = 0.85f),
        topLeft      = Offset(cx - 22.dp.toPx(), 145.dp.toPx()),
        size         = Size(44.dp.toPx(), 30.dp.toPx()),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx()),
    )

    // Arms (back view)
    drawRoundRect(color = armColor.copy(alpha = 0.85f), topLeft = Offset(cx - 60.dp.toPx(), 74.dp.toPx()), size = Size(22.dp.toPx(), 80.dp.toPx()), cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx()))
    drawRoundRect(color = armColor.copy(alpha = 0.7f),  topLeft = Offset(cx - 58.dp.toPx(), 158.dp.toPx()), size = Size(18.dp.toPx(), 60.dp.toPx()), cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx()))
    drawRoundRect(color = armColor.copy(alpha = 0.85f), topLeft = Offset(cx + 38.dp.toPx(), 74.dp.toPx()), size = Size(22.dp.toPx(), 80.dp.toPx()), cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx()))
    drawRoundRect(color = armColor.copy(alpha = 0.7f),  topLeft = Offset(cx + 40.dp.toPx(), 158.dp.toPx()), size = Size(18.dp.toPx(), 60.dp.toPx()), cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx()))

    // Glutes
    drawRoundRect(color = hamColor.copy(alpha = 0.85f), topLeft = Offset(cx - 22.dp.toPx(), 175.dp.toPx()), size = Size(44.dp.toPx(), 26.dp.toPx()), cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx()))

    // Hamstrings
    drawRoundRect(color = hamColor.copy(alpha = 0.8f), topLeft = Offset(cx - 22.dp.toPx(), 202.dp.toPx()), size = Size(18.dp.toPx(), 58.dp.toPx()), cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx()))
    drawRoundRect(color = hamColor.copy(alpha = 0.8f), topLeft = Offset(cx + 4.dp.toPx(),  202.dp.toPx()), size = Size(18.dp.toPx(), 58.dp.toPx()), cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx()))

    // Calves
    drawRoundRect(color = calfColor.copy(alpha = 0.8f), topLeft = Offset(cx - 20.dp.toPx(), 262.dp.toPx()), size = Size(14.dp.toPx(), 44.dp.toPx()), cornerRadius = androidx.compose.ui.geometry.CornerRadius(5.dp.toPx()))
    drawRoundRect(color = calfColor.copy(alpha = 0.8f), topLeft = Offset(cx + 6.dp.toPx(),  262.dp.toPx()), size = Size(14.dp.toPx(), 44.dp.toPx()), cornerRadius = androidx.compose.ui.geometry.CornerRadius(5.dp.toPx()))
}

// ─── Legend ───────────────────────────────────────────────────────

@Composable
private fun MuscleRankLegend() {
    val items = listOf(
        "Нет данных" to Color(0xFF2A2A2A),
        "Дерево"     to Color(0xFF2E7D32),
        "Бронза"     to Color(0xFF8D6E63),
        "Серебро"    to Color(0xFF78909C),
        "Золото"     to Color(0xFFF9A825),
        "Алмаз"      to Color(0xFF00E5FF),
        "Чемпион+"   to Color(0xFF6A1B9A),
    )

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding        = PaddingValues(horizontal = 4.dp),
    ) {
        items(items) { (label, color) ->
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(color),
                )
                Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
            }
        }
    }
}

// ─── Expandable muscle group card ────────────────────────────────

private val bodyPartIcons = mapOf(
    "Руки"  to "💪",
    "Ноги"  to "🦵",
    "Кор"   to "🎯",
    "Плечи" to "🏋️",
    "Грудь" to "❤️",
    "Спина" to "🔰",
)

@Composable
private fun ExpandableBodyPartCard(
    bodyPartName: String,
    muscleGroups: List<MuscleGroupRank>,
) {
    var expanded by rememberSaveable { mutableStateOf(false) }

    // Compute group-level rank
    val groupsWithData = muscleGroups.filter { it.exerciseRanks.isNotEmpty() }
    val avgIndex       = if (groupsWithData.isEmpty()) -1.0
                         else groupsWithData.map { it.averageRankIndex }.average()
    val groupRank      = if (avgIndex >= 0) StrengthRanks.all[avgIndex.toInt().coerceIn(0, StrengthRanks.all.lastIndex)] else null

    val emoji = bodyPartIcons[bodyPartName] ?: "•"
    val accentColor = groupRank?.primaryColor ?: MaterialTheme.colorScheme.outline

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium)),
        shape  = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header row (clickable)
            Row(
                modifier              = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { expanded = !expanded }
                    .padding(4.dp),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(emoji, fontSize = 28.sp)
                Text(
                    bodyPartName,
                    style      = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    modifier   = Modifier.weight(1f),
                )
                groupRank?.let {
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(it.symbol, fontSize = 18.sp)
                        Text(
                            it.name,
                            style      = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color      = it.primaryColor,
                        )
                    }
                } ?: Text(
                    "Нет данных",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Icon(
                    if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    null,
                    tint     = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(22.dp),
                )
            }

            // Expanded muscle list
            AnimatedVisibility(
                visible = expanded,
                enter   = expandVertically(tween(280)) + fadeIn(tween(200)),
                exit    = shrinkVertically(tween(220)) + fadeOut(tween(150)),
            ) {
                Column(
                    modifier            = Modifier.padding(top = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(0.4f))
                    muscleGroups.forEach { group ->
                        MuscleGroupRow(group = group)
                    }
                }
            }
        }
    }
}

@Composable
private fun MuscleGroupRow(group: MuscleGroupRank) {
    var expanded by remember { mutableStateOf(false) }
    val rank = group.rank

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
    ) {
        Row(
            modifier              = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .clickable(enabled = group.exerciseRanks.isNotEmpty()) { expanded = !expanded }
                .padding(8.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            // Rank circle
            Box(
                modifier         = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(rank?.primaryColor?.copy(alpha = 0.15f) ?: MaterialTheme.colorScheme.outline.copy(0.1f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(rank?.symbol ?: "?", fontSize = 18.sp, textAlign = TextAlign.Center)
            }

            Column(Modifier.weight(1f)) {
                Text(
                    group.displayName,
                    style      = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    if (rank != null) "${rank.name} · ${group.exerciseRanks.size} упр."
                    else "Нет данных",
                    style = MaterialTheme.typography.bodySmall,
                    color = rank?.primaryColor ?: MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            if (group.exerciseRanks.isNotEmpty()) {
                Icon(
                    if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    null,
                    tint     = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp),
                )
            }
        }

        // Individual exercises
        if (expanded && group.exerciseRanks.isNotEmpty()) {
            Column(
                modifier            = Modifier.padding(start = 52.dp, end = 8.dp, bottom = 4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                group.exerciseRanks.forEach { exRank ->
                    val exRankObj = exRank.rank
                    Row(
                        Modifier.fillMaxWidth(),
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            exRank.exerciseName,
                            style    = MaterialTheme.typography.bodySmall,
                            color    = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f),
                        )
                        Row(
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Text("${exRank.best1Rm.toInt()} кг", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(exRankObj.symbol, fontSize = 14.sp)
                            Text(exRankObj.name, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = exRankObj.primaryColor)
                        }
                    }
                }
            }
        }
    }
}

// ─── Tab 2: Exercise Ranks Masonry ───────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ExerciseRanksMasonryTab(state: BodyAnalysisUiState) {
    val allExerciseRanks = state.bodyRank?.muscleGroups
        ?.flatMap { it.exerciseRanks }
        ?.sortedByDescending { it.rankIndex }
        ?: emptyList()

    if (allExerciseRanks.isEmpty()) {
        Box(Modifier.fillMaxSize(), Alignment.Center) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text("🏆", fontSize = 48.sp)
                Text(
                    "Нет данных.\nДобавьте максимумы в разделе «Анализ тела».",
                    style     = MaterialTheme.typography.bodyLarge,
                    color     = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        }
        return
    }

    LazyColumn(
        modifier        = Modifier.fillMaxSize(),
        contentPadding  = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // Staggered / masonry — two columns, alternating heights
        val chunked = allExerciseRanks.chunked(2)
        items(chunked) { pair ->
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment     = Alignment.Top,
            ) {
                pair.forEachIndexed { colIdx, exRank ->
                    // Alternate heights: col0 = tall, col1 = short (or vice versa for pairs)
                    val isTall = (chunked.indexOf(pair) + colIdx) % 2 == 0
                    ExerciseRankMasonryCard(
                        exRank  = exRank,
                        isTall  = isTall,
                        modifier = Modifier.weight(1f),
                    )
                }
                // If odd number, fill the remaining slot with a placeholder spacer
                if (pair.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }

        item { Spacer(Modifier.height(24.dp)) }
    }
}

@Composable
private fun ExerciseRankMasonryCard(
    exRank: ExerciseRank,
    isTall: Boolean,
    modifier: Modifier = Modifier,
) {
    val rank = exRank.rank

    val infiniteTransition = rememberInfiniteTransition(label = "masonry_glow_${exRank.exerciseId}")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue  = 0.1f,
        targetValue   = 0.28f,
        animationSpec = infiniteRepeatable(tween(2400, easing = EaseInOutSine), RepeatMode.Reverse),
        label         = "masonry_glow",
    )

    val cardHeight = if (isTall) 180.dp else 150.dp

    Box(
        modifier = modifier
            .height(cardHeight)
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        Color(0xFF0D1B2A),
                        rank.primaryColor.copy(alpha = 0.22f),
                        Color(0xFF0A0E1A),
                    )
                )
            )
            .border(
                1.5.dp,
                Brush.linearGradient(listOf(rank.primaryColor.copy(alpha = 0.55f), rank.secondaryColor.copy(alpha = 0.22f))),
                RoundedCornerShape(20.dp),
            )
            .drawBehind {
                drawCircle(
                    color  = rank.glowColor.copy(alpha = glowAlpha),
                    radius = size.minDimension * 0.6f,
                    center = Offset(size.width * 0.8f, size.height * 0.2f),
                )
            }
            .padding(14.dp),
        contentAlignment = Alignment.BottomStart,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            // Big rank symbol at top-right
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.TopEnd) {
                Text(
                    rank.symbol,
                    fontSize = if (isTall) 42.sp else 34.sp,
                )
            }

            Spacer(Modifier.weight(1f))

            // Exercise name
            Text(
                exRank.exerciseName,
                style      = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.ExtraBold,
                color      = Color.White,
                maxLines   = 2,
            )

            // Best 1RM
            Text(
                "${exRank.best1Rm.toInt()} кг × 1",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.6f),
            )

            // Rank name chip
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(rank.primaryColor.copy(alpha = 0.22f))
                    .border(0.5.dp, rank.primaryColor.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 3.dp),
            ) {
                Text(
                    rank.name,
                    style      = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color      = rank.primaryColor,
                )
            }
        }
    }
}

// ─── Add dialog (reused from BodyAnalysisScreen) ─────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddExerciseRankDialog(
    exercises: List<ExerciseEntity>,
    selectedEx: ExerciseEntity?,
    weightInput: String,
    onSelectEx: (ExerciseEntity) -> Unit,
    onWeightChange: (String) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit,
) {
    var searchQuery by remember { mutableStateOf("") }
    val filtered = remember(searchQuery, exercises) {
        if (searchQuery.isBlank()) exercises
        else exercises.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape  = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text("Добавить / обновить данные", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

                OutlinedTextField(
                    value         = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder   = { Text("Поиск упражнения") },
                    modifier      = Modifier.fillMaxWidth(),
                    singleLine    = true,
                    shape         = RoundedCornerShape(12.dp),
                )

                if (selectedEx == null) {
                    Card(
                        modifier = Modifier.fillMaxWidth().height(220.dp),
                        shape    = RoundedCornerShape(12.dp),
                        colors   = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    ) {
                        androidx.compose.foundation.lazy.LazyColumn {
                            items(filtered.take(10)) { ex ->
                                Row(
                                    modifier  = Modifier.fillMaxWidth().clickable { onSelectEx(ex) }.padding(12.dp),
                                    verticalAlignment     = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                ) {
                                    Column(Modifier.weight(1f)) {
                                        Text(ex.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                        Text(ex.primaryMuscle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(0.3f), modifier = Modifier.padding(horizontal = 12.dp))
                            }
                        }
                    }
                } else {
                    Row(
                        modifier  = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.primaryContainer.copy(0.3f)).padding(12.dp),
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Text(selectedEx.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    }

                    OutlinedTextField(
                        value         = weightInput,
                        onValueChange = { if (it.all { c -> c.isDigit() || c == '.' }) onWeightChange(it) },
                        label         = { Text("Вес на 1 повторение") },
                        suffix        = { Text("кг") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier      = Modifier.fillMaxWidth(),
                        singleLine    = true,
                        shape         = RoundedCornerShape(12.dp),
                    )

                    val previewRankIdx = weightInput.toDoubleOrNull()?.let {
                        ExerciseRankThresholds.rankIndexFor(selectedEx.name, it)
                    }
                    if (previewRankIdx != null) {
                        val previewRank = StrengthRanks.all[previewRankIdx]
                        Row(
                            modifier  = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(previewRank.primaryColor.copy(0.1f)).padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text(previewRank.symbol, fontSize = 24.sp)
                            Column {
                                Text("Ранг упражнения", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(previewRank.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = previewRank.primaryColor)
                            }
                        }
                    }
                }

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    androidx.compose.material3.TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("Отмена") }
                    Button(
                        onClick  = onSave,
                        modifier = Modifier.weight(1f),
                        enabled  = selectedEx != null && weightInput.toDoubleOrNull() != null,
                        shape    = RoundedCornerShape(12.dp),
                    ) { Text("Сохранить") }
                }
            }
        }
    }
}