package com.android.nexuscrm.ui.screens

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.android.nexuscrm.R
import com.android.nexuscrm.model.UserRole
import com.android.nexuscrm.ui.components.CRMTextField
import com.android.nexuscrm.ui.components.FrostedGlassButton
import com.android.nexuscrm.ui.components.GlassCard
import com.android.nexuscrm.ui.components.GreetingHeader
import java.util.concurrent.Executor

@Composable
fun LoginScreen(
    onLoginSuccess: (String, String) -> Boolean,
    onRegisterClicked: () -> Unit,
    onBiometricSuccess: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? FragmentActivity

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.anime_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize().blur(12.dp),
            contentScale = ContentScale.Crop
        )

        Box(modifier = Modifier.fillMaxSize().background(
            brush = Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.4f), Color.Black.copy(alpha = 0.7f)))
        ))

        Column(
            modifier = Modifier.fillMaxWidth().padding(24.dp).align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            GreetingHeader()
            Spacer(modifier = Modifier.height(32.dp))
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    var email by remember { mutableStateOf("") }
                    var password by remember { mutableStateOf("") }
                    var error by remember { mutableStateOf("") }

                    CRMTextField(value = email, onValueChange = { email = it }, label = "Email")
                    Spacer(modifier = Modifier.height(12.dp))
                    CRMTextField(value = password, onValueChange = { password = it }, label = "Password", isPassword = true)

                    if (error.isNotEmpty()) {
                        Text(text = error, color = Color.Red.copy(alpha = 0.8f), fontSize = 14.sp, modifier = Modifier.padding(top = 8.dp))
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    FrostedGlassButton(text = "Login") {
                        if (onLoginSuccess(email, password)) error = ""
                        else error = "Invalid Credentials or Account Disabled"
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    TextButton(onClick = onRegisterClicked) {
                        Text("Create New Account", color = Color.White.copy(alpha = 0.8f))
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    val bm = BiometricManager.from(context)
                    if (bm.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL) == BiometricManager.BIOMETRIC_SUCCESS) {
                        IconButton(
                            onClick = { activity?.let { showBiometricPrompt(it, onBiometricSuccess) } },
                            modifier = Modifier.size(48.dp).background(Color.White.copy(alpha = 0.1f), CircleShape)
                        ) { Icon(Icons.Default.Fingerprint, contentDescription = "Biometric", tint = Color.White) }
                    }
                }
            }
        }
    }
}

fun showBiometricPrompt(activity: FragmentActivity, onSuccess: () -> Unit) {
    val executor: Executor = ContextCompat.getMainExecutor(activity)
    val biometricPrompt = BiometricPrompt(activity, executor, object : BiometricPrompt.AuthenticationCallback() {
        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) { onSuccess() }
    })
    val promptInfo = BiometricPrompt.PromptInfo.Builder().setTitle("NexusCRM Access").setSubtitle("Authenticate to continue").setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL).build()
    biometricPrompt.authenticate(promptInfo)
}
