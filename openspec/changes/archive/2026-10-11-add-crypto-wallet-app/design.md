# Design

## Context

Greenfield Android app in an otherwise empty repository (an unrelated Python scaffold
already exists at the root). See `proposal.md` for motivation and `specs/` for required
behavior. The app talks to a user-provided, ETH-based chain over standard Ethereum
JSON-RPC and holds a single BIP39 wallet. Security is intentionally relaxed: the key is
stored on device and there is no app password.

## Goals / Non-Goals

**Goals:**

- A single-module Android app that imports a phrase, configures a chain, shows the coin
  balance, and sends the coin with no gas-field entry.
- Deterministic, standard key derivation and Ethereum-compatible signing so the wallet
  works against any ETH-style JSON-RPC endpoint.
- Flat, minimal architecture that is easy to extend later (e.g. multiple coins).

**Non-Goals:**

- Multiple wallets/coins, transaction history, fiat values, contacts sync, biometrics,
  encryption of the stored mnemonic, or EIP-1559/exotic transaction types.
- Production-grade key management or auditing.

## Decisions

### UI and language: Kotlin + Jetpack Compose, single Activity

Compose keeps the small, single-screen app free of XML boilerplate and makes state-driven
UI straightforward. Rejected: XML Views (more boilerplate for a form-heavy flow) and Java
(poorer coroutines/Flow ergonomics). `minSdk 26` (assumption), `compileSdk`/`targetSdk`
at the current stable API.

### Crypto and RPC: web3j

Use web3j's crypto + core modules for BIP39 mnemonic handling, BIP32/BIP44 derivation of
the account at `m/44'/60'/0'/0/0`, secp256k1 signing/address checksumming, RLP encoding,
and JSON-RPC via `HttpService`. Rejected: assembling BouncyCastle + custom RLP (too much
surface for a simple app); bitcoinj (BIP39 only, no signing/tx); KEthereum/ethers-kt
(viable but less coverage out of the box). web3j needs Android desugaring/ProGuard rules,
which we will configure.

### Transaction type: legacy (type 0) with `eth_gasPrice`

Build legacy transactions and take gas price from `eth_gasPrice`, nonce from
`eth_getTransactionCount(address, "pending")`, and gas limit from `eth_estimateGas`.
Legacy transactions maximize compatibility with a custom ETH-based chain that may not
implement EIP-1559. Rejected: EIP-1559 fee fields (may be unsupported). EIP-155 chain ID
comes from the configured chain ID.

### Amount handling: BigInt wei, BigDecimal for display

Represent amounts as `BigInteger` wei and parse/format with 18 decimals via `BigDecimal`.
Never use `Double` for money. Coin decimals are assumed 18 (documented assumption); the
formatting helper is isolated so a per-coin decimals setting can be added later.

### Storage: plain app-private preferences

Persist mnemonic, derived address, chain configuration, and the address book (as JSON) in
app-private `SharedPreferences`. This matches the relaxed security requirement. Rejected:
`EncryptedSharedPreferences`/Keystore (unnecessary per requirements) and Room (overkill
for one wallet and a small contact list). The app is marked `android:allowBackup="false"`
to reduce accidental mnemonic exfiltration via backups.

### Architecture: one `:app` module, layering by package

Packages: `ui` (Compose screens + `ViewModel`s exposing `StateFlow`), `data` (repositories
for wallet, chain config, address book), `crypto` (derivation, signing), `net` (JSON-RPC
client), `model` (domain types). Network and signing run on `Dispatchers.IO`. Errors are
mapped to user-facing states rather than thrown to the UI.

### Address parsing and validation

Accept a `0x`-prefixed 40-hex-character address; normalize to lowercase for storage and
comparison; use web3j to render a checksummed form for display. Reject anything else
before building a transaction.

## Risks / Trade-offs

- **Custom chain deviates from Ethereum JSON-RPC or lacks EIP-155/legacy support** →
  keep to the most standard methods (legacy tx, `eth_getBalance`, `eth_gasPrice`,
  `eth_estimateGas`, `eth_sendRawTransaction`); surface clear RPC errors.
- **Mnemonic stored in plaintext** → accepted per requirements; confine to app-private
  storage, disable backups, and never transmit the phrase or private key.
- **18-decimal assumption is wrong for the target coin** → isolate formatting so a
  decimals setting can be added without touching specs; document the assumption.
- **`eth_gasPrice`/`eth_estimateGas` unreliable on the custom chain** → fail the send
  with an error instead of sending a bad transaction; a manual gas override is a future
  extension.
- **web3j + ProGuard/R8 on Android** → add keep rules for web3j/BouncyCastle; test a
  release build early.
- **Nonce races with pending transactions** → use the `"pending"` nonce; acceptable for a
  single-user simple wallet.

## Migration Plan

Greenfield. No data migration. Rollback is deleting the added Gradle/Android files; the
existing Python scaffold is untouched.

## Open Questions

- Confirm the coin's decimal places (assumed 18) and whether the address book should cap
  its size; both can be answered later without changing the specs or approach.
