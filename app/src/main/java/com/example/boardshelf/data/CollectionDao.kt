package com.example.boardshelf.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CollectionDao {
    // Игры
    @Transaction
    @Query("SELECT * FROM games ORDER BY name COLLATE NOCASE")
    fun observeGames(): Flow<List<GameWithDetails>>

    @Transaction
    @Query("SELECT * FROM games WHERE id = :id")
    fun observeGame(id: Long): Flow<GameWithDetails?>

    @Insert suspend fun insertGame(game: GameEntity): Long
    @Update suspend fun updateGame(game: GameEntity)
    @Delete suspend fun deleteGame(game: GameEntity)

    // Экземпляры
    @Insert suspend fun insertCopy(copy: CopyEntity): Long
    @Update suspend fun updateCopy(copy: CopyEntity)
    @Delete suspend fun deleteCopy(copy: CopyEntity)

    // Теги
    @Query("SELECT * FROM tags ORDER BY name COLLATE NOCASE")
    fun observeTags(): Flow<List<TagEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE) suspend fun insertTag(tag: TagEntity): Long
    @Delete suspend fun deleteTag(tag: TagEntity)
    @Insert(onConflict = OnConflictStrategy.IGNORE) suspend fun attachTag(ref: GameTagCrossRef)
    @Delete suspend fun detachTag(ref: GameTagCrossRef)

    // Умные списки
    @Query("SELECT * FROM smart_lists ORDER BY isPreset DESC, name COLLATE NOCASE")
    fun observeSmartLists(): Flow<List<SmartListEntity>>

    @Query("SELECT COUNT(*) FROM smart_lists")
    suspend fun smartListCount(): Int

    @Insert suspend fun insertSmartList(list: SmartListEntity): Long
    @Delete suspend fun deleteSmartList(list: SmartListEntity)
}
