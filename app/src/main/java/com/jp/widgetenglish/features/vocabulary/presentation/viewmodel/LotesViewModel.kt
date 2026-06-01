package com.jp.widgetenglish.features.vocabulary.presentation.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jp.widgetenglish.data.local.dao.LoteConProgreso
import com.jp.widgetenglish.data.local.datastore.WidgetPreferences
import com.jp.widgetenglish.data.local.entity.EstadoAprendizaje
import com.jp.widgetenglish.data.local.entity.LoteEntity
import com.jp.widgetenglish.data.local.entity.TipoContenido
import com.jp.widgetenglish.data.local.entity.TipoPalabra
import com.jp.widgetenglish.data.repository.VocabularioRepository
import com.jp.widgetenglish.data.repository.auth.AuthRepository
import com.jp.widgetenglish.features.widget.WordWidgetProvider
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LotesUiState(
    val lotes: List<LoteConProgreso> = emptyList(),
    val idLoteActivo: String? = null,
    val loteSeleccionado: LoteEntity? = null,
    val palabrasDelLote: List<PalabraConProgreso> = emptyList(),
    val isLoading: Boolean = false,
    val mensajeError: String? = null
)

class LotesViewModel(
    private val vocabularioRepository: VocabularioRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LotesUiState())
    val uiState: StateFlow<LotesUiState> = _uiState.asStateFlow()

    private var observeJob: Job? = null
    private var detalleJob: Job? = null
    private var usuarioObservadoId: String? = null

    private var loteActivandoseId: String? = null

    fun cargarLotes() {
        val usuarioId = authRepository.obtenerUsuarioActual()?.uid

        if (observeJob?.isActive == true && usuarioObservadoId == usuarioId) {
            return
        }

        observeJob?.cancel()
        detalleJob?.cancel()
        loteActivandoseId = null
        usuarioObservadoId = usuarioId

        if (usuarioId == null) {
            _uiState.value = LotesUiState(
                isLoading = false,
                mensajeError = "Usuario no identificado"
            )
            return
        }

        _uiState.value = LotesUiState(isLoading = true)

        observeJob = viewModelScope.launch {
            // Sincroniza progresos locales en segundo plano, sin bloquear la pantalla.
            viewModelScope.launch {
                try {
                    vocabularioRepository.sincronizarProgresos(usuarioId)
                } catch (e: Exception) {
                    Log.e(TAG, "Error sincronizando progresos", e)
                }
            }

            combine(
                vocabularioRepository.observarLotesConProgreso(usuarioId),
                vocabularioRepository.observarLoteActivo(usuarioId)
            ) { listaLotes, progresoLoteActivo ->
                listaLotes to progresoLoteActivo?.loteId
            }.collectLatest { (lista, idActivoRoom) ->

                val idPendiente = loteActivandoseId

                val idActivoFinal = when {
                    idPendiente != null && idActivoRoom != idPendiente -> {
                        idPendiente
                    }

                    else -> {
                        if (idPendiente != null && idActivoRoom == idPendiente) {
                            loteActivandoseId = null
                        }

                        idActivoRoom
                    }
                }

                _uiState.update {
                    it.copy(
                        lotes = lista,
                        idLoteActivo = idActivoFinal,
                        isLoading = false,
                        mensajeError = null
                    )
                }
            }
        }
    }

    fun activarLote(
        context: Context,
        lote: LoteConProgreso
    ) {
        viewModelScope.launch {
            val usuarioId = authRepository.obtenerUsuarioActual()?.uid

            if (usuarioId == null) {
                _uiState.update {
                    it.copy(mensajeError = "Usuario no identificado")
                }
                return@launch
            }

            val loteId = lote.lote.idLote
            val loteNombre = lote.lote.nombre

            if (_uiState.value.idLoteActivo == loteId) {
                return@launch
            }

            loteActivandoseId = loteId

            // Activación optimista: se ve activo inmediatamente y no parpadea.
            _uiState.update {
                it.copy(
                    idLoteActivo = loteId,
                    mensajeError = null
                )
            }

            // DataStore primero, para que el widget también responda rápido.
            try {
                WidgetPreferences.guardarLoteActivo(
                    context = context.applicationContext,
                    loteId = loteId,
                    loteNombre = loteNombre
                )

                WidgetPreferences.guardarUserId(
                    context = context.applicationContext,
                    userId = usuarioId
                )

                WidgetPreferences.reiniciarIndice(context.applicationContext)
            } catch (e: Exception) {
                Log.e(TAG, "Error guardando lote activo en DataStore", e)
            }

            try {
                vocabularioRepository.activarLote(
                    usuarioId = usuarioId,
                    loteId = loteId
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error activando lote", e)

                loteActivandoseId = null

                _uiState.update {
                    it.copy(
                        mensajeError = "No se pudo activar el lote"
                    )
                }

                return@launch
            }

            try {
                WordWidgetProvider.updateAll(context.applicationContext)

                Log.d(
                    TAG,
                    "Widget actualizado con nuevo lote activo: $loteNombre"
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error actualizando widget", e)
            }
        }
    }

    fun reiniciarProgreso(
        context: Context,
        loteId: String
    ) {
        viewModelScope.launch {
            val usuarioId = authRepository.obtenerUsuarioActual()?.uid

            if (usuarioId == null) {
                _uiState.update {
                    it.copy(mensajeError = "Usuario no identificado")
                }
                return@launch
            }

            try {
                vocabularioRepository.reiniciarProgresoLote(usuarioId, loteId)

                WidgetPreferences.reiniciarIndice(context.applicationContext)

                WordWidgetProvider.updateAll(context.applicationContext)

                Log.d(TAG, "Progreso reiniciado y widget actualizado")
            } catch (e: Exception) {
                Log.e(TAG, "Error reiniciando progreso o actualizando widget", e)

                _uiState.update {
                    it.copy(mensajeError = "No se pudo reiniciar el progreso")
                }
            }
        }
    }

    fun cargarDetalleLote(loteId: String) {
        detalleJob?.cancel()

        detalleJob = viewModelScope.launch {
            val usuarioId = authRepository.obtenerUsuarioActual()?.uid

            if (usuarioId == null) {
                _uiState.update {
                    it.copy(mensajeError = "Usuario no identificado")
                }
                return@launch
            }

            val lote = vocabularioRepository.obtenerLotePorId(loteId)

            _uiState.update {
                it.copy(loteSeleccionado = lote)
            }

            combine(
                vocabularioRepository.observarContenidoDeLote(loteId),
                vocabularioRepository.observarProgresoUsuario(usuarioId)
            ) { contenidos, progresos ->
                contenidos.mapNotNull { contenido ->
                    val progreso = progresos.find {
                        it.contenidoId == contenido.contenidoId &&
                                it.tipoContenido == contenido.tipoContenido
                    }

                    if (contenido.tipoContenido == TipoContenido.VERBO) {
                        val verbo = vocabularioRepository.obtenerVerboPorId(
                            contenido.contenidoId
                        ) ?: return@mapNotNull null

                        if (verbo.formaBase.isBlank() || verbo.traduccion.isBlank()) {
                            return@mapNotNull null
                        }

                        PalabraConProgreso(
                            id = verbo.idVerbo,
                            termino = verbo.formaBase,
                            traduccion = verbo.traduccion,
                            fonetica = verbo.fonetica,
                            estado = progreso?.estadoAprendizaje ?: EstadoAprendizaje.NO_VISTA,
                            esVerbo = true,
                            tipoPalabra = TipoPalabra.VERBO
                        )
                    } else {
                        val palabra = vocabularioRepository.obtenerPalabraPorId(
                            contenido.contenidoId
                        ) ?: return@mapNotNull null

                        if (palabra.termino.isBlank() || palabra.traduccion.isBlank()) {
                            return@mapNotNull null
                        }

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
            }.collectLatest { lista ->
                _uiState.update {
                    it.copy(palabrasDelLote = lista)
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        observeJob?.cancel()
        detalleJob?.cancel()
    }

    companion object {
        private const val TAG = "LotesViewModel"
    }
}
