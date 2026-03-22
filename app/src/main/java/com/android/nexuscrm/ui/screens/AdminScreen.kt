package com.android.nexuscrm.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.nexuscrm.model.*
import com.android.nexuscrm.ui.components.CRMTextField
import com.android.nexuscrm.ui.components.FrostedGlassButton
import com.android.nexuscrm.ui.components.GlassCard
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AdminScreen(
    users: SnapshotStateList<User>,
    logs: SnapshotStateList<ActivityLog>,
    onLogAction: (String, String) -> Unit,
    adminEmail: String
) {
    var adminTab by remember { mutableStateOf("Users") }
    
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Users", "Logs", "Setup").forEach { tab ->
                FrostedGlassTabButton(tab, adminTab == tab, Modifier.weight(1f)) { adminTab = tab }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        when(adminTab) {
            "Users" -> UserManagementSection(users, onLogAction, adminEmail)
            "Logs" -> AuditLogSection(logs)
            "Setup" -> PipelineSetupSection()
        }
    }
}

@Composable
fun UserManagementSection(users: SnapshotStateList<User>, onLogAction: (String, String) -> Unit, adminEmail: String) {
    var showAddUserDialog by remember { mutableStateOf(false) }

    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("User Control Panel", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.weight(1f))
            IconButton(onClick = { showAddUserDialog = true }) {
                Icon(Icons.Default.PersonAdd, contentDescription = "Add User", tint = Color.White)
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(users) { user ->
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(user.email, color = Color.White, fontWeight = FontWeight.Bold)
                            Text(user.role.name, color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
                        }
                        Switch(
                            checked = user.isActive,
                            onCheckedChange = { active ->
                                val index = users.indexOf(user)
                                if (index != -1) {
                                    users[index] = user.copy(isActive = active)
                                    onLogAction("User Status Updated: ${user.email} -> $active", adminEmail)
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    if (showAddUserDialog) {
        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var role by remember { mutableStateOf(UserRole.USER) }

        AlertDialog(
            onDismissRequest = { showAddUserDialog = false },
            title = { Text("Create CRM User", color = Color.White) },
            containerColor = Color(0xFF1B2735),
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    CRMTextField(email, { email = it }, "Email")
                    CRMTextField(password, { password = it }, "Password", isPassword = true)
                    Text("Role:", color = Color.White, fontSize = 12.sp)
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        UserRole.entries.forEach { r ->
                            FilterChip(
                                selected = role == r,
                                onClick = { role = r },
                                label = { Text(r.name) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    users.add(User(email, password, role))
                    onLogAction("Created User: $email ($role)", adminEmail)
                    showAddUserDialog = false
                }) { Text("Create") }
            },
            dismissButton = { TextButton(onClick = { showAddUserDialog = false }) { Text("Cancel") } }
        )
    }
}

@Composable
fun AuditLogSection(logs: List<ActivityLog>) {
    val dateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    Column {
        Text("System Audit Logs", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(logs) { log ->
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Text(dateFormat.format(Date(log.timestamp)), color = Color.White.copy(alpha = 0.4f), fontSize = 12.sp, modifier = Modifier.width(64.dp))
                    Column {
                        Text(log.action, color = Color.White, fontSize = 14.sp)
                        Text("by ${log.user}", color = Color(0xFF64B5F6), fontSize = 11.sp)
                    }
                }
                HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
            }
        }
    }
}

@Composable
fun PipelineSetupSection() {
    Column {
        Text("Pipeline Configuration", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Standard Sales Stages", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
                LeadStatus.entries.forEachIndexed { index, status ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("${index + 1}.", color = Color.White, modifier = Modifier.width(24.dp))
                        Text(status.name, color = Color.White, fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(Icons.Default.Edit, contentDescription = null, tint = Color.White.copy(alpha = 0.3f), modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text("Data Import", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        FrostedGlassButton("Import Contacts (CSV)") { /* MVP Scope: UI Placeholder */ }
    }
}

@Composable
fun FrostedGlassTabButton(text: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val alpha = if (selected) 0.2f else 0.05f
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = alpha))
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = if (selected) Color.White else Color.White.copy(alpha = 0.5f), fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}
