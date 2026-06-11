import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { TransferRequest, TransferResponse } from '../models/transfer.model';

@Injectable({
  providedIn: 'root'
})
export class TransferService {

  constructor(private http: HttpClient) {}

  transfer(request: TransferRequest): Observable<TransferResponse> {
    return this.http.post<TransferResponse>(`${environment.apiUrl}/transfers`, request);
  }

  generateIdempotencyKey(): string {
    return `${Date.now()}-${Math.random().toString(36).substring(2, 15)}`;
  }
}