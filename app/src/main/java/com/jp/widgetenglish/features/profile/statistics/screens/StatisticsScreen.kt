package com.jp.widgetenglish.features.profile.statistics.screens

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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jp.widgetenglish.features.common.AppBottomBar
import com.jp.widgetenglish.features.common.UserHeaderBlue
import com.jp.widgetenglish.features.common.UserHeaderSystemBars
import com.jp.widgetenglish.features.profile.statistics.model.StatisticsLotProgressItem
import com.jp.widgetenglish.features.profile.statistics.model.StatisticsPeriod
import com.jp.widgetenglish.features.profile.statistics.viewmodel.StatisticsViewModel
import com.widgetenglish.app.ui.Screen

private val PrimaryBlue = Color(0xFF1565C0)
private val StrongBlue = Color(0xFF0B63F6)
private val ScreenBg = Color(0xFFF5F7FB)
private val TextDark = Color(0xFF111827)
private val TextMuted = Color(0xFF6B7280)

private val Blue = Color(0xFF2563EB)
private val Purple = Color(0xFF7C3AED)
private val Green = Color(0xFF16A34A)
private val Orange = Color(0xFFF59E0B)
private val Red = Color(0xFFDC2626)
private val Teal = Color(0xFF0D9488)

private val SoftBlue = Color(0xFFEAF2FF)
private val SoftPurple = Color(0xFFF1E9FF)
private val SoftGreen = Color(0xFFEAF8EE)
private val SoftOrange = Color(0xFFFFF4E0)
private val SoftRed = Color(0xFFFFECEF)
private val SoftTeal = Color(0xFFE6FFFB)

@Composable
fun StatisticsScreen(
    viewModel: StatisticsViewModel,
    onBack: () -> Unit,
    onInicioClick: () -> Unit,
    onVocabularioClick: () -> Unit,
    onLotesClick: () -> Unit,
    onEstudioClick: () -> Unit,
    onIaClick: () -> Unit,
    onPerfilClick: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    UserHeaderSystemBars()

    LaunchedEffect(Unit) {
        viewModel.cargarEstadisticas()
    }

    Scaffold(
        containerColor = ScreenBg,
        bottomBar = {
            AppBottomBar(
                selectedRoute = Screen.Profile.route,
                onInicioClick = onInicioClick,
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
                .background(ScreenBg)
                .padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            StatisticsHeader(
                onBack = onBack
            )

            when {
                state.cargando -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = StrongBlue
                        )
                    }
                }

                state.error != null -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = state.error ?: "No se pudieron cargar las estadísticas",
                            color = Red,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                else -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp)
                            .padding(top = 16.dp, bottom = 20.dp)
                    ) {
                        SummaryGrid(
                            palabrasAprendidas = state.palabrasAprendidas,
                            quizzesRealizados = state.quizzesRealizados,
                            precisionGlobal = state.precisionGlobal,
                            rachaActual = state.rachaActual
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        WeeklyProgressCard(
                            period = state.periodoSeleccionado,
                            titlePeriod = state.tituloPeriodo,
                            values = state.progresoSemanal.map { it.valor },
                            labels = state.progresoSemanal.map { it.dia },
                            onSelectWeek = {
                                viewModel.cambiarPeriodo(StatisticsPeriod.WEEK)
                            },
                            onSelectMonth = {
                                viewModel.cambiarPeriodo(StatisticsPeriod.MONTH)
                            },
                            onSelectYear = {
                                viewModel.cambiarPeriodo(StatisticsPeriod.YEAR)
                            },
                            onPrevious = {
                                viewModel.irPeriodoAnterior()
                            },
                            onNext = {
                                viewModel.irPeriodoSiguiente()
                            },
                            onCurrent = {
                                viewModel.volverPeriodoActual()
                            }
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        LearningStatusCard(
                            aprendidas = state.aprendidasPorcentaje,
                            enProgreso = state.enProgresoPorcentaje,
                            dificiles = state.dificilesPorcentaje,
                            noConocidas = state.noVistasPorcentaje
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        LotsProgressCard(
                            porcentajeProgreso = state.porcentajeProgreso,
                            lotes = state.progresoLotes
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        CompletedLotsCard(
                            lotesCompletados = state.lotesCompletados,
                            lotes = state.progresoLotes
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        AchievementsCard(
                            rachaActual = state.rachaActual,
                            rachaMaxima = state.rachaMaxima,
                            palabrasAprendidas = state.palabrasAprendidas
                        )

                        Spacer(modifier = Modifier.navigationBarsPadding())
                    }
                }
            }
        }
    }
}

@Composable
private fun StatisticsHeader(
    onBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(UserHeaderBlue)
            .statusBarsPadding()
            .height(56.dp)
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier.align(Alignment.CenterStart)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Volver",
                tint = Color.White,
                modifier = Modifier.size(27.dp)
            )
        }

        Text(
            text = "Estadísticas",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold
        )

        Surface(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .size(42.dp),
            shape = RoundedCornerShape(14.dp),
            color = Color.White.copy(alpha = 0.16f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Filled.BarChart,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(27.dp)
                )
            }
        }
    }
}

@Composable
private fun SummaryGrid(
    palabrasAprendidas: Int,
    quizzesRealizados: Int,
    precisionGlobal: Int,
    rachaActual: Int
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SummaryStatCard(
                modifier = Modifier.weight(1f),
                title = "Palabras aprendidas",
                value = palabrasAprendidas.toString(),
                icon = Icons.Filled.Book,
                iconColor = Blue,
                iconBackground = SoftBlue
            )

            SummaryStatCard(
                modifier = Modifier.weight(1f),
                title = "Quiz completados",
                value = quizzesRealizados.toString(),
                icon = Icons.Filled.Checklist,
                iconColor = Purple,
                iconBackground = SoftPurple
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SummaryStatCard(
                modifier = Modifier.weight(1f),
                title = "Precisión",
                value = "$precisionGlobal%",
                icon = Icons.Filled.TrackChanges,
                iconColor = Teal,
                iconBackground = SoftTeal
            )

            SummaryStatCard(
                modifier = Modifier.weight(1f),
                title = "Racha",
                value = "$rachaActual día${if (rachaActual == 1) "" else "s"}",
                icon = Icons.Filled.LocalFireDepartment,
                iconColor = Orange,
                iconBackground = SoftOrange
            )
        }
    }
}

@Composable
private fun SummaryStatCard(
    modifier: Modifier,
    title: String,
    value: String,
    icon: ImageVector,
    iconColor: Color,
    iconBackground: Color
) {
    Card(
        modifier = modifier.height(108.dp),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 3.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 13.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(52.dp),
                shape = RoundedCornerShape(17.dp),
                color = iconBackground
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(29.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(11.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title,
                    color = TextMuted,
                    fontSize = 12.sp,
                    lineHeight = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = value,
                    color = iconColor,
                    fontSize = 23.sp,
                    lineHeight = 25.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun WeeklyProgressCard(
    period: StatisticsPeriod,
    titlePeriod: String,
    values: List<Int>,
    labels: List<String>,
    onSelectWeek: () -> Unit,
    onSelectMonth: () -> Unit,
    onSelectYear: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onCurrent: () -> Unit
) {
    val safeValues = if (values.isNotEmpty()) {
        values
    } else {
        listOf(0, 0, 0, 0, 0, 0, 0)
    }

    val safeLabels = if (labels.isNotEmpty()) {
        labels
    } else {
        listOf("Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom")
    }

    val maxValue = (safeValues.maxOrNull() ?: 0).coerceAtLeast(1)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 3.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Text(
                text = "Elementos practicados",
                color = TextDark,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PeriodChip(
                    text = "Semana",
                    selected = period == StatisticsPeriod.WEEK,
                    onClick = onSelectWeek
                )

                PeriodChip(
                    text = "Mes",
                    selected = period == StatisticsPeriod.MONTH,
                    onClick = onSelectMonth
                )

                PeriodChip(
                    text = "Año",
                    selected = period == StatisticsPeriod.YEAR,
                    onClick = onSelectYear
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SmallNavButton(
                    icon = Icons.Filled.ChevronLeft,
                    onClick = onPrevious
                )

                Text(
                    text = titlePeriod,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    color = TextDark,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 16.sp
                )

                SmallNavButton(
                    icon = Icons.Filled.ChevronRight,
                    onClick = onNext
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Ver actual",
                color = StrongBlue,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable {
                    onCurrent()
                }
            )

            Spacer(modifier = Modifier.height(18.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(190.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                safeValues.forEachIndexed { index, value ->
                    WeeklyBar(
                        modifier = Modifier.weight(1f),
                        label = safeLabels.getOrNull(index) ?: "",
                        value = value,
                        maxValue = maxValue,
                        compactLabel = period == StatisticsPeriod.YEAR
                    )
                }
            }
        }
    }
}

@Composable
private fun PeriodChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.clickable {
            onClick()
        },
        shape = RoundedCornerShape(50),
        color = if (selected) {
            StrongBlue
        } else {
            SoftBlue
        }
    ) {
        Text(
            text = text,
            color = if (selected) {
                Color.White
            } else {
                StrongBlue
            },
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
        )
    }
}

@Composable
private fun SmallNavButton(
    icon: ImageVector,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .size(34.dp)
            .clickable {
                onClick()
            },
        shape = CircleShape,
        color = SoftBlue
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = StrongBlue,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
private fun WeeklyBar(
    modifier: Modifier,
    label: String,
    value: Int,
    maxValue: Int,
    compactLabel: Boolean
) {
    val normalizedFraction = if (maxValue > 0) {
        value.toFloat() / maxValue.toFloat()
    } else {
        0f
    }

    val barHeight = (normalizedFraction * 115f).dp.coerceAtLeast(8.dp)

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        Text(
            text = value.toString(),
            color = StrongBlue,
            fontSize = if (compactLabel) 10.sp else 11.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )

        Spacer(modifier = Modifier.height(6.dp))

        Box(
            modifier = Modifier
                .width(if (compactLabel) 16.dp else 22.dp)
                .height(barHeight)
                .clip(
                    RoundedCornerShape(
                        topStart = 8.dp,
                        topEnd = 8.dp
                    )
                )
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF3D8BFF),
                            StrongBlue
                        )
                    )
                )
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = label,
            color = TextMuted,
            fontSize = if (compactLabel) 9.sp else 11.sp,
            textAlign = TextAlign.Center,
            maxLines = 2,
            lineHeight = if (compactLabel) 10.sp else 12.sp
        )
    }
}

@Composable
private fun LearningStatusCard(
    aprendidas: Int,
    enProgreso: Int,
    dificiles: Int,
    noConocidas: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 3.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Text(
                text = "Estado de aprendizaje",
                color = TextDark,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(modifier = Modifier.height(18.dp))

            LearningStatusRow(
                label = "Aprendidas",
                value = aprendidas,
                color = Green
            )

            LearningStatusRow(
                label = "En progreso",
                value = enProgreso,
                color = StrongBlue
            )

            LearningStatusRow(
                label = "Difíciles",
                value = dificiles,
                color = Orange
            )

            LearningStatusRow(
                label = "No vistas",
                value = noConocidas,
                color = Red
            )
        }
    }
}

@Composable
private fun LearningStatusRow(
    label: String,
    value: Int,
    color: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = TextDark,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(110.dp)
        )

        LinearProgressIndicator(
            progress = {
                (value / 100f).coerceIn(0f, 1f)
            },
            modifier = Modifier
                .weight(1f)
                .height(9.dp)
                .clip(RoundedCornerShape(20.dp)),
            color = color,
            trackColor = Color(0xFFE5E7EB)
        )

        Spacer(modifier = Modifier.width(10.dp))

        Text(
            text = "$value%",
            color = TextDark,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.End,
            modifier = Modifier.width(46.dp)
        )
    }
}

@Composable
private fun LotsProgressCard(
    porcentajeProgreso: Int,
    lotes: List<StatisticsLotProgressItem>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 3.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Text(
                text = "Progreso por lotes",
                color = TextDark,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(modifier = Modifier.height(14.dp))

            val lotesMostrar = lotes
                .sortedByDescending { it.porcentaje }
                .take(5)

            if (lotesMostrar.isEmpty()) {
                LotProgressRow(
                    name = "Progreso general",
                    value = porcentajeProgreso
                )
            } else {
                lotesMostrar.forEachIndexed { index, lote ->
                    LotProgressRow(
                        name = lote.nombre,
                        value = lote.porcentaje
                    )

                    if (index != lotesMostrar.lastIndex) {
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun LotProgressRow(
    name: String,
    value: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(38.dp),
            shape = RoundedCornerShape(12.dp),
            color = SoftBlue
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Filled.School,
                    contentDescription = null,
                    tint = StrongBlue,
                    modifier = Modifier.size(21.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = name,
            color = TextDark,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(130.dp),
            maxLines = 1
        )

        LinearProgressIndicator(
            progress = {
                (value / 100f).coerceIn(0f, 1f)
            },
            modifier = Modifier
                .weight(1f)
                .height(8.dp)
                .clip(RoundedCornerShape(20.dp)),
            color = StrongBlue,
            trackColor = Color(0xFFE5E7EB)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = "$value%",
            color = TextDark,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun CompletedLotsCard(
    lotesCompletados: Int,
    lotes: List<StatisticsLotProgressItem>
) {
    var expanded by rememberSaveable {
        mutableStateOf(false)
    }

    val lotesTerminados = lotes
        .filter(::isCompletedLot)
        .sortedBy { it.nombre }

    val totalCompletados = maxOf(
        lotesCompletados,
        lotesTerminados.size
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 3.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .clickable {
                        expanded = !expanded
                    }
                    .padding(vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(46.dp),
                    shape = RoundedCornerShape(15.dp),
                    color = SoftGreen
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.EmojiEvents,
                            contentDescription = null,
                            tint = Green,
                            modifier = Modifier.size(25.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Lotes completados",
                        color = TextDark,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(3.dp))

                    Text(
                        text = if (expanded) {
                            "Toca para ocultar el detalle"
                        } else {
                            "Toca para ver el detalle"
                        },
                        color = TextMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = SoftGreen
                ) {
                    Text(
                        text = totalCompletados.toString(),
                        color = Green,
                        fontSize = 23.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Surface(
                    modifier = Modifier.size(34.dp),
                    shape = CircleShape,
                    color = Color(0xFFF3F4F6)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = if (expanded) "-" else "+",
                            color = Green,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            if (expanded) {
                Spacer(modifier = Modifier.height(14.dp))

                when {
                    totalCompletados == 0 -> {
                        EmptyCompletedLotsMessage(
                            text = "Aun no hay lotes completados. Completa todas las palabras de un lote para verlo aqui."
                        )
                    }

                    lotesTerminados.isEmpty() -> {
                        EmptyCompletedLotsMessage(
                            text = "Hay lotes completados, pero el detalle aun no esta disponible en este dispositivo."
                        )
                    }

                    else -> {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            lotesTerminados.forEach { lote ->
                                CompletedLotRow(lote = lote)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyCompletedLotsMessage(
    text: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = Color(0xFFF8FAFC)
    ) {
        Text(
            text = text,
            color = TextMuted,
            fontSize = 13.sp,
            lineHeight = 17.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 18.dp)
        )
    }
}

@Composable
private fun CompletedLotRow(
    lote: StatisticsLotProgressItem
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = SoftGreen.copy(alpha = 0.55f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = RoundedCornerShape(13.dp),
                color = Color.White
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Filled.School,
                        contentDescription = null,
                        tint = Green,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(11.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = lote.nombre,
                    color = TextDark,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = "${lote.aprendidas} / ${lote.total} aprendidas",
                    color = TextMuted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Text(
                text = "100%",
                color = Green,
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

private fun isCompletedLot(
    lote: StatisticsLotProgressItem
): Boolean {
    return lote.porcentaje >= 100 ||
            (
                    lote.total > 0 &&
                            lote.aprendidas >= lote.total
                    )
}

@Composable
private fun AchievementsCard(
    rachaActual: Int,
    rachaMaxima: Int,
    palabrasAprendidas: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 3.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Text(
                text = "Logros",
                color = TextDark,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AchievementItem(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Filled.LocalFireDepartment,
                    title = "$rachaActual días",
                    subtitle = "Racha actual",
                    iconColor = Orange,
                    backgroundColor = SoftOrange
                )

                AchievementItem(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Filled.EmojiEvents,
                    title = "$rachaMaxima días",
                    subtitle = "Mejor racha",
                    iconColor = Purple,
                    backgroundColor = SoftPurple
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            AchievementItem(
                modifier = Modifier.fillMaxWidth(),
                icon = Icons.Filled.Book,
                title = "$palabrasAprendidas palabras aprendidas",
                subtitle = "Vocabulario dominado",
                iconColor = Green,
                backgroundColor = SoftGreen
            )
        }
    }
}

@Composable
private fun AchievementItem(
    modifier: Modifier,
    icon: ImageVector,
    title: String,
    subtitle: String,
    iconColor: Color,
    backgroundColor: Color
) {
    Surface(
        modifier = modifier.height(72.dp),
        shape = RoundedCornerShape(18.dp),
        color = backgroundColor
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(42.dp),
                shape = CircleShape,
                color = iconColor
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column {
                Text(
                    text = title,
                    color = TextDark,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1
                )

                Text(
                    text = subtitle,
                    color = TextMuted,
                    fontSize = 11.sp,
                    maxLines = 1
                )
            }
        }
    }
}
