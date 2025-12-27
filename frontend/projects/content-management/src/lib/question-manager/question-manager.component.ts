import { Component, inject, OnInit } from '@angular/core';
import { QuestionService } from '../../../../shared/src/lib/services/question.service';
import { Router, RouterLink } from '@angular/router';
import { TopicPool } from '../../../../shared/src/lib/interfaces/topic-pool';
import { Question } from '../../../../shared/src/lib/interfaces/question';
import { NgClass, NgForOf, NgIf } from '@angular/common';
import { SubjectService } from '../../../../shared/src/lib/services/subject.service';
import { Subject } from '../../../../shared/src/lib/interfaces/subject';
import { UserInitializationService } from '../../../../shared/src/lib/services/user-initialization.service';

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
  private userInitService = inject(UserInitializationService);
  private router = inject(Router);

  subjects: Subject[] = [];
  filteredSubjects: Subject[] = [];
  openSubjectId: number | null = null;
  openPoolId: number | null = null;

  questionsByPool: { [poolId: number]: Question[] } = {};
  questionLoadErrorByPool: { [poolId: number]: boolean } = {};

  isPublicMode = false;
  isAdmin = false;
  userId: number | null = null;

  allQuestions: Question[] = [];
  loadError = false;
  errorMessage = '';
  noQuestionsFound = false;

  async ngOnInit() {
    await this.loadCurrentUser();
    this.loadSubjects();
  }

  private async loadCurrentUser() {
    const user = this.userInitService.getCurrentUser();

    if (user?.id) {
      this.userId = user.id;
      this.isAdmin = user.isAdmin || false;
    } else {
      try {
        const initializedUser = await this.userInitService.initializeUser();
        if (initializedUser) {
          this.userId = initializedUser.id;
          this.isAdmin = initializedUser.isAdmin || false;
        }
      } catch (err) {
        console.error('Fehler beim Laden des Users:', err);
      }
    }
  }

  toggleMode(): void {
    this.isPublicMode = !this.isPublicMode;
    this.loadQuestions();
  }

  private loadSubjects(): void {
    this.subjectService.getAllSubjects().subscribe({
      next: (data) => {
        this.subjects = data;
        this.loadQuestions();
      },
      error: (err) => {
        console.error('Fehler beim Laden der Fächer', err);
        this.loadError = true;
        this.errorMessage = 'Fächer konnten nicht geladen werden';
      }
    });
  }

  loadQuestions(): void {
    this.allQuestions = [];
    this.filteredSubjects = [];
    this.questionsByPool = {};
    this.noQuestionsFound = false;

    const observable = this.isPublicMode
      ? this.questionService.getAllPublicQuestions()
      : this.userId
        ? this.questionService.getAllQuestionsFromLoggedInUser(this.userId)
        : null;

    if (!observable) {
      this.noQuestionsFound = true;
      return;
    }

    observable.subscribe({
      next: (questions) => {
        this.allQuestions = questions;

        if (questions.length === 0) {
          this.noQuestionsFound = true;
          return;
        }

        this.groupQuestionsByPool(questions);
        this.filterSubjectsWithQuestions();
      },
      error: (err) => {
        console.error('Fehler beim Laden der Fragen:', err);
        this.loadError = true;
        this.errorMessage = 'Fragen konnten nicht geladen werden';
      }
    });
  }

  private groupQuestionsByPool(questions: Question[]): void {
    questions.forEach(question => {
      const poolId = question.topicPool?.id;
      if (poolId) {
        if (!this.questionsByPool[poolId]) {
          this.questionsByPool[poolId] = [];
        }
        this.questionsByPool[poolId].push(question);
      }
    });
  }

  private filterSubjectsWithQuestions(): void {
    this.filteredSubjects = this.subjects
      .map(subject => {
        const filteredPools = subject.topicPools?.filter(pool =>
          this.questionsByPool[pool.id]?.length > 0
        ) || [];

        return {
          ...subject,
          topicPools: filteredPools
        };
      })
      .filter(subject => subject.topicPools.length > 0);
  }

  toggleSubject(subjectId: number): void {
    this.openSubjectId = this.openSubjectId === subjectId ? null : subjectId;
  }

  togglePool(pool: TopicPool): void {
    this.openPoolId = this.openPoolId === pool.id ? null : pool.id;
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

          if (this.questionsByPool[poolId].length === 0) {
            this.loadQuestions();
          }
        },
        error: (err) => {
          console.error('Fehler beim Löschen', err);
          if (err.status === 403) {
            alert(err.error?.error || 'Du hast keine Berechtigung diese Frage zu löschen.');
          } else {
            alert('Fehler beim Löschen der Frage.');
          }
        }
      });
    }
  }

  editQuestion(question: Question): void {
    this.router.navigate(['/questions/edit', question.id]);
  }
}
