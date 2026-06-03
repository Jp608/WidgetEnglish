package com.jp.widgetenglish.features.profile.support

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jp.widgetenglish.features.common.AppBottomBar
import com.jp.widgetenglish.features.common.LightUserSystemBars
import com.widgetenglish.app.ui.Screen

private const val SupportEmail = "widgetenglish1@gmail.com"

private val ScreenBackground = Color(0xFFF5F7FC)
private val PrimaryBlue = Color(0xFF1565FF)
private val DeepBlue = Color(0xFF081A3A)
private val TextDark = Color(0xFF111827)
private val TextMuted = Color(0xFF6B7280)
private val BorderSoft = Color(0xFFE5EAF3)
private val SoftBlue = Color(0xFFEAF2FF)
private val SoftGreen = Color(0xFFE9F8F0)
private val SoftPurple = Color(0xFFF2ECFF)

private data class HelpTopicContent(
    val id: String,
    val title: String,
    val subtitle: String,
    val description: String,
    val icon: ImageVector,
    val iconColor: Color,
    val iconBackground: Color,
    val sections: List<HelpSection>,
    val isContact: Boolean = false
)

private data class HelpSection(
    val title: String,
    val body: String,
    val bullets: List<String> = emptyList()
)

@Composable
fun HelpSupportScreen(
    onBack: () -> Unit,
    onTopicClick: (String) -> Unit,
    onInicioClick: () -> Unit,
    onVocabularioClick: () -> Unit,
    onLotesClick: () -> Unit,
    onEstudioClick: () -> Unit,
    onIaClick: () -> Unit,
    onPerfilClick: () -> Unit
) {
    LightUserSystemBars()

    Scaffold(
        containerColor = ScreenBackground,
        bottomBar = {
            AppBottomBar(
                selectedRoute = Screen.Profile.route,
                onInicioClick = onInicioClick,
                onVocabularioClick = onVocabularioClick,
                onLotesClick = onLotesClick,
                onEstudioClick = onEstudioClick,
                onIaClick = onIaClick,
                onPerfilClick = onPerfilClick
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(ScreenBackground)
                .padding(bottom = innerPadding.calculateBottomPadding())
                .statusBarsPadding(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 26.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                HelpSupportHero(onBack = onBack)
            }

            items(helpTopics()) { topic ->
                HelpTopicCard(
                    topic = topic,
                    onClick = { onTopicClick(topic.id) }
                )
            }

            item {
                Text(
                    text = "Tambien puedes revisar tu configuracion de objetivo diario, lote activo y widget desde Perfil si el problema esta relacionado con progreso o contenido.",
                    color = TextMuted,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 10.dp)
                )
            }
        }
    }
}

@Composable
fun HelpSupportDetailScreen(
    topicId: String,
    onBack: () -> Unit,
    onInicioClick: () -> Unit,
    onVocabularioClick: () -> Unit,
    onLotesClick: () -> Unit,
    onEstudioClick: () -> Unit,
    onIaClick: () -> Unit,
    onPerfilClick: () -> Unit
) {
    val topic = remember(topicId) {
        helpTopics().firstOrNull { it.id == topicId } ?: helpTopics().first()
    }
    val context = LocalContext.current

    LightUserSystemBars()

    Scaffold(
        containerColor = ScreenBackground,
        bottomBar = {
            AppBottomBar(
                selectedRoute = Screen.Profile.route,
                onInicioClick = onInicioClick,
                onVocabularioClick = onVocabularioClick,
                onLotesClick = onLotesClick,
                onEstudioClick = onEstudioClick,
                onIaClick = onIaClick,
                onPerfilClick = onPerfilClick
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(ScreenBackground)
                .padding(bottom = innerPadding.calculateBottomPadding())
                .statusBarsPadding(),
            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                HelpDetailHeader(
                    topic = topic,
                    onBack = onBack
                )
            }

            items(topic.sections) { section ->
                HelpAnswerCard(section = section)
            }

            if (topic.isContact) {
                item {
                    ContactActionCard(
                        onSendEmail = {
                            openSupportEmail(
                                context = context,
                                subject = "Soporte Widget English",
                                body = "Hola, necesito ayuda con Widget English.\n\nDescripcion:\n\nPasos para reproducir:\n\nDispositivo:\n"
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun HelpSupportHero(
    onBack: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier.size(42.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Volver",
                tint = DeepBlue,
                modifier = Modifier.size(25.dp)
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Surface(
            modifier = Modifier.size(64.dp),
            shape = CircleShape,
            color = SoftBlue
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Help,
                    contentDescription = null,
                    tint = PrimaryBlue,
                    modifier = Modifier.size(38.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Ayuda y soporte",
                color = DeepBlue,
                fontSize = 27.sp,
                fontWeight = FontWeight.ExtraBold,
                lineHeight = 31.sp
            )

            Text(
                text = "Encuentra respuestas y contactanos cuando lo necesites.",
                color = TextMuted,
                fontSize = 17.sp,
                lineHeight = 24.sp
            )
        }
    }
}

@Composable
private fun HelpTopicCard(
    topic: HelpTopicContent,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(70.dp),
                shape = CircleShape,
                color = topic.iconBackground
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = topic.icon,
                        contentDescription = topic.title,
                        tint = topic.iconColor,
                        modifier = Modifier.size(38.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = topic.title,
                    color = DeepBlue,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    lineHeight = 24.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = topic.subtitle,
                    color = TextMuted,
                    fontSize = 15.sp,
                    lineHeight = 21.sp
                )
            }

            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = Color(0xFFA8B1C5),
                modifier = Modifier.size(30.dp)
            )
        }
    }
}

@Composable
private fun HelpDetailHeader(
    topic: HelpTopicContent,
    onBack: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver",
                        tint = DeepBlue
                    )
                }

                Text(
                    text = "Ayuda y soporte",
                    color = DeepBlue,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )

                Surface(
                    modifier = Modifier.size(40.dp),
                    shape = RoundedCornerShape(13.dp),
                    color = topic.iconBackground
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = topic.icon,
                            contentDescription = null,
                            tint = topic.iconColor,
                            modifier = Modifier.size(23.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = topic.title,
                color = DeepBlue,
                fontSize = 25.sp,
                fontWeight = FontWeight.ExtraBold,
                lineHeight = 29.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = topic.description,
                color = TextMuted,
                fontSize = 15.sp,
                lineHeight = 22.sp
            )
        }
    }
}

@Composable
private fun HelpAnswerCard(
    section: HelpSection
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, BorderSoft)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(34.dp),
                    shape = RoundedCornerShape(11.dp),
                    color = SoftBlue
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.Info,
                            contentDescription = null,
                            tint = PrimaryBlue,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = section.title,
                    color = TextDark,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.ExtraBold,
                    lineHeight = 22.sp,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = section.body,
                color = TextMuted,
                fontSize = 14.sp,
                lineHeight = 21.sp
            )

            if (section.bullets.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))

                Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                    section.bullets.forEach { bullet ->
                        HelpBullet(text = bullet)
                    }
                }
            }
        }
    }
}

@Composable
private fun HelpBullet(text: String) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(
            imageVector = Icons.Filled.CheckCircle,
            contentDescription = null,
            tint = Color(0xFF16A34A),
            modifier = Modifier
                .padding(top = 2.dp)
                .size(16.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = text,
            color = TextDark,
            fontSize = 13.sp,
            lineHeight = 19.sp,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ContactActionCard(
    onSendEmail: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(Color.White, Color(0xFFEAF2FF))
                    )
                )
                .padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier.size(58.dp),
                shape = CircleShape,
                color = SoftBlue
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Filled.Email,
                        contentDescription = null,
                        tint = PrimaryBlue,
                        modifier = Modifier.size(31.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = SupportEmail,
                color = DeepBlue,
                fontSize = 17.sp,
                fontWeight = FontWeight.ExtraBold
            )

            Text(
                text = "Usa este correo para soporte personalizado, reportes de errores o dudas que no quedaron resueltas.",
                color = TextMuted,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onSendEmail,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                Icon(
                    imageVector = Icons.Filled.Email,
                    contentDescription = null,
                    modifier = Modifier.size(19.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Enviar correo",
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

private fun openSupportEmail(
    context: android.content.Context,
    subject: String,
    body: String
) {
    val encodedSubject = Uri.encode(subject)
    val encodedBody = Uri.encode(body)
    val intent = Intent(Intent.ACTION_SENDTO).apply {
        data = Uri.parse("mailto:$SupportEmail?subject=$encodedSubject&body=$encodedBody")
    }

    try {
        context.startActivity(intent)
    } catch (_: ActivityNotFoundException) {
        Toast.makeText(
            context,
            "No se encontro una app de correo instalada.",
            Toast.LENGTH_LONG
        ).show()
    }
}

private fun helpTopics(): List<HelpTopicContent> {
    return listOf(
        HelpTopicContent(
            id = "faq",
            title = "Preguntas frecuentes",
            subtitle = "Resuelve dudas comunes sobre uso, cuenta y progreso",
            description = "Aqui encuentras respuestas directas a los problemas mas comunes dentro de Widget English.",
            icon = Icons.AutoMirrored.Filled.Help,
            iconColor = PrimaryBlue,
            iconBackground = SoftBlue,
            sections = faqSections()
        ),
        HelpTopicContent(
            id = "guide",
            title = "Guia de uso",
            subtitle = "Aprende a usar vocabulario, lotes, estudio, IA y widget",
            description = "Una guia rapida para entender las secciones principales y aprovechar mejor tu aprendizaje diario.",
            icon = Icons.Filled.Book,
            iconColor = PrimaryBlue,
            iconBackground = SoftPurple,
            sections = guideSections()
        ),
        HelpTopicContent(
            id = "contact",
            title = "Contactar soporte",
            subtitle = "Escribenos para recibir ayuda personalizada",
            description = "Si tu duda no se resuelve con la guia o necesitas reportar un error, contactanos por correo.",
            icon = Icons.Filled.Email,
            iconColor = Color(0xFF0D9488),
            iconBackground = SoftGreen,
            sections = contactSections(),
            isContact = true
        )
    )
}

private fun faqSections(): List<HelpSection> {
    return listOf(
        HelpSection(
            title = "No veo mi progreso actualizado",
            body = "El progreso se actualiza cuando una palabra, verbo o contenido queda marcado como aprendido. Si vienes de estudiar, vuelve a Inicio o Estadisticas para refrescar los datos.",
            bullets = listOf(
                "Revisa que estes usando la misma cuenta.",
                "Comprueba que el lote estudiado tenga contenido pendiente.",
                "Si el dato no cambia, cierra y abre la app para forzar una nueva lectura local."
            )
        ),
        HelpSection(
            title = "Mi racha diaria no aumenta",
            body = "La racha depende del cumplimiento del objetivo diario configurado en Perfil. Si tu objetivo es de 5 contenidos, la racha sube cuando completas esos 5 durante el dia.",
            bullets = listOf(
                "Revisa tu objetivo diario en Perfil.",
                "Estudia contenidos nuevos, no solo revisiones ya aprendidas.",
                "El cambio puede reflejarse al volver a Inicio."
            )
        ),
        HelpSection(
            title = "No aparece el lote que quiero estudiar",
            body = "Los lotes disponibles aparecen en la seccion Lotes. Algunos pueden tener pocos elementos si ya aprendiste la mayoria del contenido o si el lote aun esta siendo ampliado.",
            bullets = listOf(
                "Entra en Lotes y toca Ver contenido para revisar sus palabras.",
                "Activa el lote que quieras practicar en el widget.",
                "Si ya completaste un lote, puedes seguir practicando desde Estudio."
            )
        ),
        HelpSection(
            title = "El widget no cambia de palabra",
            body = "El widget toma contenido del lote activo. Si el lote esta completo o no tiene pendientes, puede mostrar menos variedad.",
            bullets = listOf(
                "Activa otro lote desde Lotes.",
                "Comprueba que el widget tenga permisos y este agregado en la pantalla de inicio.",
                "Abre la app despues de cambiar el lote para sincronizar el contenido."
            )
        ),
        HelpSection(
            title = "La IA no responde como esperaba",
            body = "El tutor IA funciona mejor con preguntas concretas: una frase para corregir, una palabra para explicar o un tema gramatical especifico.",
            bullets = listOf(
                "Incluye contexto: nivel, frase o duda exacta.",
                "Pide ejemplos cortos si la respuesta es muy larga.",
                "Si falla la conexion, intenta nuevamente cuando tengas internet estable."
            )
        )
    )
}

private fun guideSections(): List<HelpSection> {
    return listOf(
        HelpSection(
            title = "Inicio",
            body = "Inicio resume tu lote activo, objetivo diario, accesos rapidos y estadisticas principales. Es la pantalla para revisar rapidamente que debes estudiar hoy.",
            bullets = listOf(
                "Usa Lote activo para saber que contenido alimenta el widget.",
                "Objetivo diario muestra cuanto falta para cumplir tu meta.",
                "Resumen combina palabras, verbos y lotes disponibles."
            )
        ),
        HelpSection(
            title = "Vocabulario",
            body = "Vocabulario te permite consultar palabras y verbos con traduccion, pronunciacion, ejemplos y estado de aprendizaje.",
            bullets = listOf(
                "Usa los filtros por categoria para encontrar contenido rapido.",
                "Toca una palabra para ver mas detalle.",
                "El icono de audio ayuda a practicar pronunciacion."
            )
        ),
        HelpSection(
            title = "Lotes",
            body = "Los lotes agrupan contenido por tema. Puedes activar uno para el widget, ver su contenido y estudiar desde las herramientas de practica.",
            bullets = listOf(
                "Activa solo el lote que quieras ver en el widget.",
                "Ver contenido muestra las palabras incluidas y su estado.",
                "El progreso indica cuanto contenido del lote ya aprendiste."
            )
        ),
        HelpSection(
            title = "Estudio",
            body = "Estudio concentra los modos de practica. Utiliza quizzes, tarjetas y repasos para avanzar en palabras, verbos y lotes.",
            bullets = listOf(
                "Elige sesiones cortas si quieres cumplir tu objetivo diario rapido.",
                "Repasa falladas para reforzar contenido dificil.",
                "Combina quiz y tarjetas para reconocer y recordar mejor."
            )
        ),
        HelpSection(
            title = "Perfil y widget",
            body = "Desde Perfil ajustas tu nombre, objetivo diario, preferencias de aprendizaje y apariencia del widget.",
            bullets = listOf(
                "Cambia el objetivo diario segun tu tiempo disponible.",
                "Ajusta el modo de seleccion de contenido si quieres mas control.",
                "Personaliza el widget para que sea legible en tu pantalla."
            )
        ),
        HelpSection(
            title = "Tutor IA",
            body = "La IA sirve para resolver dudas de ingles, corregir frases, pedir ejemplos y practicar explicaciones sencillas.",
            bullets = listOf(
                "Pide correcciones como: Corrige esta frase y explicame el error.",
                "Solicita ejemplos con palabras nuevas del lote activo.",
                "Usala como apoyo; el progreso se consolida practicando en Estudio."
            )
        )
    )
}

private fun contactSections(): List<HelpSection> {
    return listOf(
        HelpSection(
            title = "Cuando escribirnos",
            body = "Contacta soporte si hay un problema que no puedes resolver desde la app o si necesitas ayuda puntual con tu cuenta.",
            bullets = listOf(
                "Problemas para iniciar sesion o recuperar acceso.",
                "Progreso, racha o lotes que no se actualizan correctamente.",
                "Errores visuales, cierres inesperados o fallos del widget."
            )
        ),
        HelpSection(
            title = "Que incluir en el correo",
            body = "Mientras mas claro sea el reporte, mas facil sera ayudarte. Incluye la informacion basica del problema y lo que esperabas que pasara.",
            bullets = listOf(
                "Correo de tu cuenta o nombre de usuario.",
                "Pantalla donde ocurre el problema.",
                "Pasos para repetir el error y, si puedes, una captura.",
                "Modelo del dispositivo o version de Android si el fallo parece del telefono."
            )
        ),
        HelpSection(
            title = "Recomendaciones antes de contactar",
            body = "Algunos problemas se resuelven con pasos simples. Prueba estas acciones antes de enviar el reporte si el caso no es urgente.",
            bullets = listOf(
                "Cierra y abre la app.",
                "Verifica tu conexion a internet.",
                "Confirma que estas en la cuenta correcta.",
                "Cambia temporalmente de lote si el problema esta en el widget."
            )
        )
    )
}
