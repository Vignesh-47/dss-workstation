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
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DssBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 48.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Congratulatory Header & Large Green Checkmark
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(DssSuccess),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_check),
                    contentDescription = "Task complete",
                    tint = Color.White,
                    modifier = Modifier.size(64.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "TASK COMPLETE!",
                style = DssTypography.displayLarge.copy(
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF14532D)
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Please call your supervisor for inspection and handover.",
                style = DssTypography.headlineLarge.copy(
                    fontSize = 28.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = DssTextPrimary,
                    textAlign = TextAlign.Center
                )
            )
        }

        // Suggested reporting phrase card with TTS
        Card(
            modifier = Modifier
                .fillMaxWidth(0.85f),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(3.dp, Color(0xFF86EFAC)),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                modifier = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                Text(
                    text = "WHAT TO SAY TO YOUR SUPERVISOR:",
                    style = DssTypography.labelLarge.copy(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF15803D)
                    )
                )

                Text(
                    text = "\"$completionPhrase\"",
                    style = DssTypography.titleLarge.copy(
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = DssTextPrimary,
                        textAlign = TextAlign.Center
                    )
                )

                AudioSpeakButton(
                    onClick = onSpeakCompletionPhrase,
                    isSpeaking = isSpeaking,
                    text = "PLAY REPORTING PHRASE ALOUD",
                    modifier = Modifier.fillMaxWidth(0.7f)
                )
            }
        }

        // Single large button at bottom: "CONFIRM & GO TO NEXT"
        DssBigButton(
            text = "CONFIRM & GO TO NEXT",
            onClick = onConfirmAndNext,
            variant = DssButtonVariant.SUCCESS,
            minHeight = DssDimens.GiantButtonHeight, // 92dp
            minWidth = 380.dp,
            modifier = Modifier.fillMaxWidth(0.85f),
            iconResId = R.drawable.ic_check,
            fontSize = 28.sp
        )
    }
}
