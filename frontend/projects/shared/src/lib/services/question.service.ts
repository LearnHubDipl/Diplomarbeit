import {inject, Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {Question} from '../interfaces/question';
import {QuestionRequest} from '../interfaces/question-creation-request';
import {API_BASE_URL} from './globals';
import {TopicPool} from '../interfaces/topic-pool';

@Injectable({
  providedIn: 'root'
})
export class QuestionService {
  private httpClient = inject(HttpClient);

  getQuestionById(id: number): Observable<Question> {
    return this.httpClient.get<Question>(API_BASE_URL + '/questions/' + id);
  }

  getQuestionsByTopicPool(topicPool: TopicPool): Observable<Question[]> {
    return this.httpClient.get<Question[]>(API_BASE_URL + '/questions/byTopicPool/' + topicPool.id);
  }

  createQuestion(questionRequest: QuestionRequest): Observable<Question> {
    return this.httpClient.post<Question>(API_BASE_URL + '/questions', questionRequest);
  }
}
