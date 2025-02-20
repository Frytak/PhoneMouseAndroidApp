package com.example.phonemouse.models

import android.util.Log
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
            PacketIdentifier.Touch -> TouchPoint.fromBytes(bytes)
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
                0.toByte() -> SwitchController
                1.toByte() -> Gravity
                2.toByte() -> Key
                3.toByte() -> Touch
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
                0.toByte() -> Idle
                1.toByte() -> Gravity
                2.toByte() -> Touchpad
                3.toByte() -> Tablet
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
        return ByteBuffer.allocate(Gravity.SIZE_BYTES)
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
        val SIZE_BYTES = Float.SIZE_BYTES * 3
        val SIZE_BITS = Float.SIZE_BITS * 3

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

class TouchPoint(val x: Float, val y: Float): Packet {
    override fun toBytes(): ByteArray {
        return ByteBuffer.allocate(TouchPoint.SIZE_BYTES)
            .order(ByteOrder.LITTLE_ENDIAN)
            .putFloat(x)
            .putFloat(y)
            .array()
    }

    override fun fromBytes(bytes: ByteBuffer): Packet {
        return Companion.fromBytes(bytes)
    }

    companion object {
        val SIZE_BYTES = Float.SIZE_BYTES * 2
        val SIZE_BITS = Float.SIZE_BITS * 2

        fun fromBytes(bytes: ByteBuffer): Packet {
            bytes.order(ByteOrder.LITTLE_ENDIAN)
            return TouchPoint(
                bytes.getFloat(),
                bytes.getFloat()
            )
        }
    }
}

class TouchPoints(val touchPoints: List<TouchPoint>): Packet {
    override fun toBytes(): ByteArray {
        val bytes = ByteBuffer.allocate(Int.SIZE_BYTES + TouchPoint.SIZE_BYTES * touchPoints.size).order(ByteOrder.LITTLE_ENDIAN)
        bytes.put(touchPoints.size.toByte())

        for (touchPoint in touchPoints) {
            bytes.put(touchPoint.toBytes())
        }

        return bytes.array();
    }

    override fun fromBytes(bytes: ByteBuffer): Packet {
        return Companion.fromBytes(bytes)
    }

    companion object {
        fun fromBytes(bytes: ByteBuffer): Packet {
            bytes.order(ByteOrder.LITTLE_ENDIAN)
            val length = bytes.get().toInt()

            val touchPoints: MutableList<TouchPoint> = mutableListOf()

            for (i in 0..length/TouchPoint.SIZE_BYTES) {
                val touchPoint = ByteArray(TouchPoint.SIZE_BYTES)
                bytes.get(touchPoint, i*TouchPoint.SIZE_BYTES, TouchPoint.SIZE_BYTES)
            }

            return TouchPoints(touchPoints)
        }
    }
}

enum class Key: Packet {
    BTN_LEFT,
    BTN_RIGHT;

    override fun toBytes(): ByteArray {
        val bytes = ByteBuffer.allocate(Short.SIZE_BYTES).order(ByteOrder.LITTLE_ENDIAN)

        val value = when (this) {
            BTN_LEFT -> 272
            BTN_RIGHT -> 273
        }
        bytes.putShort(value.toShort())

        return bytes.array();
    }

    override fun fromBytes(bytes: ByteBuffer): Packet {
        return Companion.fromBytes(bytes)
    }

    companion object {
        fun fromBytes(bytes: ByteBuffer): Packet {
            bytes.order(ByteOrder.LITTLE_ENDIAN)
            val value = bytes.getShort().toInt()

            return when (value) {
                272 -> BTN_LEFT
                273 -> BTN_RIGHT
                else -> throw Error("Invalid key")
            }
        }
    }
}