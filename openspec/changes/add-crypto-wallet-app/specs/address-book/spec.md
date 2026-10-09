# Spec Delta

## Purpose

Stores named recipient addresses on device so they can be reused when sending the target
coin.

## ADDED Requirements

### Requirement: Add a named address

The system SHALL let the user save a name and a destination address, SHALL reject an
empty name or a malformed address, and SHALL keep at most one entry per address.

#### Scenario: Valid entry saved

- **WHEN** the user saves a non-empty name and a well-formed address
- **THEN** the system stores the entry in the address book

#### Scenario: Invalid entry rejected

- **WHEN** the user saves an empty name or a malformed address
- **THEN** the system rejects it with an error and stores nothing

#### Scenario: Existing address not duplicated

- **WHEN** the user saves an address that is already in the book
- **THEN** the system updates the existing entry instead of adding a duplicate

### Requirement: List saved addresses

The system SHALL list the saved names and their addresses.

#### Scenario: Book lists entries

- **WHEN** the user opens the address book
- **THEN** the system shows all saved entries with their names and addresses

### Requirement: Select an address when sending

The send flow SHALL allow the user to choose a destination from the address book as an
alternative to entering it manually.

#### Scenario: Choosing from the book fills the address

- **WHEN** the user selects an entry from the address book during a send
- **THEN** the system uses that entry's address as the destination

### Requirement: Remove a saved address

The system SHALL let the user delete a saved entry.

#### Scenario: Entry removed

- **WHEN** the user deletes an entry
- **THEN** the system removes it from the address book

### Requirement: Persist the address book

The system SHALL persist the address book in app-private storage across launches.

#### Scenario: Book survives restart

- **WHEN** the app is reopened after entries were saved
- **THEN** the saved entries are still present
