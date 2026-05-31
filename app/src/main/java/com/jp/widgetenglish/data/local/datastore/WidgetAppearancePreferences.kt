package com.jp.widgetenglish.data.local.datastore

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

enum class WidgetColorTheme {
    AZUL,
    MORADO,
    VERDE,
    NARANJA,
    TURQUESA,
    ROSA,
    INDIGO,
    ROJO,
    CIELO_SUAVE,
    LAVANDA_SUAVE,
    MENTA_SUAVE,
    CORAL_SUAVE,
    CRISTAL,
    AURORA,
    OCEANO,
    OSCURO
}

enum class WidgetTextSizeOption {
    COMPACTO,
    NORMAL,
    GRANDE
}

enum class WidgetVisualStyle {
    CLASICO,
    MINIMALISTA,
    CARD_SUAVE,
    CONTRASTE_ALTO,
    NOCTURNO
}

data class WidgetAppearanceSettings(
    val colorTheme: WidgetColorTheme = WidgetColorTheme.AZUL,
    val visualStyle: WidgetVisualStyle = WidgetVisualStyle.CLASICO,
    val textSize: WidgetTextSizeOption = WidgetTextSizeOption.NORMAL,
    val mostrarLote: Boolean = true,
    val mostrarProgreso: Boolean = true,
    val mostrarFonetica: Boolean = true,
    val mostrarTraduccion: Boolean = true
)

object WidgetAppearancePreferences {

    val DEFAULT_SETTINGS = WidgetAppearanceSettings()

    private val KEY_COLOR_THEME = stringPreferencesKey("widget_appearance_color_theme")
    private val KEY_VISUAL_STYLE = stringPreferencesKey("widget_appearance_visual_style")
    private val KEY_TEXT_SIZE = stringPreferencesKey("widget_appearance_text_size")
    private val KEY_MOSTRAR_LOTE = booleanPreferencesKey("widget_appearance_mostrar_lote")
    private val KEY_MOSTRAR_PROGRESO = booleanPreferencesKey("widget_appearance_mostrar_progreso")
    private val KEY_MOSTRAR_FONETICA = booleanPreferencesKey("widget_appearance_mostrar_fonetica")
    private val KEY_MOSTRAR_TRADUCCION = booleanPreferencesKey("widget_appearance_mostrar_traduccion")

    fun observarConfiguracion(context: Context): Flow<WidgetAppearanceSettings> {
        return context.widgetDataStore.data.map { prefs ->
            WidgetAppearanceSettings(
                colorTheme = prefs[KEY_COLOR_THEME].toEnumOrDefault(WidgetColorTheme.AZUL),
                visualStyle = prefs[KEY_VISUAL_STYLE].toEnumOrDefault(WidgetVisualStyle.CLASICO),
                textSize = prefs[KEY_TEXT_SIZE].toEnumOrDefault(WidgetTextSizeOption.NORMAL),
                mostrarLote = prefs[KEY_MOSTRAR_LOTE] ?: true,
                mostrarProgreso = prefs[KEY_MOSTRAR_PROGRESO] ?: true,
                mostrarFonetica = prefs[KEY_MOSTRAR_FONETICA] ?: true,
                mostrarTraduccion = prefs[KEY_MOSTRAR_TRADUCCION] ?: true
            )
        }
    }

    suspend fun obtenerConfiguracionRapida(context: Context): WidgetAppearanceSettings {
        return observarConfiguracion(context).first()
    }

    suspend fun guardarConfiguracion(
        context: Context,
        settings: WidgetAppearanceSettings
    ) {
        context.widgetDataStore.edit { prefs ->
            prefs[KEY_COLOR_THEME] = settings.colorTheme.name
            prefs[KEY_VISUAL_STYLE] = settings.visualStyle.name
            prefs[KEY_TEXT_SIZE] = settings.textSize.name
            prefs[KEY_MOSTRAR_LOTE] = settings.mostrarLote
            prefs[KEY_MOSTRAR_PROGRESO] = settings.mostrarProgreso
            prefs[KEY_MOSTRAR_FONETICA] = settings.mostrarFonetica
            prefs[KEY_MOSTRAR_TRADUCCION] = settings.mostrarTraduccion
        }
    }

    suspend fun reiniciar(context: Context) {
        guardarConfiguracion(
            context = context,
            settings = DEFAULT_SETTINGS
        )
    }

    private inline fun <reified T : Enum<T>> String?.toEnumOrDefault(
        default: T
    ): T {
        return this?.let { rawValue ->
            runCatching {
                enumValueOf<T>(rawValue)
            }.getOrNull()
        } ?: default
    }
}
