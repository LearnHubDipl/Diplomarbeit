import { Component, inject, OnInit } from '@angular/core';
import { QuestionService } from '../../../../shared/src/lib/services/question.service';
import { Router, RouterLink } from '@angular/router';
import { TopicPool } from '../../../../shared/src/lib/interfaces/topic-pool';
import { Question } from '../../../../shared/src/lib/interfaces/question';
import { NgClass, NgForOf, NgIf } from '@angular/common';
import { SubjectService } from '../../../../shared/src/lib/services/subject.service';
import { Subject } from '../../../../shared/src/lib/interfaces/subject';

@Component({
  selector: 'lib-question-manager',
  standalone: true,
  imports: [
    NgClass,
    NgForOf,
    NgIf,
    RouterLink
  ],
  templateUrl: './question-manager.component.html',
  styleUrl: './question-manager.component.css'
})
export class QuestionManagerComponent implements OnInit {

  private subjectService = inject(SubjectService);
  private questionService = inject(QuestionService);
  private router = inject(Router);

  subjects: Subject[] = [];
  openSubjectId: number | null = null;
  openPoolId: number | null = null;

  questionsByPool: { [poolId: number]: Question[] } = {};
  questionLoadErrorByPool: { [poolId: number]: boolean } = {};

  loadError = false;
  errorMessage = '';

  ngOnInit(): void {
    this.subjectService.getAllSubjects().subscribe({
      next: (data) => {
        this.subjects = data;
      },
      error: (err) => {
        console.error('Fehler beim Laden der Fächer', err);
        this.loadError = true;
        this.errorMessage = 'Fragen konnten nicht geladen werden';
      }
    });
  }

  toggleSubject(subjectId: number): void {
    this.openSubjectId = this.openSubjectId === subjectId ? null : subjectId;
  }

  togglePool(pool: TopicPool): void {
    if (this.openPoolId === pool.id) {
      this.openPoolId = null;
      return;
    }

    this.openPoolId = pool.id;

    if (!this.questionsByPool[pool.id] && !this.questionLoadErrorByPool[pool.id]) {
      this.questionService.getQuestionsByTopicPool(pool).subscribe({
        next: (questions) => {
          this.questionsByPool[pool.id] = questions;
        },
        error: (err) => {
          console.error('Fehler beim Laden der Fragen', err);
          this.questionLoadErrorByPool[pool.id] = true;
        }
      });
    }
  }

  getFirstCharacters(text: string): string {
    const characterLength = 30;
    if (!text) return '';
    return text.length > characterLength
      ? text.slice(0, characterLength) + '...'
      : text;
  }

  deleteQuestion(question: Question, poolId: number): void {
    if (confirm('Wirklich löschen?')) {
      this.questionService.deleteQuestion(question.id).subscribe({
        next: () => {
          this.questionsByPool[poolId] =
            this.questionsByPool[poolId].filter(q => q.id !== question.id);
        },
        error: (err) => console.error('Fehler beim Löschen', err)
      });
    }
  }

  editQuestion(question: Question): void {
    this.router.navigate(['/questions/edit', question.id]);
  }
}
