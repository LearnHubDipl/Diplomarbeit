import {Answer, AnswerUpdateRequest} from './answer';
import {TopicPool} from './topic-pool';
import {Media} from './media';
import {Solution} from './solution';
import {UserSlim} from './userSlim';

export enum QuestionType {
  FREETEXT = 'FREETEXT',
  MULTIPLE_CHOICE = 'MULTIPLE_CHOICE'
}

export interface Question {
  id: number;
  text: string;
  explanation: string;
  type: QuestionType;
  difficulty: number;
  isPublic: boolean;
  approvalRequested?: boolean;
  answers: Answer[];
  topicPool: TopicPool;
  media: Media;
  solutions: Solution[];
  user?: UserSlim;
}

export interface QuestionUpdateRequest{
  text?: string;
  explanation?: string;
  type?: QuestionType;
  answers?: AnswerUpdateRequest[];
  isPublic?: boolean;
  approvalRequested?: boolean;
}
