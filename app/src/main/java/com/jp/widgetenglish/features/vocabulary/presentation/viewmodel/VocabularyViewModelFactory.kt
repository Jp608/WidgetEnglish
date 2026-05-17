package com.jp.widgetenglish.features.vocabulary.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.jp.widgetenglish.data.repository.VocabularioRepository
import com.jp.widgetenglish.data.repository.auth.AuthRepository

class VocabularyViewModelFactory(
    private val repository: VocabularioRepository,
    private val authRepository: AuthRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(VocabularyViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return VocabularyViewModel(repository, authRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
