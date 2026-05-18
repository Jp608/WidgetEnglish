package com.jp.widgetenglish.features.vocabulary.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jp.widgetenglish.data.local.entity.Dificultad
import com.jp.widgetenglish.data.local.entity.EstadoAprendizaje
import com.jp.widgetenglish.data.local.entity.ProgresoUsuarioEntity
import com.jp.widgetenglish.data.local.entity.TipoContenido
import com.jp.widgetenglish.data.local.entity.TipoPalabra
import com.jp.widgetenglish.data.remote.firestore.UsuarioFirestoreDataSource
import com.jp.widgetenglish.data.repository.VocabularioRepository
import com.jp.widgetenglish.data.repository.auth.AuthRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class VocabularyViewModel(
    private val repository: VocabularioRepository,
    private val authRepository: AuthRepository,
    private val usuarioFirestoreDataSource: UsuarioFirestoreDataSource
) : ViewModel() {

    private val _filtroActual = MutableStateFlow(VocabularioFiltro.TODAS)
    private val _seccionActual = MutableStateFlow(VocabularioSeccion.PALABRAS)
    private val _textoBusqueda = MutableStateFlow("")
    private val _mostrarDialogoRevertir = MutableStateFlow<PalabraConProgreso?>(null)

    private val _usuarioIdActual = MutableStateFlow<String?>(null)

    private val _uiState = MutableStateFlow(VocabularyUiState(cargando = true))
    val uiState: StateFlow<VocabularyUiState> = _uiState.asStateFlow()

    init {
        cargarUsuarioActual()
        cargarVocabulario()
    }

    fun cargarUsuarioActual() {
        _usuarioIdActual.value = authRepository.obtenerUsuarioActual()?.uid
    }

    private fun cargarVocabulario() {
        viewModelScope.launch {
            val contentFlow = combine(
                repository.observarPalabras(),
                repository.observarVerbos()
            ) { palabras, verbos ->
                palabras to verbos
            }

            val userProgressFlow: Flow<List<ProgresoUsuarioEntity>> =
                _usuarioIdActual.flatMapLatest { usuarioId ->
                    if (usuarioId.isNullOrBlank()) {
                        flowOf(emptyList())
                    } else {
                        repository.observarProgresoUsuario(usuarioId)
                    }
                }

            val filtersFlow = combine(
                _filtroActual,
                _seccionActual,
                _textoBusqueda,
                _mostrarDialogoRevertir
            ) { filtro, seccion, busqueda, dialogo ->
                FilterState(
                    filtro = filtro,
                    seccion = seccion,
                    busqueda = busqueda,
                    dialogo = dialogo
                )
            }

            combine(
                contentFlow,
                userProgressFlow,
                filtersFlow
            ) { content, progresos, filters ->

                val (palabras, verbos) = content
                val (filtro, seccion, busqueda, dialogo) = filters

                val listaPalabras = palabras.map { palabra ->
                    val progreso = progresos.find {
                        it.contenidoId == palabra.idPalabra &&
                                it.tipoContenido == TipoContenido.PALABRA
                    }

                    PalabraConProgreso(
                        id = palabra.idPalabra,
                        termino = palabra.termino,
                        traduccion = palabra.traduccion,
                        tipoPalabra = palabra.tipoPalabra,
                        estado = progreso?.estadoAprendizaje ?: EstadoAprendizaje.NO_VISTA,
                        fonetica = palabra.fonetica,
                        dificultad = convertirDificultad(palabra.dificultad),
                        esVerbo = false,
                        ejemplo = palabra.ejemplo,
                        ejemploTraduccion = palabra.ejemploTraduccion
                    )
                }

                val listaVerbos = verbos.map { verbo ->
                    val progreso = progresos.find {
                        it.contenidoId == verbo.idVerbo &&
                                it.tipoContenido == TipoContenido.VERBO
                    }

                    PalabraConProgreso(
                        id = verbo.idVerbo,
                        termino = verbo.formaBase,
                        traduccion = verbo.traduccion,
                        tipoPalabra = TipoPalabra.OTRO,
                        estado = progreso?.estadoAprendizaje ?: EstadoAprendizaje.NO_VISTA,
                        fonetica = verbo.fonetica,
                        dificultad = convertirDificultad(verbo.dificultad),
                        esVerbo = true,
                        ejemploIngles = verbo.ejemploIngles,
                        ejemploEspanol = verbo.ejemploEspanol,
                        pasadoSimple = verbo.pasadoSimple,
                        participioPasado = verbo.participioPasado,
                        esIrregular = verbo.esIrregular
                    )
                }

                val listaCompleta = when (seccion) {
                    VocabularioSeccion.PALABRAS -> listaPalabras
                    VocabularioSeccion.VERBOS -> listaVerbos
                }

                val busquedaLimpia = busqueda.trim()

                val filtradas = listaCompleta.filter { item ->
                    val coincideTexto = if (busquedaLimpia.isBlank()) {
                        true
                    } else {
                        item.termino.contains(busquedaLimpia, ignoreCase = true) ||
                                item.traduccion.contains(busquedaLimpia, ignoreCase = true)
                    }

                    val coincideFiltro = when (filtro) {
                        VocabularioFiltro.TODAS -> true

                        VocabularioFiltro.PENDIENTES ->
                            item.estado == EstadoAprendizaje.NO_VISTA

                        VocabularioFiltro.EN_PROGRESO ->
                            item.estado == EstadoAprendizaje.EN_PROGRESO ||
                                    item.estado == EstadoAprendizaje.DIFICIL

                        VocabularioFiltro.APRENDIDAS ->
                            item.estado == EstadoAprendizaje.APRENDIDA
                    }

                    coincideTexto && coincideFiltro
                }

                val total = listaCompleta.size

                val pendientes = listaCompleta.count {
                    it.estado == EstadoAprendizaje.NO_VISTA
                }

                val enProgreso = listaCompleta.count {
                    it.estado == EstadoAprendizaje.EN_PROGRESO ||
                            it.estado == EstadoAprendizaje.DIFICIL
                }

                val aprendidas = listaCompleta.count {
                    it.estado == EstadoAprendizaje.APRENDIDA
                }

                VocabularyUiState(
                    cargando = false,
                    palabrasOriginales = listaCompleta,
                    palabrasFiltradas = filtradas,
                    filtroActual = filtro,
                    seccionActual = seccion,
                    textoBusqueda = busqueda,
                    totalPalabras = total,
                    palabrasPendientes = pendientes,
                    palabrasEnProgreso = enProgreso,
                    palabrasAprendidas = aprendidas,
                    mostrarDialogoRevertir = dialogo
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    private fun convertirDificultad(dificultad: Dificultad): String {
        return when (dificultad) {
            Dificultad.FACIL -> "Básico"
            Dificultad.MEDIA -> "Intermedio"
            Dificultad.DIFICIL -> "Avanzado"
        }
    }

    private data class FilterState(
        val filtro: VocabularioFiltro,
        val seccion: VocabularioSeccion,
        val busqueda: String,
        val dialogo: PalabraConProgreso?
    )

    fun onSearchTextChanged(text: String) {
        _textoBusqueda.value = text
    }

    fun onFiltroChanged(filtro: VocabularioFiltro) {
        _filtroActual.value = filtro
    }

    fun onSeccionChanged(seccion: VocabularioSeccion) {
        _seccionActual.value = seccion
    }

    fun marcarComoAprendido(
        palabraId: String,
        esVerbo: Boolean
    ) {
        viewModelScope.launch {
            val userId = authRepository.obtenerUsuarioActual()?.uid ?: return@launch
            val tipo = if (esVerbo) TipoContenido.VERBO else TipoContenido.PALABRA

            val progresoExistente = repository.obtenerProgresoContenido(
                usuarioId = userId,
                contenidoId = palabraId,
                tipoContenido = tipo
            )

            if (progresoExistente == null) {
                val progresoNuevo = ProgresoUsuarioEntity(
                    id = "${userId}_${palabraId}_${tipo.name}",
                    usuarioId = userId,
                    contenidoId = palabraId,
                    tipoContenido = tipo,
                    estadoAprendizaje = EstadoAprendizaje.APRENDIDA,
                    nivelDominio = 1f,
                    respuestasCorrectas = 1,
                    respuestasIncorrectas = 0,
                    vecesRepasado = 1,
                    aprendido = true,
                    favorito = false,
                    ultimaRevision = System.currentTimeMillis(),
                    proximaRevision = null
                )

                repository.guardarProgresoUsuario(progresoNuevo)
            } else {
                repository.marcarContenidoComoAprendido(
                    usuarioId = userId,
                    contenidoId = palabraId,
                    tipoContenido = tipo
                )
            }

            sincronizarPalabrasAprendidasConFirestore(userId)
            cargarUsuarioActual()
        }
    }

    fun mostrarConfirmacionRevertir(palabra: PalabraConProgreso) {
        _mostrarDialogoRevertir.value = palabra
    }

    fun ocultarConfirmacionRevertir() {
        _mostrarDialogoRevertir.value = null
    }

    fun revertirEstadoAprendido(
        palabraId: String,
        esVerbo: Boolean
    ) {
        viewModelScope.launch {
            val userId = authRepository.obtenerUsuarioActual()?.uid ?: return@launch
            val tipo = if (esVerbo) TipoContenido.VERBO else TipoContenido.PALABRA

            val progresoExistente = repository.obtenerProgresoContenido(
                usuarioId = userId,
                contenidoId = palabraId,
                tipoContenido = tipo
            )

            if (progresoExistente == null) {
                val progresoNuevo = ProgresoUsuarioEntity(
                    id = "${userId}_${palabraId}_${tipo.name}",
                    usuarioId = userId,
                    contenidoId = palabraId,
                    tipoContenido = tipo,
                    estadoAprendizaje = EstadoAprendizaje.EN_PROGRESO,
                    nivelDominio = 0.5f,
                    respuestasCorrectas = 0,
                    respuestasIncorrectas = 0,
                    vecesRepasado = 0,
                    aprendido = false,
                    favorito = false,
                    ultimaRevision = System.currentTimeMillis(),
                    proximaRevision = null
                )

                repository.guardarProgresoUsuario(progresoNuevo)
            } else {
                repository.revertirContenidoAprendido(
                    usuarioId = userId,
                    contenidoId = palabraId,
                    tipoContenido = tipo
                )
            }

            sincronizarPalabrasAprendidasConFirestore(userId)
            ocultarConfirmacionRevertir()
            cargarUsuarioActual()
        }
    }

    private suspend fun sincronizarPalabrasAprendidasConFirestore(userId: String) {
        val progresos = repository.observarProgresoUsuario(userId).first()

        val totalAprendidas = progresos.count { progreso ->
            progreso.estadoAprendizaje == EstadoAprendizaje.APRENDIDA &&
                    (
                            progreso.tipoContenido == TipoContenido.PALABRA ||
                                    progreso.tipoContenido == TipoContenido.VERBO
                            )
        }

        usuarioFirestoreDataSource.actualizarPalabrasAprendidas(
            firebaseUid = userId,
            cantidad = totalAprendidas
        )
    }
}