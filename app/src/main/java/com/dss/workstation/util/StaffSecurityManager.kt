package com.dss.workstation.util

import android.content.Context
import android.content.SharedPreferences
import java.security.MessageDigest
import java.security.SecureRandom
import android.util.Base64

class StaffSecurityManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    init {
        // Initialize with default PIN "1234" if not set
        if (!prefs.contains(KEY_PIN_HASH)) {
            setPin("1234")
        }
    }

    fun verifyPin(enteredPin: String): Boolean {
        val storedHash = prefs.getString(KEY_PIN_HASH, null) ?: return false
        val storedSalt = prefs.getString(KEY_PIN_SALT, null) ?: return false

        val calculatedHash = hashPin(enteredPin, storedSalt)
        return calculatedHash == storedHash
    }

    fun changePin(oldPin: String, newPin: String): Boolean {
        if (!verifyPin(oldPin)) return false
        if (newPin.length < 4 || newPin.length > 6) return false
        setPin(newPin)
        return true
    }

    fun setPin(newPin: String) {
        val saltBytes = ByteArray(16)
        SecureRandom().nextBytes(saltBytes)
        val saltBase64 = Base64.encodeToString(saltBytes, Base64.NO_WRAP)

        val hash = hashPin(newPin, saltBase64)
        prefs.edit()
            .putString(KEY_PIN_SALT, saltBase64)
            .putString(KEY_PIN_HASH, hash)
            .apply()
    }

    private fun hashPin(pin: String, salt: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        md.update(salt.toByteArray(Charsets.UTF_8))
        val hashedBytes = md.digest(pin.toByteArray(Charsets.UTF_8))
        return Base64.encodeToString(hashedBytes, Base64.NO_WRAP)
    }

    companion object {
        private const val PREFS_NAME = "dss_staff_security_prefs"
        private const val KEY_PIN_HASH = "staff_pin_hash"
        private const val KEY_PIN_SALT = "staff_pin_salt"
    }
}
