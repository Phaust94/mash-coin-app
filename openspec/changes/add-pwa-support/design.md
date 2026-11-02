# Design: wallet-pwa

## Architecture Overview

The PWA wallet will be built as a single-page application using modern web technologies that mirror the native Android wallet's functionality while providing web compatibility.

### Technology Stack
- **Frontend**: React with TypeScript and Vite build system
- **State Management**: Zustand or Redux for wallet state management
- **Crypto**: WebCrypto API and ethers.js for blockchain operations
- **Storage**: IndexedDB with service worker for offline support
- **PWA Features**: Service workers, manifest.json, offline caching, installable experience

## Component Structure

### Core Components

1. **WalletProvider** - Context provider for wallet state and operations
2. **SeedPhraseImport** - Component for importing wallet via BIP39 seed phrase
3. **ChainConfig** - Component for setting up blockchain connections
4. **WalletView** - Main wallet interface showing balance and actions
5. **SendForm** - Transaction composition and signing form
6. **AddressBook** - Interface for managing saved addresses
7. **NetworkProvider** - Web3 provider abstraction for browser compatibility

### Folder Structure
```
src/
├── components/
│   ├── wallet/
│   ├── crypto/
│   ├── network/
│   └── ui/
├── context/
│   └── WalletContext.tsx
├── hooks/
│   └── useWallet.ts
├── services/
│   ├── web3.ts
│   ├── storage.ts
│   └── crypto.ts
├── utils/
│   └── format.ts
└── App.tsx
```

## State Management

The wallet state will be managed through a context-based approach with Zustand or Redux, providing:
- Wallet identity (address, chain info)
- Balance state (loading, error, current value)
- Transaction queue state
- Address book state
- Chain configuration state

## API Design

The PWA will expose a clean API surface that mirrors the native app's functionality:

### Wallet Operations
- `importWallet(seedPhrase: string, chainConfig: ChainConfig)`
- `getBalance(address: string): Promise<Balance>`
- `sendToken(amount: string, to: string): Promise<TransactionHash>`
- `getAddressBook(): Promise<AddressBookEntry[]>`

### PWA-Specific Features
- `registerServiceWorker()`
- `addToHomeScreen()`
- `getInstallPrompt()`

## Security Considerations

- Use WebCrypto API for secure key management
- Store seed phrases in encrypted IndexedDB
- Implement proper origin verification for web3 provider
- Use Content Security Policy (CSP) headers
- Regular security audits of dependencies

## Testing Strategy

- Unit tests for core logic and state management
- Integration tests for wallet operations
- End-to-end tests using Playwright or Cypress
- PWA-specific tests for service worker functionality
- Accessibility testing for screen readers and keyboard navigation