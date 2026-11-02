# Spec: wallet-pwa

## Overview
This spec defines the behavior for the PWA wallet functionality, which provides a web-based interface for MASH wallet operations. The specification covers all wallet capabilities including seed phrase management, chain configuration, balance checking, and token transfers.

## Requirements

### Functional Requirements

1. **Seed Phrase Management**
   - Import wallet using BIP39 seed phrase (12 or 24 words)
   - Validate seed phrase checksum
   - Derive ETH account at path m/44'/60'/0'/0/0
   - Display derived address for confirmation
   - Save wallet securely in browser storage

2. **Chain Configuration**
   - Set JSON-RPC URL (http/https)
   - Select chain alias (e.g., ETH)
   - Enter positive chain ID
   - Save chain configuration

3. **Balance Checking**
   - Display wallet address and balance
   - Manual refresh action to fetch current balance
   - Loading/error states during balance fetching

4. **Token Transfers**
   - Enter amount and destination address
   - Select from address book or enter new address
   - Automatically resolve nonce, gas price, gas limit, and EIP-155 chain ID
   - Sign transaction on browser using WebCrypto API
   - Broadcast transaction via `eth_sendRawTransaction`
   - Display transaction hash result

5. **Address Book**
   - Save named addresses (add/update/delete)
   - Persist address book across sessions
   - Copy address to clipboard
   - Select from address book for transfers

### Non-Functional Requirements

- **Security**: Secure storage of seed phrases and private keys in browser environment
- **Compatibility**: Support modern browsers with Web3 API capabilities
- **Performance**: Efficient balance fetching and transaction signing
- **UX**: Intuitive interface matching native app experience
- **Offline Support**: Service worker for offline access to wallet functions

### Edge Cases

- Invalid seed phrases (checksum failure)
- Invalid JSON-RPC URLs
- Insufficient funds for transaction
- Network connectivity issues
- Browser storage limitations