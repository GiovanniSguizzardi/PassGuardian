package br.com.passguardian

import androidx.fragment.app.FragmentActivity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import br.com.passguardian.ui.navigation.Route
import br.com.passguardian.ui.screens.LoginScreen
import br.com.passguardian.ui.screens.PasswordEditScreen
import br.com.passguardian.ui.screens.PasswordListScreen
import br.com.passguardian.ui.screens.SmsCodeScreen
import br.com.passguardian.ui.theme.PassGuardianTheme
import br.com.passguardian.viewmodel.AuthState
import br.com.passguardian.viewmodel.AuthViewModel
import br.com.passguardian.viewmodel.PasswordViewModel

class MainActivity : FragmentActivity() {

    private val authVm = AuthViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            PassGuardianTheme {
                Surface(color = MaterialTheme.colorScheme.background) {

                    val nav = rememberNavController()
                    val authState by authVm.state.collectAsState()
                    val pwdVm = remember { PasswordViewModel() }

                    // Controle central de fluxo por AuthState
                    LaunchedEffect(authState) {
                        when (authState) {
                            is AuthState.LoggedIn -> {
                                // Se estiver em Login/Sms, vai pra lista
                                if (nav.currentDestination?.route == Route.Login.path ||
                                    nav.currentDestination?.route == Route.Sms.path
                                ) {
                                    nav.navigate(Route.Passwords.path) {
                                        popUpTo(Route.Login.path) { inclusive = true }
                                        popUpTo(Route.Sms.path) { inclusive = true }
                                    }
                                }
                            }

                            is AuthState.NeedsSmsCode -> {
                                if (nav.currentDestination?.route != Route.Sms.path) {
                                    nav.navigate(Route.Sms.path)
                                }
                            }

                            is AuthState.LoggedOut -> {
                                nav.navigate(Route.Login.path) {
                                    popUpTo(0) { inclusive = true }
                                }
                            }

                            else -> {}
                        }
                    }

                    NavHost(
                        navController = nav,
                        startDestination = Route.Login.path
                    ) {
                        composable(Route.Login.path) {
                            LoginScreen(
                                authVm = authVm,
                                onLoggedIn = {
                                    nav.navigate(Route.Passwords.path) {
                                        popUpTo(Route.Login.path) { inclusive = true }
                                    }
                                },
                                onNeedsSms = {
                                    nav.navigate(Route.Sms.path)
                                }
                            )
                        }

                        composable(Route.Sms.path) {
                            SmsCodeScreen(
                                authVm = authVm,
                                onDone = {
                                    nav.navigate(Route.Passwords.path) {
                                        popUpTo(Route.Login.path) { inclusive = true }
                                        popUpTo(Route.Sms.path) { inclusive = true }
                                    }
                                }
                            )
                        }

                        composable(Route.Passwords.path) {
                            PasswordListScreen(
                                vm = pwdVm,
                                onAdd = { nav.navigate(Route.PasswordEdit.create(Route.NEW_ID)) },
                                onEdit = { id -> nav.navigate(Route.PasswordEdit.create(id)) }
                            )
                        }

                        composable(
                            route = Route.PasswordEdit.path,
                            arguments = listOf(navArgument("id") { type = NavType.StringType })
                        ) { entry ->
                            val id = entry.arguments?.getString("id") ?: Route.NEW_ID
                            PasswordEditScreen(
                                vm = pwdVm,
                                id = id,
                                onDone = { nav.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}