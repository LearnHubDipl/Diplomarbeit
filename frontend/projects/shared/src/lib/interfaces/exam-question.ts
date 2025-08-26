import {Question} from './question';

export interface ExamQuestion {
  id: number;
  question: Question;
  freetextAnswer?: string;
  isCorrect: boolean;
  selectedAnswers?: {
    id: number;
    text: string;
  }[]
}
