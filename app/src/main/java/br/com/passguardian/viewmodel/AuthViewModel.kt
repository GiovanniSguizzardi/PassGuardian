package br.com.passguardian.viewmodel

import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthMultiFactorException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.PhoneMultiFactorGenerator
import com.google.firebase.auth.MultiFactorResolver
import com.google.firebase.auth.PhoneMultiFactorInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit

sealed class AuthState {
    data object LoggedOut : AuthState()
    data object Loading : AuthState()
    data object LoggedIn : AuthState()
    data object NeedsSmsCode : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()

    private val _state = MutableStateFlow<AuthState>(
        if (auth.currentUser != null) AuthState.LoggedIn else AuthState.LoggedOut
    )
    val state: StateFlow<AuthState> = _state

    private var pendingResolver: MultiFactorResolver? = null
    private var pendingVerificationId: String? = null

    fun signOut() {
        auth.signOut()
        pendingResolver = null
        pendingVerificationId = null
        _state.value = AuthState.LoggedOut
    }

    // LOGIN INICIAL
    fun signInWithGoogle(activity: FragmentActivity) {
        viewModelScope.launch {
            _state.value = AuthState.Loading
            try {
                val firebaseCred = getGoogleFirebaseCredential(activity)

                try {
                    auth.signInWithCredential(firebaseCred).await()
                    _state.value = AuthState.LoggedIn
                } catch (e: FirebaseAuthMultiFactorException) {
                    startSmsMfa(activity, e.resolver)
                }

            } catch (e: GetCredentialException) {
                _state.value = AuthState.Error(e.message ?: "Falha ao obter credencial do Google")
            } catch (e: Exception) {
                _state.value = AuthState.Error(e.message ?: "Erro inesperado")
            }
        }
    }

    // REAUTH (fallback para desbloquear dentro do app)
    fun reauthWithGoogle(activity: FragmentActivity) {
        viewModelScope.launch {
            _state.value = AuthState.Loading
            try {
                val user = auth.currentUser ?: run {
                    _state.value = AuthState.LoggedOut
                    return@launch
                }

                val firebaseCred = getGoogleFirebaseCredential(activity)

                try {
                    user.reauthenticate(firebaseCred).await()
                    _state.value = AuthState.LoggedIn
                } catch (e: FirebaseAuthMultiFactorException) {
                    startSmsMfa(activity, e.resolver)
                }

            } catch (e: GetCredentialException) {
                _state.value = AuthState.Error(e.message ?: "Falha ao obter credencial do Google")
            } catch (e: Exception) {
                _state.value = AuthState.Error(e.message ?: "Erro inesperado")
            }
        }
    }

    private suspend fun getGoogleFirebaseCredential(activity: FragmentActivity): com.google.firebase.auth.AuthCredential {
        val credentialManager = CredentialManager.create(activity)

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(
                activity.getString(
                    activity.resources.getIdentifier(
                        "web_client_id",
                        "string",
                        activity.packageName
                    )
                )
            )
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        val result = credentialManager.getCredential(
            context = activity,
            request = request
        )

        val cred = result.credential

        val googleIdTokenCredential = when {
            cred is CustomCredential &&
                    cred.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL ->
                GoogleIdTokenCredential.createFrom(cred.data)

            else -> throw IllegalStateException("Credencial retornada não é Google ID Token.")
        }

        return GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)
    }

    private fun startSmsMfa(activity: FragmentActivity, resolver: MultiFactorResolver) {
        val phoneFactor = resolver.hints
            .firstOrNull { it.factorId == PhoneMultiFactorGenerator.FACTOR_ID }
                as? PhoneMultiFactorInfo
            ?: run {
                _state.value = AuthState.Error("Nenhum telefone MFA cadastrado para esta conta.")
                return
            }

        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                pendingResolver = resolver
                pendingVerificationId = verificationId
                _state.value = AuthState.NeedsSmsCode
            }

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                viewModelScope.launch {
                    try {
                        val assertion = PhoneMultiFactorGenerator.getAssertion(credential)
                        resolver.resolveSignIn(assertion).await()
                        pendingResolver = null
                        pendingVerificationId = null
                        _state.value = AuthState.LoggedIn
                    } catch (e: Exception) {
                        _state.value = AuthState.Error(e.message ?: "Falha ao validar SMS automaticamente")
                    }
                }
            }

            override fun onVerificationFailed(e: FirebaseException) {
                _state.value = AuthState.Error(e.message ?: "Falha ao enviar SMS")
            }
        }

        val options = PhoneAuthOptions.newBuilder()
            .setMultiFactorSession(resolver.session)
            .setMultiFactorHint(phoneFactor)
            .setTimeout(30L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callbacks)
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    fun submitSmsCode(code: String) {
        viewModelScope.launch {
            _state.value = AuthState.Loading
            try {
                val verificationId = pendingVerificationId ?: run {
                    _state.value = AuthState.Error("Sessão de SMS expirou. Tente novamente.")
                    return@launch
                }
                val resolver = pendingResolver ?: run {
                    _state.value = AuthState.Error("Sessão de SMS expirou. Tente novamente.")
                    return@launch
                }

                val cred = PhoneAuthProvider.getCredential(verificationId, code)
                val assertion = PhoneMultiFactorGenerator.getAssertion(cred)
                resolver.resolveSignIn(assertion).await()

                pendingResolver = null
                pendingVerificationId = null
                _state.value = AuthState.LoggedIn
            } catch (e: Exception) {
                _state.value = AuthState.Error(e.message ?: "Código inválido ou expirado")
            }
        }
    }
}