

package com.jp.widgetenglish.data.local.database

import com.jp.widgetenglish.data.local.entity.Dificultad
import com.jp.widgetenglish.data.local.entity.EstadoAprendizaje
import com.jp.widgetenglish.data.local.entity.LoteContenidoEntity
import com.jp.widgetenglish.data.local.entity.LoteEntity
import com.jp.widgetenglish.data.local.entity.NivelLote
import com.jp.widgetenglish.data.local.entity.PalabraEntity
import com.jp.widgetenglish.data.local.entity.ProgresoLoteEntity
import com.jp.widgetenglish.data.local.entity.ProgresoUsuarioEntity
import com.jp.widgetenglish.data.local.entity.RolUsuario
import com.jp.widgetenglish.data.local.entity.TipoContenido
import com.jp.widgetenglish.data.local.entity.TipoLote
import com.jp.widgetenglish.data.local.entity.TipoPalabra
import com.jp.widgetenglish.data.local.entity.UsuarioEntity
import com.jp.widgetenglish.data.local.entity.VerboEntity

object DatabaseSeeder {

    suspend fun seed(database: AppDatabase) {
        val usuario = UsuarioEntity(
            idUsuario = "usuario_prueba",
            firebaseUid = "firebase_prueba",
            nombre = "Usuario Prueba",
            correo = "usuario@prueba.com",
            rol = RolUsuario.USUARIO
        )

        val palabraDog = PalabraEntity(
            idPalabra = "palabra_dog",
            termino = "dog",
            traduccion = "perro",
            tipoPalabra = TipoPalabra.SUSTANTIVO,
            fonetica = "/dɔːɡ/",
            ejemplo = "The dog is friendly.",
            ejemploTraduccion = "El perro es amigable.",
            dificultad = Dificultad.FACIL
        )

        val palabraHouse = PalabraEntity(
            idPalabra = "palabra_house",
            termino = "house",
            traduccion = "casa",
            tipoPalabra = TipoPalabra.SUSTANTIVO,
            fonetica = "/haʊs/",
            ejemplo = "This is my house.",
            ejemploTraduccion = "Esta es mi casa.",
            dificultad = Dificultad.FACIL
        )

        val verboGo = VerboEntity(
            idVerbo = "verbo_go",
            formaBase = "go",
            pasadoSimple = "went",
            participioPasado = "gone",
            traduccion = "ir",
            fonetica = "/ɡoʊ/",
            ejemploIngles = "I go to school.",
            ejemploEspanol = "Yo voy a la escuela.",
            dificultad = Dificultad.FACIL,
            esIrregular = true
        )

        val loteBasico = LoteEntity(
            idLote = "lote_basico",
            nombre = "Inglés básico",
            descripcion = "Primer lote de vocabulario básico.",
            tipoLote = TipoLote.TEMATICO,
            nivel = NivelLote.A1,
            cantidadContenido = 3,
            cantidadSugeridaEstudio = 10,
            orden = 1
        )

        val contenidos = listOf(
            LoteContenidoEntity(
                id = "lc_dog",
                loteId = "lote_basico",
                contenidoId = "palabra_dog",
                tipoContenido = TipoContenido.PALABRA,
                orden = 1
            ),
            LoteContenidoEntity(
                id = "lc_house",
                loteId = "lote_basico",
                contenidoId = "palabra_house",
                tipoContenido = TipoContenido.PALABRA,
                orden = 2
            ),
            LoteContenidoEntity(
                id = "lc_go",
                loteId = "lote_basico",
                contenidoId = "verbo_go",
                tipoContenido = TipoContenido.VERBO,
                orden = 3
            )
        )

        val progresoLote = ProgresoLoteEntity(
            id = "pl_usuario_prueba_lote_basico",
            usuarioId = "usuario_prueba",
            loteId = "lote_basico",
            activo = true,
            completado = false,
            progresoPorcentaje = 0f,
            contenidosAprendidos = 0,
            totalContenidos = 3,
            fechaInicio = System.currentTimeMillis()
        )

        val progresosContenido = listOf(
            ProgresoUsuarioEntity(
                id = "pu_usuario_prueba_dog",
                usuarioId = "usuario_prueba",
                contenidoId = "palabra_dog",
                tipoContenido = TipoContenido.PALABRA,
                estadoAprendizaje = EstadoAprendizaje.NO_VISTA
            ),
            ProgresoUsuarioEntity(
                id = "pu_usuario_prueba_house",
                usuarioId = "usuario_prueba",
                contenidoId = "palabra_house",
                tipoContenido = TipoContenido.PALABRA,
                estadoAprendizaje = EstadoAprendizaje.NO_VISTA
            ),
            ProgresoUsuarioEntity(
                id = "pu_usuario_prueba_go",
                usuarioId = "usuario_prueba",
                contenidoId = "verbo_go",
                tipoContenido = TipoContenido.VERBO,
                estadoAprendizaje = EstadoAprendizaje.NO_VISTA
            )
        )

        database.usuarioDao().insertarUsuario(usuario)

        database.palabraDao().insertarPalabras(
            listOf(palabraDog, palabraHouse)
        )

        database.verboDao().insertarVerbos(
            listOf(verboGo)
        )

        database.loteDao().insertarLote(loteBasico)
        database.loteDao().insertarContenidosLote(contenidos)

        database.progresoDao().insertarProgresoLote(progresoLote)

        progresosContenido.forEach { progreso ->
            database.progresoDao().insertarProgresoUsuario(progreso)
        }
    }
}