import { Component, OnInit, ViewChild, ElementRef } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators, FormGroup } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';

import { SubjectService } from '../../../../shared/src/lib/services/subject.service';
import { TopicPoolService } from '../../../../shared/src/lib/services/topic-pool.service';
import { TopicContentService } from '../../../../shared/src/lib/services/topic-content.service';
import { TeachersService } from '../../../../shared/src/lib/services/teacher.service';

import { TopicContent } from '../../../../shared/src/lib/interfaces/topicContent';
import { Teacher } from '../../../../shared/src/lib/interfaces/teacher';
import { TopicPool } from '../../../../shared/src/lib/interfaces/topic-pool';
import { Subject as SchoolSubject } from '../../../../shared/src/lib/interfaces/subject';
import { API_BASE_URL } from '../../../../shared/src/lib/services/globals';

declare var bootstrap: any;

type MeDto = {
  keycloakSub: string;
  name: string;
  email: string;
  isTeacher: boolean;
  isAdmin: boolean;
  profilePictureId?: number;
};

@Component({
  selector: 'app-subject-pool-detail',
  standalone: true,
  imports: [CommonModule, RouterLink, ReactiveFormsModule],
  templateUrl: './subject-pool-detail.component.html'
})
export class SubjectPoolDetailComponent implements OnInit {
  subjectId!: number;
  poolId!: number;
  subjectName = 'Fach';
  poolName = 'Themenpool';

  notes: TopicContent[] = [];
  teachers: Teacher[] = [];

  loading = false;
  uploading = false;
  error: string | null = null;

  form!: FormGroup;

  // Edit-Status
  editing = false;
  editingOldFileName: string | null = null;

  // ✅ eingeloggter User (aus Backend /users/me)
  me: MeDto | null = null;

  // ✅ Rollen
  isAdminOrTeacher = false;
  isStudent = true;

  @ViewChild('fileInput') fileInput!: ElementRef<HTMLInputElement>;

  constructor(
    private route: ActivatedRoute,
    private fb: FormBuilder,
    private subjectsApi: SubjectService,
    private poolsApi: TopicPoolService,
    private notesApi: TopicContentService,
    private teachersApi: TeachersService,
    private http: HttpClient
  ) {}

  async ngOnInit(): Promise<void> {
    // ✅ User laden (DB-Wahrheit: isAdmin/isTeacher)
    await this.loadMe();

    this.form = this.fb.group({
      title: ['', Validators.required],
      description: [''],
      uploaderName: [''],
      teacherId: [null], // Validator kommt gleich je nach Rolle
      file: [null, Validators.required],
    });

    // ✅ Teacher Auswahl: für Schüler:innen Pflicht (für Freigabeprozess)
    this.applyTeacherValidator();

    this.subjectId = Number(this.route.snapshot.paramMap.get('subjectId'));
    this.poolId    = Number(this.route.snapshot.paramMap.get('poolId'));

    this.subjectsApi.get(this.subjectId).subscribe({
      next: (s: SchoolSubject) => (this.subjectName = s?.name ?? `Fach ${this.subjectId}`)
    });

    this.poolsApi.getTopicPoolsBySubject(this.subjectId).subscribe({
      next: (list: TopicPool[]) => {
        const found = list.find(p => p.id === this.poolId);
        this.poolName = found?.name ?? `Themenpool ${this.poolId}`;
      }
    });

    this.loadNotes();
    this.loadTeachers();
  }

  private async loadMe(): Promise<void> {
    try {
      this.me = await firstValueFrom(this.http.get<MeDto>(`${API_BASE_URL}/users/me`));
      this.isAdminOrTeacher = !!(this.me?.isAdmin || this.me?.isTeacher);
      this.isStudent = !this.isAdminOrTeacher;
    } catch {
      // Wenn /users/me nicht erreichbar ist, fail-safe: wie Schüler behandeln
      this.me = null;
      this.isAdminOrTeacher = false;
      this.isStudent = true;
    }
  }

  private applyTeacherValidator(): void {
    const ctrl = this.form?.get('teacherId');
    if (!ctrl) return;

    if (this.isStudent) {
      ctrl.setValidators([Validators.required]);
    } else {
      ctrl.clearValidators(); // Lehrer/Admin brauchen keine Freigabe
    }
    ctrl.updateValueAndValidity();
  }

  loadNotes(): void {
    this.loading = true;
    this.error = null;

    this.notesApi.listNotes(this.poolId).subscribe({
      next: list => {
        const raw = list ?? [];
        this.notes = this.filterNotesForVisibility(raw);
        this.loading = false;
      },
      error: () => {
        this.error = 'Fehler beim Laden der Mitschriften (Server nicht erreichbar?)';
        this.loading = false;
      }
    });
  }

  private loadTeachers(): void {
    this.teachersApi.list().subscribe({
      next: (t: Teacher[]) => (this.teachers = t),
      error: () => (this.teachers = [])
    });
  }

  // ---------- Rechte & Status Helfer ----------

  private getStatus(n: any): string {
    // akzeptiert mehrere mögliche Felder (robust)
    return (
      (n?.status as string) ||
      (n?.noteStatus as string) ||
      (n?.approvalStatus as string) ||
      (n?.approved === true ? 'APPROVED' : null) ||
      (n?.isApproved === true ? 'APPROVED' : null) ||
      'APPROVED' // fallback: wenn Backend keinen Status liefert
    );
  }

  isPending(n: any): boolean {
    const s = (this.getStatus(n) || '').toUpperCase();
    return s === 'PENDING' || s === 'WAITING' || s === 'IN_REVIEW';
  }

  isApproved(n: any): boolean {
    const s = (this.getStatus(n) || '').toUpperCase();
    return s === 'APPROVED' || s === 'PUBLIC' || s === 'PUBLISHED';
  }

  private getUploaderSub(n: any): string | null {
    return n?.uploaderSub || n?.uploaderKeycloakSub || n?.ownerSub || null;
  }

  isOwnNote(n: any): boolean {
    if (!this.me?.keycloakSub) return false;
    const sub = this.getUploaderSub(n);
    return !!sub && sub === this.me.keycloakSub;
  }

  canEditOrDelete(n: any): boolean {
    // Lehrer/Admin dürfen alles, Schüler nur eigene
    return this.isAdminOrTeacher || this.isOwnNote(n);
  }

  private filterNotesForVisibility(list: TopicContent[]): TopicContent[] {
    return list.filter((n: any) => {
      // Approved: alle sehen
      if (this.isApproved(n)) return true;

      // Pending: nur der Uploader sieht (ausgegraut)
      if (this.isPending(n)) return this.isOwnNote(n) || this.isAdminOrTeacher;

      // Alles andere: sicherheitshalber nur Owner/Teacher/Admin
      return this.isOwnNote(n) || this.isAdminOrTeacher;
    });
  }

  // ---------- Form / Upload ----------

  onFileChange(ev: Event): void {
    const input = ev.target as HTMLInputElement;
    if (input.files && input.files.length) {
      this.form.patchValue({ file: input.files[0] as any });
    }
  }

  resetFileInput(el?: HTMLInputElement): void {
    const node = el ?? this.fileInput?.nativeElement;
    if (node) {
      node.setAttribute('value', '');
      node.value = '';
    }
    this.form.patchValue({ file: null });
    this.form.get('file')?.markAsPristine();
    this.form.get('file')?.markAsUntouched();
  }

  upload(): void {
    if (this.form.invalid || !this.form.value.file) return;

    // Schüler müssen einen Lehrer auswählen (Freigabeprozess)
    if (this.isStudent && (this.form.value.teacherId == null || this.form.value.teacherId === '')) {
      this.error = 'Bitte wähle eine Lehrperson für die Freigabe aus.';
      return;
    }

    this.uploading = true;
    this.error = null;

    const v = this.form.value;

    const params: any = {
      file: v.file!,
      title: v.title!,
      description: (v.description ?? '').trim(),
      uploaderName: (v.uploaderName ?? '').trim() || undefined,
      teacherId: v.teacherId ?? undefined
    };

    if (this.editing && this.editingOldFileName) {
      params.replaceFileName = this.editingOldFileName;
    }

    this.notesApi.uploadNote(this.poolId, this.subjectId, params).subscribe({
      next: () => {
        this.form.reset();
        this.resetFileInput();
        this.uploading = false;
        this.closeUploadModal();
        this.editing = false;
        this.editingOldFileName = null;
        this.loadNotes();
      },
      error: (err) => {
        if (err?.status === 403) {
          this.error = 'Du hast keine Berechtigung, diese Aktion auszuführen.';
        } else {
          this.error = 'Upload fehlgeschlagen (nur PDF erlaubt?)';
        }
        this.uploading = false;
      }
    });
  }

  openUploadModal(): void {
    this.editing = false;
    this.editingOldFileName = null;
    this.form.reset();

    // Teacher Pflicht nur für Schüler
    this.applyTeacherValidator();

    // file Pflicht
    this.form.get('file')?.setValidators([Validators.required]);
    this.form.get('file')?.updateValueAndValidity();
    this.resetFileInput();

    const modalEl = document.getElementById('uploadModal');
    if (modalEl) {
      const instance = bootstrap.Modal.getInstance(modalEl) || new bootstrap.Modal(modalEl);
      instance.show();
    }
  }

  editNote(n: TopicContent, ev: Event): void {
    ev.stopPropagation();

    if (!this.canEditOrDelete(n)) return; // ✅ Schüler darf nur eigene bearbeiten

    this.editing = true;
    this.editingOldFileName = (n as any).fileName;

    this.form.patchValue({
      title: (n as any).title ?? (n as any).fileName,
      description: (n as any).description ?? '',
      uploaderName: (n as any).uploaderName ?? '',
      teacherId: (n as any).teacherId ?? null,
      file: null
    });

    // beim Edit weiterhin PDF Pflicht (du ersetzt ja das File)
    this.form.get('file')?.setValidators([Validators.required]);
    this.form.get('file')?.updateValueAndValidity();
    this.resetFileInput();

    // Schüler: Teacher bleibt Pflicht
    this.applyTeacherValidator();

    const modalEl = document.getElementById('uploadModal');
    if (modalEl) {
      const instance = bootstrap.Modal.getInstance(modalEl) || new bootstrap.Modal(modalEl);
      instance.show();
    }
  }

  deleteNote(n: TopicContent, ev: Event): void {
    ev.stopPropagation();

    if (!this.canEditOrDelete(n)) return; // ✅ Schüler darf nur eigene löschen

    const fileName = (n as any).fileName;
    if (!fileName) {
      console.error('Kein fileName im Objekt:', n);
      alert('Löschen geht nicht: Dateiname fehlt.');
      return;
    }

    if (!confirm(`Soll die Mitschrift "${(n as any).title ?? fileName}" wirklich gelöscht werden?`)) return;

    this.notesApi.deleteNote(this.poolId, fileName).subscribe({
      next: () => this.loadNotes(),
      error: err => {
        console.error('DELETE fehlgeschlagen', err);
        if (err?.status === 403) {
          alert('Du hast keine Berechtigung, diese Mitschrift zu löschen.');
          return;
        }
        alert('Löschen fehlgeschlagen.');
      }
    });
  }

  private closeUploadModal(): void {
    const modalEl = document.getElementById('uploadModal');
    if (modalEl) {
      const instance = bootstrap.Modal.getInstance(modalEl) || new bootstrap.Modal(modalEl);
      instance.hide();
    }
  }
}
