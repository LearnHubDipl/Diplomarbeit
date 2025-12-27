import {inject, Injectable} from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {Observable} from 'rxjs';
import {Question, QuestionUpdateRequest} from '../interfaces/question';
import {QuestionRequest} from '../interfaces/question-creation-request';
import {API_BASE_URL} from './globals';
import {TopicPool} from '../interfaces/topic-pool';

@Injectable({
  providedIn: 'root'
})
export class QuestionService {
  private httpClient = inject(HttpClient);
  private testurl= 'http://localhost:8080/api';

  getQuestionById(id: number): Observable<Question> {
    return this.httpClient.get<Question>(API_BASE_URL + '/questions/' + id);
  }

  getQuestionsByTopicPool(topicPool: TopicPool): Observable<Question[]> {
    return this.httpClient.get<Question[]>(API_BASE_URL + '/questions/byTopicPool/' + topicPool.id);
  }

  createQuestion(questionRequest: QuestionRequest): Observable<Question> {
    return this.httpClient.post<Question>(API_BASE_URL + '/questions', questionRequest);
  }

  deleteQuestion(id: number) {
    return this.httpClient.delete(API_BASE_URL + '/questions/' + id);
  }

  updateQuestion(id: number, updateRequest: QuestionUpdateRequest){
    return this.httpClient.patch<Question>(API_BASE_URL + '/questions/' + id, updateRequest);
  }

  getQuestionsForPractice(userId: number, topicPoolId?: number): Observable<number[]> {
    let params = new HttpParams().set('userId', userId.toString());

    if (topicPoolId != null) {
      params = params.set('topicPoolId', topicPoolId.toString());
    }

    return this.httpClient.get<number[]>(API_BASE_URL + `/questions/ids`, { params });
  }

  getAllQuestionsFromLoggedInUser(userId: number): Observable<Question[]> {
    return this.httpClient.get<Question[]>(API_BASE_URL + '/questions/user/' + userId);
  }

  getAllPublicQuestions(): Observable<Question[]> {
    return this.httpClient.get<Question[]>(API_BASE_URL + '/questions/public');
  }
}
