package com.soapp.wallet.transfer

import com.soapp.wallet.crypto.Mnemonics
import org.junit.Assert.assertEquals
import org.junit.Test
import org.web3j.crypto.SignedRawTransaction
import org.web3j.crypto.TransactionDecoder
import java.math.BigInteger

class TransactionSignerTest {

    private val credentials =
        Mnemonics.deriveCredentials("test test test test test test test test test test test junk")

    @Test
    fun `signed transaction decodes with the expected fields`() {
        val to = "0x9858EfFD232B4033E47d90003D41EC34EcaEda94"
        val signed = TransactionSigner.sign(
            chainId = 1337L,
            credentials = credentials,
            nonce = BigInteger.valueOf(7L),
            gasPrice = BigInteger.valueOf(1_000_000_000L),
            gasLimit = BigInteger.valueOf(21_000L),
            to = to,
            value = BigInteger.valueOf(12_345L)
        )

        val decoded = TransactionDecoder.decode(signed) as SignedRawTransaction
        assertEquals(to.lowercase(), decoded.to.lowercase())
        assertEquals(BigInteger.valueOf(12_345L), decoded.value)
        assertEquals(BigInteger.valueOf(7L), decoded.nonce)
        assertEquals(1337L, decoded.chainId)
        assertEquals(credentials.address.lowercase(), decoded.from.lowercase())
    }
}
