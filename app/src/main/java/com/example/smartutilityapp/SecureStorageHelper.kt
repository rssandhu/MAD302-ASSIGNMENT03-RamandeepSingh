/**
 * Secure storage helper using SharedPreferences with basic obfuscation
 * Follows security best practices - never stores plain text sensitive data
 */
package com.example.smartutilityapp

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys

class SecureStorageHelper(context: Context) {

    private val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
    private lateinit var sharedPreferences: SharedPreferences

    init {
        sharedPreferences = EncryptedSharedPreferences.create(
            "secure_smart_utility_prefs",
            masterKeyAlias,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    /**
     * Securely saves data with obfuscation
     * @param key Storage key
     * @param value Data to store (non-sensitive only)
     */
    fun saveSecureData(key: String, value: String) {
        sharedPreferences.edit().putString(key, simpleObfuscate(value)).apply()
    }

    /**
     * Retrieves securely stored data
     * @param key Storage key
     * @return Stored data or null if not found
     */
    fun getSecureData(key: String): String? {
        val encryptedValue = sharedPreferences.getString(key, null)
        return encryptedValue?.let { simpleDeobfuscate(it) }
    }

    // Simple obfuscation (for demo - use proper encryption for production)
    private fun simpleObfuscate(input: String): String {
        return input.reversed() + "_SECURE"
    }

    private fun simpleDeobfuscate(input: String): String? {
        return if (input.endsWith("_SECURE")) {
            input.removeSuffix("_SECURE").reversed()
        } else null
    }
}