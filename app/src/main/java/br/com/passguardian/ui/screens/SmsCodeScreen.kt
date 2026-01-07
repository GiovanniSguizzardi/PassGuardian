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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import br.com.passguardian.viewmodel.AuthState
import br.com.passguardian.viewmodel.AuthViewModel

@Composable
fun SmsCodeScreen(
    authVm: AuthViewModel,
    onDone: () -> Unit
) {
    val state by authVm.state.collectAsState()
    var code by remember { mutableStateOf("") }

    LaunchedEffect(state) {
        if (state is AuthState.LoggedIn) onDone()
    }

    val loading = state is AuthState.Loading
    val error = (state as? AuthState.Error)?.message

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Confirmação por SMS", style = MaterialTheme.typography.headlineMedium)
        Text("Digite o código enviado para seu telefone")

        OutlinedTextField(
            value = code,
            onValueChange = { code = it },
            label = { Text("Código") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = { authVm.submitSmsCode(code) },
            enabled = code.length >= 4 && !loading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (loading) "Verificando..." else "Confirmar")
        }

        if (error != null) {
            Text("Erro: $error", color = MaterialTheme.colorScheme.error)
        }
    }
}