package com.example.data.model

import java.util.Calendar

data class MotivationQuote(
    val id: Int,
    val quote: String,
    val author: String,
    val category: String // "Discipline", "Focus", "Stoic", "Resilience"
)

data class DailyChallenge(
    val id: String,
    val title: String,
    val description: String,
    val category: String,
    val difficulty: String // "Standard", "Beast", "Iron"
)

object MotivationVault {
    val quotes = listOf(
        MotivationQuote(1, "We must all suffer one of two things: the pain of discipline or the pain of regret.", "Jim Rohn", "Discipline"),
        MotivationQuote(2, "Discipline is choosing between what you want now, and what you want most.", "Abraham Lincoln", "Focus"),
        MotivationQuote(3, "At dawn, when you have trouble getting out of bed, tell yourself: 'I have to go to work — as a human being.'", "Marcus Aurelius", "Stoic"),
        MotivationQuote(4, "Don't count the days, make the days count.", "Muhammad Ali", "Resilience"),
        MotivationQuote(5, "Discipline equals freedom.", "Jocko Willink", "Discipline"),
        MotivationQuote(6, "The only bad workout is the one that didn't happen.", "Anonymous", "Fitness"),
        MotivationQuote(7, "You don't have to be extreme, just consistent.", "Anonymous", "Discipline"),
        MotivationQuote(8, "No citizen has a right to be an amateur in the matter of physical training.", "Socrates", "Fitness"),
        MotivationQuote(9, "Small disciplines repeated with consistency every day lead to great achievements.", "John C. Maxwell", "Discipline"),
        MotivationQuote(10, "He who has a why to live can bear almost any how.", "Friedrich Nietzsche", "Resilience"),
        MotivationQuote(11, "Risk comes from not knowing what you're doing. Master the charts, master yourself.", "Warren Buffett", "Trading"),
        MotivationQuote(12, "Read 500 pages like this every day. That's how knowledge works. It builds up, like compound interest.", "Warren Buffett", "Reading"),
        MotivationQuote(13, "First say to yourself what you would be; and then do what you have to do.", "Epictetus", "Stoic"),
        MotivationQuote(14, "Action is the foundational key to all success.", "Pablo Picasso", "Focus"),
        MotivationQuote(15, "Stay hard. When your mind tells you that you are done, you're really only 40 percent done.", "David Goggins", "Resilience")
    )

    // Action-oriented short powerful motivational messages for task start / execution
    val taskExecutionMessages = listOf(
        "Stop delaying. Start now!",
        "Your goals won't achieve themselves.",
        "Finish today's mission.",
        "Small actions create big results.",
        "Don't wait for motivation. Take action.",
        "Execution beats intention every time. Lock in.",
        "The clock is ticking. Deliver results.",
        "Discipline is doing it especially when you don't feel like it.",
        "Zero excuses. Attack this task right now.",
        "Win this hour. Win your day."
    )

    // Action-oriented pre-task warnings (10 minutes before)
    val taskPreReminderMessages = listOf(
        "Starts in 10 minutes. Clear your desk and eliminate distractions.",
        "10-minute warning: Preparation determines execution. Get ready.",
        "Don't wait for motivation. Take action in 10 minutes.",
        "Upcoming task: Small actions create big results. Prepare to lock in.",
        "10 minutes until execution. Prime your mind and start on time."
    )

    // Incomplete / pending task follow-up reminders
    val incompleteTaskMessages = listOf(
        "You still have a task pending. Get it done!",
        "Stop delaying. Start now! Finish what you started.",
        "Your goals won't achieve themselves. Eliminate this pending task.",
        "Small actions create big results. Don't leave today unfinished.",
        "Finish today's mission with zero regrets. Execute now."
    )

    // 5:00 AM Morning Wake Up calls (rotating daily)
    val morningWakeupMessages = listOf(
        "WAKE UP! 🔥 Your goals are waiting.",
        "5:00 AM Iron Will: Feet on the floor. While others sleep, you conquer.",
        "Stop delaying. Start now! Today's standard begins right here.",
        "Discipline equals freedom. Rise and attack your routine.",
        "Don't wait for motivation. Take action and win the morning."
    )

    // 9:00 PM Performance Review calls (rotating daily)
    val nightReviewMessages = listOf(
        "Finish today's mission. Review your score and plan tomorrow.",
        "Accountability time: Review your discipline score and celebrate wins.",
        "Scoreboard check: Measure today's progress and lock in tomorrow's 3 goals.",
        "Small actions create big results. Close out the day with honesty and purpose.",
        "Reflect, recharge, and set tomorrow's non-negotiable targets."
    )

    // Dynamic rotation helpers that ensure different messages each day
    fun getDailyWakeupMessage(): String {
        val day = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
        return morningWakeupMessages[day % morningWakeupMessages.size]
    }

    fun getTaskExecutionMessage(offset: Int = 0): String {
        val day = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val index = (day + hour + offset) % taskExecutionMessages.size
        return taskExecutionMessages[Math.abs(index)]
    }

    fun getPreReminderMessage(offset: Int = 0): String {
        val day = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val index = (day + hour + offset) % taskPreReminderMessages.size
        return taskPreReminderMessages[Math.abs(index)]
    }

    fun getIncompleteTaskMessage(): String {
        val day = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
        return incompleteTaskMessages[day % incompleteTaskMessages.size]
    }

    fun getNightReviewMessage(): String {
        val day = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
        return nightReviewMessages[day % nightReviewMessages.size]
    }

    val completionMessages = listOf(
        "Standard set. Standard met. Keep moving forward.",
        "Momentum locked in. Your future self thanks you.",
        "Execution over excuses. Clean victory.",
        "Another brick in your iron foundation.",
        "One task closer to absolute mastery.",
        "Discipline recorded. Consistency compounds.",
        "Relentless focus in action. Outstanding work."
    )

    val supportiveResetMessages = listOf(
        "One bad day does not define you. Start again.",
        "A stumble is not a defeat. Reset, breathe, and execute.",
        "The scoreboard resets at dawn. Forgive yourself and move forward.",
        "Failure is an event, never a person. Tomorrow is day one.",
        "Champions aren't made when everything goes right; they are forged in how they respond to friction.",
        "Put your head down. Pick up the routine right now."
    )

    val dailyChallenges = listOf(
        DailyChallenge("ch_wake", "5:00 AM Cold Start", "Wake up on the first alarm without hitting snooze.", "Discipline", "Standard"),
        DailyChallenge("ch_deep_study", "Zero-Phone Study Sprint", "Complete 50 minutes of focused school/study with phone in airplane mode.", "Focus", "Standard"),
        DailyChallenge("ch_gym_heavy", "Iron Therapy", "Push past perceived limits in the gym or workout block with zero skipped sets.", "Fitness", "Beast"),
        DailyChallenge("ch_trading_focus", "30-Min Chart Deep Dive", "Spend exactly 30 uninterrupted minutes studying risk management & setups.", "Trading", "Beast"),
        DailyChallenge("ch_reading", "15 Pages of Wisdom", "Read 15 pages of non-fiction/philosophy before turning off lights.", "Mindset", "Standard"),
        DailyChallenge("ch_water", "Hydration Discipline", "Drink 3 liters of water throughout the day.", "Health", "Standard")
    )
}
