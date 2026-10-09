package com.soapp.wallet.net

import org.junit.Assert.assertEquals
import org.junit.Test

class ExplorerLinkTest {

    private val address = "0xf39fd6e51aad88f6f4ce6ab8827279cfffb92266"

    @Test
    fun `builds the address page url`() {
        assertEquals(
            "https://explorer.example/address/$address",
            ExplorerLink.addressUrl("https://explorer.example", address)
        )
    }

    @Test
    fun `tolerates trailing slashes on the base url`() {
        assertEquals(
            "https://explorer.example/address/$address",
            ExplorerLink.addressUrl("https://explorer.example///", address)
        )
    }
}
