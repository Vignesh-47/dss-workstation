package com.dss.workstation.ui.staff

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dss.workstation.R
import com.dss.workstation.ui.components.DssBigButton
import com.dss.workstation.ui.components.DssButtonVariant
import com.dss.workstation.ui.theme.*

@Composable
fun BackupRestoreScreen(
    backupStatus: String?,
    onExport: (Uri) -> Unit,
    onImport: (Uri) -> Unit,
    onClearStatus: () -> Unit,
    modifier: Modifier = Modifier
) {
    // SAF Export Document Launcher
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/zip")
    ) { uri: Uri? ->
        if (uri != null) {
            onExport(uri)
        }
    }

    // SAF Import Document Launcher
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            onImport(uri)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Column {
            Text(
                text = "BACKUP & RESTORE SYSTEM (.dssbundle)",
                style = DssTypography.headlineLarge.copy(
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = DssTextPrimary
                )
            )
            Text(
                text = "Save or deploy workstation configurations, templates, and photos as a single archive",
                style = DssTypography.bodyMedium.copy(fontSize = 18.sp)
            )
        }

        // Status Card if active
        if (backupStatus != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                border = BorderStroke(2.dp, DssPrimary)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = backupStatus,
                        style = DssTypography.bodyLarge.copy(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = DssPrimary
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    Button(
                        onClick = onClearStatus,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = DssTextPrimary)
                    ) {
                        Text("DISMISS", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Export Card
            Card(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(0.7f),
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
                            text = "Export Workstation Bundle",
                            style = DssTypography.titleLarge.copy(fontSize = 26.sp, fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Serializes all task templates, steps, checklists, schedules, and step photo files into a single '.dssbundle' ZIP package.\n\nCan be saved to USB flash drives, SD cards, or device Downloads for backup or cloning across workstations.",
                            style = DssTypography.bodyMedium.copy(fontSize = 18.sp, color = DssTextSecondary)
                        )
                    }

                    DssBigButton(
                        text = "EXPORT .DSSBUNDLE",
                        onClick = {
                            val defaultName = "workstation_backup_${System.currentTimeMillis()}.dssbundle"
                            exportLauncher.launch(defaultName)
                        },
                        variant = DssButtonVariant.PRIMARY,
                        minHeight = 72.dp,
                        iconResId = R.drawable.ic_backup,
                        fontSize = 20.sp,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Import Card
            Card(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(0.7f),
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
                            text = "Import & Restore Bundle",
                            style = DssTypography.titleLarge.copy(fontSize = 26.sp, fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Select a '.dssbundle' file from USB storage, SD card, or local files.\n\nThe system validates archive integrity, restores photos into workstation storage, and updates the local SQLite database.",
                            style = DssTypography.bodyMedium.copy(fontSize = 18.sp, color = DssTextSecondary)
                        )
                    }

                    DssBigButton(
                        text = "SELECT .DSSBUNDLE TO IMPORT",
                        onClick = {
                            importLauncher.launch(arrayOf("application/zip", "application/octet-stream", "*/*"))
                        },
                        variant = DssButtonVariant.SUCCESS,
                        minHeight = 72.dp,
                        iconResId = R.drawable.ic_restore,
                        fontSize = 20.sp,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
