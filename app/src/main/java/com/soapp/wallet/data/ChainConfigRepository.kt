package com.soapp.wallet.data

import com.soapp.wallet.model.ChainConfig
import kotlinx.serialization.json.Json
import java.net.URI

/** Stores the single target chain configuration with validation on save. */
class ChainConfigRepository(
    private val store: KeyValueStore,
    private val json: Json = Json { ignoreUnknownKeys = true }
) {
    fun load(): ChainConfig? = store.getString(KEY)?.let { raw ->
        runCatching { json.decodeFromString(ChainConfig.serializer(), raw) }.getOrNull()
    }

    fun save(config: ChainConfig): Result<Unit> {
        val error = validate(config)
            ?: return runCatching {
                store.putString(KEY, json.encodeToString(ChainConfig.serializer(), config))
            }
        return Result.failure(IllegalArgumentException(error))
    }

    fun clear() = store.remove(KEY)

    companion object {
        const val KEY = "chain_config"

        /** Returns a user-facing error message, or null when the config is valid. */
        fun validate(config: ChainConfig): String? {
            if (!isValidHttpUrl(config.rpcUrl)) return "Enter a valid http(s) JSON-RPC URL"
            if (config.coinAlias.isBlank()) return "Enter a coin alias"
            if (config.chainId <= 0) return "Enter a positive numeric chain ID"
            return null
        }

        private fun isValidHttpUrl(url: String): Boolean = try {
            val uri = URI(url.trim())
            (uri.scheme == "http" || uri.scheme == "https") && !uri.host.isNullOrBlank()
        } catch (e: Exception) {
            false
        }
    }
}
