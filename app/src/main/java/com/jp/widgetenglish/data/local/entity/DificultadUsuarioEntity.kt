package com.jp.widgetenglish.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "dificultades_usuario")
data class DificultadUsuarioEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val usuarioId: String,
    val tema: String, // Ej: "Verbo To Be", "Colores", "Pronunciación TH"
    val fallos: Int = 0,
    val ultimaVezFallado: Long = System.currentTimeMillis()
)
