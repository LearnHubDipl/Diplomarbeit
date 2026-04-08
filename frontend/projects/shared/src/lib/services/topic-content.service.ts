import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { map } from 'rxjs';
import { API_BASE_URL } from './globals';
import { TopicNoteDto } from '../interfaces/topicNoteDto';
import { mapNoteToTopicContent } from './mapNoteToTopicContent';
import { TopicContent } from '../interfaces/topicContent';

@Injectable({ providedIn: 'root' })
export class TopicContentService {
  private base = API_BASE_URL + '/topic-pools';

  constructor(private http: HttpClient) {}

  listNotes(topicPoolId: number) {
    return this.http
      .get<TopicNoteDto[]>(`${this.base}/${topicPoolId}/notes`)
      .pipe(map(list => list.map(mapNoteToTopicContent)));
  }

  uploadNote(
    topicPoolId: number,
    _subjectId: number,
    params: {
      file?: File | null;
      title: string;
      description?: string;
      uploaderName?: string;
      teacherEmail?: string;
      replaceFileName?: string;
    }
  ) {
    const fd = new FormData();

    if (params.file) {
      fd.append('file', params.file);
      fd.append('filename', params.file.name);
    }

    if (params.title)           fd.append('title', params.title);
    if (params.description)     fd.append('description', params.description);
    if (params.uploaderName)    fd.append('uploaderName', params.uploaderName);
    if (params.teacherEmail)    fd.append('teacherEmail', params.teacherEmail);
    if (params.replaceFileName) fd.append('replaceFileName', params.replaceFileName);

    return this.http
      .post<TopicNoteDto>(`${this.base}/${topicPoolId}/notes/upload`, fd)
      .pipe(map(mapNoteToTopicContent));
  }

  deleteNote(topicPoolId: number, fileName: string) {
    return this.http.delete<void>(`${this.base}/${topicPoolId}/notes/${encodeURIComponent(fileName)}`);
  }
}
