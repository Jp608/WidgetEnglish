package com.jp.widgetenglish.data.local.entity



import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "palabras")
data class PalabraEntity(
    @PrimaryKey
    val idPalabra: String,

    val termino: String,
    val traduccion: String,
    val tipoPalabra: com.jp.widgetenglish.data.local.entity.TipoPalabra,

    val fonetica: String? = null,
    val ejemplo: String? = null,
    val ejemploTraduccion: String? = null,

    val dificultad: com.jp.widgetenglish.data.local.entity.Dificultad = _root_ide_package_.com.jp.widgetenglish.data.local.entity.Dificultad.FACIL,
    val activo: Boolean = true,

    val vecesFallada: Int = 0,
    val vecesEstudiada: Int = 0,

    val fechaCreacion: Long = System.currentTimeMillis(),
    val fechaActualizacion: Long? = null
)