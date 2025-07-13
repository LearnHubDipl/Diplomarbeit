import {inject, Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {Question} from '../interfaces/question';
import {API_BASE_URL} from './globals';

@Injectable({
  providedIn: 'root'
})
export class QuestionService {
  httpClient: HttpClient = inject(HttpClient);

  constructor() { }

  getQuestionById(id: number): Observable<Question> {
    return this.httpClient.get<Question>(API_BASE_URL + '/questions/' + id);
  }
}
