import {Component, OnInit} from '@angular/core';
import {CommonModule, NgFor, NgIf} from '@angular/common';
import {FormsModule} from '@angular/forms';
import {ActivatedRoute, Router, RouterModule} from '@angular/router';
import {DomSanitizer, SafeResourceUrl} from '@angular/platform-browser';

import {Subject as SchoolSubject} from '../../../../shared/src/lib/interfaces/subject';
import {TopicPool} from '../../../../shared/src/lib/interfaces/topic-pool';

import {SubjectService} from '../../../../shared/src/lib/services/subject.service';
import {TopicPoolService} from '../../../../shared/src/lib/services/topic-pool.service';
import {TopicContentsService} from '../../../../shared/src/lib/services/topic-content.service';
import {FileService} from '../../../../shared/src/lib/services/file-service';
import {finalize} from 'rxjs';
import { HttpErrorResponse } from '@angular/common/http';

@Component({
  selector: 'lib-subject-detail',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule, NgIf, NgFor],
  templateUrl: './subject-detail.component.html'
})
export class SubjectDetailComponent implements OnInit {
  subjectId!: number;
  subject?: SchoolSubject;
  pools: TopicPool[] = [];
  selectedPoolId?: number;

  uploadOpen = false;
  title = '';
  uploaderName = '';
  file: File | null = null;
  poolForUpload: number | null = null;
  newPoolName = '';
  loading = false;
  uploading = false;
  error: string | null = null;
  previewSrc: SafeResourceUrl | null = null;
  private lastDataUrl: string | null = null;
  uploaderUserId: number | null = null;

  constructor(
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly subjectsApi: SubjectService,
    private readonly poolsApi: TopicPoolService,
    private readonly contentsApi: TopicContentsService,
    private readonly fileService: FileService,
    private readonly sanitizer: DomSanitizer
  ) {
  }

  ngOnInit(): void {
    this.route.paramMap.subscribe(pm => {
      const idParam = pm.get('id');
      const poolParam = pm.get('poolId');
      this.subjectId = Number(idParam);
      this.selectedPoolId = poolParam ? Number(poolParam) : undefined;

      if (Number.isNaN(this.subjectId)) {
        this.error = 'Ungültige Fach-ID.';
        return;
      }
      this.loadData();
    });
  }


  applyFilter(poolId: number | undefined): void {
    this.selectedPoolId = poolId;
    if (poolId) {
      this.router.navigate(['/subjects', this.subjectId, 'pools', poolId]);
    } else {
      this.router.navigate(['/subjects', this.subjectId]);
    }
  }

  openUpload(): void {
    this.uploadOpen = true;
  }

  cancelUpload(): void {
    this.uploadOpen = false;
    this.uploading = false;
    this.title = '';
    this.uploaderName = '';
    this.file = null;
    this.poolForUpload = this.poolForUpload ?? (this.pools[0]?.id as number) ?? null;
    this.newPoolName = '';
    this.previewSrc = null;
    this.lastDataUrl = null;
  }

  onFile(ev: Event): void {
    const input = ev.target as HTMLInputElement;
    const f = input.files?.[0] ?? null;
    this.file = f;
    this.previewSrc = null;
    this.lastDataUrl = null;
    if (!f) return;
    const blobUrl = URL.createObjectURL(f);
    this.previewSrc = this.sanitizer.bypassSecurityTrustResourceUrl(blobUrl);
    const reader = new FileReader();
    reader.onload = () => (this.lastDataUrl = (reader.result as string) ?? null);
    reader.onerror = () => {
      this.lastDataUrl = null;
    };
    reader.readAsDataURL(f);
  }

  toSafePdfUrl(raw: string | null | undefined): SafeResourceUrl | null {
    if (!raw) return null;
    return this.sanitizer.bypassSecurityTrustResourceUrl(`${raw}#toolbar=0&navpanes=0&scrollbar=0`);
  }

  getPoolId(p: any): number | undefined {
    return p?.id ?? p?.poolId ?? p?.topicPoolId ?? p?.contentId ?? p?.topic_pool_id;
  }

  getPoolName(p: any): string {
    return p?.name ?? p?.poolName ?? p?.topicPoolName ?? p?.title ?? 'Unbenannt';
  }

  getPoolNameById(id?: number): string {
    if (!id) return '';
    const found = this.pools.find(x => this.getPoolId(x) === id);
    return this.getPoolName(found);
  }

  trackPool = (_: number, p: any) => this.getPoolId(p) ?? -1;

  goToPoolAny(p: TopicPool) {
    this.router.navigate(['/subjects', this.subjectId, 'pools', p.id]);
  }


  submitUpload(): void {
    alert('Upload wird später implementiert');
  }

  private reloadPools(): void {
    const url = `${/* exakt */ ''}`;
    this.poolsApi.getTopicPoolsBySubject(this.subjectId).subscribe({
      next: (list) => {
        console.log('[reloadPools] received', list);
        this.pools = list ?? [];
      },
      error: (e) => {
        console.error('[reloadPools] ERROR', e);
        this.error = 'Themenpools konnten nicht geladen werden.';
      }
    });
  }


  private loadData(): void {
    this.error = null;
    this.loading = true;

    this.subjectsApi.get(this.subjectId).subscribe({
      next: (s) => {
        this.subject = s;
        this.loading = false;
        this.reloadPools(); // <- WICHTIG: Pools immer aus eigener Quelle
        if (!this.poolForUpload) {
          this.poolForUpload = this.pools?.[0]?.id ?? null;
        }
      },
      error: () => {
        this.error = 'Fach konnte nicht geladen werden.';
        this.loading = false;
      }
    });
  }

  quickCreatePool(): void {
    const name = this.newPoolName.trim();
    if (!name) return;
    this.poolsApi.createOne(this.subjectId, name).subscribe({
      next: () => {
        this.newPoolName = '';
        this.reloadPools();
      },
      error: () => alert('Themenpool konnte nicht erstellt werden.')
    });
  }

  renamingId?: number;
  deletingId?: number;

  renamePool(p: TopicPool, ev?: MouseEvent) {
    ev?.stopPropagation();
    const current = p.name ?? '';
    const name = prompt('Neuer Name für Themenpool:', current)?.trim();
    if (!name || name === current) return;

    if (this.renamingId === p.id) return; // block double fire
    this.renamingId = p.id;

    // optimistic
    const idx = this.pools.findIndex(x => x.id === p.id);
    const old = {...this.pools[idx]};
    const updatedLocal = {...old, name};
    this.pools = [
      ...this.pools.slice(0, idx),
      updatedLocal,
      ...this.pools.slice(idx + 1)
    ];

    this.poolsApi.updateOne(this.subjectId, p.id, {name})
      .subscribe({
        next: (server) => {
          // Übernehme Server-Wahrheit (trim etc.), kein reloadPools!
          const fixed = {...updatedLocal, ...server};
          this.pools = [
            ...this.pools.slice(0, idx),
            fixed,
            ...this.pools.slice(idx + 1)
          ];
          this.renamingId = undefined;
        },
        error: (err: { url: any; status: any; error: any; }) => {
          // Rollback
          this.pools = [
            ...this.pools.slice(0, idx),
            old,
            ...this.pools.slice(idx + 1)
          ];
          this.renamingId = undefined;
          console.error('Rename error detail:', {url: err.url, status: err.status, body: err.error});
          alert('Themenpool konnte nicht umbenannt werden.');
        }
      });
  }

  deletePool(p: TopicPool, ev?: MouseEvent) {
    ev?.stopPropagation();
    if (!confirm(`Themenpool "${p.name}" wirklich löschen?`)) return;
    if (this.deletingId === p.id) return;

    this.deletingId = p.id;

    // optimistic remove
    const idx = this.pools.findIndex(x => x.id === p.id);
    const removed = this.pools[idx];
    this.pools = [...this.pools.slice(0, idx), ...this.pools.slice(idx + 1)];
    if (this.selectedPoolId === p.id) this.selectedPoolId = undefined;

    this.poolsApi.deleteOne(this.subjectId, p.id)
      .subscribe({
        next: () => {
          this.deletingId = undefined;
        },
        error: (err) => {
          // Rollback
          this.pools = [
            ...this.pools.slice(0, idx),
            removed,
            ...this.pools.slice(idx + 1)
          ];
          this.deletingId = undefined;
          console.error('Delete error detail:', {url: err.url, status: err.status, body: err.error});
          alert('Themenpool konnte nicht gelöscht werden.');
        }
      });
  }
}
