package com.jp.widgetenglish.features.admin.profile

import android.app.Activity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowInsetsControllerCompat
import com.jp.widgetenglish.features.admin.components.AdminBottomBar
import com.jp.widgetenglish.features.profile.viewmodel.ProfileViewModel

private val BackgroundSoft = Color(0xFFF5F7FB)

private val HeaderBlue = Color(0xFF2468D8)
private val PrimaryBlue = Color(0xFF2468D8)
private val PrimaryBlueSoft = Color(0xFFE3F1FF)
private val PrimaryBlueExtraSoft = Color(0xFFF0F7FF)

private val CardWhite = Color.White
private val TextMain = Color(0xFF1F2A60)
private val TextMuted = Color(0xFF7B8190)
private val BorderSoft = Color(0xFFE2E8F0)

private val GreenMain = Color(0xFF2E9D4D)
private val GreenSoft = Color(0xFFEAF7EC)

private val OrangeMain = Color(0xFFFF7A00)
private val OrangeSoft = Color(0xFFFFF1DF)

private val PurpleMain = Color(0xFF5B5EDB)
private val PurpleSoft = Color(0xFFEDEBFF)

private val RedSoft = Color(0xFFFFEBEE)
private val RedMain = Color(0xFFC62828)

@Composable
fun AdminProfileScreen(
    profileViewModel: ProfileViewModel,
    onResumenClick: () -> Unit,
    onRankingClick: () -> Unit,
    onActividadClick: () -> Unit,
    onCerrarSesionClick: () -> Unit
) {
    ProfileStatusBarColor()

    val profileState by profileViewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        profileViewModel.cargarDatosUsuario()
    }

    val usuario = profileState.usuario
    val nombreAdmin = usuario?.nombre?.takeIf { it.isNotBlank() } ?: "Administrador"
    val correoAdmin = usuario?.correo?.takeIf { it.isNotBlank() } ?: "Correo no disponible"
    val rolAdmin = usuario?.rol?.name ?: "ADMIN"
    val estadoAdmin = when (usuario?.activo) {
        true -> "Activo"
        false -> "Inactivo"
        null -> "Sin estado"
    }
    val estadoAdminBackground = if (usuario?.activo == false) RedSoft else GreenSoft
    val estadoAdminColor = if (usuario?.activo == false) RedMain else GreenMain
    val idAdmin = usuario?.firebaseUid ?: usuario?.idUsuario ?: "No disponible"

    var showLogoutDialog by remember { mutableStateOf(false) }
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showSecurityDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }

    if (showLogoutDialog) {
        LogoutDialog(
            onDismiss = { showLogoutDialog = false },
            onConfirm = {
                showLogoutDialog = false
                onCerrarSesionClick()
            }
        )
    }

    if (showEditProfileDialog) {
        AdminEditProfileDialog(
            currentName = nombreAdmin,
            email = correoAdmin,
            isSaving = profileState.guardandoPerfil,
            message = profileState.mensaje,
            error = profileState.error,
            onDismiss = {
                profileViewModel.limpiarMensajes()
                showEditProfileDialog = false
            },
            onSave = { nombre ->
                profileViewModel.actualizarPerfilAdministrador(nombre)
            }
        )
    }

    if (showSecurityDialog) {
        AdminSecurityDialog(
            email = correoAdmin,
            isSending = profileState.enviandoCorreoSeguridad,
            message = profileState.mensaje,
            error = profileState.error,
            onDismiss = {
                profileViewModel.limpiarMensajes()
                showSecurityDialog = false
            },
            onSendReset = {
                profileViewModel.enviarCorreoSeguridadAdministrador()
            }
        )
    }

    if (showAboutDialog) {
        AboutAdminPanelDialog(
            onDismiss = { showAboutDialog = false }
        )
    }

    Scaffold(
        topBar = {
            FixedProfileHeader()
        },
        bottomBar = {
            AdminBottomBar(
                selected = "perfil",
                onResumenClick = onResumenClick,
                onRankingClick = onRankingClick,
                onActividadClick = onActividadClick,
                onPerfilClick = {}
            )
        },
        containerColor = BackgroundSoft
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(BackgroundSoft)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 22.dp)
        ) {
            ProfileMainCard(
                nombre = nombreAdmin,
                correo = correoAdmin
            )

            Spacer(modifier = Modifier.height(24.dp))

            SectionTitle(
                title = "Información de la cuenta",
                subtitle = "Datos principales del administrador"
            )

            Spacer(modifier = Modifier.height(14.dp))

            ProfileInfoItem(
                icon = Icons.Filled.Person,
                title = "Nombre",
                value = nombreAdmin,
                background = PrimaryBlueSoft,
                iconColor = PrimaryBlue
            )

            Spacer(modifier = Modifier.height(12.dp))

            ProfileInfoItem(
                icon = Icons.Filled.Email,
                title = "Correo",
                value = correoAdmin,
                background = GreenSoft,
                iconColor = GreenMain
            )

            Spacer(modifier = Modifier.height(12.dp))

            ProfileInfoItem(
                icon = Icons.Filled.Security,
                title = "Rol",
                value = rolAdmin,
                background = OrangeSoft,
                iconColor = OrangeMain
            )

            Spacer(modifier = Modifier.height(12.dp))

            ProfileInfoItem(
                icon = Icons.Filled.VerifiedUser,
                title = "Estado de cuenta",
                value = estadoAdmin,
                background = estadoAdminBackground,
                iconColor = estadoAdminColor
            )

            Spacer(modifier = Modifier.height(12.dp))

            ProfileInfoItem(
                icon = Icons.Filled.AdminPanelSettings,
                title = "UID de Firebase",
                value = idAdmin,
                background = PurpleSoft,
                iconColor = PurpleMain
            )

            Spacer(modifier = Modifier.height(24.dp))

            SectionTitle(
                title = "Configuración",
                subtitle = "Opciones de cuenta y seguridad"
            )

            Spacer(modifier = Modifier.height(14.dp))

            ProfileOptionCard(
                icon = Icons.Filled.Settings,
                title = "Ajustes de perfil",
                subtitle = "Editar datos del administrador",
                background = PrimaryBlueSoft,
                iconColor = PrimaryBlue,
                onClick = {
                    profileViewModel.limpiarMensajes()
                    showEditProfileDialog = true
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            ProfileOptionCard(
                icon = Icons.Filled.Lock,
                title = "Seguridad",
                subtitle = "Contraseña y acceso",
                background = PurpleSoft,
                iconColor = PurpleMain,
                onClick = {
                    profileViewModel.limpiarMensajes()
                    showSecurityDialog = true
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            ProfileOptionCard(
                icon = Icons.Filled.Info,
                title = "Acerca del panel",
                subtitle = "Información del módulo administrador",
                background = OrangeSoft,
                iconColor = OrangeMain,
                onClick = {
                    showAboutDialog = true
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            AdminAccessCard()

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { showLogoutDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryBlue
                )
            ) {
                Icon(
                    imageVector = Icons.Filled.Logout,
                    contentDescription = null
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "Cerrar sesión",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(26.dp))
        }
    }
}

@Composable
private fun ProfileStatusBarColor() {
    val view = LocalView.current

    DisposableEffect(Unit) {
        val window = (view.context as? Activity)?.window

        if (window != null) {
            val controller = WindowInsetsControllerCompat(window, view)

            val previousStatusBarColor = window.statusBarColor
            val previousNavigationBarColor = window.navigationBarColor
            val previousLightStatusBars = controller.isAppearanceLightStatusBars
            val previousLightNavigationBars = controller.isAppearanceLightNavigationBars

            window.statusBarColor = HeaderBlue.toArgb()
            window.navigationBarColor = Color.White.toArgb()

            controller.isAppearanceLightStatusBars = false
            controller.isAppearanceLightNavigationBars = true

            onDispose {
                window.statusBarColor = previousStatusBarColor
                window.navigationBarColor = previousNavigationBarColor
                controller.isAppearanceLightStatusBars = previousLightStatusBars
                controller.isAppearanceLightNavigationBars = previousLightNavigationBars
            }
        } else {
            onDispose { }
        }
    }
}

@Composable
private fun FixedProfileHeader() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = HeaderBlue,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = 22.dp,
                    end = 22.dp,
                    top = 34.dp,
                    bottom = 22.dp
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Perfil",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "Cuenta de administrador",
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.86f)
                )
            }

            Surface(
                modifier = Modifier.size(42.dp),
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.18f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Filled.AdminPanelSettings,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileMainCard(
    nombre: String,
    correo: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(
            containerColor = CardWhite
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            PrimaryBlueSoft,
                            Color.White,
                            PrimaryBlueExtraSoft
                        )
                    )
                )
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(92.dp)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    PrimaryBlue,
                                    Color(0xFF8ED6F8)
                                )
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.AdminPanelSettings,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(48.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = nombre,
                    color = TextMain,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = correo,
                    color = TextMuted,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = GreenSoft
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.VerifiedUser,
                            contentDescription = null,
                            tint = GreenMain,
                            modifier = Modifier.size(18.dp)
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        Text(
                            text = "Cuenta administrativa",
                            color = GreenMain,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(
    title: String,
    subtitle: String
) {
    Column {
        Text(
            text = title,
            color = TextMain,
            fontSize = 21.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(3.dp))

        Text(
            text = subtitle,
            color = TextMuted,
            fontSize = 13.sp
        )
    }
}

@Composable
private fun ProfileInfoItem(
    icon: ImageVector,
    title: String,
    value: String,
    background: Color,
    iconColor: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = BorderSoft,
                shape = RoundedCornerShape(24.dp)
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = CardWhite
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                color = background
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    color = TextMuted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = value,
                    color = TextMain,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun ProfileOptionCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    background: Color,
    iconColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .border(
                width = 1.dp,
                color = BorderSoft,
                shape = RoundedCornerShape(24.dp)
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = CardWhite
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                color = background
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    color = TextMain,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = subtitle,
                    color = TextMuted,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun AdminAccessCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = BorderSoft,
                shape = RoundedCornerShape(26.dp)
            ),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(
            containerColor = CardWhite
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 3.dp
        )
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(54.dp),
                shape = CircleShape,
                color = PrimaryBlueSoft
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Filled.Security,
                        contentDescription = null,
                        tint = PrimaryBlue,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Permisos administrativos",
                    color = TextMain,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Puedes consultar usuarios, rankings y actividad general de la aplicación.",
                    color = TextMuted,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
private fun LogoutDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Surface(
                modifier = Modifier.size(52.dp),
                shape = CircleShape,
                color = RedSoft
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Filled.Logout,
                        contentDescription = null,
                        tint = RedMain,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        },
        title = {
            Text(
                text = "¿Cerrar sesión?",
                fontWeight = FontWeight.Bold,
                color = TextMain
            )
        },
        text = {
            Text(
                text = "¿Deseas cerrar tu sesión de administrador?",
                textAlign = TextAlign.Center,
                color = TextMuted
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryBlue
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Cerrar sesión")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Cancelar")
            }
        },
        shape = RoundedCornerShape(26.dp)
    )
}

@Composable
private fun AdminEditProfileDialog(
    currentName: String,
    email: String,
    isSaving: Boolean,
    message: String?,
    error: String?,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var name by remember(currentName) { mutableStateOf(currentName) }

    AlertDialog(
        onDismissRequest = {
            if (!isSaving) {
                onDismiss()
            }
        },
        icon = {
            DialogIcon(
                icon = Icons.Filled.Settings,
                background = PrimaryBlueSoft,
                iconColor = PrimaryBlue
            )
        },
        title = {
            Text(
                text = "Ajustes de perfil",
                fontWeight = FontWeight.Bold,
                color = TextMain,
                textAlign = TextAlign.Center
            )
        },
        text = {
            Column {
                Text(
                    text = "Actualiza el nombre que se muestra dentro del panel administrativo.",
                    color = TextMuted,
                    textAlign = TextAlign.Center,
                    lineHeight = 19.sp,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isSaving,
                    singleLine = true,
                    label = { Text("Nombre del administrador") },
                    shape = RoundedCornerShape(16.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                DialogInfoRow(
                    icon = Icons.Filled.Email,
                    title = "Correo asociado",
                    value = email,
                    background = GreenSoft,
                    iconColor = GreenMain
                )

                Spacer(modifier = Modifier.height(10.dp))

                DialogHintBox(
                    text = "El correo, el rol y los permisos se mantienen protegidos y no se editan desde esta sección.",
                    background = PrimaryBlueExtraSoft,
                    textColor = TextMuted
                )

                AdminDialogFeedback(
                    message = message,
                    error = error
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(name) },
                enabled = !isSaving && name.trim().length >= 3 && name.trim() != currentName.trim(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryBlue
                ),
                shape = RoundedCornerShape(14.dp)
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
                Text("Cerrar")
            }
        },
        shape = RoundedCornerShape(26.dp)
    )
}

@Composable
private fun AdminSecurityDialog(
    email: String,
    isSending: Boolean,
    message: String?,
    error: String?,
    onDismiss: () -> Unit,
    onSendReset: () -> Unit
) {
    AlertDialog(
        onDismissRequest = {
            if (!isSending) {
                onDismiss()
            }
        },
        icon = {
            DialogIcon(
                icon = Icons.Filled.Lock,
                background = PurpleSoft,
                iconColor = PurpleMain
            )
        },
        title = {
            Text(
                text = "Seguridad",
                fontWeight = FontWeight.Bold,
                color = TextMain,
                textAlign = TextAlign.Center
            )
        },
        text = {
            Column {
                Text(
                    text = "Envía un correo seguro para cambiar o restablecer la contraseña de la cuenta administrativa.",
                    color = TextMuted,
                    textAlign = TextAlign.Center,
                    lineHeight = 19.sp,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                DialogInfoRow(
                    icon = Icons.Filled.Email,
                    title = "Destino del correo",
                    value = email,
                    background = GreenSoft,
                    iconColor = GreenMain
                )

                Spacer(modifier = Modifier.height(10.dp))

                DialogHintBox(
                    text = "Firebase enviará el enlace al correo registrado. No se mostrará ni se almacenará ninguna contraseña en la app.",
                    background = PurpleSoft,
                    textColor = PurpleMain
                )

                Spacer(modifier = Modifier.height(8.dp))

                DialogHintBox(
                    text = "Si la cuenta usa Google, el acceso principal seguirá gestionándose desde Google.",
                    background = OrangeSoft,
                    textColor = OrangeMain
                )

                AdminDialogFeedback(
                    message = message,
                    error = error
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onSendReset,
                enabled = !isSending && email.isNotBlank() && email != "Correo no disponible",
                colors = ButtonDefaults.buttonColors(
                    containerColor = PurpleMain
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                if (isSending) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Enviar correo")
                }
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                enabled = !isSending,
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Cerrar")
            }
        },
        shape = RoundedCornerShape(26.dp)
    )
}

@Composable
private fun AboutAdminPanelDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            DialogIcon(
                icon = Icons.Filled.Info,
                background = OrangeSoft,
                iconColor = OrangeMain
            )
        },
        title = {
            Text(
                text = "Acerca del panel",
                fontWeight = FontWeight.Bold,
                color = TextMain,
                textAlign = TextAlign.Center
            )
        },
        text = {
            Column {
                Text(
                    text = "Panel administrativo de WidgetEnglish",
                    color = TextMain,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Un espacio para revisar la actividad educativa de la app y tomar decisiones con datos claros.",
                    color = TextMuted,
                    fontSize = 13.sp,
                    lineHeight = 19.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                AboutPanelItem(
                    icon = Icons.Filled.AdminPanelSettings,
                    title = "Alcance",
                    value = "Usuarios, rankings, actividad, categorías más estudiadas y palabras con dificultad.",
                    background = PrimaryBlueSoft,
                    iconColor = PrimaryBlue
                )

                Spacer(modifier = Modifier.height(10.dp))

                AboutPanelItem(
                    icon = Icons.Filled.Security,
                    title = "Privacidad",
                    value = "La información se usa para seguimiento académico, reportes y mejora de la experiencia.",
                    background = GreenSoft,
                    iconColor = GreenMain
                )

                Spacer(modifier = Modifier.height(10.dp))

                AboutPanelItem(
                    icon = Icons.Filled.Info,
                    title = "Versión",
                    value = "WidgetEnglish · Panel administrador",
                    background = OrangeSoft,
                    iconColor = OrangeMain
                )

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = BorderSoft)
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Los cambios realizados desde este módulo deben conservar la integridad de los datos de aprendizaje de los usuarios.",
                    color = TextMuted,
                    fontSize = 12.sp,
                    lineHeight = 17.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryBlue
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Entendido")
            }
        },
        shape = RoundedCornerShape(26.dp)
    )
}

@Composable
private fun DialogIcon(
    icon: ImageVector,
    background: Color,
    iconColor: Color
) {
    Surface(
        modifier = Modifier.size(54.dp),
        shape = CircleShape,
        color = background
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(30.dp)
            )
        }
    }
}

@Composable
private fun DialogInfoRow(
    icon: ImageVector,
    title: String,
    value: String,
    background: Color,
    iconColor: Color
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFFAFBFF),
        border = BorderStroke(1.dp, BorderSoft)
    ) {
        Row(
            modifier = Modifier.padding(13.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = background
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(21.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = TextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = value,
                    color = TextMain,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 17.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun DialogHintBox(
    text: String,
    background: Color,
    textColor: Color
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = background,
        border = BorderStroke(1.dp, BorderSoft)
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 12.sp,
            lineHeight = 17.sp,
            modifier = Modifier.padding(horizontal = 13.dp, vertical = 11.dp)
        )
    }
}

@Composable
private fun AdminDialogFeedback(
    message: String?,
    error: String?
) {
    val text = error ?: message ?: return
    val color = if (error != null) RedMain else GreenMain

    Spacer(modifier = Modifier.height(12.dp))

    Text(
        text = text,
        color = color,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun AboutPanelItem(
    icon: ImageVector,
    title: String,
    value: String,
    background: Color,
    iconColor: Color
) {
    DialogInfoRow(
        icon = icon,
        title = title,
        value = value,
        background = background,
        iconColor = iconColor
    )
}
