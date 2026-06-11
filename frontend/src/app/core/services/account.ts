import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AccountResponse, CreateAccountRequest } from '../models/account.model';
import { TransactionLogResponse } from '../models/transaction.model';

@Injectable({
  providedIn: 'root'
})
export class AccountService {

  constructor(private http: HttpClient) {}

  createAccount(request: CreateAccountRequest): Observable<AccountResponse> {
    return this.http.post<AccountResponse>(`${environment.apiUrl}/accounts`, request);
  }

  getAccount(accountId: string): Observable<AccountResponse> {
    return this.http.get<AccountResponse>(`${environment.apiUrl}/accounts/${accountId}`);
  }

  getBalance(accountId: string): Observable<{ balance: number }> {
    return this.http.get<{ balance: number }>(`${environment.apiUrl}/accounts/${accountId}/balance`);
  }

  getTransactions(accountId: string): Observable<TransactionLogResponse[]> {
    return this.http.get<TransactionLogResponse[]>(`${environment.apiUrl}/accounts/${accountId}/transactions`);
  }
}
