package com.jp.widgetenglish.ai.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class AiDimens(
    val screenHorizontalPadding: Dp,
    val cardPadding: Dp,
    val heroIconSize: Dp,
    val titleSizeCompact: TextUnit
)

@Composable
fun rememberAiDimens(): AiDimens {
    val width = LocalConfiguration.current.screenWidthDp

    return when {
        width < 360 -> AiDimens(
            screenHorizontalPadding = 14.dp,
            cardPadding = 14.dp,
            heroIconSize = 76.dp,
            titleSizeCompact = 18.sp
        )

        width < 420 -> AiDimens(
            screenHorizontalPadding = 18.dp,
            cardPadding = 18.dp,
            heroIconSize = 84.dp,
            titleSizeCompact = 20.sp
        )

        else -> AiDimens(
            screenHorizontalPadding = 22.dp,
            cardPadding = 20.dp,
            heroIconSize = 92.dp,
            titleSizeCompact = 22.sp
        )
    }
}

object AiUiColors {
    val Blue = Color(0xFF1565FF)
    val Purple = Color(0xFF7A5CFF)
    val DarkText = Color(0xFF102A7A)
    val SoftBackground = Color(0xFFF5F7FC)
    val SoftCard = Color(0xFFFFFFFF)
    val LightBlue = Color(0xFFEAF2FF)
    val LightPurple = Color(0xFFF0EBFF)
    val LightGreen = Color(0xFFEAF7EE)
    val GrayText = Color(0xFF667085)
    val Border = Color(0xFFE8ECF4)
}

fun aiPrimaryGradient(): Brush {
    return Brush.horizontalGradient(
        colors = listOf(
            AiUiColors.Blue,
            AiUiColors.Purple
        )
    )
}