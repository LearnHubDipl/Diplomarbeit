import { Media } from './media';

export interface UserSlim {
  id: number;
  keycloakSub: string;
  name: string;
  email: string;
  isTeacher: boolean;
  isAdmin?: boolean;
  profilePicture?: Media;
  className?: string;
}
