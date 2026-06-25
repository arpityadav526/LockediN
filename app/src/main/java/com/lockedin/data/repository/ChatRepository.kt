package com.lockedin.data.repository

import com.lockedin.data.db.dao.ChatMessageDao
import com.lockedin.data.db.entity.ChatMessageEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepository @Inject constructor(
    private val chatMessageDao: ChatMessageDao
) {
    fun getAllMessages(): Flow<List<ChatMessageEntity>> = chatMessageDao.getAllMessages()

    fun getRecentMessages(limit: Int = 50): Flow<List<ChatMessageEntity>> =
        chatMessageDao.getRecentMessages(limit).map { it.reversed() }

    suspend fun insertMessage(message: ChatMessageEntity): Long =
        chatMessageDao.insert(message)

    suspend fun clearAllMessages() = chatMessageDao.deleteAll()

    suspend fun getMessageCount(): Int = chatMessageDao.getMessageCount()
}
