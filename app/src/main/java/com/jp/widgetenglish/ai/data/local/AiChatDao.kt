package com.jp.widgetenglish.ai.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AiChatDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversation(conversation: AiConversationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: AiMessageEntity)

    @Query("""
        SELECT * FROM ai_conversations
        ORDER BY updatedAt DESC
    """)
    fun getConversations(): Flow<List<AiConversationEntity>>

    @Query("""
        SELECT * FROM ai_messages
        WHERE conversationId = :conversationId
        ORDER BY createdAt ASC
    """)
    fun getMessagesByConversation(conversationId: String): Flow<List<AiMessageEntity>>

    @Query("""
        UPDATE ai_conversations
        SET title = :title,
            summary = :summary,
            updatedAt = :updatedAt
        WHERE id = :conversationId
    """)
    suspend fun updateConversationInfo(
        conversationId: String,
        title: String,
        summary: String,
        updatedAt: Long
    )

    @Query("DELETE FROM ai_messages WHERE conversationId = :conversationId")
    suspend fun deleteMessagesByConversation(conversationId: String)

    @Query("DELETE FROM ai_conversations WHERE id = :conversationId")
    suspend fun deleteConversation(conversationId: String)
}