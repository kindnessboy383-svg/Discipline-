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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Database(
    entities = [
        TaskEntity::class,
        HabitEntity::class,
        HabitLogEntity::class,
        TimetableSlotEntity::class,
        DisciplineDayEntity::class,
        WeeklyReviewEntity::class,
        UserSettingsEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class DisciplineDatabase : RoomDatabase() {

    abstract fun disciplineDao(): DisciplineDao

    companion object {
        @Volatile
        private var INSTANCE: DisciplineDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): DisciplineDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DisciplineDatabase::class.java,
                    "discipline_x_database"
                )
                .fallbackToDestructiveMigration()
                .addCallback(DatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        prepopulateDatabase(database.disciplineDao())
                    }
                }
            }

            private suspend fun prepopulateDatabase(dao: DisciplineDao) {
                // 1. Initial User Settings
                dao.insertOrUpdateUserSettings(
                    UserSettingsEntity(
                        id = 1,
                        notificationsEnabled = true,
                        soundEnabled = true,
                        vibrationEnabled = true,
                        notificationSoundType = "DEFAULT",
                        morningMotivationTime = "05:00",
                        nightReviewTime = "21:00",
                        remind10MinBefore = true,
                        gentleMissedReminder = true,
                        currentStreak = 7,
                        bestStreak = 14,
                        lastActiveDate = ""
                    )
                )

                // 2. Pre-populate Timetable exactly as requested by user
                val timetableSlots = listOf(
                    TimetableSlotEntity(
                        orderIndex = 1,
                        title = "Wake Up",
                        startTime = "05:00",
                        endTime = "05:00",
                        category = "Routine",
                        note = "Get up immediately on the first alarm. No snooze.",
                        focusDurationMinutes = 0
                    ),
                    TimetableSlotEntity(
                        orderIndex = 2,
                        title = "Morning Exercise & Getting Ready",
                        startTime = "05:00",
                        endTime = "06:00",
                        category = "Fitness",
                        note = "Hydrate, dynamic warm-up, calisthenics, shower and breakfast.",
                        focusDurationMinutes = 45
                    ),
                    TimetableSlotEntity(
                        orderIndex = 3,
                        title = "School",
                        startTime = "06:00",
                        endTime = "16:00",
                        category = "School",
                        note = "Total focus in classes. Maximize learning during lectures.",
                        focusDurationMinutes = 0
                    ),
                    TimetableSlotEntity(
                        orderIndex = 4,
                        title = "Focused Study",
                        startTime = "16:30",
                        endTime = "17:30",
                        category = "Study",
                        note = "High-priority school revision and homework deep work block.",
                        focusDurationMinutes = 60
                    ),
                    TimetableSlotEntity(
                        orderIndex = 5,
                        title = "Rest & Preparation",
                        startTime = "17:30",
                        endTime = "18:00",
                        category = "Routine",
                        note = "Mental decompression, pre-workout hydration, gym bag check.",
                        focusDurationMinutes = 0
                    ),
                    TimetableSlotEntity(
                        orderIndex = 6,
                        title = "Gym Session",
                        startTime = "18:00",
                        endTime = "19:30",
                        category = "Fitness",
                        note = "Hypertrophy & strength training. Push intensity and track lifts.",
                        focusDurationMinutes = 90
                    ),
                    TimetableSlotEntity(
                        orderIndex = 7,
                        title = "Refresh & Dinner Preparation",
                        startTime = "19:30",
                        endTime = "20:00",
                        category = "Routine",
                        note = "Nutritious high-protein dinner and cool-down shower.",
                        focusDurationMinutes = 0
                    ),
                    TimetableSlotEntity(
                        orderIndex = 8,
                        title = "Trading Learning Block",
                        startTime = "20:00",
                        endTime = "21:00",
                        category = "Trading",
                        note = "30 minutes focused learning (charts/setups/risk) + 30 min review/journaling.",
                        focusDurationMinutes = 30
                    ),
                    TimetableSlotEntity(
                        orderIndex = 9,
                        title = "Book Reading & Next-Day Planning",
                        startTime = "21:30",
                        endTime = "22:00",
                        category = "Reading",
                        note = "10-15 min non-fiction reading, prep clothes & review tomorrow's tasks.",
                        focusDurationMinutes = 15
                    )
                )
                dao.insertTimetableSlots(timetableSlots)

                // 3. Pre-populate Core Habits
                val habits = listOf(
                    HabitEntity(name = "5:00 AM Wake Up", description = "Feet on floor, no delay", category = "Discipline", colorHex = "#C084FC", iconKey = "alarm", isSystemHabit = true),
                    HabitEntity(name = "Morning Exercise", description = "Calisthenics & mobility", category = "Fitness", colorHex = "#10B981", iconKey = "fitness", isSystemHabit = true),
                    HabitEntity(name = "School & Deep Study", description = "Zero distraction revision", category = "Study", colorHex = "#38BDF8", iconKey = "school", isSystemHabit = true),
                    HabitEntity(name = "Gym Iron Session", description = "Heavy lifting & consistency", category = "Fitness", colorHex = "#F43F5E", iconKey = "fitness", isSystemHabit = true),
                    HabitEntity(name = "Trading Mastery (30m)", description = "Focused chart analysis & strategy", category = "Trading", colorHex = "#F59E0B", iconKey = "chart", isSystemHabit = true),
                    HabitEntity(name = "Book Reading & Planning", description = "Read 15 pages before bed", category = "Mindset", colorHex = "#A855F7", iconKey = "book", isSystemHabit = true)
                )
                val habitIds = mutableListOf<Long>()
                for (h in habits) {
                    val id = dao.insertHabit(h)
                    habitIds.add(id)
                }

                // 4. Initial Sample Tasks for Today and Upcoming
                val cal = Calendar.getInstance()
                cal.set(Calendar.HOUR_OF_DAY, 17)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)

                val todayTasks = listOf(
                    TaskEntity(
                        title = "Physics & Calculus Chapter 4 Review",
                        description = "Solve 10 practice problems and summarize core formulas.",
                        priority = "HIGH",
                        category = "Study",
                        dueDateMillis = cal.timeInMillis,
                        reminderMinutesBefore = 10,
                        subtasksJson = "[{\"id\":\"1\",\"text\":\"Review lecture notes\",\"isCompleted\":true},{\"id\":\"2\",\"text\":\"Solve exercise 4.1 to 4.5\",\"isCompleted\":true},{\"id\":\"3\",\"text\":\"Formula flashcard check\",\"isCompleted\":false}]"
                    ),
                    TaskEntity(
                        title = "Trading: Support & Resistance Backtest",
                        description = "Examine 5 recent setups on EUR/USD and record entries in trading journal.",
                        priority = "HIGH",
                        category = "Trading",
                        dueDateMillis = cal.timeInMillis + 3 * 3600 * 1000L,
                        reminderMinutesBefore = 10,
                        subtasksJson = "[{\"id\":\"4\",\"text\":\"30-min focused chart backtesting\",\"isCompleted\":false},{\"id\":\"5\",\"text\":\"Log risk/reward in spreadsheet\",\"isCompleted\":false}]"
                    ),
                    TaskEntity(
                        title = "Gym: Push Day (Chest & Triceps)",
                        description = "Bench press 4x8, Incline Dumbbell Press 3x10, Dips 3x12.",
                        priority = "MEDIUM",
                        category = "Gym",
                        dueDateMillis = cal.timeInMillis + 1 * 3600 * 1000L,
                        reminderMinutesBefore = 10,
                        isCompleted = true,
                        completedAtMillis = System.currentTimeMillis() - 3600 * 1000L
                    ),
                    TaskEntity(
                        title = "Read 15 Pages of 'Atomic Habits'",
                        description = "Chapter 8: How to make habits irresistible.",
                        priority = "LOW",
                        category = "Reading",
                        dueDateMillis = cal.timeInMillis + 5 * 3600 * 1000L,
                        reminderMinutesBefore = 10
                    )
                )
                for (task in todayTasks) {
                    dao.insertTask(task)
                }

                // 5. Pre-fill past 14 days of realistic Discipline Day history for rich analytics
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                val histCal = Calendar.getInstance()
                for (i in 14 downTo 1) {
                    histCal.timeInMillis = System.currentTimeMillis() - i * 86400000L
                    val dateStr = sdf.format(histCal.time)
                    val baseScore = 75 + (i * 7) % 23 // Realistic score 75 - 98
                    val dayEntity = DisciplineDayEntity(
                        dateString = dateStr,
                        score = baseScore,
                        tasksCompleted = 4 + (i % 2),
                        tasksTotal = 5,
                        habitsCompleted = 5 + (i % 2),
                        habitsTotal = 6,
                        timetableCompleted = 7 + (i % 2),
                        timetableTotal = 9,
                        studyCompleted = true,
                        gymCompleted = (i % 7 != 0), // gym rest day once a week
                        readingCompleted = (i % 5 != 0),
                        tradingCompleted = true,
                        notes = if (i == 1) "Great focus in trading and gym" else "High execution day"
                    )
                    dao.insertOrUpdateDisciplineDay(dayEntity)

                    // Log habit completions for history
                    for (hId in habitIds) {
                        if ((hId.toInt() + i) % 7 != 0) {
                            dao.insertHabitLog(HabitLogEntity(habitId = hId, dateString = dateStr, completed = true))
                        }
                    }
                }
            }
        }
    }
}
