# Spec Delta

## Purpose

Defines the single target chain the wallet operates against: the JSON-RPC endpoint, the
human-readable coin alias, and the numeric chain ID used for signing.

## ADDED Requirements

### Requirement: Configure the target chain

The system SHALL let the user set a JSON-RPC URL, a coin alias, and a numeric chain ID,
and SHALL persist these settings on device.

#### Scenario: Valid configuration saved

- **WHEN** the user supplies an http(s) JSON-RPC URL, a non-empty coin alias, and a positive integer chain ID
- **THEN** the system saves the configuration

#### Scenario: Invalid URL rejected

- **WHEN** the user supplies a URL that is empty, not http(s), or otherwise not a valid URL
- **THEN** the system rejects it with an error and does not save the configuration

#### Scenario: Invalid chain ID rejected

- **WHEN** the user supplies a chain ID that is empty, non-numeric, or not a positive integer
- **THEN** the system rejects it with an error and does not save the configuration

### Requirement: Use the configured chain for network operations

The system SHALL send all balance and transaction requests to the configured JSON-RPC URL
using standard Ethereum JSON-RPC methods, and SHALL use the configured chain ID for
EIP-155 transaction signing.

#### Scenario: Requests target the configured endpoint

- **WHEN** the system performs a balance or transaction operation
- **THEN** it sends the request to the configured JSON-RPC URL

#### Scenario: Signing uses the configured chain ID

- **WHEN** the system signs a transaction
- **THEN** the signature incorporates the configured chain ID per EIP-155

### Requirement: Display the coin alias

The system SHALL display the configured coin alias wherever the balance or a transaction
amount is shown.

#### Scenario: Alias shown with balance

- **WHEN** the balance is displayed
- **THEN** it is labeled with the configured coin alias

### Requirement: Require configuration before use

The system SHALL require a saved, valid chain configuration before allowing balance or
send operations.

#### Scenario: Operations blocked without configuration

- **WHEN** no chain configuration is saved and the user attempts to view a balance or send
- **THEN** the system prompts the user to configure the chain instead of performing the operation
