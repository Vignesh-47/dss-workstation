package com.dss.workstation.ui.learner

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.dss.workstation.data.model.ScheduleStatus
import com.dss.workstation.ui.components.DssTopBar
import com.dss.workstation.ui.components.HelpDialog

@Composable
fun LearnerScreen(
    viewModel: LearnerViewModel,
    onOpenStaff: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentStage by viewModel.currentStage.collectAsState()
    val scheduleList by viewModel.dailyScheduleList.collectAsState()
    val activeSchedule by viewModel.activeSchedule.collectAsState()
    val currentStepIndex by viewModel.currentStepIndex.collectAsState()
    val checkedItems by viewModel.checkedItems.collectAsState()
    val isHelpDialogOpen by viewModel.isHelpDialogOpen.collectAsState()
    val isSpeaking by viewModel.isSpeaking.collectAsState()

    // Find the next task in the queue
    val currentIndex = scheduleList.indexOfFirst { it.schedule.id == activeSchedule?.schedule?.id }
    val nextScheduleItem = if (currentIndex != -1) {
        scheduleList.drop(currentIndex + 1).firstOrNull { it.schedule.status != ScheduleStatus.COMPLETED }
    } else {
        scheduleList.firstOrNull { it.schedule.status == ScheduleStatus.PENDING }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Persistent Workstation Top Bar
            DssTopBar(
                currentStage = currentStage,
                onStageClick = { stage ->
                    // Learners can navigate back to earlier stages or view TODAY
                    if (stage.ordinal <= currentStage.ordinal || stage == LearnerStage.TODAY) {
                        viewModel.jumpToStage(stage)
                    }
                },
                onStaffClick = onOpenStaff
            )

            // Stage Screen Body
            Box(modifier = Modifier.weight(1f)) {
                when (currentStage) {
                    LearnerStage.TODAY -> {
                        TodayScreen(
                            scheduleList = scheduleList,
                            activeScheduleId = activeSchedule?.schedule?.id,
                            onSelectTask = { selected ->
                                viewModel.selectTaskFromToday(selected)
                            }
                        )
                    }
                    LearnerStage.NOW -> {
                        NowScreen(
                            activeSchedule = activeSchedule,
                            onStartTask = { viewModel.startActiveTask() },
                            onBackToToday = { viewModel.returnToToday() }
                        )
                    }
                    LearnerStage.HOW -> {
                        val steps = activeSchedule?.templateWithDetails?.sortedSteps ?: emptyList()
                        val currentStep = steps.getOrNull(currentStepIndex)

                        HowScreen(
                            step = currentStep,
                            stepIndex = currentStepIndex,
                            totalSteps = steps.size,
                            isSpeaking = isSpeaking,
                            onSpeakInstruction = { viewModel.speakCurrentStepInstruction() },
                            onNeedHelp = { viewModel.openHelpDialog() },
                            onPrevStep = { viewModel.prevStep() },
                            onNextStep = { viewModel.nextStep() }
                        )
                    }
                    LearnerStage.CHECK -> {
                        val checklist = activeSchedule?.templateWithDetails?.sortedChecklistItems ?: emptyList()
                        CheckScreen(
                            checklistItems = checklist,
                            checkedItemIds = checkedItems,
                            onToggleItem = { viewModel.toggleCheckItem(it) },
                            onContinueToDone = { viewModel.proceedToDone() },
                            onBackToSteps = { viewModel.jumpToStage(LearnerStage.HOW) }
                        )
                    }
                    LearnerStage.DONE -> {
                        val completionPhrase = activeSchedule?.templateWithDetails?.template?.completionPhrase
                            ?: "I have finished packing this parcel. Please check."
                        DoneScreen(
                            completionPhrase = completionPhrase,
                            isSpeaking = isSpeaking,
                            onSpeakCompletionPhrase = { viewModel.speakCompletionPhrase() },
                            onConfirmAndNext = { viewModel.confirmAndGoToNext() }
                        )
                    }
                    LearnerStage.NEXT -> {
                        NextScreen(
                            nextScheduleItem = nextScheduleItem,
                            onStartNextTask = { nextItem ->
                                viewModel.startNextTask(nextItem)
                            },
                            onReturnToToday = { viewModel.returnToToday() }
                        )
                    }
                }
            }
        }

        // Assistance Requested Modal (In-place Help Dialog)
        if (isHelpDialogOpen) {
            val helpPhrase = activeSchedule?.templateWithDetails?.template?.helpPhrase
                ?: "I need help with this step. Please assist me."
            HelpDialog(
                helpPhrase = helpPhrase,
                onSpeakOutLoud = { viewModel.speakHelpPhrase() },
                onDismiss = { viewModel.closeHelpDialog() },
                isSpeaking = isSpeaking
            )
        }
    }
}
