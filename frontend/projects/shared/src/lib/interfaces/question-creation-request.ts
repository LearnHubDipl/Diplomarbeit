import { QuestionType } from './question';

export interface QuestionRequest {
  text: string;
  explanation: string;
  type: QuestionType;
  difficulty: number;
  isPublic: boolean;
  userId: number;
  topicPoolId: number;
  answers: AnswerCreationRequest[];
}

export interface AnswerCreationRequest {
  text: string;
  isCorrect: boolean;
}
