package com.example.phonemouse.ui.screens.controllers

import android.content.res.Configuration
import android.util.Log
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.phonemouse.PHONE_MOUSE_TAG
import com.example.phonemouse.models.Key
import com.example.phonemouse.models.PacketIdentifier
import com.example.phonemouse.models.PacketMessage
import com.example.phonemouse.models.TouchPoint
import com.example.phonemouse.models.TouchPoints
import com.example.phonemouse.viewmodels.DevicesViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Instant
import kotlin.time.Duration
import kotlin.time.toKotlinDuration

fun buttonClick(view: View, devicesViewModel: DevicesViewModel, button: Key) {
    view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
    button.pressed = true;
    devicesViewModel.sendTCPPacketMessage(PacketMessage(PacketIdentifier.Key, button))
    button.pressed = false;
    devicesViewModel.sendTCPPacketMessage(PacketMessage(PacketIdentifier.Key, button))
}

data class ExtendedTouchPoint(val touchPoint: TouchPoint, val first_x: Float, val first_y: Float, var held: Key? = null, val created: Duration = java.time.Duration.between(Instant.EPOCH, Instant.now()).toKotlinDuration())

enum class AreaType {
    Area,
    ButtonLeft,
    ButtonRight;

    companion object {
        fun areaType(scaffoldSize: IntSize, buttonsSize: IntSize, x: Float, y: Float): AreaType {
            // Button
            if (scaffoldSize.height > y && y > scaffoldSize.height - buttonsSize.height) {
                // Left
                if (x < buttonsSize.width / 2) {
                    return ButtonLeft
                    // Right
                } else {
                    return ButtonRight
                }
                // Touchpad area click
            } else {
                return Area
            }
        }
    }
}

@Composable
fun TouchpadControllerScreen(
    navController: NavController,
    devicesViewModel: DevicesViewModel,
) {
    val configuration = LocalConfiguration.current
    val view = LocalView.current

    var scaffoldSize by remember { mutableStateOf<IntSize?>(null) }
    var buttonsSize by remember { mutableStateOf<IntSize?>(null) }

    val coroutineScope = rememberCoroutineScope()
    var touchPointTimers: HashMap<Long, Job> = hashMapOf()

    Scaffold(Modifier
        .onSizeChanged { size -> scaffoldSize = size }
        .pointerInput(Unit) {
            awaitEachGesture {
                var previousTouchPoints: HashMap<Long, ExtendedTouchPoint> = hashMapOf()
                while (true) {
                    val touchPoints: HashMap<Long, ExtendedTouchPoint> = hashMapOf()
                    val event = awaitPointerEvent()

                    event.changes.forEach {
                        if (it.pressed) {
                            val touchPoint = TouchPoint(it.id.value, it.position.x, it.position.y)
                            if (previousTouchPoints.containsKey(it.id.value)) {
                                touchPoints[it.id.value] = ExtendedTouchPoint(
                                    touchPoint,
                                    previousTouchPoints[it.id.value]!!.first_x,
                                    previousTouchPoints[it.id.value]!!.first_y,
                                    previousTouchPoints[it.id.value]!!.held,
                                    previousTouchPoints[it.id.value]!!.created
                                )
                            } else {
                                touchPoints[it.id.value] =
                                    ExtendedTouchPoint(touchPoint, it.position.x, it.position.y)
                                touchPointTimers[it.id.value] = coroutineScope.launch {
                                    val touchPoint = touchPoints[it.id.value]!!

                                    val elapsedTime = java.time.Duration.between(Instant.EPOCH, Instant.now()).toKotlinDuration() - touchPoint.created
                                    if (elapsedTime.inWholeMilliseconds < 750) {
                                        Log.d(PHONE_MOUSE_TAG, "Delay...")
                                        delay(1000 - elapsedTime.inWholeMilliseconds)
                                        Log.d(PHONE_MOUSE_TAG, "BACK!")
                                    }

                                    Log.d(PHONE_MOUSE_TAG, "Checking hold area")

                                    val first_area = AreaType.areaType(
                                        scaffoldSize ?: IntSize(0, 0),
                                        buttonsSize ?: IntSize(0, 0),
                                        touchPoint.first_x,
                                        touchPoint.first_y
                                    )
                                    val current_area = AreaType.areaType(
                                        scaffoldSize ?: IntSize(0, 0),
                                        buttonsSize ?: IntSize(0, 0),
                                        touchPoint.touchPoint.x,
                                        touchPoint.touchPoint.y
                                    )

                                    if (first_area != current_area) {
                                        Log.d(PHONE_MOUSE_TAG, "Area doesn't match " + first_area + " " + current_area)
                                        return@launch
                                    }
                                    var key: Key = Key.BTN_LEFT
                                    when (first_area) {
                                        AreaType.Area -> return@launch
                                        AreaType.ButtonLeft -> key = Key.BTN_LEFT
                                        AreaType.ButtonRight -> key = Key.BTN_RIGHT
                                    }
                                    key.pressed = true

                                    Log.d(PHONE_MOUSE_TAG, "HOLD! " + key.name)
                                    touchPoint.held = key;
                                    devicesViewModel.sendTCPPacketMessage(
                                        PacketMessage(
                                            PacketIdentifier.Key,
                                            key
                                        )
                                    )
                                }
                            }
                            it.consume()
                        }
                    }

                    //Log.d(PHONE_MOUSE_TAG, "[")
                    //touchPoints.forEach { _, it -> Log.d(PHONE_MOUSE_TAG, "{ id: ${it.touchPoint.id}, x: ${it.touchPoint.x}, y: ${it.touchPoint.y}, created: ${it.created} } ") }
                    //Log.d(PHONE_MOUSE_TAG, "]")

                    val timeDelta = java.time.Duration
                        .between(Instant.EPOCH, Instant.now())
                        .toKotlinDuration()

                    previousTouchPoints.forEach { _, previousTouchPoint ->
                        if (!touchPoints.contains(previousTouchPoint.touchPoint.id)) {
                            val touchPoint = touchPoints[previousTouchPoint.touchPoint.id]

                            if (previousTouchPoint.held != null) {
                                previousTouchPoint.held!!.pressed = false
                                devicesViewModel.sendTCPPacketMessage(PacketMessage(PacketIdentifier.Key, previousTouchPoint.held!!))
                            } else if ((timeDelta.inWholeMilliseconds - previousTouchPoint.created.inWholeMilliseconds) < 200) {
                                // TODO: Change this to only detect the click and set of haptic feedback if the click was not on a button

                                when (AreaType.areaType(
                                    scaffoldSize ?: IntSize(0, 0),
                                    buttonsSize ?: IntSize(0, 0),
                                    previousTouchPoint.touchPoint.x,
                                    previousTouchPoint.touchPoint.y
                                )) {
                                    AreaType.Area -> buttonClick(
                                        view,
                                        devicesViewModel,
                                        Key.BTN_LEFT
                                    )

                                    AreaType.ButtonLeft -> buttonClick(
                                        view,
                                        devicesViewModel,
                                        Key.BTN_LEFT
                                    )

                                    AreaType.ButtonRight -> buttonClick(
                                        view,
                                        devicesViewModel,
                                        Key.BTN_RIGHT
                                    )
                                }
                            }
                        }
                    }

                    val filteredTouchPoints = touchPoints.values.filter { it.held == null }
                    if (!filteredTouchPoints.isEmpty() || touchPoints.isEmpty()) {
                        devicesViewModel.sendTCPPacketMessage(
                            PacketMessage(
                                PacketIdentifier.Touch,
                                TouchPoints(filteredTouchPoints.map { it.touchPoint })
                            )
                        )
                    }
                    previousTouchPoints = touchPoints
                }
            }
        }
    ) { innerPadding ->
        Column {
            Box(
                Modifier
                    .fillMaxWidth()
                    .weight(
                        if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                            0.7f
                        } else {
                            0.85f
                        }, true
                    )
                    .then(Modifier.padding(innerPadding))
            )
            HorizontalDivider(modifier = Modifier.fillMaxWidth())
            Row(Modifier
                .onSizeChanged { size -> buttonsSize = size }
                .weight(
                    if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                        0.3f
                    } else {
                        0.15f
                    }
                )
                .height(10.dp)
            ) {
                Box(
                    Modifier
                        .weight(0.5f)
                        .fillMaxHeight()
                        .clickable { })
                VerticalDivider(modifier = Modifier.fillMaxHeight())
                Box(
                    Modifier
                        .weight(0.5f)
                        .fillMaxHeight()
                        .clickable { })
            }
        }
    }
}
