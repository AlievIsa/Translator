package com.example.translator.data.remote.translation.api

import android.util.Log
import com.example.translator.data.remote.translation.models.LanguageDto
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.Locale

class GoogleTranslatorApi(private val apiKey: String): TranslatorApiService {

    private val okHttpClient = OkHttpClient()
    private val gson = Gson()

    override suspend fun getLanguages(): List<LanguageDto> = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("https://google-translator9.p.rapidapi.com/v2/languages")
            .get()
            .addHeader("X-RapidAPI-Key", apiKey)
            .addHeader("X-RapidAPI-Host", "google-translator9.p.rapidapi.com")
            .build()

        try {
            val response = okHttpClient.newCall(request).execute()
            if (response.isSuccessful) {
                response.body?.string()?.let { responseBody ->
                    val languageResponse = gson.fromJson(responseBody, LanguageResponse::class.java)
                    languageResponse.data.languages.mapNotNull { languageData ->
                        try {
                            val locale = Locale(languageData.language)
                            val displayLanguage = locale.displayLanguage
                            if (displayLanguage.isNotBlank()
                                && displayLanguage != languageData.language
                                && !displayLanguage.contains('-'))
                            {
                                LanguageDto(languageData.language, displayLanguage)
                            } else {
                                null
                            }
                        } catch (e: Exception) {
                            null
                        }
                    }
                } ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: IOException) {
            e.printStackTrace()
            emptyList()
        }
    }

    override suspend fun getTranslation(
        text: String,
        sourceLangCode: String,
        targetLangCode: String
    ): String = withContext(Dispatchers.IO) {
        val mediaType = "application/json".toMediaTypeOrNull()
        val requestBody = """
            {
                "q": "$text",
                "source": "$sourceLangCode",
                "target": "$targetLangCode",
                "format": "text"
            }
        """.trimIndent().toRequestBody(mediaType)
        val request = Request.Builder()
            .url("https://google-translator9.p.rapidapi.com/v2")
            .post(requestBody)
            .addHeader("content-type", "application/json")
            .addHeader("X-RapidAPI-Key", apiKey)
            .addHeader("X-RapidAPI-Host", "google-translator9.p.rapidapi.com")
            .build()

        try {
            val response = okHttpClient.newCall(request).execute()
            if (response.isSuccessful) {
                response.body?.string()?.let { responseBody ->
                    val translationResponse = gson.fromJson(responseBody, TranslationResponse::class.java)
                    translationResponse.data.translatedText[0].text
                } ?: "Empty String"
            } else {
                "Response is not successful"
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.d("Error", "getTranslation: ${e.message}")
            "Exception ${e.message}"
        }
    }
}