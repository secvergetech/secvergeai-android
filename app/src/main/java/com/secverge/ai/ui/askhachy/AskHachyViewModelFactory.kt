package com.secverge.ai.ui.askhachy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class AskHachyViewModelFactory(
    private val repository: AskHachyRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {
        if (modelClass.isAssignableFrom(AskHachyViewModel::class.java)) {
            return AskHachyViewModel(repository) as T
        }

        throw IllegalArgumentException(
            "Unknown ViewModel class: ${modelClass.name}"
        )
    }
}
