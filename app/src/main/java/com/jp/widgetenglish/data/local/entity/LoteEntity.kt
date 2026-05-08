package com.jp.widgetenglish.data.local.entity


import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "lotes")
data class LoteEntity(
    @PrimaryKey
    val idLote: String,

    val nombre: String,
    val descripcion: String? = null,

    val tipoLote: com.jp.widgetenglish.data.local.entity.TipoLote = _root_ide_package_.com.jp.widgetenglish.data.local.entity.TipoLote.TEMATICO,
    val nivel: com.jp.widgetenglish.data.local.entity.NivelLote = _root_ide_package_.com.jp.widgetenglish.data.local.entity.NivelLote.GENERAL,

    val imgUrl: String? = null,
    val colorHex: String? = null,
    val icono: String? = null,

    val activo: Boolean = true,
    val orden: Int = 0,

    val cantidadContenido: Int = 0,
    val cantidadSugeridaEstudio: Int = 10,

    val vecesEstudiado: Int = 0,
    val vecesCompletado: Int = 0,

    val fechaCreacion: Long = System.currentTimeMillis(),
    val fechaActualizacion: Long? = null
)