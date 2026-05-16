

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
        // Verificar si ya hay datos
        val palabrasExistentes = database.palabraDao().obtenerPalabraPorId("p1")
        if (palabrasExistentes != null) return

        val usuario = UsuarioEntity(
            idUsuario = "usuario_prueba",
            firebaseUid = "firebase_prueba",
            nombre = "Usuario Prueba",
            correo = "usuario@prueba.com",
            rol = RolUsuario.USUARIO,
            rachaActual = 5
        )

        val listaPalabras = listOf(
            PalabraEntity("p1", "Dog", "Perro", TipoPalabra.SUSTANTIVO, "/dɔːɡ/", "The dog is barking.", "El perro está ladrando.", Dificultad.FACIL),
            PalabraEntity("p2", "House", "Casa", TipoPalabra.SUSTANTIVO, "/haʊs/", "This is my house.", "Esta es mi casa.", Dificultad.FACIL),
            PalabraEntity("p3", "Book", "Libro", TipoPalabra.SUSTANTIVO, "/bʊk/", "I am reading a book.", "Estoy leyendo un libro.", Dificultad.FACIL),
            PalabraEntity("p4", "Happy", "Feliz", TipoPalabra.ADJETIVO, "/ˈhæpi/", "She is very happy.", "Ella está muy feliz.", Dificultad.FACIL),
            PalabraEntity("p5", "Cat", "Gato", TipoPalabra.SUSTANTIVO, "/kæt/", "The cat is sleeping.", "El gato está durmiendo.", Dificultad.FACIL),
            PalabraEntity("p6", "Sun", "Sol", TipoPalabra.SUSTANTIVO, "/sʌn/", "The sun is hot.", "El sol está caliente.", Dificultad.FACIL),
            PalabraEntity("p7", "Friend", "Amigo", TipoPalabra.SUSTANTIVO, "/frend/", "He is my best friend.", "Él es mi mejor amigo.", Dificultad.FACIL),
            PalabraEntity("p8", "Water", "Agua", TipoPalabra.SUSTANTIVO, "/ˈwɔːtər/", "I need some water.", "Necesito algo de agua.", Dificultad.FACIL),
            PalabraEntity("p9", "Red", "Rojo", TipoPalabra.ADJETIVO, "/red/", "The apple is red.", "La manzana es roja.", Dificultad.FACIL),
            PalabraEntity("p10", "Big", "Grande", TipoPalabra.ADJETIVO, "/bɪɡ/", "That is a big car.", "Ese es un coche grande.", Dificultad.FACIL),
            PalabraEntity("p11", "Apple", "Manzana", TipoPalabra.SUSTANTIVO, "/ˈæpl/", "I like apples.", "Me gustan las manzanas.", Dificultad.FACIL),
            PalabraEntity("p12", "Tree", "Árbol", TipoPalabra.SUSTANTIVO, "/triː/", "The tree is tall.", "El árbol es alto.", Dificultad.FACIL),
            PalabraEntity("p13", "School", "Escuela", TipoPalabra.SUSTANTIVO, "/skuːl/", "I go to school.", "Voy a la escuela.", Dificultad.FACIL),
            PalabraEntity("p14", "Mother", "Madre", TipoPalabra.SUSTANTIVO, "/ˈmʌðər/", "My mother is kind.", "Mi madre es amable.", Dificultad.FACIL),
            PalabraEntity("p15", "Father", "Padre", TipoPalabra.SUSTANTIVO, "/ˈfɑːðər/", "My father works hard.", "Mi padre trabaja duro.", Dificultad.FACIL),
            PalabraEntity("p16", "Green", "Verde", TipoPalabra.ADJETIVO, "/ɡriːn/", "The grass is green.", "La hierba es verde.", Dificultad.FACIL),
            PalabraEntity("p17", "Small", "Pequeño", TipoPalabra.ADJETIVO, "/smɔːl/", "It is a small bird.", "Es un pájaro pequeño.", Dificultad.FACIL),
            PalabraEntity("p18", "Blue", "Azul", TipoPalabra.ADJETIVO, "/bluː/", "The sky is blue.", "El cielo es azul.", Dificultad.FACIL),
            PalabraEntity("p19", "City", "Ciudad", TipoPalabra.SUSTANTIVO, "/ˈsɪti/", "I live in a big city.", "Vivo en una ciudad grande.", Dificultad.MEDIA),
            PalabraEntity("p20", "Window", "Ventana", TipoPalabra.SUSTANTIVO, "/ˈwɪndoʊ/", "Open the window.", "Abre la ventana.", Dificultad.FACIL)
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
            cantidadContenido = listaPalabras.size + 1,
            cantidadSugeridaEstudio = 10,
            orden = 1
        )

        val contenidos = listaPalabras.mapIndexed { index, palabra ->
            LoteContenidoEntity(
                id = "lc_${palabra.idPalabra}",
                loteId = "lote_basico",
                contenidoId = palabra.idPalabra,
                tipoContenido = TipoContenido.PALABRA,
                orden = index + 1
            )
        }.toMutableList()

        contenidos.add(
            LoteContenidoEntity(
                id = "lc_go",
                loteId = "lote_basico",
                contenidoId = "verbo_go",
                tipoContenido = TipoContenido.VERBO,
                orden = contenidos.size + 1
            )
        )

        val progresoLote = ProgresoLoteEntity(
            id = "pl_usuario_prueba_lote_basico",
            usuarioId = "firebase_prueba",
            loteId = "lote_basico",
            activo = true,
            completado = false,
            progresoPorcentaje = 25f,
            contenidosAprendidos = 5,
            totalContenidos = contenidos.size,
            fechaInicio = System.currentTimeMillis()
        )

        val progresosContenido = listaPalabras.mapIndexed { index, palabra ->
            ProgresoUsuarioEntity(
                id = "pu_usuario_prueba_${palabra.idPalabra}",
                usuarioId = "firebase_prueba",
                contenidoId = palabra.idPalabra,
                tipoContenido = TipoContenido.PALABRA,
                estadoAprendizaje = if (index < 5) EstadoAprendizaje.APRENDIDA else if (index < 10) EstadoAprendizaje.EN_PROGRESO else EstadoAprendizaje.NO_VISTA,
                aprendido = index < 5
            )
        }

        database.usuarioDao().insertarUsuario(usuario)
        database.palabraDao().insertarPalabras(listaPalabras)
        database.verboDao().insertarVerbos(listOf(verboGo))
        database.loteDao().insertarLote(loteBasico)
        database.loteDao().insertarContenidosLote(contenidos)
        database.progresoDao().insertarProgresoLote(progresoLote)
        progresosContenido.forEach { database.progresoDao().insertarProgresoUsuario(it) }
    }
}