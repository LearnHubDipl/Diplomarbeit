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
  imports: [CommonModule, FormsModule, SubjectCardComponent, UploadBannerComponent],
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

  submitCreate() {
    const names = (this.newPoolsText || '')
      .split('\n')
      .map(s => s.trim())
      .filter(Boolean)
      .slice(0, 10);

    this.loading = true;
    this.subjectsApi.create({
      name: this.newName.trim(),
      description: this.newDescription || '',
      imageUrl: this.newImageUrl.trim() || undefined
    })
      .pipe(
        switchMap(created =>
          names.length ? this.poolsApi.createBatch(created.id, names) : of([])
        ),
        finalize(() => this.loading = false)
      )
      .subscribe({
        next: () => {
          this.createOpen = false;
          this.cancelCreate();   // Felder leeren
          this.load();           // Liste wirklich neu laden
        },
        error: (err) => alert(err?.error?.message || 'Anlegen fehlgeschlagen')
      });
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
}
