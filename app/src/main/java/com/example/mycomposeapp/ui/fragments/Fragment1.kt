package com.example.mycomposeapp.ui.fragments

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.mycomposeapp.ui.components.CombinedGForceGraph
import com.example.mycomposeapp.ui.components.LegendItem
import com.example.mycomposeapp.ui.components.ValueDisplay
import kotlin.math.pow
import kotlin.math.sqrt

// Fragment1.kt
@SuppressLint("UnrememberedMutableState")
@Composable
fun Fragment1(acceleration: State<Triple<Float, Float, Float>>) {
    val historyX = remember { mutableStateListOf<Float>() }
    val historyY = remember { mutableStateListOf<Float>() }
    val historyZ = remember { mutableStateListOf<Float>() }
    val maxDataPoints = 50 // Increased buffer size for smoother animation
    var showGraph by remember { mutableStateOf(true) }

    // Calculate total G once for both display modes
    val totalG by derivedStateOf {
        sqrt(
            acceleration.value.first.pow(2) +
                    acceleration.value.second.pow(2) +
                    acceleration.value.third.pow(2)
        ) / 9.81f
    }

    LaunchedEffect(acceleration.value) {
        val xG = acceleration.value.first / 9.81f
        val yG = acceleration.value.second / 9.81f
        val zG = acceleration.value.third / 9.81f

        historyX.add(xG)
        historyY.add(yG)
        historyZ.add(zG)

        // Trim history if needed
        if (historyX.size > maxDataPoints) {
            historyX.removeAt(0)
            historyY.removeAt(0)
            historyZ.removeAt(0)
        }
    }

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize()
    ) {
        // Combined button with total G display
        Button(
            onClick = { showGraph = !showGraph },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (showGraph) "Show Numerical Values" else "Show Graphs",
                    style = MaterialTheme.typography.labelLarge
                )
                Text(
                    text = "Total G: ${"%.2f".format(totalG)}",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (showGraph) {
            // Smoother graph implementation
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(8.dp)
            ) {
                Text(
                    text = "Acceleration Graph",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Combined graph with all axes
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                        .background(MaterialTheme.colorScheme.background)
                        .padding(4.dp)
                ) {
                    CombinedGForceGraph(
                        historyX = historyX,
                        historyY = historyY,
                        historyZ = historyZ,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Legend
                Row(
                    modifier = Modifier.padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    LegendItem(color = Color.Red, text = "X Axis")
                    Spacer(modifier = Modifier.width(16.dp))
                    LegendItem(color = Color.Green, text = "Y Axis")
                    Spacer(modifier = Modifier.width(16.dp))
                    LegendItem(color = Color.Blue, text = "Z Axis")
                }
            }
        } else {
            // Numerical display
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ValueDisplay(
                    label = "X Axis",
                    value = acceleration.value.first / 9.81f,
                    color = Color.Red
                )
                Spacer(modifier = Modifier.height(16.dp))
                ValueDisplay(
                    label = "Y Axis",
                    value = acceleration.value.second / 9.81f,
                    color = Color.Green
                )
                Spacer(modifier = Modifier.height(16.dp))
                ValueDisplay(
                    label = "Z Axis",
                    value = acceleration.value.third / 9.81f,
                    color = Color.Blue
                )
            }
        }
    }
}