package com.jp.widgetenglish.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_sessions")
data class ChatSessionEntity(
    @PrimaryKey
    val id: String,
    val titulo: String,
    val resumen: String? = null,
    val fecha: Long = System.currentTimeMillis(),
    val ultimaInteraccion: Long = System.currentTimeMillis()
)
