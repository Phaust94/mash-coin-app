# Spec Delta

## Purpose

Lets the user create the app's single self-custodial wallet by importing a BIP39 recovery
phrase and deriving an ETH-style account address, persisted on device.

## ADDED Requirements

### Requirement: Import a recovery phrase

The system SHALL accept a recovery phrase of exactly 12 or 24 words and SHALL validate it
against the BIP39 wordlist and checksum before use.

#### Scenario: Valid 12-word phrase

- **WHEN** the user submits a valid 12-word BIP39 phrase
- **THEN** the system accepts it and proceeds to derive the account

#### Scenario: Valid 24-word phrase

- **WHEN** the user submits a valid 24-word BIP39 phrase
- **THEN** the system accepts it and proceeds to derive the account

#### Scenario: Invalid phrase rejected

- **WHEN** the user submits a phrase with an unsupported word count, words outside the BIP39 wordlist, or an invalid checksum
- **THEN** the system rejects it, shows an error, and stores nothing

### Requirement: Derive the account address

The system SHALL derive one ETH-style account address from the imported phrase using the
BIP44 path `m/44'/60'/0'/0/0`, deterministically for the same phrase.

#### Scenario: Deterministic address

- **WHEN** the same valid phrase is imported more than once
- **THEN** the derived address is identical each time

#### Scenario: Address shown after import

- **WHEN** a phrase is imported successfully
- **THEN** the system displays the derived address so the user can confirm it

### Requirement: Persist the wallet on device

The system SHALL store the recovery phrase and derived address in app-private storage and
SHALL restore them on later launches without re-entering the phrase.

#### Scenario: Wallet survives restart

- **WHEN** a wallet has been imported and the app is reopened
- **THEN** the stored wallet is loaded without prompting for the phrase again

### Requirement: Replace the existing wallet

The system SHALL support exactly one wallet, and importing a new phrase when a wallet
already exists SHALL replace the stored wallet.

#### Scenario: Re-import replaces wallet

- **WHEN** the user imports a new valid phrase while a wallet already exists
- **THEN** the previously stored phrase and address are replaced by the new wallet

### Requirement: Gate the app on wallet presence

The system SHALL show the import flow when no wallet is stored and the main wallet
interface once a wallet exists.

#### Scenario: First launch without wallet

- **WHEN** the app opens and no wallet is stored
- **THEN** the import flow is shown

#### Scenario: Launch with existing wallet

- **WHEN** the app opens and a wallet is stored
- **THEN** the main wallet interface is shown
