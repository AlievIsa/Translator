package com.example.translator.data.remote.translation.sync

import com.example.translator.data.remote.translation.models.LanguageDto
import com.example.translator.data.remote.translation.models.TranslationDto
import com.example.translator.data.remote.translation.sync.FirebaseSyncImpl.Companion.KEY_CREATED
import com.example.translator.data.remote.translation.sync.FirebaseSyncImpl.Companion.KEY_ID
import com.example.translator.data.remote.translation.sync.FirebaseSyncImpl.Companion.KEY_IS_DELETED_FROM_HISTORY
import com.example.translator.data.remote.translation.sync.FirebaseSyncImpl.Companion.KEY_IS_SELECTED
import com.example.translator.data.remote.translation.sync.FirebaseSyncImpl.Companion.KEY_SOURCE_LANG
import com.example.translator.data.remote.translation.sync.FirebaseSyncImpl.Companion.KEY_SOURCE_TEXT
import com.example.translator.data.remote.translation.sync.FirebaseSyncImpl.Companion.KEY_TARGET_LANG
import com.example.translator.data.remote.translation.sync.FirebaseSyncImpl.Companion.KEY_TEXT
import com.example.translator.data.remote.translation.sync.FirebaseSyncImpl.Companion.KEY_USER_ID
import com.google.android.play.integrity.internal.al
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

class FirebaseSyncImpl: SyncTranslations {

    private val firestore = Firebase.firestore
    private val auth = Firebase.auth

    override suspend fun getAll(): Flow<List<TranslationDto>> = flow {
        val translations = firestore.collection(KEY_COLLECTION_TRANSLATIONS)
            .whereEqualTo(KEY_USER_ID, auth.currentUser!!.uid)
            .get()
            .await()
            .documents
            .map { document ->
                document.toTranslationDto()
            }
        emit(translations)
    }

    override suspend fun updateAll(translations: List<TranslationDto>) {
        val userId = auth.currentUser!!.uid
        val remoteTranslations = firestore.collection(KEY_COLLECTION_TRANSLATIONS)
            .whereEqualTo(KEY_USER_ID, userId)
            .get()
            .await()
            .documents
            .map { it.id to it.toTranslationDto() }
            .toMap()
        val localTranslationIds = translations.map { it.id.toString() }.toSet()
        // Deleting translations that are not in the local database
        val toDelete = remoteTranslations.keys.filterNot { localTranslationIds.contains(it) }
        val batch = firestore.batch()
        toDelete.forEach { id ->
            val docRef = firestore.collection(KEY_COLLECTION_TRANSLATIONS).document(id)
            batch.delete(docRef)
        }
        // Adding/Updating translations from the local database
        translations.forEach { translationDto ->
            val docRef = firestore.collection(KEY_COLLECTION_TRANSLATIONS)
                .document(translationDto.id.toString())
            batch.set(docRef, translationDto.toMap())
        }

        batch.commit().await()
    }

    companion object {
        // collection name
        const val KEY_COLLECTION_TRANSLATIONS = "translations"

        // translation entity
        const val KEY_ID = "id"
        const val KEY_USER_ID = "user_id"
        const val KEY_SOURCE_LANG = "source_lang"
        const val KEY_TARGET_LANG = "target_lang"
        const val KEY_SOURCE_TEXT = "source_text"
        const val KEY_TEXT = "text"
        const val KEY_IS_SELECTED = "is_selected"
        const val KEY_IS_DELETED_FROM_HISTORY = "is_deleted_from_history"
        const val KEY_CREATED = "created"
    }

    private fun DocumentSnapshot.toTranslationDto(): TranslationDto {
        return TranslationDto(
            id = getLong(KEY_ID)?.toInt() ?: 0,
            userId = getString(KEY_USER_ID) ?: "",
            sourceLang = LanguageDto(
                code = getString("$KEY_SOURCE_LANG.code") ?: "",
                title = getString("$KEY_SOURCE_LANG.title") ?: ""
            ),
            targetLang = LanguageDto(
                code = getString("$KEY_TARGET_LANG.code") ?: "",
                title = getString("$KEY_TARGET_LANG.title") ?: ""
            ),
            sourceText = getString(KEY_SOURCE_TEXT) ?: "",
            text = getString(KEY_TEXT) ?: "",
            isSelected = getBoolean(KEY_IS_SELECTED) ?: false,
            isDeletedFromHistory = getBoolean(KEY_IS_DELETED_FROM_HISTORY) ?: false,
            created = getLong(KEY_CREATED) ?: System.currentTimeMillis()
        )
    }

    private fun TranslationDto.toMap(): Map<String, Any> {
        return mapOf(
            KEY_ID to id,
            KEY_USER_ID to auth.currentUser!!.uid,
            KEY_SOURCE_LANG to mapOf(
                "code" to sourceLang.code,
                "title" to sourceLang.title
            ),
            KEY_TARGET_LANG to mapOf(
                "code" to targetLang.code,
                "title" to targetLang.title
            ),
            KEY_SOURCE_TEXT to sourceText,
            KEY_TEXT to text,
            KEY_IS_SELECTED to isSelected,
            KEY_IS_DELETED_FROM_HISTORY to isDeletedFromHistory,
            KEY_CREATED to created,
        )
    }
}
