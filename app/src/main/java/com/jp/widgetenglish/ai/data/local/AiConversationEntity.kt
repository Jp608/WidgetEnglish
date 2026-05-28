package com.jp.widgetenglish.ai.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ai_conversations")
data class AiConversationEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val summary: String,
    val createdAt: Long,
    val updatedAt: Long
)