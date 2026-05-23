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
import com.jp.widgetenglish.data.remote.firestore.UsuarioFirestoreDataSource
import com.jp.widgetenglish.data.repository.VocabularioRepositoryImpl
import com.jp.widgetenglish.domain.learning.LearningContentSelector
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

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
        }
    }

    companion object {
        const val ACTION_NEXT_WORD = "com.jp.widgetenglish.widget.ACTION_NEXT_WORD"
        const val ACTION_PLAY_SOUND = "com.jp.widgetenglish.widget.ACTION_PLAY_SOUND"
        const val ACTION_REFRESH_WIDGET = "com.jp.widgetenglish.widget.ACTION_REFRESH_WIDGET"
        const val EXTRA_TEXT = "extra_text"

        private const val REQUEST_CODE_OPEN_APP = 20_000
        private const val REQUEST_CODE_SOUND_OFFSET = 10_000

        private const val COMPACT_MAX_WIDTH = 170
        private const val COMPACT_MAX_HEIGHT = 100
        private const val LARGE_MIN_WIDTH = 260
        private const val LARGE_MIN_HEIGHT = 150

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
            views.setTextViewText(R.id.widget_termino, data.termino)
            views.setTextViewText(R.id.widget_traduccion, data.traduccion)

            views.setOnClickPendingIntent(
                R.id.widget_root,
                pendingIntentAbrirApp(context)
            )

            views.setOnClickPendingIntent(
                R.id.widget_btn_next,
                pendingIntentSiguiente(context, appWidgetId)
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
                pendingIntentAbrirApp(context)
            )

            views.setOnClickPendingIntent(
                R.id.widget_btn_next,
                pendingIntentSiguiente(context, appWidgetId)
            )

            views.setOnClickPendingIntent(
                R.id.widget_btn_sound,
                pendingIntentSonido(context, appWidgetId, data.termino)
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
                pendingIntentAbrirApp(context)
            )

            views.setOnClickPendingIntent(
                R.id.widget_btn_next,
                pendingIntentSiguiente(context, appWidgetId)
            )

            views.setOnClickPendingIntent(
                R.id.widget_btn_sound,
                pendingIntentSonido(context, appWidgetId, data.termino)
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

        private fun pendingIntentAbrirApp(context: Context): PendingIntent {
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }

            return PendingIntent.getActivity(
                context,
                REQUEST_CODE_OPEN_APP,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
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