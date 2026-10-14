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
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(DssBackground)
    ) {
        val isCompact = maxWidth < 700.dp

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = if (isCompact) 14.dp else 36.dp, vertical = if (isCompact) 14.dp else 24.dp)
        ) {
            // Page Headline & Summary Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = if (isCompact) Modifier.weight(1f) else Modifier) {
                    Text(
                        text = "TODAY'S SHIFT",
                        style = DssTypography.headlineLarge.copy(
                            fontSize = if (isCompact) 20.sp else 38.sp,
                            fontWeight = FontWeight.Black,
                            color = DssTextPrimary
                        )
                    )
                    Text(
                        text = if (isCompact) "Select your assigned task" else "Select your current assigned packing workstation task",
                        style = DssTypography.bodyMedium.copy(
                            fontSize = if (isCompact) 13.sp else 22.sp,
                            color = DssTextSecondary
                        )
                    )
                }

                val completedCount = scheduleList.count { it.schedule.status == ScheduleStatus.COMPLETED }
                Surface(
                    color = Color(0xFFEFF6FF),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.5.dp, DssPrimary)
                ) {
                    Text(
                        text = "$completedCount/${scheduleList.size} Done",
                        style = DssTypography.labelLarge.copy(
                            fontSize = if (isCompact) 14.sp else 20.sp,
                            color = DssPrimary,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.padding(horizontal = if (isCompact) 10.dp else 20.dp, vertical = if (isCompact) 6.dp else 12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(if (isCompact) 12.dp else 24.dp))

            // Schedule items list
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(if (isCompact) 10.dp else 18.dp)
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

                    val borderWidth = if (isCurrentActive) 3.dp else 1.5.dp

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = !isCompleted) {
                                onSelectTask(item)
                            },
                        shape = RoundedCornerShape(18.dp),
                        border = BorderStroke(borderWidth, borderColor),
                        colors = CardDefaults.cardColors(containerColor = cardBackground),
                        elevation = CardDefaults.cardElevation(defaultElevation = if (isCurrentActive) 4.dp else 1.dp)
                    ) {
                        if (isCompact) {
                            // Compact Mobile Card: Vertical Stack
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
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
                                                modifier = Modifier.size(24.dp)
                                            )
                                        } else {
                                            Text(
                                                text = "${index + 1}",
                                                style = DssTypography.headlineLarge.copy(
                                                    fontSize = 18.sp,
                                                    fontWeight = FontWeight.Black,
                                                    color = if (isCurrentActive) Color.White else DssTextPrimary
                                                )
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = item.templateWithDetails.template.title,
                                            style = DssTypography.titleLarge.copy(
                                                fontSize = 18.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isCompleted) Color(0xFF166534) else DssTextPrimary
                                            )
                                        )
                                        Text(
                                            text = "${item.templateWithDetails.sortedSteps.size} steps • Est. ${item.templateWithDetails.template.estimatedMinutes} mins",
                                            style = DssTypography.bodyMedium.copy(
                                                fontSize = 13.sp,
                                                color = DssTextSecondary
                                            )
                                        )
                                    }
                                }

                                if (isCompleted) {
                                    Surface(
                                        color = Color(0xFFDCFCE7),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center,
                                            modifier = Modifier.padding(vertical = 8.dp)
                                        ) {
                                            Icon(
                                                painter = painterResource(id = R.drawable.ic_check),
                                                contentDescription = null,
                                                tint = DssSuccess,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "COMPLETED",
                                                style = DssTypography.labelLarge.copy(
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF166534)
                                                )
                                            )
                                        }
                                    }
                                } else {
                                    DssBigButton(
                                        text = if (isCurrentActive) "START THIS TASK" else "SELECT TASK",
                                        onClick = { onSelectTask(item) },
                                        variant = if (isCurrentActive) DssButtonVariant.SUCCESS else DssButtonVariant.PRIMARY,
                                        minHeight = 48.dp,
                                        modifier = Modifier.fillMaxWidth(),
                                        fontSize = 16.sp
                                    )
                                }
                            }
                        } else {
                            // Wide Tablet Card: Row Layout
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
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
    }
}
