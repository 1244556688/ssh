package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPurple

/**
 * A beautiful modern radial gauge displaying percentage stats with smooth transitions.
 */
@Composable
fun CircularGauge(
    percentage: Float,
    label: String,
    modifier: Modifier = Modifier,
    primaryColor: Color = NeonPurple,
    secondaryColor: Color = NeonCyan
) {
    val animatedPercentage by animateFloatAsState(targetValue = percentage, label = "gauge_anim")

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 14.dp.toPx()
            val innerSize = size.minDimension - strokeWidth
            val offset = (size.minDimension - innerSize) / 2f
            
            // Background track
            drawArc(
                color = DarkSurfaceBorder.copy(alpha = 0.2f),
                startAngle = 135f,
                sweepAngle = 270f,
                useCenter = false,
                topLeft = Offset(offset, offset),
                size = Size(innerSize, innerSize),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Dynamic progress brush gradient
            val gradientBrush = Brush.sweepGradient(
                colors = listOf(primaryColor, secondaryColor, primaryColor),
                center = center
            )

            // Foreground progress arc
            drawArc(
                brush = gradientBrush,
                startAngle = 135f,
                sweepAngle = (animatedPercentage / 100f) * 270f,
                useCenter = false,
                topLeft = Offset(offset, offset),
                size = Size(innerSize, innerSize),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }

        // Percentage text overlay
        Text(
            text = "${percentage.toInt()}%",
            style = MaterialTheme.typography.titleLarge,
            color = Color.White
        )
    }
}

/**
 * A custom Canvas-drawn wave chart representing historically updated values over time.
 */
@Composable
fun RealtimeLineChart(
    history: List<Float>,
    modifier: Modifier = Modifier,
    lineColor: Color = NeonCyan
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        if (history.isEmpty()) return@Canvas

        val maxVal = 100f
        val stepX = size.width / (history.size - 1).coerceAtLeast(1)
        
        // Draw grid lines
        val gridLines = 4
        val gridStepY = size.height / gridLines
        for (i in 0..gridLines) {
            val y = i * gridStepY
            drawLine(
                color = DarkSurfaceBorder.copy(alpha = 0.15f),
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 1.dp.toPx()
            )
        }

        // Plot history points
        val points = history.mapIndexed { index, value ->
            val x = index * stepX
            val normValue = (value / maxVal).coerceIn(0f, 1f)
            val y = size.height - (normValue * size.height)
            Offset(x, y)
        }

        // Draw connection strokes
        for (i in 0 until points.size - 1) {
            drawLine(
                color = lineColor,
                start = points[i],
                end = points[i + 1],
                strokeWidth = 3.dp.toPx(),
                cap = StrokeCap.Round
            )
        }

        // Fill background area under curve with a nice vertical gradient
        val fillBrush = Brush.verticalGradient(
            colors = listOf(lineColor.copy(alpha = 0.35f), Color.Transparent)
        )
        val path = androidx.compose.ui.graphics.Path().apply {
            moveTo(0f, size.height)
            points.forEach { lineTo(it.x, it.y) }
            lineTo(size.width, size.height)
            close()
        }
        drawPath(path = path, brush = fillBrush)
    }
}
