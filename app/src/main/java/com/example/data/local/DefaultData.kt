package com.example.data.local

import com.example.data.model.*
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.*

object SecurityHelper {
    fun hashPassword(password: String, salt: String): String {
        val input = password + salt
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(input.toByteArray())
        return digest.joinToString("") { "%02x".format(it) }
    }

    fun generateSalt(): String {
        return UUID.randomUUID().toString().replace("-", "").take(16)
    }

    fun verifyPassword(password: String, salt: String, expectedHash: String): Boolean {
        return hashPassword(password, salt) == expectedHash
    }
}

object DefaultData {
    fun getDefaultAdmin(): AdminUser {
        val salt = "academic_salt_2026"
        val hash = SecurityHelper.hashPassword("admin", salt)
        return AdminUser(
            id = 1,
            username = "admin",
            passwordHash = hash,
            salt = salt,
            updatedAt = System.currentTimeMillis()
        )
    }

    fun getDefaultBannerSettings(): BannerSettings {
        return BannerSettings(
            id = 1,
            enabled = true,
            title = "Routine Scrapper By Dhrubo",
            subtitle = "Academic Routine & Student Information Portal",
            highlight1 = "⚡ Spring 2026 Active",
            highlight2 = "📅 Real-time Routine",
            highlight3 = "🎯 Midterm Prep",
            highlight1Position = "Top",
            highlight2Position = "Bottom",
            highlight3Position = "Right",
            highlight1Hidden = false,
            highlight2Hidden = false,
            highlight3Hidden = false,
            font = "Modern Sans",
            position = "Middle"
        )
    }

    fun getDefaultAppSettings(): AppSettings {
        return AppSettings(
            id = 1,
            classAlertsEnabled = true,
            notificationSound = "Gentle Chime",
            isDarkMode = false,
            isDesktopMode = false,
            department = "Computer Science & Engineering",
            section = "58_A"
        )
    }

    fun getDefaultClasses(): List<ClassRoutine> {
        return listOf(
            // Saturday
            ClassRoutine(day = "Saturday", courseName = "Data Structures & Algorithms", courseCode = "CSE201", startTime = "08:30 AM", endTime = "10:00 AM", room = "KT-401", teacher = "Dr. M. R. Rahman (MRR)"),
            ClassRoutine(day = "Saturday", courseName = "Algorithms Lab", courseCode = "CSE202", startTime = "10:00 AM", endTime = "11:30 AM", room = "LAB-302", teacher = "Engr. Farhana Ahmed (FA)"),
            ClassRoutine(day = "Saturday", courseName = "Discrete Mathematics", courseCode = "CSE212", startTime = "11:30 AM", endTime = "01:00 PM", room = "KT-303", teacher = "Prof. S. K. Das (SKD)"),

            // Sunday
            ClassRoutine(day = "Sunday", courseName = "Database Management Systems", courseCode = "CSE311", startTime = "09:00 AM", endTime = "10:30 AM", room = "KT-204", teacher = "Dr. Tanvir Hossain (TH)"),
            ClassRoutine(day = "Sunday", courseName = "DBMS Lab", courseCode = "CSE312", startTime = "10:30 AM", endTime = "12:00 PM", room = "NET-LAB", teacher = "Dr. Tanvir Hossain (TH)"),
            ClassRoutine(day = "Sunday", courseName = "Software Engineering", courseCode = "CSE323", startTime = "01:30 PM", endTime = "03:00 PM", room = "KT-502", teacher = "Tasnim Dhrubo (TD)"),

            // Monday
            ClassRoutine(day = "Monday", courseName = "Computer Networks", courseCode = "CSE331", startTime = "08:30 AM", endTime = "10:00 AM", room = "KT-402", teacher = "Dr. M. R. Rahman (MRR)"),
            ClassRoutine(day = "Monday", courseName = "Operating Systems", courseCode = "CSE315", startTime = "10:00 AM", endTime = "11:30 AM", room = "KT-301", teacher = "Prof. Kamrul Hasan (KH)"),
            ClassRoutine(day = "Monday", courseName = "Artificial Intelligence", courseCode = "CSE411", startTime = "12:00 PM", endTime = "01:30 PM", room = "KT-601", teacher = "Dr. Nadia Islam (NI)"),

            // Tuesday
            ClassRoutine(day = "Tuesday", courseName = "Discrete Mathematics", courseCode = "CSE212", startTime = "08:30 AM", endTime = "10:00 AM", room = "KT-303", teacher = "Prof. S. K. Das (SKD)"),
            ClassRoutine(day = "Tuesday", courseName = "Software Engineering", courseCode = "CSE323", startTime = "10:00 AM", endTime = "11:30 AM", room = "KT-502", teacher = "Tasnim Dhrubo (TD)"),
            ClassRoutine(day = "Tuesday", courseName = "Operating Systems Lab", courseCode = "CSE316", startTime = "01:00 PM", endTime = "02:30 PM", room = "SYS-LAB", teacher = "Prof. Kamrul Hasan (KH)"),

            // Wednesday
            ClassRoutine(day = "Wednesday", courseName = "Database Management Systems", courseCode = "CSE311", startTime = "09:00 AM", endTime = "10:30 AM", room = "KT-204", teacher = "Dr. Tanvir Hossain (TH)"),
            ClassRoutine(day = "Wednesday", courseName = "Web Technologies", courseCode = "CSE345", startTime = "10:30 AM", endTime = "12:00 PM", room = "WEB-LAB", teacher = "Engr. Farhana Ahmed (FA)"),
            ClassRoutine(day = "Wednesday", courseName = "Computer Networks", courseCode = "CSE331", startTime = "01:30 PM", endTime = "03:00 PM", room = "KT-402", teacher = "Dr. M. R. Rahman (MRR)"),

            // Thursday
            ClassRoutine(day = "Thursday", courseName = "Artificial Intelligence", courseCode = "CSE411", startTime = "08:30 AM", endTime = "10:00 AM", room = "KT-601", teacher = "Dr. Nadia Islam (NI)"),
            ClassRoutine(day = "Thursday", courseName = "Technical Writing & Ethics", courseCode = "ENG102", startTime = "10:00 AM", endTime = "11:30 AM", room = "KT-201", teacher = "Ms. Sharmin Akter (SA)"),
            ClassRoutine(day = "Thursday", courseName = "Capstone Consultation & Mentoring", courseCode = "CSE400", startTime = "11:30 AM", endTime = "01:00 PM", room = "FACULTY-415", teacher = "Tasnim Dhrubo (TD)")
        )
    }

    fun getDefaultTeachers(): List<Teacher> {
        return listOf(
            Teacher(
                fullName = "Dr. M. R. Rahman",
                initial = "MRR",
                designation = "Professor & Head",
                courses = "CSE201, CSE331",
                department = "Computer Science & Engineering",
                room = "KT-410",
                phone = "+880 1711-234567",
                email = "mrrahman@university.edu"
            ),
            Teacher(
                fullName = "Tasnim Dhrubo",
                initial = "TD",
                designation = "Assistant Professor & Routine Coordinator",
                courses = "CSE323, CSE400",
                department = "Computer Science & Engineering",
                room = "KT-415",
                phone = "+880 1819-987654",
                email = "dhrubo@university.edu"
            ),
            Teacher(
                fullName = "Prof. S. K. Das",
                initial = "SKD",
                designation = "Associate Professor",
                courses = "CSE212",
                department = "Computer Science & Engineering",
                room = "KT-320",
                phone = "+880 1912-345678",
                email = "skdas@university.edu"
            ),
            Teacher(
                fullName = "Dr. Tanvir Hossain",
                initial = "TH",
                designation = "Assistant Professor",
                courses = "CSE311, CSE312",
                department = "Computer Science & Engineering",
                room = "KT-218",
                phone = "+880 1723-456789",
                email = "thossain@university.edu"
            ),
            Teacher(
                fullName = "Engr. Farhana Ahmed",
                initial = "FA",
                designation = "Senior Lecturer",
                courses = "CSE202, CSE345",
                department = "Computer Science & Engineering",
                room = "KT-325",
                phone = "+880 1634-567890",
                email = "fahmed@university.edu"
            ),
            Teacher(
                fullName = "Prof. Kamrul Hasan",
                initial = "KH",
                designation = "Professor",
                courses = "CSE315, CSE316",
                department = "Computer Science & Engineering",
                room = "KT-408",
                phone = "+880 1745-678901",
                email = "khasan@university.edu"
            ),
            Teacher(
                fullName = "Dr. Nadia Islam",
                initial = "NI",
                designation = "Associate Professor",
                courses = "CSE411",
                department = "Computer Science & Engineering",
                room = "KT-512",
                phone = "+880 1856-789012",
                email = "nislam@university.edu"
            ),
            Teacher(
                fullName = "Ms. Sharmin Akter",
                initial = "SA",
                designation = "Lecturer",
                courses = "ENG102",
                department = "Humanities & Social Sciences",
                room = "KT-209",
                phone = "+880 1967-890123",
                email = "sakter@university.edu"
            )
        )
    }

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    private fun getUpcomingDateString(daysOffset: Int): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, daysOffset)
        return dateFormat.format(cal.time)
    }

    fun getDefaultQuizzes(): List<Quiz> {
        return listOf(
            Quiz(
                id = 1,
                courseName = "Software Engineering",
                courseCode = "CSE323",
                quizNumber = "Quiz #2",
                topic = "Agile Scrum Methodology & Sprint Planning",
                description = "Focus on User Stories, Acceptance Criteria, Sprint Backlog estimation, and Scrum ceremonies (Sprint Review & Retrospective). Chapter 3 of Sommerville. Bring scientific calculator.",
                teacherInitial = "TD",
                room = "KT-502",
                date = getUpcomingDateString(1),
                time = "10:00 AM",
                driveLink = "https://drive.google.com/drive/folders/sample-se323-quiz2"
            ),
            Quiz(
                id = 2,
                courseName = "Discrete Mathematics",
                courseCode = "CSE212",
                quizNumber = "Quiz #3",
                topic = "Graph Theory, Eulerian & Hamiltonian Circuits",
                description = "Syllabus covers planar graphs, coloring theorem, and Dijkstra shortest path algorithm. 20 marks total (15 MCQ + 5 problem solving).",
                teacherInitial = "SKD",
                room = "KT-303",
                date = getUpcomingDateString(3),
                time = "08:30 AM",
                driveLink = "https://drive.google.com/drive/folders/sample-dm212-quiz3"
            ),
            Quiz(
                id = 3,
                courseName = "Database Management Systems",
                courseCode = "CSE311",
                quizNumber = "Quiz #1",
                topic = "Relational Algebra & Normalization (1NF to BCNF)",
                description = "Prepare functional dependency closure calculations, canonical cover, and 3NF/BCNF decomposition problems.",
                teacherInitial = "TH",
                room = "KT-204",
                date = getUpcomingDateString(6),
                time = "09:00 AM",
                driveLink = "https://drive.google.com/drive/folders/sample-dbms311-quiz1"
            )
        )
    }

    fun getDefaultQuizFiles(): List<QuizFile> {
        return listOf(
            QuizFile(
                id = 1,
                quizId = 1,
                originalName = "scrum_guide_2026.pdf",
                displayName = "Agile_Scrum_Workflow_Handout.pdf",
                mimeType = "application/pdf",
                fileSize = "1.4 MB",
                storagePath = "sample_files/scrum_guide.pdf"
            ),
            QuizFile(
                id = 2,
                quizId = 1,
                originalName = "lecture_sprint_estimation.pptx",
                displayName = "Lecture_Sprint_Estimation_Slides.pptx",
                mimeType = "application/vnd.ms-powerpoint",
                fileSize = "3.8 MB",
                storagePath = "sample_files/sprint_slides.pptx"
            ),
            QuizFile(
                id = 3,
                quizId = 1,
                originalName = "burndown_chart_sample.png",
                displayName = "Sprint_Burndown_Sample.png",
                mimeType = "image/png",
                fileSize = "450 KB",
                storagePath = "sample_files/burndown.png"
            ),
            QuizFile(
                id = 4,
                quizId = 2,
                originalName = "graph_theory_practice_questions.pdf",
                displayName = "Graph_Theory_Practice_Set.pdf",
                mimeType = "application/pdf",
                fileSize = "820 KB",
                storagePath = "sample_files/graph_problems.pdf"
            ),
            QuizFile(
                id = 5,
                quizId = 3,
                originalName = "normalization_worked_examples.pdf",
                displayName = "DBMS_Normalization_Guide_BCNF.pdf",
                mimeType = "application/pdf",
                fileSize = "1.1 MB",
                storagePath = "sample_files/normalization.pdf"
            )
        )
    }

    fun getDefaultCalendarEvents(): List<CalendarEvent> {
        return listOf(
            CalendarEvent(
                date = getUpcomingDateString(0),
                topic = "Weekly Routine Review & Lab Coordination",
                time = "02:00 PM",
                priority = "Normal"
            ),
            CalendarEvent(
                date = getUpcomingDateString(1),
                topic = "Software Engineering Quiz #2 (Agile Scrum)",
                time = "10:00 AM",
                priority = "Important"
            ),
            CalendarEvent(
                date = getUpcomingDateString(4),
                topic = "Midterm Examination Routine Announcement",
                time = "11:00 AM",
                priority = "Most Important"
            ),
            CalendarEvent(
                date = getUpcomingDateString(8),
                topic = "Project Sprint 1 Review & Client Demo",
                time = "03:00 PM",
                priority = "Important"
            ),
            CalendarEvent(
                date = getUpcomingDateString(14),
                topic = "Midterm Examination Commences",
                time = "09:00 AM",
                priority = "Most Important"
            ),
            CalendarEvent(
                date = getUpcomingDateString(20),
                topic = "Last Date of Grade Submission for Quizzes",
                time = "05:00 PM",
                priority = "Normal"
            )
        )
    }

    fun getDefaultNotifications(): List<AppNotification> {
        return listOf(
            AppNotification(
                id = 1,
                title = "Welcome to Routine Scrapper By Dhrubo",
                message = "Your centralized academic routine and student portal is now active for Spring 2026.",
                type = "class",
                referenceId = "Sunday",
                timestamp = System.currentTimeMillis() - 3600_000,
                isRead = false
            ),
            AppNotification(
                id = 2,
                title = "Upcoming Quiz Alert: CSE323",
                message = "Software Engineering Quiz #2 is scheduled for tomorrow at 10:00 AM in room KT-502.",
                type = "quiz",
                referenceId = "1",
                timestamp = System.currentTimeMillis() - 1800_000,
                isRead = false
            ),
            AppNotification(
                id = 3,
                title = "Academic Notice: Midterm Routine",
                message = "Midterm Examination routine announcement is scheduled for this week.",
                type = "calendar",
                referenceId = getUpcomingDateString(4),
                timestamp = System.currentTimeMillis() - 600_000,
                isRead = false
            )
        )
    }
}
