import { Component, OnInit } from '@angular/core';
import { CommonModule, NgFor, NgIf } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { firstValueFrom } from 'rxjs';
import { Subject as SchoolSubject } from '../../../../shared/src/lib/interfaces/subject';
import { TopicPool } from '../../../../shared/src/lib/interfaces/topic-pool';
import { SubjectService } from '../../../../shared/src/lib/services/subject.service';
import { TopicContentsService } from '../../../../shared/src/lib/services/topic-content.service';
import { TopicPoolService } from '../../../../shared/src/lib/services/topic-pool.service';
import { TopicContent } from '../../../../shared/src/lib/interfaces/topicContent';
import { FileService, UploadPdfDto } from '../../../../shared/src/lib/services/file-service';
import { UploadPdfResult } from '../../../../shared/src/lib/interfaces/UploadPdfResult';

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
  items: TopicContent[] = [];
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
  emailModalOpen = false;
  emailTo = '';
  emailSending = false;
  emailError: string | null = null;
  private lastUploadedFileName: string | null = null;
  private lastUploadedMeta: {
    title: string;
    uploaderName?: string;
    subjectName?: string;
    topicPoolName?: string;
  } | null = null;

  constructor(
    private readonly route: ActivatedRoute,
    private readonly subjectsApi: SubjectService,
    private readonly poolsApi: TopicPoolService,
    private readonly contentsApi: TopicContentsService,
    private readonly fileService: FileService,
    private readonly sanitizer: DomSanitizer
  ) {}

  ngOnInit(): void {
    const idParam = this.route.snapshot.paramMap.get('id');
    this.subjectId = Number(idParam);
    if (Number.isNaN(this.subjectId)) {
      this.error = 'Ungültige Fach-ID.';
      return;
    }
    this.loadData();
  }


  private loadData(): void {
    this.error = null;
    this.loading = true;

    this.subjectsApi.get(this.subjectId).subscribe({
      next: (s) => {
        this.subject = s;
        this.pools = s.topicPools ?? [];
        if (!this.poolForUpload && this.pools.length > 0) {
          this.poolForUpload = (this.pools[0].id as number) ?? null;
        }
        this.loading = false;
      },
      error: () => {
        this.error = 'Fach konnte nicht geladen werden.';
        this.loading = false;
      }
    });

    this.loadContents();
  }

  loadContents(): void {
    this.contentsApi.list(this.subjectId, this.selectedPoolId).subscribe({
      next: (list) => (this.items = list ?? []),
      error: () => (this.error = 'Mitschriften konnten nicht geladen werden.')
    });
  }

  applyFilter(poolId: number | undefined): void {
    this.selectedPoolId = poolId;
    this.loadContents();
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
    this.emailModalOpen = false;
    this.emailTo = '';
    this.emailSending = false;
    this.emailError = null;
    this.lastUploadedFileName = null;
    this.lastUploadedMeta = null;
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
      console.error('Datei konnte nicht gelesen werden.');
      this.lastDataUrl = null;
    };
    reader.readAsDataURL(f);
  }

  quickCreatePool(): void {
    const name = this.newPoolName.trim();
    if (!name) return;

    this.poolsApi.createOne(this.subjectId, name).subscribe({
      next: (tp) => {
        this.pools = [...this.pools, tp];
        this.poolForUpload = (tp.id as number) ?? null;
        this.newPoolName = '';
      },
      error: () => alert('Themenpool konnte nicht erstellt werden.')
    });
  }

  async submitUpload(): Promise<void> {
    if (!this.file || !this.title.trim()) return;
    this.uploading = true;

    const tempId = Date.now();
    const tempUrl = URL.createObjectURL(this.file);
    const optimisticItem: TopicContent = {
      id: tempId as unknown as number,
      title: this.title.trim(),
      pdfUrl: tempUrl,
      uploaderName: this.uploaderName?.trim() || undefined,
      subjectId: this.subjectId,
      subjectName: this.subject?.name ?? undefined,
      topicPoolId: this.poolForUpload ?? undefined,
      topicPoolName: this.pools.find(p => (p as any)?.id === this.poolForUpload)?.name ?? undefined,
      date: new Date().toISOString() as unknown as Date
    };
    this.items = [optimisticItem, ...this.items];

    try {
      const base64Raw = this.lastDataUrl
        ? this.stripDataUrlPrefix(this.lastDataUrl)
        : await this.fileToBase64String(this.file);

      const payload: UploadPdfDto = {
        title: optimisticItem.title,
        fileName: this.ensurePdfName(this.file.name || 'upload.pdf'),
        base64: base64Raw.replace(/\r?\n/g, ''),
        subjectId: this.subjectId ?? null,
        topicPoolId: this.poolForUpload ?? null,
        uploaderUserId: this.uploaderUserId ?? null
      };

      const created: TopicContent | UploadPdfResult =
        await firstValueFrom(this.fileService.uploadPdfBase64(payload));

      let newItem: TopicContent | null = null;

      if ((created as any)?.id) {
        newItem = created as TopicContent;
      } else if ((created as any)?.contentId || (created as any)?.publicUrl) {
        const res = created as UploadPdfResult;
        newItem = {
          ...optimisticItem,
          id: (res as any).contentId ?? (optimisticItem.id as number),
          pdfUrl: (res.publicUrl ?? optimisticItem.pdfUrl) as string
        };
      }

      if (newItem) {
        this.items = this.items.map(it => it.id === optimisticItem.id ? newItem! : it);
      } else {
        this.loadContents();
      }

      this.lastUploadedFileName = payload.fileName;
      this.lastUploadedMeta = {
        title: payload.title,
        uploaderName: this.uploaderName?.trim() || undefined,
        subjectName: this.subject?.name ?? undefined,
        topicPoolName: this.pools.find(p => (p as any)?.id === this.poolForUpload)?.name ?? undefined
      };
      this.emailTo = '';
      this.emailError = null;
      this.emailModalOpen = true;

    } catch (err) {
      console.error('Upload fehlgeschlagen', err);
      this.items = this.items.filter(it => it.id !== optimisticItem.id);
      alert('Upload fehlgeschlagen. Bitte prüfen & erneut versuchen.');
      this.uploading = false;
    } finally {
      URL.revokeObjectURL(tempUrl);
    }
  }

  // ---------- Email  ----------
  private async confirmEmailSendFrontend(): Promise<void> {
    if (!this.emailTo?.trim()) {
      this.emailError = 'Bitte eine gültige E-Mail-Adresse eingeben.';
      return;
    }
    if (!this.file) {
      this.emailError = 'Keine PDF-Datei vorhanden.';
      return;
    }

    try {
      this.emailSending = true;
      this.emailError = null;
      const pdfBase64 = await this.fileToBase64String(this.file);
      const meta = {
        toEmail: this.emailTo.trim(),
        subjectLine: `Neue Mitschrift: ${this.title.trim()}`,
        textBody: this.buildPlainTextBody(),
        attachmentName: this.ensurePdfName(this.file.name || 'upload.pdf'),
        attachmentBase64: pdfBase64
      };

      const emlBlob = this.buildEmlBlob(meta);
      const emlName = this.safeFileStem(this.title || 'mitschrift') + '.eml';

      const url = URL.createObjectURL(emlBlob);
      const a = document.createElement('a');
      a.href = url;
      a.download = emlName;
      document.body.appendChild(a);
      a.click();
      document.body.removeChild(a);
      URL.revokeObjectURL(url);

      this.emailModalOpen = false;
      this.cancelUpload();
      alert('E-Mail-Entwurf (.eml) wurde erstellt. Öffne ihn in deinem Mailprogramm und klicke auf „Senden“.');
    } catch (e: any) {
      this.emailError = e?.message || 'Fehler beim Erstellen der E-Mail.';
    } finally {
      this.emailSending = false;
    }
  }
  private buildPlainTextBody(): string {
    const lines = [
      'Neue Mitschrift wurde hochgeladen.',
      '',
      `Titel: ${this.title?.trim() || '-'}`,
      this.uploaderName?.trim() ? `Uploader: ${this.uploaderName.trim()}` : '',
      this.subject?.name ? `Fach: ${this.subject.name}` : '',
      (this.pools.find(p => (p as any)?.id === this.poolForUpload)?.name) ? `Themenpool: ${this.pools.find(p => (p as any)?.id === this.poolForUpload)?.name}` : '',
      '',
      'PDF liegt als Anhang bei.'
    ].filter(Boolean);
    return lines.join('\r\n');
  }
  private buildEmlBlob(meta: {
    toEmail: string;
    subjectLine: string;
    textBody: string;
    attachmentName: string;
    attachmentBase64: string;
  }): Blob {
    const outer = '====BOUNDARY_OUTER_' + Date.now() + '====';
    const alt   = '====BOUNDARY_ALT_'   + (Date.now()+1)  + '====';

    const from = 'no-reply@learnhub.local';
    const date = new Date().toUTCString();

    const b64Chunked = this.chunk76(meta.attachmentBase64);

    const headers =
      `From: ${from}\r\n` +
      `To: ${meta.toEmail}\r\n` +
      `Subject: ${this.escapeHeader(meta.subjectLine)}\r\n` +
      `Date: ${date}\r\n` +
      `MIME-Version: 1.0\r\n` +
      `Content-Type: multipart/mixed; boundary="${outer}"\r\n\r\n`;

    const bodyAltStart =
      `--${outer}\r\n` +
      `Content-Type: multipart/alternative; boundary="${alt}"\r\n\r\n` +

      `--${alt}\r\n` +
      `Content-Type: text/plain; charset=UTF-8\r\n` +
      `Content-Transfer-Encoding: 7bit\r\n\r\n` +
      `${meta.textBody}\r\n\r\n` +

      `--${alt}--\r\n`;

    const attachmentPart =
      `--${outer}\r\n` +
      `Content-Type: application/pdf; name="${this.escapeHeader(meta.attachmentName)}"\r\n` +
      `Content-Transfer-Encoding: base64\r\n` +
      `Content-Disposition: attachment; filename="${this.escapeHeader(meta.attachmentName)}"\r\n\r\n` +
      `${b64Chunked}\r\n\r\n` +
      `--${outer}--\r\n`;

    const eml = headers + bodyAltStart + attachmentPart;
    return new Blob([eml], { type: 'message/rfc822;charset=utf-8' });
  }

  private chunk76(b64: string): string {
    return b64.replace(/(.{1,76})/g, '$1\r\n').trim();
  }

  private escapeHeader(v: string): string {
    return v.replace(/[\r\n"]/g, ' ').trim();
  }

  private safeFileStem(v: string): string {
    return (v || 'mail').toLowerCase().replace(/[^a-z0-9._-]+/g, '-').replace(/-+/g, '-').replace(/^-|-$/g, '');
  }

  private ensurePdfName(name: string): string {
    const safe = name?.trim() || 'upload.pdf';
    return safe.toLowerCase().endsWith('.pdf') ? safe : `${safe}.pdf`;
  }

  private stripDataUrlPrefix(dataUrlOrBase64: string): string {
    return dataUrlOrBase64.replace(/^data:application\/pdf;base64,/, '');
  }

  private async fileToBase64String(file: File): Promise<string> {
    const arrayBuf = await file.arrayBuffer();
    const bytes = new Uint8Array(arrayBuf);
    let binary = '';
    for (let i = 0; i < bytes.byteLength; i++) binary += String.fromCharCode(bytes[i]);
    return btoa(binary);
  }

  toSafePdfUrl(raw: string | null | undefined): SafeResourceUrl | null {
    if (!raw) return null;
    return this.sanitizer.bypassSecurityTrustResourceUrl(
      `${raw}#toolbar=0&navpanes=0&scrollbar=0`
    );
  }

  trackPool = (_: number, p: TopicPool | undefined): number =>
    (p as any)?.id ?? -1;

  trackContent = (_: number, c: TopicContent | undefined): number =>
    (c as any)?.id ?? -1;

}
