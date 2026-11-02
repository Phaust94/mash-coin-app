import React, { useState } from 'react';
import { ethers } from 'ethers';
import { validateSeedPhrase } from '../../services/crypto';
import { useWalletStore } from '../../context/WalletContext';
import { StorageService } from '../../services/storage';

/** First standard derivation path used by Ethereum wallets (m/44'/60'/0'/0/0). */
const DEFAULT_DERIVATION_PATH = "m/44'/60'/0'/0/0";

/** Default Ethereum Mainnet chain used to derive the address on first import. */
const ETH_MAINNET = {
  id: 'ethereum-mainnet',
  name: 'ETH',
  rpcUrl: 'https://ethereum-rpc.publicnode.com',
  chainId: 1,
  explorerUrl: 'https://etherscan.io'
};

/**
 * Derive the primary address (first derivation path) from a BIP39 seed phrase.
 */
export const deriveAddress = (seedPhrase: string): string => {
  const wallet = ethers.HDNodeWallet.fromPhrase(
    seedPhrase.trim(),
    undefined,
    DEFAULT_DERIVATION_PATH
  );
  return wallet.address;
};

export const SeedPhraseImport: React.FC = () => {
  const { setAddress, setChainConfig, setLoading, error, setError, setNetworkConfigured } = useWalletStore();

  const [seedPhrase, setSeedPhrase] = useState('');
  const [isProcessing, setIsProcessing] = useState(false);

  const handleImport = async () => {
    if (!seedPhrase.trim()) {
      setError('Seed phrase is required');
      return;
    }

    setIsProcessing(true);
    setError(null);
    setLoading(true);

    try {
      const validation = validateSeedPhrase(seedPhrase);

      if (!validation.isValid) {
        setError('Invalid seed phrase');
        setIsProcessing(false);
        setLoading(false);
        return;
      }

      // Derive the main address on Ethereum Mainnet immediately.
      const newAddress = deriveAddress(seedPhrase);

      // Persist the seed phrase locally so the send flow can derive the
      // signing key without ever asking the user for a private key.
      const storage = new StorageService();
      await storage.initialize();
      await storage.saveWallet({
        address: newAddress,
        seedPhrase: seedPhrase.trim(),
        chainConfig: ETH_MAINNET
      });

      setAddress(newAddress);
      // Default network context; the user still needs to confirm/set chain settings
      // before the balance is shown and sending is enabled.
      setChainConfig(ETH_MAINNET);
      setNetworkConfigured(false);
      setError(null);
      setIsProcessing(false);
      setLoading(false);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to import wallet');
      setIsProcessing(false);
      setLoading(false);
    }
  };

  return (
    <div className="max-w-2xl mx-auto">
      <h1 className="text-2xl font-bold text-gray-800 mb-6">MASH Wallet</h1>

      <div className="bg-white rounded-lg shadow-md border border-gray-200 p-6 mb-8">
        <div className="flex items-center gap-3">
          <svg className="w-5 h-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24" strokeWidth="2">
            <path d="M9 12l2 2 4-4m6 2a9 9 0 1 1-18 0 9 9 0 0 1 18 0z" strokeLinecap="round" strokeLinejoin="round" />
          </svg>
          <span className="text-sm font-medium text-gray-600">Secure wallet import — your seed phrase never leaves your device.</span>
        </div>
      </div>

      <h2 className="text-2xl font-bold text-gray-800 mb-6">Import Wallet</h2>

      <div className="mb-6">
        <label className="block text-gray-700 font-medium mb-2">
          Seed Phrase (12 or 24 words)
        </label>
        <input
          type="text"
          value={seedPhrase}
          onChange={(e) => setSeedPhrase(e.target.value)}
          className="w-full p-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
          placeholder="Enter your 12 or 24 word seed phrase"
        />
      </div>

      {error && (
        <div className="mb-4 text-red-500">
          <p className="font-medium">Error: {error}</p>
        </div>
      )}

      <button
        onClick={handleImport}
        className="w-full bg-blue-600 text-white py-2 rounded-md font-medium hover:bg-blue-700 transition-colors"
        disabled={isProcessing}
      >
        {isProcessing ? 'Importing...' : 'Import Wallet'}
      </button>

      {isProcessing && (
        <div className="mt-4 text-gray-500">
          <p>Please wait while we process your request...</p>
        </div>
      )}
    </div>
  );
};
