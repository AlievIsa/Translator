package com.example.translator.data.remote.translation.api

import com.example.translator.data.remote.translation.models.LanguageDto

interface TranslatorApiService {

    suspend fun getLanguages(): List<LanguageDto>

    suspend fun getTranslation(
        text: String,
        sourceLangCode: String,
        targetLangCode: String
    ): String

}