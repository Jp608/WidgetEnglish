package com.jp.widgetenglish.data.local.datastore

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlin.math.abs

val Context.dailyGoalDataStore by preferencesDataStore(name = "daily_goal_prefs")

data class DailyGoalSettings(
    val automatico: Boolean,
    val objetivoManual: Int,
    val objetivoAutomaticoActual: Int
) {
    val objetivoEfectivo: Int
        get() = if (automatico) {
            objetivoAutomaticoActual.coerceIn(
                DailyGoalPreferences.MIN_OBJETIVO_AUTOMATICO,
                DailyGoalPreferences.MAX_OBJETIVO_AUTOMATICO
            )
        } else {
            DailyGoalPreferences.normalizarObjetivoManual(objetivoManual)
        }
}

object DailyGoalPreferences {

    const val MIN_OBJETIVO_AUTOMATICO = 5
    const val MAX_OBJETIVO_AUTOMATICO = 15
    const val OBJETIVO_AUTOMATICO_INICIAL = 5

    const val OBJETIVO_MANUAL_INICIAL = 10
    val OBJETIVOS_MANUALES = listOf(10, 15, 20, 25, 30, 35, 40)

    private val KEY_AUTOMATICO = booleanPreferencesKey("daily_goal_automatico")
    private val KEY_OBJETIVO_MANUAL = intPreferencesKey("daily_goal_manual")
    private val KEY_OBJETIVO_AUTOMATICO_ACTUAL =
        intPreferencesKey("daily_goal_automatico_actual")

    fun observarConfiguracion(context: Context): Flow<DailyGoalSettings> {
        return context.dailyGoalDataStore.data.map { prefs ->
            DailyGoalSettings(
                automatico = prefs[KEY_AUTOMATICO] ?: true,
                objetivoManual = normalizarObjetivoManual(
                    prefs[KEY_OBJETIVO_MANUAL] ?: OBJETIVO_MANUAL_INICIAL
                ),
                objetivoAutomaticoActual = (
                        prefs[KEY_OBJETIVO_AUTOMATICO_ACTUAL]
                            ?: OBJETIVO_AUTOMATICO_INICIAL
                        ).coerceIn(
                        MIN_OBJETIVO_AUTOMATICO,
                        MAX_OBJETIVO_AUTOMATICO
                    )
            )
        }
    }

    suspend fun obtenerConfiguracionRapida(context: Context): DailyGoalSettings {
        return observarConfiguracion(context).first()
    }

    suspend fun guardarAutomatico(
        context: Context,
        automatico: Boolean
    ) {
        context.dailyGoalDataStore.edit { prefs ->
            prefs[KEY_AUTOMATICO] = automatico
        }
    }

    suspend fun guardarObjetivoManual(
        context: Context,
        objetivo: Int
    ) {
        context.dailyGoalDataStore.edit { prefs ->
            prefs[KEY_AUTOMATICO] = false
            prefs[KEY_OBJETIVO_MANUAL] = normalizarObjetivoManual(objetivo)
        }
    }

    suspend fun guardarObjetivoAutomaticoActual(
        context: Context,
        objetivo: Int
    ) {
        context.dailyGoalDataStore.edit { prefs ->
            prefs[KEY_OBJETIVO_AUTOMATICO_ACTUAL] = objetivo.coerceIn(
                MIN_OBJETIVO_AUTOMATICO,
                MAX_OBJETIVO_AUTOMATICO
            )
        }
    }

    suspend fun aumentarObjetivoAutomatico(context: Context) {
        val actual = obtenerConfiguracionRapida(context).objetivoAutomaticoActual

        guardarObjetivoAutomaticoActual(
            context = context,
            objetivo = actual + 1
        )
    }

    suspend fun disminuirObjetivoAutomatico(context: Context) {
        val actual = obtenerConfiguracionRapida(context).objetivoAutomaticoActual

        guardarObjetivoAutomaticoActual(
            context = context,
            objetivo = actual - 1
        )
    }

    suspend fun reiniciar(context: Context) {
        context.dailyGoalDataStore.edit { prefs ->
            prefs[KEY_AUTOMATICO] = true
            prefs[KEY_OBJETIVO_MANUAL] = OBJETIVO_MANUAL_INICIAL
            prefs[KEY_OBJETIVO_AUTOMATICO_ACTUAL] = OBJETIVO_AUTOMATICO_INICIAL
        }
    }

    fun normalizarObjetivoManual(objetivo: Int): Int {
        return OBJETIVOS_MANUALES.minByOrNull { opcion ->
            abs(opcion - objetivo)
        } ?: OBJETIVO_MANUAL_INICIAL
    }
}
