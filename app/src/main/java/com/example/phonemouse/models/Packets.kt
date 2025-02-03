package com.example.phonemouse.models

enum class Packets(val byte: Byte) {
    SwitchController(0),
    Gravity(1),
    Key(2),
    Touch(3);
}

enum class PhoneMouseState(val byte: Byte) {
    Idle(0),
    Gravity(1),
    Touchpad(2),
    Tablet(3);
}

class Gravity(val x: Float, val y: Float, val z: Float) {
    fun toBytes(): ByteArray {
        return byteArrayOf(x.toBits().toByte(), y.toBits().toByte(), z.toBits().toByte())
    }

    fun constructor(bytes: ByteArray) {
    }
}