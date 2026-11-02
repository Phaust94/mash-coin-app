import React, { useState } from 'react';
import { useWalletStore } from '../../context/WalletContext';
import { StorageService } from '../../services/storage';

interface ChainConfigProps {
  onBack: () => void;
}

export const ChainConfig: React.FC<ChainConfigProps> = ({ onBack }) => {
  const { chainConfig, setChainConfig, setNetworkConfigured, setBalance } = useWalletStore();

  const [rpcUrl, setRpcUrl] = useState(chainConfig?.rpcUrl ?? '');
  const [chainAlias, setChainAlias] = useState(chainConfig?.name ?? '');
  const [chainId, setChainId] = useState(chainConfig ? String(chainConfig.chainId) : '');
  const [explorerUrl, setExplorerUrl] = useState(chainConfig?.explorerUrl ?? '');
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState(false);

  const handleSave = async () => {
    if (!rpcUrl || !chainAlias || !chainId) {
      setError('JSON-RPC URL, coin alias and chain ID are required');
      return;
    }

    const chainIdNum = parseInt(chainId, 10);
    if (isNaN(chainIdNum) || chainIdNum <= 0) {
      setError('Chain ID must be a positive number');
      return;
    }

    try {
      const config = {
        id: chainAlias.toLowerCase(),
        name: chainAlias,
        rpcUrl,
        chainId: chainIdNum,
        explorerUrl: explorerUrl || undefined
      };

      const storage = new StorageService();
      await storage.initialize();
      await storage.saveConfig(config);

      setChainConfig(config);
      setNetworkConfigured(true);
      setBalance(null);
      setSuccess(true);
      setError(null);

      setTimeout(() => setSuccess(false), 2000);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to save configuration');
    }
  };

  return (
    <div className="max-w-2xl mx-auto">
      <h1 className="text-3xl font-bold text-gray-900 mb-8">Chain settings</h1>

      <div className="space-y-6">
        <div>
          <label className="block text-sm font-medium text-gray-500 mb-1">
            JSON-RPC URL
          </label>
          <input
            type="url"
            value={rpcUrl}
            onChange={(e) => setRpcUrl(e.target.value)}
            className="w-full p-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
            placeholder="https://your-rpc-endpoint.com"
          />
        </div>

        <div>
          <label className="block text-sm font-medium text-gray-500 mb-1">
            Coin alias
          </label>
          <input
            type="text"
            value={chainAlias}
            onChange={(e) => setChainAlias(e.target.value)}
            className="w-full p-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
            placeholder="MASH"
          />
        </div>

        <div>
          <label className="block text-sm font-medium text-gray-500 mb-1">
            Chain ID
          </label>
          <input
            type="number"
            value={chainId}
            onChange={(e) => setChainId(e.target.value)}
            className="w-full p-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
            placeholder="27744"
          />
        </div>

        <div>
          <label className="block text-sm font-medium text-gray-500 mb-1">
            Explorer URL (optional)
          </label>
          <input
            type="url"
            value={explorerUrl}
            onChange={(e) => setExplorerUrl(e.target.value)}
            className="w-full p-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
            placeholder="https://family-coin-explorer.frakt.al"
          />
        </div>
      </div>

      {error && (
        <div className="mt-4 text-red-500">
          <p className="font-medium">Error: {error}</p>
        </div>
      )}

      {success && (
        <div className="mt-4 text-green-600">
          <p className="font-medium">Configuration saved successfully!</p>
        </div>
      )}

      <div className="mt-8 space-y-3">
        <button
          onClick={handleSave}
          className="px-8 py-2.5 bg-blue-700 text-white rounded-full font-medium hover:bg-blue-800 transition-colors"
        >
          Save
        </button>

        <div>
          <button
            onClick={onBack}
            className="px-8 py-2.5 text-blue-700 bg-white border-2 border-blue-700 rounded-full font-medium hover:bg-blue-50 transition-colors"
          >
            Back
          </button>
        </div>
      </div>
    </div>
  );
};
