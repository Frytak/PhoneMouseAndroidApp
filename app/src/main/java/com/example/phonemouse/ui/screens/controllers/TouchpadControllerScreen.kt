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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.phonemouse.PHONE_MOUSE_TAG
import com.example.phonemouse.models.Key
import com.example.phonemouse.models.PacketIdentifier
import com.example.phonemouse.models.PacketMessage
import com.example.phonemouse.models.TouchPoint
import com.example.phonemouse.viewmodels.DevicesViewModel

fun buttonClick(view: View, devicesViewModel: DevicesViewModel, button: Key) {
    view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
    devicesViewModel.sendTCPPacketMessage(PacketMessage(PacketIdentifier.Key, button))
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

    Scaffold(Modifier
        .onSizeChanged { size -> scaffoldSize = size }
        .pointerInput(Unit) {
            awaitEachGesture {
                var previousTouchPoints: HashMap<Long, TouchPoint> = hashMapOf()
                while (true) {
                    val touchPoints: HashMap<Long, TouchPoint> = hashMapOf()
                    val event = awaitPointerEvent()

                    event.changes.forEach {
                        if (it.pressed) {
                            touchPoints[it.id.value] = TouchPoint(it.id.value, it.position.x, it.position.y)
                        }
                    }



                    Log.d(PHONE_MOUSE_TAG, "[")
                    touchPoints.forEach { _, it -> Log.d(PHONE_MOUSE_TAG, "{ id: ${it.id}, x: ${it.x}, y: ${it.y} } ") }
                    Log.d(PHONE_MOUSE_TAG, "]")

                    //devicesViewModel.sendTCPPacketMessage(PacketMessage(PacketIdentifier.Touch, TouchPoints(touchPoints)))
                    previousTouchPoints = touchPoints
                }
            }
        }
    ) { innerPadding ->
        Column {
            Box(Modifier
                .fillMaxWidth()
                .weight(if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) { 0.7f } else { 0.85f }, true)
                .then(Modifier.padding(innerPadding))
            )
            HorizontalDivider(modifier = Modifier.fillMaxWidth())
            Row(Modifier
                .onSizeChanged { size -> buttonsSize = size }
                .weight(if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) { 0.3f } else { 0.15f }).height(10.dp)
            ) {
                Box(Modifier.weight(0.5f).fillMaxHeight().clickable { buttonClick(view, devicesViewModel, Key.BTN_LEFT) })
                VerticalDivider(modifier = Modifier.fillMaxHeight())
                Box(Modifier.weight(0.5f).fillMaxHeight().clickable { buttonClick(view, devicesViewModel, Key.BTN_RIGHT) })
            }
        }
    }
}
