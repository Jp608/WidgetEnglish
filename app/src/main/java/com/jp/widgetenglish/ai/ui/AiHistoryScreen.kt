package com.jp.widgetenglish.ai.ui

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jp.widgetenglish.ai.data.local.AiChatDao
import com.jp.widgetenglish.ai.data.local.AiConversationEntity
import com.jp.widgetenglish.features.common.AppBottomBar
import com.widgetenglish.app.ui.Screen
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private val AiBlue = Color(0xFF0057E7)
private val AiBlue2 = Color(0xFF1565FF)
private val AiPurple = Color(0xFF7A5CFF)
private val AiBg = Color(0xFFF7F9FD)
private val AiText = Color(0xFF08145F)
private val AiMuted = Color(0xFF6B7280)
private val AiBorder = Color(0xFFE5EAF3)
private val AiSoftBlue = Color(0xFFEAF2FF)
private val AiSoftPurple = Color(0xFFF0EBFF)
private val AiSoftGreen = Color(0xFFEAF7EE)

private data class AiHistoryDimens(
    val horizontalPadding: Dp,
    val heroPadding: Dp,
    val heroIconSize: Dp,
    val heroTitleSize: Int,
    val cardCorner: Dp
)

@Composable
private fun rememberAiHistoryDimens(): AiHistoryDimens {
    val width = LocalConfiguration.current.screenWidthDp

    return when {
        width < 360 -> AiHistoryDimens(
            horizontalPadding = 14.dp,
            heroPadding = 14.dp,
            heroIconSize = 74.dp,
            heroTitleSize = 20,
            cardCorner = 22.dp
        )

        width < 420 -> AiHistoryDimens(
            horizontalPadding = 18.dp,
            heroPadding = 18.dp,
            heroIconSize = 84.dp,
            heroTitleSize = 23,
            cardCorner = 24.dp
        )

        else -> AiHistoryDimens(
            horizontalPadding = 22.dp,
            heroPadding = 20.dp,
            heroIconSize = 92.dp,
            heroTitleSize = 25,
            cardCorner = 26.dp
        )
    }
}

private fun aiHeaderGradient(): Brush {
    return Brush.horizontalGradient(
        colors = listOf(
            AiBlue,
            AiBlue2,
            AiPurple
        )
    )
}

private fun aiButtonGradient(): Brush {
    return Brush.horizontalGradient(
        colors = listOf(
            Color(0xFF0057E7),
            Color(0xFF1565FF),
            Color(0xFF7A5CFF)
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiHistoryScreen(
    aiChatDao: AiChatDao,
    onInicioClick: () -> Unit,
    onVocabularioClick: () -> Unit,
    onLotesClick: () -> Unit,
    onEstudioClick: () -> Unit,
    onIaClick: () -> Unit,
    onPerfilClick: () -> Unit,
    onBack: (() -> Unit)? = null
) {
    val scope = rememberCoroutineScope()
    val dimens = rememberAiHistoryDimens()

    var selectedConversationId by remember { mutableStateOf<String?>(null) }
    var isCreatingNewConversation by remember { mutableStateOf(false) }
    var conversationToDelete by remember { mutableStateOf<AiConversationEntity?>(null) }

    if (selectedConversationId != null || isCreatingNewConversation) {
        BackHandler {
            selectedConversationId = null
            isCreatingNewConversation = false
        }

        AiChatScreen(
            aiChatDao = aiChatDao,
            initialConversationId = selectedConversationId,
            onBack = {
                selectedConversationId = null
                isCreatingNewConversation = false
            },
            onInicioClick = onInicioClick,
            onVocabularioClick = onVocabularioClick,
            onLotesClick = onLotesClick,
            onEstudioClick = onEstudioClick,
            onIaClick = {
                selectedConversationId = null
                isCreatingNewConversation = false
            },
            onPerfilClick = onPerfilClick
        )
        return
    }

    val conversations by aiChatDao
        .getConversations()
        .collectAsState(initial = emptyList())

    conversationToDelete?.let { conversation ->
        AlertDialog(
            onDismissRequest = {
                conversationToDelete = null
            },
            title = {
                Text("Eliminar conversación")
            },
            text = {
                Text("¿Seguro que deseas eliminar esta conversación? Esta acción no se puede deshacer.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            aiChatDao.deleteMessagesByConversation(conversation.id)
                            aiChatDao.deleteConversation(conversation.id)
                            conversationToDelete = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        conversationToDelete = null
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

    Scaffold(
        containerColor = AiBg,
        topBar = {
            AiHistoryHeader(
                onBack = {
                    onBack?.invoke() ?: onInicioClick()
                }
            )
        },
        bottomBar = {
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
    ) { paddingValues ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(AiBg),
            contentPadding = PaddingValues(
                start = dimens.horizontalPadding,
                end = dimens.horizontalPadding,
                top = 18.dp,
                bottom = 22.dp
            ),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                AiAssistantHeroCard(
                    dimens = dimens,
                    onNewConversationClick = {
                        isCreatingNewConversation = true
                    }
                )
            }

            if (conversations.isEmpty()) {
                item {
                    AiEmptyHistoryCard(
                        dimens = dimens
                    )
                }
            } else {
                item {
                    AiHistorySectionHeader()
                }

                items(
                    items = conversations,
                    key = { it.id }
                ) { conversation ->
                    AiConversationCard(
                        conversation = conversation,
                        onClick = {
                            selectedConversationId = conversation.id
                        },
                        onDeleteClick = {
                            conversationToDelete = conversation
                        }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    AiPrivacyNote()
                    Spacer(modifier = Modifier.navigationBarsPadding())
                }
            }
        }
    }
}

@Composable
private fun AiHistoryHeader(
    onBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(104.dp)
            .background(aiHeaderGradient())
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
            fontWeight = FontWeight.ExtraBold
        )

        Surface(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .size(42.dp),
            shape = RoundedCornerShape(14.dp),
            color = Color.White.copy(alpha = 0.16f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                AiRobotMark(
                    size = 28.dp,
                    backgroundColor = Color.Transparent,
                    faceColor = Color.White,
                    detailColor = AiBlue
                )
            }
        }
    }
}

@Composable
private fun AiAssistantHeroCard(
    dimens: AiHistoryDimens,
    onNewConversationClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(dimens.cardCorner),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(dimens.heroPadding)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(dimens.heroIconSize),
                    shape = RoundedCornerShape(24.dp),
                    color = AiSoftBlue
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        AiRobotMark(
                            size = dimens.heroIconSize * 0.56f,
                            backgroundColor = Color.Transparent,
                            faceColor = AiBlue,
                            detailColor = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Tu tutor de inglés",
                        color = AiText,
                        fontSize = dimens.heroTitleSize.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Consulta traducciones, ejemplos, correcciones y consejos.",
                        color = AiMuted,
                        fontSize = 15.sp,
                        lineHeight = 21.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            AiGradientButton(
                text = "Nueva conversación",
                icon = Icons.Default.Add,
                onClick = onNewConversationClick
            )
        }
    }
}

@Composable
private fun AiGradientButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(aiButtonGradient())
            .clickable { onClick() }
            .padding(horizontal = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(26.dp)
        )

        Spacer(modifier = Modifier.width(10.dp))

        Text(
            text = text,
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

@Composable
private fun AiHistorySectionHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp, bottom = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {


        Surface(
            shape = RoundedCornerShape(70.dp),
            color = AiSoftBlue
        ) {
            Text(
                text = "Conversaciones recientes",
                color = AiText,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun AiEmptyHistoryCard(
    dimens: AiHistoryDimens
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 390.dp),
        shape = RoundedCornerShape(dimens.cardCorner),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = dimens.heroPadding, vertical = 26.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.size(140.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    modifier = Modifier.size(128.dp),
                    shape = CircleShape,
                    color = AiSoftBlue.copy(alpha = 0.9f)
                ) {}

                Text(
                    text = "✦",
                    color = AiPurple,
                    fontSize = 20.sp,
                    modifier = Modifier.align(Alignment.CenterStart)
                )

                Text(
                    text = "✦",
                    color = AiBlue2,
                    fontSize = 18.sp,
                    modifier = Modifier.align(Alignment.CenterEnd)
                )

                AiRobotMark(
                    size = 78.dp,
                    backgroundColor = Color.Transparent,
                    faceColor = AiBlue,
                    detailColor = Color.White
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Aún no tienes conversaciones",
                color = AiText,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Haz tu primera consulta y retómala después desde aquí.",
                color = AiMuted,
                fontSize = 15.sp,
                lineHeight = 21.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.widthIn(max = 300.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AiFeatureTile(
                    modifier = Modifier.weight(1f),
                    title = "Traducciones",
                    description = "Traduce palabras, frases y oraciones.",
                    icon = Icons.Default.Translate,
                    iconColor = AiBlue,
                    background = AiSoftBlue
                )

                AiFeatureTile(
                    modifier = Modifier.weight(1f),
                    title = "Correcciones",
                    description = "Mejora tu gramática y escritura.",
                    icon = Icons.Default.CheckCircle,
                    iconColor = AiPurple,
                    background = AiSoftPurple
                )
            }
        }
    }
}

@Composable
private fun AiFeatureTile(
    modifier: Modifier,
    title: String,
    description: String,
    icon: ImageVector,
    iconColor: Color,
    background: Color
) {
    OutlinedCard(
        modifier = modifier.heightIn(min = 98.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.outlinedCardColors(
            containerColor = Color.White
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(46.dp),
                shape = RoundedCornerShape(14.dp),
                color = background
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    color = AiText,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = description,
                    color = AiMuted,
                    fontSize = 11.sp,
                    lineHeight = 15.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun AiConversationCard(
    conversation: AiConversationEntity,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val visual = conversationVisual(conversation.title + conversation.summary)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(70.dp),
                shape = RoundedCornerShape(22.dp),
                color = visual.background
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = visual.icon,
                        contentDescription = null,
                        tint = visual.color,
                        modifier = Modifier.size(34.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = conversation.title.ifBlank { "Conversación IA" },
                    color = AiText,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(5.dp))

                Text(
                    text = conversation.summary.ifBlank { "Sin resumen disponible." },
                    color = AiMuted,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(7.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = null,
                        tint = AiBlue,
                        modifier = Modifier.size(17.dp)
                    )

                    Spacer(modifier = Modifier.width(5.dp))

                    Text(
                        text = formatSmartDate(conversation.updatedAt),
                        color = AiMuted,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Surface(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFFF2F4F8)
            ) {
                IconButton(
                    onClick = onDeleteClick
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar conversación",
                        tint = Color(0xFF4B5563),
                        modifier = Modifier.size(23.dp)
                    )
                }
            }
        }
    }
}

private data class ConversationVisual(
    val icon: ImageVector,
    val color: Color,
    val background: Color
)

private fun conversationVisual(text: String): ConversationVisual {
    val lower = text.lowercase()

    return when {
        lower.contains("consejo") || lower.contains("estudiar") -> ConversationVisual(
            icon = Icons.Default.School,
            color = Color(0xFF7C3AED),
            background = Color(0xFFF3E8FF)
        )

        lower.contains("oración") || lower.contains("oraciones") || lower.contains("frase") -> ConversationVisual(
            icon = Icons.Default.Description,
            color = Color(0xFF16A34A),
            background = AiSoftGreen
        )

        else -> ConversationVisual(
            icon = Icons.Default.Translate,
            color = AiBlue,
            background = AiSoftBlue
        )
    }
}

@Composable
private fun AiPrivacyNote() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 2.dp, bottom = 6.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Tus conversaciones son privadas y seguras",
            color = Color(0xFF9CA3AF),
            fontSize = 12.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun AiRobotMark(
    size: Dp,
    backgroundColor: Color,
    faceColor: Color,
    detailColor: Color
) {
    Box(
        modifier = Modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        if (backgroundColor != Color.Transparent) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                shape = CircleShape,
                color = backgroundColor
            ) {}
        }

        Box(
            modifier = Modifier
                .size(size * 0.62f)
                .clip(RoundedCornerShape(size * 0.18f))
                .background(faceColor),
            contentAlignment = Alignment.Center
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(size * 0.12f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(size * 0.075f),
                    shape = CircleShape,
                    color = detailColor
                ) {}

                Surface(
                    modifier = Modifier.size(size * 0.075f),
                    shape = CircleShape,
                    color = detailColor
                ) {}
            }

            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = size * 0.13f)
                    .width(size * 0.18f)
                    .height(size * 0.035f),
                shape = RoundedCornerShape(50),
                color = detailColor
            ) {}
        }

        Surface(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .size(size * 0.07f),
            shape = CircleShape,
            color = faceColor
        ) {}

        Surface(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = size * 0.07f)
                .width(size * 0.025f)
                .height(size * 0.14f),
            shape = RoundedCornerShape(50),
            color = faceColor
        ) {}
    }
}

private fun formatSmartDate(timestamp: Long): String {
    val date = Date(timestamp)
    val now = Calendar.getInstance()
    val target = Calendar.getInstance().apply {
        time = date
    }

    val timeFormatter = SimpleDateFormat("h:mm a", Locale.getDefault())
    val dateFormatter = SimpleDateFormat("dd MMM · h:mm a", Locale.getDefault())

    val isSameYear = now.get(Calendar.YEAR) == target.get(Calendar.YEAR)
    val isSameDay =
        isSameYear &&
                now.get(Calendar.DAY_OF_YEAR) == target.get(Calendar.DAY_OF_YEAR)

    if (isSameDay) {
        return "Hoy · ${timeFormatter.format(date).lowercase()}"
    }

    val yesterday = Calendar.getInstance().apply {
        add(Calendar.DAY_OF_YEAR, -1)
    }

    val isYesterday =
        yesterday.get(Calendar.YEAR) == target.get(Calendar.YEAR) &&
                yesterday.get(Calendar.DAY_OF_YEAR) == target.get(Calendar.DAY_OF_YEAR)

    if (isYesterday) {
        return "Ayer · ${timeFormatter.format(date).lowercase()}"
    }

    return dateFormatter.format(date).lowercase()
}