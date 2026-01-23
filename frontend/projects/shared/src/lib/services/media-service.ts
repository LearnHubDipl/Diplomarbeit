// shared/src/lib/services/media.service.ts
import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {Media} from '../interfaces/media';
import {API_BASE_URL} from './globals';

@Injectable({providedIn: 'root'})
export class MediaService {

  constructor(private http: HttpClient) {
  }

  create(dto: { path: string; type: 'img' | 'pdf'; description?: string }): Observable<Media> {
    return this.http.post<Media>(API_BASE_URL  + '/media', dto);
  }
}
