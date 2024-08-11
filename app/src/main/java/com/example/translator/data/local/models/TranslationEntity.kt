package com.example.translator.data.local.models

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "translation")
data class TranslationEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @Embedded("source_") val sourceLang: LanguageEntity,
    @Embedded("target_") val targetLang: LanguageEntity,
    val sourceText: String,
    val text: String,
    val isSelected: Boolean = false,
    val isDeletedFromHistory: Boolean = false,
    val created: Long = System.currentTimeMillis()
)