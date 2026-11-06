package com.example.data.local

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface DisciplineDao {

    // === TASKS ===
    @Query("SELECT * FROM tasks ORDER BY dueDateMillis ASC")
    fun getAllTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE dueDateMillis BETWEEN :startMillis AND :endMillis ORDER BY dueDateMillis ASC")
    fun getTasksForRange(startMillis: Long, endMillis: Long): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE isCompleted = 0 AND dueDateMillis < :currentTimeMillis ORDER BY dueDateMillis ASC")
    fun getOverdueTasksFlow(currentTimeMillis: Long): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE isCompleted = 0 AND dueDateMillis < :currentTimeMillis")
    suspend fun getOverdueTasksSync(currentTimeMillis: Long): List<TaskEntity>

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getTaskById(id: Long): TaskEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity): Long

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Delete
    suspend fun deleteTask(task: TaskEntity)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteTaskById(id: Long)

    // === HABITS ===
    @Query("SELECT * FROM habits ORDER BY id ASC")
    fun getAllHabits(): Flow<List<HabitEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: HabitEntity): Long

    @Update
    suspend fun updateHabit(habit: HabitEntity)

    @Delete
    suspend fun deleteTask(habit: HabitEntity)

    @Query("DELETE FROM habits WHERE id = :id")
    suspend fun deleteHabitById(id: Long)

    // === HABIT LOGS ===
    @Query("SELECT * FROM habit_logs WHERE dateString = :dateString")
    fun getHabitLogsForDate(dateString: String): Flow<List<HabitLogEntity>>

    @Query("SELECT * FROM habit_logs WHERE dateString LIKE :monthPrefix || '%'")
    fun getHabitLogsForMonth(monthPrefix: String): Flow<List<HabitLogEntity>>

    @Query("SELECT * FROM habit_logs WHERE habitId = :habitId")
    fun getHabitLogsForHabit(habitId: Long): Flow<List<HabitLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabitLog(log: HabitLogEntity)

    @Query("DELETE FROM habit_logs WHERE habitId = :habitId AND dateString = :dateString")
    suspend fun deleteHabitLog(habitId: Long, dateString: String)

    // === TIMETABLE SLOTS ===
    @Query("SELECT * FROM timetable_slots ORDER BY orderIndex ASC")
    fun getTimetableSlots(): Flow<List<TimetableSlotEntity>>

    @Query("SELECT * FROM timetable_slots ORDER BY orderIndex ASC")
    suspend fun getTimetableSlotsSync(): List<TimetableSlotEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTimetableSlot(slot: TimetableSlotEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTimetableSlots(slots: List<TimetableSlotEntity>)

    @Update
    suspend fun updateTimetableSlot(slot: TimetableSlotEntity)

    @Query("DELETE FROM timetable_slots WHERE id = :id")
    suspend fun deleteTimetableSlotById(id: Long)

    @Query("SELECT COUNT(*) FROM timetable_slots")
    suspend fun getTimetableSlotCount(): Int

    // === DISCIPLINE DAYS ===
    @Query("SELECT * FROM discipline_days WHERE dateString = :dateString")
    fun getDisciplineDay(dateString: String): Flow<DisciplineDayEntity?>

    @Query("SELECT * FROM discipline_days WHERE dateString = :dateString")
    suspend fun getDisciplineDaySync(dateString: String): DisciplineDayEntity?

    @Query("SELECT * FROM discipline_days WHERE dateString LIKE :monthPrefix || '%' ORDER BY dateString ASC")
    fun getDisciplineDaysForMonth(monthPrefix: String): Flow<List<DisciplineDayEntity>>

    @Query("SELECT * FROM discipline_days ORDER BY dateString DESC")
    fun getAllDisciplineDays(): Flow<List<DisciplineDayEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateDisciplineDay(day: DisciplineDayEntity)

    // === WEEKLY REVIEWS ===
    @Query("SELECT * FROM weekly_reviews WHERE weekKey = :weekKey")
    fun getWeeklyReview(weekKey: String): Flow<WeeklyReviewEntity?>

    @Query("SELECT * FROM weekly_reviews ORDER BY weekKey DESC")
    fun getAllWeeklyReviews(): Flow<List<WeeklyReviewEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateWeeklyReview(review: WeeklyReviewEntity)

    // === USER SETTINGS ===
    @Query("SELECT * FROM user_settings WHERE id = 1")
    fun getUserSettings(): Flow<UserSettingsEntity?>

    @Query("SELECT * FROM user_settings WHERE id = 1")
    suspend fun getUserSettingsSync(): UserSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateUserSettings(settings: UserSettingsEntity)
}
