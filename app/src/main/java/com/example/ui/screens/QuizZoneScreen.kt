package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Quiz
import com.example.data.model.QuizFile
import com.example.ui.components.GlobalBanner
import com.example.ui.components.ZoneVibe
import com.example.ui.viewmodel.AcademicViewModel
import com.example.util.FileStorageManager
import kotlinx.coroutines.launch

@Composable
fun QuizZoneScreen(
    viewModel: AcademicViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val bannerSettings by viewModel.bannerSettings.collectAsStateWithLifecycle()
    val allQuizzes by viewModel.allQuizzes.collectAsStateWithLifecycle()
    val allFiles by viewModel.allFiles.collectAsStateWithLifecycle()
    val isAdmin by viewModel.isAdminLoggedIn.collectAsStateWithLifecycle()

    var selectedQuizForDetails by remember { mutableStateOf<Quiz?>(null) }
    var quizToEdit by remember { mutableStateOf<Quiz?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }

    // Blinking animation for persistent Quiz Alert
    val infiniteTransition = rememberInfiniteTransition(label = "quiz_alert_blink")
    val alertAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alert_alpha"
    )

    // Find upcoming quizzes for the persistent alert
    val upcomingQuizzes = remember(allQuizzes) {
        allQuizzes.take(2)
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("quiz_zone_screen"),
        contentPadding = PaddingValues(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Global Banner
        item {
            GlobalBanner(
                bannerSettings = bannerSettings,
                currentZone = ZoneVibe.QUIZ
            )
        }

        // Persistent Next-Quiz Alert Banner (Section 15)
        if (upcomingQuizzes.isNotEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    upcomingQuizzes.forEach { quiz ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedQuizForDetails = quiz }
                                .testTag("quiz_alert_banner_${quiz.id}"),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFFEF2F2) // Light red container
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFEF4444))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFEF4444).copy(alpha = alertAlpha)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = "Alert",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Text(
                                        text = "🚨 UPCOMING QUIZ ALERT",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFB91C1C),
                                        letterSpacing = 1.sp
                                    )
                                    Text(
                                        text = "${quiz.courseName} (${quiz.courseCode}) • ${quiz.quizNumber}",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF7F1D1D)
                                    )
                                    Text(
                                        text = "Date: ${quiz.date} at ${quiz.time} • Room ${quiz.room} • Faculty: ${quiz.teacherInitial}",
                                        fontSize = 12.sp,
                                        color = Color(0xFF991B1B)
                                    )
                                }

                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = "View",
                                    tint = Color(0xFFB91C1C)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section Header: All Quizzes + Admin Add Quiz
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Scheduled Quizzes & Tests (${allQuizzes.size})",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                if (isAdmin) {
                    Button(
                        onClick = { showAddDialog = true },
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("admin_add_quiz_btn")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Add Quiz", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // List of Quizzes
        if (allQuizzes.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No scheduled quizzes at this time.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )
                }
            }
        } else {
            items(allQuizzes, key = { it.id }) { quiz ->
                val quizFilesCount = allFiles.count { it.quizId == quiz.id }

                QuizSummaryCard(
                    quiz = quiz,
                    filesCount = quizFilesCount,
                    isAdmin = isAdmin,
                    onClick = { selectedQuizForDetails = quiz },
                    onEdit = { quizToEdit = quiz },
                    onDelete = {
                        scope.launch {
                            viewModel.repository.deleteQuiz(quiz)
                            Toast.makeText(context, "Deleted quiz", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
    }

    // Quiz Details Dialog / Sheet (Section 11, 12, 13, 14)
    selectedQuizForDetails?.let { quiz ->
        QuizDetailsDialog(
            quiz = quiz,
            files = allFiles.filter { it.quizId == quiz.id },
            isAdmin = isAdmin,
            onDismiss = { selectedQuizForDetails = null },
            onAddFile = { file ->
                scope.launch {
                    viewModel.repository.insertQuizFile(file)
                }
            },
            onDeleteFile = { file ->
                scope.launch {
                    viewModel.repository.deleteQuizFile(file)
                    Toast.makeText(context, "Removed file", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    // Add or Edit Quiz Dialog
    if (showAddDialog || quizToEdit != null) {
        val editing = quizToEdit
        QuizFormDialog(
            quiz = editing,
            onDismiss = {
                showAddDialog = false
                quizToEdit = null
            },
            onSave = { updatedQuiz ->
                scope.launch {
                    if (editing != null) {
                        viewModel.repository.updateQuiz(updatedQuiz)
                        Toast.makeText(context, "Quiz updated", Toast.LENGTH_SHORT).show()
                    } else {
                        viewModel.repository.insertQuiz(updatedQuiz)
                        Toast.makeText(context, "Quiz scheduled", Toast.LENGTH_SHORT).show()
                    }
                    showAddDialog = false
                    quizToEdit = null
                }
            }
        )
    }
}

@Composable
fun QuizSummaryCard(
    quiz: Quiz,
    filesCount: Int,
    isAdmin: Boolean,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("quiz_card_${quiz.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = quiz.courseCode,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFEF4444).copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = quiz.quizNumber,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFEF4444),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                if (isAdmin) {
                    Row {
                        IconButton(onClick = onEdit, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(16.dp))
                        }
                        IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            Text(
                text = "${quiz.courseName} - ${quiz.topic}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📅 ${quiz.date} • ${quiz.time} • Room ${quiz.room}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (quiz.driveLink.isNotBlank()) {
                        Icon(
                            imageVector = Icons.Default.CloudQueue,
                            contentDescription = "Drive Link",
                            tint = Color(0xFF2563EB),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    if (filesCount > 0) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Icon(Icons.Default.AttachFile, contentDescription = null, modifier = Modifier.size(12.dp))
                                Text(text = "$filesCount files", fontSize = 10.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QuizDetailsDialog(
    quiz: Quiz,
    files: List<QuizFile>,
    isAdmin: Boolean,
    onDismiss: () -> Unit,
    onAddFile: (QuizFile) -> Unit,
    onDeleteFile: (QuizFile) -> Unit
) {
    val context = LocalContext.current

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val quizFile = FileStorageManager.saveUriToQuizFiles(context, uri, quiz.id)
            if (quizFile != null) {
                onAddFile(quizFile)
                Toast.makeText(context, "Attached ${quizFile.displayName} from device!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Could not attach file from device", Toast.LENGTH_SHORT).show()
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "${quiz.courseName} • ${quiz.quizNumber}",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${quiz.courseCode} • Faculty: ${quiz.teacherInitial} • Room ${quiz.room}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Topic
                item {
                    Text(
                        text = "Topic: ${quiz.topic}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // Date & Time
                item {
                    Text(
                        text = "Scheduled: ${quiz.date} at ${quiz.time}",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Quiz Description (Syllabus coverage, Preparation notes - Section 11)
                item {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Syllabus Coverage & Instructions:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = quiz.description.ifBlank { "No detailed instructions provided." },
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Google Drive Link (Section 14)
                if (quiz.driveLink.isNotBlank()) {
                    item {
                        OutlinedButton(
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(quiz.driveLink))
                                context.startActivity(intent)
                            },
                            modifier = Modifier.fillMaxWidth().testTag("open_quiz_drive_link_btn"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.CloudQueue, contentDescription = null, tint = Color(0xFF2563EB))
                            Spacer(Modifier.width(8.dp))
                            Text("Open Google Drive Resources", color = Color(0xFF2563EB))
                        }
                    }
                }

                // Quiz Files (Section 12 & 13)
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Attached Study Files (${files.size})",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )

                        if (isAdmin) {
                            FilledTonalButton(
                                onClick = { filePickerLauncher.launch("*/*") },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier.testTag("admin_attach_file_btn")
                            ) {
                                Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Attach File", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                if (files.isEmpty()) {
                    item {
                        Text(
                            text = "No study files attached to this quiz yet.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    items(files, key = { it.id }) { file ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = when {
                                                file.displayName.endsWith(".pdf", ignoreCase = true) -> Icons.Default.PictureAsPdf
                                                file.displayName.endsWith(".png", ignoreCase = true) || file.displayName.endsWith(".jpg", ignoreCase = true) -> Icons.Default.Image
                                                else -> Icons.Default.Description
                                            },
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = file.displayName,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${file.mimeType} • ${file.fileSize}",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                // Open Action (Section 13)
                                IconButton(
                                    onClick = {
                                        val success = FileStorageManager.openQuizFile(context, file)
                                        if (!success) {
                                            Toast.makeText(context, "No app available to open ${file.displayName}", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    modifier = Modifier.size(30.dp).testTag("open_quiz_file_${file.id}")
                                ) {
                                    Icon(Icons.Default.Visibility, contentDescription = "Open file", modifier = Modifier.size(16.dp))
                                }

                                // Download Action (Section 13)
                                IconButton(
                                    onClick = {
                                        val downloadedUri = FileStorageManager.downloadQuizFileToDevice(context, file)
                                        if (downloadedUri != null) {
                                            Toast.makeText(context, "Saved ${file.displayName} to Downloads folder", Toast.LENGTH_LONG).show()
                                        } else {
                                            Toast.makeText(context, "Failed to download ${file.displayName}", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    modifier = Modifier.size(30.dp).testTag("download_quiz_file_${file.id}")
                                ) {
                                    Icon(Icons.Default.Download, contentDescription = "Download file", modifier = Modifier.size(16.dp))
                                }

                                if (isAdmin) {
                                    IconButton(
                                        onClick = { onDeleteFile(file) },
                                        modifier = Modifier.size(30.dp).testTag("delete_quiz_file_${file.id}")
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
fun QuizFormDialog(
    quiz: Quiz?,
    onDismiss: () -> Unit,
    onSave: (Quiz) -> Unit
) {
    var courseName by remember { mutableStateOf(quiz?.courseName ?: "") }
    var courseCode by remember { mutableStateOf(quiz?.courseCode ?: "CSE") }
    var quizNumber by remember { mutableStateOf(quiz?.quizNumber ?: "Quiz #1") }
    var topic by remember { mutableStateOf(quiz?.topic ?: "") }
    var description by remember { mutableStateOf(quiz?.description ?: "") }
    var teacherInitial by remember { mutableStateOf(quiz?.teacherInitial ?: "TD") }
    var room by remember { mutableStateOf(quiz?.room ?: "KT-") }
    var date by remember { mutableStateOf(quiz?.date ?: "2026-09-30") }
    var time by remember { mutableStateOf(quiz?.time ?: "10:00 AM") }
    var driveLink by remember { mutableStateOf(quiz?.driveLink ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (quiz == null) "Schedule New Quiz" else "Edit Quiz")
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
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
                        label = { Text("Course Code (e.g. CSE323)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = quizNumber,
                        onValueChange = { quizNumber = it },
                        label = { Text("Quiz Number (e.g. Quiz #2, Class Test 1)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = topic,
                        onValueChange = { topic = it },
                        label = { Text("Quiz Topic") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Instructions & Syllabus Coverage") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                }
                item {
                    OutlinedTextField(
                        value = teacherInitial,
                        onValueChange = { teacherInitial = it },
                        label = { Text("Faculty Initial (e.g. TD, MRR)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = room,
                        onValueChange = { room = it },
                        label = { Text("Room Number") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = date,
                        onValueChange = { date = it },
                        label = { Text("Date (YYYY-MM-DD)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = time,
                        onValueChange = { time = it },
                        label = { Text("Time (e.g. 10:00 AM)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = driveLink,
                        onValueChange = { driveLink = it },
                        label = { Text("Google Drive Link (Optional)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (courseName.isNotBlank() && topic.isNotBlank()) {
                        val result = Quiz(
                            id = quiz?.id ?: 0L,
                            courseName = courseName.trim(),
                            courseCode = courseCode.trim(),
                            quizNumber = quizNumber.trim(),
                            topic = topic.trim(),
                            description = description.trim(),
                            teacherInitial = teacherInitial.trim().uppercase(),
                            room = room.trim(),
                            date = date.trim(),
                            time = time.trim(),
                            driveLink = driveLink.trim()
                        )
                        onSave(result)
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
