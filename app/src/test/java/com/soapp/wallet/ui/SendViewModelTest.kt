package com.soapp.wallet.ui

import com.soapp.wallet.data.AddressBookRepository
import com.soapp.wallet.data.ChainConfigRepository
import com.soapp.wallet.data.WalletRepository
import com.soapp.wallet.model.AddressBookEntry
import com.soapp.wallet.model.ChainConfig
import com.soapp.wallet.model.Wallet
import com.soapp.wallet.testing.FakeRpcClient
import com.soapp.wallet.testing.InMemoryKeyValueStore
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SendViewModelTest {

    private val address = "0xf39fd6e51aad88f6f4ce6ab8827279cfffb92266"

    private fun viewModel(book: AddressBookRepository): SendViewModel {
        val store = InMemoryKeyValueStore()
        val walletRepository = WalletRepository(store).apply {
            save(
                Wallet(
                    address = address,
                    mnemonic = "test test test test test test test test test test test junk"
                )
            )
        }
        val chainConfigRepository = ChainConfigRepository(store).apply {
            save(ChainConfig("http://localhost:8545", "ETH", 1337L))
        }
        return SendViewModel(walletRepository, chainConfigRepository, book, { FakeRpcClient() })
    }

    @Test
    fun `refresh picks up entries added after creation`() {
        val book = AddressBookRepository(InMemoryKeyValueStore())
        val vm = viewModel(book)
        assertTrue(vm.state.value.entries.isEmpty())

        book.add("Alice", address)
        vm.refresh()

        assertEquals(1, vm.state.value.entries.size)
        assertEquals("Alice", vm.state.value.entries.first().name)
    }

    @Test
    fun `refresh keeps typed amount and destination`() {
        val book = AddressBookRepository(InMemoryKeyValueStore())
        val vm = viewModel(book)
        vm.onAmountChange("1.5")
        vm.onDestinationChange(address)

        book.add("Alice", address)
        vm.refresh()

        assertEquals("1.5", vm.state.value.amount)
        assertEquals(address, vm.state.value.destination)
    }

    @Test
    fun `onPick fills the destination from the book entry`() {
        val book = AddressBookRepository(InMemoryKeyValueStore())
        val vm = viewModel(book)

        vm.onPick(AddressBookEntry("Alice", address))

        assertEquals(address, vm.state.value.destination)
    }
}
