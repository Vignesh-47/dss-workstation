package com.dss.workstation.ui.learner

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.dss.workstation.R
import com.dss.workstation.data.model.TaskStepEntity
import com.dss.workstation.ui.components.AudioSpeakButton
import com.dss.workstation.ui.components.DssBigButton
import com.dss.workstation.ui.components.DssButtonVariant
import com.dss.workstation.ui.theme.*
import com.dss.workstation.util.PhotoStorageManager

@Composable
fun HowScreen(
    step: TaskStepEntity?,
    stepIndex: Int,
    totalSteps: Int,
    isSpeaking: Boolean,
    onSpeakInstruction: () -> Unit,
    onNeedHelp: () -> Unit,
    onPrevStep: () -> Unit,
    onNextStep: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val photoStorage = PhotoStorageManager(context)

    val isLastStep = stepIndex >= totalSteps - 1

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DssBackground)
            .padding(horizontal = 28.dp, vertical = 20.dp)
    ) {
        // Step Header Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = DssPrimary,
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "STEP ${stepIndex + 1} OF $totalSteps",
                    style = DssTypography.headlineLarge.copy(
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    ),
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 10.dp)
                )
            }

            // Persistent brightly colored "I Need Help" Button
            DssBigButton(
                text = "I NEED HELP",
                onClick = onNeedHelp,
                variant = DssButtonVariant.WARNING,
                minHeight = 72.dp,
                minWidth = 240.dp,
                iconResId = R.drawable.ic_help,
                fontSize = 24.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Split View in Landscape:
        // Left (55% width): High-resolution image card
        // Right (45% width): Large instruction text + Audio Speak Button + Nav Buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // LEFT 55%: Image Card
            Card(
                modifier = Modifier
                    .weight(0.55f)
                    .fillMaxHeight(),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(3.dp, Color(0xFFCBD5E1)),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val imagePath = step?.imagePath
                    val drawableId = photoStorage.resolveDrawableResId(imagePath)
                    val file = if (imagePath != null) photoStorage.getFileFromRelativePath(imagePath) else null

                    if (drawableId != null) {
                        Image(
                            painter = painterResource(id = drawableId),
                            contentDescription = "Step illustration",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(16.dp)),
                            contentScale = ContentScale.Fit
                        )
                    } else if (file != null) {
                        AsyncImage(
                            model = file,
                            contentDescription = "Step photo",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(16.dp)),
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        // Fallback default graphic
                        Image(
                            painter = painterResource(id = R.drawable.step1_materials),
                            contentDescription = "Step illustration",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(16.dp)),
                            contentScale = ContentScale.Fit
                        )
                    }
                }
            }

            // RIGHT 45%: Instructions + Audio + Navigation
            Column(
                modifier = Modifier
                    .weight(0.45f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Content: Instruction Card + Audio Speak Button
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Instruction Card (Text >= 30 sp, bold, high contrast)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(2.dp, Color(0xFFE2E8F0)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Text(
                            text = step?.instruction ?: "Follow the instructions for this step.",
                            style = DssTypography.titleLarge.copy(
                                fontSize = 32.sp,
                                lineHeight = 42.sp,
                                fontWeight = FontWeight.Bold,
                                color = DssTextPrimary
                            ),
                            modifier = Modifier.padding(24.dp)
                        )
                    }

                    // Audio Speak Button
                    AudioSpeakButton(
                        onClick = onSpeakInstruction,
                        isSpeaking = isSpeaking,
                        text = "READ INSTRUCTION ALOUD",
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Bottom Row Navigation: PREVIOUS STEP and NEXT STEP / PROCEED TO CHECK
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Previous Step Button
                    DssBigButton(
                        text = "PREVIOUS",
                        onClick = onPrevStep,
                        variant = DssButtonVariant.SECONDARY,
                        minHeight = 84.dp,
                        modifier = Modifier.weight(0.4f),
                        iconResId = R.drawable.ic_arrow_back,
                        fontSize = 22.sp
                    )

                    // Next Step / Proceed to Check Button
                    DssBigButton(
                        text = if (isLastStep) "PROCEED TO CHECK" else "NEXT STEP",
                        onClick = onNextStep,
                        variant = if (isLastStep) DssButtonVariant.SUCCESS else DssButtonVariant.PRIMARY,
                        minHeight = 84.dp,
                        modifier = Modifier.weight(0.6f),
                        iconResId = if (isLastStep) R.drawable.ic_check else R.drawable.ic_arrow_forward,
                        fontSize = 24.sp
                    )
                }
            }
        }
    }
}
