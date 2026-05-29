package com.jp.widgetenglish.features.profile.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jp.widgetenglish.data.local.datastore.DailyGoalPreferences
import com.jp.widgetenglish.data.local.datastore.DailyGoalSettings
import com.jp.widgetenglish.data.local.dao.UsuarioDao
import com.jp.widgetenglish.data.local.datastore.LearningPreferences
import com.jp.widgetenglish.data.local.datastore.LearningSettings
import com.jp.widgetenglish.data.local.datastore.ModoSeleccionContenido
import com.jp.widgetenglish.data.local.datastore.WidgetPreferences
import com.jp.widgetenglish.data.local.entity.RolUsuario
import com.jp.widgetenglish.data.local.entity.UsuarioEntity
import com.jp.widgetenglish.data.repository.StreakRepository
import com.jp.widgetenglish.data.repository.auth.AuthRepository
import com.jp.widgetenglish.features.widget.WordWidgetProvider
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
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
    ),

    val dailyGoalSettings: DailyGoalSettings = DailyGoalSettings(
        automatico = true,
        objetivoManual = DailyGoalPreferences.OBJETIVO_MANUAL_INICIAL,
        objetivoAutomaticoActual = DailyGoalPreferences.OBJETIVO_AUTOMATICO_INICIAL
    )
)

class ProfileViewModel(
    private val authRepository: AuthRepository,
    private val usuarioDao: UsuarioDao,
    private val streakRepository: StreakRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private var dailyGoalUpdateJob: Job? = null

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

    fun cargarConfiguracionObjetivoDiario(context: Context) {
        viewModelScope.launch {
            try {
                val settings = DailyGoalPreferences.obtenerConfiguracionRapida(
                    context = context.applicationContext
                )

                _uiState.value = _uiState.value.copy(
                    dailyGoalSettings = settings
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error cargando objetivo diario", e)

                _uiState.value = _uiState.value.copy(
                    error = "No se pudo cargar el objetivo diario"
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

    fun guardarConfiguracionAprendizaje(
        context: Context,
        modo: ModoSeleccionContenido,
        automatico: Boolean,
        objetivo: Int
    ) {
        viewModelScope.launch {
            try {
                val appContext = context.applicationContext

                LearningPreferences.guardarModoSeleccionContenido(
                    context = appContext,
                    modo = modo
                )

                LearningPreferences.guardarObjetivoDiarioAutomatico(
                    context = appContext,
                    automatico = automatico
                )

                LearningPreferences.guardarObjetivoDiarioManual(
                    context = appContext,
                    objetivo = objetivo
                )

                reiniciarWidgetPorCambioDeConfiguracion(appContext)
                cargarConfiguracionAprendizaje(appContext)

            } catch (e: Exception) {
                Log.e(TAG, "Error guardando configuracion de aprendizaje", e)

                _uiState.value = _uiState.value.copy(
                    error = "No se pudo guardar la configuraciÃ³n"
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

    fun cambiarModoObjetivoDiario(
        context: Context,
        automatico: Boolean
    ) {
        if (_uiState.value.dailyGoalSettings.automatico == automatico) return

        val settingsOptimistas = _uiState.value.dailyGoalSettings.copy(
            automatico = automatico
        )

        actualizarObjetivoDiarioOptimista(settingsOptimistas)

        persistirCambioObjetivoDiario(context) { appContext ->
            DailyGoalPreferences.guardarAutomatico(
                context = appContext,
                automatico = automatico
            )
        }
    }

    fun cambiarObjetivoDiarioUsuario(
        context: Context,
        objetivo: Int
    ) {
        val objetivoNormalizado = DailyGoalPreferences.normalizarObjetivoManual(objetivo)
        val settingsActuales = _uiState.value.dailyGoalSettings

        if (
            !settingsActuales.automatico &&
            settingsActuales.objetivoManual == objetivoNormalizado
        ) {
            return
        }

        val settingsOptimistas = _uiState.value.dailyGoalSettings.copy(
            automatico = false,
            objetivoManual = objetivoNormalizado
        )

        actualizarObjetivoDiarioOptimista(settingsOptimistas)

        persistirCambioObjetivoDiario(context) { appContext ->
            DailyGoalPreferences.guardarObjetivoManual(
                context = appContext,
                objetivo = objetivoNormalizado
            )
        }
    }

    fun reiniciarObjetivoDiario(context: Context) {
        val settingsOptimistas = DailyGoalSettings(
            automatico = true,
            objetivoManual = DailyGoalPreferences.OBJETIVO_MANUAL_INICIAL,
            objetivoAutomaticoActual = DailyGoalPreferences.OBJETIVO_AUTOMATICO_INICIAL
        )

        if (_uiState.value.dailyGoalSettings == settingsOptimistas) return

        actualizarObjetivoDiarioOptimista(settingsOptimistas)

        persistirCambioObjetivoDiario(context) { appContext ->
            DailyGoalPreferences.reiniciar(appContext)
        }
    }

    private fun actualizarObjetivoDiarioOptimista(
        settings: DailyGoalSettings
    ) {
        _uiState.value = _uiState.value.copy(
            dailyGoalSettings = settings,
            error = null
        )
    }

    private fun persistirCambioObjetivoDiario(
        context: Context,
        guardar: suspend (Context) -> Unit
    ) {
        val appContext = context.applicationContext

        dailyGoalUpdateJob?.cancel()

        dailyGoalUpdateJob = viewModelScope.launch {
            try {
                guardar(appContext)
                recalcularObjetivoDiarioActual(appContext)

            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Error actualizando objetivo diario", e)

                _uiState.value = _uiState.value.copy(
                    error = "No se pudo actualizar el objetivo diario"
                )

                cargarConfiguracionObjetivoDiario(appContext)
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

    private suspend fun recalcularObjetivoDiarioActual(context: Context) {
        val userId = authRepository.obtenerUsuarioActual()?.uid ?: return
        val objetivo = DailyGoalPreferences
            .obtenerConfiguracionRapida(context)
            .objetivoEfectivo

        streakRepository.registrarActividadDiaria(
            usuarioId = userId,
            elementosEstudiados = 0,
            objetivoDiario = objetivo
        )
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
