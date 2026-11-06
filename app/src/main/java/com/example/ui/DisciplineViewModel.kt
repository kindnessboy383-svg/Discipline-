package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.DisciplineDatabase
import com.example.data.model.*
import com.example.data.repository.DisciplineRepository
import com.example.notification.NotificationHelper
import com.example.notification.NotificationScheduler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class DisciplineViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: DisciplineRepository
    private val context = application.applicationContext

    init {
        val db = DisciplineDatabase.getDatabase(application, viewModelScope)
        repository = DisciplineRepository(db.disciplineDao())

        // Initialize notification channels and schedules based on saved settings
        viewModelScope.launch(Dispatchers.IO) {
            val settings = repository.getUserSettingsSync()
            val soundEnabled = settings?.soundEnabled ?: true
            val vibrationEnabled = settings?.vibrationEnabled ?: true
            val soundType = settings?.notificationSoundType ?: "DEFAULT"

            NotificationHelper.createNotificationChannels(
                context = context,
                soundEnabled = soundEnabled,
                vibrationEnabled = vibrationEnabled,
                soundType = soundType
            )

            if (settings?.notificationsEnabled != false) {
                NotificationScheduler.scheduleDailyMorningWakeup(context, settings?.morningMotivationTime ?: "05:00")
                NotificationScheduler.scheduleDailyNightReview(context, settings?.nightReviewTime ?: "21:00")
            }
        }
    }

    private val monthFormat = SimpleDateFormat("yyyy-MM", Locale.US)
    private val currentMonthPrefix = monthFormat.format(Calendar.getInstance().time)

    // Current date string
    val todayDateString: String get() = repository.getTodayDateString()

    // Quotes
    private val _currentQuote = MutableStateFlow(MotivationVault.quotes.random())
    val currentQuote: StateFlow<MotivationQuote> = _currentQuote.asStateFlow()

    fun nextQuote() {
        _currentQuote.value = MotivationVault.quotes.random()
    }

    // Feedback message (for task completions / supportive resets)
    private val _userFeedbackMessage = MutableStateFlow<String?>(null)
    val userFeedbackMessage: StateFlow<String?> = _userFeedbackMessage.asStateFlow()

    fun clearFeedbackMessage() {
        _userFeedbackMessage.value = null
    }

    // Data streams
    val allTasks: StateFlow<List<TaskEntity>> = repository.allTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allHabits: StateFlow<List<HabitEntity>> = repository.allHabits
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todayHabitLogs: StateFlow<List<HabitLogEntity>> = repository.getHabitLogsForDate(todayDateString)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val timetableSlots: StateFlow<List<TimetableSlotEntity>> = repository.timetableSlots
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todayDisciplineDay: StateFlow<DisciplineDayEntity?> = repository.getDisciplineDay(todayDateString)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val monthlyDisciplineDays: StateFlow<List<DisciplineDayEntity>> = repository.getDisciplineDaysForMonth(currentMonthPrefix)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userSettings: StateFlow<UserSettingsEntity?> = repository.userSettings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allWeeklyReviews: StateFlow<List<WeeklyReviewEntity>> = repository.allWeeklyReviews
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Live Discipline Score calculation (0 to 100)
    val liveScore: StateFlow<Int> = combine(
        allTasks,
        allHabits,
        todayHabitLogs,
        timetableSlots
    ) { tasks, habits, logs, slots ->
        val todayStart = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val todayEnd = todayStart + 86400000L

        // Tasks component (max 50 points)
        val todayTasks = tasks.filter { it.dueDateMillis in todayStart until todayEnd }
        val taskScore = if (todayTasks.isNotEmpty()) {
            val completed = todayTasks.count { it.isCompleted }
            (completed.toFloat() / todayTasks.size.toFloat() * 50f).toInt()
        } else {
            35 // Base credit when no tasks scheduled today
        }

        // Habits component (max 30 points)
        val habitScore = if (habits.isNotEmpty()) {
            val completed = logs.count { it.completed }
            (completed.toFloat() / habits.size.toFloat() * 30f).toInt().coerceAtMost(30)
        } else {
            20
        }

        // Timetable component (max 20 points)
        val timetableScore = if (slots.isNotEmpty()) {
            val completed = slots.count { it.isCompletedToday && it.lastCompletedDate == todayDateString }
            (completed.toFloat() / slots.size.toFloat() * 20f).toInt()
        } else {
            15
        }

        val total = (taskScore + habitScore + timetableScore).coerceIn(0, 100)
        total
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 75)

    // Save calculated score to daily entity
    init {
        viewModelScope.launch(Dispatchers.IO) {
            combine(liveScore, allTasks, allHabits, todayHabitLogs, timetableSlots) { score, tasks, habits, logs, slots ->
                val todayStart = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis
                val todayTasks = tasks.filter { it.dueDateMillis in todayStart until (todayStart + 86400000L) }
                val completedTasks = todayTasks.count { it.isCompleted }
                val completedHabits = logs.count { it.completed }
                val completedSlots = slots.count { it.isCompletedToday && it.lastCompletedDate == todayDateString }

                val studyDone = completedTasks > 0 || completedSlots > 2
                val gymDone = slots.any { it.category == "Fitness" && it.isCompletedToday && it.lastCompletedDate == todayDateString }
                val readingDone = slots.any { it.category == "Reading" && it.isCompletedToday && it.lastCompletedDate == todayDateString }
                val tradingDone = slots.any { it.category == "Trading" && it.isCompletedToday && it.lastCompletedDate == todayDateString }

                DisciplineDayEntity(
                    dateString = todayDateString,
                    score = score,
                    tasksCompleted = completedTasks,
                    tasksTotal = todayTasks.size.coerceAtLeast(1),
                    habitsCompleted = completedHabits,
                    habitsTotal = habits.size.coerceAtLeast(1),
                    timetableCompleted = completedSlots,
                    timetableTotal = slots.size.coerceAtLeast(1),
                    studyCompleted = studyDone,
                    gymCompleted = gymDone,
                    readingCompleted = readingDone,
                    tradingCompleted = tradingDone
                )
            }.collectLatest { dayEntity ->
                repository.saveDisciplineDay(dayEntity)
            }
        }
    }

    // === TASK OPERATIONS ===
    fun addTask(
        title: String,
        description: String,
        priority: String,
        category: String,
        dueDateMillis: Long,
        reminderMinutesBefore: Int,
        subtasks: List<SubTask>
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val task = TaskEntity(
                title = title.trim(),
                description = description.trim(),
                priority = priority,
                category = category,
                dueDateMillis = dueDateMillis,
                reminderMinutesBefore = reminderMinutesBefore,
                subtasksJson = SubTaskHelper.toJson(subtasks)
            )
            val newId = repository.insertTask(task)
            val scheduledTask = task.copy(id = newId)
            NotificationScheduler.scheduleTaskReminders(context, scheduledTask)
            _userFeedbackMessage.value = "Task scheduled with sound & vibration reminders."
        }
    }

    fun updateTask(task: TaskEntity, subtasks: List<SubTask>) {
        viewModelScope.launch(Dispatchers.IO) {
            val updated = task.copy(subtasksJson = SubTaskHelper.toJson(subtasks))
            repository.updateTask(updated)
            NotificationScheduler.scheduleTaskReminders(context, updated)
        }
    }

    fun toggleTask(task: TaskEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            val isNowCompleted = repository.toggleTaskCompletion(task)
            if (isNowCompleted) {
                NotificationScheduler.cancelTaskReminders(context, task.id)
                _userFeedbackMessage.value = MotivationVault.completionMessages.random()
            } else {
                NotificationScheduler.scheduleTaskReminders(context, task)
            }
        }
    }

    fun toggleSubtask(task: TaskEntity, subtaskId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.toggleSubtask(task, subtaskId)
        }
    }

    fun deleteTask(task: TaskEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            NotificationScheduler.cancelTaskReminders(context, task.id)
            repository.deleteTask(task)
            _userFeedbackMessage.value = "Task and reminders removed."
        }
    }

    // === HABIT OPERATIONS ===
    fun toggleHabit(habitId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            val isDone = todayHabitLogs.value.any { it.habitId == habitId && it.completed }
            repository.toggleHabitForDate(habitId, todayDateString, isDone)
            if (!isDone) {
                _userFeedbackMessage.value = MotivationVault.completionMessages.random()
            }
        }
    }

    fun addHabit(name: String, description: String, category: String, colorHex: String, iconKey: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val habit = HabitEntity(
                name = name.trim(),
                description = description.trim(),
                category = category,
                colorHex = colorHex,
                iconKey = iconKey,
                isSystemHabit = false
            )
            repository.insertHabit(habit)
            _userFeedbackMessage.value = "New habit added to daily tracking."
        }
    }

    fun deleteHabit(habitId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteHabitById(habitId)
        }
    }

    // === TIMETABLE OPERATIONS ===
    fun toggleTimetableSlot(slot: TimetableSlotEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.toggleTimetableSlotCompletion(slot)
            val wasDone = (slot.isCompletedToday && slot.lastCompletedDate == todayDateString)
            if (!wasDone) {
                _userFeedbackMessage.value = MotivationVault.completionMessages.random()
            }
        }
    }

    fun updateTimetableSlot(slot: TimetableSlotEntity, newTitle: String, newStart: String, newEnd: String, newNote: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val updated = slot.copy(
                title = newTitle.trim(),
                startTime = newStart.trim(),
                endTime = newEnd.trim(),
                note = newNote.trim()
            )
            repository.updateTimetableSlot(updated)
            _userFeedbackMessage.value = "Timetable slot updated."
        }
    }

    fun addTimetableSlot(title: String, startTime: String, endTime: String, category: String, note: String, focusDuration: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val count = repository.getTimetableSlotsSync().size
            val slot = TimetableSlotEntity(
                orderIndex = count + 1,
                title = title.trim(),
                startTime = startTime.trim(),
                endTime = endTime.trim(),
                category = category,
                note = note.trim(),
                focusDurationMinutes = focusDuration
            )
            repository.insertTimetableSlot(slot)
            _userFeedbackMessage.value = "Slot added to daily timetable."
        }
    }

    fun deleteTimetableSlot(slotId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteTimetableSlotById(slotId)
        }
    }

    // === WEEKLY REVIEW ===
    fun saveWeeklyReview(weekKey: String, startDate: String, endDate: String, improvements: String, goal1: String, goal2: String, goal3: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val scoreAvg = liveScore.value
            val tasks = allTasks.value
            val review = WeeklyReviewEntity(
                weekKey = weekKey,
                startDate = startDate,
                endDate = endDate,
                averageScore = scoreAvg,
                completedTasks = tasks.count { it.isCompleted },
                missedTasks = tasks.count { !it.isCompleted && it.dueDateMillis < System.currentTimeMillis() },
                improvements = improvements.trim(),
                goal1 = goal1.trim(),
                goal2 = goal2.trim(),
                goal3 = goal3.trim(),
                isSubmitted = true
            )
            repository.saveWeeklyReview(review)
            _userFeedbackMessage.value = "Weekly review recorded. Next week goals set!"
        }
    }

    // === SETTINGS & NOTIFICATIONS CONFIG ===
    fun updateNotificationSettings(
        enabled: Boolean,
        sound: Boolean,
        vibration: Boolean,
        soundType: String,
        morningTime: String,
        nightTime: String,
        remind10Min: Boolean,
        gentleOverdue: Boolean
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val current = repository.getUserSettingsSync() ?: UserSettingsEntity()
            val updated = current.copy(
                notificationsEnabled = enabled,
                soundEnabled = sound,
                vibrationEnabled = vibration,
                notificationSoundType = soundType,
                morningMotivationTime = morningTime,
                nightReviewTime = nightTime,
                remind10MinBefore = remind10Min,
                gentleMissedReminder = gentleOverdue
            )
            repository.updateUserSettings(updated)

            // Re-create channels to apply sound and vibration
            NotificationHelper.createNotificationChannels(
                context = context,
                soundEnabled = sound,
                vibrationEnabled = vibration,
                soundType = soundType
            )

            if (enabled) {
                NotificationScheduler.scheduleDailyMorningWakeup(context, morningTime)
                NotificationScheduler.scheduleDailyNightReview(context, nightTime)
            }
            _userFeedbackMessage.value = "Notification settings saved & channels updated."
        }
    }

    // === INSTANT TEST NOTIFICATION ACTIONS ===
    fun testNotification(type: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val settings = repository.getUserSettingsSync()
            val sound = settings?.soundEnabled ?: true
            val vib = settings?.vibrationEnabled ?: true
            val soundType = settings?.notificationSoundType ?: "DEFAULT"

            when (type) {
                "WAKEUP" -> {
                    NotificationHelper.showNotification(
                        context = context,
                        notificationId = 8001,
                        channelId = NotificationHelper.CHANNEL_WAKEUP,
                        title = MotivationVault.getDailyWakeupMessage(),
                        message = "Stop delaying. Start now! Your goals won't achieve themselves.",
                        subText = "5:00 AM IRON WILL",
                        soundEnabled = sound,
                        vibrationEnabled = vib,
                        soundType = soundType
                    )
                }
                "SCHEDULED_TASK" -> {
                    val punchline = MotivationVault.getTaskExecutionMessage()
                    NotificationHelper.showNotification(
                        context = context,
                        notificationId = 8002,
                        channelId = NotificationHelper.CHANNEL_TASKS,
                        title = "🚨 [HIGH PRIORITY] Trading: Support & Resistance",
                        message = "$punchline Scheduled for 8:00 PM. Don't wait for motivation. Take action!",
                        subText = "TASK EXECUTION",
                        soundEnabled = sound,
                        vibrationEnabled = vib,
                        soundType = soundType
                    )
                }
                "PRE_REMINDER" -> {
                    val prePunchline = MotivationVault.getPreReminderMessage()
                    NotificationHelper.showNotification(
                        context = context,
                        notificationId = 8003,
                        channelId = NotificationHelper.CHANNEL_UPCOMING,
                        title = "Upcoming: Gym Push Session (10 Minutes)",
                        message = "$prePunchline Starts in 10 minutes at 6:00 PM. Small actions create big results.",
                        subText = "10-MIN WARNING",
                        soundEnabled = sound,
                        vibrationEnabled = vib,
                        soundType = soundType
                    )
                }
                "PENDING_TASK" -> {
                    val incompletePunchline = MotivationVault.getIncompleteTaskMessage()
                    NotificationHelper.showNotification(
                        context = context,
                        notificationId = 8004,
                        channelId = NotificationHelper.CHANNEL_PENDING,
                        title = "Pending: Physics & Calculus Review",
                        message = "$incompletePunchline Your goals won't achieve themselves.",
                        subText = "PENDING TASK",
                        soundEnabled = sound,
                        vibrationEnabled = vib,
                        soundType = soundType
                    )
                }
                "NIGHT_REVIEW" -> {
                    val nightPunchline = MotivationVault.getNightReviewMessage()
                    NotificationHelper.showNotification(
                        context = context,
                        notificationId = 8005,
                        channelId = NotificationHelper.CHANNEL_REVIEW,
                        title = "Daily Performance Review 🌙",
                        message = "$nightPunchline Finish today's mission and lock in tomorrow's wins.",
                        subText = "9:00 PM ACCOUNTABILITY",
                        soundEnabled = sound,
                        vibrationEnabled = vib,
                        soundType = soundType
                    )
                }
            }
            _userFeedbackMessage.value = "Test notification sent with real sound & vibration."
        }
    }

    suspend fun exportDataJson(): String = repository.exportDataJson()
}
