import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { TopicContent } from '../interfaces/topicContent';
import {TopicContentSlim} from '../interfaces/TopicContentSlim';
import {API_BASE_URL} from './globals';

export interface CreateTopicContentRequest {
  subjectId: number;
  topicPoolId?: number | null;
  mediaId: number;
  title: string;
  uploaderName: string;
  description?: string;
}

@Injectable({ providedIn: 'root' })
export class TopicContentsService {
  constructor(private http: HttpClient) {}

  list(subjectId: number, topicPoolId?: number): Observable<TopicContent[]> {
    let params = new HttpParams().set('subjectId', subjectId);
    if (topicPoolId != null) params = params.set('topicPoolId', topicPoolId);
    return this.http.get<TopicContentSlim[]>(`${API_BASE_URL}/subjects/topic-contents`, { params });
  }

  create(req: CreateTopicContentRequest): Observable<TopicContentSlim> {
    return this.http.post<TopicContentSlim>(`${API_BASE_URL}/subjects/topic-contents`, req);
  }
}
