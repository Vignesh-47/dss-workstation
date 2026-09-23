package com.dss.workstation.ui.learner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dss.workstation.data.model.DailyScheduleEntity
import com.dss.workstation.data.model.DailyScheduleWithTemplate
import com.dss.workstation.data.model.ScheduleStatus
import com.dss.workstation.data.model.TaskTemplateWithDetails
import com.dss.workstation.data.repository.ScheduleRepository
import com.dss.workstation.data.repository.TaskRepository
import com.dss.workstation.util.TtsHelper
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class LearnerViewModel(
    private val scheduleRepository: ScheduleRepository,
    private val taskRepository: TaskRepository,
    val ttsHelper: TtsHelper
) : ViewModel() {

    private val _currentStage = MutableStateFlow(LearnerStage.TODAY)
    val currentStage: StateFlow<LearnerStage> = _currentStage.asStateFlow()

    val dailyScheduleList: StateFlow<List<DailyScheduleWithTemplate>> =
        scheduleRepository.dailyScheduleWithTemplates
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _activeSchedule = MutableStateFlow<DailyScheduleWithTemplate?>(null)
    val activeSchedule: StateFlow<DailyScheduleWithTemplate?> = _activeSchedule.asStateFlow()

    private val _currentStepIndex = MutableStateFlow(0)
    val currentStepIndex: StateFlow<Int> = _currentStepIndex.asStateFlow()

    private val _checkedItems = MutableStateFlow<Set<String>>(emptySet())
    val checkedItems: StateFlow<Set<String>> = _checkedItems.asStateFlow()

    private val _isHelpDialogOpen = MutableStateFlow(false)
    val isHelpDialogOpen: StateFlow<Boolean> = _isHelpDialogOpen.asStateFlow()

    val isSpeaking: StateFlow<Boolean> = ttsHelper.isSpeaking
    val ttsFeedback: StateFlow<String?> = ttsHelper.lastError

    init {
        // Automatically sync active schedule from database
        viewModelScope.launch {
            dailyScheduleList.collect { list ->
                if (_activeSchedule.value == null && list.isNotEmpty()) {
                    val inProgress = list.firstOrNull { it.schedule.status == ScheduleStatus.IN_PROGRESS }
                        ?: list.firstOrNull { it.schedule.status == ScheduleStatus.PENDING }
                        ?: list.first()
                    _activeSchedule.value = inProgress
                    _currentStepIndex.value = inProgress.schedule.currentStepIndex
                } else if (_activeSchedule.value != null) {
                    val updated = list.firstOrNull { it.schedule.id == _activeSchedule.value?.schedule?.id }
                    if (updated != null) {
                        _activeSchedule.value = updated
                    }
                }
            }
        }
    }

    fun selectTaskFromToday(item: DailyScheduleWithTemplate) {
        _activeSchedule.value = item
        _currentStepIndex.value = item.schedule.currentStepIndex
        _checkedItems.value = emptySet()
        _currentStage.value = LearnerStage.NOW
    }

    fun startActiveTask() {
        val active = _activeSchedule.value ?: return
        viewModelScope.launch {
            scheduleRepository.startTask(active.schedule.id)
            _currentStepIndex.value = 0
            _checkedItems.value = emptySet()
            _currentStage.value = LearnerStage.HOW
            // Auto read step 1 instruction aloud
            val firstStep = active.templateWithDetails.sortedSteps.firstOrNull()
            firstStep?.let { ttsHelper.speak(it.instruction) }
        }
    }

    fun nextStep() {
        val active = _activeSchedule.value ?: return
        val steps = active.templateWithDetails.sortedSteps
        if (_currentStepIndex.value < steps.size - 1) {
            val nextIndex = _currentStepIndex.value + 1
            _currentStepIndex.value = nextIndex
            viewModelScope.launch {
                scheduleRepository.updateStepIndex(active.schedule.id, nextIndex)
            }
            // Auto read next step
            val step = steps[nextIndex]
            ttsHelper.speak(step.instruction)
        } else {
            // Final step completed -> transition to CHECK
            _currentStage.value = LearnerStage.CHECK
            viewModelScope.launch {
                scheduleRepository.markChecking(active.schedule.id)
            }
            ttsHelper.speak("Check your work. Verify all items on the checklist.")
        }
    }

    fun prevStep() {
        if (_currentStepIndex.value > 0) {
            val prevIndex = _currentStepIndex.value - 1
            _currentStepIndex.value = prevIndex
            val active = _activeSchedule.value
            active?.let {
                val step = it.templateWithDetails.sortedSteps[prevIndex]
                ttsHelper.speak(step.instruction)
                viewModelScope.launch {
                    scheduleRepository.updateStepIndex(it.schedule.id, prevIndex)
                }
            }
        } else {
            _currentStage.value = LearnerStage.NOW
        }
    }

    fun toggleCheckItem(itemId: String) {
        val current = _checkedItems.value.toMutableSet()
        if (current.contains(itemId)) {
            current.remove(itemId)
        } else {
            current.add(itemId)
        }
        _checkedItems.value = current
    }

    fun isChecklistComplete(): Boolean {
        val active = _activeSchedule.value ?: return false
        val allItemIds = active.templateWithDetails.sortedChecklistItems.map { it.id }.toSet()
        return allItemIds.isNotEmpty() && _checkedItems.value.containsAll(allItemIds)
    }

    fun proceedToDone() {
        if (!isChecklistComplete()) return
        _currentStage.value = LearnerStage.DONE
        val phrase = _activeSchedule.value?.templateWithDetails?.template?.completionPhrase
            ?: "I have finished packing this parcel. Please check."
        ttsHelper.speak("Task complete! $phrase")
    }

    fun confirmAndGoToNext() {
        val active = _activeSchedule.value ?: return
        viewModelScope.launch {
            scheduleRepository.markCompleted(active.schedule.id)
            _currentStage.value = LearnerStage.NEXT
        }
    }

    fun startNextTask(nextScheduleItem: DailyScheduleWithTemplate) {
        _activeSchedule.value = nextScheduleItem
        _currentStepIndex.value = 0
        _checkedItems.value = emptySet()
        _currentStage.value = LearnerStage.NOW
    }

    fun returnToToday() {
        _checkedItems.value = emptySet()
        _currentStage.value = LearnerStage.TODAY
    }

    fun jumpToStage(stage: LearnerStage) {
        _currentStage.value = stage
    }

    fun openHelpDialog() {
        _isHelpDialogOpen.value = true
        val phrase = _activeSchedule.value?.templateWithDetails?.template?.helpPhrase
            ?: "I need help with this step. Please assist me."
        ttsHelper.speak(phrase)
    }

    fun closeHelpDialog() {
        _isHelpDialogOpen.value = false
        ttsHelper.stop()
    }

    fun speakHelpPhrase() {
        val phrase = _activeSchedule.value?.templateWithDetails?.template?.helpPhrase
            ?: "I need help with this step. Please assist me."
        ttsHelper.speak(phrase)
    }

    fun speakCurrentStepInstruction() {
        val active = _activeSchedule.value ?: return
        val steps = active.templateWithDetails.sortedSteps
        if (_currentStepIndex.value in steps.indices) {
            ttsHelper.speak(steps[_currentStepIndex.value].instruction)
        }
    }

    fun speakCompletionPhrase() {
        val phrase = _activeSchedule.value?.templateWithDetails?.template?.completionPhrase
            ?: "I have finished packing this parcel. Please check."
        ttsHelper.speak(phrase)
    }

    override fun onCleared() {
        super.onCleared()
        ttsHelper.shutdown()
    }
}
