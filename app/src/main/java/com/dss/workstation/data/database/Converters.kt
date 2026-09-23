package com.dss.workstation.data.database

import androidx.room.TypeConverter
import com.dss.workstation.data.model.ScheduleStatus

class Converters {
    @TypeConverter
    fun fromScheduleStatus(value: ScheduleStatus?): String {
        return value?.name ?: ScheduleStatus.PENDING.name
    }

    @TypeConverter
    fun toScheduleStatus(value: String?): ScheduleStatus {
        return try {
            if (value != null) ScheduleStatus.valueOf(value) else ScheduleStatus.PENDING
        } catch (e: Exception) {
            ScheduleStatus.PENDING
        }
    }
}
