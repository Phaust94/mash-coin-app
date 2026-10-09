package com.soapp.wallet.net

/** Builds block-explorer links from the user-configured explorer base URL. */
object ExplorerLink {

    /**
     * Returns `<base>/address/<address>` for an etherscan-style explorer,
     * tolerating trailing slashes on the base URL.
     */
    fun addressUrl(explorerBaseUrl: String, address: String): String =
        explorerBaseUrl.trim().trimEnd('/') + "/address/" + address.trim()
}
