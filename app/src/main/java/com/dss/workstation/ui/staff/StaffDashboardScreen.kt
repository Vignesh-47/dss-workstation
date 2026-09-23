package com.dss.workstation.ui.staff

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.dss.workstation.R
import com.dss.workstation.ui.components.DssBigButton
import com.dss.workstation.ui.components.DssButtonVariant
import com.dss.workstation.ui.theme.*
import com.dss.workstation.util.TtsHelper

@Composable
fun StaffDashboardScreen(
    viewModel: StaffViewModel,
    ttsHelper: TtsHelper,
    onExitStaff: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentTab by viewModel.currentTab.collectAsState()
    val templates by viewModel.allTemplatesWithDetails.collectAsState()
    val scheduleList by viewModel.dailyScheduleList.collectAsState()
    val editingTemplate by viewModel.editingTemplate.collectAsState()
    val backupStatus by viewModel.backupStatus.collectAsState()

    Row(modifier = modifier.fillMaxSize().background(DssBackground)) {
        // Left Navigation Sidebar (280dp wide)
        Surface(
            modifier = Modifier
                .width(300.dp)
                .fillMaxHeight(),
            color = Color.White,
            shadowElevation = 4.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Header Brand
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(DssPrimary, RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_settings),
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "STAFF CONTROL",
                                style = DssTypography.titleMedium.copy(
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Black,
                                    color = DssTextPrimary
                                )
                            )
                            Text(
                                text = "Administration Suite",
                                style = DssTypography.bodyMedium.copy(fontSize = 14.sp)
                            )
                        }
                    }

                    Divider(color = Color(0xFFE2E8F0), thickness = 2.dp)

                    // Navigation Tabs
                    val navItems = listOf(
                        Triple(StaffTab.SCHEDULE_ORGANIZER, "Schedule Organizer", R.drawable.ic_check),
                        Triple(StaffTab.TEMPLATES, "Template Builder", R.drawable.ic_photo),
                        Triple(StaffTab.LIVE_PREVIEW, "Live Preview Sandbox", R.drawable.ic_volume_up),
                        Triple(StaffTab.TASK_RECOVERY, "Task Recovery & Reset", R.drawable.ic_refresh),
                        Triple(StaffTab.BACKUP_RESTORE, "Backup & Restore", R.drawable.ic_backup),
                        Triple(StaffTab.KIOSK_SETTINGS, "Kiosk & Security", R.drawable.ic_lock)
                    )

                    navItems.forEach { (tab, label, iconRes) ->
                        val isSelected = currentTab == tab
                        val bg = if (isSelected) DssPrimary else Color.Transparent
                        val contentColor = if (isSelected) Color.White else DssTextPrimary

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(bg)
                                .clickable { viewModel.selectTab(tab) }
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    painter = painterResource(id = iconRes),
                                    contentDescription = null,
                                    tint = contentColor,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = label,
                                    color = contentColor,
                                    fontSize = 16.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                // Return to Learner Flow Button
                DssBigButton(
                    text = "EXIT TO WORKSTATION",
                    onClick = onExitStaff,
                    variant = DssButtonVariant.SECONDARY,
                    minHeight = 64.dp,
                    fontSize = 16.sp,
                    iconResId = R.drawable.ic_arrow_back,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Right Content Area
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            when (currentTab) {
                StaffTab.SCHEDULE_ORGANIZER -> {
                    ScheduleOrganizerScreen(
                        scheduleList = scheduleList,
                        allTemplates = templates,
                        onAddTemplate = { templateId, customTitle -> viewModel.addTemplateToQueue(templateId, customTitle) },
                        onRenameItem = { scheduleId, newTitle -> viewModel.renameScheduleItem(scheduleId, newTitle) },
                        onMoveUp = { viewModel.moveScheduleItemUp(it) },
                        onMoveDown = { viewModel.moveScheduleItemDown(it) },
                        onRemoveItem = { viewModel.removeScheduleItem(it) },
                        onClearSchedule = { viewModel.clearDailySchedule() }
                    )
                }
                StaffTab.TEMPLATES -> {
                    TemplateBuilderScreen(
                        templates = templates,
                        editingTemplate = editingTemplate,
                        onStartNew = { viewModel.startNewTemplate() },
                        onEdit = { viewModel.editTemplate(it) },
                        onDuplicate = { viewModel.duplicateTemplate(it) },
                        onDelete = { viewModel.deleteTemplate(it) },
                        onSaveTemplate = { title, desc, mins, help, comp, steps, check ->
                            viewModel.saveEditingTemplate(title, desc, mins, help, comp, steps, check)
                        },
                        onCancelEditing = { viewModel.cancelTemplateEditing() }
                    )
                }
                StaffTab.LIVE_PREVIEW -> {
                    LivePreviewScreen(
                        templates = templates,
                        ttsHelper = ttsHelper,
                        onExitPreview = { viewModel.selectTab(StaffTab.TEMPLATES) }
                    )
                }
                StaffTab.TASK_RECOVERY -> {
                    TaskRecoveryScreen(
                        scheduleList = scheduleList,
                        onResetToStep1 = { viewModel.resetScheduleToStep1(it) },
                        onJumpToStep = { id, step -> viewModel.jumpScheduleToStep(id, step) },
                        onForceComplete = { viewModel.forceCompleteSchedule(it) }
                    )
                }
                StaffTab.BACKUP_RESTORE -> {
                    BackupRestoreScreen(
                        backupStatus = backupStatus,
                        onExport = { viewModel.exportBackup(it) },
                        onImport = { viewModel.importBackup(it) },
                        onClearStatus = { viewModel.clearBackupStatus() }
                    )
                }
                StaffTab.KIOSK_SETTINGS -> {
                    KioskSettingsScreen(
                        securityManager = viewModel.securityManager
                    )
                }
            }
        }
    }
}
