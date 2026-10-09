# MASH — minimal ETH wallet (Android)

A deliberately small, self-custodial Android wallet for a custom ETH-based chain. It
imports a BIP39 recovery phrase, derives a standard ETH account, points at a
user-provided JSON-RPC endpoint, shows the chain coin's balance, and sends that coin.

Scope is intentionally narrow: **one wallet, one coin**. Security is relaxed by design
(the phrase is stored in app-private storage, there is no app password) because this chain
has no ties to real currency.

## Prerequisites

- **JDK 17 or newer** (developed and verified with JDK 21). Point `JAVA_HOME` at it, or
  ensure `java` on `PATH` is a supported JDK.
- **Android SDK** with:
  - Platform `android-35`
  - Build-tools `35.0.0`
  - Platform-tools
- Tell Gradle where the SDK is, either:
  - create `local.properties` with `sdk.dir=/path/to/android-sdk`, or
  - export `ANDROID_HOME=/path/to/android-sdk`.

The project uses the Gradle wrapper, so no separate Gradle install is required.

## Build, test, run

```bash
# Debug build (produces app/build/outputs/apk/debug/app-debug.apk)
./gradlew :app:assembleDebug

# Unit tests
./gradlew :app:testDebugUnitTest

# Minified release build (R8), produces app-release-unsigned.apk
./gradlew :app:assembleRelease

# Install the debug build on a connected device/emulator and launch it
./gradlew :app:installDebug
adb shell am start -n com.soapp.wallet/.MainActivity
```

On Windows use `gradlew.bat` instead of `./gradlew`.

## Using the app

1. **Import wallet** — enter a 12- or 24-word recovery phrase. It is validated (wordlist +
   checksum) and the account is derived at `m/44'/60'/0'/0/0`. The derived address is shown
   for confirmation.
2. **Chain settings** — set the JSON-RPC URL (http/https), a coin alias (e.g. `ETH`), and a
   positive numeric chain ID. Balance and send are blocked until this is saved.
3. **Home** — shows the address and the coin balance (fetched automatically on open, with a
   manual **Refresh** action and loading/error states).
4. **Send coin** — enter an amount and a destination address, or pick one from the address
   book. The nonce, gas price, gas limit, and EIP-155 chain ID are resolved automatically;
   the transaction is signed on device and broadcast via `eth_sendRawTransaction`, and the
   resulting transaction hash is shown.
5. **Address book** — save named addresses (add/update/delete); they persist across launches.
6. **Re-import wallet** — replaces the stored single wallet.

## Assumptions

- The coin has **18 decimals** (adjust `WeiFormat` if the target coin differs).
- **Legacy (type 0) EIP-155** transactions are used for maximum compatibility.
- `minSdk 26`; `compileSdk`/`targetSdk` 35.
- Backups are disabled (`android:allowBackup="false"`).

## Layout

```
app/src/main/java/com/soapp/wallet/
  crypto/    BIP39/BIP44 derivation, address + amount helpers
  data/      wallet, chain-config, and address-book repositories (+ key/value store)
  model/     domain types
  net/       Ethereum JSON-RPC client (web3j)
  transfer/  offline transaction signing + send orchestration
  ui/        ViewModels (StateFlow) and Compose screens
```

The unrelated Python scaffold at the repository root is untouched.
