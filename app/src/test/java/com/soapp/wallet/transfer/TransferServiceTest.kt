package com.soapp.wallet.transfer

import com.soapp.wallet.crypto.Mnemonics
import com.soapp.wallet.model.ChainConfig
import com.soapp.wallet.net.Web3jRpcClient
import com.soapp.wallet.testing.JsonRpcDispatcher
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigInteger

class TransferServiceTest {

    private val mnemonic = "test test test test test test test test test test test junk"
    private val credentials = Mnemonics.deriveCredentials(mnemonic)
    private val to = "0x9858EfFD232B4033E47d90003D41EC34EcaEda94"
    private val txHash = "0x" + "ab".repeat(32)

    private fun okResults(extra: Pair<String, String>? = null): Map<String, String> {
        val base = mutableMapOf(
            "eth_getTransactionCount" to "\"0x0\"",
            "eth_gasPrice" to "\"0x3b9aca00\"",
            "eth_estimateGas" to "\"0x5208\"",
            "eth_sendRawTransaction" to "\"$txHash\"",
            "eth_getBalance" to "\"0x0\""
        )
        extra?.let { base[it.first] = it.second }
        return base
    }

    private fun service(url: String) =
        TransferService(ChainConfig(url, "ETH", 1337L)) { u -> Web3jRpcClient(u) }

    @Test
    fun `successful send broadcasts and returns the hash`() {
        val server = MockWebServer()
        val dispatcher = JsonRpcDispatcher(okResults())
        server.dispatcher = dispatcher
        server.start()
        try {
            val result = service(server.url("/").toString())
                .send(credentials, to, BigInteger.valueOf(1000L))

            assertTrue(result is TransferResult.Success)
            assertEquals(txHash, (result as TransferResult.Success).txHash)

            val methods = dispatcher.calls.map { it.method }
            assertTrue(
                methods.containsAll(
                    listOf(
                        "eth_getTransactionCount",
                        "eth_gasPrice",
                        "eth_estimateGas",
                        "eth_sendRawTransaction"
                    )
                )
            )
        } finally {
            server.shutdown()
        }
    }

    @Test
    fun `rpc error yields a failure`() {
        val server = MockWebServer()
        server.dispatcher = JsonRpcDispatcher(emptyMap(), error = "execution reverted")
        server.start()
        try {
            val result = service(server.url("/").toString())
                .send(credentials, to, BigInteger.valueOf(1000L))

            assertTrue(result is TransferResult.Failure)
        } finally {
            server.shutdown()
        }
    }

    @Test
    fun `invalid destination is refused without a network call`() {
        val server = MockWebServer()
        server.start()
        try {
            val result = service(server.url("/").toString())
                .send(credentials, "not-an-address", BigInteger.valueOf(1000L))

            assertTrue(result is TransferResult.Failure)
            assertEquals(0, server.requestCount)
        } finally {
            server.shutdown()
        }
    }

    @Test
    fun `non-positive amount is refused without a network call`() {
        val server = MockWebServer()
        server.start()
        try {
            val result = service(server.url("/").toString())
                .send(credentials, to, BigInteger.ZERO)

            assertTrue(result is TransferResult.Failure)
            assertEquals(0, server.requestCount)
        } finally {
            server.shutdown()
        }
    }

    @Test
    fun `secret never leaves the device during balance and send`() {
        val server = MockWebServer()
        val dispatcher = JsonRpcDispatcher(okResults())
        server.dispatcher = dispatcher
        server.start()
        try {
            val url = server.url("/").toString()
            Web3jRpcClient(url).getBalance(credentials.address)
            service(url).send(credentials, to, BigInteger.valueOf(1000L))

            val privateKey = credentials.ecKeyPair.privateKey.toString(16).lowercase()
            assertTrue(dispatcher.calls.isNotEmpty())
            dispatcher.calls.forEach { call ->
                assertFalse(call.body.contains(mnemonic))
                assertFalse(call.body.contains("junk"))
                assertFalse(call.body.contains(privateKey))
            }
        } finally {
            server.shutdown()
        }
    }
}
