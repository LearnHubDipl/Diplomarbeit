import {Subject} from './subject';

export interface TopicPool {
  id: number;
  name: string;
  description: string;
  subject?: Subject;
}
