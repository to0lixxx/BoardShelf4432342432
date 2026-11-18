package com.example.boardshelf.data

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.Junction
import androidx.room.PrimaryKey
import androidx.room.Relation

enum class GameStatus(val label: String) {
    OWNED("Есть"), WISHLIST("Хочу"), PLAYED("Играл"), SOLD("Продал")
}

enum class BoxCondition(val label: String) {
    NEW_SEALED("Запечатана"), LIKE_NEW("Как новая"), GOOD("Хорошая"),
    FAIR("Потёртая"), POOR("Плохая")
}

@Entity(tableName = "games")
data class GameEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val bggId: Int? = null,
    val name: String,
    val year: Int? = null,
    val minPlayers: Int = 1,
    val maxPlayers: Int = 4,
    val playTimeMin: Int = 60,
    val weight: Float? = null,          // сложность 1..5 как на BGG
    val isCooperative: Boolean = false,
    val status: GameStatus = GameStatus.OWNED,
    val rating: Float? = null,
    val notes: String = "",
    val addedAt: Long = System.currentTimeMillis()
)

/** Конкретный экземпляр (копия) игры. У одной игры их может быть несколько. */
@Entity(
    tableName = "copies",
    foreignKeys = [ForeignKey(
        entity = GameEntity::class, parentColumns = ["id"],
        childColumns = ["gameId"], onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("gameId")]
)
data class CopyEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val gameId: Long,
    val condition: BoxCondition = BoxCondition.GOOD,
    val purchasePrice: Double? = null,
    val currency: String = "RUB",
    val purchasePlace: String = "",
    val shelfLocation: String = "",
    val language: String = "",
    val purchasedAt: Long? = null
)

@Entity(tableName = "tags", indices = [Index("name", unique = true)])
data class TagEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String
)

@Entity(
    tableName = "game_tags",
    primaryKeys = ["gameId", "tagId"],
    foreignKeys = [
        ForeignKey(GameEntity::class, ["id"], ["gameId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(TagEntity::class, ["id"], ["tagId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("tagId")]
)
data class GameTagCrossRef(val gameId: Long, val tagId: Long)

/** Умный список = сохранённый набор условий. Пустое условие (null) не фильтрует. */
@Entity(tableName = "smart_lists")
data class SmartListEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val maxPlayTime: Int? = null,
    val players: Int? = null,
    val cooperative: Boolean? = null,
    val maxWeight: Float? = null,
    @ColumnInfo(defaultValue = "") val requiredTagIds: String = "",   // "1,5,7"
    val status: GameStatus? = null,
    val isPreset: Boolean = false
)

data class GameWithDetails(
    @Embedded val game: GameEntity,
    @Relation(parentColumn = "id", entityColumn = "gameId")
    val copies: List<CopyEntity>,
    @Relation(
        parentColumn = "id", entityColumn = "id",
        associateBy = Junction(GameTagCrossRef::class, parentColumn = "gameId", entityColumn = "tagId")
    )
    val tags: List<TagEntity>
)
