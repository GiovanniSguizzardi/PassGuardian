package br.com.passguardian.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import br.com.passguardian.data.model.PasswordItem
import br.com.passguardian.viewmodel.PasswordViewModel

@Composable
fun PasswordListScreen(
    vm: PasswordViewModel,
    onAdd: () -> Unit,
    onEdit: (String) -> Unit
) {
    val items by vm.items.collectAsState()
    val loading by vm.loading.collectAsState()
    val error by vm.error.collectAsState()

    LaunchedEffect(Unit) { vm.loadList() }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Senhas", style = MaterialTheme.typography.headlineMedium)
            Button(onClick = onAdd) { Text("Adicionar") }
        }

        if (loading) Text("Carregando...")
        if (error != null) Text("Erro: $error", color = MaterialTheme.colorScheme.error)

        if (!loading && items.isEmpty()) {
            Text("Nenhuma senha cadastrada ainda...")
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(items) { item ->
                PasswordRow(
                    item = item,
                    onOpen = { onEdit(item.id) },
                    onDelete = { vm.delete(item.id) }
                )
            }
        }
    }
}

@Composable
private fun PasswordRow(
    item: PasswordItem,
    onOpen: () -> Unit,
    onDelete: () -> Unit
) {
    Card {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(item.title, style = MaterialTheme.typography.titleMedium)
                Text("Toque para ver/editar", style = MaterialTheme.typography.bodySmall)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onOpen) { Text("Abrir") }
                IconButton(onClick = onDelete) { Text("🗑️") }
            }
        }
    }
}