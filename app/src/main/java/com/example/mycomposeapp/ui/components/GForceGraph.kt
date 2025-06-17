package com.example.mycomposeapp.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Composable
fun CombinedGForceGraph(
    historyX: List<Float>,
    historyY: List<Float>,
    historyZ: List<Float>,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val textPaint = remember(density) {
        android.graphics.Paint().apply {
            color = android.graphics.Color.WHITE
            textSize = with(density) { 12.sp.toPx() }
            textAlign = android.graphics.Paint.Align.LEFT
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        drawGrid(textPaint)
        drawLineGraph(historyX, Color.Red)
        drawLineGraph(historyY, Color.Green)
        drawLineGraph(historyZ, Color.Blue)
    }
}


private fun DrawScope.drawGrid(textPaint: android.graphics.Paint) {
    val width = size.width
    val height = size.height
    val centerY = height / 2
    val maxG = 6f
    val gridColor = Color.LightGray.copy(alpha = 0.3f)

    // Horizontal lines with labels
    for (g in -5..5) {
        val gf = g.toFloat()
        val y = centerY - (gf * height) / (2 * maxG)
        drawLine(
            color = gridColor,
            start = Offset(0f, y),
            end = Offset(width, y),
            strokeWidth = 1.dp.toPx()
        )
        if (g != 0) {
            drawContext.canvas.nativeCanvas.drawText(
                "${"%.1f".format(gf)}G",
                8f,
                y - 4f,
                textPaint
            )
        }
    }

    // Vertical lines
    val stepCount = 10
    for (i in 0..stepCount) {
        val x = width * i / stepCount
        drawLine(
            color = gridColor,
            start = Offset(x, 0f),
            end = Offset(x, height),
            strokeWidth = 1.dp.toPx()
        )
    }

    // Zero line
    drawLine(
        color = Color.Gray.copy(alpha = 0.5f),
        start = Offset(0f, centerY),
        end = Offset(width, centerY),
        strokeWidth = 2.dp.toPx()
    )
}


private fun DrawScope.drawLineGraph(
    history: List<Float>,
    color: Color
) {
    if (history.isEmpty()) return

    val width = size.width
    val height = size.height
    val centerY = height / 2
    val maxG = 6f
    val pointCount = history.size
    val stepX = width / (pointCount - 1).coerceAtLeast(1)

    for (i in 1 until pointCount) {
        val prevValue = history[i - 1].coerceIn(-maxG, maxG)
        val currentValue = history[i].coerceIn(-maxG, maxG)

        val prevY = centerY - (prevValue * height) / (2 * maxG)
        val currentY = centerY - (currentValue * height) / (2 * maxG)

        drawLine(
            color = color,
            start = Offset((i - 1) * stepX, prevY),
            end = Offset(i * stepX, currentY),
            strokeWidth = 2.dp.toPx(),
            cap = StrokeCap.Round
        )

        drawCircle(
            color = color,
            radius = 1.dp.toPx(),
            center = Offset(i * stepX, currentY)
        )
    }
}