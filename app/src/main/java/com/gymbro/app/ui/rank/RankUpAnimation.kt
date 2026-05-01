package com.gymbro.app.ui.rank

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.gymbro.app.domain.model.StrengthRank

@Composable
fun RankUpDialog(
    newRank: StrengthRank,
    onDismiss: () -> Unit,
) {
    val scale by animateFloatAsState(
        targetValue   = 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label         = "scale",
    )
    val alpha by animateFloatAsState(
        targetValue   = 1f,
        animationSpec = tween(600),
        label         = "alpha",
    )
    val infiniteTransition = rememberInfiniteTransition(label = "rankup")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue  = 0.4f,
        targetValue   = 1f,
        animationSpec = infiniteRepeatable(tween(900, easing = EaseInOutSine), RepeatMode.Reverse),
        label         = "glow",
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties       = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.85f))
                .alpha(alpha),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.88f)
                    .scale(scale)
                    .clip(RoundedCornerShape(32.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color(0xFF0A0E1A),
                                newRank.primaryColor.copy(alpha = 0.2f),
                                Color(0xFF0A0E1A),
                            )
                        )
                    )
                    .drawBehind {
                        drawRoundRect(
                            brush        = Brush.linearGradient(listOf(newRank.primaryColor, newRank.secondaryColor)),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(32.dp.toPx()),
                            style        = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx()),
                            alpha        = glowAlpha,
                        )
                        drawCircle(
                            color  = newRank.glowColor.copy(alpha = glowAlpha * 0.2f),
                            radius = size.minDimension,
                            center = center,
                        )
                    }
                    .padding(36.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text("🎉", fontSize = 48.sp)
                Text(
                    "НОВЫЙ РАНГ!",
                    style         = MaterialTheme.typography.labelLarge,
                    fontWeight    = FontWeight.ExtraBold,
                    color         = newRank.primaryColor,
                    letterSpacing = 4.sp,
                )
                Text(newRank.symbol, fontSize = 80.sp, textAlign = TextAlign.Center)
                Text(
                    newRank.name,
                    style      = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Black,
                    color      = newRank.primaryColor,
                )
                Text(
                    newRank.description,
                    style     = MaterialTheme.typography.bodyMedium,
                    color     = Color.White.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape    = RoundedCornerShape(16.dp),
                    colors   = ButtonDefaults.buttonColors(
                        containerColor = newRank.primaryColor,
                        contentColor   = Color.Black,
                    ),
                ) {
                    Text("Продолжать!", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}