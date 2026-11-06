package com.example.ui.screens.tasks

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SubTask
import com.example.data.model.SubTaskHelper
import com.example.data.model.TaskEntity
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

enum class TaskFilter(val title: String) {
    TODAY("Today"),
    PENDING("Pending"),
    OVERDUE("Overdue"),
    COMPLETED("Completed"),
    ALL("All")
}

@Composable
fun TaskManagerScreen(
    tasks: List<TaskEntity>,
    onToggleTask: (TaskEntity) -> Unit,
    onToggleSubtask: (TaskEntity, String) -> Unit,
    onAddTask: (String, String, String, String, Long, Int, List<SubTask>) -> Unit,
    onUpdateTask: (TaskEntity, List<SubTask>) -> Unit,
    onDeleteTask: (TaskEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf(TaskFilter.TODAY) }
    var showAddDialog by remember { mutableStateOf(false) }
    var taskToEdit by remember { mutableStateOf<TaskEntity?>(null) }

    val now = System.currentTimeMillis()
    val todayStart = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
    val todayEnd = todayStart + 86400000L

    val filteredTasks = remember(tasks, selectedFilter) {
        when (selectedFilter) {
            TaskFilter.TODAY -> tasks.filter { it.dueDateMillis in todayStart until todayEnd || (!it.isCompleted && it.dueDateMillis < todayEnd) }
            TaskFilter.PENDING -> tasks.filter { !it.isCompleted }
            TaskFilter.OVERDUE -> tasks.filter { !it.isCompleted && it.dueDateMillis < now }
            TaskFilter.COMPLETED -> tasks.filter { it.isCompleted }
            TaskFilter.ALL -> tasks
        }
    }

    val overdueCount = tasks.count { !it.isCompleted && it.dueDateMillis < now }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp)
    ) {
        // Top Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "SMART TASK MANAGER",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )
                    Text(
                        text = "${tasks.count { !it.isCompleted }} tasks remaining to execute",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                    )
                }

                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    containerColor = NeonLavender,
                    contentColor = Color(0xFF1E0A3C),
                    shape = CircleShape,
                    modifier = Modifier.size(48.dp).testTag("fab_add_task")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Task")
                }
            }
        }

        // Overdue gentle reminder banner if there are overdue tasks
        if (overdueCount > 0) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("overdue_banner"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                    border = androidx.compose.foundation.BorderStroke(1.dp, PriorityHigh.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(PriorityHigh.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.PriorityHigh, contentDescription = null, tint = PriorityHigh, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "$overdueCount Overdue Tasks",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = PriorityHigh
                                )
                            )
                            Text(
                                text = "One bad day does not define you. Start again right now.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                }
            }
        }

        // Filter Chips Row
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(TaskFilter.values()) { filter ->
                    val isSelected = filter == selectedFilter
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedFilter = filter },
                        label = {
                            Text(
                                text = filter.title,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = NeonPurple.copy(alpha = 0.25f),
                            selectedLabelColor = NeonLavender,
                            containerColor = DarkSurface
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = DarkBorder,
                            selectedBorderColor = NeonLavender
                        ),
                        modifier = Modifier.testTag("filter_${filter.name}")
                    )
                }
            }
        }

        if (filteredTasks.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(DarkSurface)
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.CheckCircleOutline,
                            contentDescription = null,
                            tint = NeonLavender.copy(alpha = 0.6f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No tasks found in this view",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        )
                        Text(
                            text = "Hit the + button to lock in your next mission.",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                        )
                    }
                }
            }
        } else {
            items(filteredTasks, key = { it.id }) { task ->
                TaskCard(
                    task = task,
                    onToggle = { onToggleTask(task) },
                    onToggleSubtask = { subtaskId -> onToggleSubtask(task, subtaskId) },
                    onEdit = { taskToEdit = task },
                    onDelete = { onDeleteTask(task) }
                )
            }
        }
    }

    if (showAddDialog) {
        AddTaskDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { title, desc, prio, cat, due, rem, subtasks ->
                onAddTask(title, desc, prio, cat, due, rem, subtasks)
                showAddDialog = false
            }
        )
    }

    taskToEdit?.let { task ->
        EditTaskDialog(
            task = task,
            onDismiss = { taskToEdit = null },
            onSave = { updatedTask, subtasks ->
                onUpdateTask(updatedTask, subtasks)
                taskToEdit = null
            }
        )
    }
}

@Composable
fun TaskCard(
    task: TaskEntity,
    onToggle: () -> Unit,
    onToggleSubtask: (String) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var expandedSubtasks by remember { mutableStateOf(false) }
    val subtasks = remember(task.subtasksJson) { SubTaskHelper.fromJson(task.subtasksJson) }
    val isOverdue = !task.isCompleted && task.dueDateMillis < System.currentTimeMillis()

    val priorityColor = when (task.priority) {
        "HIGH" -> PriorityHigh
        "MEDIUM" -> PriorityMedium
        else -> PriorityLow
    }

    val timeFormat = SimpleDateFormat("h:mm a, MMM d", Locale.US)
    val formattedDue = timeFormat.format(task.dueDateMillis)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .testTag("task_card_${task.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (task.isCompleted) PriorityLow.copy(alpha = 0.4f)
            else if (isOverdue) PriorityHigh.copy(alpha = 0.6f)
            else DarkBorder
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Checkbox(
                    checked = task.isCompleted,
                    onCheckedChange = { onToggle() },
                    colors = CheckboxDefaults.colors(
                        checkedColor = PriorityLow,
                        uncheckedColor = DarkBorder,
                        checkmarkColor = Color.Black
                    ),
                    modifier = Modifier.testTag("checkbox_task_${task.id}")
                )

                Spacer(modifier = Modifier.width(6.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Priority Badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(priorityColor.copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = task.priority,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = priorityColor,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                )
                            )
                        }

                        // Category Badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(DarkSurfaceElevated)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = task.category,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = TextSecondary,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 10.sp
                                )
                            )
                        }

                        if (isOverdue) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(PriorityHigh.copy(alpha = 0.2f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "OVERDUE",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = PriorityHigh,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 10.sp
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (task.isCompleted) TextTertiary else TextPrimary,
                            textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                        )
                    )

                    if (task.description.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = task.description,
                            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = "Deadline",
                            tint = if (isOverdue) PriorityHigh else TextTertiary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = formattedDue,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = if (isOverdue) PriorityHigh else TextSecondary,
                                fontSize = 11.sp
                            )
                        )
                        if (task.reminderMinutesBefore > 0) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = "Reminder",
                                tint = NeonLavender,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "${task.reminderMinutesBefore}m before",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = NeonLavender,
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }
                }

                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(36.dp).testTag("edit_task_${task.id}")
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = TextSecondary, modifier = Modifier.size(18.dp))
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(36.dp).testTag("delete_task_${task.id}")
                ) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = TextTertiary, modifier = Modifier.size(18.dp))
                }
            }

            // Subtasks Breakdown Section
            if (subtasks.isNotEmpty()) {
                val doneSubtasks = subtasks.count { it.isCompleted }
                val totalSubtasks = subtasks.size
                val subtaskProgress = if (totalSubtasks > 0) doneSubtasks.toFloat() / totalSubtasks.toFloat() else 0f

                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(DarkSurfaceElevated)
                        .clickable { expandedSubtasks = !expandedSubtasks }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (expandedSubtasks) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            tint = NeonLavender,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Subtasks ($doneSubtasks/$totalSubtasks)",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    LinearProgressIndicator(
                        progress = { subtaskProgress },
                        modifier = Modifier
                            .width(80.dp)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = NeonLavender,
                        trackColor = DarkBorder
                    )
                }

                AnimatedVisibility(visible = expandedSubtasks) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp, start = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        subtasks.forEach { sub ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onToggleSubtask(sub.id) }
                                    .padding(vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = sub.isCompleted,
                                    onCheckedChange = { onToggleSubtask(sub.id) },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = PriorityLow,
                                        uncheckedColor = DarkBorder,
                                        checkmarkColor = Color.Black
                                    ),
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = sub.text,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = if (sub.isCompleted) TextTertiary else TextPrimary,
                                        textDecoration = if (sub.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AddTaskDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String, String, String, Long, Int, List<SubTask>) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf("HIGH") }
    var category by remember { mutableStateOf("Study") }
    var reminderMinutes by remember { mutableIntStateOf(10) }
    var subtaskInput by remember { mutableStateOf("") }
    val subtasks = remember { mutableStateListOf<SubTask>() }

    val categories = listOf("Study", "Trading", "Gym", "School", "Reading", "Personal", "Routine")
    val priorities = listOf("HIGH", "MEDIUM", "LOW")

    // Default due date: today at 8:00 PM
    val defaultDue = remember {
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 20)
            set(Calendar.MINUTE, 0)
        }.timeInMillis
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create New Mission", color = TextPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task Title *") },
                    modifier = Modifier.fillMaxWidth().testTag("add_task_title_input")
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description / Protocol") },
                    modifier = Modifier.fillMaxWidth().testTag("add_task_desc_input")
                )

                Text("Priority Level", style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    priorities.forEach { p ->
                        FilterChip(
                            selected = priority == p,
                            onClick = { priority = p },
                            label = { Text(p, fontSize = 11.sp) }
                        )
                    }
                }

                Text("Category", style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(categories) { c ->
                        FilterChip(
                            selected = category == c,
                            onClick = { category = c },
                            label = { Text(c, fontSize = 11.sp) }
                        )
                    }
                }

                // Subtask input
                Text("Break Into Steps (Subtasks)", style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = subtaskInput,
                        onValueChange = { subtaskInput = it },
                        placeholder = { Text("e.g. Solve 5 problems", fontSize = 12.sp) },
                        modifier = Modifier.weight(1f).testTag("subtask_input")
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    IconButton(
                        onClick = {
                            if (subtaskInput.isNotBlank()) {
                                subtasks.add(SubTask(text = subtaskInput.trim()))
                                subtaskInput = ""
                            }
                        },
                        modifier = Modifier.testTag("add_subtask_btn")
                    ) {
                        Icon(Icons.Default.AddCircle, contentDescription = "Add Step", tint = NeonLavender)
                    }
                }

                subtasks.forEachIndexed { index, sub ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("• ${sub.text}", style = MaterialTheme.typography.bodySmall.copy(color = TextPrimary))
                        IconButton(onClick = { subtasks.removeAt(index) }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Close, contentDescription = null, tint = TextTertiary, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onAdd(title, description, priority, category, defaultDue, reminderMinutes, subtasks.toList())
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = NeonLavender),
                modifier = Modifier.testTag("save_task_btn")
            ) {
                Text("Lock In Mission", color = Color(0xFF1E0A3C), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        },
        containerColor = DarkSurface,
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
fun EditTaskDialog(
    task: TaskEntity,
    onDismiss: () -> Unit,
    onSave: (TaskEntity, List<SubTask>) -> Unit
) {
    var title by remember { mutableStateOf(task.title) }
    var description by remember { mutableStateOf(task.description) }
    var priority by remember { mutableStateOf(task.priority) }
    var category by remember { mutableStateOf(task.category) }
    val subtasks = remember { mutableStateListOf<SubTask>().apply { addAll(SubTaskHelper.fromJson(task.subtasksJson)) } }
    var subtaskInput by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Mission", color = TextPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth()
                )
                // Add Subtask
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = subtaskInput,
                        onValueChange = { subtaskInput = it },
                        placeholder = { Text("Add step...", fontSize = 12.sp) },
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = {
                            if (subtaskInput.isNotBlank()) {
                                subtasks.add(SubTask(text = subtaskInput.trim()))
                                subtaskInput = ""
                            }
                        }
                    ) {
                        Icon(Icons.Default.AddCircle, contentDescription = null, tint = NeonLavender)
                    }
                }
                subtasks.forEachIndexed { i, s ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(s.text, style = MaterialTheme.typography.bodySmall.copy(color = TextPrimary))
                        IconButton(onClick = { subtasks.removeAt(i) }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Close, contentDescription = null, tint = TextTertiary, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val updated = task.copy(
                        title = title.trim(),
                        description = description.trim(),
                        priority = priority,
                        category = category
                    )
                    onSave(updated, subtasks.toList())
                },
                colors = ButtonDefaults.buttonColors(containerColor = NeonLavender)
            ) {
                Text("Update", color = Color(0xFF1E0A3C), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = TextSecondary) }
        },
        containerColor = DarkSurface,
        shape = RoundedCornerShape(20.dp)
    )
}
