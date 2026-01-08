package br.com.passguardian.ui.navigation

sealed class Route(val path: String) {
    data object Login : Route("login")
    data object Sms : Route("sms")
    data object Passwords : Route("passwords")

    data object UnlockGate : Route("unlock_gate/{id}") {
        fun create(id: String) = "unlock_gate/$id"
    }

    data object PasswordEdit : Route("password_edit/{id}") {
        fun create(id: String) = "password_edit/$id"
    }

    companion object {
        const val NEW_ID = "new"
    }
}
