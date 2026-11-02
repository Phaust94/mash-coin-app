import React, { useCallback, useEffect, useState } from 'react';
import { ethers } from 'ethers';
import { useWalletStore } from '../../context/WalletContext';
import { fetchOnChainBalance } from '../../services/balance';

export type WalletScreen = 'wallet' | 'send' | 'history' | 'chain-settings' | 'address-book';

interface WalletViewProps {
  onNavigate: (screen: WalletScreen) => void;
}

export const WalletView: React.FC<WalletViewProps> = ({ onNavigate }) => {
  const { address, chainConfig, balance, networkConfigured, setBalance, clearWallet } =
    useWalletStore();
  const [isRefreshing, setIsRefreshing] = useState(false);
  const [balanceError, setBalanceError] = useState<string | null>(null);
  const [lastUpdated, setLastUpdated] = useState<Date | null>(null);

  const fetchBalance = useCallback(async () => {
    if (!address || !chainConfig || !networkConfigured) return;

    setIsRefreshing(true);
    setBalanceError(null);

    try {
      // A fresh provider is created per refresh so we always hit the RPC
      // instead of reading any cached value.
      const rawBalance = await fetchOnChainBalance(
        chainConfig.rpcUrl,
        chainConfig.chainId,
        address
      );

      setBalance({
        value: rawBalance.toString(),
        formatted: Number(ethers.formatEther(rawBalance)).toLocaleString(undefined, {
          maximumFractionDigits: 4
        }),
        unit: chainConfig.name
      });
      setLastUpdated(new Date());
    } catch (err) {
      console.error('Failed to fetch balance:', err);
      setBalance(null);
      setBalanceError('Could not fetch balance. Check the JSON-RPC URL in Chain settings.');
    } finally {
      setIsRefreshing(false);
    }
  }, [address, chainConfig, networkConfigured, setBalance]);

  useEffect(() => {
    if (address && networkConfigured && chainConfig) {
      void fetchBalance();
    }
  }, [address, chainConfig, networkConfigured, fetchBalance]);

  const copyToClipboard = (addr: string) => {
    if (typeof navigator !== 'undefined' && navigator.clipboard) {
      navigator.clipboard.writeText(addr);
    }
  };

  const handleReImport = () => {
    clearWallet();
  };

  const balanceDisplay =
    networkConfigured && balance ? `${balance.formatted} ${balance.unit}` : '---';

  const historyUrl =
    chainConfig?.explorerUrl && address
      ? `${chainConfig.explorerUrl.replace(/\/+$/, '')}/address/${address}?tab=txs`
      : null;

  return (
    <div className="max-w-2xl mx-auto">
      <h1 className="text-3xl font-bold text-gray-900 mb-8">Wallet</h1>

      {/* Address */}
      <div className="mb-8">
        <p className="text-sm font-medium text-gray-500 mb-2">Address</p>
        <div className="flex items-center gap-2">
          <p className="font-mono text-base text-gray-900 break-all">{address}</p>
          <button
            type="button"
            onClick={() => copyToClipboard(address || '')}
            className="flex-shrink-0 p-1 text-gray-400 hover:text-gray-600"
            aria-label="Copy address"
          >
            <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24" strokeWidth="2">
              <path d="M8 16H6a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2h8a2 2 0 0 1 2 2v2m2 4v6a2 2 0 0 1-2 2h-6a2 2 0 0 1-2-2v-6a2 2 0 0 1 2-2h6a2 2 0 0 1 2 2z" strokeLinecap="round" strokeLinejoin="round" />
            </svg>
          </button>
        </div>
      </div>

      {/* Balance */}
      <div className="mb-8">
        <p className="text-sm font-medium text-gray-500 mb-2">Balance</p>
        <p className="text-5xl font-bold text-gray-900">{balanceDisplay}</p>
        {networkConfigured && lastUpdated && !balanceError && (
          <p className="mt-2 text-xs text-gray-400">
            Updated {lastUpdated.toLocaleTimeString()}
          </p>
        )}
        {networkConfigured && balanceError && (
          <p className="mt-2 text-sm text-red-600" role="alert">
            {balanceError}
          </p>
        )}
      </div>

      {/* Primary actions */}
      <div className="flex gap-3 mb-6">
        <button
          type="button"
          onClick={() => void fetchBalance()}
          disabled={isRefreshing || !networkConfigured}
          className="px-6 py-2.5 bg-blue-700 text-white text-base font-medium rounded-full hover:bg-blue-800 disabled:opacity-40 disabled:cursor-not-allowed transition-colors"
        >
          {isRefreshing ? 'Refreshing...' : 'Refresh'}
        </button>
        <button
          type="button"
          onClick={() => onNavigate('send')}
          disabled={!networkConfigured}
          className="px-6 py-2.5 bg-blue-700 text-white text-base font-medium rounded-full hover:bg-blue-800 disabled:opacity-40 disabled:cursor-not-allowed transition-colors"
        >
          Send coin
        </button>
      </div>

      {/* Secondary navigation */}
      <div className="space-y-3">
        <button
          type="button"
          onClick={() => {
            if (historyUrl) {
              window.open(historyUrl, '_blank', 'noopener,noreferrer');
            }
          }}
          disabled={!historyUrl}
          className="w-full px-4 py-3 text-base font-medium text-blue-700 bg-white border-2 border-blue-700 rounded-full hover:bg-blue-50 disabled:opacity-40 disabled:cursor-not-allowed transition-colors"
        >
          History
        </button>
        <button
          type="button"
          onClick={() => onNavigate('chain-settings')}
          className="w-full px-4 py-3 text-base font-medium text-blue-700 bg-white border-2 border-blue-700 rounded-full hover:bg-blue-50 transition-colors"
        >
          Chain settings
        </button>
        <button
          type="button"
          onClick={() => onNavigate('address-book')}
          className="w-full px-4 py-3 text-base font-medium text-blue-700 bg-white border-2 border-blue-700 rounded-full hover:bg-blue-50 transition-colors"
        >
          Address book
        </button>
        <button
          type="button"
          onClick={handleReImport}
          className="w-full px-4 py-3 text-base font-medium text-blue-700 bg-white border-2 border-blue-700 rounded-full hover:bg-blue-50 transition-colors"
        >
          Re-import wallet
        </button>
      </div>
    </div>
  );
};
