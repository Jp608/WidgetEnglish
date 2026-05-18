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

                val ranking = ordenarRanking(
                    usuarios = usuarios,
                    criterio = _uiState.value.criterioRanking
                )

                val usuariosActivos = ordenarActividad(
                    usuarios = usuarios,
                    criterio = _uiState.value.criterioActividad
                )

                _uiState.value = _uiState.value.copy(
                    cargando = false,
                    totalUsuarios = usuarios.size,
                    usuariosActivos = usuarios.count { it.activo },
                    totalPalabrasAprendidas = usuarios.sumOf { it.palabrasAprendidas },
                    totalQuizzesRealizados = usuarios.sumOf { it.quizzesRealizados },
                    totalLotesCompletados = usuarios.sumOf { it.lotesCompletados },
                    rankingUsuarios = ranking,
                    usuariosMasActivos = usuariosActivos
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
        val usuariosActuales = _uiState.value.rankingUsuarios

        _uiState.value = _uiState.value.copy(
            criterioRanking = criterio,
            rankingUsuarios = ordenarRanking(
                usuarios = usuariosActuales,
                criterio = criterio
            )
        )

        cargarDatosAdmin()
    }

    fun cambiarCriterioActividad(criterio: CriterioActividad) {
        val usuariosActuales = _uiState.value.usuariosMasActivos

        _uiState.value = _uiState.value.copy(
            criterioActividad = criterio,
            usuariosMasActivos = ordenarActividad(
                usuarios = usuariosActuales,
                criterio = criterio
            )
        )

        cargarDatosAdmin()
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