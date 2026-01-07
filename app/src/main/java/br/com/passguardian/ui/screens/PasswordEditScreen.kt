package br.com.passguardian.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import br.com.passguardian.ui.navigation.Route
import br.com.passguardian.viewmodel.PasswordViewModel

@Composable
fun PasswordEditScreen(
    vm: PasswordViewModel,
    id: String,
    onDone: () -> Unit
) {
    val current by vm.current.collectAsState()
    val loading by vm.loading.collectAsState()
    val error by vm.error.collectAsState()

    var title by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val isNew = id == Route.NEW_ID

    LaunchedEffect(id) {
        if (isNew) {
            vm.clearCurrent()
            title = ""
            password = ""
        } else {
            vm.loadOne(id)
        }
    }

    // Preenche quando carregar o item
    LaunchedEffect(current?.id) {
        if (!isNew && current != null) {
            title = current!!.title
            password = current!!.password
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = if (isNew) "Adicionar senha" else "Editar senha",
            style = MaterialTheme.typography.headlineMedium
        )

        if (loading) Text("Carregando...")
        if (error != null) Text("Erro: $error", color = MaterialTheme.colorScheme.error)

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Título (ex: YouTube)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Senha") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Button(
            onClick = {
                if (isNew) vm.add(title, password, onDone)
                else vm.update(id, title, password, onDone)
            },
            enabled = title.isNotBlank() && password.isNotBlank() && !loading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Salvar")
        }
    }
}