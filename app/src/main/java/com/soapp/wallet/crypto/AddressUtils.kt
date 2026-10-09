package com.soapp.wallet.crypto

import org.web3j.crypto.Keys

/** Validation, normalization, and checksum display for ETH-style addresses. */
object AddressUtils {

    private val HEX_ADDRESS = Regex("^0x[0-9a-fA-F]{40}$")

    fun isValid(address: String): Boolean = HEX_ADDRESS.matches(address.trim())

    fun normalize(address: String): String = address.trim().lowercase()

    fun toChecksum(address: String): String = Keys.toChecksumAddress(normalize(address))
}
