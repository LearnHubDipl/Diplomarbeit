import {inject, Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {API_BASE_URL} from './globals';

export interface StreakDTO {
  streak: number;
}

@Injectable({ providedIn: 'root' })
export class StreakService {
  private http = inject(HttpClient);

  getStreak(userId: number): Observable<StreakDTO> {
    return this.http.get<StreakDTO>(`${API_BASE_URL}/streak/user/${userId}`);
  }

  updateStreak(userId: number): Observable<StreakDTO> {
    return this.http.post<StreakDTO>(`${API_BASE_URL}/streak/user/${userId}/update`, {});
  }
}
