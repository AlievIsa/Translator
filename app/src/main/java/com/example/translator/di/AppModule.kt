package com.example.translator.di

import android.content.Context
import androidx.room.Room
import com.example.translator.data.local.TranslatorDB
import com.example.translator.data.remote.authentication.AuthWithEmailAndPassword
import com.example.translator.data.remote.authentication.FirebaseAuthImpl
import com.example.translator.data.remote.translation.api.GoogleTranslatorApi
import com.example.translator.data.remote.translation.api.TranslatorApiService
import com.example.translator.data.remote.translation.sync.FirebaseSyncImpl
import com.example.translator.data.remote.translation.sync.SyncTranslations
import com.example.translator.domain.Repository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    private val API_KEY = "63aeb83176msh8119d30189d8d83p19e5c3jsne50ebdda1774"

    @Provides
    @Singleton
    fun provideTranslatorDB(@ApplicationContext context: Context): TranslatorDB {
        return Room.databaseBuilder(
            context,
            TranslatorDB::class.java,
            "translator.db"
        ).build()
    }

    @Provides
    @Singleton
    fun provideTranslatorApi(): TranslatorApiService {
        val apiKey = API_KEY
        return GoogleTranslatorApi(apiKey)
    }

    @Provides
    @Singleton
    fun provideFirebaseAuthImpl(): AuthWithEmailAndPassword {
        return FirebaseAuthImpl()
    }

    @Provides
    @Singleton
    fun provideTranslationSync(): SyncTranslations {
        return FirebaseSyncImpl()
    }

    @Provides
    @Singleton
    fun provideRepository(
        @ApplicationContext context: Context,
        translatorDB: TranslatorDB,
        translatorApi: TranslatorApiService,
        firebaseAuth: AuthWithEmailAndPassword,
        firebaseSync: SyncTranslations
    ): Repository {
        return Repository(context, translatorDB, translatorApi, firebaseAuth, firebaseSync)
    }
}