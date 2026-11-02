import { create } from 'zustand';
import { persist } from 'zustand/middleware';

export interface ChainConfig {
  id: string;
  name: string;
  rpcUrl: string;
  chainId: number;
  explorerUrl?: string;
}

export interface AddressBookEntry {
  name: string;
  address: string;
}

export interface Balance {
  value: string;
  formatted: string;
  unit: string;
}

export interface WalletState {
  address: string | null;
  balance: Balance | null;
  chainConfig: ChainConfig | null;
  addressBook: AddressBookEntry[];
  isLoading: boolean;
  error: string | null;
  /** True once the user has explicitly saved chain/network settings. */
  networkConfigured: boolean;

  // Actions
  setAddress: (address: string) => void;
  setBalance: (balance: Balance | null) => void;
  setChainConfig: (config: ChainConfig) => void;
  setAddressBook: (addressBook: AddressBookEntry[]) => void;
  addAddressBookEntry: (entry: AddressBookEntry) => void;
  removeAddressBookEntry: (address: string) => void;
  setLoading: (loading: boolean) => void;
  setError: (error: string | null) => void;
  setNetworkConfigured: (configured: boolean) => void;
  clearWallet: () => void;
}

const initialState = {
  address: null,
  balance: null,
  chainConfig: null,
  addressBook: [],
  isLoading: false,
  error: null,
  networkConfigured: false
};

export const useWalletStore = create<WalletState>()(
  persist(
    (set) => ({
      ...initialState,

      setAddress: (address: string) => set({ address }),
      setBalance: (balance: Balance | null) => set({ balance }),
      setChainConfig: (config: ChainConfig) => set({ chainConfig: config }),
      setAddressBook: (addressBook: AddressBookEntry[]) => set({ addressBook }),
      addAddressBookEntry: (entry: AddressBookEntry) =>
        set((state) => ({
          addressBook: [...state.addressBook, entry]
        })),
      removeAddressBookEntry: (address: string) =>
        set((state) => ({
          addressBook: state.addressBook.filter((e) => e.address !== address)
        })),
      setLoading: (isLoading: boolean) => set({ isLoading }),
      setError: (error: string | null) => set({ error }),
      setNetworkConfigured: (networkConfigured: boolean) => set({ networkConfigured }),
      clearWallet: () => set(initialState)
    }),
    {
      name: 'mash-wallet-store',
      version: 1,
      // The original default RPC (eth.llamarpc.com) stopped responding, so
      // migrate any saved config that still points at it.
      migrate: (persistedState: unknown) => {
        const state = persistedState as WalletState;

        if (state?.chainConfig?.rpcUrl?.includes('llamarpc.com')) {
          state.chainConfig = {
            ...state.chainConfig,
            rpcUrl: 'https://ethereum-rpc.publicnode.com'
          };
        }

        return state;
      },
      partialize: (state) => ({
        address: state.address,
        chainConfig: state.chainConfig,
        addressBook: state.addressBook,
        networkConfigured: state.networkConfigured
      })
    }
  )
);
