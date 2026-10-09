package com.soapp.wallet.transfer

import org.web3j.crypto.Credentials
import org.web3j.crypto.RawTransaction
import org.web3j.crypto.TransactionEncoder
import org.web3j.utils.Numeric
import java.math.BigInteger

/**
 * Builds and signs a legacy (type 0) EIP-155 transaction entirely offline.
 * No network access happens here; the returned value is the raw signed transaction.
 */
object TransactionSigner {

    fun sign(
        chainId: Long,
        credentials: Credentials,
        nonce: BigInteger,
        gasPrice: BigInteger,
        gasLimit: BigInteger,
        to: String,
        value: BigInteger
    ): String {
        val raw = RawTransaction.createEtherTransaction(nonce, gasPrice, gasLimit, to, value)
        val signed = TransactionEncoder.signMessage(raw, chainId, credentials)
        return Numeric.toHexString(signed)
    }
}
