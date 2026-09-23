package com.dss.workstation.data.repository

import com.dss.workstation.data.dao.TaskDao
import com.dss.workstation.data.model.ChecklistItemEntity
import com.dss.workstation.data.model.TaskStepEntity
import com.dss.workstation.data.model.TaskTemplateEntity
import com.dss.workstation.data.model.TaskTemplateWithDetails
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class TaskRepository(private val taskDao: TaskDao) {

    val allTemplatesWithDetails: Flow<List<TaskTemplateWithDetails>> =
        taskDao.getAllTemplatesWithDetails()

    fun getTemplateWithDetails(templateId: String): Flow<TaskTemplateWithDetails?> =
        taskDao.getTemplateWithDetails(templateId)

    suspend fun getTemplateWithDetailsSync(templateId: String): TaskTemplateWithDetails? =
        taskDao.getTemplateWithDetailsSync(templateId)

    suspend fun saveTemplate(
        template: TaskTemplateEntity,
        steps: List<TaskStepEntity>,
        checklist: List<ChecklistItemEntity>
    ) {
        taskDao.saveTemplateWithDetails(template, steps, checklist)
    }

    suspend fun deleteTemplate(templateId: String) {
        taskDao.deleteTemplateById(templateId)
    }

    suspend fun duplicateTemplate(templateId: String): String {
        val original = taskDao.getTemplateWithDetailsSync(templateId) ?: return ""
        val newTemplateId = UUID.randomUUID().toString()
        val duplicatedTemplate = original.template.copy(
            id = newTemplateId,
            title = "${original.template.title} (Copy)",
            createdAt = System.currentTimeMillis()
        )
        val duplicatedSteps = original.sortedSteps.map { step ->
            step.copy(
                id = UUID.randomUUID().toString(),
                templateId = newTemplateId
            )
        }
        val duplicatedChecklist = original.sortedChecklistItems.map { item ->
            item.copy(
                id = UUID.randomUUID().toString(),
                templateId = newTemplateId
            )
        }
        taskDao.saveTemplateWithDetails(duplicatedTemplate, duplicatedSteps, duplicatedChecklist)
        return newTemplateId
    }
}
