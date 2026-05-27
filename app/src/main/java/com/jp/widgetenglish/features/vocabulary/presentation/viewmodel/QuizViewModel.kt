package com.jp.widgetenglish.features.vocabulary.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.jp.widgetenglish.data.local.dao.UsuarioDao
import com.jp.widgetenglish.data.local.entity.EstadoAprendizaje
import com.jp.widgetenglish.data.local.entity.RolUsuario
import com.jp.widgetenglish.data.local.entity.TipoContenido
import com.jp.widgetenglish.data.local.entity.TipoPalabra
import com.jp.widgetenglish.data.local.entity.UsuarioEntity
import com.jp.widgetenglish.data.repository.VocabularioRepository
import com.jp.widgetenglish.data.repository.auth.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.jp.widgetenglish.data.repository.StreakRepository
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
    val mostrarFeedback: Boolean = false,
    val quizRegistrado: Boolean = false
)

class QuizViewModel(
    private val vocabularioRepository: VocabularioRepository,
    private val authRepository: AuthRepository,
    private val usuarioDao: UsuarioDao,
    private val streakRepository: StreakRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(QuizUiState())
    val uiState: StateFlow<QuizUiState> = _uiState.asStateFlow()

    fun iniciarQuiz(
        loteId: String,
        repasarFalladas: Boolean,
        failedIds: List<String> = emptyList(),
        limite: Int = 10
    ) {
        _uiState.update {
            QuizUiState(
                loteId = loteId,
                cargando = true,
                mensajeError = null,
                estaFinalizado = false,
                quizRegistrado = false
            )
        }

        viewModelScope.launch {
            val usuarioId = authRepository.obtenerUsuarioActual()?.uid

            if (usuarioId == null) {
                _uiState.update {
                    it.copy(
                        cargando = false,
                        mensajeError = "Usuario no identificado"
                    )
                }
                return@launch
            }

            asegurarUsuarioLocal(usuarioId)

            val contenidos = vocabularioRepository
                .observarContenidoDeLote(loteId)
                .first()

            val progresos = vocabularioRepository
                .observarProgresoUsuario(usuarioId)
                .first()

            val palabras = contenidos.mapNotNull { contenido ->
                val progreso = progresos.find {
                    it.contenidoId == contenido.contenidoId &&
                            it.tipoContenido == contenido.tipoContenido
                }

                if (contenido.tipoContenido == TipoContenido.VERBO) {
                    vocabularioRepository.obtenerVerboPorId(contenido.contenidoId)
                        ?.let { verbo ->
                            PalabraConProgreso(
                                id = verbo.idVerbo,
                                termino = verbo.formaBase,
                                traduccion = verbo.traduccion,
                                fonetica = verbo.fonetica,
                                estado = progreso?.estadoAprendizaje
                                    ?: EstadoAprendizaje.NO_VISTA,
                                esVerbo = true,
                                tipoPalabra = TipoPalabra.VERBO,
                                pasadoSimple = verbo.pasadoSimple,
                                participioPasado = verbo.participioPasado,
                                esIrregular = verbo.esIrregular,
                                ejemploIngles = verbo.ejemploIngles,
                                ejemploEspanol = verbo.ejemploEspanol
                            )
                        }
                } else {
                    vocabularioRepository.obtenerPalabraPorId(contenido.contenidoId)
                        ?.let { palabra ->
                            PalabraConProgreso(
                                id = palabra.idPalabra,
                                termino = palabra.termino,
                                traduccion = palabra.traduccion,
                                fonetica = palabra.fonetica,
                                estado = progreso?.estadoAprendizaje
                                    ?: EstadoAprendizaje.NO_VISTA,
                                esVerbo = false,
                                tipoPalabra = palabra.tipoPalabra,
                                ejemplo = palabra.ejemplo,
                                ejemploTraduccion = palabra.ejemploTraduccion
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
                    all.shuffled().take(limite)
                }
            }

            if (palabras.size < 4 && !repasarFalladas) {
                _uiState.update {
                    it.copy(
                        cargando = false,
                        mensajeError = "El lote debe tener al menos 4 palabras para iniciar el quiz."
                    )
                }
                return@launch
            }

            if (palabras.isEmpty()) {
                _uiState.update {
                    it.copy(
                        cargando = false,
                        mensajeError = "No hay palabras para repasar."
                    )
                }
                return@launch
            }

            val preguntas = palabras.shuffled().map { palabra ->
                val distractores = vocabularioRepository.obtenerDistractores(
                    excluirIds = listOf(palabra.id),
                    cantidad = 3
                )

                val opciones = (
                        distractores.map { it.second } + palabra.traduccion
                        ).shuffled()

                QuizQuestion(
                    palabra = palabra,
                    opciones = opciones,
                    respuestaCorrecta = palabra.traduccion
                )
            }

            _uiState.update {
                it.copy(
                    loteId = loteId,
                    preguntas = preguntas,
                    indicePreguntaActual = 0,
                    score = 0,
                    respuestasFalladas = emptyList(),
                    respuestasAcertadas = emptyList(),
                    estaFinalizado = false,
                    cargando = false,
                    mensajeError = null,
                    opcionSeleccionada = null,
                    mostrarFeedback = false,
                    quizRegistrado = false
                )
            }
        }
    }

    fun seleccionarOpcion(opcion: String) {
        val currentState = _uiState.value

        if (currentState.estaFinalizado) return
        if (currentState.mostrarFeedback) return

        _uiState.update {
            it.copy(
                opcionSeleccionada = opcion,
                mostrarFeedback = true
            )
        }
    }

    fun siguientePregunta() {
        val currentState = _uiState.value

        if (currentState.estaFinalizado) return
        if (currentState.quizRegistrado) return
        if (currentState.preguntas.isEmpty()) return
        if (currentState.indicePreguntaActual !in currentState.preguntas.indices) return
        if (currentState.opcionSeleccionada == null) return

        val preguntaActual = currentState.preguntas[currentState.indicePreguntaActual]
        val fueCorrecta = currentState.opcionSeleccionada == preguntaActual.respuestaCorrecta

        val newScore = if (fueCorrecta) {
            currentState.score + 1
        } else {
            currentState.score
        }

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
            finalizarQuiz(
                finalScore = newScore,
                falladas = newFalladas,
                acertadas = newAcertadas
            )
        }
    }

    private fun finalizarQuiz(
        finalScore: Int,
        falladas: List<PalabraConProgreso>,
        acertadas: List<PalabraConProgreso>
    ) {
        val preguntasRespondidas = _uiState.value.preguntas.size

        _uiState.update {
            it.copy(
                score = finalScore,
                respuestasFalladas = falladas,
                respuestasAcertadas = acertadas,
                estaFinalizado = true,
                quizRegistrado = true
            )
        }

        viewModelScope.launch {
            val usuarioId = authRepository.obtenerUsuarioActual()?.uid ?: return@launch

            try {
                falladas.forEach { palabra ->
                    vocabularioRepository.marcarContenidoComoDificil(
                        usuarioId = usuarioId,
                        contenidoId = palabra.id,
                        tipoContenido = if (palabra.esVerbo) {
                            TipoContenido.VERBO
                        } else {
                            TipoContenido.PALABRA
                        }
                    )
                }

                acertadas.forEach { palabra ->
                    vocabularioRepository.marcarContenidoComoAprendido(
                        usuarioId = usuarioId,
                        contenidoId = palabra.id,
                        tipoContenido = if (palabra.esVerbo) {
                            TipoContenido.VERBO
                        } else {
                            TipoContenido.PALABRA
                        }
                    )
                }

                registrarQuizCompletado(
                    usuarioId = usuarioId,
                    preguntasRespondidas = preguntasRespondidas
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error guardando resultados del quiz", e)
            }
        }
    }

    private suspend fun registrarQuizCompletado(
        usuarioId: String,
        preguntasRespondidas: Int
    ) {
        try {
            usuarioDao.incrementarQuizzesRealizados(usuarioId)
        } catch (e: Exception) {
            Log.e(TAG, "Error incrementando quizzes en Room", e)
        }

        try {
            streakRepository.registrarQuizCompletado(
                usuarioId = usuarioId,
                preguntasRespondidas = preguntasRespondidas
            )

            streakRepository.sincronizarEstadisticasActuales(usuarioId)
        } catch (e: Exception) {
            Log.e(TAG, "Error sincronizando estadísticas del quiz", e)
        }
    }

    private suspend fun asegurarUsuarioLocal(usuarioId: String): UsuarioEntity {
        val usuarioExistente = usuarioDao.obtenerUsuarioPorFirebaseUid(usuarioId)

        if (usuarioExistente != null) {
            return usuarioExistente
        }

        val firebaseUser = authRepository.obtenerUsuarioActual()

        val nuevoUsuario = UsuarioEntity(
            idUsuario = usuarioId,
            firebaseUid = usuarioId,
            nombre = firebaseUser?.displayName ?: "Usuario",
            correo = firebaseUser?.email ?: "",
            avatar = firebaseUser?.photoUrl?.toString(),
            rol = RolUsuario.USUARIO,
            activo = true,
            fechaRegistro = System.currentTimeMillis(),
            ultimoAcceso = System.currentTimeMillis(),
            rachaActual = 0,
            rachaMaxima = 0,
            palabrasAprendidas = 0,
            quizzesRealizados = 0,
            lotesCompletados = 0,
            porcentajeProgreso = 0
        )

        usuarioDao.insertarUsuario(nuevoUsuario)

        Log.d(
            TAG,
            "Usuario local creado para quiz. Usuario: $usuarioId"
        )

        return nuevoUsuario
    }

    companion object {
        private const val TAG = "QuizViewModel"
    }
}

class QuizViewModelFactory(
    private val vocabularioRepository: VocabularioRepository,
    private val authRepository: AuthRepository,
    private val usuarioDao: UsuarioDao,
    private val streakRepository: StreakRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(QuizViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return QuizViewModel(
                vocabularioRepository = vocabularioRepository,
                authRepository = authRepository,
                usuarioDao = usuarioDao,
                streakRepository = streakRepository
            ) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}