/// <reference types="vite/client" />

type PlaidAccountMetadata = {
  id: string;
  name?: string;
  mask?: string;
  type?: string;
  subtype?: string;
};

type PlaidSuccessMetadata = {
  institution?: {
    name?: string;
  };
  accounts: PlaidAccountMetadata[];
};

type PlaidHandler = {
  open: () => void;
  exit: () => void;
  destroy: () => void;
};

type PlaidCreateConfig = {
  token: string;
  onSuccess: (publicToken: string, metadata: PlaidSuccessMetadata) => void;
  onExit?: () => void;
};

interface Window {
  Plaid?: {
    create: (config: PlaidCreateConfig) => PlaidHandler;
  };
}
