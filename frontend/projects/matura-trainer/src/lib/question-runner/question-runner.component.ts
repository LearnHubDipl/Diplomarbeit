import {Component, inject, OnInit} from '@angular/core';
import { Question } from '../../../../shared/src/lib/interfaces/question';
import { QuestionService } from '../../../../shared/src/lib/services/question.service';
import {NgForOf} from '@angular/common';


@Component({
  selector: 'lib-question-runner',
  imports: [
    NgForOf
  ],
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
