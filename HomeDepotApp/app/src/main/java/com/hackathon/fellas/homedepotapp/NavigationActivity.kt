package com.hackathon.fellas.homedepotapp

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.support.annotation.RequiresApi
import android.support.v7.app.AppCompatActivity
import android.util.Log
import kotlinx.android.synthetic.main.activity_navigation.*
import java.lang.Math.abs
import java.util.*


class NavigationActivity : AppCompatActivity(), SensorEventListener, TextToSpeech.OnInitListener {
    override fun onInit(p0: Int) {}

    private var stepsAtChange = 0
    private var steps = 0

    private lateinit var textToSpeech: TextToSpeech

    override fun onSensorChanged(event: SensorEvent?) {
        if (event != null) {
            steps = event.values[0].toInt()
            if (direction != Direction.FORWARD) {
                val stepsChange = steps - stepsAtChange
                if (stepsChange > 2) {
                    direction = Direction.FORWARD
                    arrowImageView?.setImageResource(R.drawable.arrow_forward)
                }
            }
        }
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
        // Empty
    }

    private var direction: Direction? = null

    enum class Direction {
        RIGHT, LEFT, FORWARD
    }

    private val sensorManager: SensorManager by lazy { (getSystemService(Context.SENSOR_SERVICE) as SensorManager?)!! }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        textToSpeech = TextToSpeech(this, this)
        setContentView(R.layout.activity_navigation)

        textToSpeech.language = Locale.US

        navigator.startNavigation()
        val stepCounter = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        sensorManager.registerListener(this, stepCounter, SensorManager.SENSOR_DELAY_NORMAL)

        if (checkSelfPermission(Manifest.permission.BLUETOOTH)
                == PackageManager.PERMISSION_GRANTED
                && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            startBluetoothScan()
            Log.v("PERMS", "Bluetooth permission granted")
        } else {
            Log.e("PERMS", "Permission not granted for bluetooth")
        }
    }

    private lateinit var bluetoothAdapter: BluetoothAdapter

    private var navigator = NavigationMap { inst, degrees ->
        instructions.text = inst
        if (degrees != null) {
            when {
                abs(degrees) <= 15 -> arrowImageView?.setImageResource(R.drawable.arrow_forward)
                degrees > 0 -> {
                    arrowImageView?.setImageResource(R.drawable.arrow_left)
                    stepsAtChange = steps
                    direction = Direction.LEFT
                }
                degrees < 0 -> {
                    arrowImageView?.setImageResource(R.drawable.arrow_right)
                    stepsAtChange = steps
                    direction = Direction.RIGHT
                }
            }
        }
        @Suppress("DEPRECATION")
        textToSpeech.speak(inst, TextToSpeech.QUEUE_ADD, null)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun startBluetoothScan() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        @Suppress("DEPRECATION")
        bluetoothAdapter.startLeScan { device, signalStrength, _ ->
            if (device.name != null && device.name.contains("Beacon")) {
                val beaconId = Character.getNumericValue(
                        device.name[device.name.indexOf("Beacon ") + "Beacon ".length]
                ) - 1
                navigator.updateSignalReading(beaconId, signalStrength)
            }
        }
    }

}