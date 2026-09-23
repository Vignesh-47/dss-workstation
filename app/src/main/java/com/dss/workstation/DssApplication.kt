package com.dss.workstation

import android.app.Application
import com.dss.workstation.data.database.AppDatabase
import com.dss.workstation.data.repository.ScheduleRepository
import com.dss.workstation.data.repository.TaskRepository
import com.dss.workstation.util.BackupRestoreManager
import com.dss.workstation.util.PhotoStorageManager
import com.dss.workstation.util.StaffSecurityManager
import com.dss.workstation.util.TtsHelper

class DssApplication : Application() {

    val database by lazy { AppDatabase.getInstance(this) }
    val taskRepository by lazy { TaskRepository(database.taskDao()) }
    val scheduleRepository by lazy { ScheduleRepository(database.scheduleDao()) }
    val securityManager by lazy { StaffSecurityManager(this) }
    val photoStorageManager by lazy { PhotoStorageManager(this) }
    val ttsHelper by lazy { TtsHelper(this) }
    val backupRestoreManager by lazy {
        BackupRestoreManager(this, database.taskDao(), database.scheduleDao())
    }

    override fun onCreate() {
        super.onCreate()
        // Warm up database and photo storage directory
        database.openHelper.writableDatabase
        photoStorageManager.photosDir
    }
}
