package br.com.passguardian.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import br.com.passguardian.viewmodel.AuthState
import br.com.passguardian.viewmodel.AuthViewModel

@Composable
fun LoginScreen(
    authVm: AuthViewModel,
    onLoggedIn: () -> Unit,
    onNeedsSms: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? FragmentActivity

    val state by authVm.state.collectAsState()

    LaunchedEffect(state) {
        when (state) {
            is AuthState.LoggedIn -> onLoggedIn()
            is AuthState.NeedsSmsCode -> onNeedsSms()
            else -> {}
        }
    }

    val loading = state is AuthState.Loading
    val error = (state as? AuthState.Error)?.message

    Column(
        modifier = Modifier.fillMaxSize().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("PassGuardian", style = MaterialTheme.typography.headlineMedium)
        Text("Entre com sua conta Google")

        Button(
            onClick = {
                if (activity != null) authVm.signInWithGoogle(activity)
            },
            enabled = !loading && activity != null,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (loading) "Entrando..." else "Entrar com Google")
        }

        if (error != null) {
            Text("Erro: $error", color = MaterialTheme.colorScheme.error)
        }
    }
}