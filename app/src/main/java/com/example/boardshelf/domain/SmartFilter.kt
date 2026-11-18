package com.example.boardshelf.domain

import com.example.boardshelf.data.GameStatus
import com.example.boardshelf.data.GameWithDetails
import com.example.boardshelf.data.SmartListEntity

data class SmartFilter(
    val maxPlayTime: Int? = null,
    val players: Int? = null,
    val cooperative: Boolean? = null,
    val maxWeight: Float? = null,
    val requiredTagIds: Set<Long> = emptySet(),
    val status: GameStatus? = null
) {
    fun matches(item: GameWithDetails): Boolean {
        val g = item.game
        if (maxPlayTime != null && g.playTimeMin > maxPlayTime) return false
        if (players != null && players !in g.minPlayers..g.maxPlayers) return false
        if (cooperative != null && g.isCooperative != cooperative) return false
        if (maxWeight != null && (g.weight ?: Float.MAX_VALUE) > maxWeight) return false
        if (status != null && g.status != status) return false
        if (requiredTagIds.isNotEmpty() && !item.tags.map { it.id }.containsAll(requiredTagIds)) return false
        return true
    }
}

fun SmartListEntity.toFilter() = SmartFilter(
    maxPlayTime, players, cooperative, maxWeight,
    requiredTagIds.split(",").mapNotNull { it.trim().toLongOrNull() }.toSet(),
    status
)

/** Сколько стоит коллекция. Валюты не смешиваются: сумма считается отдельно по каждой. */
data class CollectionStats(
    val ownedGames: Int = 0,
    val ownedCopies: Int = 0,
    val valueByCurrency: Map<String, Double> = emptyMap()
) {
    fun valueText(): String =
        if (valueByCurrency.isEmpty()) "—"
        else valueByCurrency.entries.joinToString(" · ") { "%,.0f %s".format(it.value, it.key) }

    companion object {
        fun from(games: List<GameWithDetails>): CollectionStats {
            val owned = games.filter { it.game.status == GameStatus.OWNED }
            val copies = owned.flatMap { it.copies }
            return CollectionStats(
                ownedGames = owned.size,
                ownedCopies = copies.size,
                valueByCurrency = copies
                    .filter { it.purchasePrice != null }
                    .groupBy { it.currency.ifBlank { "—" } }
                    .mapValues { (_, list) -> list.sumOf { it.purchasePrice ?: 0.0 } }
            )
        }
    }
}
