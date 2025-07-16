import {inject, Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {Question} from '../interfaces/question';
import {API_BASE_URL} from './globals';
import {CheckAnswerRequest, CheckAnswerResponse} from '../interfaces/answer';

@Injectable({
  providedIn: 'root'
})
export class AnswerService {
  httpClient: HttpClient = inject(HttpClient);

  constructor() { }

  checkAnswers(request: CheckAnswerRequest): Observable<CheckAnswerResponse> {
    return this.httpClient.post<CheckAnswerResponse>(API_BASE_URL + '/answers/check', request);
  }
}
