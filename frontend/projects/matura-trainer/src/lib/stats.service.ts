import { Injectable } from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {map, Observable, of} from 'rxjs';
import {QuestionPoolEntry} from './stats-home/stats-home.component';
import {TopicPool} from './stats-topics/stats-topics.component';

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

  private mockEntries: QuestionPoolEntry[] = [
    { questionId: 1, questionPoolId: 1, answeredAt: '2025-07-08T09:10:00', correctCount: 3, lastAnsweredCorrectly: true },
    { questionId: 2, questionPoolId: 2, answeredAt: '2025-07-07T14:30:00', correctCount: 1, lastAnsweredCorrectly: false },
    { questionId: 3, questionPoolId: 1, answeredAt: '2025-07-06T10:20:00', correctCount: 2, lastAnsweredCorrectly: true },
    { questionId: 4, questionPoolId: 3, answeredAt: '2025-07-09T08:50:00', correctCount: 0, lastAnsweredCorrectly: false },
    { questionId: 5, questionPoolId: 1, answeredAt: '2025-07-05T13:20:00', correctCount: 4, lastAnsweredCorrectly: true },
    { questionId: 5, questionPoolId: 1, answeredAt: null, correctCount: 4, lastAnsweredCorrectly: false }
  ];

  private mockTopicPools: TopicPool[] = [
    { id: 1, name: 'Themenpool 1' },
    { id: 2, name: 'Themenpool 2' },
    { id: 3, name: 'Themenpool 3' },
  ];

  getMockedEntries(): Observable<QuestionPoolEntry[]> {
    return of(this.mockEntries);
  }

  getMockedTopicPools(): Observable<TopicPool[]> {
    return of(this.mockTopicPools);
  }
}
