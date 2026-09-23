package com.dss.workstation.ui.staff

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dss.workstation.data.model.DailyScheduleWithTemplate
import com.dss.workstation.data.model.ScheduleStatus
import com.dss.workstation.ui.theme.*

@Composable
fun TaskRecoveryScreen(
    scheduleList: List<DailyScheduleWithTemplate>,
    onResetToStep1: (String) -> Unit,
    onJumpToStep: (scheduleId: String, stepIndex: Int) -> Unit,
    onForceComplete: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var jumpTargetSchedule by remember { mutableStateOf<DailyScheduleWithTemplate?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Column {
            Text(
                text = "TASK RECOVERY & WORKSTATION OVERRIDE",
                style = DssTypography.headlineLarge.copy(
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = DssTextPrimary
                )
            )
            Text(
                text = "Intervene and recover tasks if a student is stuck or in an invalid state",
                style = DssTypography.bodyMedium.copy(fontSize = 18.sp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (scheduleList.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("No active shift tasks to manage.", style = DssTypography.bodyLarge)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(scheduleList) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(2.dp, Color(0xFFCBD5E1))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.templateWithDetails.template.title,
                                    style = DssTypography.titleLarge.copy(
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    val statusColor = when (item.schedule.status) {
                                        ScheduleStatus.IN_PROGRESS -> DssPrimary
                                        ScheduleStatus.COMPLETED -> DssSuccess
                                        ScheduleStatus.CHECKING -> DssWarning
                                        else -> DssTextSecondary
                                    }

                                    Surface(
                                        color = statusColor.copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = item.schedule.status.name,
                                            color = statusColor,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                        )
                                    }

                                    val totalSteps = item.templateWithDetails.sortedSteps.size
                                    Text(
                                        text = "Current Step: ${item.schedule.currentStepIndex + 1} of $totalSteps",
                                        style = DssTypography.bodyMedium.copy(fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                                    )
                                }
                            }

                            // Recovery Buttons
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Button(
                                    onClick = { onResetToStep1(item.schedule.id) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEFF6FF), contentColor = DssPrimary),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("RESET TO STEP 1", fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = { jumpTargetSchedule = item },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF1F5F9), contentColor = DssTextPrimary),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("JUMP TO STEP...", fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = { onForceComplete(item.schedule.id) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDCFCE7), contentColor = Color(0xFF166534)),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("FORCE COMPLETE", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Step selection dialog
    jumpTargetSchedule?.let { target ->
        AlertDialog(
            onDismissRequest = { jumpTargetSchedule = null },
            title = {
                Text("Select Step for: ${target.templateWithDetails.template.title}", style = DssTypography.titleMedium)
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    target.templateWithDetails.sortedSteps.forEachIndexed { index, step ->
                        Button(
                            onClick = {
                                onJumpToStep(target.schedule.id, index)
                                jumpTargetSchedule = null
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF8FAFC), contentColor = DssTextPrimary),
                            border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "Step ${index + 1}: ${step.instruction}",
                                maxLines = 1,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { jumpTargetSchedule = null }) {
                    Text("CANCEL", fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}
