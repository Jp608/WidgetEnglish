# Revisión módulo IA - feature/copiloto-ia

Fecha: 2026-06-01
Dispositivo: emulator-5554, Android 14, 1080x2400
APK probada: `app/build/outputs/apk/debug/app-debug.apk`

## Flujo probado

- Instalación debug sobre el emulador: correcta.
- Apertura de app: correcta, sesión existente cargada.
- Navegación a módulo IA desde bottom bar: correcta.
- Apertura de historial de chats: correcta.
- Apertura de sala de chat: correcta.
- Envío de mensaje a Groq: correcto, HTTP 200.
- Pruebas unitarias debug: correctas.

## Evidencia generada

- `06_ai_history.xml`: historial de chats con sesiones existentes.
- `07_ai_room.xml`: sala de chat cargada.
- `10_ai_room_test1c.xml`: respuesta de IA al mensaje de prueba.
- `11_ai_room_keyboard_closed.xml`: estado de sala de chat con teclado cerrado.
- `12_emu_abs.png`: captura visual por mecanismo del emulador. Salió negra por framebuffer/FLAG_SECURE del display.

## Hallazgos

1. Error de precisión de IA:
   - Prompt de prueba: `Corrige la frase I has a apple`
   - Respuesta observada: corrige a `I have an apple`, pero traduce como `Tú tienes una manzana`.
   - Corrección esperada: `Yo tengo una manzana`.

2. El chat no conserva contexto real:
   - `ChatRepositoryImpl.enviarMensaje()` solo envía el mensaje actual a la IA.
   - No se envía el historial de la conversación, por lo que Jimmy no puede recordar turnos anteriores.

3. Riesgo de privacidad en logs:
   - `HttpLoggingInterceptor.Level.BODY` registra respuestas completas de IA en Logcat.
   - Esto puede incluir contenido escrito por el usuario y respuestas educativas.

4. API key embebida en código:
   - La clave de Groq está escrita directamente en `AppNavGraph.kt`.
   - Riesgo alto para distribución o repositorio compartido.

5. Header del historial IA puede invadir status bar:
   - En `06_ai_history.xml`, el header inicia en `y=0` y el título está dentro del área superior.
   - En dispositivos con notch/status bar puede verse tapado.

6. Botón flotante Jimmy Copilot puede interferir con bottom bar:
   - En `window_initial.xml`, el botón flotante se superpone visualmente al área inferior derecha, cerca de Perfil.
   - Puede dificultar taps o hacer sentir saturada la navegación.

7. Error visual de capturas por emulador:
   - `adb screencap` y `adb emu screenrecord screenshot` generan imagen negra.
   - La UI sí existe y responde, confirmado por `uiautomator`.
