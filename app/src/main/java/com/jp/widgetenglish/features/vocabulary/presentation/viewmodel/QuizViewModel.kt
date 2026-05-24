package com.jp.widgetenglish.features.vocabulary.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.jp.widgetenglish.data.local.entity.EstadoAprendizaje
import com.jp.widgetenglish.data.local.entity.TipoContenido
import com.jp.widgetenglish.data.local.entity.TipoPalabra
import com.jp.widgetenglish.data.repository.VocabularioRepository
import com.jp.widgetenglish.data.repository.auth.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class QuizQuestion(
    val palabra: PalabraConProgreso,
    val opciones: List<String>,
    val respuestaCorrecta: String
)

data class QuizUiState(
    val loteId: String = "",
    val preguntas: List<QuizQuestion> = emptyList(),
    val indicePreguntaActual: Int = 0,
    val score: Int = 0,
    val respuestasFalladas: List<PalabraConProgreso> = emptyList(),
    val respuestasAcertadas: List<PalabraConProgreso> = emptyList(),
    val estaFinalizado: Boolean = false,
    val cargando: Boolean = true,
    val mensajeError: String? = null,
    val opcionSeleccionada: String? = null,
    val mostrarFeedback: Boolean = false
)

class QuizViewModel(
    private val vocabularioRepository: VocabularioRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(QuizUiState())
    val uiState: StateFlow<QuizUiState> = _uiState.asStateFlow()

    fun iniciarQuiz(loteId: String, repasarFalladas: Boolean, failedIds: List<String> = emptyList()) {
        // Resetear el estado inmediatamente para evitar que efectos secundarios detecten el estado anterior
        _uiState.update { 
            it.copy(
                cargando = true, 
                mensajeError = null, 
                estaFinalizado = false,
                indicePreguntaActual = 0,
                opcionSeleccionada = null,
                mostrarFeedback = false
            ) 
        }

        viewModelScope.launch {
            val usuarioId = authRepository.obtenerUsuarioActual()?.uid
            if (usuarioId == null) {
                _uiState.update { it.copy(cargando = false, mensajeError = "Usuario no identificado") }
                return@launch
            }

            val contenidos = vocabularioRepository.observarContenidoDeLote(loteId).first()
            val progresos = vocabularioRepository.observarProgresoUsuario(usuarioId).first()

            val palabras = contenidos.mapNotNull { contenido ->
                val progreso = progresos.find {
                    it.contenidoId == contenido.contenidoId &&
                            it.tipoContenido == contenido.tipoContenido
                }

                if (contenido.tipoContenido == TipoContenido.VERBO) {
                    vocabularioRepository.obtenerVerboPorId(contenido.contenidoId)?.let { verbo ->
                        PalabraConProgreso(
                            id = verbo.idVerbo,
                            termino = verbo.formaBase,
                            traduccion = verbo.traduccion,
                            fonetica = verbo.fonetica,
                            estado = progreso?.estadoAprendizaje ?: EstadoAprendizaje.NO_VISTA,
                            esVerbo = true,
                            tipoPalabra = TipoPalabra.VERBO
                        )
                    }
                } else {
                    vocabularioRepository.obtenerPalabraPorId(contenido.contenidoId)?.let { palabra ->
                        PalabraConProgreso(
                            id = palabra.idPalabra,
                            termino = palabra.termino,
                            traduccion = palabra.traduccion,
                            fonetica = palabra.fonetica,
                            estado = progreso?.estadoAprendizaje ?: EstadoAprendizaje.NO_VISTA,
                            esVerbo = false,
                            tipoPalabra = palabra.tipoPalabra
                        )
                    }
                }
            }.let { all ->
                if (repasarFalladas) {
                    if (failedIds.isNotEmpty()) {
                        all.filter { it.id in failedIds }
                    } else {
                        all.filter { it.estado == EstadoAprendizaje.DIFICIL }
                    }
                } else {
                    all
                }
            }

            if (palabras.size < 4 && !repasarFalladas) {
                _uiState.update { it.copy(cargando = false, mensajeError = "El lote debe tener al menos 4 palabras para iniciar el quiz.") }
                return@launch
            }
            
            if (palabras.isEmpty()) {
                _uiState.update { it.copy(cargando = false, mensajeError = "No hay palabras para repasar.") }
                return@launch
            }

            val preguntas = palabras.shuffled().map { palabra ->
                val distractores = vocabularioRepository.obtenerDistractores(listOf(palabra.id), 3)
                val opciones = (distractores.map { it.second } + palabra.traduccion).shuffled()
                QuizQuestion(palabra, opciones, palabra.traduccion)
            }

            _uiState.update {
                it.copy(
                    loteId = loteId,
                    preguntas = preguntas,
                    indicePreguntaActual = 0,
                    score = 0,
                    // Si no estamos repasando, limpiamos la lista de falladas. 
                    // Si estamos repasando, mantenemos la lista anterior para que el filtro funcione si el LaunchedEffect se dispara de nuevo? 
                    // En realidad, 'palabras' ya contiene el filtro.
                    respuestasFalladas = if (repasarFalladas) it.respuestasFalladas else emptyList(),
                    estaFinalizado = false,
                    cargando = false,
                    opcionSeleccionada = null,
                    mostrarFeedback = false
                )
            }
        }
    }

    fun seleccionarOpcion(opcion: String) {
        if (_uiState.value.mostrarFeedback) return

        _uiState.update {
            it.copy(
                opcionSeleccionada = opcion,
                mostrarFeedback = true
            )
        }
    }

    fun siguientePregunta() {
        val currentState = _uiState.value
        val preguntaActual = currentState.preguntas[currentState.indicePreguntaActual]
        val fueCorrecta = currentState.opcionSeleccionada == preguntaActual.respuestaCorrecta

        val newScore = if (fueCorrecta) currentState.score + 1 else currentState.score
        
        val newFalladas = if (!fueCorrecta) {
            currentState.respuestasFalladas + preguntaActual.palabra
        } else {
            currentState.respuestasFalladas
        }

        val newAcertadas = if (fueCorrecta) {
            currentState.respuestasAcertadas + preguntaActual.palabra
        } else {
            currentState.respuestasAcertadas
        }

        if (currentState.indicePreguntaActual + 1 < currentState.preguntas.size) {
            _uiState.update {
                it.copy(
                    indicePreguntaActual = currentState.indicePreguntaActual + 1,
                    score = newScore,
                    respuestasFalladas = newFalladas,
                    respuestasAcertadas = newAcertadas,
                    opcionSeleccionada = null,
                    mostrarFeedback = false
                )
            }
        } else {
            // Finalizar Quiz
            finalizarQuiz(newScore, newFalladas, newAcertadas)
        }
    }

    private fun finalizarQuiz(finalScore: Int, falladas: List<PalabraConProgreso>, acertadas: List<PalabraConProgreso>) {
        viewModelScope.launch {
            val usuarioId = authRepository.obtenerUsuarioActual()?.uid ?: return@launch
            
            // Marcar falladas como DIFICIL
            falladas.forEach { palabra ->
                vocabularioRepository.marcarContenidoComoDificil(
                    usuarioId,
                    palabra.id,
                    if (palabra.esVerbo) TipoContenido.VERBO else TipoContenido.PALABRA
                )
            }
            
            // Marcar acertadas como APRENDIDA (Nueva solicitud)
            acertadas.forEach { palabra ->
                vocabularioRepository.marcarContenidoComoAprendido(
                    usuarioId,
                    palabra.id,
                    if (palabra.esVerbo) TipoContenido.VERBO else TipoContenido.PALABRA
                )
            }

            _uiState.update {
                it.copy(
                    score = finalScore,
                    respuestasFalladas = falladas,
                    respuestasAcertadas = acertadas,
                    estaFinalizado = true
                )
            }
        }
    }
}

class QuizViewModelFactory(
    private val vocabularioRepository: VocabularioRepository,
    private val authRepository: AuthRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(QuizViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return QuizViewModel(vocabularioRepository, authRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
