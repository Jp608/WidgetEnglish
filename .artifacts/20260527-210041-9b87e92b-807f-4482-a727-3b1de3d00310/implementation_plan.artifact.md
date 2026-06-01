# Implementation Plan - Administrator Statistics (HU38 & HU36)

Implementation of global statistics for the administrator to track popular categories and common errors across all users.

## Proposed Changes

### Data Layer

#### [EstadisticasFirestoreDataSource.kt](file:///C:/Users/Usuario/Desktop/WidgetEnglish/WidgetEnglish/app/src/main/java/com/jp/widgetenglish/data/remote/firestore/EstadisticasFirestoreDataSource.kt)
- Add methods to increment global counters using `FieldValue.increment`:
    - `incrementarUsoCategoria(loteId: String, nombre: String)`: Increments study/quiz count for a category.
    - `registrarErrorPalabra(palabraId: String, termino: String, loteId: String)`: Increments error count for a specific word.
- Add methods to fetch aggregated stats:
    - `obtenerCategoriasStats(): List<CategoriaStatsDto>`: Returns categories sorted by usage.
    - `obtenerErroresPalabrasStats(): List<PalabraErrorStatsDto>`: Returns top 50 words with most errors.

#### [NEW] [AdminStatsModels.kt](file:///C:/Users/Usuario/Desktop/WidgetEnglish/WidgetEnglish/app/src/main/java/com/jp/widgetenglish/data/remote/firestore/AdminStatsModels.kt)
- Data classes for aggregated statistics:
```kotlin
data class CategoriaStatsDto(
    val id: String = "",
    val nombre: String = "",
    val vecesEstudiada: Int = 0
)

data class PalabraErrorStatsDto(
    val id: String = "",
    val termino: String = "",
    val loteId: String = "",
    val cantidadErrores: Int = 0
)
```

### Features - Administrator

#### [AdminViewModel.kt](file:///C:/Users/Usuario/Desktop/WidgetEnglish/WidgetEnglish/app/src/main/java/com/jp/widgetenglish/features/admin/AdminViewModel.kt)
- Update `AdminUiState` to include `categoriasStats` and `erroresStats`.
- Update `cargarDatosAdmin` to fetch these new statistics from Firestore.

#### [NEW] [AdminCategoriasScreen.kt](file:///C:/Users/Usuario/Desktop/WidgetEnglish/WidgetEnglish/app/src/main/java/com/jp/widgetenglish/features/admin/AdminCategoriasScreen.kt)
- UI for HU38 showing most and least studied categories in a list or chart format.

#### [NEW] [AdminErroresScreen.kt](file:///C:/Users/Usuario/Desktop/WidgetEnglish/WidgetEnglish/app/src/main/java/com/jp/widgetenglish/features/admin/AdminErroresScreen.kt)
- UI for HU36 showing top 50 errors from quizzes with filtering by category.

#### [AdminDashboardScreen.kt](file:///C:/Users/Usuario/Desktop/WidgetEnglish/WidgetEnglish/app/src/main/java/com/jp/widgetenglish/features/admin/AdminDashboardScreen.kt)
- Replace "En construcción" cards (Vocabulario/Lotes) with links to the new Statistics screens.

### Features - Tracking

#### [QuizViewModel.kt](file:///C:/Users/Usuario/Desktop/WidgetEnglish/WidgetEnglish/app/src/main/java/com/jp/widgetenglish/features/vocabulary/presentation/viewmodel/QuizViewModel.kt)
- In `finalizarQuiz`, call `incrementarUsoCategoria` for the current batch.
- In `finalizarQuiz`, iterate through `falladas` and call `registrarErrorPalabra` for each one.

#### [CardsViewModel.kt](file:///C:/Users/Usuario/Desktop/WidgetEnglish/WidgetEnglish/app/src/main/java/com/jp/widgetenglish/features/vocabulary/presentation/cards/viewmodel/CardsViewModel.kt)
- In `finalizarSesion`, call `incrementarUsoCategoria` for the current batch (tracking study activity).
- **Note**: Per user request, word errors from Flashcards will NOT be recorded in global stats, only those from Quizzes.

---

### [NEW] Firestore Security Rules

To resolve the "Permission Denied" error, the following rules must be applied in the Firebase Console:

```javascript
service cloud.firestore {
  match /databases/{database}/documents {

    // Global Statistics Rules
    match /stats_globales/{statType}/detalle/{id} {
      // Allow any authenticated user to increment the counters
      allow update: if request.auth != null
                    && request.resource.data.diff(resource.data).affectedKeys()
                       .hasOnly(['vecesEstudiada', 'cantidadErrores', 'ultimaActualizacion', 'nombre', 'termino', 'loteId']);

      // Allow creation if it doesn't exist
      allow create: if request.auth != null;

      // Only allow ADMIN to read the full statistics lists
      allow read: if request.auth != null &&
                  get(/databases/$(database)/documents/usuarios/$(request.auth.uid)).data.rol == 'ADMIN';
    }

    // Existing rules for other collections...
  }
}
```

## Verification Plan

### Manual Verification
1.  **User Activity**:
    - Perform a Quiz with several intentionally wrong answers.
    - Perform a Flashcard session (errors here should not count towards global word error stats).
2.  **Admin View**:
    - Access Admin Dashboard.
    - Check "Categorías Populares" to see if the count for the studied batch increased.
    - Check "Errores Frecuentes" to see if the quiz errors appear.
    - Test filtering by category in the errors screen.
