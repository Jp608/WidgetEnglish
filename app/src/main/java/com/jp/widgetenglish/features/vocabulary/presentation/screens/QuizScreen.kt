package com.jp.widgetenglish.features.vocabulary.presentation.screens

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jp.widgetenglish.features.common.TtsHelper
import com.jp.widgetenglish.features.vocabulary.presentation.viewmodel.QuizViewModel

private val QuizBlue = Color(0xFF2563EB)
private val QuizBg = Color(0xFFF8FAFC)
private val TextDark = Color(0xFF111827)
private val TextMuted = Color(0xFF6B7280)
private val BorderLight = Color(0xFFE5E7EB)
private val CorrectGreen = Color(0xFF10B981)
private val WrongRed = Color(0xFFEF4444)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(
    loteId: String,
    repasarFalladas: Boolean,
    failedIds: List<String> = emptyList(),
    limite: Int = 10,
    viewModel: QuizViewModel,
    onBack: () -> Unit,
    onFinish: (Int, Int, List<String>) -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val ttsHelper = remember { TtsHelper(context) }

    var finishHandled by remember(loteId, repasarFalladas, limite) {
        mutableStateOf(false)
    }

    var puedeFinalizarQuiz by remember(loteId, repasarFalladas, limite) {
        mutableStateOf(false)
    }

    DisposableEffect(Unit) {
        onDispose {
            ttsHelper.shutdown()
        }
    }

    LaunchedEffect(loteId, repasarFalladas, limite, failedIds) {
        finishHandled = false
        puedeFinalizarQuiz = false

        android.util.Log.d(
            "QuizDebug",
            "QuizScreen inicia. repasarFalladas=$repasarFalladas, limite=$limite, failedIds=${failedIds.size}, ids=$failedIds"
        )

        viewModel.iniciarQuiz(
            loteId = loteId,
            repasarFalladas = repasarFalladas,
            failedIds = failedIds,
            limite = limite
        )
    }

    LaunchedEffect(
        state.cargando,
        state.estaFinalizado,
        state.preguntas,
        state.loteId
    ) {
        if (
            !state.cargando &&
            !state.estaFinalizado &&
            state.preguntas.isNotEmpty() &&
            state.loteId == loteId
        ) {
            puedeFinalizarQuiz = true
        }
    }

    LaunchedEffect(
        state.indicePreguntaActual,
        state.preguntas,
        state.estaFinalizado
    ) {
        if (
            state.preguntas.isNotEmpty() &&
            !state.estaFinalizado &&
            state.indicePreguntaActual in state.preguntas.indices
        ) {
            val palabraActual = state.preguntas[state.indicePreguntaActual].palabra
            ttsHelper.speak(palabraActual.termino)
        }
    }

    LaunchedEffect(
        state.estaFinalizado,
        puedeFinalizarQuiz
    ) {
        if (
            puedeFinalizarQuiz &&
            state.estaFinalizado &&
            !finishHandled
        ) {
            finishHandled = true
            puedeFinalizarQuiz = false

            onFinish(
                state.score,
                state.preguntas.size,
                state.respuestasFalladas.map { it.id }
            )
        }
    }

    val puedeMostrarPregunta =
        state.preguntas.isNotEmpty() &&
                state.indicePreguntaActual in state.preguntas.indices

    Scaffold(
        containerColor = QuizBg,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                ),
                title = {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Quiz",
                            fontWeight = FontWeight.ExtraBold,
                            color = TextDark,
                            fontSize = 24.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = TextDark,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { }) {
                        Icon(
                            imageVector = Icons.Default.BarChart,
                            contentDescription = null,
                            tint = TextDark,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            )
        },
        bottomBar = {
            if (!state.cargando && state.mensajeError == null && puedeMostrarPregunta) {
                Surface(
                    color = QuizBg,
                    shadowElevation = 0.dp
                ) {
                    Button(
                        onClick = {
                            viewModel.siguientePregunta()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .windowInsetsPadding(WindowInsets.ime)
                            .padding(horizontal = 24.dp)
                            .padding(top = 8.dp, bottom = 12.dp)
                            .height(58.dp),
                        enabled = state.mostrarFeedback,
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = QuizBlue,
                            disabledContainerColor = BorderLight,
                            disabledContentColor = Color(0xFF9CA3AF)
                        ),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 2.dp,
                            disabledElevation = 0.dp
                        )
                    ) {
                        Text(
                            text = "Continuar",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }
    ) { padding ->
        when {
            state.cargando -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = QuizBlue
                    )
                }
            }

            state.mensajeError != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Surface(
                            color = Color(0xFFFFEBEE),
                            shape = CircleShape,
                            modifier = Modifier.size(80.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = null,
                                tint = Color(0xFFC62828),
                                modifier = Modifier.padding(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = state.mensajeError.orEmpty(),
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold,
                            color = TextDark,
                            fontSize = 18.sp
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = onBack,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = QuizBlue
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Text(
                                text = "Entendido",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            puedeMostrarPregunta -> {
                val pregunta = state.preguntas[state.indicePreguntaActual]
                val progresoActual = ((state.indicePreguntaActual + 1).toFloat() / state.preguntas.size)
                    .coerceIn(0f, 1f)
                val porcentajeActual = (progresoActual * 100f).toInt()

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp)
                        .padding(top = 12.dp, bottom = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Pregunta ${state.indicePreguntaActual + 1} de ${state.preguntas.size}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMuted
                        )

                        Text(
                            text = "$porcentajeActual%",
                            fontSize = 18.sp,
                            color = QuizBlue,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    LinearProgressIndicator(
                        progress = { progresoActual },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 14.dp)
                            .height(10.dp)
                            .clip(RoundedCornerShape(20.dp)),
                        color = QuizBlue,
                        trackColor = BorderLight
                    )

                    Spacer(modifier = Modifier.height(22.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(32.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        ),
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = 0.dp
                        ),
                        border = BorderStroke(
                            width = 1.dp,
                            color = BorderLight
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(
                                horizontal = 28.dp,
                                vertical = 30.dp
                            ),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "¿Qué significa...?",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextMuted
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = pregunta.palabra.termino,
                                fontSize = 38.sp,
                                lineHeight = 42.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextDark,
                                textAlign = TextAlign.Center
                            )

                            pregunta.palabra.fonetica?.let { fonetica ->
                                if (fonetica.isNotBlank()) {
                                    Text(
                                        text = fonetica,
                                        fontSize = 20.sp,
                                        color = TextMuted,
                                        modifier = Modifier.padding(top = 4.dp),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(30.dp))

                            pregunta.opciones.forEachIndexed { index, opcion ->
                                val letra = when (index) {
                                    0 -> "A"
                                    1 -> "B"
                                    2 -> "C"
                                    else -> "D"
                                }

                                QuizOptionItem(
                                    letra = letra,
                                    texto = opcion,
                                    seleccionada = state.opcionSeleccionada == opcion,
                                    esCorrecta = state.mostrarFeedback &&
                                            opcion == pregunta.respuestaCorrecta,
                                    esIncorrecta = state.mostrarFeedback &&
                                            state.opcionSeleccionada == opcion &&
                                            opcion != pregunta.respuestaCorrecta,
                                    onClick = {
                                        if (!state.mostrarFeedback) {
                                            viewModel.seleccionarOpcion(opcion)
                                        }
                                    }
                                )

                                if (index != pregunta.opciones.lastIndex) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                }
                            }

                            if (state.mostrarFeedback) {
                                Spacer(modifier = Modifier.height(18.dp))

                                val esCorrecta =
                                    state.opcionSeleccionada == pregunta.respuestaCorrecta

                                FeedbackBox(
                                    esCorrecta = esCorrecta,
                                    termino = pregunta.palabra.termino,
                                    traduccion = pregunta.palabra.traduccion
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))
                }
            }
        }
    }
}

@Composable
fun QuizOptionItem(
    letra: String,
    texto: String,
    seleccionada: Boolean,
    esCorrecta: Boolean,
    esIncorrecta: Boolean,
    onClick: () -> Unit
) {
    val borderColor = when {
        esCorrecta -> CorrectGreen
        esIncorrecta -> WrongRed
        seleccionada -> QuizBlue
        else -> BorderLight
    }

    val backgroundColor = when {
        esCorrecta -> Color(0xFFEAFBF2)
        esIncorrecta -> Color(0xFFFEF2F2)
        seleccionada -> Color(0xFFEFF6FF)
        else -> Color.White
    }

    val iconContainerColor = when {
        esCorrecta -> CorrectGreen
        esIncorrecta -> WrongRed
        seleccionada -> QuizBlue
        else -> Color(0xFFF3F4F6)
    }

    val iconTextColor = if (esCorrecta || esIncorrecta || seleccionada) {
        Color.White
    } else {
        TextMuted
    }

    val textColor = when {
        esCorrecta -> Color(0xFF065F46)
        esIncorrecta -> Color(0xFF991B1B)
        else -> TextDark
    }

    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            width = if (seleccionada || esCorrecta || esIncorrecta) 2.dp else 1.dp,
            color = borderColor
        ),
        color = backgroundColor,
        tonalElevation = if (seleccionada) 2.dp else 0.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconContainerColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = letra,
                    color = iconTextColor,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = texto,
                modifier = Modifier.weight(1f),
                fontSize = 17.sp,
                lineHeight = 21.sp,
                fontWeight = if (seleccionada || esCorrecta || esIncorrecta) {
                    FontWeight.Bold
                } else {
                    FontWeight.Medium
                },
                color = textColor
            )

            if (esCorrecta) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = CorrectGreen,
                    modifier = Modifier.size(24.dp)
                )
            } else if (esIncorrecta) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = null,
                    tint = WrongRed,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun FeedbackBox(
    esCorrecta: Boolean,
    termino: String,
    traduccion: String
) {
    val backgroundColor = if (esCorrecta) {
        Color(0xFFEFF6FF)
    } else {
        Color(0xFFFEF2F2)
    }

    val color = if (esCorrecta) {
        QuizBlue
    } else {
        WrongRed
    }

    val borderColor = color.copy(alpha = 0.22f)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = backgroundColor,
        border = BorderStroke(
            width = 1.dp,
            color = borderColor
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(38.dp),
                shape = CircleShape,
                color = color
            ) {
                Icon(
                    imageVector = if (esCorrecta) {
                        Icons.Default.Check
                    } else {
                        Icons.Default.Close
                    },
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.padding(8.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = if (esCorrecta) {
                        "¡Correcto!"
                    } else {
                        "¡Sigue practicando!"
                    },
                    fontWeight = FontWeight.ExtraBold,
                    color = color,
                    fontSize = 17.sp
                )

                Text(
                    text = "\"$termino\" es \"$traduccion\".",
                    fontSize = 15.sp,
                    lineHeight = 19.sp,
                    color = color.copy(alpha = 0.72f),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}