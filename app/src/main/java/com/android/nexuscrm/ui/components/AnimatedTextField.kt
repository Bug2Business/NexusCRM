package com.android.nexuscrm.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import kotlin.math.sin

@Composable
fun AnimatedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isError: Boolean,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailingIcon: @Composable (() -> Unit)? = null
) {

    var isFocused by remember { mutableStateOf(false) }

    val glow by animateFloatAsState(
        if (isFocused) 1f else 0f, label = ""
    )

    val shake = remember { Animatable(0f) }

    LaunchedEffect(isError) {
        if (isError) {
            shake.animateTo(1f, tween(400))
            shake.snapTo(0f)
        }
    }

    val offsetX = if (isError) sin(shake.value * 10) * 10 else 0.0

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        visualTransformation = visualTransformation,
        trailingIcon = trailingIcon,
        isError = isError,
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { translationX = offsetX.toFloat() }
            .onFocusChanged { isFocused = it.isFocused }
            .border(
                if (isFocused) 1.5.dp else 1.dp,
                if (isError) Color.Red else Color.Cyan.copy(alpha = glow),
                MaterialTheme.shapes.medium
            ),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent,
            errorBorderColor = Color.Transparent
        )
    )
}
