package com.example.sensores

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import android.widget.Button
import android.media.MediaPlayer

class MainActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var gyroscope: Sensor? = null
    private lateinit var statusText: TextView
    private lateinit var restartButton: Button
    private var isDeviceStable = true

    private lateinit var stableSound: MediaPlayer
    private lateinit var movementSound: MediaPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inicializa los elementos de la interfaz
        statusText = findViewById(R.id.statusText)
        restartButton = findViewById(R.id.restartButton)

        // Carga los sonidos desde res/raw con manejo de excepciones
        try {
            stableSound = MediaPlayer.create(this, R.raw.despacito)
            movementSound = MediaPlayer.create(this, R.raw.cancion2)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Inicializa el sensor manager y los sensores
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

        // Configura el botón de reinicio
        restartButton.setOnClickListener {
            resetDetection()
        }
    }

    override fun onResume() {
        super.onResume()
        accelerometer?.also { sensor ->
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
        }
        gyroscope?.also { sensor ->
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            when (it.sensor.type) {
                Sensor.TYPE_ACCELEROMETER -> handleAccelerometerData(it)
                Sensor.TYPE_GYROSCOPE -> handleGyroscopeData(it)
            }
        }
    }

    private fun handleAccelerometerData(event: SensorEvent) {
        val (x, y, z) = event.values
        isDeviceStable = x in -0.5..0.5 && y in -0.5..0.5 && z in 9.5..10.5
        updateStatus()
    }

    private fun handleGyroscopeData(event: SensorEvent) {
        val rotationRate = event.values[2]
        if (rotationRate > 2.0) {
            isDeviceStable = false
            updateStatus()
        }
    }

    private fun updateStatus() {
        if (isDeviceStable) {
            statusText.text = "Se mueve? , nop"

            // Detener el sonido de movimiento si está en reproducción
            if (movementSound.isPlaying) {
                movementSound.stop()
                movementSound.prepare() // Listo para reproducirse de nuevo en caso necesario
            }

            // Reproducir sonido de estabilidad
            if (!stableSound.isPlaying) {
                stableSound.start()
            }
        } else {
            statusText.text = "Se mueve? , sip"

            // Detener el sonido de estabilidad si está en reproducción
            if (stableSound.isPlaying) {
                stableSound.stop()
                stableSound.prepare() // Listo para reproducirse de nuevo en caso necesario
            }

            // Reproducir sonido de movimiento
            if (!movementSound.isPlaying) {
                movementSound.start()
            }
        }
    }


    private fun resetDetection() {
        isDeviceStable = true
        statusText.text = "Estado: Estable"
        if (stableSound.isPlaying) stableSound.stop()
        stableSound.prepare()
        if (movementSound.isPlaying) movementSound.stop()
        movementSound.prepare()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Liberar recursos de MediaPlayer al cerrar la app
        stableSound.release()
        movementSound.release()
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
