package com.gymbro.app.ui.rank

import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.gymbro.app.domain.model.StrengthRank

@Composable
fun RankUpDialog(newRank: StrengthRank, onDismiss: () -> Unit) {
    var targetScale by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(Unit) { targetScale = 1f }

    val scale by animateFloatAsState(
        targetValue   = targetScale,
        animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow),
        label         = "rankup_scale",
    )
    val alpha by animateFloatAsState(
        targetValue   = if (targetScale > 0f) 1f else 0f,
        animationSpec = tween(500),
        label         = "rankup_alpha",
    )
    val infiniteTransition = rememberInfiniteTransition(label = "rankup_glow")
    val glowPulse by infiniteTransition.animateFloat(
        initialValue  = 0.4f,
        targetValue   = 1f,
        animationSpec = infiniteRepeatable(tween(900, easing = EaseInOutSine), RepeatMode.Reverse),
        label         = "rankup_pulse",
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties       = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier         = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.88f)),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.88f)
                    .scale(scale)
                    .clip(RoundedCornerShape(32.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF0A0E1A), newRank.primaryColor.copy(alpha = 0.18f), Color(0xFF0A0E1A))
                        )
                    )
                    .drawBehind {
                        drawRoundRect(
                            brush        = Brush.linearGradient(listOf(newRank.primaryColor, newRank.secondaryColor)),
                            cornerRadius = CornerRadius(32.dp.toPx()),
                            style        = Stroke(2.dp.toPx()),
                            alpha        = glowPulse,
                        )
                        drawCircle(newRank.glowColor.copy(alpha = glowPulse * 0.18f), size.minDimension * 0.7f)
                    }
                    .padding(36.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Text("🎉", fontSize = 44.sp)
                Text(
                    "НОВЫЙ РАНГ",
                    style         = MaterialTheme.typography.labelLarge,
                    fontWeight    = FontWeight.ExtraBold,
                    color         = newRank.primaryColor,
                    letterSpacing = 4.sp,
                )
                Text(newRank.symbol, fontSize = 88.sp, textAlign = TextAlign.Center)
                Text(
                    newRank.name,
                    style      = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Black,
                    color      = newRank.primaryColor,
                    textAlign  = TextAlign.Center,
                )
                Text(
                    newRank.description,
                    style     = MaterialTheme.typography.bodyMedium,
                    color     = Color.White.copy(alpha = 0.65f),
                    textAlign = TextAlign.Center,
                    lineHeight= 22.sp,
                )
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick  = onDismiss,
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