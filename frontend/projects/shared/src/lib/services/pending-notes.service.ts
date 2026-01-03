import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {API_BASE_URL} from './globals';

export interface PendingNoteDto {
  topicPoolId: number;
  fileName: string;
  title?: string;
  uploaderName?: string;
  uploaderSub?: string;
  teacherId?: number;
  createdAt?: number;
  status?: 'PENDING' | 'APPROVED' | string;
  approved?: boolean;
  publicUrl?: string;
}

@Injectable({ providedIn: 'root' })
export class PendingNotesService {
  private http = inject(HttpClient);

  listMyPending(): Observable<PendingNoteDto[]> {
    return this.http.get<PendingNoteDto[]>(`${API_BASE_URL}/notes/pending`);
  }

  approve(topicPoolId: number, fileName: string): Observable<any> {
    return this.http.post(
      `${API_BASE_URL}/topic-pools/${topicPoolId}/notes/${encodeURIComponent(fileName)}/approve`,
      {}
    );
  }

  reject(topicPoolId: number, fileName: string): Observable<any> {
    return this.http.post(
      `${API_BASE_URL}/topic-pools/${topicPoolId}/notes/${encodeURIComponent(fileName)}/reject`,
      {}
    );
  }
}
