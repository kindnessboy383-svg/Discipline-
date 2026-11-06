package com.example.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.local.DisciplineDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == "android.intent.action.QUICKBOOT_POWERON" ||
            intent.action == "com.htc.intent.action.QUICKBOOT_POWERON"
        ) {
            val scope = CoroutineScope(Dispatchers.IO)
            scope.launch {
                val db = DisciplineDatabase.getDatabase(context, scope)
                val dao = db.disciplineDao()
                val settings = dao.getUserSettingsSync()

                if (settings?.notificationsEnabled != false) {
                    // Reschedule daily alarms
                    NotificationScheduler.scheduleDailyMorningWakeup(
                        context,
                        settings?.morningMotivationTime ?: "05:00"
                    )
                    NotificationScheduler.scheduleDailyNightReview(
                        context,
                        settings?.nightReviewTime ?: "21:00"
                    )

                    // Reschedule pending tasks
                    val tasks = dao.getAllTasks().firstOrNull() ?: emptyList()
                    val now = System.currentTimeMillis()
                    for (task in tasks) {
                        if (!task.isCompleted && task.dueDateMillis > now) {
                            NotificationScheduler.scheduleTaskReminders(context, task)
                        }
                    }
                }
            }
        }
    }
}
