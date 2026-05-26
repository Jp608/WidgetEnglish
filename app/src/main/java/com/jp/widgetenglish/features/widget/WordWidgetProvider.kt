package com.jp.widgetenglish.features.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.RemoteViews
import com.google.firebase.firestore.FirebaseFirestore
import com.jp.widgetenglish.MainActivity
import com.jp.widgetenglish.R
import com.jp.widgetenglish.data.local.database.DatabaseProvider
import com.jp.widgetenglish.data.local.datastore.LearningPreferences
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
                        marcarPalabraActualComoAprendida(context.applicationContext)
                        avanzarPalabra(context.applicationContext)
                        updateAll(context.applicationContext)
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

        private suspend fun marcarPalabraActualComoAprendida(context: Context): Boolean {
            val db = DatabaseProvider.getDatabase(context)
            val prefs = WidgetPreferences.obtenerTodasLasPreferenciasRapidas(context)

            val loteId = prefs.loteId
            val userId = prefs.userId
            val wordIndex = prefs.wordIndex

            if (loteId.isNullOrBlank() || userId.isNullOrBlank()) {
                return false
            }

            val contenidosSesion = obtenerContenidoSesionActual(
                context = context,
                loteId = loteId,
                userId = userId
            )

            if (contenidosSesion.isEmpty()) {
                return false
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
                    progresoDao = db.progresoDao()
                )

                streakRepository.registrarActividadDiaria(
                    usuarioId = userId,
                    elementosEstudiados = 1
                )
            }

            return !yaEstabaAprendida
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
                        data = data
                    )
                }

                R.layout.widget_word_large -> {
                    configurarWidgetGrande(
                        context = context,
                        views = views,
                        appWidgetId = appWidgetId,
                        data = data
                    )
                }

                else -> {
                    configurarWidgetNormal(
                        context = context,
                        views = views,
                        appWidgetId = appWidgetId,
                        data = data
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
            data: WidgetData
        ) {
            views.setTextViewText(
                R.id.widget_text_lote,
                data.loteNombre.ifBlank { "Lote" }
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

            if (data.fonetica.isBlank()) {
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
            data: WidgetData
        ) {
            views.setTextViewText(R.id.widget_lote_nombre, data.loteNombre)
            views.setTextViewText(R.id.widget_progreso, data.progreso)
            views.setTextViewText(R.id.widget_termino, data.termino)
            views.setTextViewText(R.id.widget_fonetica, data.fonetica)
            views.setTextViewText(R.id.widget_traduccion, data.traduccion)

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
            data: WidgetData
        ) {
            views.setTextViewText(R.id.widget_lote_nombre, data.loteNombre)
            views.setTextViewText(R.id.widget_progreso, data.progreso)
            views.setTextViewText(R.id.widget_termino, data.termino)
            views.setTextViewText(R.id.widget_fonetica, data.fonetica)
            views.setTextViewText(R.id.widget_traduccion, data.traduccion)

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
                limite = configuracion.objetivoEfectivo
            )
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