//com.example.mycomposeapp
package com.example.mycomposeapp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.ContextCompat
import com.example.mycomposeapp.sensor.AccelerationSensorManager
import com.example.mycomposeapp.ui.MainScreen
import com.example.mycomposeapp.ui.theme.MyComposeAppTheme

class MainActivity : ComponentActivity() {

    private lateinit var sensorManager: AccelerationSensorManager
    private val acceleration = mutableStateOf(Triple(0f, 0f, 0f))

    private lateinit var requestPermissionLauncher: ActivityResultLauncher<Array<String>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sensorManager = AccelerationSensorManager(this)
        sensorManager.startListening { x, y, z ->
            acceleration.value = Triple(x, y, z)
        }

        requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val allGranted = permissions.all { it.value }
            if (allGranted) {
                launchApp()
            } else {
                Toast.makeText(this, "Camera and microphone permissions are required", Toast.LENGTH_LONG).show()
                finish()
            }
        }

        val requiredPermissions = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        )

        if (requiredPermissions.all { perm ->
                ContextCompat.checkSelfPermission(this, perm) == PackageManager.PERMISSION_GRANTED
            }) {
            launchApp()
        } else {
            requestPermissionLauncher.launch(requiredPermissions)
        }
    }

    private fun launchApp() {
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