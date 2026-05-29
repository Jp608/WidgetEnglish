package com.jp.widgetenglish.features.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.jp.widgetenglish.data.local.datastore.DailyGoalPreferences
import com.jp.widgetenglish.data.local.datastore.DailyGoalSettings
import com.jp.widgetenglish.data.local.datastore.LearningPreferences
import com.jp.widgetenglish.data.local.datastore.LearningSettings
import com.jp.widgetenglish.data.local.datastore.ModoSeleccionContenido
import com.jp.widgetenglish.features.common.AppBottomBar
import com.jp.widgetenglish.features.profile.viewmodel.ProfileViewModel
import com.widgetenglish.app.ui.Screen
import kotlin.math.roundToInt
import androidx.compose.material.icons.filled.BarChart

private val PrimaryBlue = Color(0xFF1565FF)
private val DeepBlue = Color(0xFF08145F)
private val DarkBlue = Color(0xFF102A7A)
private val AccentPurple = Color(0xFF7A5CFF)
private val SoftBlue = Color(0xFFEAF2FF)
private val SoftPurple = Color(0xFFF0EBFF)
private val SoftOrange = Color(0xFFFFF0E6)
private val ScreenBackground = Color(0xFFF5F7FC)
private val TextDark = Color(0xFF1F2937)
private val TextMuted = Color(0xFF6B7280)
private val BorderSoft = Color(0xFFE5EAF3)
private val DangerRed = Color(0xFFEF4444)

private fun profileGradient(): Brush = Brush.horizontalGradient(
    colors = listOf(
        Color(0xFF0072FF),
        Color(0xFF1565FF),
        AccentPurple
    )
)

private fun primaryButtonGradient(): Brush = Brush.horizontalGradient(
    colors = listOf(
        Color(0xFF0057E7),
        PrimaryBlue,
        AccentPurple
    )
)

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
        viewModel.cargarConfiguracionObjetivoDiario(context)
    }

    var showLogoutDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var unavailableOptionTitle by remember { mutableStateOf<String?>(null) }
    var objetivoDiarioExpanded by remember { mutableStateOf(false) }
    var aprendizajeExpanded by remember { mutableStateOf(false) }

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

            Spacer(modifier = Modifier.height(14.dp))

            Column(
                modifier = Modifier.padding(horizontal = 20.dp)
            ) {
                StatsCard(
                    aprendidas = "${uiState.usuario?.palabrasAprendidas ?: 0}",
                    lotes = "${uiState.usuario?.lotesCompletados ?: 0}",
                    racha = "${uiState.usuario?.rachaActual ?: 0}"
                )

                Spacer(modifier = Modifier.height(22.dp))

                SectionTitle("Objetivo diario")

                Spacer(modifier = Modifier.height(8.dp))

                DailyGoalSettingsCard(
                    settings = uiState.dailyGoalSettings,
                    expanded = objetivoDiarioExpanded,
                    onToggleExpanded = {
                        objetivoDiarioExpanded = !objetivoDiarioExpanded
                    },
                    onSave = { automatico, objetivo ->
                        if (automatico) {
                            viewModel.cambiarModoObjetivoDiario(
                                context = context,
                                automatico = true
                            )
                        } else {
                            viewModel.cambiarObjetivoDiarioUsuario(
                                context = context,
                                objetivo = objetivo
                            )
                        }
                        objetivoDiarioExpanded = false
                    },
                    onReset = {
                        viewModel.reiniciarObjetivoDiario(context)
                    }
                )

                Spacer(modifier = Modifier.height(22.dp))

                SectionTitle("Aprendizaje")

                Spacer(modifier = Modifier.height(8.dp))

                LearningSettingsCard(
                    settings = uiState.learningSettings,
                    expanded = aprendizajeExpanded,
                    onToggleExpanded = {
                        aprendizajeExpanded = !aprendizajeExpanded
                    },
                    onSave = { modo, automatico, objetivo ->
                        viewModel.guardarConfiguracionAprendizaje(
                            context = context,
                            modo = modo,
                            automatico = automatico,
                            objetivo = objetivo
                        )
                        aprendizajeExpanded = false
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
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(175.dp)
            .clip(RoundedCornerShape(bottomStart = 34.dp, bottomEnd = 34.dp))
            .background(profileGradient())
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(78.dp)
                .align(Alignment.CenterStart)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.20f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Person,
                contentDescription = "Avatar",
                tint = Color.White,
                modifier = Modifier.size(42.dp)
            )
        }

        Surface(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 26.dp)
                .size(34.dp),
            shape = RoundedCornerShape(12.dp),
            color = Color.White.copy(alpha = 0.16f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = "Editar perfil",
                    tint = Color.White,
                    modifier = Modifier.size(19.dp)
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 96.dp, end = 46.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = nombre,
                fontSize = 19.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(5.dp))

            Text(
                text = correo,
                fontSize = 12.5.sp,
                color = Color.White.copy(alpha = 0.88f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
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
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp, horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProfileStat(
                value = aprendidas,
                label = "Aprendidas",
                icon = Icons.Filled.School,
                iconColor = PrimaryBlue,
                iconBg = SoftBlue
            )

            MaterialVerticalDivider(
                modifier = Modifier.height(58.dp),
                color = BorderSoft
            )

            ProfileStat(
                value = lotes,
                label = "Lotes",
                icon = Icons.Filled.Layers,
                iconColor = AccentPurple,
                iconBg = SoftPurple
            )

            MaterialVerticalDivider(
                modifier = Modifier.height(58.dp),
                color = BorderSoft
            )

            ProfileStat(
                value = racha,
                label = "Racha",
                icon = Icons.Filled.Whatshot,
                iconColor = Color(0xFFF97316),
                iconBg = SoftOrange
            )
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    val icon = when (text) {
        "Objetivo diario" -> Icons.Filled.Whatshot
        "Aprendizaje" -> Icons.Filled.School
        "Mi cuenta" -> Icons.Filled.Person
        "Sesión" -> Icons.Filled.Logout
        else -> Icons.Filled.Settings
    }

    val subtitle = when (text) {
        "Objetivo diario" -> "Define tu meta y mantén el enfoque cada día."
        "Aprendizaje" -> "Personaliza cómo se muestra el contenido y la cantidad."
        "Mi cuenta" -> "Gestiona tu cuenta y preferencias de la aplicación."
        "Sesión" -> "Opciones relacionadas con tu cuenta y progreso."
        else -> ""
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (text == "Sesión") Color(0xFF22C55E) else PrimaryBlue,
            modifier = Modifier
                .padding(top = 2.dp)
                .size(24.dp)
        )

        Spacer(modifier = Modifier.width(10.dp))

        Column {
            Text(
                text = text,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextDark
            )

            if (subtitle.isNotBlank()) {
                Text(
                    text = subtitle,
                    fontSize = 13.sp,
                    color = TextMuted,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
private fun DailyGoalSettingsCard(
    settings: DailyGoalSettings,
    expanded: Boolean,
    onToggleExpanded: () -> Unit,
    onSave: (Boolean, Int) -> Unit,
    onReset: () -> Unit
) {
    var automaticoPendiente by remember(settings.automatico) {
        mutableStateOf(settings.automatico)
    }

    var objetivoPendiente by remember(settings.objetivoManual) {
        mutableStateOf(settings.objetivoManual)
    }

    val objetivoMostrado = if (automaticoPendiente) {
        settings.objetivoAutomaticoActual
    } else {
        objetivoPendiente
    }

    val hayCambios = automaticoPendiente != settings.automatico ||
            (!automaticoPendiente && objetivoPendiente != settings.objetivoManual)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggleExpanded),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RoundIconBubble(
                    icon = Icons.Filled.Whatshot,
                    iconColor = PrimaryBlue,
                    background = SoftBlue
                )

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Meta diaria",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextDark
                    )

//                    Text(
//                        text = "Ajusta cuántas palabras quieres estudiar por día.",
//                        fontSize = 13.sp,
//                        color = TextMuted,
//                        lineHeight = 18.sp
//                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "$objetivoMostrado",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = PrimaryBlue
                    )
                    Text(
                        text = "palabras",
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Icon(
                    imageVector = if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.ChevronRight,
                    contentDescription = null,
                    tint = TextMuted
                )
            }

            if (expanded) {
                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    LargeModeCard(
                        modifier = Modifier.weight(1f),
                        title = "Automático",
                        subtitle = "La app ajusta tu meta",
                        icon = Icons.Filled.Settings,
                        selected = automaticoPendiente,
                        onClick = { automaticoPendiente = true }
                    )

                    LargeModeCard(
                        modifier = Modifier.weight(1f),
                        title = "Manual",
                        subtitle = "Elige tu meta diaria",
                        icon = Icons.Filled.Person,
                        selected = !automaticoPendiente,
                        onClick = { automaticoPendiente = false }
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (!automaticoPendiente) {
                    DailyGoalOptions(
                        objetivoActual = objetivoPendiente,
                        onObjetivoManualChanged = { objetivo ->
                            objetivoPendiente = objetivo
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                }

                Text(
                    text = if (automaticoPendiente) {
                        "La app inicia en 5 palabras y ajusta la meta entre 5 y 15 según tu desempeño."
                    } else {
                        "Elige cuántas palabras quieres aprender cada día."
                    },
                    fontSize = 13.sp,
                    color = TextMuted,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(18.dp))

                ProfileActionButtons(
                    resetText = "Restablecer",
                    saveText = "Guardar",
                    saveEnabled = hayCambios,
                    onReset = {
                        automaticoPendiente = true
                        objetivoPendiente = DailyGoalPreferences.OBJETIVO_MANUAL_INICIAL
                        onReset()
                    },
                    onSave = {
                        onSave(automaticoPendiente, objetivoMostrado)
                    }
                )
            }
        }
    }
}

@Composable
private fun DailyGoalOptions(
    objetivoActual: Int,
    onObjetivoManualChanged: (Int) -> Unit
) {
    val opciones = DailyGoalPreferences.OBJETIVOS_MANUALES
    val filas = opciones.chunked(4)

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        filas.forEach { fila ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                fila.forEach { objetivo ->
                    CompactNumberChip(
                        modifier = Modifier.weight(1f),
                        label = "$objetivo",
                        selected = objetivoActual == objetivo,
                        onClick = { onObjetivoManualChanged(objetivo) }
                    )
                }

                repeat(4 - fila.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun LearningSettingsCard(
    settings: LearningSettings,
    expanded: Boolean,
    onToggleExpanded: () -> Unit,
    onSave: (ModoSeleccionContenido, Boolean, Int) -> Unit,
    onReset: () -> Unit
) {
    var modoPendiente by remember(settings.modoSeleccionContenido) {
        mutableStateOf(settings.modoSeleccionContenido)
    }

    var automaticoPendiente by remember(settings.objetivoDiarioAutomatico) {
        mutableStateOf(settings.objetivoDiarioAutomatico)
    }

    var objetivoPendiente by remember(settings.objetivoDiarioManual) {
        mutableStateOf(settings.objetivoDiarioManual)
    }

    val objetivoMostrado = if (automaticoPendiente) {
        settings.objetivoDiarioActual
    } else {
        objetivoPendiente
    }

    val hayCambios = modoPendiente != settings.modoSeleccionContenido ||
            automaticoPendiente != settings.objetivoDiarioAutomatico ||
            (!automaticoPendiente && objetivoPendiente != settings.objetivoDiarioManual)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggleExpanded),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RoundIconBubble(
                    icon = Icons.Filled.Layers,
                    iconColor = PrimaryBlue,
                    background = SoftBlue
                )

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Algoritmo de aprendizaje",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextDark
                    )

                    Text(
                        text = "Widget.",
                        fontSize = 13.sp,
                        color = TextMuted,
                        lineHeight = 18.sp
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "$objetivoMostrado",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = PrimaryBlue
                    )
                    Text(
                        text = "palabras",
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Icon(
                    imageVector = if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.ChevronRight,
                    contentDescription = null,
                    tint = TextMuted
                )
            }

            if (expanded) {
                Spacer(modifier = Modifier.height(18.dp))

                SegmentedModeSelector(
                    selectedMode = modoPendiente,
                    onModeSelected = { modoPendiente = it }
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = descripcionModo(modoPendiente),
                    fontSize = 13.sp,
                    color = TextMuted,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(18.dp))

                HorizontalDivider(color = BorderSoft)

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Cantidad automática",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextDark
                        )

                        Text(
                            text = "Deja que la app ajuste la cantidad por sesión según tu progreso.",
                            fontSize = 12.sp,
                            color = TextMuted,
                            lineHeight = 17.sp
                        )
                    }

                    Switch(
                        checked = automaticoPendiente,
                        onCheckedChange = { automaticoPendiente = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = PrimaryBlue,
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = Color(0xFFCBD5E1)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                SessionAmountPanel(
                    automatico = automaticoPendiente,
                    objetivoMostrado = objetivoMostrado,
                    objetivoPendiente = objetivoPendiente,
                    onObjetivoChanged = { objetivoPendiente = it }
                )

                Spacer(modifier = Modifier.height(18.dp))

                ProfileActionButtons(
                    resetText = "Restablecer",
                    saveText = "Guardar",
                    saveEnabled = hayCambios,
                    onReset = onReset,
                    onSave = {
                        onSave(modoPendiente, automaticoPendiente, objetivoPendiente)
                    }
                )
            }
        }
    }
}

@Composable
private fun LegacyLearningSettingsCard(
    settings: LearningSettings,
    expanded: Boolean,
    onToggleExpanded: () -> Unit,
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
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggleExpanded),
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

                Icon(
                    imageVector = if (expanded) {
                        Icons.Filled.KeyboardArrowUp
                    } else {
                        Icons.Filled.KeyboardArrowDown
                    },
                    contentDescription = null,
                    tint = TextMuted
                )
            }

            if (expanded) {
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
}

@Composable
private fun ModeOptionButton(
    modifier: Modifier = Modifier,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    CompactNumberChip(
        modifier = modifier,
        label = label,
        selected = selected,
        onClick = onClick
    )
}

@Composable
private fun RoundIconBubble(
    icon: ImageVector,
    iconColor: Color,
    background: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.size(54.dp),
        shape = CircleShape,
        color = background
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(27.dp)
            )
        }
    }
}

@Composable
private fun LargeModeCard(
    modifier: Modifier,
    title: String,
    subtitle: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .heightIn(min = 70.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = if (selected) SoftBlue.copy(alpha = 0.55f) else Color.White,
        border = BorderStroke(
            width = if (selected) 1.4.dp else 1.dp,
            color = if (selected) PrimaryBlue else BorderSoft
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected) PrimaryBlue else TextMuted,
                modifier = Modifier.size(25.dp)
            )

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = if (selected) PrimaryBlue else TextDark,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = subtitle,
                    color = TextMuted,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun CompactNumberChip(
    modifier: Modifier = Modifier,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(15.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(15.dp),
        color = if (selected) PrimaryBlue else Color.White,
        border = BorderStroke(
            width = if (selected) 0.dp else 1.dp,
            color = if (selected) Color.Transparent else BorderSoft
        ),
        shadowElevation = if (selected) 2.dp else 1.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = label,
                color = if (selected) Color.White else TextDark,
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun SegmentedModeSelector(
    selectedMode: ModoSeleccionContenido,
    onModeSelected: (ModoSeleccionContenido) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = BorderStroke(1.dp, BorderSoft)
    ) {
        Row(modifier = Modifier.padding(3.dp)) {
            CompactSegment(
                modifier = Modifier.weight(1f),
                label = "Secuencia",
                selected = selectedMode == ModoSeleccionContenido.SECUENCIAL,
                onClick = { onModeSelected(ModoSeleccionContenido.SECUENCIAL) }
            )

            CompactSegment(
                modifier = Modifier.weight(1f),
                label = "Aleatorio",
                selected = selectedMode == ModoSeleccionContenido.ALEATORIO,
                onClick = { onModeSelected(ModoSeleccionContenido.ALEATORIO) }
            )

            CompactSegment(
                modifier = Modifier.weight(1f),
                label = "IA",
                selected = selectedMode == ModoSeleccionContenido.INTELIGENTE,
                onClick = { onModeSelected(ModoSeleccionContenido.INTELIGENTE) }
            )
        }
    }
}

@Composable
private fun CompactSegment(
    modifier: Modifier,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(40.dp)
            .clip(RoundedCornerShape(13.dp))
            .background(if (selected) SoftBlue else Color.Transparent)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (selected) PrimaryBlue else TextMuted,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 13.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun SessionAmountPanel(
    automatico: Boolean,
    objetivoMostrado: Int,
    objetivoPendiente: Int,
    onObjetivoChanged: (Int) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = SoftBlue.copy(alpha = 0.75f),
        border = BorderStroke(1.dp, Color(0xFFD7E8FF))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (automatico) "Cantidad actual" else "Palabras por sesión",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextDark
                    )

                    Text(
                        text = if (automatico) {
                            "Se ajustará según el desempeño."
                        } else {
                            "Ajusta cuántas palabras verás en cada sesión."
                        },
                        fontSize = 12.sp,
                        color = TextMuted,
                        lineHeight = 17.sp
                    )
                }

                Text(
                    text = "$objetivoMostrado",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = PrimaryBlue
                )
            }

            if (!automatico) {
                Spacer(modifier = Modifier.height(10.dp))

                Slider(
                    value = objetivoPendiente.toFloat(),
                    onValueChange = { value ->
                        onObjetivoChanged(value.roundToInt())
                    },
                    valueRange = LearningPreferences.MIN_OBJETIVO_DIARIO.toFloat()..
                            LearningPreferences.MAX_OBJETIVO_DIARIO.toFloat(),
                    steps = 9
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    for (value in LearningPreferences.MIN_OBJETIVO_DIARIO..LearningPreferences.MAX_OBJETIVO_DIARIO) {
                        Text(
                            text = "$value",
                            fontSize = 10.sp,
                            color = if (value == objetivoPendiente) PrimaryBlue else TextMuted,
                            fontWeight = if (value == objetivoPendiente) FontWeight.ExtraBold else FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileActionButtons(
    resetText: String,
    saveText: String,
    saveEnabled: Boolean,
    onReset: () -> Unit,
    onSave: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedButton(
            onClick = onReset,
            modifier = Modifier
                .weight(1f)
                .height(50.dp),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.2.dp, PrimaryBlue)
        ) {
            Text(
                text = resetText,
                color = PrimaryBlue,
                fontWeight = FontWeight.ExtraBold
            )
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .height(50.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    brush = if (saveEnabled) {
                        primaryButtonGradient()
                    } else {
                        Brush.horizontalGradient(listOf(Color(0xFFE5E7EB), Color(0xFFE5E7EB)))
                    }
                )
                .clickable(enabled = saveEnabled) { onSave() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = saveText,
                color = if (saveEnabled) Color.White else Color(0xFF9CA3AF),
                fontWeight = FontWeight.ExtraBold,
                fontSize = 15.sp
            )
        }
    }
}

private fun descripcionModo(
    modo: ModoSeleccionContenido
): String {
    return when (modo) {
        ModoSeleccionContenido.SECUENCIAL ->
            "Avanza por bloques diarios en el orden del lote y reemplaza aprendidas por pendientes."

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
    icon: ImageVector,
    iconColor: Color = PrimaryBlue,
    iconBg: Color = SoftBlue
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            modifier = Modifier.size(46.dp),
            shape = RoundedCornerShape(16.dp),
            color = iconBg
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(25.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(7.dp))

        Text(
            text = value,
            fontSize = 25.sp,
            fontWeight = FontWeight.ExtraBold,
            color = DarkBlue
        )

        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = TextMuted,
            fontWeight = FontWeight.SemiBold
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
    val isDanger = iconColor == Color.Red || iconColor == DangerRed

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDanger) Color(0xFFFFF1F2) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(46.dp),
                shape = RoundedCornerShape(15.dp),
                color = if (isDanger) Color(0xFFFFE2E2) else SoftBlue
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = if (isDanger) DangerRed else iconColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextDark
                )

                Text(
                    text = subtitle,
                    fontSize = 13.sp,
                    color = TextMuted,
                    lineHeight = 18.sp
                )
            }

            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = Color(0xFFCBD5E1),
                modifier = Modifier.size(26.dp)
            )
        }
    }
}
