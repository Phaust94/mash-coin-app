import { describe, test, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { ChainConfig } from './ChainConfig';

describe('ChainConfig Component', () => {
  test('should have chain ID input', () => {
    render(<ChainConfig onBack={() => {}} />);

    const chainIdInput = screen.getByPlaceholderText('27744');
    expect(chainIdInput).toBeInTheDocument();
  });

  test('should have all required inputs', () => {
    render(<ChainConfig onBack={() => {}} />);

    expect(screen.getByPlaceholderText('https://your-rpc-endpoint.com')).toBeInTheDocument();
    expect(screen.getByPlaceholderText('MASH')).toBeInTheDocument();
    expect(screen.getByPlaceholderText('27744')).toBeInTheDocument();
    expect(screen.getByPlaceholderText('https://family-coin-explorer.frakt.al')).toBeInTheDocument();
  });

  test('should have save and back buttons', () => {
    render(<ChainConfig onBack={() => {}} />);

    expect(screen.getByRole('button', { name: 'Save' })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Back' })).toBeInTheDocument();
  });
});
