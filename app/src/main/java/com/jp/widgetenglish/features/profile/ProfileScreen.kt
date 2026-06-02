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
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.style.TextDecoration
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
import com.jp.widgetenglish.data.local.datastore.WidgetAppearancePreferences
import com.jp.widgetenglish.data.local.datastore.WidgetAppearanceSettings
import com.jp.widgetenglish.data.local.datastore.WidgetColorTheme
import com.jp.widgetenglish.data.local.datastore.WidgetLayoutSizeOption
import com.jp.widgetenglish.data.local.datastore.WidgetTextSizeOption
import com.jp.widgetenglish.data.local.datastore.WidgetVisualStyle
import com.jp.widgetenglish.features.auth.TermsAndConditionsInfoDialog
import com.jp.widgetenglish.features.common.AppBottomBar
import com.jp.widgetenglish.features.common.USER_DISPLAY_NAME_MAX_LENGTH
import com.jp.widgetenglish.features.common.USER_DISPLAY_NAME_MIN_LENGTH
import com.jp.widgetenglish.features.profile.viewmodel.ProfileConfirmation
import com.jp.widgetenglish.features.profile.viewmodel.ProfileConfirmationTarget
import com.jp.widgetenglish.features.profile.viewmodel.ProfileViewModel
import com.widgetenglish.app.ui.Screen
import kotlin.math.roundToInt
import androidx.compose.material.icons.filled.BarChart
import kotlinx.coroutines.delay

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
private val TextSecondaryStrong = Color(0xFF4B5563)
private val BorderSoft = Color(0xFFE5EAF3)
private val DangerRed = Color(0xFFEF4444)
private val DialogContainer = Color(0xFF24262D)
private val DialogTextPrimary = Color(0xFFF8FAFC)
private val DialogTextSecondary = Color(0xFFCBD5E1)
private val DialogDivider = Color(0xFF94A3B8)
private val DialogResetButton = Color(0xFF2563EB)
private val DialogCancelRed = Color(0xFFF87171)

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
        viewModel.cargarAparienciaWidget(context)
    }

    var showLogoutDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showWidgetCustomizationDialog by remember { mutableStateOf(false) }
    var showTermsInfoDialog by remember { mutableStateOf(false) }
    var showEditNameDialog by remember { mutableStateOf(false) }
    var unavailableOptionTitle by remember { mutableStateOf<String?>(null) }
    var objetivoDiarioExpanded by remember { mutableStateOf(false) }
    var aprendizajeExpanded by remember { mutableStateOf(false) }
    val nombreVisible = uiState.usuario?.nombre.orEmpty()
    val correoVisible = uiState.usuario?.correo ?: "Invitado"

    LaunchedEffect(uiState.cuentaEliminada) {
        if (uiState.cuentaEliminada) {
            authViewModel.cerrarSesion()

            navController.navigate(Screen.Login.route) {
                popUpTo(0) {
                    inclusive = true
                }
                launchSingleTop = true
            }
        }
    }

    LaunchedEffect(uiState.confirmation) {
        if (uiState.confirmation != null) {
            delay(3_000)
            viewModel.limpiarMensajes()
        }
    }

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
            onDismissRequest = {
                if (!uiState.eliminandoCuenta) {
                    showDeleteDialog = false
                }
            },
            icon = {
                Icon(
                    imageVector = Icons.Filled.DeleteForever,
                    contentDescription = null,
                    tint = DangerRed
                )
            },
            title = {
                Text(
                    text = "¿Eliminar cuenta?",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Esta acción eliminará tu cuenta, tus datos personales, progreso, rachas, lotes completados, actividad y estadísticas asociadas. No se puede deshacer.",
                    textAlign = TextAlign.Center
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.eliminarCuenta(context)
                    },
                    enabled = !uiState.eliminandoCuenta,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DangerRed,
                        disabledContainerColor = Color(0xFFFCA5A5)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (uiState.eliminandoCuenta) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "Eliminar definitivamente",
                            color = Color.White
                        )
                    }
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showDeleteDialog = false },
                    enabled = !uiState.eliminandoCuenta,
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

    if (showWidgetCustomizationDialog) {
        WidgetCustomizationDialog(
            settings = uiState.widgetAppearanceSettings,
            onDismiss = { showWidgetCustomizationDialog = false },
            onSave = { settings ->
                if (settings != uiState.widgetAppearanceSettings) {
                    viewModel.guardarAparienciaWidget(
                        context = context,
                        settings = settings
                    )
                }
                showWidgetCustomizationDialog = false
            }
        )
    }

    if (showTermsInfoDialog) {
        TermsAndConditionsInfoDialog(
            onDismiss = { showTermsInfoDialog = false }
        )
    }

    if (showEditNameDialog) {
        EditProfileNameDialog(
            currentName = nombreVisible,
            isSaving = uiState.guardandoPerfil,
            message = uiState.mensaje,
            error = uiState.error,
            onDismiss = {
                if (!uiState.guardandoPerfil) {
                    viewModel.limpiarMensajes()
                    showEditNameDialog = false
                }
            },
            onSave = { nombre ->
                viewModel.actualizarNombrePerfil(nombre)
                showEditNameDialog = false
            }
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
                nombre = nombreVisible,
                correo = correoVisible,
                onEditNameClick = {
                    viewModel.limpiarMensajes()
                    showEditNameDialog = true
                }
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

                SectionTitle("Perfil personalizado")

                Spacer(modifier = Modifier.height(8.dp))

                ProfileNameCard(
                    nombre = nombreVisible,
                    correo = correoVisible,
                    isSaving = uiState.guardandoPerfil,
                    onClick = {
                        viewModel.limpiarMensajes()
                        showEditNameDialog = true
                    }
                )

                ConfirmationText(
                    confirmation = uiState.confirmation,
                    target = ProfileConfirmationTarget.NAME
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

                ConfirmationText(
                    confirmation = uiState.confirmation,
                    target = ProfileConfirmationTarget.DAILY_GOAL
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

                ConfirmationText(
                    confirmation = uiState.confirmation,
                    target = ProfileConfirmationTarget.LEARNING
                )

                Spacer(modifier = Modifier.height(22.dp))

                SectionTitle("Mi cuenta")

                Spacer(modifier = Modifier.height(8.dp))

                ProfileOption(
                    icon = Icons.Filled.Settings,
                    label = "Personalización del widget",
                    subtitle = uiState.widgetAppearanceSettings.summaryLabel(),
                    iconColor = PrimaryBlue
                ) {
                    showWidgetCustomizationDialog = true
                }

                ConfirmationText(
                    confirmation = uiState.confirmation,
                    target = ProfileConfirmationTarget.WIDGET_APPEARANCE
                )

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

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Términos y condiciones",
                    color = PrimaryBlue,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .clickable { showTermsInfoDialog = true }
                        .padding(vertical = 10.dp)
                )

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
    correo: String,
    onEditNameClick: () -> Unit
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
                .size(34.dp)
                .clickable { onEditNameClick() },
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
            if (nombre.isNotBlank()) {
                Text(
                    text = nombre,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(5.dp))
            }

            Text(
                text = correo,
                fontSize = if (nombre.isBlank()) 15.sp else 12.5.sp,
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
private fun ProfileNameCard(
    nombre: String,
    correo: String,
    isSaving: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(17.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(50.dp),
                shape = RoundedCornerShape(17.dp),
                color = SoftBlue
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = null,
                        tint = PrimaryBlue,
                        modifier = Modifier.size(25.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Nombre visible",
                    fontSize = 13.sp,
                    color = TextMuted,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = nombre.ifBlank { "Sin nombre visible" },
                    fontSize = 18.sp,
                    color = TextDark,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = correo,
                    fontSize = 12.5.sp,
                    color = TextMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    color = PrimaryBlue,
                    strokeWidth = 2.dp
                )
            } else {
                Icon(
                    imageVector = Icons.Filled.ChevronRight,
                    contentDescription = null,
                    tint = Color(0xFFCBD5E1),
                    modifier = Modifier.size(26.dp)
                )
            }
        }
    }
}

@Composable
private fun EditProfileNameDialog(
    currentName: String,
    isSaving: Boolean,
    message: String?,
    error: String?,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var name by remember(currentName) {
        mutableStateOf(currentName.take(USER_DISPLAY_NAME_MAX_LENGTH))
    }
    val cleanName = name.trim()
    val canSave = !isSaving &&
            cleanName.length in USER_DISPLAY_NAME_MIN_LENGTH..USER_DISPLAY_NAME_MAX_LENGTH &&
            cleanName != currentName.trim()

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Filled.Edit,
                contentDescription = null,
                tint = PrimaryBlue,
                modifier = Modifier.size(30.dp)
            )
        },
        title = {
            Text(
                text = "Nombre visible",
                color = TextDark,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column {
                Text(
                    text = "Este nombre se mostrará dentro de WidgetEnglish.",
                    color = TextMuted,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { value ->
                        name = value.take(USER_DISPLAY_NAME_MAX_LENGTH)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isSaving,
                    singleLine = true,
                    label = { Text("Nombre") },
                    shape = RoundedCornerShape(16.dp)
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "${name.length}/$USER_DISPLAY_NAME_MAX_LENGTH",
                    color = TextMuted,
                    fontSize = 11.sp,
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth()
                )

                val feedback = error ?: message
                if (feedback != null) {
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = feedback,
                        color = if (error != null) DangerRed else PrimaryBlue,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(cleanName) },
                enabled = canSave,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryBlue
                )
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Guardar")
                }
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                enabled = !isSaving,
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Cancelar")
            }
        },
        shape = RoundedCornerShape(24.dp)
    )
}

@Composable
private fun SectionTitle(text: String) {
    val icon = when (text) {
        "Perfil personalizado" -> Icons.Filled.Person
        "Objetivo diario" -> Icons.Filled.Whatshot
        "Aprendizaje" -> Icons.Filled.School
        "Mi cuenta" -> Icons.Filled.Person
        "Sesión" -> Icons.Filled.Logout
        else -> Icons.Filled.Settings
    }

    val subtitle = when (text) {
        "Perfil personalizado" -> "Elige cómo quieres que se muestre tu nombre en la app."
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
private fun ConfirmationText(
    confirmation: ProfileConfirmation?,
    target: ProfileConfirmationTarget
) {
    val activeConfirmation = confirmation
        ?.takeIf { it.target == target }
        ?: return

    Spacer(modifier = Modifier.height(8.dp))

    Text(
        text = activeConfirmation.text,
        color = Color(0xFF16A34A),
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center
    )
}

@Composable
private fun WidgetCustomizationDialog(
    settings: WidgetAppearanceSettings,
    onDismiss: () -> Unit,
    onSave: (WidgetAppearanceSettings) -> Unit
) {
    var colorTheme by remember(settings.colorTheme) {
        mutableStateOf(settings.colorTheme)
    }
    var visualStyle by remember(settings.visualStyle) {
        mutableStateOf(settings.visualStyle)
    }
    var textSize by remember(settings.textSize) {
        mutableStateOf(settings.textSize)
    }
    var layoutSize by remember(settings.layoutSize) {
        mutableStateOf(settings.layoutSize)
    }
    var mostrarLote by remember(settings.mostrarLote) {
        mutableStateOf(settings.mostrarLote)
    }
    var mostrarProgreso by remember(settings.mostrarProgreso) {
        mutableStateOf(settings.mostrarProgreso)
    }
    var mostrarFonetica by remember(settings.mostrarFonetica) {
        mutableStateOf(settings.mostrarFonetica)
    }
    var mostrarTraduccion by remember(settings.mostrarTraduccion) {
        mutableStateOf(settings.mostrarTraduccion)
    }

    val pendingSettings = WidgetAppearanceSettings(
        colorTheme = colorTheme,
        visualStyle = visualStyle,
        textSize = textSize,
        layoutSize = layoutSize,
        mostrarLote = mostrarLote,
        mostrarProgreso = mostrarProgreso,
        mostrarFonetica = mostrarFonetica,
        mostrarTraduccion = mostrarTraduccion
    )
    val hasChanges = pendingSettings != settings

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Personalización del widget",
                fontWeight = FontWeight.ExtraBold,
                color = DialogTextPrimary
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 620.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Vista previa",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = DialogTextPrimary
                )

                WidgetAppearancePreview(settings = pendingSettings)

                HorizontalDivider(color = DialogDivider)

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 330.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Estilo visual",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = DialogTextPrimary
                    )

                    WidgetStyleSelector(
                        selectedStyle = visualStyle,
                        onStyleSelected = { visualStyle = it }
                    )

                    Text(
                        text = "Tema de color",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = DialogTextPrimary
                    )

                    WidgetThemeSelector(
                        selectedTheme = colorTheme,
                        onThemeSelected = { colorTheme = it }
                    )

                    Text(
                        text = "Tamaño de texto",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = DialogTextPrimary
                    )

                    WidgetTextSizeSelector(
                        selectedSize = textSize,
                        onSizeSelected = { textSize = it }
                    )

                    Text(
                        text = "Tamaño del widget",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = DialogTextPrimary
                    )

                    WidgetLayoutSizeSelector(
                        selectedSize = layoutSize,
                        onSizeSelected = { layoutSize = it }
                    )

                    WidgetSwitchRow(
                        title = "Mostrar lote",
                        subtitle = "Nombre del lote activo en el encabezado.",
                        checked = mostrarLote,
                        onCheckedChange = { mostrarLote = it }
                    )

                    WidgetSwitchRow(
                        title = "Mostrar progreso",
                        subtitle = "Contador de avance dentro de la sesión.",
                        checked = mostrarProgreso,
                        onCheckedChange = { mostrarProgreso = it }
                    )

                    WidgetSwitchRow(
                        title = "Mostrar fonética",
                        subtitle = "Pronunciación escrita debajo de la palabra.",
                        checked = mostrarFonetica,
                        onCheckedChange = { mostrarFonetica = it }
                    )

                    WidgetSwitchRow(
                        title = "Mostrar traducción",
                        subtitle = "Traducción en español debajo de la palabra.",
                        checked = mostrarTraduccion,
                        onCheckedChange = { mostrarTraduccion = it }
                    )

                    OutlinedButton(
                        onClick = {
                            val defaults = WidgetAppearancePreferences.DEFAULT_SETTINGS
                            colorTheme = defaults.colorTheme
                            visualStyle = defaults.visualStyle
                            textSize = defaults.textSize
                            layoutSize = defaults.layoutSize
                            mostrarLote = defaults.mostrarLote
                            mostrarProgreso = defaults.mostrarProgreso
                            mostrarFonetica = defaults.mostrarFonetica
                            mostrarTraduccion = defaults.mostrarTraduccion
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, DialogResetButton),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = DialogResetButton.copy(alpha = 0.18f),
                            contentColor = Color.White
                        )
                    ) {
                        Text(
                            text = "Restablecer apariencia",
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(pendingSettings) },
                enabled = hasChanges,
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryBlue,
                    disabledContainerColor = Color(0xFFE5E7EB),
                    disabledContentColor = Color(0xFF9CA3AF)
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, DialogCancelRed),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = DialogCancelRed.copy(alpha = 0.14f),
                    contentColor = DialogCancelRed
                )
            ) {
                Text(
                    text = "Cancelar",
                    color = DialogCancelRed,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        },
        shape = RoundedCornerShape(24.dp),
        containerColor = DialogContainer,
        titleContentColor = DialogTextPrimary,
        textContentColor = DialogTextPrimary
    )
}

@Composable
private fun WidgetAppearancePreview(
    settings: WidgetAppearanceSettings
) {
    val colors = widgetPreviewColors(settings)
    val titleSize = when (settings.textSize) {
        WidgetTextSizeOption.COMPACTO -> 16.sp
        WidgetTextSizeOption.NORMAL -> 19.sp
        WidgetTextSizeOption.GRANDE -> 22.sp
    }
    val previewShape = when (settings.visualStyle) {
        WidgetVisualStyle.MINIMALISTA -> RoundedCornerShape(12.dp)
        WidgetVisualStyle.CARD_SUAVE -> RoundedCornerShape(24.dp)
        else -> RoundedCornerShape(18.dp)
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = previewShape,
        color = if (colors.backgroundBrush == null) colors.background else Color.Transparent,
        border = BorderStroke(1.dp, BorderSoft)
    ) {
        Column(
            modifier = if (colors.backgroundBrush == null) {
                Modifier
            } else {
                Modifier.background(colors.backgroundBrush)
            }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (colors.headerBrush == null) {
                            Modifier.background(colors.primary)
                        } else {
                            Modifier.background(colors.headerBrush)
                        }
                    )
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (settings.mostrarLote) "Emociones" else "WidgetEnglish",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = colors.onPrimary,
                    maxLines = 1
                )

                if (settings.mostrarProgreso) {
                    Text(
                        text = "3 / 10",
                        fontSize = 12.sp,
                        color = colors.onPrimary,
                        maxLines = 1
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Happy",
                    fontSize = titleSize,
                    fontWeight = FontWeight.ExtraBold,
                    color = colors.primary,
                    maxLines = 1
                )

                if (settings.mostrarFonetica) {
                    Text(
                        text = "/ˈhæpi/",
                        fontSize = 12.sp,
                        color = colors.muted,
                        maxLines = 1
                    )
                }

                if (settings.mostrarTraduccion) {
                    Text(
                        text = "Feliz",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.text,
                        maxLines = 1
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier
                            .width(56.dp)
                            .height(34.dp),
                        shape = RoundedCornerShape(15.dp),
                        color = colors.primary
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "✓",
                                color = colors.onPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }

                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(34.dp),
                        shape = RoundedCornerShape(15.dp),
                        color = colors.primary
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "Siguiente",
                                color = colors.onPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WidgetStyleSelector(
    selectedStyle: WidgetVisualStyle,
    onStyleSelected: (WidgetVisualStyle) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        WidgetVisualStyle.entries.chunked(2).forEach { rowStyles ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                rowStyles.forEach { style ->
                    WidgetStyleCard(
                        modifier = Modifier.weight(1f),
                        style = style,
                        selected = selectedStyle == style,
                        onClick = { onStyleSelected(style) }
                    )
                }

                repeat(2 - rowStyles.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun WidgetStyleCard(
    modifier: Modifier = Modifier,
    style: WidgetVisualStyle,
    selected: Boolean,
    onClick: () -> Unit
) {
    val colors = widgetPreviewColors(
        WidgetAppearanceSettings(
            colorTheme = WidgetColorTheme.AZUL,
            visualStyle = style
        )
    )

    Surface(
        modifier = modifier
            .heightIn(min = 78.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = if (selected) Color(0xFFEAF2FF) else Color.White,
        border = BorderStroke(
            width = if (selected) 1.4.dp else 1.dp,
            color = if (selected) PrimaryBlue else BorderSoft
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Surface(
                    modifier = Modifier
                        .height(8.dp)
                        .weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    color = colors.primary
                ) {}

                Surface(
                    modifier = Modifier
                        .height(8.dp)
                        .weight(0.7f),
                    shape = RoundedCornerShape(8.dp),
                    color = colors.muted.copy(alpha = 0.45f)
                ) {}
            }

            Text(
                text = style.label(),
                color = if (selected) Color(0xFF0B4FD8) else TextDark,
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = style.description(),
                color = TextSecondaryStrong,
                fontSize = 11.sp,
                lineHeight = 14.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun WidgetThemeSelector(
    selectedTheme: WidgetColorTheme,
    onThemeSelected: (WidgetColorTheme) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        WidgetColorTheme.entries.chunked(3).forEach { rowThemes ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                rowThemes.forEach { theme ->
                    WidgetThemeChip(
                        modifier = Modifier.weight(1f),
                        theme = theme,
                        selected = selectedTheme == theme,
                        onClick = { onThemeSelected(theme) }
                    )
                }

                repeat(3 - rowThemes.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun WidgetThemeChip(
    modifier: Modifier = Modifier,
    theme: WidgetColorTheme,
    selected: Boolean,
    onClick: () -> Unit
) {
    val colors = baseWidgetPreviewColors(theme)
    val swatchBrush = colors.headerBrush ?: Brush.horizontalGradient(
        colors = listOf(colors.primary, colors.primary)
    )

    Surface(
        modifier = modifier
            .height(46.dp)
            .clip(RoundedCornerShape(15.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(15.dp),
        color = if (selected) Color(0xFFEAF2FF) else Color.White,
        border = BorderStroke(
            width = if (selected) 1.5.dp else 1.dp,
            color = if (selected) PrimaryBlue else BorderSoft
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .clip(CircleShape)
                    .background(swatchBrush)
            )

            Text(
                text = theme.label(),
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (selected) Color(0xFF0B4FD8) else TextDark,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun WidgetTextSizeSelector(
    selectedSize: WidgetTextSizeOption,
    onSizeSelected: (WidgetTextSizeOption) -> Unit
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
                label = "Compacto",
                selected = selectedSize == WidgetTextSizeOption.COMPACTO,
                onClick = { onSizeSelected(WidgetTextSizeOption.COMPACTO) }
            )

            CompactSegment(
                modifier = Modifier.weight(1f),
                label = "Normal",
                selected = selectedSize == WidgetTextSizeOption.NORMAL,
                onClick = { onSizeSelected(WidgetTextSizeOption.NORMAL) }
            )

            CompactSegment(
                modifier = Modifier.weight(1f),
                label = "Grande",
                selected = selectedSize == WidgetTextSizeOption.GRANDE,
                onClick = { onSizeSelected(WidgetTextSizeOption.GRANDE) }
            )
        }
    }
}

@Composable
private fun WidgetLayoutSizeSelector(
    selectedSize: WidgetLayoutSizeOption,
    onSizeSelected: (WidgetLayoutSizeOption) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        WidgetLayoutSizeOption.entries.chunked(2).forEach { rowSizes ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                rowSizes.forEach { size ->
                    WidgetLayoutSizeCard(
                        modifier = Modifier.weight(1f),
                        size = size,
                        selected = selectedSize == size,
                        onClick = { onSizeSelected(size) }
                    )
                }

                repeat(2 - rowSizes.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun WidgetLayoutSizeCard(
    modifier: Modifier = Modifier,
    size: WidgetLayoutSizeOption,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .heightIn(min = 72.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = if (selected) Color(0xFFEAF2FF) else Color.White,
        border = BorderStroke(
            width = if (selected) 1.5.dp else 1.dp,
            color = if (selected) PrimaryBlue else BorderSoft
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Text(
                text = size.label(),
                color = if (selected) Color(0xFF0B4FD8) else TextDark,
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = size.description(),
                color = TextSecondaryStrong,
                fontSize = 11.sp,
                lineHeight = 14.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun WidgetSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                color = DialogTextPrimary
            )

            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = DialogTextSecondary,
                lineHeight = 17.sp
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = PrimaryBlue
            )
        )
    }
}

private data class WidgetPreviewColors(
    val primary: Color,
    val background: Color,
    val text: Color,
    val muted: Color,
    val soundBackground: Color,
    val onPrimary: Color = Color.White,
    val backgroundBrush: Brush? = null,
    val headerBrush: Brush? = null
)

private fun widgetPreviewColors(
    settings: WidgetAppearanceSettings
): WidgetPreviewColors {
    val baseColors = baseWidgetPreviewColors(settings.colorTheme)

    return when (settings.visualStyle) {
        WidgetVisualStyle.CLASICO -> baseColors

        WidgetVisualStyle.MINIMALISTA -> baseColors.copy(
            background = Color.White,
            text = TextDark,
            muted = TextMuted,
            backgroundBrush = null
        )

        WidgetVisualStyle.CARD_SUAVE -> baseColors.copy(
            background = baseColors.soundBackground,
            muted = baseColors.muted.copy(alpha = 0.9f)
        )

        WidgetVisualStyle.CONTRASTE_ALTO -> baseColors.copy(
            background = Color.White,
            text = Color.Black,
            muted = Color(0xFF334155),
            backgroundBrush = null,
            headerBrush = null
        )

        WidgetVisualStyle.NOCTURNO -> baseColors.copy(
            background = Color(0xFF0F172A),
            text = Color(0xFFF8FAFC),
            muted = Color(0xFFCBD5E1),
            backgroundBrush = null
        )
    }
}

private fun baseWidgetPreviewColors(
    theme: WidgetColorTheme
): WidgetPreviewColors {
    return when (theme) {
        WidgetColorTheme.AZUL -> WidgetPreviewColors(
            primary = Color(0xFF1565C0),
            background = Color.White,
            text = Color(0xFF333333),
            muted = Color(0xFF7B8EA6),
            soundBackground = Color(0xFFE3F2FD)
        )

        WidgetColorTheme.MORADO -> WidgetPreviewColors(
            primary = Color(0xFF7C3AED),
            background = Color(0xFFFBF8FF),
            text = Color(0xFF2D2145),
            muted = Color(0xFF8B7AA8),
            soundBackground = Color(0xFFF0EBFF)
        )

        WidgetColorTheme.VERDE -> WidgetPreviewColors(
            primary = Color(0xFF059669),
            background = Color(0xFFF4FFF9),
            text = Color(0xFF143D2C),
            muted = Color(0xFF6B8E7C),
            soundBackground = Color(0xFFE7F8EF)
        )

        WidgetColorTheme.NARANJA -> WidgetPreviewColors(
            primary = Color(0xFFF97316),
            background = Color(0xFFFFFBF5),
            text = Color(0xFF3F2A1D),
            muted = Color(0xFFA0795D),
            soundBackground = Color(0xFFFFF0E6)
        )

        WidgetColorTheme.TURQUESA -> WidgetPreviewColors(
            primary = Color(0xFF0891B2),
            background = Color(0xFFF0FDFF),
            text = Color(0xFF12363F),
            muted = Color(0xFF5F8792),
            soundBackground = Color(0xFFE6FAFD)
        )

        WidgetColorTheme.ROSA -> WidgetPreviewColors(
            primary = Color(0xFFDB2777),
            background = Color(0xFFFFF7FB),
            text = Color(0xFF442038),
            muted = Color(0xFFA36B8B),
            soundBackground = Color(0xFFFDE7F3)
        )

        WidgetColorTheme.INDIGO -> WidgetPreviewColors(
            primary = Color(0xFF4F46E5),
            background = Color(0xFFF8FAFF),
            text = Color(0xFF202044),
            muted = Color(0xFF777AA6),
            soundBackground = Color(0xFFEEF2FF)
        )

        WidgetColorTheme.ROJO -> WidgetPreviewColors(
            primary = Color(0xFFDC2626),
            background = Color(0xFFFFF7F7),
            text = Color(0xFF451A1A),
            muted = Color(0xFFA46666),
            soundBackground = Color(0xFFFEE2E2)
        )

        WidgetColorTheme.CIELO_SUAVE -> WidgetPreviewColors(
            primary = Color(0xFF60A5FA),
            background = Color(0xFFF0F7FF),
            text = Color(0xFF1E3A5F),
            muted = Color(0xFF6B8FB9),
            soundBackground = Color(0xFFE4F1FF),
            onPrimary = Color(0xFF0F172A)
        )

        WidgetColorTheme.LAVANDA_SUAVE -> WidgetPreviewColors(
            primary = Color(0xFFA78BFA),
            background = Color(0xFFFBF7FF),
            text = Color(0xFF322653),
            muted = Color(0xFF8A77B3),
            soundBackground = Color(0xFFF2ECFF),
            onPrimary = Color(0xFF1F1833)
        )

        WidgetColorTheme.MENTA_SUAVE -> WidgetPreviewColors(
            primary = Color(0xFF0D9488),
            background = Color(0xFFF0FDFA),
            text = Color(0xFF143F3A),
            muted = Color(0xFF5B8F86),
            soundBackground = Color(0xFFDCFDF7)
        )

        WidgetColorTheme.CORAL_SUAVE -> WidgetPreviewColors(
            primary = Color(0xFFFB7185),
            background = Color(0xFFFFF5F7),
            text = Color(0xFF4A2230),
            muted = Color(0xFFA06A78),
            soundBackground = Color(0xFFFFE7EC),
            onPrimary = Color(0xFF3A1620)
        )

        WidgetColorTheme.CRISTAL -> WidgetPreviewColors(
            primary = Color(0xFF2563EB),
            background = Color(0xD9FFFFFF),
            text = Color(0xFF162033),
            muted = Color(0xFF667085),
            soundBackground = Color(0xBFE0F2FE)
        )

        WidgetColorTheme.AURORA -> WidgetPreviewColors(
            primary = Color(0xFF7C3AED),
            background = Color(0xFFFFF7FD),
            text = Color(0xFF2D1838),
            muted = Color(0xFF8B6C9C),
            soundBackground = Color(0xFFF7E8FF),
            backgroundBrush = Brush.verticalGradient(
                colors = listOf(Color(0xFFFFF7FD), Color(0xFFEFF6FF))
            ),
            headerBrush = Brush.horizontalGradient(
                colors = listOf(Color(0xFF2563EB), Color(0xFF7C3AED), Color(0xFFDB2777))
            )
        )

        WidgetColorTheme.OCEANO -> WidgetPreviewColors(
            primary = Color(0xFF0369A1),
            background = Color(0xFFEFFBFF),
            text = Color(0xFF123341),
            muted = Color(0xFF5D8798),
            soundBackground = Color(0xFFDFF8FF),
            backgroundBrush = Brush.verticalGradient(
                colors = listOf(Color(0xFFEFFBFF), Color(0xFFE7FFF8))
            ),
            headerBrush = Brush.horizontalGradient(
                colors = listOf(Color(0xFF0891B2), Color(0xFF2563EB))
            )
        )

        WidgetColorTheme.OSCURO -> WidgetPreviewColors(
            primary = Color(0xFF38BDF8),
            background = Color(0xFF111827),
            text = Color(0xFFF9FAFB),
            muted = Color(0xFFB6C2D1),
            soundBackground = Color(0xFF1F2937)
        )
    }
}

private fun WidgetColorTheme.label(): String {
    return when (this) {
        WidgetColorTheme.AZUL -> "Azul"
        WidgetColorTheme.MORADO -> "Morado"
        WidgetColorTheme.VERDE -> "Verde"
        WidgetColorTheme.NARANJA -> "Naranja"
        WidgetColorTheme.TURQUESA -> "Turquesa"
        WidgetColorTheme.ROSA -> "Rosa"
        WidgetColorTheme.INDIGO -> "Índigo"
        WidgetColorTheme.ROJO -> "Rojo"
        WidgetColorTheme.CIELO_SUAVE -> "Cielo"
        WidgetColorTheme.LAVANDA_SUAVE -> "Lavanda"
        WidgetColorTheme.MENTA_SUAVE -> "Menta"
        WidgetColorTheme.CORAL_SUAVE -> "Coral"
        WidgetColorTheme.CRISTAL -> "Cristal"
        WidgetColorTheme.AURORA -> "Aurora"
        WidgetColorTheme.OCEANO -> "Océano"
        WidgetColorTheme.OSCURO -> "Oscuro"
    }
}

private fun WidgetVisualStyle.label(): String {
    return when (this) {
        WidgetVisualStyle.CLASICO -> "Clásico"
        WidgetVisualStyle.MINIMALISTA -> "Minimalista"
        WidgetVisualStyle.CARD_SUAVE -> "Card suave"
        WidgetVisualStyle.CONTRASTE_ALTO -> "Contraste alto"
        WidgetVisualStyle.NOCTURNO -> "Nocturno"
    }
}

private fun WidgetVisualStyle.description(): String {
    return when (this) {
        WidgetVisualStyle.CLASICO -> "Equilibrado y familiar."
        WidgetVisualStyle.MINIMALISTA -> "Más limpio y ligero."
        WidgetVisualStyle.CARD_SUAVE -> "Fondos suaves y limpios."
        WidgetVisualStyle.CONTRASTE_ALTO -> "Lectura más fuerte."
        WidgetVisualStyle.NOCTURNO -> "Pensado para fondos oscuros."
    }
}

private fun WidgetTextSizeOption.label(): String {
    return when (this) {
        WidgetTextSizeOption.COMPACTO -> "Compacto"
        WidgetTextSizeOption.NORMAL -> "Normal"
        WidgetTextSizeOption.GRANDE -> "Grande"
    }
}

private fun WidgetLayoutSizeOption.label(): String {
    return when (this) {
        WidgetLayoutSizeOption.AUTOMATICO -> "Automático"
        WidgetLayoutSizeOption.COMPACTO -> "Compacto"
        WidgetLayoutSizeOption.NORMAL -> "Normal"
        WidgetLayoutSizeOption.GRANDE -> "Grande"
    }
}

private fun WidgetLayoutSizeOption.description(): String {
    return when (this) {
        WidgetLayoutSizeOption.AUTOMATICO -> "La app decide según el launcher."
        WidgetLayoutSizeOption.COMPACTO -> "Fuerza la vista más pequeña."
        WidgetLayoutSizeOption.NORMAL -> "Mantiene el formato estándar."
        WidgetLayoutSizeOption.GRANDE -> "Usa la vista con más detalle."
    }
}

private fun WidgetAppearanceSettings.summaryLabel(): String {
    return "${visualStyle.label()} · ${colorTheme.label()} · ${layoutSize.label()}"
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
