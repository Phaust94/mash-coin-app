package com.soapp.wallet.data

import com.soapp.wallet.crypto.AddressUtils
import com.soapp.wallet.model.AddressBookEntry
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

/** Stores the address book in app-private storage as JSON. */
class AddressBookRepository(
    private val store: KeyValueStore,
    private val json: Json = Json { ignoreUnknownKeys = true }
) {
    private val listSerializer = ListSerializer(AddressBookEntry.serializer())

    fun list(): List<AddressBookEntry> =
        store.getString(KEY)?.let { raw ->
            runCatching { json.decodeFromString(listSerializer, raw) }.getOrNull()
        } ?: emptyList()

    /**
     * Adds an entry, updating the name when the address already exists.
     * Rejects a blank name or a malformed address without changing storage.
     */
    fun add(name: String, address: String): Result<Unit> {
        val trimmedName = name.trim()
        if (trimmedName.isBlank()) return Result.failure(IllegalArgumentException("Enter a name"))
        if (!AddressUtils.isValid(address)) {
            return Result.failure(IllegalArgumentException("Enter a valid address"))
        }

        val normalized = AddressUtils.normalize(address)
        val entries = list().toMutableList()
        val index = entries.indexOfFirst { AddressUtils.normalize(it.address) == normalized }
        val entry = AddressBookEntry(name = trimmedName, address = normalized)
        if (index >= 0) entries[index] = entry else entries.add(entry)

        persist(entries)
        return Result.success(Unit)
    }

    fun remove(address: String) {
        val normalized = AddressUtils.normalize(address)
        persist(list().filterNot { AddressUtils.normalize(it.address) == normalized })
    }

    private fun persist(entries: List<AddressBookEntry>) {
        store.putString(KEY, json.encodeToString(listSerializer, entries))
    }

    companion object {
        const val KEY = "address_book"
    }
}
