package com.example.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.data.model.TaskEntity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object NotificationScheduler {

    // Request code offsets
    private const val OFFSET_EXACT_TIME = 1
    private const val OFFSET_PRE_REMINDER = 2
    private const val OFFSET_MISSED_TASK = 3

    private const val REQ_CODE_MORNING_WAKEUP = 1001
    private const val REQ_CODE_NIGHT_REVIEW = 1002

    fun scheduleTaskReminders(context: Context, task: TaskEntity) {
        if (task.isCompleted) {
            cancelTaskReminders(context, task.id)
            return
        }

        val now = System.currentTimeMillis()
        val timeFormat = SimpleDateFormat("h:mm a", Locale.US)
        val timeString = timeFormat.format(Date(task.dueDateMillis))

        // 1. Exact Scheduled Task Time Alarm
        if (task.dueDateMillis > now) {
            val exactIntent = Intent(context, AlarmReceiver::class.java).apply {
                action = AlarmReceiver.ACTION_TASK_EXACT_TIME
                putExtra(AlarmReceiver.EXTRA_TASK_ID, task.id)
                putExtra(AlarmReceiver.EXTRA_TASK_TITLE, task.title)
                putExtra(AlarmReceiver.EXTRA_TASK_TIME_STRING, timeString)
                putExtra(AlarmReceiver.EXTRA_TASK_PRIORITY, task.priority)
            }
            scheduleAlarm(
                context = context,
                triggerTimeMillis = task.dueDateMillis,
                requestCode = (task.id * 10 + OFFSET_EXACT_TIME).toInt(),
                intent = exactIntent
            )
        }

        // 2. 10 Minutes Before Important Task Reminder
        if (task.reminderMinutesBefore > 0) {
            val preReminderTime = task.dueDateMillis - (task.reminderMinutesBefore * 60 * 1000L)
            if (preReminderTime > now) {
                val preIntent = Intent(context, AlarmReceiver::class.java).apply {
                    action = AlarmReceiver.ACTION_TASK_PRE_REMINDER
                    putExtra(AlarmReceiver.EXTRA_TASK_ID, task.id)
                    putExtra(AlarmReceiver.EXTRA_TASK_TITLE, task.title)
                    putExtra(AlarmReceiver.EXTRA_TASK_TIME_STRING, timeString)
                    putExtra(AlarmReceiver.EXTRA_TASK_PRIORITY, task.priority)
                }
                scheduleAlarm(
                    context = context,
                    triggerTimeMillis = preReminderTime,
                    requestCode = (task.id * 10 + OFFSET_PRE_REMINDER).toInt(),
                    intent = preIntent
                )
            }
        }

        // 3. Incomplete Task Reminder (20 minutes after deadline if task remains incomplete)
        val incompleteReminderTime = task.dueDateMillis + (20 * 60 * 1000L)
        if (incompleteReminderTime > now) {
            val missedIntent = Intent(context, AlarmReceiver::class.java).apply {
                action = AlarmReceiver.ACTION_MISSED_TASK_REMINDER
                putExtra(AlarmReceiver.EXTRA_TASK_ID, task.id)
                putExtra(AlarmReceiver.EXTRA_TASK_TITLE, task.title)
            }
            scheduleAlarm(
                context = context,
                triggerTimeMillis = incompleteReminderTime,
                requestCode = (task.id * 10 + OFFSET_MISSED_TASK).toInt(),
                intent = missedIntent
            )
        }
    }

    fun cancelTaskReminders(context: Context, taskId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        listOf(OFFSET_EXACT_TIME, OFFSET_PRE_REMINDER, OFFSET_MISSED_TASK).forEach { offset ->
            val intent = Intent(context, AlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                (taskId * 10 + offset).toInt(),
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent)
                pendingIntent.cancel()
            }
        }
    }

    // Schedule 5:00 AM daily wake up
    fun scheduleDailyMorningWakeup(context: Context, timeString: String = "05:00") {
        scheduleDailyAlarm(
            context = context,
            requestCode = REQ_CODE_MORNING_WAKEUP,
            action = AlarmReceiver.ACTION_MORNING_WAKEUP,
            timeString = timeString
        )
    }

    // Schedule 9:00 PM daily performance review
    fun scheduleDailyNightReview(context: Context, timeString: String = "21:00") {
        scheduleDailyAlarm(
            context = context,
            requestCode = REQ_CODE_NIGHT_REVIEW,
            action = AlarmReceiver.ACTION_NIGHT_REVIEW,
            timeString = timeString
        )
    }

    private fun scheduleDailyAlarm(context: Context, requestCode: Int, action: String, timeString: String) {
        val parts = timeString.split(":")
        val hour = parts.getOrNull(0)?.toIntOrNull() ?: 5
        val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            this.action = action
        }

        scheduleAlarm(
            context = context,
            triggerTimeMillis = calendar.timeInMillis,
            requestCode = requestCode,
            intent = intent
        )
    }

    private fun scheduleAlarm(context: Context, triggerTimeMillis: Long, requestCode: Int, intent: Intent) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTimeMillis, pendingIntent)
                } else {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTimeMillis, pendingIntent)
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTimeMillis, pendingIntent)
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTimeMillis, pendingIntent)
            }
        } catch (_: SecurityException) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTimeMillis, pendingIntent)
        } catch (_: Exception) {}
    }
}
