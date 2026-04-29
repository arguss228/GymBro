package com.gymbro.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * График прогресса — нарастающий максимальный вес.
 * Реализован через Compose Canvas (без MPAndroidChart) — корректно
 * адаптируется к светлой и тёмной теме через MaterialTheme.colorScheme.
 *
 * @param points (timestampMillis, weightKg) — уже отфильтрованные и
 *               преобразованные в нарастающий максимум в ViewModel.
 * @param label  подпись набора данных (отображается в заголовке).
 */
@Composable
fun ProgressChart(
    points: List<Pair<Long, Float>>,
    label: String,
    modifier: Modifier = Modifier,
) {
    if (points.isEmpty()) return

    val primary    = MaterialTheme.colorScheme.primary
    val onSurface  = MaterialTheme.colorScheme.onSurface
    val gridColor  = MaterialTheme.colorScheme.outlineVariant
    val dateFormat = SimpleDateFormat("d MMM", Locale("ru"))

    val maxVal = points.maxOf { it.second }
    val minVal = points.minOf { it.second }
    val range  = (maxVal - minVal).coerceAtLeast(1f)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(20.dp),
        colors   = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    "Макс: ${"%.1f".format(maxVal)} кг",
                    style = MaterialTheme.typography.labelMedium,
                    color = primary,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    "Точек: ${points.size}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            // Canvas chart
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(12.dp)),
            ) {
                val w     = size.width
                val h     = size.height
                val pad   = 12.dp.toPx()  // left/right padding inside canvas
                val stepX = if (points.size > 1) (w - pad * 2) / (points.size - 1) else w

                // ── Horizontal grid lines ──
                val gridLines = 4
                repeat(gridLines + 1) { i ->
                    val y = h * i / gridLines
                    drawLine(
                        color       = gridColor.copy(alpha = 0.4f),
                        start       = Offset(0f, y),
                        end         = Offset(w, y),
                        strokeWidth = 1.dp.toPx(),
                    )
                }

                // ── Build path ──
                val linePath = Path()
                points.forEachIndexed { i, (_, v) ->
                    val cx = pad + i * stepX
                    val cy = h - ((v - minVal) / range) * (h - pad)
                    if (i == 0) linePath.moveTo(cx, cy) else linePath.lineTo(cx, cy)
                }

                // ── Fill under the line ──
                val fillPath = Path().apply {
                    addPath(linePath)
                    val lastCx = pad + (points.size - 1) * stepX
                    lineTo(lastCx, h)
                    lineTo(pad, h)
                    close()
                }
                drawPath(
                    path  = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(primary.copy(alpha = 0.30f), Color.Transparent),
                    ),
                )

                // ── Draw line stroke ──
                drawPath(
                    path  = linePath,
                    color = primary,
                    style = Stroke(
                        width = 2.5.dp.toPx(),
                        cap   = StrokeCap.Round,
                        join  = StrokeJoin.Round,
                    ),
                )

                // ── Dots ──
                points.forEachIndexed { i, (_, v) ->
                    val cx = pad + i * stepX
                    val cy = h - ((v - minVal) / range) * (h - pad)
                    drawCircle(color = primary, radius = 4.dp.toPx(), center = Offset(cx, cy))
                    drawCircle(color = Color.White, radius = 2.dp.toPx(), center = Offset(cx, cy))
                }
            }

            // ── X-axis labels ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    dateFormat.format(Date(points.first().first)),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (points.size > 2) {
                    Text(
                        dateFormat.format(Date(points[points.size / 2].first)),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Text(
                    dateFormat.format(Date(points.last().first)),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}