# Spec Delta

## Purpose

Shows the current balance of the configured coin for the wallet address and keeps it up
to date on open and on demand.

## ADDED Requirements

### Requirement: Display the balance

The system SHALL display the wallet's balance of the target coin, labeled with the
configured coin alias, assuming 18 decimal places for the coin's smallest unit.

#### Scenario: Balance shown with alias

- **WHEN** a balance value is available
- **THEN** the system shows it labeled with the configured coin alias

#### Scenario: Zero balance shown

- **WHEN** the wallet address holds no funds
- **THEN** the system displays a balance of 0

### Requirement: Fetch the balance on open

The system SHALL automatically fetch the current balance from the configured endpoint
when the main interface opens.

#### Scenario: Opening triggers a fetch

- **WHEN** the main interface opens
- **THEN** the system requests the balance and shows a loading state until it resolves

### Requirement: Refresh the balance manually

The system SHALL provide a refresh action that re-fetches the current balance.

#### Scenario: Manual refresh updates the balance

- **WHEN** the user triggers the refresh action
- **THEN** the system re-fetches the balance and updates the displayed value

### Requirement: Handle balance fetch failures

The system SHALL show an error state when a balance fetch fails and SHALL NOT crash.

#### Scenario: Fetch failure shows an error

- **WHEN** the balance request fails or times out
- **THEN** the system shows an error message and keeps the refresh action available

#### Scenario: Recovery after failure

- **WHEN** a later refresh succeeds after a failure
- **THEN** the system replaces the error state with the newly fetched balance

### Requirement: Open the address history in the block explorer

The system SHALL provide a History action on the main interface, placed above the
chain-settings action, that opens the wallet address page of the configured block
explorer in an external browser at `<explorer>/address/<wallet address>`. The action
SHALL be disabled while no explorer URL is configured.

#### Scenario: History opens the explorer

- **WHEN** an explorer URL is configured and the user triggers the History action
- **THEN** the system opens `<explorer>/address/<wallet address>` in an external browser

#### Scenario: History disabled without explorer

- **WHEN** no explorer URL is configured
- **THEN** the History action is shown disabled

### Requirement: Copy the wallet address

The system SHALL provide a copy action next to the displayed wallet address on the main
interface that copies the address to the clipboard.

#### Scenario: Address copied

- **WHEN** the user triggers the copy action next to the address
- **THEN** the wallet address is copied to the clipboard
