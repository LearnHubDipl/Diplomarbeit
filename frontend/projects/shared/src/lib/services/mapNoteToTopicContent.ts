import { TopicContent } from '../interfaces/topicContent';
import { TopicNoteDto } from '../interfaces/topicNoteDto';

export function mapNoteToTopicContent(n: Partial<TopicNoteDto> & any): TopicContent {
  const url = n.publicUrl ?? n.url ?? n.pdfUrl ?? '';
  const fileName = n.fileName ?? (url ? url.split('/').pop() : '');

  return {
    id: n.id,
    title: n.title ?? (fileName || 'PDF'),
    description: n.description ?? undefined,
    subjectId: n.subjectId ?? undefined,
    topicPoolId: n.topicPoolId ?? undefined,
    media: url ? ({ id: 0, path: url, type: 'pdf' } as any) : undefined,
    uploaderName: n.uploaderName ?? undefined,

    createdAt: n.createdAt ? new Date(n.createdAt) : undefined,
    date: n.createdAt ? new Date(n.createdAt) : (n.date ? new Date(n.date) : undefined),

    pdfUrl: url || undefined,
    approved: true,
    fileName: fileName || '',

    teacherId: n.teacherId ?? undefined,

    uploaderSub: n.uploaderSub,
    status: n.status,
  };
}
