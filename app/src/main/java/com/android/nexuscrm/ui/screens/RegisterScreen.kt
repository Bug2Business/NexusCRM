package com.android.nexuscrm.ui.screens

import android.os.Handler
import android.os.Looper
import androidx.compose.ui.geometry.CornerRadius
import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.android.nexuscrm.R
import com.android.nexuscrm.ui.components.*

@Composable
fun RegisterScreen(
    onRegisterSuccess: (String, String) -> String?,
    onBack: () -> Unit
) {
    val view = LocalView.current

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    var isLoading by remember { mutableStateOf(false) }
    var isSuccess by remember { mutableStateOf(false) }

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    val scale by animateFloatAsState(if (visible) 1f else 0.7f, spring(), label = "")
    val alpha by animateFloatAsState(if (visible) 1f else 0f, tween(500), label = "")

    val infiniteTransition = rememberInfiniteTransition(label = "")
    val gradientShift by infiniteTransition.animateFloat(
        0f, 1000f,
        infiniteRepeatable(tween(4000), RepeatMode.Reverse), label = ""
    )

    val isEmailValid = email.contains("@")
    val isPasswordValid = password.length >= 6
    val passwordsMatch = password == confirmPassword

    Box(Modifier.fillMaxSize()) {

        Image(
            painterResource(R.drawable.anime_bg),
            null,
            Modifier.fillMaxSize().blur(25.dp),
            contentScale = ContentScale.Crop
        )

        Box(Modifier.fillMaxSize().background(Color.Black.copy(0.4f)))

        Box(
            Modifier.padding(24.dp)
                .fillMaxWidth()
                .align(Alignment.Center)
                .graphicsLayer { scaleX = scale; scaleY = scale; this.alpha = alpha }
                .background(Color.White.copy(0.1f), RoundedCornerShape(28.dp))
                .border(1.dp, Color.White.copy(0.3f), RoundedCornerShape(28.dp))
                .padding(24.dp)
        ) {

            Column(horizontalAlignment = Alignment.CenterHorizontally) {

                Text("Create Account ✨", color = Color.White)

                Spacer(Modifier.height(20.dp))

                AnimatedTextField(email, { email = it }, "Email", email.isNotEmpty() && !isEmailValid)

                Spacer(Modifier.height(10.dp))

                var passwordVisible by remember { mutableStateOf(false) }

                AnimatedTextField(
                    password,
                    { password = it },
                    "Password",
                    password.isNotEmpty() && !isPasswordValid,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                contentDescription = if (passwordVisible) "Hide password" else "Show password",
                                tint = Color.White.copy(alpha = 0.7f)
                            )
                        }
                    }
                )

                Spacer(Modifier.height(6.dp))

                PasswordStrengthMeter(password)

                Spacer(Modifier.height(10.dp))

                var confirmPasswordVisible by remember { mutableStateOf(false) }

                AnimatedTextField(
                    confirmPassword,
                    { confirmPassword = it },
                    "Confirm Password",
                    confirmPassword.isNotEmpty() && !passwordsMatch,
                    visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                            Icon(
                                imageVector = if (confirmPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                contentDescription = if (confirmPasswordVisible) "Hide password" else "Show password",
                                tint = Color.White.copy(alpha = 0.7f)
                            )
                        }
                    }
                )

                Spacer(Modifier.height(10.dp))

                if (errorMessage.isNotEmpty()) {
                    Text(errorMessage, color = Color.Red)
                }

                Spacer(Modifier.height(20.dp))

                if (isLoading) {
                    CircularProgressIndicator(color = Color.White)
                } else if (isSuccess) {
                    SuccessCheckmark()
                } else {

                    val interactionSource = remember { MutableInteractionSource() }
                    val pressed by interactionSource.collectIsPressedAsState()

                    val buttonScale by animateFloatAsState(if (pressed) 0.94f else 1f, spring(), label = "")
                    val glow by animateFloatAsState(if (pressed) 0.3f else 0.1f, label = "")

                    Button(
                        onClick = {
                            view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)

                            if (!isEmailValid || !isPasswordValid || !passwordsMatch) {
                                errorMessage = "Fix errors above"
                                return@Button
                            }

                            isLoading = true

                            val result = onRegisterSuccess(email, password)

                            isLoading = false

                            if (result == null) {
                                isSuccess = true
                                Handler(Looper.getMainLooper()).postDelayed({
                                    onBack()
                                }, 1400)
                            } else {
                                errorMessage = result
                            }
                        },
                        interactionSource = interactionSource,
                        modifier = Modifier.fillMaxWidth().height(54.dp)
                            .graphicsLayer { scaleX = buttonScale; scaleY = buttonScale },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        contentPadding = PaddingValues()
                    ) {

                        Box(
                            Modifier.fillMaxSize()
                                .background(
                                    Brush.linearGradient(
                                        listOf(Color(0xFF6A11CB), Color(0xFF2575FC)),
                                        start = Offset(gradientShift, 0f),
                                        end = Offset(gradientShift + 600f, 600f)
                                    ),
                                    RoundedCornerShape(16.dp)
                                )
                                .drawBehind {
                                    drawRoundRect(Color.White.copy(glow), cornerRadius = CornerRadius(30f))
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Register", color = Color.White)
                        }
                    }
                }

                Spacer(Modifier.height(10.dp))

                TextButton(onClick = onBack) {
                    Text("Already have an account? Login", color = Color.White)
                }
            }
        }
    }
}
