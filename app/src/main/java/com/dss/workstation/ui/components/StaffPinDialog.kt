package com.dss.workstation.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.dss.workstation.R
import com.dss.workstation.ui.theme.*

@Composable
fun StaffPinDialog(
    onPinEntered: (String) -> Boolean,
    onDismiss: () -> Unit
) {
    var enteredPin by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .width(520.dp)
                .wrapContentHeight()
                .background(Color.White, RoundedCornerShape(DssDimens.DialogCornerRadius))
                .border(
                    width = if (isError) 4.dp else 2.dp,
                    color = if (isError) DssDanger else Color(0xFFCBD5E1),
                    shape = RoundedCornerShape(DssDimens.DialogCornerRadius)
                )
                .padding(32.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_lock),
                        contentDescription = "Staff Access",
                        tint = DssPrimary,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "STAFF ACCESS",
                        style = DssTypography.headlineLarge.copy(
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                Text(
                    text = "Enter 4 to 6 digit staff PIN to open dashboard",
                    style = DssTypography.bodyMedium.copy(fontSize = 18.sp)
                )

                // Masked PIN bullets
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(
                            color = if (isError) DssDangerContainer else Color(0xFFF1F5F9),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(horizontal = 24.dp, vertical = 14.dp)
                ) {
                    repeat(6) { index ->
                        val filled = index < enteredPin.length
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        isError -> DssDanger
                                        filled -> DssPrimary
                                        else -> Color(0xFFCBD5E1)
                                    }
                                )
                        )
                    }
                }

                AnimatedVisibility(visible = isError) {
                    Text(
                        text = "Incorrect Staff PIN. Please try again.",
                        color = DssDanger,
                        style = DssTypography.bodyMedium.copy(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                // Numeric Keypad Grid (3x4)
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val rows = listOf(
                        listOf("1", "2", "3"),
                        listOf("4", "5", "6"),
                        listOf("7", "8", "9"),
                        listOf("CLEAR", "0", "DEL")
                    )

                    for (row in rows) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            for (key in row) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(68.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(
                                            when (key) {
                                                "CLEAR" -> Color(0xFFFEE2E2)
                                                "DEL" -> Color(0xFFF1F5F9)
                                                else -> Color(0xFFF8FAFC)
                                            }
                                        )
                                        .border(
                                            2.dp,
                                            when (key) {
                                                "CLEAR" -> Color(0xFFFCA5A5)
                                                else -> Color(0xFFCBD5E1)
                                            },
                                            RoundedCornerShape(14.dp)
                                        )
                                        .clickable {
                                            isError = false
                                            when (key) {
                                                "CLEAR" -> enteredPin = ""
                                                "DEL" -> if (enteredPin.isNotEmpty()) enteredPin = enteredPin.dropLast(1)
                                                else -> {
                                                    if (enteredPin.length < 6) {
                                                        enteredPin += key
                                                        if (enteredPin.length >= 4) {
                                                            val success = onPinEntered(enteredPin)
                                                            if (!success && enteredPin.length == 6) {
                                                                isError = true
                                                                enteredPin = ""
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = key,
                                        style = DssTypography.labelLarge.copy(
                                            fontSize = if (key.length > 1) 18.sp else 28.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (key == "CLEAR") DssDanger else DssTextPrimary
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                // Action Buttons: Cancel and Enter
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DssBigButton(
                        text = "CANCEL",
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        variant = DssButtonVariant.SECONDARY,
                        minHeight = 64.dp,
                        fontSize = 20.sp
                    )

                    DssBigButton(
                        text = "ENTER",
                        onClick = {
                            if (enteredPin.length >= 4) {
                                val success = onPinEntered(enteredPin)
                                if (!success) {
                                    isError = true
                                    enteredPin = ""
                                }
                            } else {
                                isError = true
                            }
                        },
                        modifier = Modifier.weight(1f),
                        variant = DssButtonVariant.PRIMARY,
                        minHeight = 64.dp,
                        fontSize = 20.sp
                    )
                }
            }
        }
    }
}
