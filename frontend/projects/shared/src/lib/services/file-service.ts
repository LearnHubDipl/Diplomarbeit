import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import {firstValueFrom, Observable} from 'rxjs';
import { UploadPdfResult } from '../interfaces/UploadPdfResult';
import {TopicContent} from '../interfaces/topicContent';
import {API_BASE_URL} from './globals';


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

  constructor(private http: HttpClient) {}

  uploadPdfBase64(dto: UploadPdfDto): Observable<UploadPdfResult | TopicContent> {
    return this.http.post<UploadPdfResult | TopicContent>(`${API_BASE_URL}/pdf-base64`, dto);
  }

  sendPdfByEmail(payload: {
    toEmail: string;
    fileName: string;
    title: string;
    uploaderName?: string;
    subjectName?: string;
    topicPoolName?: string;
  }) {
    return this.http.post<void>(`${API_BASE_URL}/send-email`, payload);
  }
}
