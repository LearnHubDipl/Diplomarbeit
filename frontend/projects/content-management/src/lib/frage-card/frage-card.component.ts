import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {CommonModule} from '@angular/common';
import {QuestionService} from '../../../../shared/src/lib/services/question.service';
import {Question} from '../../../../shared/src/lib/interfaces/question';
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

  constructor(
    private router: Router,
    private questionService: QuestionService,
    private route: ActivatedRoute,
  ) {
  }

  ngOnInit() {
    const topicPoolId = Number(this.route.snapshot.queryParamMap.get('topicPoolId'));
    const topicPool: TopicPool = {
      id: topicPoolId, //todo: ausgewähltes topic pool anzeigen
      name: '',
      description: '',
    };

    this.questionService.getQuestionsByTopicPool(topicPool).subscribe({
      next: (data) => {
        this.questions = data;
      }
    });
  }

  toggleAnswer(): void { //auf und zu decken von Fragen/Antworten
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

  goToNext(): void {
    this.next();
  }

  finishedLearning(): void {
    this.router.navigate(['/finished']);
  }
}
