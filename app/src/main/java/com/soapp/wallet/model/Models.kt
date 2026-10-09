package com.soapp.wallet.model

import kotlinx.serialization.Serializable

/** The single wallet held by the app. */
@Serializable
data class Wallet(
    val address: String,
    val mnemonic: String
)

/** The single target chain the wallet operates against. */
@Serializable
data class ChainConfig(
    val rpcUrl: String,
    val coinAlias: String,
    val chainId: Long
)

/** A named recipient address saved in the phonebook. */
@Serializable
data class AddressBookEntry(
    val name: String,
    val address: String
)
