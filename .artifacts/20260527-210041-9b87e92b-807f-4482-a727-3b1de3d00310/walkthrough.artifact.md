# Walkthrough - Administrator Statistics (HU38 & HU36)

Implemented global statistics for the administrator to monitor category popularity and common learning difficulties (word errors).

## Changes

### 1. Data Layer & Tracking
- **Global Stats Storage**: Implemented logic in `EstadisticasFirestoreDataSource` to increment global counters in Firestore using `FieldValue.increment`.
- **Quiz Tracking**: `QuizViewModel` now records both the usage of the category and the specific words that were failed during the quiz.
- **Study Tracking**: `CardsViewModel` records the usage of the category during flashcard sessions (errors in flashcards are not tracked globally as per request).

### 2. Administrator Features
- **New Data Models**: Added `CategoriaStatsDto` and `PalabraErrorStatsDto` to handle aggregated data.
- **Admin Dashboard**: Updated to include direct access to "Categorías" and "Errores" statistics.
- **Category Stats Screen (HU38)**: Displays a ranked list of categories based on how many times they've been studied or used in quizzes.
- **Word Error Stats Screen (HU36)**: Displays the top 50 most failed words in quizzes across all users, with the ability to filter by category.

## Verification Summary

### Automated Tests
- The project builds successfully with `:app:assembleDebug`.

### Manual Verification Steps
1. **Recording Data**:
   - Complete a Quiz and intentionally fail some words.
   - Complete a Flashcard session.
2. **Reviewing Stats**:
   - Log in as an Administrator.
   - Navigate to the Admin Dashboard.
   - Click on "Categorías" to see the updated usage count.
   - Click on "Errores" to see the list of most failed words and use the filter to narrow down by category.
