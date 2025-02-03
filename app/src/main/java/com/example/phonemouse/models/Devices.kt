package com.example.phonemouse.models

import android.util.Log
import java.io.IOException
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.MulticastSocket
import java.net.Socket

class Device(var name: String, var address: InetAddress, var tcp_port: Int, var udp_port: Int) {
    var tcp_socket: Socket? = null
    var udp_socket: DatagramSocket? = null

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Device) return false

        return this.name == other.name
            && this.address == other.address
            && this.tcp_port == other.tcp_port
            && this.udp_port == other.udp_port
    }

    fun copy(): Device {
        return Device(this.name, this.address, this.tcp_port, this.udp_port)
    }

    fun isConnected() = tcp_socket != null && udp_socket != null

    fun connect() {
        if (this.isConnected()) { return; }
        tcp_socket = Socket(address, tcp_port)
        udp_socket = DatagramSocket(udp_port)
        Log.d("PhoneMouse", "Connected!")
        setState(PhoneMouseState.Idle)
    }

    fun disconnect() {
        tcp_socket?.let {
            it.close()
            tcp_socket = null
            Log.d("PhoneMouse", "Disconnected TCP")
        }

        udp_socket?.let {
            it.close()
            udp_socket = null
            Log.d("PhoneMouse", "Disconnected UDP")
        }
    }

    fun setState(state: PhoneMouseState) {
        if (!isConnected()) { return; }
        tcp_socket?.getOutputStream()?.write(byteArrayOf(0.toByte(), state.byte))
    }
}

class Devices {
    private fun listenForDevices(
        multicastSocket: MulticastSocket,
        detectedDevices: MutableList<Device>,
        running: Boolean,
        onDeviceDetected: ((detectedDevice: Device, detectedDevices: List<Device>) -> Unit)? = null
    ) {
        val packet = DatagramPacket(ByteArray(32), 32)

        while (running) {
            Log.d("PhoneMouse", "Listening for device responses")
            try {
                multicastSocket.receive(packet)
            } catch (err: IOException) {
                Log.w("PhoneMouse", "Receiving ping responses from devices interrupted. $err")
                return
            }

            val detectedDevice = Device(packet.address.hostName, packet.address, 2855, packet.port)
            // TODO: Check if the detected device is the current device
            if (detectedDevices.contains(detectedDevice)) { return; }

            Log.d("PhoneMouse", "Detected device: $detectedDevice")
            detectedDevices.add(detectedDevice)

            if (onDeviceDetected != null) {
                onDeviceDetected(detectedDevice, detectedDevices)
            }
        }
    }

    private fun sendOutPings(multicastSocket: MulticastSocket, pingCount: Int, interval: Long, socketAddress: InetSocketAddress) {
        for (i in 1..pingCount) {
            Log.d("PhoneMouse", "Sending out a multicast ping")
            multicastSocket.send(DatagramPacket("GTF".toByteArray(), 3, socketAddress))
            Thread.sleep(interval)
        }
    }

    fun detectDevices(port: Int = 2856, pingCount: Int = 5, interval: Long = 1000, onDeviceDetected: ((detectedDevice: Device, detectedDevices: List<Device>) -> Unit)? = null): List<Device> {
        Log.d("PhoneMouse", "Detecting devices")
        val detectedDevices: MutableList<Device> = mutableListOf()
        val multicastSocket = MulticastSocket()

        var running = true;
        Thread { listenForDevices(multicastSocket, detectedDevices, running, onDeviceDetected) }.start()

        // TODO: Get multicast address
        sendOutPings(multicastSocket, pingCount, interval, InetSocketAddress("192.168.200.255", port))
        running = false;
        multicastSocket.close()

        return detectedDevices
    }
}