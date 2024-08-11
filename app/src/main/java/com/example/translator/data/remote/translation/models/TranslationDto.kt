package com.example.translator.data.remote.translation.models

data class TranslationDto(
    val id: Int = 0,
    val userId: String = "",
    val sourceLang: LanguageDto,
    val targetLang: LanguageDto,
    val sourceText: String,
    val text: String,
    val isSelected: Boolean = false,
    val isDeletedFromHistory: Boolean = false,
    val created: Long = System.currentTimeMillis()
)