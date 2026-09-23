package com.dss.workstation.ui.staff

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dss.workstation.data.model.DailyScheduleEntity
import com.dss.workstation.data.model.DailyScheduleWithTemplate
import com.dss.workstation.data.model.ScheduleStatus
import com.dss.workstation.data.model.TaskTemplateWithDetails
import com.dss.workstation.ui.components.DssTopBar
import com.dss.workstation.ui.components.HelpDialog
import com.dss.workstation.ui.learner.*
import com.dss.workstation.ui.theme.*
import com.dss.workstation.util.TtsHelper

@Composable
fun LivePreviewScreen(
    templates: List<TaskTemplateWithDetails>,
    ttsHelper: TtsHelper,
    onExitPreview: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (templates.isEmpty()) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No templates available to preview.", style = DssTypography.headlineLarge)
        }
        return
    }

    var selectedTemplateIndex by remember { mutableStateOf(0) }
    val currentTemplate = templates.getOrNull(selectedTemplateIndex) ?: templates.first()

    // Ephemeral Sandbox State
    var sandboxStage by remember { mutableStateOf(LearnerStage.NOW) }
    var sandboxStepIndex by remember { mutableStateOf(0) }
    var sandboxCheckedItems by remember { mutableStateOf(setOf<String>()) }
    var isHelpOpen by remember { mutableStateOf(false) }

    val isSpeaking by ttsHelper.isSpeaking.collectAsState()

    // Ephemeral daily schedule mock
    val mockSchedule = remember(currentTemplate) {
        DailyScheduleWithTemplate(
            schedule = DailyScheduleEntity(
                id = "mock_sandbox_id",
                templateId = currentTemplate.template.id,
                queueOrder = 1,
                status = ScheduleStatus.IN_PROGRESS,
                currentStepIndex = sandboxStepIndex
            ),
            templateWithDetails = currentTemplate
        )
    }

    Column(modifier = modifier.fillMaxSize()) {
        // Staff Sandbox Indicator Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFFFEF3C7),
            shadowElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "🧪 SANDBOX PREVIEW MODE",
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF92400E),
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "Template: ${currentTemplate.template.title}",
                        fontSize = 16.sp,
                        color = DssTextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = {
                            // Cycle template
                            selectedTemplateIndex = (selectedTemplateIndex + 1) % templates.size
                            sandboxStage = LearnerStage.NOW
                            sandboxStepIndex = 0
                            sandboxCheckedItems = emptySet()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = DssTextPrimary),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Switch Template (${selectedTemplateIndex + 1}/${templates.size})", fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onExitPreview,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626), contentColor = Color.White),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Exit Preview", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Top bar
        DssTopBar(
            currentStage = sandboxStage,
            onStageClick = { stage -> sandboxStage = stage },
            onStaffClick = onExitPreview
        )

        // Sandbox Stage Screen
        Box(modifier = Modifier.weight(1f)) {
            when (sandboxStage) {
                LearnerStage.TODAY -> {
                    TodayScreen(
                        scheduleList = listOf(mockSchedule),
                        activeScheduleId = mockSchedule.schedule.id,
                        onSelectTask = { sandboxStage = LearnerStage.NOW }
                    )
                }
                LearnerStage.NOW -> {
                    NowScreen(
                        activeSchedule = mockSchedule,
                        onStartTask = {
                            sandboxStage = LearnerStage.HOW
                            sandboxStepIndex = 0
                            val first = currentTemplate.sortedSteps.firstOrNull()
                            first?.let { ttsHelper.speak(it.instruction) }
                        },
                        onBackToToday = { sandboxStage = LearnerStage.TODAY }
                    )
                }
                LearnerStage.HOW -> {
                    val steps = currentTemplate.sortedSteps
                    val currentStep = steps.getOrNull(sandboxStepIndex)
                    HowScreen(
                        step = currentStep,
                        stepIndex = sandboxStepIndex,
                        totalSteps = steps.size,
                        isSpeaking = isSpeaking,
                        onSpeakInstruction = { currentStep?.let { ttsHelper.speak(it.instruction) } },
                        onNeedHelp = { isHelpOpen = true },
                        onPrevStep = {
                            if (sandboxStepIndex > 0) {
                                sandboxStepIndex--
                                steps.getOrNull(sandboxStepIndex)?.let { ttsHelper.speak(it.instruction) }
                            } else {
                                sandboxStage = LearnerStage.NOW
                            }
                        },
                        onNextStep = {
                            if (sandboxStepIndex < steps.size - 1) {
                                sandboxStepIndex++
                                steps.getOrNull(sandboxStepIndex)?.let { ttsHelper.speak(it.instruction) }
                            } else {
                                sandboxStage = LearnerStage.CHECK
                            }
                        }
                    )
                }
                LearnerStage.CHECK -> {
                    CheckScreen(
                        checklistItems = currentTemplate.sortedChecklistItems,
                        checkedItemIds = sandboxCheckedItems,
                        onToggleItem = { id ->
                            val s = sandboxCheckedItems.toMutableSet()
                            if (s.contains(id)) s.remove(id) else s.add(id)
                            sandboxCheckedItems = s
                        },
                        onContinueToDone = { sandboxStage = LearnerStage.DONE },
                        onBackToSteps = { sandboxStage = LearnerStage.HOW }
                    )
                }
                LearnerStage.DONE -> {
                    DoneScreen(
                        completionPhrase = currentTemplate.template.completionPhrase,
                        isSpeaking = isSpeaking,
                        onSpeakCompletionPhrase = { ttsHelper.speak(currentTemplate.template.completionPhrase) },
                        onConfirmAndNext = { sandboxStage = LearnerStage.NEXT }
                    )
                }
                LearnerStage.NEXT -> {
                    NextScreen(
                        nextScheduleItem = null,
                        onStartNextTask = { sandboxStage = LearnerStage.NOW },
                        onReturnToToday = { sandboxStage = LearnerStage.TODAY }
                    )
                }
            }

            if (isHelpOpen) {
                HelpDialog(
                    helpPhrase = currentTemplate.template.helpPhrase,
                    onSpeakOutLoud = { ttsHelper.speak(currentTemplate.template.helpPhrase) },
                    onDismiss = { isHelpOpen = false },
                    isSpeaking = isSpeaking
                )
            }
        }
    }
}
