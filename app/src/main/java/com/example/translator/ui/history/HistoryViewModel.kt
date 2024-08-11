package com.example.translator.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.translator.domain.Repository
import com.example.translator.domain.models.Translation
import com.example.translator.domain.toDomain
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val repository: Repository
): ViewModel() {

    val translations = repository.getHistoryTranslations().map { it.map { it.toDomain() } }.asLiveData()

    fun deleteTranslation(translation: Translation) = viewModelScope.launch {
        repository.deleteHistoryTranslation(translation)
    }

    fun changeTranslationSelection(translation: Translation, isSelected: Boolean) = viewModelScope.launch {
        repository.upsertTranslation(translation.copy(isSelected = isSelected))
    }

    fun deleteAllHistory() = viewModelScope.launch {
        repository.deleteAllHistory()
    }
}