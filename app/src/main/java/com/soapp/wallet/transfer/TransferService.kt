package com.soapp.wallet.transfer

import com.soapp.wallet.crypto.AddressUtils
import com.soapp.wallet.model.ChainConfig
import com.soapp.wallet.net.RpcClient
import org.web3j.crypto.Credentials
import java.math.BigInteger

sealed interface TransferResult {
    data class Success(val txHash: String) : TransferResult
    data class Failure(val message: String) : TransferResult
}

/**
 * Orchestrates a transfer: fetches nonce, gas price, and gas limit, signs the
 * transaction locally, then broadcasts the raw signed transaction. The private
 * key never leaves the device - only the signed payload is sent.
 */
class TransferService(
    private val config: ChainConfig,
    private val rpcFactory: (String) -> RpcClient
) {

    fun send(credentials: Credentials, to: String, valueWei: BigInteger): TransferResult {
        if (!AddressUtils.isValid(to)) {
            return TransferResult.Failure("Enter a valid destination address")
        }
        if (valueWei.signum() <= 0) {
            return TransferResult.Failure("Enter an amount greater than 0")
        }

        return try {
            val rpc = rpcFactory(config.rpcUrl)
            val from = credentials.address
            val nonce = rpc.pendingNonce(from)
            val gasPrice = rpc.gasPrice()
            val gasLimit = rpc.estimateGas(from, to, valueWei)
            val signed = TransactionSigner.sign(
                chainId = config.chainId,
                credentials = credentials,
                nonce = nonce,
                gasPrice = gasPrice,
                gasLimit = gasLimit,
                to = to,
                value = valueWei
            )
            TransferResult.Success(rpc.sendRawTransaction(signed))
        } catch (e: Exception) {
            TransferResult.Failure(e.message ?: "Transaction failed")
        }
    }
}
