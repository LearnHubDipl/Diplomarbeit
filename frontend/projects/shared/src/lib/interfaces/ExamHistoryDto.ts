export interface ExamHistoryDto {
  id: number;
  score: number;
  questionCount: number;
  startedAt: string;
  finishedAt: string;
  subjectNames: string[];
}
