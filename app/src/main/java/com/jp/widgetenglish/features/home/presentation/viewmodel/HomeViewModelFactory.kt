package com.jp.widgetenglish.features.home.presentation.viewmodel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.jp.widgetenglish.data.repository.VocabularioRepository

class HomeViewModelFactory(
    private val repository: VocabularioRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            return HomeViewModel(repository) as T
        }
        throw IllegalArgumentException("ViewModel desconocido")
    }
}