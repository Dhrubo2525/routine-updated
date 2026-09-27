package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Teacher
import com.example.ui.components.GlobalBanner
import com.example.ui.components.ZoneVibe
import com.example.ui.viewmodel.AcademicViewModel
import kotlinx.coroutines.launch

@Composable
fun TeachersZoneScreen(
    viewModel: AcademicViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val bannerSettings by viewModel.bannerSettings.collectAsStateWithLifecycle()
    val allTeachers by viewModel.allTeachers.collectAsStateWithLifecycle()
    val isAdmin by viewModel.isAdminLoggedIn.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var teacherToEdit by remember { mutableStateOf<Teacher?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }

    val filteredTeachers = remember(allTeachers, searchQuery) {
        if (searchQuery.isBlank()) allTeachers
        else allTeachers.filter {
            it.fullName.contains(searchQuery, ignoreCase = true) ||
                    it.initial.contains(searchQuery, ignoreCase = true) ||
                    it.courses.contains(searchQuery, ignoreCase = true) ||
                    it.department.contains(searchQuery, ignoreCase = true)
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("teachers_zone_screen"),
        contentPadding = PaddingValues(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Global Banner
        item {
            GlobalBanner(
                bannerSettings = bannerSettings,
                currentZone = ZoneVibe.TEACHERS
            )
        }

        // Search Bar & Admin Add action
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Faculty Directory (${filteredTeachers.size})",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    if (isAdmin) {
                        Button(
                            onClick = { showAddDialog = true },
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("admin_add_teacher_btn")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Add Faculty", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Search field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("teacher_search_field"),
                    placeholder = { Text("Search by name, initial, courses...") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }
        }

        // Teachers Cards
        items(filteredTeachers, key = { it.id }) { teacher ->
            TeacherCard(
                teacher = teacher,
                isAdmin = isAdmin,
                onEdit = { teacherToEdit = teacher },
                onDelete = {
                    scope.launch {
                        viewModel.repository.deleteTeacher(teacher)
                        Toast.makeText(context, "Removed ${teacher.fullName}", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }

    // Add or Edit Teacher Dialog
    if (showAddDialog || teacherToEdit != null) {
        val editing = teacherToEdit
        TeacherFormDialog(
            teacher = editing,
            onDismiss = {
                showAddDialog = false
                teacherToEdit = null
            },
            onSave = { updatedTeacher ->
                scope.launch {
                    if (editing != null) {
                        viewModel.repository.updateTeacher(updatedTeacher)
                        Toast.makeText(context, "Teacher updated", Toast.LENGTH_SHORT).show()
                    } else {
                        viewModel.repository.insertTeacher(updatedTeacher)
                        Toast.makeText(context, "Teacher added", Toast.LENGTH_SHORT).show()
                    }
                    showAddDialog = false
                    teacherToEdit = null
                }
            }
        )
    }
}

@Composable
fun TeacherCard(
    teacher: Teacher,
    isAdmin: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = remember {
        context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("teacher_card_${teacher.id}"),
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header: Avatar, Name, Initial Badge, and Admin edit/delete buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = teacher.initial,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = teacher.fullName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = teacher.designation,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "${teacher.department} • Room ${teacher.room}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (isAdmin) {
                    Row {
                        IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(16.dp))
                        }
                        IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            // Courses badge row
            if (teacher.courses.isNotBlank()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Book,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "Assigned: ${teacher.courses}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

            // Phone Row with direct call and copy phone icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = teacher.phone,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    // Small copy phone icon
                    IconButton(
                        onClick = {
                            clipboardManager.setPrimaryClip(ClipData.newPlainText("Teacher Phone", teacher.phone))
                            Toast.makeText(context, "Copied phone number", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.size(24.dp).testTag("copy_phone_${teacher.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ContentCopy,
                            contentDescription = "Copy phone",
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                // Direct Call Action
                FilledTonalButton(
                    onClick = {
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${teacher.phone}"))
                        context.startActivity(intent)
                    },
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.testTag("call_btn_${teacher.id}")
                ) {
                    Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Call", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Email Row with direct mail and copy email icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = teacher.email,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    // Small copy email icon
                    IconButton(
                        onClick = {
                            clipboardManager.setPrimaryClip(ClipData.newPlainText("Teacher Email", teacher.email))
                            Toast.makeText(context, "Copied email address", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.size(24.dp).testTag("copy_email_${teacher.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ContentCopy,
                            contentDescription = "Copy email",
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                // Direct Email Action
                OutlinedButton(
                    onClick = {
                        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:${teacher.email}"))
                        context.startActivity(intent)
                    },
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.testTag("email_btn_${teacher.id}")
                ) {
                    Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Email", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Copy Full Details action button at bottom of teacher card
            Button(
                onClick = {
                    val fullDetails = """
                        Faculty: ${teacher.fullName} (${teacher.initial})
                        Designation: ${teacher.designation}
                        Department: ${teacher.department}
                        Courses: ${teacher.courses}
                        Room: ${teacher.room}
                        Phone: ${teacher.phone}
                        Email: ${teacher.email}
                    """.trimIndent()
                    clipboardManager.setPrimaryClip(ClipData.newPlainText("Teacher Details", fullDetails))
                    Toast.makeText(context, "Teacher details copied to clipboard", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("copy_details_btn_${teacher.id}"),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ContentPaste,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text("Copy Full Details", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
fun TeacherFormDialog(
    teacher: Teacher?,
    onDismiss: () -> Unit,
    onSave: (Teacher) -> Unit
) {
    var fullName by remember { mutableStateOf(teacher?.fullName ?: "") }
    var initial by remember { mutableStateOf(teacher?.initial ?: "") }
    var designation by remember { mutableStateOf(teacher?.designation ?: "Assistant Professor") }
    var courses by remember { mutableStateOf(teacher?.courses ?: "") }
    var department by remember { mutableStateOf(teacher?.department ?: "Computer Science & Engineering") }
    var room by remember { mutableStateOf(teacher?.room ?: "KT-") }
    var phone by remember { mutableStateOf(teacher?.phone ?: "+880 ") }
    var email by remember { mutableStateOf(teacher?.email ?: "@university.edu") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (teacher == null) "Add Faculty Member" else "Edit Faculty Member")
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        label = { Text("Full Name") },
                        modifier = Modifier.fillMaxWidth().testTag("teacher_fullname_input")
                    )
                }
                item {
                    OutlinedTextField(
                        value = initial,
                        onValueChange = { initial = it },
                        label = { Text("Teacher Initial (e.g. MRR, TD)") },
                        modifier = Modifier.fillMaxWidth().testTag("teacher_initial_input")
                    )
                }
                item {
                    OutlinedTextField(
                        value = designation,
                        onValueChange = { designation = it },
                        label = { Text("Designation") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = courses,
                        onValueChange = { courses = it },
                        label = { Text("Courses (e.g. CSE201, CSE323)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = department,
                        onValueChange = { department = it },
                        label = { Text("Department") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = room,
                        onValueChange = { room = it },
                        label = { Text("Room / Office") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Phone Number") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email Address") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (fullName.isNotBlank() && initial.isNotBlank()) {
                        val result = Teacher(
                            id = teacher?.id ?: 0L,
                            fullName = fullName.trim(),
                            initial = initial.trim().uppercase(),
                            designation = designation.trim(),
                            courses = courses.trim(),
                            department = department.trim(),
                            room = room.trim(),
                            phone = phone.trim(),
                            email = email.trim()
                        )
                        onSave(result)
                    }
                },
                modifier = Modifier.testTag("save_teacher_btn")
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
