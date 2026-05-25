package com.jp.widgetenglish.features.vocabulary.presentation.cards.model

enum class CardsStudyFilter {
    TODAS,
    EN_PROGRESO,
    APRENDIDAS,
    DIFICILES
}

enum class CardsStudyMode {
    ALEATORIO,
    ORDEN_LOTE
}

data class CardsSessionConfig(
    val cantidad: Int = 10,
    val usarTodas: Boolean = false,
    val filtro: CardsStudyFilter = CardsStudyFilter.EN_PROGRESO,
    val modo: CardsStudyMode = CardsStudyMode.ALEATORIO,
    val mostrarTraduccionAlInicio: Boolean = true,
    val incluirPronunciacion: Boolean = true,
    val mostrarEjemploUso: Boolean = true,
    val saltarDominadas: Boolean = false
) {
    fun cantidadReal(totalDisponible: Int): Int {
        return if (usarTodas) {
            totalDisponible
        } else {
            cantidad.coerceAtMost(totalDisponible)
        }
    }

    fun tiempoEstimadoMinutos(): Int {
        val cantidadBase = if (usarTodas) {
            20
        } else {
            cantidad
        }

        return when {
            cantidadBase <= 5 -> 3
            cantidadBase <= 10 -> 6
            cantidadBase <= 15 -> 9
            else -> 12
        }
    }
}

data class CardsSessionSummary(
    val loteId: String = "",
    val loteNombre: String = "",
    val totalEstudiadas: Int = 0,
    val conocidas: Int = 0,
    val noConocidas: Int = 0,
    val dificiles: Int = 0,
    val aprendidas: Int = 0,
    val progresoFinal: Float = 0f
)