package com.soapp.wallet.testing

import com.soapp.wallet.data.KeyValueStore

/** In-memory [KeyValueStore] for repository unit tests. */
class InMemoryKeyValueStore : KeyValueStore {
    private val map = mutableMapOf<String, String>()

    override fun getString(key: String): String? = map[key]

    override fun putString(key: String, value: String) {
        map[key] = value
    }

    override fun remove(key: String) {
        map.remove(key)
    }
}
