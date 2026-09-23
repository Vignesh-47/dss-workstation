package com.dss.workstation.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dss.workstation.R
import com.dss.workstation.ui.theme.*

@Composable
fun AudioSpeakButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isSpeaking: Boolean = false,
    text: String = "READ OUT LOUD"
) {
    val backgroundColor = if (isSpeaking) Color(0xFFFEF3C7) else Color(0xFFEFF6FF)
    val borderColor = if (isSpeaking) DssWarning else DssPrimary
    val contentColor = if (isSpeaking) Color(0xFF92400E) else DssPrimary

    Button(
        onClick = onClick,
        modifier = modifier
            .defaultMinSize(minHeight = DssDimens.SecondaryButtonHeight),
        shape = RoundedCornerShape(DssDimens.ButtonCornerRadius),
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            contentColor = contentColor
        ),
        border = BorderStroke(2.dp, borderColor),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_volume_up),
                contentDescription = "Read out loud",
                tint = contentColor,
                modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = if (isSpeaking) "SPEAKING..." else text,
                style = DssTypography.labelLarge.copy(
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = contentColor
                )
            )
        }
    }
}
