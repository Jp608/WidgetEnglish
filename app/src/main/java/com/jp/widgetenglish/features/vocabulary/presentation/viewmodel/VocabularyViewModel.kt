package com.jp.widgetenglish.features.vocabulary.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jp.widgetenglish.data.local.dao.UsuarioDao
import com.jp.widgetenglish.data.local.entity.EstadoAprendizaje
import com.jp.widgetenglish.data.local.entity.TipoContenido
import com.jp.widgetenglish.data.repository.VocabularioRepository
import com.jp.widgetenglish.data.repository.auth.AuthRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class VocabularyViewModel(
    private val repository: VocabularioRepository,
    private val authRepository: AuthRepository,
    private val usuarioDao: UsuarioDao
) : ViewModel() {

    private val _filtroActual = MutableStateFlow(VocabularioFiltro.TODAS)
    private val _textoBusqueda = MutableStateFlow("")
    private val _mostrarDialogoRevertir = MutableStateFlow<PalabraConProgreso?>(null)

    private val _uiState = MutableStateFlow(VocabularyUiState())
    val uiState: StateFlow<VocabularyUiState> = _uiState.asStateFlow()

    init {
        cargarVocabulario()
    }

    private fun cargarVocabulario() {
        viewModelScope.launch {
            val firebaseUser = authRepository.obtenerUsuarioActual() ?: return@launch
            val userId = firebaseUser.uid

            combine(
                repository.observarPalabras(),
                repository.observarProgresoUsuario(userId),
                _filtroActual,
                _textoBusqueda,
                _mostrarDialogoRevertir
            ) { palabras, progresos, filtro, busqueda, dialogo ->
                
                val listaConProgreso = palabras.map { palabra ->
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
                        }
                    )
                }

                val filtradas = listaConProgreso.filter { palabra ->
                    val coincideTexto = palabra.termino.contains(busqueda, ignoreCase = true) ||
                            palabra.traduccion.contains(busqueda, ignoreCase = true)

                    val coincideFiltro = when (filtro) {
                        VocabularioFiltro.TODAS -> true
                        VocabularioFiltro.PENDIENTES -> palabra.estado == EstadoAprendizaje.NO_VISTA
                        VocabularioFiltro.EN_PROGRESO -> palabra.estado == EstadoAprendizaje.EN_PROGRESO || palabra.estado == EstadoAprendizaje.DIFICIL
                        VocabularioFiltro.APRENDIDAS -> palabra.estado == EstadoAprendizaje.APRENDIDA
                    }
                    coincideTexto && coincideFiltro
                }

                val total = listaConProgreso.size
                val pendientes = listaConProgreso.count { it.estado == EstadoAprendizaje.NO_VISTA }
                val enProgreso = listaConProgreso.count { it.estado == EstadoAprendizaje.EN_PROGRESO || it.estado == EstadoAprendizaje.DIFICIL }
                val aprendidas = listaConProgreso.count { it.estado == EstadoAprendizaje.APRENDIDA }

                VocabularyUiState(
                    cargando = false,
                    palabrasOriginales = listaConProgreso,
                    palabrasFiltradas = filtradas,
                    filtroActual = filtro,
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

    fun onSearchTextChanged(text: String) {
        _textoBusqueda.value = text
    }

    fun onFiltroChanged(filtro: VocabularioFiltro) {
        _filtroActual.value = filtro
    }

    fun marcarComoAprendido(palabraId: String) {
        viewModelScope.launch {
            val userId = authRepository.obtenerUsuarioActual()?.uid ?: return@launch
            repository.marcarContenidoComoAprendido(userId, palabraId, TipoContenido.PALABRA)
        }
    }

    fun mostrarConfirmacionRevertir(palabra: PalabraConProgreso) {
        _mostrarDialogoRevertir.value = palabra
    }

    fun ocultarConfirmacionRevertir() {
        _mostrarDialogoRevertir.value = null
    }

    fun revertirEstadoAprendido(palabraId: String) {
        viewModelScope.launch {
            val userId = authRepository.obtenerUsuarioActual()?.uid ?: return@launch
            repository.revertirContenidoAprendido(userId, palabraId, TipoContenido.PALABRA)
            ocultarConfirmacionRevertir()
        }
    }
}