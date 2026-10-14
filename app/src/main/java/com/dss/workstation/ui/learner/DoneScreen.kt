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
import com.dss.workstation.ui.components.AudioSpeakButton
import com.dss.workstation.ui.components.DssBigButton
import com.dss.workstation.ui.components.DssButtonVariant
import com.dss.workstation.ui.theme.*

@Composable
fun DoneScreen(
    completionPhrase: String,
    isSpeaking: Boolean,
    onSpeakCompletionPhrase: () -> Unit,
    onConfirmAndNext: () -> Unit,
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
                .padding(horizontal = if (isCompact) 16.dp else 48.dp, vertical = if (isCompact) 16.dp else 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(if (isCompact) 14.dp else 20.dp)
        ) {
            // Congratulatory Header & Checkmark
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(if (isCompact) 64.dp else 96.dp)
                        .clip(CircleShape)
                        .background(DssSuccess),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_check),
                        contentDescription = "Task complete",
                        tint = Color.White,
                        modifier = Modifier.size(if (isCompact) 40.dp else 64.dp)
                    )
                }

                Spacer(modifier = Modifier.height(if (isCompact) 8.dp else 16.dp))

                Text(
                    text = "TASK COMPLETE!",
                    style = DssTypography.displayLarge.copy(
                        fontSize = if (isCompact) 26.sp else 42.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF14532D)
                    )
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Please call your supervisor for inspection and handover.",
                    style = DssTypography.headlineLarge.copy(
                        fontSize = if (isCompact) 16.sp else 28.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = DssTextPrimary,
                        textAlign = TextAlign.Center
                    )
                )
            }

            // Suggested reporting phrase card with TTS
            Card(
                modifier = Modifier.fillMaxWidth(if (isCompact) 0.98f else 0.85f),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(if (isCompact) 2.dp else 3.dp, Color(0xFF86EFAC)),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(if (isCompact) 16.dp else 28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(if (isCompact) 12.dp else 18.dp)
                ) {
                    Text(
                        text = "WHAT TO SAY TO YOUR SUPERVISOR:",
                        style = DssTypography.labelLarge.copy(
                            fontSize = if (isCompact) 14.sp else 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF15803D)
                        )
                    )

                    Text(
                        text = ""$completionPhrase"",
                        style = DssTypography.titleLarge.copy(
                            fontSize = if (isCompact) 20.sp else 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = DssTextPrimary,
                            textAlign = TextAlign.Center
                        )
                    )

                    AudioSpeakButton(
                        onClick = onSpeakCompletionPhrase,
                        isSpeaking = isSpeaking,
                        text = "PLAY REPORTING PHRASE ALOUD",
                        modifier = Modifier.fillMaxWidth(if (isCompact) 1f else 0.7f)
                    )
                }
            }

            // Single large button: "CONFIRM & GO TO NEXT"
            DssBigButton(
                text = "CONFIRM & GO TO NEXT",
                onClick = onConfirmAndNext,
                variant = DssButtonVariant.SUCCESS,
                minHeight = if (isCompact) 56.dp else 92.dp,
                modifier = Modifier.fillMaxWidth(if (isCompact) 0.98f else 0.85f),
                iconResId = R.drawable.ic_check,
                fontSize = if (isCompact) 18.sp else 28.sp
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
