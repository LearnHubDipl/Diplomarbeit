import {Component, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';
import {Subject} from '../../../../shared/src/lib/interfaces/subject';
import {SubjectService} from '../../../../shared/src/lib/services/subject.service';
import {TopicPoolService} from '../../../../shared/src/lib/services/topic-pool.service';
import {MediaService} from '../../../../shared/src/lib/services/media-service';
import {firstValueFrom} from 'rxjs';
import {SubjectCardComponent} from '../subject-card/subject-card.component';
import {KeycloakOperationService} from '../../../../shared/src/lib/auth';
import {AuthContextService} from '../../../../shared/src/lib/auth/AuthContextService';


@Component({
  selector: 'lib-subjects',
  standalone: true,
  imports: [CommonModule, FormsModule, SubjectCardComponent],
  templateUrl: './subjects.component.html'
})
export class SubjectsComponent implements OnInit {
  subjects: Subject[] = [];
  loading = false;
  error: string | null = null;

  searchQuery = '';

  canManageSubjects = false;

  createOpen = false;
  newName = '';
  newDescription = '';
  newPoolsText = '';
  newImageUrl = '';
  newImageDesc = '';
  imageError = false;

  editOpen = false;
  editId: number | null = null;
  editName = '';
  editDescription = '';
  editImageUrl = '';
  editImageDesc = '';
  editImageError = false;

  constructor(
    private subjectsApi: SubjectService,
    private poolsApi: TopicPoolService,
    private mediaApi: MediaService,
    private keycloakOps: KeycloakOperationService,
    private authCtx: AuthContextService
  ) {}

  async ngOnInit() {
    await this.authCtx.loadMe();
    this.canManageSubjects = this.authCtx.canManage();
    this.load();
  }

  get filteredSubjects(): Subject[] {
    const q = this.searchQuery.trim().toLowerCase();
    if (!q) return this.subjects;
    return this.subjects.filter(s =>
      s.name.toLowerCase().includes(q) ||
      (s.description || '').toLowerCase().includes(q)
    );
  }

  load(): void {
    this.loading = true;
    this.error = null;
    this.subjectsApi.getAllSubjects().subscribe({
      next: (list) => {
        this.subjects = list ?? [];
        this.loading = false;
      },
      error: () => {
        this.error = 'Fächer konnten nicht geladen werden.';
        this.loading = false;
      }
    });
  }

  openCreate(): void {
    if (!this.canManageSubjects) return;
    this.createOpen = true;
  }

  cancelCreate(): void {
    this.createOpen = false;
    this.newName = '';
    this.newDescription = '';
    this.newPoolsText = '';
    this.newImageUrl = '';
    this.newImageDesc = '';
    this.imageError = false;
  }

  async submitCreate(): Promise<void> {
    if (!this.canManageSubjects) return;

    const name = this.newName.trim();
    if (!name) return;

    const description = this.newDescription || '';
    const path = this.newImageUrl.trim();
    const pools = (this.newPoolsText || '')
      .split('\n')
      .map((s) => s.trim())
      .filter(Boolean)
      .slice(0, 10);

    this.loading = true;

    try {
      let imgId: number | undefined;

      if (path) {
        const media = await firstValueFrom(
          this.mediaApi.create({
            path,
            type: 'img',
            description: this.newImageDesc || undefined
          })
        );
        imgId = media?.id;
      }

      const body: any = {
        name,
        description,
        ...(imgId !== undefined ? {imgId} : {})
      };

      const created = await firstValueFrom(this.subjectsApi.create(body));

      if (pools.length) {
        await firstValueFrom(this.poolsApi.createBatch(created.id, pools));
      }

      this.cancelCreate();
      this.load();
    } catch (err: any) {
      console.error(err);
      alert(err?.error?.message || err?.message || 'Fach konnte nicht erstellt werden.');
    } finally {
      this.loading = false;
    }
  }

  onEdit(s: Subject): void {
    if (!this.canManageSubjects) return;

    this.editId = s.id;
    this.editName = s.name;
    this.editDescription = s.description || '';
    this.editImageUrl = '';
    this.editImageDesc = '';
    this.editOpen = true;
  }

  cancelEdit(): void {
    this.editOpen = false;
    this.editId = null;
    this.editName = '';
    this.editDescription = '';
    this.editImageUrl = '';
    this.editImageDesc = '';
    this.editImageError = false;
  }

  async submitEdit(): Promise<void> {
    if (!this.canManageSubjects) return;

    if (this.editId == null) return;
    const name = this.editName.trim();
    if (!name) return;

    try {
      let imgId: number | undefined;
      const path = this.editImageUrl.trim();

      if (path) {
        const media = await firstValueFrom(
          this.mediaApi.create({
            path,
            type: 'img',
            description: this.editImageDesc || undefined
          })
        );
        imgId = media?.id;
      }

      await firstValueFrom(
        this.subjectsApi.update(this.editId, {
          name,
          description: this.editDescription || '',
          ...(imgId !== undefined ? {imgId} : {})
        })
      );

      this.cancelEdit();
      this.load();
    } catch (err: any) {
      console.error(err);
      alert(err?.error?.message || err?.message || 'Fach konnte nicht aktualisiert werden.');
    }
  }

  onDelete(s: Subject): void {
    if (!this.canManageSubjects) return;

    if (!confirm(`Fach "${s.name}" wirklich löschen?`)) return;
    this.subjectsApi.delete(s.id).subscribe({
      next: () => this.load(),
      error: (err) => alert(err?.error?.message || 'Fach konnte nicht gelöscht werden.')
    });
  }

  trackSubject = (_: number, s: Subject) => s?.id ?? -1;

  get normalizedNewImageUrl(): string {
    const p = (this.newImageUrl || '').trim();
    if (!p) return '';
    if (/^https?:\/\//i.test(p) || p.startsWith('/')) return p;
    return '/' + p;
  }

  get normalizedEditImageUrl(): string {
    const p = (this.editImageUrl || '').trim();
    if (!p) return '';
    if (/^https?:\/\//i.test(p) || p.startsWith('/')) return p;
    return '/' + p;
  }
}
