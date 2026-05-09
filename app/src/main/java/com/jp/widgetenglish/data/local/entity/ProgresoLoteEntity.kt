package com.jp.widgetenglish.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "progreso_lote",
    foreignKeys = [
        ForeignKey(
            entity = UsuarioEntity::class,
            parentColumns = ["idUsuario"],
            childColumns = ["usuarioId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = LoteEntity::class,
            parentColumns = ["idLote"],
            childColumns = ["loteId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["usuarioId"]),
        Index(value = ["loteId"]),
        Index(value = ["usuarioId", "loteId"], unique = true)
    ]
)
data class ProgresoLoteEntity(
    @PrimaryKey
    val id: String,

    val usuarioId: String,
    val loteId: String,

    val activo: Boolean = false,
    val completado: Boolean = false,

    val progresoPorcentaje: Float = 0f,
    val contenidosAprendidos: Int = 0,
    val totalContenidos: Int = 0,

    val fechaInicio: Long? = null,
    val fechaUltimoEstudio: Long? = null,
    val fechaCompletado: Long? = null
)