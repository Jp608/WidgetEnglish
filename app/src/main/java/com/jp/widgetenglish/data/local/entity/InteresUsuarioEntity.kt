package com.jp.widgetenglish.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "intereses_usuario")
data class InteresUsuarioEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val usuarioId: String,
    val interes: String, // Ej: "Fútbol", "Videojuegos", "Música"
    val frecuencia: Int = 1
)
