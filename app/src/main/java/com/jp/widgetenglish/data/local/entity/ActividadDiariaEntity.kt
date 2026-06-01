package com.jp.widgetenglish.data.local.entity

import androidx.room.Entity

@Entity(
    tableName = "actividad_diaria",
    primaryKeys = ["usuarioId", "fecha"]
)
data class ActividadDiariaEntity(
    val usuarioId: String,

    // Formato recomendado: "yyyy-MM-dd"
    // Ejemplo: "2026-05-25"
    val fecha: String,

    // Actividad acumulada del día
    val elementosEstudiados: Int = 0,
    val tarjetasEstudiadas: Int = 0,
    val preguntasQuizRespondidas: Int = 0,
    val quizzesCompletados: Int = 0,

    // Meta diaria
    val objetivoDiario: Int = 10,
    val objetivoCumplido: Boolean = false,

    // Control de racha
    val fechaCumplimiento: Long? = null,
    val ultimaActualizacion: Long = System.currentTimeMillis()
)