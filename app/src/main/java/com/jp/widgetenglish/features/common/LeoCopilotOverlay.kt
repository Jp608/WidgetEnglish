package com.jp.widgetenglish.features.common

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.jp.widgetenglish.data.remote.ai.GroqAiClient
import com.jp.widgetenglish.features.ai.presentation.screens.VoiceAssistantOverlay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun LeoCopilotOverlay(
    pantallaActual: String,
    contextoAdicional: String? = null,
    aiClient: GroqAiClient
) {
    var showDialog by remember { mutableStateOf(false) }
    var showVoiceOverlay by remember { mutableStateOf(false) }
    var tip by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // Configuración para el arrastre
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    
    val screenWidthPx = with(density) { configuration.screenWidthDp.dp.toPx() }
    val screenHeightPx = with(density) { configuration.screenHeightDp.dp.toPx() }
    val buttonSizePx = with(density) { 60.dp.toPx() }

    // Posición inicial: abajo a la derecha
    var offsetX by remember { mutableStateOf(screenWidthPx - buttonSizePx - with(density) { 20.dp.toPx() }) }
    var offsetY by remember { mutableStateOf(screenHeightPx - buttonSizePx - with(density) { 100.dp.toPx() }) }

    Box(modifier = Modifier.fillMaxSize()) {
        // Botón Flotante Draggable (Arrastrable)
        Surface(
            modifier = Modifier
                .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
                .size(60.dp)
                .shadow(12.dp, CircleShape)
                .clip(CircleShape)
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        offsetX = (offsetX + dragAmount.x).coerceIn(0f, screenWidthPx - buttonSizePx)
                        offsetY = (offsetY + dragAmount.y).coerceIn(0f, screenHeightPx - buttonSizePx)
                    }
                }
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = {
                            showDialog = true
                            if (tip == null) {
                                loading = true
                                scope.launch {
                                    tip = aiClient.obtenerAsistenciaContextual(pantallaActual, contextoAdicional)
                                    loading = false
                                }
                            }
                        },
                        onLongPress = {
                            showVoiceOverlay = true
                        }
                    )
                },
            color = Color.Transparent
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFF1A237E), Color(0xFF039BE5))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "Leo Copilot",
                    tint = Color.White,
                    modifier = Modifier.size(30.dp)
                )
            }
        }

        // Diálogo de Asistencia Contextual
        if (showDialog) {
            Dialog(onDismissRequest = { showDialog = false }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Leo dice... ✨",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1565C0)
                            )
                            IconButton(onClick = { showDialog = false }) {
                                Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.Gray)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        if (loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(40.dp),
                                color = Color(0xFF039BE5),
                                strokeWidth = 3.dp
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Pensando en algo útil...", color = Color.Gray, fontSize = 14.sp)
                        } else {
                            Text(
                                text = tip ?: "¡Hola! Estoy aquí para ayudarte.",
                                textAlign = TextAlign.Center,
                                fontSize = 16.sp,
                                color = Color.DarkGray,
                                lineHeight = 24.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = {
                                tip = null
                                loading = true
                                scope.launch {
                                    tip = aiClient.obtenerAsistenciaContextual(pantallaActual, contextoAdicional)
                                    loading = false
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE3F2FD), contentColor = Color(0xFF1565C0)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("¿Otro consejo?", fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedButton(
                            onClick = {
                                showDialog = false
                                showVoiceOverlay = true
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Mic, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Hablar con Leo", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        if (showVoiceOverlay) {
            VoiceAssistantOverlay(
                onDismiss = { showVoiceOverlay = false },
                aiClient = aiClient
            )
        }
    }
}
