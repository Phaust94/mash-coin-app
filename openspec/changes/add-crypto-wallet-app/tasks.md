# Tasks

## 1. Project setup

- [x] 1.1 Scaffold a single-module Android project (`settings.gradle.kts`, root and `app` build scripts, manifest, `MainActivity`, Compose enabled, `minSdk 26`); verify `./gradlew :app:assembleDebug` builds a runnable empty app
- [x] 1.2 Add dependencies (Compose BOM, `lifecycle-viewmodel-compose`, Kotlin coroutines, web3j crypto + core, a JSON library for the address book); verify the build resolves them with `./gradlew :app:dependencies`
- [ ] 1.3 Add R8/ProGuard keep rules for web3j/BouncyCastle and set `android:allowBackup="false"`; verify `./gradlew :app:assembleRelease` succeeds and the app launches — *`assembleRelease` (R8) verified on JDK 21 + SDK 35; app-launch check pending a device/emulator*
- [x] 1.4 Add a README with build/run instructions; verify each documented command runs as written — *build/test/release commands verified; `installDebug` needs a device*

## 2. Wallet setup

- [x] 2.1 Implement BIP39 phrase validation and BIP44 (`m/44'/60'/0'/0/0`) derivation in a `crypto` component; verify unit tests: a known 12-word and 24-word phrase each map to their known address, and invalid word count/checksum is rejected
- [x] 2.2 Implement a wallet repository that persists the mnemonic and derived address in app-private storage; verify a unit test that save/load round-trips and a fresh instance reads the stored wallet
- [ ] 2.3 Build the import UI (phrase entry, validation error, display of the derived address); verify manually that valid phrases proceed, invalid ones show an error and store nothing, and the address is shown — *UI implemented; manual check pending a device/emulator*
- [ ] 2.4 Wire app gating and single-wallet replacement; verify manually that a fresh install shows the import flow, an existing wallet shows the main interface, and re-importing replaces the stored wallet — *gating + re-import implemented; manual check pending a device/emulator*

## 3. Chain configuration

- [x] 3.1 Implement a chain-config repository persisting JSON-RPC URL, coin alias, and chain ID with validation; verify unit tests for valid save plus rejection of a bad URL, empty alias, and non-numeric or non-positive chain ID
- [ ] 3.2 Build the chain-config UI; verify manually that a valid configuration saves and invalid inputs are rejected with errors — *UI implemented; manual check pending a device/emulator*
- [x] 3.3 Implement an Ethereum JSON-RPC client bound to the configured URL; verify with an `OkHttp MockWebServer` test that `eth_getBalance` and `eth_chainId` requests are sent to the configured endpoint
- [ ] 3.4 Add an optional explorer URL to the chain configuration (blank allowed, non-blank must be http(s), values trimmed on save, old payloads without it still load); verify unit tests — *implemented + unit-tested; verify on device*

## 4. Balance view

- [x] 4.1 Implement balance retrieval (`eth_getBalance`) and wei-to-coin formatting at 18 decimals with `BigDecimal`; verify unit tests for 0, a fractional amount, and exactly 1 coin
- [ ] 4.2 Build the home screen showing the balance labeled with the coin alias, auto-fetch on open, a manual refresh action, and loading/error states; verify manually plus a ViewModel unit test with a fake repository (open triggers fetch, refresh updates the value, failure shows error and recovery clears it) — *ViewModel unit test passes; manual UI check pending a device/emulator*
- [ ] 4.3 Add a History action above Chain settings that opens `<explorer>/address/<wallet address>` in an external browser and is disabled while no explorer URL is configured; verify unit tests for the URL building and ViewModel state — *implemented + unit-tested; verify on device*

## 5. Address book

- [x] 5.1 Implement the address book repository (JSON persistence, add/update/delete/list, empty-name and malformed-address rejection, no duplicate addresses); verify unit tests for each case and that entries survive a new instance
- [ ] 5.2 Build the address book UI (list, add, delete); verify manually that entries persist across an app restart — *UI implemented; manual check pending a device/emulator*

## 6. Coin transfer

- [x] 6.1 Implement the transaction builder/signer (pending nonce, `eth_estimateGas`, `eth_gasPrice`, EIP-155 chain ID, web3j signing); verify offline unit tests that decode the signed raw transaction and assert the to address, value, nonce, and chain ID, and that an invalid address or amount is refused
- [ ] 6.2 Build the send flow UI (amount + destination, manual entry or pick from the address book) with amount/address/balance validation; verify manually that valid input proceeds and non-positive, over-balance, and malformed-address inputs are blocked — *UI implemented; manual check pending a device/emulator*
- [x] 6.3 Wire broadcasting via `eth_sendRawTransaction` and outcome reporting (hash on success, error otherwise); verify with a MockWebServer test for both an accepted transaction and an RPC error, asserting no send is reported on failure
- [x] 6.4 Verify the secret never leaves the device; assert from recorded MockWebServer request bodies during balance fetch and send that no mnemonic or private key is present

## 7. Integration verification

- [ ] 7.1 Run an end-to-end manual pass on a device/emulator against a test ETH JSON-RPC endpoint: import a phrase, configure the chain, see the balance, add a phonebook entry, send to it, and confirm a transaction hash is returned; record the result — *pending a device/emulator + a test endpoint (skipped by user decision on 2026-10-10)*
- [ ] 7.2 Verify failure handling across the app: with an unreachable/bad endpoint, opening, refreshing, and sending each show an error without crashing — *pending a device/emulator (skipped by user decision on 2026-10-10)*

## Workflow follow-up

- Archive the change after the project's review requirements are satisfied (the remaining device/emulator checks above must be completed first).
- Verify the archived result.