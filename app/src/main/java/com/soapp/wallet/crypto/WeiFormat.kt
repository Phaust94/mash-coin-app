package com.soapp.wallet.crypto

import java.math.BigDecimal
import java.math.BigInteger

/** Conversion between wei and whole-coin amounts, always using 18 decimals. */
object WeiFormat {

    const val DECIMALS = 18

    /** Formats a wei value as a plain (non-scientific) whole-coin string. */
    fun format(wei: BigInteger): String {
        val value = BigDecimal(wei).movePointLeft(DECIMALS).stripTrailingZeros()
        return value.toPlainString()
    }

    /** Parses a whole-coin amount into wei, or null if it is invalid or negative. */
    fun parseOrNull(amount: String): BigInteger? = try {
        val value = BigDecimal(amount.trim())
        if (value.signum() < 0) null else value.movePointRight(DECIMALS).toBigIntegerExact()
    } catch (e: Exception) {
        null
    }
}
