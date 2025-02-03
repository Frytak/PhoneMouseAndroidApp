package com.example.phonemouse.viewmodels

import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.phonemouse.models.Device
import com.example.phonemouse.models.Devices
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.reduce

class DevicesViewModel : ViewModel() {
    private val devices = Devices()

    private val _connectedDevice = MutableStateFlow<Device?>(null)
    val connectedDevice: StateFlow<Device?> = _connectedDevice.asStateFlow()

    private val _detectedDevices = MutableStateFlow<List<Device>>(emptyList())
    val detectedDevices: StateFlow<List<Device>> = _detectedDevices.asStateFlow()

    private val _isDetectingDevices = MutableStateFlow<Boolean>(false)
    val isDetectingDevices: StateFlow<Boolean> = _isDetectingDevices.asStateFlow()

    fun detectDevices(port: Int = 2856) {
        if (_isDetectingDevices.value) { return; }
        _isDetectingDevices.value = true;

        Thread {
            _detectedDevices.value = devices.detectDevices(2856) { detectedDevice, detectedDevices ->
                // Remember to create a new list
                _detectedDevices.value = detectedDevices.toList()
            }

            _isDetectingDevices.value = false;
        }.start()
    }

    fun disconnect() {
        Thread {
            if (_connectedDevice.value == null) {
                return@Thread
            }
            Log.d("PhoneMouse", "Disconnecting from device...")
            _connectedDevice.value?.disconnect()
            _connectedDevice.value = null
        }.start()
    }

    fun connect(device: Device) {
        // TODO: Clone
        Thread {
            Log.d("PhoneMouse", "Connecting to device...")
            val device = device.copy()
            device.connect()
            _connectedDevice.value = device;
        }.start()
    }
}