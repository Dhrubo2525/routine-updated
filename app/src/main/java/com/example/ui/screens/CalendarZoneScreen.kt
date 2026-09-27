package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.CalendarEvent
import com.example.ui.components.GlobalBanner
import com.example.ui.components.ZoneVibe
import com.example.ui.theme.EventImportantColor
import com.example.ui.theme.EventMostImportantColor
import com.example.ui.theme.EventNormalColor
import com.example.ui.viewmodel.AcademicViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CalendarZoneScreen(
    viewModel: AcademicViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val bannerSettings by viewModel.bannerSettings.collectAsStateWithLifecycle()
    val allEvents by viewModel.allEvents.collectAsStateWithLifecycle()
    val isAdmin by viewModel.isAdminLoggedIn.collectAsStateWithLifecycle()

    val todayCal = remember { Calendar.getInstance() }
    val todayDateStr = remember {
        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(todayCal.time)
    }

    var displayedYear by remember { mutableStateOf(todayCal.get(Calendar.YEAR)) }
    var displayedMonth by remember { mutableStateOf(todayCal.get(Calendar.MONTH)) } // 0-based
    var selectedDateStr by remember { mutableStateOf(todayDateStr) }

    var showAddEventDialog by remember { mutableStateOf(false) }
    var eventToEdit by remember { mutableStateOf<CalendarEvent?>(null) }

    val monthNames = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )

    val currentMonthYearText = "${monthNames[displayedMonth]} $displayedYear"

    // Events for the selected date
    val selectedDateEvents = remember(allEvents, selectedDateStr) {
        allEvents.filter { it.date == selectedDateStr }
    }

    // Days in current displayed month
    val daysInMonth = remember(displayedYear, displayedMonth) {
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, displayedYear)
        cal.set(Calendar.MONTH, displayedMonth)
        cal.set(Calendar.DAY_OF_MONTH, 1)
        val maxDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) // 1=Sun, 2=Mon...
        // Map to standard starting Monday or Sunday:
        Pair(firstDayOfWeek, maxDays)
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("calendar_zone_screen"),
        contentPadding = PaddingValues(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Global Banner
        item {
            GlobalBanner(
                bannerSettings = bannerSettings,
                currentZone = ZoneVibe.CALENDAR
            )
        }

        // Calendar Month Navigation Bar
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .testTag("academic_calendar_card"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Header: Month Title, Prev/Next, Today button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = currentMonthYearText,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Academic Term 2026",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            // "Today" button
                            FilledTonalButton(
                                onClick = {
                                    val now = Calendar.getInstance()
                                    displayedYear = now.get(Calendar.YEAR)
                                    displayedMonth = now.get(Calendar.MONTH)
                                    selectedDateStr = todayDateStr
                                },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.testTag("calendar_today_btn")
                            ) {
                                Text("Today", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            // Prev Month
                            IconButton(
                                onClick = {
                                    if (displayedMonth == 0) {
                                        displayedMonth = 11
                                        displayedYear--
                                    } else {
                                        displayedMonth--
                                    }
                                },
                                modifier = Modifier.size(32.dp).testTag("prev_month_btn")
                            ) {
                                Icon(Icons.Default.ChevronLeft, contentDescription = "Previous Month")
                            }

                            // Next Month
                            IconButton(
                                onClick = {
                                    if (displayedMonth == 11) {
                                        displayedMonth = 0
                                        displayedYear++
                                    } else {
                                        displayedMonth++
                                    }
                                },
                                modifier = Modifier.size(32.dp).testTag("next_month_btn")
                            ) {
                                Icon(Icons.Default.ChevronRight, contentDescription = "Next Month")
                            }
                        }
                    }

                    // Weekday Labels
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        val daysAbbr = listOf("Su", "Mo", "Tu", "We", "Th", "Fr", "Sa")
                        daysAbbr.forEach { dayName ->
                            Text(
                                text = dayName,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // Calendar Grid (7 columns)
                    val (firstDayOfWeek, maxDays) = daysInMonth
                    val leadingBlanks = firstDayOfWeek - 1
                    val totalSlots = leadingBlanks + maxDays
                    val totalRows = (totalSlots + 6) / 7

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        for (row in 0 until totalRows) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                for (col in 0 until 7) {
                                    val cellIndex = row * 7 + col
                                    val dayNum = cellIndex - leadingBlanks + 1

                                    if (dayNum in 1..maxDays) {
                                        val dateStr = "%04d-%02d-%02d".format(displayedYear, displayedMonth + 1, dayNum)
                                        val isSelected = dateStr == selectedDateStr
                                        val isToday = dateStr == todayDateStr
                                        val dayEvents = allEvents.filter { it.date == dateStr }

                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .aspectRatio(1f)
                                                .padding(2.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(
                                                    when {
                                                        isSelected -> MaterialTheme.colorScheme.primary
                                                        isToday -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                                        else -> Color.Transparent
                                                    }
                                                )
                                                .border(
                                                    width = if (isToday && !isSelected) 1.5.dp else 0.dp,
                                                    color = if (isToday && !isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                                    shape = RoundedCornerShape(8.dp)
                                                )
                                                .clickable { selectedDateStr = dateStr },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text(
                                                    text = "$dayNum",
                                                    fontSize = 12.sp,
                                                    fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                                                    color = when {
                                                        isSelected -> MaterialTheme.colorScheme.onPrimary
                                                        isToday -> MaterialTheme.colorScheme.primary
                                                        else -> MaterialTheme.colorScheme.onSurface
                                                    }
                                                )

                                                // Event dot markers (Important = Yellow, Most Important = Red, Normal = Blue)
                                                if (dayEvents.isNotEmpty()) {
                                                    Row(
                                                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                                                        modifier = Modifier.padding(top = 1.dp)
                                                    ) {
                                                        dayEvents.take(3).forEach { ev ->
                                                            val dotColor = when (ev.priority) {
                                                                "Most Important" -> EventMostImportantColor
                                                                "Important" -> EventImportantColor
                                                                else -> EventNormalColor
                                                            }
                                                            Box(
                                                                modifier = Modifier
                                                                    .size(4.dp)
                                                                    .clip(CircleShape)
                                                                    .background(if (isSelected) Color.White else dotColor)
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    } else {
                                        Spacer(modifier = Modifier.weight(1f).aspectRatio(1f))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Selected Date Events Section
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Events on $selectedDateStr",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    if (isAdmin) {
                        Button(
                            onClick = { showAddEventDialog = true },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.testTag("admin_add_event_btn")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Add Event", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                if (selectedDateEvents.isEmpty()) {
                    Text(
                        text = "No academic events scheduled on this date.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        selectedDateEvents.forEach { event ->
                            CalendarEventItemCard(
                                event = event,
                                isAdmin = isAdmin,
                                onEdit = { eventToEdit = event },
                                onDelete = {
                                    scope.launch {
                                        viewModel.repository.deleteCalendarEvent(event)
                                        Toast.makeText(context, "Deleted event", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Add or Edit Event Dialog
    if (showAddEventDialog || eventToEdit != null) {
        val editing = eventToEdit
        CalendarEventFormDialog(
            defaultDate = selectedDateStr,
            event = editing,
            onDismiss = {
                showAddEventDialog = false
                eventToEdit = null
            },
            onSave = { updatedEvent ->
                scope.launch {
                    if (editing != null) {
                        viewModel.repository.updateCalendarEvent(updatedEvent)
                        Toast.makeText(context, "Event updated", Toast.LENGTH_SHORT).show()
                    } else {
                        viewModel.repository.insertCalendarEvent(updatedEvent)
                        Toast.makeText(context, "Event added to calendar", Toast.LENGTH_SHORT).show()
                    }
                    showAddEventDialog = false
                    eventToEdit = null
                }
            }
        )
    }
}

@Composable
fun CalendarEventItemCard(
    event: CalendarEvent,
    isAdmin: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (borderColor, bgColor, label) = when (event.priority) {
        "Most Important" -> Triple(EventMostImportantColor, EventMostImportantColor.copy(alpha = 0.12f), "Most Important")
        "Important" -> Triple(EventImportantColor, EventImportantColor.copy(alpha = 0.15f), "Important")
        else -> Triple(EventNormalColor, EventNormalColor.copy(alpha = 0.1f), "Normal")
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("event_card_${event.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.2.dp, borderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = bgColor,
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Event,
                        contentDescription = null,
                        tint = borderColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = event.topic,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (event.time.isNotBlank()) {
                    Text(
                        text = "Time: ${event.time}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(6.dp),
                color = bgColor
            ) {
                Text(
                    text = label,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = borderColor,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }

            if (isAdmin) {
                Row {
                    IconButton(onClick = onEdit, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(14.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(14.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun CalendarEventFormDialog(
    defaultDate: String,
    event: CalendarEvent?,
    onDismiss: () -> Unit,
    onSave: (CalendarEvent) -> Unit
) {
    var topic by remember { mutableStateOf(event?.topic ?: "") }
    var date by remember { mutableStateOf(event?.date ?: defaultDate) }
    var time by remember { mutableStateOf(event?.time ?: "10:00 AM") }
    var priority by remember { mutableStateOf(event?.priority ?: "Normal") }

    val priorities = listOf("Normal", "Important", "Most Important")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (event == null) "Add Calendar Event" else "Edit Event")
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = topic,
                    onValueChange = { topic = it },
                    label = { Text("Event Topic / Announcement") },
                    modifier = Modifier.fillMaxWidth().testTag("event_topic_input")
                )

                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Date (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = time,
                    onValueChange = { time = it },
                    label = { Text("Time (Optional)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Priority Level:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    priorities.forEach { p ->
                        val isSelected = p == priority
                        val btnColor = when (p) {
                            "Most Important" -> EventMostImportantColor
                            "Important" -> EventImportantColor
                            else -> EventNormalColor
                        }

                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { priority = p },
                            color = if (isSelected) btnColor else btnColor.copy(alpha = 0.15f),
                            border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, btnColor)
                        ) {
                            Text(
                                text = p,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else btnColor,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (topic.isNotBlank()) {
                        onSave(
                            CalendarEvent(
                                id = event?.id ?: 0L,
                                date = date.trim(),
                                topic = topic.trim(),
                                time = time.trim(),
                                priority = priority
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
