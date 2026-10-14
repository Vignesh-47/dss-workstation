package com.dss.workstation.ui.learner

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.dss.workstation.R
import com.dss.workstation.data.model.DailyScheduleWithTemplate
import com.dss.workstation.ui.components.DssBigButton
import com.dss.workstation.ui.components.DssButtonVariant
import com.dss.workstation.ui.theme.*
import com.dss.workstation.util.PhotoStorageManager

@Composable
fun NowScreen(
    activeSchedule: DailyScheduleWithTemplate?,
    onStartTask: () -> Unit,
    onBackToToday: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val photoStorage = PhotoStorageManager(context)

    val template = activeSchedule?.templateWithDetails?.template
    val finalStep = activeSchedule?.templateWithDetails?.sortedSteps?.lastOrNull()
    val totalSteps = activeSchedule?.templateWithDetails?.sortedSteps?.size ?: 0

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
                .padding(horizontal = if (isCompact) 18.dp else 48.dp, vertical = if (isCompact) 16.dp else 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(if (isCompact) 14.dp else 20.dp)
        ) {
            // Top Header
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "CURRENT TASK",
                    style = DssTypography.headlineLarge.copy(
                        fontSize = if (isCompact) 20.sp else 28.sp,
                        color = DssPrimary,
                        fontWeight = FontWeight.ExtraBold
                    )
                )
                Spacer(modifier = Modifier.height(if (isCompact) 4.dp else 8.dp))
                Text(
                    text = template?.title ?: "No Active Task",
                    style = DssTypography.displayLarge.copy(
                        fontSize = if (isCompact) 26.sp else 44.sp,
                        fontWeight = FontWeight.Black,
                        color = DssTextPrimary,
                        textAlign = TextAlign.Center
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Total $totalSteps guided steps • Est. ${template?.estimatedMinutes ?: 10} minutes",
                    style = DssTypography.bodyMedium.copy(
                        fontSize = if (isCompact) 15.sp else 22.sp,
                        color = DssTextSecondary
                    )
                )
            }

            // Center: Large Preview Outcome Card
            Card(
                modifier = Modifier
                    .fillMaxWidth(if (isCompact) 0.95f else 0.65f)
                    .height(if (isCompact) 210.dp else 300.dp),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(if (isCompact) 2.dp else 3.dp, Color(0xFFCBD5E1)),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val imagePath = finalStep?.imagePath ?: "res:drawable/step5_attach_label"
                    val drawableId = photoStorage.resolveDrawableResId(imagePath)
                    val file = photoStorage.getFileFromRelativePath(imagePath)

                    if (drawableId != null) {
                        Image(
                            painter = painterResource(id = drawableId),
                            contentDescription = "Finished task preview",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(14.dp)),
                            contentScale = ContentScale.Fit
                        )
                    } else if (file != null) {
                        AsyncImage(
                            model = file,
                            contentDescription = "Finished task preview",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(14.dp)),
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        Image(
                            painter = painterResource(id = R.drawable.step5_attach_label),
                            contentDescription = "Finished task preview",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(14.dp)),
                            contentScale = ContentScale.Fit
                        )
                    }
                }
            }

            // Bottom Action Controls
            if (isCompact) {
                Column(
                    modifier = Modifier.fillMaxWidth(0.95f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    DssBigButton(
                        text = "START TASK NOW",
                        onClick = onStartTask,
                        variant = DssButtonVariant.SUCCESS,
                        minHeight = 58.dp,
                        modifier = Modifier.fillMaxWidth(),
                        iconResId = R.drawable.ic_arrow_forward,
                        fontSize = 18.sp
                    )
                    DssBigButton(
                        text = "TODAY'S SCHEDULE",
                        onClick = onBackToToday,
                        variant = DssButtonVariant.SECONDARY,
                        minHeight = 52.dp,
                        modifier = Modifier.fillMaxWidth(),
                        iconResId = R.drawable.ic_arrow_back,
                        fontSize = 16.sp
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(0.85f),
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DssBigButton(
                        text = "TODAY'S SCHEDULE",
                        onClick = onBackToToday,
                        variant = DssButtonVariant.SECONDARY,
                        minHeight = 84.dp,
                        modifier = Modifier.weight(0.35f),
                        iconResId = R.drawable.ic_arrow_back,
                        fontSize = 22.sp
                    )

                    DssBigButton(
                        text = "START TASK NOW",
                        onClick = onStartTask,
                        variant = DssButtonVariant.SUCCESS,
                        minHeight = 84.dp,
                        modifier = Modifier.weight(0.65f),
                        iconResId = R.drawable.ic_arrow_forward,
                        fontSize = 26.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
