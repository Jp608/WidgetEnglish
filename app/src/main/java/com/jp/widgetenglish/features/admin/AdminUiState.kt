package com.jp.widgetenglish.features.admin
import com.jp.widgetenglish.data.remote.firestore.AdminUsuarioDto

data class AdminUiState(
    val cargando: Boolean = true,
    val error: String? = null,

    val totalUsuarios: Int = 0,
    val usuariosActivos: Int = 0,
    val totalPalabrasAprendidas: Int = 0,
    val totalQuizzesRealizados: Int = 0,
    val totalLotesCompletados: Int = 0,

    val rankingUsuarios: List<AdminUsuarioDto> = emptyList(),
    val usuariosMasActivos: List<AdminUsuarioDto> = emptyList(),
    val criterioActividad: CriterioActividad = CriterioActividad.ACTIVIDAD,
    val criterioRanking: CriterioRanking = CriterioRanking.PALABRAS
)

enum class CriterioRanking {
    PALABRAS,
    QUIZZES,
    RACHA
}
enum class CriterioActividad {
    ACTIVIDAD,
    RACHA,
    CUMPLIMIENTO
}