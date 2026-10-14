package com.dss.workstation.ui.learner

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dss.workstation.R
import com.dss.workstation.data.model.DailyScheduleWithTemplate
import com.dss.workstation.ui.components.DssBigButton
import com.dss.workstation.ui.components.DssButtonVariant
import com.dss.workstation.ui.theme.*

@Composable
fun NextScreen(
    nextScheduleItem: DailyScheduleWithTemplate?,
    onStartNextTask: (DailyScheduleWithTemplate) -> Unit,
    onReturnToToday: () -> Unit,
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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = if (isCompact) 16.dp else 48.dp, vertical = if (isCompact) 18.dp else 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(if (isCompact) 14.dp else 20.dp)
        ) {
            if (nextScheduleItem != null) {
                // Case A: Next scheduled task available
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "UP NEXT ON YOUR SCHEDULE",
                        style = DssTypography.headlineLarge.copy(
                            fontSize = if (isCompact) 18.sp else 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = DssPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Ready to proceed to your next assignment?",
                        style = DssTypography.bodyMedium.copy(
                            fontSize = if (isCompact) 15.sp else 22.sp,
                            color = DssTextSecondary
                        )
                    )
                }

                // Next Task Card Preview
                Card(
                    modifier = Modifier.fillMaxWidth(if (isCompact) 0.98f else 0.75f),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(if (isCompact) 2.dp else 3.dp, DssPrimary),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(if (isCompact) 18.dp else 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(if (isCompact) 10.dp else 16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(if (isCompact) 52.dp else 72.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFDBEAFE)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_arrow_forward),
                                contentDescription = null,
                                tint = DssPrimary,
                                modifier = Modifier.size(if (isCompact) 32.dp else 44.dp)
                            )
                        }

                        Text(
                            text = nextScheduleItem.templateWithDetails.template.title,
                            style = DssTypography.displayLarge.copy(
                                fontSize = if (isCompact) 22.sp else 36.sp,
                                fontWeight = FontWeight.Black,
                                color = DssTextPrimary,
                                textAlign = TextAlign.Center
                            )
                        )

                        Text(
                            text = "${nextScheduleItem.templateWithDetails.sortedSteps.size} steps • Est. ${nextScheduleItem.templateWithDetails.template.estimatedMinutes} mins",
                            style = DssTypography.bodyMedium.copy(
                                fontSize = if (isCompact) 15.sp else 22.sp,
                                color = DssTextSecondary
                            )
                        )
                    }
                }

                // Action Buttons
                if (isCompact) {
                    Column(
                        modifier = Modifier.fillMaxWidth(0.98f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DssBigButton(
                            text = "START NEXT TASK",
                            onClick = { onStartNextTask(nextScheduleItem) },
                            variant = DssButtonVariant.SUCCESS,
                            minHeight = 56.dp,
                            modifier = Modifier.fillMaxWidth(),
                            iconResId = R.drawable.ic_arrow_forward,
                            fontSize = 17.sp
                        )
                        DssBigButton(
                            text = "VIEW SCHEDULE",
                            onClick = onReturnToToday,
                            variant = DssButtonVariant.SECONDARY,
                            minHeight = 50.dp,
                            modifier = Modifier.fillMaxWidth(),
                            iconResId = R.drawable.ic_arrow_back,
                            fontSize = 15.sp
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(0.75f),
                        horizontalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        DssBigButton(
                            text = "SCHEDULE",
                            onClick = onReturnToToday,
                            variant = DssButtonVariant.SECONDARY,
                            minHeight = 84.dp,
                            modifier = Modifier.weight(0.35f),
                            iconResId = R.drawable.ic_arrow_back,
                            fontSize = 22.sp
                        )

                        DssBigButton(
                            text = "START NEXT TASK",
                            onClick = { onStartNextTask(nextScheduleItem) },
                            variant = DssButtonVariant.SUCCESS,
                            minHeight = 84.dp,
                            modifier = Modifier.weight(0.65f),
                            iconResId = R.drawable.ic_arrow_forward,
                            fontSize = 26.sp
                        )
                    }
                }
            } else {
                // Case B: All shift tasks completed!
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(if (isCompact) 68.dp else 100.dp)
                            .clip(CircleShape)
                            .background(DssSuccess),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_check),
                            contentDescription = "All done",
                            tint = Color.White,
                            modifier = Modifier.size(if (isCompact) 44.dp else 68.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(if (isCompact) 10.dp else 20.dp))

                    Text(
                        text = "ALL TASKS FINISHED!",
                        style = DssTypography.displayLarge.copy(
                            fontSize = if (isCompact) 26.sp else 44.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF14532D)
                        )
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "All scheduled workstation tasks are complete.\nGreat work today!",
                        style = DssTypography.headlineLarge.copy(
                            fontSize = if (isCompact) 16.sp else 28.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = DssTextPrimary,
                            textAlign = TextAlign.Center
                        )
                    )
                }

                DssBigButton(
                    text = "RETURN TO TODAY'S SCHEDULE",
                    onClick = onReturnToToday,
                    variant = DssButtonVariant.PRIMARY,
                    minHeight = if (isCompact) 56.dp else 92.dp,
                    modifier = Modifier.fillMaxWidth(if (isCompact) 0.98f else 0.8f),
                    iconResId = R.drawable.ic_refresh,
                    fontSize = if (isCompact) 17.sp else 26.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
