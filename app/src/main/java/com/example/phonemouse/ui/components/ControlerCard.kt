package com.example.phonemouse.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp

@Composable
fun ControlerCard(
    modifier: Modifier = Modifier,
    text: String,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
) {
    Card(
        modifier = Modifier
            .clickable(onClick != null && enabled, "Connect to device", Role.Button) { if (onClick != null && enabled) onClick() }
            .then(modifier),
        colors = CardDefaults.cardColors(containerColor = if (!enabled) MaterialTheme.colorScheme.surfaceContainerHigh else MaterialTheme.colorScheme.primaryContainer),
    ) {
        Box(
            modifier = Modifier.padding(20.dp).fillMaxSize(),
        ) {
            Text(
                text = text,
                modifier = Modifier.align(Alignment.BottomEnd)
            )
        }
    }
}