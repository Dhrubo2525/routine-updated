package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.ClassRoutine
import com.example.ui.components.GlobalBanner
import com.example.ui.components.ZoneVibe
import com.example.ui.viewmodel.AcademicViewModel
import kotlinx.coroutines.launch

enum class AdminTab {
    ROUTINE,
    BANNER,
    ALERTS,
    BACKUP,
    ACCOUNT
}

@Composable
fun AdminZoneScreen(
    viewModel: AcademicViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val isAdminLoggedIn by viewModel.isAdminLoggedIn.collectAsStateWithLifecycle()
    val bannerSettings by viewModel.bannerSettings.collectAsStateWithLifecycle()
    val appSettings by viewModel.appSettings.collectAsStateWithLifecycle()
    val allClasses by viewModel.allClasses.collectAsStateWithLifecycle()
    val pendingImport by viewModel.pendingImport.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableStateOf(AdminTab.ROUTINE) }

    // Login Form State
    var loginUsername by remember { mutableStateOf("") }
    var loginPassword by remember { mutableStateOf("") }
    var loginError by remember { mutableStateOf(false) }

    // Dialogs
    var showAddClassDialog by remember { mutableStateOf(false) }
    var classToEdit by remember { mutableStateOf<ClassRoutine?>(null) }
    var showImportDialog by remember { mutableStateOf(false) }
    var showDeleteAllDialog by remember { mutableStateOf(false) }
    var importJsonText by remember { mutableStateOf("") }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("admin_zone_screen"),
        contentPadding = PaddingValues(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Global Banner
        item {
            GlobalBanner(
                bannerSettings = bannerSettings,
                currentZone = ZoneVibe.ADMIN
            )
        }

        if (!isAdminLoggedIn) {
            // Admin Authentication Card (Section 4, 31)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .testTag("admin_login_card"),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(54.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }

                        Text(
                            text = "Admin Zone Login",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Manage routines, faculty directory, quizzes, and global banner settings.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )

                        OutlinedTextField(
                            value = loginUsername,
                            onValueChange = {
                                loginUsername = it
                                loginError = false
                            },
                            label = { Text("Username") },
                            modifier = Modifier.fillMaxWidth().testTag("admin_username_input"),
                            shape = RoundedCornerShape(10.dp)
                        )

                        OutlinedTextField(
                            value = loginPassword,
                            onValueChange = {
                                loginPassword = it
                                loginError = false
                            },
                            label = { Text("Password") },
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth().testTag("admin_password_input"),
                            shape = RoundedCornerShape(10.dp)
                        )

                        if (loginError) {
                            Text(
                                text = "Invalid credentials. (Default: admin / admin)",
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 12.sp
                            )
                        }

                        Button(
                            onClick = {
                                scope.launch {
                                    val ok = viewModel.loginAdmin(loginPassword)
                                    if (!ok) {
                                        loginError = true
                                    } else {
                                        loginPassword = ""
                                        loginError = false
                                        Toast.makeText(context, "Logged in as Admin", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth().testTag("admin_login_submit_btn"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Login to Admin Zone", fontWeight = FontWeight.Bold)
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = "Default: admin / admin",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        } else {
            // Authenticated Admin Dashboard
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Header Bar with Logout
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Admin Control Panel",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "Authorized Manager",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Button(
                            onClick = {
                                viewModel.logoutAdmin()
                                Toast.makeText(context, "Logged out", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                contentColor = MaterialTheme.colorScheme.onErrorContainer
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.testTag("admin_logout_btn")
                        ) {
                            Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Logout", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Admin Navigation Tabs
                    ScrollableTabRow(
                        selectedTabIndex = selectedTab.ordinal,
                        edgePadding = 0.dp,
                        modifier = Modifier.fillMaxWidth().testTag("admin_tabs_row")
                    ) {
                        Tab(
                            selected = selectedTab == AdminTab.ROUTINE,
                            onClick = { selectedTab = AdminTab.ROUTINE },
                            text = { Text("Routine", fontSize = 12.sp) }
                        )
                        Tab(
                            selected = selectedTab == AdminTab.BANNER,
                            onClick = { selectedTab = AdminTab.BANNER },
                            text = { Text("Banner", fontSize = 12.sp) }
                        )
                        Tab(
                            selected = selectedTab == AdminTab.ALERTS,
                            onClick = { selectedTab = AdminTab.ALERTS },
                            text = { Text("Alerts", fontSize = 12.sp) }
                        )
                        Tab(
                            selected = selectedTab == AdminTab.BACKUP,
                            onClick = { selectedTab = AdminTab.BACKUP },
                            text = { Text("Backup", fontSize = 12.sp) }
                        )
                        Tab(
                            selected = selectedTab == AdminTab.ACCOUNT,
                            onClick = { selectedTab = AdminTab.ACCOUNT },
                            text = { Text("Account", fontSize = 12.sp) }
                        )
                    }
                }
            }

            // Tab 1: Routine Management (Section 34)
            if (selectedTab == AdminTab.ROUTINE) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Routine Classes (${allClasses.size})",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Button(
                            onClick = { showAddClassDialog = true },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.testTag("admin_add_class_btn")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Add Class", fontSize = 11.sp)
                        }
                    }
                }

                items(allClasses, key = { it.id }) { cls ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${cls.day}: ${cls.courseName} (${cls.courseCode})",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = "${cls.startTime} - ${cls.endTime} • Room ${cls.room} • ${cls.teacher}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            IconButton(
                                onClick = { classToEdit = cls },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(14.dp))
                            }

                            IconButton(
                                onClick = {
                                    scope.launch {
                                        viewModel.repository.deleteClass(cls)
                                        Toast.makeText(context, "Deleted class slot", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(14.dp))
                            }
                        }
                    }
                }
            }

            // Tab 2: Banner Settings (Section 28, 29)
            if (selectedTab == AdminTab.BANNER) {
                item {
                    BannerSettingsEditor(
                        bannerSettings = bannerSettings,
                        onSave = { updated ->
                            scope.launch {
                                viewModel.repository.updateBannerSettings(updated)
                                Toast.makeText(context, "Banner configuration saved", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }

            // Tab 3: Alerts & Notification Settings (Section 22, 23, 25, 26)
            if (selectedTab == AdminTab.ALERTS) {
                item {
                    AlertSettingsEditor(
                        appSettings = appSettings,
                        onSave = { updated ->
                            scope.launch {
                                viewModel.repository.updateAppSettings(updated)
                                Toast.makeText(context, "Alert settings saved", Toast.LENGTH_SHORT).show()
                            }
                        },
                        onTestSound = { sound -> viewModel.testSound(sound) },
                        onPreviewAlert = { viewModel.triggerAlertPreview() },
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }

            // Tab 4: Backup, Import & Export (Section 35, 50)
            if (selectedTab == AdminTab.BACKUP) {
                item {
                    BackupManagementSection(
                        onExport = {
                            scope.launch {
                                val json = viewModel.exportBackupJson()
                                val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                cm.setPrimaryClip(ClipData.newPlainText("Routine Scrapper Backup", json))
                                Toast.makeText(context, "Backup JSON copied to clipboard!", Toast.LENGTH_LONG).show()
                            }
                        },
                        onImportClick = { showImportDialog = true },
                        onDeleteAllClick = { showDeleteAllDialog = true },
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }

            // Tab 5: Account & Credentials (Section 31, 32)
            if (selectedTab == AdminTab.ACCOUNT) {
                item {
                    AccountSettingsSection(
                        onUpdateCredentials = { user, pass ->
                            scope.launch {
                                viewModel.repository.updateAdminCredentials(user, pass)
                                Toast.makeText(context, "Credentials updated", Toast.LENGTH_SHORT).show()
                            }
                        },
                        onResetAdminData = {
                            viewModel.resetAdminCredentials()
                            Toast.makeText(context, "Admin credentials reset to admin / admin", Toast.LENGTH_SHORT).show()
                        },
                        onDeleteAllData = { showDeleteAllDialog = true },
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
        }
    }

    // Add or Edit Class Dialog
    if (showAddClassDialog || classToEdit != null) {
        val editing = classToEdit
        ClassRoutineFormDialog(
            classRoutine = editing,
            onDismiss = {
                showAddClassDialog = false
                classToEdit = null
            },
            onSave = { updated ->
                scope.launch {
                    if (editing != null) {
                        viewModel.repository.updateClass(updated)
                        Toast.makeText(context, "Class updated", Toast.LENGTH_SHORT).show()
                    } else {
                        viewModel.repository.insertClass(updated)
                        Toast.makeText(context, "Class added", Toast.LENGTH_SHORT).show()
                    }
                    showAddClassDialog = false
                    classToEdit = null
                }
            }
        )
    }

    // Import Input Dialog
    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            title = { Text("Import Backup JSON") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Paste full backup JSON below to preview restoration:",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = importJsonText,
                        onValueChange = { importJsonText = it },
                        modifier = Modifier.fillMaxWidth().height(150.dp),
                        placeholder = { Text("{\"version\": 1, ...}") }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (importJsonText.isNotBlank()) {
                            viewModel.prepareImport(importJsonText)
                            showImportDialog = false
                            importJsonText = ""
                        }
                    }
                ) {
                    Text("Parse & Preview")
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Import Preview Confirmation Modal (Section 50)
    pendingImport?.let { summary ->
        AlertDialog(
            onDismissRequest = { viewModel.cancelImport() },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFF59E0B))
                    Text("Import Preview", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "The following academic records were detected in the backup:",
                        fontSize = 13.sp
                    )
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text("• ${summary.classesCount} Classes", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                            Text("• ${summary.teachersCount} Teachers", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                            Text("• ${summary.quizzesCount} Quizzes", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                            Text("• ${summary.eventsCount} Calendar Events", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                            Text("• ${summary.filesCount} Quiz Files", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                        }
                    }
                    Text(
                        text = "Existing corresponding data will be replaced by the imported data.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.confirmImport() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.testTag("confirm_import_btn")
                ) {
                    Text("Confirm Import")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.cancelImport() }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Delete All Academic Data Confirmation Dialog
    if (showDeleteAllDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAllDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = "Delete All Data?",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Are you sure you want to delete all academic data? This will permanently remove all class routines, teachers, quizzes, study files, academic calendar events, and notifications.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "This action cannot be undone.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteAllData()
                        showDeleteAllDialog = false
                        Toast.makeText(context, "All data has been deleted", Toast.LENGTH_LONG).show()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    ),
                    modifier = Modifier.testTag("confirm_delete_all_data_btn")
                ) {
                    Text("Delete Everything")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showDeleteAllDialog = false },
                    modifier = Modifier.testTag("cancel_delete_all_data_btn")
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun ClassRoutineFormDialog(
    classRoutine: ClassRoutine?,
    onDismiss: () -> Unit,
    onSave: (ClassRoutine) -> Unit
) {
    val days = listOf("Saturday", "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday")
    var day by remember { mutableStateOf(classRoutine?.day ?: "Saturday") }
    var courseName by remember { mutableStateOf(classRoutine?.courseName ?: "") }
    var courseCode by remember { mutableStateOf(classRoutine?.courseCode ?: "CSE") }
    var startTime by remember { mutableStateOf(classRoutine?.startTime ?: "08:30 AM") }
    var endTime by remember { mutableStateOf(classRoutine?.endTime ?: "10:00 AM") }
    var room by remember { mutableStateOf(classRoutine?.room ?: "KT-") }
    var teacher by remember { mutableStateOf(classRoutine?.teacher ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (classRoutine == null) "Add Routine Class" else "Edit Routine Class")
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Text("Day of Week:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        days.take(3).forEach { d ->
                            val isSel = d == day
                            FilledTonalButton(
                                onClick = { day = d },
                                shape = RoundedCornerShape(6.dp),
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = if (isSel) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(d.take(3), fontSize = 11.sp)
                            }
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        days.drop(3).forEach { d ->
                            val isSel = d == day
                            FilledTonalButton(
                                onClick = { day = d },
                                shape = RoundedCornerShape(6.dp),
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = if (isSel) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(d.take(3), fontSize = 11.sp)
                            }
                        }
                    }
                }
                item {
                    OutlinedTextField(
                        value = courseName,
                        onValueChange = { courseName = it },
                        label = { Text("Course Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = courseCode,
                        onValueChange = { courseCode = it },
                        label = { Text("Course Code (e.g. CSE201)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = startTime,
                            onValueChange = { startTime = it },
                            label = { Text("Start Time") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = endTime,
                            onValueChange = { endTime = it },
                            label = { Text("End Time") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                item {
                    OutlinedTextField(
                        value = room,
                        onValueChange = { room = it },
                        label = { Text("Room (e.g. KT-401)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = teacher,
                        onValueChange = { teacher = it },
                        label = { Text("Teacher Name & Initial") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (courseName.isNotBlank()) {
                        onSave(
                            ClassRoutine(
                                id = classRoutine?.id ?: 0L,
                                day = day,
                                courseName = courseName.trim(),
                                courseCode = courseCode.trim(),
                                startTime = startTime.trim(),
                                endTime = endTime.trim(),
                                room = room.trim(),
                                teacher = teacher.trim()
                            )
                        )
                    }
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun BannerSettingsEditor(
    bannerSettings: com.example.data.model.BannerSettings,
    onSave: (com.example.data.model.BannerSettings) -> Unit,
    modifier: Modifier = Modifier
) {
    var title by remember { mutableStateOf(bannerSettings.title) }
    var subtitle by remember { mutableStateOf(bannerSettings.subtitle) }
    var font by remember { mutableStateOf(bannerSettings.font) }
    var position by remember { mutableStateOf(bannerSettings.position) }
    var enabled by remember { mutableStateOf(bannerSettings.enabled) }

    var highlight1 by remember { mutableStateOf(bannerSettings.highlight1) }
    var highlight1Pos by remember { mutableStateOf(bannerSettings.highlight1Position) }
    var highlight1Hidden by remember { mutableStateOf(bannerSettings.highlight1Hidden) }

    var highlight2 by remember { mutableStateOf(bannerSettings.highlight2) }
    var highlight2Pos by remember { mutableStateOf(bannerSettings.highlight2Position) }
    var highlight2Hidden by remember { mutableStateOf(bannerSettings.highlight2Hidden) }

    var highlight3 by remember { mutableStateOf(bannerSettings.highlight3) }
    var highlight3Pos by remember { mutableStateOf(bannerSettings.highlight3Position) }
    var highlight3Hidden by remember { mutableStateOf(bannerSettings.highlight3Hidden) }

    val fonts = listOf("Modern Sans", "Academic Serif", "Monospace", "Tech Bold")
    val positions = listOf("Left", "Middle", "Right")
    val highlightPositions = listOf("Left", "Right", "Top", "Bottom")

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Global Banner Settings", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Visible", fontSize = 12.sp)
                    Switch(checked = enabled, onCheckedChange = { enabled = it })
                }
            }

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Banner Headline") },
                modifier = Modifier.fillMaxWidth().testTag("banner_headline_input")
            )

            OutlinedTextField(
                value = subtitle,
                onValueChange = { subtitle = it },
                label = { Text("Banner Subtitle") },
                modifier = Modifier.fillMaxWidth()
            )

            // Font Picker
            Text("Headline Typography:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                fonts.forEach { f ->
                    val isSel = f == font
                    FilledTonalButton(
                        onClick = { font = f },
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (isSel) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(f.take(7), fontSize = 10.sp)
                    }
                }
            }

            // Main Text Position (Left, Middle, Right)
            Text("Main Text Alignment:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                positions.forEach { p ->
                    val isSel = p == position
                    FilledTonalButton(
                        onClick = { position = p },
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (isSel) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(p, fontSize = 11.sp)
                    }
                }
            }

            Divider()

            // Highlight 1
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Highlight 1", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(if (highlight1Hidden) "Hidden" else "Shown", fontSize = 11.sp)
                        Checkbox(checked = !highlight1Hidden, onCheckedChange = { highlight1Hidden = !it })
                    }
                }
                OutlinedTextField(
                    value = highlight1,
                    onValueChange = { highlight1 = it },
                    label = { Text("Highlight 1 Text") },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    highlightPositions.forEach { pos ->
                        val isSel = pos == highlight1Pos
                        FilledTonalButton(
                            onClick = { highlight1Pos = pos },
                            shape = RoundedCornerShape(6.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = if (isSel) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = if (isSel) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(pos, fontSize = 10.sp)
                        }
                    }
                }
            }

            Divider()

            // Highlight 2
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Highlight 2", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(if (highlight2Hidden) "Hidden" else "Shown", fontSize = 11.sp)
                        Checkbox(checked = !highlight2Hidden, onCheckedChange = { highlight2Hidden = !it })
                    }
                }
                OutlinedTextField(
                    value = highlight2,
                    onValueChange = { highlight2 = it },
                    label = { Text("Highlight 2 Text") },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    highlightPositions.forEach { pos ->
                        val isSel = pos == highlight2Pos
                        FilledTonalButton(
                            onClick = { highlight2Pos = pos },
                            shape = RoundedCornerShape(6.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = if (isSel) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = if (isSel) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(pos, fontSize = 10.sp)
                        }
                    }
                }
            }

            Divider()

            // Highlight 3
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Highlight 3", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(if (highlight3Hidden) "Hidden" else "Shown", fontSize = 11.sp)
                        Checkbox(checked = !highlight3Hidden, onCheckedChange = { highlight3Hidden = !it })
                    }
                }
                OutlinedTextField(
                    value = highlight3,
                    onValueChange = { highlight3 = it },
                    label = { Text("Highlight 3 Text") },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    highlightPositions.forEach { pos ->
                        val isSel = pos == highlight3Pos
                        FilledTonalButton(
                            onClick = { highlight3Pos = pos },
                            shape = RoundedCornerShape(6.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = if (isSel) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = if (isSel) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(pos, fontSize = 10.sp)
                        }
                    }
                }
            }

            Button(
                onClick = {
                    onSave(
                        bannerSettings.copy(
                            title = title.trim(),
                            subtitle = subtitle.trim(),
                            font = font,
                            position = position,
                            enabled = enabled,
                            highlight1 = highlight1.trim(),
                            highlight1Position = highlight1Pos,
                            highlight1Hidden = highlight1Hidden,
                            highlight2 = highlight2.trim(),
                            highlight2Position = highlight2Pos,
                            highlight2Hidden = highlight2Hidden,
                            highlight3 = highlight3.trim(),
                            highlight3Position = highlight3Pos,
                            highlight3Hidden = highlight3Hidden
                        )
                    )
                },
                modifier = Modifier.fillMaxWidth().testTag("save_banner_settings_btn")
            ) {
                Text("Save Banner Configuration")
            }
        }
    }
}

@Composable
fun AlertSettingsEditor(
    appSettings: com.example.data.model.AppSettings,
    onSave: (com.example.data.model.AppSettings) -> Unit,
    onTestSound: (String) -> Unit,
    onPreviewAlert: () -> Unit,
    modifier: Modifier = Modifier
) {
    var alertsEnabled by remember { mutableStateOf(appSettings.classAlertsEnabled) }
    var selectedSound by remember { mutableStateOf(appSettings.notificationSound) }

    val sounds = listOf("Gentle Chime", "Bell", "Double Beep", "Soft Pulse")

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text("Class Reminder & Sound Alerts", fontWeight = FontWeight.Bold, fontSize = 16.sp)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("10-Minute Class Reminder", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Text("Triggers audio cue & on-screen toast 10m before class", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(
                    checked = alertsEnabled,
                    onCheckedChange = { alertsEnabled = it }
                )
            }

            Divider()

            Text("Notification Sound Tone:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                sounds.forEach { snd ->
                    val isSel = snd == selectedSound
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedSound = snd },
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSel) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = if (isSel) androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = snd,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 13.sp
                            )

                            // Test Tone Button
                            FilledTonalButton(
                                onClick = { onTestSound(snd) },
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Icon(Icons.Default.VolumeUp, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Test Sound", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            Divider()

            // Section 25: Admin Alert Preview (View Alert Overview)
            OutlinedButton(
                onClick = onPreviewAlert,
                modifier = Modifier.fillMaxWidth().testTag("admin_preview_alert_btn"),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Preview, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("View Alert Overview (Live Student Preview)")
            }

            Button(
                onClick = {
                    onSave(
                        appSettings.copy(
                            classAlertsEnabled = alertsEnabled,
                            notificationSound = selectedSound
                        )
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Alert Settings")
            }
        }
    }
}

@Composable
fun BackupManagementSection(
    onExport: () -> Unit,
    onImportClick: () -> Unit,
    onDeleteAllClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Backup & Recovery", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(
                text = "Export and restore entire academic records including classes, faculty directory, quizzes, descriptions, and calendar events.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Button(
                onClick = onExport,
                modifier = Modifier.fillMaxWidth().testTag("export_backup_json_btn"),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.FileDownload, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Export Routine Data (JSON)")
            }

            OutlinedButton(
                onClick = onImportClick,
                modifier = Modifier.fillMaxWidth().testTag("import_backup_json_btn"),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.FileUpload, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Import Routine Data (JSON)")
            }

            Divider()

            // Delete All Data Option
            OutlinedButton(
                onClick = onDeleteAllClick,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.fillMaxWidth().testTag("admin_delete_all_data_btn"),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.DeleteForever, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Delete All Data", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun AccountSettingsSection(
    onUpdateCredentials: (String, String) -> Unit,
    onResetAdminData: () -> Unit,
    onDeleteAllData: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var newUsername by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Admin Account & Security", fontWeight = FontWeight.Bold, fontSize = 16.sp)

            OutlinedTextField(
                value = newUsername,
                onValueChange = { newUsername = it },
                label = { Text("New Username") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = newPassword,
                onValueChange = { newPassword = it },
                label = { Text("New Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    if (newUsername.isNotBlank() && newPassword.isNotBlank()) {
                        onUpdateCredentials(newUsername.trim(), newPassword.trim())
                        newUsername = ""
                        newPassword = ""
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Update Admin Password")
            }

            Divider()

            // Section 32: Reset Admin Data
            OutlinedButton(
                onClick = onResetAdminData,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.fillMaxWidth().testTag("reset_admin_data_btn")
            ) {
                Icon(Icons.Default.Restore, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Reset Admin Credentials (admin/admin)")
            }

            if (onDeleteAllData != null) {
                OutlinedButton(
                    onClick = onDeleteAllData,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.fillMaxWidth().testTag("account_delete_all_data_btn"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.DeleteForever, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Delete All Academic Data", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
