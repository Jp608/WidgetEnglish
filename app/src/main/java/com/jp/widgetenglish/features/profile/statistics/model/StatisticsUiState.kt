package com.jp.widgetenglish.features.profile.statistics.model

enum class StatisticsPeriod {
    WEEK,
    MONTH,
    YEAR
}

data class StatisticsUiState(
    val cargando: Boolean = true,
    val error: String? = null,

    val palabrasAprendidas: Int = 0,
    val quizzesRealizados: Int = 0,
    val porcentajeProgreso: Int = 0,
    val rachaActual: Int = 0,
    val rachaMaxima: Int = 0,
    val lotesCompletados: Int = 0,

    val precisionGlobal: Int = 0,

    val aprendidasPorcentaje: Int = 0,
    val enProgresoPorcentaje: Int = 0,
    val dificilesPorcentaje: Int = 0,
    val noVistasPorcentaje: Int = 0,

    val progresoLotes: List<StatisticsLotProgressItem> = emptyList(),

    // Historial de la gráfica.
    // Por ahora conservamos el nombre progresoSemanal para no romper StatisticsScreen.
    // Pero puede representar semana, mes o año según periodoSeleccionado.
    val progresoSemanal: List<StatisticsWeeklyItem> = emptyList(),

    val periodoSeleccionado: StatisticsPeriod = StatisticsPeriod.WEEK,
    val fechaReferenciaMillis: Long = System.currentTimeMillis(),
    val tituloPeriodo: String = "Semana actual"
)

data class StatisticsLotProgressItem(
    val loteId: String,
    val nombre: String,
    val porcentaje: Int,
    val aprendidas: Int,
    val total: Int
)

data class StatisticsWeeklyItem(
    val dia: String,
    val valor: Int
)