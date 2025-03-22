package com.example.phonemouse.models

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.phonemouse.PHONE_MOUSE_TAG
import java.io.IOException
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.Inet4Address
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.MulticastSocket
import java.net.NetworkInterface
import java.net.Socket
import java.time.LocalDateTime

class Device(var name: String, var address: InetAddress, var tcpPort: Int, var udpPort: Int) {
    var tcpSocket: Socket? = null
    var udpSocket: DatagramSocket? = null
    var connectionStartTime: LocalDateTime? = null

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Device) return false

        return this.name == other.name
            && this.address == other.address
            && this.tcpPort == other.tcpPort
            && this.udpPort == other.udpPort
    }

    fun copy(): Device {
        return Device(this.name, this.address, this.tcpPort, this.udpPort)
    }

    fun isConnected() = tcpSocket != null && udpSocket != null

    @RequiresApi(Build.VERSION_CODES.O)
    fun connect() {
        if (this.isConnected()) { return; }
        tcpSocket = Socket(address, tcpPort)
        udpSocket = DatagramSocket(udpPort)

        Log.d(PHONE_MOUSE_TAG, "Connected!")
        connectionStartTime = LocalDateTime.now()
        setState(PhoneMouseState.Idle)
    }

    fun disconnect() {
        connectionStartTime = null

        tcpSocket?.let {
            it.close()
            tcpSocket = null
            Log.d(PHONE_MOUSE_TAG, "Disconnected TCP")
        }

        udpSocket?.let {
            it.close()
            udpSocket = null
            Log.d(PHONE_MOUSE_TAG, "Disconnected UDP")
        }
    }

    fun sendTCPPacketMessage(packetMessage: PacketMessage) {
        if (!isConnected()) { return; }

        val packetMessageBytes = packetMessage.toBytes().asList().toMutableList()
        packetMessageBytes.add(0, 1)
        Log.d(PHONE_MOUSE_TAG, "Sending TCP packet `${packetMessageBytes}`")

        tcpSocket?.getOutputStream()?.write(packetMessageBytes.toByteArray())
    }

    fun sendUDPPacketMessage(packetMessage: PacketMessage) {
        if (!isConnected()) { return; }

        val packetMessageBytes = packetMessage.toBytes()
        //Log.d(PHONE_MOUSE_TAG, "Sending UDP packet `${packetMessageBytes}`")

        udpSocket?.send(DatagramPacket(packetMessageBytes, packetMessageBytes.size, address, udpPort))
    }

    fun setState(state: PhoneMouseState) {
        sendTCPPacketMessage(PacketMessage(PacketIdentifier.SwitchController, state))
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
            Log.d(PHONE_MOUSE_TAG, "Listening for device responses")
            try {
                multicastSocket.receive(packet)
            } catch (err: IOException) {
                Log.w(PHONE_MOUSE_TAG, "Receiving ping responses from devices interrupted. $err")
                return
            }

            val detectedDevice = Device(packet.address.hostName, packet.address, 2855, packet.port)
            // TODO: Check if the detected device is the current device
            if (detectedDevices.contains(detectedDevice)) { return; }

            Log.d(PHONE_MOUSE_TAG, "Detected device: $detectedDevice")
            detectedDevices.add(detectedDevice)

            if (onDeviceDetected != null) {
                onDeviceDetected(detectedDevice, detectedDevices)
            }
        }
    }

    private fun sendOutPings(multicastSocket: MulticastSocket, pingCount: Int, interval: Long, socketAddress: InetSocketAddress) {
        for (i in 1..pingCount) {
            Log.d(PHONE_MOUSE_TAG, "Sending out a multicast ping")
            multicastSocket.send(DatagramPacket(byteArrayOf(4), 1, socketAddress))
            Thread.sleep(interval)
        }
    }

    fun detectDevices(port: Int = 2856, pingCount: Int = 5, interval: Long = 1000, onDeviceDetected: ((detectedDevice: Device, detectedDevices: List<Device>) -> Unit)? = null): List<Device> {
        Log.d(PHONE_MOUSE_TAG, "Detecting devices")
        val detectedDevices: MutableList<Device> = mutableListOf()

        val threadPool: MutableList<Thread> = mutableListOf()
        for (networkInterface in NetworkInterface.getNetworkInterfaces()) {
            if (networkInterface.supportsMulticast()) {
                val thread = Thread {
                    val multicastSocket = MulticastSocket()

                    var running = true
                    Thread { listenForDevices(multicastSocket, detectedDevices, running, onDeviceDetected) }.start()

                    // TODO: Calculate the multicast address from the mask
                    val multicastAddress = networkInterface.inetAddresses.toList().filter { it is Inet4Address }[0].address;
                    multicastAddress.set(3, 255.toByte())

                    sendOutPings(multicastSocket, pingCount, interval, InetSocketAddress(InetAddress.getByAddress(multicastAddress), port))
                    running = false
                    multicastSocket.close()
                }

                threadPool.add(thread)
                thread.start()
            }
        }

        var anyRunning = false;
        do {
            anyRunning = false;
            for (thread in threadPool) {
                if (thread.isAlive) {
                    anyRunning = true;
                }
            }
        } while (anyRunning)

        return detectedDevices
    }
}