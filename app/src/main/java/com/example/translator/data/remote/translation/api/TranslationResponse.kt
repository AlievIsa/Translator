package com.example.translator.data.remote.translation.api

import com.google.gson.annotations.SerializedName

data class TranslationResponse(
    @SerializedName("data") val data: Translations
)

data class Translations(
    @SerializedName("translations") val translatedText: List<TranslatedText>
)

data class TranslatedText(
    @SerializedName("translatedText") val text: String
)