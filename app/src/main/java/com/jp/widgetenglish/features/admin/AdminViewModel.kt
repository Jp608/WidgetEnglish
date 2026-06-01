package com.jp.widgetenglish.features.admin

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jp.widgetenglish.data.remote.firestore.AdminFirestoreDataSource
import com.jp.widgetenglish.data.remote.firestore.AdminUsuarioDto
import com.jp.widgetenglish.data.remote.firestore.EstadisticasFirestoreDataSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AdminViewModel(
    private val adminFirestoreDataSource: AdminFirestoreDataSource,
    private val estadisticasFirestoreDataSource: EstadisticasFirestoreDataSource
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminUiState())
    val uiState: StateFlow<AdminUiState> = _uiState.asStateFlow()

    private var datosAdminCargados = false
    private var cargaEnCurso = false

    fun cargarDatosAdmin(forzarActualizacion: Boolean = false) {
        if (cargaEnCurso) return
        if (datosAdminCargados && !forzarActualizacion) return

        cargaEnCurso = true

        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(
                    cargando = true,
                    error = null
                )

                val usuarios = adminFirestoreDataSource.obtenerUsuarios()
                val erroresParciales = mutableListOf<String>()

                val categoriasStats = runCatching {
                    estadisticasFirestoreDataSource.obtenerCategoriasStats()
                }.onFailure { error ->
                    Log.w(TAG, "No se pudieron cargar estadísticas de categorías", error)
                    erroresParciales.add("No se pudieron cargar las categorías más usadas.")
                }.getOrDefault(emptyList())

                val erroresStats = runCatching {
                    estadisticasFirestoreDataSource.obtenerErroresPalabrasStats()
                }.onFailure { error ->
                    Log.w(TAG, "No se pudieron cargar estadísticas de errores", error)
                    erroresParciales.add("No se pudieron cargar las palabras con más errores.")
                }.getOrDefault(emptyList())

                val totalUsuarios = usuarios.size
                val usuariosActivos = usuarios.count { it.activo }
                val totalPalabras = usuarios.sumOf { it.palabrasAprendidas }
                val totalQuizzes = usuarios.sumOf { it.quizzesRealizados }
                val totalLotes = usuarios.sumOf { it.lotesCompletados }

                val promedioPalabras = if (totalUsuarios > 0) {
                    totalPalabras / totalUsuarios
                } else {
                    0
                }

                val promedioQuizzes = if (totalUsuarios > 0) {
                    totalQuizzes / totalUsuarios
                } else {
                    0
                }

                val porcentajeActivos = if (totalUsuarios > 0) {
                    ((usuariosActivos.toFloat() / totalUsuarios.toFloat()) * 100).toInt()
                } else {
                    0
                }

                val cumplimientoPromedio = if (totalUsuarios > 0) {
                    usuarios.sumOf { it.porcentajeProgreso } / totalUsuarios
                } else {
                    0
                }

                val estadoActual = _uiState.value
                val mensajeError = erroresParciales
                    .takeIf { it.isNotEmpty() }
                    ?.joinToString(separator = "\n")

                _uiState.value = estadoActual.copy(
                    cargando = false,
                    error = mensajeError,

                    totalUsuarios = totalUsuarios,
                    usuariosActivos = usuariosActivos,
                    totalPalabrasAprendidas = totalPalabras,
                    totalQuizzesRealizados = totalQuizzes,
                    totalLotesCompletados = totalLotes,

                    promedioPalabrasPorUsuario = promedioPalabras,
                    promedioQuizzesPorUsuario = promedioQuizzes,
                    porcentajeUsuariosActivos = porcentajeActivos,
                    porcentajeCumplimientoPromedio = cumplimientoPromedio,

                    usuarios = usuarios,
                    rankingUsuarios = ordenarRanking(
                        usuarios = usuarios,
                        criterio = estadoActual.criterioRanking
                    ),
                    usuariosMasActivos = ordenarActividad(
                        usuarios = usuarios,
                        criterio = estadoActual.criterioActividad
                    ),
                    categoriasStats = categoriasStats,
                    erroresStats = erroresStats
                )

                datosAdminCargados = true
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    cargando = false,
                    error = e.message ?: "Error al cargar datos administrativos"
                )
            } finally {
                cargaEnCurso = false
            }
        }
    }

    fun cambiarCriterioRanking(criterio: CriterioRanking) {
        val usuarios = _uiState.value.usuarios

        _uiState.value = _uiState.value.copy(
            criterioRanking = criterio,
            rankingUsuarios = ordenarRanking(
                usuarios = usuarios,
                criterio = criterio
            )
        )
    }

    fun cambiarCriterioActividad(criterio: CriterioActividad) {
        val usuarios = _uiState.value.usuarios

        _uiState.value = _uiState.value.copy(
            criterioActividad = criterio,
            usuariosMasActivos = ordenarActividad(
                usuarios = usuarios,
                criterio = criterio
            )
        )
    }

    private fun ordenarActividad(
        usuarios: List<AdminUsuarioDto>,
        criterio: CriterioActividad
    ): List<AdminUsuarioDto> {
        return when (criterio) {
            CriterioActividad.ACTIVIDAD -> usuarios.sortedByDescending { it.ultimoAcceso }
            CriterioActividad.RACHA -> usuarios.sortedByDescending { it.rachaActual }
            CriterioActividad.CUMPLIMIENTO -> usuarios.sortedByDescending { it.porcentajeProgreso }
        }.take(10)
    }

    private fun ordenarRanking(
        usuarios: List<AdminUsuarioDto>,
        criterio: CriterioRanking
    ): List<AdminUsuarioDto> {
        return when (criterio) {
            CriterioRanking.PALABRAS -> usuarios.sortedByDescending { it.palabrasAprendidas }
            CriterioRanking.QUIZZES -> usuarios.sortedByDescending { it.quizzesRealizados }
            CriterioRanking.RACHA -> usuarios.sortedByDescending { it.rachaActual }
        }.take(10)
    }

    companion object {
        private const val TAG = "AdminViewModel"
    }
}
