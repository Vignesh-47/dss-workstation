package com.dss.workstation.data.dao

import androidx.room.*
import com.dss.workstation.data.model.ChecklistItemEntity
import com.dss.workstation.data.model.TaskStepEntity
import com.dss.workstation.data.model.TaskTemplateEntity
import com.dss.workstation.data.model.TaskTemplateWithDetails
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    @Query("SELECT * FROM task_templates ORDER BY createdAt DESC")
    fun getAllTemplates(): Flow<List<TaskTemplateEntity>>

    @Transaction
    @Query("SELECT * FROM task_templates ORDER BY createdAt DESC")
    fun getAllTemplatesWithDetails(): Flow<List<TaskTemplateWithDetails>>

    @Transaction
    @Query("SELECT * FROM task_templates WHERE id = :templateId")
    fun getTemplateWithDetails(templateId: String): Flow<TaskTemplateWithDetails?>

    @Transaction
    @Query("SELECT * FROM task_templates WHERE id = :templateId")
    suspend fun getTemplateWithDetailsSync(templateId: String): TaskTemplateWithDetails?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemplate(template: TaskTemplateEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSteps(steps: List<TaskStepEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChecklistItems(items: List<ChecklistItemEntity>)

    @Delete
    suspend fun deleteTemplate(template: TaskTemplateEntity)

    @Query("DELETE FROM task_templates WHERE id = :templateId")
    suspend fun deleteTemplateById(templateId: String)

    @Query("DELETE FROM task_steps WHERE templateId = :templateId")
    suspend fun deleteStepsForTemplate(templateId: String)

    @Query("DELETE FROM checklist_items WHERE templateId = :templateId")
    suspend fun deleteChecklistForTemplate(templateId: String)

    @Query("DELETE FROM task_templates")
    suspend fun deleteAllTemplates()

    @Transaction
    suspend fun saveTemplateWithDetails(
        template: TaskTemplateEntity,
        steps: List<TaskStepEntity>,
        checklist: List<ChecklistItemEntity>
    ) {
        insertTemplate(template)
        deleteStepsForTemplate(template.id)
        insertSteps(steps)
        deleteChecklistForTemplate(template.id)
        insertChecklistItems(checklist)
    }
}
