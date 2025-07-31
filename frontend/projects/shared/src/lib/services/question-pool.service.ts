import {inject, Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {QuestionPool, QuestionPoolEntryRequest} from '../interfaces/question-pool';
import {API_BASE_URL} from './globals';

@Injectable({
  providedIn: 'root'
})
export class QuestionPoolService {
  httpClient: HttpClient = inject(HttpClient);

  constructor() { }

  getQuestionPoolForUser(userId: number) : Observable<QuestionPool> {
    return this.httpClient.get<QuestionPool>(API_BASE_URL + '/questionPools/' + userId);
  }

  postQuestionsToQuestionPool(request: QuestionPoolEntryRequest) : Observable<QuestionPool> {
    return this.httpClient.post<QuestionPool>(API_BASE_URL + '/questionPools/addQuestions', request);
  }
}
