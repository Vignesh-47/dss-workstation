package com.dss.workstation.ui.learner

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dss.workstation.R
import com.dss.workstation.data.model.DailyScheduleWithTemplate
import com.dss.workstation.data.model.ScheduleStatus
import com.dss.workstation.ui.components.DssBigButton
import com.dss.workstation.ui.components.DssButtonVariant
import com.dss.workstation.ui.theme.*

@Composable
fun TodayScreen(
    scheduleList: List<DailyScheduleWithTemplate>,
    activeScheduleId: String?,
    onSelectTask: (DailyScheduleWithTemplate) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DssBackground)
            .padding(horizontal = 36.dp, vertical = 24.dp)
    ) {
        // Page Headline
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "TODAY'S SHIFT SCHEDULE",
                    style = DssTypography.headlineLarge.copy(
                        fontSize = 38.sp,
                        fontWeight = FontWeight.Black,
                        color = DssTextPrimary
                    )
                )
                Text(
                    text = "Select your current assigned packing workstation task",
                    style = DssTypography.bodyMedium.copy(
                        fontSize = 22.sp,
                        color = DssTextSecondary
                    )
                )
            }

            // Summary pill
            val completedCount = scheduleList.count { it.schedule.status == ScheduleStatus.COMPLETED }
            Surface(
                color = Color(0xFFEFF6FF),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(2.dp, DssPrimary)
            ) {
                Text(
                    text = "$completedCount of ${scheduleList.size} Completed",
                    style = DssTypography.labelLarge.copy(
                        fontSize = 20.sp,
                        color = DssPrimary
                    ),
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Schedule items list
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            itemsIndexed(scheduleList) { index, item ->
                val isCompleted = item.schedule.status == ScheduleStatus.COMPLETED
                val isCurrentActive = item.schedule.status == ScheduleStatus.IN_PROGRESS ||
                        (activeScheduleId == item.schedule.id && !isCompleted)

                val cardBackground = when {
                    isCompleted -> Color(0xFFF0FDF4)
                    isCurrentActive -> Color(0xFFEFF6FF)
                    else -> Color.White
                }

                val borderColor = when {
                    isCompleted -> DssSuccess
                    isCurrentActive -> DssPrimary
                    else -> Color(0xFFCBD5E1)
                }

                val borderWidth = if (isCurrentActive) 4.dp else 2.dp

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = !isCompleted) {
                            onSelectTask(item)
                        },
                    shape = RoundedCornerShape(DssDimens.CardCornerRadius),
                    border = BorderStroke(borderWidth, borderColor),
                    colors = CardDefaults.cardColors(containerColor = cardBackground),
                    elevation = CardDefaults.cardElevation(defaultElevation = if (isCurrentActive) 6.dp else 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Left: Number Badge + Title & Description
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            // Queue Order Number Badge
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when {
                                            isCompleted -> DssSuccess
                                            isCurrentActive -> DssPrimary
                                            else -> Color(0xFFE2E8F0)
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isCompleted) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_check),
                                        contentDescription = "Completed",
                                        tint = Color.White,
                                        modifier = Modifier.size(40.dp)
                                    )
                                } else {
                                    Text(
                                        text = "${index + 1}",
                                        style = DssTypography.headlineLarge.copy(
                                            fontSize = 28.sp,
                                            fontWeight = FontWeight.Black,
                                            color = if (isCurrentActive) Color.White else DssTextPrimary
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(24.dp))

                            Column {
                                Text(
                                    text = item.templateWithDetails.template.title,
                                    style = DssTypography.titleLarge.copy(
                                        fontSize = 28.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isCompleted) Color(0xFF166534) else DssTextPrimary
                                    )
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${item.templateWithDetails.sortedSteps.size} steps • Est. ${item.templateWithDetails.template.estimatedMinutes} mins",
                                    style = DssTypography.bodyMedium.copy(
                                        fontSize = 20.sp,
                                        color = DssTextSecondary
                                    )
                                )
                            }
                        }

                        // Right: Action Button or Completed status
                        if (isCompleted) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .background(Color(0xFFDCFCE7), RoundedCornerShape(14.dp))
                                    .padding(horizontal = 20.dp, vertical = 12.dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_check),
                                    contentDescription = null,
                                    tint = DssSuccess,
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "COMPLETED",
                                    style = DssTypography.labelLarge.copy(
                                        fontSize = 20.sp,
                                        color = Color(0xFF166534)
                                    )
                                )
                            }
                        } else {
                            DssBigButton(
                                text = if (isCurrentActive) "START THIS TASK" else "SELECT TASK",
                                onClick = { onSelectTask(item) },
                                variant = if (isCurrentActive) DssButtonVariant.SUCCESS else DssButtonVariant.PRIMARY,
                                minHeight = 72.dp,
                                minWidth = 240.dp,
                                fontSize = 22.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
