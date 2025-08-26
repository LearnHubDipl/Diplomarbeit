import {inject, Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {CheckAnswerRequest} from '../interfaces/answer';
import {Question} from '../interfaces/question';
import {Observable} from 'rxjs';
import {API_BASE_URL} from './globals';

export interface CreateExamRequest {
  userId: number;
  topicPoolIds: number[];
  numberOfQuestions: number;
  timeLimitMinutes: number;
}

export interface CreatedExamResponse {
  examId: number;
  timeLimit: number;
  startedAt: string;
  questionCount: number;
  questions: Question[];
}

export interface SubmitExamRequest {
  examId: number;
  answers: CheckAnswerRequest[];
}

export interface SubmittedExamResponse {
  examId: number;
  score: number;
  questions: {
    questionId: number;
    correct: boolean;
    selectedAnswerIds?: number[];
    freeTextAnswer?: string;
  }[];
}

@Injectable({
  providedIn: 'root'
})
export class ExamService {

  httpClient: HttpClient = inject(HttpClient);

  constructor() { }

  createExam(request: CreateExamRequest): Observable<CreatedExamResponse> {
    return this.httpClient.post<CreatedExamResponse>(`${API_BASE_URL}/exams/create`, request);
  }

  submitExam(request: SubmitExamRequest): Observable<SubmittedExamResponse> {
    return this.httpClient.post<SubmittedExamResponse>(`${API_BASE_URL}/exams/submit`, request);
  }

  /*
  getExam(examId: number): Observable<CreatedExamResponse> {
    return this.httpClient.get<CreatedExamResponse>(`${API_BASE_URL}/exams/${examId}`);
  }*/
}
