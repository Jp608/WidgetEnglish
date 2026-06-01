package com.jp.widgetenglish.features.admin.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AdminBottomBar(
    selected: String,
    onResumenClick: () -> Unit,
    onRankingClick: () -> Unit,
    onActividadClick: () -> Unit,
    onPerfilClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(78.dp)
            .background(Color.White)
            .navigationBarsPadding()
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        AdminBottomItem(
            title = "Resumen",
            icon = Icons.Filled.Home,
            selected = selected == "resumen",
            onClick = onResumenClick
        )

        AdminBottomItem(
            title = "Ranking",
            icon = Icons.Filled.EmojiEvents,
            selected = selected == "ranking",
            onClick = onRankingClick
        )

        AdminBottomItem(
            title = "Actividad",
            icon = Icons.Filled.TrendingUp,
            selected = selected == "actividad",
            onClick = onActividadClick
        )

        AdminBottomItem(
            title = "Perfil",
            icon = Icons.Filled.Person,
            selected = selected == "perfil",
            onClick = onPerfilClick
        )
    }
}

@Composable
private fun AdminBottomItem(
    title: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    val color = if (selected) Color(0xFF1565C0) else Color(0xFF9EA3B0)
    val background = if (selected) Color(0xFFE3F2FD) else Color.Transparent

    Column(
        modifier = Modifier
            .width(78.dp)
            .fillMaxHeight()
            .clip(RoundedCornerShape(24.dp))
            .background(background)
            .clickable { onClick() }
            .padding(vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = color
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = title,
            color = color,
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )
    }
}