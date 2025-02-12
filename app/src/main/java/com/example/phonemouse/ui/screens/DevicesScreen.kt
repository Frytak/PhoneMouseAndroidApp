package com.example.phonemouse.ui.screens

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
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
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.phonemouse.PHONE_MOUSE_TAG
import com.example.phonemouse.PhoneMouseRoutes
import com.example.phonemouse.models.Device
import com.example.phonemouse.ui.components.DeviceCard
import com.example.phonemouse.viewmodels.DevicesViewModel
import java.net.DatagramPacket
import java.net.InetAddress
import java.net.MulticastSocket
import java.net.NetworkInterface
import java.net.SocketException
import java.util.Enumeration

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

    var enumeration: Enumeration<NetworkInterface>? = null
    try {
        enumeration = NetworkInterface.getNetworkInterfaces()
    } catch (e: SocketException) {
        e.printStackTrace()
    }
    var wlan0: NetworkInterface? = null
    val sb = StringBuilder()
    while (enumeration?.hasMoreElements() == true) {
        wlan0 = enumeration?.nextElement()
        sb.append(wlan0?.getName() + " ")
        Log.i(PHONE_MOUSE_TAG, "Interface detected: " + wlan0?.name.toString() + " " + wlan0?.supportsMulticast().toString())
        if (wlan0?.getName().equals("wlan0")) {
            //there is probably a better way to find ethernet interface
            Log.i(PHONE_MOUSE_TAG, "wlan0 found")
        }
    }

    //Thread {
    //    val multicast = MulticastSocket()
    //    for (i in 1..5) {
    //        Log.d(PHONE_MOUSE_TAG, "Sending out a multicast ping")
    //        multicast.send(DatagramPacket(byteArrayOf(4), 1, socketAddress))
    //        Thread.sleep(1500)
    //    }
    //    multicast.networkInterface = wlan0
    //}.start()

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
    ) { innerPadding ->
        Box(
            modifier = Modifier.padding(innerPadding),
        ) {
            AnimatedVisibility(
                visible = deviceList.size >= 1 || isDetectingDevices,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                PullToRefreshBox(
                    onRefresh = { devicesViewModel.detectDevices() },
                    isRefreshing = isDetectingDevices,
                ) {
                    LazyColumn(
                        state = deviceListScrollState,
                        modifier = Modifier
                            .padding(20.dp, 0.dp)
                            .fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        if (connectedDevice != null) {
                            item {
                                DeviceCard(
                                    device = connectedDevice!!,
                                    onClick = { devicesViewModel.disconnect() },
                                    connected = true,
                                )
                            }

                            item {
                                Spacer(modifier = Modifier)
                            }
                        }

                        items(deviceList) { device ->
                            DeviceCard(
                                device = device,
                                onClick = { devicesViewModel.connect(device) },
                                connected = device.address == connectedDevice?.address,
                            )
                        }
                    }
                }
            }

            AnimatedVisibility(
                visible = deviceList.size == 0 && !isDetectingDevices,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text("No devices detected")
                    Button(
                        onClick = { devicesViewModel.detectDevices() },
                    ) { Text("Refresh") }
                }
            }
        }
    }
}