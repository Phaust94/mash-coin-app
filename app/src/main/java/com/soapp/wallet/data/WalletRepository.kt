package com.soapp.wallet.data

import com.soapp.wallet.model.Wallet
import kotlinx.serialization.json.Json

/** Stores the single wallet (mnemonic + derived address) in app-private storage. */
class WalletRepository(
    private val store: KeyValueStore,
    private val json: Json = Json { ignoreUnknownKeys = true }
) {
    fun save(wallet: Wallet) {
        store.putString(KEY, json.encodeToString(Wallet.serializer(), wallet))
    }

    fun load(): Wallet? = store.getString(KEY)?.let { raw ->
        runCatching { json.decodeFromString(Wallet.serializer(), raw) }.getOrNull()
    }

    fun clear() = store.remove(KEY)

    fun hasWallet(): Boolean = load() != null

    companion object {
        const val KEY = "wallet"
    }
}
