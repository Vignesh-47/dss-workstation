package com.dss.workstation.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.dss.workstation.R
import com.dss.workstation.ui.theme.*

@Composable
fun HelpDialog(
    helpPhrase: String,
    onSpeakOutLoud: () -> Unit,
    onDismiss: () -> Unit,
    isSpeaking: Boolean = false
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        // High visibility Amber Alert Box covering center tablet screen
        Box(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .wrapContentHeight()
                .background(Color.White, RoundedCornerShape(DssDimens.DialogCornerRadius))
                .border(
                    width = DssDimens.AlertBorderWidth,
                    color = DssAmberAlertBorder,
                    shape = RoundedCornerShape(DssDimens.DialogCornerRadius)
                )
                .padding(36.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Header Alert Icon & Badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .background(DssWarningContainer, RoundedCornerShape(16.dp))
                        .padding(horizontal = 24.dp, vertical = 12.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_help),
                        contentDescription = "Help Request",
                        tint = DssWarning,
                        modifier = Modifier.size(44.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "ASSISTANCE REQUESTED",
                        style = DssTypography.headlineLarge.copy(
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF92400E)
                        )
                    )
                }

                // Help Phrase Text in High-Contrast Large Type (32 sp)
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFFFFFBEB),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(2.dp, Color(0xFFFDE68A))
                ) {
                    Text(
                        text = "\"$helpPhrase\"",
                        style = DssTypography.titleLarge.copy(
                            fontSize = 34.sp,
                            fontWeight = FontWeight.Bold,
                            color = DssTextPrimary,
                            textAlign = TextAlign.Center
                        ),
                        modifier = Modifier.padding(28.dp)
                    )
                }

                Text(
                    text = "A supervisor or team member will assist you shortly.",
                    style = DssTypography.bodyMedium.copy(
                        fontSize = 22.sp,
                        textAlign = TextAlign.Center
                    )
                )

                // Buttons Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // TTS Speak Button
                    DssBigButton(
                        text = if (isSpeaking) "SPEAKING..." else "SPEAK OUT LOUD",
                        onClick = onSpeakOutLoud,
                        modifier = Modifier.weight(1f),
                        variant = DssButtonVariant.WARNING,
                        iconResId = R.drawable.ic_volume_up,
                        fontSize = 24.sp
                    )

                    // Staff Handled / Close Button
                    DssBigButton(
                        text = "STAFF HANDLED / RESUME",
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        variant = DssButtonVariant.SUCCESS,
                        iconResId = R.drawable.ic_check,
                        fontSize = 24.sp
                    )
                }
            }
        }
    }
}
