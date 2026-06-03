package com.jp.widgetenglish.features.common

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowInsetsControllerCompat

val UserHeaderBlue = Color(0xFF2468D8)

@Composable
fun UserHeaderSystemBars() {
    UserSystemBars(
        statusBarColor = UserHeaderBlue,
        lightStatusBars = false
    )
}

@Composable
fun LightUserSystemBars() {
    UserSystemBars(
        statusBarColor = Color.White,
        lightStatusBars = true
    )
}

@Composable
fun UserSystemBars(
    statusBarColor: Color,
    lightStatusBars: Boolean,
    navigationBarColor: Color = Color.White,
    lightNavigationBars: Boolean = true
) {
    val view = LocalView.current

    SideEffect {
        val window = (view.context as? Activity)?.window

        if (window != null) {
            val controller = WindowInsetsControllerCompat(window, view)

            window.statusBarColor = statusBarColor.toArgb()
            window.navigationBarColor = navigationBarColor.toArgb()
            controller.isAppearanceLightStatusBars = lightStatusBars
            controller.isAppearanceLightNavigationBars = lightNavigationBars
        }
    }
}
