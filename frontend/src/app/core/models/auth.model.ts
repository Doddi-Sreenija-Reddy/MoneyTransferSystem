export interface LoginRequest {
  username: string;
  password: string;
}

export interface AuthUser {
  accountId: string;
  holderName: string;
  username: string;
}

// The backend login endpoint returns AccountResponse,
// which we treat as the login response shape.
export interface LoginResponse {
  id: string;
  username: string;
  holderName: string;
  balance: number;
  status: string;
}