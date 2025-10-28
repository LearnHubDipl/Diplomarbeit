import {inject, Injectable} from '@angular/core';
import {HttpClient, HttpHeaders} from '@angular/common/http';
import {BehaviorSubject, Observable} from 'rxjs';
import {UserSlim} from '../interfaces/userSlim';
import {API_BASE_URL} from "./globals";

@Injectable({
  providedIn: 'root'
})
export class UserService {
  private httpClient: HttpClient = inject(HttpClient);


  private URL: string = 'http://localhost:8080/api/';

  // BehaviorSubject to track current user
  private userSubject = new BehaviorSubject<UserSlim | null>(null);
  user$ = this.userSubject.asObservable(); // Observable that components can subscribe to

  // Track previous user ID for detecting user changes
  private previousUserId: number | null = null;

  // Event listener for auth changes
  private authEventListener: any;

  constructor() {
    // Initialize from localStorage on service creation
    const token = localStorage.getItem('token');
    if (token) {
      this.refreshUserData(token);
    }

    console.log("hier")
    console.log(this.user$)
    // Listen for auth state changes
    this.authEventListener = this.handleAuthStateChange.bind(this);
    window.addEventListener('auth-state-changed', this.authEventListener);
  }

  ngOnDestroy() {
    // Clean up event listener
    if (this.authEventListener) {
      window.removeEventListener('auth-state-changed', this.authEventListener);
    }
  }

  // Handle auth state changes from outside Angular
  private handleAuthStateChange(event: any) {
    const token = event.detail?.token;
    console.log('Auth state changed event received:', token ? 'Token present' : 'No token');
    this.refreshUserData(token || '');
  }

  // Get current user synchronously (if needed)
  getUser(): UserSlim | null {
    return this.userSubject.value;
  }

  // Method to refresh user data with a token
  refreshUserData(token: string): void {
    if (!token) {
      // If no token provided, clear the user
      this.previousUserId = this.userSubject.value?.id || null;
      this.userSubject.next(null);
      return;
    }
    // TODO User
    /*
        this.getPersonByToken(token).subscribe({
          next: (user) => {
            // Check if this is a different user than the previous one
            const isNewUser = this.previousUserId !== null && this.previousUserId !== user.id;
            if (isNewUser) {
              console.log('New user logged in:', user.name);
              // You could trigger specific actions for new user login here
            }

            // Update user information
            this.previousUserId = user.id;
            this.userSubject.next(user);
          },
          error: (error) => {
            console.error('Error fetching user data:', error);
            this.userSubject.next(null);
          }
        });
      }

      getPersonByToken(token: string): Observable<UserSlim> {
        const headers = new HttpHeaders({
          'Authorization': `Bearer ${token}`
        });
        return this.http.get<UserSlim>(${this.URL}persons/token, {headers});
      }
    */
  }
  getUserById(id: number): Observable<UserSlim> {
    return this.httpClient.get<UserSlim>(API_BASE_URL + "/users/" + id)
  }
}
