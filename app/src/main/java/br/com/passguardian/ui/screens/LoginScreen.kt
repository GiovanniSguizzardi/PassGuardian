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
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
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
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            androidx.compose.material3.Icon(
                imageVector = Icons.Filled.Lock,
                contentDescription = null
            )
            Spacer(Modifier.width(10.dp))
            Column {
                Text("PassGuardian", style = MaterialTheme.typography.headlineMedium)
                Text(
                    "Suas senhas protegidas no seu dispositivo.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        Spacer(Modifier.height(18.dp))

        OutlinedCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "Entrar",
                    style = MaterialTheme.typography.titleLarge
                )

                Text(
                    "Use sua conta Google. Se sua conta exigir, você vai confirmar via SMS.",
                    style = MaterialTheme.typography.bodyMedium
                )

                Button(
                    onClick = { if (activity != null) authVm.signInWithGoogle(activity) },
                    enabled = !loading && activity != null,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (loading) {
                        CircularProgressIndicator(
                            strokeWidth = 2.dp,
                            modifier = Modifier
                                .height(18.dp)
                                .width(18.dp)
                        )
                        Spacer(Modifier.width(10.dp))
                        Text("Entrando…")
                    } else {
                        Text("Entrar com Google")
                    }
                }

                if (error != null) {
                    Text("Erro: $error", color = MaterialTheme.colorScheme.error)
                }

                if (activity == null) {
                    Text(
                        "Erro: Activity inválida para login.",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}