package com.android.nexuscrm.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun BiometricButton(
    onSuccess: () -> Unit
) {
    var scanning by remember { mutableStateOf(false) }
    var success by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    val scale by animateFloatAsState(
        targetValue = if (scanning) 1.1f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = ""
    )

    val color by animateColorAsState(
        targetValue = when {
            success -> Color(0xFF00C853)
            scanning -> Color.Cyan
            else -> Color.Gray
        },
        label = ""
    )

    Box(
        modifier = Modifier
            .size(80.dp)
            .scale(scale)
            .background(color.copy(alpha = 0.2f), CircleShape)
            .clickable {
                if (!scanning && !success) {
                    scanning = true

                    scope.launch {
                        delay(1200)
                        scanning = false
                        success = true
                        delay(600)
                        onSuccess()
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (success) "✓" else "🔐",
            color = color
        )
    }
}
