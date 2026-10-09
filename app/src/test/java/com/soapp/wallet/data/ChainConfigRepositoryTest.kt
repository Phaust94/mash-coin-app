package com.soapp.wallet.data

import com.soapp.wallet.model.ChainConfig
import com.soapp.wallet.testing.InMemoryKeyValueStore
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ChainConfigRepositoryTest {

    private val valid = ChainConfig(rpcUrl = "http://localhost:8545", coinAlias = "ETH", chainId = 1337)

    @Test
    fun `valid configuration is saved and loaded`() {
        val repo = ChainConfigRepository(InMemoryKeyValueStore())
        assertTrue(repo.save(valid).isSuccess)
        assertEquals(valid, repo.load())
    }

    @Test
    fun `bad url is rejected and nothing is saved`() {
        val repo = ChainConfigRepository(InMemoryKeyValueStore())
        assertTrue(repo.save(valid.copy(rpcUrl = "ftp://nope")).isFailure)
        assertTrue(repo.save(valid.copy(rpcUrl = "not a url")).isFailure)
        assertNull(repo.load())
    }

    @Test
    fun `empty alias is rejected`() {
        val repo = ChainConfigRepository(InMemoryKeyValueStore())
        assertTrue(repo.save(valid.copy(coinAlias = "  ")).isFailure)
        assertNull(repo.load())
    }

    @Test
    fun `non-positive chain id is rejected`() {
        val repo = ChainConfigRepository(InMemoryKeyValueStore())
        assertTrue(repo.save(valid.copy(chainId = 0)).isFailure)
        assertTrue(repo.save(valid.copy(chainId = -1)).isFailure)
        assertNull(repo.load())
    }

    @Test
    fun `validate returns a message for invalid and null for valid`() {
        assertNotNull(ChainConfigRepository.validate(valid.copy(coinAlias = "")))
        assertNull(ChainConfigRepository.validate(valid))
    }

    @Test
    fun `blank explorer url is allowed`() {
        val repo = ChainConfigRepository(InMemoryKeyValueStore())
        assertTrue(repo.save(valid.copy(explorerUrl = "  ")).isSuccess)
        assertEquals("", repo.load()?.explorerUrl)
    }

    @Test
    fun `non-blank explorer url must be valid`() {
        val repo = ChainConfigRepository(InMemoryKeyValueStore())
        assertTrue(repo.save(valid.copy(explorerUrl = "https://explorer.example")).isSuccess)
        assertEquals("https://explorer.example", repo.load()?.explorerUrl)

        val bad = ChainConfigRepository(InMemoryKeyValueStore())
        assertTrue(bad.save(valid.copy(explorerUrl = "ftp://nope")).isFailure)
        assertNull(bad.load())
    }

    @Test
    fun `payload saved before the explorer setting still loads`() {
        val store = InMemoryKeyValueStore()
        store.putString(
            ChainConfigRepository.KEY,
            """{"rpcUrl":"http://localhost:8545","coinAlias":"ETH","chainId":1337}"""
        )

        val loaded = ChainConfigRepository(store).load()
        assertEquals(valid, loaded)
        assertEquals("", loaded?.explorerUrl)
    }
}
