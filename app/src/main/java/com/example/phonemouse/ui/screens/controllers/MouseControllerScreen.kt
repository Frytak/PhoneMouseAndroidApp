package com.example.phonemouse.ui.screens.controllers

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.example.phonemouse.viewmodels.DevicesViewModel

@Composable
fun MouseControllerScreen(
    navController: NavController,
    devicesViewModel: DevicesViewModel,
) {
    Scaffold { innerPadding ->
        Text(
            text = "TODO",
            modifier = Modifier.padding(innerPadding),
        )
    }
}