package com.dss.workstation.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dss.workstation.ui.theme.*

enum class DssButtonVariant {
    PRIMARY,
    SUCCESS,
    WARNING,
    DANGER,
    OUTLINE,
    SECONDARY
}

@Composable
fun DssBigButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: DssButtonVariant = DssButtonVariant.PRIMARY,
    enabled: Boolean = true,
    minHeight: Dp = DssDimens.PrimaryButtonHeight,
    minWidth: Dp = 200.dp,
    iconResId: Int? = null,
    iconVector: ImageVector? = null,
    fontSize: TextUnit = 26.sp
) {
    val (backgroundColor, contentColor, borderStroke) = when (variant) {
        DssButtonVariant.PRIMARY -> Triple(
            DssPrimary,
            DssOnPrimary,
            BorderStroke(2.dp, DssPrimaryVariant)
        )
        DssButtonVariant.SUCCESS -> Triple(
            DssSuccess,
            DssOnSuccess,
            BorderStroke(2.dp, Color(0xFF14532D))
        )
        DssButtonVariant.WARNING -> Triple(
            DssWarning,
            DssOnWarning,
            BorderStroke(2.dp, Color(0xFF92400E))
        )
        DssButtonVariant.DANGER -> Triple(
            DssDanger,
            DssOnDanger,
            BorderStroke(2.dp, Color(0xFF7F1D1D))
        )
        DssButtonVariant.OUTLINE -> Triple(
            Color.White,
            DssTextPrimary,
            BorderStroke(3.dp, DssPrimary)
        )
        DssButtonVariant.SECONDARY -> Triple(
            Color(0xFFE2E8F0),
            DssTextPrimary,
            BorderStroke(2.dp, Color(0xFFCBD5E1))
        )
    }

    Button(
        onClick = onClick,
        modifier = modifier
            .defaultMinSize(minWidth = minWidth, minHeight = minHeight),
        enabled = enabled,
        shape = RoundedCornerShape(DssDimens.ButtonCornerRadius),
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            contentColor = contentColor,
            disabledContainerColor = Color(0xFFCBD5E1),
            disabledContentColor = Color(0xFF64748B)
        ),
        border = if (enabled) borderStroke else null,
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 6.dp,
            pressedElevation = 2.dp,
            disabledElevation = 0.dp
        ),
        contentPadding = PaddingValues(horizontal = 28.dp, vertical = 16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (iconResId != null) {
                Icon(
                    painter = painterResource(id = iconResId),
                    contentDescription = null,
                    modifier = Modifier
                        .size(36.dp)
                        .padding(end = 12.dp),
                    tint = if (enabled) contentColor else Color(0xFF64748B)
                )
            } else if (iconVector != null) {
                Icon(
                    imageVector = iconVector,
                    contentDescription = null,
                    modifier = Modifier
                        .size(36.dp)
                        .padding(end = 12.dp),
                    tint = if (enabled) contentColor else Color(0xFF64748B)
                )
            }
            Text(
                text = text,
                style = DssTypography.labelLarge.copy(
                    fontSize = fontSize,
                    fontWeight = FontWeight.Bold,
                    color = if (enabled) contentColor else Color(0xFF64748B)
                )
            )
        }
    }
}
