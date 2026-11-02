import React, { useState } from 'react';
import { useWalletStore } from "../../context/WalletContext";
import { StorageService } from "../../services/storage";

interface AddressBookProps {
  onBack?: () => void;
}

export const AddressBook: React.FC<AddressBookProps> = ({ onBack }) => {
  const { addressBook, setAddressBook, removeAddressBookEntry } = useWalletStore();
  const [newEntryName, setNewEntryName] = useState('');
  const [newEntryAddress, setNewEntryAddress] = useState('');
  const [error, setError] = useState<string | null>(null);

  const handleSave = async () => {
    if (!newEntryName || !newEntryAddress) {
      setError('Name and address are required');
      return;
    }

    // Validate address format
    if (!newEntryAddress.startsWith('0x') || newEntryAddress.length !== 42) {
      setError('Invalid address format');
      return;
    }

    try {
      // Save to address book state
      const storage = new StorageService();
      await storage.initialize();
      await storage.saveAddressBook([
        ...addressBook,
        { name: newEntryName, address: newEntryAddress }
      ]);

      // Update state
      setAddressBook([
        ...addressBook,
        { name: newEntryName, address: newEntryAddress }
      ]);
      
      setNewEntryName('');
      setNewEntryAddress('');
      setError(null);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to save address');
    }
  };

  const handleRemove = (address: string) => {
    removeAddressBookEntry(address);
    const storage = new StorageService();
    storage.initialize();
    storage.saveAddressBook(addressBook.filter((e) => e.address !== address));
  };

  return (
    <div className="p-6 bg-white rounded-lg shadow-md">
      <h2 className="text-xl font-bold mb-4">Address Book</h2>
      
      <div className="space-y-4">
        <div className="p-4 bg-gray-50 rounded-md">
          <h3 className="text-sm font-medium mb-3">Saved Addresses</h3>
          {addressBook.length === 0 ? (
            <p className="text-gray-500">No saved addresses</p>
          ) : (
            <ul className="space-y-2 max-h-40 overflow-y-auto">
              {addressBook.map((entry) => (
                <li key={entry.address} className="flex items-center justify-between gap-3 p-2 bg-white rounded-md shadow-sm">
                  <span className="text-gray-700 flex-1">{entry.name}</span>
                  <button
                    type="button"
                    onClick={() => copyToClipboard(entry.address)}
                    className="text-blue-600 font-medium text-sm"
                  >
                    Copy
                  </button>
                  <button
                    type="button"
                    onClick={() => handleRemove(entry.address)}
                    className="text-red-600 font-medium text-sm"
                  >
                    Remove
                  </button>
                </li>
              ))}
            </ul>
          )}
        </div>
        
        <div className="p-4 bg-gray-50 rounded-md">
          <h3 className="text-sm font-medium mb-3">Add New Address</h3>
          
          <div className="space-y-3">
            <div>
              <label className="block text-gray-700 font-medium mb-1">Name</label>
              <input
                type="text"
                value={newEntryName}
                onChange={(e) => setNewEntryName(e.target.value)}
                className="w-full p-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                placeholder="Friend, Exchange, etc."
              />
            </div>
            
            <div>
              <label className="block text-gray-700 font-medium mb-1">Address</label>
              <input
                type="text"
                value={newEntryAddress}
                onChange={(e) => setNewEntryAddress(e.target.value)}
                className="w-full p-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                placeholder="0x..."
              />
            </div>
          </div>
          
          <button
            onClick={handleSave}
            className="w-full py-2 bg-blue-600 text-white rounded-md font-medium hover:bg-blue-700 transition-colors"
            disabled={!newEntryName || !newEntryAddress}
          >
            {error ? 'Fix errors' : 'Add Address'}
          </button>
        </div>
      </div>
      
      {error && (
        <div className="mt-4 text-red-500">
          <p className="font-medium">Error: {error}</p>
        </div>
      )}

      {onBack && (
        <button
          onClick={onBack}
          className="mt-6 px-8 py-2.5 text-blue-700 bg-white border-2 border-blue-700 rounded-full font-medium hover:bg-blue-50 transition-colors"
        >
          Back
        </button>
      )}
    </div>
  );
};

const copyToClipboard = (address: string) => {
  if (typeof navigator !== 'undefined' && navigator.clipboard) {
    navigator.clipboard.writeText(address).then(() => {
      // Success - could show toast
    });
  }
};