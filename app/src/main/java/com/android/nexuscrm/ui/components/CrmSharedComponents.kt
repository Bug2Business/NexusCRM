package com.android.nexuscrm.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.nexuscrm.model.LeadStatus
import java.util.Calendar

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    padding: androidx.compose.ui.unit.Dp = 16.dp,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White.copy(alpha = 0.08f))
            .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(24.dp))
            .padding(padding)
    ) {
        content()
    }
}

@Composable
fun CRMTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isPassword: Boolean = false,
    leadingIcon: ImageVector? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        leadingIcon = leadingIcon?.let {
            { Icon(it, contentDescription = null, tint = Color.White.copy(alpha = 0.6f)) }
        },
        visualTransformation = if (isPassword) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
        modifier = Modifier.fillMaxWidth(),
        keyboardOptions = keyboardOptions,
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedLabelColor = Color.White,
            unfocusedLabelColor = Color.White.copy(alpha = 0.5f),
            focusedBorderColor = Color.White.copy(alpha = 0.4f),
            unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
            cursorColor = Color.White
        ),
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
fun FrostedGlassButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .shadow(12.dp, RoundedCornerShape(27.dp)),
        shape = RoundedCornerShape(27.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.15f))
    ) {
        Text(text, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
}

@Composable
fun GreetingHeader() {
    val greetingData = remember {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when (hour) {
            in 5..11 -> Triple("Good Morning", "☀️", listOf(Color(0xFFFFE000), Color(0xFFFFAB00)))
            in 12..16 -> Triple("Good Afternoon", "🌤️", listOf(Color(0xFF00B4DB), Color(0xFF0083B0)))
            in 17..20 -> Triple("Good Evening", "🌅", listOf(Color(0xFFF09819), Color(0xFFEDDE5D)))
            else -> Triple("Good Night", "🌙", listOf(Color(0xFF8E2DE2), Color(0xFF4A00E0)))
        }
    }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(greetingData.second, fontSize = 54.sp)
        Text(
            text = greetingData.first,
            fontSize = 38.sp,
            fontWeight = FontWeight.Black,
            style = TextStyle(brush = Brush.horizontalGradient(greetingData.third))
        )
    }
}

@Composable
fun StatCard(title: String, value: String, modifier: Modifier = Modifier) {
    GlassCard(modifier = modifier, padding = 12.dp) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Text(title, color = Color.White.copy(alpha = 0.6f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Text(value, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
fun StatusBadge(status: LeadStatus, onClick: () -> Unit) {
    val color = when (status) {
        LeadStatus.NEW -> Color(0xFF64B5F6)
        LeadStatus.CONTACTED -> Color(0xFFFFB74D)
        LeadStatus.PROPOSAL -> Color(0xFFBA68C8)
        LeadStatus.CLOSED -> Color(0xFF81C784)
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.2f))
            .border(1.dp, color.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(status.name, color = color, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}
