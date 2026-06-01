package com.jp.widgetenglish.data.remote.firestore

data class CategoriaStatsDto(
    val id: String = "",
    val nombre: String = "",
    val vecesEstudiada: Int = 0
)

data class PalabraErrorStatsDto(
    val id: String = "",
    val termino: String = "",
    val loteId: String = "",
    val cantidadErrores: Int = 0
)
