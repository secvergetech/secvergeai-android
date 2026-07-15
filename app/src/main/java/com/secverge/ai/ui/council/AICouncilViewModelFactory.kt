package com.secverge.ai.ui.council

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class AICouncilViewModelFactory(private val repository: AICouncilRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AICouncilViewModel::class.java)) return AICouncilViewModel(repository) as T
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
