package com.example.translator.data.remote.translation.api

import com.google.gson.annotations.SerializedName

data class LanguageResponse(
    @SerializedName("data") val data: LanguageData
)

data class LanguageData(
    @SerializedName("languages") val languages: List<LanguageItem>
)

data class LanguageItem(
    @SerializedName("language") val language: String
)