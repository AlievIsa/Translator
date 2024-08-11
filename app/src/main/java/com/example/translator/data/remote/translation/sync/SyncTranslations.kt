package com.example.translator.data.remote.translation.sync

import com.example.translator.data.remote.translation.models.TranslationDto
import kotlinx.coroutines.flow.Flow

interface SyncTranslations {

    suspend fun getAll(): Flow<List<TranslationDto>>

    suspend fun updateAll(translations: List<TranslationDto>)
}