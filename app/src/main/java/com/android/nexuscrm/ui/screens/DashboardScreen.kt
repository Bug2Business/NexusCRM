package com.android.nexuscrm.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.nexuscrm.model.CRMTask
import com.android.nexuscrm.model.Lead
import com.android.nexuscrm.model.LeadStatus
import com.android.nexuscrm.ui.components.GlassCard
import com.android.nexuscrm.ui.components.StatCard

@Composable
fun DashboardScreen(leads: List<Lead>, tasks: List<CRMTask>) {
    val totalRevenue = leads.sumOf { it.value }
    val activeLeads = leads.count { it.status != LeadStatus.CLOSED }
    val pendingTasks = tasks.count { !it.isCompleted }
    val progress = if (leads.isEmpty()) 0f else leads.count { it.status == LeadStatus.CLOSED }.toFloat() / leads.size

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        item { Text("Overview", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold); Spacer(modifier = Modifier.height(16.dp)) }
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard("Active Leads", activeLeads.toString(), Modifier.weight(1f))
                StatCard("Revenue", "$${"%.1fk".format(totalRevenue / 1000)}", Modifier.weight(1.2f))
                StatCard("Tasks", pendingTasks.toString(), Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text("Conversion Progress", color = Color.White, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape), color = Color(0xFF64B5F6), trackColor = Color.White.copy(alpha = 0.1f))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("${(progress * 100).toInt()}% of leads closed", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
        item { Text("Urgent Actions", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold); Spacer(modifier = Modifier.height(12.dp)) }
        items(tasks.filter { !it.isCompleted }.take(5)) { task -> TaskRow(task); Spacer(modifier = Modifier.height(8.dp)) }
        if (tasks.none { !it.isCompleted }) item { Text("No upcoming tasks! 🎉", color = Color.White.copy(alpha = 0.5f), fontSize = 14.sp) }
    }
}

@Composable
fun TaskRow(task: CRMTask) {
    GlassCard(modifier = Modifier.fillMaxWidth(), padding = 12.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(if (task.isCompleted) Color.Gray else Color(0xFFE57373)))
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(task.title, color = Color.White, fontSize = 14.sp)
                Text(task.deadline, color = Color.White.copy(alpha = 0.4f), fontSize = 11.sp)
            }
        }
    }
}
