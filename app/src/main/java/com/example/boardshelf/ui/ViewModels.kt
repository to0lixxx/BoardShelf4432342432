package com.example.boardshelf.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.boardshelf.data.*
import com.example.boardshelf.domain.CollectionStats
import com.example.boardshelf.domain.toFilter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CollectionUiState(
    val games: List<GameWithDetails> = emptyList(),
    val smartLists: List<SmartListEntity> = emptyList(),
    val selectedListId: Long? = null,
    val query: String = "",
    val stats: CollectionStats = CollectionStats()
)

@HiltViewModel
class CollectionViewModel @Inject constructor(
    private val repo: CollectionRepository
) : ViewModel() {

    private val selectedList = MutableStateFlow<Long?>(null)
    private val query = MutableStateFlow("")

    val state: StateFlow<CollectionUiState> = combine(
        repo.games, repo.smartLists, selectedList, query
    ) { games, lists, sel, q ->
        val filter = lists.firstOrNull { it.id == sel }?.toFilter()
        CollectionUiState(
            games = games.filter {
                (filter?.matches(it) ?: true) && it.game.name.contains(q, ignoreCase = true)
            },
            smartLists = lists, selectedListId = sel, query = q,
            stats = CollectionStats.from(games)
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CollectionUiState())

    init { viewModelScope.launch { repo.seedPresets() } }

    fun selectList(id: Long?) { selectedList.value = id }
    fun setQuery(q: String) { query.value = q }
    fun addGame(game: GameEntity) = viewModelScope.launch { repo.addGame(game) }
    fun addSmartList(list: SmartListEntity) = viewModelScope.launch { repo.createSmartList(list) }
    fun deleteSmartList(list: SmartListEntity) = viewModelScope.launch {
        selectedList.value = null
        repo.deleteSmartList(list)
    }
}

@HiltViewModel
class GameDetailViewModel @Inject constructor(
    private val repo: CollectionRepository,
    savedState: SavedStateHandle
) : ViewModel() {
    private val gameId: Long = checkNotNull(savedState["gameId"])

    val game: StateFlow<GameWithDetails?> = repo.game(gameId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    val allTags: StateFlow<List<TagEntity>> = repo.tags
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addCopy(copy: CopyEntity) = viewModelScope.launch { repo.addCopy(copy.copy(gameId = gameId)) }
    fun deleteCopy(copy: CopyEntity) = viewModelScope.launch { repo.deleteCopy(copy) }
    fun setStatus(status: GameStatus) = viewModelScope.launch {
        game.value?.game?.let { repo.updateGame(it.copy(status = status)) }
    }
    fun toggleTag(tagId: Long, enabled: Boolean) = viewModelScope.launch { repo.setTag(gameId, tagId, enabled) }
    fun createTag(name: String) = viewModelScope.launch {
        if (name.isBlank()) return@launch
        val id = repo.createTag(name)
        if (id > 0) repo.setTag(gameId, id, true)
    }
    fun deleteGame(onDone: () -> Unit) = viewModelScope.launch {
        game.value?.game?.let { repo.deleteGame(it) }
        onDone()
    }
}
