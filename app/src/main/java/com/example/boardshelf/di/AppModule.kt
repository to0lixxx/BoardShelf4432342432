package com.example.boardshelf.di

import android.content.Context
import androidx.room.Room
import com.example.boardshelf.data.AppDatabase
import com.example.boardshelf.data.CollectionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides @Singleton
    fun db(@ApplicationContext ctx: Context): AppDatabase =
        Room.databaseBuilder(ctx, AppDatabase::class.java, "boardshelf.db").build()

    @Provides
    fun dao(db: AppDatabase): CollectionDao = db.dao()
}
