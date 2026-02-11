import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {firstValueFrom} from 'rxjs';
import {API_BASE_URL} from '../services/globals';

@Injectable({ providedIn: 'root' })
export class AuthContextService {
  private me?: any;
  private URL = API_BASE_URL + '/users';

  constructor(private http: HttpClient) {}

  async loadMe(): Promise<void> {
    this.me = await firstValueFrom(
      this.http.get(`${this.URL}/me`)
    );
  }

  isAdmin(): boolean {
    return !!this.me?.isAdmin;
  }

  isTeacher(): boolean {
    return !!this.me?.isTeacher;
  }

  canManage(): boolean {
    return this.isAdmin() || this.isTeacher();
  }
}
