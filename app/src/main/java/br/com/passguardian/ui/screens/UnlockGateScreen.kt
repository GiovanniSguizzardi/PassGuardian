package br.com.passguardian.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import br.com.passguardian.security.AccessGate
import br.com.passguardian.security.BiometricHelper
import br.com.passguardian.security.BiometricResult
import br.com.passguardian.viewmodel.AuthState
import br.com.passguardian.viewmodel.AuthViewModel

@Composable
fun UnlockGateScreen(
    authVm: AuthViewModel,
    onUnlocked: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? FragmentActivity
    val gate = remember { AccessGate(context) }

    val authState by authVm.state.collectAsState()
    var error by remember { mutableStateOf<String?>(null) }

    // Janela de 10 min
    val windowMs = 10 * 60 * 1000L

    // Se já está desbloqueado, entra direto (UX boa)
    LaunchedEffect(Unit) {
        if (gate.isUnlocked(windowMs)) onUnlocked()
    }

    // Se usuário fez reauth e voltou LoggedIn, marca e segue
    LaunchedEffect(authState) {
        if (authState is AuthState.LoggedIn) {
            gate.markUnlockedNow()
            onUnlocked()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Confirmação necessária", style = MaterialTheme.typography.headlineMedium)
        Text("Para ver ou editar esta senha, confirme sua identidade.")

        Button(
            onClick = {
                error = null
                if (activity == null) {
                    error = "Activity inválida para biometria"
                    return@Button
                }

                BiometricHelper.authenticate(
                    activity = activity,
                    title = "Desbloquear",
                    subtitle = "Use sua biometria para continuar",
                    onResult = { res ->
                        when (res) {
                            is BiometricResult.Success -> {
                                gate.markUnlockedNow()
                                onUnlocked()
                            }
                            is BiometricResult.NotAvailable -> {
                                error = "Biometria indisponível neste dispositivo."
                            }
                            is BiometricResult.Failed -> {
                                error = res.message
                            }
                        }
                    }
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Usar biometria")
        }

        OutlinedButton(
            onClick = {
                // fallback: reauth Google + (se tiver) MFA por SMS
                if (activity == null) {
                    error = "Activity inválida para reautenticação"
                    return@OutlinedButton
                }
                authVm.reauthWithGoogle(activity)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Usar código SMS (fallback)")
        }

        if (error != null) {
            Text("Erro: $error", color = MaterialTheme.colorScheme.error)
        }
    }
}