package br.com.passguardian.data.repository

import br.com.passguardian.data.model.PasswordItem
import br.com.passguardian.security.CryptoManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class PasswordRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val crypto: CryptoManager = CryptoManager()
) {
    private fun uid(): String = auth.currentUser?.uid ?: error("Usuário não logado")

    private fun col() =
        db.collection("users").document(uid()).collection("passwords")

    suspend fun list(): List<PasswordItem> {
        val snap = col().orderBy("updatedAt").get().await()
        return snap.documents.map { doc ->
            PasswordItem(
                id = doc.id,
                title = doc.getString("title") ?: "",
                username = "", // não mostrar na lista
                password = "", // não mostrar na lista
                updatedAt = doc.getLong("updatedAt") ?: 0L
            )
        }.reversed()
    }

    suspend fun getDecrypted(id: String): PasswordItem? {
        val doc = col().document(id).get().await()
        if (!doc.exists()) return null

        val title = doc.getString("title") ?: ""
        val usernameEnc = doc.getString("usernameEnc") ?: ""
        val passwordEnc = doc.getString("passwordEnc") ?: ""
        val updatedAt = doc.getLong("updatedAt") ?: 0L

        val u = uid()

        val username = if (usernameEnc.isBlank()) "" else crypto.decrypt(u, usernameEnc)
        val password = if (passwordEnc.isBlank()) "" else crypto.decrypt(u, passwordEnc)

        return PasswordItem(
            id = doc.id,
            title = title,
            username = username,
            password = password,
            updatedAt = updatedAt
        )
    }

    suspend fun addEncrypted(item: PasswordItem) {
        val u = uid()
        val now = System.currentTimeMillis()

        val usernameEnc = if (item.username.isBlank()) "" else crypto.encrypt(u, item.username)
        val passwordEnc = crypto.encrypt(u, item.password)

        col().add(
            mapOf(
                "title" to item.title,
                "usernameEnc" to usernameEnc,
                "passwordEnc" to passwordEnc,
                "updatedAt" to now
            )
        ).await()
    }

    suspend fun updateEncrypted(id: String, item: PasswordItem) {
        val u = uid()
        val now = System.currentTimeMillis()

        val usernameEnc = if (item.username.isBlank()) "" else crypto.encrypt(u, item.username)
        val passwordEnc = crypto.encrypt(u, item.password)

        col().document(id).set(
            mapOf(
                "title" to item.title,
                "usernameEnc" to usernameEnc,
                "passwordEnc" to passwordEnc,
                "updatedAt" to now
            )
        ).await()
    }

    suspend fun delete(id: String) {
        col().document(id).delete().await()
    }
}