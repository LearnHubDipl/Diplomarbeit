import { TopicContent } from '../interfaces/topicContent';
import { TopicNoteDto } from '../interfaces/topicNoteDto';

export function mapNoteToTopicContent(dto: TopicNoteDto): TopicContent {
  // Fallbacks damit Frontend-Logik zuverlässig klappt:
  const approved =
    (dto as any).approved === true ||
    (dto as any).isApproved === true ||
    (dto as any).is_approved === true;

  const status =
    (dto as any).status ||
    (approved ? 'APPROVED' : 'PENDING');

  return {
    ...(dto as any),

    fileName: (dto as any).fileName ?? (dto as any).filename ?? '',
    pdfUrl: (dto as any).pdfUrl ?? (dto as any).publicUrl ?? '',

    title: (dto as any).title ?? '',
    description: (dto as any).description ?? '',

    uploaderName: (dto as any).uploaderName ?? '',
    uploaderSub: (dto as any).uploaderSub ?? (dto as any).uploaderKeycloakSub ?? (dto as any).ownerSub ?? '',

    teacherId: (dto as any).teacherId ?? null,

    approved,
    status,

    createdAt: (dto as any).createdAt ?? (dto as any).date ?? undefined,
  } as any;
}
