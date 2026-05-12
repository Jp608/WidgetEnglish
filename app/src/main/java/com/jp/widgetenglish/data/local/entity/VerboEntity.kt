package com.jp.widgetenglish.data.local.entity



import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "verbos")
data class VerboEntity(
    @PrimaryKey
    val idVerbo: String,

    val formaBase: String,
    val pasadoSimple: String,
    val participioPasado: String,

    val traduccion: String,
    val fonetica: String? = null,

    val ejemploIngles: String? = null,
    val ejemploEspanol: String? = null,

    val dificultad: com.jp.widgetenglish.data.local.entity.Dificultad = _root_ide_package_.com.jp.widgetenglish.data.local.entity.Dificultad.FACIL,
    val esIrregular: Boolean = false,
    val activo: Boolean = true,

    val vecesFallado: Int = 0,
    val vecesEstudiado: Int = 0,

    val fechaCreacion: Long = System.currentTimeMillis(),
    val fechaActualizacion: Long? = null
)