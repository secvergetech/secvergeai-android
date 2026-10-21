package com.secverge.ai.ui.askhachy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class AskHachyViewModelFactory(
    private val repository: AskHachyRepository,
    private val historyStore: AskHachyHistoryStore
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {
        if (modelClass.isAssignableFrom(AskHachyViewModel::class.java)) {
            return AskHachyViewModel(
                repository = repository,
                historyStore = historyStore
            ) as T
        }

        throw IllegalArgumentException(
            "Unknown ViewModel class: ${modelClass.name}"
        )
    }
}
