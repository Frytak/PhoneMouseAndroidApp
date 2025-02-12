package com.example.phonemouse.ui.screens

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.phonemouse.PhoneMouseRoutes
import com.example.phonemouse.R
import com.example.phonemouse.ui.components.ControlerCard
import com.example.phonemouse.ui.components.DeviceCard
import com.example.phonemouse.viewmodels.DevicesViewModel

data class Controller(val name: String, val route: PhoneMouseRoutes, @DrawableRes val icon: Int, val iconModifier: Modifier)

@Composable
fun MainScreen(
    navController: NavController,
    devicesViewModel: DevicesViewModel,
) {
    val connectedDevice by devicesViewModel.connectedDevice.collectAsState()
    val controllerList: List<Controller> = listOf(
        Controller("Mouse", PhoneMouseRoutes.MouseController, R.drawable.gravity_controller_background, Modifier.rotate(20f).scale(1.4f)),
        Controller("Touchpad", PhoneMouseRoutes.TouchpadController, R.drawable.touchpad_controller_background, Modifier.rotate(-14f).scale(1.4f).padding(start = 4.dp)),
        Controller("Tablet", PhoneMouseRoutes.TabletController, R.drawable.gravity_controller_background, Modifier.rotate(20f).scale(1.4f)),
        Controller("Gravity", PhoneMouseRoutes.GravityController, R.drawable.gravity_controller_background, Modifier.rotate(20f).scale(1.4f)),
    )

    Scaffold { innerPadding ->
        Column (
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (connectedDevice != null) {
                DeviceCard(
                    modifier = Modifier.padding(20.dp).fillMaxWidth(),
                    device = connectedDevice!!,
                    connected = true,
                    onClick = { navController.navigate(PhoneMouseRoutes.Devices.name) },
                )
            } else {
                Card(
                    modifier = Modifier.padding(20.dp).fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                ) {
                    Column(Modifier.padding(20.dp).fillMaxWidth()) {
                        Text(
                            text = "Connect to a device...",
                            style = MaterialTheme.typography.headlineMedium,
                        )
                        Spacer(Modifier.height(8.dp))
                        Button(
                            modifier = Modifier.align(Alignment.End),
                            onClick = { navController.navigate(PhoneMouseRoutes.Devices.name) },
                        ) {
                            Text("Devices")
                            Spacer(Modifier.width(8.dp))
                            Icon(Icons.AutoMirrored.Default.ArrowForward, "Devices")
                        }
                    }
                }
            }
            HorizontalDivider(Modifier.padding(20.dp, 0.dp))
            LazyVerticalGrid(
                modifier = Modifier.fillMaxSize(),
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(controllerList) { controller ->
                    ControlerCard(
                        text = controller.name,
                        onClick = { navController.navigate(controller.route.name) },
                        enabled = connectedDevice != null,
                        modifier = Modifier.fillMaxWidth().aspectRatio(1.2f),
                        icon = {
                            Image(
                                modifier = controller.iconModifier,
                                painter = painterResource(controller.icon),
                                colorFilter = if (connectedDevice != null) ColorFilter.tint(MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.25f)) else ColorFilter.tint(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)),
                                contentDescription = null,
                            )
                        },
                    )
                }
            }
        }
    }
}