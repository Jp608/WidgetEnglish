package com.jp.widgetenglish.features.vocabulary.presentation.cards.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MoodBad
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.WatchLater
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jp.widgetenglish.features.common.TtsHelper
import com.jp.widgetenglish.features.vocabulary.presentation.cards.viewmodel.CardsAnswerType
import com.jp.widgetenglish.features.vocabulary.presentation.cards.viewmodel.CardsViewModel
import com.jp.widgetenglish.features.vocabulary.presentation.viewmodel.PalabraConProgreso
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Psychology
private val PrimaryBlue = Color(0xFF1565C0)
private val StrongBlue = Color(0xFF0057E7)
private val ScreenBg = Color(0xFFF7F9FD)
private val TextDark = Color(0xFF08145F)
private val TextMuted = Color(0xFF6B7280)

private val SoftBlue = Color(0xFFEAF2FF)
private val SoftGreen = Color(0xFFEAF8EE)
private val SoftRed = Color(0xFFFFEEEE)
private val SoftOrange = Color(0xFFFFF4E0)

private val Green = Color(0xFF2E7D32)
private val Red = Color(0xFFD32F2F)
private val Orange = Color(0xFFE97B00)

private val SelectedGreen = Color(0xFF188038)
private val SelectedRed = Color(0xFFE53935)
private val SelectedOrange = Color(0xFFFF8F00)

@Composable
fun CardsSessionScreen(
    viewModel: CardsViewModel,
    onBack: () -> Unit,
    onSessionFinished: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val ttsHelper = remember { TtsHelper(context) }

    var finishHandled by remember {
        mutableStateOf(false)
    }

    DisposableEffect(Unit) {
        onDispose {
            ttsHelper.shutdown()
        }
    }

    LaunchedEffect(state.sesionFinalizada) {
        if (state.sesionFinalizada && !finishHandled) {
            finishHandled = true
            onSessionFinished()
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
                    CircularProgressIndicator(color = PrimaryBlue)
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
                        text = state.error.orEmpty(),
                        color = Red,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            }

            else -> {
                val tarjeta = state.tarjetaActual

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(ScreenBg)
                        .padding(bottom = innerPadding.calculateBottomPadding())
                ) {
                    CardsSessionHeader(
                        title = "Tarjetas de aprendizaje",
                        onBack = onBack
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp)
                            .padding(top = 12.dp, bottom = 14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        SessionProgressCard(
                            loteNombre = state.loteActivo?.nombre ?: "Sin lote",
                            current = state.numeroTarjetaActual,
                            total = state.totalSesion,
                            progress = state.porcentajeSesion
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        if (tarjeta != null) {
                            FlashCard(
                                tarjeta = tarjeta,
                                mostrarTraduccion = state.config.mostrarTraduccionAlInicio,
                                mostrarPronunciacion = state.config.incluirPronunciacion,
                                mostrarEjemplo = state.config.mostrarEjemploUso,
                                onSpeak = {
                                    ttsHelper.speak(tarjeta.termino)
                                },
                                onPrevious = {
                                    viewModel.irAnterior()
                                },
                                onNext = {
                                    viewModel.irSiguiente()
                                },
                                canGoPrevious = state.puedeIrAnterior,
                                canGoNext = state.puedeIrSiguiente
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "Usa las flechas para navegar entre tarjetas",
                                color = TextMuted,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            AnswerButtonsGrid(
                                selectedAnswer = state.respuestaActual,
                                onKnown = {
                                    viewModel.clasificarTarjetaActual(CardsAnswerType.LA_CONOZCO)
                                },
                                onUnknown = {
                                    viewModel.clasificarTarjetaActual(CardsAnswerType.NO_LA_CONOZCO)
                                },
                                onDifficult = {
                                    viewModel.clasificarTarjetaActual(CardsAnswerType.DIFICIL)
                                },
                                onLearned = {
                                    viewModel.clasificarTarjetaActual(CardsAnswerType.APRENDIDA)
                                }
                            )

                            if (state.esUltimaTarjeta) {
                                Spacer(modifier = Modifier.height(14.dp))

                                FinishStudyCard(
                                    enabled = state.respuestaActual != null,
                                    onFinish = {
                                        viewModel.finalizarSesion()
                                    }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.navigationBarsPadding())
                    }
                }
            }
        }
    }
}

@Composable
private fun CardsSessionHeader(
    title: String,
    onBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(92.dp)
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFF0057E7),
                        Color(0xFF1565C0)
                    )
                )
            )
            .statusBarsPadding()
            .padding(horizontal = 12.dp),
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
                modifier = Modifier.size(30.dp)
            )
        }

        Text(
            text = title,
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 54.dp)
        )

        Icon(
            imageVector = Icons.Filled.Share,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 10.dp)
                .size(28.dp)
        )
    }
}

@Composable
private fun SessionProgressCard(
    loteNombre: String,
    current: Int,
    total: Int,
    progress: Int
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFD4E4FF)),
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(42.dp),
                    shape = RoundedCornerShape(14.dp),
                    color = SoftBlue
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.MenuBook,
                            contentDescription = null,
                            tint = StrongBlue,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Lote activo: ",
                            color = TextMuted,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )

                        Text(
                            text = loteNombre,
                            color = TextDark,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                    }

                    Text(
                        text = "Tarjeta $current de $total",
                        color = TextMuted,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Text(
                    text = "$progress%",
                    color = StrongBlue,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            LinearProgressIndicator(
                progress = { (progress / 100f).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(7.dp)
                    .clip(RoundedCornerShape(30.dp)),
                color = StrongBlue,
                trackColor = Color(0xFFDDE3EB)
            )
        }
    }
}

@Composable
private fun FlashCard(
    tarjeta: PalabraConProgreso,
    mostrarTraduccion: Boolean,
    mostrarPronunciacion: Boolean,
    mostrarEjemplo: Boolean,
    onSpeak: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    canGoPrevious: Boolean,
    canGoNext: Boolean
) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 28.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 6.dp
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp, vertical = 22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = tarjeta.termino,
                    color = TextDark,
                    fontSize = 38.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center,
                    lineHeight = 42.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                if (mostrarPronunciacion && !tarjeta.fonetica.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(5.dp))

                    Text(
                        text = tarjeta.fonetica.orEmpty(),
                        color = TextMuted,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (mostrarPronunciacion) {
                    Spacer(modifier = Modifier.height(18.dp))

                    PronunciationButton(
                        onClick = onSpeak
                    )
                }

                if (mostrarEjemplo && tieneEjemplo(tarjeta)) {
                    Spacer(modifier = Modifier.height(20.dp))

                    ExampleBox(tarjeta = tarjeta)
                }

                Spacer(modifier = Modifier.height(20.dp))

                InfoSection(
                    tarjeta = tarjeta,
                    mostrarTraduccion = mostrarTraduccion
                )
            }
        }

        FloatingArrowButton(
            modifier = Modifier.align(Alignment.CenterStart),
            icon = Icons.Filled.KeyboardArrowLeft,
            enabled = canGoPrevious,
            onClick = onPrevious
        )

        FloatingArrowButton(
            modifier = Modifier.align(Alignment.CenterEnd),
            icon = Icons.Filled.KeyboardArrowRight,
            enabled = canGoNext,
            onClick = onNext
        )
    }
}

@Composable
private fun PronunciationButton(
    onClick: () -> Unit
) {
    FilledTonalButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = Color(0xFFE3F2FD),
            contentColor = StrongBlue
        )
    ) {
        Icon(
            imageVector = Icons.Filled.VolumeUp,
            contentDescription = null,
            modifier = Modifier.size(27.dp)
        )

        Spacer(modifier = Modifier.width(10.dp))

        Text(
            text = "Reproducir pronunciación",
            fontSize = 15.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

@Composable
private fun FloatingArrowButton(
    modifier: Modifier,
    icon: ImageVector,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier.size(46.dp),
        shape = CircleShape,
        color = if (enabled) Color.White else Color(0xFFF3F4F6),
        shadowElevation = if (enabled) 5.dp else 0.dp,
        onClick = {
            if (enabled) onClick()
        }
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (enabled) StrongBlue else Color(0xFFCBD5E1),
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
private fun ExampleBox(
    tarjeta: PalabraConProgreso
) {
    val ejemploIngles = if (tarjeta.esVerbo) {
        tarjeta.ejemploIngles
    } else {
        tarjeta.ejemplo
    }

    val ejemploEspanol = if (tarjeta.esVerbo) {
        tarjeta.ejemploEspanol
    } else {
        tarjeta.ejemploTraduccion
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = Color(0xFFF8FBFF),
        border = BorderStroke(1.dp, Color(0xFFCFE0FF))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Surface(
                modifier = Modifier.size(38.dp),
                shape = RoundedCornerShape(15.dp),
                color = SoftBlue
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "”",
                        color = StrongBlue,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Ejemplo:",
                    color = TextDark,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold
                )

                if (!ejemploIngles.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(9.dp))

                    Text(
                        text = ejemploIngles,
                        color = TextDark,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        lineHeight = 20.sp
                    )
                }

                if (!ejemploEspanol.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = ejemploEspanol,
                        color = TextMuted,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoSection(
    tarjeta: PalabraConProgreso,
    mostrarTraduccion: Boolean
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Información",
                color = TextDark,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(modifier = Modifier.width(10.dp))

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(1.dp)
                    .background(Color(0xFFE5E7EB))
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (mostrarTraduccion) {
            InfoRow(
                icon = Icons.Filled.Translate,
                label = "Traducción",
                value = tarjeta.traduccion
            )
        }

        if (tarjeta.esVerbo) {
            InfoRow(
                icon = Icons.Filled.WatchLater,
                label = "Tipo de verbo",
                value = if (tarjeta.esIrregular == true) "Irregular" else "Regular"
            )

            InfoRow(
                icon = Icons.Filled.Description,
                label = "Pasado",
                value = tarjeta.pasadoSimple ?: "-"
            )

            InfoRow(
                icon = Icons.Filled.Book,
                label = "Participio",
                value = tarjeta.participioPasado ?: "-"
            )
        } else {
            InfoRow(
                icon = Icons.Filled.Book,
                label = "Categoría",
                value = tarjeta.tipoPalabra.name.lowercase().replaceFirstChar {
                    it.uppercase()
                }
            )
        }
    }
}

@Composable
private fun InfoRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(22.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = label,
                    color = TextMuted,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = value,
                    color = TextDark,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    lineHeight = 18.sp
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Color(0xFFE5E7EB))
        )
    }
}

@Composable
private fun AnswerButtonsGrid(
    selectedAnswer: CardsAnswerType?,
    onKnown: () -> Unit,
    onUnknown: () -> Unit,
    onDifficult: () -> Unit,
    onLearned: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            AnswerButton(
                modifier = Modifier.weight(1f),
                title = "La conozco",
                subtitle = "Reforzar",
                icon = Icons.Filled.Check,
                baseColor = Green,
                selectedColor = SelectedGreen,
                softColor = SoftGreen,
                selected = selectedAnswer == CardsAnswerType.LA_CONOZCO,
                onClick = onKnown
            )

            AnswerButton(
                modifier = Modifier.weight(1f),
                title = "No la conozco",
                subtitle = "Repasar más",
                icon = Icons.Filled.HelpOutline,
                baseColor = Red,
                selectedColor = SelectedRed,
                softColor = SoftRed,
                selected = selectedAnswer == CardsAnswerType.NO_LA_CONOZCO,
                onClick = onUnknown
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            AnswerButton(
                modifier = Modifier.weight(1f),
                title = "Difícil",
                subtitle = "Priorizar",
                icon = Icons.Filled.Psychology,
                baseColor = Orange,
                selectedColor = SelectedOrange,
                softColor = SoftOrange,
                selected = selectedAnswer == CardsAnswerType.DIFICIL,
                onClick = onDifficult
            )

            AnswerButton(
                modifier = Modifier.weight(1f),
                title = "Aprendida",
                subtitle = "Domino",
                icon = Icons.Filled.School,
                baseColor = Green,
                selectedColor = SelectedGreen,
                softColor = SoftGreen,
                selected = selectedAnswer == CardsAnswerType.APRENDIDA,
                onClick = onLearned
            )
        }
    }
}

@Composable
private fun AnswerButton(
    modifier: Modifier,
    title: String,
    subtitle: String,
    icon: ImageVector,
    baseColor: Color,
    selectedColor: Color,
    softColor: Color,
    selected: Boolean,
    onClick: () -> Unit
) {
    val containerColor = if (selected) selectedColor else Color.White
    val borderColor = if (selected) selectedColor else baseColor.copy(alpha = 0.35f)
    val titleColor = if (selected) Color.White else baseColor
    val subtitleColor = if (selected) Color.White.copy(alpha = 0.82f) else TextMuted
    val iconCircleColor = if (selected) Color.White else softColor
    val iconColor = if (selected) selectedColor else baseColor

    Surface(
        modifier = modifier.height(84.dp),
        shape = RoundedCornerShape(22.dp),
        color = containerColor,
        border = BorderStroke(
            width = if (selected) 2.dp else 1.3.dp,
            color = borderColor
        ),
        shadowElevation = if (selected) 7.dp else 2.dp,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(38.dp),
                shape = CircleShape,
                color = iconCircleColor
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(23.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title,
                    color = titleColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    lineHeight = 16.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = subtitle,
                    color = subtitleColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun FinishStudyCard(
    enabled: Boolean,
    onFinish: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = if (enabled) SoftBlue else Color(0xFFF3F4F6),
        border = BorderStroke(
            width = 1.5.dp,
            color = if (enabled) StrongBlue.copy(alpha = 0.45f) else Color(0xFFE5E7EB)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier.size(54.dp),
                shape = CircleShape,
                color = if (enabled) StrongBlue else Color(0xFFD1D5DB)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Filled.Flag,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(30.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = if (enabled) {
                    "¡Última tarjeta lista!"
                } else {
                    "Clasifica esta tarjeta para finalizar"
                },
                color = TextDark,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Cuando termines, veremos el resumen de tu sesión.",
                color = TextMuted,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(14.dp))

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(17.dp),
                color = if (enabled) StrongBlue else Color(0xFFD1D5DB),
                shadowElevation = if (enabled) 5.dp else 0.dp,
                onClick = {
                    if (enabled) onFinish()
                }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.DoneAll,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Text(
                        text = "Finalizar estudio",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}

private fun tieneEjemplo(
    tarjeta: PalabraConProgreso
): Boolean {
    return if (tarjeta.esVerbo) {
        !tarjeta.ejemploIngles.isNullOrBlank() ||
                !tarjeta.ejemploEspanol.isNullOrBlank()
    } else {
        !tarjeta.ejemplo.isNullOrBlank() ||
                !tarjeta.ejemploTraduccion.isNullOrBlank()
    }
}
