package com.example.phonemouse.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController

@Composable
fun SettingsScreen(
    navController: NavController,
) {
    Scaffold { innerPadding ->
        Text(
            text = "TODO",
            modifier = Modifier.padding(innerPadding),
        )
    }
}