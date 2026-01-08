package br.com.passguardian.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import br.com.passguardian.ui.copyToClipboard
import br.com.passguardian.ui.navigation.Route
import br.com.passguardian.viewmodel.PasswordViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordEditScreen(
    vm: PasswordViewModel,
    id: String,
    onDone: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val current by vm.current.collectAsState()
    val loading by vm.loading.collectAsState()
    val error by vm.error.collectAsState()

    var title by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val isNew = id == Route.NEW_ID

    var showPassword by remember { mutableStateOf(false) }
    var secondsLeft by remember { mutableStateOf(0) }

    val snackbarHostState = remember { SnackbarHostState() }

    // Mostrar senha por 10s (contador)
    LaunchedEffect(showPassword) {
        if (showPassword) {
            secondsLeft = 10
            while (secondsLeft > 0) {
                delay(1000)
                secondsLeft -= 1
            }
            showPassword = false
        } else {
            secondsLeft = 0
        }
    }

    LaunchedEffect(id) {
        if (isNew) {
            vm.clearCurrent()
            title = ""
            username = ""
            password = ""
        } else {
            vm.loadOneDecrypted(id)
        }
    }

    LaunchedEffect(current?.id) {
        if (!isNew && current != null) {
            title = current!!.title
            username = current!!.username
            password = current!!.password
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            title = ""
            username = ""
            password = ""
            showPassword = false
            secondsLeft = 0
            vm.clearCurrent()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isNew) "Adicionar" else "Editar") },
                navigationIcon = {
                    IconButton(onClick = onDone) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
                actions = {
                    if (!isNew && !loading && error == null) {
                        IconButton(
                            onClick = {
                                if (username.isNotBlank()) {
                                    copyToClipboard(context, "Usuário", username)
                                    scope.launch { snackbarHostState.showSnackbar("Usuário copiado") }
                                }
                            },
                            enabled = username.isNotBlank()
                        ) {
                            Icon(Icons.Filled.ContentCopy, contentDescription = "Copiar usuário")
                        }

                        IconButton(
                            onClick = {
                                copyToClipboard(context, "Senha", password)
                                scope.launch { snackbarHostState.showSnackbar("Senha copiada") }
                            }
                        ) {
                            Icon(Icons.Filled.ContentCopy, contentDescription = "Copiar senha")
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            if (loading) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    CircularProgressIndicator()
                }
            }

            if (error != null) {
                Text("Erro: $error", color = MaterialTheme.colorScheme.error)
            }

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Título") },
                placeholder = { Text("Ex: YouTube") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Usuário (opcional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Senha") },
                visualTransformation = if (showPassword)
                    androidx.compose.ui.text.input.VisualTransformation.None
                else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(
                        onClick = { showPassword = !showPassword },
                        enabled = !loading && password.isNotBlank()
                    ) {
                        Icon(
                            imageVector = if (showPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = if (showPassword) "Esconder" else "Mostrar"
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            if (showPassword && secondsLeft > 0) {
                Text(
                    "Visível por $secondsLeft s",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(Modifier.height(4.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                TextButton(
                    onClick = onDone,
                    modifier = Modifier.weight(1f)
                ) { Text("Cancelar") }

                Button(
                    onClick = {
                        if (isNew) vm.add(title, username, password, onDone)
                        else vm.update(id, title, username, password, onDone)
                    },
                    enabled = title.isNotBlank() && password.isNotBlank() && !loading,
                    modifier = Modifier.weight(1f)
                ) {
                    if (loading) {
                        CircularProgressIndicator(
                            strokeWidth = 2.dp,
                            modifier = Modifier
                                .height(18.dp)
                                .width(18.dp)
                        )
                        Spacer(Modifier.width(10.dp))
                        Text("Salvando…")
                    } else {
                        Text("Salvar")
                    }
                }
            }
        }
    }
}