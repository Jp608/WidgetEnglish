package com.jp.widgetenglish.features.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
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
fun ConstructionScreen(
    titulo: String,
    descripcion: String = "Esta sección estará disponible próximamente.",
    onVolverInicio: () -> Unit,
    onPerfilClick: () -> Unit,
    onVocabularioClick: () -> Unit,
    onLotesClick: () -> Unit,
    onEstudioClick: () -> Unit,
    onIaClick: () -> Unit
) {
    Scaffold(
        bottomBar = {
            ConstructionBottomBar(
                onInicioClick = onVolverInicio,
                onVocabularioClick = onVocabularioClick,
                onLotesClick = onLotesClick,
                onEstudioClick = onEstudioClick,
                onIaClick = onIaClick,
                onPerfilClick = onPerfilClick
            )
        },
        containerColor = Color(0xFFF5F5F5)
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
                .background(Color(0xFFF5F5F5)),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 8.dp
                )
            ) {
                Column(
                    modifier = Modifier.padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Filled.Construction,
                        contentDescription = null,
                        tint = Color(0xFF1565C0),
                        modifier = Modifier.size(64.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = titulo,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A237E),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = descripcion,
                        fontSize = 15.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = onVolverInicio,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1565C0)
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(
                            text = "Volver al inicio",
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ConstructionBottomBar(
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
        ConstructionBottomItem(
            titulo = "Inicio",
            icono = Icons.Filled.Home,
            selected = false,
            onClick = onInicioClick
        )

        ConstructionBottomItem(
            titulo = "Vocabula\nrio",
            icono = Icons.Filled.Book,
            selected = false,
            onClick = onVocabularioClick
        )

        ConstructionBottomItem(
            titulo = "Lotes",
            icono = Icons.Filled.Book,
            selected = false,
            onClick = onLotesClick
        )

        ConstructionBottomItem(
            titulo = "Estudio",
            icono = Icons.Filled.School,
            selected = false,
            onClick = onEstudioClick
        )

        ConstructionBottomItem(
            titulo = "IA",
            icono = Icons.Filled.Flag,
            selected = false,
            onClick = onIaClick
        )

        ConstructionBottomItem(
            titulo = "Perfil",
            icono = Icons.Filled.Person,
            selected = false,
            onClick = onPerfilClick
        )
    }
}

@Composable
private fun ConstructionBottomItem(
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
            .height(70.dp)
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
            tint = color,
            modifier = Modifier.size(25.dp)
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = titulo,
            color = color,
            fontSize = 11.sp,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.SemiBold,
            lineHeight = 12.sp
        )
    }
}