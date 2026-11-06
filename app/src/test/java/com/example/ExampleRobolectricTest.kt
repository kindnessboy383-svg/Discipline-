package com.example

import android.app.NotificationManager
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.MotivationVault
import com.example.data.model.SubTask
import com.example.data.model.SubTaskHelper
import com.example.data.model.UserSettingsEntity
import com.example.notification.NotificationHelper
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Discipline X", appName)
    }

    @Test
    fun `subtask json serialization and deserialization`() {
        val subtasks = listOf(
            SubTask(id = "1", text = "Chart analysis", isCompleted = true),
            SubTask(id = "2", text = "Backtest 5 setups", isCompleted = false)
        )
        val json = SubTaskHelper.toJson(subtasks)
        val parsed = SubTaskHelper.fromJson(json)
        assertEquals(2, parsed.size)
        assertEquals("Chart analysis", parsed[0].text)
        assertTrue(parsed[0].isCompleted)
        assertEquals("Backtest 5 setups", parsed[1].text)
    }

    @Test
    fun `motivation vault contains quotes and challenges`() {
        assertTrue(MotivationVault.quotes.isNotEmpty())
        assertTrue(MotivationVault.dailyChallenges.isNotEmpty())
        assertTrue(MotivationVault.completionMessages.isNotEmpty())
        assertTrue(MotivationVault.supportiveResetMessages.isNotEmpty())
    }

    @Test
    fun `action oriented notification messages exist and rotate`() {
        // Verify key action-oriented phrases requested by user
        val executionMsgs = MotivationVault.taskExecutionMessages
        assertTrue(executionMsgs.contains("Stop delaying. Start now!"))
        assertTrue(executionMsgs.contains("Your goals won't achieve themselves."))
        assertTrue(executionMsgs.contains("Finish today's mission."))
        assertTrue(executionMsgs.contains("Small actions create big results."))
        assertTrue(executionMsgs.contains("Don't wait for motivation. Take action."))

        // Verify rotation helpers return valid strings
        val wakeupMsg = MotivationVault.getDailyWakeupMessage()
        val execMsg = MotivationVault.getTaskExecutionMessage()
        val preMsg = MotivationVault.getPreReminderMessage()
        val incompleteMsg = MotivationVault.getIncompleteTaskMessage()
        val reviewMsg = MotivationVault.getNightReviewMessage()

        assertNotNull(wakeupMsg)
        assertTrue(wakeupMsg.isNotBlank())
        assertNotNull(execMsg)
        assertTrue(execMsg.isNotBlank())
        assertNotNull(preMsg)
        assertTrue(preMsg.isNotBlank())
        assertNotNull(incompleteMsg)
        assertTrue(incompleteMsg.isNotBlank())
        assertNotNull(reviewMsg)
        assertTrue(reviewMsg.isNotBlank())
    }

    @Test
    fun `notification channels creation and sound uris`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        NotificationHelper.createNotificationChannels(
            context = context,
            soundEnabled = true,
            vibrationEnabled = true,
            soundType = "ALARM"
        )

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channels = notificationManager.notificationChannels
        assertNotNull(channels)
        assertTrue(channels.isNotEmpty())

        val wakeupChannel = notificationManager.getNotificationChannel(NotificationHelper.CHANNEL_WAKEUP)
        assertNotNull(wakeupChannel)
        assertEquals("5:00 AM Wake Up Call", wakeupChannel.name)

        val defaultSound = NotificationHelper.getSoundUri("DEFAULT")
        val alarmSound = NotificationHelper.getSoundUri("ALARM")
        val ringtoneSound = NotificationHelper.getSoundUri("RINGTONE")
        assertNotNull(defaultSound)
        assertNotNull(alarmSound)
        assertNotNull(ringtoneSound)
    }

    @Test
    fun `user settings notification defaults`() {
        val settings = UserSettingsEntity()
        assertEquals("05:00", settings.morningMotivationTime)
        assertEquals("21:00", settings.nightReviewTime)
        assertTrue(settings.notificationsEnabled)
        assertTrue(settings.soundEnabled)
        assertTrue(settings.vibrationEnabled)
        assertEquals("DEFAULT", settings.notificationSoundType)
    }
}
