import { describe, test, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import { WalletView } from './WalletView';
import { useWalletStore } from '../../context/WalletContext';

vi.mock('../../context/WalletContext', () => ({
  useWalletStore: vi.fn()
}));

type Store = ReturnType<typeof useWalletStore>;

const baseStore = {
  address: '0x0e0f1af639d6f1f08acb27260b0a5e26869535a5',
  chainConfig: null,
  balance: null,
  networkConfigured: false,
  setBalance: vi.fn(),
  clearWallet: vi.fn()
};

const mockStore = (overrides: Record<string, unknown> = {}) => {
  vi.mocked(useWalletStore).mockReturnValue({
    ...baseStore,
    ...overrides
  } as unknown as Store);
};

describe('WalletView Component', () => {
  test('should show wallet header', () => {
    mockStore();

    render(<WalletView onNavigate={vi.fn()} />);

    expect(screen.getByText('Wallet')).toBeInTheDocument();
  });

  test('should show address', () => {
    mockStore();

    render(<WalletView onNavigate={vi.fn()} />);

    expect(screen.getByText('0x0e0f1af639d6f1f08acb27260b0a5e26869535a5')).toBeInTheDocument();
  });

  test('should show dash when no chain config', () => {
    mockStore();

    render(<WalletView onNavigate={vi.fn()} />);

    expect(screen.getByText('---')).toBeInTheDocument();
  });

  test('should disable send coin until network is configured', () => {
    mockStore();

    render(<WalletView onNavigate={vi.fn()} />);

    expect(screen.getByRole('button', { name: 'Send coin' })).toBeDisabled();
  });

  test('should disable history when no explorer URL is configured', () => {
    mockStore();

    render(<WalletView onNavigate={vi.fn()} />);

    expect(screen.getByRole('button', { name: 'History' })).toBeDisabled();
  });

  test('should enable history when an explorer URL is configured', () => {
    mockStore({
      chainConfig: {
        id: 'ethereum-mainnet',
        name: 'ETH',
        rpcUrl: 'https://eth.llamarpc.com',
        chainId: 1,
        explorerUrl: 'https://etherscan.io'
      }
    });

    render(<WalletView onNavigate={vi.fn()} />);

    expect(screen.getByRole('button', { name: 'History' })).toBeEnabled();
  });
});
