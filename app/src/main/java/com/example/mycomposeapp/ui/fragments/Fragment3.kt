// ui/fragments/Fragment3.kt
package com.example.mycomposeapp.ui.fragments

import android.graphics.Color
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



@Composable
fun Fragment3() {
    val context = LocalContext.current
    val lifecycleOwner = context as LifecycleOwner
    val previewView = remember { PreviewView(context) }
    val executor = remember { Executors.newSingleThreadExecutor() }

    var centerColor by remember { mutableStateOf(Color.BLACK) }

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

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        // Camera preview view
        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize()
        )

        // Center point overlay
        Box(
            modifier = Modifier
                .size(16.dp)
                .align(Alignment.Center)
                .background(
                    color = MaterialTheme.colorScheme.primary,
                    shape = CircleShape
                )
        )

        // Color display at the bottom
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .align(Alignment.BottomCenter)
                .background(androidx.compose.ui.graphics.Color(centerColor)),
            contentAlignment = Alignment.Center
        ) {
            val hex = String.format("#%06X", 0xFFFFFF and centerColor)
            Text(
                text = "Color: $hex",
                style = MaterialTheme.typography.bodyLarge,
                color = if (androidx.compose.ui.graphics.Color(centerColor).luminance() < 0.5f)
                    androidx.compose.ui.graphics.Color.White
                else
                    androidx.compose.ui.graphics.Color.Black
            )
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
