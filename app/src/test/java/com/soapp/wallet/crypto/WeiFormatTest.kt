package com.soapp.wallet.crypto

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.math.BigDecimal
import java.math.BigInteger

class WeiFormatTest {

    private val oneCoin = BigInteger.TEN.pow(18)

    private fun coins(amount: String): BigInteger =
        BigDecimal(amount).movePointRight(WeiFormat.DECIMALS).toBigIntegerExact()

    @Test
    fun `formats zero`() {
        assertEquals("0", WeiFormat.format(BigInteger.ZERO))
    }

    @Test
    fun `formats exactly one coin`() {
        assertEquals("1", WeiFormat.format(oneCoin))
    }

    @Test
    fun `formats a fractional amount`() {
        assertEquals("0.5", WeiFormat.format(oneCoin.divide(BigInteger.valueOf(2))))
    }

    @Test
    fun `formats a large amount without scientific notation`() {
        assertEquals("1234", WeiFormat.format(BigInteger.valueOf(1234).multiply(oneCoin)))
    }

    @Test
    fun `formats one wei`() {
        assertEquals("0.000000000000000001", WeiFormat.format(BigInteger.ONE))
    }

    @Test
    fun `parses whole and fractional amounts`() {
        assertEquals(oneCoin, WeiFormat.parseOrNull("1"))
        assertEquals(oneCoin.divide(BigInteger.valueOf(2)), WeiFormat.parseOrNull("0.5"))
        assertEquals(BigInteger.ZERO, WeiFormat.parseOrNull("0"))
    }

    @Test
    fun `display rounds down to one decimal with thousands separators`() {
        assertEquals("1,234.5", WeiFormat.formatDisplay(coins("1234.5678")))
        assertEquals("1.9", WeiFormat.formatDisplay(coins("1.99")))
        assertEquals("1,234,567.8", WeiFormat.formatDisplay(coins("1234567.89")))
        assertEquals("1", WeiFormat.formatDisplay(oneCoin))
        assertEquals("0", WeiFormat.formatDisplay(BigInteger.ZERO))
        assertEquals("0", WeiFormat.formatDisplay(BigInteger.ONE))
    }

    @Test
    fun `rejects invalid amounts`() {
        assertNull(WeiFormat.parseOrNull("-1"))
        assertNull(WeiFormat.parseOrNull("abc"))
        assertNull(WeiFormat.parseOrNull(""))
        assertNull(WeiFormat.parseOrNull("0.0000000000000000001")) // more than 18 decimals
    }
}
