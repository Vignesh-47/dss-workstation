package com.dss.workstation.data.repository

import com.dss.workstation.data.dao.ScheduleDao
import com.dss.workstation.data.model.DailyScheduleEntity
import com.dss.workstation.data.model.DailyScheduleWithTemplate
import com.dss.workstation.data.model.ScheduleStatus
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class ScheduleRepository(private val scheduleDao: ScheduleDao) {

    val dailyScheduleWithTemplates: Flow<List<DailyScheduleWithTemplate>> =
        scheduleDao.getScheduleWithTemplates()

    val activeSchedule: Flow<DailyScheduleWithTemplate?> =
        scheduleDao.getActiveScheduleWithTemplate()

    suspend fun getScheduleSync(scheduleId: String): DailyScheduleWithTemplate? =
        scheduleDao.getScheduleWithTemplateSync(scheduleId)

    suspend fun startTask(scheduleId: String) {
        val allSchedules = scheduleDao.getScheduleWithTemplateSync(scheduleId) ?: return
        // Pause any other in-progress task
        scheduleDao.updateScheduleStatus(scheduleId, ScheduleStatus.IN_PROGRESS)
    }

    suspend fun updateStepIndex(scheduleId: String, stepIndex: Int) {
        scheduleDao.updateStepIndex(scheduleId, stepIndex)
    }

    suspend fun markChecking(scheduleId: String) {
        scheduleDao.updateScheduleStatus(scheduleId, ScheduleStatus.CHECKING)
    }

    suspend fun markCompleted(scheduleId: String) {
        scheduleDao.updateScheduleStatus(scheduleId, ScheduleStatus.COMPLETED)
    }

    suspend fun resetTaskToStep(scheduleId: String, stepIndex: Int) {
        scheduleDao.updateStepIndex(scheduleId, stepIndex)
        scheduleDao.updateScheduleStatus(scheduleId, ScheduleStatus.IN_PROGRESS)
    }

    suspend fun forceCompleteTask(scheduleId: String) {
        scheduleDao.updateScheduleStatus(scheduleId, ScheduleStatus.COMPLETED)
    }

    suspend fun addToSchedule(templateId: String, currentCount: Int) {
        val newSchedule = DailyScheduleEntity(
            id = UUID.randomUUID().toString(),
            templateId = templateId,
            queueOrder = currentCount + 1,
            status = ScheduleStatus.PENDING,
            currentStepIndex = 0
        )
        scheduleDao.insertSchedule(newSchedule)
    }

    suspend fun removeScheduleItem(scheduleId: String) {
        scheduleDao.deleteScheduleById(scheduleId)
    }

    suspend fun updateScheduleItem(schedule: DailyScheduleEntity) {
        scheduleDao.updateSchedule(schedule)
    }

    suspend fun reorderSchedule(updatedList: List<DailyScheduleEntity>) {
        val reordered = updatedList.mapIndexed { index, item ->
            item.copy(queueOrder = index + 1)
        }
        scheduleDao.replaceDailySchedule(reordered)
    }

    suspend fun clearDailySchedule() {
        scheduleDao.clearSchedule()
    }
}
