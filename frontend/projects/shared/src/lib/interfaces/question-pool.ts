import {Question} from './question';

export interface QuestionPool {
  // user: User;
  id: number;
  entries: QuestionPoolEntry[];
}
export interface QuestionPoolEntry {
  id: number;
  answeredAt: Date;
  lastAnsweredCorrectly: boolean;
  correctCount: number;
  question: Question;
}


export interface QuestionPoolEntryRequest {
  userId: number;
  questionIds: number[];
}
