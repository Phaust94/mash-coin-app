package com.soapp.wallet.data

import com.soapp.wallet.model.Wallet
import com.soapp.wallet.testing.InMemoryKeyValueStore
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WalletRepositoryTest {

    private val wallet = Wallet(
        address = "0xf39fd6e51aad88f6f4ce6ab8827279cfffb92266",
        mnemonic = "test test test test test test test test test test test junk"
    )

    @Test
    fun `save then load round-trips`() {
        val repo = WalletRepository(InMemoryKeyValueStore())
        repo.save(wallet)
        assertEquals(wallet, repo.load())
    }

    @Test
    fun `a fresh instance reads the stored wallet`() {
        val store = InMemoryKeyValueStore()
        WalletRepository(store).save(wallet)

        val fresh = WalletRepository(store)
        assertEquals(wallet, fresh.load())
        assertTrue(fresh.hasWallet())
    }

    @Test
    fun `clear removes the wallet`() {
        val repo = WalletRepository(InMemoryKeyValueStore())
        repo.save(wallet)
        repo.clear()
        assertFalse(repo.hasWallet())
    }
}
