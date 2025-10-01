export interface TopicNoteDto {
  id: number;
  title: string;
  description: string;
  createdAt: Date;
  pdfUrl: string;
  uploaderName?: string | null;
  teacherId?: number | null;
  teacherName?: string | null;
  subjectId?: number | null;
  topicPoolId?: number | null;
  fileName?: string;
  url?: string;
  publicUrl?: string;
}
