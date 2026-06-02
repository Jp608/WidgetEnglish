package com.jp.widgetenglish.features.ai.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jp.widgetenglish.data.local.entity.ChatMessageEntity
import com.jp.widgetenglish.features.ai.presentation.viewmodel.ChatViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatRoomScreen(
    sessionId: String,
    viewModel: ChatViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.roomState.collectAsState()
    var textState by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(sessionId) {
        viewModel.cargarMensajes(sessionId)
    }

    LaunchedEffect(state.mensajes.size) {
        if (state.mensajes.isNotEmpty()) {
            listState.animateScrollToItem(state.mensajes.size - 1)
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Chateando con Jimmy ✨", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFF1565C0),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = Color(0xFFF5F5F5)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                state = listState,
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.mensajes) { mensaje ->
                    MessageBubble(mensaje)
                }

                if (state.cargandoRespuesta) {
                    item {
                        Surface(
                            color = Color(0xFFE3F2FD),
                            shape = RoundedCornerShape(topStart = 0.dp, topEnd = 20.dp, bottomStart = 20.dp, bottomEnd = 20.dp),
                            modifier = Modifier.padding(end = 64.dp)
                        ) {
                            Text(
                                text = "Jimmy está pensando...",
                                modifier = Modifier.padding(12.dp),
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }

            // Input area
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                tonalElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .navigationBarsPadding()
                        .imePadding(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = textState,
                        onValueChange = { textState = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Escribe algo en inglés o español...") },
                        colors = TextFieldDefaults.colors(
                            unfocusedContainerColor = Color.Transparent,
                            focusedContainerColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent
                        ),
                        maxLines = 3
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    IconButton(
                        onClick = {
                            if (textState.isNotBlank()) {
                                viewModel.enviarMensaje(textState)
                                textState = ""
                            }
                        },
                        enabled = !state.cargandoRespuesta && textState.isNotBlank(),
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = if (textState.isNotBlank()) Color(0xFF1565C0) else Color.LightGray,
                            contentColor = Color.White
                        )
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Enviar")
                    }
                }
            }
        }
    }
}

@Composable
fun MessageBubble(mensaje: ChatMessageEntity) {
    val isUser = mensaje.role == "user"
    val alignment = if (isUser) Alignment.End else Alignment.Start
    val color = if (isUser) Color(0xFF1565C0) else Color(0xFFE8EAF6)
    val textColor = if (isUser) Color.White else Color.Black
    val shape = if (isUser) {
        RoundedCornerShape(topStart = 20.dp, topEnd = 0.dp, bottomStart = 20.dp, bottomEnd = 20.dp)
    } else {
        RoundedCornerShape(topStart = 0.dp, topEnd = 20.dp, bottomStart = 20.dp, bottomEnd = 20.dp)
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        Surface(
            color = color,
            shape = shape,
            modifier = Modifier.padding(
                start = if (isUser) 64.dp else 0.dp,
                end = if (isUser) 0.dp else 64.dp
            )
        ) {
            Text(
                text = mensaje.content,
                modifier = Modifier.padding(12.dp),
                fontSize = 15.sp,
                color = textColor,
                lineHeight = 20.sp
            )
        }
    }
}
