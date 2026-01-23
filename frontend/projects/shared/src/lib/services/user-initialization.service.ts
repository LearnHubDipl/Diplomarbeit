import { Injectable } from '@angular/core';
import { UserService } from './user.service';
import { KeycloakOperationService } from '../auth';
import { UserSlim } from '../interfaces/userSlim';
import { BehaviorSubject, Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class UserInitializationService {
  private currentUserSubject = new BehaviorSubject<UserSlim | null>(null);
  public currentUser$: Observable<UserSlim | null> = this.currentUserSubject.asObservable();

  private isInitializing = false;
  private initializationPromise: Promise<UserSlim | null> | null = null;

  constructor(
    private userService: UserService,
    private keycloakService: KeycloakOperationService
  ) {}

  /**
   * Initializes the current user by registering/retrieving them from the backend.
   * This should be called once after successful Keycloak authentication.
   */
  async initializeUser(): Promise<UserSlim | null> {
    if (this.initializationPromise) {
      return this.initializationPromise;
    }

    const currentUser = this.currentUserSubject.value;
    if (currentUser) {
      return Promise.resolve(currentUser);
    }

    this.isInitializing = true;
    this.initializationPromise = this.performInitialization();

    try {
      const user = await this.initializationPromise;
      return user;
    } finally {
      this.isInitializing = false;
    }
  }

  private async performInitialization(): Promise<UserSlim | null> {
    try {
      // Check if user is logged in
      if (!this.keycloakService.isLoggedIn()) {
        console.log('[UserInit] User not logged in');
        return null;
      }

      const token = await this.keycloakService.getToken();
      if (!token) {
        console.log('[UserInit] No token available');
        return null;
      }


      const user = await this.userService.registerOrGetCurrentUserAsync();

      this.currentUserSubject.next(user);

      return user;
    } catch (error) {
      console.error('[UserInit] Error initializing user:', error);
      this.currentUserSubject.next(null);
      return null;
    }
  }

  getCurrentUser(): UserSlim | null {
    return this.currentUserSubject.value;
  }

  clearUser(): void {
    this.currentUserSubject.next(null);
    this.initializationPromise = null;
  }

  isInitializingUser(): boolean {
    return this.isInitializing;
  }
}
