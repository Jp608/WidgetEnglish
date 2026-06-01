package com.jp.widgetenglish.features.profile.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.firestore.FirebaseFirestoreException
import com.jp.widgetenglish.data.local.dao.ActividadDiariaDao
import com.jp.widgetenglish.data.local.dao.ProgresoDao
import com.jp.widgetenglish.data.local.datastore.DailyGoalPreferences
import com.jp.widgetenglish.data.local.datastore.DailyGoalSettings
import com.jp.widgetenglish.data.local.dao.UsuarioDao
import com.jp.widgetenglish.data.local.datastore.LearningPreferences
import com.jp.widgetenglish.data.local.datastore.LearningSettings
import com.jp.widgetenglish.data.local.datastore.ModoSeleccionContenido
import com.jp.widgetenglish.data.local.datastore.WidgetAppearancePreferences
import com.jp.widgetenglish.data.local.datastore.WidgetAppearanceSettings
import com.jp.widgetenglish.data.local.datastore.WidgetPreferences
import com.jp.widgetenglish.data.local.entity.RolUsuario
import com.jp.widgetenglish.data.local.entity.UsuarioEntity
import com.jp.widgetenglish.data.remote.firestore.UsuarioFirestoreDataSource
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
    val guardandoPerfil: Boolean = false,
    val enviandoCorreoSeguridad: Boolean = false,
    val eliminandoCuenta: Boolean = false,
    val cuentaEliminada: Boolean = false,
    val mensaje: String? = null,
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
    ),

    val widgetAppearanceSettings: WidgetAppearanceSettings =
        WidgetAppearancePreferences.DEFAULT_SETTINGS
)

class ProfileViewModel(
    private val authRepository: AuthRepository,
    private val usuarioDao: UsuarioDao,
    private val progresoDao: ProgresoDao,
    private val actividadDiariaDao: ActividadDiariaDao,
    private val usuarioFirestoreDataSource: UsuarioFirestoreDataSource,
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

    fun cargarAparienciaWidget(context: Context) {
        viewModelScope.launch {
            try {
                val settings = WidgetAppearancePreferences.obtenerConfiguracionRapida(
                    context = context.applicationContext
                )

                _uiState.value = _uiState.value.copy(
                    widgetAppearanceSettings = settings,
                    error = null
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error cargando apariencia del widget", e)

                _uiState.value = _uiState.value.copy(
                    error = "No se pudo cargar la apariencia del widget"
                )
            }
        }
    }

    fun guardarAparienciaWidget(
        context: Context,
        settings: WidgetAppearanceSettings
    ) {
        viewModelScope.launch {
            try {
                val appContext = context.applicationContext

                WidgetAppearancePreferences.guardarConfiguracion(
                    context = appContext,
                    settings = settings
                )

                _uiState.value = _uiState.value.copy(
                    widgetAppearanceSettings = settings,
                    error = null
                )

                WordWidgetProvider.updateAll(appContext)
            } catch (e: Exception) {
                Log.e(TAG, "Error guardando apariencia del widget", e)

                _uiState.value = _uiState.value.copy(
                    error = "No se pudo guardar la apariencia del widget"
                )
            }
        }
    }

    fun reiniciarAparienciaWidget(context: Context) {
        guardarAparienciaWidget(
            context = context,
            settings = WidgetAppearancePreferences.DEFAULT_SETTINGS
        )
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
        WidgetPreferences.reiniciarSesionSecuencial(context)
        WordWidgetProvider.updateAll(context)
    }

    fun limpiarMensajes() {
        _uiState.value = _uiState.value.copy(
            mensaje = null,
            error = null
        )
    }

    fun actualizarPerfilAdministrador(nombre: String) {
        val nombreLimpio = nombre.trim()

        if (nombreLimpio.length < 3) {
            _uiState.value = _uiState.value.copy(
                error = "El nombre debe tener al menos 3 caracteres"
            )
            return
        }

        viewModelScope.launch {
            val firebaseUser = authRepository.obtenerUsuarioActual()

            if (firebaseUser == null) {
                _uiState.value = _uiState.value.copy(
                    error = "No hay usuario autenticado"
                )
                return@launch
            }

            _uiState.value = _uiState.value.copy(
                guardandoPerfil = true,
                mensaje = null,
                error = null
            )

            try {
                authRepository.actualizarNombreUsuarioActual(nombreLimpio)
                    .getOrThrow()

                usuarioFirestoreDataSource.actualizarNombreUsuario(
                    firebaseUid = firebaseUser.uid,
                    nombre = nombreLimpio
                )

                val usuarioLocal = usuarioDao.obtenerUsuarioPorFirebaseUid(firebaseUser.uid)
                val usuarioActualizado = usuarioLocal?.copy(
                    nombre = nombreLimpio,
                    ultimoAcceso = System.currentTimeMillis()
                )

                if (usuarioActualizado != null) {
                    usuarioDao.actualizarUsuario(usuarioActualizado)
                }

                _uiState.value = _uiState.value.copy(
                    usuario = usuarioActualizado ?: _uiState.value.usuario?.copy(nombre = nombreLimpio),
                    guardandoPerfil = false,
                    mensaje = "Perfil actualizado correctamente",
                    error = null
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error actualizando perfil administrador", e)

                _uiState.value = _uiState.value.copy(
                    guardandoPerfil = false,
                    error = e.message ?: "No se pudo actualizar el perfil"
                )
            }
        }
    }

    fun enviarCorreoSeguridadAdministrador() {
        val correo = _uiState.value.usuario?.correo
            ?: authRepository.obtenerUsuarioActual()?.email
            ?: ""

        if (correo.isBlank()) {
            _uiState.value = _uiState.value.copy(
                error = "No hay correo asociado para enviar instrucciones"
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                enviandoCorreoSeguridad = true,
                mensaje = null,
                error = null
            )

            val result = authRepository.recuperarPassword(correo)

            _uiState.value = result.fold(
                onSuccess = {
                    _uiState.value.copy(
                        enviandoCorreoSeguridad = false,
                        mensaje = "Enviamos instrucciones de seguridad a $correo",
                        error = null
                    )
                },
                onFailure = { error ->
                    Log.e(TAG, "Error enviando correo de seguridad", error)

                    _uiState.value.copy(
                        enviandoCorreoSeguridad = false,
                        error = error.message ?: "No se pudo enviar el correo de seguridad"
                    )
                }
            )
        }
    }

    fun eliminarCuenta(context: Context) {
        viewModelScope.launch {
            val firebaseUser = authRepository.obtenerUsuarioActual()

            if (firebaseUser == null) {
                _uiState.value = _uiState.value.copy(
                    eliminandoCuenta = false,
                    error = "No hay usuario autenticado"
                )
                return@launch
            }

            if (authRepository.requiereInicioSesionReciente()) {
                _uiState.value = _uiState.value.copy(
                    eliminandoCuenta = false,
                    error = "Por seguridad, vuelve a iniciar sesión y luego intenta eliminar la cuenta."
                )
                return@launch
            }

            val appContext = context.applicationContext
            val userId = firebaseUser.uid

            _uiState.value = _uiState.value.copy(
                eliminandoCuenta = true,
                error = null
            )

            try {
                usuarioFirestoreDataSource.eliminarUsuarioCompleto(userId)

                authRepository.eliminarCuentaActual()
                    .getOrThrow()

                actividadDiariaDao.eliminarActividadesUsuario(userId)
                progresoDao.eliminarProgresoUsuario(userId)
                progresoDao.eliminarProgresoLotesUsuario(userId)
                usuarioDao.eliminarUsuarioPorFirebaseUid(userId)

                LearningPreferences.reiniciarConfiguracionAprendizaje(appContext)
                DailyGoalPreferences.reiniciar(appContext)
                WidgetAppearancePreferences.reiniciar(appContext)
                WidgetPreferences.limpiarSesionWidget(appContext)
                WordWidgetProvider.updateAll(appContext)

                _uiState.value = ProfileUiState(
                    usuario = null,
                    cargando = false,
                    autenticado = false,
                    eliminandoCuenta = false,
                    cuentaEliminada = true
                )
            } catch (e: FirebaseAuthRecentLoginRequiredException) {
                Log.e(TAG, "Firebase requiere reautenticación para eliminar cuenta", e)

                _uiState.value = _uiState.value.copy(
                    eliminandoCuenta = false,
                    error = "Por seguridad, vuelve a iniciar sesión y luego intenta eliminar la cuenta."
                )
            } catch (e: FirebaseFirestoreException) {
                Log.e(TAG, "Error eliminando datos remotos de la cuenta", e)

                _uiState.value = _uiState.value.copy(
                    eliminandoCuenta = false,
                    error = if (e.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                        "No se pudo eliminar la cuenta por permisos de Firebase. Revisa las reglas de Firestore."
                    } else {
                        "No se pudo eliminar la información de la cuenta."
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error eliminando cuenta", e)

                _uiState.value = _uiState.value.copy(
                    eliminandoCuenta = false,
                    error = e.message ?: "No se pudo eliminar la cuenta"
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

    companion object {
        private const val TAG = "ProfileViewModel"
    }
}
