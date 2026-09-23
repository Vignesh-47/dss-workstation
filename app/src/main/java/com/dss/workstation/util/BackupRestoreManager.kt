package com.dss.workstation.util

import android.content.Context
import android.net.Uri
import com.dss.workstation.data.dao.ScheduleDao
import com.dss.workstation.data.dao.TaskDao
import com.dss.workstation.data.model.ChecklistItemEntity
import com.dss.workstation.data.model.DailyScheduleEntity
import com.dss.workstation.data.model.TaskStepEntity
import com.dss.workstation.data.model.TaskTemplateEntity
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.*
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

data class DssBackupData(
    val version: Int = 1,
    val exportedAt: Long = System.currentTimeMillis(),
    val templates: List<TaskTemplateEntity>,
    val steps: List<TaskStepEntity>,
    val checklistItems: List<ChecklistItemEntity>,
    val schedules: List<DailyScheduleEntity>
)

class BackupRestoreManager(
    private val context: Context,
    private val taskDao: TaskDao,
    private val scheduleDao: ScheduleDao
) {
    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()

    suspend fun exportBundle(destinationUri: Uri): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val allTemplatesWithDetails = taskDao.getAllTemplatesWithDetails().first()
            val allSchedules = scheduleDao.getDailySchedules().first()

            val templates = allTemplatesWithDetails.map { it.template }
            val steps = allTemplatesWithDetails.flatMap { it.steps }
            val checklist = allTemplatesWithDetails.flatMap { it.checklistItems }

            val backupData = DssBackupData(
                templates = templates,
                steps = steps,
                checklistItems = checklist,
                schedules = allSchedules
            )

            val outputStream = context.contentResolver.openOutputStream(destinationUri)
                ?: return@withContext Result.failure(IOException("Could not open destination URI for writing."))

            ZipOutputStream(BufferedOutputStream(outputStream)).use { zipOut ->
                // 1. Write data.json
                val jsonEntry = ZipEntry("data.json")
                zipOut.putNextEntry(jsonEntry)
                val jsonBytes = gson.toJson(backupData).toByteArray(Charsets.UTF_8)
                zipOut.write(jsonBytes)
                zipOut.closeEntry()

                // 2. Write referenced local photo files
                val photoFilesDir = File(context.filesDir, "task_photos")
                if (photoFilesDir.exists()) {
                    val files = photoFilesDir.listFiles() ?: emptyArray()
                    for (file in files) {
                        if (file.isFile) {
                            val photoEntry = ZipEntry("task_photos/${file.name}")
                            zipOut.putNextEntry(photoEntry)
                            FileInputStream(file).use { fileIn ->
                                fileIn.copyTo(zipOut)
                            }
                            zipOut.closeEntry()
                        }
                    }
                }
            }
            Result.success(templates.size)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun importBundle(sourceUri: Uri): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(sourceUri)
                ?: return@withContext Result.failure(IOException("Could not open source URI for reading."))

            var backupData: DssBackupData? = null
            val photoFilesDir = File(context.filesDir, "task_photos")
            if (!photoFilesDir.exists()) photoFilesDir.mkdirs()

            ZipInputStream(BufferedInputStream(inputStream)).use { zipIn ->
                var entry: ZipEntry? = zipIn.nextEntry
                while (entry != null) {
                    val entryName = entry.name
                    if (entryName == "data.json") {
                        val reader = InputStreamReader(zipIn, Charsets.UTF_8)
                        backupData = gson.fromJson(reader, DssBackupData::class.java)
                    } else if (entryName.startsWith("task_photos/") && !entry.isDirectory) {
                        val targetFile = File(context.filesDir, entryName)
                        targetFile.parentFile?.mkdirs()
                        FileOutputStream(targetFile).use { fileOut ->
                            zipIn.copyTo(fileOut)
                        }
                    }
                    zipIn.closeEntry()
                    entry = zipIn.nextEntry
                }
            }

            val data = backupData ?: return@withContext Result.failure(
                IllegalArgumentException("Invalid .dssbundle: data.json not found in archive.")
            )

            // Restore records into Room DB
            for (template in data.templates) {
                val templateSteps = data.steps.filter { it.templateId == template.id }
                val templateChecklist = data.checklistItems.filter { it.templateId == template.id }
                taskDao.saveTemplateWithDetails(template, templateSteps, templateChecklist)
            }

            if (data.schedules.isNotEmpty()) {
                scheduleDao.replaceDailySchedule(data.schedules)
            }

            Result.success(data.templates.size)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}
