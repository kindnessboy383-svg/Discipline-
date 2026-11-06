package com.example.ui.screens.timetable

import androidx.compose.animation.animateContentSize
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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TimetableSlotEntity
import com.example.ui.components.TimerDialog
import com.example.ui.theme.*

@Composable
fun TimetableScreen(
    slots: List<TimetableSlotEntity>,
    onToggleSlot: (TimetableSlotEntity) -> Unit,
    onUpdateSlot: (TimetableSlotEntity, String, String, String, String) -> Unit,
    onAddSlot: (String, String, String, String, String, Int) -> Unit,
    onDeleteSlot: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var slotToEdit by remember { mutableStateOf<TimetableSlotEntity?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    var activeTimerSlot by remember { mutableStateOf<TimetableSlotEntity?>(null) }

    val completedCount = slots.count { it.isCompletedToday }
    val totalCount = slots.size
    val progress = if (totalCount > 0) completedCount.toFloat() / totalCount.toFloat() else 0f

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp)
    ) {
        // Header Card with Timetable Summary
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("timetable_header_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "DAILY TIMETABLE",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            )
                            Text(
                                text = "Master your 24 hours. Iron adherence.",
                                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                            )
                        }

                        IconButton(
                            onClick = { showAddDialog = true },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(NeonPurple.copy(alpha = 0.2f))
                                .border(1.dp, NeonLavender, CircleShape)
                                .testTag("add_timetable_slot_btn")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add Slot", tint = NeonLavender)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Daily Routine Progress",
                            style = MaterialTheme.typography.labelMedium.copy(color = TextSecondary)
                        )
                        Text(
                            text = "$completedCount / $totalCount Slots Done",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = NeonLavender,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = NeonLavender,
                        trackColor = DarkBorder
                    )
                }
            }
        }

        // List of Timetable Slots
        items(slots) { slot ->
            TimetableSlotCard(
                slot = slot,
                onToggle = { onToggleSlot(slot) },
                onEdit = { slotToEdit = slot },
                onStartTimer = { activeTimerSlot = slot },
                onDelete = { onDeleteSlot(slot.id) }
            )
        }
    }

    // Edit Slot Dialog
    slotToEdit?.let { slot ->
        EditSlotDialog(
            slot = slot,
            onDismiss = { slotToEdit = null },
            onSave = { title, start, end, note ->
                onUpdateSlot(slot, title, start, end, note)
                slotToEdit = null
            }
        )
    }

    // Add Slot Dialog
    if (showAddDialog) {
        AddSlotDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { title, start, end, cat, note, focusMins ->
                onAddSlot(title, start, end, cat, note, focusMins)
                showAddDialog = false
            }
        )
    }

    // Focus Timer Dialog
    activeTimerSlot?.let { slot ->
        val minutes = if (slot.focusDurationMinutes > 0) slot.focusDurationMinutes else 30
        TimerDialog(
            initialMinutes = minutes,
            title = slot.title,
            subtitle = if (slot.note.isNotBlank()) slot.note else "${slot.startTime} to ${slot.endTime} focus session",
            onDismiss = { activeTimerSlot = null },
            onCompleted = {
                onToggleSlot(slot)
                activeTimerSlot = null
            }
        )
    }
}

@Composable
fun TimetableSlotCard(
    slot: TimetableSlotEntity,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onStartTimer: () -> Unit,
    onDelete: () -> Unit
) {
    val categoryColor = when (slot.category) {
        "Fitness" -> PriorityHigh
        "Trading" -> PriorityMedium
        "Study", "School" -> TechCyan
        "Reading" -> NeonLavender
        else -> TextSecondary
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .testTag("slot_card_${slot.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (slot.isCompletedToday) PriorityLow.copy(alpha = 0.5f) else DarkBorder
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = slot.isCompletedToday,
                    onCheckedChange = { onToggle() },
                    colors = CheckboxDefaults.colors(
                        checkedColor = PriorityLow,
                        uncheckedColor = DarkBorder,
                        checkmarkColor = Color.Black
                    )
                )

                Spacer(modifier = Modifier.width(6.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = slot.title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (slot.isCompletedToday) TextTertiary else TextPrimary
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${slot.startTime} - ${slot.endTime}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = NeonLavender
                            )
                        )
                        Text(
                            text = " • ${slot.category}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = categoryColor,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                }

                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(36.dp).testTag("edit_slot_${slot.id}")
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit Timing", tint = TextSecondary, modifier = Modifier.size(18.dp))
                }
            }

            if (slot.note.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = slot.note,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextSecondary,
                        fontSize = 12.sp
                    ),
                    modifier = Modifier.padding(start = 42.dp)
                )
            }

            // Quick launch focus timer for trading/reading/study blocks
            if (slot.focusDurationMinutes > 0) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 42.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onStartTimer,
                        modifier = Modifier
                            .height(38.dp)
                            .testTag("timer_btn_${slot.id}"),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonPurple.copy(alpha = 0.8f)),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.HourglassTop, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Start ${slot.focusDurationMinutes}m Focus",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = TextTertiary, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun EditSlotDialog(
    slot: TimetableSlotEntity,
    onDismiss: () -> Unit,
    onSave: (String, String, String, String) -> Unit
) {
    var title by remember { mutableStateOf(slot.title) }
    var startTime by remember { mutableStateOf(slot.startTime) }
    var endTime by remember { mutableStateOf(slot.endTime) }
    var note by remember { mutableStateOf(slot.note) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Timetable Timing", color = TextPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Slot Name") },
                    modifier = Modifier.fillMaxWidth().testTag("edit_slot_title")
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = startTime,
                        onValueChange = { startTime = it },
                        label = { Text("Start (HH:mm)") },
                        modifier = Modifier.weight(1f).testTag("edit_slot_start")
                    )
                    OutlinedTextField(
                        value = endTime,
                        onValueChange = { endTime = it },
                        label = { Text("End (HH:mm)") },
                        modifier = Modifier.weight(1f).testTag("edit_slot_end")
                    )
                }
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Notes / Protocol") },
                    modifier = Modifier.fillMaxWidth().testTag("edit_slot_note")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(title, startTime, endTime, note) },
                colors = ButtonDefaults.buttonColors(containerColor = NeonLavender),
                modifier = Modifier.testTag("save_slot_edit_btn")
            ) {
                Text("Save", color = Color(0xFF1E0A3C), fontWeight = FontWeight.Bold)
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
fun AddSlotDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String, String, String, String, Int) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var startTime by remember { mutableStateOf("12:00") }
    var endTime by remember { mutableStateOf("13:00") }
    var category by remember { mutableStateOf("Routine") }
    var note by remember { mutableStateOf("") }
    var focusMins by remember { mutableIntStateOf(30) }

    val categories = listOf("Routine", "Study", "Fitness", "Trading", "Reading", "Personal")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Timetable Slot", color = TextPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title (e.g. Trading Journal)") },
                    modifier = Modifier.fillMaxWidth().testTag("new_slot_title")
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = startTime,
                        onValueChange = { startTime = it },
                        label = { Text("Start Time") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = endTime,
                        onValueChange = { endTime = it },
                        label = { Text("End Time") },
                        modifier = Modifier.weight(1f)
                    )
                }
                // Category Selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categories.take(3).forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat, fontSize = 11.sp) }
                        )
                    }
                }
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Instructions") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onAdd(title, startTime, endTime, category, note, focusMins)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = NeonLavender),
                modifier = Modifier.testTag("confirm_add_slot_btn")
            ) {
                Text("Add Slot", color = Color(0xFF1E0A3C), fontWeight = FontWeight.Bold)
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
