import {TopicPool} from './topic-pool';
import {ExamQuestion} from './exam-question';

export interface Exam {
  id: number;
  timeLimit: number;
  startedAt: Date;
  finishedAt: Date;
  questionCount: number;
  score: number;
  // user: User;
  topicPools: TopicPool[];
  questions: ExamQuestion[];
}
