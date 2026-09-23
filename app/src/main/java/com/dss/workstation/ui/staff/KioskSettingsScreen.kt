package com.dss.workstation.ui.staff

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dss.workstation.R
import com.dss.workstation.ui.components.DssBigButton
import com.dss.workstation.ui.components.DssButtonVariant
import com.dss.workstation.ui.theme.*
import com.dss.workstation.util.StaffSecurityManager

@Composable
fun KioskSettingsScreen(
    securityManager: StaffSecurityManager,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? Activity

    val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
    var isPinned by remember {
        mutableStateOf(activityManager?.lockTaskModeState != ActivityManager.LOCK_TASK_MODE_NONE)
    }

    // PIN Change state
    var currentPin by remember { mutableStateOf("") }
    var newPin by remember { mutableStateOf("") }
    var confirmNewPin by remember { mutableStateOf("") }
    var pinMessage by remember { mutableStateOf<String?>(null) }
    var isPinSuccess by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Column {
            Text(
                text = "WORKSTATION KIOSK & SECURITY",
                style = DssTypography.headlineLarge.copy(
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = DssTextPrimary
                )
            )
            Text(
                text = "Lock workstation tablet into dedicated kiosk mode and manage staff credentials",
                style = DssTypography.bodyMedium.copy(fontSize = 18.sp)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Kiosk Lock Task Card
            Card(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(0.85f),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(2.dp, Color(0xFFCBD5E1))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(28.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text(
                            text = "Kiosk Screen Pinning",
                            style = DssTypography.titleLarge.copy(fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        )

                        Text(
                            text = "Screen Pinning (startLockTask) prevents learners from swiping to Android home, opening notification drawers, or launching unassigned applications on the workstation tablet.\n\nMode Status: ${if (isPinned) "PINNED (Active Kiosk)" else "UNPINNED (Standard Android)"}",
                            style = DssTypography.bodyMedium.copy(fontSize = 18.sp, color = DssTextSecondary)
                        )

                        Surface(
                            color = if (isPinned) Color(0xFFDCFCE7) else Color(0xFFFEF3C7),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = if (isPinned) "Workstation is locked to this app." else "App is not pinned.",
                                color = if (isPinned) Color(0xFF166534) else Color(0xFF92400E),
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                            )
                        }
                    }

                    DssBigButton(
                        text = if (isPinned) "STOP LOCK TASK (UNPIN)" else "START LOCK TASK (PIN TO TABLET)",
                        onClick = {
                            if (isPinned) {
                                activity?.stopLockTask()
                                isPinned = false
                            } else {
                                activity?.startLockTask()
                                isPinned = true
                            }
                        },
                        variant = if (isPinned) DssButtonVariant.DANGER else DssButtonVariant.PRIMARY,
                        minHeight = 72.dp,
                        iconResId = R.drawable.ic_lock,
                        fontSize = 20.sp,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Change PIN Card
            Card(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(0.85f),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(2.dp, Color(0xFFCBD5E1))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(28.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Text(
                            text = "Update Staff PIN",
                            style = DssTypography.titleLarge.copy(fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        )

                        OutlinedTextField(
                            value = currentPin,
                            onValueChange = { currentPin = it },
                            label = { Text("Current 4-6 Digit PIN") },
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = LocalTextStyle.current.copy(fontSize = 18.sp)
                        )

                        OutlinedTextField(
                            value = newPin,
                            onValueChange = { newPin = it },
                            label = { Text("New 4-6 Digit PIN") },
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = LocalTextStyle.current.copy(fontSize = 18.sp)
                        )

                        OutlinedTextField(
                            value = confirmNewPin,
                            onValueChange = { confirmNewPin = it },
                            label = { Text("Confirm New PIN") },
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = LocalTextStyle.current.copy(fontSize = 18.sp)
                        )

                        if (pinMessage != null) {
                            Text(
                                text = pinMessage ?: "",
                                color = if (isPinSuccess) DssSuccess else DssDanger,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }

                    DssBigButton(
                        text = "SAVE NEW PIN",
                        onClick = {
                            if (newPin != confirmNewPin) {
                                pinMessage = "New PIN and confirmation do not match."
                                isPinSuccess = false
                            } else if (newPin.length < 4 || newPin.length > 6) {
                                pinMessage = "PIN must be between 4 and 6 digits."
                                isPinSuccess = false
                            } else {
                                val changed = securityManager.changePin(currentPin, newPin)
                                if (changed) {
                                    pinMessage = "Staff PIN successfully updated!"
                                    isPinSuccess = true
                                    currentPin = ""
                                    newPin = ""
                                    confirmNewPin = ""
                                } else {
                                    pinMessage = "Current PIN is incorrect."
                                    isPinSuccess = false
                                }
                            }
                        },
                        variant = DssButtonVariant.SUCCESS,
                        minHeight = 64.dp,
                        fontSize = 20.sp,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
