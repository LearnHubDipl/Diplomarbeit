// shared/src/lib/services/file.service.ts
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import {firstValueFrom, Observable} from 'rxjs';
import { UploadPdfResult } from '../interfaces/UploadPdfResult';
import {TopicContent} from '../interfaces/topicContent';


export interface UploadPdfDto {
  title: string;
  subjectId: number | null;
  topicPoolId: number | null;
  uploaderUserId: number | null;
  base64: string;
  fileName: string;
}

@Injectable({ providedIn: 'root' })
export class FileService {
  private base = 'http://localhost:8080/api/files';

  constructor(private http: HttpClient) {}

  uploadPdfBase64(dto: UploadPdfDto): Observable<UploadPdfResult | TopicContent> {
    return this.http.post<UploadPdfResult | TopicContent>(`${this.base}/pdf-base64`, dto);
  }

  sendPdfByEmail(payload: {
    toEmail: string;
    fileName: string;
    title: string;
    uploaderName?: string;
    subjectName?: string;
    topicPoolName?: string;
  }) {
    return this.http.post<void>(`${this.base}/send-email`, payload);
  }
}
