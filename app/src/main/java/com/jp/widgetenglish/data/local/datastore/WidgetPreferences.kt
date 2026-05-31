package com.jp.widgetenglish.data.local.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

import kotlinx.coroutines.flow.first

val Context.widgetDataStore by preferencesDataStore(name = "widget_prefs")

object WidgetPreferences {

    private val KEY_LOTE_ID     = stringPreferencesKey("lote_activo_id")
    private val KEY_LOTE_NOMBRE = stringPreferencesKey("lote_activo_nombre")
    private val KEY_WORD_INDEX  = intPreferencesKey("word_index")
    private val KEY_USER_ID     = stringPreferencesKey("user_id")
    private val KEY_SEQUENTIAL_LOTE_ID = stringPreferencesKey("sequential_lote_id")
    private val KEY_SEQUENTIAL_USER_ID = stringPreferencesKey("sequential_user_id")
    private val KEY_SEQUENTIAL_START_DAY = intPreferencesKey("sequential_start_day")

    fun obtenerLoteId(context: Context): Flow<String?> =
        context.widgetDataStore.data.map { it[KEY_LOTE_ID] }

    fun obtenerLoteNombre(context: Context): Flow<String?> =
        context.widgetDataStore.data.map { it[KEY_LOTE_NOMBRE] }

    fun obtenerWordIndex(context: Context): Flow<Int> =
        context.widgetDataStore.data.map { it[KEY_WORD_INDEX] ?: 0 }

    fun obtenerUserId(context: Context): Flow<String?> =
        context.widgetDataStore.data.map { it[KEY_USER_ID] }

    suspend fun guardarLoteActivo(context: Context, loteId: String, loteNombre: String) {
        context.widgetDataStore.edit { prefs ->
            prefs[KEY_LOTE_ID]     = loteId
            prefs[KEY_LOTE_NOMBRE] = loteNombre
            prefs[KEY_WORD_INDEX]  = 0
        }
    }

    suspend fun guardarUserId(context: Context, userId: String) {
        context.widgetDataStore.edit { it[KEY_USER_ID] = userId }
    }

    suspend fun limpiarSesionWidget(context: Context) {
        context.widgetDataStore.edit { prefs ->
            prefs.remove(KEY_LOTE_ID)
            prefs.remove(KEY_LOTE_NOMBRE)
            prefs.remove(KEY_WORD_INDEX)
            prefs.remove(KEY_USER_ID)
            prefs.remove(KEY_SEQUENTIAL_LOTE_ID)
            prefs.remove(KEY_SEQUENTIAL_USER_ID)
            prefs.remove(KEY_SEQUENTIAL_START_DAY)
        }
    }

    suspend fun avanzarPalabra(context: Context, total: Int) {
        context.widgetDataStore.edit { prefs ->
            val actual = prefs[KEY_WORD_INDEX] ?: 0
            prefs[KEY_WORD_INDEX] = (actual + 1) % total
        }
    }

    suspend fun reiniciarIndice(context: Context) {
        context.widgetDataStore.edit { it[KEY_WORD_INDEX] = 0 }
    }

    suspend fun actualizarIndiceDirecto(context: Context, nuevoIndice: Int) {
        context.widgetDataStore.edit { it[KEY_WORD_INDEX] = nuevoIndice }
    }

    suspend fun obtenerDiaInicioSecuencial(
        context: Context,
        loteId: String,
        userId: String,
        diaActual: Int
    ): Int {
        val prefs = context.widgetDataStore.data.first()
        val inicioGuardado = prefs[KEY_SEQUENTIAL_START_DAY]
        val mismaSesion = prefs[KEY_SEQUENTIAL_LOTE_ID] == loteId &&
                prefs[KEY_SEQUENTIAL_USER_ID] == userId

        if (mismaSesion && inicioGuardado != null) {
            return inicioGuardado
        }

        context.widgetDataStore.edit {
            it[KEY_SEQUENTIAL_LOTE_ID] = loteId
            it[KEY_SEQUENTIAL_USER_ID] = userId
            it[KEY_SEQUENTIAL_START_DAY] = diaActual
        }

        return diaActual
    }

    suspend fun reiniciarSesionSecuencial(context: Context) {
        context.widgetDataStore.edit { prefs ->
            prefs.remove(KEY_SEQUENTIAL_LOTE_ID)
            prefs.remove(KEY_SEQUENTIAL_USER_ID)
            prefs.remove(KEY_SEQUENTIAL_START_DAY)
        }
    }

    data class PreferenciasSnapshot(
        val loteId: String?,
        val loteNombre: String?,
        val wordIndex: Int,
        val userId: String?
    )

    suspend fun obtenerTodasLasPreferenciasRapidas(context: Context): PreferenciasSnapshot {
        val prefs = context.widgetDataStore.data.first()
        return PreferenciasSnapshot(
            loteId = prefs[KEY_LOTE_ID],
            loteNombre = prefs[KEY_LOTE_NOMBRE],
            wordIndex = prefs[KEY_WORD_INDEX] ?: 0,
            userId = prefs[KEY_USER_ID]
        )
    }
}
