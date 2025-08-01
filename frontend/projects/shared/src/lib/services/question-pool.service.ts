import {inject, Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {QuestionPool, QuestionPoolEntry, QuestionPoolEntryRequest} from '../interfaces/question-pool';
import {API_BASE_URL} from './globals';
import {TopicPool} from '../interfaces/topic-pool';

@Injectable({
  providedIn: 'root'
})
export class QuestionPoolService {
  httpClient: HttpClient = inject(HttpClient);

  constructor() { }

  getQuestionPoolForUser(userId: number) : Observable<QuestionPool> {
    return this.httpClient.get<QuestionPool>(API_BASE_URL + '/questionPools/' + userId);
  }

  getEntriesByTopicPool(userId: number, topicPool: TopicPool) : Observable<QuestionPoolEntry[]> {
    return this.httpClient.get<QuestionPoolEntry[]>(API_BASE_URL + '/questionPools/' + userId + '/' + topicPool.id);
  }

  postQuestionsToQuestionPool(request: QuestionPoolEntryRequest) : Observable<QuestionPool> {
    return this.httpClient.post<QuestionPool>(API_BASE_URL + '/questionPools/addQuestions', request);
  }
}
