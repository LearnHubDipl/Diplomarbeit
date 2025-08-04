import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { map, Observable } from 'rxjs';
import { TopicPool } from './stats-topics/stats-topics.component';

export interface QuestionPoolEntrySlimDto {
  questionId: number;
  answeredAt: string | null;
  correctCount: number;
  lastAnsweredCorrectly: boolean | null;
}

export interface QuestionPoolDto {
  id: number;
  userId: number;
  topicPools: TopicPool[];
}

@Injectable({
  providedIn: 'root'
})
export class StatsService {

  private streakApiUrl = 'http://localhost:8080/streak';
  private questionPoolApiUrl = 'http://localhost:8080/api/questionPools';

  constructor(private http: HttpClient) {}

  getStreak(userId: number): Observable<number> {
    return this.http.get<{ streak: number }>(`${this.streakApiUrl}/user/${userId}`)
      .pipe(map(response => response.streak));
  }

  getEntriesByTopicPool(userId: number, topicPoolId: number): Observable<QuestionPoolEntrySlimDto[]> {
    return this.http.get<QuestionPoolEntrySlimDto[]>(`http://localhost:8080/api/questionPools/${userId}/${topicPoolId}`);
  }


  getTopicPools(userId: number): Observable<TopicPool[]> {
    return this.http.get<TopicPool[]>(`http://localhost:8080/api/questionPools/${userId}/topicPools`);
  }


  getAllEntries(userId: number): Observable<QuestionPoolEntrySlimDto[]> {
    return this.http.get<any>(`${this.questionPoolApiUrl}/${userId}`).pipe(
      map(pool => pool.entries as QuestionPoolEntrySlimDto[])
    );
  }
}
