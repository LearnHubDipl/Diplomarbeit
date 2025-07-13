import {Component, inject, OnInit} from '@angular/core';
import {QuestionService} from '../services/question.service';
import {Question} from '../interfaces/question';

@Component({
  selector: 'lib-question-runner',
  imports: [],
  templateUrl: './question-runner.component.html',
  styleUrl: './question-runner.component.css'
})
export class QuestionRunnerComponent implements OnInit {
  questionService: QuestionService = inject(QuestionService);
  question: Question | undefined;

  ngOnInit() {
    this.questionService.getQuestionById(3).subscribe(q => {console.log(q); this.question = q;});
  }
}
