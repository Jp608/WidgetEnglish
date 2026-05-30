package com.jp.widgetenglish.features.admin.stats

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun StatsEmptyState(
    text: String,
    icon: ImageVector,
    iconColor: Color = Color(0xFF7B8190).copy(alpha = 0.3f)
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = iconColor
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = text,
            color = Color(0xFF7B8190),
            textAlign = TextAlign.Center,
            fontSize = 16.sp
        )
    }
}
