import {Component, inject, OnInit} from '@angular/core';
import { KeycloakOperationService } from '../../../../shared/src/lib/auth';
import { UserInitializationService } from '../../../../shared/src/lib/services/user-initialization.service';
import { UserSlim } from '../../../../shared/src/lib/interfaces/userSlim';
import {DatePipe, NgForOf, NgIf} from '@angular/common';
import {PendingNotesService} from '../../../../shared/src/lib/services/pending-notes.service';
import {PendingNoteDto} from '../../../../shared/src/lib/interfaces/pendingNoteDto';

@Component({
  selector: 'lib-personal-place',
  standalone: true,
  templateUrl: './personal-place.component.html',
  imports: [NgIf, NgForOf, DatePipe],
  styleUrls: ['./personal-place.component.css']
})
export class PersonalPlaceComponent implements OnInit {
  keycloakService = inject(KeycloakOperationService);
  userInitService = inject(UserInitializationService);
  pendingNotesService = inject(PendingNotesService);
  givenName = '';
  familyName = '';
  displayName = '';
  klasse = '';
  isStudent = false;
  isTeacher = false;
  email = '';

  // User from backend
  backendUser: UserSlim | null = null;
  isLoadingUser = true;

  pendingNotes: PendingNoteDto[] = [];
  isLoadingPending = false;
  pendingError: string | null = null;



  async ngOnInit() {
    this.loadKeycloakData();
    await this.loadBackendUser();

    if (this.canSeeTeacherArea()) {
      this.loadPendingNotes();
    }
  }

  /**
   * Load data from Keycloak token
   */
  private loadKeycloakData() {
    try {
      this.givenName = this.keycloakService.getGivenName();
      this.familyName = this.keycloakService.getFamilyName();
      this.displayName = this.keycloakService.getDisplayName();
      this.klasse = this.keycloakService.getClassFromDN();
      this.isStudent = this.keycloakService.getIsStudent();
      this.isTeacher = this.keycloakService.getIsTeacher();
      this.email = this.keycloakService.getEmail();

      console.log('PersonalPlaceComponent -> Keycloak Data loaded');
      console.log('User info:', {
        givenName: this.givenName,
        familyName: this.familyName,
        displayName: this.displayName,
        klasse: this.klasse,
        isStudent: this.isStudent,
        isTeacher: this.isTeacher,
        email: this.email
      });
    } catch (err) {
      console.error('Error loading Keycloak data:', err);
    }
  }

  /**
   * Load user from backend (should already be initialized at app start)
   */
  private async loadBackendUser() {
    try {
      this.isLoadingUser = true;

      // Get the already initialized user
      this.backendUser = this.userInitService.getCurrentUser();

      // If not available yet, wait for initialization
      if (!this.backendUser) {
        console.log('User not yet initialized, waiting...');
        this.backendUser = await this.userInitService.initializeUser();
      }

      if (this.backendUser) {
        console.log('Backend user loaded:', this.backendUser);
        console.log('Database ID:', this.backendUser.id);
        console.log('Keycloak Sub:', this.backendUser.keycloakSub);
      } else {
        console.warn('No backend user available');
      }
    } catch (error) {
      console.error('Error loading backend user:', error);
    } finally {
      this.isLoadingUser = false;
    }
  }

  canSeeTeacherArea(): boolean {
    // Du nutzt aktuell backendUser?.isAdmin + Keycloak isTeacher
    return !!this.backendUser?.isAdmin || this.isTeacher;
  }

  loadPendingNotes() {
    this.isLoadingPending = true;
    this.pendingError = null;

    this.pendingNotesService.listMyPending().subscribe({
      next: (list) => {
        this.pendingNotes = list ?? [];
        console.log('[PendingNotes] loaded', this.pendingNotes);
        this.isLoadingPending = false;
      },
      error: (err) => {
        console.error('[PendingNotes] load failed', err);
        this.pendingError =
          err?.error?.error ||
          err?.message ||
          'Pending Mitschriften konnten nicht geladen werden.';
        this.isLoadingPending = false;
      }
    });
  }

  approveNote(n: PendingNoteDto) {
    if (!n?.topicPoolId || !n?.fileName) return;

    this.pendingNotesService.approve(n.topicPoolId, n.fileName).subscribe({
      next: () => this.loadPendingNotes(),
      error: (err) => {
        console.error('[PendingNotes] approve failed', err);
        alert('Freigeben ist fehlgeschlagen.');
      }
    });
  }

  rejectNote(n: PendingNoteDto) {
    if (!n?.topicPoolId || !n?.fileName) return;

    const ok = confirm('Mitschrift wirklich ablehnen? (Sie wird gelöscht)');
    if (!ok) return;

    this.pendingNotesService.reject(n.topicPoolId, n.fileName).subscribe({
      next: () => this.loadPendingNotes(),
      error: (err) => {
        console.error('[PendingNotes] reject failed', err);
        alert('Ablehnen ist fehlgeschlagen.');
      }
    });
  }

  async doLogout() {
    // Clear user before logout
    this.userInitService.clearUser();
    await this.keycloakService.logout();
  }
}
