package com.soapp.wallet.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.soapp.wallet.crypto.AddressUtils
import com.soapp.wallet.crypto.Mnemonics
import com.soapp.wallet.crypto.WeiFormat
import com.soapp.wallet.data.AddressBookRepository
import com.soapp.wallet.data.ChainConfigRepository
import com.soapp.wallet.data.WalletRepository
import com.soapp.wallet.model.AddressBookEntry
import com.soapp.wallet.model.ChainConfig
import com.soapp.wallet.model.Wallet
import com.soapp.wallet.net.RpcClient
import com.soapp.wallet.transfer.TransferResult
import com.soapp.wallet.transfer.TransferService
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ---------------------------------------------------------------------------
// Wallet setup
// ---------------------------------------------------------------------------

data class WalletSetupUiState(
    val phrase: String = "",
    val error: String? = null,
    val derivedAddress: String? = null,
    val working: Boolean = false
)

class WalletSetupViewModel(
    private val walletRepository: WalletRepository,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    private val _state = MutableStateFlow(WalletSetupUiState())
    val state: StateFlow<WalletSetupUiState> = _state.asStateFlow()

    fun onPhraseChange(value: String) {
        _state.update { it.copy(phrase = value, error = null) }
    }

    fun import() {
        val phrase = _state.value.phrase
        if (!Mnemonics.isValid(phrase)) {
            _state.update {
                it.copy(
                    error = "Enter a valid 12- or 24-word recovery phrase",
                    derivedAddress = null
                )
            }
            return
        }
        _state.update { it.copy(working = true, error = null) }
        viewModelScope.launch(ioDispatcher) {
            runCatching {
                val address = Mnemonics.deriveAddress(phrase)
                walletRepository.save(Wallet(address = address, mnemonic = Mnemonics.normalize(phrase)))
                address
            }.onSuccess { address ->
                _state.update { it.copy(working = false, derivedAddress = address, error = null) }
            }.onFailure { e ->
                _state.update { it.copy(working = false, error = e.message ?: "Import failed") }
            }
        }
    }

    companion object {
        fun factory(walletRepository: WalletRepository) = viewModelFactory {
            initializer { WalletSetupViewModel(walletRepository) }
        }
    }
}

// ---------------------------------------------------------------------------
// Home / balance
// ---------------------------------------------------------------------------

data class HomeUiState(
    val address: String = "",
    val coinAlias: String = "",
    val balance: String? = null,
    val loading: Boolean = false,
    val error: String? = null,
    val needsConfig: Boolean = false
)

class HomeViewModel(
    private val walletRepository: WalletRepository,
    private val chainConfigRepository: ChainConfigRepository,
    private val rpcFactory: (String) -> RpcClient,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        val wallet = walletRepository.load()
        val config = chainConfigRepository.load()

        if (wallet == null) {
            _state.update { it.copy(loading = false, error = "No wallet found") }
            return
        }
        if (config == null) {
            _state.update {
                it.copy(
                    address = wallet.address,
                    needsConfig = true,
                    loading = false,
                    error = "Configure a chain to view your balance"
                )
            }
            return
        }

        _state.update {
            it.copy(
                address = wallet.address,
                coinAlias = config.coinAlias,
                needsConfig = false,
                loading = true,
                error = null
            )
        }

        viewModelScope.launch(ioDispatcher) {
            runCatching { rpcFactory(config.rpcUrl).getBalance(wallet.address) }
                .onSuccess { wei ->
                    _state.update {
                        it.copy(loading = false, balance = WeiFormat.format(wei), error = null)
                    }
                }
                .onFailure { e ->
                    _state.update {
                        it.copy(loading = false, error = e.message ?: "Failed to load balance")
                    }
                }
        }
    }

    companion object {
        fun factory(
            walletRepository: WalletRepository,
            chainConfigRepository: ChainConfigRepository,
            rpcFactory: (String) -> RpcClient
        ) = viewModelFactory {
            initializer { HomeViewModel(walletRepository, chainConfigRepository, rpcFactory) }
        }
    }
}

// ---------------------------------------------------------------------------
// Chain configuration
// ---------------------------------------------------------------------------

data class ChainConfigUiState(
    val rpcUrl: String = "",
    val coinAlias: String = "",
    val chainId: String = "",
    val error: String? = null,
    val saved: Boolean = false
)

class ChainConfigViewModel(
    private val chainConfigRepository: ChainConfigRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ChainConfigUiState())
    val state: StateFlow<ChainConfigUiState> = _state.asStateFlow()

    init {
        chainConfigRepository.load()?.let { config ->
            _state.value = ChainConfigUiState(
                rpcUrl = config.rpcUrl,
                coinAlias = config.coinAlias,
                chainId = config.chainId.toString()
            )
        }
    }

    fun onRpcUrlChange(value: String) = _state.update { it.copy(rpcUrl = value, error = null, saved = false) }
    fun onCoinAliasChange(value: String) = _state.update { it.copy(coinAlias = value, error = null, saved = false) }
    fun onChainIdChange(value: String) = _state.update { it.copy(chainId = value, error = null, saved = false) }

    fun save() {
        val s = _state.value
        val chainId = s.chainId.trim().toLongOrNull()
        if (chainId == null || chainId <= 0) {
            _state.update { it.copy(error = "Enter a positive numeric chain ID", saved = false) }
            return
        }
        val config = ChainConfig(rpcUrl = s.rpcUrl.trim(), coinAlias = s.coinAlias.trim(), chainId = chainId)
        chainConfigRepository.save(config)
            .onSuccess { _state.update { it.copy(error = null, saved = true) } }
            .onFailure { e ->
                _state.update { it.copy(error = e.message ?: "Invalid configuration", saved = false) }
            }
    }

    companion object {
        fun factory(chainConfigRepository: ChainConfigRepository) = viewModelFactory {
            initializer { ChainConfigViewModel(chainConfigRepository) }
        }
    }
}

// ---------------------------------------------------------------------------
// Address book
// ---------------------------------------------------------------------------

data class AddressBookUiState(
    val entries: List<AddressBookEntry> = emptyList(),
    val name: String = "",
    val address: String = "",
    val error: String? = null
)

class AddressBookViewModel(
    private val addressBookRepository: AddressBookRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AddressBookUiState())
    val state: StateFlow<AddressBookUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        _state.update { it.copy(entries = addressBookRepository.list()) }
    }

    fun onNameChange(value: String) = _state.update { it.copy(name = value, error = null) }
    fun onAddressChange(value: String) = _state.update { it.copy(address = value, error = null) }

    fun add() {
        val s = _state.value
        addressBookRepository.add(s.name, s.address)
            .onSuccess {
                _state.update {
                    it.copy(entries = addressBookRepository.list(), name = "", address = "", error = null)
                }
            }
            .onFailure { e ->
                _state.update { it.copy(error = e.message ?: "Could not save address") }
            }
    }

    fun remove(address: String) {
        addressBookRepository.remove(address)
        _state.update { it.copy(entries = addressBookRepository.list()) }
    }

    companion object {
        fun factory(addressBookRepository: AddressBookRepository) = viewModelFactory {
            initializer { AddressBookViewModel(addressBookRepository) }
        }
    }
}

// ---------------------------------------------------------------------------
// Send
// ---------------------------------------------------------------------------

data class SendUiState(
    val coinAlias: String = "",
    val amount: String = "",
    val destination: String = "",
    val entries: List<AddressBookEntry> = emptyList(),
    val sending: Boolean = false,
    val error: String? = null,
    val successHash: String? = null
)

class SendViewModel(
    private val walletRepository: WalletRepository,
    private val chainConfigRepository: ChainConfigRepository,
    private val addressBookRepository: AddressBookRepository,
    private val rpcFactory: (String) -> RpcClient,
    private val transferFactory: (ChainConfig, (String) -> RpcClient) -> TransferService =
        { config, factory -> TransferService(config, factory) },
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    private val _state = MutableStateFlow(SendUiState())
    val state: StateFlow<SendUiState> = _state.asStateFlow()

    init {
        val alias = chainConfigRepository.load()?.coinAlias ?: ""
        _state.value = SendUiState(coinAlias = alias, entries = addressBookRepository.list())
    }

    fun onAmountChange(value: String) = _state.update { it.copy(amount = value, error = null) }
    fun onDestinationChange(value: String) = _state.update { it.copy(destination = value, error = null) }
    fun onPick(entry: AddressBookEntry) = _state.update { it.copy(destination = entry.address, error = null) }
    fun consumeSuccess() = _state.update { it.copy(successHash = null) }

    fun send() {
        val s = _state.value
        val wallet = walletRepository.load()
        val config = chainConfigRepository.load()

        if (wallet == null) {
            _state.update { it.copy(error = "No wallet found") }
            return
        }
        if (config == null) {
            _state.update { it.copy(error = "Configure a chain first") }
            return
        }
        val valueWei = WeiFormat.parseOrNull(s.amount)
        if (valueWei == null || valueWei.signum() <= 0) {
            _state.update { it.copy(error = "Enter an amount greater than 0") }
            return
        }
        if (!AddressUtils.isValid(s.destination)) {
            _state.update { it.copy(error = "Enter a valid destination address") }
            return
        }

        _state.update { it.copy(sending = true, error = null, successHash = null) }
        viewModelScope.launch(ioDispatcher) {
            runCatching {
                val rpc = rpcFactory(config.rpcUrl)
                val balance = rpc.getBalance(wallet.address)
                if (valueWei > balance) throw IllegalArgumentException("Amount exceeds balance")
                val credentials = Mnemonics.deriveCredentials(wallet.mnemonic)
                transferFactory(config, rpcFactory).send(credentials, s.destination, valueWei)
            }.onSuccess { result ->
                when (result) {
                    is TransferResult.Success ->
                        _state.update { it.copy(sending = false, successHash = result.txHash, error = null) }
                    is TransferResult.Failure ->
                        _state.update { it.copy(sending = false, error = result.message) }
                }
            }.onFailure { e ->
                _state.update { it.copy(sending = false, error = e.message ?: "Transaction failed") }
            }
        }
    }

    companion object {
        fun factory(
            walletRepository: WalletRepository,
            chainConfigRepository: ChainConfigRepository,
            addressBookRepository: AddressBookRepository,
            rpcFactory: (String) -> RpcClient
        ) = viewModelFactory {
            initializer {
                SendViewModel(
                    walletRepository,
                    chainConfigRepository,
                    addressBookRepository,
                    rpcFactory
                )
            }
        }
    }
}
