package com.soapp.wallet.data

import com.soapp.wallet.testing.InMemoryKeyValueStore
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AddressBookRepositoryTest {

    private val address = "0xf39Fd6e51aad88F6F4ce6aB8827279cffFb92266"

    @Test
    fun `valid entry is saved`() {
        val repo = AddressBookRepository(InMemoryKeyValueStore())
        assertTrue(repo.add("Alice", address).isSuccess)
        assertEquals(1, repo.list().size)
        assertEquals("Alice", repo.list().first().name)
        assertEquals(address.lowercase(), repo.list().first().address)
    }

    @Test
    fun `blank name is rejected`() {
        val repo = AddressBookRepository(InMemoryKeyValueStore())
        assertTrue(repo.add("   ", address).isFailure)
        assertTrue(repo.list().isEmpty())
    }

    @Test
    fun `malformed address is rejected`() {
        val repo = AddressBookRepository(InMemoryKeyValueStore())
        assertTrue(repo.add("Alice", "0x1234").isFailure)
        assertTrue(repo.list().isEmpty())
    }

    @Test
    fun `existing address updates instead of duplicating`() {
        val repo = AddressBookRepository(InMemoryKeyValueStore())
        repo.add("Alice", address)
        repo.add("Alice renamed", address.uppercase().replace("0X", "0x"))
        assertEquals(1, repo.list().size)
        assertEquals("Alice renamed", repo.list().first().name)
    }

    @Test
    fun `remove deletes an entry`() {
        val repo = AddressBookRepository(InMemoryKeyValueStore())
        repo.add("Alice", address)
        repo.remove(address)
        assertTrue(repo.list().isEmpty())
    }

    @Test
    fun `entries survive a new instance`() {
        val store = InMemoryKeyValueStore()
        AddressBookRepository(store).add("Bob", address)
        assertEquals(1, AddressBookRepository(store).list().size)
    }
}
