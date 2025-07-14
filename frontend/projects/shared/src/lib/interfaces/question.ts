import {Answer} from './answer';
import {TopicPool} from './topic-pool';
import {Media} from './media';

export interface Question {
  id: number;
  text: string;
  explanation: string;
  type: number;
  difficulty: number;
  isPublic: boolean;
  answers: Answer[];
  topicPool: TopicPool;
  media: Media;
}
