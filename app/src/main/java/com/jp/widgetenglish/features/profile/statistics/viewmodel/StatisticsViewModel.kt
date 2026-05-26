package com.jp.widgetenglish.features.profile.statistics.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jp.widgetenglish.data.local.dao.ActividadDiariaDao
import com.jp.widgetenglish.data.local.dao.UsuarioDao
import com.jp.widgetenglish.data.local.entity.ActividadDiariaEntity
import com.jp.widgetenglish.data.local.entity.EstadoAprendizaje
import com.jp.widgetenglish.data.local.entity.LoteEntity
import com.jp.widgetenglish.data.local.entity.ProgresoLoteEntity
import com.jp.widgetenglish.data.local.entity.ProgresoUsuarioEntity
import com.jp.widgetenglish.data.local.entity.UsuarioEntity
import com.jp.widgetenglish.data.repository.VocabularioRepository
import com.jp.widgetenglish.data.repository.auth.AuthRepository
import com.jp.widgetenglish.features.profile.statistics.model.StatisticsLotProgressItem
import com.jp.widgetenglish.features.profile.statistics.model.StatisticsPeriod
import com.jp.widgetenglish.features.profile.statistics.model.StatisticsUiState
import com.jp.widgetenglish.features.profile.statistics.model.StatisticsWeeklyItem
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class StatisticsViewModel(
    private val authRepository: AuthRepository,
    private val usuarioDao: UsuarioDao,
    private val actividadDiariaDao: ActividadDiariaDao,
    private val vocabularioRepository: VocabularioRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatisticsUiState())
    val uiState: StateFlow<StatisticsUiState> = _uiState.asStateFlow()

    private val _periodoSeleccionado = MutableStateFlow(StatisticsPeriod.WEEK)
    private val _fechaReferenciaMillis = MutableStateFlow(System.currentTimeMillis())

    private var cargarJob: Job? = null

    init {
        cargarEstadisticas()
    }

    fun cargarEstadisticas() {
        cargarJob?.cancel()

        cargarJob = viewModelScope.launch {
            val firebaseUser = authRepository.obtenerUsuarioActual()

            if (firebaseUser == null) {
                _uiState.value = StatisticsUiState(
                    cargando = false,
                    error = "No hay usuario autenticado"
                )
                return@launch
            }

            val userId = firebaseUser.uid

            val datosBaseFlow = combine(
                usuarioDao.observarUsuarioPorFirebaseUid(userId),
                vocabularioRepository.observarProgresoUsuario(userId),
                vocabularioRepository.observarProgresoLotes(userId),
                vocabularioRepository.observarLotes(),
                actividadDiariaDao.observarActividadesUsuario(userId)
            ) { usuario, progresosUsuario, progresosLotes, lotes, actividades ->
                StatisticsBaseData(
                    usuario = usuario,
                    progresosUsuario = progresosUsuario,
                    progresosLotes = progresosLotes,
                    lotes = lotes,
                    actividades = actividades
                )
            }

            combine(
                datosBaseFlow,
                _periodoSeleccionado,
                _fechaReferenciaMillis
            ) { datos, periodo, fechaReferenciaMillis ->

                construirEstado(
                    datos = datos,
                    periodo = periodo,
                    fechaReferenciaMillis = fechaReferenciaMillis
                )
            }.collect { nuevoEstado ->
                _uiState.value = nuevoEstado
            }
        }
    }

    fun cambiarPeriodo(periodo: StatisticsPeriod) {
        _periodoSeleccionado.value = periodo
    }

    fun irPeriodoAnterior() {
        moverPeriodo(direccion = -1)
    }

    fun irPeriodoSiguiente() {
        moverPeriodo(direccion = 1)
    }

    fun volverPeriodoActual() {
        _fechaReferenciaMillis.value = System.currentTimeMillis()
    }

    private fun moverPeriodo(direccion: Int) {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = _fechaReferenciaMillis.value
        }

        when (_periodoSeleccionado.value) {
            StatisticsPeriod.WEEK -> calendar.add(Calendar.WEEK_OF_YEAR, direccion)
            StatisticsPeriod.MONTH -> calendar.add(Calendar.MONTH, direccion)
            StatisticsPeriod.YEAR -> calendar.add(Calendar.YEAR, direccion)
        }

        _fechaReferenciaMillis.value = calendar.timeInMillis
    }

    private fun construirEstado(
        datos: StatisticsBaseData,
        periodo: StatisticsPeriod,
        fechaReferenciaMillis: Long
    ): StatisticsUiState {
        val progresosUsuario = datos.progresosUsuario
        val progresosLotes = datos.progresosLotes
        val lotes = datos.lotes
        val actividades = datos.actividades
        val usuario = datos.usuario

        val totalProgresos = progresosUsuario.size

        val aprendidas = progresosUsuario.count {
            it.estadoAprendizaje == EstadoAprendizaje.APRENDIDA
        }

        val enProgreso = progresosUsuario.count {
            it.estadoAprendizaje == EstadoAprendizaje.EN_PROGRESO
        }

        val dificiles = progresosUsuario.count {
            it.estadoAprendizaje == EstadoAprendizaje.DIFICIL
        }

        val noVistas = progresosUsuario.count {
            it.estadoAprendizaje == EstadoAprendizaje.NO_VISTA
        }

        val totalCorrectas = progresosUsuario.sumOf { it.respuestasCorrectas }
        val totalIncorrectas = progresosUsuario.sumOf { it.respuestasIncorrectas }
        val totalRespondidas = totalCorrectas + totalIncorrectas

        val precision = if (totalRespondidas > 0) {
            ((totalCorrectas.toFloat() / totalRespondidas.toFloat()) * 100f)
                .toInt()
                .coerceIn(0, 100)
        } else {
            0
        }

        val progresoPorLotes = progresosLotes
            .sortedWith(
                compareByDescending<ProgresoLoteEntity> { it.activo }
                    .thenByDescending { it.progresoPorcentaje }
            )
            .mapNotNull { progreso ->
                val lote = lotes.firstOrNull { it.idLote == progreso.loteId }

                if (lote == null) {
                    null
                } else {
                    StatisticsLotProgressItem(
                        loteId = progreso.loteId,
                        nombre = lote.nombre,
                        porcentaje = progreso.progresoPorcentaje.toInt().coerceIn(0, 100),
                        aprendidas = progreso.contenidosAprendidos,
                        total = progreso.totalContenidos
                    )
                }
            }

        val progresoGeneral = usuario?.porcentajeProgreso
            ?: calcularPromedioLotes(progresoPorLotes)

        return StatisticsUiState(
            cargando = false,
            error = null,

            palabrasAprendidas = usuario?.palabrasAprendidas ?: aprendidas,
            quizzesRealizados = usuario?.quizzesRealizados ?: 0,
            porcentajeProgreso = progresoGeneral,
            rachaActual = usuario?.rachaActual ?: 0,
            rachaMaxima = usuario?.rachaMaxima ?: 0,
            lotesCompletados = usuario?.lotesCompletados ?: contarLotesCompletados(progresoPorLotes),

            precisionGlobal = precision,

            aprendidasPorcentaje = porcentaje(aprendidas, totalProgresos),
            enProgresoPorcentaje = porcentaje(enProgreso, totalProgresos),
            dificilesPorcentaje = porcentaje(dificiles, totalProgresos),
            noVistasPorcentaje = porcentaje(noVistas, totalProgresos),

            progresoLotes = progresoPorLotes,
            progresoSemanal = calcularHistorial(
                actividades = actividades,
                periodo = periodo,
                fechaReferenciaMillis = fechaReferenciaMillis
            ),

            periodoSeleccionado = periodo,
            fechaReferenciaMillis = fechaReferenciaMillis,
            tituloPeriodo = obtenerTituloPeriodo(
                periodo = periodo,
                fechaReferenciaMillis = fechaReferenciaMillis
            )
        )
    }

    private fun porcentaje(
        cantidad: Int,
        total: Int
    ): Int {
        return if (total > 0) {
            ((cantidad.toFloat() / total.toFloat()) * 100f)
                .toInt()
                .coerceIn(0, 100)
        } else {
            0
        }
    }

    private fun calcularPromedioLotes(
        lotes: List<StatisticsLotProgressItem>
    ): Int {
        return if (lotes.isNotEmpty()) {
            lotes.map { it.porcentaje }
                .average()
                .toInt()
                .coerceIn(0, 100)
        } else {
            0
        }
    }

    private fun contarLotesCompletados(
        lotes: List<StatisticsLotProgressItem>
    ): Int {
        return lotes.count { lote ->
            lote.porcentaje >= 100 ||
                    (
                            lote.total > 0 &&
                                    lote.aprendidas >= lote.total
                            )
        }
    }

    private fun calcularHistorial(
        actividades: List<ActividadDiariaEntity>,
        periodo: StatisticsPeriod,
        fechaReferenciaMillis: Long
    ): List<StatisticsWeeklyItem> {
        return when (periodo) {
            StatisticsPeriod.WEEK -> calcularHistorialSemanal(
                actividades = actividades,
                fechaReferenciaMillis = fechaReferenciaMillis
            )

            StatisticsPeriod.MONTH -> calcularHistorialMensual(
                actividades = actividades,
                fechaReferenciaMillis = fechaReferenciaMillis
            )

            StatisticsPeriod.YEAR -> calcularHistorialAnual(
                actividades = actividades,
                fechaReferenciaMillis = fechaReferenciaMillis
            )
        }
    }

    private fun calcularHistorialSemanal(
        actividades: List<ActividadDiariaEntity>,
        fechaReferenciaMillis: Long
    ): List<StatisticsWeeklyItem> {
        val actividadesPorFecha = actividades.associateBy { it.fecha }

        return obtenerDiasDeSemana(fechaReferenciaMillis).map { dia ->
            val actividad = actividadesPorFecha[dia.fecha]

            StatisticsWeeklyItem(
                dia = dia.label,
                valor = actividad?.elementosEstudiados ?: 0
            )
        }
    }

    private fun calcularHistorialMensual(
        actividades: List<ActividadDiariaEntity>,
        fechaReferenciaMillis: Long
    ): List<StatisticsWeeklyItem> {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = fechaReferenciaMillis
            set(Calendar.DAY_OF_MONTH, 1)
        }

        val mes = calendar.get(Calendar.MONTH)
        val year = calendar.get(Calendar.YEAR)

        val semanas = mutableMapOf<Int, Int>()

        actividades.forEach { actividad ->
            val fecha = parseFecha(actividad.fecha) ?: return@forEach

            val actividadCalendar = Calendar.getInstance().apply {
                time = fecha
            }

            val mismoMes = actividadCalendar.get(Calendar.MONTH) == mes
            val mismoYear = actividadCalendar.get(Calendar.YEAR) == year

            if (mismoMes && mismoYear) {
                val semanaDelMes = actividadCalendar.get(Calendar.WEEK_OF_MONTH)
                    .coerceAtLeast(1)

                semanas[semanaDelMes] = (semanas[semanaDelMes] ?: 0) + actividad.elementosEstudiados
            }
        }

        val cantidadSemanas = obtenerCantidadSemanasDelMes(fechaReferenciaMillis)

        return (1..cantidadSemanas).map { semana ->
            StatisticsWeeklyItem(
                dia = "S$semana",
                valor = semanas[semana] ?: 0
            )
        }
    }

    private fun calcularHistorialAnual(
        actividades: List<ActividadDiariaEntity>,
        fechaReferenciaMillis: Long
    ): List<StatisticsWeeklyItem> {
        val yearReferencia = Calendar.getInstance().apply {
            timeInMillis = fechaReferenciaMillis
        }.get(Calendar.YEAR)

        val meses = IntArray(12) { 0 }

        actividades.forEach { actividad ->
            val fecha = parseFecha(actividad.fecha) ?: return@forEach

            val actividadCalendar = Calendar.getInstance().apply {
                time = fecha
            }

            val yearActividad = actividadCalendar.get(Calendar.YEAR)

            if (yearActividad == yearReferencia) {
                val mes = actividadCalendar.get(Calendar.MONTH)
                meses[mes] += actividad.elementosEstudiados
            }
        }

        val labels = listOf(
            "Ene", "Feb", "Mar", "Abr",
            "May", "Jun", "Jul", "Ago",
            "Sep", "Oct", "Nov", "Dic"
        )

        return labels.mapIndexed { index, label ->
            StatisticsWeeklyItem(
                dia = label,
                valor = meses[index]
            )
        }
    }

    private fun obtenerDiasDeSemana(
        fechaReferenciaMillis: Long
    ): List<DayInfo> {
        val formatoFecha = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        val calendar = Calendar.getInstance().apply {
            timeInMillis = fechaReferenciaMillis
            firstDayOfWeek = Calendar.MONDAY
        }

        while (calendar.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
            calendar.add(Calendar.DAY_OF_YEAR, -1)
        }

        return List(7) {
            val fecha = formatoFecha.format(calendar.time)
            val label = obtenerLabelDia(calendar)

            calendar.add(Calendar.DAY_OF_YEAR, 1)

            DayInfo(
                fecha = fecha,
                label = label
            )
        }
    }

    private fun obtenerCantidadSemanasDelMes(
        fechaReferenciaMillis: Long
    ): Int {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = fechaReferenciaMillis
            set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
        }

        return calendar.get(Calendar.WEEK_OF_MONTH).coerceAtLeast(4)
    }

    private fun obtenerTituloPeriodo(
        periodo: StatisticsPeriod,
        fechaReferenciaMillis: Long
    ): String {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = fechaReferenciaMillis
        }

        return when (periodo) {
            StatisticsPeriod.WEEK -> {
                val dias = obtenerDiasDeSemana(fechaReferenciaMillis)
                val inicio = dias.firstOrNull()?.fecha.orEmpty()
                val fin = dias.lastOrNull()?.fecha.orEmpty()

                "Semana $inicio - $fin"
            }

            StatisticsPeriod.MONTH -> {
                val formato = SimpleDateFormat("MMMM yyyy", Locale("es", "ES"))
                formato.format(calendar.time)
                    .replaceFirstChar { it.uppercase() }
            }

            StatisticsPeriod.YEAR -> {
                calendar.get(Calendar.YEAR).toString()
            }
        }
    }

    private fun obtenerLabelDia(
        calendar: Calendar
    ): String {
        return when (calendar.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> "Lun"
            Calendar.TUESDAY -> "Mar"
            Calendar.WEDNESDAY -> "Mié"
            Calendar.THURSDAY -> "Jue"
            Calendar.FRIDAY -> "Vie"
            Calendar.SATURDAY -> "Sáb"
            Calendar.SUNDAY -> "Dom"
            else -> ""
        }
    }

    private fun parseFecha(fecha: String): Date? {
        return try {
            FORMATO_FECHA.parse(fecha)
        } catch (e: Exception) {
            null
        }
    }

    private data class DayInfo(
        val fecha: String,
        val label: String
    )

    private data class StatisticsBaseData(
        val usuario: UsuarioEntity?,
        val progresosUsuario: List<ProgresoUsuarioEntity>,
        val progresosLotes: List<ProgresoLoteEntity>,
        val lotes: List<LoteEntity>,
        val actividades: List<ActividadDiariaEntity>
    )

    companion object {
        private val FORMATO_FECHA = SimpleDateFormat(
            "yyyy-MM-dd",
            Locale.getDefault()
        )
    }
}