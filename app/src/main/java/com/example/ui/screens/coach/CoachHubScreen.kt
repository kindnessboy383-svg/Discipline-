package com.example.ui.screens.coach

import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DailyChallenge
import com.example.data.model.MotivationVault
import com.example.data.model.UserSettingsEntity
import com.example.data.model.WeeklyReviewEntity
import com.example.ui.theme.*

enum class CoachTab(val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    MOTIVATION("Motivation", Icons.Default.Bolt),
    WEEKLY_REVIEW("Sunday Review", Icons.Default.EventRepeat),
    SETTINGS("Settings", Icons.Default.Tune)
}

@Composable
fun CoachHubScreen(
    userSettings: UserSettingsEntity?,
    weeklyReviews: List<WeeklyReviewEntity>,
    onSaveWeeklyReview: (String, String, String, String, String, String, String) -> Unit,
    onUpdateSettings: (Boolean, Boolean, Boolean, String, String, String, Boolean, Boolean) -> Unit,
    onRequestNotificationPermission: () -> Unit,
    hasNotificationPermission: Boolean,
    onTestNotification: (String) -> Unit,
    onExportData: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(CoachTab.MOTIVATION) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Coach Hub Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "DISCIPLINE COACH",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                )
                Text(
                    text = "Mindset, Sunday reflections, and real notification controls.",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Tab Selector Row
        TabRow(
            selectedTabIndex = selectedTab.ordinal,
            containerColor = DarkSurface,
            contentColor = NeonLavender,
            divider = { HorizontalDivider(color = DarkBorder) },
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab.ordinal]),
                    color = NeonLavender
                )
            }
        ) {
            CoachTab.values().forEach { tab ->
                val isSelected = tab == selectedTab
                Tab(
                    selected = isSelected,
                    onClick = { selectedTab = tab },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(tab.icon, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(tab.title, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, fontSize = 12.sp)
                        }
                    },
                    selectedContentColor = NeonLavender,
                    unselectedContentColor = TextSecondary,
                    modifier = Modifier.testTag("tab_${tab.name}")
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        when (selectedTab) {
            CoachTab.MOTIVATION -> MotivationTabContent()
            CoachTab.WEEKLY_REVIEW -> WeeklyReviewTabContent(
                weeklyReviews = weeklyReviews,
                onSave = onSaveWeeklyReview
            )
            CoachTab.SETTINGS -> SettingsTabContent(
                settings = userSettings ?: UserSettingsEntity(),
                hasNotificationPermission = hasNotificationPermission,
                onRequestPermission = onRequestNotificationPermission,
                onUpdate = onUpdateSettings,
                onTestNotification = onTestNotification,
                onExport = onExportData
            )
        }
    }
}

@Composable
fun MotivationTabContent() {
    var selectedCategory by remember { mutableStateOf("All") }
    val categories = listOf("All", "Discipline", "Focus", "Stoic", "Resilience")

    val completedChallenges = remember { mutableStateMapOf<String, Boolean>() }

    val filteredQuotes = remember(selectedCategory) {
        if (selectedCategory == "All") MotivationVault.quotes
        else MotivationVault.quotes.filter { it.category == selectedCategory }
    }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        // Supportive Reset Banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth().testTag("supportive_reset_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                border = androidx.compose.foundation.BorderStroke(1.dp, NeonLavender.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Healing, contentDescription = null, tint = NeonLavender, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("RESET PROTOCOL", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = NeonLavender))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "\"One bad day does not define you. Start again.\"",
                        style = MaterialTheme.typography.titleMedium.copy(fontStyle = FontStyle.Italic, fontWeight = FontWeight.Bold, color = TextPrimary)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Discipline is not about being flawless; it is about refusing to let one setback destroy your momentum.",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                    )
                }
            }
        }

        // Daily Discipline Challenges
        item {
            Text(
                text = "DAILY DISCIPLINE CHALLENGES",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
            )
        }

        items(MotivationVault.dailyChallenges) { challenge ->
            val isChecked = completedChallenges[challenge.id] == true
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { completedChallenges[challenge.id] = !isChecked },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, if (isChecked) PriorityLow.copy(alpha = 0.5f) else DarkBorder)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isChecked,
                        onCheckedChange = { completedChallenges[challenge.id] = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = PriorityLow,
                            uncheckedColor = DarkBorder,
                            checkmarkColor = Color.Black
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = challenge.title,
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (isChecked) PriorityLow else TextPrimary
                            )
                        )
                        Text(
                            text = challenge.description,
                            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, fontSize = 11.sp)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(TechCyan.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = challenge.difficulty,
                            style = MaterialTheme.typography.labelSmall.copy(color = TechCyan, fontWeight = FontWeight.Bold, fontSize = 9.sp)
                        )
                    }
                }
            }
        }

        // Quote Vault Header
        item {
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "QUOTE VAULT",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
                )
            }
        }

        items(filteredQuotes) { quote ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "\"${quote.quote}\"",
                        style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic, color = TextPrimary)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "— ${quote.author}", style = MaterialTheme.typography.labelSmall.copy(color = TechCyan, fontWeight = FontWeight.SemiBold))
                        Text(text = quote.category, style = MaterialTheme.typography.labelSmall.copy(color = NeonLavender))
                    }
                }
            }
        }
    }
}

@Composable
fun WeeklyReviewTabContent(
    weeklyReviews: List<WeeklyReviewEntity>,
    onSave: (String, String, String, String, String, String, String) -> Unit
) {
    var improvements by remember { mutableStateOf("") }
    var goal1 by remember { mutableStateOf("") }
    var goal2 by remember { mutableStateOf("") }
    var goal3 by remember { mutableStateOf("") }

    val currentWeekKey = "2026-W45"

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth().testTag("weekly_review_card"),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "SUNDAY ACCOUNTABILITY REVIEW",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
                    )
                    Text(
                        text = "Review your weekly execution and set 3 non-negotiables for next week.",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, fontSize = 11.sp)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = improvements,
                        onValueChange = { improvements = it },
                        label = { Text("Where did discipline slip? Key lesson learned:") },
                        modifier = Modifier.fillMaxWidth().testTag("review_improvements_input"),
                        minLines = 2
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("THREE NON-NEGOTIABLE GOALS FOR NEXT WEEK", style = MaterialTheme.typography.labelSmall.copy(color = NeonLavender, fontWeight = FontWeight.Bold))
                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = goal1,
                        onValueChange = { goal1 = it },
                        label = { Text("Goal 1 (e.g. 5 AM Wake Up 6/7 Days)") },
                        modifier = Modifier.fillMaxWidth().testTag("review_goal_1")
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = goal2,
                        onValueChange = { goal2 = it },
                        label = { Text("Goal 2 (e.g. Complete 5 Trading Sessions)") },
                        modifier = Modifier.fillMaxWidth().testTag("review_goal_2")
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = goal3,
                        onValueChange = { goal3 = it },
                        label = { Text("Goal 3 (e.g. Read 100 Pages & Hit Gym 5x)") },
                        modifier = Modifier.fillMaxWidth().testTag("review_goal_3")
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            onSave(currentWeekKey, "2026-11-01", "2026-11-07", improvements, goal1, goal2, goal3)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonLavender),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp).testTag("save_weekly_review_btn")
                    ) {
                        Text("Lock In Weekly Goals", color = Color(0xFF1E0A3C), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        if (weeklyReviews.isNotEmpty()) {
            item {
                Text(
                    text = "PAST WEEKLY REVIEWS",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
                )
            }
            items(weeklyReviews) { rev ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(text = "Week: ${rev.weekKey}", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = NeonLavender))
                        if (rev.improvements.isNotBlank()) {
                            Text(text = "Reflection: ${rev.improvements}", style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary))
                        }
                        if (rev.goal1.isNotBlank()) {
                            Text(text = "• ${rev.goal1}", style = MaterialTheme.typography.bodySmall.copy(color = TextPrimary))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsTabContent(
    settings: UserSettingsEntity,
    hasNotificationPermission: Boolean,
    onRequestPermission: () -> Unit,
    onUpdate: (Boolean, Boolean, Boolean, String, String, String, Boolean, Boolean) -> Unit,
    onTestNotification: (String) -> Unit,
    onExport: () -> Unit
) {
    val context = LocalContext.current
    var notificationsEnabled by remember(settings) { mutableStateOf(settings.notificationsEnabled) }
    var soundEnabled by remember(settings) { mutableStateOf(settings.soundEnabled) }
    var vibrationEnabled by remember(settings) { mutableStateOf(settings.vibrationEnabled) }
    var soundType by remember(settings) { mutableStateOf(settings.notificationSoundType) }
    var morningTime by remember(settings) { mutableStateOf(settings.morningMotivationTime) }
    var nightTime by remember(settings) { mutableStateOf(settings.nightReviewTime) }
    var remind10Min by remember(settings) { mutableStateOf(settings.remind10MinBefore) }
    var gentleMissed by remember(settings) { mutableStateOf(settings.gentleMissedReminder) }

    fun triggerUpdate() {
        onUpdate(
            notificationsEnabled,
            soundEnabled,
            vibrationEnabled,
            soundType,
            morningTime,
            nightTime,
            remind10Min,
            gentleMissed
        )
    }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        // 1. Android 13 Notification Permission Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth().testTag("notification_permission_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (hasNotificationPermission) PriorityLow.copy(alpha = 0.5f) else PriorityHigh.copy(alpha = 0.5f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (hasNotificationPermission) Icons.Default.NotificationsActive else Icons.Default.NotificationsOff,
                        contentDescription = null,
                        tint = if (hasNotificationPermission) PriorityLow else PriorityHigh,
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (hasNotificationPermission) "Real Android Notifications: Active" else "Notification Permission Required",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (hasNotificationPermission) PriorityLow else PriorityHigh
                            )
                        )
                        Text(
                            text = "Guarantees 5:00 AM wake up, scheduled task alerts, and 9:00 PM review fire even when the app is closed.",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, fontSize = 11.sp)
                        )
                    }
                    if (!hasNotificationPermission) {
                        Button(
                            onClick = onRequestPermission,
                            colors = ButtonDefaults.buttonColors(containerColor = PriorityHigh),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("request_permission_btn")
                        ) {
                            Text("Grant", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // 2. Sound, Vibration & Tone Selection
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "REAL NOTIFICATION SOUND & VIBRATION",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
                    )
                    Text(
                        text = "Custom hardware sound & haptic vibration controls",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, fontSize = 11.sp)
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    SettingToggleRow(
                        title = "Enable Notification Sound",
                        subtitle = "Play loud audible chime/alarm at task deadlines and 5:00 AM",
                        checked = soundEnabled,
                        onCheckedChange = {
                            soundEnabled = it
                            triggerUpdate()
                        }
                    )

                    SettingToggleRow(
                        title = "Enable Phone Vibration",
                        subtitle = "Vibrate with distinct double-pulse haptic pattern",
                        checked = vibrationEnabled,
                        onCheckedChange = {
                            vibrationEnabled = it
                            triggerUpdate()
                        }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Select Notification Sound Type",
                        style = MaterialTheme.typography.labelMedium.copy(color = NeonLavender, fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val soundOptions = listOf(
                            Triple("DEFAULT", "Standard Chime", Icons.Default.MusicNote),
                            Triple("ALARM", "Loud Alarm", Icons.Default.Alarm),
                            Triple("RINGTONE", "Phone Ringtone", Icons.Default.RingVolume)
                        )

                        soundOptions.forEach { (typeKey, label, icon) ->
                            val isSelected = soundType == typeKey
                            OutlinedCard(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        soundType = typeKey
                                        triggerUpdate()
                                    }
                                    .testTag("sound_type_$typeKey"),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.outlinedCardColors(
                                    containerColor = if (isSelected) NeonPurple.copy(alpha = 0.2f) else DarkSurfaceElevated
                                ),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) NeonLavender else DarkBorder
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        tint = if (isSelected) NeonLavender else TextSecondary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 10.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) TextPrimary else TextSecondary
                                        )
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    SettingToggleRow(
                        title = "10-Minute Pre-Task Warning",
                        subtitle = "Send an audible heads-up 10 minutes before important tasks",
                        checked = remind10Min,
                        onCheckedChange = {
                            remind10Min = it
                            triggerUpdate()
                        }
                    )

                    SettingToggleRow(
                        title = "Incomplete Task Follow-up",
                        subtitle = "Alert: 'You still have a task pending. Get it done!'",
                        checked = gentleMissed,
                        onCheckedChange = {
                            gentleMissed = it
                            triggerUpdate()
                        }
                    )
                }
            }
        }

        // 3. System Volume & Phone Settings Integration
        item {
            Card(
                modifier = Modifier.fillMaxWidth().testTag("system_sound_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "PHONE SETTINGS & VOLUME CONTROLS",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
                    )
                    Text(
                        text = "Adjust system reminder volumes, ringtones, and notification channel priorities directly in Android settings.",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, fontSize = 11.sp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                try {
                                    val intent = Intent(Settings.ACTION_SOUND_SETTINGS).apply {
                                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                    }
                                    context.startActivity(intent)
                                } catch (_: Exception) {}
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceElevated),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f).testTag("open_sound_settings_btn")
                        ) {
                            Icon(Icons.Default.VolumeUp, contentDescription = null, tint = TechCyan, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Volume Settings", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                try {
                                    val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                        Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                            putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                        }
                                    } else {
                                        Intent(Settings.ACTION_SETTINGS).apply {
                                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                        }
                                    }
                                    context.startActivity(intent)
                                } catch (_: Exception) {}
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceElevated),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f).testTag("open_channel_settings_btn")
                        ) {
                            Icon(Icons.Default.Tune, contentDescription = null, tint = NeonLavender, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Channel Settings", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // 4. Daily Schedule Times (5:00 AM & 9:00 PM)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "SCHEDULED RECURRING ALARM TIMES",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = morningTime,
                            onValueChange = {
                                morningTime = it
                                triggerUpdate()
                            },
                            label = { Text("5:00 AM Wake Up") },
                            modifier = Modifier.weight(1f).testTag("morning_time_input")
                        )
                        OutlinedTextField(
                            value = nightTime,
                            onValueChange = {
                                nightTime = it
                                triggerUpdate()
                            },
                            label = { Text("9:00 PM Review") },
                            modifier = Modifier.weight(1f).testTag("night_time_input")
                        )
                    }
                }
            }
        }

        // 5. Test Real Notifications on Android 13 Panel
        item {
            Card(
                modifier = Modifier.fillMaxWidth().testTag("test_notifications_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, NeonLavender.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.TouchApp, contentDescription = null, tint = NeonLavender, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "INSTANT NOTIFICATION TEST PANEL",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = NeonLavender)
                        )
                    }
                    Text(
                        text = "Test every real scheduled notification with actual sound & vibration now:",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, fontSize = 11.sp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        TestNotificationButton(
                            title = "1. Test 5:00 AM Wake Up Alarm",
                            subtitle = "\"WAKE UP! 🔥 Your goals are waiting.\"",
                            tag = "test_wakeup_btn",
                            onClick = { onTestNotification("WAKEUP") }
                        )

                        TestNotificationButton(
                            title = "2. Test Scheduled Task Alert",
                            subtitle = "Plays sound & shows task name with exact time",
                            tag = "test_task_btn",
                            onClick = { onTestNotification("SCHEDULED_TASK") }
                        )

                        TestNotificationButton(
                            title = "3. Test 10-Minute Pre-Task Warning",
                            subtitle = "Sends warning chime 10 minutes before deadline",
                            tag = "test_pre_btn",
                            onClick = { onTestNotification("PRE_REMINDER") }
                        )

                        TestNotificationButton(
                            title = "4. Test Incomplete Task Alert",
                            subtitle = "\"You still have a task pending. Get it done!\"",
                            tag = "test_pending_btn",
                            onClick = { onTestNotification("PENDING_TASK") }
                        )

                        TestNotificationButton(
                            title = "5. Test 9:00 PM Review Reminder",
                            subtitle = "\"Daily Performance Review 🌙\"",
                            tag = "test_review_btn",
                            onClick = { onTestNotification("NIGHT_REVIEW") }
                        )
                    }
                }
            }
        }

        // 6. Data Backup & Export
        item {
            Card(
                modifier = Modifier.fillMaxWidth().testTag("backup_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "DATA BACKUP & LOCAL PERSISTENCE",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
                    )
                    Text(
                        text = "All data is securely persisted on-device in Room Database. Export full JSON backup anytime.",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, fontSize = 11.sp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = onExport,
                        colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceElevated),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("export_data_btn")
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, tint = NeonLavender)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Export Local Backup (JSON)", color = TextPrimary, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun TestNotificationButton(
    title: String,
    subtitle: String,
    tag: String,
    onClick: () -> Unit
) {
    FilledTonalButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().testTag(tag),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.filledTonalButtonColors(containerColor = DarkSurfaceElevated),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimary))
                Text(text = subtitle, style = MaterialTheme.typography.bodySmall.copy(color = NeonLavender, fontSize = 11.sp))
            }
            Icon(Icons.Default.PlayArrow, contentDescription = "Test", tint = NeonLavender, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
fun SettingToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, color = TextPrimary))
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, fontSize = 11.sp))
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = NeonLavender,
                checkedTrackColor = DeepPurple,
                uncheckedTrackColor = DarkBorder
            )
        )
    }
}
