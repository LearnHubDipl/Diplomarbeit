import { Injectable } from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {map, Observable, of} from 'rxjs';
import {QuestionPoolEntry} from './stats-home/stats-home.component';

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

  private mockData: QuestionPoolEntry[] = [
    { answeredAt: '2025-07-08T09:10:00', lastAnsweredCorrectly: true, correctCount: 3, questionId: 1, questionPoolId: 1 },
    { answeredAt: '2025-07-07T14:30:00', lastAnsweredCorrectly: false, correctCount: 1, questionId: 2, questionPoolId: 2 },
    { answeredAt: '2025-07-06T10:20:00', lastAnsweredCorrectly: true, correctCount: 2, questionId: 3, questionPoolId: 3 },
    { answeredAt: '2025-07-09T08:50:00', lastAnsweredCorrectly: false, correctCount: 0, questionId: 4, questionPoolId: 4 },
    { answeredAt: '2025-07-05T13:20:00', lastAnsweredCorrectly: true, correctCount: 4, questionId: 5, questionPoolId: 5 },
  ];

  getMockedEntries(): Observable<QuestionPoolEntry[]> {
    return of(this.mockData);
  }
}
