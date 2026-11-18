package com.example.boardshelf.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.boardshelf.data.*

@Composable
fun CollectionScreen(
    onOpenGame: (Long) -> Unit,
    vm: CollectionViewModel = hiltViewModel()
) {
    val state by vm.state.collectAsState()
    var showAddGame by remember { mutableStateOf(false) }
    var showAddList by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddGame = true }) {
                Icon(Icons.Default.Add, contentDescription = "Добавить игру")
            }
        }
    ) { padding ->
        Column(Modifier.padding(padding).padding(horizontal = 16.dp)) {
            Spacer(Modifier.height(16.dp))
            Text("Моя коллекция", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(12.dp))

            // Стоимость коллекции
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("Стоимость коллекции", style = MaterialTheme.typography.labelLarge)
                    Text(state.stats.valueText(), style = MaterialTheme.typography.headlineSmall)
                    Text(
                        "${state.stats.ownedGames} игр · ${state.stats.ownedCopies} экз.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            OutlinedTextField(
                value = state.query, onValueChange = vm::setQuery,
                label = { Text("Поиск по названию") }, singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            )

            // Умные списки
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(
                        selected = state.selectedListId == null,
                        onClick = { vm.selectList(null) }, label = { Text("Все") }
                    )
                }
                items(state.smartLists, key = { it.id }) { list ->
                    FilterChip(
                        selected = state.selectedListId == list.id,
                        onClick = { vm.selectList(list.id) }, label = { Text(list.name) }
                    )
                }
                item { AssistChip(onClick = { showAddList = true }, label = { Text("+ Список") }) }
            }

            state.smartLists.firstOrNull { it.id == state.selectedListId && !it.isPreset }?.let { l ->
                TextButton(onClick = { vm.deleteSmartList(l) }) { Text("Удалить список «${l.name}»") }
            }

            LazyColumn(
                contentPadding = PaddingValues(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.games, key = { it.game.id }) { item ->
                    GameRow(item, onClick = { onOpenGame(item.game.id) })
                }
            }
        }
    }

    if (showAddGame) AddGameDialog(
        onDismiss = { showAddGame = false },
        onSave = { vm.addGame(it); showAddGame = false }
    )
    if (showAddList) AddSmartListDialog(
        onDismiss = { showAddList = false },
        onSave = { vm.addSmartList(it); showAddList = false }
    )
}

@Composable
private fun GameRow(item: GameWithDetails, onClick: () -> Unit) {
    val g = item.game
    Card(Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(g.name, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                AssistChip(onClick = onClick, label = { Text(g.status.label) })
            }
            Text(
                "${g.minPlayers}–${g.maxPlayers} игроков · ${g.playTimeMin} мин" +
                    (if (g.isCooperative) " · кооператив" else "") +
                    (g.weight?.let { " · вес %.1f".format(it) } ?: ""),
                style = MaterialTheme.typography.bodySmall
            )
            if (item.tags.isNotEmpty())
                Text(item.tags.joinToString(" ") { "#${it.name}" }, style = MaterialTheme.typography.labelSmall)
            if (item.copies.size > 1)
                Text("Экземпляров: ${item.copies.size}", style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun AddGameDialog(onDismiss: () -> Unit, onSave: (GameEntity) -> Unit) {
    var name by remember { mutableStateOf("") }
    var minP by remember { mutableStateOf("2") }
    var maxP by remember { mutableStateOf("4") }
    var time by remember { mutableStateOf("60") }
    var weight by remember { mutableStateOf("") }
    var coop by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf(GameStatus.OWNED) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Новая игра") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedTextField(name, { name = it }, label = { Text("Название") }, singleLine = true)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(minP, { minP = it }, label = { Text("Мин.") }, singleLine = true, modifier = Modifier.weight(1f))
                    OutlinedTextField(maxP, { maxP = it }, label = { Text("Макс.") }, singleLine = true, modifier = Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(time, { time = it }, label = { Text("Минут") }, singleLine = true, modifier = Modifier.weight(1f))
                    OutlinedTextField(weight, { weight = it }, label = { Text("Вес 1–5") }, singleLine = true, modifier = Modifier.weight(1f))
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(coop, { coop = it }); Spacer(Modifier.width(8.dp)); Text("Кооператив")
                }
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(GameStatus.entries) { s ->
                        FilterChip(status == s, { status = s }, label = { Text(s.label) })
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = name.isNotBlank(),
                onClick = {
                    val mn = minP.toIntOrNull() ?: 1
                    onSave(
                        GameEntity(
                            name = name.trim(), minPlayers = mn,
                            maxPlayers = maxOf(mn, maxP.toIntOrNull() ?: mn),
                            playTimeMin = time.toIntOrNull() ?: 60,
                            weight = weight.replace(',', '.').toFloatOrNull(),
                            isCooperative = coop, status = status
                        )
                    )
                }
            ) { Text("Сохранить") }
        },
        dismissButton = { TextButton(onDismiss) { Text("Отмена") } }
    )
}

@Composable
private fun AddSmartListDialog(onDismiss: () -> Unit, onSave: (SmartListEntity) -> Unit) {
    var name by remember { mutableStateOf("") }
    var maxTime by remember { mutableStateOf("") }
    var players by remember { mutableStateOf("") }
    var coop by remember { mutableStateOf(false) }
    var maxWeight by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Умный список") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedTextField(name, { name = it }, label = { Text("Название") }, singleLine = true)
                OutlinedTextField(maxTime, { maxTime = it }, label = { Text("Не дольше, мин (необяз.)") }, singleLine = true)
                OutlinedTextField(players, { players = it }, label = { Text("Число игроков (необяз.)") }, singleLine = true)
                OutlinedTextField(maxWeight, { maxWeight = it }, label = { Text("Вес не выше (необяз.)") }, singleLine = true)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(coop, { coop = it }); Spacer(Modifier.width(8.dp)); Text("Только кооперативные")
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = name.isNotBlank(),
                onClick = {
                    onSave(
                        SmartListEntity(
                            name = name.trim(),
                            maxPlayTime = maxTime.toIntOrNull(),
                            players = players.toIntOrNull(),
                            maxWeight = maxWeight.replace(',', '.').toFloatOrNull(),
                            cooperative = if (coop) true else null
                        )
                    )
                }
            ) { Text("Создать") }
        },
        dismissButton = { TextButton(onDismiss) { Text("Отмена") } }
    )
}
