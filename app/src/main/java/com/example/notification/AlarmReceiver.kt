package com.example.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.local.DisciplineDatabase
import com.example.data.model.MotivationVault
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AlarmReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_MORNING_WAKEUP = "com.example.disciplinex.ACTION_MORNING_WAKEUP"
        const val ACTION_TASK_EXACT_TIME = "com.example.disciplinex.ACTION_TASK_EXACT_TIME"
        const val ACTION_TASK_PRE_REMINDER = "com.example.disciplinex.ACTION_TASK_PRE_REMINDER"
        const val ACTION_MISSED_TASK_REMINDER = "com.example.disciplinex.ACTION_MISSED_TASK_REMINDER"
        const val ACTION_NIGHT_REVIEW = "com.example.disciplinex.ACTION_NIGHT_REVIEW"

        const val EXTRA_TASK_ID = "extra_task_id"
        const val EXTRA_TASK_TITLE = "extra_task_title"
        const val EXTRA_TASK_TIME_STRING = "extra_task_time_string"
        const val EXTRA_TASK_PRIORITY = "extra_task_priority"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val pendingResult = goAsync()
        val scope = CoroutineScope(Dispatchers.IO)

        scope.launch {
            try {
                val db = DisciplineDatabase.getDatabase(context, scope)
                val dao = db.disciplineDao()
                val settings = dao.getUserSettingsSync()

                val notificationsEnabled = settings?.notificationsEnabled ?: true
                if (!notificationsEnabled) return@launch

                val soundEnabled = settings?.soundEnabled ?: true
                val vibrationEnabled = settings?.vibrationEnabled ?: true
                val soundType = settings?.notificationSoundType ?: "DEFAULT"

                when (action) {
                    // 1. Every day at 5:00 AM: Wake up notification with dynamic action motivation
                    ACTION_MORNING_WAKEUP -> {
                        val wakeupTitle = MotivationVault.getDailyWakeupMessage()
                        NotificationHelper.showNotification(
                            context = context,
                            notificationId = 1001,
                            channelId = NotificationHelper.CHANNEL_WAKEUP,
                            title = wakeupTitle,
                            message = "Stop delaying. Start now! Your goals won't achieve themselves.",
                            subText = "5:00 AM IRON WILL",
                            soundEnabled = soundEnabled,
                            vibrationEnabled = vibrationEnabled,
                            soundType = soundType
                        )
                        // Schedule next day's 5:00 AM alarm
                        NotificationScheduler.scheduleDailyMorningWakeup(
                            context,
                            settings?.morningMotivationTime ?: "05:00"
                        )
                    }

                    // 2. At every scheduled task time: Action-oriented short powerful message
                    ACTION_TASK_EXACT_TIME -> {
                        val taskId = intent.getLongExtra(EXTRA_TASK_ID, 0L)
                        val title = intent.getStringExtra(EXTRA_TASK_TITLE) ?: "Scheduled Task"
                        val timeStr = intent.getStringExtra(EXTRA_TASK_TIME_STRING) ?: run {
                            SimpleDateFormat("h:mm a", Locale.US).format(Date())
                        }
                        val priority = intent.getStringExtra(EXTRA_TASK_PRIORITY) ?: "HIGH"
                        val prefix = if (priority == "HIGH") "🚨 [HIGH PRIORITY] " else "⚡ "

                        // Verify task is not completed yet
                        val task = if (taskId > 0) dao.getTaskById(taskId) else null
                        if (task == null || !task.isCompleted) {
                            val actionPunchline = MotivationVault.getTaskExecutionMessage(taskId.toInt())
                            NotificationHelper.showNotification(
                                context = context,
                                notificationId = (20000 + taskId).toInt(),
                                channelId = NotificationHelper.CHANNEL_TASKS,
                                title = "$prefix$title",
                                message = "$actionPunchline Scheduled for $timeStr. Don't wait for motivation. Take action!",
                                subText = "TASK EXECUTION",
                                soundEnabled = soundEnabled,
                                vibrationEnabled = vibrationEnabled,
                                soundType = soundType
                            )
                        }
                    }

                    // 3. 10 minutes before important tasks: Action-oriented heads up
                    ACTION_TASK_PRE_REMINDER -> {
                        val taskId = intent.getLongExtra(EXTRA_TASK_ID, 0L)
                        val title = intent.getStringExtra(EXTRA_TASK_TITLE) ?: "Important Task"
                        val timeStr = intent.getStringExtra(EXTRA_TASK_TIME_STRING) ?: "soon"

                        val task = if (taskId > 0) dao.getTaskById(taskId) else null
                        if (task == null || !task.isCompleted) {
                            val preReminderPunchline = MotivationVault.getPreReminderMessage(taskId.toInt())
                            NotificationHelper.showNotification(
                                context = context,
                                notificationId = (30000 + taskId).toInt(),
                                channelId = NotificationHelper.CHANNEL_UPCOMING,
                                title = "Upcoming: $title (10 Minutes)",
                                message = "$preReminderPunchline Starts at $timeStr. Small actions create big results.",
                                subText = "10-MIN WARNING",
                                soundEnabled = soundEnabled,
                                vibrationEnabled = vibrationEnabled,
                                soundType = soundType
                            )
                        }
                    }

                    // 4. If a task is incomplete: Short, powerful urgency
                    ACTION_MISSED_TASK_REMINDER -> {
                        val taskId = intent.getLongExtra(EXTRA_TASK_ID, 0L)
                        val task = if (taskId > 0) dao.getTaskById(taskId) else null

                        // Only notify if the task is genuinely incomplete
                        if (task != null && !task.isCompleted) {
                            val incompletePunchline = MotivationVault.getIncompleteTaskMessage()
                            NotificationHelper.showNotification(
                                context = context,
                                notificationId = (40000 + taskId).toInt(),
                                channelId = NotificationHelper.CHANNEL_PENDING,
                                title = "Pending: ${task.title}",
                                message = "$incompletePunchline Your goals won't achieve themselves.",
                                subText = "PENDING TASK",
                                soundEnabled = soundEnabled,
                                vibrationEnabled = vibrationEnabled,
                                soundType = soundType
                            )
                        }
                    }

                    // 5. At 9:00 PM: Daily performance review reminder
                    ACTION_NIGHT_REVIEW -> {
                        val nightPunchline = MotivationVault.getNightReviewMessage()
                        NotificationHelper.showNotification(
                            context = context,
                            notificationId = 1002,
                            channelId = NotificationHelper.CHANNEL_REVIEW,
                            title = "Daily Performance Review 🌙",
                            message = "$nightPunchline Finish today's mission and lock in tomorrow's wins.",
                            subText = "9:00 PM ACCOUNTABILITY",
                            soundEnabled = soundEnabled,
                            vibrationEnabled = vibrationEnabled,
                            soundType = soundType
                        )
                        // Schedule next day's 9:00 PM alarm
                        NotificationScheduler.scheduleDailyNightReview(
                            context,
                            settings?.nightReviewTime ?: "21:00"
                        )
                    }
                }
            } catch (_: Exception) {
            } finally {
                pendingResult.finish()
            }
        }
    }
}
