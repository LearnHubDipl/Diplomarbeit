// shared/src/lib/services/media.service.ts
import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {Media} from '../interfaces/media';

@Injectable({providedIn: 'root'})
export class MediaService {
  private base = 'http://localhost:8080/api/media'; // oder '/api/media' mit Proxy

  constructor(private http: HttpClient) {
  }

  create(dto: { path: string; type: 'img' | 'pdf'; description?: string }): Observable<Media> {
    return this.http.post<Media>(this.base, dto);
  }
}
