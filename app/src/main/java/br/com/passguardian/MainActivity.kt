package br.com.passguardian

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import br.com.passguardian.security.AccessGate
import br.com.passguardian.ui.navigation.Route
import br.com.passguardian.ui.screens.LoginScreen
import br.com.passguardian.ui.screens.PasswordEditScreen
import br.com.passguardian.ui.screens.PasswordListScreen
import br.com.passguardian.ui.screens.SmsCodeScreen
import br.com.passguardian.ui.screens.UnlockGateScreen
import br.com.passguardian.ui.theme.PassGuardianTheme
import br.com.passguardian.viewmodel.AuthState
import br.com.passguardian.viewmodel.AuthViewModel
import br.com.passguardian.viewmodel.PasswordViewModel

class MainActivity : FragmentActivity() {

    private val authVm = AuthViewModel()
    private val accessGate by lazy { AccessGate(applicationContext) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )

        setContent {
            PassGuardianTheme {
                Surface(color = MaterialTheme.colorScheme.background) {

                    val nav = rememberNavController()
                    val authState by authVm.state.collectAsState()
                    val pwdVm = remember { PasswordViewModel() }

                    LaunchedEffect(authState) {
                        when (authState) {
                            is AuthState.LoggedOut -> {
                                nav.navigate(Route.Login.path) {
                                    popUpTo(0)
                                    launchSingleTop = true
                                }
                            }
                            is AuthState.NeedsSmsCode -> {
                                nav.navigate(Route.Sms.path) {
                                    launchSingleTop = true
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
                                        launchSingleTop = true
                                    }
                                },
                                onNeedsSms = {
                                    nav.navigate(Route.Sms.path) { launchSingleTop = true }
                                }
                            )
                        }

                        composable(Route.Sms.path) {
                            SmsCodeScreen(
                                authVm = authVm,
                                onDone = { nav.popBackStack() }
                            )
                        }

                        composable(Route.Passwords.path) {
                            PasswordListScreen(
                                vm = pwdVm,
                                onAdd = {
                                    nav.navigate(Route.PasswordEdit.create(Route.NEW_ID)) {
                                        launchSingleTop = true
                                    }
                                },
                                onOpen = { id ->
                                    nav.navigate(Route.UnlockGate.create(id)) {
                                        launchSingleTop = true
                                    }
                                },
                                onSignOut = {
                                    authVm.signOut()
                                }
                            )
                        }

                        composable(
                            route = Route.UnlockGate.path,
                            arguments = listOf(navArgument("id") { type = NavType.StringType })
                        ) { entry ->
                            val id = entry.arguments?.getString("id") ?: Route.NEW_ID

                            UnlockGateScreen(
                                authVm = authVm,
                                onUnlocked = {
                                    nav.navigate(Route.PasswordEdit.create(id)) {
                                        popUpTo(Route.UnlockGate.path) { inclusive = true }
                                        launchSingleTop = true
                                    }
                                }
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
                                onDone = {
                                    nav.navigate(Route.Passwords.path) {
                                        popUpTo(Route.Passwords.path) { inclusive = true }
                                        launchSingleTop = true
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        if (!isChangingConfigurations) {
            accessGate.clear()
        }
    }
}