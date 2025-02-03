package com.example.phonemouse.ui.screens

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.phonemouse.PhoneMouseRoutes
import com.example.phonemouse.models.Device
import com.example.phonemouse.ui.components.DeviceCard
import com.example.phonemouse.viewmodels.DevicesViewModel
import java.net.InetAddress

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun DevicesScreen(
    navController: NavController,
    devicesViewModel: DevicesViewModel,
) {
    val deviceList by devicesViewModel.detectedDevices.collectAsState()
    //val deviceList = listOf(
    //    Device("Pavilion", InetAddress.getByName("192.168.200.153"), 2855, 2856),
    //    Device("Pavilion", InetAddress.getByName("192.168.200.153"), 2855, 2856),
    //    Device("Pavilion", InetAddress.getByName("192.168.200.153"), 2855, 2856),
    //    Device("Pavilion", InetAddress.getByName("192.168.200.153"), 2855, 2856),
    //    Device("Pavilion", InetAddress.getByName("192.168.200.153"), 2855, 2856),
    //    Device("Pavilion", InetAddress.getByName("192.168.200.153"), 2855, 2856),
    //    Device("Pavilion", InetAddress.getByName("192.168.200.153"), 2855, 2856),
    //    Device("Pavilion", InetAddress.getByName("192.168.200.153"), 2855, 2856),
    //    Device("Pavilion", InetAddress.getByName("192.168.200.153"), 2855, 2856),
    //    Device("Pavilion", InetAddress.getByName("192.168.200.153"), 2855, 2856),
    //    Device("Pavilion", InetAddress.getByName("192.168.200.153"), 2855, 2856),
    //    Device("Pavilion", InetAddress.getByName("192.168.200.153"), 2855, 2856),
    //    Device("Pavilion", InetAddress.getByName("192.168.200.153"), 2855, 2856),
    //    Device("Pavilion", InetAddress.getByName("192.168.200.153"), 2855, 2856),
    //    Device("Pavilion", InetAddress.getByName("192.168.200.153"), 2855, 2856),
    //)
    val isDetectingDevices by devicesViewModel.isDetectingDevices.collectAsState()
    val connectedDevice by devicesViewModel.connectedDevice.collectAsState()

    val deviceListScrollState = rememberLazyListState()
    val topAppBarElevation by animateDpAsState(
        if (deviceListScrollState.canScrollBackward) 8.dp else 0.dp,
        label = "TopAppBar elevation animation",
    )

    Scaffold(
        topBar = {
            Surface(tonalElevation = topAppBarElevation) {
                TopAppBar(
                    title = { Text(PhoneMouseRoutes.Devices.name) },
                    navigationIcon = {
                        IconButton(onClick = { navController.navigateUp() }) {
                            Icon(imageVector = Icons.AutoMirrored.Default.ArrowBack, contentDescription = "Back")
                        }
                    },
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { devicesViewModel.detectDevices() },
                containerColor = if (!isDetectingDevices) FloatingActionButtonDefaults.containerColor else MaterialTheme.colorScheme.surfaceContainerHighest,
            ) {
                if (!isDetectingDevices) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh devices list"
                    )
                } else {
                    CircularProgressIndicator(modifier = Modifier.scale(0.6f))
                }
            }
        },
    ) { innerPadding ->
        PullToRefreshBox(
            onRefresh = { devicesViewModel.detectDevices() },
            isRefreshing = isDetectingDevices,
            modifier = Modifier.padding(innerPadding),
        ) {
            LazyColumn(
                state = deviceListScrollState,
                modifier = Modifier
                    .padding(20.dp, 0.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                item {
                    Spacer(modifier = Modifier)
                }

                items(deviceList) { device ->
                    DeviceCard(
                        device = device,
                        onClick = {
                            devicesViewModel.disconnect()
                            if (device != connectedDevice) {
                                devicesViewModel.connect(device)
                            }
                        },
                        connected = device.address == connectedDevice?.address,
                    )
                }
            }
        }
    }
}