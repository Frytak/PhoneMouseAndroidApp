package com.example.phonemouse.models

import android.util.Log
import androidx.compose.runtime.Composable
import com.example.phonemouse.PHONE_MOUSE_TAG
import java.nio.ByteBuffer
import java.nio.ByteOrder

interface Packet {
    fun toBytes(): ByteArray

    fun fromBytes(bytes: ByteBuffer): Packet
    fun fromBytes(bytes: ByteArray): Packet {
        return this.fromBytes(ByteBuffer.wrap(bytes))
    }
}

class PacketMessage(val identifier: PacketIdentifier, val packet: Packet): Packet {

    override fun toBytes(): ByteArray {
        return this.identifier.toBytes() + this.packet.toBytes()
    }

    override fun fromBytes(bytes: ByteBuffer): PacketMessage {
        val identifier = PacketIdentifier.fromBytes(bytes)
        val packet = when (identifier) {
            PacketIdentifier.SwitchController -> PhoneMouseState.fromBytes(bytes)
            PacketIdentifier.Gravity -> Gravity.fromBytes(bytes)
            PacketIdentifier.Key -> TODO("Key packet is not yet implemented")
            PacketIdentifier.Touch -> TODO("Touch packet is not yet implemented")
        }

        return PacketMessage(identifier, packet)
    }
}

enum class PacketIdentifier(val byte: Byte): Packet {
    SwitchController(0),
    Gravity(1),
    Key(2),
    Touch(3);

    override fun toBytes(): ByteArray {
        return byteArrayOf(this.byte)
    }

    override fun fromBytes(bytes: ByteBuffer): PacketIdentifier {
        return Companion.fromBytes(bytes)
    }

    companion object {
        fun fromBytes(bytes: ByteBuffer): PacketIdentifier {
            val byte = bytes.get()

            return when (byte) {
                0.toByte() -> PacketIdentifier.SwitchController
                1.toByte() -> PacketIdentifier.Gravity
                2.toByte() -> PacketIdentifier.Key
                3.toByte() -> PacketIdentifier.Touch
                else -> {
                    val error = "Invalid `Packets`. Got byte `${byte.toString()}`"
                    Log.e(PHONE_MOUSE_TAG, error)
                    throw Error(error)
                }
            }
        }
    }
}

enum class PhoneMouseState(val byte: Byte): Packet {
    Idle(0),
    Gravity(1),
    Touchpad(2),
    Tablet(3);

    override fun toBytes(): ByteArray {
        return byteArrayOf(this.byte)
    }

    override fun fromBytes(bytes: ByteBuffer): PhoneMouseState {
        return Companion.fromBytes(bytes)
    }

    companion object {
        fun fromBytes(bytes: ByteBuffer): PhoneMouseState {
            val byte = bytes.get()

            return when (byte) {
                0.toByte() -> PhoneMouseState.Idle
                1.toByte() -> PhoneMouseState.Gravity
                2.toByte() -> PhoneMouseState.Touchpad
                3.toByte() -> PhoneMouseState.Tablet
                else -> {
                    val error = "Invalid PhoneMouseState. Got byte `${byte.toString()}`"
                    Log.e(PHONE_MOUSE_TAG, error)
                    throw Error(error)
                }
            }
        }
    }
}

class Gravity(val x: Float, val y: Float, val z: Float): Packet {
    override fun toBytes(): ByteArray {
        return ByteBuffer.allocate(Float.SIZE_BYTES * 3)
            .order(ByteOrder.LITTLE_ENDIAN)
            .putFloat(x)
            .putFloat(y)
            .putFloat(z)
            .array()
    }

    override fun fromBytes(bytes: ByteBuffer): Packet {
        return Companion.fromBytes(bytes)
    }

    companion object {
        fun fromBytes(bytes: ByteBuffer): Packet {
            bytes.order(ByteOrder.LITTLE_ENDIAN)
            return Gravity(
                bytes.getFloat(),
                bytes.getFloat(),
                bytes.getFloat()
            )
        }
    }
}