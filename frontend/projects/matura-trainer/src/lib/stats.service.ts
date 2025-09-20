import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { map, Observable } from 'rxjs';
import { TopicPool } from './stats-topics/stats-topics.component';

export interface StatsLegendEntry {
  label: string;
  value: number; // Prozentwert
  color: string;
}

export interface StatsOverviewDto {
  incorrect: number;
  sufficient: number;
  correctTwice: number;
  correctOnce: number;
  unanswered: number;
  legend: StatsLegendEntry[];
}

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
  private statsApiUrl = 'http://localhost:8080/api/stats'; // neue API

  constructor(private http: HttpClient) {}

  /** Streak bleibt unverändert */
  getStreak(userId: number): Observable<number> {
    return this.http.get<{ streak: number }>(`${this.streakApiUrl}/user/${userId}`)
      .pipe(map(response => response.streak));
  }

  /** Für Kompatibilität, falls du noch die Einträge brauchst */
  getEntriesByTopicPool(userId: number, topicPoolId: number): Observable<QuestionPoolEntrySlimDto[]> {
    return this.http.get<QuestionPoolEntrySlimDto[]>(`${this.questionPoolApiUrl}/${userId}/${topicPoolId}`);
  }

  getTopicPools(userId: number): Observable<TopicPool[]> {
    return this.http.get<TopicPool[]>(`${this.questionPoolApiUrl}/${userId}/topicPools`);
  }

  /** **Neu:** aggregierte Statistik inkl. Legende für global */
  getStatsOverview(userId: number): Observable<StatsOverviewDto> {
    return this.http.get<StatsOverviewDto>(`${this.statsApiUrl}/${userId}/overview`);
  }

  /** **Neu:** aggregierte Statistik inkl. Legende für einen TopicPool */
  getStatsOverviewForTopicPool(userId: number, topicPoolId: number): Observable<StatsOverviewDto> {
    return this.http.get<StatsOverviewDto>(`${this.statsApiUrl}/${userId}/topicPool/${topicPoolId}/overview`);
  }
}
