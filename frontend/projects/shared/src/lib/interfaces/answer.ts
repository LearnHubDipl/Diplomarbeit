export interface Answer {
  id: number;
  text: string;
  isCorrect: boolean;
}

export interface CheckAnswerRequest {
  questionId: number;
  selectedAnswerIds: number[] | null,
  freeTextAnswer: string | null;
}

export interface CheckAnswerResponse {
  questionId: number;
  correct: boolean;
  correctAnswerIds: number[] | null,
  correctFreeTextAnswers: string[] | null;
  explanation: string;
}
