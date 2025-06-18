package com.example.mycomposeapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.mycomposeapp.sensor.AccelerationSensorManager
import com.example.mycomposeapp.ui.MainScreen
import com.example.mycomposeapp.ui.theme.MyComposeAppTheme

class MainActivity : ComponentActivity() {
    private lateinit var sensorManager: AccelerationSensorManager
    private val acceleration = mutableStateOf(Triple(0f, 0f, 0f))
    private var shouldShowPermissionRationale = mutableStateOf(false)

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
                // Check if we should show rationale for any permission
                val shouldShowRationale = permissions.any { entry ->
                    !entry.value && shouldShowRequestPermissionRationale(entry.key)
                }

                shouldShowPermissionRationale.value = shouldShowRationale
                showPermissionUI()
            }
        }

        checkPermissions()
    }

    private fun checkPermissions() {
        val requiredPermissions = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        )

        if (requiredPermissions.all { perm ->
                ContextCompat.checkSelfPermission(this, perm) == PackageManager.PERMISSION_GRANTED
            }) {
            launchApp()
        } else {
            // First check if we should show rationale before requesting
            val shouldShowRationale = requiredPermissions.any { perm ->
                shouldShowRequestPermissionRationale(perm)
            }

            shouldShowPermissionRationale.value = shouldShowRationale
            showPermissionUI()
        }
    }

    private fun showPermissionUI() {
        setContent {
            MyComposeAppTheme {
                PermissionRequestScreen(
                    shouldShowRationale = shouldShowPermissionRationale.value,
                    onRequestPermission = {
                        val requiredPermissions = arrayOf(
                            Manifest.permission.CAMERA,
                            Manifest.permission.RECORD_AUDIO
                        )
                        requestPermissionLauncher.launch(requiredPermissions)
                    },
                    onOpenSettings = {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        val uri = Uri.fromParts("package", packageName, null)
                        intent.data = uri
                        startActivity(intent)
                    }
                )
            }
        }
    }

    private fun launchApp() {
        setContent {
            MyComposeAppTheme {
                MainScreen(acceleration = acceleration, this)
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

@Composable
fun PermissionRequestScreen(
    shouldShowRationale: Boolean,
    onRequestPermission: () -> Unit,
    onOpenSettings: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Permissions Required",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (shouldShowRationale) {
            Text(
                text = "This app needs camera and microphone permissions to function properly. " +
                        "Please grant these permissions in the next dialog.",
                modifier = Modifier.padding(bottom = 24.dp),
                textAlign = TextAlign.Center
            )
        } else {
            Text(
                text = "Please grant camera and microphone permissions in system dialog",
                modifier = Modifier.padding(bottom = 24.dp),
                textAlign = TextAlign.Center
            )
        }

        Button(onClick = onRequestPermission) {
            Text("Grant Permissions")
        }

        if (!shouldShowRationale) {
            Spacer(modifier = Modifier.height(16.dp))
            TextButton(onClick = onOpenSettings) {
                Text("Open App Settings")
            }
        }
    }
}