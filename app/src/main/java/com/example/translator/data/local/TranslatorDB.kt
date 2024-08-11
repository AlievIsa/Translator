package com.example.translator.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.translator.data.local.models.TranslationEntity

@Database(entities = [TranslationEntity::class], version = 2)
abstract class TranslatorDB: RoomDatabase() {

    abstract val translationDao: TranslationDao

}