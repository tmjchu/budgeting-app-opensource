import { useCallback, useState } from 'react';
import { api } from '../lib/api';

let plaidScriptPromise: Promise<void> | null = null;

function loadPlaidScript(): Promise<void> {
  if (window.Plaid) {
    return Promise.resolve();
  }

  if (!plaidScriptPromise) {
    plaidScriptPromise = new Promise((resolve, reject) => {
      const script = document.createElement('script');
      script.src = 'https://cdn.plaid.com/link/v2/stable/link-initialize.js';
      script.async = true;
      script.onload = () => resolve();
      script.onerror = () => reject(new Error('Unable to load Plaid Link.'));
      document.body.appendChild(script);
    });
  }

  return plaidScriptPromise;
}

export function usePlaidLink(onConnected: () => Promise<void> | void) {
  const [isConnecting, setIsConnecting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const connect = useCallback(async () => {
    setIsConnecting(true);
    setError(null);
    try {
      const [{ linkToken }] = await Promise.all([api.createLinkToken(), loadPlaidScript()]);
      if (!window.Plaid) {
        throw new Error('Plaid Link was not available after loading.');
      }
      const handler = window.Plaid.create({
        token: linkToken,
        onSuccess: async (publicToken, metadata) => {
          await api.exchangePublicToken(publicToken, metadata);
          await onConnected();
          setIsConnecting(false);
          handler.destroy();
        },
        onExit: () => setIsConnecting(false)
      });
      handler.open();
    } catch (caught) {
      setError(caught instanceof Error ? caught.message : 'Unable to connect account.');
      setIsConnecting(false);
    }
  }, [onConnected]);

  return { connect, isConnecting, error };
}
