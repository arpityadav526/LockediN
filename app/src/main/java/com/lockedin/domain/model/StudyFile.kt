package com.lockedin.domain.model

data class StudyFile(
    val id: Long,
    val name: String,
    val path: String,
    val mimeType: String,
    val sizeBytes: Long,
    val receivedAt: Long
)
