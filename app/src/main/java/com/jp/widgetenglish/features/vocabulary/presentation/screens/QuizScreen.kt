package com.jp.widgetenglish.features.vocabulary.presentation.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(
    loteId: String,
    repasarFalladas: Boolean,
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

    LaunchedEffect(loteId, repasarFalladas, limite) {
        finishHandled = false
        puedeFinalizarQuiz = false

        viewModel.iniciarQuiz(
            loteId = loteId,
            repasarFalladas = repasarFalladas,
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

    Scaffold(
        containerColor = Color(0xFFF8FAFC),
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
                            color = Color(0xFF111827)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color(0xFF111827)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { }) {
                        Icon(
                            imageVector = Icons.Default.BarChart,
                            contentDescription = null,
                            tint = Color(0xFF111827)
                        )
                    }
                }
            )
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
                        color = Color(0xFF2563EB)
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
                            color = Color(0xFF111827),
                            fontSize = 18.sp
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = onBack,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF2563EB)
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

            state.preguntas.isNotEmpty() &&
                    state.indicePreguntaActual in state.preguntas.indices -> {
                val pregunta = state.preguntas[state.indicePreguntaActual]

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Pregunta ${state.indicePreguntaActual + 1} de ${state.preguntas.size}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF6B7280)
                        )

                        Text(
                            text = "${(((state.indicePreguntaActual + 1).toFloat() / state.preguntas.size) * 100).toInt()}%",
                            fontSize = 14.sp,
                            color = Color(0xFF2563EB),
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    LinearProgressIndicator(
                        progress = {
                            ((state.indicePreguntaActual + 1).toFloat() / state.preguntas.size)
                                .coerceIn(0f, 1f)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                            .height(10.dp)
                            .clip(RoundedCornerShape(20.dp)),
                        color = Color(0xFF2563EB),
                        trackColor = Color(0xFFE5E7EB)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

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
                            color = Color(0xFFE5E7EB)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "¿Qué significa...?",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF6B7280)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = pregunta.palabra.termino,
                                fontSize = 36.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF111827),
                                textAlign = TextAlign.Center
                            )

                            pregunta.palabra.fonetica?.let { fonetica ->
                                Text(
                                    text = fonetica,
                                    fontSize = 18.sp,
                                    color = Color(0xFF6B7280),
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(32.dp))

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
                                        if (!state.mostrarFeedback && !state.estaFinalizado) {
                                            viewModel.seleccionarOpcion(opcion)
                                        }
                                    }
                                )

                                Spacer(modifier = Modifier.height(12.dp))
                            }

                            if (state.mostrarFeedback) {
                                Spacer(modifier = Modifier.height(16.dp))

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

                    Spacer(modifier = Modifier.weight(1f))

                    Button(
                        onClick = {
                            viewModel.siguientePregunta()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                            .padding(bottom = 8.dp),
                        enabled = state.mostrarFeedback && !state.estaFinalizado,
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2563EB),
                            disabledContainerColor = Color(0xFFE5E7EB)
                        ),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 2.dp
                        )
                    ) {
                        Text(
                            text = "Continuar",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
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
        esCorrecta -> Color(0xFF10B981)
        esIncorrecta -> Color(0xFFEF4444)
        seleccionada -> Color(0xFF2563EB)
        else -> Color(0xFFE5E7EB)
    }

    val backgroundColor = when {
        esCorrecta -> Color(0xFFEAFBF2)
        esIncorrecta -> Color(0xFFFEF2F2)
        seleccionada -> Color(0xFFEFF6FF)
        else -> Color.White
    }

    val iconContainerColor = when {
        esCorrecta -> Color(0xFF10B981)
        esIncorrecta -> Color(0xFFEF4444)
        seleccionada -> Color(0xFF2563EB)
        else -> Color(0xFFF3F4F6)
    }

    val iconTextColor = if (
        esCorrecta ||
        esIncorrecta ||
        seleccionada
    ) {
        Color.White
    } else {
        Color(0xFF6B7280)
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
                fontWeight = if (seleccionada) {
                    FontWeight.Bold
                } else {
                    FontWeight.Medium
                },
                color = when {
                    esCorrecta -> Color(0xFF065F46)
                    esIncorrecta -> Color(0xFF991B1B)
                    else -> Color(0xFF111827)
                }
            )

            if (esCorrecta) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color(0xFF10B981),
                    modifier = Modifier.size(24.dp)
                )
            } else if (esIncorrecta) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = null,
                    tint = Color(0xFFEF4444),
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
        Color(0xFF2563EB)
    } else {
        Color(0xFFEF4444)
    }

    val borderColor = if (esCorrecta) {
        Color(0xFF2563EB).copy(alpha = 0.2f)
    } else {
        Color(0xFFEF4444).copy(alpha = 0.2f)
    }

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
                modifier = Modifier.size(32.dp),
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
                    modifier = Modifier.padding(6.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = if (esCorrecta) {
                        "¡Correcto!"
                    } else {
                        "¡Sigue practicando!"
                    },
                    fontWeight = FontWeight.ExtraBold,
                    color = color,
                    fontSize = 16.sp
                )

                Text(
                    text = "\"$termino\" es \"$traduccion\".",
                    fontSize = 14.sp,
                    color = color.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}