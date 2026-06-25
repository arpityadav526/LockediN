package com.lockedin.data.repository

import android.content.Context
import android.net.Uri
import com.lockedin.core.utils.FileUtils
import com.lockedin.data.db.dao.FileDao
import com.lockedin.data.db.entity.FileEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FileRepository @Inject constructor(
    private val fileDao: FileDao,
    @ApplicationContext private val context: Context
) {
    fun getAllFiles(): Flow<List<FileEntity>> = fileDao.getAllFiles()

    suspend fun getFileById(id: Long): FileEntity? = fileDao.getFileById(id)

    suspend fun deleteFile(file: FileEntity) {
        // Delete actual file from disk
        val diskFile = File(file.path)
        if (diskFile.exists()) {
            diskFile.delete()
        }
        // Delete from database
        fileDao.delete(file)
    }

    suspend fun deleteAllFiles() {
        // Delete homework directory
        withContext(Dispatchers.IO) {
            val homeworkDir = File(context.filesDir, "homework")
            if (homeworkDir.exists()) {
                homeworkDir.deleteRecursively()
            }
        }
        fileDao.deleteAll()
    }

    suspend fun saveFileFromUri(uri: Uri, mimeType: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val fileName = FileUtils.getFileNameFromUri(context, uri)
                    ?: "${System.currentTimeMillis()}.file"
                val destDir = File(context.filesDir, "homework").also { it.mkdirs() }
                val destFile = File(destDir, fileName)

                // Handle duplicate file names
                val finalFile = if (destFile.exists()) {
                    val nameWithoutExt = fileName.substringBeforeLast(".")
                    val ext = fileName.substringAfterLast(".", "")
                    val newName = "${nameWithoutExt}_${System.currentTimeMillis()}" +
                            if (ext.isNotEmpty()) ".$ext" else ""
                    File(destDir, newName)
                } else {
                    destFile
                }

                context.contentResolver.openInputStream(uri)?.use { input ->
                    FileOutputStream(finalFile).use { output ->
                        input.copyTo(output)
                    }
                }

                val entity = FileEntity(
                    name = finalFile.name,
                    path = finalFile.absolutePath,
                    mimeType = mimeType,
                    sizeBytes = finalFile.length(),
                    receivedAt = System.currentTimeMillis()
                )
                fileDao.insert(entity)
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }

    fun getHomeworkDirectorySize(): Long {
        val homeworkDir = File(context.filesDir, "homework")
        return FileUtils.getDirectorySize(homeworkDir)
    }
}
