package com.lockedin.core.utils

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import java.io.File
import java.text.DecimalFormat

object FileUtils {

    /**
     * Extracts the display name of a file from a content URI.
     */
    fun getFileNameFromUri(context: Context, uri: Uri): String? {
        var name: String? = null
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex >= 0 && cursor.moveToFirst()) {
                name = cursor.getString(nameIndex)
            }
        }
        return name
    }

    /**
     * Formats file size in bytes to a human-readable string.
     */
    fun formatFileSize(sizeBytes: Long): String {
        if (sizeBytes <= 0) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB")
        val digitGroups = (Math.log10(sizeBytes.toDouble()) / Math.log10(1024.0)).toInt()
        val formatter = DecimalFormat("#,##0.#")
        return "${formatter.format(sizeBytes / Math.pow(1024.0, digitGroups.toDouble()))} ${units[digitGroups]}"
    }

    /**
     * Returns the MIME type icon description for a given MIME type.
     */
    fun getMimeTypeCategory(mimeType: String): FileCategory {
        return when {
            mimeType.startsWith("image/") -> FileCategory.IMAGE
            mimeType == "application/pdf" -> FileCategory.PDF
            mimeType.contains("word") || mimeType.contains("document") -> FileCategory.DOCUMENT
            mimeType.startsWith("text/") -> FileCategory.TEXT
            else -> FileCategory.OTHER
        }
    }

    /**
     * Calculates total size of all files in a directory recursively.
     */
    fun getDirectorySize(directory: File): Long {
        var size = 0L
        if (directory.exists()) {
            directory.walkTopDown().forEach { file ->
                if (file.isFile) {
                    size += file.length()
                }
            }
        }
        return size
    }
}

enum class FileCategory {
    IMAGE, PDF, DOCUMENT, TEXT, OTHER
}
