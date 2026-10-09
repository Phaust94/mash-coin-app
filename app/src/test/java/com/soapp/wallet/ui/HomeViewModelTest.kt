package com.soapp.wallet.ui

import com.soapp.wallet.data.ChainConfigRepository
import com.soapp.wallet.data.WalletRepository
import com.soapp.wallet.model.ChainConfig
import com.soapp.wallet.model.Wallet
import com.soapp.wallet.testing.FakeRpcClient
import com.soapp.wallet.testing.InMemoryKeyValueStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import java.math.BigInteger

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val scheduler = TestCoroutineScheduler()
    private val dispatcher = StandardTestDispatcher(scheduler)

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun repositories(explorerUrl: String = ""): Pair<WalletRepository, ChainConfigRepository> {
        val store = InMemoryKeyValueStore()
        val walletRepository = WalletRepository(store).apply {
            save(
                Wallet(
                    address = "0xf39fd6e51aad88f6f4ce6ab8827279cfffb92266",
                    mnemonic = "test test test test test test test test test test test junk"
                )
            )
        }
        val chainConfigRepository = ChainConfigRepository(store).apply {
            save(ChainConfig("http://localhost:8545", "ETH", 1337L, explorerUrl))
        }
        return walletRepository to chainConfigRepository
    }

    private val oneEther = BigInteger.TEN.pow(18)

    @Test
    fun `opening fetches the balance`() = runTest(dispatcher) {
        val (walletRepository, chainConfigRepository) = repositories()
        val fake = FakeRpcClient(balance = oneEther)

        val viewModel = HomeViewModel(walletRepository, chainConfigRepository, { fake }, dispatcher)
        advanceUntilIdle()

        assertEquals("1", viewModel.state.value.balance)
        assertEquals("ETH", viewModel.state.value.coinAlias)
        assertFalse(viewModel.state.value.loading)
        assertEquals(1, fake.balanceCalls)
    }

    @Test
    fun `refresh updates the balance`() = runTest(dispatcher) {
        val (walletRepository, chainConfigRepository) = repositories()
        val fake = FakeRpcClient(balance = oneEther)

        val viewModel = HomeViewModel(walletRepository, chainConfigRepository, { fake }, dispatcher)
        advanceUntilIdle()
        assertEquals("1", viewModel.state.value.balance)

        fake.balance = oneEther.multiply(BigInteger.valueOf(2L))
        viewModel.refresh()
        advanceUntilIdle()

        assertEquals("2", viewModel.state.value.balance)
        assertEquals(2, fake.balanceCalls)
    }

    @Test
    fun `history url is null without an explorer url`() = runTest(dispatcher) {
        val (walletRepository, chainConfigRepository) = repositories()
        val fake = FakeRpcClient(balance = oneEther)

        val viewModel = HomeViewModel(walletRepository, chainConfigRepository, { fake }, dispatcher)
        advanceUntilIdle()

        assertNull(viewModel.state.value.historyUrl)
    }

    @Test
    fun `history url points at the explorer address page`() = runTest(dispatcher) {
        val (walletRepository, chainConfigRepository) =
            repositories(explorerUrl = "https://explorer.example/")
        val fake = FakeRpcClient(balance = oneEther)

        val viewModel = HomeViewModel(walletRepository, chainConfigRepository, { fake }, dispatcher)
        advanceUntilIdle()

        assertEquals(
            "https://explorer.example/address/0xf39fd6e51aad88f6f4ce6ab8827279cfffb92266",
            viewModel.state.value.historyUrl
        )
    }
    @Test
    fun `failure shows an error and recovery clears it`() = runTest(dispatcher) {
        val (walletRepository, chainConfigRepository) = repositories()
        val fake = FakeRpcClient(balance = oneEther, fail = true)

        val viewModel = HomeViewModel(walletRepository, chainConfigRepository, { fake }, dispatcher)
        advanceUntilIdle()
        assertNotNull(viewModel.state.value.error)

        fake.fail = false
        fake.balance = oneEther.multiply(BigInteger.valueOf(3L))
        viewModel.refresh()
        advanceUntilIdle()

        assertNull(viewModel.state.value.error)
        assertEquals("3", viewModel.state.value.balance)
    }
}
