import { ethers } from 'ethers';

/**
 * Fetch the native balance for an address from an arbitrary JSON-RPC endpoint.
 *
 * A fresh provider is created per call so a refresh always performs a new
 * request instead of reusing a cached provider.
 */
export const fetchOnChainBalance = async (
  rpcUrl: string,
  chainId: number,
  address: string
): Promise<bigint> => {
  const provider = new ethers.JsonRpcProvider(rpcUrl, chainId, { staticNetwork: true });
  return provider.getBalance(address, 'latest');
};
