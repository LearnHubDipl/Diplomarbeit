import {inject, Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {API_BASE_URL} from './globals';
import {Subject} from '../interfaces/subject';

@Injectable({
  providedIn: 'root'
})
export class SubjectService {
  httpClient: HttpClient = inject(HttpClient);

  constructor() { }

  getAllSubjects(): Observable<Subject[]> {
    return this.httpClient.get<Subject[]>(API_BASE_URL + '/subjects/');
  }
}
