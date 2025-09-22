import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { TopicPool } from '../interfaces/topic-pool';
import { API_BASE_URL } from './globals';

@Injectable({
  providedIn: 'root'
})
export class TopicPoolService {
  private httpClient = inject(HttpClient);

  getTopicPoolsBySubject(subjectId: number): Observable<TopicPool[]> {
    return this.httpClient.get<TopicPool[]>(`${API_BASE_URL}/subjects/${subjectId}/topics`);
  }

  findBySubject(subjectId: number) {
    return this.httpClient.get<TopicPool[]>(`${API_BASE_URL}/subjects/${subjectId}/topics`);
  }

  getAllTopicPools(): Observable<TopicPool[]> {
    return this.httpClient.get<TopicPool[]>(`${API_BASE_URL}/topic-pools`);
  }

  createBatch(subjectId: number, names: string[]) {
    return this.httpClient.post<{ id: number; name: string; description?: string }[]>(
      `${API_BASE_URL}/subjects/${subjectId}/topics/batch`,
      { names }
    );
  }

  createOne(subjectId: number, name: string, description?: string) {
    return this.httpClient.post<TopicPool>(
      `${API_BASE_URL}/subjects/${subjectId}/topics`,
      { name, description }
    );
  }

  updateOne(subjectId: number, poolId: number, body: { name?: string; description?: string }) {
    return this.httpClient.put<TopicPool>(
      `${API_BASE_URL}/subjects/${subjectId}/topics/${poolId}`,
      body
    );
  }

  deleteOne(subjectId: number, poolId: number) {
    return this.httpClient.delete<void>(
      `${API_BASE_URL}/subjects/${subjectId}/topics/${poolId}`
    );
  }

}
