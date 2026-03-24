package com.android.nexuscrm.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

fun calculateStrength(password: String): Pair<Float, Color> {
    var score = 0

    if (password.length >= 6) score++
    if (password.any { it.isDigit() }) score++
    if (password.any { it.isUpperCase() }) score++
    if (password.any { "!@#\$%^&*".contains(it) }) score++

    return when (score) {
        0 -> 0f to Color.Red
        1 -> 0.25f to Color.Red
        2 -> 0.5f to Color.Yellow
        3 -> 0.75f to Color.Cyan
        else -> 1f to Color.Green
    }
}

@Composable
fun PasswordStrengthMeter(password: String) {

    val (strength, color) = calculateStrength(password)

    val animatedStrength by animateFloatAsState(strength, label = "")

    Column {
        Box(
            Modifier
                .fillMaxWidth()
                .height(6.dp)
                .background(Color.Gray.copy(0.3f))
        ) {
            Box(
                Modifier
                    .fillMaxWidth(animatedStrength)
                    .height(6.dp)
                    .background(color)
            )
        }

        Spacer(Modifier.height(4.dp))

        Text(
            text = when {
                strength < 0.3 -> "Weak"
                strength < 0.7 -> "Medium"
                else -> "Strong"
            },
            color = color
        )
    }
}
