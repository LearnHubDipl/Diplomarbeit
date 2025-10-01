import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_BASE_URL } from './globals';

export interface Teacher {
  id: number;
  name: string;
}

@Injectable({ providedIn: 'root' })
export class TeachersService {
  private base = API_BASE_URL + '/teachers';

  constructor(private http: HttpClient) {}

  list(): Observable<Teacher[]> {
    return this.http.get<Teacher[]>(this.base);
  }
}
