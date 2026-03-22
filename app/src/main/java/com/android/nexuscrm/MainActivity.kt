package com.android.nexuscrm

import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.graphics.Color
import androidx.fragment.app.FragmentActivity
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.android.nexuscrm.model.*
import com.android.nexuscrm.ui.components.*
import com.android.nexuscrm.ui.screens.*

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            val userDatabase = remember { 
                mutableStateListOf(
                    User("admin@crm.com", "admin123", UserRole.ADMIN),
                    User("user@crm.com", "user123", UserRole.USER),
                    User("manager@crm.com", "manager123", UserRole.MANAGER)
                )
            }

            val leads = remember {
                mutableStateListOf(
                    Lead(1, "John Doe", "TechCorp", 5000.0, LeadStatus.NEW, "555-0101", "john@techcorp.com", mutableListOf(Interaction("Note", "Met at conference"))),
                    Lead(2, "Jane Smith", "DesignHub", 12000.0, LeadStatus.PROPOSAL, "555-0102", "jane@designhub.com"),
                    Lead(3, "Robert Brown", "LogiLink", 2500.0, LeadStatus.CONTACTED, "555-0103", "rob@logilink.net"),
                    Lead(4, "Alice Green", "EcoBuild", 20000.0, LeadStatus.CLOSED, "555-0104", "alice@ecobuild.com")
                )
            }
            val tasks = remember {
                mutableStateListOf(
                    CRMTask(1, "Follow up with John Doe", "Today", contactName = "John Doe"),
                    CRMTask(2, "Send proposal to TechCorp", "Tomorrow", contactName = "Jane Smith"),
                    CRMTask(3, "Quarterly Review", "Next Week")
                )
            }
            val activityLogs = remember {
                mutableStateListOf(
                    ActivityLog(1, "System Initialized", "System"),
                    ActivityLog(2, "Default Leads Loaded", "System")
                )
            }

            fun logAction(action: String, user: String) {
                val id = (activityLogs.maxOfOrNull { it.id } ?: 0) + 1
                activityLogs.add(0, ActivityLog(id, action, user))
            }

            MaterialTheme(
                colorScheme = darkColorScheme(
                    primary = Color(0xFF64B5F6),
                    secondary = Color(0xFF81C784),
                    background = Color(0xFF0F2027),
                    surface = Color(0xFF1B2735)
                )
            ) {
                val navController = rememberNavController()
                var currentUser by remember { mutableStateOf<User?>(null) }

                NavHost(navController = navController, startDestination = "login") {
                    composable("login") {
                        val showExitDialog = remember { mutableStateOf(false) }
                        if (showExitDialog.value) {
                            ExitAlertDialog(onConfirm = { finish() }, onDismiss = { showExitDialog.value = false })
                        }
                        BackHandler(enabled = !showExitDialog.value) { showExitDialog.value = true }
                        
                        LoginScreen(
                            onLoginSuccess = { email, password ->
                                val user = userDatabase.find { it.email == email && it.password == password }
                                if (user != null) {
                                    if (!user.isActive) return@LoginScreen false
                                    currentUser = user
                                    logAction("User Logged In", user.email)
                                    navController.navigate("main_crm") { popUpTo("login") { inclusive = true } }
                                    true
                                } else false
                            },
                            onRegisterClicked = { navController.navigate("register") },
                            onBiometricSuccess = {
                                val user = userDatabase.find { it.role == UserRole.ADMIN }
                                if (user != null) {
                                    currentUser = user
                                    logAction("Biometric Login Success", user.email)
                                    navController.navigate("main_crm") { popUpTo("login") { inclusive = true } }
                                }
                            }
                        )
                    }

                    composable("register") {
                        val showExitDialog = remember { mutableStateOf(false) }
                        if (showExitDialog.value) {
                            ExitAlertDialog(onConfirm = { finish() }, onDismiss = { showExitDialog.value = false })
                        }
                        BackHandler(enabled = !showExitDialog.value) { showExitDialog.value = true }

                        RegisterScreen(
                            onRegisterSuccess = { email, password ->
                                if (userDatabase.any { it.email == email }) "Email already exists"
                                else {
                                    userDatabase.add(User(email, password, UserRole.USER))
                                    logAction("New User Registered", email)
                                    navController.navigate("login")
                                    null
                                }
                            },
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable("main_crm") {
                        MainCRMContainer(
                            currentUser = currentUser,
                            leads = leads,
                            tasks = tasks,
                            users = userDatabase,
                            logs = activityLogs,
                            onLogAction = ::logAction,
                            onLogout = {
                                currentUser?.let { logAction("User Logged Out", it.email) }
                                navController.navigate("login") { popUpTo("main_crm") { inclusive = true } }
                            }
                        )
                    }
                }
            }
        }
    }
}
