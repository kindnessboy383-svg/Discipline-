package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "discipline_days")
data class DisciplineDayEntity(
    @PrimaryKey
    val dateString: String, // "yyyy-MM-dd"
    val score: Int = 0, // 0 - 100
    val tasksCompleted: Int = 0,
    val tasksTotal: Int = 0,
    val habitsCompleted: Int = 0,
    val habitsTotal: Int = 0,
    val timetableCompleted: Int = 0,
    val timetableTotal: Int = 0,
    val studyCompleted: Boolean = false,
    val gymCompleted: Boolean = false,
    val readingCompleted: Boolean = false,
    val tradingCompleted: Boolean = false,
    val notes: String = ""
)

@Entity(tableName = "weekly_reviews")
data class WeeklyReviewEntity(
    @PrimaryKey
    val weekKey: String, // e.g. "2026-W45"
    val startDate: String, // "yyyy-MM-dd"
    val endDate: String, // "yyyy-MM-dd"
    val averageScore: Int = 0,
    val completedTasks: Int = 0,
    val missedTasks: Int = 0,
    val improvements: String = "",
    val goal1: String = "",
    val goal2: String = "",
    val goal3: String = "",
    val isSubmitted: Boolean = false,
    val updatedAtMillis: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_settings")
data class UserSettingsEntity(
    @PrimaryKey
    val id: Int = 1,
    val notificationsEnabled: Boolean = true,
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val notificationSoundType: String = "DEFAULT", // "DEFAULT", "ALARM", "RINGTONE"
    val morningMotivationTime: String = "05:00",
    val nightReviewTime: String = "21:00",
    val remind10MinBefore: Boolean = true,
    val gentleMissedReminder: Boolean = true,
    val currentStreak: Int = 1,
    val bestStreak: Int = 1,
    val lastActiveDate: String = ""
)
