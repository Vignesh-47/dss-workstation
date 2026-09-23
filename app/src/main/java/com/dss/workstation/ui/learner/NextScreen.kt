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
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DssBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 48.dp, vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        if (nextScheduleItem != null) {
            // Case A: Next scheduled task available
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "UP NEXT ON YOUR SCHEDULE",
                    style = DssTypography.headlineLarge.copy(
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = DssPrimary
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Ready to proceed to your next assignment?",
                    style = DssTypography.bodyMedium.copy(
                        fontSize = 22.sp,
                        color = DssTextSecondary
                    )
                )
            }

            // Next Task Card Preview
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.75f),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(3.dp, DssPrimary),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFDBEAFE)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_arrow_forward),
                            contentDescription = null,
                            tint = DssPrimary,
                            modifier = Modifier.size(44.dp)
                        )
                    }

                    Text(
                        text = nextScheduleItem.templateWithDetails.template.title,
                        style = DssTypography.displayLarge.copy(
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Black,
                            color = DssTextPrimary,
                            textAlign = TextAlign.Center
                        )
                    )

                    Text(
                        text = "${nextScheduleItem.templateWithDetails.sortedSteps.size} steps • Est. ${nextScheduleItem.templateWithDetails.template.estimatedMinutes} mins",
                        style = DssTypography.bodyMedium.copy(
                            fontSize = 22.sp,
                            color = DssTextSecondary
                        )
                    )
                }
            }

            // Action Buttons
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
        } else {
            // Case B: All shift tasks completed!
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(DssSuccess),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_check),
                        contentDescription = "All done",
                        tint = Color.White,
                        modifier = Modifier.size(68.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "ALL TASKS FINISHED!",
                    style = DssTypography.displayLarge.copy(
                        fontSize = 44.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF14532D)
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "All scheduled workstation tasks are complete.\nGreat work today!",
                    style = DssTypography.headlineLarge.copy(
                        fontSize = 28.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = DssTextPrimary,
                        textAlign = TextAlign.Center
                    )
                )
            }

            // Return to Today's Schedule button
            DssBigButton(
                text = "RETURN TO TODAY'S SCHEDULE",
                onClick = onReturnToToday,
                variant = DssButtonVariant.PRIMARY,
                minHeight = DssDimens.GiantButtonHeight, // 92dp
                minWidth = 400.dp,
                iconResId = R.drawable.ic_refresh,
                fontSize = 26.sp
            )
        }
    }
}
