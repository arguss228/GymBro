package com.gymbro.app.ui.components

import android.graphics.Color as AndroidColor
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * График прогресса — точки «вес подхода во времени» или «1RM во времени».
 *
 * @param points пары (timestampMillis, value)
 * @param label подпись датасета (например "Вес, кг")
 */
@Composable
fun ProgressChart(
    points: List<Pair<Long, Float>>,
    label: String,
    modifier: Modifier = Modifier,
) {
    val primaryColor = MaterialTheme.colorScheme.primary.toArgb()
    val textColor = MaterialTheme.colorScheme.onSurface.toArgb()
    val gridColor = MaterialTheme.colorScheme.onSurfaceVariant.toArgb()

    val dateFormatter = remember { SimpleDateFormat("d MMM", Locale.getDefault()) }

    AndroidView(
        modifier = modifier
            .fillMaxWidth()
            .height(220.dp)
            .clip(RoundedCornerShape(16.dp)),
        factory = { ctx ->
            LineChart(ctx).apply {
                description.isEnabled = false
                legend.textColor = textColor
                setNoDataTextColor(textColor)
                setTouchEnabled(true)
                setScaleEnabled(true)
                setPinchZoom(true)
                axisRight.isEnabled = false
                axisLeft.textColor = textColor
                axisLeft.gridColor = gridColor
                xAxis.position = XAxis.XAxisPosition.BOTTOM
                xAxis.textColor = textColor
                xAxis.gridColor = gridColor
                xAxis.granularity = 1f
                setExtraOffsets(8f, 8f, 16f, 8f)
            }
        },
        update = { chart ->
            if (points.isEmpty()) {
                chart.clear()
                chart.setNoDataText("Нет данных")
                chart.invalidate()
                return@AndroidView
            }
            val entries = points.mapIndexed { idx, (_, v) -> Entry(idx.toFloat(), v) }
            val set = LineDataSet(entries, label).apply {
                color = primaryColor
                setCircleColor(primaryColor)
                lineWidth = 2.2f
                circleRadius = 4f
                setDrawValues(false)
                mode = LineDataSet.Mode.CUBIC_BEZIER
                setDrawFilled(true)
                fillColor = primaryColor
                fillAlpha = 40
            }
            chart.data = LineData(set)

            chart.xAxis.valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    val idx = value.toInt().coerceIn(0, points.lastIndex)
                    return dateFormatter.format(Date(points[idx].first))
                }
            }

            chart.invalidate()
            chart.animateX(700)
        }
    )
}
