package com.jp.widgetenglish.features.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jp.widgetenglish.data.remote.firestore.AdminFirestoreDataSource
import com.jp.widgetenglish.data.remote.firestore.AdminUsuarioDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AdminViewModel(
    private val adminFirestoreDataSource: AdminFirestoreDataSource
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminUiState())
    val uiState: StateFlow<AdminUiState> = _uiState.asStateFlow()

    init {
        cargarDatosAdmin()
    }

    fun cargarDatosAdmin() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(
                    cargando = true,
                    error = null
                )

                val usuarios = adminFirestoreDataSource.obtenerUsuarios()

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

                _uiState.value = estadoActual.copy(
                    cargando = false,
                    error = null,

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
                    )
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    cargando = false,
                    error = e.message ?: "Error al cargar datos administrativos"
                )
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
}