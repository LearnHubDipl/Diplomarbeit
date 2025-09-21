import {Media} from './media';

export interface UserSlim {
  id: number;
  name: string;
  email: string;
  isTeacher: boolean;
  profilePicture: Media;
}
