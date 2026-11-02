# Proposal

## Why

This change builds a Progressive Web App (PWA) version of the MASH Android Ethereum wallet. Mobile web users need a secure, self-custodial wallet experience that works across all platforms including Android, iOS, and desktop browsers, with the same functionality as the native Android app.

## What Changes

- Create a new PWA capability `wallet-pwa` that exposes wallet functionality as a web application
- Port the existing wallet logic to TypeScript/JavaScript for browser compatibility
- Add PWA-specific features like service workers, offline support, and native-like install experience
- Implement all wallet features: import seed phrase, chain configuration, balance checking, and token transfers
- Add web3 provider integration for blockchain communication

## Capabilities

### New Capabilities
- `wallet-pwa`: Web-based wallet functionality with PWA features for multi-platform access to MASH wallet services

### Modified Capabilities

## Impact

- **Affected code**: The wallet's core logic (crypto/, data/, net/, transfer/) will be refactored for browser compatibility
- **New dependencies**: Browser-compatible web3 library, WebCrypto API, and service worker APIs
- **New API surface**: Web API endpoints for wallet operations
- **Platform expansion**: From Android-only to web/mobile/desktop with native-like PWA experience
- **Security considerations**: Secure storage mechanisms for browser environment, WebCrypto API usage