package com.lockedin.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.lockedin.data.db.dao.ChatMessageDao
import com.lockedin.data.db.dao.FileDao
import com.lockedin.data.db.dao.NoteDao
import com.lockedin.data.db.entity.ChatMessageEntity
import com.lockedin.data.db.entity.FileEntity
import com.lockedin.data.db.entity.NoteEntity

@Database(
    entities = [FileEntity::class, NoteEntity::class, ChatMessageEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun fileDao(): FileDao
    abstract fun noteDao(): NoteDao
    abstract fun chatMessageDao(): ChatMessageDao
}
