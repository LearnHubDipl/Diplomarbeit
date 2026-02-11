import {inject, Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {firstValueFrom, Observable} from 'rxjs';
import {UserSlim} from '../interfaces/userSlim';
import {API_BASE_URL} from './globals';

@Injectable({
  providedIn: 'root'
})
export class UserService {
  private httpClient: HttpClient = inject(HttpClient);
  private URL = API_BASE_URL + '/users';

  registerOrGetCurrentUser(): Observable<UserSlim> {
    return this.httpClient.post<UserSlim>(`${this.URL}/register`, {});
  }

  async registerOrGetCurrentUserAsync(token?: string): Promise<UserSlim> {
    console.log('[UserService] Making request to /register');
    console.log('[UserService] Token will be added by interceptor');

    return firstValueFrom(
      this.httpClient.post<UserSlim>(`${this.URL}/register`, {})
    );
  }

  getUserById(id: number): Observable<UserSlim> {
    return this.httpClient.get<UserSlim>(`${this.URL}/${id}`);
  }

  getUserByKeycloakSub(keycloakSub: string): Observable<UserSlim> {
    return this.httpClient.get<UserSlim>(`${this.URL}/keycloak/${keycloakSub}`);
  }

  getAllUsers(): Observable<UserSlim[]> {
    return this.httpClient.get<UserSlim[]>(this.URL);
  }

  getAllTeachers(): Observable<UserSlim[]> {
    return this.httpClient.get<UserSlim[]>(`${this.URL}/teachers`);
  }
}
