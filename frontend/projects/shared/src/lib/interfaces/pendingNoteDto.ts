export interface PendingNoteDto {
  topicPoolId: number;
  fileName: string;

  title?: string;
  uploaderName?: string;
  uploaderSub?: string;

  teacherId?: number;

  createdAt?: number;
  status?: 'PENDING' | 'APPROVED' | string;
  approved?: boolean;

  publicUrl?: string;

}
