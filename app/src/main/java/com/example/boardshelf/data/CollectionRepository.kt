package com.example.boardshelf.data

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CollectionRepository @Inject constructor(private val dao: CollectionDao) {
    val games = dao.observeGames()
    val tags = dao.observeTags()
    val smartLists = dao.observeSmartLists()

    fun game(id: Long) = dao.observeGame(id)

    suspend fun addGame(game: GameEntity) = dao.insertGame(game)
    suspend fun updateGame(game: GameEntity) = dao.updateGame(game)
    suspend fun deleteGame(game: GameEntity) = dao.deleteGame(game)

    suspend fun addCopy(copy: CopyEntity) = dao.insertCopy(copy)
    suspend fun deleteCopy(copy: CopyEntity) = dao.deleteCopy(copy)

    suspend fun createTag(name: String): Long = dao.insertTag(TagEntity(name = name.trim()))
    suspend fun setTag(gameId: Long, tagId: Long, enabled: Boolean) {
        val ref = GameTagCrossRef(gameId, tagId)
        if (enabled) dao.attachTag(ref) else dao.detachTag(ref)
    }

    suspend fun createSmartList(list: SmartListEntity) = dao.insertSmartList(list)
    suspend fun deleteSmartList(list: SmartListEntity) = dao.deleteSmartList(list)

    /** Стартовые умные списки создаются один раз при первом запуске. */
    suspend fun seedPresets() {
        if (dao.smartListCount() > 0) return
        dao.insertSmartList(SmartListEntity(name = "До 45 минут", maxPlayTime = 45, isPreset = true))
        dao.insertSmartList(SmartListEntity(name = "На двоих", players = 2, isPreset = true))
        dao.insertSmartList(SmartListEntity(name = "Кооператив", cooperative = true, isPreset = true))
    }
}
