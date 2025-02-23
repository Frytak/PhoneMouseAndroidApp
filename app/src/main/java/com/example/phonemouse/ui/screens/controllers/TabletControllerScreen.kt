package com.example.phonemouse.ui.screens.controllers

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.phonemouse.PHONE_MOUSE_TAG
import com.example.phonemouse.models.PacketIdentifier
import com.example.phonemouse.models.PacketMessage
import com.example.phonemouse.models.TouchPoint
import com.example.phonemouse.models.TouchPoints
import com.example.phonemouse.viewmodels.DevicesViewModel

@Composable
fun TabletControllerScreen(
    navController: NavController,
    devicesViewModel: DevicesViewModel,
) {
    Scaffold { innerPadding ->
        Box(Modifier
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(20.dp)
            .fillMaxSize()
            .then(Modifier.padding(innerPadding))
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        val touchPoints: MutableList<TouchPoint> = mutableListOf()

                        event.changes.map {
                            touchPoints.add(TouchPoint(it.id.value, it.position.x, it.position.y))
                        }

                        devicesViewModel.sendTCPPacketMessage(PacketMessage(PacketIdentifier.Touch, TouchPoints(touchPoints)))
                    }
                }
            }
        )
    }
}