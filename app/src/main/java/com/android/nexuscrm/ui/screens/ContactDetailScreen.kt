package com.android.nexuscrm.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.nexuscrm.model.Interaction
import com.android.nexuscrm.model.Lead
import com.android.nexuscrm.ui.components.CRMTextField
import com.android.nexuscrm.ui.components.GlassCard
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ContactDetailScreen(lead: Lead, onBack: () -> Unit, onAddInteraction: (String, String) -> Unit) {
    var showInteractionDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
        }
        
        Text(lead.name, color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Black)
        Text(lead.company, color = Color.White.copy(alpha = 0.6f), fontSize = 16.sp)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ContactInfoBox(Icons.Default.Phone, lead.phone, Modifier.weight(1f))
            ContactInfoBox(Icons.Default.Email, lead.email, Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(24.dp))
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Activity Timeline", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.weight(1f))
            TextButton(onClick = { showInteractionDialog = true }) {
                Text("+ Log Action", color = Color(0xFF64B5F6))
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (lead.notes.isEmpty()) {
                item { Text("No interactions yet.", color = Color.White.copy(alpha = 0.4f), modifier = Modifier.padding(16.dp)) }
            }
            items(lead.notes) { interaction ->
                TimelineItem(interaction)
            }
        }
    }

    if (showInteractionDialog) {
        var content by remember { mutableStateOf("") }
        var type by remember { mutableStateOf("Note") }
        
        AlertDialog(
            onDismissRequest = { showInteractionDialog = false },
            title = { Text("Log Interaction", color = Color.White) },
            containerColor = Color(0xFF1B2735),
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Call", "Meeting", "Email", "Note").forEach { t ->
                            FilterChip(selected = type == t, onClick = { type = t }, label = { Text(t) })
                        }
                    }
                    CRMTextField(content, { content = it }, "What happened?")
                }
            },
            confirmButton = {
                Button(onClick = {
                    onAddInteraction(type, content)
                    showInteractionDialog = false
                }) { Text("Log") }
            },
            dismissButton = { TextButton(onClick = { showInteractionDialog = false }) { Text("Cancel") } }
        )
    }
}

@Composable
fun ContactInfoBox(icon: ImageVector, text: String, modifier: Modifier = Modifier) {
    GlassCard(modifier = modifier, padding = 12.dp) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, contentDescription = null, tint = Color.White.copy(alpha = 0.6f), modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(text.ifEmpty { "N/A" }, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun TimelineItem(interaction: Interaction) {
    val df = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
    GlassCard(modifier = Modifier.fillMaxWidth(), padding = 12.dp) {
        Row(verticalAlignment = Alignment.Top) {
            val icon = when(interaction.type) {
                "Call" -> Icons.Default.Call
                "Meeting" -> Icons.Default.Groups
                "Email" -> Icons.Default.Email
                else -> Icons.Default.EditNote
            }
            Icon(icon, contentDescription = null, tint = Color(0xFF64B5F6), modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(interaction.content, color = Color.White, fontSize = 14.sp)
                Text("${interaction.type} • ${df.format(Date(interaction.timestamp))}", color = Color.White.copy(alpha = 0.4f), fontSize = 11.sp)
            }
        }
    }
}
