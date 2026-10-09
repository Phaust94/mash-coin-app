package com.soapp.wallet.net

import com.soapp.wallet.testing.JsonRpcDispatcher
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.assertEquals
import org.junit.Test
import java.math.BigInteger

class Web3jRpcClientTest {

    private val address = "0xf39fd6e51aad88f6f4ce6ab8827279cfffb92266"

    @Test
    fun `balance request targets the configured endpoint`() {
        val server = MockWebServer()
        val dispatcher = JsonRpcDispatcher(mapOf("eth_getBalance" to "\"0xde0b6b3a7640000\""))
        server.dispatcher = dispatcher
        server.start()
        try {
            val client = Web3jRpcClient(server.url("/").toString())
            val balance = client.getBalance(address)

            assertEquals(BigInteger("1000000000000000000"), balance)
            assertEquals("eth_getBalance", dispatcher.calls.single().method)
            assertEquals("/", server.takeRequest().path)
        } finally {
            server.shutdown()
        }
    }

    @Test
    fun `chain id request targets the configured endpoint`() {
        val server = MockWebServer()
        val dispatcher = JsonRpcDispatcher(mapOf("eth_chainId" to "\"0x539\""))
        server.dispatcher = dispatcher
        server.start()
        try {
            val client = Web3jRpcClient(server.url("/").toString())

            assertEquals(1337L, client.chainId())
            assertEquals("eth_chainId", dispatcher.calls.single().method)
        } finally {
            server.shutdown()
        }
    }
}
