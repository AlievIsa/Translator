package com.example.translator.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.translator.domain.Repository
import com.example.translator.domain.models.Language
import com.example.translator.domain.models.Translation
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: Repository
): ViewModel() {

    private val _languages = MutableLiveData<List<Language>>()
    val languages: LiveData<List<Language>> get() = _languages

    private val _translation = MutableLiveData<String>()
    val translation: LiveData<String> get() = _translation

    var currentSourceLang: Language?
        get() = getLanguageFromPreferences(SOURCE_LANG_KEY)
        set(value) {
            saveLanguageToPreferences(SOURCE_LANG_KEY, value)
        }
    var currentTargetLang: Language?
        get() = getLanguageFromPreferences(TARGET_LANG_KEY)
        set(value) {
            saveLanguageToPreferences(TARGET_LANG_KEY, value)
        }
    var isTranslationEnable = true

    init {
        viewModelScope.launch {
            _languages.value = repository.getLanguages().sortedBy { it.title }
        }
    }

    private fun getLanguageFromPreferences(key: String): Language? {
        return repository.getLanguageFromPreferences(key)
    }

    private fun saveLanguageToPreferences(key: String, language: Language?) {
        return repository.saveLanguageToPreferences(key, language)
    }

    fun getTranslation(text: String) =  viewModelScope.launch {
        _translation.value = repository.getTranslation(text, currentSourceLang!!.code, currentTargetLang!!.code)
        repository.upsertTranslation(
            Translation(
                sourceLang = currentSourceLang!!,
                targetLang = currentTargetLang!!,
                sourceText = text,
                text = translation.value!!,
            )
        )
    }

    fun clearTranslationField() {
        _translation.value = ""
    }

    companion object {
        const val SOURCE_LANG_KEY = "SOURCE_LANG_KEY"
        const val TARGET_LANG_KEY = "TARGET_LANG_KEY"
    }
}