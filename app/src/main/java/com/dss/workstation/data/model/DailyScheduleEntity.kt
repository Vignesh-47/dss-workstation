package com.dss.workstation.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "daily_schedule",
    foreignKeys = [
        ForeignKey(
            entity = TaskTemplateEntity::class,
            parentColumns = ["id"],
            childColumns = ["templateId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("templateId")]
)
data class DailyScheduleEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val templateId: String,
    val queueOrder: Int,
    val status: ScheduleStatus = ScheduleStatus.PENDING,
    val currentStepIndex: Int = 0
)
