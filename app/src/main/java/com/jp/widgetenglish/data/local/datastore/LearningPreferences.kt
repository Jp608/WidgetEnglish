package com.jp.widgetenglish.data.local.datastore

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

enum class ModoSeleccionContenido {
    SECUENCIAL,
    ALEATORIO,
    INTELIGENTE
}

data class LearningSettings(
    val modoSeleccionContenido: ModoSeleccionContenido,
    val objetivoDiarioAutomatico: Boolean,
    val objetivoDiarioManual: Int,
    val objetivoDiarioActual: Int
) {
    val objetivoEfectivo: Int
        get() = if (objetivoDiarioAutomatico) {
            objetivoDiarioActual
        } else {
            objetivoDiarioManual
        }.coerceIn(
            LearningPreferences.MIN_OBJETIVO_DIARIO,
            LearningPreferences.MAX_OBJETIVO_DIARIO
        )
}

object LearningPreferences {

    const val MIN_OBJETIVO_DIARIO = 5
    const val MAX_OBJETIVO_DIARIO = 15

    private val KEY_MODO_SELECCION_CONTENIDO =
        stringPreferencesKey("learning_modo_seleccion_contenido")

    private val KEY_OBJETIVO_DIARIO_AUTOMATICO =
        booleanPreferencesKey("learning_objetivo_diario_automatico")

    private val KEY_OBJETIVO_DIARIO_MANUAL =
        intPreferencesKey("learning_objetivo_diario_manual")

    private val KEY_OBJETIVO_DIARIO_ACTUAL =
        intPreferencesKey("learning_objetivo_diario_actual")

    fun observarModoSeleccionContenido(context: Context): Flow<ModoSeleccionContenido> {
        return context.widgetDataStore.data.map { prefs ->
            val rawValue = prefs[KEY_MODO_SELECCION_CONTENIDO]

            rawValue?.let {
                runCatching {
                    ModoSeleccionContenido.valueOf(it)
                }.getOrNull()
            } ?: ModoSeleccionContenido.INTELIGENTE
        }
    }

    fun observarObjetivoDiarioAutomatico(context: Context): Flow<Boolean> {
        return context.widgetDataStore.data.map { prefs ->
            prefs[KEY_OBJETIVO_DIARIO_AUTOMATICO] ?: true
        }
    }

    fun observarObjetivoDiarioManual(context: Context): Flow<Int> {
        return context.widgetDataStore.data.map { prefs ->
            val objetivo = prefs[KEY_OBJETIVO_DIARIO_MANUAL] ?: MIN_OBJETIVO_DIARIO
            objetivo.coerceIn(MIN_OBJETIVO_DIARIO, MAX_OBJETIVO_DIARIO)
        }
    }

    fun observarObjetivoDiarioActual(context: Context): Flow<Int> {
        return context.widgetDataStore.data.map { prefs ->
            val objetivo = prefs[KEY_OBJETIVO_DIARIO_ACTUAL] ?: MIN_OBJETIVO_DIARIO
            objetivo.coerceIn(MIN_OBJETIVO_DIARIO, MAX_OBJETIVO_DIARIO)
        }
    }

    suspend fun obtenerConfiguracionRapida(context: Context): LearningSettings {
        val prefs = context.widgetDataStore.data.first()

        val modoRaw = prefs[KEY_MODO_SELECCION_CONTENIDO]

        val modo = modoRaw?.let {
            runCatching {
                ModoSeleccionContenido.valueOf(it)
            }.getOrNull()
        } ?: ModoSeleccionContenido.INTELIGENTE

        val automatico = prefs[KEY_OBJETIVO_DIARIO_AUTOMATICO] ?: true

        val objetivoManual = (
                prefs[KEY_OBJETIVO_DIARIO_MANUAL] ?: MIN_OBJETIVO_DIARIO
                ).coerceIn(
                MIN_OBJETIVO_DIARIO,
                MAX_OBJETIVO_DIARIO
            )

        val objetivoActual = (
                prefs[KEY_OBJETIVO_DIARIO_ACTUAL] ?: MIN_OBJETIVO_DIARIO
                ).coerceIn(
                MIN_OBJETIVO_DIARIO,
                MAX_OBJETIVO_DIARIO
            )

        return LearningSettings(
            modoSeleccionContenido = modo,
            objetivoDiarioAutomatico = automatico,
            objetivoDiarioManual = objetivoManual,
            objetivoDiarioActual = objetivoActual
        )
    }

    suspend fun guardarModoSeleccionContenido(
        context: Context,
        modo: ModoSeleccionContenido
    ) {
        context.widgetDataStore.edit { prefs ->
            prefs[KEY_MODO_SELECCION_CONTENIDO] = modo.name
        }
    }

    suspend fun guardarObjetivoDiarioAutomatico(
        context: Context,
        automatico: Boolean
    ) {
        context.widgetDataStore.edit { prefs ->
            prefs[KEY_OBJETIVO_DIARIO_AUTOMATICO] = automatico
        }
    }

    suspend fun guardarObjetivoDiarioManual(
        context: Context,
        objetivo: Int
    ) {
        context.widgetDataStore.edit { prefs ->
            prefs[KEY_OBJETIVO_DIARIO_MANUAL] = objetivo.coerceIn(
                MIN_OBJETIVO_DIARIO,
                MAX_OBJETIVO_DIARIO
            )
        }
    }

    suspend fun guardarObjetivoDiarioActual(
        context: Context,
        objetivo: Int
    ) {
        context.widgetDataStore.edit { prefs ->
            prefs[KEY_OBJETIVO_DIARIO_ACTUAL] = objetivo.coerceIn(
                MIN_OBJETIVO_DIARIO,
                MAX_OBJETIVO_DIARIO
            )
        }
    }

    suspend fun aumentarObjetivoDiarioActual(context: Context) {
        val actual = observarObjetivoDiarioActual(context).first()

        guardarObjetivoDiarioActual(
            context = context,
            objetivo = actual + 1
        )
    }

    suspend fun disminuirObjetivoDiarioActual(context: Context) {
        val actual = observarObjetivoDiarioActual(context).first()

        guardarObjetivoDiarioActual(
            context = context,
            objetivo = actual - 1
        )
    }

    suspend fun reiniciarConfiguracionAprendizaje(context: Context) {
        context.widgetDataStore.edit { prefs ->
            prefs[KEY_MODO_SELECCION_CONTENIDO] = ModoSeleccionContenido.INTELIGENTE.name
            prefs[KEY_OBJETIVO_DIARIO_AUTOMATICO] = true
            prefs[KEY_OBJETIVO_DIARIO_MANUAL] = MIN_OBJETIVO_DIARIO
            prefs[KEY_OBJETIVO_DIARIO_ACTUAL] = MIN_OBJETIVO_DIARIO
        }
    }
}