package com.jp.widgetenglish.ai.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jp.widgetenglish.R
import com.jp.widgetenglish.ai.data.local.AiChatDao
import com.jp.widgetenglish.features.common.AppBottomBar
import com.widgetenglish.app.ui.Screen
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.ui.text.TextStyle
private val ChatBlue = Color(0xFF0057E7)
private val ChatBlue2 = Color(0xFF1565FF)
private val ChatPurple = Color(0xFF7A5CFF)
private val ChatBg = Color(0xFFF7F9FD)
private val ChatText = Color(0xFF08145F)
private val ChatMuted = Color(0xFF6B7280)
private val ChatBorder = Color(0xFFE5EAF3)
private val ChatSoftBlue = Color(0xFFEAF2FF)
private val ChatSoftPurple = Color(0xFFF0EBFF)
private val ChatSoftGreen = Color(0xFFEAF7EE)

private data class AiChatDimens(
    val horizontalPadding: Dp,
    val introPadding: Dp,
    val robotSize: Dp,
    val messageMaxWidth: Dp,
    val titleSize: Int
)

@Composable
private fun rememberAiChatDimens(): AiChatDimens {
    val width = LocalConfiguration.current.screenWidthDp

    return when {
        width < 360 -> AiChatDimens(
            horizontalPadding = 12.dp,
            introPadding = 14.dp,
            robotSize = 64.dp,
            messageMaxWidth = 270.dp,
            titleSize = 19
        )

        width < 420 -> AiChatDimens(
            horizontalPadding = 18.dp,
            introPadding = 18.dp,
            robotSize = 76.dp,
            messageMaxWidth = 315.dp,
            titleSize = 22
        )

        else -> AiChatDimens(
            horizontalPadding = 22.dp,
            introPadding = 20.dp,
            robotSize = 84.dp,
            messageMaxWidth = 360.dp,
            titleSize = 24
        )
    }
}

private fun chatHeaderGradient(): Brush {
    return Brush.horizontalGradient(
        colors = listOf(
            ChatBlue,
            ChatBlue2,
            ChatPurple
        )
    )
}

private fun chatButtonGradient(): Brush {
    return Brush.horizontalGradient(
        colors = listOf(
            ChatBlue,
            ChatBlue2,
            ChatPurple
        )
    )
}

@OptIn(
    ExperimentalMaterial3Api::class,
    androidx.compose.foundation.layout.ExperimentalLayoutApi::class
)
@Composable
fun AiChatScreen(
    aiChatDao: AiChatDao,
    initialConversationId: String? = null,
    onBack: () -> Unit = {},
    onInicioClick: () -> Unit = {},
    onVocabularioClick: () -> Unit = {},
    onLotesClick: () -> Unit = {},
    onEstudioClick: () -> Unit = {},
    onIaClick: () -> Unit = {},
    onPerfilClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val dimens = rememberAiChatDimens()
    val listState = rememberLazyListState()

    val viewModel: AiChatViewModel = viewModel(
        key = "ai_chat_${initialConversationId ?: "new"}",
        factory = AiChatViewModelFactory(
            apiKey = context.getString(R.string.gemini_api_key),
            aiChatDao = aiChatDao
        )
    )

    val state by viewModel.uiState.collectAsState()

    val isKeyboardVisible = WindowInsets.isImeVisible

    LaunchedEffect(initialConversationId) {
        viewModel.initConversation(initialConversationId)
    }

    LaunchedEffect(state.messages.size, state.isLoading) {
        val targetIndex = state.messages.size + if (state.isLoading) 1 else 0
        if (targetIndex > 0) {
            listState.animateScrollToItem(targetIndex - 1)
        }
    }

    Scaffold(
        containerColor = ChatBg,
        topBar = {
            AiChatHeader(
                onBack = onBack
            )
        },
        bottomBar = {
            if (!isKeyboardVisible) {
                AppBottomBar(
                    selectedRoute = Screen.Ia.route,
                    onInicioClick = onInicioClick,
                    onVocabularioClick = onVocabularioClick,
                    onLotesClick = onLotesClick,
                    onEstudioClick = onEstudioClick,
                    onIaClick = onIaClick,
                    onPerfilClick = onPerfilClick
                )
            }
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(ChatBg)
                .imePadding()
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(
                    start = dimens.horizontalPadding,
                    end = dimens.horizontalPadding,
                    top = 16.dp,
                    bottom = 14.dp
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    AiChatIntroCard(
                        dimens = dimens,
                        onTranslateClick = {
                            viewModel.onInputChange("Traduce al inglés: ")
                        },
                        onCorrectClick = {
                            viewModel.onInputChange("Corrige esta frase en inglés: ")
                        },
                        onAdviceClick = {
                            viewModel.onInputChange("Dame un consejo para mejorar mi inglés.")
                        }
                    )
                }

                itemsIndexed(
                    items = state.messages,
                    key = { index, message -> "$index-${message.role}-${message.content.take(20)}" }
                ) { _, message ->
                    AiMessageBubble(
                        message = message,
                        maxWidth = dimens.messageMaxWidth
                    )
                }

                if (state.isLoading) {
                    item {
                        AiLoadingBubble()
                    }
                }
            }

            state.error?.let { error ->
                Text(
                    text = error,
                    color = Color(0xFFDC2626),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = dimens.horizontalPadding)
                        .padding(bottom = 6.dp)
                )
            }

            AiChatInputBar(
                value = state.input,
                onValueChange = viewModel::onInputChange,
                enabled = !state.isLoading,
                onSendClick = {
                    focusManager.clearFocus()
                    viewModel.sendMessage()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = dimens.horizontalPadding)
                    .padding(bottom = if (isKeyboardVisible) 4.dp else 10.dp)
            )
        }
    }
}

@Composable
private fun AiChatHeader(
    onBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(104.dp)
            .background(chatHeaderGradient())
            .statusBarsPadding()
            .padding(horizontal = 16.dp),
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
            text = "Asistente IA",
            color = Color.White,
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            maxLines = 1
        )

        Surface(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .size(42.dp),
            shape = RoundedCornerShape(14.dp),
            color = Color.White.copy(alpha = 0.16f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                AiChatRobotMark(
                    size = 28.dp,
                    faceColor = Color.White,
                    detailColor = ChatBlue
                )
            }
        }
    }
}

@Composable
private fun AiChatIntroCard(
    dimens: AiChatDimens,
    onTranslateClick: () -> Unit,
    onCorrectClick: () -> Unit,
    onAdviceClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(dimens.introPadding)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(dimens.robotSize),
                    shape = RoundedCornerShape(24.dp),
                    color = ChatSoftBlue
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        AiChatRobotMark(
                            size = dimens.robotSize * 0.58f,
                            faceColor = ChatBlue,
                            detailColor = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Tutor de inglés con Gemini",
                        color = ChatText,
                        fontSize = dimens.titleSize.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Traducciones, correcciones, ejemplos y consejos en un solo lugar.",
                        color = ChatMuted,
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AiQuickActionChip(
                    modifier = Modifier.weight(1f),
                    text = "Traducir",
                    icon = Icons.Default.Translate,
                    iconColor = ChatBlue,
                    background = ChatSoftBlue,
                    onClick = onTranslateClick
                )

                AiQuickActionChip(
                    modifier = Modifier.weight(1f),
                    text = "Corregir",
                    icon = Icons.Default.CheckCircle,
                    iconColor = Color(0xFF16A34A),
                    background = ChatSoftGreen,
                    onClick = onCorrectClick
                )

                AiQuickActionChip(
                    modifier = Modifier.weight(1f),
                    text = "Consejo",
                    icon = Icons.Default.Lightbulb,
                    iconColor = Color(0xFF7C3AED),
                    background = ChatSoftPurple,
                    onClick = onAdviceClick
                )
            }
        }
    }
}

@Composable
private fun AiQuickActionChip(
    modifier: Modifier,
    text: String,
    icon: ImageVector,
    iconColor: Color,
    background: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .heightIn(min = 46.dp)
            .clip(RoundedCornerShape(18.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        color = Color.White,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = ChatBorder
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(28.dp),
                shape = CircleShape,
                color = background
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(17.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            Text(
                text = text,
                color = ChatText,
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun AiMessageBubble(
    message: AiChatMessage,
    maxWidth: Dp
) {
    val isUser = message.role == AiRole.USER

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Top
    ) {
        if (!isUser) {
            Surface(
                modifier = Modifier
                    .size(36.dp)
                    .padding(top = 3.dp),
                shape = RoundedCornerShape(12.dp),
                color = ChatSoftBlue
            ) {
                Box(contentAlignment = Alignment.Center) {
                    AiChatRobotMark(
                        size = 23.dp,
                        faceColor = ChatBlue,
                        detailColor = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))
        }

        Surface(
            shape = RoundedCornerShape(
                topStart = 22.dp,
                topEnd = 22.dp,
                bottomStart = if (isUser) 22.dp else 6.dp,
                bottomEnd = if (isUser) 6.dp else 22.dp
            ),
            color = if (isUser) ChatBlue else Color.White,
            tonalElevation = 0.dp,
            shadowElevation = if (isUser) 0.dp else 2.dp,
            modifier = Modifier.widthIn(max = maxWidth)
        ) {
            Text(
                text = message.content,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 13.dp),
                color = if (isUser) Color.White else Color(0xFF1F2937),
                fontSize = 14.sp,
                lineHeight = 21.sp,
                fontWeight = if (isUser) FontWeight.SemiBold else FontWeight.Normal
            )
        }
    }
}

@Composable
private fun AiLoadingBubble() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(36.dp),
            shape = RoundedCornerShape(12.dp),
            color = ChatSoftBlue
        ) {
            Box(contentAlignment = Alignment.Center) {
                AiChatRobotMark(
                    size = 23.dp,
                    faceColor = ChatBlue,
                    detailColor = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            shadowElevation = 2.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = ChatBlue
                )

                Spacer(modifier = Modifier.width(9.dp))

                Text(
                    text = "Gemini está respondiendo...",
                    color = ChatMuted,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
private fun AiChatInputBar(
    value: String,
    onValueChange: (String) -> Unit,
    enabled: Boolean,
    onSendClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 50.dp, max = 120.dp),
                enabled = enabled,
                textStyle = TextStyle(
                    color = ChatText,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                placeholder = {
                    Text(
                        text = "Escribe tu duda de inglés...",
                        color = ChatMuted
                    )
                },
                maxLines = 4,
                shape = RoundedCornerShape(22.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ChatBorder,
                    unfocusedBorderColor = ChatBorder,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    cursorColor = ChatBlue
                ),
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Send
                ),
                keyboardActions = KeyboardActions(
                    onSend = {
                        if (enabled && value.isNotBlank()) {
                            onSendClick()
                        }
                    }
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(
                        if (enabled && value.isNotBlank()) {
                            chatButtonGradient()
                        } else {
                            Brush.horizontalGradient(
                                listOf(Color(0xFFE5E7EB), Color(0xFFE5E7EB))
                            )
                        }
                    )
                    .clickable(
                        enabled = enabled && value.isNotBlank()
                    ) {
                        onSendClick()
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Enviar",
                    tint = if (enabled && value.isNotBlank()) {
                        Color.White
                    } else {
                        Color(0xFF9CA3AF)
                    },
                    modifier = Modifier.size(25.dp)
                )
            }
        }
    }
}

@Composable
private fun AiChatRobotMark(
    size: Dp,
    faceColor: Color,
    detailColor: Color
) {
    Box(
        modifier = Modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(size * 0.72f)
                .clip(RoundedCornerShape(size * 0.2f))
                .background(faceColor),
            contentAlignment = Alignment.Center
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(size * 0.13f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(size * 0.08f),
                    shape = CircleShape,
                    color = detailColor
                ) {}

                Surface(
                    modifier = Modifier.size(size * 0.08f),
                    shape = CircleShape,
                    color = detailColor
                ) {}
            }

            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = size * 0.15f)
                    .width(size * 0.2f)
                    .height(size * 0.04f),
                shape = RoundedCornerShape(50),
                color = detailColor
            ) {}
        }

        Surface(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .size(size * 0.075f),
            shape = CircleShape,
            color = faceColor
        ) {}

        Surface(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = size * 0.065f)
                .width(size * 0.028f)
                .height(size * 0.15f),
            shape = RoundedCornerShape(50),
            color = faceColor
        ) {}
    }
}