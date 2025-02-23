package com.example.phonemouse.ui.screens.controllers

import android.content.res.Configuration
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerId
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.phonemouse.models.Key
import com.example.phonemouse.models.PacketIdentifier
import com.example.phonemouse.models.PacketMessage
import com.example.phonemouse.models.TouchPoint
import com.example.phonemouse.models.TouchPoints
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

    Scaffold { innerPadding ->
        Column {
            Box(Modifier
                .fillMaxWidth()
                .weight(if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) { 0.7f } else { 0.85f }, true)
                .then(Modifier.padding(innerPadding))
                .pointerInput(Unit) {
                    awaitEachGesture {
                        while (true) {
                            val touchPoints: MutableList<TouchPoint> = mutableListOf()
                            val event = awaitPointerEvent()

                            event.changes.forEach {
                                if (it.pressed) {
                                    touchPoints.add(TouchPoint(it.id.value, it.position.x, it.position.y))
                                }
                                it.consume()
                            }

                            devicesViewModel.sendTCPPacketMessage(PacketMessage(PacketIdentifier.Touch, TouchPoints(touchPoints)))
                        }
                    }
                }
            )
            HorizontalDivider(modifier = Modifier.fillMaxWidth())
            Row(modifier = Modifier.weight(if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) { 0.3f } else { 0.15f }).height(10.dp)) {
                Box(modifier = Modifier.weight(0.5f).fillMaxHeight().clickable { buttonClick(view, devicesViewModel, Key.BTN_LEFT) })
                VerticalDivider(modifier = Modifier.fillMaxHeight())
                Box(modifier = Modifier.weight(0.5f).fillMaxHeight().clickable { buttonClick(view, devicesViewModel, Key.BTN_RIGHT) })
            }
        }
    }
}
