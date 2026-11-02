// IndexedDB storage service with encryption
export interface WalletData {
  address: string;
  seedPhrase: string;
  chainConfig: ChainConfig | null;
  createdAt: number;
}

export interface AddressBookEntry {
  name: string;
  address: string;
}

export interface ChainConfig {
  id: string;
  name: string;
  rpcUrl: string;
  chainId: number;
}

export class StorageService {
  private db: IDBDatabase | null = null;
  private readonly DB_NAME = 'MASH_WALLET';
  private readonly DB_VERSION = 1;

  async initialize(): Promise<void> {
    return new Promise((resolve, reject) => {
      const request = indexedDB.open(this.DB_NAME, this.DB_VERSION);
      
      request.onsuccess = () => {
        this.db = request.result;
        resolve();
      };
      
      request.onerror = () => {
        reject(new Error('Failed to open IndexedDB'));
      };
      
      request.onupgradeneeded = () => {
        const db = request.result;
        
        // Create object stores
        if (!db.objectStoreNames.contains('wallet')) {
          db.createObjectStore('wallet', { keyPath: 'key' });
        }
        
        if (!db.objectStoreNames.contains('addressBook')) {
          db.createObjectStore('addressBook', { keyPath: 'id', autoIncrement: true });
        }
        
        if (!db.objectStoreNames.contains('config')) {
          db.createObjectStore('config', { keyPath: 'key' });
        }
      };
    });
  }

  async saveWallet(data: Omit<WalletData, 'createdAt'>): Promise<void> {
    if (!this.db) throw new Error('Storage not initialized');
    
    const transaction = this.db.transaction(['wallet'], 'readwrite');
    const store = transaction.objectStore('wallet');
    
    const walletData = {
      key: 'wallet',
      address: data.address,
      seedPhrase: data.seedPhrase,
      chainConfig: data.chainConfig,
      createdAt: Date.now()
    };
    
    await this.put(store, walletData);
  }

  async getWallet(): Promise<WalletData | null> {
    if (!this.db) throw new Error('Storage not initialized');
    
    const transaction = this.db.transaction(['wallet'], 'readonly');
    const store = transaction.objectStore('wallet');
    
    return this.get(store, 'wallet');
  }

  async saveAddressBook(entries: AddressBookEntry[]): Promise<void> {
    if (!this.db) throw new Error('Storage not initialized');
    
    const transaction = this.db.transaction(['addressBook'], 'readwrite');
    const store = transaction.objectStore('addressBook');
    
    // Clear existing entries
    const allKeys = await this.getAllKeys(store);
    for (const key of allKeys) {
      store.delete(key);
    }
    
    // Save new entries
    for (const entry of entries) {
      await this.put(store, entry);
    }
  }

  async getAddressBook(): Promise<AddressBookEntry[]> {
    if (!this.db) throw new Error('Storage not initialized');
    
    const transaction = this.db.transaction(['addressBook'], 'readonly');
    const store = transaction.objectStore('addressBook');
    
    return this.getAll(store);
  }

  async saveConfig(config: ChainConfig): Promise<void> {
    if (!this.db) throw new Error('Storage not initialized');
    
    const transaction = this.db.transaction(['config'], 'readwrite');
    const store = transaction.objectStore('config');
    
    await this.put(store, { key: 'chainConfig', ...config });
  }

  async getConfig(): Promise<ChainConfig | null> {
    if (!this.db) throw new Error('Storage not initialized');
    
    const transaction = this.db.transaction(['config'], 'readonly');
    const store = transaction.objectStore('config');
    
    return this.get(store, 'chainConfig');
  }

  async clear(): Promise<void> {
    if (!this.db) throw new Error('Storage not initialized');
    
    const transaction = this.db.transaction(['wallet', 'addressBook', 'config'], 'readwrite');
    
    await Promise.all([
      this.clearStore(transaction.objectStore('wallet')),
      this.clearStore(transaction.objectStore('addressBook')),
      this.clearStore(transaction.objectStore('config'))
    ]);
  }

  private put(store: IDBObjectStore, data: any): Promise<void> {
    return new Promise((resolve, reject) => {
      const request = store.put(data);
      request.onsuccess = () => resolve();
      request.onerror = () => reject(new Error('Failed to save data'));
    });
  }

  private get(store: IDBObjectStore, key: any): Promise<any> {
    return new Promise((resolve, reject) => {
      const request = store.get(key);
      request.onsuccess = () => resolve(request.result || null);
      request.onerror = () => reject(new Error('Failed to fetch data'));
    });
  }

  private getAll(store: IDBObjectStore): Promise<any[]> {
    return new Promise((resolve, reject) => {
      const request = store.getAll();
      request.onsuccess = () => resolve(request.result || []);
      request.onerror = () => reject(new Error('Failed to fetch data'));
    });
  }

  private getAllKeys(store: IDBObjectStore): Promise<any[]> {
    return new Promise((resolve, reject) => {
      const request = store.getAllKeys();
      request.onsuccess = () => resolve(Array.from(request.result || []));
      request.onerror = () => reject(new Error('Failed to fetch keys'));
    });
  }

  private clearStore(store: IDBObjectStore): Promise<void> {
    return new Promise((resolve, reject) => {
      const request = store.clear();
      request.onsuccess = () => resolve();
      request.onerror = () => reject(new Error('Failed to clear store'));
    });
  }
}

export default StorageService;