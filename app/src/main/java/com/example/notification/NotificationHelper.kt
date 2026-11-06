package com.example.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity

object NotificationHelper {

    const val CHANNEL_WAKEUP = "discipline_wakeup_channel_v2"
    const val CHANNEL_TASKS = "discipline_tasks_channel_v2"
    const val CHANNEL_UPCOMING = "discipline_upcoming_channel_v2"
    const val CHANNEL_PENDING = "discipline_pending_channel_v2"
    const val CHANNEL_REVIEW = "discipline_review_channel_v2"

    fun getSoundUri(soundType: String): Uri {
        return when (soundType) {
            "ALARM" -> RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            "RINGTONE" -> RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            else -> RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        }
    }

    fun createNotificationChannels(
        context: Context,
        soundEnabled: Boolean = true,
        vibrationEnabled: Boolean = true,
        soundType: String = "DEFAULT"
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val soundUri = if (soundEnabled) getSoundUri(soundType) else null

            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_ALARM)
                .build()

            val vibrationPattern = if (vibrationEnabled) {
                longArrayOf(0, 500, 200, 500)
            } else {
                longArrayOf(0)
            }

            // 1. 5:00 AM Wake Up Alarm Channel
            val wakeupChannel = NotificationChannel(
                CHANNEL_WAKEUP,
                "5:00 AM Wake Up Call",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Daily 5:00 AM wake up call with high volume alarm sound and vibration"
                enableVibration(vibrationEnabled)
                if (vibrationEnabled) setVibrationPattern(vibrationPattern)
                if (soundEnabled && soundUri != null) {
                    setSound(soundUri, audioAttributes)
                } else {
                    setSound(null, null)
                }
            }

            // 2. Scheduled Task Channel (At exact task time)
            val taskChannel = NotificationChannel(
                CHANNEL_TASKS,
                "Scheduled Task Execution",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alerts triggered at exact task scheduled times"
                enableVibration(vibrationEnabled)
                if (vibrationEnabled) setVibrationPattern(vibrationPattern)
                if (soundEnabled && soundUri != null) {
                    setSound(soundUri, audioAttributes)
                } else {
                    setSound(null, null)
                }
            }

            // 3. 10 Minutes Before Important Tasks
            val upcomingChannel = NotificationChannel(
                CHANNEL_UPCOMING,
                "10-Minute Pre-Task Warning",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Reminds 10 minutes before important task deadlines"
                enableVibration(vibrationEnabled)
                if (vibrationEnabled) setVibrationPattern(vibrationPattern)
                if (soundEnabled && soundUri != null) {
                    setSound(soundUri, audioAttributes)
                } else {
                    setSound(null, null)
                }
            }

            // 4. Pending / Incomplete Task Follow-up
            val pendingChannel = NotificationChannel(
                CHANNEL_PENDING,
                "Incomplete Task Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alerts when a task remains pending after deadline"
                enableVibration(vibrationEnabled)
                if (vibrationEnabled) setVibrationPattern(vibrationPattern)
                if (soundEnabled && soundUri != null) {
                    setSound(soundUri, audioAttributes)
                } else {
                    setSound(null, null)
                }
            }

            // 5. 9:00 PM Daily Performance Review
            val reviewChannel = NotificationChannel(
                CHANNEL_REVIEW,
                "9:00 PM Performance Review",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Night review and daily discipline reflection"
                enableVibration(vibrationEnabled)
                if (vibrationEnabled) setVibrationPattern(vibrationPattern)
                if (soundEnabled && soundUri != null) {
                    setSound(soundUri, audioAttributes)
                } else {
                    setSound(null, null)
                }
            }

            notificationManager.createNotificationChannels(
                listOf(wakeupChannel, taskChannel, upcomingChannel, pendingChannel, reviewChannel)
            )
        }
    }

    fun showNotification(
        context: Context,
        notificationId: Int,
        channelId: String,
        title: String,
        message: String,
        subText: String? = "DISCIPLINE X",
        soundEnabled: Boolean = true,
        vibrationEnabled: Boolean = true,
        soundType: String = "DEFAULT"
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val soundUri = getSoundUri(soundType)

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setSubText(subText)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)

        if (soundEnabled) {
            builder.setSound(soundUri)
        } else {
            builder.setSilent(true)
        }

        if (vibrationEnabled) {
            builder.setVibrate(longArrayOf(0, 500, 200, 500))
        } else {
            builder.setVibrate(longArrayOf(0))
        }

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, builder.build())
    }
}
