import {inject, Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {TopicPool} from '../interfaces/topic-pool';
import {Subject} from '../interfaces/subject';
import {API_BASE_URL} from './globals';

@Injectable({
  providedIn: 'root'
})
export class TopicPoolService {
  private httpClient = inject(HttpClient);

  getTopicPoolsBySubject(subjectId: number): Observable<TopicPool[]> {
    return this.httpClient.get<TopicPool[]>(API_BASE_URL + `/subjects/${subjectId}/topics`);
  }

  getAllTopicPools(): Observable<TopicPool[]> {
    return this.httpClient.get<TopicPool[]>(API_BASE_URL + '/topic-pools');
  }
  createBatch(subjectId: number, names: string[]) {
    return this.httpClient.post<{ id: number; name: string; description?: string }[]>(API_BASE_URL+`/subjects/${subjectId}/topics/batch`, { names }
    );
  }

  createOne(subjectId: number, name: string, description?: string) {
    return this.httpClient.post<TopicPool>(+API_BASE_URL+`/subjects/${subjectId}/topics`, { name, description });
  }
}
