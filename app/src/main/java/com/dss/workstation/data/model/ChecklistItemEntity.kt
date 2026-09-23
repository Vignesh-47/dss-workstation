package com.dss.workstation.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "checklist_items",
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
data class ChecklistItemEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val templateId: String,
    val itemOrder: Int,
    val checkPrompt: String
)
