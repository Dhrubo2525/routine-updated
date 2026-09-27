package com.example.data.local

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ClassDao {
    @Query("SELECT * FROM classes ORDER BY id ASC")
    fun getAllClasses(): Flow<List<ClassRoutine>>

    @Query("SELECT * FROM classes WHERE day = :day ORDER BY startTime ASC")
    fun getClassesForDay(day: String): Flow<List<ClassRoutine>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(classRoutine: ClassRoutine): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(classes: List<ClassRoutine>)

    @Update
    suspend fun update(classRoutine: ClassRoutine)

    @Delete
    suspend fun delete(classRoutine: ClassRoutine)

    @Query("DELETE FROM classes WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM classes")
    suspend fun deleteAll()
}

@Dao
interface TeacherDao {
    @Query("SELECT * FROM teachers ORDER BY fullName ASC")
    fun getAllTeachers(): Flow<List<Teacher>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(teacher: Teacher): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(teachers: List<Teacher>)

    @Update
    suspend fun update(teacher: Teacher)

    @Delete
    suspend fun delete(teacher: Teacher)

    @Query("DELETE FROM teachers WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM teachers")
    suspend fun deleteAll()
}

@Dao
interface QuizDao {
    @Query("SELECT * FROM quizzes ORDER BY date ASC, time ASC")
    fun getAllQuizzes(): Flow<List<Quiz>>

    @Query("SELECT * FROM quizzes WHERE id = :id")
    fun getQuizById(id: Long): Flow<Quiz?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(quiz: Quiz): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(quizzes: List<Quiz>)

    @Update
    suspend fun update(quiz: Quiz)

    @Delete
    suspend fun delete(quiz: Quiz)

    @Query("DELETE FROM quizzes WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM quizzes")
    suspend fun deleteAll()
}

@Dao
interface QuizFileDao {
    @Query("SELECT * FROM quiz_files WHERE quizId = :quizId ORDER BY id ASC")
    fun getFilesForQuiz(quizId: Long): Flow<List<QuizFile>>

    @Query("SELECT * FROM quiz_files ORDER BY id ASC")
    fun getAllFiles(): Flow<List<QuizFile>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(file: QuizFile): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(files: List<QuizFile>)

    @Update
    suspend fun update(file: QuizFile)

    @Delete
    suspend fun delete(file: QuizFile)

    @Query("DELETE FROM quiz_files WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM quiz_files WHERE quizId = :quizId")
    suspend fun deleteForQuiz(quizId: Long)

    @Query("DELETE FROM quiz_files")
    suspend fun deleteAll()
}

@Dao
interface CalendarEventDao {
    @Query("SELECT * FROM calendar_events ORDER BY date ASC, time ASC")
    fun getAllEvents(): Flow<List<CalendarEvent>>

    @Query("SELECT * FROM calendar_events WHERE date = :date")
    fun getEventsForDate(date: String): Flow<List<CalendarEvent>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: CalendarEvent): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(events: List<CalendarEvent>)

    @Update
    suspend fun update(event: CalendarEvent)

    @Delete
    suspend fun delete(event: CalendarEvent)

    @Query("DELETE FROM calendar_events WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM calendar_events")
    suspend fun deleteAll()
}

@Dao
interface BannerSettingsDao {
    @Query("SELECT * FROM banner_settings WHERE id = 1")
    fun getBannerSettings(): Flow<BannerSettings?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(settings: BannerSettings)
}

@Dao
interface AppSettingsDao {
    @Query("SELECT * FROM app_settings WHERE id = 1")
    fun getAppSettings(): Flow<AppSettings?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(settings: AppSettings)
}

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    fun getAllNotifications(): Flow<List<AppNotification>>

    @Query("SELECT * FROM notifications WHERE isRead = 0 ORDER BY timestamp DESC")
    fun getUnreadNotifications(): Flow<List<AppNotification>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(notification: AppNotification): Long

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: Long)

    @Query("UPDATE notifications SET isRead = 1")
    suspend fun markAllAsRead()

    @Query("DELETE FROM notifications")
    suspend fun deleteAll()
}

@Dao
interface AdminUserDao {
    @Query("SELECT * FROM admin_users WHERE id = 1")
    fun getAdmin(): Flow<AdminUser?>

    @Query("SELECT * FROM admin_users WHERE id = 1")
    suspend fun getAdminSync(): AdminUser?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(adminUser: AdminUser)
}
