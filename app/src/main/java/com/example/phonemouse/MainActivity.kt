@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.phonemouse

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.phonemouse.models.PhoneMouseState
import com.example.phonemouse.ui.screens.DevicesScreen
import com.example.phonemouse.ui.screens.MainScreen
import com.example.phonemouse.ui.screens.SettingsScreen
import com.example.phonemouse.ui.screens.controllers.GravityControllerScreen
import com.example.phonemouse.ui.screens.controllers.MouseControllerScreen
import com.example.phonemouse.ui.screens.controllers.TabletControllerScreen
import com.example.phonemouse.ui.screens.controllers.TouchpadControllerScreen
import com.example.phonemouse.ui.theme.PhoneMouseTheme
import com.example.phonemouse.viewmodels.DevicesViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PhoneMouseTheme {
                Main()
            }
        }
    }
}

enum class PhoneMouseRoutes {
    Main,
    Devices,
    Settings,

    MouseController,
    TouchpadController,
    TabletController,
    GravityController,
}

const val PHONE_MOUSE_TAG = "PhoneMouse"

@Composable
fun Main() {
    val navController = rememberNavController()
    val devicesViewModel: DevicesViewModel = viewModel()

    // Change the device state
    LaunchedEffect(navController) {
        navController.currentBackStackEntryFlow.collect { backStackEntry ->
            when (backStackEntry.destination.route) {
                PhoneMouseRoutes.Devices.name -> if (devicesViewModel.detectedDevices.value.isEmpty()) devicesViewModel.detectDevices()
                PhoneMouseRoutes.GravityController.name -> devicesViewModel.setState(PhoneMouseState.Gravity)
                PhoneMouseRoutes.TabletController.name -> devicesViewModel.setState(PhoneMouseState.Tablet)
                PhoneMouseRoutes.TouchpadController.name -> devicesViewModel.setState(PhoneMouseState.Touchpad)
                else -> devicesViewModel.setState(PhoneMouseState.Idle)
            }
        }
    }

    PhoneMouseTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            NavHost(
                navController = navController,
                startDestination = PhoneMouseRoutes.Main.name,
                modifier = Modifier.fillMaxSize(),
            ) {
                composable(route = PhoneMouseRoutes.Main.name) {
                    MainScreen(
                        navController = navController,
                        devicesViewModel = devicesViewModel,
                    )
                }
                composable(route = PhoneMouseRoutes.Devices.name) {
                    DevicesScreen(
                        navController = navController,
                        devicesViewModel = devicesViewModel,
                    )
                }
                composable(route = PhoneMouseRoutes.Settings.name) {
                    SettingsScreen(navController = navController)
                }
                composable(route = PhoneMouseRoutes.MouseController.name) {
                    MouseControllerScreen(
                        navController = navController,
                        devicesViewModel = devicesViewModel,
                    )
                }
                composable(route = PhoneMouseRoutes.TouchpadController.name) {
                    TouchpadControllerScreen(
                        navController = navController,
                        devicesViewModel = devicesViewModel,
                    )
                }
                composable(route = PhoneMouseRoutes.TabletController.name) {
                    TabletControllerScreen(
                        navController = navController,
                        devicesViewModel = devicesViewModel,
                    )
                }
                composable(route = PhoneMouseRoutes.GravityController.name) {
                    GravityControllerScreen(
                        navController = navController,
                        devicesViewModel = devicesViewModel,
                    )
                }
            }
        }
    }
}