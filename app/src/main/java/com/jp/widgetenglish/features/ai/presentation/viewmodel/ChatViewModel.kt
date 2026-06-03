package com.jp.widgetenglish.features.ai.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.jp.widgetenglish.data.repository.ChatRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ChatViewModel(private val repository: ChatRepository) : ViewModel() {

    private val _historyState = MutableStateFlow(ChatHistoryUiState(cargando = true))
    val historyState: StateFlow<ChatHistoryUiState> = _historyState.asStateFlow()

    private val _roomState = MutableStateFlow(ChatRoomUiState())
    val roomState: StateFlow<ChatRoomUiState> = _roomState.asStateFlow()
    private var roomMessagesJob: Job? = null

    init {
        observarSesiones()
    }

    private fun observarSesiones() {
        viewModelScope.launch {
            repository.observarSesiones().collect { lista ->
                _historyState.value = ChatHistoryUiState(sesiones = lista, cargando = false)
            }
        }
    }

    fun crearNuevaSesion(titulo: String, onCreada: (String) -> Unit) {
        viewModelScope.launch {
            val id = repository.crearNuevaSesion(titulo)
            onCreada(id)
        }
    }

    fun cargarMensajes(sessionId: String) {
        roomMessagesJob?.cancel()
        _roomState.update {
            it.copy(
                sessionId = sessionId,
                mensajes = emptyList(),
                error = null,
                cargandoRespuesta = false
            )
        }
        roomMessagesJob = viewModelScope.launch {
            repository.observarMensajes(sessionId).collect { lista ->
                _roomState.update { it.copy(mensajes = lista) }
            }
        }
    }

    fun enviarMensaje(contenido: String) {
        val sessionId = _roomState.value.sessionId ?: return
        if (contenido.isBlank()) return

        viewModelScope.launch {
            _roomState.update {
                it.copy(
                    cargandoRespuesta = true,
                    error = null
                )
            }
            try {
                repository.enviarMensaje(sessionId, contenido)
            } catch (e: Exception) {
                _roomState.update {
                    it.copy(
                        error = e.message ?: "No se pudo enviar el mensaje."
                    )
                }
            } finally {
                _roomState.update { it.copy(cargandoRespuesta = false) }
            }
        }
    }

    fun eliminarSesion(sessionId: String) {
        viewModelScope.launch {
            repository.eliminarSesion(sessionId)
        }
    }
}

class ChatViewModelFactory(private val repository: ChatRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ChatViewModel(repository) as T
    }
}
