package com.soapp.wallet.crypto

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AddressUtilsTest {

    private val lowercase = "0xf39fd6e51aad88f6f4ce6ab8827279cfffb92266"

    @Test
    fun `accepts lowercase and checksummed addresses`() {
        assertTrue(AddressUtils.isValid(lowercase))
        assertTrue(AddressUtils.isValid("0xf39Fd6e51aad88F6F4ce6aB8827279cffFb92266"))
    }

    @Test
    fun `rejects malformed addresses`() {
        assertFalse(AddressUtils.isValid("f39fd6e51aad88f6f4ce6ab8827279cfffb92266")) // no 0x
        assertFalse(AddressUtils.isValid("0x1234")) // too short
        assertFalse(AddressUtils.isValid("0xzzzzd6e51aad88f6f4ce6ab8827279cfffb92266")) // non-hex
        assertFalse(AddressUtils.isValid(""))
    }

    @Test
    fun `normalizes to lowercase`() {
        assertEquals(lowercase, AddressUtils.normalize("0xF39FD6E51AAD88F6F4CE6AB8827279CFFFB92266"))
    }

    @Test
    fun `checksum round-trips`() {
        val checksummed = AddressUtils.toChecksum(lowercase)
        assertEquals(lowercase, checksummed.lowercase())
        assertTrue(AddressUtils.isValid(checksummed))
    }
}
