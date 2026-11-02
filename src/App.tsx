import { useState } from 'react';
import './index.css';
import { SeedPhraseImport } from './components/wallet/SeedPhraseImport';
import { ChainConfig } from './components/wallet/ChainConfig';
import { WalletView, type WalletScreen } from './components/wallet/WalletView';
import { SendForm } from './components/wallet/SendForm';
import { AddressBook } from './components/wallet/AddressBook';
import { useWalletStore } from './context/WalletContext';

type Screen = WalletScreen;

function App() {
  const { address } = useWalletStore();
  const [screen, setScreen] = useState<Screen>('wallet');

  const goTo = (next: Screen) => {
    setScreen(next);
  };

  const renderScreen = () => {
    switch (screen) {
      case 'chain-settings':
        return <ChainConfig onBack={() => goTo('wallet')} />;
      case 'send':
        return <SendForm onBack={() => goTo('wallet')} />;
      case 'address-book':
        return <AddressBook onBack={() => goTo('wallet')} />;
      case 'history':
        return (
          <div className="max-w-2xl mx-auto">
            <h1 className="text-3xl font-bold text-gray-900 mb-8">History</h1>
            <p className="text-gray-500 mb-8">No transactions yet.</p>
            <button
              type="button"
              onClick={() => goTo('wallet')}
              className="px-8 py-2.5 text-blue-700 bg-white border-2 border-blue-700 rounded-full font-medium hover:bg-blue-50 transition-colors"
            >
              Back
            </button>
          </div>
        );
      case 'wallet':
      default:
        return <WalletView onNavigate={goTo} />;
    }
  };

  return (
    <div className="min-h-screen bg-[#f8fafc] text-[#1e293b]">
      <header className="bg-white border-b border-gray-200 shadow-sm py-4">
        <div className="max-w-7xl mx-auto px-4 flex items-center gap-3">
          <div className="w-10 h-10 rounded-xl bg-blue-600 flex items-center justify-center">
            <svg
              className="w-6 h-6 text-white"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              strokeWidth="2"
              strokeLinecap="round"
              strokeLinejoin="round"
            >
              <path d="M4 7.5A2.5 2.5 0 0 1 6.5 5H19a1 1 0 0 1 1 1v2.5" />
              <rect x="4" y="7.5" width="16" height="11.5" rx="2.5" />
              <path d="M16 13h2.5" />
            </svg>
          </div>
          <span className="text-xl font-bold text-gray-800">MASH Wallet</span>
        </div>
      </header>

      <main className="max-w-7xl mx-auto p-8">
        {address ? renderScreen() : <SeedPhraseImport />}
      </main>
    </div>
  );
}

export default App;
