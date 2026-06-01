package com.jp.widgetenglish.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "progreso_usuario",
    indices = [
        Index(value = ["usuarioId"]),
        Index(value = ["contenidoId", "tipoContenido"]),
        Index(value = ["usuarioId", "contenidoId", "tipoContenido"], unique = true)
    ]
)
data class ProgresoUsuarioEntity(
    @PrimaryKey
    val id: String,

    val usuarioId: String,
    val contenidoId: String,
    val tipoContenido: TipoContenido,

    val estadoAprendizaje: EstadoAprendizaje = EstadoAprendizaje.NO_VISTA,
    val nivelDominio: Float = 0f,

    val respuestasCorrectas: Int = 0,
    val respuestasIncorrectas: Int = 0,
    val vecesRepasado: Int = 0,

    val aprendido: Boolean = false,
    val favorito: Boolean = false,

    val ultimaRevision: Long? = null,
    val proximaRevision: Long? = null
)
