import {Component, inject, OnInit} from '@angular/core';
import {ActivatedRoute, Router, RouterLink} from '@angular/router';
import {CommonModule} from '@angular/common';
import {QuestionService} from '../../../../shared/src/lib/services/question.service';
import {Question, QuestionType} from '../../../../shared/src/lib/interfaces/question';
import {TopicPool} from '../../../../shared/src/lib/interfaces/topic-pool';
import {ReactiveFormsModule} from '@angular/forms';
import {UserInitializationService} from '../../../../shared/src/lib/services/user-initialization.service';

@Component({
  selector: 'lib-frage-card',
  templateUrl: './frage-card.component.html',
  styleUrls: ['./frage-card.component.css'],
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  standalone: true
})

export class FrageCardComponent implements OnInit {
  router = inject(Router);
  questionService = inject(QuestionService);
  route = inject(ActivatedRoute);
  userInitService = inject(UserInitializationService);

  questions: Question[] = [];
  currentIndex = 0;
  showAnswer = false;

  QuestionType = QuestionType;

  async ngOnInit() {
    const topicPoolId = Number(this.route.snapshot.queryParamMap.get('topicPoolId'));
    const isPublicParam = this.route.snapshot.queryParamMap.get('isPublic');
    const isPublic = isPublicParam === 'true';

    console.log('Loading questions - isPublic:', isPublic, 'topicPoolId:', topicPoolId);

    if (isPublic) {
      // Load public questions
      this.questionService.getAllPublicQuestions().subscribe({
        next: (data) => {
          console.log('Öffentliche Fragen vom Backend:', data);
          // Filter by topic pool
          this.questions = data.filter(q => q.topicPool?.id === topicPoolId);
          console.log('Gefilterte Fragen:', this.questions);
        },
        error: (err) => {
          console.error('Fehler beim Laden der öffentlichen Fragen:', err);
        }
      });
    } else {
      // Load private questions - need userId
      let userId: number | null = null;
      const user = this.userInitService.getCurrentUser();

      if (user) {
        userId = user.id;
      } else {
        try {
          const initializedUser = await this.userInitService.initializeUser();
          userId = initializedUser?.id || null;
        } catch (err) {
          console.error('Fehler beim Laden des Users:', err);
        }
      }

      if (userId) {
        this.questionService.getAllQuestionsFromLoggedInUser(userId).subscribe({
          next: (data) => {
            console.log('Persönliche Fragen vom Backend:', data);
            // Filter by topic pool
            this.questions = data.filter(q => q.topicPool?.id === topicPoolId);
            console.log('Gefilterte Fragen:', this.questions);
          },
          error: (err) => {
            console.error('Fehler beim Laden der persönlichen Fragen:', err);
          }
        });
      } else {
        console.error('Keine User ID verfügbar für persönliche Fragen');
      }
    }
  }

  toggleAnswer(): void {
    this.showAnswer = !this.showAnswer;
  }

  next(): void {
    if (this.currentIndex < this.questions.length - 1) {
      this.currentIndex++;
      this.showAnswer = false;
    }
  }

  prev(): void {
    if (this.currentIndex > 0) {
      this.currentIndex--;
      this.showAnswer = false;
    }
  }

  finishedLearning(): void {
    this.router.navigate(['/study/finished']);
  }

  getCorrectAnswers(question: Question): string[] {
    if (question.type !== QuestionType.MULTIPLE_CHOICE) return [];

    const correctAnswers = question.answers.filter(a => a.isCorrect).map(a => a.text);
    console.log(`Korrekte Antworten für Frage "${question.text}":`, correctAnswers);
    return correctAnswers;
  }

  getDifficultyEmoji(difficulty: number): string {
    switch (difficulty) {
      case 1: return '😃';
      case 2: return '😐';
      case 3: return '😓';
      default: return '';
    }
  }

  getDifficultyLabel(difficulty: number): string {
    switch (difficulty) {
      case 1: return 'Leicht';
      case 2: return 'Mittel';
      case 3: return 'Schwer';
      default: return '';
    }
  }
}
