import {Answer} from './answer';
import {TopicPool} from './topic-pool';
import {Media} from './media';
import {Solution} from './solution';

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
  answers: Answer[];
  topicPool: TopicPool;
  media: Media;
  solutions: Solution[];
}
