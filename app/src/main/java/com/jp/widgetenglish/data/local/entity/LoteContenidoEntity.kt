package com.jp.widgetenglish.data.local.entity



import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "lote_contenido",
    foreignKeys = [
        ForeignKey(
            entity = _root_ide_package_.com.jp.widgetenglish.data.local.entity.LoteEntity::class,
            parentColumns = ["idLote"],
            childColumns = ["loteId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["loteId"]),
        Index(value = ["contenidoId", "tipoContenido"])
    ]
)
data class LoteContenidoEntity(
    @PrimaryKey
    val id: String,

    val loteId: String,
    val contenidoId: String,
    val tipoContenido: com.jp.widgetenglish.data.local.entity.TipoContenido,

    val fechaAgregado: Long = System.currentTimeMillis(),
    val orden: Int = 0
)