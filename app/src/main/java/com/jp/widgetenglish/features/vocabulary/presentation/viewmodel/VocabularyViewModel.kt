package com.jp.widgetenglish.features.vocabulary.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jp.widgetenglish.data.local.entity.EstadoAprendizaje
import com.jp.widgetenglish.data.local.entity.TipoContenido
import com.jp.widgetenglish.data.local.entity.TipoPalabra
import com.jp.widgetenglish.data.repository.VocabularioRepository
import com.jp.widgetenglish.data.repository.auth.AuthRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class VocabularyViewModel(
    private val repository: VocabularioRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _filtroActual = MutableStateFlow(VocabularioFiltro.TODAS)
    private val _seccionActual = MutableStateFlow(VocabularioSeccion.PALABRAS)
    private val _textoBusqueda = MutableStateFlow("")
    private val _mostrarDialogoRevertir = MutableStateFlow<PalabraConProgreso?>(null)

    private val _uiState = MutableStateFlow(VocabularyUiState())
    val uiState: StateFlow<VocabularyUiState> = _uiState.asStateFlow()

    init {
        cargarVocabulario()
    }

    private fun cargarVocabulario() {
        viewModelScope.launch {
            // Flujo de datos base (Palabras y Verbos) - Siempre disponible
            val contentFlow = combine(
                repository.observarPalabras(),
                repository.observarVerbos()
            ) { palabras, verbos ->
                palabras to verbos
            }

            // Flujo de usuario (Progreso) - Puede ser vacío inicialmente
            val userProgressFlow = authRepository.obtenerUsuarioActual()?.let { user ->
                repository.observarProgresoUsuario(user.uid)
            } ?: flowOf(emptyList())

            // Flujo de filtros de UI
            val filtersFlow = combine(
                _filtroActual,
                _seccionActual,
                _textoBusqueda,
                _mostrarDialogoRevertir
            ) { filtro, seccion, busqueda, dialogo ->
                FilterState(filtro, seccion, busqueda, dialogo)
            }

            // Combinación final: Contenido + Progreso + Filtros
            combine(contentFlow, userProgressFlow, filtersFlow) { content, progresos, filters ->
                val (palabras, verbos) = content
                val (filtro, seccion, busqueda, dialogo) = filters

                val listaPalabras = palabras.map { palabra ->
                    val progreso = progresos.find { it.contenidoId == palabra.idPalabra && it.tipoContenido == TipoContenido.PALABRA }
                    PalabraConProgreso(
                        id = palabra.idPalabra,
                        termino = palabra.termino,
                        traduccion = palabra.traduccion,
                        tipoPalabra = palabra.tipoPalabra,
                        estado = progreso?.estadoAprendizaje ?: EstadoAprendizaje.NO_VISTA,
                        fonetica = palabra.fonetica,
                        dificultad = when(palabra.dificultad) {
                            com.jp.widgetenglish.data.local.entity.Dificultad.FACIL -> "Básico"
                            com.jp.widgetenglish.data.local.entity.Dificultad.MEDIA -> "Intermedio"
                            com.jp.widgetenglish.data.local.entity.Dificultad.DIFICIL -> "Avanzado"
                        },
                        esVerbo = false
                    )
                }

                val listaVerbos = verbos.map { verbo ->
                    val progreso = progresos.find { it.contenidoId == verbo.idVerbo && it.tipoContenido == TipoContenido.VERBO }
                    PalabraConProgreso(
                        id = verbo.idVerbo,
                        termino = verbo.formaBase,
                        traduccion = verbo.traduccion,
                        tipoPalabra = TipoPalabra.OTRO,
                        estado = progreso?.estadoAprendizaje ?: EstadoAprendizaje.NO_VISTA,
                        fonetica = verbo.fonetica,
                        dificultad = when(verbo.dificultad) {
                            com.jp.widgetenglish.data.local.entity.Dificultad.FACIL -> "Básico"
                            com.jp.widgetenglish.data.local.entity.Dificultad.MEDIA -> "Intermedio"
                            com.jp.widgetenglish.data.local.entity.Dificultad.DIFICIL -> "Avanzado"
                        },
                        esVerbo = true,
                        pasadoSimple = verbo.pasadoSimple,
                        participioPasado = verbo.participioPasado,
                        esIrregular = verbo.esIrregular
                    )
                }

                val listaCompleta = when (seccion) {
                    VocabularioSeccion.PALABRAS -> listaPalabras.filter { it.tipoPalabra == TipoPalabra.SUSTANTIVO }
                    VocabularioSeccion.ADJETIVOS -> listaPalabras.filter { it.tipoPalabra == TipoPalabra.ADJETIVO }
                    VocabularioSeccion.VERBOS -> listaVerbos
                }

                val filtradas = listaCompleta.filter { item ->
                    val coincideTexto = item.termino.contains(busqueda, ignoreCase = true) ||
                            item.traduccion.contains(busqueda, ignoreCase = true)

                    val coincideFiltro = when (filtro) {
                        VocabularioFiltro.TODAS -> true
                        VocabularioFiltro.PENDIENTES -> item.estado == EstadoAprendizaje.NO_VISTA
                        VocabularioFiltro.EN_PROGRESO -> item.estado == EstadoAprendizaje.EN_PROGRESO || item.estado == EstadoAprendizaje.DIFICIL
                        VocabularioFiltro.APRENDIDAS -> item.estado == EstadoAprendizaje.APRENDIDA
                    }
                    coincideTexto && coincideFiltro
                }

                val total = listaCompleta.size
                val pendientes = listaCompleta.count { it.estado == EstadoAprendizaje.NO_VISTA }
                val enProgreso = listaCompleta.count { it.estado == EstadoAprendizaje.EN_PROGRESO || it.estado == EstadoAprendizaje.DIFICIL }
                val aprendidas = listaCompleta.count { it.estado == EstadoAprendizaje.APRENDIDA }

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

    fun marcarComoAprendido(palabraId: String, esVerbo: Boolean) {
        viewModelScope.launch {
            val userId = authRepository.obtenerUsuarioActual()?.uid ?: return@launch
            val tipo = if (esVerbo) TipoContenido.VERBO else TipoContenido.PALABRA
            repository.marcarContenidoComoAprendido(userId, palabraId, tipo)
        }
    }

    fun mostrarConfirmacionRevertir(palabra: PalabraConProgreso) {
        _mostrarDialogoRevertir.value = palabra
    }

    fun ocultarConfirmacionRevertir() {
        _mostrarDialogoRevertir.value = null
    }

    fun revertirEstadoAprendido(palabraId: String, esVerbo: Boolean) {
        viewModelScope.launch {
            val userId = authRepository.obtenerUsuarioActual()?.uid ?: return@launch
            val tipo = if (esVerbo) TipoContenido.VERBO else TipoContenido.PALABRA
            repository.revertirContenidoAprendido(userId, palabraId, tipo)
            ocultarConfirmacionRevertir()
        }
    }
}
