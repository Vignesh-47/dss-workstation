package com.dss.workstation.data.model

import androidx.room.Embedded
import androidx.room.Relation

data class TaskTemplateWithDetails(
    @Embedded val template: TaskTemplateEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "templateId"
    )
    val steps: List<TaskStepEntity> = emptyList(),
    @Relation(
        parentColumn = "id",
        entityColumn = "templateId"
    )
    val checklistItems: List<ChecklistItemEntity> = emptyList()
) {
    val sortedSteps: List<TaskStepEntity>
        get() = steps.sortedBy { it.stepOrder }

    val sortedChecklistItems: List<ChecklistItemEntity>
        get() = checklistItems.sortedBy { it.itemOrder }
}
