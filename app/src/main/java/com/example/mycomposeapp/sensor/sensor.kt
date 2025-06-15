package com.example.mycomposeapp.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager

class AccelerationSensorManager(context: Context) : SensorEventListener {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private var onSensorChanged: ((Float, Float, Float) -> Unit)? = null

    fun startListening(callback: (Float, Float, Float) -> Unit) {
        onSensorChanged = callback
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME)
    }

    fun stopListening() {
        sensorManager.unregisterListener(this)
        onSensorChanged = null
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            onSensorChanged?.invoke(it.values[0], it.values[1], it.values[2])
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}