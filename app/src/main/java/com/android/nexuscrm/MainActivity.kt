package com.android.nexuscrm

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.BackHandler
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.compose.*
import java.util.Calendar
import java.util.concurrent.Executor

// --- CRM DATA MODELS ---

enum class UserRole { ADMIN, USER }
data class User(val email: String, val password: String, val role: UserRole)

enum class LeadStatus { NEW, CONTACTED, PROPOSAL, CLOSED }
data class Lead(
    val id: Int,
    val name: String,
    val company: String,
    val value: String,
    val status: LeadStatus
)

data class CRMTask(
    val id: Int,
    val title: String,
    val deadline: String,
    val isCompleted: Boolean = false
)

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            val userDatabase = remember { 
                mutableStateListOf(
                    User("admin@crm.com", "admin123", UserRole.ADMIN),
                    User("user@crm.com", "user123", UserRole.USER)
                )
            }

            // Global State for Leads & Tasks
            val leads = remember {
                mutableStateListOf(
                    Lead(1, "John Doe", "TechCorp", "$5,000", LeadStatus.NEW),
                    Lead(2, "Jane Smith", "DesignHub", "$12,000", LeadStatus.PROPOSAL),
                    Lead(3, "Robert Brown", "LogiLink", "$2,500", LeadStatus.CONTACTED),
                    Lead(4, "Alice Green", "EcoBuild", "$20,000", LeadStatus.CLOSED)
                )
            }
            val tasks = remember {
                mutableStateListOf(
                    CRMTask(1, "Follow up with John Doe", "Today"),
                    CRMTask(2, "Send proposal to TechCorp", "Tomorrow"),
                    CRMTask(3, "Quarterly Review", "Next Week")
                )
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

                NavHost(
                    navController = navController,
                    startDestination = "login"
                ) {
                    composable("login") {
                        val showExitDialog = remember { mutableStateOf(false) }
                        if (showExitDialog.value) {
                            AlertDialog(
                                onDismissRequest = { showExitDialog.value = false },
                                title = { Text("Exit App") },
                                text = { Text("Are you sure you want to exit?") },
                                confirmButton = {
                                    TextButton(onClick = { finish() }) {
                                        Text("Exit")
                                    }
                                },
                                dismissButton = {
                                    TextButton(onClick = { showExitDialog.value = false }) {
                                        Text("Cancel")
                                    }
                                }
                            )
                        }
                        BackHandler(enabled = !showExitDialog.value) { showExitDialog.value = true }
                        
                        LoginScreen(
                            onLoginSuccess = { email, password ->
                                val user = userDatabase.find { it.email == email && it.password == password }
                                if (user != null) {
                                    currentUser = user
                                    navController.navigate("main_crm") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                    true
                                } else false
                            },
                            onRegisterClicked = { navController.navigate("register") },
                            onBiometricSuccess = {
                                currentUser = userDatabase.find { it.role == UserRole.ADMIN }
                                navController.navigate("main_crm") {
                                    popUpTo("login") { inclusive = true }
                                }
                            }
                        )
                    }

                    composable("register") {
                        val showExitDialog = remember { mutableStateOf(false) }
                        if (showExitDialog.value) {
                            AlertDialog(
                                onDismissRequest = { showExitDialog.value = false },
                                title = { Text("Exit App") },
                                text = { Text("Are you sure you want to exit?") },
                                confirmButton = {
                                    TextButton(onClick = { finish() }) {
                                        Text("Exit")
                                    }
                                },
                                dismissButton = {
                                    TextButton(onClick = { showExitDialog.value = false }) {
                                        Text("Cancel")
                                    }
                                }
                            )
                        }
                        BackHandler(enabled = !showExitDialog.value) { showExitDialog.value = true }

                        RegisterScreen(
                            onRegisterSuccess = { email, password ->
                                if (userDatabase.any { it.email == email }) "Email already exists"
                                else {
                                    userDatabase.add(User(email, password, UserRole.USER))
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
                            onLogout = {
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

/* ---------------- LOGIN & REGISTER (Maintaining Glassmorphism) ---------------- */

@Composable
fun LoginScreen(
    onLoginSuccess: (String, String) -> Boolean,
    onRegisterClicked: () -> Unit,
    onBiometricSuccess: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? FragmentActivity

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.anime_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize().blur(12.dp),
            contentScale = ContentScale.Crop
        )

        Box(modifier = Modifier.fillMaxSize().background(
            brush = Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.4f), Color.Black.copy(alpha = 0.7f)))
        ))

        Column(
            modifier = Modifier.fillMaxWidth().padding(24.dp).align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            GreetingHeader()

            Spacer(modifier = Modifier.height(32.dp))

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    var email by remember { mutableStateOf("") }
                    var password by remember { mutableStateOf("") }
                    var error by remember { mutableStateOf("") }

                    CRMTextField(value = email, onValueChange = { email = it }, label = "Email")
                    Spacer(modifier = Modifier.height(12.dp))
                    CRMTextField(value = password, onValueChange = { password = it }, label = "Password", isPassword = true)

                    if (error.isNotEmpty()) {
                        Text(text = error, color = Color.Red.copy(alpha = 0.8f), fontSize = 14.sp, modifier = Modifier.padding(top = 8.dp))
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    FrostedGlassButton(text = "Login") {
                        error = if (onLoginSuccess(email, password)) ""
                        else "Invalid Credentials"
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    TextButton(onClick = onRegisterClicked) {
                        Text("Create New Account", color = Color.White.copy(alpha = 0.8f))
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Biometric
                    val bm = BiometricManager.from(context)
                    if (bm.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL) == BiometricManager.BIOMETRIC_SUCCESS) {
                        IconButton(
                            onClick = { activity?.let { showBiometricPrompt(it, onBiometricSuccess) } },
                            modifier = Modifier.size(48.dp).background(Color.White.copy(alpha = 0.1f), CircleShape)
                        ) {
                            Icon(Icons.Default.Fingerprint, contentDescription = "Biometric", tint = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RegisterScreen(
    onRegisterSuccess: (String, String) -> String?,
    onBack: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Image(painter = painterResource(id = R.drawable.anime_bg), contentDescription = null, modifier = Modifier.fillMaxSize().blur(12.dp), contentScale = ContentScale.Crop)
        Box(modifier = Modifier.fillMaxSize().background(brush = Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.4f), Color.Black.copy(alpha = 0.7f)))))

        Column(modifier = Modifier.fillMaxWidth().padding(24.dp).align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Create Account", fontSize = 32.sp, color = Color.White, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(24.dp))
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    var email by remember { mutableStateOf("") }
                    var pass by remember { mutableStateOf("") }
                    var confirmPass by remember { mutableStateOf("") }
                    var error by remember { mutableStateOf("") }

                    CRMTextField(email, { email = it }, "Email")
                    Spacer(modifier = Modifier.height(12.dp))
                    CRMTextField(pass, { pass = it }, "Password", isPassword = true)
                    Spacer(modifier = Modifier.height(12.dp))
                    CRMTextField(confirmPass, { confirmPass = it }, "Confirm Password", isPassword = true)

                    if (error.isNotEmpty()) {
                        Text(text = error, color = Color.White, fontSize = 14.sp, modifier = Modifier.padding(top = 8.dp))
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    FrostedGlassButton("Register") {
                        if (pass != confirmPass) {
                            error = "Passwords do not match"
                        } else {
                            val msg = onRegisterSuccess(email, pass)
                            if (msg != null) error = msg
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    TextButton(onClick = onBack) {
                        Text("Already have an account? Login", color = Color.White.copy(alpha = 0.8f))
                    }
                }
            }
        }
    }
}

/* ---------------- MAIN CRM CONTAINER ---------------- */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainCRMContainer(
    currentUser: User?,
    leads: List<Lead>,
    tasks: List<CRMTask>,
    onLogout: () -> Unit
) {
    val tabs = listOf("Dashboard", "Leads", "Tasks")
    var selectedTab by remember { mutableIntStateOf(0) }
    
    Box(modifier = Modifier.fillMaxSize()) {
        // Shared Background for consistent UI
        Image(
            painter = painterResource(id = R.drawable.anime_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize().blur(20.dp),
            contentScale = ContentScale.Crop
        )
        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f)))

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text("NexusCRM Central", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            Text(currentUser?.email ?: "Guest", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
                        }
                    },
                    actions = {
                        IconButton(onClick = onLogout) {
                            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Logout", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White.copy(alpha = 0.05f))
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = Color.White.copy(alpha = 0.05f),
                    tonalElevation = 0.dp
                ) {
                    tabs.forEachIndexed { index, title ->
                        NavigationBarItem(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            icon = {
                                Icon(
                                    imageVector = when(index) {
                                        0 -> Icons.Default.Dashboard
                                        1 -> Icons.Default.People
                                        else -> Icons.Default.Checklist
                                    },
                                    contentDescription = title
                                )
                            },
                            label = { Text(title) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.White,
                                unselectedIconColor = Color.White.copy(alpha = 0.4f),
                                indicatorColor = Color.White.copy(alpha = 0.1f)
                            )
                        )
                    }
                }
            }
        ) { padding ->
            Column(modifier = Modifier.padding(padding).fillMaxSize()) {
                AnimatedContent(
                    targetState = selectedTab,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "tab_content"
                ) { target ->
                    when(target) {
                        0 -> DashboardScreen(leads, tasks)
                        1 -> LeadsScreen(leads)
                        2 -> TasksScreen(tasks)
                    }
                }
            }
        }
    }
}

/* ---------------- ADVANCED CRM SCREENS ---------------- */

@Composable
fun DashboardScreen(leads: List<Lead>, tasks: List<CRMTask>) {
    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        item {
            Text("Overview", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                StatCard("Active Leads", leads.count { it.status != LeadStatus.CLOSED }.toString(), Modifier.weight(1f))
                StatCard("Revenue", "$45.2k", Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text("Conversion Progress", color = Color.White, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    LinearProgressIndicator(
                        progress = { 0.65f },
                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                        color = Color(0xFF64B5F6),
                        trackColor = Color.White.copy(alpha = 0.1f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("65% of target reached", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        item {
            Text("Urgent Tasks", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
        }

        items(tasks.take(3)) { task ->
            TaskRow(task)
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun LeadsScreen(leads: List<Lead>) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        var searchQuery by remember { mutableStateOf("") }
        
        Text("Your Leads", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        
        CRMTextField(searchQuery, { searchQuery = it }, "Search Leads...", leadingIcon = Icons.Default.Search)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(leads.filter { it.name.contains(searchQuery, true) || it.company.contains(searchQuery, true) }) { lead ->
                LeadCard(lead)
            }
        }
    }
}

@Composable
fun TasksScreen(tasks: List<CRMTask>) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Daily Agenda", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(tasks) { task ->
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = task.isCompleted,
                            onCheckedChange = {},
                            colors = CheckboxDefaults.colors(checkmarkColor = Color.Black)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(task.title, color = Color.White, fontWeight = FontWeight.Medium)
                            Text(task.deadline, color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

/* ---------------- UI COMPONENTS ---------------- */

@Composable
fun StatCard(title: String, value: String, modifier: Modifier = Modifier) {
    GlassCard(modifier = modifier) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
            Text(value, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
fun LeadCard(lead: Lead) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text(lead.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(lead.company, color = Color.White.copy(alpha = 0.7f), fontSize = 13.sp)
                Text(lead.value, color = Color(0xFF64B5F6), fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
            StatusBadge(lead.status)
        }
    }
}

@Composable
fun StatusBadge(status: LeadStatus) {
    val color = when(status) {
        LeadStatus.NEW -> Color(0xFF64B5F6)
        LeadStatus.CONTACTED -> Color(0xFFFFB74D)
        LeadStatus.PROPOSAL -> Color(0xFFBA68C8)
        LeadStatus.CLOSED -> Color(0xFF81C784)
    }
    Box(
        modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(color.copy(alpha = 0.2f)).border(1.dp, color.copy(alpha = 0.5f), RoundedCornerShape(8.dp)).padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(status.name, color = color, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun TaskRow(task: CRMTask) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFFE57373)))
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(task.title, color = Color.White, fontSize = 14.sp)
                Text(task.deadline, color = Color.White.copy(alpha = 0.4f), fontSize = 11.sp)
            }
        }
    }
}

@Composable
fun GlassCard(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White.copy(alpha = 0.08f))
            .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(24.dp))
            .padding(16.dp)
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
    leadingIcon: ImageVector? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        leadingIcon = leadingIcon?.let { { Icon(it, contentDescription = null, tint = Color.White.copy(alpha = 0.6f)) } },
        visualTransformation = if (isPassword) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
        modifier = Modifier.fillMaxWidth(),
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
        modifier = Modifier.fillMaxWidth().height(54.dp).shadow(12.dp, RoundedCornerShape(27.dp)),
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

fun showBiometricPrompt(activity: FragmentActivity, onSuccess: () -> Unit) {
    val executor: Executor = ContextCompat.getMainExecutor(activity)
    val biometricPrompt = BiometricPrompt(activity, executor, object : BiometricPrompt.AuthenticationCallback() {
        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) { onSuccess() }
    })
    val promptInfo = BiometricPrompt.PromptInfo.Builder()
        .setTitle("NexusCRM Access")
        .setSubtitle("Authenticate to continue")
        .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)
        .build()
    biometricPrompt.authenticate(promptInfo)
}
