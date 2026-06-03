package com.jp.widgetenglish.features.home.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jp.widgetenglish.data.local.datastore.DailyGoalPreferences
import com.jp.widgetenglish.data.local.dao.ActividadDiariaDao
import com.jp.widgetenglish.data.local.dao.UsuarioDao
import com.jp.widgetenglish.data.local.entity.ActividadDiariaEntity
import com.jp.widgetenglish.data.local.entity.LoteEntity
import com.jp.widgetenglish.data.local.entity.PalabraEntity
import com.jp.widgetenglish.data.local.entity.ProgresoLoteEntity
import com.jp.widgetenglish.data.local.entity.UsuarioEntity
import com.jp.widgetenglish.data.local.entity.VerboEntity
import com.jp.widgetenglish.data.repository.VocabularioRepository
import com.jp.widgetenglish.data.repository.auth.AuthRepository
import com.jp.widgetenglish.features.common.resolveUserDisplayName
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HomeViewModel(
    private val repository: VocabularioRepository,
    private val authRepository: AuthRepository,
    private val usuarioDao: UsuarioDao,
    private val actividadDiariaDao: ActividadDiariaDao,
    private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var cargarHomeJob: Job? = null
    private var usuarioObservadoId: String? = null

    init {
        cargarHome()
    }

    fun cargarHome() {
        val firebaseUser = authRepository.obtenerUsuarioActual()
        val userId = firebaseUser?.uid

        if (cargarHomeJob?.isActive == true && usuarioObservadoId == userId) {
            return
        }

        cargarHomeJob?.cancel()
        usuarioObservadoId = userId

        if (firebaseUser == null || userId == null) {
            _uiState.value = HomeUiState(
                cargando = false,
                error = "No hay usuario autenticado"
            )
            return
        }

        cargarHomeJob = viewModelScope.launch {
            val hoy = obtenerFechaActual()

            val datosPrincipalesFlow = combine(
                usuarioDao.observarUsuarioPorFirebaseUid(userId),
                actividadDiariaDao.observarActividadPorFecha(
                    usuarioId = userId,
                    fecha = hoy
                ),
                repository.observarPalabras(),
                repository.observarVerbos(),
                repository.observarLotes()
            ) { usuarioLocal, actividadHoy, palabras, verbos, lotes ->
                HomeDatosPrincipales(
                    usuarioLocal = usuarioLocal,
                    actividadHoy = actividadHoy,
                    palabras = palabras,
                    verbos = verbos,
                    lotes = lotes
                )
            }

            combine(
                datosPrincipalesFlow,
                repository.observarLoteActivo(userId),
                DailyGoalPreferences.observarConfiguracion(context)
            ) { datos, loteActivo, dailyGoalSettings ->

                val correo = datos.usuarioLocal?.correo
                    ?: firebaseUser.email
                    ?: ""

                val nombre = resolveUserDisplayName(
                    localName = datos.usuarioLocal?.nombre,
                    firebaseDisplayName = firebaseUser.displayName,
                    email = correo
                )

                val loteActivoInfo = datos.lotes.firstOrNull { lote ->
                    lote.idLote == loteActivo?.loteId
                }

                val objetivoDiario = dailyGoalSettings.objetivoEfectivo
                val progresoDiario = datos.actividadHoy?.elementosEstudiados ?: 0

                HomeUiState(
                    cargando = false,

                    nombreUsuario = nombre,
                    correoUsuario = correo,

                    rachaActual = datos.usuarioLocal?.rachaActual ?: 0,
                    rachaMaxima = datos.usuarioLocal?.rachaMaxima ?: 0,

                    palabras = datos.palabras,
                    verbos = datos.verbos,
                    lotes = datos.lotes,

                    loteActivo = loteActivo,
                    loteActivoInfo = loteActivoInfo,

                    objetivoDiario = objetivoDiario,
                    progresoDiario = progresoDiario.coerceAtMost(objetivoDiario),
                    objetivoDiarioCumplido = progresoDiario >= objetivoDiario,

                    error = null
                )
            }.collect { newState ->
                _uiState.value = newState
            }
        }
    }

    private fun obtenerFechaActual(): String {
        val formato = SimpleDateFormat(
            "yyyy-MM-dd",
            Locale.getDefault()
        )

        return formato.format(Date())
    }
}

private data class HomeDatosPrincipales(
    val usuarioLocal: UsuarioEntity?,
    val actividadHoy: ActividadDiariaEntity?,
    val palabras: List<PalabraEntity>,
    val verbos: List<VerboEntity>,
    val lotes: List<LoteEntity>
)
