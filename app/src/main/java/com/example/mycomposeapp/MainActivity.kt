package com.example.mycomposeapp

import android.Manifest

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.mycomposeapp.ui.theme.MyComposeAppTheme
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.background
import kotlin.math.sqrt
import androidx.compose.ui.Alignment

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.annotation.RequiresPermission
import kotlin.math.log10

class MainActivity : ComponentActivity(), SensorEventListener {



    private lateinit var sensorManager: SensorManager
    private var _acceleration = mutableStateOf(Triple(0f, 0f, 0f))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI)

        setContent {
            MyComposeAppTheme {
                MainScreen(acceleration = _acceleration)
            }
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            _acceleration.value = Triple(it.values[0], it.values[1], it.values[2])
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}

@Composable
fun MainScreen(acceleration: State<Triple<Float, Float, Float>>) {

    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(selected = selectedTab == 0, onClick = { selectedTab = 0 }, icon = { Text("Fragment 1") })
                NavigationBarItem(selected = selectedTab == 1, onClick = { selectedTab = 1 }, icon = { Text("Fragment 2") })
                NavigationBarItem(selected = selectedTab == 2, onClick = { selectedTab = 2 }, icon = { Text("Fragment 3") })
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when (selectedTab) {
                0 -> Fragment1(acceleration)
                1 -> Fragment2()
                2 -> Fragment3()
            }
        }
    }
}

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
            Text(text = if (showGraph) "Afficher Valeur Numérique" else "Afficher Graphiques")
        }
        Spacer(modifier = Modifier.height(16.dp))

        if (showGraph) {
            Text(text = "Graphique des forces G par axe")
            Spacer(modifier = Modifier.height(16.dp))
            Box(modifier = Modifier.background(Color.Black).padding(8.dp)) {
                Column {
                    GForceGraph(historyX, Color.Red, "Axe X")
                    GForceGraph(historyY, Color.Green, "Axe Y")
                    GForceGraph(historyZ, Color.Blue, "Axe Z")
                    GForceGraph(historyTotal, Color.Black, "Total G")
                }
            }
        } else {
            Text(
                text = "Total G: ${sqrt(acceleration.value.first * acceleration.value.first + acceleration.value.second * acceleration.value.second + acceleration.value.third * acceleration.value.third) / 9.81f}",
                style = MaterialTheme.typography.headlineMedium,
                //color = Color.Black,
                modifier = Modifier.fillMaxWidth().wrapContentWidth(Alignment.CenterHorizontally)
            )
        }
    }
}

@Composable
fun GForceGraph(history: List<Float>, color: Color, label: String) {
    Column {
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
        Canvas(modifier = Modifier.fillMaxWidth().height(100.dp).background(Color.LightGray)) {
            val stepX = size.width / (history.size.coerceAtLeast(1))
            val maxY = 4f // Échelle max (peut être ajustée en fonction des besoins)
            val stepY = size.height / (2 * maxY)
            drawLine(Color.Gray, Offset(0f, size.height / 2), Offset(size.width, size.height / 2), strokeWidth = 2f)
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

class AudioUtils {

    companion object {
        private const val SAMPLE_RATE = 44100 // Taux d'échantillonnage
        private const val BUFFER_SIZE = 1024 // Taille du tampon d'enregistrement

        private var audioRecord: AudioRecord? = null

        // Méthode pour obtenir les décibels
        @RequiresPermission(Manifest.permission.RECORD_AUDIO)
        fun getDecibels(): Double {
            // On récupère la taille du tampon minimale nécessaire pour l'enregistrement
            val bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT)

            // Initialisation de l'AudioRecord
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSize
            )

            val buffer = ShortArray(BUFFER_SIZE)
            audioRecord?.startRecording()

            // Lecture des données audio
            val numberOfReadBytes = audioRecord?.read(buffer, 0, buffer.size) ?: 0
            audioRecord?.stop()

            var sum = 0.0
            // Calcul de la somme des carrés des valeurs audio
            for (i in 0 until numberOfReadBytes) {
                sum += (buffer[i].toDouble() * buffer[i].toDouble())
            }

            val amplitude = sum / numberOfReadBytes
            return 10 * log10(amplitude) // Calcul du niveau en décibels
        }
    }
}

@Composable
fun Fragment2() {
    var currentDb by remember { mutableDoubleStateOf(0.0) }
    var maxDb by remember { mutableDoubleStateOf(0.0) }

    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = "Décibels actuels: ${currentDb.toInt()} dB", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Valeur maximale: ${maxDb.toInt()} dB",
                style = MaterialTheme.typography.bodyLarge
            )
            Button(onClick = { maxDb = 0.0 }) {
                Text("Réinitialiser Max")
            }
        }
    }



    LaunchedEffect(Unit) {
        while (true) {
            val newDb = (10..1000).random().toDouble() //AudioUtils.getDecibels()
            currentDb = newDb
            if (newDb > maxDb) {
                maxDb = newDb
            }
            kotlinx.coroutines.delay(500)
        }
    }
}




@Composable
fun Fragment3() {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "Page 3", style = MaterialTheme.typography.headlineMedium)
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    MyComposeAppTheme {
        MainScreen(acceleration = remember { mutableStateOf(Triple(0f, 0f, 0f)) })
    }
}
