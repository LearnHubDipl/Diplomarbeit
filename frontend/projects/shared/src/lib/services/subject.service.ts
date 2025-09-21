import {inject, Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {API_BASE_URL} from './globals';
import {Subject} from '../interfaces/subject';
import {CreateSubjectRequestDto} from '../interfaces/CreateSubjectRequestDto';

@Injectable({
  providedIn: 'root'
})
export class SubjectService {
  httpClient: HttpClient = inject(HttpClient);

  constructor() {
  }
  list(): Observable<Subject[]> {
    return this.httpClient.get<Subject[]>(API_BASE_URL + '/subjects');
  }

  getAllSubjects(): Observable<Subject[]> {
    return this.httpClient.get<Subject[]>(API_BASE_URL + '/subjects');
  }

  get(id: number): Observable<Subject> {
    return this.httpClient.get<Subject>(`${API_BASE_URL}/subjects/${id}`);
  }

  create(dto: CreateSubjectRequestDto): Observable<Subject> {
    return this.httpClient.post<Subject>(API_BASE_URL + '/subjects', dto);
  }

  update(id: number, payload: { name?: string; description?: string; imgId?: number | null }) {
    return this.httpClient.put<Subject>(`${API_BASE_URL}/subjects/${id}`, payload);
  }

  delete(id: number) {
    return this.httpClient.delete<void>(`${API_BASE_URL}/subjects/${id}`);
  }
}
