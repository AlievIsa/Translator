package com.example.translator.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.example.translator.data.SortOrder
import com.example.translator.data.local.models.TranslationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TranslationDao {

    @Query("SELECT * FROM translation")
    fun getAll(): Flow<List<TranslationEntity>>

    @Query("SELECT * FROM translation WHERE isDeletedFromHistory != 1 ORDER BY created DESC")
    fun getAllHistory(): Flow<List<TranslationEntity>>

    fun getAllSelected(searchQuery: String, sortOrder: SortOrder): Flow<List<TranslationEntity>> =
        when(sortOrder) {
            SortOrder.BY_NAME -> getAllSelectedSortedByText(searchQuery)
            SortOrder.BY_DATE -> getAllSelectedSortedByDateCreated(searchQuery)
        }

    @Query("SELECT * FROM translation WHERE isSelected = 1 AND " +
            "(sourceText LIKE '%' || :searchQuery || '%' OR text LIKE '%' || :searchQuery || '%')" +
            " ORDER BY sourceText ")
    fun getAllSelectedSortedByText(searchQuery: String): Flow<List<TranslationEntity>>

    @Query("SELECT * FROM translation WHERE isSelected = 1 AND " +
            "(sourceText LIKE '%' || :searchQuery || '%' OR text LIKE '%' || :searchQuery || '%')" +
            " ORDER BY created DESC")
    fun getAllSelectedSortedByDateCreated(searchQuery: String): Flow<List<TranslationEntity>>

    @Upsert
    suspend fun upsert(translationEntity: TranslationEntity)

    @Delete
    suspend fun delete(translationEntity: TranslationEntity)

    @Query("DELETE FROM translation WHERE isSelected != 1")
    suspend fun deleteAllHistory()

    @Query("DELETE FROM translation WHERE isSelected")
    suspend fun deleteAllSelected()

}