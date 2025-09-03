export interface TopicContentSlim {
  id: number;
  title: string;
  description?: string;
  subjectId: number;
  subjectName: string;
  topicPoolId?: number;
  topicPoolName?: string;
  uploaderName?: string;
  thumbnailUrl?: string;
  pdfUrl?: string;
}
