package com.example.safemotion.data.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.example.safemotion.data.model.IncidentType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

data class SensorStatus(
    val accelerometerAvailable: Boolean = false,
    val gyroscopeAvailable: Boolean = false,
    val monitoring: Boolean = false
)

interface RunSensorMonitor {
    val status: StateFlow<SensorStatus>
    val incidents: Flow<IncidentType>
    fun start()
    fun stop()
}

class AndroidRunSensorMonitor(context: Context) : RunSensorMonitor, SensorEventListener {
    private val manager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer = manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val gyroscope = manager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
    private val detector = IncidentDetector()
    private val _status = MutableStateFlow(
        SensorStatus(
            accelerometerAvailable = accelerometer != null,
            gyroscopeAvailable = gyroscope != null
        )
    )
    override val status: StateFlow<SensorStatus> = _status.asStateFlow()

    private val _incidents = MutableSharedFlow<IncidentType>(extraBufferCapacity = 1)
    override val incidents: Flow<IncidentType> = _incidents.asSharedFlow()

    override fun start() {
        if (_status.value.monitoring || accelerometer == null || gyroscope == null) return
        detector.reset()
        val accelerometerRegistered = manager.registerListener(
            this, accelerometer, SensorManager.SENSOR_DELAY_GAME
        )
        val gyroscopeRegistered = manager.registerListener(
            this, gyroscope, SensorManager.SENSOR_DELAY_GAME
        )
        if (!accelerometerRegistered || !gyroscopeRegistered) {
            manager.unregisterListener(this)
            return
        }
        _status.value = _status.value.copy(monitoring = true)
    }

    override fun stop() {
        manager.unregisterListener(this)
        detector.reset()
        _status.value = _status.value.copy(monitoring = false)
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (!_status.value.monitoring) return
        val values = event.values
        val incident = when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> detector.onAcceleration(
                values[0], values[1], values[2], event.timestamp
            )
            Sensor.TYPE_GYROSCOPE -> detector.onRotation(
                values[0], values[1], values[2], event.timestamp
            )
            else -> null
        }
        if (incident != null) _incidents.tryEmit(incident)
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) = Unit
}

