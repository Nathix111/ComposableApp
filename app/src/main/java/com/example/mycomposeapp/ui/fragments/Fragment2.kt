// ui/fragments/Fragment2.kt
package com.example.mycomposeapp.ui.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import com.example.mycomposeapp.MainActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlin.math.*

@Composable
fun Fragment2(activity: MainActivity) {
    var currentDb by remember { mutableFloatStateOf(0f) }
    var maxDb by remember { mutableFloatStateOf(0f) }
    var isActive by remember { mutableStateOf(true) }
    val dbHistory = remember { mutableStateListOf<Float>() }
    val MAX_HISTORY = 50 // Number of points to keep in history

    // Audio configuration
    val sampleRate = 44100
    val channelConfig = AudioFormat.CHANNEL_IN_MONO
    val audioFormat = AudioFormat.ENCODING_PCM_16BIT
    val bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)

    val audioRecord = remember {
        if (ActivityCompat.checkSelfPermission(
                activity,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                channelConfig,
                audioFormat,
                bufferSize
            )
        } else {
            null
        }
    }

    DisposableEffect(audioRecord) {
        if (audioRecord?.state == AudioRecord.STATE_INITIALIZED) {
            audioRecord.startRecording()
        }

        onDispose {
            isActive = false
            audioRecord?.stop()
            audioRecord?.release()
        }
    }

    LaunchedEffect(isActive) {
        withContext(Dispatchers.IO) {
            val buffer = ShortArray(bufferSize)
            while (isActive && audioRecord?.state == AudioRecord.STATE_INITIALIZED) {
                try {
                    val bytesRead = audioRecord.read(buffer, 0, bufferSize)

                    if (bytesRead > 0) {
                        var sum = 0.0
                        for (i in 0 until bytesRead) {
                            sum += buffer[i] * buffer[i]
                        }
                        val rms = sqrt(sum / bytesRead)
                        val reference = 0.1  // avoid log10(0) and scale range
                        val db = (20 * log10(rms / reference)).toFloat().coerceAtLeast(0f)

                        withContext(Dispatchers.Main) {
                            currentDb = db
                            if (db > maxDb) maxDb = db

                            // Add to history
                            dbHistory.add(db)
                            if (dbHistory.size > MAX_HISTORY) {
                                dbHistory.removeAt(0)
                            }
                        }
                    }
                    delay(100)
                } catch (e: Exception) {
                    e.printStackTrace()
                    withContext(Dispatchers.Main) {
                        // Handle error
                    }
                    break
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "%.1f dB".format(currentDb),
            fontSize = 48.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Decibel history graph
        DecibelGraph(
            dbHistory = dbHistory,
            modifier = Modifier
                .height(300.dp)
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Max: %.1f dB".format(maxDb), fontSize = 20.sp)
            Spacer(modifier = Modifier.width(16.dp))
            Button(onClick = { maxDb = 0f }) {
                Text("Réinitialiser")
            }
        }
    }
}

@Composable
fun DecibelGraph(dbHistory: List<Float>, modifier: Modifier = Modifier) {
    val graphColor = MaterialTheme.colorScheme.primary
    val minDbInHistory = 0f
    val maxDbInHistory = 120f
    val range = maxDbInHistory - minDbInHistory
    val legendSteps = listOf(120f, 80f, 40f, 0f)

    Row(modifier = modifier) {
        // Y-axis labels (legends)
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(end = 8.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            legendSteps.forEach { label ->
                Text(
                    text = "%.0f".format(label),
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }

        // Graph itself
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val padding = 8.dp.toPx()

            // Draw grid lines
            legendSteps.forEach { label ->
                val normalized = (label - minDbInHistory) / range
                val yPos = height - padding - (height - 2 * padding) * normalized

                drawLine(
                    color = Color.LightGray,
                    start = Offset(0f, yPos),
                    end = Offset(width, yPos),
                    strokeWidth = 1.dp.toPx()
                )
            }

            // Draw the graph path
            if (dbHistory.size > 1) {
                val path = Path()
                val xStep = width / (dbHistory.size - 1)

                dbHistory.forEachIndexed { index, db ->
                    val x = index * xStep
                    val normalizedDb = (db - minDbInHistory) / range
                    val y = height - padding - (height - 2 * padding) * normalizedDb

                    if (index == 0) {
                        path.moveTo(x, y)
                    } else {
                        path.lineTo(x, y)
                    }
                }

                drawPath(
                    path = path,
                    color = graphColor,
                    style = Stroke(
                        width = 3.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                )
            }
        }
    }
}
