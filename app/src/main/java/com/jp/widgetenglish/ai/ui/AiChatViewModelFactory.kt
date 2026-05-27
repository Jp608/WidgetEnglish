package com.jp.widgetenglish.ai.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.jp.widgetenglish.ai.data.AiChatRepository

class AiChatViewModelFactory(
    private val apiKey: String
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return AiChatViewModel(
            repository = AiChatRepository(apiKey = apiKey)
        ) as T
    }
}