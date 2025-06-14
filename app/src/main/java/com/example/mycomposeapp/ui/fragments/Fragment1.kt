// ui/fragments/Fragment1.kt
package com.example.mycomposeapp.ui.fragments

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.mycomposeapp.ui.components.GForceGraph
import kotlin.math.sqrt

@Composable
fun Fragment1(acceleration: State<Triple<Float, Float, Float>>) {
    val historyX = remember { mutableStateListOf<Float>() }
    val historyY = remember { mutableStateListOf<Float>() }
    val historyZ = remember { mutableStateListOf<Float>() }
    val historyTotal = remember { mutableStateListOf<Float>() }
    val maxDataPoints = 30
    var showGraph by remember { mutableStateOf(true) }

    LaunchedEffect(acceleration.value) {
        val xG = acceleration.value.first / 9.81f
        val yG = acceleration.value.second / 9.81f
        val zG = acceleration.value.third / 9.81f
        val totalG = xG + yG + zG

        historyX.add(xG)
        historyY.add(yG)
        historyZ.add(zG)
        historyTotal.add(totalG)

        if (historyX.size > maxDataPoints) {
            historyX.removeAt(0)
            historyY.removeAt(0)
            historyZ.removeAt(0)
            historyTotal.removeAt(0)
        }
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Button(onClick = { showGraph = !showGraph }) {
            Text(text = if (showGraph) "Show Numerical Values" else "Show Graphs")
        }
        Spacer(modifier = Modifier.height(16.dp))

        if (showGraph) {
            Text(text = "G-force per axis graph")
            Spacer(modifier = Modifier.height(16.dp))
            Box(modifier = Modifier.background(Color.Black).padding(8.dp)) {
                Column {
                    GForceGraph(historyX, Color.Red, "X Axis")
                    GForceGraph(historyY, Color.Green, "Y Axis")
                    GForceGraph(historyZ, Color.Blue, "Z Axis")
                    GForceGraph(historyTotal, Color.Black, "Total G")
                }
            }
        } else {
            Text(
                text = "Total G: ${sqrt(
                    acceleration.value.first * acceleration.value.first +
                            acceleration.value.second * acceleration.value.second +
                            acceleration.value.third * acceleration.value.third
                ) / 9.81f}",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.fillMaxWidth().wrapContentWidth(Alignment.CenterHorizontally)
            )
        }
    }
}