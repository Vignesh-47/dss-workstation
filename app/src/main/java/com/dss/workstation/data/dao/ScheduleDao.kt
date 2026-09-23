package com.dss.workstation.data.dao

import androidx.room.*
import com.dss.workstation.data.model.DailyScheduleEntity
import com.dss.workstation.data.model.DailyScheduleWithTemplate
import com.dss.workstation.data.model.ScheduleStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduleDao {

    @Transaction
    @Query("SELECT * FROM daily_schedule ORDER BY queueOrder ASC")
    fun getScheduleWithTemplates(): Flow<List<DailyScheduleWithTemplate>>

    @Query("SELECT * FROM daily_schedule ORDER BY queueOrder ASC")
    fun getDailySchedules(): Flow<List<DailyScheduleEntity>>

    @Transaction
    @Query("SELECT * FROM daily_schedule WHERE status = 'IN_PROGRESS' LIMIT 1")
    fun getActiveScheduleWithTemplate(): Flow<DailyScheduleWithTemplate?>

    @Transaction
    @Query("SELECT * FROM daily_schedule WHERE id = :scheduleId")
    suspend fun getScheduleWithTemplateSync(scheduleId: String): DailyScheduleWithTemplate?

    @Query("SELECT * FROM daily_schedule WHERE id = :scheduleId")
    suspend fun getScheduleById(scheduleId: String): DailyScheduleEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedule(schedule: DailyScheduleEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedules(schedules: List<DailyScheduleEntity>)

    @Update
    suspend fun updateSchedule(schedule: DailyScheduleEntity)

    @Query("UPDATE daily_schedule SET status = :status WHERE id = :id")
    suspend fun updateScheduleStatus(id: String, status: ScheduleStatus)

    @Query("UPDATE daily_schedule SET currentStepIndex = :stepIndex WHERE id = :id")
    suspend fun updateStepIndex(id: String, stepIndex: Int)

    @Delete
    suspend fun deleteSchedule(schedule: DailyScheduleEntity)

    @Query("DELETE FROM daily_schedule WHERE id = :id")
    suspend fun deleteScheduleById(id: String)

    @Query("DELETE FROM daily_schedule")
    suspend fun clearSchedule()

    @Transaction
    suspend fun replaceDailySchedule(schedules: List<DailyScheduleEntity>) {
        clearSchedule()
        insertSchedules(schedules)
    }
}
