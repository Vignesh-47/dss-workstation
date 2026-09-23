package com.dss.workstation.ui.staff

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dss.workstation.data.model.*
import com.dss.workstation.data.repository.ScheduleRepository
import com.dss.workstation.data.repository.TaskRepository
import com.dss.workstation.util.BackupRestoreManager
import com.dss.workstation.util.StaffSecurityManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

enum class StaffTab {
    SCHEDULE_ORGANIZER,
    TEMPLATES,
    LIVE_PREVIEW,
    TASK_RECOVERY,
    BACKUP_RESTORE,
    KIOSK_SETTINGS
}

class StaffViewModel(
    private val taskRepository: TaskRepository,
    private val scheduleRepository: ScheduleRepository,
    val securityManager: StaffSecurityManager,
    private val backupRestoreManager: BackupRestoreManager
) : ViewModel() {

    private val _currentTab = MutableStateFlow(StaffTab.SCHEDULE_ORGANIZER)
    val currentTab: StateFlow<StaffTab> = _currentTab.asStateFlow()

    val allTemplatesWithDetails: StateFlow<List<TaskTemplateWithDetails>> =
        taskRepository.allTemplatesWithDetails
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dailyScheduleList: StateFlow<List<DailyScheduleWithTemplate>> =
        scheduleRepository.dailyScheduleWithTemplates
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Currently editing template (if in editor)
    private val _editingTemplate = MutableStateFlow<TaskTemplateWithDetails?>(null)
    val editingTemplate: StateFlow<TaskTemplateWithDetails?> = _editingTemplate.asStateFlow()

    // Backup & Restore status message
    private val _backupStatus = MutableStateFlow<String?>(null)
    val backupStatus: StateFlow<String?> = _backupStatus.asStateFlow()

    private val _isLockTaskActive = MutableStateFlow(false)
    val isLockTaskActive: StateFlow<Boolean> = _isLockTaskActive.asStateFlow()

    fun selectTab(tab: StaffTab) {
        _currentTab.value = tab
    }

    // --- Schedule Organizer Functions ---
    fun addTemplateToQueue(templateId: String, customTitle: String? = null) {
        viewModelScope.launch {
            var targetTemplateId = templateId
            val trimmedTitle = customTitle?.trim()
            if (!trimmedTitle.isNullOrEmpty()) {
                val existing = taskRepository.getTemplateWithDetails(templateId)
                if (existing != null && existing.template.title != trimmedTitle) {
                    val newId = UUID.randomUUID().toString()
                    val newTemplate = existing.template.copy(id = newId, title = trimmedTitle)
                    val newSteps = existing.sortedSteps.map { it.copy(id = UUID.randomUUID().toString(), templateId = newId) }
                    val newChecklist = existing.sortedChecklistItems.map { it.copy(id = UUID.randomUUID().toString(), templateId = newId) }
                    taskRepository.saveTemplate(newTemplate, newSteps, newChecklist)
                    targetTemplateId = newId
                }
            }
            val count = dailyScheduleList.value.size
            scheduleRepository.addToSchedule(targetTemplateId, count)
        }
    }

    fun renameScheduleItem(scheduleId: String, newTitle: String) {
        val trimmed = newTitle.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch {
            val item = dailyScheduleList.value.find { it.schedule.id == scheduleId } ?: return@launch
            val existingTemplate = item.templateWithDetails
            if (existingTemplate.template.title != trimmed) {
                val newId = UUID.randomUUID().toString()
                val newTemplate = existingTemplate.template.copy(id = newId, title = trimmed)
                val newSteps = existingTemplate.sortedSteps.map { it.copy(id = UUID.randomUUID().toString(), templateId = newId) }
                val newChecklist = existingTemplate.sortedChecklistItems.map { it.copy(id = UUID.randomUUID().toString(), templateId = newId) }
                taskRepository.saveTemplate(newTemplate, newSteps, newChecklist)
                scheduleRepository.updateScheduleItem(item.schedule.copy(templateId = newId))
            }
        }
    }

    fun removeScheduleItem(scheduleId: String) {
        viewModelScope.launch {
            scheduleRepository.removeScheduleItem(scheduleId)
        }
    }

    fun moveScheduleItemUp(index: Int) {
        if (index <= 0) return
        val currentList = dailyScheduleList.value.map { it.schedule }.toMutableList()
        val item = currentList.removeAt(index)
        currentList.add(index - 1, item)
        viewModelScope.launch {
            scheduleRepository.reorderSchedule(currentList)
        }
    }

    fun moveScheduleItemDown(index: Int) {
        val currentList = dailyScheduleList.value.map { it.schedule }.toMutableList()
        if (index >= currentList.size - 1) return
        val item = currentList.removeAt(index)
        currentList.add(index + 1, item)
        viewModelScope.launch {
            scheduleRepository.reorderSchedule(currentList)
        }
    }

    fun clearDailySchedule() {
        viewModelScope.launch {
            scheduleRepository.clearDailySchedule()
        }
    }

    // --- Template Builder Functions ---
    fun startNewTemplate() {
        val newId = UUID.randomUUID().toString()
        _editingTemplate.value = TaskTemplateWithDetails(
            template = TaskTemplateEntity(
                id = newId,
                title = "New Packing Template",
                description = "",
                estimatedMinutes = 10,
                helpPhrase = "I need help with this step. Please assist me.",
                completionPhrase = "I have finished packing this parcel. Please check."
            ),
            steps = listOf(
                TaskStepEntity(
                    id = UUID.randomUUID().toString(),
                    templateId = newId,
                    stepOrder = 1,
                    instruction = "Step 1 instruction..."
                )
            ),
            checklistItems = listOf(
                ChecklistItemEntity(
                    id = UUID.randomUUID().toString(),
                    templateId = newId,
                    itemOrder = 1,
                    checkPrompt = "Item is verified"
                )
            )
        )
    }

    fun editTemplate(template: TaskTemplateWithDetails) {
        _editingTemplate.value = template
    }

    fun cancelTemplateEditing() {
        _editingTemplate.value = null
    }

    fun saveEditingTemplate(
        title: String,
        description: String,
        estimatedMinutes: Int,
        helpPhrase: String,
        completionPhrase: String,
        steps: List<TaskStepEntity>,
        checklist: List<ChecklistItemEntity>
    ) {
        val current = _editingTemplate.value ?: return
        viewModelScope.launch {
            val updatedTemplate = current.template.copy(
                title = title,
                description = description,
                estimatedMinutes = estimatedMinutes,
                helpPhrase = helpPhrase,
                completionPhrase = completionPhrase
            )
            taskRepository.saveTemplate(updatedTemplate, steps, checklist)
            _editingTemplate.value = null
        }
    }

    fun duplicateTemplate(templateId: String) {
        viewModelScope.launch {
            taskRepository.duplicateTemplate(templateId)
        }
    }

    fun deleteTemplate(templateId: String) {
        viewModelScope.launch {
            taskRepository.deleteTemplate(templateId)
        }
    }

    // --- Task Recovery & Reset Functions ---
    fun resetScheduleToStep1(scheduleId: String) {
        viewModelScope.launch {
            scheduleRepository.resetTaskToStep(scheduleId, 0)
        }
    }

    fun jumpScheduleToStep(scheduleId: String, stepIndex: Int) {
        viewModelScope.launch {
            scheduleRepository.resetTaskToStep(scheduleId, stepIndex)
        }
    }

    fun forceCompleteSchedule(scheduleId: String) {
        viewModelScope.launch {
            scheduleRepository.forceCompleteTask(scheduleId)
        }
    }

    // --- Backup & Restore Functions ---
    fun exportBackup(uri: Uri) {
        viewModelScope.launch {
            _backupStatus.value = "Exporting workstation data bundle..."
            val result = backupRestoreManager.exportBundle(uri)
            if (result.isSuccess) {
                _backupStatus.value = "Backup successfully exported (${result.getOrNull()} templates)."
            } else {
                _backupStatus.value = "Export failed: ${result.exceptionOrNull()?.message}"
            }
        }
    }

    fun importBackup(uri: Uri) {
        viewModelScope.launch {
            _backupStatus.value = "Importing .dssbundle archive..."
            val result = backupRestoreManager.importBundle(uri)
            if (result.isSuccess) {
                _backupStatus.value = "Successfully restored ${result.getOrNull()} templates and photos."
            } else {
                _backupStatus.value = "Import failed: ${result.exceptionOrNull()?.message}"
            }
        }
    }

    fun clearBackupStatus() {
        _backupStatus.value = null
    }

    fun setLockTaskActive(active: Boolean) {
        _isLockTaskActive.value = active
    }
}
