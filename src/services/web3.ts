import { ethers } from 'ethers';

export interface Balance {
  value: string;
  formatted: string;
  unit: 'ETH';
}

export interface ChainConfig {
  id: string;
  name: string;
  rpcUrl: string;
  chainId: number;
}

export interface TransactionHash {
  hash: string;
  blockExplorerUrl?: string;
}

export class Web3Service {
  private provider: ethers.BrowserProvider | null = null;
  private config: ChainConfig | null = null;

  async initialize(config: ChainConfig): Promise<void> {
    try {
      this.provider = new ethers.BrowserProvider((window as any).ethereum);
      this.config = config;
    } catch (error) {
      throw new Error('Failed to initialize Web3 provider');
    }
  }

  async getBalance(address: string): Promise<Balance> {
    if (!this.provider) {
      throw new Error('Web3 provider not initialized');
    }

    try {
      const balance = await this.provider.getBalance(address);
      const formattedBalance = ethers.formatEther(balance);
      
      return {
        value: balance.toString(),
        formatted: formattedBalance,
        unit: 'ETH'
      };
    } catch (error) {
      throw new Error('Failed to fetch balance');
    }
  }

  async sendTransaction(
    to: string,
    amount: string,
    _privateKey: string
  ): Promise<TransactionHash> {
    if (!this.provider) {
      throw new Error('Web3 provider not initialized');
    }

    try {
      // Create transaction
      const value = ethers.parseEther(amount);
      
      // Get signer (in real implementation, we'd derive from seed phrase)
      const signer = await this.provider.getSigner();
      
      // Send transaction
      const tx = await signer.sendTransaction({
        to,
        value,
        gasLimit: 21000,
        maxFeePerGas: ethers.parseUnits('1', 'gwei'),
        maxPriorityFeePerGas: ethers.parseUnits('0.5', 'gwei'),
        chainId: this.config?.chainId || 1
      });
      
      // Wait for transaction
      await tx.wait();
      
      return {
        hash: tx.hash,
        blockExplorerUrl: this.config?.id === 'ethereum' 
          ? `https://etherscan.io/tx/${tx.hash}`
          : undefined
      };
    } catch (error) {
      throw new Error('Failed to send transaction');
    }
  }

  getCurrentProvider(): ethers.BrowserProvider | null {
    return this.provider;
  }
}

export default Web3Service;