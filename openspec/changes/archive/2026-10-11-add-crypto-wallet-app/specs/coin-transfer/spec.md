# Spec Delta

## Purpose

Lets the user send the target coin from the stored wallet to a destination address by
building, signing, and broadcasting an Ethereum transaction.

## ADDED Requirements

### Requirement: Initiate a send

The system SHALL provide a single send action on the main interface that opens a send
flow.

#### Scenario: Send action opens the flow

- **WHEN** the user taps the send action
- **THEN** the system opens a form for the amount and destination address

### Requirement: Collect and validate the amount and destination

The send flow SHALL require an amount of the target coin and a destination address, SHALL
reject a non-positive amount, an amount greater than the current balance, and a
malformed destination address.

#### Scenario: Valid amount and address accepted

- **WHEN** the user enters a positive amount within the balance and a well-formed destination address
- **THEN** the system proceeds to build the transaction

#### Scenario: Invalid amount rejected

- **WHEN** the user enters an amount that is non-positive or greater than the current balance
- **THEN** the system blocks the send and shows a validation error

#### Scenario: Invalid address rejected

- **WHEN** the user enters a malformed destination address
- **THEN** the system blocks the send and shows a validation error

### Requirement: Determine transaction parameters automatically

The system SHALL determine the account nonce, gas limit, gas price, and chain ID without
requiring the user to enter gas fields.

#### Scenario: Parameters resolved automatically

- **WHEN** the user confirms a valid send
- **THEN** the system fetches the nonce and gas parameters and applies the configured chain ID without prompting the user for them

#### Scenario: Parameter lookup failure

- **WHEN** resolving the nonce or gas parameters fails
- **THEN** the system aborts the send and shows an error without signing or broadcasting

### Requirement: Sign locally and broadcast

The system SHALL sign the transaction on device with the stored private key using
EIP-155 and SHALL broadcast it via `eth_sendRawTransaction`, and the private key SHALL
never leave the device.

#### Scenario: Transaction broadcast

- **WHEN** the transaction is signed
- **THEN** the system broadcasts the raw signed transaction to the configured endpoint

#### Scenario: Key stays on device

- **WHEN** any transaction is built or sent
- **THEN** only the signed transaction is transmitted, never the recovery phrase or private key

### Requirement: Report the send outcome

The system SHALL report a successful send with its transaction hash or a clear error on
failure.

#### Scenario: Success reports the hash

- **WHEN** the endpoint accepts the signed transaction
- **THEN** the system shows a success message including the transaction hash

#### Scenario: Failure leaves state unchanged

- **WHEN** signing or broadcasting fails
- **THEN** the system shows an error and no transaction is reported as sent
