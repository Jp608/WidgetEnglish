package com.jp.widgetenglish.features.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider as MaterialVerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.jp.widgetenglish.data.local.datastore.LearningPreferences
import com.jp.widgetenglish.data.local.datastore.LearningSettings
import com.jp.widgetenglish.data.local.datastore.ModoSeleccionContenido
import com.jp.widgetenglish.features.common.AppBottomBar
import com.jp.widgetenglish.features.profile.viewmodel.ProfileViewModel
import com.widgetenglish.app.ui.Screen
import kotlin.math.roundToInt
import androidx.compose.material.icons.filled.BarChart
private val PrimaryBlue = Color(0xFF1565C0)
private val DarkBlue = Color(0xFF1A237E)
private val SoftBlue = Color(0xFFE3F2FD)
private val ScreenBackground = Color(0xFFF5F7FB)
private val TextDark = Color(0xFF1F2937)
private val TextMuted = Color(0xFF6B7280)

@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = viewModel(),
    authViewModel: com.jp.widgetenglish.features.auth.viewmodel.AuthViewModel
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.cargarDatosUsuario()
        viewModel.cargarConfiguracionAprendizaje(context)
    }

    var showLogoutDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var unavailableOptionTitle by remember { mutableStateOf<String?>(null) }

    fun navegar(route: String) {
        navController.navigate(route) {
            launchSingleTop = true
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Filled.Logout,
                    contentDescription = null,
                    tint = PrimaryBlue
                )
            },
            title = {
                Text(
                    text = "¿Cerrar sesión?",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "¿Estás seguro de que deseas cerrar sesión?",
                    textAlign = TextAlign.Center
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false

                        authViewModel.cerrarSesion()

                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryBlue
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Cerrar sesión")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showLogoutDialog = false },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Cancelar")
                }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Filled.DeleteForever,
                    contentDescription = null,
                    tint = Color.Red
                )
            },
            title = {
                Text(
                    text = "Eliminación no disponible",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Por ahora esta pantalla no elimina la cuenta ni el progreso. Para proteger tus datos, esta acción queda desactivada hasta implementar la eliminación real con Firebase.",
                    textAlign = TextAlign.Center
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryBlue
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Entendido",
                        color = Color.White
                    )
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showDeleteDialog = false },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Cancelar")
                }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }

    unavailableOptionTitle?.let { title ->
        AlertDialog(
            onDismissRequest = { unavailableOptionTitle = null },
            title = {
                Text(
                    text = "$title no disponible",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Esta sección todavía no está implementada.",
                    textAlign = TextAlign.Center
                )
            },
            confirmButton = {
                Button(
                    onClick = { unavailableOptionTitle = null },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryBlue
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Entendido")
                }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }

    Scaffold(
        bottomBar = {
            AppBottomBar(
                selectedRoute = Screen.Profile.route,
                onInicioClick = { navegar(Screen.Home.route) },
                onVocabularioClick = { navegar(Screen.Vocabulario.route) },
                onLotesClick = { navegar(Screen.Lotes.route) },
                onEstudioClick = { navegar(Screen.Estudio.route) },
                onIaClick = { navegar(Screen.Ia.route) },
                onPerfilClick = { navegar(Screen.Profile.route) }
            )
        },
        containerColor = ScreenBackground
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
                .background(ScreenBackground)
                .verticalScroll(rememberScrollState())
        ) {
            ProfileHeader(
                nombre = uiState.usuario?.nombre ?: "Usuario",
                correo = uiState.usuario?.correo ?: "Invitado"
            )

            Spacer(modifier = Modifier.height(20.dp))

            Column(
                modifier = Modifier.padding(horizontal = 20.dp)
            ) {
                StatsCard(
                    aprendidas = "${uiState.usuario?.palabrasAprendidas ?: 0}",
                    lotes = "${uiState.usuario?.lotesCompletados ?: 0}",
                    racha = "${uiState.usuario?.rachaActual ?: 0}"
                )

                Spacer(modifier = Modifier.height(22.dp))

                SectionTitle("Aprendizaje")

                Spacer(modifier = Modifier.height(8.dp))

                LearningSettingsCard(
                    settings = uiState.learningSettings,
                    onModoChanged = { modo ->
                        viewModel.cambiarModoSeleccionContenido(
                            context = context,
                            modo = modo
                        )
                    },
                    onAutomaticoChanged = { automatico ->
                        viewModel.cambiarObjetivoDiarioAutomatico(
                            context = context,
                            automatico = automatico
                        )
                    },
                    onObjetivoManualChanged = { objetivo ->
                        viewModel.cambiarObjetivoDiarioManual(
                            context = context,
                            objetivo = objetivo
                        )
                    },
                    onReset = {
                        viewModel.reiniciarConfiguracionAprendizaje(context)
                    }
                )

                Spacer(modifier = Modifier.height(22.dp))

                SectionTitle("Mi cuenta")

                Spacer(modifier = Modifier.height(8.dp))

                ProfileOption(
                    icon = Icons.Filled.Settings,
                    label = "Configuración general",
                    subtitle = "Preferencias de la aplicación",
                    iconColor = PrimaryBlue
                ) {
                    unavailableOptionTitle = "Configuración general"
                }

                ProfileOption(
                    icon = Icons.Filled.Help,
                    label = "Ayuda y soporte",
                    subtitle = "Preguntas frecuentes y contacto",
                    iconColor = PrimaryBlue
                ) {
                    unavailableOptionTitle = "Ayuda y soporte"
                }

                Spacer(modifier = Modifier.height(22.dp))

                SectionTitle("Sesión")

                Spacer(modifier = Modifier.height(8.dp))

                ProfileOption(
                    icon = Icons.Filled.Logout,
                    label = "Cerrar sesión",
                    subtitle = "Salir de tu cuenta actual",
                    iconColor = PrimaryBlue
                ) {
                    showLogoutDialog = true
                }

                ProfileOption(
                    icon = Icons.Filled.BarChart,
                    label = "Estadísticas",
                    subtitle = "Consulta tu progreso, racha y aprendizaje",
                    iconColor = PrimaryBlue
                ) {
                    navegar(Screen.Statistics.route)
                }

                Spacer(modifier = Modifier.height(8.dp))

                ProfileOption(
                    icon = Icons.Filled.DeleteForever,
                    label = "Eliminar cuenta",
                    subtitle = "Borrar cuenta y progreso",
                    iconColor = Color.Red
                ) {
                    showDeleteDialog = true
                }

                uiState.error?.let { error ->
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = error,
                        color = Color.Red,
                        fontSize = 13.sp,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Composable
private fun ProfileHeader(
    nombre: String,
    correo: String
) {
    val blueGradient = listOf(
        DarkBlue,
        PrimaryBlue,
        Color(0xFF0288D1)
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(230.dp)
            .clip(RoundedCornerShape(bottomStart = 54.dp, bottomEnd = 54.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = blueGradient
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier
                    .size(86.dp)
                    .clip(CircleShape),
                color = Color.White.copy(alpha = 0.22f)
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = "Avatar",
                        modifier = Modifier.size(46.dp),
                        tint = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = nombre,
                fontSize = 21.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = correo,
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.82f)
            )
        }
    }
}

@Composable
private fun StatsCard(
    aprendidas: String,
    lotes: String,
    racha: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 18.dp, horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProfileStat(
                value = aprendidas,
                label = "Aprendidas",
                icon = Icons.Filled.School
            )

            MaterialVerticalDivider(
                modifier = Modifier.height(42.dp),
                color = Color(0xFFE5E7EB)
            )

            ProfileStat(
                value = lotes,
                label = "Lotes",
                icon = Icons.Filled.Layers
            )

            MaterialVerticalDivider(
                modifier = Modifier.height(42.dp),
                color = Color(0xFFE5E7EB)
            )

            ProfileStat(
                value = racha,
                label = "Racha",
                icon = Icons.Filled.Whatshot
            )
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold,
        color = TextMuted
    )
}

@Composable
private fun LearningSettingsCard(
    settings: LearningSettings,
    onModoChanged: (ModoSeleccionContenido) -> Unit,
    onAutomaticoChanged: (Boolean) -> Unit,
    onObjetivoManualChanged: (Int) -> Unit,
    onReset: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(42.dp),
                    shape = CircleShape,
                    color = SoftBlue
                ) {
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.School,
                            contentDescription = null,
                            tint = PrimaryBlue,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Algoritmo de aprendizaje",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )

                    Text(
                        text = "Controla cómo se eligen las palabras del widget.",
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "Modo de selección",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextDark
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ModeOptionButton(
                    modifier = Modifier.weight(1f),
                    label = "Secuencial",
                    selected = settings.modoSeleccionContenido == ModoSeleccionContenido.SECUENCIAL,
                    onClick = {
                        onModoChanged(ModoSeleccionContenido.SECUENCIAL)
                    }
                )

                ModeOptionButton(
                    modifier = Modifier.weight(1f),
                    label = "Aleatorio",
                    selected = settings.modoSeleccionContenido == ModoSeleccionContenido.ALEATORIO,
                    onClick = {
                        onModoChanged(ModoSeleccionContenido.ALEATORIO)
                    }
                )

                ModeOptionButton(
                    modifier = Modifier.weight(1f),
                    label = "IA",
                    selected = settings.modoSeleccionContenido == ModoSeleccionContenido.INTELIGENTE,
                    onClick = {
                        onModoChanged(ModoSeleccionContenido.INTELIGENTE)
                    }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = descripcionModo(settings.modoSeleccionContenido),
                fontSize = 12.sp,
                color = TextMuted,
                lineHeight = 17.sp
            )

            Spacer(modifier = Modifier.height(18.dp))

            HorizontalDivider(
                color = Color(0xFFE5E7EB)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Objetivo automático",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextDark
                    )

                    Text(
                        text = "La app ajusta tu meta diaria entre 5 y 15 palabras.",
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                }

                Switch(
                    checked = settings.objetivoDiarioAutomatico,
                    onCheckedChange = onAutomaticoChanged,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = PrimaryBlue,
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = Color(0xFFCBD5E1)
                    )
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = SoftBlue.copy(alpha = 0.75f)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = if (settings.objetivoDiarioAutomatico) {
                                    "Meta inteligente actual"
                                } else {
                                    "Palabras por sesión"
                                },
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextDark
                            )

                            Text(
                                text = if (settings.objetivoDiarioAutomatico) {
                                    "Se ajustará según tu desempeño."
                                } else {
                                    "Tú decides cuántas palabras ver."
                                },
                                fontSize = 12.sp,
                                color = TextMuted
                            )
                        }

                        Text(
                            text = "${settings.objetivoEfectivo}",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBlue
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (!settings.objetivoDiarioAutomatico) {
                        Slider(
                            value = settings.objetivoDiarioManual.toFloat(),
                            onValueChange = { value ->
                                onObjetivoManualChanged(value.roundToInt())
                            },
                            valueRange = LearningPreferences.MIN_OBJETIVO_DIARIO.toFloat()..
                                    LearningPreferences.MAX_OBJETIVO_DIARIO.toFloat(),
                            steps = 9
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${LearningPreferences.MIN_OBJETIVO_DIARIO}",
                                fontSize = 11.sp,
                                color = TextMuted
                            )

                            Text(
                                text = "${LearningPreferences.MAX_OBJETIVO_DIARIO}",
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedButton(
                onClick = onReset,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Restablecer configuración inteligente")
            }
        }
    }
}

@Composable
private fun ModeOptionButton(
    modifier: Modifier = Modifier,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    if (selected) {
        Button(
            onClick = onClick,
            modifier = modifier.height(40.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = PrimaryBlue,
                contentColor = Color.White
            ),
            contentPadding = ButtonDefaults.ContentPadding
        ) {
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    } else {
        OutlinedButton(
            onClick = onClick,
            modifier = modifier.height(40.dp),
            shape = RoundedCornerShape(14.dp),
            contentPadding = ButtonDefaults.ContentPadding
        ) {
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

private fun descripcionModo(
    modo: ModoSeleccionContenido
): String {
    return when (modo) {
        ModoSeleccionContenido.SECUENCIAL ->
            "Muestra el contenido siguiendo el orden del lote. Ideal si quieres avanzar paso a paso."

        ModoSeleccionContenido.ALEATORIO ->
            "Mezcla las palabras del lote y mantiene el mismo orden durante el día."

        ModoSeleccionContenido.INTELIGENTE ->
            "Prioriza palabras no vistas, difíciles o en progreso, y repasa aprendidas con menor frecuencia."
    }
}

@Composable
fun ProfileStat(
    value: String,
    label: String,
    icon: ImageVector
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = PrimaryBlue,
            modifier = Modifier.size(20.dp)
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = value,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = DarkBlue
        )

        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted
        )
    }
}

@Composable
fun ProfileOption(
    icon: ImageVector,
    label: String,
    subtitle: String,
    iconColor: Color,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(38.dp),
                shape = CircleShape,
                color = if (iconColor == Color.Red) {
                    Color.Red.copy(alpha = 0.1f)
                } else {
                    SoftBlue
                }
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = iconColor,
                        modifier = Modifier.size(21.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = label,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextDark
                )

                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = TextMuted
                )
            }

            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = Color(0xFFCBD5E1)
            )
        }
    }
}
