package com.example.translator.domain

import android.content.Context
import android.net.Uri
import com.example.translator.data.SortOrder
import com.example.translator.data.local.TranslatorDB
import com.example.translator.data.remote.authentication.AuthWithEmailAndPassword
import com.example.translator.data.remote.translation.api.TranslatorApiService
import com.example.translator.data.remote.translation.sync.SyncTranslations
import com.example.translator.domain.models.Language
import com.example.translator.domain.models.Translation
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class Repository(
    private val context: Context,
    private val translatorDB: TranslatorDB,
    private val translatorApiService: TranslatorApiService,
    private val firebaseAuth: AuthWithEmailAndPassword,
    private val firebaseSync: SyncTranslations
) {

    private val sharedPrefs = context.getSharedPreferences(SHARED_PREFS_KEY, Context.MODE_PRIVATE)

    suspend fun getLanguages() = translatorApiService.getLanguages().map { it.toDomain() }

    suspend fun getTranslation(
        text: String,
        sourceLangCode: String,
        targetLangCode: String
    ) = translatorApiService.getTranslation(
        text,
        sourceLangCode,
        targetLangCode
    )

    fun getLanguageFromPreferences(key: String): Language? {
        val code = sharedPrefs.getString("${key}_code", null)
        val title = sharedPrefs.getString("${key}_title", null)
        return if (code != null && title != null) Language(code, title) else null
    }

    fun saveLanguageToPreferences(key: String, language: Language?) {
        sharedPrefs.edit().apply {
            putString("${key}_code", language?.code)
            putString("${key}_title", language?.title)
            apply()
        }
    }

    fun getHistoryTranslations() = translatorDB.translationDao.getAllHistory()

    fun getSelectedTranslations(searchQuery: String, sortOrder: SortOrder) =
        translatorDB.translationDao.getAllSelected(searchQuery, sortOrder)

    suspend fun upsertTranslation(translation: Translation) {
        translatorDB.translationDao.upsert(translation.toEntity())
        syncTranslationToFirebase()
    }

    suspend fun deleteHistoryTranslation(translation: Translation) {
        val translationEntity = translation.toEntity()
        if (translationEntity.isSelected) {
            translatorDB.translationDao.upsert(translationEntity.copy(isDeletedFromHistory = true))
        } else {
            translatorDB.translationDao.delete(translationEntity)
        }
        syncTranslationToFirebase()
    }

    suspend fun deleteSelectedTranslation(translation: Translation) {
        val translationEntity = translation.toEntity()
        if (translationEntity.isDeletedFromHistory) {
            translatorDB.translationDao.delete(translationEntity)
        } else {
            translatorDB.translationDao.upsert(translationEntity.copy(isSelected = false))
        }
        syncTranslationToFirebase()
    }

    suspend fun deleteAllHistory() {
        val translations = translatorDB.translationDao.getAllHistory().first()
        translations.forEach {
            deleteHistoryTranslation(it.toDomain())
        }
    }

    suspend fun deleteAllSelected() {
        val translations = translatorDB.translationDao.getAllSelected("", SortOrder.BY_DATE).first()
        translations.forEach {
            deleteSelectedTranslation(it.toDomain())
        }
    }

    suspend fun logIn(email: String, password: String): Result<Boolean> {
        val result = firebaseAuth.logIn(email, password)
        if (result.isSuccess) {
            syncData()
        }
        return result
    }

    suspend fun signUp(email: String, password: String, name: String, photoUri: Uri): Result<Boolean> {
        val result = firebaseAuth.signUp(email, password, name, photoUri)
        if (result.isSuccess) {
            syncData()
        }
        return result
    }

    fun getCurrentUser() = firebaseAuth.currentUser

    suspend fun signOut() = firebaseAuth.signOut()

    private suspend fun syncData() {
        val remoteTranslations = firebaseSync.getAll().first()
        // Merge remote translations into local database
        remoteTranslations.forEach { translationDto ->
            translatorDB.translationDao.upsert(translationDto.toDomain().toEntity())
        }
        val localTranslations = translatorDB.translationDao.getAll().first()
        // Push local translations to Firebase
        firebaseSync.updateAll(localTranslations.map { it.toDomain().toDto() })
    }

    private suspend fun syncTranslationToFirebase() {
        if (firebaseAuth.currentUser.value != null) {
            val translations = translatorDB.translationDao.getAll().first()
            firebaseSync.updateAll(translations.map { it.toDomain().toDto() })
        }
    }

    companion object {
        const val SHARED_PREFS_KEY = "TRANSLATOR_PREFS"
    }
}