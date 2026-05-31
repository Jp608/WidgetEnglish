package com.jp.widgetenglish.features.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.widget.RemoteViews
import com.google.firebase.firestore.FirebaseFirestore
import com.jp.widgetenglish.MainActivity
import com.jp.widgetenglish.R
import com.jp.widgetenglish.data.local.database.DatabaseProvider
import com.jp.widgetenglish.data.local.datastore.LearningPreferences
import com.jp.widgetenglish.data.local.datastore.ModoSeleccionContenido
import com.jp.widgetenglish.data.local.datastore.WidgetAppearancePreferences
import com.jp.widgetenglish.data.local.datastore.WidgetAppearanceSettings
import com.jp.widgetenglish.data.local.datastore.WidgetColorTheme
import com.jp.widgetenglish.data.local.datastore.WidgetTextSizeOption
import com.jp.widgetenglish.data.local.datastore.WidgetVisualStyle
import com.jp.widgetenglish.data.local.datastore.WidgetPreferences
import com.jp.widgetenglish.data.local.entity.LoteContenidoEntity
import com.jp.widgetenglish.data.local.entity.TipoContenido
import com.jp.widgetenglish.data.local.entity.EstadoAprendizaje
import com.jp.widgetenglish.data.local.entity.ProgresoUsuarioEntity
import com.jp.widgetenglish.data.remote.firestore.UsuarioFirestoreDataSource
import com.jp.widgetenglish.data.repository.VocabularioRepositoryImpl
import com.jp.widgetenglish.domain.learning.LearningContentSelector
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import android.widget.Toast
import com.jp.widgetenglish.data.repository.StreakRepository
import com.jp.widgetenglish.data.remote.firestore.EstadisticasFirestoreDataSource

class WordWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        appWidgetIds.forEach { appWidgetId ->
            requestUpdateWidget(
                context = context.applicationContext,
                appWidgetManager = appWidgetManager,
                appWidgetId = appWidgetId
            )
        }
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle
    ) {
        super.onAppWidgetOptionsChanged(
            context,
            appWidgetManager,
            appWidgetId,
            newOptions
        )

        val pendingResult = goAsync()

        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                updateWidget(
                    context = context.applicationContext,
                    appWidgetManager = appWidgetManager,
                    appWidgetId = appWidgetId
                )
            } finally {
                pendingResult.finish()
            }
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        when (intent.action) {
            ACTION_NEXT_WORD -> {
                val pendingResult = goAsync()

                CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
                    try {
                        avanzarPalabra(context.applicationContext)
                        updateAll(context.applicationContext)
                    } finally {
                        pendingResult.finish()
                    }
                }
            }

            ACTION_MARK_LEARNED -> {
                val pendingResult = goAsync()

                CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
                    try {
                        val resultado = marcarPalabraActualComoAprendida(context.applicationContext)

                        avanzarDespuesDeMarcarAprendida(
                            context = context.applicationContext,
                            contenidoMarcado = resultado.contenidoMarcado
                        )
                        updateAll(context.applicationContext)

                        if (resultado.debeSincronizarFirebase && !resultado.userId.isNullOrBlank()) {
                            CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
                                sincronizarEstadisticasWidget(
                                    context = context.applicationContext,
                                    userId = resultado.userId
                                )
                            }
                        }
                    } finally {
                        pendingResult.finish()
                    }
                }
            }

            ACTION_PLAY_SOUND -> {
                val text = intent.getStringExtra(EXTRA_TEXT).orEmpty()

                if (text.isNotBlank()) {
                    val ttsIntent = Intent(
                        context.applicationContext,
                        TtsReceiver::class.java
                    ).apply {
                        putExtra("text", text)
                    }

                    context.applicationContext.sendBroadcast(ttsIntent)
                }
            }

            ACTION_REFRESH_WIDGET -> {
                val pendingResult = goAsync()

                CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
                    try {
                        updateAll(context.applicationContext)
                    } finally {
                        pendingResult.finish()
                    }
                }
            }

            ACTION_OPEN_APP_REQUEST -> {
                manejarSolicitudAbrirApp(context.applicationContext)
            }
        }
    }

    companion object {
        const val ACTION_NEXT_WORD = "com.jp.widgetenglish.widget.ACTION_NEXT_WORD"
        const val ACTION_MARK_LEARNED = "com.jp.widgetenglish.widget.ACTION_MARK_LEARNED"
        const val ACTION_PLAY_SOUND = "com.jp.widgetenglish.widget.ACTION_PLAY_SOUND"
        const val ACTION_REFRESH_WIDGET = "com.jp.widgetenglish.widget.ACTION_REFRESH_WIDGET"
        const val EXTRA_TEXT = "extra_text"
        const val ACTION_OPEN_APP_REQUEST = "com.jp.widgetenglish.widget.ACTION_OPEN_APP_REQUEST"

        private const val REQUEST_CODE_OPEN_APP = 20_000
        private const val REQUEST_CODE_SOUND_OFFSET = 10_000
        private const val REQUEST_CODE_LEARNED_OFFSET = 30_000

        private const val COMPACT_MAX_WIDTH = 180
        private const val COMPACT_MAX_HEIGHT = 110
        private const val LARGE_MIN_WIDTH = 280
        private const val LARGE_MIN_HEIGHT = 160

        private const val DOUBLE_TAP_WINDOW_MS = 1_500L
        private const val WIDGET_PREFS_NAME = "widget_open_prefs"
        private const val KEY_LAST_OPEN_TAP = "last_open_tap"

        private val widgetScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

        fun requestUpdateAll(context: Context) {
            widgetScope.launch {
                updateAll(context.applicationContext)
            }
        }


        private data class MarkLearnedResult(
            val userId: String?,
            val debeSincronizarFirebase: Boolean,
            val contenidoMarcado: LoteContenidoEntity? = null
        )

        private data class WidgetThemeColors(
            val primary: Int,
            val background: Int,
            val text: Int,
            val muted: Int,
            val onPrimary: Int = Color.WHITE,
            val soundBackground: Int
        )

        suspend fun updateAll(context: Context) {
            val appContext = context.applicationContext
            val appWidgetManager = AppWidgetManager.getInstance(appContext)
            val componentName = ComponentName(appContext, WordWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)

            appWidgetIds.forEach { appWidgetId ->
                updateWidget(
                    context = appContext,
                    appWidgetManager = appWidgetManager,
                    appWidgetId = appWidgetId
                )
            }
        }


        private fun requestUpdateWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            widgetScope.launch {
                updateWidget(
                    context = context.applicationContext,
                    appWidgetManager = appWidgetManager,
                    appWidgetId = appWidgetId
                )
            }
        }

        private suspend fun avanzarPalabra(context: Context) {
            val contenidosSesion = obtenerContenidoSesionActual(context)

            if (contenidosSesion.isEmpty()) return

            val currentIndex = WidgetPreferences.obtenerWordIndex(context).first()
            val nextIndex = (currentIndex + 1) % contenidosSesion.size

            WidgetPreferences.actualizarIndiceDirecto(
                context,
                nextIndex
            )
        }

        private suspend fun avanzarDespuesDeMarcarAprendida(
            context: Context,
            contenidoMarcado: LoteContenidoEntity?
        ) {
            val configuracion = LearningPreferences.obtenerConfiguracionRapida(context)

            if (
                configuracion.modoSeleccionContenido != ModoSeleccionContenido.SECUENCIAL ||
                contenidoMarcado == null
            ) {
                avanzarPalabra(context)
                return
            }

            val contenidosSesion = obtenerContenidoSesionActual(context)

            if (contenidosSesion.isEmpty()) return

            val currentIndex = WidgetPreferences.obtenerWordIndex(context).first()
            val safeIndex = currentIndex.coerceAtLeast(0) % contenidosSesion.size
            val contenidoEnIndice = contenidosSesion[safeIndex]
            val contenidoActualSigueEnIndice = mismoContenido(
                a = contenidoEnIndice,
                b = contenidoMarcado
            )
            val nextIndex = if (contenidoActualSigueEnIndice) {
                (safeIndex + 1) % contenidosSesion.size
            } else {
                safeIndex
            }

            WidgetPreferences.actualizarIndiceDirecto(
                context = context,
                nuevoIndice = nextIndex
            )
        }

        private suspend fun sincronizarEstadisticasWidget(
            context: Context,
            userId: String
        ) {
            try {
                val db = DatabaseProvider.getDatabase(context)

                val estadisticasFirestoreDataSource = EstadisticasFirestoreDataSource(
                    firestore = FirebaseFirestore.getInstance()
                )

                val streakRepository = StreakRepository(
                    actividadDiariaDao = db.actividadDiariaDao(),
                    usuarioDao = db.usuarioDao(),
                    progresoDao = db.progresoDao(),
                    estadisticasFirestoreDataSource = estadisticasFirestoreDataSource,
                    context = context.applicationContext
                )

                streakRepository.sincronizarEstadisticasActuales(userId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        private suspend fun marcarPalabraActualComoAprendida(context: Context): MarkLearnedResult {
            val db = DatabaseProvider.getDatabase(context)
            val prefs = WidgetPreferences.obtenerTodasLasPreferenciasRapidas(context)

            val loteId = prefs.loteId
            val userId = prefs.userId
            val wordIndex = prefs.wordIndex

            if (loteId.isNullOrBlank() || userId.isNullOrBlank()) {
                return MarkLearnedResult(
                    userId = null,
                    debeSincronizarFirebase = false
                )
            }

            val contenidosSesion = obtenerContenidoSesionActual(
                context = context,
                loteId = loteId,
                userId = userId
            )

            if (contenidosSesion.isEmpty()) {
                return MarkLearnedResult(
                    userId = null,
                    debeSincronizarFirebase = false
                )
            }

            val safeIndex = wordIndex.coerceAtLeast(0) % contenidosSesion.size
            val itemActual = contenidosSesion[safeIndex]
            val ahora = System.currentTimeMillis()

            val progresoExistente = db.progresoDao().obtenerProgresoContenido(
                usuarioId = userId,
                contenidoId = itemActual.contenidoId,
                tipoContenido = itemActual.tipoContenido
            )

            val yaEstabaAprendida = progresoExistente?.estadoAprendizaje == EstadoAprendizaje.APRENDIDA

            val progresoAprendido = if (progresoExistente == null) {
                ProgresoUsuarioEntity(
                    id = "${userId}_${itemActual.contenidoId}_${itemActual.tipoContenido.name}",
                    usuarioId = userId,
                    contenidoId = itemActual.contenidoId,
                    tipoContenido = itemActual.tipoContenido,
                    estadoAprendizaje = EstadoAprendizaje.APRENDIDA,
                    nivelDominio = 1f,
                    respuestasCorrectas = 1,
                    respuestasIncorrectas = 0,
                    vecesRepasado = 1,
                    aprendido = true,
                    favorito = false,
                    ultimaRevision = ahora,
                    proximaRevision = null
                )
            } else {
                progresoExistente.copy(
                    estadoAprendizaje = EstadoAprendizaje.APRENDIDA,
                    nivelDominio = 1f,
                    respuestasCorrectas = progresoExistente.respuestasCorrectas + 1,
                    vecesRepasado = progresoExistente.vecesRepasado + 1,
                    aprendido = true,
                    ultimaRevision = ahora
                )
            }

            db.progresoDao().insertarProgresoUsuario(progresoAprendido)

            actualizarProgresoLoteDesdeWidget(
                context = context,
                usuarioId = userId,
                loteId = loteId
            )

            if (!yaEstabaAprendida) {
                val streakRepository = StreakRepository(
                    actividadDiariaDao = db.actividadDiariaDao(),
                    usuarioDao = db.usuarioDao(),
                    progresoDao = db.progresoDao(),
                    context = context.applicationContext
                )

                streakRepository.registrarActividadDiaria(
                    usuarioId = userId,
                    elementosEstudiados = 1
                )
            }

            return MarkLearnedResult(
                userId = userId,
                debeSincronizarFirebase = !yaEstabaAprendida,
                contenidoMarcado = itemActual
            )
        }

        private suspend fun actualizarProgresoLoteDesdeWidget(
            context: Context,
            usuarioId: String,
            loteId: String
        ) {
            val db = DatabaseProvider.getDatabase(context)

            val contenidosDelLote = db.loteDao()
                .observarContenidoDeLote(loteId)
                .first()

            if (contenidosDelLote.isEmpty()) {
                return
            }

            val progresosUsuario = db.progresoDao()
                .observarProgresoUsuario(usuarioId)
                .first()

            val aprendidas = contenidosDelLote.count { contenido ->
                progresosUsuario.any { progreso ->
                    progreso.contenidoId == contenido.contenidoId &&
                            progreso.tipoContenido == contenido.tipoContenido &&
                            progreso.estadoAprendizaje == EstadoAprendizaje.APRENDIDA
                }
            }

            val total = contenidosDelLote.size

            val porcentaje = if (total > 0) {
                ((aprendidas.toFloat() / total.toFloat()) * 100f)
                    .coerceIn(0f, 100f)
            } else {
                0f
            }

            db.progresoDao().actualizarProgresoLoteFull(
                usuarioId = usuarioId,
                loteId = loteId,
                activo = true,
                progresoPorcentaje = porcentaje,
                aprendidas = aprendidas,
                total = total,
                fecha = System.currentTimeMillis()
            )
        }

        private suspend fun updateWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val data = obtenerContenidoWidget(context)
            val appearance = WidgetAppearancePreferences.obtenerConfiguracionRapida(context)

            val layoutId = seleccionarLayout(
                appWidgetManager = appWidgetManager,
                appWidgetId = appWidgetId
            )

            val views = RemoteViews(
                context.packageName,
                layoutId
            )

            when (layoutId) {
                R.layout.widget_word_compact -> {
                    configurarWidgetCompacto(
                        context = context,
                        views = views,
                        appWidgetId = appWidgetId,
                        data = data,
                        appearance = appearance
                    )
                }

                R.layout.widget_word_large -> {
                    configurarWidgetGrande(
                        context = context,
                        views = views,
                        appWidgetId = appWidgetId,
                        data = data,
                        appearance = appearance
                    )
                }

                else -> {
                    configurarWidgetNormal(
                        context = context,
                        views = views,
                        appWidgetId = appWidgetId,
                        data = data,
                        appearance = appearance
                    )
                }
            }

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        private fun seleccionarLayout(
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ): Int {
            val options = appWidgetManager.getAppWidgetOptions(appWidgetId)

            val minWidth = options.getInt(
                AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH,
                0
            )

            val minHeight = options.getInt(
                AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT,
                0
            )

            return when {
                minWidth <= COMPACT_MAX_WIDTH || minHeight <= COMPACT_MAX_HEIGHT -> {
                    R.layout.widget_word_compact
                }

                minWidth >= LARGE_MIN_WIDTH && minHeight >= LARGE_MIN_HEIGHT -> {
                    R.layout.widget_word_large
                }

                else -> {
                    R.layout.widget_word_normal
                }
            }
        }

        private fun configurarWidgetCompacto(
            context: Context,
            views: RemoteViews,
            appWidgetId: Int,
            data: WidgetData,
            appearance: WidgetAppearanceSettings
        ) {
            aplicarAparienciaCompacta(views, appearance)

            views.setTextViewText(
                R.id.widget_text_lote,
                appearance.headerTitle(data.loteNombre)
            )

            views.setTextViewText(
                R.id.widget_text_progress,
                data.progreso.ifBlank { "" }
            )

            views.setTextViewText(
                R.id.widget_text_word,
                data.termino
            )

            views.setTextViewText(
                R.id.widget_text_translation,
                data.traduccion
            )
            views.setViewVisibility(
                R.id.widget_text_translation,
                if (appearance.mostrarTraduccion && data.traduccion.isNotBlank()) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
            )

            aplicarHeaderVisibility(
                views = views,
                headerId = R.id.widget_compact_header,
                loteId = R.id.widget_text_lote,
                progresoId = R.id.widget_text_progress,
                appearance = appearance
            )

            if (data.fonetica.isBlank() || !appearance.mostrarFonetica) {
                views.setViewVisibility(
                    R.id.widget_text_pronunciation,
                    View.GONE
                )
            } else {
                views.setViewVisibility(
                    R.id.widget_text_pronunciation,
                    View.VISIBLE
                )

                views.setTextViewText(
                    R.id.widget_text_pronunciation,
                    data.fonetica
                )
            }

            views.setOnClickPendingIntent(
                R.id.widget_root,
                pendingIntentSolicitarAbrirApp(context)
            )

            views.setOnClickPendingIntent(
                R.id.widget_btn_next,
                pendingIntentSiguiente(context, appWidgetId)
            )

            views.setOnClickPendingIntent(
                R.id.widget_btn_learned,
                pendingIntentAprendida(context, appWidgetId)
            )
        }

        private fun configurarWidgetNormal(
            context: Context,
            views: RemoteViews,
            appWidgetId: Int,
            data: WidgetData,
            appearance: WidgetAppearanceSettings
        ) {
            aplicarAparienciaNormal(views, appearance)

            views.setTextViewText(
                R.id.widget_lote_nombre,
                appearance.headerTitle(data.loteNombre)
            )
            views.setTextViewText(R.id.widget_progreso, data.progreso)
            views.setTextViewText(R.id.widget_termino, data.termino)
            views.setTextViewText(R.id.widget_fonetica, data.fonetica)
            views.setTextViewText(R.id.widget_traduccion, data.traduccion)
            aplicarTranslationVisibility(
                views = views,
                translationId = R.id.widget_traduccion,
                translation = data.traduccion,
                appearance = appearance
            )

            aplicarHeaderVisibility(
                views = views,
                headerId = R.id.widget_header,
                loteId = R.id.widget_lote_nombre,
                progresoId = R.id.widget_progreso,
                appearance = appearance
            )

            views.setViewVisibility(
                R.id.widget_fonetica,
                if (appearance.mostrarFonetica && data.fonetica.isNotBlank()) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
            )

            if (data.tipo == WidgetContentType.VERBO) {
                views.setTextViewText(
                    R.id.widget_extra,
                    data.textoVerbo
                )
                views.setViewVisibility(R.id.widget_extra, View.VISIBLE)
            } else {
                views.setTextViewText(R.id.widget_extra, "")
                views.setViewVisibility(R.id.widget_extra, View.GONE)
            }

            views.setOnClickPendingIntent(
                R.id.widget_root,
                pendingIntentSolicitarAbrirApp(context)
            )

            views.setOnClickPendingIntent(
                R.id.widget_btn_next,
                pendingIntentSiguiente(context, appWidgetId)
            )

            views.setOnClickPendingIntent(
                R.id.widget_btn_sound,
                pendingIntentSonido(context, appWidgetId, data.termino)
            )

            views.setOnClickPendingIntent(
                R.id.widget_btn_learned,
                pendingIntentAprendida(context, appWidgetId)
            )
        }

        private fun configurarWidgetGrande(
            context: Context,
            views: RemoteViews,
            appWidgetId: Int,
            data: WidgetData,
            appearance: WidgetAppearanceSettings
        ) {
            aplicarAparienciaGrande(views, appearance)

            views.setTextViewText(
                R.id.widget_lote_nombre,
                appearance.headerTitle(data.loteNombre)
            )
            views.setTextViewText(R.id.widget_progreso, data.progreso)
            views.setTextViewText(R.id.widget_termino, data.termino)
            views.setTextViewText(R.id.widget_fonetica, data.fonetica)
            views.setTextViewText(R.id.widget_traduccion, data.traduccion)
            aplicarTranslationVisibility(
                views = views,
                translationId = R.id.widget_traduccion,
                translation = data.traduccion,
                appearance = appearance
            )

            aplicarHeaderVisibility(
                views = views,
                headerId = R.id.widget_header,
                loteId = R.id.widget_lote_nombre,
                progresoId = R.id.widget_progreso,
                appearance = appearance
            )

            views.setViewVisibility(
                R.id.widget_fonetica,
                if (appearance.mostrarFonetica && data.fonetica.isNotBlank()) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
            )

            if (data.tipo == WidgetContentType.VERBO) {
                views.setViewVisibility(R.id.widget_verbo_box, View.VISIBLE)
                views.setTextViewText(R.id.widget_verbo_base, data.termino)
                views.setTextViewText(R.id.widget_verbo_pasado, data.pasadoSimple)
                views.setTextViewText(
                    R.id.widget_verbo_participio,
                    data.participioPasado
                )
            } else {
                views.setViewVisibility(R.id.widget_verbo_box, View.GONE)
                views.setTextViewText(R.id.widget_verbo_base, "")
                views.setTextViewText(R.id.widget_verbo_pasado, "")
                views.setTextViewText(R.id.widget_verbo_participio, "")
            }

            views.setOnClickPendingIntent(
                R.id.widget_root,
                pendingIntentSolicitarAbrirApp(context)
            )

            views.setOnClickPendingIntent(
                R.id.widget_btn_next,
                pendingIntentSiguiente(context, appWidgetId)
            )

            views.setOnClickPendingIntent(
                R.id.widget_btn_sound,
                pendingIntentSonido(context, appWidgetId, data.termino)
            )

            views.setOnClickPendingIntent(
                R.id.widget_btn_learned,
                pendingIntentAprendida(context, appWidgetId)
            )

        }

        private fun aplicarAparienciaCompacta(
            views: RemoteViews,
            appearance: WidgetAppearanceSettings
        ) {
            val colors = appearance.themeColors()

            views.setWidgetBackground(
                viewId = R.id.widget_root,
                backgroundColor = colors.background,
                backgroundResource = appearance.rootBackgroundRes(compact = true)
            )
            views.setWidgetBackground(
                viewId = R.id.widget_compact_header,
                backgroundColor = colors.primary,
                backgroundResource = appearance.headerBackgroundRes(compact = true)
            )
            views.setInt(R.id.widget_btn_next, "setBackgroundResource", appearance.compactButtonBackgroundRes())
            views.setInt(R.id.widget_btn_learned, "setBackgroundResource", appearance.compactButtonBackgroundRes())

            views.setTextColor(R.id.widget_text_lote, colors.onPrimary)
            views.setTextColor(R.id.widget_text_progress, colors.onPrimary)
            views.setTextColor(R.id.widget_text_word, colors.primary)
            views.setTextColor(R.id.widget_text_pronunciation, colors.muted)
            views.setTextColor(R.id.widget_text_translation, colors.text)
            views.setTextColor(R.id.widget_btn_next, colors.onPrimary)
            views.setTextColor(R.id.widget_btn_learned, colors.onPrimary)

            views.setTextSize(R.id.widget_text_lote, 12f, appearance)
            views.setTextSize(R.id.widget_text_progress, 12f, appearance)
            views.setTextSize(R.id.widget_text_word, 18f, appearance)
            views.setTextSize(R.id.widget_text_pronunciation, 11f, appearance)
            views.setTextSize(R.id.widget_text_translation, 14f, appearance)
            views.setTextSize(R.id.widget_btn_next, 15f, appearance)
            views.setTextSize(R.id.widget_btn_learned, 17f, appearance)
        }

        private fun aplicarAparienciaNormal(
            views: RemoteViews,
            appearance: WidgetAppearanceSettings
        ) {
            val colors = appearance.themeColors()

            views.setWidgetBackground(
                viewId = R.id.widget_root,
                backgroundColor = colors.background,
                backgroundResource = appearance.rootBackgroundRes(compact = false)
            )
            views.setWidgetBackground(
                viewId = R.id.widget_header,
                backgroundColor = colors.primary,
                backgroundResource = appearance.headerBackgroundRes(compact = false)
            )
            views.setInt(R.id.widget_btn_next, "setBackgroundResource", appearance.buttonBackgroundRes())
            views.setInt(R.id.widget_btn_learned, "setBackgroundResource", appearance.buttonBackgroundRes())
            views.setInt(R.id.widget_btn_sound, "setBackgroundResource", appearance.soundBackgroundRes())

            aplicarColoresWidgetBase(views, colors)
            views.setTextColor(R.id.widget_extra, colors.primary)

            views.setTextSize(R.id.widget_lote_nombre, 11f, appearance)
            views.setTextSize(R.id.widget_progreso, 11f, appearance)
            views.setTextSize(R.id.widget_termino, 20f, appearance)
            views.setTextSize(R.id.widget_extra, 12f, appearance)
            views.setTextSize(R.id.widget_fonetica, 12f, appearance)
            views.setTextSize(R.id.widget_traduccion, 14f, appearance)
            views.setTextSize(R.id.widget_btn_next, 13f, appearance)
            views.setTextSize(R.id.widget_btn_learned, 16f, appearance)
        }

        private fun aplicarAparienciaGrande(
            views: RemoteViews,
            appearance: WidgetAppearanceSettings
        ) {
            val colors = appearance.themeColors()

            views.setWidgetBackground(
                viewId = R.id.widget_root,
                backgroundColor = colors.background,
                backgroundResource = appearance.rootBackgroundRes(compact = false)
            )
            views.setWidgetBackground(
                viewId = R.id.widget_header,
                backgroundColor = colors.primary,
                backgroundResource = appearance.headerBackgroundRes(compact = false)
            )
            views.setInt(R.id.widget_btn_next, "setBackgroundResource", appearance.buttonBackgroundRes())
            views.setInt(R.id.widget_btn_learned, "setBackgroundResource", appearance.buttonBackgroundRes())
            views.setInt(R.id.widget_btn_sound, "setBackgroundResource", appearance.soundBackgroundRes())

            aplicarColoresWidgetBase(views, colors)
            views.setTextColor(R.id.widget_verbo_base_label, colors.text)
            views.setTextColor(R.id.widget_verbo_pasado_label, colors.text)
            views.setTextColor(R.id.widget_verbo_participio_label, colors.text)
            views.setTextColor(R.id.widget_verbo_base, colors.primary)
            views.setTextColor(R.id.widget_verbo_pasado, colors.primary)
            views.setTextColor(R.id.widget_verbo_participio, colors.primary)

            views.setTextSize(R.id.widget_lote_nombre, 12f, appearance)
            views.setTextSize(R.id.widget_progreso, 12f, appearance)
            views.setTextSize(R.id.widget_termino, 24f, appearance)
            views.setTextSize(R.id.widget_fonetica, 12f, appearance)
            views.setTextSize(R.id.widget_traduccion, 15f, appearance)
            views.setTextSize(R.id.widget_btn_next, 13f, appearance)
            views.setTextSize(R.id.widget_btn_learned, 17f, appearance)
            views.setTextSize(R.id.widget_verbo_base_label, 10f, appearance)
            views.setTextSize(R.id.widget_verbo_pasado_label, 10f, appearance)
            views.setTextSize(R.id.widget_verbo_participio_label, 10f, appearance)
            views.setTextSize(R.id.widget_verbo_base, 14f, appearance)
            views.setTextSize(R.id.widget_verbo_pasado, 14f, appearance)
            views.setTextSize(R.id.widget_verbo_participio, 14f, appearance)
        }

        private fun aplicarColoresWidgetBase(
            views: RemoteViews,
            colors: WidgetThemeColors
        ) {
            views.setTextColor(R.id.widget_lote_nombre, colors.onPrimary)
            views.setTextColor(R.id.widget_progreso, colors.onPrimary)
            views.setTextColor(R.id.widget_termino, colors.primary)
            views.setTextColor(R.id.widget_fonetica, colors.muted)
            views.setTextColor(R.id.widget_traduccion, colors.text)
            views.setTextColor(R.id.widget_btn_next, colors.onPrimary)
            views.setTextColor(R.id.widget_btn_learned, colors.onPrimary)
        }

        private fun aplicarHeaderVisibility(
            views: RemoteViews,
            headerId: Int,
            loteId: Int,
            progresoId: Int,
            appearance: WidgetAppearanceSettings
        ) {
            views.setViewVisibility(
                headerId,
                View.VISIBLE
            )
            views.setViewVisibility(
                loteId,
                View.VISIBLE
            )
            views.setViewVisibility(
                progresoId,
                if (appearance.mostrarProgreso) View.VISIBLE else View.GONE
            )
        }

        private fun WidgetAppearanceSettings.headerTitle(loteNombre: String): String {
            return if (mostrarLote) {
                loteNombre.ifBlank { "WidgetEnglish" }
            } else {
                "WidgetEnglish"
            }
        }

        private fun aplicarTranslationVisibility(
            views: RemoteViews,
            translationId: Int,
            translation: String,
            appearance: WidgetAppearanceSettings
        ) {
            views.setViewVisibility(
                translationId,
                if (appearance.mostrarTraduccion && translation.isNotBlank()) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
            )
        }

        private fun WidgetAppearanceSettings.themeColors(): WidgetThemeColors {
            val baseColors = when (colorTheme) {
                WidgetColorTheme.AZUL -> WidgetThemeColors(
                    primary = 0xFF1565C0.toInt(),
                    background = Color.WHITE,
                    text = 0xFF333333.toInt(),
                    muted = 0xFF7B8EA6.toInt(),
                    soundBackground = 0xFFE3F2FD.toInt()
                )

                WidgetColorTheme.MORADO -> WidgetThemeColors(
                    primary = 0xFF7C3AED.toInt(),
                    background = 0xFFFBF8FF.toInt(),
                    text = 0xFF2D2145.toInt(),
                    muted = 0xFF8B7AA8.toInt(),
                    soundBackground = 0xFFF0EBFF.toInt()
                )

                WidgetColorTheme.VERDE -> WidgetThemeColors(
                    primary = 0xFF059669.toInt(),
                    background = 0xFFF4FFF9.toInt(),
                    text = 0xFF143D2C.toInt(),
                    muted = 0xFF6B8E7C.toInt(),
                    soundBackground = 0xFFE7F8EF.toInt()
                )

                WidgetColorTheme.NARANJA -> WidgetThemeColors(
                    primary = 0xFFF97316.toInt(),
                    background = 0xFFFFFBF5.toInt(),
                    text = 0xFF3F2A1D.toInt(),
                    muted = 0xFFA0795D.toInt(),
                    soundBackground = 0xFFFFF0E6.toInt()
                )

                WidgetColorTheme.TURQUESA -> WidgetThemeColors(
                    primary = 0xFF0891B2.toInt(),
                    background = 0xFFF0FDFF.toInt(),
                    text = 0xFF12363F.toInt(),
                    muted = 0xFF5F8792.toInt(),
                    soundBackground = 0xFFE6FAFD.toInt()
                )

                WidgetColorTheme.ROSA -> WidgetThemeColors(
                    primary = 0xFFDB2777.toInt(),
                    background = 0xFFFFF7FB.toInt(),
                    text = 0xFF442038.toInt(),
                    muted = 0xFFA36B8B.toInt(),
                    soundBackground = 0xFFFDE7F3.toInt()
                )

                WidgetColorTheme.INDIGO -> WidgetThemeColors(
                    primary = 0xFF4F46E5.toInt(),
                    background = 0xFFF8FAFF.toInt(),
                    text = 0xFF202044.toInt(),
                    muted = 0xFF777AA6.toInt(),
                    soundBackground = 0xFFEEF2FF.toInt()
                )

                WidgetColorTheme.ROJO -> WidgetThemeColors(
                    primary = 0xFFDC2626.toInt(),
                    background = 0xFFFFF7F7.toInt(),
                    text = 0xFF451A1A.toInt(),
                    muted = 0xFFA46666.toInt(),
                    soundBackground = 0xFFFEE2E2.toInt()
                )

                WidgetColorTheme.CIELO_SUAVE -> WidgetThemeColors(
                    primary = 0xFF60A5FA.toInt(),
                    background = 0xFFF0F7FF.toInt(),
                    text = 0xFF1E3A5F.toInt(),
                    muted = 0xFF6B8FB9.toInt(),
                    onPrimary = 0xFF0F172A.toInt(),
                    soundBackground = 0xFFE4F1FF.toInt()
                )

                WidgetColorTheme.LAVANDA_SUAVE -> WidgetThemeColors(
                    primary = 0xFFA78BFA.toInt(),
                    background = 0xFFFBF7FF.toInt(),
                    text = 0xFF322653.toInt(),
                    muted = 0xFF8A77B3.toInt(),
                    onPrimary = 0xFF1F1833.toInt(),
                    soundBackground = 0xFFF2ECFF.toInt()
                )

                WidgetColorTheme.MENTA_SUAVE -> WidgetThemeColors(
                    primary = 0xFF0D9488.toInt(),
                    background = 0xFFF0FDFA.toInt(),
                    text = 0xFF143F3A.toInt(),
                    muted = 0xFF5B8F86.toInt(),
                    soundBackground = 0xFFDCFDF7.toInt()
                )

                WidgetColorTheme.CORAL_SUAVE -> WidgetThemeColors(
                    primary = 0xFFFB7185.toInt(),
                    background = 0xFFFFF5F7.toInt(),
                    text = 0xFF4A2230.toInt(),
                    muted = 0xFFA06A78.toInt(),
                    onPrimary = 0xFF3A1620.toInt(),
                    soundBackground = 0xFFFFE7EC.toInt()
                )

                WidgetColorTheme.CRISTAL -> WidgetThemeColors(
                    primary = 0xFF2563EB.toInt(),
                    background = 0xD9FFFFFF.toInt(),
                    text = 0xFF162033.toInt(),
                    muted = 0xFF667085.toInt(),
                    soundBackground = 0xBFE0F2FE.toInt()
                )

                WidgetColorTheme.AURORA -> WidgetThemeColors(
                    primary = 0xFF7C3AED.toInt(),
                    background = 0xFFFFF7FD.toInt(),
                    text = 0xFF2D1838.toInt(),
                    muted = 0xFF8B6C9C.toInt(),
                    soundBackground = 0xFFF7E8FF.toInt()
                )

                WidgetColorTheme.OCEANO -> WidgetThemeColors(
                    primary = 0xFF0369A1.toInt(),
                    background = 0xFFEFFBFF.toInt(),
                    text = 0xFF123341.toInt(),
                    muted = 0xFF5D8798.toInt(),
                    soundBackground = 0xFFDFF8FF.toInt()
                )

                WidgetColorTheme.OSCURO -> WidgetThemeColors(
                    primary = 0xFF38BDF8.toInt(),
                    background = 0xFF111827.toInt(),
                    text = 0xFFF9FAFB.toInt(),
                    muted = 0xFFB6C2D1.toInt(),
                    soundBackground = 0xFF1F2937.toInt()
                )
            }

            return baseColors.withVisualStyle(visualStyle)
        }

        private fun WidgetThemeColors.withVisualStyle(
            style: WidgetVisualStyle
        ): WidgetThemeColors {
            return when (style) {
                WidgetVisualStyle.CLASICO -> this

                WidgetVisualStyle.MINIMALISTA -> copy(
                    background = Color.WHITE,
                    text = 0xFF1F2937.toInt(),
                    muted = 0xFF64748B.toInt(),
                    soundBackground = 0xFFF8FAFC.toInt()
                )

                WidgetVisualStyle.CARD_SUAVE -> copy(
                    background = when (primary) {
                        0xFF38BDF8.toInt() -> 0xFF0F172A.toInt()
                        else -> soundBackground
                    },
                    muted = when (primary) {
                        0xFF38BDF8.toInt() -> 0xFFCBD5E1.toInt()
                        else -> muted
                    }
                )

                WidgetVisualStyle.CONTRASTE_ALTO -> copy(
                    background = Color.WHITE,
                    text = Color.BLACK,
                    muted = 0xFF334155.toInt(),
                    soundBackground = 0xFFF1F5F9.toInt()
                )

                WidgetVisualStyle.NOCTURNO -> copy(
                    background = 0xFF0F172A.toInt(),
                    text = 0xFFF8FAFC.toInt(),
                    muted = 0xFFCBD5E1.toInt(),
                    soundBackground = 0xFF1E293B.toInt()
                )
            }
        }

        private fun WidgetAppearanceSettings.textScale(): Float {
            return when (textSize) {
                WidgetTextSizeOption.COMPACTO -> 0.9f
                WidgetTextSizeOption.NORMAL -> 1f
                WidgetTextSizeOption.GRANDE -> 1.14f
            }
        }

        private fun WidgetAppearanceSettings.buttonBackgroundRes(): Int {
            return when (colorTheme) {
                WidgetColorTheme.AZUL -> R.drawable.widget_button_bg
                WidgetColorTheme.MORADO -> R.drawable.widget_button_bg_morado
                WidgetColorTheme.VERDE -> R.drawable.widget_button_bg_verde
                WidgetColorTheme.NARANJA -> R.drawable.widget_button_bg_naranja
                WidgetColorTheme.TURQUESA -> R.drawable.widget_button_bg_turquesa
                WidgetColorTheme.ROSA -> R.drawable.widget_button_bg_rosa
                WidgetColorTheme.INDIGO -> R.drawable.widget_button_bg_indigo
                WidgetColorTheme.ROJO -> R.drawable.widget_button_bg_rojo
                WidgetColorTheme.CIELO_SUAVE -> R.drawable.widget_button_bg_cielo_suave
                WidgetColorTheme.LAVANDA_SUAVE -> R.drawable.widget_button_bg_lavanda_suave
                WidgetColorTheme.MENTA_SUAVE -> R.drawable.widget_button_bg_menta_suave
                WidgetColorTheme.CORAL_SUAVE -> R.drawable.widget_button_bg_coral_suave
                WidgetColorTheme.CRISTAL -> R.drawable.widget_button_bg_cristal
                WidgetColorTheme.AURORA -> R.drawable.widget_button_bg_aurora
                WidgetColorTheme.OCEANO -> R.drawable.widget_button_bg_oceano
                WidgetColorTheme.OSCURO -> R.drawable.widget_button_bg_oscuro
            }
        }

        private fun WidgetAppearanceSettings.compactButtonBackgroundRes(): Int {
            return when (colorTheme) {
                WidgetColorTheme.AZUL -> R.drawable.widget_compact_primary_button_bg
                WidgetColorTheme.MORADO -> R.drawable.widget_compact_primary_button_bg_morado
                WidgetColorTheme.VERDE -> R.drawable.widget_compact_primary_button_bg_verde
                WidgetColorTheme.NARANJA -> R.drawable.widget_compact_primary_button_bg_naranja
                WidgetColorTheme.TURQUESA -> R.drawable.widget_compact_primary_button_bg_turquesa
                WidgetColorTheme.ROSA -> R.drawable.widget_compact_primary_button_bg_rosa
                WidgetColorTheme.INDIGO -> R.drawable.widget_compact_primary_button_bg_indigo
                WidgetColorTheme.ROJO -> R.drawable.widget_compact_primary_button_bg_rojo
                WidgetColorTheme.CIELO_SUAVE -> R.drawable.widget_compact_primary_button_bg_cielo_suave
                WidgetColorTheme.LAVANDA_SUAVE -> R.drawable.widget_compact_primary_button_bg_lavanda_suave
                WidgetColorTheme.MENTA_SUAVE -> R.drawable.widget_compact_primary_button_bg_menta_suave
                WidgetColorTheme.CORAL_SUAVE -> R.drawable.widget_compact_primary_button_bg_coral_suave
                WidgetColorTheme.CRISTAL -> R.drawable.widget_compact_primary_button_bg_cristal
                WidgetColorTheme.AURORA -> R.drawable.widget_compact_primary_button_bg_aurora
                WidgetColorTheme.OCEANO -> R.drawable.widget_compact_primary_button_bg_oceano
                WidgetColorTheme.OSCURO -> R.drawable.widget_compact_primary_button_bg_oscuro
            }
        }

        private fun WidgetAppearanceSettings.soundBackgroundRes(): Int {
            return when (colorTheme) {
                WidgetColorTheme.AZUL -> R.drawable.widget_sound_bg
                WidgetColorTheme.MORADO -> R.drawable.widget_sound_bg_morado
                WidgetColorTheme.VERDE -> R.drawable.widget_sound_bg_verde
                WidgetColorTheme.NARANJA -> R.drawable.widget_sound_bg_naranja
                WidgetColorTheme.TURQUESA -> R.drawable.widget_sound_bg_turquesa
                WidgetColorTheme.ROSA -> R.drawable.widget_sound_bg_rosa
                WidgetColorTheme.INDIGO -> R.drawable.widget_sound_bg_indigo
                WidgetColorTheme.ROJO -> R.drawable.widget_sound_bg_rojo
                WidgetColorTheme.CIELO_SUAVE -> R.drawable.widget_sound_bg_cielo_suave
                WidgetColorTheme.LAVANDA_SUAVE -> R.drawable.widget_sound_bg_lavanda_suave
                WidgetColorTheme.MENTA_SUAVE -> R.drawable.widget_sound_bg_menta_suave
                WidgetColorTheme.CORAL_SUAVE -> R.drawable.widget_sound_bg_coral_suave
                WidgetColorTheme.CRISTAL -> R.drawable.widget_sound_bg_cristal
                WidgetColorTheme.AURORA -> R.drawable.widget_sound_bg_aurora
                WidgetColorTheme.OCEANO -> R.drawable.widget_sound_bg_oceano
                WidgetColorTheme.OSCURO -> R.drawable.widget_sound_bg_oscuro
            }
        }

        private fun WidgetAppearanceSettings.rootBackgroundRes(compact: Boolean): Int? {
            if (visualStyle == WidgetVisualStyle.CONTRASTE_ALTO ||
                visualStyle == WidgetVisualStyle.MINIMALISTA ||
                visualStyle == WidgetVisualStyle.NOCTURNO
            ) {
                return null
            }

            return when (colorTheme) {
                WidgetColorTheme.CRISTAL -> {
                    if (compact) R.drawable.widget_compact_card_bg_cristal else R.drawable.widget_bg_cristal
                }
                WidgetColorTheme.AURORA -> {
                    if (compact) R.drawable.widget_compact_card_bg_aurora else R.drawable.widget_bg_aurora
                }
                WidgetColorTheme.OCEANO -> {
                    if (compact) R.drawable.widget_compact_card_bg_oceano else R.drawable.widget_bg_oceano
                }
                else -> null
            }
        }

        private fun WidgetAppearanceSettings.headerBackgroundRes(compact: Boolean): Int? {
            if (visualStyle == WidgetVisualStyle.CONTRASTE_ALTO) {
                return null
            }

            return when (colorTheme) {
                WidgetColorTheme.CRISTAL -> {
                    if (compact) R.drawable.widget_compact_header_bg_cristal else R.drawable.widget_header_bg_cristal
                }
                WidgetColorTheme.AURORA -> {
                    if (compact) R.drawable.widget_compact_header_bg_aurora else R.drawable.widget_header_bg_aurora
                }
                WidgetColorTheme.OCEANO -> {
                    if (compact) R.drawable.widget_compact_header_bg_oceano else R.drawable.widget_header_bg_oceano
                }
                else -> null
            }
        }

        private fun RemoteViews.setWidgetBackground(
            viewId: Int,
            backgroundColor: Int,
            backgroundResource: Int?
        ) {
            if (backgroundResource != null) {
                setInt(viewId, "setBackgroundResource", backgroundResource)
            } else {
                setInt(viewId, "setBackgroundColor", backgroundColor)
            }
        }

        private fun RemoteViews.setTextSize(
            viewId: Int,
            baseSp: Float,
            appearance: WidgetAppearanceSettings
        ) {
            setTextViewTextSize(
                viewId,
                TypedValue.COMPLEX_UNIT_SP,
                baseSp * appearance.textScale()
            )
        }

        private suspend fun obtenerContenidoWidget(context: Context): WidgetData {
            val db = DatabaseProvider.getDatabase(context)
            val prefs = WidgetPreferences.obtenerTodasLasPreferenciasRapidas(context)

            var loteId = prefs.loteId
            var loteNombre = prefs.loteNombre
            val userId = prefs.userId
            val wordIndex = prefs.wordIndex

            if (loteId.isNullOrBlank() && !userId.isNullOrBlank()) {
                val loteActivo = db.progresoDao()
                    .observarLoteActivo(userId)
                    .first()

                if (loteActivo != null) {
                    loteId = loteActivo.loteId
                    loteNombre = db.loteDao()
                        .obtenerLotePorId(loteActivo.loteId)
                        ?.nombre

                    if (!loteId.isNullOrBlank() && !loteNombre.isNullOrBlank()) {
                        WidgetPreferences.guardarLoteActivo(
                            context = context,
                            loteId = loteId,
                            loteNombre = loteNombre
                        )
                    }
                }
            }

            if (loteId.isNullOrBlank() || userId.isNullOrBlank()) {
                return WidgetData(
                    loteNombre = "WidgetEnglish",
                    progreso = "",
                    tipo = WidgetContentType.PALABRA,
                    termino = "Sin lote activo",
                    fonetica = "",
                    traduccion = "Activa un lote en la app"
                )
            }

            val contenidosSesion = obtenerContenidoSesionActual(
                context = context,
                loteId = loteId,
                userId = userId
            )

            if (contenidosSesion.isEmpty()) {
                return WidgetData(
                    loteNombre = loteNombre ?: "WidgetEnglish",
                    progreso = "",
                    tipo = WidgetContentType.PALABRA,
                    termino = "Sin contenido",
                    fonetica = "",
                    traduccion = "No hay palabras para estudiar"
                )
            }

            val safeIndex = wordIndex.coerceAtLeast(0) % contenidosSesion.size
            val item = contenidosSesion[safeIndex]

            return if (item.tipoContenido == TipoContenido.VERBO) {
                val verbo = db.verboDao().obtenerVerboPorId(item.contenidoId)

                WidgetData(
                    loteNombre = loteNombre ?: "WidgetEnglish",
                    progreso = "${safeIndex + 1} / ${contenidosSesion.size}",
                    tipo = WidgetContentType.VERBO,
                    termino = verbo?.formaBase.orEmpty(),
                    fonetica = verbo?.fonetica.orEmpty(),
                    traduccion = verbo?.traduccion.orEmpty(),
                    pasadoSimple = verbo?.pasadoSimple.orEmpty(),
                    participioPasado = verbo?.participioPasado.orEmpty()
                )
            } else {
                val palabra = db.palabraDao().obtenerPalabraPorId(item.contenidoId)

                WidgetData(
                    loteNombre = loteNombre ?: "WidgetEnglish",
                    progreso = "${safeIndex + 1} / ${contenidosSesion.size}",
                    tipo = WidgetContentType.PALABRA,
                    termino = palabra?.termino.orEmpty(),
                    fonetica = palabra?.fonetica.orEmpty(),
                    traduccion = palabra?.traduccion.orEmpty()
                )
            }
        }

        private suspend fun obtenerContenidoSesionActual(
            context: Context
        ): List<LoteContenidoEntity> {
            val prefs = WidgetPreferences.obtenerTodasLasPreferenciasRapidas(context)
            val loteId = prefs.loteId
            val userId = prefs.userId

            if (loteId.isNullOrBlank() || userId.isNullOrBlank()) {
                return emptyList()
            }

            return obtenerContenidoSesionActual(
                context = context,
                loteId = loteId,
                userId = userId
            )
        }

        private suspend fun obtenerContenidoSesionActual(
            context: Context,
            loteId: String,
            userId: String
        ): List<LoteContenidoEntity> {
            val db = DatabaseProvider.getDatabase(context)

            val repository = VocabularioRepositoryImpl(
                palabraDao = db.palabraDao(),
                verboDao = db.verboDao(),
                loteDao = db.loteDao(),
                progresoDao = db.progresoDao(),
                usuarioFirestoreDataSource = UsuarioFirestoreDataSource(
                    FirebaseFirestore.getInstance()
                )
            )

            val configuracion = LearningPreferences.obtenerConfiguracionRapida(context)

            val contenidosDelLote = repository
                .observarContenidoDeLote(loteId)
                .first()
                .sortedBy { it.orden }

            val progresosUsuario = repository
                .observarProgresoUsuario(userId)
                .first()

            return LearningContentSelector.seleccionarContenido(
                contenidos = contenidosDelLote,
                progresos = progresosUsuario,
                modo = configuracion.modoSeleccionContenido,
                limite = configuracion.objetivoEfectivo,
                diaInicioSecuencial = if (
                    configuracion.modoSeleccionContenido == ModoSeleccionContenido.SECUENCIAL
                ) {
                    val diaActual = LearningContentSelector.obtenerDiaLocalActual()

                    WidgetPreferences.obtenerDiaInicioSecuencial(
                        context = context,
                        loteId = loteId,
                        userId = userId,
                        diaActual = diaActual
                    )
                } else {
                    null
                }
            )
        }

        private fun mismoContenido(
            a: LoteContenidoEntity,
            b: LoteContenidoEntity
        ): Boolean {
            return a.contenidoId == b.contenidoId &&
                    a.tipoContenido == b.tipoContenido
        }

        private fun pendingIntentSiguiente(
            context: Context,
            appWidgetId: Int
        ): PendingIntent {
            val intent = Intent(context, WordWidgetProvider::class.java).apply {
                action = ACTION_NEXT_WORD
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            }

            return PendingIntent.getBroadcast(
                context,
                appWidgetId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        private fun pendingIntentAprendida(
            context: Context,
            appWidgetId: Int
        ): PendingIntent {
            val intent = Intent(context, WordWidgetProvider::class.java).apply {
                action = ACTION_MARK_LEARNED
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            }

            return PendingIntent.getBroadcast(
                context,
                appWidgetId + REQUEST_CODE_LEARNED_OFFSET,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        private fun pendingIntentSonido(
            context: Context,
            appWidgetId: Int,
            text: String
        ): PendingIntent {
            val intent = Intent(context, WordWidgetProvider::class.java).apply {
                action = ACTION_PLAY_SOUND
                putExtra(EXTRA_TEXT, text)
            }

            return PendingIntent.getBroadcast(
                context,
                appWidgetId + REQUEST_CODE_SOUND_OFFSET,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        private fun pendingIntentSolicitarAbrirApp(context: Context): PendingIntent {
            val intent = Intent(context, WordWidgetProvider::class.java).apply {
                action = ACTION_OPEN_APP_REQUEST
            }

            return PendingIntent.getBroadcast(
                context,
                REQUEST_CODE_OPEN_APP,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        private fun manejarSolicitudAbrirApp(context: Context) {
            val prefs = context.getSharedPreferences(
                WIDGET_PREFS_NAME,
                Context.MODE_PRIVATE
            )

            val now = System.currentTimeMillis()
            val lastTap = prefs.getLong(KEY_LAST_OPEN_TAP, 0L)
            val dentroDelTiempo = now - lastTap <= DOUBLE_TAP_WINDOW_MS

            if (dentroDelTiempo) {
                prefs.edit()
                    .putLong(KEY_LAST_OPEN_TAP, 0L)
                    .apply()

                abrirApp(context)
            } else {
                prefs.edit()
                    .putLong(KEY_LAST_OPEN_TAP, now)
                    .apply()

                Toast.makeText(
                    context,
                    "Toca otra vez para abrir WidgetEnglish",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        private fun abrirApp(context: Context) {
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }

            context.startActivity(intent)
        }
    }
}

private enum class WidgetContentType {
    PALABRA,
    VERBO
}

private data class WidgetData(
    val loteNombre: String,
    val progreso: String,
    val tipo: WidgetContentType,
    val termino: String,
    val fonetica: String,
    val traduccion: String,
    val pasadoSimple: String = "",
    val participioPasado: String = ""
) {
    val textoVerbo: String
        get() {
            val formas = listOf(
                termino,
                pasadoSimple,
                participioPasado
            ).filter { it.isNotBlank() }

            return formas.joinToString(" · ")
        }
}
