import React, { useEffect, useState } from 'react';
import { ethers } from 'ethers';
import { useWalletStore } from '../../context/WalletContext';
import { StorageService } from '../../services/storage';

const DEFAULT_DERIVATION_PATH = "m/44'/60'/0'/0/0";

interface SendFormProps {
  onBack?: () => void;
}

export const SendForm: React.FC<SendFormProps> = ({ onBack }) => {
  const { chainConfig, addressBook, setBalance } = useWalletStore();

  const [to, setTo] = useState('');
  const [amount, setAmount] = useState('');
  const [seedPhrase, setSeedPhrase] = useState<string | null>(null);
  const [status, setStatus] = useState<string | null>(null);
  const [isSending, setIsSending] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const coinAlias = chainConfig?.name || 'coin';

  useEffect(() => {
    let active = true;
    (async () => {
      try {
        const storage = new StorageService();
        await storage.initialize();
        const wallet = await storage.getWallet();
        if (active) setSeedPhrase(wallet?.seedPhrase ?? null);
      } catch (err) {
        console.error('Failed to load wallet for signing:', err);
      }
    })();
    return () => {
      active = false;
    };
  }, []);

  const handleSend = async () => {
    if (!chainConfig) {
      setError('Chain configuration not set');
      return;
    }

    if (!to || !amount) {
      setError('Destination address and amount are required');
      return;
    }

    if (!to.startsWith('0x') || to.length !== 42) {
      setError('Invalid destination address');
      return;
    }

    if (!seedPhrase) {
      setError('Signing key unavailable — please re-import your wallet');
      return;
    }

    setIsSending(true);
    setError(null);
    setStatus(null);

    try {
      // Derive the signing key from the stored seed phrase — never ask the user.
      const provider = new ethers.JsonRpcProvider(chainConfig.rpcUrl);
      const signer = ethers.HDNodeWallet.fromPhrase(
        seedPhrase,
        undefined,
        DEFAULT_DERIVATION_PATH
      ).connect(provider);

      const tx = await signer.sendTransaction({
        to,
        value: ethers.parseEther(amount)
      });

      setStatus(`Sent! Transaction hash: ${tx.hash}`);
      setAmount('');
      setTo('');
      setBalance(null);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Transaction failed');
    } finally {
      setIsSending(false);
    }
  };

  return (
    <div className="max-w-2xl mx-auto">
      <h1 className="text-3xl font-bold text-gray-900 mb-8">Send {coinAlias}</h1>

      <div className="space-y-6">
        <div>
          <label className="block text-sm font-medium text-gray-500 mb-1">
            Amount ({coinAlias})
          </label>
          <input
            type="text"
            inputMode="decimal"
            value={amount}
            onChange={(e) => setAmount(e.target.value)}
            className="w-full p-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
            placeholder="0.0"
          />
        </div>

        <div>
          <label className="block text-sm font-medium text-gray-500 mb-1">
            Destination address
          </label>
          <input
            type="text"
            value={to}
            onChange={(e) => setTo(e.target.value)}
            className="w-full p-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 font-mono text-sm"
            placeholder="0x..."
          />
        </div>
      </div>

      {/* Address book */}
      <div className="mt-8">
        <h2 className="text-base font-medium text-gray-900 mb-3">Address book</h2>

        {addressBook.length === 0 ? (
          <p className="text-gray-500">No saved contacts yet.</p>
        ) : (
          <div className="space-y-3">
            {addressBook.map((entry) => (
              <div key={entry.address} className="rounded-2xl bg-blue-50 border border-blue-100 p-4">
                <p className="text-lg font-medium text-gray-900">{entry.name}</p>
                <p className="mt-1 font-mono text-sm text-gray-700 break-all">{entry.address}</p>
                <button
                  type="button"
                  onClick={() => setTo(entry.address)}
                  className="mt-3 text-sm font-medium text-blue-700 hover:text-blue-800"
                >
                  Use this address
                </button>
              </div>
            ))}
          </div>
        )}
      </div>

      {error && (
        <div className="mt-6 text-red-500">
          <p className="font-medium">Error: {error}</p>
        </div>
      )}

      {status && (
        <div className="mt-6 p-4 bg-green-50 border border-green-200 rounded-lg">
          <p className="text-sm text-green-700 break-all">{status}</p>
        </div>
      )}

      <div className="mt-8 space-y-3">
        <button
          onClick={handleSend}
          disabled={isSending}
          className="px-8 py-2.5 bg-blue-700 text-white rounded-full font-medium hover:bg-blue-800 disabled:opacity-50 transition-colors"
        >
          {isSending ? 'Sending...' : 'Send'}
        </button>

        {onBack && (
          <div>
            <button
              onClick={onBack}
              className="px-8 py-2.5 text-blue-700 bg-white border-2 border-blue-700 rounded-full font-medium hover:bg-blue-50 transition-colors"
            >
              Cancel
            </button>
          </div>
        )}
      </div>
    </div>
  );
};
