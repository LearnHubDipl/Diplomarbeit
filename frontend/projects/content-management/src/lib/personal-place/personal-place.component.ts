import { Component, inject, OnInit } from '@angular/core';
import { KeycloakOperationService } from '../../../../shared/src/lib/auth';
import { UserInitializationService } from '../../../../shared/src/lib/services/user-initialization.service';
import { UserSlim } from '../../../../shared/src/lib/interfaces/userSlim';
import {DatePipe, NgClass, NgForOf, NgIf} from '@angular/common';
import { RouterLink } from '@angular/router';

import { PendingNotesService, PendingNoteDto } from '../../../../shared/src/lib/services/pending-notes.service';
import { NotificationsService, NotificationDto } from '../../../../shared/src/lib/services/notification.service';
import { HttpClient } from '@angular/common/http';
import { API_BASE_URL } from '../../../../shared/src/lib/services/globals';

export interface MyNoteDto {
  topicPoolId?: number;
  fileName?: string;
  title?: string;
  uploaderName?: string;
  status?: string;
  approved?: boolean;
  createdAt?: number;
  pdfUrl?: string;
}

@Component({
  selector: 'lib-personal-place',
  standalone: true,
  templateUrl: './personal-place.component.html',
  imports: [NgIf, NgForOf, RouterLink, DatePipe, NgClass],
  styleUrls: ['./personal-place.component.css'],
})
export class PersonalPlaceComponent implements OnInit {
  private keycloakService = inject(KeycloakOperationService);
  private userInitService = inject(UserInitializationService);
  private pendingNotesService = inject(PendingNotesService);
  private notificationsService = inject(NotificationsService);
  private http = inject(HttpClient);

  givenName = '';
  familyName = '';
  displayName = '';
  klasse = '';
  email = '';

  isStudent = false;
  isTeacher = false;
  isAdmin = false;

  backendUser: UserSlim | null = null;
  isLoadingUser = true;

  // Lehrer: pending Mitschriften
  pendingNotes: PendingNoteDto[] = [];
  isLoadingPending = false;
  pendingError: string | null = null;

  // Schüler: eigene Mitschriften
  myNotes: MyNoteDto[] = [];
  isLoadingMyNotes = false;
  myNotesError: string | null = null;

  // Notifications
  notifications: NotificationDto[] = [];
  unreadCount = 0;
  isLoadingNotifications = false;
  notifError: string | null = null;

  async ngOnInit() {
    this.loadKeycloakData();
    await this.loadBackendUser();

    if (this.canSeeNotifications()) {
      this.loadNotifications();
    }

    if (this.canSeeTeacherArea()) {
      this.loadPendingNotes();
    }

    if (this.isStudent && !this.isAdmin) {
      this.loadMyNotes();
    }
  }

  private loadKeycloakData() {
    try {
      this.givenName = this.keycloakService.getGivenName();
      this.familyName = this.keycloakService.getFamilyName();
      this.displayName = this.keycloakService.getDisplayName();
      this.klasse = this.keycloakService.getClassFromDN();
      this.email = this.keycloakService.getEmail();

      this.isStudent = this.keycloakService.getIsStudent();
      this.isTeacher = this.keycloakService.getIsTeacher();
    } catch (err) {
      console.error('Error loading Keycloak data:', err);
    }
  }

  private async loadBackendUser() {
    try {
      this.isLoadingUser = true;

      this.backendUser = this.userInitService.getCurrentUser();
      if (!this.backendUser) {
        this.backendUser = await this.userInitService.initializeUser();
      }

      const u: any = this.backendUser;
      this.isAdmin = !!(u?.isAdmin ?? u?.admin ?? u?.is_admin);
      this.isTeacher = !!(u?.isTeacher ?? u?.teacher ?? u?.is_teacher);
    } catch (error) {
      console.error('Error loading backend user:', error);
    } finally {
      this.isLoadingUser = false;
    }
  }

  canSeeTeacherArea(): boolean {
    return this.isTeacher && !this.isAdmin;
  }

  canSeeNotifications(): boolean {
    return !this.isAdmin;
  }

  // ── Lehrer: pending Mitschriften ──────────────────────────────────────────

  loadPendingNotes() {
    this.isLoadingPending = true;
    this.pendingError = null;

    this.pendingNotesService.listMyPending().subscribe({
      next: (list) => {
        this.pendingNotes = list ?? [];
        this.isLoadingPending = false;
      },
      error: (err) => {
        console.error('[PendingNotes] load failed', err);
        this.pendingError =
          err?.error?.error || err?.message || 'Pending Mitschriften konnten nicht geladen werden.';
        this.isLoadingPending = false;
      },
    });
  }

  approveNote(n: PendingNoteDto) {
    if (!n?.topicPoolId || !n?.fileName) return;

    this.pendingNotesService.approve(n.topicPoolId, n.fileName).subscribe({
      next: () => {
        this.loadPendingNotes();
        this.loadNotifications();
      },
      error: (err) => {
        console.error('[PendingNotes] approve failed', err);
        alert('Freigeben ist fehlgeschlagen.');
      },
    });
  }

  rejectNote(n: PendingNoteDto) {
    if (!n?.topicPoolId || !n?.fileName) return;

    const ok = confirm('Mitschrift wirklich ablehnen? (Sie wird gelöscht)');
    if (!ok) return;

    this.pendingNotesService.reject(n.topicPoolId, n.fileName).subscribe({
      next: () => {
        this.loadPendingNotes();
        this.loadNotifications();
      },
      error: (err) => {
        console.error('[PendingNotes] reject failed', err);
        alert('Ablehnen ist fehlgeschlagen.');
      },
    });
  }

  // ── Schüler: eigene Mitschriften ──────────────────────────────────────────

  loadMyNotes() {
    this.isLoadingMyNotes = true;
    this.myNotesError = null;

    this.http.get<MyNoteDto[]>(`${API_BASE_URL}/notes/my`).subscribe({
      next: (list) => {
        this.myNotes = list ?? [];
        this.isLoadingMyNotes = false;
      },
      error: (err) => {
        console.error('[MyNotes] load failed', err);
        this.myNotesError =
          err?.error?.error || err?.message || 'Mitschriften konnten nicht geladen werden.';
        this.isLoadingMyNotes = false;
      },
    });
  }

  getStatusLabel(n: MyNoteDto): string {
    const s = (n.status ?? '').toUpperCase();
    if (s === 'APPROVED') return 'Freigegeben';
    if (s === 'PENDING') return 'Wartet auf Freigabe';
    return s;
  }

  getStatusClass(n: MyNoteDto): string {
    const s = (n.status ?? '').toUpperCase();
    if (s === 'APPROVED') return 'bg-success';
    if (s === 'PENDING') return 'bg-secondary';
    return 'bg-danger';
  }

  // ── Notifications ─────────────────────────────────────────────────────────

  loadNotifications() {
    this.isLoadingNotifications = true;
    this.notifError = null;

    this.notificationsService.listMe().subscribe({
      next: (list) => {
        this.notifications = (list ?? []).sort((a, b) => b.createdAt - a.createdAt);
        this.unreadCount = this.notifications.filter((n) => !n.read).length;
        this.isLoadingNotifications = false;
      },
      error: (err) => {
        console.error('[Notifications] load failed', err);
        this.notifError =
          err?.error?.error || err?.message || 'Notifications konnten nicht geladen werden.';
        this.isLoadingNotifications = false;
      },
    });
  }

  markNotificationRead(n: NotificationDto) {
    if (!n?.id || n.read) return;

    this.notificationsService.markRead(n.id).subscribe({
      next: () => this.loadNotifications(),
      error: (err) => {
        console.error('[Notifications] markRead failed', err);
        alert('Als gelesen markieren ist fehlgeschlagen.');
      },
    });
  }

  markAllNotificationsRead() {
    this.notificationsService.markAllRead().subscribe({
      next: () => this.loadNotifications(),
      error: (err) => {
        console.error('[Notifications] markAllRead failed', err);
        alert('Alle als gelesen markieren ist fehlgeschlagen.');
      },
    });
  }

  async doLogout() {
    this.userInitService.clearUser();
    await this.keycloakService.logout();
  }
}
