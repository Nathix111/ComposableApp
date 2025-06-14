// ui/components/GForceGraph.kt
package com.example.mycomposeapp.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun GForceGraph(history: List<Float>, color: Color, label: String) {
    Column {
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
        Canvas(modifier = Modifier.fillMaxWidth().height(100.dp).background(Color.LightGray)) {
            val stepX = size.width / (history.size.coerceAtLeast(1))
            val maxY = 4f // Maximum scale (can be adjusted as needed)
            val stepY = size.height / (2 * maxY)

            drawLine(
                Color.Gray,
                Offset(0f, size.height / 2),
                Offset(size.width, size.height / 2),
                strokeWidth = 2f
            )

            for (i in 1 until history.size) {
                drawLine(
                    color = color,
                    start = Offset((i - 1) * stepX, size.height / 2 - history[i - 1] * stepY),
                    end = Offset(i * stepX, size.height / 2 - history[i] * stepY),
                    strokeWidth = 4f
                )
            }
        }
    }
}