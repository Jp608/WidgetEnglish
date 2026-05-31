package com.jp.widgetenglish.features.common

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.jp.widgetenglish.data.remote.ai.GroqAiClient
import com.jp.widgetenglish.features.ai.presentation.screens.VoiceAssistantOverlay
import kotlinx.coroutines.launch

@Composable
fun JimmyCopilotOverlay(
    pantallaActual: String,
    contextoAdicional: String? = null,
    aiClient: GroqAiClient
) {
    var expanded by remember { mutableStateOf(false) }
    var showVoiceOverlay by remember { mutableStateOf(false) }
    var tip by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.BottomEnd
    ) {
        // Botón Flotante Discreto con Gestos
        Surface(
            modifier = Modifier
                .size(56.dp)
                .shadow(8.dp, CircleShape)
                .clip(CircleShape)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = {
                            expanded = true
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
                    contentDescription = "Jimmy Copilot",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        // Diálogo de Asistencia Contextual
        if (expanded) {
            Dialog(onDismissRequest = { expanded = false }) {
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
                                text = "Jimmy dice... ✨",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1565C0)
                            )
                            IconButton(onClick = { expanded = false }) {
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
                                expanded = false
                                showVoiceOverlay = true
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Mic, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Hablar con Jimmy", fontWeight = FontWeight.Bold)
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
