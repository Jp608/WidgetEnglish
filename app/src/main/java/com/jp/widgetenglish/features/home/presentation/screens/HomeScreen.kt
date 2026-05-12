package com.jp.widgetenglish.features.home.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jp.widgetenglish.features.home.presentation.viewmodel.HomeViewModel

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onVocabularioClick: () -> Unit = {},
    onLotesClick: () -> Unit = {},
    onEstudioClick: () -> Unit = {},
    onIaClick: () -> Unit = {},
    onPerfilClick: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.cargarHome()
    }

    val loteActivo = state.loteActivo

    val progresoLote = if (loteActivo != null) {
        loteActivo.progresoPorcentaje / 100f
    } else {
        0f
    }

    val progresoObjetivo = if (state.objetivoDiario > 0) {
        state.progresoDiario.toFloat() / state.objetivoDiario.toFloat()
    } else {
        0f
    }

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                onInicioClick = {},
                onVocabularioClick = onVocabularioClick,
                onLotesClick = onLotesClick,
                onEstudioClick = onEstudioClick,
                onIaClick = onIaClick,
                onPerfilClick = onPerfilClick
            )
        },
        containerColor = Color(0xFFF5F5F5)
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
                .verticalScroll(rememberScrollState())
                .background(Color(0xFFF5F5F5))
        ) {
            HeaderHome(
                nombreUsuario = state.nombreUsuario,
                rachaActual = state.rachaActual
            )

            Spacer(modifier = Modifier.height(26.dp))

            HomeProgressCard(
                titulo = "Lote activo",
                subtitulo = state.loteActivoInfo?.nombre ?: "Sin lote activo",
                progreso = progresoLote,
                detalle = if (loteActivo != null) {
                    "${loteActivo.contenidosAprendidos} / ${loteActivo.totalContenidos} palabras"
                } else {
                    "0 / 0 palabras"
                },
                icono = Icons.Filled.Book
            )

            Spacer(modifier = Modifier.height(18.dp))

            HomeProgressCard(
                titulo = "Objetivo diario",
                subtitulo = "Ver ${state.objetivoDiario} palabras",
                progreso = progresoObjetivo,
                detalle = "${state.progresoDiario} / ${state.objetivoDiario} completadas",
                icono = Icons.Filled.Flag
            )

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "Accesos rápidos",
                modifier = Modifier.padding(horizontal = 24.dp),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A237E)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                QuickAccessCard(
                    titulo = "Vocabulario",
                    icono = Icons.Filled.Book,
                    background = Color(0xFFE8EAF6),
                    iconColor = Color(0xFF3949AB),
                    modifier = Modifier.weight(1f),
                    onClick = onVocabularioClick
                )

                QuickAccessCard(
                    titulo = "Lotes",
                    icono = Icons.Filled.Book,
                    background = Color(0xFFE3F2FD),
                    iconColor = Color(0xFF1565C0),
                    modifier = Modifier.weight(1f),
                    onClick = onLotesClick
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                QuickAccessCard(
                    titulo = "Estudio",
                    icono = Icons.Filled.School,
                    background = Color(0xFFE0F7FA),
                    iconColor = Color(0xFF00838F),
                    modifier = Modifier.weight(1f),
                    onClick = onEstudioClick
                )

                QuickAccessCard(
                    titulo = "IA Chat",
                    icono = Icons.Filled.Flag,
                    background = Color(0xFFF3E5F5),
                    iconColor = Color(0xFF8E24AA),
                    modifier = Modifier.weight(1f),
                    onClick = onIaClick
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "Resumen",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A237E),
                        fontSize = 18.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Palabras disponibles: ${state.palabras.size}",
                        color = Color.Gray
                    )

                    Text(
                        text = "Verbos disponibles: ${state.verbos.size}",
                        color = Color.Gray
                    )

                    Text(
                        text = "Lotes disponibles: ${state.lotes.size}",
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun HeaderHome(
    nombreUsuario: String,
    rachaActual: Int
) {
    val primerNombre = nombreUsuario
        .trim()
        .split(" ")
        .firstOrNull()
        ?.takeIf { it.isNotBlank() }
        ?: "Usuario"

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(265.dp)
            .clip(RoundedCornerShape(bottomStart = 60.dp, bottomEnd = 60.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1A237E),
                        Color(0xFF1565C0),
                        Color(0xFF039BE5)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "¡Hola, $primerNombre! 👋",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Sigue aprendiendo cada día",
                fontSize = 20.sp,
                color = Color.White.copy(alpha = 0.88f)
            )

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "🔥 $rachaActual días de racha",
                fontSize = 18.sp,
                color = Color.White.copy(alpha = 0.95f)
            )
        }
    }
}

@Composable
private fun HomeProgressCard(
    titulo: String,
    subtitulo: String,
    progreso: Float,
    detalle: String,
    icono: ImageVector
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icono,
                    contentDescription = null,
                    tint = Color(0xFF1565C0),
                    modifier = Modifier.size(26.dp)
                )

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = titulo,
                    color = Color(0xFF1565C0),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = subtitulo,
                color = Color(0xFFD8D8E0),
                fontWeight = FontWeight.Bold,
                fontSize = 19.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            LinearProgressIndicator(
                progress = { progreso.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(8.dp)),
                color = Color(0xFF039BE5),
                trackColor = Color(0xFFE3F2FD)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = detalle,
                color = Color.Gray,
                fontSize = 15.sp
            )
        }
    }
}

@Composable
private fun QuickAccessCard(
    titulo: String,
    icono: ImageVector,
    background: Color,
    iconColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(110.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = background),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icono,
                contentDescription = titulo,
                tint = iconColor,
                modifier = Modifier.size(38.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = titulo,
                color = iconColor,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
private fun BottomNavigationBar(
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
        BottomNavItem(
            titulo = "Inicio",
            icono = Icons.Filled.Home,
            selected = true,
            onClick = onInicioClick
        )

        BottomNavItem(
            titulo = "Vocabula\nrio",
            icono = Icons.Filled.Book,
            selected = false,
            onClick = onVocabularioClick
        )

        BottomNavItem(
            titulo = "Lotes",
            icono = Icons.Filled.Book,
            selected = false,
            onClick = onLotesClick
        )

        BottomNavItem(
            titulo = "Estudio",
            icono = Icons.Filled.School,
            selected = false,
            onClick = onEstudioClick
        )

        BottomNavItem(
            titulo = "IA",
            icono = Icons.Filled.Flag,
            selected = false,
            onClick = onIaClick
        )

        BottomNavItem(
            titulo = "Perfil",
            icono = Icons.Filled.Person,
            selected = false,
            onClick = onPerfilClick
        )
    }
}

@Composable
private fun BottomNavItem(
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
            tint = color,
            modifier = Modifier.size(25.dp)
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = titulo,
            color = color,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
            lineHeight = 13.sp
        )
    }
}