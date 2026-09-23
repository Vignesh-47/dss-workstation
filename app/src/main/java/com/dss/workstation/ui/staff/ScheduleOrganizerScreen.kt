package com.dss.workstation.ui.staff

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dss.workstation.R
import com.dss.workstation.data.model.DailyScheduleWithTemplate
import com.dss.workstation.data.model.TaskTemplateWithDetails
import com.dss.workstation.ui.components.DssBigButton
import com.dss.workstation.ui.components.DssButtonVariant
import com.dss.workstation.ui.theme.*

@Composable
fun ScheduleOrganizerScreen(
    scheduleList: List<DailyScheduleWithTemplate>,
    allTemplates: List<TaskTemplateWithDetails>,
    onAddTemplate: (templateId: String, customTitle: String?) -> Unit,
    onRenameItem: (scheduleId: String, newTitle: String) -> Unit = { _, _ -> },
    onMoveUp: (Int) -> Unit,
    onMoveDown: (Int) -> Unit,
    onRemoveItem: (String) -> Unit,
    onClearSchedule: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedTemplateIndex by remember { mutableStateOf(0) }
    var customTaskName by remember { mutableStateOf("") }

    var itemToRename by remember { mutableStateOf<DailyScheduleWithTemplate?>(null) }
    var renameInput by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        // Top Toolbar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "SCHEDULE ORGANIZER",
                    style = DssTypography.headlineLarge.copy(
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = DssTextPrimary
                    )
                )
                Text(
                    text = "Configure today's packing workstation assignment queue",
                    style = DssTypography.bodyMedium.copy(fontSize = 18.sp)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                DssBigButton(
                    text = "CLEAR QUEUE",
                    onClick = onClearSchedule,
                    variant = DssButtonVariant.SECONDARY,
                    minHeight = 64.dp,
                    fontSize = 18.sp
                )

                DssBigButton(
                    text = "+ ADD TASK TO TODAY",
                    onClick = { showAddDialog = true },
                    variant = DssButtonVariant.PRIMARY,
                    minHeight = 64.dp,
                    iconResId = R.drawable.ic_add,
                    fontSize = 18.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Schedule list
        if (scheduleList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No tasks currently scheduled for today.\nTap '+ Add Task To Today' to assign work.",
                    style = DssTypography.bodyLarge.copy(
                        color = DssTextSecondary,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                itemsIndexed(scheduleList) { index, item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(2.dp, Color(0xFFCBD5E1))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                // Order Badge
                                Surface(
                                    color = DssPrimary,
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text(
                                        text = "#${index + 1}",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 20.sp,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(16.dp))

                                Column {
                                    Text(
                                        text = item.templateWithDetails.template.title,
                                        style = DssTypography.titleMedium.copy(
                                            fontSize = 22.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                    Text(
                                        text = "Status: ${item.schedule.status} • ${item.templateWithDetails.sortedSteps.size} steps",
                                        style = DssTypography.bodyMedium.copy(fontSize = 16.sp)
                                    )
                                }
                            }

                            // Reordering, Rename & Delete Actions
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = {
                                        itemToRename = item
                                        renameInput = item.templateWithDetails.template.title
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF1F5F9), contentColor = DssTextPrimary),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("RENAME", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                }

                                Button(
                                    onClick = { onMoveUp(index) },
                                    enabled = index > 0,
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEFF6FF), contentColor = DssPrimary),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("▲ UP", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                }

                                Button(
                                    onClick = { onMoveDown(index) },
                                    enabled = index < scheduleList.size - 1,
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEFF6FF), contentColor = DssPrimary),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("▼ DOWN", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                }

                                Button(
                                    onClick = { onRemoveItem(item.schedule.id) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFEE2E2), contentColor = DssDanger),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("REMOVE", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal to name and add a task to today's queue
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = {
                Text("Add Task to Today's Shift", style = DssTypography.headlineLarge.copy(fontSize = 24.sp, fontWeight = FontWeight.Bold))
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "TASK NAME / LABEL",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = DssTextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = customTaskName,
                        onValueChange = { customTaskName = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        placeholder = { Text("e.g. Pack Parcel #102, Gift Basket Order") }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "SELECT WORKFLOW TEMPLATE",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = DssTextPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().heightIn(max = 240.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(allTemplates.size) { idx ->
                            val template = allTemplates[idx]
                            val isSelected = selectedTemplateIndex == idx
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedTemplateIndex = idx
                                        if (customTaskName.isBlank() || allTemplates.any { it.template.title == customTaskName }) {
                                            customTaskName = template.template.title
                                        }
                                    },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) Color(0xFFEFF6FF) else Color(0xFFF8FAFC)
                                ),
                                border = BorderStroke(
                                    if (isSelected) 2.dp else 1.dp,
                                    if (isSelected) DssPrimary else Color(0xFFCBD5E1)
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = {
                                            selectedTemplateIndex = idx
                                            if (customTaskName.isBlank() || allTemplates.any { it.template.title == customTaskName }) {
                                                customTaskName = template.template.title
                                            }
                                        }
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = template.template.title,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp
                                        )
                                        Text(
                                            text = "${template.sortedSteps.size} steps • Est. ${template.template.estimatedMinutes} mins",
                                            fontSize = 13.sp,
                                            color = DssTextSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (allTemplates.isNotEmpty()) {
                            val template = allTemplates[selectedTemplateIndex.coerceIn(0, allTemplates.size - 1)]
                            onAddTemplate(template.template.id, customTaskName.trim().ifEmpty { null })
                        }
                        showAddDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DssSuccess)
                ) {
                    Text("+ ADD TO TODAY ✓", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("CANCEL", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // Modal to rename an existing scheduled task
    if (itemToRename != null) {
        AlertDialog(
            onDismissRequest = { itemToRename = null },
            title = {
                Text("Rename Scheduled Task", style = DssTypography.headlineLarge.copy(fontSize = 22.sp, fontWeight = FontWeight.Bold))
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Enter new name for this task in today's shift queue:", fontSize = 15.sp, color = DssTextSecondary)
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = renameInput,
                        onValueChange = { renameInput = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        itemToRename?.let {
                            onRenameItem(it.schedule.id, renameInput)
                        }
                        itemToRename = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DssPrimary)
                ) {
                    Text("SAVE NAME", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToRename = null }) {
                    Text("CANCEL", fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}
