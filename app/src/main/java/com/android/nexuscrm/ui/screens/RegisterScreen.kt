package com.android.nexuscrm.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.nexuscrm.R
import com.android.nexuscrm.ui.components.CRMTextField
import com.android.nexuscrm.ui.components.FrostedGlassButton
import com.android.nexuscrm.ui.components.GlassCard

@Composable
fun RegisterScreen(onRegisterSuccess: (String, String) -> String?, onBack: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.anime_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize().blur(12.dp),
            contentScale = ContentScale.Crop
        )
        Box(modifier = Modifier.fillMaxSize().background(brush = Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.4f), Color.Black.copy(alpha = 0.7f)))))

        Column(modifier = Modifier.fillMaxWidth().padding(24.dp).align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Create Account", fontSize = 32.sp, color = Color.White, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(24.dp))
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    var email by remember { mutableStateOf("") }
                    var pass by remember { mutableStateOf("") }
                    var confirmPass by remember { mutableStateOf("") }
                    var error by remember { mutableStateOf("") }

                    CRMTextField(email, { email = it }, "Email")
                    Spacer(modifier = Modifier.height(12.dp))
                    CRMTextField(pass, { pass = it }, "Password", isPassword = true)
                    Spacer(modifier = Modifier.height(12.dp))
                    CRMTextField(confirmPass, { confirmPass = it }, "Confirm Password", isPassword = true)

                    if (error.isNotEmpty()) {
                        Text(text = error, color = Color.White, fontSize = 14.sp, modifier = Modifier.padding(top = 8.dp))
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    FrostedGlassButton("Register") {
                        if (pass != confirmPass) error = "Passwords do not match"
                        else {
                            val msg = onRegisterSuccess(email, pass)
                            if (msg != null) error = msg
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    TextButton(onClick = onBack) {
                        Text("Already have an account? Login", color = Color.White.copy(alpha = 0.8f))
                    }
                }
            }
        }
    }
}
