import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { API_BASE_URL } from './globals';
import { Observable } from 'rxjs';

export interface NotificationDto {
  id: string;
  type: string;
  title: string;
  message: string;
  createdAt: number;
  read: boolean;
  meta?: Record<string, any>;
}

@Injectable({ providedIn: 'root' })
export class NotificationsService {
  constructor(private http: HttpClient) {}

  listMe(): Observable<NotificationDto[]> {
    return this.http.get<NotificationDto[]>(`${API_BASE_URL}/notifications/me`);
  }

  markRead(id: string): Observable<void> {
    return this.http.post<void>(
      `${API_BASE_URL}/notifications/${encodeURIComponent(id)}/read`,
      {}
    );
  }

  markAllRead(): Observable<void> {
    return this.http.post<void>(`${API_BASE_URL}/notifications/read-all`, {});
  }
}
