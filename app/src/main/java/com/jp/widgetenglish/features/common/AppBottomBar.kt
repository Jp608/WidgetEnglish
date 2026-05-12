package com.jp.widgetenglish.features.common

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
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
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
fun AppBottomBar(
    selectedRoute: String,
    onInicioClick: () -> Unit,
    onVocabularioClick: () -> Unit,
    onLotesClick: () -> Unit,
    onEstudioClick: () -> Unit,
    onIaClick: () -> Unit,
    onPerfilClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(78.dp)
            .background(Color.White)
            .navigationBarsPadding()
            .padding(horizontal = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        AppBottomItem(
            titulo = "Inicio",
            icono = Icons.Filled.Home,
            selected = selectedRoute == "home",
            onClick = onInicioClick
        )

        AppBottomItem(
            titulo = "Vocabula\nrio",
            icono = Icons.Filled.Book,
            selected = selectedRoute == "vocabulario",
            onClick = onVocabularioClick
        )

        AppBottomItem(
            titulo = "Lotes",
            icono = Icons.Filled.Book,
            selected = selectedRoute == "lotes",
            onClick = onLotesClick
        )

        AppBottomItem(
            titulo = "Estudio",
            icono = Icons.Filled.School,
            selected = selectedRoute == "estudio",
            onClick = onEstudioClick
        )

        AppBottomItem(
            titulo = "IA",
            icono = Icons.Filled.Flag,
            selected = selectedRoute == "ia",
            onClick = onIaClick
        )

        AppBottomItem(
            titulo = "Perfil",
            icono = Icons.Filled.Person,
            selected = selectedRoute == "profile",
            onClick = onPerfilClick
        )
    }
}

@Composable
private fun AppBottomItem(
    titulo: String,
    icono: ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    val color = if (selected) Color(0xFF1565C0) else Color(0xFFC2C5D1)
    val background = if (selected) Color(0xFFE3F2FD) else Color.Transparent

    Column(
        modifier = Modifier
            .width(62.dp)
            .fillMaxHeight()
            .clip(RoundedCornerShape(24.dp))
            .background(background)
            .clickable { onClick() }
            .padding(vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icono,
            contentDescription = titulo,
            tint = color
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = titulo,
            color = color,
            fontSize = 11.sp,
            textAlign = TextAlign.Center,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
            lineHeight = 12.sp
        )
    }
}