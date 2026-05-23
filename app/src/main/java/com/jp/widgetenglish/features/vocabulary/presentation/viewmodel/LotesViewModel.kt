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

    fun cargarLotes() {
        observeJob?.cancel()

        observeJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, mensajeError = null) }

            val usuarioId = authRepository.obtenerUsuarioActual()?.uid

            if (usuarioId == null) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        mensajeError = "Usuario no identificado"
                    )
                }
                return@launch
            }

            try {
                vocabularioRepository.sincronizarProgresos(usuarioId)
            } catch (e: Exception) {
                Log.e(TAG, "Error sincronizando progresos", e)
            }

            combine(
                vocabularioRepository.observarLotesConProgreso(usuarioId),
                vocabularioRepository.observarLoteActivo(usuarioId)
            ) { listaLotes, progresoLoteActivo ->
                listaLotes to progresoLoteActivo?.loteId
            }.collectLatest { (lista, idActivo) ->
                _uiState.update {
                    it.copy(
                        lotes = lista,
                        idLoteActivo = idActivo,
                        isLoading = false,
                        mensajeError = null
                    )
                }
            }
        }
    }

    fun activarLote(context: Context, lote: LoteConProgreso) {
        viewModelScope.launch {
            val usuarioId = authRepository.obtenerUsuarioActual()?.uid

            if (usuarioId == null) {
                _uiState.update {
                    it.copy(mensajeError = "Usuario no identificado")
                }
                return@launch
            }

            try {
                vocabularioRepository.activarLote(usuarioId, lote.lote.idLote)

                WidgetPreferences.guardarLoteActivo(
                    context = context.applicationContext,
                    loteId = lote.lote.idLote,
                    loteNombre = lote.lote.nombre
                )

                WidgetPreferences.guardarUserId(
                    context = context.applicationContext,
                    userId = usuarioId
                )

                WidgetPreferences.reiniciarIndice(context.applicationContext)

                WordWidgetProvider.updateAll(context.applicationContext)

                Log.d(
                    TAG,
                    "Widget actualizado con nuevo lote activo: ${lote.lote.nombre}"
                )

            } catch (e: Exception) {
                Log.e(TAG, "Error activando lote o actualizando widget", e)

                _uiState.update {
                    it.copy(mensajeError = "No se pudo activar el lote")
                }
            }
        }
    }

    fun reiniciarProgreso(context: Context, loteId: String) {
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
        viewModelScope.launch {
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
                contenidos.map { contenido ->
                    val progreso = progresos.find {
                        it.contenidoId == contenido.contenidoId &&
                                it.tipoContenido == contenido.tipoContenido
                    }

                    if (contenido.tipoContenido == TipoContenido.VERBO) {
                        val verbo = vocabularioRepository.obtenerVerboPorId(
                            contenido.contenidoId
                        )

                        PalabraConProgreso(
                            id = verbo?.idVerbo.orEmpty(),
                            termino = verbo?.formaBase.orEmpty(),
                            traduccion = verbo?.traduccion.orEmpty(),
                            fonetica = verbo?.fonetica,
                            estado = progreso?.estadoAprendizaje
                                ?: EstadoAprendizaje.NO_VISTA,
                            esVerbo = true,
                            tipoPalabra = TipoPalabra.VERBO
                        )
                    } else {
                        val palabra = vocabularioRepository.obtenerPalabraPorId(
                            contenido.contenidoId
                        )

                        PalabraConProgreso(
                            id = palabra?.idPalabra.orEmpty(),
                            termino = palabra?.termino.orEmpty(),
                            traduccion = palabra?.traduccion.orEmpty(),
                            fonetica = palabra?.fonetica,
                            estado = progreso?.estadoAprendizaje
                                ?: EstadoAprendizaje.NO_VISTA,
                            esVerbo = false,
                            tipoPalabra = palabra?.tipoPalabra
                                ?: TipoPalabra.SUSTANTIVO
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

    companion object {
        private const val TAG = "LotesViewModel"
    }
}