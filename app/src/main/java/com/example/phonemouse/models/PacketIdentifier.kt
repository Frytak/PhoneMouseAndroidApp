package com.example.phonemouse.models
import android.util.Log
import com.example.phonemouse.PHONE_MOUSE_TAG
import com.example.phonemouse.models.PhoneMouseStateIdentifier.Idle
import com.example.phonemouse.models.PhoneMouseStateIdentifier.Mouse
import com.example.phonemouse.models.PhoneMouseStateIdentifier.Tablet
import com.example.phonemouse.models.PhoneMouseStateIdentifier.Touchpad
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.time.Duration
import java.time.Instant
import kotlin.math.absoluteValue
import kotlin.time.toKotlinDuration

interface Packet {
    fun toBytes(): ByteArray

    fun fromBytes(bytes: ByteBuffer): Packet
    fun fromBytes(bytes: ByteArray): Packet {
        return this.fromBytes(ByteBuffer.wrap(bytes))
    }
}

class PacketMessage constructor(val packet: Packet, val timeDelta: Duration = java.time.Duration.between(Instant.EPOCH, Instant.now()).toKotlinDuration()): Packet {
    override fun toBytes(): ByteArray {
        val duration = timeDelta.toComponents { seconds, nanoseconds ->
            ByteBuffer.allocate(Long.SIZE_BYTES + Int.SIZE_BYTES)
                .order(ByteOrder.LITTLE_ENDIAN)
                .putLong(seconds)
                .putInt(nanoseconds.absoluteValue)
        }

        return duration.array() + this.packet.toBytes()
    }

    override fun fromBytes(bytes: ByteBuffer): PacketMessage {
        val timeDelta = java.time.Duration.ofSeconds(bytes.getLong(), bytes.getInt().toLong()).toKotlinDuration()
        val identifier = PacketIdentifier.fromBytes(bytes)
        val packet = when (identifier) {
            PacketIdentifier.SwitchController -> PhoneMouseStateIdentifier.fromBytes(bytes)
            PacketIdentifier.Gravity -> Gravity.fromBytes(bytes)
            PacketIdentifier.Key -> TODO("Key packet is not yet implemented")
            PacketIdentifier.Touch -> TouchPoint.fromBytes(bytes)
            PacketIdentifier.Ping -> Ping.fromBytes(bytes)
        }

        return PacketMessage(packet, timeDelta)
    }
}

enum class PacketIdentifier(val byte: Byte): Packet {
    SwitchController(0),
    Gravity(1),
    Key(2),
    Touch(3),
    Ping(4);

    override fun toBytes(): ByteArray {
        return byteArrayOf(this.byte)
    }

    override fun fromBytes(bytes: ByteBuffer): PacketIdentifier {
        return Companion.fromBytes(bytes)
    }

    companion object {
        val SIZE_BYTES = Byte.SIZE_BYTES
        val SIZE_BITS = SIZE_BYTES * 8

        fun fromBytes(bytes: ByteBuffer): PacketIdentifier {
            val byte = bytes.get()

            return when (byte) {
                0.toByte() -> SwitchController
                1.toByte() -> Gravity
                2.toByte() -> Key
                3.toByte() -> Touch
                4.toByte() -> Ping
                else -> {
                    val error = "Invalid `Packets`. Got byte `${byte.toString()}`"
                    Log.e(PHONE_MOUSE_TAG, error)
                    throw Error(error)
                }
            }
        }
    }
}

enum class PhoneMouseStateIdentifier(val byte: Byte): Packet {
    Idle(0),
    Gravity(1),
    Touchpad(2),
    Tablet(3),
    Mouse(4);

    override fun toBytes(): ByteArray {
        return ByteBuffer
            .allocate(PhoneMouseStateIdentifier.SIZE_BYTES)
            .order(ByteOrder.LITTLE_ENDIAN)
            .put(PacketIdentifier.SwitchController.toBytes())
            .put(byte)
            .array()
    }

    override fun fromBytes(bytes: ByteBuffer): PhoneMouseStateIdentifier {
        return Companion.fromBytes(bytes)
    }

    companion object {
        val SIZE_BYTES = PacketIdentifier.SIZE_BYTES + Byte.SIZE_BYTES
        val SIZE_BITS = SIZE_BYTES * 8

        fun fromBytes(bytes: ByteBuffer): PhoneMouseStateIdentifier {
            val byte = bytes.get()

            return when (byte) {
                0.toByte() -> Idle
                1.toByte() -> Gravity
                2.toByte() -> Touchpad
                3.toByte() -> Tablet
                4.toByte() -> Mouse
                else -> {
                    val error = "Invalid PhoneMouseState. Got byte `${byte.toString()}`"
                    Log.e(PHONE_MOUSE_TAG, error)
                    throw Error(error)
                }
            }
        }
    }
}

class Area(val x: UShort, val y: UShort, val width: UShort, val height: UShort): Packet {
    override fun toBytes(): ByteArray {
        return ByteBuffer
            .allocate(SIZE_BYTES)
            .order(ByteOrder.LITTLE_ENDIAN)
            .putShort(x.toShort())
            .putShort(y.toShort())
            .putShort(width.toShort())
            .putShort(height.toShort())
            .array()
    }

    override fun fromBytes(bytes: ByteBuffer): Area {
        return Companion.fromBytes(bytes)
    }

    companion object {
        val SIZE_BYTES = UShort.SIZE_BYTES * 4
        val SIZE_BITS = SIZE_BYTES * 8

        fun fromBytes(bytes: ByteBuffer): Area {
            return Area(bytes.getShort().toUShort(), bytes.getShort().toUShort(), bytes.getShort().toUShort(), bytes.getShort().toUShort())
        }
    }
}

class TouchpadArea(val all: Area, val touchOnly: Area, val buttonLeft: Area, val buttonRight: Area): Packet {
    override fun toBytes(): ByteArray {
        return ByteBuffer
            .allocate(SIZE_BYTES)
            .order(ByteOrder.LITTLE_ENDIAN)
            .put(all.toBytes())
            .put(touchOnly.toBytes())
            .put(buttonLeft.toBytes())
            .put(buttonRight.toBytes())
            .array()
    }

    override fun fromBytes(bytes: ByteBuffer): TouchpadArea {
        return Companion.fromBytes(bytes)
    }

    companion object {
        val SIZE_BYTES = Area.SIZE_BYTES * 4
        val SIZE_BITS = SIZE_BYTES * 8

        fun fromBytes(bytes: ByteBuffer): TouchpadArea {
            return TouchpadArea(
                Area.fromBytes(bytes),
                Area.fromBytes(bytes),
                Area.fromBytes(bytes),
                Area.fromBytes(bytes)
            )
        }
    }
}

class TouchpadState(val touchpadArea: TouchpadArea): Packet {
    override fun toBytes(): ByteArray {
        return ByteBuffer
            .allocate(Byte.SIZE_BYTES + Byte.SIZE_BYTES)
            .order(ByteOrder.LITTLE_ENDIAN)
            .put(PhoneMouseStateIdentifier.Touchpad.toBytes())
            .put(touchpadArea.toBytes())
            .array()
    }

    override fun fromBytes(bytes: ByteBuffer): TouchpadState {
        return Companion.fromBytes(bytes)
    }

    companion object {
        fun fromBytes(bytes: ByteBuffer): TouchpadState {
            return TouchpadState(TouchpadArea.fromBytes(bytes))
        }
    }
}

class Ping: Packet {
    override fun toBytes(): ByteArray {
        return byteArrayOf()
    }

    override fun fromBytes(bytes: ByteBuffer): Packet {
        return Ping()
    }

    companion object {
        val SIZE_BYTES = 0
        val SIZE_BITS = SIZE_BYTES * 8

        fun fromBytes(bytes: ByteBuffer): Packet {
            return Ping()
        }
    }
}

class Gravity(val x: Float, val y: Float, val z: Float): Packet {
    override fun toBytes(): ByteArray {
        return ByteBuffer.allocate(Byte.SIZE_BYTES + Gravity.SIZE_BYTES)
            .order(ByteOrder.LITTLE_ENDIAN)
            .put(PacketIdentifier.Gravity.toBytes())
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
        val SIZE_BITS = SIZE_BYTES * 8

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

class TouchPoint(val id: Long, val x: Float, val y: Float): Packet {
    override fun toBytes(): ByteArray {
        return ByteBuffer.allocate(TouchPoint.SIZE_BYTES)
            .order(ByteOrder.LITTLE_ENDIAN)
            .putLong(id)
            .putFloat(x)
            .putFloat(y)
            .array()
    }

    override fun fromBytes(bytes: ByteBuffer): Packet {
        return Companion.fromBytes(bytes)
    }

    companion object {
        val SIZE_BYTES = Long.SIZE_BYTES + Float.SIZE_BYTES * 2
        val SIZE_BITS = Long.SIZE_BITS + Float.SIZE_BITS * 2

        fun fromBytes(bytes: ByteBuffer): Packet {
            bytes.order(ByteOrder.LITTLE_ENDIAN)
            return TouchPoint(
                bytes.getLong(),
                bytes.getFloat(),
                bytes.getFloat()
            )
        }
    }
}

class TouchPoints(val touchPoints: List<TouchPoint>): Packet {
    override fun toBytes(): ByteArray {
        val bytes = ByteBuffer
            .allocate(Byte.SIZE_BYTES + Byte.SIZE_BYTES + TouchPoint.SIZE_BYTES * touchPoints.size)
            .order(ByteOrder.LITTLE_ENDIAN)
            .put(PacketIdentifier.Touch.toBytes())
            .put(touchPoints.size.toByte())

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

enum class Key(var pressed: Boolean = false): Packet {
    BTN_LEFT,
    BTN_RIGHT;

    override fun toBytes(): ByteArray {
        val bytes = ByteBuffer
            .allocate(Byte.SIZE_BYTES + Short.SIZE_BYTES + Byte.SIZE_BYTES)
            .order(ByteOrder.LITTLE_ENDIAN)
            .put(PacketIdentifier.Key.toBytes())

        val value = when (this) {
            BTN_LEFT -> 272
            BTN_RIGHT -> 273
        }
        bytes.putShort(value.toShort()).put(if (pressed) { 1 } else { 0 })

        return bytes.array();
    }

    override fun fromBytes(bytes: ByteBuffer): Packet {
        return Companion.fromBytes(bytes)
    }

    companion object {
        fun fromBytes(bytes: ByteBuffer): Packet {
            bytes.order(ByteOrder.LITTLE_ENDIAN)
            val value = bytes.getShort().toInt()
            val pressed = bytes.get() > 0

            var key = when (value) {
                272 -> BTN_LEFT
                273 -> BTN_RIGHT
                else -> throw Error("Invalid key")
            };

            key.pressed = pressed

            return key
        }
    }
}
