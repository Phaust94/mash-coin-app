package com.soapp.wallet.net

import org.web3j.protocol.Web3j
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.protocol.core.methods.request.Transaction
import org.web3j.protocol.http.HttpService
import java.math.BigInteger

/** The Ethereum JSON-RPC operations the app relies on. */
interface RpcClient {
    fun getBalance(address: String): BigInteger
    fun chainId(): Long
    fun pendingNonce(address: String): BigInteger
    fun gasPrice(): BigInteger
    fun estimateGas(from: String, to: String, value: BigInteger): BigInteger
    fun sendRawTransaction(signedHex: String): String
}

/** [RpcClient] backed by web3j talking to a configured JSON-RPC endpoint. */
class Web3jRpcClient(private val rpcUrl: String) : RpcClient {

    private val web3j: Web3j = Web3j.build(HttpService(rpcUrl))

    override fun getBalance(address: String): BigInteger =
        web3j.ethGetBalance(address, DefaultBlockParameterName.LATEST).send().balance

    override fun chainId(): Long =
        web3j.ethChainId().send().chainId.toLong()

    override fun pendingNonce(address: String): BigInteger =
        web3j.ethGetTransactionCount(address, DefaultBlockParameterName.PENDING)
            .send().transactionCount

    override fun gasPrice(): BigInteger =
        web3j.ethGasPrice().send().gasPrice

    override fun estimateGas(from: String, to: String, value: BigInteger): BigInteger =
        web3j.ethEstimateGas(Transaction(from, null, null, null, to, value, "0x"))
            .send().amountUsed

    override fun sendRawTransaction(signedHex: String): String {
        val response = web3j.ethSendRawTransaction(signedHex).send()
        if (response.hasError()) throw RuntimeException(response.error.message)
        return response.transactionHash
    }
}
