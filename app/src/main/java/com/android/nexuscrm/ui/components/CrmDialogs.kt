package com.android.nexuscrm.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ExitAlertDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Exit App") },
        text = { Text("Are you sure you want to exit?") },
        confirmButton = { TextButton(onClick = onConfirm) { Text("Exit") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun AddLeadDialog(onDismiss: () -> Unit, onAdd: (String, String, String, String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var company by remember { mutableStateOf("") }
    var value by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Lead", color = Color.White) },
        containerColor = Color(0xFF1B2735),
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                CRMTextField(name, { name = it }, "Name")
                CRMTextField(company, { company = it }, "Company")
                CRMTextField(email, { email = it }, "Email", keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email))
                CRMTextField(value, { value = it }, "Value ($)", keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                CRMTextField(phone, { phone = it }, "Phone", keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone))
                if (error.isNotEmpty()) Text(error, color = Color.Red, fontSize = 12.sp)
            }
        },
        confirmButton = {
            Button(onClick = {
                if (name.isBlank() || company.isBlank()) error = "Fields cannot be empty"
                else onAdd(name, company, value, phone, email)
            }) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = Color.White) } }
    )
}

@Composable
fun AddTaskDialog(onDismiss: () -> Unit, onAdd: (String, String, String) -> Unit) {
    var title by remember { mutableStateOf("") }
    var deadline by remember { mutableStateOf("") }
    var contact by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Task", color = Color.White) },
        containerColor = Color(0xFF1B2735),
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                CRMTextField(title, { title = it }, "Task Title")
                CRMTextField(contact, { contact = it }, "Related Contact (Optional)")
                CRMTextField(deadline, { deadline = it }, "Deadline (e.g. Today)")
                if (error.isNotEmpty()) Text(error, color = Color.Red, fontSize = 12.sp)
            }
        },
        confirmButton = {
            Button(onClick = {
                if (title.isBlank()) error = "Title cannot be empty"
                else onAdd(title, deadline, contact)
            }) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = Color.White) } }
    )
}
