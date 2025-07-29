import { Injectable } from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {map, Observable} from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class StatsService {

  constructor(private http: HttpClient) {}
  private apiUrl = 'http://localhost:8080/streak';
  getStreak(userId: number): Observable<number> {
    return this.http.get<{ streak: number }>(`${this.apiUrl}/user/${userId}`)
      .pipe(map(response => response.streak));
  }
}
