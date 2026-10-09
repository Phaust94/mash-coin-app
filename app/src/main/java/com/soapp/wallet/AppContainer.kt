package com.soapp.wallet

import android.content.Context
import com.soapp.wallet.data.AddressBookRepository
import com.soapp.wallet.data.ChainConfigRepository
import com.soapp.wallet.data.KeyValueStore
import com.soapp.wallet.data.PrefsKeyValueStore
import com.soapp.wallet.data.WalletRepository
import com.soapp.wallet.net.RpcClient
import com.soapp.wallet.net.Web3jRpcClient

/** Manual dependency container shared by the app's screens. */
class AppContainer(context: Context) {

    private val store: KeyValueStore =
        PrefsKeyValueStore(context.getSharedPreferences("soapp_prefs", Context.MODE_PRIVATE))

    val walletRepository = WalletRepository(store)
    val chainConfigRepository = ChainConfigRepository(store)
    val addressBookRepository = AddressBookRepository(store)

    val rpcFactory: (String) -> RpcClient = { url -> Web3jRpcClient(url) }
}
