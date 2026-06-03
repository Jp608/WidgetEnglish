package com.jp.widgetenglish.features.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.jp.widgetenglish.features.auth.viewmodel.AuthViewModel
import com.widgetenglish.app.ui.Screen

@Composable
fun RegisterScreen(
    navController: NavController,
    viewModel: AuthViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var mostrarTerminosRegistro by remember { mutableStateOf(false) }

    val blueGradient = listOf(
        Color(0xFF1A237E),
        Color(0xFF1565C0),
        Color(0xFF0288D1)
    )



    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .clip(RoundedCornerShape(bottomStart = 60.dp, bottomEnd = 60.dp))
                .background(brush = Brush.verticalGradient(colors = blueGradient)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "Crear cuenta", fontSize = 30.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = "Únete y empieza a aprender", fontSize = 14.sp, color = Color.White.copy(alpha = 0.8f))
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(170.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "Datos de registro", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1A237E))
                    Spacer(modifier = Modifier.height(20.dp))

                    OutlinedTextField(
                        value = uiState.nombre,
                        onValueChange = { viewModel.actualizarNombre(it) },
                        label = { Text("Nombre completo") },
                        leadingIcon = { Icon(Icons.Filled.Person, null, tint = Color(0xFF1565C0)) },
                        trailingIcon = {
                            AuthCharacterCounter(
                                currentLength = uiState.nombre.length,
                                maxLength = AuthInputLimits.NAME,
                                modifier = Modifier.padding(end = 12.dp)
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = uiState.correo,
                        onValueChange = { viewModel.actualizarCorreo(it) },
                        label = { Text("Correo electrónico") },
                        leadingIcon = { Icon(Icons.Filled.Email, null, tint = Color(0xFF1565C0)) },
                        trailingIcon = {
                            AuthCharacterCounter(
                                currentLength = uiState.correo.length,
                                maxLength = AuthInputLimits.EMAIL,
                                modifier = Modifier.padding(end = 12.dp)
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = uiState.password,
                        onValueChange = { viewModel.actualizarPassword(it) },
                        label = { Text("Contraseña") },
                        leadingIcon = { Icon(Icons.Filled.Lock, null, tint = Color(0xFF1565C0)) },
                        trailingIcon = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                AuthCharacterCounter(
                                    currentLength = uiState.password.length,
                                    maxLength = AuthInputLimits.PASSWORD
                                )
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                        contentDescription = null, tint = Color(0xFF1565C0)
                                    )
                                }
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = uiState.confirmPassword,
                        onValueChange = { viewModel.actualizarConfirmPassword(it) },
                        label = { Text("Confirmar contraseña") },
                        leadingIcon = { Icon(Icons.Filled.LockOpen, null, tint = Color(0xFF1565C0)) },
                        trailingIcon = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                AuthCharacterCounter(
                                    currentLength = uiState.confirmPassword.length,
                                    maxLength = AuthInputLimits.PASSWORD
                                )
                                IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                    Icon(
                                        imageVector = if (confirmPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                        contentDescription = null, tint = Color(0xFF1565C0)
                                    )
                                }
                            }
                        },
                        visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    uiState.error?.let {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            if (viewModel.prepararRegistroConTerminos()) {
                                mostrarTerminosRegistro = true
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0)),
                        enabled = !uiState.cargando
                    ) {
                        if (uiState.cargando) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Text("Crear cuenta", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    TextButton(onClick = { navController.popBackStack() }) {
                        Text("¿Ya tienes cuenta? ", color = Color.Gray, fontSize = 13.sp)
                        Text("Inicia sesión", color = Color(0xFF1565C0), fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(text = "Al registrarte aceptas nuestros Términos y Política de Privacidad", fontSize = 11.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (mostrarTerminosRegistro) {
        TermsAndConditionsDialog(
            isLoading = uiState.cargando,
            onDismiss = {
                if (!uiState.cargando) {
                    mostrarTerminosRegistro = false
                }
            },
            onAccept = {
                mostrarTerminosRegistro = false
                viewModel.registrar(aceptaTerminos = true)
            }
        )
    }
}
