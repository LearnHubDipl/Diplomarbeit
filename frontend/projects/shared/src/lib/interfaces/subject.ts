import {Media} from './media';
import {TopicPool} from './topic-pool';

export interface Subject {
  id: number;
  name: string;
  description: string;
  img: Media;
  topicPools: TopicPool[];
}
