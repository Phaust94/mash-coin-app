import { describe, test, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { WalletView } from './WalletView';
import { useWalletStore } from '../../context/WalletContext';
import { fetchOnChainBalance } from '../../services/balance';

vi.mock('../../context/WalletContext', () => ({
  useWalletStore: vi.fn()
}));

vi.mock('../../services/balance', () => ({
  fetchOnChainBalance: vi.fn()
}));

type Store = ReturnType<typeof useWalletStore>;

const chainConfig = {
  id: 'ethereum-mainnet',
  name: 'ETH',
  rpcUrl: 'https://ethereum-rpc.publicnode.com',
  chainId: 1,
  explorerUrl: 'https://etherscan.io'
};

const mockStore = (setBalance: ReturnType<typeof vi.fn>) => {
  vi.mocked(useWalletStore).mockReturnValue({
    address: '0x0e0f1af639d6f1f08acb27260b0a5e26869535a5',
    chainConfig,
    balance: null,
    networkConfigured: true,
    setBalance,
    clearWallet: vi.fn()
  } as unknown as Store);
};

describe('WalletView refresh', () => {
  const getBalance = vi.mocked(fetchOnChainBalance);

  beforeEach(() => {
    getBalance.mockReset();
  });

  test('re-fetches the on-chain balance when Refresh is clicked', async () => {
    const setBalance = vi.fn();
    getBalance
      .mockResolvedValueOnce(1_000_000_000_000_000_000n)
      .mockResolvedValueOnce(2_000_000_000_000_000_000n);

    mockStore(setBalance);
    render(<WalletView onNavigate={vi.fn()} />);

    await waitFor(() => expect(setBalance).toHaveBeenCalledTimes(1));

    fireEvent.click(screen.getByRole('button', { name: 'Refresh' }));

    await waitFor(() => expect(setBalance).toHaveBeenCalledTimes(2));
    expect(getBalance).toHaveBeenCalledTimes(2);
    expect(getBalance).toHaveBeenLastCalledWith(
      'https://ethereum-rpc.publicnode.com',
      1,
      '0x0e0f1af639d6f1f08acb27260b0a5e26869535a5'
    );
  });

  test('formats the balance with at most one decimal place', async () => {
    const setBalance = vi.fn();
    getBalance.mockResolvedValueOnce(1_234_567_890_000_000_000n);

    mockStore(setBalance);
    render(<WalletView onNavigate={vi.fn()} />);

    await waitFor(() => expect(setBalance).toHaveBeenCalledTimes(1));

    const balance = setBalance.mock.calls[0][0] as { formatted: string; unit: string };
    expect(balance.formatted).toMatch(/^1[.,]2$/);
    expect(balance.unit).toBe('ETH');
  });

  test('shows an error instead of failing silently', async () => {
    const setBalance = vi.fn();
    getBalance.mockRejectedValueOnce(new Error('RPC down'));

    mockStore(setBalance);
    render(<WalletView onNavigate={vi.fn()} />);

    expect(
      await screen.findByText('Could not fetch balance. Check the JSON-RPC URL in Chain settings.')
    ).toBeInTheDocument();
  });
});
