package com.android.nexuscrm.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.nexuscrm.model.CRMTask
import com.android.nexuscrm.ui.components.GlassCard

@Composable
fun TasksScreen(tasks: List<CRMTask>, onToggle: (CRMTask) -> Unit, onDelete: (CRMTask) -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Daily Agenda", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold); Spacer(modifier = Modifier.height(16.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (tasks.isEmpty()) {
                item { Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) { Text("Relax, no tasks today!", color = Color.White.copy(alpha = 0.5f)) } }
            }
            items(tasks) { task ->
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = task.isCompleted, onCheckedChange = { onToggle(task) }, colors = CheckboxDefaults.colors(checkmarkColor = Color.Black))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(task.title, color = Color.White, fontWeight = FontWeight.Medium, style = if (task.isCompleted) TextStyle(textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough) else TextStyle.Default)
                            Text("${task.deadline}${if(task.contactName.isNotEmpty()) " • ${task.contactName}" else ""}", color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
                        }
                        IconButton(onClick = { onDelete(task) }) { Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.White.copy(alpha = 0.4f)) }
                    }
                }
            }
        }
    }
}
