import { ethers } from 'ethers';
import { validateSeedPhrase } from './crypto';
import { StorageService } from './storage';
import { Web3Service } from './web3';

export interface WalletState {
  address: string | null;
  balance: bigint | null;
  chainConfig: ChainConfig | null;
  addressBook: AddressBookEntry[];
  isLoading: boolean;
  error: string | null;
}

export interface ChainConfig {
  id: string;
  name: string;
  rpcUrl: string;
  chainId: number;
}

export interface AddressBookEntry {
  name: string;
  address: string;
}

export class WalletService {
  private storage: StorageService;
  private web3: Web3Service;

  constructor() {
    this.storage = new StorageService();
    this.web3 = new Web3Service();
  }

  async initialize(): Promise<void> {
    await this.storage.initialize();
  }

  async importWallet(seedPhrase: string, chainConfig: ChainConfig): Promise<string> {
    // Validate seed phrase
    const validation = validateSeedPhrase(seedPhrase);
    if (!validation.isValid) {
      throw new Error('Invalid seed phrase');
    }

    // Derive address (simplified - in real implementation, use BIP44 derivation)
    const address = this.deriveAddress(seedPhrase);
    
    // Save wallet
    await this.storage.saveWallet({
      address,
      seedPhrase,
      chainConfig
    });
    
    // Initialize web3 provider
    await this.web3.initialize(chainConfig);
    
    return address;
  }

  async getBalance(address: string): Promise<bigint> {
    const balance = await this.web3.getBalance(address);
    return ethers.parseEther(balance.formatted);
  }

  async sendTransaction(
    to: string,
    amount: string,
    privateKey: string
  ): Promise<string> {
    const result = await this.web3.sendTransaction(to, amount, privateKey);
    return result.hash;
  }

  async saveAddressBook(entries: AddressBookEntry[]): Promise<void> {
    await this.storage.saveAddressBook(entries);
  }

  async getAddressBook(): Promise<AddressBookEntry[]> {
    return await this.storage.getAddressBook();
  }

  async saveChainConfig(config: ChainConfig): Promise<void> {
    await this.storage.saveConfig(config);
  }

  async getChainConfig(): Promise<ChainConfig | null> {
    return await this.storage.getConfig();
  }

  async getStoredWallet(): Promise<WalletState> {
    const wallet = await this.storage.getWallet();
    const addressBook = await this.storage.getAddressBook();
    const config = await this.storage.getConfig();

    return {
      address: wallet?.address || null,
      balance: null,
      chainConfig: config,
      addressBook,
      isLoading: false,
      error: null
    };
  }

  private deriveAddress(seedPhrase: string): string {
    // Simplified address derivation - in real implementation, use BIP44
    const hash = ethers.sha256(ethers.toUtf8Bytes(seedPhrase));
    return ethers.computeAddress(hash);
  }
}

export default WalletService;