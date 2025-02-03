package com.example.phonemouse.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.phonemouse.PhoneMouseRoutes
import com.example.phonemouse.models.Device
import com.example.phonemouse.ui.components.ControlerCard
import com.example.phonemouse.ui.components.DeviceCard
import com.example.phonemouse.ui.theme.PhoneMouseTheme
import com.example.phonemouse.viewmodels.DevicesViewModel
import java.net.InetAddress

data class Controller(val name: String, val route: PhoneMouseRoutes)

@Composable
fun MainScreen(
    navController: NavController,
    devicesViewModel: DevicesViewModel,
) {
    val connectedDevice by devicesViewModel.connectedDevice.collectAsState()
    val controllerList: List<Controller> = listOf(
        Controller("Mouse", PhoneMouseRoutes.MouseController),
        Controller("Touchpad", PhoneMouseRoutes.TouchpadController),
        Controller("Tablet", PhoneMouseRoutes.TabletController),
        Controller("Gravity", PhoneMouseRoutes.GravityController),
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
                    onClick = { devicesViewModel.disconnect() },
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
                    )
                }
            }
        }
    }
}