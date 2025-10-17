import {inject, Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {UserSlim} from '../interfaces/userSlim';
import { API_BASE_URL } from "./globals";

@Injectable({
  providedIn: 'root'
})
export class UserService {
  private httpClient: HttpClient = inject(HttpClient);

  getUserById(id: number): Observable<UserSlim> {
      return this.httpClient.get<UserSlim>(API_BASE_URL + "/users/" + id)
  }
}
