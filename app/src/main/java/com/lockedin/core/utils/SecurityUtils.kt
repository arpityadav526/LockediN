package com.lockedin.core.utils

import java.security.MessageDigest

object SecurityUtils {

    /**
     * Hashes a PIN using SHA-256 and returns the hex string.
     * Never store the raw PIN — always store the hash.
     */
    fun hashPin(pin: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(pin.toByteArray(Charsets.UTF_8))
        return hash.joinToString("") { "%02x".format(it) }
    }

    /**
     * Verifies an input PIN against a stored hash.
     */
    fun verifyPin(inputPin: String, storedHash: String): Boolean {
        return hashPin(inputPin) == storedHash
    }
}
