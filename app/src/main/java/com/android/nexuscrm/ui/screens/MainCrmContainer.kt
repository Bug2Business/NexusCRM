package com.android.nexuscrm.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.nexuscrm.R
import com.android.nexuscrm.model.*
import com.android.nexuscrm.ui.components.AddLeadDialog
import com.android.nexuscrm.ui.components.AddTaskDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainCRMContainer(
    currentUser: User?,
    leads: SnapshotStateList<Lead>,
    tasks: SnapshotStateList<CRMTask>,
    users: SnapshotStateList<User>,
    logs: SnapshotStateList<ActivityLog>,
    onLogAction: (String, String) -> Unit,
    onLogout: () -> Unit
) {
    val isAdmin = currentUser?.role == UserRole.ADMIN
    val tabs = if (isAdmin) listOf("Dashboard", "Leads", "Tasks", "Admin") else listOf("Dashboard", "Leads", "Tasks")
    var selectedTab by remember { mutableIntStateOf(0) }
    var showAddLeadDialog by remember { mutableStateOf(false) }
    var showAddTaskDialog by remember { mutableStateOf(false) }
    var selectedLeadForDetail by remember { mutableStateOf<Lead?>(null) }
    
    Box(modifier = Modifier.fillMaxSize()) {
        Image(painter = painterResource(id = R.drawable.anime_bg), contentDescription = null, modifier = Modifier.fillMaxSize().blur(20.dp), contentScale = ContentScale.Crop)
        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f)))

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text("NexusCRM Central", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            Text("${currentUser?.email} [${currentUser?.role}]", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
                        }
                    },
                    actions = {
                        IconButton(onClick = onLogout) { Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Logout", tint = Color.White) }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White.copy(alpha = 0.05f))
                )
            },
            bottomBar = {
                NavigationBar(containerColor = Color.White.copy(alpha = 0.05f), tonalElevation = 0.dp) {
                    tabs.forEachIndexed { index, title ->
                        NavigationBarItem(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index; selectedLeadForDetail = null },
                            icon = { Icon(imageVector = when(title) { 
                                "Dashboard" -> Icons.Default.Dashboard 
                                "Leads" -> Icons.Default.People
                                "Tasks" -> Icons.Default.Checklist
                                else -> Icons.Default.AdminPanelSettings
                            }, contentDescription = title) },
                            label = { Text(title) },
                            colors = NavigationBarItemDefaults.colors(selectedIconColor = Color.White, unselectedIconColor = Color.White.copy(alpha = 0.4f), indicatorColor = Color.White.copy(alpha = 0.1f))
                        )
                    }
                }
            },
            floatingActionButton = {
                if ((selectedTab == 1 || selectedTab == 2) && selectedLeadForDetail == null) {
                    FloatingActionButton(
                        onClick = { if (selectedTab == 1) showAddLeadDialog = true else showAddTaskDialog = true },
                        containerColor = Color(0xFF64B5F6), contentColor = Color.White, shape = CircleShape
                    ) { Icon(Icons.Default.Add, contentDescription = "Add") }
                }
            }
        ) { padding ->
            Column(modifier = Modifier.padding(padding).fillMaxSize()) {
                AnimatedContent(targetState = selectedTab, transitionSpec = { fadeIn() togetherWith fadeOut() }, label = "tab_content") { targetIndex ->
                    val target = tabs.getOrNull(targetIndex) ?: "Dashboard"
                    when(target) {
                        "Dashboard" -> DashboardScreen(leads, tasks)
                        "Leads" -> {
                            if (selectedLeadForDetail != null) {
                                ContactDetailScreen(
                                    lead = selectedLeadForDetail!!,
                                    onBack = { selectedLeadForDetail = null },
                                    onAddInteraction = { type, content ->
                                        val index = leads.indexOf(selectedLeadForDetail)
                                        if (index != -1) {
                                            leads[index].notes.add(0, Interaction(type, content))
                                            onLogAction("Interaction logged for ${selectedLeadForDetail?.name}", currentUser?.email ?: "")
                                        }
                                    }
                                )
                            } else {
                                LeadsScreen(leads, 
                                    onDelete = { 
                                        leads.remove(it)
                                        onLogAction("Lead Deleted: ${it.name}", currentUser?.email ?: "Unknown")
                                    },
                                    onStatusChange = { lead, newStatus ->
                                        val index = leads.indexOf(lead)
                                        if (index != -1) {
                                            leads[index] = lead.copy(status = newStatus)
                                            onLogAction("Lead Status Changed: ${lead.name} to $newStatus", currentUser?.email ?: "Unknown")
                                        }
                                    },
                                    onSelectLead = { selectedLeadForDetail = it }
                                )
                            }
                        }
                        "Tasks" -> TasksScreen(tasks, 
                            onToggle = { task ->
                                val index = tasks.indexOf(task)
                                if (index != -1) {
                                    tasks[index] = task.copy(isCompleted = !task.isCompleted)
                                    onLogAction("Task ${if (task.isCompleted) "Reopened" else "Completed"}: ${task.title}", currentUser?.email ?: "Unknown")
                                }
                            },
                            onDelete = { 
                                tasks.remove(it) 
                                onLogAction("Task Deleted: ${it.title}", currentUser?.email ?: "Unknown")
                            }
                        )
                        "Admin" -> AdminScreen(users, logs, onLogAction, currentUser?.email ?: "")
                    }
                }
            }
        }
    }

    if (showAddLeadDialog) {
        AddLeadDialog(
            onDismiss = { showAddLeadDialog = false },
            onAdd = { name, company, value, phone, email ->
                val id = (leads.maxOfOrNull { it.id } ?: 0) + 1
                leads.add(Lead(id, name, company, value.toDoubleOrNull() ?: 0.0, LeadStatus.NEW, phone, email))
                onLogAction("New Lead Added: $name", currentUser?.email ?: "Unknown")
                showAddLeadDialog = false
            }
        )
    }

    if (showAddTaskDialog) {
        AddTaskDialog(
            onDismiss = { showAddTaskDialog = false },
            onAdd = { title, deadline, contact ->
                val id = (tasks.maxOfOrNull { it.id } ?: 0) + 1
                tasks.add(CRMTask(id, title, deadline, contactName = contact))
                onLogAction("New Task Added: $title", currentUser?.email ?: "Unknown")
                showAddTaskDialog = false
            }
        )
    }
}
