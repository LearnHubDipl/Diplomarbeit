import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { Subject } from '../../../../shared/src/lib/interfaces/subject';
import { SubjectService } from '../../../../shared/src/lib/services/subject.service';
import { TopicPoolService } from '../../../../shared/src/lib/services/topic-pool.service';


import { SubjectCardComponent } from '../subject-card/subject-card.component';
import { UploadBannerComponent } from '../upload-banner/upload-banner.component';
import {MediaService} from '../../../../shared/src/lib/services/media-service';
import {finalize, firstValueFrom, of, switchMap} from 'rxjs';

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

  createOpen = false;
  newName = '';
  newDescription = '';
  newPoolsText = '';
  newImageUrl = '';
  newImageDesc = '';

  editOpen = false;
  editId: number | null = null;
  editName = '';
  editDescription = '';
  editImageUrl = '';
  editImageDesc = '';
  imageError = false;

  constructor(
    private subjectsApi: SubjectService,
    private poolsApi: TopicPoolService,
    private mediaApi: MediaService
  ) {}

  ngOnInit(): void { this.load(); }

  load(): void {
    this.loading = true; this.error = null;
    this.subjectsApi.getAllSubjects().subscribe({
      next: (list) => { this.subjects = list ?? []; this.loading = false; },
      error: () => { this.error = 'Fächer konnten nicht geladen werden.'; this.loading = false; }
    });
  }

  openCreate(): void { this.createOpen = true; }
  cancelCreate(): void {
    this.createOpen = false;
    this.newName = ''; this.newDescription = '';
    this.newPoolsText = ''; this.newImageUrl = ''; this.newImageDesc = '';
  }

  async submitCreate(): Promise<void> {
    const name = this.newName.trim();
    if (!name) return;

    const description = this.newDescription || '';
    const path = this.newImageUrl.trim();
    const pools = (this.newPoolsText || '')
      .split('\n')
      .map(s => s.trim())
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
        description
      };
      if (imgId) {
        body.img = { id: imgId };
      }

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
    this.editName = ''; this.editDescription = '';
    this.editImageUrl = ''; this.editImageDesc = '';
  }
  async submitEdit(): Promise<void> {
    if (this.editId == null) return;
    const name = this.editName.trim();
    if (!name) return;

    try {
      let imgId: number | undefined;
      const path = this.editImageUrl.trim();
      if (path) {
        const media = await firstValueFrom(
          this.mediaApi.create({path, type: 'img', description: this.editImageDesc || undefined})
        );
        imgId = media?.id;
      }

      await firstValueFrom(this.subjectsApi.update(this.editId, {
        name,
        description: this.editDescription || '',
        ...(imgId !== undefined ? {imgId} : {})
      }));

      this.cancelEdit();
      this.load();
    } catch (err: any) {
      console.error(err);
      alert(err?.error?.message || err?.message || 'Fach konnte nicht aktualisiert werden.');
    }
  }

  onDelete(s: Subject): void {
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

}
