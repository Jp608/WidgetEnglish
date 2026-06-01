package com.jp.widgetenglish.features.vocabulary.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.jp.widgetenglish.data.local.dao.UsuarioDao
import com.jp.widgetenglish.data.local.entity.EstadoAprendizaje
import com.jp.widgetenglish.data.local.entity.LoteEntity
import com.jp.widgetenglish.data.repository.VocabularioRepository
import com.jp.widgetenglish.data.repository.auth.AuthRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class StudyUiState(
    val palabrasAprendidas: Int = 0,
    val precisionGlobal: Int = 0,
    val rachaActual: Int = 0,
    val quizCompletados: Int = 0,
    val loteActivo: LoteEntity? = null,
    val progresoLote: Float = 0f,
    val cargando: Boolean = true,
    val cantidadPalabrasQuiz: Int = 10,
    val usuarioIdActual: String? = null,
    val error: String? = null
)

class StudyViewModel(
    private val vocabularioRepository: VocabularioRepository,
    private val authRepository: AuthRepository,
    private val usuarioDao: UsuarioDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(StudyUiState())
    val uiState: StateFlow<StudyUiState> = _uiState.asStateFlow()

    private var estadisticasJob: Job? = null

    init {
        cargarEstadisticas()
    }

    fun cargarEstadisticas() {
        estadisticasJob?.cancel()

        estadisticasJob = viewModelScope.launch {
            val userId = authRepository.obtenerUsuarioActual()?.uid

            if (userId == null) {
                _uiState.update { currentState ->
                    StudyUiState(
                        cargando = false,
                        cantidadPalabrasQuiz = currentState.cantidadPalabrasQuiz,
                        error = "Usuario no identificado"
                    )
                }
                return@launch
            }

            _uiState.update {
                it.copy(
                    cargando = true,
                    usuarioIdActual = userId,
                    error = null
                )
            }

            combine(
                vocabularioRepository.observarProgresoUsuario(userId),
                vocabularioRepository.observarLoteActivo(userId),
                vocabularioRepository.observarLotes(),
                usuarioDao.observarUsuarioPorFirebaseUid(userId)
            ) { progresos, loteActivoProg, lotes, usuario ->

                val aprendidas = progresos.count {
                    it.estadoAprendizaje == EstadoAprendizaje.APRENDIDA
                }

                val totalCorrectas = progresos.sumOf {
                    it.respuestasCorrectas
                }

                val totalIncorrectas = progresos.sumOf {
                    it.respuestasIncorrectas
                }

                val totalRespondidas = totalCorrectas + totalIncorrectas

                val precision = if (totalRespondidas > 0) {
                    (totalCorrectas.toFloat() / totalRespondidas * 100).toInt()
                } else {
                    0
                }

                val infoLoteActivo = lotes.find {
                    it.idLote == loteActivoProg?.loteId
                }

                StudyUiState(
                    palabrasAprendidas = aprendidas,
                    precisionGlobal = precision,
                    rachaActual = usuario?.rachaActual ?: 0,
                    quizCompletados = usuario?.quizzesRealizados ?: 0,
                    loteActivo = infoLoteActivo,
                    progresoLote = loteActivoProg?.progresoPorcentaje ?: 0f,
                    cargando = false,
                    cantidadPalabrasQuiz = _uiState.value.cantidadPalabrasQuiz,
                    usuarioIdActual = userId,
                    error = null
                )
            }.collect { newState ->
                _uiState.value = newState
            }
        }
    }

    fun setCantidadPalabras(cantidad: Int) {
        _uiState.update {
            it.copy(cantidadPalabrasQuiz = cantidad)
        }
    }
}

class StudyViewModelFactory(
    private val vocabularioRepository: VocabularioRepository,
    private val authRepository: AuthRepository,
    private val usuarioDao: UsuarioDao
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StudyViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return StudyViewModel(
                vocabularioRepository = vocabularioRepository,
                authRepository = authRepository,
                usuarioDao = usuarioDao
            ) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}