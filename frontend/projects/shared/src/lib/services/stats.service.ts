import { Injectable } from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import { map, Observable } from 'rxjs';
import { TopicPool } from '../../../../matura-trainer/src/lib/stats-topics/stats-topics.component';
import {ProgressOverviewDto} from '../../../../matura-trainer/src/lib/home/home.component';
import {Exam} from '../interfaces/exam';
import { API_BASE_URL } from "./globals";


// exam.service.ts
export interface AnswerSlimDto {
  id: number;
  text: string;
}

export interface QuestionDetailDto {
  id: number;
  text: string;
  answers: AnswerSlimDto[]; // alle möglichen Antworten
}

export interface ExamQuestionDetailDto {
  id: number;
  question: QuestionDetailDto;
  freeTextAnswer: string;
  isCorrect: boolean;
  selectedAnswers: AnswerSlimDto[]; // gewählte Antworten
}

export interface ExamDto {
  id: number;
  score: number;
  questionCount: number;
  startedAt: string;
  finishedAt: string;
  questions: ExamQuestionDetailDto[];
}


export interface ExamQuestionSlimDto {
  id: number;
  question: { id: number; questionText: string;answers: { id: number; text: string }[]; };
  freeTextAnswer: string;
  isCorrect: boolean;
  selectedAnswers: { id: number; text: string }[];
  correctAnswerIds: number[];
  correctFreeTextAnswers: string[];
}

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
  //private questionPoolApiUrl = 'http://localhost:8080/api/questionPools';
  private statsApiUrl = 'http://localhost:8080/api/stats';
  private examApiUrl = 'http://localhost:8080/api/exams';

  constructor(private http: HttpClient) {}

  getStreak(userId: number): Observable<number> {
    return this.http.get<{ streak: number }>(`${this.streakApiUrl}/user/${userId}`)
      .pipe(map(response => response.streak));
  }

  getEntriesByTopicPool(userId: number, topicPoolId: number): Observable<QuestionPoolEntrySlimDto[]> {
    return this.http.get<QuestionPoolEntrySlimDto[]>(`${API_BASE_URL}/questionPools/${userId}/${topicPoolId}`);
  }

  getTopicPools(userId: number): Observable<TopicPool[]> {
    return this.http.get<TopicPool[]>(`${API_BASE_URL}/questionPools/${userId}/topicPools`);
  }

  getStatsOverview(userId: number): Observable<StatsOverviewDto> {
    return this.http.get<StatsOverviewDto>(`${API_BASE_URL}/stats/${userId}/overview`);
  }
  getStatsOverviewForTopicPool(userId: number, topicPoolId: number): Observable<StatsOverviewDto> {
    return this.http.get<StatsOverviewDto>(`${API_BASE_URL}/stats/${userId}/topicPool/${topicPoolId}/overview`);
  }

  getAllExams(): Observable<Exam[]> {
    return this.http.get<Exam[]>(`${API_BASE_URL}/exams`);
  }

  getProgressOverview(userId: number, topicPoolId?: number): Observable<ProgressOverviewDto> {
    let params = new HttpParams();
    if (topicPoolId != null) {
      params = params.set('topicPoolId', topicPoolId.toString());
    }
    return this.http.get<ProgressOverviewDto>(`${API_BASE_URL}/stats/${userId}/progress`, { params });
  }



}
