import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { RewardResponse } from '../models/reward.model';

@Injectable({
  providedIn: 'root'
})
export class RewardService {

  constructor(private http: HttpClient) {}

  /**
   * Fetches total reward points and full reward history for the given account.
   */
  getRewards(accountId: string): Observable<RewardResponse> {
    return this.http.get<RewardResponse>(`${environment.apiUrl}/rewards/${accountId}`);
  }
}
