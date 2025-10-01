import { Component, OnInit, ViewChild, ElementRef } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators, FormGroup } from '@angular/forms';

import { SubjectService } from '../../../../shared/src/lib/services/subject.service';
import { TopicPoolService } from '../../../../shared/src/lib/services/topic-pool.service';
import { TopicContentService } from '../../../../shared/src/lib/services/topic-content.service';
import { TeachersService } from '../../../../shared/src/lib/services/teacher.service';

import { TopicContent } from '../../../../shared/src/lib/interfaces/topicContent';
import { Teacher } from '../../../../shared/src/lib/interfaces/teacher';
import { TopicPool } from '../../../../shared/src/lib/interfaces/topic-pool';
import { Subject as SchoolSubject } from '../../../../shared/src/lib/interfaces/subject';

declare var bootstrap: any; // Bootstrap JS für Modal

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

  @ViewChild('fileInput') fileInput!: ElementRef<HTMLInputElement>;

  constructor(
    private route: ActivatedRoute,
    private fb: FormBuilder,
    private subjectsApi: SubjectService,
    private poolsApi: TopicPoolService,
    private notesApi: TopicContentService,
    private teachersApi: TeachersService
  ) {}

  ngOnInit(): void {
    this.form = this.fb.group({
      title: ['', Validators.required],
      description: [''],
      uploaderName: [''],
      teacherId: [null],
      file: [null, Validators.required],
    });

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

  loadNotes(): void {
    this.loading = true;
    this.error = null;
    this.notesApi.listNotes(this.poolId).subscribe({
      next: list => {
        this.notes = list ?? [];
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
    this.uploading = true;
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
      error: () => {
        this.error = 'Upload fehlgeschlagen (nur PDF erlaubt?)';
        this.uploading = false;
      }
    });
  }

  openUploadModal(): void {
    this.editing = false;
    this.editingOldFileName = null;
    this.form.reset();
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

    this.editing = true;
    this.editingOldFileName = n.fileName;

    this.form.patchValue({
      title: n.title ?? n.fileName,
      description: n.description ?? '',
      uploaderName: n.uploaderName ?? '',
      teacherId: (n as any).teacherId ?? null,
      file: null
    });

    this.form.get('file')?.setValidators([Validators.required]);
    this.form.get('file')?.updateValueAndValidity();
    this.resetFileInput();

    const modalEl = document.getElementById('uploadModal');
    if (modalEl) {
      const instance = bootstrap.Modal.getInstance(modalEl) || new bootstrap.Modal(modalEl);
      instance.show();
    }
  }

  deleteNote(n: TopicContent, ev: Event): void {
    ev.stopPropagation();

    if (!n.fileName) {
      console.error('Kein fileName im Objekt:', n);
      alert('Löschen geht nicht: Dateiname fehlt.');
      return;
    }

    if (!confirm(`Soll die Mitschrift "${n.title ?? n.fileName}" wirklich gelöscht werden?`)) return;

    this.notesApi.deleteNote(this.poolId, n.fileName)
      .subscribe({
        next: () => this.loadNotes(),
        error: err => {
          console.error('DELETE fehlgeschlagen', err);
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
