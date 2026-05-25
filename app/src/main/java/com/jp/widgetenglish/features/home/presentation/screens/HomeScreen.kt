package com.jp.widgetenglish.features.home.presentation.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CheckCircle
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

private val BackgroundColor = Color(0xFFF8FAFC)
private val PrimaryBlue = Color(0xFF2563EB)
private val DarkText = Color(0xFF111827)
private val MutedText = Color(0xFF6B7280)

private val SoftBlue = Color(0xFFEFF6FF)
private val SoftPurple = Color(0xFFF3E8FF)
private val SoftGreen = Color(0xFFEAFBF2)
private val SoftOrange = Color(0xFFFFF3E6)

private val SuccessGreen = Color(0xFF10B981)

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
        containerColor = BackgroundColor,
        topBar = {
            TopStatusBarBackground()
        },
        bottomBar = {
            BottomNavigationBar(
                onInicioClick = {},
                onVocabularioClick = onVocabularioClick,
                onLotesClick = onLotesClick,
                onEstudioClick = onEstudioClick,
                onIaClick = onIaClick,
                onPerfilClick = onPerfilClick
            )
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundColor)
                .padding(
                    top = innerPadding.calculateTopPadding(),
                    bottom = innerPadding.calculateBottomPadding()
                )
                .verticalScroll(rememberScrollState())
        ) {
            HeaderHome(
                nombreUsuario = state.nombreUsuario,
                rachaActual = state.rachaActual,
                objetivoCumplido = state.objetivoDiarioCumplido
            )

            Spacer(modifier = Modifier.height(26.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                HomeProgressCard(
                    titulo = "Lote activo",
                    subtitulo = state.loteActivoInfo?.nombre ?: "Sin lote activo",
                    progreso = progresoLote,
                    detalle = if (loteActivo != null) {
                        "${loteActivo.contenidosAprendidos} / ${loteActivo.totalContenidos} palabras"
                    } else {
                        "0 / 0 palabras"
                    },
                    icono = Icons.Filled.Book,
                    iconBackground = SoftBlue,
                    iconColor = PrimaryBlue,
                    progressColor = PrimaryBlue,
                    modifier = Modifier.weight(1f)
                )

                HomeProgressCard(
                    titulo = "Objetivo diario",
                    subtitulo = if (state.objetivoDiarioCumplido) {
                        "Cumplido hoy 🎉"
                    } else {
                        "Ver ${state.objetivoDiario} palabras"
                    },
                    progreso = progresoObjetivo,
                    detalle = "${state.progresoDiario} / ${state.objetivoDiario} completadas",
                    icono = if (state.objetivoDiarioCumplido) {
                        Icons.Filled.CheckCircle
                    } else {
                        Icons.Filled.Flag
                    },
                    iconBackground = if (state.objetivoDiarioCumplido) {
                        SoftGreen
                    } else {
                        SoftPurple
                    },
                    iconColor = if (state.objetivoDiarioCumplido) {
                        SuccessGreen
                    } else {
                        Color(0xFF7C3AED)
                    },
                    progressColor = if (state.objetivoDiarioCumplido) {
                        SuccessGreen
                    } else {
                        Color(0xFF7C3AED)
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            SectionTitle(text = "Accesos rápidos")

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickAccessCard(
                    titulo = "Vocab.",
                    icono = Icons.Filled.Book,
                    background = SoftBlue,
                    iconColor = PrimaryBlue,
                    modifier = Modifier.weight(1f),
                    onClick = onVocabularioClick
                )

                QuickAccessCard(
                    titulo = "Lotes",
                    icono = Icons.Filled.Book,
                    background = SoftGreen,
                    iconColor = Color(0xFF10B981),
                    modifier = Modifier.weight(1f),
                    onClick = onLotesClick
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickAccessCard(
                    titulo = "Estudio",
                    icono = Icons.Filled.School,
                    background = SoftPurple,
                    iconColor = Color(0xFF7C3AED),
                    modifier = Modifier.weight(1f),
                    onClick = onEstudioClick
                )

                QuickAccessCard(
                    titulo = "Tutor IA",
                    icono = Icons.Filled.Flag,
                    background = SoftOrange,
                    iconColor = Color(0xFFF97316),
                    modifier = Modifier.weight(1f),
                    onClick = onIaClick
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            SummaryCard(
                palabras = state.palabras.size,
                verbos = state.verbos.size,
                lotes = state.lotes.size
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun TopStatusBarBackground() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(
                WindowInsets.statusBars
                    .asPaddingValues()
                    .calculateTopPadding()
            )
            .background(BackgroundColor)
    )
}

@Composable
private fun HeaderHome(
    nombreUsuario: String,
    rachaActual: Int,
    objetivoCumplido: Boolean
) {
    val primerNombre = nombreUsuario
        .trim()
        .split(" ")
        .firstOrNull()
        ?.takeIf { it.isNotBlank() }
        ?: "Usuario"

    val textoRacha = if (rachaActual == 1) {
        "🔥  1 día de racha"
    } else {
        "🔥  $rachaActual días de racha"
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(235.dp)
            .clip(RoundedCornerShape(bottomStart = 46.dp, bottomEnd = 46.dp))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFF0EA5E9),
                        Color(0xFF2563EB),
                        Color(0xFF7C3AED)
                    )
                )
            )
    ) {
        Box(
            modifier = Modifier
                .size(165.dp)
                .align(Alignment.BottomEnd)
                .background(
                    color = Color.White.copy(alpha = 0.08f),
                    shape = CircleShape
                )
        )

        Box(
            modifier = Modifier
                .size(80.dp)
                .align(Alignment.TopEnd)
                .padding(top = 24.dp, end = 42.dp)
                .background(
                    color = Color.White.copy(alpha = 0.10f),
                    shape = CircleShape
                )
        )

        Column(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(horizontal = 28.dp)
        ) {
            Text(
                text = "¡Hola, $primerNombre! 👋",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (objetivoCumplido) {
                    "¡Objetivo diario cumplido!"
                } else {
                    "Sigue aprendiendo cada día"
                },
                fontSize = 17.sp,
                color = Color.White.copy(alpha = 0.90f)
            )

            Spacer(modifier = Modifier.height(18.dp))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50.dp))
                    .background(Color.White.copy(alpha = 0.16f))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = textoRacha,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun HomeProgressCard(
    titulo: String,
    subtitulo: String,
    progreso: Float,
    detalle: String,
    icono: ImageVector,
    iconBackground: Color,
    iconColor: Color,
    progressColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(170.dp),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(iconBackground),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icono,
                        contentDescription = titulo,
                        tint = iconColor,
                        modifier = Modifier.size(30.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = titulo,
                        color = DarkText,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        maxLines = 2
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = subtitulo,
                        color = MutedText,
                        fontSize = 14.sp,
                        maxLines = 2
                    )
                }
            }

            LinearProgressIndicator(
                progress = { progreso.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(20.dp)),
                color = progressColor,
                trackColor = Color(0xFFE5E7EB)
            )

            Text(
                text = detalle,
                color = MutedText,
                fontSize = 14.sp,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        modifier = Modifier.padding(horizontal = 24.dp),
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.ExtraBold,
        color = DarkText
    )
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
            .height(86.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = background),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(
            width = 1.dp,
            color = iconColor.copy(alpha = 0.10f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(Color.White.copy(alpha = 0.88f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icono,
                    contentDescription = titulo,
                    tint = iconColor,
                    modifier = Modifier.size(27.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Text(
                text = titulo,
                color = DarkText,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                maxLines = 1,
                modifier = Modifier.weight(1f)
            )

            Text(
                text = "›",
                color = iconColor,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun SummaryCard(
    palabras: Int,
    verbos: Int,
    lotes: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Resumen",
                color = DarkText,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(modifier = Modifier.height(18.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SummaryItem(
                    label = "Palabras disponibles",
                    value = palabras.toString(),
                    iconText = "Aa",
                    iconColor = PrimaryBlue,
                    iconBackground = SoftBlue,
                    modifier = Modifier.weight(1f)
                )

                VerticalDivider()

                SummaryItem(
                    label = "Verbos disponibles",
                    value = verbos.toString(),
                    iconText = "∞",
                    iconColor = Color(0xFF7C3AED),
                    iconBackground = SoftPurple,
                    modifier = Modifier.weight(1f)
                )

                VerticalDivider()

                SummaryItem(
                    label = "Lotes disponibles",
                    value = lotes.toString(),
                    iconText = "≋",
                    iconColor = Color(0xFF10B981),
                    iconBackground = SoftGreen,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun SummaryItem(
    label: String,
    value: String,
    iconText: String,
    iconColor: Color,
    iconBackground: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(iconBackground, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = iconText,
                color = iconColor,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 20.sp
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = label,
            color = MutedText,
            fontSize = 11.sp,
            textAlign = TextAlign.Center,
            lineHeight = 14.sp,
            maxLines = 2
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = value,
            color = DarkText,
            fontSize = 23.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

@Composable
private fun VerticalDivider() {
    Box(
        modifier = Modifier
            .height(82.dp)
            .width(1.dp)
            .background(Color(0xFFE5E7EB))
    )
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
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 10.dp, vertical = 8.dp),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(76.dp)
                .padding(horizontal = 4.dp),
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
                titulo = "Vocab.",
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
}

@Composable
private fun BottomNavItem(
    titulo: String,
    icono: ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    val color = if (selected) PrimaryBlue else Color(0xFF6B7280)

    Column(
        modifier = Modifier
            .width(58.dp)
            .fillMaxHeight()
            .clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icono,
            contentDescription = titulo,
            tint = color,
            modifier = Modifier.size(25.dp)
        )

        Spacer(modifier = Modifier.height(5.dp))

        Text(
            text = titulo,
            color = color,
            fontSize = 11.sp,
            textAlign = TextAlign.Center,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            maxLines = 1
        )

        Spacer(modifier = Modifier.height(5.dp))

        Box(
            modifier = Modifier
                .height(3.dp)
                .width(if (selected) 30.dp else 0.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(if (selected) PrimaryBlue else Color.Transparent)
        )
    }
}