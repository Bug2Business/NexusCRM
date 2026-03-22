package com.android.nexuscrm.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.nexuscrm.model.Lead
import com.android.nexuscrm.model.LeadStatus
import com.android.nexuscrm.ui.components.CRMTextField
import com.android.nexuscrm.ui.components.GlassCard
import com.android.nexuscrm.ui.components.StatusBadge

@Composable
fun LeadsScreen(leads: List<Lead>, onDelete: (Lead) -> Unit, onStatusChange: (Lead, LeadStatus) -> Unit, onSelectLead: (Lead) -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        var searchQuery by remember { mutableStateOf("") }
        Text("Your Leads", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold); Spacer(modifier = Modifier.height(16.dp))
        CRMTextField(searchQuery, { searchQuery = it }, "Search Leads...", leadingIcon = Icons.Default.Search)
        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            val filteredLeads = leads.filter { it.name.contains(searchQuery, true) || it.company.contains(searchQuery, true) }
            if (filteredLeads.isEmpty()) {
                item { Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) { Text("No leads found", color = Color.White.copy(alpha = 0.5f)) } }
            }
            items(filteredLeads) { lead -> LeadCard(lead, onDelete, onStatusChange, onSelectLead) }
        }
    }
}

@Composable
fun LeadCard(lead: Lead, onDelete: (Lead) -> Unit, onStatusChange: (Lead, LeadStatus) -> Unit, onSelectLead: (Lead) -> Unit) {
    var showStatusMenu by remember { mutableStateOf(false) }

    GlassCard(modifier = Modifier.fillMaxWidth().clickable { onSelectLead(lead) }) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(lead.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(lead.company, color = Color.White.copy(alpha = 0.7f), fontSize = 13.sp)
                if (lead.phone.isNotEmpty()) Text(lead.phone, color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
                Text("$${"%,.0f".format(lead.value)}", color = Color(0xFF64B5F6), fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box {
                    StatusBadge(lead.status, onClick = { showStatusMenu = true })
                    DropdownMenu(
                        expanded = showStatusMenu,
                        onDismissRequest = { showStatusMenu = false },
                        modifier = Modifier.background(Color(0xFF1B2735))
                    ) {
                        LeadStatus.entries.forEach { status ->
                            DropdownMenuItem(
                                text = { Text(status.name, color = if (status == lead.status) Color(0xFF64B5F6) else Color.White) },
                                onClick = { onStatusChange(lead, status); showStatusMenu = false }
                            )
                        }
                    }
                }
                IconButton(onClick = { onDelete(lead) }) { Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.White.copy(alpha = 0.4f)) }
            }
        }
    }
}
