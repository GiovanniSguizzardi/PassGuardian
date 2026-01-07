package br.com.passguardian.security

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class AccessGate(context: Context) {

    private val prefs = EncryptedSharedPreferences.create(
        context,
        "access_gate_prefs",
        MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    private val keyLastUnlock = "last_unlock_ms"

    fun isUnlocked(windowMs: Long): Boolean {
        val last = prefs.getLong(keyLastUnlock, 0L)
        val now = System.currentTimeMillis()
        return last > 0 && (now - last) <= windowMs
    }

    fun markUnlockedNow() {
        prefs.edit().putLong(keyLastUnlock, System.currentTimeMillis()).apply()
    }

    fun clear() {
        prefs.edit().remove(keyLastUnlock).apply()
    }
}