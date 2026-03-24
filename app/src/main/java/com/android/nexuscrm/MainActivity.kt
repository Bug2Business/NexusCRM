package com.android.nexuscrm

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.navigation.compose.*
import com.android.nexuscrm.firebase.FirebaseAuthManager
import com.android.nexuscrm.model.*
import com.android.nexuscrm.ui.components.*
import com.android.nexuscrm.ui.screens.*
import com.android.nexuscrm.ui.theme.NexusCRMTheme
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObjects

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Firebase
        FirebaseApp.initializeApp(this)
        val db = FirebaseFirestore.getInstance()

        setContent {
            NexusCRMTheme {
                // Root container to ensure no white flashes during navigation
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black)
                ) {
                    val navController = rememberNavController()
                    var currentUser by remember { mutableStateOf<User?>(null) }

                    // Real-time data lists synced with Firebase
                    val leads = remember { mutableStateListOf<Lead>() }
                    val tasks = remember { mutableStateListOf<CRMTask>() }
                    val logs = remember { mutableStateListOf<ActivityLog>() }
                    val userDatabase = remember { mutableStateListOf<User>() }

                    // Sync logic: Only runs when a user is logged in
                    LaunchedEffect(currentUser) {
                        if (currentUser != null) {
                            // Listen for Leads
                            db.collection("leads").addSnapshotListener { snapshot, _ ->
                                snapshot?.let {
                                    leads.clear()
                                    leads.addAll(it.toObjects<Lead>())
                                }
                            }
                            // Listen for Tasks
                            db.collection("tasks").addSnapshotListener { snapshot, _ ->
                                snapshot?.let {
                                    tasks.clear()
                                    tasks.addAll(it.toObjects<CRMTask>())
                                }
                            }
                            // Listen for Users
                            db.collection("users").addSnapshotListener { snapshot, _ ->
                                snapshot?.let {
                                    userDatabase.clear()
                                    userDatabase.addAll(it.toObjects<User>())
                                }
                            }
                        }
                    }

                    // Cloud Logging Function
                    fun logAction(action: String, userEmail: String) {
                        val newLog = ActivityLog(
                            id = System.currentTimeMillis().toInt(),
                            action = action,
                            user = userEmail
                        )
                        db.collection("logs").add(newLog)
                    }

                    Box(modifier = Modifier.fillMaxSize().graphicsLayer { alpha = 0.99f }) {
                        NavHost(
                            navController = navController,
                            startDestination = "login"
                        ) {
                            // 🔐 LOGIN
                            composable("login") {
                                val showExitDialog = remember { mutableStateOf(false) }

                                if (showExitDialog.value) {
                                    ExitAlertDialog(
                                        onConfirm = { finish() },
                                        onDismiss = { showExitDialog.value = false }
                                    )
                                }

                                BackHandler(enabled = !showExitDialog.value) {
                                    showExitDialog.value = true
                                }

                                LoginScreen(
                                    onLoginSuccess = { email, password ->
                                        FirebaseAuthManager.loginUser(
                                            email,
                                            password,
                                            onSuccess = {
                                                currentUser = User(email, "", UserRole.USER)
                                                navController.navigate("main_crm") {
                                                    popUpTo("login") { inclusive = true }
                                                }
                                            },
                                            onError = { /* Error handled in UI */ }
                                        )
                                        true
                                    },
                                    onRegisterClicked = {
                                        navController.navigate("register")
                                    }
                                )
                            }

                            // 📝 REGISTER
                            composable("register") {
                                RegisterScreen(
                                    onRegisterSuccess = { email, password ->
                                        FirebaseAuthManager.registerUser(
                                            email,
                                            password,
                                            onSuccess = {
                                                val newUser = User(email, "", UserRole.USER)
                                                db.collection("users").document(email).set(newUser)
                                                navController.navigate("login")
                                            },
                                            onError = { /* Error handled in UI */ }
                                        )
                                        null
                                    },
                                    onBack = {
                                        navController.popBackStack()
                                    }
                                )
                            }

                            // 🏠 MAIN CRM
                            composable("main_crm") {
                                MainCRMContainer(
                                    currentUser = currentUser,
                                    leads = leads,
                                    tasks = tasks,
                                    users = userDatabase,
                                    logs = logs,
                                    onLogAction = ::logAction,
                                    onLogout = {
                                        FirebaseAuthManager.logout()
                                        currentUser = null
                                        navController.navigate("login") {
                                            popUpTo("main_crm") { inclusive = true }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}