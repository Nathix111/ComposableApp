// MainActivity.kt
package com.example.mycomposeapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.mutableStateOf
import com.example.mycomposeapp.ui.theme.MyComposeAppTheme
import com.example.mycomposeapp.ui.MainScreen
import com.example.mycomposeapp.sensor.AccelerationSensorManager

class MainActivity : ComponentActivity() {
    private lateinit var sensorManager: AccelerationSensorManager
    private val acceleration = mutableStateOf(Triple(0f, 0f, 0f))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sensorManager = AccelerationSensorManager(this)
        sensorManager.startListening { x, y, z ->
            acceleration.value = Triple(x, y, z)
        }

        setContent {
            MyComposeAppTheme {
                MainScreen(acceleration = acceleration)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.stopListening()
    }

    override fun onResume() {
        super.onResume()
        sensorManager.startListening { x, y, z ->
            acceleration.value = Triple(x, y, z)
        }
    }
}