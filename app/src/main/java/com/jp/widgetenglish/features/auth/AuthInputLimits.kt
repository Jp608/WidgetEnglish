package com.jp.widgetenglish.features.auth

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

object AuthInputLimits {
    const val NAME = 40
    const val EMAIL = 50
    const val PASSWORD = 25
    const val RESET_CODE = 6
}

fun String.limitTo(maxLength: Int): String = take(maxLength)

@Composable
fun AuthCharacterCounter(
    currentLength: Int,
    maxLength: Int,
    modifier: Modifier = Modifier
) {
    val isAtLimit = currentLength >= maxLength

    Text(
        text = "$currentLength/$maxLength",
        modifier = modifier,
        color = if (isAtLimit) Color(0xFF1565C0) else Color.Gray,
        fontSize = 11.sp,
        fontWeight = if (isAtLimit) FontWeight.SemiBold else FontWeight.Normal
    )
}
