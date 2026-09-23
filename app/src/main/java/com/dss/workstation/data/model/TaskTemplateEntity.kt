package com.dss.workstation.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "task_templates")
data class TaskTemplateEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String = "",
    val estimatedMinutes: Int = 10,
    val helpPhrase: String = "I need help with this step. Please assist me.",
    val completionPhrase: String = "I have finished packing this parcel. Please check.",
    val createdAt: Long = System.currentTimeMillis()
)
