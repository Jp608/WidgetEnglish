package com.jp.widgetenglish.features.ai.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jp.widgetenglish.data.local.entity.ChatSessionEntity
import com.jp.widgetenglish.features.ai.presentation.viewmodel.ChatViewModel
import com.jp.widgetenglish.features.common.AppBottomBar
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatHistoryScreen(
    viewModel: ChatViewModel,
    onChatClick: (String) -> Unit,
    onBackClick: () -> Unit,
    onVocabularioClick: () -> Unit,
    onLotesClick: () -> Unit,
    onEstudioClick: () -> Unit,
    onIaClick: () -> Unit,
    onPerfilClick: () -> Unit
) {
    val state by viewModel.historyState.collectAsState()

    Scaffold(
        bottomBar = {
            AppBottomBar(
                selectedRoute = "ia",
                onInicioClick = onBackClick,
                onVocabularioClick = onVocabularioClick,
                onLotesClick = onLotesClick,
                onEstudioClick = onEstudioClick,
                onIaClick = onIaClick,
                onPerfilClick = onPerfilClick
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { 
                    viewModel.crearNuevaSesion("Nueva Práctica") { id ->
                        onChatClick(id)
                    }
                },
                containerColor = Color(0xFF1565C0),
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nuevo Chat")
            }
        },
        containerColor = Color(0xFFF5F5F5)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = paddingValues.calculateBottomPadding())
        ) {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFF1A237E), Color(0xFF1565C0))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Práctica con Leo ✨",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            if (state.cargando) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (state.sesiones.isEmpty()) {
                EmptyHistoryState {
                    viewModel.crearNuevaSesion("Mi primera charla") { onChatClick(it) }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(state.sesiones) { sesion ->
                        ChatSessionCard(
                            sesion = sesion,
                            onClick = { onChatClick(sesion.id) },
                            onDelete = { viewModel.eliminarSesion(sesion.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChatSessionCard(
    sesion: ChatSessionEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val sdf = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())
    val fechaStr = sdf.format(Date(sesion.ultimaInteraccion))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(Color(0xFFE3F2FD), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Chat, contentDescription = null, tint = Color(0xFF1565C0))
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = sesion.titulo,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF1A237E)
                )
                Text(
                    text = sesion.resumen ?: "Pulsa para empezar a chatear...",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = fechaStr,
                    fontSize = 12.sp,
                    color = Color.LightGray
                )
            }

            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color(0xFFEF9A9A))
            }
        }
    }
}

@Composable
fun EmptyHistoryState(onStart: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Chat,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = Color.LightGray
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text("Aún no has practicado con Leo", fontWeight = FontWeight.Bold, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        Text(
            "Leo es tu tutor de IA. Puedes pedirle ayuda con traducciones o practicar conversaciones reales.",
            color = Color.Gray,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onStart,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0))
        ) {
            Text("Comenzar a practicar")
        }
    }
}
