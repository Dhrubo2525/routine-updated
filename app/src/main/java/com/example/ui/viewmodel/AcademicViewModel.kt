package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.DefaultData
import com.example.data.model.*
import com.example.data.repository.AcademicRepository
import com.example.data.repository.ImportSummary
import com.example.ui.components.ClassAlertData
import com.example.ui.components.ZoneVibe
import com.example.util.FileStorageManager
import com.example.util.PdfGenerator
import com.example.util.SoundPlayer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

enum class ActiveZone {
    STUDENT,
    DASHBOARD,
    TEACHERS,
    QUIZ,
    CALENDAR,
    ADMIN
}

enum class ClassStatus {
    UPCOMING,
    IN_PROGRESS,
    COMPLETED
}

data class ClassWithStatus(
    val classRoutine: ClassRoutine,
    val status: ClassStatus,
    val timeLabel: String
)

data class NextClassCountdown(
    val classRoutine: ClassRoutine?,
    val countdownText: String, // e.g. "01:23:45"
    val isToday: Boolean,
    val dayLabel: String
)

data class NextQuizCountdown(
    val quiz: Quiz?,
    val countdownText: String
)

class AcademicViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application, viewModelScope)
    val repository = AcademicRepository(database, application)

    // Current Zone
    private val _activeZone = MutableStateFlow(ActiveZone.DASHBOARD)
    val activeZone: StateFlow<ActiveZone> = _activeZone.asStateFlow()

    // Student Zone Sub-view: Week View vs Day View
    private val _isWeekView = MutableStateFlow(false)
    val isWeekView: StateFlow<Boolean> = _isWeekView.asStateFlow()

    // Real-world day
    val currentRealDay: String = getCurrentDayOfWeek()

    private val _selectedDay = MutableStateFlow(currentRealDay)
    val selectedDay: StateFlow<String> = _selectedDay.asStateFlow()

    // Admin Auth State
    private val _isAdminLoggedIn = MutableStateFlow(false)
    val isAdminLoggedIn: StateFlow<Boolean> = _isAdminLoggedIn.asStateFlow()

    // Active On-screen Alert Toast
    private val _activeAlert = MutableStateFlow<ClassAlertData?>(null)
    val activeAlert: StateFlow<ClassAlertData?> = _activeAlert.asStateFlow()

    // Search and Filters
    val teacherSearchQuery = MutableStateFlow("")
    val quizSearchQuery = MutableStateFlow("")

    // Import Preview State
    private val _pendingImport = MutableStateFlow<ImportSummary?>(null)
    val pendingImport: StateFlow<ImportSummary?> = _pendingImport.asStateFlow()

    // Data streams from repository
    val allClasses = repository.allClasses.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val allTeachers = repository.allTeachers.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val allQuizzes = repository.allQuizzes.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val allFiles = repository.allFiles.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val allEvents = repository.allEvents.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val bannerSettings = repository.bannerSettings.map {
        it ?: DefaultData.getDefaultBannerSettings()
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        DefaultData.getDefaultBannerSettings()
    )

    val appSettings = repository.appSettings.map {
        it ?: DefaultData.getDefaultAppSettings()
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        DefaultData.getDefaultAppSettings()
    )

    val notifications = repository.allNotifications.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    // Current Time in millis, updated every second
    private val _currentTimeMillis = MutableStateFlow(System.currentTimeMillis())
    val currentTimeMillis: StateFlow<Long> = _currentTimeMillis.asStateFlow()

    init {
        viewModelScope.launch {
            repository.ensureInitialized()
        }

        // Timer ticking loop for live countdowns
        viewModelScope.launch(Dispatchers.Default) {
            while (true) {
                delay(1000)
                _currentTimeMillis.value = System.currentTimeMillis()
            }
        }
    }

    // Helper: Map current zone to banner vibe
    fun getBannerZoneVibe(zone: ActiveZone): ZoneVibe {
        return when (zone) {
            ActiveZone.STUDENT -> ZoneVibe.STUDENT
            ActiveZone.DASHBOARD -> ZoneVibe.DASHBOARD
            ActiveZone.TEACHERS -> ZoneVibe.TEACHERS
            ActiveZone.QUIZ -> ZoneVibe.QUIZ
            ActiveZone.CALENDAR -> ZoneVibe.CALENDAR
            ActiveZone.ADMIN -> ZoneVibe.ADMIN
        }
    }

    fun setZone(zone: ActiveZone) {
        _activeZone.value = zone
    }

    fun setWeekView(weekView: Boolean) {
        _isWeekView.value = weekView
    }

    fun setSelectedDay(day: String) {
        _selectedDay.value = day
    }

    // Notification Click
    fun onNotificationClicked(notification: AppNotification) {
        viewModelScope.launch {
            repository.markNotificationAsRead(notification.id)
            when (notification.type) {
                "class" -> {
                    _activeZone.value = ActiveZone.STUDENT
                    _isWeekView.value = false
                    if (notification.referenceId.isNotBlank()) {
                        _selectedDay.value = notification.referenceId
                    }
                }
                "quiz" -> {
                    _activeZone.value = ActiveZone.QUIZ
                }
                "calendar" -> {
                    _activeZone.value = ActiveZone.CALENDAR
                }
                else -> {
                    _activeZone.value = ActiveZone.DASHBOARD
                }
            }
        }
    }

    fun markAllNotificationsRead() {
        viewModelScope.launch { repository.markAllNotificationsAsRead() }
    }

    fun clearNotifications() {
        viewModelScope.launch { repository.clearAllNotifications() }
    }

    // Toggle Themes & Modes
    fun toggleDarkMode() {
        viewModelScope.launch {
            val current = appSettings.value
            repository.updateAppSettings(current.copy(isDarkMode = !current.isDarkMode))
        }
    }

    fun toggleDesktopMode() {
        viewModelScope.launch {
            val current = appSettings.value
            repository.updateAppSettings(current.copy(isDesktopMode = !current.isDesktopMode))
        }
    }

    // Admin Auth
    suspend fun loginAdmin(password: String): Boolean {
        val success = repository.authenticateAdmin(password)
        if (success) {
            _isAdminLoggedIn.value = true
        }
        return success
    }

    fun logoutAdmin() {
        _isAdminLoggedIn.value = false
    }

    fun resetAdminCredentials() {
        viewModelScope.launch {
            repository.resetAdminCredentials()
        }
    }

    // Alert & Sound system
    fun triggerAlertPreview() {
        val previewData = ClassAlertData(
            courseName = "Software Engineering",
            courseCode = "CSE323",
            room = "KT-502",
            time = "10:00 AM",
            teacher = "Tasnim Dhrubo (TD)",
            message = "Class starts in 10 minutes!"
        )
        _activeAlert.value = previewData
        SoundPlayer.playSound(getApplication(), appSettings.value.notificationSound)
    }

    fun testSound(soundName: String) {
        SoundPlayer.playSound(getApplication(), soundName)
    }

    fun dismissAlert() {
        _activeAlert.value = null
    }

    // PDF generation
    fun downloadWeeklyPdf(): File? {
        val classes = allClasses.value
        val settings = appSettings.value
        return PdfGenerator.generateWeeklyPdf(
            context = getApplication(),
            classes = classes,
            department = settings.department,
            section = settings.section
        )
    }

    fun downloadAndSaveWeeklyPdf(context: Context): File? {
        val file = downloadWeeklyPdf() ?: return null
        FileStorageManager.downloadPdfToDownloads(
            context = context,
            sourcePdf = file,
            targetFileName = "Routine_Scrapper_Weekly_Routine.pdf"
        )
        return file
    }

    fun sharePdf(file: File) {
        PdfGenerator.shareOrOpenPdf(getApplication(), file)
    }

    // Backup & Import
    suspend fun exportBackupJson(): String {
        return repository.exportFullBackupJson()
    }

    fun prepareImport(jsonString: String) {
        viewModelScope.launch {
            val summary = repository.parseImportSummary(jsonString)
            _pendingImport.value = summary
        }
    }

    fun confirmImport() {
        val summary = _pendingImport.value ?: return
        viewModelScope.launch {
            val success = repository.applyFullImport(summary.jsonContent)
            if (success) {
                repository.addNotification(
                    AppNotification(
                        title = "Backup Restored Successfully",
                        message = "Imported ${summary.classesCount} classes, ${summary.teachersCount} teachers, and ${summary.quizzesCount} quizzes.",
                        type = "class",
                        referenceId = currentRealDay
                    )
                )
            }
            _pendingImport.value = null
        }
    }

    fun cancelImport() {
        _pendingImport.value = null
    }

    fun deleteAllData() {
        viewModelScope.launch {
            repository.deleteAllData()
        }
    }

    // Today's classes with status
    val todayClassesWithStatus: StateFlow<List<ClassWithStatus>> = combine(
        allClasses,
        currentTimeMillis
    ) { classes, _ ->
        val today = currentRealDay
        val dayClasses = classes.filter { it.day.equals(today, ignoreCase = true) }
        dayClasses.map { cls ->
            val status = calculateClassStatus(cls.startTime, cls.endTime)
            ClassWithStatus(
                classRoutine = cls,
                status = status,
                timeLabel = "${cls.startTime} - ${cls.endTime}"
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Next Class Countdown
    val nextClassCountdown: StateFlow<NextClassCountdown> = combine(
        allClasses,
        currentTimeMillis
    ) { classes, _ ->
        calculateNextClass(classes)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        NextClassCountdown(null, "No classes", true, "")
    )

    // Next Quiz Countdown
    val nextQuizCountdown: StateFlow<NextQuizCountdown> = combine(
        allQuizzes,
        currentTimeMillis
    ) { quizzes, _ ->
        calculateNextQuiz(quizzes)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        NextQuizCountdown(null, "No remaining quiz is scheduled")
    )

    // Today's non-duplicate teachers
    val todayTeachers: StateFlow<List<Teacher>> = combine(
        allClasses,
        allTeachers
    ) { classes, teachers ->
        val today = currentRealDay
        val teacherNamesToday = classes
            .filter { it.day.equals(today, ignoreCase = true) }
            .map { it.teacher.trim() }
            .toSet()

        teachers.filter { teacher ->
            teacherNamesToday.any { name ->
                name.contains(teacher.initial, ignoreCase = true) ||
                        name.contains(teacher.fullName, ignoreCase = true)
            }
        }.ifEmpty {
            // If initials don't match exactly, fallback to first 2 teachers to display
            teachers.take(3)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Today's remaining calendar events
    val todayCalendarEvents: StateFlow<List<CalendarEvent>> = combine(
        allEvents,
        currentTimeMillis
    ) { events, _ ->
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        events.filter { it.date == todayStr }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private fun calculateClassStatus(startTimeStr: String, endTimeStr: String): ClassStatus {
        try {
            val format = SimpleDateFormat("hh:mm a", Locale.US)
            val nowCal = Calendar.getInstance()
            val startParsed = format.parse(startTimeStr.trim()) ?: return ClassStatus.UPCOMING
            val endParsed = format.parse(endTimeStr.trim()) ?: return ClassStatus.UPCOMING

            val startCal = Calendar.getInstance().apply {
                val temp = Calendar.getInstance()
                temp.time = startParsed
                set(Calendar.HOUR_OF_DAY, temp.get(Calendar.HOUR_OF_DAY))
                set(Calendar.MINUTE, temp.get(Calendar.MINUTE))
                set(Calendar.SECOND, 0)
            }

            val endCal = Calendar.getInstance().apply {
                val temp = Calendar.getInstance()
                temp.time = endParsed
                set(Calendar.HOUR_OF_DAY, temp.get(Calendar.HOUR_OF_DAY))
                set(Calendar.MINUTE, temp.get(Calendar.MINUTE))
                set(Calendar.SECOND, 0)
            }

            return when {
                nowCal.before(startCal) -> ClassStatus.UPCOMING
                nowCal.after(endCal) -> ClassStatus.COMPLETED
                else -> ClassStatus.IN_PROGRESS
            }
        } catch (e: Exception) {
            return ClassStatus.UPCOMING
        }
    }

    private fun calculateNextClass(classes: List<ClassRoutine>): NextClassCountdown {
        if (classes.isEmpty()) {
            return NextClassCountdown(null, "No classes scheduled", true, "")
        }

        val today = currentRealDay
        val nowCal = Calendar.getInstance()
        val format = SimpleDateFormat("hh:mm a", Locale.US)

        // First check today's upcoming classes
        val todayClasses = classes.filter { it.day.equals(today, ignoreCase = true) }
        for (cls in todayClasses) {
            try {
                val parsed = format.parse(cls.startTime.trim())
                if (parsed != null) {
                    val startCal = Calendar.getInstance().apply {
                        val temp = Calendar.getInstance()
                        temp.time = parsed
                        set(Calendar.HOUR_OF_DAY, temp.get(Calendar.HOUR_OF_DAY))
                        set(Calendar.MINUTE, temp.get(Calendar.MINUTE))
                        set(Calendar.SECOND, 0)
                    }
                    if (startCal.after(nowCal)) {
                        val diffMillis = startCal.timeInMillis - nowCal.timeInMillis
                        val hours = (diffMillis / (1000 * 60 * 60))
                        val minutes = (diffMillis / (1000 * 60)) % 60
                        val seconds = (diffMillis / 1000) % 60
                        val countdown = "%02d:%02d:%02d".format(hours, minutes, seconds)
                        return NextClassCountdown(cls, countdown, true, "Today • ${cls.startTime}")
                    }
                }
            } catch (e: Exception) {
                // Ignore
            }
        }

        // If no more classes today, find the earliest class on subsequent days
        val daysOrder = listOf("Saturday", "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday")
        val currentIdx = daysOrder.indexOfFirst { it.equals(today, ignoreCase = true) }
        val startCheckIdx = if (currentIdx >= 0) (currentIdx + 1) % daysOrder.size else 0

        for (i in 0 until daysOrder.size) {
            val checkDay = daysOrder[(startCheckIdx + i) % daysOrder.size]
            val dayClasses = classes.filter { it.day.equals(checkDay, ignoreCase = true) }
            if (dayClasses.isNotEmpty()) {
                val firstClass = dayClasses.first()
                return NextClassCountdown(
                    firstClass,
                    "Starts ${firstClass.startTime}",
                    false,
                    "$checkDay • ${firstClass.startTime}"
                )
            }
        }

        return NextClassCountdown(classes.firstOrNull(), "Upcoming", false, "Scheduled")
    }

    private fun calculateNextQuiz(quizzes: List<Quiz>): NextQuizCountdown {
        if (quizzes.isEmpty()) {
            return NextQuizCountdown(null, "No remaining quiz is scheduled")
        }

        val sdf = SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.US)
        val now = System.currentTimeMillis()

        val sortedUpcoming = quizzes.mapNotNull { quiz ->
            try {
                val dateStr = "${quiz.date} ${quiz.time}"
                val targetDate = sdf.parse(dateStr)
                if (targetDate != null && targetDate.time > now) {
                    Pair(quiz, targetDate.time)
                } else null
            } catch (e: Exception) {
                null
            }
        }.sortedBy { it.second }

        if (sortedUpcoming.isEmpty()) {
            // If all dates past or invalid, pick the last entered quiz
            return NextQuizCountdown(quizzes.firstOrNull(), "Scheduled")
        }

        val (nextQuiz, targetMillis) = sortedUpcoming.first()
        val diffMillis = targetMillis - now
        val days = diffMillis / (1000 * 60 * 60 * 24)
        val hours = (diffMillis / (1000 * 60 * 60)) % 24
        val minutes = (diffMillis / (1000 * 60)) % 60
        val seconds = (diffMillis / 1000) % 60

        val countdown = if (days > 0) {
            "${days}d ${hours}h ${minutes}m"
        } else {
            "%02d:%02d:%02d".format(hours, minutes, seconds)
        }

        return NextQuizCountdown(nextQuiz, countdown)
    }

    companion object {
        fun getCurrentDayOfWeek(): String {
            val cal = Calendar.getInstance()
            return when (cal.get(Calendar.DAY_OF_WEEK)) {
                Calendar.SATURDAY -> "Saturday"
                Calendar.SUNDAY -> "Sunday"
                Calendar.MONDAY -> "Monday"
                Calendar.TUESDAY -> "Tuesday"
                Calendar.WEDNESDAY -> "Wednesday"
                Calendar.THURSDAY -> "Thursday"
                Calendar.FRIDAY -> "Friday"
                else -> "Saturday"
            }
        }
    }
}
