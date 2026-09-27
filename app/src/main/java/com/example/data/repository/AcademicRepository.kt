package com.example.data.repository

import android.content.Context
import com.example.data.local.*
import com.example.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

data class ImportSummary(
    val classesCount: Int,
    val teachersCount: Int,
    val quizzesCount: Int,
    val filesCount: Int,
    val eventsCount: Int,
    val jsonContent: String
)

class AcademicRepository(
    private val database: AppDatabase,
    private val context: Context
) {
    private val classDao = database.classDao()
    private val teacherDao = database.teacherDao()
    private val quizDao = database.quizDao()
    private val quizFileDao = database.quizFileDao()
    private val calendarEventDao = database.calendarEventDao()
    private val bannerSettingsDao = database.bannerSettingsDao()
    private val appSettingsDao = database.appSettingsDao()
    private val notificationDao = database.notificationDao()
    private val adminUserDao = database.adminUserDao()

    // Flows
    val allClasses: Flow<List<ClassRoutine>> = classDao.getAllClasses()
    val allTeachers: Flow<List<Teacher>> = teacherDao.getAllTeachers()
    val allQuizzes: Flow<List<Quiz>> = quizDao.getAllQuizzes()
    val allFiles: Flow<List<QuizFile>> = quizFileDao.getAllFiles()
    val allEvents: Flow<List<CalendarEvent>> = calendarEventDao.getAllEvents()
    val bannerSettings: Flow<BannerSettings?> = bannerSettingsDao.getBannerSettings()
    val appSettings: Flow<AppSettings?> = appSettingsDao.getAppSettings()
    val allNotifications: Flow<List<AppNotification>> = notificationDao.getAllNotifications()
    val adminUser: Flow<AdminUser?> = adminUserDao.getAdmin()

    fun getFilesForQuiz(quizId: Long): Flow<List<QuizFile>> = quizFileDao.getFilesForQuiz(quizId)

    // Ensure initial data exists if database was created empty
    suspend fun ensureInitialized() = withContext(Dispatchers.IO) {
        val admin = adminUserDao.getAdminSync()
        if (admin == null) {
            adminUserDao.insertOrUpdate(DefaultData.getDefaultAdmin())
            bannerSettingsDao.insertOrUpdate(DefaultData.getDefaultBannerSettings())
            appSettingsDao.insertOrUpdate(DefaultData.getDefaultAppSettings())
            classDao.insertAll(DefaultData.getDefaultClasses())
            teacherDao.insertAll(DefaultData.getDefaultTeachers())
            quizDao.insertAll(DefaultData.getDefaultQuizzes())
            quizFileDao.insertAll(DefaultData.getDefaultQuizFiles())
            calendarEventDao.insertAll(DefaultData.getDefaultCalendarEvents())
            DefaultData.getDefaultNotifications().forEach { notificationDao.insert(it) }
        }
    }

    // Class operations
    suspend fun insertClass(classRoutine: ClassRoutine): Long = withContext(Dispatchers.IO) {
        classDao.insert(classRoutine)
    }

    suspend fun updateClass(classRoutine: ClassRoutine) = withContext(Dispatchers.IO) {
        classDao.update(classRoutine)
    }

    suspend fun deleteClass(classRoutine: ClassRoutine) = withContext(Dispatchers.IO) {
        classDao.delete(classRoutine)
    }

    suspend fun deleteAllClasses() = withContext(Dispatchers.IO) {
        classDao.deleteAll()
    }

    // Teacher operations
    suspend fun insertTeacher(teacher: Teacher): Long = withContext(Dispatchers.IO) {
        teacherDao.insert(teacher)
    }

    suspend fun updateTeacher(teacher: Teacher) = withContext(Dispatchers.IO) {
        teacherDao.update(teacher)
    }

    suspend fun deleteTeacher(teacher: Teacher) = withContext(Dispatchers.IO) {
        teacherDao.delete(teacher)
    }

    suspend fun deleteAllTeachers() = withContext(Dispatchers.IO) {
        teacherDao.deleteAll()
    }

    // Quiz operations
    suspend fun insertQuiz(quiz: Quiz): Long = withContext(Dispatchers.IO) {
        quizDao.insert(quiz)
    }

    suspend fun updateQuiz(quiz: Quiz) = withContext(Dispatchers.IO) {
        quizDao.update(quiz)
    }

    suspend fun deleteQuiz(quiz: Quiz) = withContext(Dispatchers.IO) {
        quizDao.delete(quiz)
        quizFileDao.deleteForQuiz(quiz.id)
    }

    suspend fun deleteAllQuizzes() = withContext(Dispatchers.IO) {
        quizDao.deleteAll()
        quizFileDao.deleteAll()
    }

    // Quiz file operations
    suspend fun insertQuizFile(file: QuizFile): Long = withContext(Dispatchers.IO) {
        quizFileDao.insert(file)
    }

    suspend fun updateQuizFile(file: QuizFile) = withContext(Dispatchers.IO) {
        quizFileDao.update(file)
    }

    suspend fun deleteQuizFile(file: QuizFile) = withContext(Dispatchers.IO) {
        quizFileDao.delete(file)
    }

    // Calendar operations
    suspend fun insertCalendarEvent(event: CalendarEvent): Long = withContext(Dispatchers.IO) {
        calendarEventDao.insert(event)
    }

    suspend fun updateCalendarEvent(event: CalendarEvent) = withContext(Dispatchers.IO) {
        calendarEventDao.update(event)
    }

    suspend fun deleteCalendarEvent(event: CalendarEvent) = withContext(Dispatchers.IO) {
        calendarEventDao.delete(event)
    }

    suspend fun deleteAllCalendarEvents() = withContext(Dispatchers.IO) {
        calendarEventDao.deleteAll()
    }

    // Delete all academic data
    suspend fun deleteAllData() = withContext(Dispatchers.IO) {
        classDao.deleteAll()
        teacherDao.deleteAll()
        quizDao.deleteAll()
        quizFileDao.deleteAll()
        calendarEventDao.deleteAll()
        notificationDao.deleteAll()
    }

    // Settings
    suspend fun updateBannerSettings(settings: BannerSettings) = withContext(Dispatchers.IO) {
        bannerSettingsDao.insertOrUpdate(settings)
    }

    suspend fun updateAppSettings(settings: AppSettings) = withContext(Dispatchers.IO) {
        appSettingsDao.insertOrUpdate(settings)
    }

    // Notifications
    suspend fun addNotification(notification: AppNotification): Long = withContext(Dispatchers.IO) {
        notificationDao.insert(notification)
    }

    suspend fun markNotificationAsRead(id: Long) = withContext(Dispatchers.IO) {
        notificationDao.markAsRead(id)
    }

    suspend fun markAllNotificationsAsRead() = withContext(Dispatchers.IO) {
        notificationDao.markAllAsRead()
    }

    suspend fun clearAllNotifications() = withContext(Dispatchers.IO) {
        notificationDao.deleteAll()
    }

    // Authentication
    suspend fun authenticateAdmin(password: String): Boolean = withContext(Dispatchers.IO) {
        val admin = adminUserDao.getAdminSync() ?: return@withContext false
        SecurityHelper.verifyPassword(password, admin.salt, admin.passwordHash)
    }

    suspend fun updateAdminCredentials(username: String, newPassword: String): Boolean = withContext(Dispatchers.IO) {
        val newSalt = SecurityHelper.generateSalt()
        val newHash = SecurityHelper.hashPassword(newPassword, newSalt)
        adminUserDao.insertOrUpdate(
            AdminUser(
                id = 1,
                username = username,
                passwordHash = newHash,
                salt = newSalt,
                updatedAt = System.currentTimeMillis()
            )
        )
        true
    }

    suspend fun resetAdminCredentials() = withContext(Dispatchers.IO) {
        adminUserDao.insertOrUpdate(DefaultData.getDefaultAdmin())
    }

    // Export & Backup
    suspend fun exportFullBackupJson(): String = withContext(Dispatchers.IO) {
        val root = JSONObject()
        root.put("version", 1)
        root.put("exportedAt", System.currentTimeMillis())

        val classesList = allClasses.first()
        val classesArray = JSONArray()
        classesList.forEach {
            val item = JSONObject()
            item.put("day", it.day)
            item.put("courseName", it.courseName)
            item.put("courseCode", it.courseCode)
            item.put("startTime", it.startTime)
            item.put("endTime", it.endTime)
            item.put("room", it.room)
            item.put("teacher", it.teacher)
            classesArray.put(item)
        }
        root.put("classes", classesArray)

        val teachersList = allTeachers.first()
        val teachersArray = JSONArray()
        teachersList.forEach {
            val item = JSONObject()
            item.put("fullName", it.fullName)
            item.put("initial", it.initial)
            item.put("designation", it.designation)
            item.put("courses", it.courses)
            item.put("department", it.department)
            item.put("room", it.room)
            item.put("phone", it.phone)
            item.put("email", it.email)
            teachersArray.put(item)
        }
        root.put("teachers", teachersArray)

        val quizzesList = allQuizzes.first()
        val quizzesArray = JSONArray()
        quizzesList.forEach {
            val item = JSONObject()
            item.put("id", it.id)
            item.put("courseName", it.courseName)
            item.put("courseCode", it.courseCode)
            item.put("quizNumber", it.quizNumber)
            item.put("topic", it.topic)
            item.put("description", it.description)
            item.put("teacherInitial", it.teacherInitial)
            item.put("room", it.room)
            item.put("date", it.date)
            item.put("time", it.time)
            item.put("driveLink", it.driveLink)
            quizzesArray.put(item)
        }
        root.put("quizzes", quizzesArray)

        val filesList = allFiles.first()
        val filesArray = JSONArray()
        filesList.forEach {
            val item = JSONObject()
            item.put("quizId", it.quizId)
            item.put("originalName", it.originalName)
            item.put("displayName", it.displayName)
            item.put("mimeType", it.mimeType)
            item.put("fileSize", it.fileSize)
            item.put("storagePath", it.storagePath)
            filesArray.put(item)
        }
        root.put("quizFiles", filesArray)

        val eventsList = allEvents.first()
        val eventsArray = JSONArray()
        eventsList.forEach {
            val item = JSONObject()
            item.put("date", it.date)
            item.put("topic", it.topic)
            item.put("time", it.time)
            item.put("priority", it.priority)
            eventsArray.put(item)
        }
        root.put("calendarEvents", eventsArray)

        val banner = bannerSettings.first() ?: DefaultData.getDefaultBannerSettings()
        val bannerObj = JSONObject()
        bannerObj.put("title", banner.title)
        bannerObj.put("subtitle", banner.subtitle)
        bannerObj.put("highlight1", banner.highlight1)
        bannerObj.put("highlight2", banner.highlight2)
        bannerObj.put("highlight3", banner.highlight3)
        bannerObj.put("font", banner.font)
        bannerObj.put("position", banner.position)
        root.put("bannerSettings", bannerObj)

        val appSet = appSettings.first() ?: DefaultData.getDefaultAppSettings()
        val appSetObj = JSONObject()
        appSetObj.put("department", appSet.department)
        appSetObj.put("section", appSet.section)
        appSetObj.put("classAlertsEnabled", appSet.classAlertsEnabled)
        appSetObj.put("notificationSound", appSet.notificationSound)
        root.put("appSettings", appSetObj)

        root.toString(2)
    }

    suspend fun parseImportSummary(jsonString: String): ImportSummary? = withContext(Dispatchers.Default) {
        try {
            val root = JSONObject(jsonString)
            val classesCount = root.optJSONArray("classes")?.length() ?: 0
            val teachersCount = root.optJSONArray("teachers")?.length() ?: 0
            val quizzesCount = root.optJSONArray("quizzes")?.length() ?: 0
            val filesCount = root.optJSONArray("quizFiles")?.length() ?: 0
            val eventsCount = root.optJSONArray("calendarEvents")?.length() ?: 0
            ImportSummary(
                classesCount = classesCount,
                teachersCount = teachersCount,
                quizzesCount = quizzesCount,
                filesCount = filesCount,
                eventsCount = eventsCount,
                jsonContent = jsonString
            )
        } catch (e: Exception) {
            null
        }
    }

    suspend fun applyFullImport(jsonString: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val root = JSONObject(jsonString)

            // Classes
            root.optJSONArray("classes")?.let { arr ->
                classDao.deleteAll()
                val list = mutableListOf<ClassRoutine>()
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    list.add(
                        ClassRoutine(
                            day = obj.optString("day", "Monday"),
                            courseName = obj.optString("courseName", ""),
                            courseCode = obj.optString("courseCode", ""),
                            startTime = obj.optString("startTime", ""),
                            endTime = obj.optString("endTime", ""),
                            room = obj.optString("room", ""),
                            teacher = obj.optString("teacher", "")
                        )
                    )
                }
                classDao.insertAll(list)
            }

            // Teachers
            root.optJSONArray("teachers")?.let { arr ->
                teacherDao.deleteAll()
                val list = mutableListOf<Teacher>()
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    list.add(
                        Teacher(
                            fullName = obj.optString("fullName", ""),
                            initial = obj.optString("initial", ""),
                            designation = obj.optString("designation", ""),
                            courses = obj.optString("courses", ""),
                            department = obj.optString("department", ""),
                            room = obj.optString("room", ""),
                            phone = obj.optString("phone", ""),
                            email = obj.optString("email", "")
                        )
                    )
                }
                teacherDao.insertAll(list)
            }

            // Quizzes
            root.optJSONArray("quizzes")?.let { arr ->
                quizDao.deleteAll()
                val list = mutableListOf<Quiz>()
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    list.add(
                        Quiz(
                            id = obj.optLong("id", 0L),
                            courseName = obj.optString("courseName", ""),
                            courseCode = obj.optString("courseCode", ""),
                            quizNumber = obj.optString("quizNumber", ""),
                            topic = obj.optString("topic", ""),
                            description = obj.optString("description", ""),
                            teacherInitial = obj.optString("teacherInitial", ""),
                            room = obj.optString("room", ""),
                            date = obj.optString("date", ""),
                            time = obj.optString("time", ""),
                            driveLink = obj.optString("driveLink", "")
                        )
                    )
                }
                quizDao.insertAll(list)
            }

            // Quiz Files
            root.optJSONArray("quizFiles")?.let { arr ->
                quizFileDao.deleteAll()
                val list = mutableListOf<QuizFile>()
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    list.add(
                        QuizFile(
                            quizId = obj.optLong("quizId", 1L),
                            originalName = obj.optString("originalName", ""),
                            displayName = obj.optString("displayName", ""),
                            mimeType = obj.optString("mimeType", ""),
                            fileSize = obj.optString("fileSize", ""),
                            storagePath = obj.optString("storagePath", "")
                        )
                    )
                }
                quizFileDao.insertAll(list)
            }

            // Calendar Events
            root.optJSONArray("calendarEvents")?.let { arr ->
                calendarEventDao.deleteAll()
                val list = mutableListOf<CalendarEvent>()
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    list.add(
                        CalendarEvent(
                            date = obj.optString("date", ""),
                            topic = obj.optString("topic", ""),
                            time = obj.optString("time", ""),
                            priority = obj.optString("priority", "Normal")
                        )
                    )
                }
                calendarEventDao.insertAll(list)
            }

            // Banner settings
            root.optJSONObject("bannerSettings")?.let { obj ->
                val current = bannerSettings.first() ?: DefaultData.getDefaultBannerSettings()
                bannerSettingsDao.insertOrUpdate(
                    current.copy(
                        title = obj.optString("title", current.title),
                        subtitle = obj.optString("subtitle", current.subtitle),
                        highlight1 = obj.optString("highlight1", current.highlight1),
                        highlight2 = obj.optString("highlight2", current.highlight2),
                        highlight3 = obj.optString("highlight3", current.highlight3),
                        font = obj.optString("font", current.font),
                        position = obj.optString("position", current.position)
                    )
                )
            }

            true
        } catch (e: Exception) {
            false
        }
    }
}
