package com.soapp.wallet.crypto

import org.web3j.crypto.Bip32ECKeyPair
import org.web3j.crypto.Credentials
import org.web3j.crypto.MnemonicUtils

/**
 * BIP39 mnemonic validation and BIP44 account derivation for the ETH-style
 * path `m/44'/60'/0'/0/0`.
 */
object Mnemonics {

    private val WHITESPACE = Regex("\\s+")

    private const val HARDENED = 0x80000000.toInt()
    private val BIP44_PATH = intArrayOf(
        44 + HARDENED,
        60 + HARDENED,
        0 + HARDENED,
        0,
        0
    )

    /** Collapses arbitrary whitespace and trims a phrase to its canonical form. */
    fun normalize(phrase: String): String =
        phrase.trim().split(WHITESPACE).filter { it.isNotEmpty() }.joinToString(" ")

    fun words(phrase: String): List<String> =
        phrase.trim().split(WHITESPACE).filter { it.isNotEmpty() }

    /**
     * True only for a phrase of exactly 12 or 24 words that passes BIP39
     * wordlist and checksum validation.
     */
    fun isValid(phrase: String): Boolean {
        val w = words(phrase)
        if (w.size != 12 && w.size != 24) return false
        return try {
            MnemonicUtils.generateEntropy(w.joinToString(" "))
            true
        } catch (e: Exception) {
            false
        }
    }

    fun deriveCredentials(phrase: String): Credentials {
        val seed = MnemonicUtils.generateSeed(normalize(phrase), "")
        val root = Bip32ECKeyPair.generateKeyPair(seed)
        val keyPair = Bip32ECKeyPair.deriveKeyPair(root, BIP44_PATH)
        return Credentials.create(keyPair)
    }

    fun deriveAddress(phrase: String): String = deriveCredentials(phrase).address
}
