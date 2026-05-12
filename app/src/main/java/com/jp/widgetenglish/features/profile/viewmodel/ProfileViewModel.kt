package com.jp.widgetenglish.features.profile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jp.widgetenglish.data.local.dao.UsuarioDao
import com.jp.widgetenglish.data.local.entity.RolUsuario
import com.jp.widgetenglish.data.local.entity.UsuarioEntity
import com.jp.widgetenglish.data.repository.auth.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProfileUiState(
    val usuario: UsuarioEntity? = null,
    val cargando: Boolean = true,
    val autenticado: Boolean = true,
    val error: String? = null
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

    fun cargarDatosUsuario() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                cargando = true,
                error = null
            )

            val firebaseUser = authRepository.obtenerUsuarioActual()

            if (firebaseUser == null) {
                _uiState.value = ProfileUiState(
                    usuario = null,
                    cargando = false,
                    autenticado = false,
                    error = "No hay usuario autenticado"
                )
                return@launch
            }

            val usuarioLocal = usuarioDao.obtenerUsuarioPorFirebaseUid(firebaseUser.uid)

            if (usuarioLocal != null) {
                val usuarioActualizado = usuarioLocal.copy(
                    nombre = firebaseUser.displayName ?: usuarioLocal.nombre,
                    correo = firebaseUser.email ?: usuarioLocal.correo,
                    avatar = firebaseUser.photoUrl?.toString() ?: usuarioLocal.avatar,
                    ultimoAcceso = System.currentTimeMillis()
                )

                usuarioDao.actualizarUsuario(usuarioActualizado)

                _uiState.value = ProfileUiState(
                    usuario = usuarioActualizado,
                    cargando = false,
                    autenticado = true
                )
            } else {
                val nuevoUsuario = UsuarioEntity(
                    idUsuario = firebaseUser.uid,
                    firebaseUid = firebaseUser.uid,
                    nombre = firebaseUser.displayName ?: "Usuario",
                    correo = firebaseUser.email ?: "",
                    avatar = firebaseUser.photoUrl?.toString(),
                    rol = RolUsuario.USUARIO,
                    activo = true,
                    fechaRegistro = System.currentTimeMillis(),
                    ultimoAcceso = System.currentTimeMillis()
                )

                usuarioDao.insertarUsuario(nuevoUsuario)

                _uiState.value = ProfileUiState(
                    usuario = nuevoUsuario,
                    cargando = false,
                    autenticado = true
                )
            }
        }
    }

    fun cerrarSesion() {
        authRepository.cerrarSesion()
        _uiState.value = ProfileUiState(
            usuario = null,
            cargando = false,
            autenticado = false
        )
    }
}