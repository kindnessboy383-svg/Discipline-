package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.DisciplineViewModel
import com.example.ui.components.CustomBottomBar
import com.example.ui.components.NavDestination
import com.example.ui.screens.analytics.MonthlyAnalyticsScreen
import com.example.ui.screens.coach.CoachHubScreen
import com.example.ui.screens.dashboard.DashboardScreen
import com.example.ui.screens.habits.HabitTrackerScreen
import com.example.ui.screens.tasks.TaskManagerScreen
import com.example.ui.screens.timetable.TimetableScreen
import com.example.ui.theme.*
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainApp()
            }
        }
    }
}

@Composable
fun MainApp(viewModel: DisciplineViewModel = viewModel()) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var currentDestination by remember { mutableStateOf(NavDestination.DASHBOARD) }
    var exportJsonContent by remember { mutableStateOf<String?>(null) }

    // Check notification permission state
    var hasNotificationPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasNotificationPermission = isGranted
        if (isGranted) {
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Notifications enabled for daily discipline reminders.")
            }
        }
    }

    // Auto-prompt permission on first launch if not granted
    LaunchedEffect(Unit) {
        if (!hasNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    // BackHandler to navigate back to Dashboard
    BackHandler(enabled = currentDestination != NavDestination.DASHBOARD) {
        currentDestination = NavDestination.DASHBOARD
    }

    // Observe ViewModel state
    val score by viewModel.liveScore.collectAsStateWithLifecycle()
    val tasks by viewModel.allTasks.collectAsStateWithLifecycle()
    val habits by viewModel.allHabits.collectAsStateWithLifecycle()
    val todayLogs by viewModel.todayHabitLogs.collectAsStateWithLifecycle()
    val timetableSlots by viewModel.timetableSlots.collectAsStateWithLifecycle()
    val quote by viewModel.currentQuote.collectAsStateWithLifecycle()
    val userSettings by viewModel.userSettings.collectAsStateWithLifecycle()
    val monthlyDays by viewModel.monthlyDisciplineDays.collectAsStateWithLifecycle()
    val weeklyReviews by viewModel.allWeeklyReviews.collectAsStateWithLifecycle()
    val feedbackMessage by viewModel.userFeedbackMessage.collectAsStateWithLifecycle()

    // Show feedback messages
    LaunchedEffect(feedbackMessage) {
        feedbackMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearFeedbackMessage()
        }
    }

    val streakDays = userSettings?.currentStreak ?: 7

    Scaffold(
        modifier = Modifier.fillMaxSize().background(DarkBackground),
        containerColor = DarkBackground,
        contentWindowInsets = WindowInsets.safeDrawing,
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                snackbar = { data ->
                    Snackbar(
                        snackbarData = data,
                        containerColor = DarkSurfaceElevated,
                        contentColor = TextPrimary,
                        actionColor = NeonLavender,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .padding(16.dp)
                            .testTag("app_snackbar")
                    )
                }
            )
        },
        bottomBar = {
            CustomBottomBar(
                currentDestination = currentDestination,
                onNavigate = { currentDestination = it }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentDestination) {
                NavDestination.DASHBOARD -> DashboardScreen(
                    score = score,
                    streakDays = streakDays,
                    quote = quote,
                    onRefreshQuote = { viewModel.nextQuote() },
                    tasks = tasks,
                    timetableSlots = timetableSlots,
                    onToggleTask = { viewModel.toggleTask(it) },
                    onToggleSlot = { viewModel.toggleTimetableSlot(it) },
                    onNavigateToTasks = { currentDestination = NavDestination.TASKS },
                    onNavigateToTimetable = { currentDestination = NavDestination.TIMETABLE },
                    onNavigateToHabits = { currentDestination = NavDestination.HABITS }
                )

                NavDestination.TIMETABLE -> TimetableScreen(
                    slots = timetableSlots,
                    onToggleSlot = { viewModel.toggleTimetableSlot(it) },
                    onUpdateSlot = { slot, title, start, end, note ->
                        viewModel.updateTimetableSlot(slot, title, start, end, note)
                    },
                    onAddSlot = { title, start, end, cat, note, focusMins ->
                        viewModel.addTimetableSlot(title, start, end, cat, note, focusMins)
                    },
                    onDeleteSlot = { viewModel.deleteTimetableSlot(it) }
                )

                NavDestination.TASKS -> TaskManagerScreen(
                    tasks = tasks,
                    onToggleTask = { viewModel.toggleTask(it) },
                    onToggleSubtask = { task, subId -> viewModel.toggleSubtask(task, subId) },
                    onAddTask = { title, desc, prio, cat, due, rem, subtasks ->
                        viewModel.addTask(title, desc, prio, cat, due, rem, subtasks)
                    },
                    onUpdateTask = { task, subtasks ->
                        viewModel.updateTask(task, subtasks)
                    },
                    onDeleteTask = { viewModel.deleteTask(it) }
                )

                NavDestination.HABITS -> HabitTrackerScreen(
                    habits = habits,
                    todayLogs = todayLogs,
                    onToggleHabit = { viewModel.toggleHabit(it) },
                    onAddHabit = { name, desc, cat, color, icon ->
                        viewModel.addHabit(name, desc, cat, color, icon)
                    },
                    onDeleteHabit = { viewModel.deleteHabit(it) }
                )

                NavDestination.ANALYTICS -> MonthlyAnalyticsScreen(
                    monthlyDays = monthlyDays,
                    tasks = tasks
                )

                NavDestination.MORE -> CoachHubScreen(
                    userSettings = userSettings,
                    weeklyReviews = weeklyReviews,
                    onSaveWeeklyReview = { weekKey, start, end, imp, g1, g2, g3 ->
                        viewModel.saveWeeklyReview(weekKey, start, end, imp, g1, g2, g3)
                    },
                    onUpdateSettings = { enabled, sound, vib, soundType, morning, night, rem10, gentle ->
                        viewModel.updateNotificationSettings(enabled, sound, vib, soundType, morning, night, rem10, gentle)
                    },
                    onRequestNotificationPermission = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    },
                    hasNotificationPermission = hasNotificationPermission,
                    onTestNotification = { type ->
                        viewModel.testNotification(type)
                    },
                    onExportData = {
                        coroutineScope.launch {
                            val json = viewModel.exportDataJson()
                            exportJsonContent = json
                        }
                    }
                )
            }
        }
    }

    // Export Data JSON Viewer Dialog
    exportJsonContent?.let { json ->
        AlertDialog(
            onDismissRequest = { exportJsonContent = null },
            title = {
                Text("Discipline X Data Export", color = TextPrimary, fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    Text(
                        "Your complete routine and schedule backup in JSON format:",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 260.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(DarkSurfaceElevated)
                            .padding(12.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = json,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                color = TechCyan
                            )
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { exportJsonContent = null },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonLavender)
                ) {
                    Text("Done", color = Color(0xFF1E0A3C), fontWeight = FontWeight.Bold)
                }
            },
            containerColor = DarkSurface,
            shape = RoundedCornerShape(20.dp)
        )
    }
}
