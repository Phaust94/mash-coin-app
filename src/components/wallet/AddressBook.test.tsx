import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import { AddressBook } from './AddressBook';

// Mock the wallet store
vi.mock('../../context/WalletContext', () => ({
  useWalletStore: () => ({
    addressBook: [],
    setAddressBook: vi.fn(),
    removeAddressBookEntry: vi.fn(),
    addAddressBookEntry: vi.fn()
  })
}));

// Mock the storage service
vi.mock('../../services/storage', () => ({
  StorageService: function() {
    return {
      saveAddressBook: vi.fn().mockResolvedValue(undefined),
      getAddressBook: vi.fn().mockResolvedValue([])
    };
  }
}));

describe('AddressBook Component', () => {
  it('should render without errors', () => {
    render(<AddressBook />);
    expect(true).toBe(true);
  });
  
  it('should show address book heading', () => {
    render(<AddressBook />);
    const heading = screen.getByText('Address Book');
    expect(heading).toBeInTheDocument();
  });
  
  it('should show saved addresses section', () => {
    render(<AddressBook />);
    const section = screen.getByText('Saved Addresses');
    expect(section).toBeInTheDocument();
  });
  
  it('should show add new address section', () => {
    render(<AddressBook />);
    const section = screen.getByText('Add New Address');
    expect(section).toBeInTheDocument();
  });
});