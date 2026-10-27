package com.example.service

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class CompassSensorState(
    val heading: Float = 0f,
    val accuracy: Int = SensorManager.SENSOR_STATUS_ACCURACY_HIGH,
    val isSensorAvailable: Boolean = true,
    val needsCalibration: Boolean = false,
    val sensorTypeUsed: String = "Rotation Vector"
)

class CompassSensorManager(context: Context) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private var rotationVectorSensor: Sensor? = null
    private var accelerometerSensor: Sensor? = null
    private var magnetometerSensor: Sensor? = null

    private val _sensorState = MutableStateFlow(CompassSensorState())
    val sensorState: StateFlow<CompassSensorState> = _sensorState.asStateFlow()

    private val rotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)

    // Fallback sensor buffers
    private val lastAccelerometer = FloatArray(3)
    private val lastMagnetometer = FloatArray(3)
    private var isAccelerometerSet = false
    private var isMagnetometerSet = false

    private var isListening = false

    init {
        rotationVectorSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        if (rotationVectorSensor == null) {
            accelerometerSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            magnetometerSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        }

        val hasSensors = rotationVectorSensor != null || (accelerometerSensor != null && magnetometerSensor != null)
        _sensorState.value = _sensorState.value.copy(
            isSensorAvailable = hasSensors,
            sensorTypeUsed = if (rotationVectorSensor != null) "Sensor Rotasi Fusi" else if (hasSensors) "Akselerometer & Magnetik" else "Tidak Tersedia"
        )
    }

    fun startListening() {
        if (isListening || sensorManager == null) return

        if (rotationVectorSensor != null) {
            sensorManager.registerListener(this, rotationVectorSensor, SensorManager.SENSOR_DELAY_UI)
            isListening = true
        } else if (accelerometerSensor != null && magnetometerSensor != null) {
            sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_UI)
            sensorManager.registerListener(this, magnetometerSensor, SensorManager.SENSOR_DELAY_UI)
            isListening = true
        }
    }

    fun stopListening() {
        if (!isListening || sensorManager == null) return
        sensorManager.unregisterListener(this)
        isListening = false
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event ?: return

        when (event.sensor.type) {
            Sensor.TYPE_ROTATION_VECTOR -> {
                SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                SensorManager.getOrientation(rotationMatrix, orientationAngles)
                // Azimuth angle in radians -> convert to degrees
                var azimuthInDegrees = Math.toDegrees(orientationAngles[0].toDouble()).toFloat()
                azimuthInDegrees = (azimuthInDegrees + 360f) % 360f

                val smoothed = QiblaCalculator.smoothDegree(_sensorState.value.heading, azimuthInDegrees, 0.2f)

                _sensorState.value = _sensorState.value.copy(
                    heading = smoothed,
                    accuracy = event.accuracy,
                    needsCalibration = event.accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE || event.accuracy == SensorManager.SENSOR_STATUS_ACCURACY_LOW
                )
            }
            Sensor.TYPE_ACCELEROMETER -> {
                System.arraycopy(event.values, 0, lastAccelerometer, 0, event.values.size)
                isAccelerometerSet = true
                calculateOrientationFromAccelMag()
            }
            Sensor.TYPE_MAGNETIC_FIELD -> {
                System.arraycopy(event.values, 0, lastMagnetometer, 0, event.values.size)
                isMagnetometerSet = true
                calculateOrientationFromAccelMag()
            }
        }
    }

    private fun calculateOrientationFromAccelMag() {
        if (isAccelerometerSet && isMagnetometerSet) {
            val success = SensorManager.getRotationMatrix(rotationMatrix, null, lastAccelerometer, lastMagnetometer)
            if (success) {
                SensorManager.getOrientation(rotationMatrix, orientationAngles)
                var azimuth = Math.toDegrees(orientationAngles[0].toDouble()).toFloat()
                azimuth = (azimuth + 360f) % 360f

                val smoothed = QiblaCalculator.smoothDegree(_sensorState.value.heading, azimuth, 0.15f)

                _sensorState.value = _sensorState.value.copy(
                    heading = smoothed
                )
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        _sensorState.value = _sensorState.value.copy(
            accuracy = accuracy,
            needsCalibration = accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE || accuracy == SensorManager.SENSOR_STATUS_ACCURACY_LOW
        )
    }
}
