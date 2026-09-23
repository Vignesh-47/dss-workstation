package com.dss.workstation.util

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.UUID

class PhotoStorageManager(private val context: Context) {

    val photosDir: File
        get() {
            val dir = File(context.filesDir, "task_photos")
            if (!dir.exists()) {
                dir.mkdirs()
            }
            return dir
        }

    fun createTempCameraPhotoUri(): Pair<Uri, File> {
        val fileName = "camera_${UUID.randomUUID()}.jpg"
        val photoFile = File(photosDir, fileName)
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            photoFile
        )
        return Pair(uri, photoFile)
    }

    fun copyUriToInternalStorage(sourceUri: Uri): String? {
        return try {
            val fileName = "photo_${UUID.randomUUID()}.jpg"
            val targetFile = File(photosDir, fileName)

            context.contentResolver.openInputStream(sourceUri)?.use { input: InputStream ->
                FileOutputStream(targetFile).use { output ->
                    input.copyTo(output)
                }
            }
            "task_photos/$fileName"
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun getFileFromRelativePath(relativePath: String): File? {
        val file = File(context.filesDir, relativePath)
        return if (file.exists()) file else null
    }

    fun deletePhoto(relativePath: String): Boolean {
        if (!relativePath.startsWith("task_photos/")) return false
        val file = File(context.filesDir, relativePath)
        return if (file.exists()) file.delete() else false
    }

    /**
     * Helper to resolve path for UI:
     * - If starts with "res:drawable/", returns the drawable resource ID
     * - If relative path in filesDir, returns File
     */
    fun resolveDrawableResId(path: String?): Int? {
        if (path == null) return null
        if (path.startsWith("res:drawable/")) {
            val resName = path.removePrefix("res:drawable/")
            val resId = context.resources.getIdentifier(resName, "drawable", context.packageName)
            if (resId != 0) return resId
        }
        return null
    }
}
