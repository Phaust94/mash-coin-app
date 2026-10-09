package com.soapp.wallet.testing

import com.soapp.wallet.net.RpcClient
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest
import java.math.BigInteger
import java.util.Collections

/**
 * MockWebServer dispatcher that answers JSON-RPC calls by method name and
 * records every request body, so tests can assert what was transmitted.
 */
class JsonRpcDispatcher(
    private val results: Map<String, String>,
    private val error: String? = null
) : Dispatcher() {

    data class RecordedCall(val method: String, val body: String)

    val calls: MutableList<RecordedCall> = Collections.synchronizedList(mutableListOf())

    override fun dispatch(request: RecordedRequest): MockResponse {
        val body = request.body.readUtf8()
        val id = Regex("\"id\"\\s*:\\s*(\\d+)").find(body)?.groupValues?.get(1) ?: "1"
        val method = Regex("\"method\"\\s*:\\s*\"([^\"]+)\"").find(body)?.groupValues?.get(1) ?: ""
        calls.add(RecordedCall(method, body))

        val payload = if (error != null) {
            """{"jsonrpc":"2.0","id":$id,"error":{"code":-32000,"message":"$error"}}"""
        } else {
            val result = results[method] ?: "null"
            """{"jsonrpc":"2.0","id":$id,"result":$result}"""
        }
        return MockResponse()
            .setHeader("Content-Type", "application/json")
            .setBody(payload)
    }
}

/** Configurable fake [RpcClient] for ViewModel and service tests. */
class FakeRpcClient(
    var balance: BigInteger = BigInteger.ZERO,
    var fail: Boolean = false
) : RpcClient {

    var balanceCalls = 0
        private set

    override fun getBalance(address: String): BigInteger {
        balanceCalls++
        if (fail) throw RuntimeException("network down")
        return balance
    }

    override fun chainId(): Long = 1L
    override fun pendingNonce(address: String): BigInteger = BigInteger.ZERO
    override fun gasPrice(): BigInteger = BigInteger.ONE
    override fun estimateGas(from: String, to: String, value: BigInteger): BigInteger =
        BigInteger.valueOf(21_000L)

    override fun sendRawTransaction(signedHex: String): String = "0x" + "ab".repeat(32)
}
