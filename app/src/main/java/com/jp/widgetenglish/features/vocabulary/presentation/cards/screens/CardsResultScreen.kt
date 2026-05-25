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
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoodBad
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jp.widgetenglish.features.vocabulary.presentation.cards.viewmodel.CardsViewModel

private val StrongBlue = Color(0xFF0057E7)
private val PrimaryBlue = Color(0xFF1565C0)
private val Purple = Color(0xFF7C3AED)
private val ScreenBg = Color(0xFFF7F9FD)
private val TextDark = Color(0xFF08145F)
private val TextMuted = Color(0xFF6B7280)

private val Green = Color(0xFF16A34A)
private val DarkGreen = Color(0xFF15803D)
private val Red = Color(0xFFDC2626)
private val Orange = Color(0xFFF59E0B)
private val PinkRed = Color(0xFFE11D48)

private val SoftBlue = Color(0xFFEAF2FF)
private val SoftGreen = Color(0xFFEAF8EE)
private val SoftRed = Color(0xFFFFECEF)
private val SoftOrange = Color(0xFFFFF4E0)

@Composable
fun CardsResultScreen(
    viewModel: CardsViewModel,
    onBackToStudy: () -> Unit,
    onRepeatSession: () -> Unit,
    onNewConfig: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val resumen = state.resumen

    Scaffold(
        containerColor = ScreenBg
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(ScreenBg)
                .padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            ResultHeader()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 18.dp)
                    .padding(top = 14.dp, bottom = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (resumen == null) {
                    EmptyResultState(
                        onBackToStudy = onBackToStudy
                    )
                } else {
                    SummaryCard(
                        loteNombre = resumen.loteNombre,
                        totalEstudiadas = resumen.totalEstudiadas,
                        progresoFinal = resumen.progresoFinal.toInt()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    StatsGrid(
                        conocidas = resumen.conocidas,
                        noConocidas = resumen.noConocidas,
                        dificiles = resumen.dificiles,
                        aprendidas = resumen.aprendidas
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    ProgressFinalCard(
                        progreso = resumen.progresoFinal.toInt()
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    ResultActionButton(
                        text = "Repetir sesión",
                        icon = Icons.Filled.Refresh,
                        style = ResultButtonStyle.PRIMARY,
                        onClick = {
                            viewModel.reiniciarSesion()
                            onRepeatSession()
                        }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    ResultActionButton(
                        text = "Nueva configuración",
                        icon = Icons.Filled.Book,
                        style = ResultButtonStyle.SECONDARY_BLUE,
                        onClick = onNewConfig
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    ResultActionButton(
                        text = "Volver a modo estudio",
                        icon = Icons.Filled.Home,
                        style = ResultButtonStyle.SECONDARY_NEUTRAL,
                        onClick = onBackToStudy
                    )

                    Spacer(modifier = Modifier.navigationBarsPadding())
                }
            }
        }
    }
}

@Composable
private fun ResultHeader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .clip(
                RoundedCornerShape(
                    bottomStart = 38.dp,
                    bottomEnd = 38.dp
                )
            )
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        StrongBlue,
                        PrimaryBlue,
                        Purple
                    )
                )
            )
            .statusBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier.size(74.dp),
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.18f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Filled.EmojiEvents,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(45.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "¡Sesión completada!",
                color = Color.White,
                fontSize = 29.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Buen trabajo repasando tus tarjetas",
                color = Color.White.copy(alpha = 0.84f),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun SummaryCard(
    loteNombre: String,
    totalEstudiadas: Int,
    progresoFinal: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(23.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 3.dp
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(58.dp),
                shape = RoundedCornerShape(19.dp),
                color = SoftBlue
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Filled.School,
                        contentDescription = null,
                        tint = StrongBlue,
                        modifier = Modifier.size(31.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = loteNombre,
                    color = TextDark,
                    fontSize = 21.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "$totalEstudiadas tarjetas estudiadas",
                    color = TextMuted,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Text(
                text = "$progresoFinal%",
                color = StrongBlue,
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

@Composable
private fun StatsGrid(
    conocidas: Int,
    noConocidas: Int,
    dificiles: Int,
    aprendidas: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(23.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 3.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatBox(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Filled.Check,
                    label = "Conocidas",
                    value = conocidas,
                    color = Green,
                    darkColor = DarkGreen,
                    background = SoftGreen
                )

                StatBox(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Filled.School,
                    label = "Aprendidas",
                    value = aprendidas,
                    color = Green,
                    darkColor = DarkGreen,
                    background = SoftGreen
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatBox(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Filled.MoodBad,
                    label = "Difíciles",
                    value = dificiles,
                    color = Orange,
                    darkColor = Orange,
                    background = SoftOrange
                )

                StatBox(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Filled.Star,
                    label = "Por repasar",
                    value = noConocidas,
                    color = Red,
                    darkColor = PinkRed,
                    background = SoftRed
                )
            }
        }
    }
}

@Composable
private fun StatBox(
    modifier: Modifier,
    icon: ImageVector,
    label: String,
    value: Int,
    color: Color,
    darkColor: Color,
    background: Color
) {
    Surface(
        modifier = modifier.height(96.dp),
        shape = RoundedCornerShape(20.dp),
        color = background,
        border = BorderStroke(
            width = 1.4.dp,
            color = color.copy(alpha = 0.38f)
        ),
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(46.dp),
                shape = CircleShape,
                color = color
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(27.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = value.toString(),
                    color = darkColor,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    lineHeight = 30.sp
                )

                Text(
                    text = label,
                    color = darkColor,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun ProgressFinalCard(
    progreso: Int
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = SoftBlue,
        border = BorderStroke(1.dp, Color(0xFFC7DBFF))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(42.dp),
                    shape = CircleShape,
                    color = StrongBlue
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.TrendingUp,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(25.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = "Progreso actualizado",
                    color = TextDark,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = "$progreso%",
                    color = StrongBlue,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            LinearProgressIndicator(
                progress = { (progreso / 100f).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(9.dp)
                    .clip(RoundedCornerShape(30.dp)),
                color = StrongBlue,
                trackColor = Color(0xFFDDE3EB)
            )
        }
    }
}

private enum class ResultButtonStyle {
    PRIMARY,
    SECONDARY_BLUE,
    SECONDARY_NEUTRAL
}

@Composable
private fun ResultActionButton(
    text: String,
    icon: ImageVector,
    style: ResultButtonStyle,
    onClick: () -> Unit
) {
    val containerColor = when (style) {
        ResultButtonStyle.PRIMARY -> StrongBlue
        ResultButtonStyle.SECONDARY_BLUE -> SoftBlue
        ResultButtonStyle.SECONDARY_NEUTRAL -> Color.White
    }

    val contentColor = when (style) {
        ResultButtonStyle.PRIMARY -> Color.White
        ResultButtonStyle.SECONDARY_BLUE -> StrongBlue
        ResultButtonStyle.SECONDARY_NEUTRAL -> TextDark
    }

    val borderColor = when (style) {
        ResultButtonStyle.PRIMARY -> StrongBlue
        ResultButtonStyle.SECONDARY_BLUE -> StrongBlue.copy(alpha = 0.35f)
        ResultButtonStyle.SECONDARY_NEUTRAL -> Color(0xFFE5E7EB)
    }

    val iconBackgroundColor = when (style) {
        ResultButtonStyle.PRIMARY -> Color.White.copy(alpha = 0.18f)
        ResultButtonStyle.SECONDARY_BLUE -> Color.White
        ResultButtonStyle.SECONDARY_NEUTRAL -> SoftBlue
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp),
        shape = RoundedCornerShape(17.dp),
        color = containerColor,
        border = BorderStroke(
            width = 1.5.dp,
            color = borderColor
        ),
        shadowElevation = if (style == ResultButtonStyle.PRIMARY) 4.dp else 1.dp,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Surface(
                modifier = Modifier.size(34.dp),
                shape = CircleShape,
                color = iconBackgroundColor
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = contentColor,
                        modifier = Modifier.size(21.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = text,
                color = contentColor,
                fontSize = 17.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun EmptyResultState(
    onBackToStudy: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text = "No hay resumen disponible",
            color = TextDark,
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Inicia una sesión de tarjetas para ver tus resultados.",
            color = TextMuted,
            fontSize = 16.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(28.dp))

        Button(
            onClick = onBackToStudy,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = StrongBlue,
                contentColor = Color.White
            )
        ) {
            Text(
                text = "Volver a modo estudio",
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}