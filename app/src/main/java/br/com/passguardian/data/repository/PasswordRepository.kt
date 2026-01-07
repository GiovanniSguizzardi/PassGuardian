package br.com.passguardian.data.repository

import br.com.passguardian.data.model.PasswordItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class PasswordRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
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
                password = doc.getString("password") ?: "",
                updatedAt = doc.getLong("updatedAt") ?: 0L
            )
        }.reversed()
    }

    suspend fun get(id: String): PasswordItem? {
        val doc = col().document(id).get().await()
        if (!doc.exists()) return null
        return PasswordItem(
            id = doc.id,
            title = doc.getString("title") ?: "",
            password = doc.getString("password") ?: "",
            updatedAt = doc.getLong("updatedAt") ?: 0L
        )
    }

    suspend fun add(title: String, password: String) {
        val now = System.currentTimeMillis()
        col().add(mapOf("title" to title, "password" to password, "updatedAt" to now)).await()
    }

    suspend fun update(id: String, title: String, password: String) {
        val now = System.currentTimeMillis()
        col().document(id).set(mapOf("title" to title, "password" to password, "updatedAt" to now)).await()
    }

    suspend fun delete(id: String) {
        col().document(id).delete().await()
    }
}