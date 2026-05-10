package com.jp.widgetenglish.features.profile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jp.widgetenglish.data.local.dao.UsuarioDao
import com.jp.widgetenglish.data.local.entity.UsuarioEntity
import com.jp.widgetenglish.data.repository.auth.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProfileUiState(
    val usuario: UsuarioEntity? = null,
    val cargando: Boolean = true
)

class ProfileViewModel(
    private val authRepository: AuthRepository,
    private val usuarioDao: UsuarioDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        cargarDatosUsuario()
    }

    private fun cargarDatosUsuario() {
        val firebaseUser = authRepository.obtenerUsuarioActual()
        if (firebaseUser != null) {
            viewModelScope.launch {
                val usuarioLocal = usuarioDao.obtenerUsuarioPorFirebaseUid(firebaseUser.uid)
                _uiState.value = ProfileUiState(usuario = usuarioLocal, cargando = false)
            }
        } else {
            _uiState.value = ProfileUiState(cargando = false)
        }
    }

    fun cerrarSesion() {
        authRepository.cerrarSesion()
    }
}
