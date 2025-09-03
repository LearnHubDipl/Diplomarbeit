import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {CommonModule} from '@angular/common';
import {QuestionService} from '../../../../shared/src/lib/services/question.service';
import {Question, QuestionType} from '../../../../shared/src/lib/interfaces/question';
import {TopicPool} from '../../../../shared/src/lib/interfaces/topic-pool';
import {ReactiveFormsModule} from '@angular/forms';

@Component({
  selector: 'lib-frage-card',
  templateUrl: './frage-card.component.html',
  styleUrls: ['./frage-card.component.css'],
  imports: [CommonModule, ReactiveFormsModule],
  standalone: true
})

export class FrageCardComponent implements OnInit {
  questions: Question[] = [];
  currentIndex = 0;
  showAnswer = false;

  QuestionType = QuestionType;

  constructor(
    private router: Router,
    private questionService: QuestionService,
    private route: ActivatedRoute,
  ) {}

  ngOnInit() {
    const topicPoolId = Number(this.route.snapshot.queryParamMap.get('topicPoolId'));
    const topicPool: TopicPool = {
      id: topicPoolId,
      name: '',
      description: '',
    };

    this.questionService.getQuestionsByTopicPool(topicPool).subscribe({
      next: (data) => {
        console.log('Fragen vom Backend:', data); // <-- Logging
        this.questions = data;
      },
      error: (err) => {
        console.error('Fehler beim Laden der Fragen:', err);
      }
    });
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
    this.router.navigate(['/finished']);
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
