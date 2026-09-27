package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "classes")
data class ClassRoutine(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val day: String,
    val courseName: String,
    val courseCode: String,
    val startTime: String,
    val endTime: String,
    val room: String,
    val teacher: String
)

@Entity(tableName = "teachers")
data class Teacher(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fullName: String,
    val initial: String,
    val designation: String,
    val courses: String,
    val department: String,
    val room: String,
    val phone: String,
    val email: String
)

@Entity(tableName = "quizzes")
data class Quiz(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val courseName: String,
    val courseCode: String,
    val quizNumber: String,
    val topic: String,
    val description: String,
    val teacherInitial: String,
    val room: String,
    val date: String, // YYYY-MM-DD
    val time: String, // e.g. "10:00 AM"
    val driveLink: String = ""
)

@Entity(tableName = "quiz_files")
data class QuizFile(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val quizId: Long,
    val originalName: String,
    val displayName: String,
    val mimeType: String,
    val fileSize: String,
    val storagePath: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "calendar_events")
data class CalendarEvent(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String, // YYYY-MM-DD
    val topic: String,
    val time: String = "",
    val priority: String = "Normal" // "Normal", "Important", "Most Important"
)

@Entity(tableName = "banner_settings")
data class BannerSettings(
    @PrimaryKey val id: Int = 1,
    val enabled: Boolean = true,
    val title: String = "Routine Scrapper By Dhrubo",
    val subtitle: String = "Academic Routine & Student Information Portal",
    val highlight1: String = "⚡ Spring 2026 Active",
    val highlight2: String = "📅 Real-time Routine",
    val highlight3: String = "🎯 Midterm Prep",
    val highlight1Position: String = "Top",
    val highlight2Position: String = "Bottom",
    val highlight3Position: String = "Right",
    val highlight1Hidden: Boolean = false,
    val highlight2Hidden: Boolean = false,
    val highlight3Hidden: Boolean = false,
    val font: String = "Modern Sans", // "Modern Sans", "Academic Serif", "Monospace", "Tech Bold"
    val position: String = "Middle" // "Left", "Middle", "Right"
)

@Entity(tableName = "app_settings")
data class AppSettings(
    @PrimaryKey val id: Int = 1,
    val classAlertsEnabled: Boolean = true,
    val notificationSound: String = "Gentle Chime", // "Gentle Chime", "Bell", "Double Beep", "Soft Pulse"
    val isDarkMode: Boolean = false,
    val isDesktopMode: Boolean = false,
    val department: String = "Computer Science & Engineering",
    val section: String = "58_A"
)

@Entity(tableName = "notifications")
data class AppNotification(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val message: String,
    val type: String, // "class", "quiz", "calendar"
    val referenceId: String, // day name or quiz id or calendar date
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)

@Entity(tableName = "admin_users")
data class AdminUser(
    @PrimaryKey val id: Int = 1,
    val username: String = "admin",
    val passwordHash: String,
    val salt: String,
    val updatedAt: Long = System.currentTimeMillis()
)
