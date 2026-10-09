# Proposal

## Why

There is no app to interact with the user's custom ETH-based chain. The user needs a
minimal, self-custodial Android wallet to import a recovery phrase, point at a chain,
view the chain coin's balance, and send that coin — without multi-asset complexity or
heavy security UX. Security is deliberately relaxed: this chain has no ties to real
currency, so storing the key on device and skipping app password protection is acceptable.

## What Changes

- Create a new greenfield Android app (Kotlin + Jetpack Compose) in this repo.
- Import a 12- or 24-word BIP39 recovery phrase, validate it, and derive an ETH-style
  address using the standard BIP44 path `m/44'/60'/0'/0/0`.
- Persist the recovery phrase and derived wallet material in app-private storage.
- Let the user configure the target chain: JSON-RPC URL, coin alias, and chain ID.
- Show the current balance of the target coin on the home screen; fetch on app open and
  provide a manual refresh action.
- Sending flow: prompt for amount and a destination address (entered manually or picked
  from an address book), then automatically fetch nonce, estimate gas, apply the EIP-155
  chain ID, sign locally, and broadcast the raw transaction.
- Maintain a local address book of named addresses that can be searched/selected when sending.
- Support exactly one wallet and one coin at a time; re-importing a phrase replaces the
  existing wallet, and reconfiguring replaces the chain settings.

Out of scope: multiple wallets, multiple coins, fiat/IRL currency conversion, contacts
sync, hardware wallets, transaction history, app password/biometric protection, and
network-specific customizations beyond standard Ethereum JSON-RPC.

## Capabilities

### New Capabilities

- `wallet-setup`: Import and validate a 12/24-word BIP39 recovery phrase, derive the
  ETH-style account via BIP44, and persist the mnemonic and derived wallet details.
- `chain-configuration`: Configure and persist the target chain's JSON-RPC URL, coin
  alias, and chain ID used for balance and send operations.
- `balance-view`: Display the target coin balance, fetch it on app open, and refresh it
  on demand.
- `coin-transfer`: Build, sign, and broadcast a transfer of the target coin given an
  amount and destination address.
- `address-book`: Store, list, and select named recipient addresses for use when sending.

### Modified Capabilities

- None (greenfield project; no existing specs).

## Impact

- Introduces a new Android/Gradle project (Kotlin, Jetpack Compose, minSdk 26) at the
  repository root. The existing empty Python scaffold (`pyproject.toml`, `.venv`) is
  unrelated and stays untouched.
- New dependencies for BIP39/BIP32 key derivation and secp256k1 signing (e.g. web3j or
  equivalent), plus networking for Ethereum JSON-RPC.
- No backend services; all secrets and the address book live on device in app-private
  storage. No ties to real currency.
