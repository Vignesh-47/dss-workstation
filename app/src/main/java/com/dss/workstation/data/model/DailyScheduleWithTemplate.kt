package com.dss.workstation.data.model

import androidx.room.Embedded
import androidx.room.Relation

data class DailyScheduleWithTemplate(
    @Embedded val schedule: DailyScheduleEntity,
    @Relation(
        entity = TaskTemplateEntity::class,
        parentColumn = "templateId",
        entityColumn = "id"
    )
    val templateWithDetails: TaskTemplateWithDetails
)
