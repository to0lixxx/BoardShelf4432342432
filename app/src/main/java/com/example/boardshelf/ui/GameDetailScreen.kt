package com.example.boardshelf.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.boardshelf.data.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GameDetailScreen(onBack: () -> Unit, vm: GameDetailViewModel = hiltViewModel()) {
    val details by vm.game.collectAsState()
    val allTags by vm.allTags.collectAsState()
    var showAddCopy by remember { mutableStateOf(false) }
    var newTag by remember { mutableStateOf("") }

    val item = details ?: return
    val g = item.game

    LazyColumn(
        Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row {
                TextButton(onBack) { Text("← Назад") }
                Spacer(Modifier.weight(1f))
                TextButton({ vm.deleteGame(onBack) }) { Text("Удалить игру") }
            }
            Text(g.name, style = MaterialTheme.typography.headlineMedium)
            Text(
                "${g.minPlayers}–${g.maxPlayers} игроков · ${g.playTimeMin} мин" +
                    (if (g.isCooperative) " · кооператив" else ""),
                style = MaterialTheme.typography.bodyMedium
            )
        }

        item {
            Text("Статус", style = MaterialTheme.typography.titleSmall)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(GameStatus.entries) { s ->
                    FilterChip(g.status == s, { vm.setStatus(s) }, label = { Text(s.label) })
                }
            }
        }

        item {
            Text("Теги", style = MaterialTheme.typography.titleSmall)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                allTags.forEach { tag ->
                    val on = item.tags.any { it.id == tag.id }
                    FilterChip(on, { vm.toggleTag(tag.id, !on) }, label = { Text(tag.name) })
                }
            }
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                OutlinedTextField(
                    newTag, { newTag = it }, label = { Text("Новый тег") },
                    singleLine = true, modifier = Modifier.weight(1f)
                )
                TextButton(
                    enabled = newTag.isNotBlank(),
                    onClick = { vm.createTag(newTag); newTag = "" }
                ) { Text("Добавить") }
            }
        }

        item {
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Text("Экземпляры (${item.copies.size})", style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.weight(1f))
                Button({ showAddCopy = true }) { Text("+ Экземпляр") }
            }
        }

        items(item.copies, key = { it.id }) { copy ->
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp)) {
                    Text("Коробка: ${copy.condition.label}", style = MaterialTheme.typography.titleSmall)
                    copy.purchasePrice?.let { Text("Цена: %,.2f %s".format(it, copy.currency)) }
                    if (copy.purchasePlace.isNotBlank()) Text("Где куплена: ${copy.purchasePlace}")
                    if (copy.shelfLocation.isNotBlank()) Text("Место: ${copy.shelfLocation}")
                    if (copy.language.isNotBlank()) Text("Язык: ${copy.language}")
                    TextButton({ vm.deleteCopy(copy) }) { Text("Удалить") }
                }
            }
        }
    }

    if (showAddCopy) AddCopyDialog(
        gameId = g.id,
        onDismiss = { showAddCopy = false },
        onSave = { vm.addCopy(it); showAddCopy = false }
    )
}

@Composable
private fun AddCopyDialog(gameId: Long, onDismiss: () -> Unit, onSave: (CopyEntity) -> Unit) {
    var condition by remember { mutableStateOf(BoxCondition.GOOD) }
    var price by remember { mutableStateOf("") }
    var currency by remember { mutableStateOf("RUB") }
    var place by remember { mutableStateOf("") }
    var shelf by remember { mutableStateOf("") }
    var language by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Новый экземпляр") },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                item {
                    Text("Состояние коробки", style = MaterialTheme.typography.labelMedium)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(BoxCondition.entries) { c ->
                            FilterChip(condition == c, { condition = c }, label = { Text(c.label) })
                        }
                    }
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(price, { price = it }, label = { Text("Цена") }, singleLine = true, modifier = Modifier.weight(2f))
                        OutlinedTextField(currency, { currency = it.uppercase() }, label = { Text("Валюта") }, singleLine = true, modifier = Modifier.weight(1f))
                    }
                }
                item { OutlinedTextField(place, { place = it }, label = { Text("Где куплена") }, singleLine = true) }
                item { OutlinedTextField(shelf, { shelf = it }, label = { Text("Место на полке") }, singleLine = true) }
                item { OutlinedTextField(language, { language = it }, label = { Text("Язык издания") }, singleLine = true) }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onSave(
                    CopyEntity(
                        gameId = gameId, condition = condition,
                        purchasePrice = price.replace(',', '.').toDoubleOrNull(),
                        currency = currency.ifBlank { "RUB" },
                        purchasePlace = place.trim(), shelfLocation = shelf.trim(),
                        language = language.trim(), purchasedAt = System.currentTimeMillis()
                    )
                )
            }) { Text("Сохранить") }
        },
        dismissButton = { TextButton(onDismiss) { Text("Отмена") } }
    )
}
