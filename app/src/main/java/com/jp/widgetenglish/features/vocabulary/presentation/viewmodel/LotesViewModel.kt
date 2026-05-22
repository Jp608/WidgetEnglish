package com.jp.widgetenglish.features.vocabulary.presentation.viewmodel

import android.content.Context
import androidx.glance.appwidget.updateAll
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jp.widgetenglish.data.local.dao.LoteConProgreso
import com.jp.widgetenglish.data.local.datastore.WidgetPreferences
import com.jp.widgetenglish.data.local.entity.LoteEntity
import com.jp.widgetenglish.data.local.entity.TipoContenido
import com.jp.widgetenglish.data.repository.VocabularioRepository
import com.jp.widgetenglish.data.repository.auth.AuthRepository
import com.jp.widgetenglish.features.widget.WordWidget
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
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
            _uiState.update { it.copy(isLoading = true) }
            
            val usuarioId = authRepository.obtenerUsuarioActual()?.uid
            
            if (usuarioId != null) {
                // Sincronizar progresos al cargar (Asegura HU15)
                try {
                    vocabularioRepository.sincronizarProgresos(usuarioId)
                } catch (e: Exception) {}

                combine(
                    vocabularioRepository.observarLotesConProgreso(usuarioId),
                    vocabularioRepository.observarLoteActivo(usuarioId)
                ) { listaLotes, progresoLoteActivo ->
                    Pair(listaLotes, progresoLoteActivo?.loteId)
                }.collectLatest { (lista, idActivo) ->
                    _uiState.update { 
                        it.copy(
                            lotes = lista,
                            idLoteActivo = idActivo,
                            isLoading = false
                        )
                    }
                }
            } else {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        mensajeError = "Usuario no identificado"
                    )
                }
            }
        }
    }

    fun activarLote(context: Context, lote: LoteConProgreso) {
        viewModelScope.launch {
            val usuarioId = authRepository.obtenerUsuarioActual()?.uid
            if (usuarioId != null) {
                // 1. Activar en Base de Datos
                vocabularioRepository.activarLote(usuarioId, lote.lote.idLote)
                
                // 2. Activar en DataStore para el Widget (Sincronización Crítica)
                WidgetPreferences.guardarLoteActivo(context, lote.lote.idLote, lote.lote.nombre)
                WidgetPreferences.guardarUserId(context, usuarioId)
                WidgetPreferences.reiniciarIndice(context) // Empezar desde la primera palabra del nuevo lote

                // 3. Forzar actualización inmediata del Widget
                try {
                    WordWidget().updateAll(context)
                    android.util.Log.d("LotesViewModel", "Widget forzado a actualizar con nuevo lote: ${lote.lote.nombre}")
                } catch (e: Exception) {
                    android.util.Log.e("LotesViewModel", "Error al notificar cambio de lote al widget", e)
                }
            }
        }
    }

    fun reiniciarProgreso(context: Context, loteId: String) {
        viewModelScope.launch {
            val usuarioId = authRepository.obtenerUsuarioActual()?.uid
            if (usuarioId != null) {
                vocabularioRepository.reiniciarProgresoLote(usuarioId, loteId)
                
                // Forzar actualización inmediata del Widget
                try {
                    WordWidget().updateAll(context)
                } catch (e: Exception) {
                    android.util.Log.e("LotesViewModel", "Widget update error", e)
                }
            }
        }
    }

    fun cargarDetalleLote(loteId: String) {
        viewModelScope.launch {
            val usuarioId = authRepository.obtenerUsuarioActual()?.uid ?: return@launch
            
            val lote = vocabularioRepository.obtenerLotePorId(loteId)
            _uiState.update { it.copy(loteSeleccionado = lote) }

            combine(
                vocabularioRepository.observarContenidoDeLote(loteId),
                vocabularioRepository.observarProgresoUsuario(usuarioId)
            ) { contenidos, progresos ->
                contenidos.map { contenido ->
                    val prog = progresos.find { 
                        it.contenidoId == contenido.contenidoId && it.tipoContenido == contenido.tipoContenido 
                    }
                    
                    if (contenido.tipoContenido == TipoContenido.VERBO) {
                        val v = vocabularioRepository.obtenerVerboPorId(contenido.contenidoId)
                        PalabraConProgreso(
                            id = v?.idVerbo ?: "",
                            termino = v?.formaBase ?: "",
                            traduccion = v?.traduccion ?: "",
                            fonetica = v?.fonetica,
                            estado = prog?.estadoAprendizaje ?: com.jp.widgetenglish.data.local.entity.EstadoAprendizaje.NO_VISTA,
                            esVerbo = true,
                            tipoPalabra = com.jp.widgetenglish.data.local.entity.TipoPalabra.VERBO
                        )
                    } else {
                        val p = vocabularioRepository.obtenerPalabraPorId(contenido.contenidoId)
                        PalabraConProgreso(
                            id = p?.idPalabra ?: "",
                            termino = p?.termino ?: "",
                            traduccion = p?.traduccion ?: "",
                            fonetica = p?.fonetica,
                            estado = prog?.estadoAprendizaje ?: com.jp.widgetenglish.data.local.entity.EstadoAprendizaje.NO_VISTA,
                            esVerbo = false,
                            tipoPalabra = p?.tipoPalabra ?: com.jp.widgetenglish.data.local.entity.TipoPalabra.SUSTANTIVO
                        )
                    }
                }
            }.collectLatest { lista ->
                _uiState.update { it.copy(palabrasDelLote = lista) }
            }
        }
    }
}
