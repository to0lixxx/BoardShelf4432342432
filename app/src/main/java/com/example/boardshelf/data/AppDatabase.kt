package com.example.boardshelf.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [GameEntity::class, CopyEntity::class, TagEntity::class,
        GameTagCrossRef::class, SmartListEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dao(): CollectionDao
}
