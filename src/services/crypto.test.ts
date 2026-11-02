import { describe, it, expect } from 'vitest';
import { validateSeedPhrase } from '../services/crypto';

describe('Crypto Service', () => {
  describe('validateSeedPhrase', () => {
    it('should validate 12-word seed phrase', () => {
      const phrase = 'abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon about';
      const result = validateSeedPhrase(phrase);
      expect(result.isValid).toBe(true);
      expect(result.words).toHaveLength(12);
    });

    it('should validate 24-word seed phrase', () => {
      const phrase = Array(24).fill('abandon').join(' ');
      const result = validateSeedPhrase(phrase);
      expect(result.isValid).toBe(true);
      expect(result.words).toHaveLength(24);
    });

    it('should reject invalid seed phrase with wrong word count', () => {
      const phrase = 'abandon abandon';
      const result = validateSeedPhrase(phrase);
      expect(result.isValid).toBe(false);
    });

    it('should reject empty seed phrase', () => {
      const phrase = '';
      const result = validateSeedPhrase(phrase);
      expect(result.isValid).toBe(false);
    });

    it('should handle whitespace-only input', () => {
      const phrase = '   ';
      const result = validateSeedPhrase(phrase);
      expect(result.isValid).toBe(false);
    });

    it('should handle single-word input', () => {
      const phrase = 'abandon';
      const result = validateSeedPhrase(phrase);
      expect(result.isValid).toBe(false);
    });
  });
});