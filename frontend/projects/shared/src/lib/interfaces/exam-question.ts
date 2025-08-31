import {Question} from './question';

export interface ExamQuestion {
  id: number;
  question: Question;
  freeTextAnswer?: string;
  isCorrect: boolean;
  selectedAnswers?: {
    id: number;
    text: string;
  }[],
  correctAnswerIds?: number[];
  correctFreeTextAnswers?: string[];
}
