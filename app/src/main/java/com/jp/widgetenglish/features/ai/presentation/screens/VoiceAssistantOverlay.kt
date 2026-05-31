package com.jp.widgetenglish.features.ai.presentation.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.jp.widgetenglish.data.remote.ai.GroqAiClient
import com.jp.widgetenglish.features.common.SttHelper
import com.jp.widgetenglish.features.common.TtsHelper
import kotlinx.coroutines.launch

@Composable
fun VoiceAssistantOverlay(
    onDismiss: () -> Unit,
    aiClient: GroqAiClient
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val ttsHelper = remember { TtsHelper(context) }
    
    var jimmyText by remember { mutableStateOf("¡Hola! Soy Jimmy. Presiona el micro y hablemos.") }
    var userText by remember { mutableStateOf("") }
    var isListening by remember { mutableStateOf(false) }
    var isThinking by remember { mutableStateOf(false) }
    var speechRate by remember { mutableStateOf(1.0f) }

    val sttHelper = remember {
        SttHelper(
            context = context,
            onResult = { result ->
                userText = result
                isListening = false
                isThinking = true
                scope.launch {
                    val response = aiClient.procesarConversacionVoz(result)
                    jimmyText = response
                    isThinking = false
                    ttsHelper.speak(response, speechRate)
                }
            },
            onPartialResult = { partial -> userText = partial },
            onError = { isListening = false }
        )
    }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            isListening = true
            sttHelper.startListening()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            ttsHelper.shutdown()
            sttHelper.destroy()
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.85f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Header & Controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.White)
                    }
                    
                    TextButton(
                        onClick = { speechRate = if (speechRate == 1.0f) 0.7f else 1.0f },
                        colors = ButtonDefaults.textButtonColors(contentColor = Color.White)
                    ) {
                        Text(if (speechRate == 1.0f) "⚡ Normal" else "🐌 Lento")
                    }
                }

                // Jimmy's Section
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = jimmyText,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        lineHeight = 32.sp
                    )
                    
                    if (isThinking) {
                        Spacer(modifier = Modifier.height(16.dp))
                        CircularProgressIndicator(color = Color(0xFF039BE5))
                    }
                }

                // User's Section
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (userText.isNotEmpty()) {
                        Text(
                            text = userText,
                            fontSize = 18.sp,
                            color = Color.LightGray,
                            textAlign = TextAlign.Center,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )
                        Spacer(modifier = Modifier.height(32.dp))
                    }

                    // Mic Button
                    Surface(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .clickable {
                                if (isListening) {
                                    sttHelper.stopListening()
                                    isListening = false
                                } else {
                                    val permissionCheck = ContextCompat.checkSelfPermission(
                                        context, Manifest.permission.RECORD_AUDIO
                                    )
                                    if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                                        userText = ""
                                        isListening = true
                                        sttHelper.startListening()
                                    } else {
                                        launcher.launch(Manifest.permission.RECORD_AUDIO)
                                    }
                                }
                            },
                        color = if (isListening) Color.Red else Color(0xFF1565C0)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (isListening) Icons.Default.Stop else Icons.Default.Mic,
                                contentDescription = "Micrófono",
                                tint = Color.White,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = if (isListening) "Escuchando..." else "Toca para hablar",
                        color = Color.White,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}
