package com.jp.widgetenglish.features.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
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
import androidx.glance.currentState
import androidx.glance.layout.*
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.jp.widgetenglish.MainActivity
import com.jp.widgetenglish.data.local.database.DatabaseProvider
import com.jp.widgetenglish.data.local.datastore.WidgetPreferences
import com.jp.widgetenglish.data.local.entity.EstadoAprendizaje
import com.jp.widgetenglish.data.local.entity.ProgresoUsuarioEntity
import com.jp.widgetenglish.data.local.entity.TipoContenido
import kotlinx.coroutines.flow.first

class WordWidget : GlanceAppWidget() {

    override val stateDefinition: GlanceStateDefinition<*> =
        PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val loteId    = WidgetPreferences.obtenerLoteId(context).first()
        val loteNom   = WidgetPreferences.obtenerLoteNombre(context).first()
        val wordIndex = WidgetPreferences.obtenerWordIndex(context).first()
        val userId    = WidgetPreferences.obtenerUserId(context).first()

        val db = DatabaseProvider.getDatabase(context)

        // Obtener contenidos del lote
        val contenidos = if (!loteId.isNullOrBlank()) {
            db.loteDao().observarContenidoDeLote(loteId).first()
        } else emptyList()

        // Obtener la palabra actual
        val itemActual = contenidos.getOrNull(wordIndex)
        val total = contenidos.size

        var termino     = "Sin lote activo"
        var traduccion  = ""
        var fonetica    = ""
        var progreso    = ""
        var esVerbo     = false
        var aprendida   = false

        if (itemActual != null) {
            esVerbo = itemActual.tipoContenido.name == "VERBO"
            val itemId = itemActual.contenidoId

            if (esVerbo) {
                val verbo = db.verboDao().obtenerVerboPorId(itemActual.contenidoId)
                if (verbo != null) {
                    termino    = verbo.formaBase
                    traduccion = verbo.traduccion
                    fonetica   = verbo.fonetica ?: ""
                }
            } else {
                val palabra = db.palabraDao().obtenerPalabraPorId(itemActual.contenidoId)
                if (palabra != null) {
                    termino    = palabra.termino
                    traduccion = palabra.traduccion
                    fonetica   = palabra.fonetica ?: ""
                }
            }

            progreso = "${wordIndex + 1} / $total"

            if (!userId.isNullOrBlank()) {
                val tipo = if (esVerbo) TipoContenido.VERBO else TipoContenido.PALABRA
                val prog = db.progresoDao().obtenerProgreso(userId, itemId, tipo)
                aprendida = prog?.estadoAprendizaje == EstadoAprendizaje.APRENDIDA
            }
        }

        provideContent {
            WidgetContent(
                termino    = termino,
                traduccion = traduccion,
                fonetica   = fonetica,
                progreso   = progreso,
                loteNombre = loteNom ?: "Sin lote activo",
                aprendida  = aprendida,
                hayLote    = !loteId.isNullOrBlank() && total > 0
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
    val azulPrimario = ColorProvider(Color(0xFF1565C0))
    val blanco       = ColorProvider(Color.White)
    val grisClaro    = ColorProvider(Color(0xFFF5F5F5))
    val verde        = ColorProvider(Color(0xFF2E7D32))

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ColorProvider(Color.White))
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
            horizontalAlignment = Alignment.Horizontal.End
        ) {
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
                horizontalAlignment = Alignment.Horizontal.CenterHorizontally
            ) {
                Text(
                    text = termino,
                    style = TextStyle(
                        color = azulPrimario,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                if (fonetica.isNotBlank()) {
                    Text(
                        text = fonetica,
                        style = TextStyle(
                            color = ColorProvider(Color(0xFF1565C0).copy(alpha = 0.7f)),
                            fontSize = 12.sp
                        )
                    )
                }
                Text(
                    text = traduccion,
                    style = TextStyle(
                        color = ColorProvider(Color(0xFF333333)),
                        fontSize = 14.sp
                    )
                )
            }

            // Botones
            Row(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalAlignment = Alignment.Horizontal.End
            ) {
                // Botón Siguiente
                Box(
                    modifier = GlanceModifier
                        .background(grisClaro)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                        .clickable(actionRunCallback<SiguientePalabraAction>())
                        .cornerRadius(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Siguiente →",
                        style = TextStyle(
                            color = azulPrimario,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                Spacer(GlanceModifier.width(8.dp))

                // Botón Aprendida
                if (!aprendida) {
                    Box(
                        modifier = GlanceModifier
                            .background(azulPrimario)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                            .clickable(actionRunCallback<MarcarAprendidaAction>())
                            .cornerRadius(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Aprendida ✓",
                            style = TextStyle(
                                color = blanco,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                } else {
                    Box(
                        modifier = GlanceModifier
                            .background(verde)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                            .cornerRadius(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "✓ Aprendida",
                            style = TextStyle(
                                color = blanco,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
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
                        color = ColorProvider(Color(0xFF888888)),
                        fontSize = 13.sp
                    )
                )
            }
        }
    }
}

// ─── Acción: Siguiente palabra ────────────────────────────────────────────────
class SiguientePalabraAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val db = DatabaseProvider.getDatabase(context)
        val loteId = WidgetPreferences.obtenerLoteId(context).first()

        if (!loteId.isNullOrBlank()) {
            val contenidos = db.loteDao().observarContenidoDeLote(loteId).first()
            if (contenidos.isNotEmpty()) {
                WidgetPreferences.avanzarPalabra(context, contenidos.size)
            }
        }
        WordWidget().update(context, glanceId)
    }
}

// ─── Acción: Marcar como aprendida ───────────────────────────────────────────
class MarcarAprendidaAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val db      = DatabaseProvider.getDatabase(context)
        val loteId  = WidgetPreferences.obtenerLoteId(context).first()
        val index   = WidgetPreferences.obtenerWordIndex(context).first()
        val userId  = WidgetPreferences.obtenerUserId(context).first()

        if (!loteId.isNullOrBlank() && !userId.isNullOrBlank()) {
            val contenidos = db.loteDao().observarContenidoDeLote(loteId).first()
            val item = contenidos.getOrNull(index)

            if (item != null) {
                val tipo = item.tipoContenido
                val id   = "${userId}_${item.contenidoId}_${tipo.name}"

                val progresoExistente = db.progresoDao()
                    .obtenerProgreso(userId, item.contenidoId, tipo)

                if (progresoExistente == null) {
                    db.progresoDao().insertarProgreso(
                        ProgresoUsuarioEntity(
                            id                 = id,
                            usuarioId          = userId,
                            contenidoId        = item.contenidoId,
                            tipoContenido      = tipo,
                            estadoAprendizaje  = EstadoAprendizaje.APRENDIDA,
                            nivelDominio       = 1f,
                            respuestasCorrectas = 1,
                            respuestasIncorrectas = 0,
                            vecesRepasado      = 1,
                            aprendido          = true,
                            favorito           = false,
                            ultimaRevision     = System.currentTimeMillis(),
                            proximaRevision    = null
                        )
                    )
                } else {
                    db.progresoDao().marcarComoAprendido(userId, item.contenidoId, tipo)
                }
            }
        }
        WordWidget().update(context, glanceId)
    }
}