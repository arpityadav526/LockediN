package com.lockedin.di

import android.content.Context
import androidx.room.Room
import com.lockedin.data.db.AppDatabase
import com.lockedin.data.db.dao.ChatMessageDao
import com.lockedin.data.db.dao.FileDao
import com.lockedin.data.db.dao.NoteDao
import com.lockedin.feature.aichat.GeminiService
import com.lockedin.feature.tools.dictionary.DictionaryApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "lockedin_database"
        ).build()

    @Provides
    fun provideFileDao(database: AppDatabase): FileDao = database.fileDao()

    @Provides
    fun provideNoteDao(database: AppDatabase): NoteDao = database.noteDao()

    @Provides
    fun provideChatMessageDao(database: AppDatabase): ChatMessageDao = database.chatMessageDao()

    @Provides
    @Singleton
    fun provideGeminiService(@Named("gemini") retrofit: Retrofit): GeminiService =
        retrofit.create(GeminiService::class.java)

    @Provides
    @Singleton
    fun provideDictionaryApiService(@Named("dictionary") retrofit: Retrofit): DictionaryApiService =
        retrofit.create(DictionaryApiService::class.java)
}
