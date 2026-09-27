package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        ClassRoutine::class,
        Teacher::class,
        Quiz::class,
        QuizFile::class,
        CalendarEvent::class,
        BannerSettings::class,
        AppSettings::class,
        AppNotification::class,
        AdminUser::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun classDao(): ClassDao
    abstract fun teacherDao(): TeacherDao
    abstract fun quizDao(): QuizDao
    abstract fun quizFileDao(): QuizFileDao
    abstract fun calendarEventDao(): CalendarEventDao
    abstract fun bannerSettingsDao(): BannerSettingsDao
    abstract fun appSettingsDao(): AppSettingsDao
    abstract fun notificationDao(): NotificationDao
    abstract fun adminUserDao(): AdminUserDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "routine_scrapper_database"
                )
                    .addCallback(AppDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class AppDatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateDatabase(database)
                    }
                }
            }

            suspend fun populateDatabase(database: AppDatabase) {
                database.classDao().insertAll(DefaultData.getDefaultClasses())
                database.teacherDao().insertAll(DefaultData.getDefaultTeachers())
                database.quizDao().insertAll(DefaultData.getDefaultQuizzes())
                database.quizFileDao().insertAll(DefaultData.getDefaultQuizFiles())
                database.calendarEventDao().insertAll(DefaultData.getDefaultCalendarEvents())
                database.bannerSettingsDao().insertOrUpdate(DefaultData.getDefaultBannerSettings())
                database.appSettingsDao().insertOrUpdate(DefaultData.getDefaultAppSettings())
                DefaultData.getDefaultNotifications().forEach { database.notificationDao().insert(it) }
                database.adminUserDao().insertOrUpdate(DefaultData.getDefaultAdmin())
            }
        }
    }
}
