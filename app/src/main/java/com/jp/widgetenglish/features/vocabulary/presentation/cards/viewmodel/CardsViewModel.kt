package com.jp.widgetenglish.features.vocabulary.presentation.cards.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.jp.widgetenglish.data.local.entity.EstadoAprendizaje
import com.jp.widgetenglish.data.local.entity.LoteEntity
import com.jp.widgetenglish.data.local.entity.ProgresoUsuarioEntity
import com.jp.widgetenglish.data.local.entity.TipoContenido
import com.jp.widgetenglish.data.local.entity.TipoPalabra
import com.jp.widgetenglish.data.repository.VocabularioRepository
import com.jp.widgetenglish.data.repository.auth.AuthRepository
import com.jp.widgetenglish.features.vocabulary.presentation.cards.model.CardsSessionConfig
import com.jp.widgetenglish.features.vocabulary.presentation.cards.model.CardsSessionSummary
import com.jp.widgetenglish.features.vocabulary.presentation.cards.model.CardsStudyFilter
import com.jp.widgetenglish.features.vocabulary.presentation.cards.model.CardsStudyMode
import com.jp.widgetenglish.features.vocabulary.presentation.viewmodel.PalabraConProgreso
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.jp.widgetenglish.data.repository.StreakRepository

enum class CardsAnswerType {
    LA_CONOZCO,
    NO_LA_CONOZCO,
    DIFICIL,
    APRENDIDA
}

data class CardsUiState(
    val cargando: Boolean = true,
    val error: String? = null,

    val usuarioId: String? = null,
    val loteActivo: LoteEntity? = null,
    val progresoLote: Float = 0f,

    val totalDisponibles: Int = 0,
    val tarjetasDisponibles: List<PalabraConProgreso> = emptyList(),
    val tarjetasSesion: List<PalabraConProgreso> = emptyList(),

    val indiceActual: Int = 0,
    val sesionIniciada: Boolean = false,
    val sesionFinalizada: Boolean = false,

    val config: CardsSessionConfig = CardsSessionConfig(),

    val conocidas: Int = 0,
    val noConocidas: Int = 0,
    val dificiles: Int = 0,
    val aprendidas: Int = 0,

    val respuestasSeleccionadas: Map<String, CardsAnswerType> = emptyMap(),

    val resumen: CardsSessionSummary? = null
) {
    val tarjetaActual: PalabraConProgreso?
        get() = tarjetasSesion.getOrNull(indiceActual)

    val totalSesion: Int
        get() = tarjetasSesion.size

    val numeroTarjetaActual: Int
        get() = if (tarjetasSesion.isEmpty()) 0 else indiceActual + 1

    val respuestaActual: CardsAnswerType?
        get() = tarjetaActual?.let { respuestasSeleccionadas[it.id] }

    val tarjetaActualYaRespondida: Boolean
        get() = respuestaActual != null

    val porcentajeSesion: Int
        get() = if (tarjetasSesion.isEmpty()) {
            0
        } else {
            ((numeroTarjetaActual.toFloat() / tarjetasSesion.size) * 100).toInt()
        }

    val puedeIrAnterior: Boolean
        get() = indiceActual > 0

    val puedeIrSiguiente: Boolean
        get() = indiceActual < tarjetasSesion.lastIndex

    val esUltimaTarjeta: Boolean
        get() = tarjetasSesion.isNotEmpty() && indiceActual == tarjetasSesion.lastIndex

}

class CardsViewModel(
    private val vocabularioRepository: VocabularioRepository,
    private val authRepository: AuthRepository,
    private val streakRepository: StreakRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CardsUiState())
    val uiState: StateFlow<CardsUiState> = _uiState.asStateFlow()

    private var cargarJob: Job? = null

    init {
        cargarDatos()
    }

    fun cargarDatos() {
        cargarJob?.cancel()

        cargarJob = viewModelScope.launch {
            val userId = authRepository.obtenerUsuarioActual()?.uid

            if (userId == null) {
                _uiState.update {
                    it.copy(
                        cargando = false,
                        error = "Usuario no identificado"
                    )
                }
                return@launch
            }

            _uiState.update {
                it.copy(
                    cargando = true,
                    error = null,
                    usuarioId = userId
                )
            }

            combine(
                vocabularioRepository.observarLoteActivo(userId),
                vocabularioRepository.observarLotes(),
                vocabularioRepository.observarProgresoUsuario(userId)
            ) { loteActivoProg, lotes, progresos ->

                val loteActivo = lotes.find {
                    it.idLote == loteActivoProg?.loteId
                }

                Triple(loteActivoProg, loteActivo, progresos)
            }.collect { (loteActivoProg, loteActivo, progresos) ->

                if (loteActivo == null || loteActivoProg == null) {
                    _uiState.update {
                        it.copy(
                            cargando = false,
                            error = "Selecciona un lote para estudiar tarjetas.",
                            loteActivo = null,
                            progresoLote = 0f,
                            totalDisponibles = 0,
                            tarjetasDisponibles = emptyList()
                        )
                    }
                    return@collect
                }

                val tarjetas = cargarTarjetasDelLote(
                    loteId = loteActivo.idLote,
                    progresos = progresos
                )

                _uiState.update { current ->
                    current.copy(
                        cargando = false,
                        error = null,
                        usuarioId = userId,
                        loteActivo = loteActivo,
                        progresoLote = loteActivoProg.progresoPorcentaje,
                        totalDisponibles = tarjetas.size,
                        tarjetasDisponibles = tarjetas
                    )
                }
            }
        }
    }

    private suspend fun cargarTarjetasDelLote(
        loteId: String,
        progresos: List<ProgresoUsuarioEntity>
    ): List<PalabraConProgreso> {
        val contenidos = vocabularioRepository
            .observarContenidoDeLote(loteId)
            .first()

        return contenidos.mapNotNull { contenido ->
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
                            tipoPalabra = TipoPalabra.VERBO,
                            estado = progreso?.estadoAprendizaje
                                ?: EstadoAprendizaje.NO_VISTA,
                            fonetica = verbo.fonetica,
                            dificultad = verbo.dificultad.name,
                            esVerbo = true,
                            ejemploIngles = verbo.ejemploIngles,
                            ejemploEspanol = verbo.ejemploEspanol,
                            pasadoSimple = verbo.pasadoSimple,
                            participioPasado = verbo.participioPasado,
                            esIrregular = verbo.esIrregular
                        )
                    }
            } else {
                vocabularioRepository.obtenerPalabraPorId(contenido.contenidoId)
                    ?.let { palabra ->
                        PalabraConProgreso(
                            id = palabra.idPalabra,
                            termino = palabra.termino,
                            traduccion = palabra.traduccion,
                            tipoPalabra = palabra.tipoPalabra,
                            estado = progreso?.estadoAprendizaje
                                ?: EstadoAprendizaje.NO_VISTA,
                            fonetica = palabra.fonetica,
                            dificultad = palabra.dificultad.name,
                            esVerbo = false,
                            ejemplo = palabra.ejemplo,
                            ejemploTraduccion = palabra.ejemploTraduccion
                        )
                    }
            }
        }
    }

    fun seleccionarCantidad(cantidad: Int) {
        _uiState.update {
            it.copy(
                config = it.config.copy(
                    cantidad = cantidad,
                    usarTodas = false
                )
            )
        }
    }

    fun seleccionarTodasLasTarjetas() {
        _uiState.update {
            it.copy(
                config = it.config.copy(
                    usarTodas = true
                )
            )
        }
    }

    fun seleccionarFiltro(filtro: CardsStudyFilter) {
        _uiState.update {
            it.copy(
                config = it.config.copy(
                    filtro = filtro
                )
            )
        }
    }

    fun seleccionarModo(modo: CardsStudyMode) {
        _uiState.update {
            it.copy(
                config = it.config.copy(
                    modo = modo
                )
            )
        }
    }

    fun toggleMostrarTraduccion() {
        _uiState.update {
            it.copy(
                config = it.config.copy(
                    mostrarTraduccionAlInicio = !it.config.mostrarTraduccionAlInicio
                )
            )
        }
    }

    fun toggleIncluirPronunciacion() {
        _uiState.update {
            it.copy(
                config = it.config.copy(
                    incluirPronunciacion = !it.config.incluirPronunciacion
                )
            )
        }
    }

    fun toggleMostrarEjemploUso() {
        _uiState.update {
            it.copy(
                config = it.config.copy(
                    mostrarEjemploUso = !it.config.mostrarEjemploUso
                )
            )
        }
    }

    fun toggleSaltarDominadas() {
        _uiState.update {
            it.copy(
                config = it.config.copy(
                    saltarDominadas = !it.config.saltarDominadas
                )
            )
        }
    }

    fun iniciarSesionTarjetas() {
        val current = _uiState.value
        val lote = current.loteActivo

        if (lote == null) {
            _uiState.update {
                it.copy(error = "Selecciona un lote activo para estudiar.")
            }
            return
        }

        val tarjetasFiltradas = aplicarConfiguracion(
            tarjetas = current.tarjetasDisponibles,
            config = current.config
        )

        if (tarjetasFiltradas.isEmpty()) {
            _uiState.update {
                it.copy(error = "No hay tarjetas disponibles con esta configuración.")
            }
            return
        }

        _uiState.update {
            it.copy(
                error = null,
                tarjetasSesion = tarjetasFiltradas,
                indiceActual = 0,
                sesionIniciada = true,
                sesionFinalizada = false,
                conocidas = 0,
                noConocidas = 0,
                dificiles = 0,
                aprendidas = 0,
                respuestasSeleccionadas = emptyMap(),
                resumen = null
            )
        }
    }

    private fun aplicarConfiguracion(
        tarjetas: List<PalabraConProgreso>,
        config: CardsSessionConfig
    ): List<PalabraConProgreso> {
        val filtradas = tarjetas.filter { tarjeta ->
            val pasaFiltro = when (config.filtro) {
                CardsStudyFilter.TODAS -> true

                CardsStudyFilter.EN_PROGRESO ->
                    tarjeta.estado == EstadoAprendizaje.NO_VISTA ||
                            tarjeta.estado == EstadoAprendizaje.EN_PROGRESO

                CardsStudyFilter.APRENDIDAS ->
                    tarjeta.estado == EstadoAprendizaje.APRENDIDA

                CardsStudyFilter.DIFICILES ->
                    tarjeta.estado == EstadoAprendizaje.DIFICIL
            }

            val pasaDominadas = if (config.saltarDominadas) {
                tarjeta.estado != EstadoAprendizaje.APRENDIDA
            } else {
                true
            }

            pasaFiltro && pasaDominadas
        }

        val ordenadas = when (config.modo) {
            CardsStudyMode.ALEATORIO -> filtradas.shuffled()
            CardsStudyMode.ORDEN_LOTE -> filtradas
        }

        val cantidad = config.cantidadReal(ordenadas.size)

        return ordenadas.take(cantidad)
    }

    fun irAnterior() {
        _uiState.update {
            if (it.indiceActual > 0) {
                it.copy(indiceActual = it.indiceActual - 1)
            } else {
                it
            }
        }
    }

    fun irSiguiente() {
        val current = _uiState.value

        if (current.indiceActual < current.tarjetasSesion.lastIndex) {
            _uiState.update {
                it.copy(indiceActual = it.indiceActual + 1)
            }
        } else {
            finalizarSesion()
        }
    }

    fun clasificarTarjetaActual(tipo: CardsAnswerType) {
        val current = _uiState.value
        val tarjeta = current.tarjetaActual ?: return
        val userId = current.usuarioId ?: return

        val respuestaAnterior = current.respuestasSeleccionadas[tarjeta.id]

        _uiState.update { state ->
            val nuevasRespuestas = state.respuestasSeleccionadas + (tarjeta.id to tipo)

            state.copy(
                respuestasSeleccionadas = nuevasRespuestas,
                conocidas = nuevasRespuestas.values.count { it == CardsAnswerType.LA_CONOZCO },
                noConocidas = nuevasRespuestas.values.count { it == CardsAnswerType.NO_LA_CONOZCO },
                dificiles = nuevasRespuestas.values.count { it == CardsAnswerType.DIFICIL },
                aprendidas = nuevasRespuestas.values.count { it == CardsAnswerType.APRENDIDA }
            )
        }

        viewModelScope.launch {
            if (respuestaAnterior != tipo) {
                guardarClasificacion(
                    userId = userId,
                    tarjeta = tarjeta,
                    tipo = tipo
                )

                actualizarProgresoLote(userId)
            }
        }
    }

    private suspend fun guardarClasificacion(
        userId: String,
        tarjeta: PalabraConProgreso,
        tipo: CardsAnswerType
    ) {
        val tipoContenido = if (tarjeta.esVerbo) {
            TipoContenido.VERBO
        } else {
            TipoContenido.PALABRA
        }

        val progresoExistente = vocabularioRepository.obtenerProgresoContenido(
            usuarioId = userId,
            contenidoId = tarjeta.id,
            tipoContenido = tipoContenido
        )

        val nuevoEstado = when (tipo) {
            CardsAnswerType.LA_CONOZCO -> EstadoAprendizaje.EN_PROGRESO
            CardsAnswerType.NO_LA_CONOZCO -> EstadoAprendizaje.EN_PROGRESO
            CardsAnswerType.DIFICIL -> EstadoAprendizaje.DIFICIL
            CardsAnswerType.APRENDIDA -> EstadoAprendizaje.APRENDIDA
        }

        val esCorrecta = tipo == CardsAnswerType.LA_CONOZCO ||
                tipo == CardsAnswerType.APRENDIDA

        val progresoNuevo = if (progresoExistente == null) {
            ProgresoUsuarioEntity(
                id = "${userId}_${tarjeta.id}_${tipoContenido.name}",
                usuarioId = userId,
                contenidoId = tarjeta.id,
                tipoContenido = tipoContenido,
                estadoAprendizaje = nuevoEstado,
                nivelDominio = calcularNivelDominio(
                    nivelActual = 0f,
                    tipo = tipo
                ),
                respuestasCorrectas = if (esCorrecta) 1 else 0,
                respuestasIncorrectas = if (esCorrecta) 0 else 1,
                vecesRepasado = 1,
                aprendido = nuevoEstado == EstadoAprendizaje.APRENDIDA,
                favorito = false,
                ultimaRevision = System.currentTimeMillis(),
                proximaRevision = null
            )
        } else {
            progresoExistente.copy(
                estadoAprendizaje = nuevoEstado,
                nivelDominio = calcularNivelDominio(
                    nivelActual = progresoExistente.nivelDominio,
                    tipo = tipo
                ),
                respuestasCorrectas = progresoExistente.respuestasCorrectas +
                        if (esCorrecta) 1 else 0,
                respuestasIncorrectas = progresoExistente.respuestasIncorrectas +
                        if (esCorrecta) 0 else 1,
                vecesRepasado = progresoExistente.vecesRepasado + 1,
                aprendido = nuevoEstado == EstadoAprendizaje.APRENDIDA,
                ultimaRevision = System.currentTimeMillis()
            )
        }

        vocabularioRepository.guardarProgresoUsuario(progresoNuevo)
    }

    private fun calcularNivelDominio(
        nivelActual: Float,
        tipo: CardsAnswerType
    ): Float {
        val nuevoNivel = when (tipo) {
            CardsAnswerType.LA_CONOZCO -> nivelActual + 0.20f
            CardsAnswerType.NO_LA_CONOZCO -> nivelActual - 0.10f
            CardsAnswerType.DIFICIL -> nivelActual - 0.05f
            CardsAnswerType.APRENDIDA -> 1f
        }

        return nuevoNivel.coerceIn(0f, 1f)
    }

    private suspend fun actualizarProgresoLote(userId: String) {
        val lote = _uiState.value.loteActivo ?: return

        val contenidos = vocabularioRepository
            .observarContenidoDeLote(lote.idLote)
            .first()

        val progresos = vocabularioRepository
            .observarProgresoUsuario(userId)
            .first()

        val total = contenidos.size

        if (total <= 0) return

        val aprendidas = contenidos.count { contenido ->
            progresos.any { progreso ->
                progreso.contenidoId == contenido.contenidoId &&
                        progreso.tipoContenido == contenido.tipoContenido &&
                        progreso.estadoAprendizaje == EstadoAprendizaje.APRENDIDA
            }
        }

        val porcentaje = ((aprendidas.toFloat() / total) * 100f)
            .coerceIn(0f, 100f)

        vocabularioRepository.actualizarProgresoLotePorcentaje(
            usuarioId = userId,
            loteId = lote.idLote,
            progresoPorcentaje = porcentaje
        )

        _uiState.update {
            it.copy(progresoLote = porcentaje)
        }
    }

    private fun avanzarDespuesDeClasificar() {
        val current = _uiState.value

        if (current.indiceActual < current.tarjetasSesion.lastIndex) {
            _uiState.update {
                it.copy(indiceActual = it.indiceActual + 1)
            }
        } else {
            finalizarSesion()
        }
    }

    fun finalizarSesion() {
        val current = _uiState.value
        val lote = current.loteActivo ?: return
        val userId = current.usuarioId ?: return

        val resumen = CardsSessionSummary(
            loteId = lote.idLote,
            loteNombre = lote.nombre,
            totalEstudiadas = current.tarjetasSesion.size,
            conocidas = current.conocidas,
            noConocidas = current.noConocidas,
            dificiles = current.dificiles,
            aprendidas = current.aprendidas,
            progresoFinal = current.progresoLote
        )

        _uiState.update {
            it.copy(
                sesionFinalizada = true,
                sesionIniciada = false,
                resumen = resumen
            )
        }

        viewModelScope.launch {
            try {
                streakRepository.registrarTarjetasEstudiadas(
                    usuarioId = userId,
                    cantidad = current.tarjetasSesion.size
                )

                streakRepository.sincronizarEstadisticasActuales(userId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun reiniciarSesion() {
        iniciarSesionTarjetas()
    }

    fun limpiarError() {
        _uiState.update {
            it.copy(error = null)
        }
    }
}

class CardsViewModelFactory(
    private val vocabularioRepository: VocabularioRepository,
    private val authRepository: AuthRepository,
    private val streakRepository: StreakRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CardsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CardsViewModel(
                vocabularioRepository = vocabularioRepository,
                authRepository = authRepository,
                streakRepository = streakRepository
            ) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}