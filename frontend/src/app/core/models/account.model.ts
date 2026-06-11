export interface Account {
  id: string;
  holderName: string;
  balance: number;
  status: AccountStatus;
  lastUpdated: string;
}

export enum AccountStatus {
  ACTIVE = 'ACTIVE',
  LOCKED = 'LOCKED',
  CLOSED = 'CLOSED'
}

export interface AccountResponse {
  id: string;
  holderName: string;
  balance: number;
  status: AccountStatus;
}

export interface CreateAccountRequest {
  username: string;
  password: string;
  holderName: string;
}
