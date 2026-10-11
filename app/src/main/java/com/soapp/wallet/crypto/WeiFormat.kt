package com.soapp.wallet.crypto

import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode
import java.text.NumberFormat
import java.util.Locale

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

    /**
     * Formats a wei value for display: rounded down to 1 decimal place with
     * comma thousands separators (e.g. 1234.5678 coins -> "1,234.5").
     */
    fun formatDisplay(wei: BigInteger): String {
        val value = BigDecimal(wei).movePointLeft(DECIMALS).setScale(1, RoundingMode.DOWN)
        return NumberFormat.getNumberInstance(Locale.US).apply {
            minimumFractionDigits = 0
            maximumFractionDigits = 1
            roundingMode = RoundingMode.DOWN
        }.format(value)
    }
}
