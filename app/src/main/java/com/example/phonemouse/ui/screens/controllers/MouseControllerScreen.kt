package com.example.phonemouse.ui.screens.controllers

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.example.phonemouse.models.Gravity
import com.example.phonemouse.models.PacketIdentifier
import com.example.phonemouse.models.PacketMessage
import com.example.phonemouse.viewmodels.DevicesViewModel
import java.time.LocalDateTime
import kotlin.time.toKotlinDuration

@Composable
fun MouseControllerScreen(
    navController: NavController,
    devicesViewModel: DevicesViewModel,
) {
    // Get the current context and obtain the SensorManager service.
    val context = LocalContext.current
    val sensorManager = remember() {
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }

    // Get the default accelerometer sensor.
    val accelerometer = remember { sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) }

    // Hold the current acceleration values in state.
    val acceleration = remember { mutableStateOf(Triple(0f, 0f, 0f)) }

    // Set up the sensor listener in a DisposableEffect to register/unregister properly.
    DisposableEffect (sensorManager, accelerometer) {
        // Define a SensorEventListener to update the state when sensor values change.
        val sensorEventListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                event?.let {
                    // Update the state with the latest x, y, and z values.
                    val duration = java.time.Duration.between(devicesViewModel.connectedDevice.value?.connectionStartTime, LocalDateTime.now()).toKotlinDuration()
                    devicesViewModel.sendTCPPacketMessage(PacketMessage(PacketIdentifier.Gravity, Gravity(it.values[0], it.values[1], it.values[2])))
                    acceleration.value = Triple(it.values[0], it.values[1], it.values[2])
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) { }
        }

        // Register the listener. You can adjust the delay as needed.
        sensorManager.registerListener(
            sensorEventListener,
            accelerometer,
            SensorManager.SENSOR_DELAY_NORMAL
        )

        // Unregister the listener when the composable leaves the composition.
        onDispose {
            sensorManager.unregisterListener(sensorEventListener)
        }
    }

    Scaffold { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            Text(text = "X: ${acceleration.value.first}")
            Text(text = "Y: ${acceleration.value.second}")
            Text(text = "Z: ${acceleration.value.third}")
        }
    }
}