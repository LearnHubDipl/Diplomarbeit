import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {firstValueFrom} from 'rxjs';

@Injectable({ providedIn: 'root' })
export class AuthContextService {
  private me?: any;

  constructor(private http: HttpClient) {}

  async loadMe(): Promise<void> {
    this.me = await firstValueFrom(
      this.http.get('http://localhost:8080/api/users/me')
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
