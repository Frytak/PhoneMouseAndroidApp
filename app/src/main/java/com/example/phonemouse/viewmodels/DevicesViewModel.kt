package com.example.phonemouse.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.phonemouse.PHONE_MOUSE_TAG
import com.example.phonemouse.models.Device
import com.example.phonemouse.models.Devices
import com.example.phonemouse.models.PacketMessage
import com.example.phonemouse.models.PhoneMouseState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.IOException

class DevicesViewModel : ViewModel() {
    private val devices = Devices()

    private val _connectedDevice = MutableStateFlow<Device?>(null)
    val connectedDevice: StateFlow<Device?> = _connectedDevice.asStateFlow()

    private val _detectedDevices = MutableStateFlow<List<Device>>(emptyList())
    val detectedDevices: StateFlow<List<Device>> = _detectedDevices.asStateFlow()

    private val _isDetectingDevices = MutableStateFlow<Boolean>(false)
    val isDetectingDevices: StateFlow<Boolean> = _isDetectingDevices.asStateFlow()

    private val _state = MutableStateFlow<PhoneMouseState>(PhoneMouseState.Idle)
    val state: StateFlow<PhoneMouseState> = _state.asStateFlow()

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
        if (_connectedDevice.value == null) return

        Thread {
            Log.d(PHONE_MOUSE_TAG, "Disconnecting from device...")

            _connectedDevice.value?.disconnect()
            _connectedDevice.value = null
        }.start()
    }

    fun connect(device: Device) {
        // TODO: Clone
        Thread {
            Log.d(PHONE_MOUSE_TAG, "Connecting to device...")
            val device = device.copy()
            device.connect()
            _connectedDevice.value = device;
        }.start()
    }

    fun setState(state: PhoneMouseState) {
        if (_connectedDevice.value == null) return

        Thread {
            try {
                _connectedDevice.value?.setState(state)
                _state.value = state
            } catch (err: IOException) {
                TODO("Handle error")
            }
        }.start()
    }

    fun sendTCPPacketMessage(packetMessage: PacketMessage) {
        if (_connectedDevice.value == null || packetMessage.packet is PhoneMouseState) return

        Thread {
            try {
                _connectedDevice.value?.sendTCPPacketMessage(packetMessage)
            } catch (err: IOException) {
                TODO("Handle error")
            }
        }.start()
    }

    fun sendUDPPacketMessage(packetMessage: PacketMessage) {
        if (_connectedDevice.value == null || packetMessage.packet is PhoneMouseState) return

        Thread {
            try {
                _connectedDevice.value?.sendUDPPacketMessage(packetMessage)
            } catch (err: IOException) {
                TODO("Handle error")
            }
        }.start()
    }
}