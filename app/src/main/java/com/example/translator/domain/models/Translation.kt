package com.example.translator.domain.models

data class Translation(
    val id: Int = 0,
    val sourceLang: Language,
    val targetLang: Language,
    val sourceText: String,
    val text: String,
    val isSelected: Boolean = false,
    val isDeletedFromHistory: Boolean = false,
    val created: Long = System.currentTimeMillis(),
)
