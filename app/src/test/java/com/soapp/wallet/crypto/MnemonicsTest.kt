package com.soapp.wallet.crypto

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MnemonicsTest {

    private val hardhat12 = "test test test test test test test test test test test junk"
    private val abandon12 = ("abandon ".repeat(11) + "about").trim()
    private val abandon24 = ("abandon ".repeat(23) + "art").trim()

    // Reference addresses computed with an independent wallet library
    // (Python eth-account, default path m/44'/60'/0'/0/0).
    private val hardhat12Address = "0xf39Fd6e51aad88F6F4ce6aB8827279cffFb92266"
    private val abandon12Address = "0x9858EfFD232B4033E47d90003D41EC34EcaEda94"
    private val abandon24Address = "0xF278cF59F82eDcf871d630F28EcC8056f25C1cdb"

    @Test
    fun `valid 12-word phrases are accepted`() {
        assertTrue(Mnemonics.isValid(hardhat12))
        assertTrue(Mnemonics.isValid(abandon12))
    }

    @Test
    fun `valid 24-word phrase is accepted`() {
        assertTrue(Mnemonics.isValid(abandon24))
    }

    @Test
    fun `invalid word count is rejected`() {
        val elevenWords = "abandon ".repeat(10) + "about"
        assertFalse(Mnemonics.isValid(elevenWords))
    }

    @Test
    fun `invalid checksum is rejected`() {
        val badChecksum = "abandon ".repeat(12).trim()
        assertFalse(Mnemonics.isValid(badChecksum))
    }

    @Test
    fun `non-wordlist words are rejected`() {
        val badWords = "zzzz ".repeat(11) + "about"
        assertFalse(Mnemonics.isValid(badWords))
    }

    @Test
    fun `known 12-word phrases map to their expected addresses`() {
        assertEquals(hardhat12Address, AddressUtils.toChecksum(Mnemonics.deriveAddress(hardhat12)))
        assertEquals(abandon12Address, AddressUtils.toChecksum(Mnemonics.deriveAddress(abandon12)))
    }

    @Test
    fun `known 24-word phrase maps to its expected address`() {
        assertEquals(abandon24Address, AddressUtils.toChecksum(Mnemonics.deriveAddress(abandon24)))
    }

    @Test
    fun `derivation is deterministic`() {
        assertEquals(Mnemonics.deriveAddress(hardhat12), Mnemonics.deriveAddress(hardhat12))
    }
}
