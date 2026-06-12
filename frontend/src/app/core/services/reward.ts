import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { RedeemRequest, RedeemResponse, RewardResponse } from '../models/reward.model';

@Injectable({
  providedIn: 'root'
})
export class RewardService {

  constructor(private http: HttpClient) {}

  /** Fetches total reward points and full reward history for the given account. */
  getRewards(accountId: string): Observable<RewardResponse> {
    return this.http.get<RewardResponse>(`${environment.apiUrl}/rewards/${accountId}`);
  }

  /** Redeems points for cashback (1 point = ₹1, minimum 10 points). */
  redeemPoints(accountId: string, request: RedeemRequest): Observable<RedeemResponse> {
    return this.http.post<RedeemResponse>(
      `${environment.apiUrl}/rewards/${accountId}/redeem`,
      request
    );
  }
}
