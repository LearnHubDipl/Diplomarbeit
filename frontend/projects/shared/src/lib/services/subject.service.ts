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
  private base = 'http://localhost:8080/api/subjects';

  constructor() {
  }
  list(): Observable<Subject[]> {
    return this.httpClient.get<Subject[]>(this.base);
  }

  getAllSubjects(): Observable<Subject[]> {
    return this.httpClient.get<Subject[]>(this.base);
  }

  get(id: number): Observable<Subject> {
    return this.httpClient.get<Subject>(`${this.base}/${id}`);
  }

  create(dto: CreateSubjectRequestDto): Observable<Subject> {
    return this.httpClient.post<Subject>(this.base, dto);
  }

  update(id: number, payload: { name?: string; description?: string; imgId?: number | null }) {
    return this.httpClient.put<Subject>(`${this.base}/${id}`, payload);
  }

  delete(id: number) {
    return this.httpClient.delete<void>(`${this.base}/${id}`);
  }
}
