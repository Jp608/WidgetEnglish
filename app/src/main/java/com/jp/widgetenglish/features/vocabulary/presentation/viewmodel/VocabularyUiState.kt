package com.jp.widgetenglish.features.vocabulary.presentation.viewmodel

import com.jp.widgetenglish.data.local.entity.EstadoAprendizaje
import com.jp.widgetenglish.data.local.entity.TipoPalabra

data class PalabraConProgreso(
    val id: String,
    val termino: String,
    val traduccion: String,
    val tipoPalabra: TipoPalabra,
    val estado: EstadoAprendizaje,
    val fonetica: String? = null,
    val dificultad: String = "Básico",
    val esVerbo: Boolean = false,

    val ejemplo: String? = null,
    val ejemploTraduccion: String? = null,

    val ejemploIngles: String? = null,
    val ejemploEspanol: String? = null,

    val pasadoSimple: String? = null,
    val participioPasado: String? = null,
    val esIrregular: Boolean = false
)

enum class VocabularioFiltro {
    TODAS,
    PENDIENTES,
    EN_PROGRESO,
    APRENDIDAS
}

enum class VocabularioSeccion {
    PALABRAS,
    VERBOS
}

data class VocabularyUiState(
    val cargando: Boolean = true,
    val palabrasOriginales: List<PalabraConProgreso> = emptyList(),
    val palabrasFiltradas: List<PalabraConProgreso> = emptyList(),
    val filtroActual: VocabularioFiltro = VocabularioFiltro.TODAS,
    val seccionActual: VocabularioSeccion = VocabularioSeccion.PALABRAS,
    val textoBusqueda: String = "",
    val error: String? = null,
    val totalPalabras: Int = 0,
    val palabrasPendientes: Int = 0,
    val palabrasEnProgreso: Int = 0,
    val palabrasAprendidas: Int = 0,
    val mostrarDialogoRevertir: PalabraConProgreso? = null
)