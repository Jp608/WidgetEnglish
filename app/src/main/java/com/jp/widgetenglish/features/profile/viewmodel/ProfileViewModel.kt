package com.jp.widgetenglish.features.profile.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jp.widgetenglish.data.local.dao.UsuarioDao
import com.jp.widgetenglish.data.local.datastore.LearningPreferences
import com.jp.widgetenglish.data.local.datastore.LearningSettings
import com.jp.widgetenglish.data.local.datastore.ModoSeleccionContenido
import com.jp.widgetenglish.data.local.datastore.WidgetPreferences
import com.jp.widgetenglish.data.local.entity.RolUsuario
import com.jp.widgetenglish.data.local.entity.UsuarioEntity
import com.jp.widgetenglish.data.repository.auth.AuthRepository
import com.jp.widgetenglish.features.widget.WordWidgetProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProfileUiState(
    val usuario: UsuarioEntity? = null,
    val cargando: Boolean = true,
    val autenticado: Boolean = true,
    val error: String? = null,

    val learningSettings: LearningSettings = LearningSettings(
        modoSeleccionContenido = ModoSeleccionContenido.INTELIGENTE,
        objetivoDiarioAutomatico = true,
        objetivoDiarioManual = LearningPreferences.MIN_OBJETIVO_DIARIO,
        objetivoDiarioActual = LearningPreferences.MIN_OBJETIVO_DIARIO
    )
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

                _uiState.value = _uiState.value.copy(
                    usuario = usuarioActualizado,
                    cargando = false,
                    autenticado = true,
                    error = null
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

                _uiState.value = _uiState.value.copy(
                    usuario = nuevoUsuario,
                    cargando = false,
                    autenticado = true,
                    error = null
                )
            }
        }
    }

    fun cargarConfiguracionAprendizaje(context: Context) {
        viewModelScope.launch {
            try {
                val settings = LearningPreferences.obtenerConfiguracionRapida(
                    context = context.applicationContext
                )

                _uiState.value = _uiState.value.copy(
                    learningSettings = settings
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error cargando configuración de aprendizaje", e)

                _uiState.value = _uiState.value.copy(
                    error = "No se pudo cargar la configuración de aprendizaje"
                )
            }
        }
    }

    fun cambiarModoSeleccionContenido(
        context: Context,
        modo: ModoSeleccionContenido
    ) {
        viewModelScope.launch {
            try {
                val appContext = context.applicationContext

                LearningPreferences.guardarModoSeleccionContenido(
                    context = appContext,
                    modo = modo
                )

                reiniciarWidgetPorCambioDeConfiguracion(appContext)
                cargarConfiguracionAprendizaje(appContext)

            } catch (e: Exception) {
                Log.e(TAG, "Error cambiando modo de selección", e)

                _uiState.value = _uiState.value.copy(
                    error = "No se pudo actualizar el modo de aprendizaje"
                )
            }
        }
    }

    fun cambiarObjetivoDiarioAutomatico(
        context: Context,
        automatico: Boolean
    ) {
        viewModelScope.launch {
            try {
                val appContext = context.applicationContext

                LearningPreferences.guardarObjetivoDiarioAutomatico(
                    context = appContext,
                    automatico = automatico
                )

                reiniciarWidgetPorCambioDeConfiguracion(appContext)
                cargarConfiguracionAprendizaje(appContext)

            } catch (e: Exception) {
                Log.e(TAG, "Error cambiando objetivo automático", e)

                _uiState.value = _uiState.value.copy(
                    error = "No se pudo actualizar el objetivo diario"
                )
            }
        }
    }

    fun cambiarObjetivoDiarioManual(
        context: Context,
        objetivo: Int
    ) {
        viewModelScope.launch {
            try {
                val appContext = context.applicationContext

                LearningPreferences.guardarObjetivoDiarioManual(
                    context = appContext,
                    objetivo = objetivo
                )

                reiniciarWidgetPorCambioDeConfiguracion(appContext)
                cargarConfiguracionAprendizaje(appContext)

            } catch (e: Exception) {
                Log.e(TAG, "Error cambiando objetivo manual", e)

                _uiState.value = _uiState.value.copy(
                    error = "No se pudo actualizar el número de palabras"
                )
            }
        }
    }

    fun reiniciarConfiguracionAprendizaje(context: Context) {
        viewModelScope.launch {
            try {
                val appContext = context.applicationContext

                LearningPreferences.reiniciarConfiguracionAprendizaje(appContext)

                reiniciarWidgetPorCambioDeConfiguracion(appContext)
                cargarConfiguracionAprendizaje(appContext)

            } catch (e: Exception) {
                Log.e(TAG, "Error reiniciando configuración de aprendizaje", e)

                _uiState.value = _uiState.value.copy(
                    error = "No se pudo reiniciar la configuración"
                )
            }
        }
    }

    private suspend fun reiniciarWidgetPorCambioDeConfiguracion(context: Context) {
        WidgetPreferences.reiniciarIndice(context)
        WordWidgetProvider.updateAll(context)
    }

    fun cerrarSesion() {
        authRepository.cerrarSesion()

        _uiState.value = ProfileUiState(
            usuario = null,
            cargando = false,
            autenticado = false
        )
    }

    companion object {
        private const val TAG = "ProfileViewModel"
    }
}