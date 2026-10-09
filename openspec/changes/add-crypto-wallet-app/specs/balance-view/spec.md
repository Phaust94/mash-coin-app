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
