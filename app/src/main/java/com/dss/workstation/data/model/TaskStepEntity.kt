package com.dss.workstation.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "task_steps",
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
data class TaskStepEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val templateId: String,
    val stepOrder: Int,
    val instruction: String,
    val imagePath: String? = null // Relative path inside app's internal filesDir or drawable resource name
)
