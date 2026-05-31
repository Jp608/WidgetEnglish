package com.jp.widgetenglish.features.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

@Composable
fun TermsAndConditionsDialog(
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onAccept: () -> Unit,
    modifier: Modifier = Modifier
) {
    var accepted by remember { mutableStateOf(false) }
    var showFullTerms by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = {
            if (!isLoading) {
                onDismiss()
            }
        }
    ) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .heightIn(max = 620.dp),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .align(Alignment.Center)
                            .clip(CircleShape)
                            .background(Color(0xFFE8F1FF)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Description,
                            contentDescription = null,
                            tint = Color(0xFF1F6FE5),
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        enabled = !isLoading,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Cerrar",
                            tint = Color(0xFF7A8496),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Términos y condiciones",
                    color = Color(0xFF1D2940),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = if (showFullTerms) FullTermsText else TermsSummaryText,
                        color = Color(0xFF4B5565),
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = Color(0xFFE3E8F0))
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .clickable(enabled = !isLoading) {
                            accepted = !accepted
                        },
                    verticalAlignment = Alignment.Top
                ) {
                    Checkbox(
                        checked = accepted,
                        onCheckedChange = { checked ->
                            accepted = checked
                        },
                        enabled = !isLoading
                    )

                    Text(
                        text = "Acepto los términos y condiciones y autorizo el tratamiento de mis datos personales.",
                        color = Color(0xFF2D3748),
                        fontSize = 11.sp,
                        lineHeight = 15.sp,
                        modifier = Modifier
                            .padding(top = 12.dp)
                            .weight(1f)
                    )
                }

                TextButton(
                    onClick = { showFullTerms = !showFullTerms },
                    enabled = !isLoading
                ) {
                    Text(
                        text = if (showFullTerms) "Ver resumen" else "Ver términos completos",
                        color = Color(0xFF1F6FE5),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        enabled = !isLoading,
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFFE53935),
                            disabledContentColor = Color(0xFFFFCDD2)
                        ),
                        border = BorderStroke(1.5.dp, Color(0xFFE53935)),
                        contentPadding = ButtonDefaults.ContentPadding
                    ) {
                        Text(
                            text = "Cancelar",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Button(
                        onClick = onAccept,
                        enabled = accepted && !isLoading,
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1F6FE5),
                            disabledContainerColor = Color(0xFFE4E9F2),
                            disabledContentColor = Color(0xFF98A2B3)
                        ),
                        contentPadding = ButtonDefaults.ContentPadding
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "Aceptar y continuar",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                lineHeight = 15.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TermsAndConditionsInfoDialog(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .heightIn(max = 640.dp),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .align(Alignment.Center)
                            .clip(CircleShape)
                            .background(Color(0xFFE8F1FF)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Description,
                            contentDescription = null,
                            tint = Color(0xFF1F6FE5),
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Cerrar",
                            tint = Color(0xFF7A8496),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Términos y condiciones",
                    color = Color(0xFF1D2940),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = FullTermsText,
                        color = Color(0xFF4B5565),
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = Color(0xFFE3E8F0))
                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1F6FE5)
                    )
                ) {
                    Text(
                        text = "Cerrar",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

private const val TermsSummaryText = """
WidgetEnglish almacena tu nombre, correo y datos de progreso de aprendizaje como palabras aprendidas, quizzes, racha, lote activo y estadísticas de aprendizaje.

Estos datos se utilizan para guardar el avance, mostrar estadísticas, recuperar el progreso y generar reportes administrativos.
"""

private val FullTermsText = """
Términos y condiciones de uso y autorización de tratamiento de datos personales

Bienvenido/a a WidgetEnglish. Antes de registrarte o iniciar sesión por primera vez, te solicitamos leer y aceptar estos términos y condiciones. Al aceptar, autorizas el uso de la aplicación y el tratamiento de tus datos personales conforme a las finalidades descritas a continuación.

1. Finalidad de la aplicación
WidgetEnglish es una aplicación educativa diseñada para apoyar el aprendizaje de vocabulario en inglés mediante lotes de palabras, quizzes, tarjetas de estudio, widgets y seguimiento del progreso del usuario.
La aplicación permite registrar avances de aprendizaje, consultar estadísticas personales y mejorar la experiencia de estudio según la actividad realizada.

2. Datos que recopilamos
Para el funcionamiento de la aplicación, WidgetEnglish podrá recopilar y almacenar los siguientes datos:
• Nombre del usuario.
• Correo electrónico asociado a la cuenta.
• Rol dentro de la aplicación, por ejemplo usuario o administrador.
• Lote activo seleccionado.
• Palabras con mayor cantidad de errores.
• Cantidad de quizzes realizados.
• Racha actual y racha máxima.
• Actividad diaria de estudio.
• Porcentaje de progreso general.
• Fecha de último acceso o última actividad.
• Estadísticas necesarias para mostrar rankings, actividad general o reportes administrativos.

WidgetEnglish no solicita datos sensibles como información médica, financiera, religiosa, política, biométrica o de ubicación exacta.

3. Finalidades del tratamiento de datos
Los datos serán tratados con las siguientes finalidades:
• Crear y administrar la cuenta del usuario.
• Permitir el inicio de sesión mediante correo o cuenta de Google.
• Guardar el progreso de aprendizaje.
• Mostrar estadísticas personales dentro de la aplicación.
• Calcular rachas, metas diarias y porcentajes de avance.
• Identificar palabras con mayor dificultad de aprendizaje.
• Mostrar al administrador información general de uso, rankings y estadísticas educativas.
• Sincronizar el progreso entre el almacenamiento local del dispositivo y la base de datos en la nube.
• Mejorar la experiencia de aprendizaje y el funcionamiento de la aplicación.
• Prevenir pérdida de progreso cuando el usuario cambie de dispositivo o reinstale la aplicación.

4. Uso de Google para iniciar sesión
Si decides iniciar sesión con Google, WidgetEnglish podrá recibir de Google datos básicos de tu cuenta, como tu nombre, correo electrónico e identificador de usuario. Estos datos se usarán únicamente para crear o acceder a tu cuenta dentro de la aplicación.
WidgetEnglish no accede a tu contraseña de Google.

5. Almacenamiento de la información
La aplicación podrá guardar información de dos formas:
• Localmente en el dispositivo, para permitir el funcionamiento de la app incluso sin conexión a internet.
• En la nube mediante Firebase, para sincronizar el progreso, recuperar datos y permitir estadísticas generales.

Cuando no haya conexión a internet, algunos datos podrán guardarse primero en el dispositivo y sincronizarse posteriormente cuando la conexión esté disponible.

6. Información visible para el administrador
El administrador de WidgetEnglish podrá consultar información relacionada con el uso educativo de la aplicación, como:
• Usuarios registrados.
• Actividad de aprendizaje.
• Quizzes realizados.
• Rachas y progreso.
• Palabras con más errores.
• Categorías o lotes más estudiados.
• Ranking de usuarios según actividad o progreso.

Esta información se utiliza con fines académicos, administrativos y de mejora de la aplicación.

7. Derechos del usuario
Como titular de tus datos personales, puedes solicitar:
• Conocer qué datos se almacenan sobre tu cuenta.
• Actualizar o corregir tus datos.
• Solicitar la eliminación de tu cuenta o de tus datos personales, cuando sea procedente.
• Revocar la autorización de tratamiento de datos.
• Solicitar información sobre el uso que se ha dado a tus datos.

Para ejercer estos derechos, podrás comunicarte con el responsable de la aplicación a través del medio de contacto definido por WidgetEnglish.

8. Seguridad de la información
WidgetEnglish implementa medidas razonables para proteger la información almacenada, incluyendo el uso de servicios de autenticación y base de datos en la nube. Sin embargo, ningún sistema digital puede garantizar seguridad absoluta.
El usuario se compromete a proteger el acceso a su cuenta y a no compartir credenciales personales con terceros.

9. Uso adecuado de la aplicación
El usuario se compromete a utilizar WidgetEnglish con fines educativos y de manera responsable. No está permitido intentar alterar el funcionamiento de la aplicación, acceder a información de otros usuarios o manipular los datos de progreso.

10. Cambios en los términos
WidgetEnglish podrá actualizar estos términos y condiciones cuando sea necesario. Si los cambios son relevantes, la aplicación podrá solicitar nuevamente la aceptación del usuario.

11. Aceptación
Al marcar la casilla de aceptación y continuar con el registro o inicio de sesión por primera vez, declaras que:
• Has leído y comprendido estos términos.
• Autorizas el tratamiento de tus datos personales para las finalidades descritas.
• Aceptas el uso de tu información de aprendizaje para generar estadísticas personales y administrativas dentro de WidgetEnglish.
""".trimIndent()
