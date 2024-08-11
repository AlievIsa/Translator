package com.example.translator.ui.selected

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.translator.data.PreferencesManager
import com.example.translator.data.SortOrder
import com.example.translator.domain.Repository
import com.example.translator.domain.models.Translation
import com.example.translator.domain.toDomain
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SelectedViewModel @Inject constructor(
    private val repository: Repository,
    private val preferencesManager: PreferencesManager,
    private val state: SavedStateHandle
): ViewModel() {

    val searchQuery = state.getLiveData("searchQuery", "")

    private val preferencesFlow = preferencesManager.preferencesFlow

    @OptIn(ExperimentalCoroutinesApi::class)
    private val translationsFlow = combine(
        searchQuery.asFlow(),
        preferencesFlow
    ) { query, filterPreferences ->
        Pair(query, filterPreferences)
    }.flatMapLatest { (query, filterPreferences) ->
        repository.getSelectedTranslations(query, filterPreferences.sortOrder).map { it.map { it.toDomain() } }
    }
    val translations = translationsFlow.asLiveData()

    fun deleteTranslation(translation: Translation) = viewModelScope.launch {
        repository.deleteSelectedTranslation(translation)
    }

    fun deleteAllSelected() = viewModelScope.launch {
        repository.deleteAllSelected()
    }

    fun onSortOrderSelected(sortOrder: SortOrder) = viewModelScope.launch {
        preferencesManager.updateSortOrder(sortOrder)
    }
}