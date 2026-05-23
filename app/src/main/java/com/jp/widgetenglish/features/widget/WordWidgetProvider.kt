package com.jp.widgetenglish.features.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.jp.widgetenglish.MainActivity
import com.jp.widgetenglish.R
import com.jp.widgetenglish.data.local.database.DatabaseProvider
import com.jp.widgetenglish.data.local.datastore.WidgetPreferences
import com.jp.widgetenglish.data.local.entity.LoteContenidoEntity
import com.jp.widgetenglish.data.local.entity.TipoContenido
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
            requestUpdateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        when (intent.action) {
            ACTION_NEXT_WORD -> {
                val pendingResult = goAsync()

                CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
                    try {
                        avanzarPalabra(context)
                        updateAll(context)
                    } finally {
                        pendingResult.finish()
                    }
                }
            }

            ACTION_PLAY_SOUND -> {
                val text = intent.getStringExtra(EXTRA_TEXT).orEmpty()
                if (text.isNotBlank()) {
                    val ttsIntent = Intent(context, TtsReceiver::class.java).apply {
                        putExtra("text", text)
                    }
                    context.sendBroadcast(ttsIntent)
                }
            }

            ACTION_REFRESH_WIDGET -> {
                val pendingResult = goAsync()

                CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
                    try {
                        updateAll(context)
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

        private val widgetScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

        fun requestUpdateAll(context: Context) {
            widgetScope.launch {
                updateAll(context)
            }
        }

        suspend fun updateAll(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, WordWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)

            appWidgetIds.forEach { appWidgetId ->
                updateWidget(context, appWidgetManager, appWidgetId)
            }
        }

        private fun requestUpdateWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            widgetScope.launch {
                updateWidget(context, appWidgetManager, appWidgetId)
            }
        }

        private suspend fun avanzarPalabra(context: Context) {
            val loteId = WidgetPreferences.obtenerLoteId(context).first()
            val userId = WidgetPreferences.obtenerUserId(context).first()

            if (loteId.isNullOrBlank() || userId.isNullOrBlank()) return

            val db = DatabaseProvider.getDatabase(context)
            val total = db.loteDao().obtenerConteoPendientesLote(loteId, userId)

            if (total > 0) {
                val currentIndex = WidgetPreferences.obtenerWordIndex(context).first()
                val nextIndex = (currentIndex + 1) % total
                WidgetPreferences.actualizarIndiceDirecto(context, nextIndex)
            }
        }

        private suspend fun updateWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val data = obtenerContenidoWidget(context)

            val views = RemoteViews(context.packageName, R.layout.widget_word)

            views.setTextViewText(R.id.widget_lote_nombre, data.loteNombre)
            views.setTextViewText(R.id.widget_progreso, data.progreso)
            views.setTextViewText(R.id.widget_termino, data.termino)
            views.setTextViewText(R.id.widget_fonetica, data.fonetica)
            views.setTextViewText(R.id.widget_traduccion, data.traduccion)

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

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        private suspend fun obtenerContenidoWidget(context: Context): WidgetData {
            val db = DatabaseProvider.getDatabase(context)
            val prefs = WidgetPreferences.obtenerTodasLasPreferenciasRapidas(context)

            var loteId = prefs.loteId
            var loteNombre = prefs.loteNombre
            val userId = prefs.userId
            val wordIndex = prefs.wordIndex

            if (loteId.isNullOrBlank() && !userId.isNullOrBlank()) {
                val loteActivo = db.progresoDao().observarLoteActivo(userId).first()

                if (loteActivo != null) {
                    loteId = loteActivo.loteId
                    loteNombre = db.loteDao().obtenerLotePorId(loteActivo.loteId)?.nombre

                    if (!loteId.isNullOrBlank() && !loteNombre.isNullOrBlank()) {
                        WidgetPreferences.guardarLoteActivo(context, loteId, loteNombre)
                    }
                }
            }

            if (loteId.isNullOrBlank() || userId.isNullOrBlank()) {
                return WidgetData(
                    loteNombre = "WidgetEnglish",
                    progreso = "",
                    termino = "Sin lote activo",
                    fonetica = "",
                    traduccion = "Activa un lote en la app"
                )
            }

            val pendientes: List<LoteContenidoEntity> =
                db.loteDao().obtenerPendientesLoteDirecto(loteId, userId)

            if (pendientes.isEmpty()) {
                return WidgetData(
                    loteNombre = loteNombre ?: "WidgetEnglish",
                    progreso = "",
                    termino = "¡Todo aprendido!",
                    fonetica = "",
                    traduccion = "Has completado este lote 🎉"
                )
            }

            val safeIndex = wordIndex % pendientes.size
            val item = pendientes[safeIndex]

            return if (item.tipoContenido == TipoContenido.VERBO) {
                val verbo = db.verboDao().obtenerVerboPorId(item.contenidoId)

                WidgetData(
                    loteNombre = loteNombre ?: "WidgetEnglish",
                    progreso = "${safeIndex + 1} / ${pendientes.size}",
                    termino = verbo?.formaBase.orEmpty(),
                    fonetica = verbo?.fonetica.orEmpty(),
                    traduccion = verbo?.traduccion.orEmpty()
                )
            } else {
                val palabra = db.palabraDao().obtenerPalabraPorId(item.contenidoId)

                WidgetData(
                    loteNombre = loteNombre ?: "WidgetEnglish",
                    progreso = "${safeIndex + 1} / ${pendientes.size}",
                    termino = palabra?.termino.orEmpty(),
                    fonetica = palabra?.fonetica.orEmpty(),
                    traduccion = palabra?.traduccion.orEmpty()
                )
            }
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
                appWidgetId + 10_000,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        private fun pendingIntentAbrirApp(context: Context): PendingIntent {
            val intent = Intent(context, MainActivity::class.java)

            return PendingIntent.getActivity(
                context,
                20_000,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }
    }
}

data class WidgetData(
    val loteNombre: String,
    val progreso: String,
    val termino: String,
    val fonetica: String,
    val traduccion: String
)