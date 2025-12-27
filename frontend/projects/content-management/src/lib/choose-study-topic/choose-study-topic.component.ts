import {Component, OnInit, inject} from '@angular/core';
import {SubjectService} from '../../../../shared/src/lib/services/subject.service';
import {Subject} from '../../../../shared/src/lib/interfaces/subject';
import {TopicPool} from '../../../../shared/src/lib/interfaces/topic-pool';
import {Router, RouterLink} from '@angular/router';
import {NgForOf, NgClass, NgIf} from '@angular/common';
import {QuestionService} from '../../../../shared/src/lib/services/question.service';
import {UserInitializationService} from '../../../../shared/src/lib/services/user-initialization.service';
import {forkJoin} from 'rxjs';

@Component({
  selector: 'lib-choose-study-topic',
  standalone: true,
  imports: [NgForOf, NgClass, RouterLink, NgIf],
  templateUrl: './choose-study-topic.component.html',
  styleUrls: ['./choose-study-topic.component.css']
})
export class ChooseStudyTopicComponent implements OnInit {
  private subjectService = inject(SubjectService);
  private questionService = inject(QuestionService);
  private userInitService = inject(UserInitializationService);
  private router = inject(Router);

  subjects: Subject[] = [];
  openSubjectId: number | null = null;
  isPublicMode = true; // true = Öffentlich, false = Persönlich
  userId: number | null = null;

  // Track which pools have questions
  poolsWithQuestions = new Set<number>();

  loadError = false;
  errorMessage = '';

  async ngOnInit(): Promise<void> {
    this.loadError = false;
    this.errorMessage = '';

    const user = this.userInitService.getCurrentUser();
    if (user) {
      this.userId = user.id;
    } else {
      try {
        const initializedUser = await this.userInitService.initializeUser();
        this.userId = initializedUser?.id || null;
      } catch (err) {
        console.error('Fehler beim Laden des Users:', err);
      }
    }

    this.subjectService.getAllSubjects().subscribe({
      next: (data) => {
        this.subjects = data;
        this.checkQuestionsAvailability();
      },
      error: (err) => {
        console.error('Fehler beim Laden der Fächer:', err);
        this.loadError = true;

        if (err.status === 0) {
          this.errorMessage = 'Backend nicht erreichbar. Bitte starte den Server.';
        } else {
          this.errorMessage = 'Unbekannter Fehler beim Laden der Fächer.';
        }
      }
    });
  }

  toggleMode(): void {
    this.isPublicMode = !this.isPublicMode;
    this.checkQuestionsAvailability();
  }

  checkQuestionsAvailability(): void {
    this.poolsWithQuestions.clear();

    const observable = this.isPublicMode
      ? this.questionService.getAllPublicQuestions()
      : this.userId
        ? this.questionService.getAllQuestionsFromLoggedInUser(this.userId)
        : null;

    if (!observable) return;

    observable.subscribe({
      next: (questions) => {
        questions.forEach(q => {
          if (q.topicPool?.id) {
            this.poolsWithQuestions.add(q.topicPool.id);
          }
        });
      },
      error: (err) => {
        console.error('Fehler beim Laden der Fragen:', err);
      }
    });
  }

  hasQuestions(poolId: number): boolean {
    return this.poolsWithQuestions.has(poolId);
  }

  toggleSubject(id: number): void {
    this.openSubjectId = this.openSubjectId === id ? null : id;
  }

  navigateToTopicPool(pool: TopicPool): void {
    if (!this.hasQuestions(pool.id)) return;

    this.router.navigate([`/study/card/${pool.id}`], {
      queryParams: {
        topicPoolId: pool.id,
        isPublic: this.isPublicMode
      }
    });
  }

  navigateToFragenkonfigurator(subject: Subject, pool: TopicPool): void {
    this.router.navigate(['/questions/new'], {
      queryParams: {
        subjectId: subject.id,
        topicPoolId: pool.id,
        fromSubjectSelection: true
      }
    });
  }
}
