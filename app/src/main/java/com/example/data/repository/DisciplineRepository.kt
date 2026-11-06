package com.example.data.repository

import com.example.data.local.DisciplineDao
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class DisciplineRepository(private val dao: DisciplineDao) {

    // Date Formatters
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    fun getTodayDateString(): String = dateFormat.format(Calendar.getInstance().time)

    // === TASKS ===
    val allTasks: Flow<List<TaskEntity>> = dao.getAllTasks()

    fun getTasksForRange(startMillis: Long, endMillis: Long): Flow<List<TaskEntity>> =
        dao.getTasksForRange(startMillis, endMillis)

    fun getOverdueTasks(currentTimeMillis: Long): Flow<List<TaskEntity>> =
        dao.getOverdueTasksFlow(currentTimeMillis)

    suspend fun getTaskById(id: Long): TaskEntity? = dao.getTaskById(id)

    suspend fun insertTask(task: TaskEntity): Long = dao.insertTask(task)

    suspend fun updateTask(task: TaskEntity) = dao.updateTask(task)

    suspend fun deleteTask(task: TaskEntity) = dao.deleteTask(task)

    suspend fun deleteTaskById(id: Long) = dao.deleteTaskById(id)

    suspend fun toggleTaskCompletion(task: TaskEntity): Boolean {
        val newStatus = !task.isCompleted
        val updated = task.copy(
            isCompleted = newStatus,
            completedAtMillis = if (newStatus) System.currentTimeMillis() else null
        )
        dao.updateTask(updated)
        return newStatus
    }

    suspend fun toggleSubtask(task: TaskEntity, subtaskId: String) {
        val currentSubtasks = SubTaskHelper.fromJson(task.subtasksJson).toMutableList()
        val index = currentSubtasks.indexOfFirst { it.id == subtaskId }
        if (index != -1) {
            val item = currentSubtasks[index]
            currentSubtasks[index] = item.copy(isCompleted = !item.isCompleted)
            val updatedJson = SubTaskHelper.toJson(currentSubtasks)
            val allSubtasksDone = currentSubtasks.all { it.isCompleted }
            val updatedTask = task.copy(
                subtasksJson = updatedJson,
                isCompleted = if (currentSubtasks.isNotEmpty() && allSubtasksDone) true else task.isCompleted,
                completedAtMillis = if (currentSubtasks.isNotEmpty() && allSubtasksDone) System.currentTimeMillis() else task.completedAtMillis
            )
            dao.updateTask(updatedTask)
        }
    }

    // === HABITS ===
    val allHabits: Flow<List<HabitEntity>> = dao.getAllHabits()

    fun getHabitLogsForDate(dateString: String): Flow<List<HabitLogEntity>> =
        dao.getHabitLogsForDate(dateString)

    fun getHabitLogsForMonth(monthPrefix: String): Flow<List<HabitLogEntity>> =
        dao.getHabitLogsForMonth(monthPrefix)

    suspend fun insertHabit(habit: HabitEntity): Long = dao.insertHabit(habit)

    suspend fun updateHabit(habit: HabitEntity) = dao.updateHabit(habit)

    suspend fun deleteHabitById(id: Long) = dao.deleteHabitById(id)

    suspend fun toggleHabitForDate(habitId: Long, dateString: String, isCurrentlyCompleted: Boolean) {
        if (isCurrentlyCompleted) {
            dao.deleteHabitLog(habitId, dateString)
        } else {
            dao.insertHabitLog(
                HabitLogEntity(
                    habitId = habitId,
                    dateString = dateString,
                    completed = true,
                    timestampMillis = System.currentTimeMillis()
                )
            )
        }
    }

    // === TIMETABLE ===
    val timetableSlots: Flow<List<TimetableSlotEntity>> = dao.getTimetableSlots()

    suspend fun getTimetableSlotsSync(): List<TimetableSlotEntity> = dao.getTimetableSlotsSync()

    suspend fun insertTimetableSlot(slot: TimetableSlotEntity): Long = dao.insertTimetableSlot(slot)

    suspend fun updateTimetableSlot(slot: TimetableSlotEntity) = dao.updateTimetableSlot(slot)

    suspend fun deleteTimetableSlotById(id: Long) = dao.deleteTimetableSlotById(id)

    suspend fun toggleTimetableSlotCompletion(slot: TimetableSlotEntity) {
        val today = getTodayDateString()
        val isDoneToday = (slot.isCompletedToday && slot.lastCompletedDate == today)
        val updated = slot.copy(
            isCompletedToday = !isDoneToday,
            lastCompletedDate = if (!isDoneToday) today else ""
        )
        dao.updateTimetableSlot(updated)
    }

    // === DISCIPLINE DAYS & SCORING ===
    fun getDisciplineDay(dateString: String): Flow<DisciplineDayEntity?> =
        dao.getDisciplineDay(dateString)

    fun getDisciplineDaysForMonth(monthPrefix: String): Flow<List<DisciplineDayEntity>> =
        dao.getDisciplineDaysForMonth(monthPrefix)

    val allDisciplineDays: Flow<List<DisciplineDayEntity>> = dao.getAllDisciplineDays()

    suspend fun saveDisciplineDay(day: DisciplineDayEntity) = dao.insertOrUpdateDisciplineDay(day)

    // === WEEKLY REVIEWS ===
    fun getWeeklyReview(weekKey: String): Flow<WeeklyReviewEntity?> =
        dao.getWeeklyReview(weekKey)

    val allWeeklyReviews: Flow<List<WeeklyReviewEntity>> = dao.getAllWeeklyReviews()

    suspend fun saveWeeklyReview(review: WeeklyReviewEntity) = dao.insertOrUpdateWeeklyReview(review)

    // === USER SETTINGS ===
    val userSettings: Flow<UserSettingsEntity?> = dao.getUserSettings()

    suspend fun getUserSettingsSync(): UserSettingsEntity? = dao.getUserSettingsSync()

    suspend fun updateUserSettings(settings: UserSettingsEntity) = dao.insertOrUpdateUserSettings(settings)

    // === BACKUP & EXPORT ===
    suspend fun exportDataJson(): String {
        val root = JSONObject()
        val slots = dao.getTimetableSlotsSync()
        val slotsArray = JSONArray()
        for (s in slots) {
            val obj = JSONObject()
            obj.put("title", s.title)
            obj.put("startTime", s.startTime)
            obj.put("endTime", s.endTime)
            obj.put("category", s.category)
            obj.put("note", s.note)
            obj.put("focusDurationMinutes", s.focusDurationMinutes)
            slotsArray.put(obj)
        }
        root.put("timetable", slotsArray)
        root.put("exportedAt", System.currentTimeMillis())
        root.put("appName", "DISCIPLINE X")
        return root.toString(2)
    }
}
