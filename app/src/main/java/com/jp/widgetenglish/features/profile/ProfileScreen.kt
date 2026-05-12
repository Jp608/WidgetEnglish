package com.jp.widgetenglish.features.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.jp.widgetenglish.features.profile.viewmodel.ProfileViewModel
import com.widgetenglish.app.ui.Screen

@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = viewModel(),
    authViewModel: com.jp.widgetenglish.features.auth.viewmodel.AuthViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    LaunchedEffect(Unit) {
        viewModel.cargarDatosUsuario()
    }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val blueGradient = listOf(
        Color(0xFF1A237E),
        Color(0xFF1565C0),
        Color(0xFF0288D1)
    )

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            icon = { Icon(Icons.Filled.Logout, null, tint = Color(0xFF1565C0)) },
            title = { Text("¿Cerrar sesión?", fontWeight = FontWeight.Bold) },
            text = { Text("¿Estás seguro de que deseas cerrar sesión?", textAlign = TextAlign.Center) },
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
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Cerrar sesión")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showLogoutDialog = false }, shape = RoundedCornerShape(10.dp)) { Text("Cancelar") }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            icon = { Icon(Icons.Filled.DeleteForever, null, tint = Color.Red) },
            title = { Text("¿Eliminar cuenta?", fontWeight = FontWeight.Bold) },
            text = { Text("Esta acción es permanente y eliminará todo tu progreso. ¿Deseas continuar?", textAlign = TextAlign.Center) },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        // TODO: Implementar eliminar cuenta en el ViewModel
                        authViewModel.cerrarSesion()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    shape = RoundedCornerShape(10.dp)
                ) { Text("Eliminar definitivamente", color = Color.White) }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteDialog = false }, shape = RoundedCornerShape(10.dp)) { Text("Cancelar") }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }

    Column(
        modifier = Modifier.fillMaxSize().background(Color(0xFFF5F5F5)).verticalScroll(rememberScrollState())
    ) {
        Box(
            modifier = Modifier.fillMaxWidth().height(220.dp)
                .clip(RoundedCornerShape(bottomStart = 60.dp, bottomEnd = 60.dp))
                .background(brush = Brush.verticalGradient(colors = blueGradient)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(
                    modifier = Modifier.size(80.dp).clip(CircleShape),
                    color = Color.White.copy(alpha = 0.25f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Filled.Person, "Avatar", modifier = Modifier.size(44.dp), tint = Color.White)
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = uiState.usuario?.nombre ?: "Usuario",
                    fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White
                )
                Text(
                    text = uiState.usuario?.correo ?: "Invitado",
                    fontSize = 13.sp, color = Color.White.copy(alpha = 0.8f)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth().padding(20.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                    ProfileStat(value = "0", label = "Aprendidas", icon = Icons.Filled.School)
                    VerticalDivider(modifier = Modifier.height(40.dp), color = Color(0xFFE0E0E0))
                    ProfileStat(value = "0", label = "Lotes", icon = Icons.Filled.Layers)
                    VerticalDivider(modifier = Modifier.height(40.dp), color = Color(0xFFE0E0E0))
                    ProfileStat(value = "${uiState.usuario?.rachaActual ?: 0}", label = "Racha", icon = Icons.Filled.Whatshot)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            Text(text = "Mi cuenta", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.Gray)
            Spacer(modifier = Modifier.height(8.dp))

            ProfileOption(icon = Icons.Filled.Settings, label = "Configuración", iconColor = Color(0xFF1565C0)) { }
            ProfileOption(icon = Icons.Filled.Help, label = "Ayuda y Soporte", iconColor = Color(0xFF1565C0)) { }

            Spacer(modifier = Modifier.height(20.dp))
            Text(text = "Sesión", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.Gray)
            Spacer(modifier = Modifier.height(8.dp))

            ProfileOption(icon = Icons.Filled.Logout, label = "Cerrar sesión", iconColor = Color(0xFF1565C0)) { showLogoutDialog = true }

            Spacer(modifier = Modifier.height(8.dp))

            ProfileOption(icon = Icons.Filled.DeleteForever, label = "Eliminar cuenta", iconColor = Color.Red) { showDeleteDialog = true }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun ProfileStat(value: String, label: String, icon: ImageVector) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(imageVector = icon, contentDescription = null, tint = Color(0xFF1565C0), modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A237E))
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
    }
}

@Composable
fun ProfileOption(icon: ImageVector, label: String, iconColor: Color, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = label, tint = iconColor)
            Spacer(modifier = Modifier.width(12.dp))
            Text(label, style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.weight(1f))
            Icon(imageVector = Icons.Filled.ChevronRight, contentDescription = null, tint = Color.LightGray)
        }
    }
}
