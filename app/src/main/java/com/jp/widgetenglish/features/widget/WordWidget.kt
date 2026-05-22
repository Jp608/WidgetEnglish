package com.jp.widgetenglish.features.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.*
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import androidx.glance.Image
import androidx.glance.ImageProvider
import com.jp.widgetenglish.MainActivity
import com.jp.widgetenglish.R
import com.jp.widgetenglish.data.local.database.DatabaseProvider
import com.jp.widgetenglish.data.local.datastore.WidgetPreferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import com.jp.widgetenglish.data.local.entity.EstadoAprendizaje
import com.jp.widgetenglish.data.local.entity.ProgresoUsuarioEntity
import com.jp.widgetenglish.data.local.entity.TipoContenido
import kotlinx.coroutines.flow.first

class WordWidget : GlanceAppWidget() {

    override val stateDefinition: GlanceStateDefinition<*> =
        PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val db = DatabaseProvider.getDatabase(context)
        
        // Carga de preferencias optimizada (una sola lectura rápida)
        val snapshot = WidgetPreferences.obtenerTodasLasPreferenciasRapidas(context)
        var loteId = snapshot.loteId
        var loteNom = snapshot.loteNombre
        val wordIndex = snapshot.wordIndex
        val userId = snapshot.userId

        // Recuperación automática solo si es estrictamente necesario
        if (loteId.isNullOrBlank() && !userId.isNullOrBlank()) {
            val activo = db.progresoDao().observarLoteActivo(userId).first()
            if (activo != null) {
                loteId = activo.loteId
                val info = db.loteDao().obtenerLotePorId(activo.loteId)
                loteNom = info?.nombre
                if (loteId != null && loteNom != null) {
                    WidgetPreferences.guardarLoteActivo(context, loteId!!, loteNom!!)
                }
            }
        }

        // Obtener contenidos del lote (Consulta directa a BD)
        val contenidos = if (!loteId.isNullOrBlank() && !userId.isNullOrBlank()) {
            db.loteDao().obtenerPendientesLoteDirecto(loteId!!, userId!!)
        } else emptyList<com.jp.widgetenglish.data.local.entity.LoteContenidoEntity>()

        val total = contenidos.size
        val safeIndex = if (total > 0) wordIndex % total else 0
        val itemActual = contenidos.getOrNull(safeIndex)

        var termino     = if (loteId.isNullOrBlank()) "Sin lote activo" else "¡Todo aprendido! 🎉"
        var traduccion  = if (loteId.isNullOrBlank()) "Activa uno en la app" else "Has completado este lote"
        var fonetica    = ""
        var progreso    = ""
        var esVerbo: Boolean = false

        if (itemActual != null) {
            esVerbo = itemActual.tipoContenido == TipoContenido.VERBO
            
            if (esVerbo) {
                val verbo = db.verboDao().obtenerVerboPorId(itemActual.contenidoId)
                termino = verbo?.formaBase ?: ""
                traduccion = verbo?.traduccion ?: ""
                fonetica = verbo?.fonetica ?: ""
            } else {
                val palabra = db.palabraDao().obtenerPalabraPorId(itemActual.contenidoId)
                termino = palabra?.termino ?: ""
                traduccion = palabra?.traduccion ?: ""
                fonetica = palabra?.fonetica ?: ""
            }
            progreso = "${safeIndex + 1} / $total"
        }

        provideContent {
            WidgetContent(
                termino    = termino,
                traduccion = traduccion,
                fonetica   = fonetica,
                progreso   = progreso,
                loteNombre = loteNom ?: "WidgetEnglish",
                aprendida  = false,
                hayLote    = total > 0
            )
        }
    }
}

@Composable
fun WidgetContent(
    termino: String,
    traduccion: String,
    fonetica: String,
    progreso: String,
    loteNombre: String,
    aprendida: Boolean,
    hayLote: Boolean
) {
    val azulPrimario = ColorProvider(R.color.blue_primary)
    val blanco       = ColorProvider(R.color.white)
    val grisClaro    = ColorProvider(R.color.gray_light)
    val verde        = ColorProvider(R.color.green_primary)

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ColorProvider(R.color.white))
            .clickable(actionStartActivity<MainActivity>()),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Header azul
        Row(
            modifier = GlanceModifier
                .fillMaxWidth()
                .background(azulPrimario)
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.End
        ) {
            Image(
                provider = ImageProvider(R.drawable.ic_app_logo),
                contentDescription = null,
                modifier = GlanceModifier.size(24.dp)
            )
            Spacer(GlanceModifier.width(8.dp))
            Text(
                text = loteNombre,
                style = TextStyle(
                    color = blanco,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                ),
                modifier = GlanceModifier.defaultWeight()
            )
            Text(
                text = progreso,
                style = TextStyle(color = blanco, fontSize = 11.sp)
            )
        }

        // Contenido central
        if (hayLote) {
            Column(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = termino,
                        style = TextStyle(
                            color = azulPrimario,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(GlanceModifier.width(8.dp))
                    // Botón de Sonido (Estilo App mejorado)
                    Box(
                        modifier = GlanceModifier
                            .size(34.dp)
                            .background(ColorProvider(R.color.soft_blue))
                            .cornerRadius(10.dp)
                            .clickable(
                                actionRunCallback<PlaySoundAction>(
                                    actionParametersOf(ActionParameters.Key<String>("text") to termino)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            provider = ImageProvider(R.drawable.ic_volume_up),
                            contentDescription = "Sonido",
                            modifier = GlanceModifier.size(20.dp)
                        )
                    }
                }
                
                if (fonetica.isNotBlank()) {
                    Text(
                        text = fonetica,
                        style = TextStyle(
                            color = ColorProvider(R.color.blue_primary_alpha),
                            fontSize = 12.sp
                        )
                    )
                }
                Text(
                    text = traduccion,
                    style = TextStyle(
                        color = ColorProvider(R.color.text_dark),
                        fontSize = 14.sp
                    )
                )
            }

            // Botón Siguiente Único y Optimizado
            Row(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = GlanceModifier
                        .background(azulPrimario)
                        .padding(horizontal = 24.dp, vertical = 6.dp)
                        .clickable(actionRunCallback<SiguientePalabraAction>())
                        .cornerRadius(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Siguiente Palabra →",
                        style = TextStyle(
                            color = blanco,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

        } else if (loteNombre != "Sin lote activo") {
             // Lote completado
            Box(
                modifier = GlanceModifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "¡Lote completado! 🎉",
                    style = TextStyle(
                        color = verde,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        } else {
            // Sin lote activo
            Box(
                modifier = GlanceModifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Activa un lote para empezar 📚",
                    style = TextStyle(
                        color = ColorProvider(R.color.text_gray),
                        fontSize = 13.sp
                    )
                )
            }
        }
    }
}

// ─── Acción: Reproducir Sonido ───────────────────────────────────────────────
class PlaySoundAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val text = parameters[ActionParameters.Key<String>("text")] ?: return
        val intent = Intent(context, TtsReceiver::class.java).apply {
            putExtra("text", text)
        }
        context.sendBroadcast(intent)
    }
}

// ─── Acción: Siguiente palabra (RE-OPTIMIZADA) ──────────────────────────────
class SiguientePalabraAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val loteId = WidgetPreferences.obtenerLoteId(context).first()
        val userId = WidgetPreferences.obtenerUserId(context).first()

        if (!loteId.isNullOrBlank()) {
            val db = DatabaseProvider.getDatabase(context)
            // Consulta de conteo directo para velocidad turbo
            val count = db.loteDao().obtenerConteoPendientesLote(loteId, userId ?: "")
            
            if (count > 0) {
                val currentIndex = WidgetPreferences.obtenerWordIndex(context).first()
                val nextIndex = (currentIndex + 1) % count
                WidgetPreferences.actualizarIndiceDirecto(context, nextIndex)
            }
        }
        
        // Refresco visual instantáneo
        WordWidget().update(context, glanceId)
    }
}

// ─── Acción: Marcar como aprendida (Eliminada del Widget) ──────────────────────
// class MarcarAprendidaAction...
