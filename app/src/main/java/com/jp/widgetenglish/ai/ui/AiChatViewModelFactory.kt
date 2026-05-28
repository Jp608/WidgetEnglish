package com.jp.widgetenglish.ai.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.jp.widgetenglish.ai.data.AiChatRepository
import com.jp.widgetenglish.ai.data.local.AiChatDao

class AiChatViewModelFactory(
    private val apiKey: String,
    private val aiChatDao: AiChatDao
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return AiChatViewModel(
            repository = AiChatRepository(
                apiKey = apiKey,
                aiChatDao = aiChatDao
            )
        ) as T
    }
}