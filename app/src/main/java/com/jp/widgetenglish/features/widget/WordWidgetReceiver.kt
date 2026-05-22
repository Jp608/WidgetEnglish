package com.jp.widgetenglish.features.widget

import androidx.glance.appwidget.GlanceAppWidgetReceiver

class WordWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget = WordWidget()
}