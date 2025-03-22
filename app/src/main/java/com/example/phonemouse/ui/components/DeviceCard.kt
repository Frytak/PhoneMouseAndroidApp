package com.example.phonemouse.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.phonemouse.models.Device

@Composable
fun DeviceCard(
    device: Device,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    connected: Boolean = false,
) {
    val labelFontSize = 12.sp

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = if (!connected) MaterialTheme.colorScheme.surfaceContainerHigh else MaterialTheme.colorScheme.primaryContainer),
    ) {
        Box(
            modifier = Modifier.clickable(
                enabled = onClick != null,
                onClickLabel = "Connect to device",
                role = Role.Button,
                interactionSource = null,
                indication = ripple(bounded = true),
            ) { if (onClick != null) onClick() },
        ) {
            Column(
                modifier = Modifier.padding(20.dp).fillMaxWidth()
            ) {
                Text(
                    text = device.name,
                    fontSize = 32.sp
                )
                Spacer(Modifier.height(10.dp))
                Row {
                    Column {
                        Text(
                            text = "Address",
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                            fontSize = labelFontSize,
                            lineHeight = 14.sp
                        )
                        Text(
                            text = device.address?.hostAddress.toString(),
                            fontSize = labelFontSize,
                            lineHeight = 14.sp
                        )
                    }
                    Spacer(Modifier.width(100.dp))
                    Column {
                        Text(
                            text = "Ports (TCP/UDP)",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            fontSize = labelFontSize,
                            lineHeight = 14.sp
                        )
                        Text(
                            text = device.tcpPort?.toString() + "/" + device.udpPort?.toString(),
                            fontSize = labelFontSize,
                            lineHeight = 14.sp
                        )
                    }
                }
            }
        }
    }
}