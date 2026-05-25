package com.jp.widgetenglish.features.vocabulary.presentation.cards.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.jp.widgetenglish.features.vocabulary.presentation.cards.model.CardsStudyFilter
import com.jp.widgetenglish.features.vocabulary.presentation.cards.viewmodel.CardsViewModel

private val StrongBlue = Color(0xFF2563EB)
private val Purple = Color(0xFF7C3AED)
private val ScreenBg = Color(0xFFF8FAFC)
private val TextDark = Color(0xFF111827)
private val TextMuted = Color(0xFF6B7280)
private val SoftBlue = Color(0xFFEFF6FF)
private val BorderSoft = Color(0xFFE5E7EB)

@Composable
fun CardsPlanScreen(
    viewModel: CardsViewModel,
    onBack: () -> Unit,
    onStartSession: () -> Unit,
    onOpenDetailedConfig: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    var startRequested by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.cargarDatos()
    }

    LaunchedEffect(
        startRequested,
        state.sesionIniciada
    ) {
        if (startRequested && state.sesionIniciada) {
            startRequested = false
            onStartSession()
        }
    }

    Scaffold(
        containerColor = ScreenBg
    ) { innerPadding ->
        when {
            state.cargando -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = innerPadding.calculateBottomPadding()),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = StrongBlue)
                }
            }

            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(ScreenBg)
                        .padding(bottom = innerPadding.calculateBottomPadding())
                        .verticalScroll(rememberScrollState())
                ) {
                    CardsPlanHeader(
                        disponibles = state.totalDisponibles,
                        progreso = state.progresoLote.toInt(),
                        onBack = onBack
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp)
                            .padding(top = 14.dp, bottom = 18.dp)
                    ) {
                        SectionTitle("Tu progreso actual")

                        Spacer(modifier = Modifier.height(10.dp))

                        CurrentProgressCard(
                            loteNombre = state.loteActivo?.nombre ?: "Sin lote activo",
                            progreso = state.progresoLote.toInt(),
                            enabled = state.loteActivo != null
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        SectionTitle("Configurar sesión")

                        Spacer(modifier = Modifier.height(10.dp))

                        QuickConfigCard(
                            cantidadSeleccionada = state.config.cantidad,
                            filtroSeleccionado = state.config.filtro,
                            usarTodas = state.config.usarTodas,
                            onCantidadSelected = { cantidad ->
                                viewModel.seleccionarCantidad(cantidad)
                            },
                            onFiltroSelected = { filtro ->
                                viewModel.seleccionarFiltro(filtro)
                            }
                        )

                        if (state.error != null) {
                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = state.error.orEmpty(),
                                color = Color(0xFFDC2626),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                startRequested = true
                                viewModel.iniciarSesionTarjetas()
                            },
                            enabled = state.loteActivo != null && state.totalDisponibles > 0,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = StrongBlue,
                                contentColor = Color.White,
                                disabledContainerColor = Color(0xFFE5E7EB),
                                disabledContentColor = Color(0xFF94A3B8)
                            ),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 4.dp
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Filled.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(26.dp)
                            )

                            Spacer(modifier = Modifier.width(10.dp))

                            Text(
                                text = "Iniciar tarjetas",
                                fontSize = 19.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedButton(
                            onClick = onOpenDetailedConfig,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(2.dp, StrongBlue),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = StrongBlue
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Settings,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
                            )

                            Spacer(modifier = Modifier.width(9.dp))

                            Text(
                                text = "Configuración detallada",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CardsPlanHeader(
    disponibles: Int,
    progreso: Int,
    onBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(210.dp)
            .clip(
                RoundedCornerShape(
                    bottomStart = 36.dp,
                    bottomEnd = 36.dp
                )
            )
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        StrongBlue,
                        Purple
                    )
                )
            )
            .statusBarsPadding()
            .padding(horizontal = 16.dp),
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier.align(Alignment.TopStart)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Volver",
                tint = Color.White,
                modifier = Modifier.size(30.dp)
            )
        }

        Icon(
            imageVector = Icons.Filled.Book,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 10.dp, end = 8.dp)
                .size(32.dp)
        )

        Text(
            text = "Plan de tarjetas",
            color = Color.White,
            fontSize = 27.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 11.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
                .padding(top = 42.dp, start = 26.dp, end = 26.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HeaderMetric(
                modifier = Modifier.weight(1f),
                icon = Icons.Filled.Book,
                value = disponibles.toString(),
                label = "Disponibles"
            )

            Box(
                modifier = Modifier
                    .height(58.dp)
                    .width(1.dp)
                    .background(Color.White.copy(alpha = 0.35f))
            )

            HeaderMetric(
                modifier = Modifier.weight(1f),
                icon = Icons.Filled.CheckCircle,
                value = "$progreso%",
                label = "Progreso"
            )
        }
    }
}

@Composable
private fun HeaderMetric(
    modifier: Modifier,
    icon: ImageVector,
    value: String,
    label: String
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            modifier = Modifier.size(46.dp),
            shape = RoundedCornerShape(15.dp),
            color = Color.White.copy(alpha = 0.18f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(26.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = value,
            color = Color.White,
            fontSize = 33.sp,
            fontWeight = FontWeight.ExtraBold
        )

        Text(
            text = label,
            color = Color.White.copy(alpha = 0.82f),
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun SectionTitle(
    text: String
) {
    Text(
        text = text,
        color = TextDark,
        fontSize = 20.sp,
        fontWeight = FontWeight.ExtraBold
    )
}

@Composable
private fun CurrentProgressCard(
    loteNombre: String,
    progreso: Int,
    enabled: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 3.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(58.dp),
                shape = RoundedCornerShape(18.dp),
                color = SoftBlue
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Filled.Book,
                        contentDescription = null,
                        tint = StrongBlue,
                        modifier = Modifier.size(30.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = if (enabled) loteNombre else "Selecciona un lote",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextDark,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LinearProgressIndicator(
                        progress = { (progreso / 100f).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .weight(1f)
                            .height(7.dp)
                            .clip(RoundedCornerShape(30.dp)),
                        color = StrongBlue,
                        trackColor = Color(0xFFE5E7EB)
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Text(
                        text = "$progreso%",
                        color = StrongBlue,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "$progreso% completado",
                    fontSize = 14.sp,
                    color = TextMuted,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun QuickConfigCard(
    cantidadSeleccionada: Int,
    filtroSeleccionado: CardsStudyFilter,
    usarTodas: Boolean,
    onCantidadSelected: (Int) -> Unit,
    onFiltroSelected: (CardsStudyFilter) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        ),
        border = BorderStroke(1.dp, BorderSoft)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                StepCircle("1")

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = "¿Cuántas palabras quieres practicar?",
                    color = TextDark,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    lineHeight = 20.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(9.dp)
            ) {
                listOf(5, 10, 15, 20).forEach { cantidad ->
                    QuantityChip(
                        modifier = Modifier.weight(1f),
                        text = cantidad.toString(),
                        selected = !usarTodas && cantidadSeleccionada == cantidad,
                        onClick = {
                            onCantidadSelected(cantidad)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color(0xFFE5E7EB))
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                StepCircle("2")

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = "¿Qué deseas repasar?",
                    color = TextDark,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                FilterChipMini(
                    modifier = Modifier.weight(0.9f),
                    text = "Todas",
                    selected = filtroSeleccionado == CardsStudyFilter.TODAS,
                    onClick = {
                        onFiltroSelected(CardsStudyFilter.TODAS)
                    }
                )

                FilterChipMini(
                    modifier = Modifier.weight(1.3f),
                    text = "En progreso",
                    selected = filtroSeleccionado == CardsStudyFilter.EN_PROGRESO,
                    onClick = {
                        onFiltroSelected(CardsStudyFilter.EN_PROGRESO)
                    }
                )

                FilterChipMini(
                    modifier = Modifier.weight(1.25f),
                    text = "Aprendidas",
                    selected = filtroSeleccionado == CardsStudyFilter.APRENDIDAS,
                    onClick = {
                        onFiltroSelected(CardsStudyFilter.APRENDIDAS)
                    }
                )

                FilterChipMini(
                    modifier = Modifier.weight(1f),
                    text = "Difíciles",
                    selected = filtroSeleccionado == CardsStudyFilter.DIFICILES,
                    onClick = {
                        onFiltroSelected(CardsStudyFilter.DIFICILES)
                    }
                )
            }
        }
    }
}

@Composable
private fun StepCircle(
    text: String
) {
    Surface(
        modifier = Modifier.size(30.dp),
        shape = CircleShape,
        color = StrongBlue
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = text,
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

@Composable
private fun QuantityChip(
    modifier: Modifier,
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .height(50.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = if (selected) StrongBlue else Color(0xFFF3F4F6),
        shadowElevation = if (selected) 3.dp else 0.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = text,
                color = if (selected) Color.White else TextMuted,
                fontSize = 19.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

@Composable
private fun FilterChipMini(
    modifier: Modifier,
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .height(42.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = if (selected) SoftBlue else Color(0xFFF3F4F6),
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) StrongBlue else Color(0xFFE5E7EB)
        )
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = text,
                color = if (selected) StrongBlue else TextMuted,
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )
        }
    }
}