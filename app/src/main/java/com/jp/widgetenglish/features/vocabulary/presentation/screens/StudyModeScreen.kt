package com.jp.widgetenglish.features.vocabulary.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
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
import com.jp.widgetenglish.features.common.AppBottomBar
import com.jp.widgetenglish.features.vocabulary.presentation.viewmodel.StudyViewModel
import com.widgetenglish.app.ui.Screen
import kotlin.math.max

private val PrimaryBlue = Color(0xFF1565C0)
private val StrongBlue = Color(0xFF0057E7)
private val ScreenBg = Color(0xFFF7F9FD)
private val TextDark = Color(0xFF08145F)
private val TextMuted = Color(0xFF6B7280)
private val SoftBlue = Color(0xFFEAF2FF)
private val SoftGreen = Color(0xFFE1F7EF)
private val Green = Color(0xFF059669)
private val DividerColor = Color(0xFFE5E7EB)

@Composable
fun StudyModeScreen(
    viewModel: StudyViewModel,
    onBack: () -> Unit,
    onOpenQuizConfig: () -> Unit,
    onOpenCards: () -> Unit,
    onInicioClick: () -> Unit,
    onVocabularioClick: () -> Unit,
    onLotesClick: () -> Unit,
    onEstudioClick: () -> Unit,
    onIaClick: () -> Unit,
    onPerfilClick: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.cargarEstadisticas()
    }

    Scaffold(
        containerColor = ScreenBg,
        bottomBar = {
            AppBottomBar(
                selectedRoute = Screen.Estudio.route,
                onInicioClick = onInicioClick,
                onVocabularioClick = onVocabularioClick,
                onLotesClick = onLotesClick,
                onEstudioClick = onEstudioClick,
                onIaClick = onIaClick,
                onPerfilClick = onPerfilClick
            )
        }
    ) { innerPadding ->
        if (state.cargando) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = innerPadding.calculateBottomPadding()),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = PrimaryBlue)
            }
        } else {
            val lote = state.loteActivo
            val totalDisponibles = lote?.cantidadContenido ?: 0
            val pendientesEstimados = max(
                0,
                totalDisponibles - ((state.progresoLote / 100f) * totalDisponibles).toInt()
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = innerPadding.calculateBottomPadding())
                    .background(ScreenBg)
            ) {
                StudyModeHeader(
                    onBack = onBack
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 18.dp)
                        .padding(top = 12.dp, bottom = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    SelectedLotCard(
                        loteNombre = lote?.nombre ?: "Sin lote seleccionado",
                        disponibles = totalDisponibles,
                        progreso = state.progresoLote,
                        enabled = lote != null,
                        onClick = onLotesClick
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "¿Cómo quieres estudiar hoy?",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextDark,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Elige una opción para continuar.",
                        fontSize = 13.sp,
                        color = TextMuted,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    StudyOptionCard(
                        badgeText = "Rápido",
                        badgeColor = SoftBlue,
                        badgeTextColor = PrimaryBlue,
                        title = "Quiz",
                        description = "Preguntas de opción múltiple con feedback inmediato.",
                        buttonText = "Ir a quiz",
                        illustrationType = StudyIllustrationType.QUIZ,
                        enabled = lote != null,
                        onClick = onOpenQuizConfig
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    StudyOptionCard(
                        badgeText = "Guiado",
                        badgeColor = SoftGreen,
                        badgeTextColor = Green,
                        title = "Tarjetas",
                        description = "Repasa pronunciación, significado y progreso.",
                        buttonText = "Abrir tarjetas",
                        illustrationType = StudyIllustrationType.CARDS,
                        enabled = lote != null,
                        onClick = onOpenCards
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    StudyStatsCard(
                        pendientes = pendientesEstimados,
                        quizCompletados = state.quizCompletados,
                        progreso = state.progresoLote.toInt()
                    )

                    if (lote == null) {
                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Selecciona un lote desde la sección Lotes para comenzar.",
                            color = Color(0xFFDC2626),
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.navigationBarsPadding())
                }
            }
        }
    }
}

@Composable
private fun StudyModeHeader(
    onBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(86.dp)
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFF0057E7),
                        Color(0xFF1565C0),
                        Color(0xFF003FC5)
                    )
                )
            )
            .statusBarsPadding()
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
            text = "Modo estudio",
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold
        )

        Icon(
            imageVector = Icons.Filled.School,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .size(29.dp)
        )
    }
}

@Composable
private fun SelectedLotCard(
    loteNombre: String,
    disponibles: Int,
    progreso: Float,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        enabled = enabled,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White,
            disabledContainerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 3.dp
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(52.dp),
                shape = RoundedCornerShape(18.dp),
                color = SoftBlue
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Book,
                        contentDescription = null,
                        tint = PrimaryBlue,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Lote seleccionado",
                    fontSize = 12.sp,
                    color = TextMuted,
                    fontWeight = FontWeight.Medium
                )

                Text(
                    text = loteNombre,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextDark,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(3.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(PrimaryBlue)
                    )

                    Spacer(modifier = Modifier.width(7.dp))

                    Text(
                        text = if (enabled) {
                            "$disponibles palabras disponibles"
                        } else {
                            "Activa un lote para estudiar"
                        },
                        fontSize = 12.sp,
                        color = TextMuted,
                        fontWeight = FontWeight.Medium
                    )
                }

                if (enabled) {
                    Spacer(modifier = Modifier.height(7.dp))

                    LinearProgressIndicator(
                        progress = { (progreso / 100f).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(5.dp)
                            .clip(RoundedCornerShape(20.dp)),
                        color = PrimaryBlue,
                        trackColor = Color(0xFFE5E7EB)
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = TextDark,
                modifier = Modifier.size(26.dp)
            )
        }
    }
}

private enum class StudyIllustrationType {
    QUIZ,
    CARDS
}

@Composable
private fun StudyOptionCard(
    badgeText: String,
    badgeColor: Color,
    badgeTextColor: Color,
    title: String,
    description: String,
    buttonText: String,
    illustrationType: StudyIllustrationType,
    enabled: Boolean,
    onClick: () -> Unit
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 132.dp)
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            StudyIllustration(
                type = illustrationType,
                modifier = Modifier
                    .width(98.dp)
                    .height(112.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = badgeColor
                ) {
                    Text(
                        text = badgeText,
                        color = badgeTextColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = title,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextDark,
                    lineHeight = 25.sp
                )

                Spacer(modifier = Modifier.height(5.dp))

                Text(
                    text = description,
                    fontSize = 12.sp,
                    color = TextMuted,
                    lineHeight = 17.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = onClick,
                    enabled = enabled,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = StrongBlue,
                        disabledContainerColor = Color(0xFFE5E7EB),
                        contentColor = Color.White,
                        disabledContentColor = Color(0xFF9CA3AF)
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp)
                ) {
                    Text(
                        text = buttonText,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Icon(
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun StudyIllustration(
    type: StudyIllustrationType,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(94.dp)
                .clip(CircleShape)
                .background(SoftBlue.copy(alpha = 0.85f))
        )

        when (type) {
            StudyIllustrationType.QUIZ -> QuizIllustration()
            StudyIllustrationType.CARDS -> CardsIllustration()
        }
    }
}

@Composable
private fun QuizIllustration() {
    Box(
        modifier = Modifier.size(108.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .width(66.dp)
                .height(86.dp),
            shape = RoundedCornerShape(14.dp),
            color = Color.White,
            shadowElevation = 6.dp
        ) {
            Column(
                modifier = Modifier.padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                repeat(3) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = PrimaryBlue,
                            modifier = Modifier.size(16.dp)
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        Box(
                            modifier = Modifier
                                .height(5.dp)
                                .width(26.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFC7DBFF))
                        )
                    }
                }
            }
        }

        Surface(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(42.dp),
            shape = CircleShape,
            color = StrongBlue,
            shadowElevation = 6.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Filled.QuestionMark,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(25.dp)
                )
            }
        }

        Surface(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .width(44.dp)
                .height(18.dp),
            shape = RoundedCornerShape(7.dp),
            color = StrongBlue
        ) {}
    }
}

@Composable
private fun CardsIllustration() {
    Box(
        modifier = Modifier.size(108.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .width(58.dp)
                .height(80.dp)
                .align(Alignment.CenterEnd),
            shape = RoundedCornerShape(14.dp),
            color = Color(0xFF0B63F6),
            shadowElevation = 5.dp
        ) {}

        Surface(
            modifier = Modifier
                .width(62.dp)
                .height(88.dp)
                .align(Alignment.Center),
            shape = RoundedCornerShape(14.dp),
            color = Color.White,
            shadowElevation = 6.dp
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = null,
                    tint = StrongBlue,
                    modifier = Modifier.size(30.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Box(
                    modifier = Modifier
                        .height(5.dp)
                        .width(30.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFC7DBFF))
                )

                Spacer(modifier = Modifier.height(6.dp))

                Box(
                    modifier = Modifier
                        .height(5.dp)
                        .width(22.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFD7E5FF))
                )
            }
        }
    }
}

@Composable
private fun StudyStatsCard(
    pendientes: Int,
    quizCompletados: Int,
    progreso: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            StudyStatItem(
                modifier = Modifier.weight(1f),
                icon = Icons.Filled.Book,
                label = "Pendientes",
                value = pendientes.toString()
            )

            VerticalDivider(
                modifier = Modifier.height(38.dp),
                color = DividerColor
            )

            StudyStatItem(
                modifier = Modifier.weight(1f),
                icon = Icons.Filled.EmojiEvents,
                label = "Quiz",
                value = quizCompletados.toString()
            )

            VerticalDivider(
                modifier = Modifier.height(38.dp),
                color = DividerColor
            )

            StudyStatItem(
                modifier = Modifier.weight(1f),
                icon = Icons.Filled.TrackChanges,
                label = "Progreso",
                value = "$progreso%"
            )
        }
    }
}

@Composable
private fun StudyStatItem(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    value: String
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = PrimaryBlue,
            modifier = Modifier.size(18.dp)
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = label,
            color = TextMuted,
            fontSize = 11.sp,
            textAlign = TextAlign.Center,
            lineHeight = 13.sp
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = value,
            color = TextDark,
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}