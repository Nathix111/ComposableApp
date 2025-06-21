// ui/fragments/Fragment3.kt
package com.example.mycomposeapp.ui.fragments

import android.annotation.SuppressLint
import android.graphics.Color
import android.widget.Toast
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.LifecycleOwner
import java.util.concurrent.Executors
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.SnapPosition
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString


@SuppressLint("LocalContextConfigurationRead")
@Composable
fun Fragment3() {
    val context = LocalContext.current
    val lifecycleOwner = context as LifecycleOwner
    val previewView = remember { PreviewView(context) }
    val executor = remember { Executors.newSingleThreadExecutor() }

    var centerColor by remember { mutableStateOf(Color.BLACK) }

    val clipboardManager = LocalClipboardManager.current
    val hex = String.format("#%06X", 0xFFFFFF and centerColor)

    val isLandscape = context.resources.configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE

    LaunchedEffect(Unit) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        val cameraProvider = cameraProviderFuture.get()

        val preview = androidx.camera.core.Preview.Builder().build().apply {
            setSurfaceProvider(previewView.surfaceProvider)
        }

        val analyzer = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
            .also {
                it.setAnalyzer(executor, PixelAnalyzer { color ->
                    centerColor = color
                })
            }

        cameraProvider.unbindAll()
        cameraProvider.bindToLifecycle(
            lifecycleOwner,
            CameraSelector.DEFAULT_BACK_CAMERA,
            preview,
            analyzer
        )
    }

    if (isLandscape) {
        // Root Box to allow overlaying the marker
        Box(modifier = Modifier.fillMaxSize()) {
            Row(modifier = Modifier.fillMaxSize()) {
                AndroidView(
                    factory = { previewView },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )

                Box(
                    modifier = Modifier
                        .width(150.dp)
                        .fillMaxHeight()
                        .background(androidx.compose.ui.graphics.Color(centerColor))
                        .clickable {
                            clipboardManager.setText(AnnotatedString(hex))
                            Toast.makeText(context, "Couleur $hex copiée au presse-papiers", Toast.LENGTH_SHORT).show()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Couleur: $hex",
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (androidx.compose.ui.graphics.Color(centerColor).luminance() < 0.5f)
                            androidx.compose.ui.graphics.Color.White
                        else
                            androidx.compose.ui.graphics.Color.Black
                    )
                }
            }

            // Center marker overlay (for landscape)
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .align(Alignment.Center)
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = CircleShape
                    )
            )
        }
    } else {
        // Portrait layout
        Box(modifier = Modifier.fillMaxSize()) {
            AndroidView(
                factory = { previewView },
                modifier = Modifier.fillMaxSize()
            )

            Box(
                modifier = Modifier
                    .size(12.dp)
                    .align(Alignment.Center)
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = CircleShape
                    )
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .align(Alignment.BottomCenter)
                    .background(androidx.compose.ui.graphics.Color(centerColor))
                    .clickable {
                        clipboardManager.setText(AnnotatedString(hex))
                        Toast.makeText(context, "Couleur $hex copiée au presse-papiers", Toast.LENGTH_SHORT).show()
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Couleur: $hex",
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (androidx.compose.ui.graphics.Color(centerColor).luminance() < 0.5f)
                        androidx.compose.ui.graphics.Color.White
                    else
                        androidx.compose.ui.graphics.Color.Black
                )
            }
        }
    }
}



class PixelAnalyzer(val onColorDetected: (Int) -> Unit) : ImageAnalysis.Analyzer {
    override fun analyze(image: ImageProxy) {
        val yPlane = image.planes[0].buffer
        val uPlane = image.planes[1].buffer
        val vPlane = image.planes[2].buffer

        val width = image.width
        val height = image.height
        val centerX = width / 2
        val centerY = height / 2

        val yRowStride = image.planes[0].rowStride
        val uvRowStride = image.planes[1].rowStride
        val uvPixelStride = image.planes[1].pixelStride

        val yIndex = centerY * yRowStride + centerX
        val uvIndex = (centerY / 2) * uvRowStride + (centerX / 2) * uvPixelStride

        val y = yPlane.get(yIndex).toInt() and 0xFF
        val u = uPlane.get(uvIndex).toInt() and 0xFF
        val v = vPlane.get(uvIndex).toInt() and 0xFF

        // Convert YUV to RGB (BT.601 conversion)
        val r = (y + 1.403 * (v - 128)).toInt().coerceIn(0, 255)
        val g = (y - 0.344 * (u - 128) - 0.714 * (v - 128)).toInt().coerceIn(0, 255)
        val b = (y + 1.770 * (u - 128)).toInt().coerceIn(0, 255)

        val color = Color.rgb(r, g, b)
        onColorDetected(color)

        image.close()
    }


}
