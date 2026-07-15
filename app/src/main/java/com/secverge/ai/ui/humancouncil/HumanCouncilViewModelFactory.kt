package com.secverge.ai.ui.humancouncil

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class HumanCouncilViewModelFactory(private val repository: HumanCouncilRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HumanCouncilViewModel::class.java)) return HumanCouncilViewModel(repository) as T
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
