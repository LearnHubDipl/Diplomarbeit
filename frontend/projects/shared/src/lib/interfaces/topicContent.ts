import { Subject } from './subject';
import { TopicPool } from './topic-pool';
import { Media } from './media';

export interface TopicContent {
  id: number;
  title: string;
  description?: string;
  subjectId?: number;
  subjectName?: string;
  topicPoolId?: number;
  topicPoolName?: string;
  subject?: Subject;
  topicPool?: TopicPool;
  media?: Media;
  uploaderName?: string;
  date?: Date;
  createdAt?: Date;
  thumbnailUrl?: string;
  pdfUrl?: string;
  approved?: boolean;
  fileName: string;
  teacherId?: number;
}
