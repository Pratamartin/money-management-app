package com.pratatec.moneymgtapp.sync

import java.security.MessageDigest

fun hashPin(pin: String): String {
    val digest = MessageDigest.getInstance("SHA-256")
    return digest.digest(pin.toByteArray())
        .joinToString("") { "%02x".format(it) }
}
